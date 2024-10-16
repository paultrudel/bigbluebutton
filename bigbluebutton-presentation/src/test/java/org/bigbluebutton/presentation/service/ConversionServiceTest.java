package org.bigbluebutton.presentation.service;

import org.bigbluebutton.presentation.exception.OfficeConversionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class ConversionServiceTest {

    protected ConversionService conversionService;

    protected abstract ConversionService createConversionService();  // To be implemented by subclasses

    @BeforeEach
    public void setUp() {
        conversionService = createConversionService();  // Create specific implementation
    }

    @Test
    void testConvertOfficeToPdf_InputFileDoesNotExist() {
        File input = mock(File.class);
        File output = mock(File.class);

        when(input.exists()).thenReturn(false);

        OfficeConversionException thrown = assertThrows(OfficeConversionException.class, () -> {
            conversionService.convertOfficeToPdf(input, output, 30);
        });

        assertEquals("Input file null does not exist", thrown.getMessage());
    }

    @Test
    void testConvertOfficeToPdf_CannotReadInputFile() {
        File input = mock(File.class);
        File output = mock(File.class);

        when(input.exists()).thenReturn(true);
        when(input.canRead()).thenReturn(false);

        OfficeConversionException thrown = assertThrows(OfficeConversionException.class, () -> {
            conversionService.convertOfficeToPdf(input, output, 30);
        });

        assertEquals("Cannot read input file null", thrown.getMessage());
    }

    @Test
    void testConvertOfficeToPdf_MaxRetries() throws Exception {
        File input = mock(File.class);
        File output = mock(File.class);

        when(input.exists()).thenReturn(true);
        when(input.canRead()).thenReturn(true);
        when(output.exists()).thenReturn(false);

        // Expect max retries to be reached and failure to occur
        OfficeConversionException thrown = assertThrows(OfficeConversionException.class, () -> {
            conversionService.convertOfficeToPdf(input, output, 30);
        });

        assertTrue(thrown.getMessage().contains("Failed to convert"));
    }
}
