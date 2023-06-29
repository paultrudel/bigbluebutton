package org.bigbluebutton.api.model.validator;

import org.bigbluebutton.api.domain.Meeting;
import org.bigbluebutton.api.model.constraint.MeetingDurationLimitsConstraint;
import org.bigbluebutton.api.model.request.ModifyMeeting;
import org.bigbluebutton.api.service.ServiceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class MeetingDurationLimitsValidator implements ConstraintValidator<MeetingDurationLimitsConstraint, ModifyMeeting> {

    private static Logger log = LoggerFactory.getLogger(MeetingEndedValidator.class);

    @Override
    public void initialize(MeetingDurationLimitsConstraint constraintAnnotation) {}

    @Override
    public boolean isValid(ModifyMeeting modifyMeeting, ConstraintValidatorContext context) {
        if(modifyMeeting.getDurationString() == null) return true;

        String meetingID = modifyMeeting.getMeetingId();
        Integer duration;

        try {
            duration = Integer.parseInt(modifyMeeting.getDurationString());
        } catch(NumberFormatException e) {
            return false;
        }

        if(meetingID == null) {
            return false;
        }

        Meeting meeting = ServiceUtils.findMeetingFromMeetingID(meetingID);

        if(meeting == null) {
            return false;
        }

        int newDuration = meeting.getDuration() + (duration / 60);

        return newDuration > 0 && newDuration < (19 * 60);
    }
}
