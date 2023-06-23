package org.bigbluebutton.api.model.request;

import org.bigbluebutton.api.model.constraint.*;
import org.bigbluebutton.api.model.shared.Checksum;

import java.util.Map;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;

@MeetingDurationLimitsConstraint
public class ModifyMeeting extends RequestWithChecksum<ModifyMeeting.Params> {

    public enum Params implements RequestParameters {
        MEETING_ID("meetingID"),
        DURATION("duration"),
        MAX_PARTICIPANTS("maxParticipants");

        private final String value;

        Params(String value) { this.value = value; }

        public String getValue() { return value; }
    }

    @MeetingIDConstraint
    @MeetingExistsConstraint
    private String meetingId;

    @IsIntegralConstraint
    @MeetingHasDurationConstraint
    @Min(value = -300, message = "Minimum value for seconds is -300")
    @Max(value = 300, message = "Maximum value for seconds is 300")
    private String durationString;
    private Integer duration;

    @IsIntegralConstraint
    private String maxParticipantsString;
    private Integer maxParticipants;

    public ModifyMeeting(Checksum checksum) {
        super(checksum);
    }

    public String getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(String meetingId) {
        this.meetingId = meetingId;
    }

    public String getDurationString() {
        return durationString;
    }

    public void setDurationString(String durationString) {
        this.durationString = durationString;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getMaxParticipantsString() {
        return maxParticipantsString;
    }

    public void setMaxParticipantsString(String maxParticipantsString) {
        this.maxParticipantsString = maxParticipantsString;
    }

    public Integer getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    @Override
    public void populateFromParamsMap(Map<String, String[]> params) {
        if(params.containsKey(Params.MEETING_ID.getValue())) setMeetingId(params.get(Params.MEETING_ID.getValue())[0]);
        if(params.containsKey(Params.DURATION.getValue())) setDurationString(params.get(Params.DURATION.getValue())[0]);
        if(params.containsKey(Params.MAX_PARTICIPANTS.getValue())) setMaxParticipantsString(params.get(Params.MAX_PARTICIPANTS.getValue())[0]);
    }

    @Override
    public void convertParamsFromString() {
        if(durationString != null) duration = Integer.parseInt(durationString);
        if(maxParticipantsString != null) maxParticipants = Integer.parseInt(maxParticipantsString);
    }
}
