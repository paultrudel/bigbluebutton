package org.bigbluebutton.api.model.validator;

import org.bigbluebutton.api.domain.Meeting;
import org.bigbluebutton.api.model.constraint.MeetingHasDurationConstraint;
import org.bigbluebutton.api.service.ServiceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class MeetingHasDurationValidator implements ConstraintValidator<MeetingHasDurationConstraint, String> {

    private static Logger log = LoggerFactory.getLogger(MeetingEndedValidator.class);

    @Override
    public void initialize(MeetingHasDurationConstraint constraintAnnotation) {}

    @Override
    public boolean isValid(String meetingID, ConstraintValidatorContext context) {

        if(meetingID == null) {
            return false;
        }

        Meeting meeting = ServiceUtils.findMeetingFromMeetingID(meetingID);

        if(meeting == null) {
            return false;
        }

        return meeting.getDuration() != 0;
    }
}
