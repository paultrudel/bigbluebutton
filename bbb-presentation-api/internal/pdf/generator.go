package pdf

import (
	"context"
	"fmt"
	"log/slog"
	"os"
	"os/exec"
	"path/filepath"
	"strconv"
	"time"

	"github.com/bigbluebutton/bigbluebutton/bbb-presentation-api/internal/config"
	"github.com/bigbluebutton/bigbluebutton/bbb-presentation-api/internal/pipeline"
	"github.com/bigbluebutton/bigbluebutton/bbb-presentation-api/internal/presentation"
)

type DownloadMarkerGenerator struct{}

func (g *DownloadMarkerGenerator) Generate(msg pipeline.Message[*FileToProcess]) (pipeline.Message[*FileToProcess], error) {
	err := presentation.MakeFileDownloadable(msg.Payload.ID, msg.Payload.File)
	if err != nil {
		return pipeline.Message[*FileToProcess]{}, err
	}
	ftp := &FileToProcess{
		ID:             msg.Payload.ID,
		File:           msg.Payload.File,
		IsDownloadable: msg.Payload.IsDownloadable,
	}
	return pipeline.NewMessageWithContext(ftp, msg.Context()), nil
}

type PageGenerator struct {
	processor presentation.PageProcessor
}

func (g *PageGenerator) Generate(msg pipeline.Message[*FileToProcess]) (pipeline.Message[*FileWithPages], error) {
	inFile := msg.Payload.File
	numPages, err := g.processor.CountPages(inFile)
	if err != nil {
		return pipeline.Message[*FileWithPages]{}, fmt.Errorf("failed to extract pages: %w", err)
	}

	cfg, err := pipeline.ContextValue[*config.Config](msg.Context(), presentation.ConfigKey)
	if err != nil {
		return pipeline.Message[*FileWithPages]{}, fmt.Errorf("could not load the required configuration: %w", err)
	}

	fwp := &FileWithPages{
		ID:    msg.Payload.ID,
		File:  inFile,
		Pages: make([]*Page, 0),
	}

	for p := 0; p < numPages; p++ {
		dir := filepath.Dir(inFile)
		outFile := fmt.Sprintf("%s%cpage-%d.pdf", dir, os.PathSeparator, p)
		extFile := fmt.Sprintf("%s%cextracted-%d.pdf", dir, os.PathSeparator, p)

		if extErr := g.processor.ExtractPage(inFile, extFile, p); extErr != nil {
			slog.Error("Failed to extract page", "error", extErr)
			os.Remove(extFile)
			continue
		}

		fileInfo, statErr := os.Stat(extFile)
		if statErr != nil {
			slog.Error("Could not determine the file size", "error", statErr)
			os.Remove(extFile)
			continue
		}

		if fileInfo.Size() > cfg.Processing.PDF.Page.MaxSize {
			dsFile := fmt.Sprintf("%s%cdownscaled-%d.pdf", dir, os.PathListSeparator, p)
			if dsErr := g.processor.DownscalePage(extFile, dsFile); dsErr != nil {
				slog.Error("Failed to downscale page", "error", dsErr)
			} else {
				os.Rename(dsFile, outFile)
			}

			os.Remove(extFile)
			os.Remove(dsFile)
		} else {
			os.Rename(extFile, outFile)
		}

		page := &Page{
			ParentFile: inFile,
			File:       outFile,
			Num:        p,
		}

		fwp.Pages = append(fwp.Pages, page)
	}

	return pipeline.NewMessageWithContext(fwp, msg.Context()), nil
}

type ThumbnailGenerator struct {
	exec func(ctx context.Context, name string, args ...string) *exec.Cmd
}

func NewThumbnailGenerator() *ThumbnailGenerator {
	return &ThumbnailGenerator{
		exec: exec.CommandContext,
	}
}

func (g *ThumbnailGenerator) Generate(msg pipeline.Message[*FileWithPages]) (pipeline.Message[*FileWithPages], error) {
	cfg, err := pipeline.ContextValue[*config.Config](msg.Context(), presentation.ConfigKey)
	if err != nil {
		return pipeline.Message[*FileWithPages]{}, fmt.Errorf("could not load the required configuration: %w", err)
	}

	timeout := cfg.Generation.Thumbnail.Timeout
	thumbnails := make([]string, 0)
	thumbnailDir := fmt.Sprintf("%s%cthumbnails", filepath.Dir(msg.Payload.File), os.PathSeparator)

	for _, page := range msg.Payload.Pages {
		thumbnail := fmt.Sprintf("%s%cthumb-%d.png", thumbnailDir, os.PathSeparator, page.Num)
		args := []string{
			"-png",
			"-scale-to",
			"150",
			"-cropbox",
			"-singlefile",
			page.File,
			thumbnail,
		}

		ctx, cancel := context.WithTimeout(context.Background(), time.Duration(timeout)*time.Second)
		defer cancel()

		cmd := g.exec(ctx, "pdftocairo", args...)
		output, thumbErr := cmd.CombinedOutput()
		if thumbErr != nil {
			slog.Error("Failed to generate thumbnail", "source", page.File, "page", page.Num, "error", thumbErr, "output", output)
			_, statErr := os.Stat(thumbnail)
			if os.IsNotExist(statErr) {
				blank := cfg.Generation.Blank.Thumbnail
				cpErr := presentation.Copy(blank, thumbnail)
				if cpErr != nil {
					slog.Error("Failed copy blank thumbnail", "source", blank, "dest", thumbnail, "error", cpErr)
				}
			}
		}
		thumbnails = append(thumbnails, thumbnail)
	}

	return pipeline.NewMessageWithContext(&FileWithPages{
		ID:         msg.Payload.ID,
		File:       msg.Payload.File,
		Pages:      msg.Payload.Pages,
		Thumbnails: thumbnails,
		TextFiles:  msg.Payload.TextFiles,
		Svgs:       msg.Payload.Svgs,
		Pngs:       msg.Payload.Pngs,
	}, msg.Context()), nil
}

type TextFileGenerator struct {
	exec func(ctx context.Context, name string, args ...string) *exec.Cmd
}

func NewTextFileGenerator() *TextFileGenerator {
	return &TextFileGenerator{
		exec: exec.CommandContext,
	}
}

func (g *TextFileGenerator) Generate(msg pipeline.Message[*FileWithPages]) (pipeline.Message[*FileWithPages], error) {
	cfg, err := pipeline.ContextValue[*config.Config](msg.Context(), presentation.ConfigKey)
	if err != nil {
		return pipeline.Message[*FileWithPages]{}, fmt.Errorf("could not load the required configuration: %w", err)
	}

	timeout := cfg.Generation.TextFile.Timeout
	textFiles := make([]string, 0)
	textFileDir := fmt.Sprintf("%s%ctextfiles", msg.Payload.File, os.PathSeparator)

	for _, page := range msg.Payload.Pages {
		textFile := fmt.Sprintf("%s%cslide-%d.txt", textFileDir, os.PathSeparator, page.Num)
		args := []string{
			"-raw",
			"-nopgbrk",
			"-enc",
			"UTF-8",
			"-f",
			strconv.Itoa(page.Num),
			"-l",
			strconv.Itoa(page.Num),
			msg.Payload.File,
			textFile,
		}

		ctx, cancel := context.WithTimeout(context.Background(), time.Duration(timeout)*time.Second)
		defer cancel()

		cmd := g.exec(ctx, "pdftotext", args...)
		output, thumbErr := cmd.CombinedOutput()
		if thumbErr != nil {
			slog.Error("Failed to generate text file", "source", msg.Payload.File, "page", page.Num, "error", thumbErr, "output", output)
		}
		textFiles = append(textFiles, textFile)
	}

	return pipeline.NewMessageWithContext(&FileWithPages{
		ID:         msg.Payload.ID,
		File:       msg.Payload.File,
		Pages:      msg.Payload.Pages,
		Thumbnails: msg.Payload.Thumbnails,
		TextFiles:  textFiles,
		Svgs:       msg.Payload.Svgs,
		Pngs:       msg.Payload.Pngs,
	}, msg.Context()), nil
}

// type SvgGenerator struct {}

// func (g SvgGenerator) Generate(msg pipeline.Message[*FileWithPages]) (pipeline.Message[*FileWithPages], error) {
// 	cfg, err := pipeline.ContextValue[*config.Config](msg.Context(), presentation.ConfigKey)
// 	if err != nil {
// 		return pipeline.Message[*FileWithPages]{}, fmt.Errorf("could not load the required configuration: %w", err)
// 	}

// 	svgs := make([]string, 0)
// 	svgDir := fmt.Sprintf("%s%csvgs", filepath.Dir(msg.Payload.File), os.PathSeparator)

// 	for _, page := range msg.Payload.Pages {
// 		svg := fmt.Sprintf("%s%cslide-%d.svg", svgDir, os.PathSeparator, page.Num)
// 	}
// }

type PngGenerator struct {
	exec func(ctx context.Context, name string, args ...string) *exec.Cmd
}

func NewPngGenerator() *PngGenerator {
	return &PngGenerator{
		exec: exec.CommandContext,
	}
}

func (g *PngGenerator) Generate(msg pipeline.Message[*FileWithPages]) (pipeline.Message[*FileWithPages], error) {
	cfg, err := pipeline.ContextValue[*config.Config](msg.Context(), presentation.ConfigKey)
	if err != nil {
		return pipeline.Message[*FileWithPages]{}, fmt.Errorf("could not load the required configuration: %w", err)
	}

	if !cfg.Generation.Png.Generate {
		return pipeline.NewMessageWithContext(&FileWithPages{
			ID:         msg.Payload.ID,
			File:       msg.Payload.File,
			Pages:      msg.Payload.Pages,
			Thumbnails: msg.Payload.Thumbnails,
			TextFiles:  msg.Payload.TextFiles,
			Svgs:       msg.Payload.Svgs,
			Pngs:       msg.Payload.Pngs,
		}, msg.Context()), nil
	}

	timeout := cfg.Generation.Png.Timeout
	pngs := make([]string, 0)
	pngDir := fmt.Sprintf("%s%cpngs", msg.Payload.File, os.PathSeparator)

	for _, page := range msg.Payload.Pages {
		png := fmt.Sprintf("%s%cslide-%d.txt", pngDir, os.PathSeparator, page.Num)

		args := []string{
			"-png",
			"-scale-to",
			strconv.Itoa(cfg.Generation.Png.SlideWidth),
			"-cropbox",
			"-singlefile",
			page.File,
			png,
		}

		ctx, cancel := context.WithTimeout(context.Background(), time.Duration(timeout)*time.Second)
		defer cancel()

		cmd := g.exec(ctx, "pdftocairo", args...)
		output, pngErr := cmd.CombinedOutput()
		if pngErr != nil {
			slog.Error("Failed to generate PNG", "source", page.File, "page", page.Num, "error", pngErr, "output", output)
			_, statErr := os.Stat(png)
			if os.IsNotExist(statErr) {
				blank := cfg.Generation.Blank.Png
				cpErr := presentation.Copy(blank, png)
				if cpErr != nil {
					slog.Error("Failed copy blank PNG", "source", blank, "dest", png, "error", cpErr)
				}
			}
		}
		pngs = append(pngs, png)
	}

	return pipeline.NewMessageWithContext(&FileWithPages{
		ID:         msg.Payload.ID,
		File:       msg.Payload.File,
		Pages:      msg.Payload.Pages,
		Thumbnails: msg.Payload.Thumbnails,
		TextFiles:  msg.Payload.TextFiles,
		Svgs:       msg.Payload.Svgs,
		Pngs:       pngs,
	}, msg.Context()), nil
}
