package com.holger.mashpit.tools;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;

public class TempFormatter implements ValueFormatter, YAxisValueFormatter {

    private DecimalFormat mFormat;

    public TempFormatter() {
        mFormat = new DecimalFormat("0.0");
    }

    @Override
    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
        return mFormat.format(value) + "°";
    }


    @Override
    public String getFormattedValue(float value, YAxis yAxis) {
        return mFormat.format(value) + "°";
    }
}
