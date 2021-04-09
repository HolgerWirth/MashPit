package com.holger.mashpit.tools;

import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimestampFormatter extends ValueFormatter {

    private final long unixTime = System.currentTimeMillis() / 1000L;
    private final long mTime = unixTime - (24 * 60 * 60);

    private final SimpleDateFormat sformat = new SimpleDateFormat("HH:mm",Locale.GERMANY);
    private final SimpleDateFormat lformat = new SimpleDateFormat("dd.MM. HH:mm", Locale.GERMANY);

    @Override
    public String getFormattedValue(float value) {
        long ts = (long)(value*1000);

        Date df = new Date(ts);
        if((long)value>mTime)
        {
            return sformat.format(df);
        }
        return lformat.format(df);
    }
}

