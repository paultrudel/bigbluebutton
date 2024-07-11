package main

import (
	"context"
	"io"
	"log"
	"net/http"
	"time"

	bbbcore "github.com/bigbluebutton/bigbluebutton/bbb-core-api/gen/bbb-core"
	"github.com/bigbluebutton/bigbluebutton/bbb-core-api/internal/model"
	"github.com/bigbluebutton/bigbluebutton/bbb-core-api/internal/validation"
	"github.com/bigbluebutton/bigbluebutton/bbb-core-api/util"
	"google.golang.org/grpc/codes"
)

func (app *Config) isMeetingRunning(w http.ResponseWriter, r *http.Request) {
	log.Println("Handling isMeetingRunning request")

	params := r.URL.Query()
	var payload model.Response

	meetingId := util.StripCtrlChars(params.Get("meetingID"))
	req := &model.IsMeetingRunningRequest{
		MeetingId: meetingId,
	}

	v := validation.IsMeetingRunningValidator{
		Request: req,
	}
	ok, key, msg := v.Validate()
	if !ok {
		app.respondWithErrorXML(w, model.ReturnCodeFailure, key, msg)
		return
	}

	ctx, cancel := context.WithTimeout(context.Background(), time.Second)
	defer cancel()

	res, err := app.BbbCore.IsMeetingRunning(ctx, &bbbcore.MeetingRunningRequest{
		MeetingId: meetingId,
	})
	if err != nil {
		log.Println(err)
		app.respondWithErrorXML(w, model.ReturnCodeFailure, model.UnknownErrorKey, model.UnknownErrorMsg)
		return
	}

	payload = model.Response{
		ReturnCode: model.ReturnCodeSuccess,
		Running:    &res.IsRunning,
	}

	app.writeXML(w, http.StatusAccepted, payload)
}

func (app *Config) getMeetingInfo(w http.ResponseWriter, r *http.Request) {
	log.Println("Handling getMeetingInfo request")

	params := r.URL.Query()

	meetingId := util.StripCtrlChars(params.Get("meetingID"))
	req := &model.GetMeetingInfoRequest{
		MeetingId: meetingId,
	}

	v := validation.GetMeetingInfoValidator{
		Request: req,
	}
	ok, key, msg := v.Validate()
	if !ok {
		app.respondWithErrorXML(w, model.ReturnCodeFailure, key, msg)
		return
	}

	ctx, cancel := context.WithTimeout(context.Background(), time.Second)
	defer cancel()

	res, err := app.BbbCore.GetMeetingInfo(ctx, &bbbcore.MeetingInfoRequest{
		MeetingId: meetingId,
	})
	if err != nil {
		log.Println(err)
		code := app.getGrpcErrorCode(err)
		switch code {
		case codes.NotFound:
			app.writeXML(w, http.StatusAccepted, app.grpcErrorToErrorResp(err))
			return
		default:
			app.respondWithErrorXML(w, model.ReturnCodeFailure, model.UnknownErrorKey, model.UnknownErrorMsg)
		}
	}

	users := make([]model.User, 0, len(res.MeetingInfo.Users))
	for _, u := range res.MeetingInfo.Users {
		user := model.GrpcUserToRespUser(u)
		users = append(users, user)
	}

	metadata := model.MapToMapData(res.MeetingInfo.Metadata, "metadata")

	payload := model.GetMeetingInfoResponse{
		ReturnCode:            model.ReturnCodeSuccess,
		MeetingName:           res.MeetingInfo.MeetingName,
		MeetingId:             res.MeetingInfo.MeetingExtId,
		InternalMeetingId:     res.MeetingInfo.MeetingIntId,
		CreateTime:            res.MeetingInfo.DurationInfo.CreateTime,
		CreateDate:            res.MeetingInfo.DurationInfo.CreatedOn,
		VoiceBridge:           res.MeetingInfo.VoiceBridge,
		DialNumber:            res.MeetingInfo.DialNumber,
		AttendeePW:            res.MeetingInfo.AttendeePw,
		ModeratorPW:           res.MeetingInfo.ModeratorPw,
		Running:               res.MeetingInfo.DurationInfo.IsRunning,
		Duration:              res.MeetingInfo.DurationInfo.Duration,
		HasUserJoined:         res.MeetingInfo.ParticipantInfo.HasUserJoined,
		Recording:             res.MeetingInfo.Recording,
		HasBeenForciblyEnded:  res.MeetingInfo.DurationInfo.HasBeenForciblyEnded,
		StartTime:             res.MeetingInfo.DurationInfo.StartTime,
		EndTime:               res.MeetingInfo.DurationInfo.EndTime,
		ParticipantCount:      res.MeetingInfo.ParticipantInfo.ParticipantCount,
		ListenerCount:         res.MeetingInfo.ParticipantInfo.ListenerCount,
		VoiceParticipantCount: res.MeetingInfo.ParticipantInfo.VoiceParticipantCount,
		VideoCount:            res.MeetingInfo.ParticipantInfo.VideoCount,
		MaxUsers:              res.MeetingInfo.ParticipantInfo.MaxUsers,
		ModeratorCount:        res.MeetingInfo.ParticipantInfo.ModeratorCount,
		Users:                 model.Users{Users: users},
		Metadata:              metadata,
		IsBreakout:            res.MeetingInfo.BreakoutInfo.IsBreakout,
		BreakoutRooms:         model.BreakoutRooms{Breakout: res.MeetingInfo.BreakoutRooms},
	}

	app.writeXML(w, http.StatusAccepted, payload)
}

func (app *Config) getMeetings(w http.ResponseWriter, r *http.Request) {
	log.Println("Handling getMeetings request")

	params := r.URL.Query()
	var payload model.Response

	meetingId := util.StripCtrlChars(params.Get("meetingID"))

	req := &model.GetMeetingsRequest{
		MeetingId: meetingId,
	}

	v := validation.GetMeetingsValidator{
		Request: req,
	}
	ok, key, msg := v.Validate()
	if !ok {
		app.respondWithErrorXML(w, model.ReturnCodeFailure, key, msg)
		return
	}

	ctx, cancel := context.WithTimeout(context.Background(), time.Second)
	defer cancel()

	stream, err := app.BbbCore.GetMeetingsStream(ctx, &bbbcore.GetMeetingsStreamRequest{
		MeetingId: meetingId,
	})
	if err != nil {
		log.Println(err)
		code := app.getGrpcErrorCode(err)
		switch code {
		case codes.NotFound:
			app.writeXML(w, http.StatusAccepted, app.grpcErrorToErrorResp(err))
			return
		default:
			app.respondWithErrorXML(w, model.ReturnCodeFailure, model.UnknownErrorKey, model.UnknownErrorMsg)
		}
	}

	meetings := make([]model.Meeting, 0)
	for {
		res, err := stream.Recv()
		if err == io.EOF {
			break
		}
		if err != nil {
			log.Println(err)
			app.respondWithErrorXML(w, model.ReturnCodeFailure, model.UnknownErrorKey, model.UnknownErrorMsg)
			return
		}
		if res.MeetingInfo != nil {
			meetings = append(meetings, model.MeetingInfoToMeeting(res.MeetingInfo))
		}
	}

	payload = model.Response{
		ReturnCode: model.ReturnCodeSuccess,
		Meetings: &model.Meetings{
			Meetings: meetings,
		},
	}

	app.writeXML(w, http.StatusAccepted, payload)
}

func (app *Config) createMeeting(w http.ResponseWriter, r *http.Request) {
	log.Println("Handling createMeeting request")

	params := r.URL.Query()

	req := &model.CreateRequest{
		Name:            util.StripCtrlChars(params.Get("name")),
		MeetingId:       util.StripCtrlChars(params.Get("meetingID")),
		VoiceBridge:     util.StripCtrlChars(params.Get("voiceBridge")),
		AttendeePw:      util.StripCtrlChars(params.Get("attendeePW")),
		ModeratorPw:     util.StripCtrlChars(params.Get("moderatorPW")),
		IsBreakoutRoom:  util.StripCtrlChars(params.Get("isBreakoutRoom")),
		ParentMeetingId: util.StripCtrlChars(params.Get("parentMeetingId")),
		Record:          util.StripCtrlChars(params.Get("record")),
	}

	v := validation.CreateValidator{
		Request: req,
	}
	ok, key, msg := v.Validate()
	if !ok {
		app.respondWithErrorXML(w, model.ReturnCodeFailure, key, msg)
		return
	}

	// TODO: check if voice bridge is in use and if parent meeting exists (ideally during create call on akka apps side)

	ctx, cancel := context.WithTimeout(context.Background(), time.Second)
	defer cancel()

	settings, err := app.processCreateQueryParams(&params)
	if err != nil {
		log.Println(err)
		app.respondWithErrorXML(w, model.ReturnCodeFailure, model.CreateMeetingErrorKey, model.CreateMeetingDuplicateMsg)
		return
	}

	res, err := app.BbbCore.CreateMeeting(ctx, &bbbcore.CreateMeetingRequest{
		CreateMeetingSettings: settings,
	})
	if err != nil {
		log.Println(err)
		app.respondWithErrorXML(w, model.ReturnCodeFailure, model.CreateMeetingErrorKey, model.CreateMeetingErrorMsg)
		return
	}

	if !res.IsValid {
		app.respondWithErrorXML(w, model.ReturnCodeFailure, model.MeetingIdNotUniqueErrorKey, model.MeetingIdNotUniqueErrorMsg)
		return
	}

	payload := model.CreateMeetingResponse{
		ReturnCode:           model.ReturnCodeSuccess,
		MeetingId:            res.MeetingExtId,
		InternalMeetingId:    res.MeetingIntId,
		ParentMeetingId:      res.ParentMeetingId,
		AttendeePW:           res.AttendeePw,
		ModeratorPW:          res.ModeratorPw,
		CreateTime:           res.CreateTime,
		VoiceBridge:          res.VoiceBridge,
		DialNumber:           res.DialNumber,
		CreateDate:           res.CreateDate,
		HasUserJoined:        res.HasUserJoined,
		Duration:             res.Duration,
		HasBeenForciblyEnded: res.HasBeenForciblyEnded,
	}

	if res.IsDuplicate {
		payload.MessageKey = model.CreateMeetingDuplicateKey
		payload.Message = model.CreateMeetingDuplicateMsg
	}

	app.writeXML(w, http.StatusAccepted, payload)
}

func (app *Config) createMeetingPost(w http.ResponseWriter, r *http.Request) {

}
