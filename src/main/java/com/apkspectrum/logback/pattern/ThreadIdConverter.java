package com.apkspectrum.logback.pattern;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.FormatInfo;

public class ThreadIdConverter extends ClassicConverter {
    @Override
    public String convert(ILoggingEvent e) {
        FormatInfo fInfo = getFormattingInfo();
        if (fInfo != null && fInfo.getMin() > 0 && fInfo.isLeftPad()
                && "0".equals(getFirstOption())) {
            String format = "%0" + fInfo.getMin() + "d";
            return String.format(format, Thread.currentThread().getId());
        }
        return String.valueOf(Thread.currentThread().getId());
    }
}
