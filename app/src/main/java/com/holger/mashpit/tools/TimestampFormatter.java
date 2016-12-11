package com.holger.mashpit.tools;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimestampFormatter implements IAxisValueFormatter {

    private long unixTime = System.currentTimeMillis() / 1000L;
    private long mTime = unixTime - (24 * 60 * 60);

    private SimpleDateFormat sformat = new SimpleDateFormat("HH:mm",Locale.GERMANY);
    private SimpleDateFormat lformat = new SimpleDateFormat("dd.MM. HH:mm", Locale.GERMANY);

    @Override
    public String getFormattedValue(float value, AxisBase axis) {

        long ts = (long)(value*1000);

        Date df = new Date(ts);
        if((long)value>mTime)
        {
            return sformat.format(df);
        }
        return lformat.format(df);
    }
}

