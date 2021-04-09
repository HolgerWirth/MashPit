package com.holger.mashpit.tools;

import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.DecimalFormat;

public class TempFormatter extends ValueFormatter {

    private final DecimalFormat mFormat;

    public TempFormatter() {
        mFormat = new DecimalFormat("0.0");
    }

    @Override
    public String getFormattedValue(float value) {
        return mFormat.format(value) + "°";
    }
}
