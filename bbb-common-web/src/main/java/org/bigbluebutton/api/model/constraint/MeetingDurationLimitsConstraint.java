package org.bigbluebutton.api.model.constraint;

import org.bigbluebutton.api.model.validator.MeetingDurationLimitsValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = MeetingDurationLimitsValidator.class)
@Target(TYPE)
@Retention(RUNTIME)
public @interface MeetingDurationLimitsConstraint {

    String key() default "meetingDurationLimitExceeded";
    String message() default "Meeting duration cannot be modified above 9 hours or below 0 seconds";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
