package org.bigbluebutton.presentation.service.impl;

import com.zaxxer.nuprocess.NuProcess;
import com.zaxxer.nuprocess.NuProcessBuilder;
import org.bigbluebutton.presentation.exception.OfficeConversionException;
import org.bigbluebutton.presentation.handler.OfficeConverterHandler;
import org.bigbluebutton.presentation.service.ConversionService;
import org.bigbluebutton.presentation.service.ConversionServiceTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConversionServiceImplTest extends ConversionServiceTest {

    @InjectMocks
    private ConversionServiceImpl conversionServiceImpl;

    @Mock
    private NuProcessBuilder nuProcessBuilder;

    @Mock
    private NuProcess nuProcess;

    @Mock
    private OfficeConverterHandler handler;

    private File input;
    private File output;

    private Semaphore semaphore;

    @Override
    protected ConversionService createConversionService() {
        return conversionServiceImpl;  // Provide the specific implementation for testing
    }

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Use a spy on a mock Semaphore
        semaphore = spy(new Semaphore(1));

        // Inject the spy Semaphore into the service
        conversionServiceImpl = new ConversionServiceImpl("/mocked/path/to/executable", semaphore);

        // Set up mock input/output files
        input = mock(File.class);
        output = mock(File.class);

        when(input.getName()).thenReturn("test.docx");
        when(output.getName()).thenReturn("output.pdf");

        super.setUp();  // Call the setup from the abstract class
    }

    @Test
    void testConvertOfficeToPdf_Success() throws Exception {
        when(input.exists()).thenReturn(true);
        when(input.canRead()).thenReturn(true);
        when(output.exists()).thenReturn(true);

        // Mock successful command execution
        when(handler.isCommandSuccessful()).thenReturn(true);
        when(handler.isCommandTimeout()).thenReturn(false);
        when(nuProcessBuilder.start()).thenReturn(nuProcess);

        // Simulate the behavior of the mocked process
        conversionServiceImpl.convertOfficeToPdf(input, output, 30);

        verify(nuProcess, times(1)).waitFor(31, TimeUnit.SECONDS);
        verify(handler, times(1)).isCommandSuccessful();
    }

    @Test
    void testConvertOfficeToPdf_CommandTimeout() throws Exception {
        when(input.exists()).thenReturn(true);
        when(input.canRead()).thenReturn(true);
        when(output.exists()).thenReturn(false);

        // Simulate timeout
        when(handler.isCommandTimeout()).thenReturn(true);
        when(nuProcessBuilder.start()).thenReturn(nuProcess);

        OfficeConversionException thrown = assertThrows(OfficeConversionException.class, () -> {
            conversionServiceImpl.convertOfficeToPdf(input, output, 30);
        });

        verify(nuProcess, times(1)).waitFor(31, TimeUnit.SECONDS);
        verify(handler, times(1)).isCommandTimeout();
        assertTrue(thrown.getMessage().contains("Command execution"));
    }

    @Test
    void testConvertOfficeToPdf_OutputFileNotCreated() throws Exception {
        when(input.exists()).thenReturn(true);
        when(input.canRead()).thenReturn(true);
        when(output.exists()).thenReturn(false);

        // Mock successful command but output file is not created
        when(handler.isCommandSuccessful()).thenReturn(true);
        when(nuProcessBuilder.start()).thenReturn(nuProcess);

        OfficeConversionException thrown = assertThrows(OfficeConversionException.class, () -> {
            conversionServiceImpl.convertOfficeToPdf(input, output, 30);
        });

        assertEquals("Output PDF from office document conversion was not created", thrown.getMessage());
    }

    @Test
    void testConvertOfficeToPdf_ScriptNotDefined() {
        conversionServiceImpl = new ConversionServiceImpl(null, 4);  // No script defined

        OfficeConversionException thrown = assertThrows(OfficeConversionException.class, () -> {
            conversionServiceImpl.convertOfficeToPdf(input, output, 30);
        });

        assertEquals("Conversion script path not defined", thrown.getMessage());
    }

    @Test
    void testConvertOfficeToPdf_ConversionScriptNotFound() {
        conversionServiceImpl = new ConversionServiceImpl("/invalid/path/to/executable", 4);

        OfficeConversionException thrown = assertThrows(OfficeConversionException.class, () -> {
            conversionServiceImpl.convertOfficeToPdf(input, output, 30);
        });

        assertEquals("Conversion script /invalid/path/to/executable not found", thrown.getMessage());
    }

    @Test
    void testConvertOfficeToPdf_SemaphoreBlocksAndRetries() throws Exception {
        when(input.exists()).thenReturn(true);
        when(input.canRead()).thenReturn(true);
        when(output.exists()).thenReturn(false);

        // Mock Semaphore acquire to throw InterruptedException on the first call
        doThrow(new InterruptedException()).when(semaphore).acquire();

        // Simulate failed acquire, should retry
        assertThrows(OfficeConversionException.class, () -> {
            conversionServiceImpl.convertOfficeToPdf(input, output, 30);
        });

        // Verify that it tried to acquire the semaphore 3 times due to retries
        verify(semaphore, times(3)).acquire();
    }
}

