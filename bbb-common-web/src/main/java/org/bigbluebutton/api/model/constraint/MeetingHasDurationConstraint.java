package org.bigbluebutton.api.model.constraint;

import org.bigbluebutton.api.model.validator.MeetingHasDurationValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = MeetingHasDurationValidator.class)
@Target(FIELD)
@Retention(RUNTIME)
public @interface MeetingHasDurationConstraint {

    String key() default "meetingHasNoDuration";
    String message() default "Specified meeting has no duration to be modified";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
