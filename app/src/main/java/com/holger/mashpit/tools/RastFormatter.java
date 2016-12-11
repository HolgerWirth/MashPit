package com.holger.mashpit.tools;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;

public class RastFormatter implements IValueFormatter {

    private DecimalFormat mFormat;

    public RastFormatter() {
        mFormat = new DecimalFormat("0");
    }

    @Override
    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
        return mFormat.format(value) + " min";
    }
}
