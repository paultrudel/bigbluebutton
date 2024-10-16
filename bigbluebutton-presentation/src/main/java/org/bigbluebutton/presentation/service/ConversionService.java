package org.bigbluebutton.presentation.service;

import org.bigbluebutton.presentation.exception.OfficeConversionException;

import java.io.File;

public interface ConversionService {

    void convertOfficeToPdf(File input, File output, int timeout) throws OfficeConversionException;
}
