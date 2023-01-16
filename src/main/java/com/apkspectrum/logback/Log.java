package com.apkspectrum.logback;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Objects;

import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.read.ListAppender;

public class Log {
    static private final String PATTERN =
            "%d{MM-dd HH:mm:ss.SSS} %3tid{0} %.-1level %logger{0}\\(%line\\) : %msg";
    static private final String PATTERN_WITH_NEWLINE = PATTERN + "%n";

    static public void e(Object msg) {
        log(Level.ERROR, Objects.toString(msg), (Object[]) null);
    }

    static public void e(String format, Object... arguments) {
        log(Level.ERROR, format, arguments);
    }

    static public void w(Object msg) {
        log(Level.WARN, Objects.toString(msg), (Object[]) null);
    }

    static public void w(String format, Object... arguments) {
        log(Level.WARN, format, arguments);
    }

    static public void i(Object msg) {
        log(Level.INFO, Objects.toString(msg), (Object[]) null);
    }

    static public void i(String format, Object... arguments) {
        log(Level.INFO, format, arguments);
    }

    static public void d(Object msg) {
        log(Level.DEBUG, Objects.toString(msg), (Object[]) null);
    }

    static public void d(String format, Object... arguments) {
        log(Level.DEBUG, format, arguments);
    }

    static public void t(Object msg) {
        log(Level.TRACE, Objects.toString(msg), (Object[]) null);
    }

    static public void t(String format, Object... arguments) {
        log(Level.TRACE, format, arguments);
    }

    static public void v(Object msg) {
        log(Level.TRACE, Objects.toString(msg), (Object[]) null);
    }

    static public void v(String format, Object... arguments) {
        log(Level.TRACE, format, arguments);
    }

    static private void log(Level level, String format, Object... arguments) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        Logger logger = getLogger(stackTrace[3].getClassName());
        LoggingEvent event =
                new LoggingEvent(Log.class.getName(), logger, level, format, null, arguments);
        event.setCallerData(Arrays.copyOfRange(stackTrace, 3, 4));
        logger.callAppenders(event);
    }

    static public Logger getLogger() {
        return getLogger(getCaller());
    }

    static private Logger getLogger(String name) {
        return (Logger) LoggerFactory.getLogger(name);
    }

    static private String getCaller() {
        StackTraceElement caller = Thread.currentThread().getStackTrace()[3];
        return caller.getClassName();
    }

    static public void setLevel(Level level) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = context.getLogger(("ROOT"));
        if (logger != null) {
            logger.setLevel(level);
        }
    }

    static public void enableConsoleLog(boolean enable) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = context.getLogger(("ROOT"));
        Appender<ILoggingEvent> appender = logger.getAppender("CONSOLE");
        if (appender == null) return;
        if (appender.isStarted() != enable) {
            if (enable) {
                appender.start();
            } else {
                appender.stop();
            }
        }
    }

    static public String getLog() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = context.getLogger(("ROOT"));
        Appender<ILoggingEvent> appender = logger.getAppender("LIST");

        if (!(appender instanceof ListAppender)) {
            return "No such ListAppender";
        }

        PatternLayout patternLayout = makeLayout(context, true);
        patternLayout.start();

        StringBuilder sb = new StringBuilder();
        for (ILoggingEvent evt : ((ListAppender<ILoggingEvent>) appender).list) {
            sb.append(patternLayout.doLayout(evt));
        }
        patternLayout.stop();

        return sb.toString();
    }

    static private PatternLayout makeLayout(LoggerContext context, boolean newLine) {
        PatternLayout patternLayout = new PatternLayout();
        patternLayout.setContext(context);
        patternLayout.setPattern(newLine ? PATTERN_WITH_NEWLINE : PATTERN);
        return patternLayout;
    }

    static public void saveLogFile(String name) {
        try (FileOutputStream file = new FileOutputStream(name)) {
            file.write(getLog().getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static {
        getLogger("ROOT"); // for load logback

        // Redirection a system output to logback.
        System.setOut(new LogPrintStream(System.out));
        System.setErr(new LogPrintStream(System.err));
    }

    static private class LogPrintStream extends PrintStream {
        public LogPrintStream(OutputStream outputStream) {
            super(outputStream);
        }

        @Override
        public void print(String str) {
            appendLog(str);
        }

        @Override
        public void println(String str) {
            appendLog(str);
        }

        @Override
        public void println(Object x) {
            appendLog(String.valueOf(x));
        }

        private void appendLog(String msg) {
            StackTraceElement[] stack = Thread.currentThread().getStackTrace();
            int caller = 3;
            for (; caller < stack.length; ++caller) {
                if (!stack[caller].getClassName().startsWith("java.")) {
                    break;
                }
            }
            if (caller >= stack.length) {
                caller = stack.length - 1;
            }
            Logger logger = getLogger(stack[caller].getClassName());
            LoggingEvent event =
                    new LoggingEvent(Log.class.getName(), logger, Level.TRACE, msg, null, null);
            event.setCallerData(Arrays.copyOfRange(stack, caller, caller + 1));
            logger.callAppenders(event);
        }
    }
}
