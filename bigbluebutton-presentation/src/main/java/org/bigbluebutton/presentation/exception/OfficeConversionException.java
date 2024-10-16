package org.bigbluebutton.presentation.exception;

public class OfficeConversionException extends Exception {

    public OfficeConversionException(String errorMessage) {
        super(errorMessage);
    }

    public OfficeConversionException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}
