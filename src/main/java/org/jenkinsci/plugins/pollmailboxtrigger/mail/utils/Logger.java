package org.jenkinsci.plugins.pollmailboxtrigger.mail.utils;

import org.jenkinsci.lib.xtrigger.XTriggerLog;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * <p>
 * Logger interface, so that we're not tied to XTriggerLog!
 * </p>
 * <p>
 * Question: is there a better/generic way of doing this?
 * </p>
 *
 * @author Nick Grealy
 */
@SuppressWarnings("unused")
public abstract class Logger {

    private static final Logger DEFAULT = new Logger() {

        @Override
        public void info(final String message) {
            write(System.out, message);
        }

        @Override
        public void error(final String message) {
            write(System.err, message);
        }

        private void write(final PrintStream out, final String message) {
            if (!"".equals(message.trim())) {
                out.print(message);
                if (!message.contains("\n")) {
                    out.print("\n");
                }
            }
        }
    };

    public static Logger getDefault() {
        return DEFAULT;
    }

    public abstract void info(final String message);

    public abstract void error(final String message);

    public OutputStream getOutputStream() {
        return new LoggerStream(this);
    }

    public PrintStream getPrintStream() {
        try {
            return new PrintStream(new LoggerStream(this), false, StandardCharsets.UTF_8.displayName());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Cannot handle UnsupportedEncodingException", e);
        }
    }

    /* utilty classes */

    public abstract static class HasLogger {

        private Logger logger;

        public HasLogger(final Logger logger) {
            this.logger = logger;
        }

        public Logger getLogger() {
            return logger;
        }
    }

    private static final class LoggerStream extends OutputStream {

        private final Logger logger;

        private LoggerStream(final Logger logger) {
            this.logger = logger;
        }

        @Override
        public void write(final byte[] b) throws IOException {
            logger.info(new String(b, StandardCharsets.UTF_8));
        }

        @Override
        public void write(final byte[] b, final int off, final int len) throws IOException {
            logger.info(new String(b, off, len, StandardCharsets.UTF_8));
        }

        @Override
        public void write(final int b) throws IOException {
            write(new byte[]{(byte) b});
        }
    }

    /* implementations */

    public static class XTriggerLoggerWrapper extends Logger {

        private XTriggerLog delegate;

        public XTriggerLoggerWrapper(final XTriggerLog delegate) {
            this.delegate = delegate;
        }

        public void error(final String message) {
            delegate.error(message);
        }

        public void info(final String message) {
            delegate.info(message);
        }
    }

}
