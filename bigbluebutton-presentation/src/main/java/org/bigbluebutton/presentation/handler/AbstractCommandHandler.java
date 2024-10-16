package org.bigbluebutton.presentation.handler;

import com.zaxxer.nuprocess.NuAbstractProcessHandler;
import com.zaxxer.nuprocess.NuProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;

public abstract class AbstractCommandHandler extends NuAbstractProcessHandler {

    private static final Logger log = LoggerFactory.getLogger(AbstractCommandHandler.class);

    protected NuProcess nuProcess;
    protected int exitCode;
    protected final StringBuilder stdoutBuilder = new StringBuilder();
    protected final StringBuilder stderrBuilder = new StringBuilder();

    @Override
    public void onPreStart(NuProcess nuProcess) {
        this.nuProcess = nuProcess;
    }

    @Override
    public void onStart(NuProcess nuProcess) {
        super.onStart(nuProcess);
    }

    @Override
    public void onStdout(ByteBuffer buffer, boolean closed) {
        if (buffer != null) {
            CharBuffer charBuffer = StandardCharsets.UTF_8.decode(buffer);
            stdoutBuilder.append(charBuffer);
        }
    }

    @Override
    public void onStderr(ByteBuffer buffer, boolean closed) {
        if (buffer != null) {
            CharBuffer charBuffer = StandardCharsets.UTF_8.decode(buffer);
            stderrBuilder.append(charBuffer);
        }
    }

    @Override
    public void onExit(int statusCode) {
        exitCode = statusCode;
    }

    /**
     *
     * @return true if the exit code of the process is different from 0
     */
    public Boolean exitedWithError() {
        return exitCode != 0;
    }

    protected Boolean stdoutContains(String value) {
        return stdoutBuilder.indexOf(value) > -1;
    }

    protected Boolean stdoutEquals(String value) {
        return stdoutBuilder.toString().trim().equals(value);
    }

    protected Boolean stderrContains(String value) {
        return stderrBuilder.indexOf(value) > -1;
    }

    public Boolean isCommandSuccessful() {
        return !exitedWithError();
    }

    public Boolean isCommandTimeout() {
        return exitCode == 124;
    }
}
