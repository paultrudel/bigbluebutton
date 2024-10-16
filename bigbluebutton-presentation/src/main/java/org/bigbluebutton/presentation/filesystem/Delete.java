package org.bigbluebutton.presentation.filesystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;

public class Delete {

    private static final Logger log = LoggerFactory.getLogger(Delete.class);

    public static void directory(File directory) {
        if (directory == null) {
            return;
        }

        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }

        for (File file: files) {
            if (file.isDirectory()) {
                directory(file);
            } else {
                if (!file.delete()) {
                    log.error("Failed to delete file: {}", file.getAbsolutePath());
                }
            }
        }

        if (!directory.delete()) {
            log.error("Failed to delete directory: {}", directory.getAbsolutePath());
        }
    }

    public static void fileAndParentDirectory(File file) {
        if (file == null) {
            return;
        }

        Path parentPath = file.toPath().getParent();
        try {
            File parentDir = new File(parentPath.toString());
            if (parentDir.exists()) {
                directory(parentDir);
            }
        } catch (Exception e) {
            log.error("Could not delete {} and its parent directory", file.getAbsolutePath());
        }
    }
}
