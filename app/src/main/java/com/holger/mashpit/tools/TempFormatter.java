package com.holger.mashpit.tools;

import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.DecimalFormat;

public class TempFormatter extends ValueFormatter {

    private final DecimalFormat mFormat;
    private final String unit;

    public TempFormatter(String format,String unit) {
        this.unit=unit;
        this.mFormat = new DecimalFormat(format);
    }

    @Override
    public String getFormattedValue(float value) {
        return mFormat.format(value) + unit;
    }
}
