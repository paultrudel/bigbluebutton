package org.bigbluebutton.presentation.service.impl;

import com.zaxxer.nuprocess.NuProcess;
import com.zaxxer.nuprocess.NuProcessBuilder;
import org.bigbluebutton.presentation.exception.OfficeConversionException;
import org.bigbluebutton.presentation.handler.OfficeConverterHandler;
import org.bigbluebutton.presentation.service.ConversionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Service
public class ConversionServiceImpl implements ConversionService {
    private static final Logger log = LoggerFactory.getLogger(ConversionServiceImpl.class);
    private static final String CONVERSION_COMMAND_TEMPLATE = "timeout %ds /bin/sh -c \"%s \\\"%s\\\" \\\"%s\\\" pdf %d\"";

    private final String officeExec;
    private final Semaphore presOfficeConversionSemaphore;

    @Autowired
    public ConversionServiceImpl(@Value("${presentation.conversion.officeExec}") String officeExec,
                                 @Value("${presentation.conversion.maxConcurrent:#{T(java.lang.Runtime).getRuntime().availableProcessors()}}")
                                 int maxConcurrentConversions) {
        this.officeExec = officeExec;
        this.presOfficeConversionSemaphore = new Semaphore(maxConcurrentConversions);
    }

    public ConversionServiceImpl(String officeExec, Semaphore presOfficeConversionSemaphore) {
        this.officeExec = officeExec;
        this.presOfficeConversionSemaphore = presOfficeConversionSemaphore;
    }

    @Override
    public void convertOfficeToPdf(File input, File output, int timeout) throws OfficeConversionException {
        boolean success = false;
        int attempts = 0;

        if (!input.exists()) {
            throw new OfficeConversionException(String.format("Input file %s does not exist", input.getName()));
        }

        if (!input.canRead()) {
            throw new OfficeConversionException(String.format("Cannot read input file %s", input.getName()));
        }

        while(!success) {
            try {
                presOfficeConversionSemaphore.acquire();
                convertOfficeToPdf(input, output, timeout);
                success = true;
            } catch (Exception e) {
                log.error("Exception during Office to PDF conversion: {}", e.getMessage());
                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException interruptedException) {
                    log.error("Thread interrupted while waiting before retry: {}", e.getMessage());
                }
            } finally {
                presOfficeConversionSemaphore.release();
            }

            if (!success) {
                if (++attempts >= 3) {
                    break;
                }
            }
        }

        if (!success) {
            throw new OfficeConversionException(String.format("Failed to convert %s to PDF", input));
        }
    }


    private void convert(final File input, final File output, final int timeout) throws OfficeConversionException {
        log.info("Conversion process started for file: {}, size: {} bytes, with timeout: {}", input.getName(), input.length(), timeout);

        if (officeExec == null || officeExec.isBlank()) {
            throw new OfficeConversionException("Conversion script path not defined");
        }

        File conversionScript = new File(officeExec);
        if (!conversionScript.exists()) {
            throw new OfficeConversionException(String.format("Conversion script %s not found", officeExec));
        }

        String command = String.format(CONVERSION_COMMAND_TEMPLATE,
                timeout,
                officeExec,
                input.getAbsolutePath(),
                output.getAbsolutePath(),
                timeout);

        log.info("Executing command: {}", command);

        NuProcessBuilder builder = new NuProcessBuilder(Arrays.asList("/bin/sh", "-c", command));
        OfficeConverterHandler handler = new OfficeConverterHandler();
        builder.setProcessListener(handler);

        NuProcess nuProcess = builder.start();
        if (nuProcess == null) {
            throw new OfficeConversionException("Could not launch office document conversion process");
        }

        try {
            nuProcess.waitFor(timeout + 1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("Exception while counting PDF pages for {}", input.getName(), e);
        }

        if (handler.isCommandTimeout()) {
            log.error("Command execution [{}] exceeded the {} secs timeout for {}", officeExec, timeout, input.getName());
        }

        if (!handler.isCommandSuccessful()) {
            throw new OfficeConversionException("Office document conversion was not successful");
        }

        if (!output.exists()) {
            throw new OfficeConversionException("Output PDF from office document conversion was not created");
        }
    }
}
