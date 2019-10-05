package com.holger.mashpit.tools;

import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.DecimalFormat;

public class RastFormatter extends ValueFormatter {

    private DecimalFormat mFormat;

    public RastFormatter() {
        mFormat = new DecimalFormat("0");
    }

    @Override
    public String getFormattedValue(float value) {
        return mFormat.format(value) + " min";
    }
}
