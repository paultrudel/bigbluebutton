package org.bigbluebutton.api.model.request;

import org.bigbluebutton.api.model.constraint.MeetingIDConstraint;
import org.bigbluebutton.api.model.constraint.NotEmpty;
import org.bigbluebutton.api.model.constraint.NotNull;
import org.bigbluebutton.api.model.constraint.UserSessionConstraint;

import java.util.Map;

public class InsertDocument implements Request<InsertDocument.Params> {

    public enum Params implements RequestParameters {
        SESSION_TOKEN("sessionToken"),
        MEETING_ID("meetingID"),
        URL("url");

        private final String value;

        Params(String value) { this.value = value; }

        public String getValue() { return value; }
    }

    @UserSessionConstraint
    private String sessionToken;

    @MeetingIDConstraint
    private String meetingID;

    @NotEmpty
    @NotNull
    private String url;

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public String getMeetingID() {
        return meetingID;
    }

    public void setMeetingID(String meetingID) {
        this.meetingID = meetingID;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public void populateFromParamsMap(Map<String, String[]> params) {
        if(params.containsKey(InsertDocument.Params.SESSION_TOKEN.getValue())) setSessionToken(params.get(InsertDocument.Params.SESSION_TOKEN.getValue())[0]);
        if(params.containsKey(InsertDocument.Params.MEETING_ID.getValue())) setMeetingID(params.get(InsertDocument.Params.MEETING_ID.getValue())[0]);
        if(params.containsKey(InsertDocument.Params.URL.getValue())) setMeetingID(params.get(InsertDocument.Params.URL.getValue())[0]);
    }

    @Override
    public void convertParamsFromString() { }
}
