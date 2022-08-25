package com.holger.mashpit.stepper;

import android.annotation.SuppressLint;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.holger.mashpit.R;
import com.holger.mashpit.model.Sensors;
import com.holger.mashpit.model.SensorsHandler;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import ernestoyaquello.com.verticalstepperform.Step;

public class SubSelectInterval extends Step<String> {

    private static final String DEBUG_TAG = "SelectInterval";
    SensorsHandler sensorsHandler;
    LayoutInflater inflater;
    ChipGroup intervalGroup;
    View intervalSelect;
    String sensorInterval;
    Boolean selected = false;
    SubscriptionHolder holder;

    public SubSelectInterval(String title) {
        super(title);
    }

    @Override
    public String getStepData() {
        return sensorInterval;
    }

    @Override
    public String getStepDataAsHumanReadableString() {
        return sensorInterval;
    }

    @Override
    protected void restoreStepData(String data) {

    }

    @Override
    protected IsDataValid isStepDataValid(String stepData) {
        Log.i(DEBUG_TAG, "isStepDataValid: "+selected);
        return selected
                ? new IsDataValid(true)
                : new IsDataValid(false, "Bitte ein Intervall auswählen!");
    }

    @SuppressLint("InflateParams")
    @Override
    protected View createStepContentLayout() {
        Log.i(DEBUG_TAG, "createContentLayout");
        holder = SubscriptionHolder.getInstance();
        inflater = LayoutInflater.from(getContext());
        intervalSelect = inflater.inflate(R.layout.activity_subscription_stepper, null, false);
        sensorsHandler = new SensorsHandler();
        return intervalSelect;
    }

    @Override
    protected void onStepOpened(boolean animated) {
        intervalGroup = intervalSelect.findViewById(R.id.chipInterval);
        setIntervals(holder.getServer(), holder.getSensor());
    }

    @Override
    protected void onStepClosed(boolean animated) {
        if(selected)
        {
            holder.setInterval(sensorInterval);
        }
        for(int i=0;i<intervalGroup.getChildCount();i++)
        {
            intervalGroup.getChildAt(i).setVisibility(View.GONE);
        }
        selected=false;
    }

    @Override
    protected void onStepMarkedAsCompleted(boolean animated) {

    }

    @Override
    protected void onStepMarkedAsUncompleted(boolean animated) {

    }

    public void setCategoryChips(ChipGroup chipsPrograms, String group, List<IntervalChips> chips) {
        for (IntervalChips category : chips) {
            @SuppressLint("InflateParams") Chip mChip = (Chip) inflater.inflate(R.layout.sensor_chip_category, null, false);
            mChip.setText(category.name);
            mChip.setTag(group);
            if(category.name.equals(holder.getInterval()))
            {
                selected=true;
                mChip.setChecked(true);
                sensorInterval=mChip.getText().toString();
                markAsCompletedOrUncompleted(true);
            }
//            mChip.setTextIsSelectable(category.selectable);
            if (!category.active) {
                mChip.setChipBackgroundColorResource(R.color.design_default_color_error);
            }
            if (!category.selectable) {
                mChip.setEnabled(false);
//                mChip.setChipBackgroundColorResource(R.color.material_blue_500);
            }
            int paddingDp = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 10,
                    intervalSelect.getResources().getDisplayMetrics()
            );
            mChip.setPadding(paddingDp, 0, paddingDp, 0);
            mChip.setOnCheckedChangeListener((compoundButton, b) -> {
                if (b) {
                    Log.i(DEBUG_TAG, "Chip selected: Group: " + compoundButton.getTag() + " Chip pressed: " + compoundButton.getText());
                    sensorInterval=compoundButton.getText().toString().trim();
                    selected=true;
                    markAsCompletedOrUncompleted(true);
                    getFormView().goToNextStep(true);
                } else {
                    Log.i(DEBUG_TAG, "Chip unselected: Group: " + compoundButton.getTag() + " Chip pressed: " + compoundButton.getText());
                    selected=false;
                    markAsCompletedOrUncompleted(true);
                }
            });
            chipsPrograms.addView(mChip);
        }
    }

    private void setIntervals(String server, String sensor) {
        List<IntervalChips> categories = new ArrayList<>();
        List<Sensors> intervallist = updateIntervalList(server,sensor);

        DecimalFormat formatter = new DecimalFormat("####");
        for (Sensors result : intervallist) {
            IntervalChips myChip = new IntervalChips();
            myChip.name = String.format("%4s", formatter.format(result.interval));
            myChip.active = true;
            myChip.selectable = result.active;
            categories.add(myChip);
        }
        setCategoryChips(intervalGroup, "intervals", categories);
    }

    private List<Sensors> updateIntervalList(String server,String sensor) {
        List<Sensors> sensorresult = sensorsHandler.getAllSensors(server);
        final List<Sensors> upresult = new ArrayList<>();
        for (Sensors sensors : sensorresult) {
            if (sensors.sensor.equals(sensor)) {
                upresult.add(sensors);
            }
        }
        return upresult;
    }

    private static class IntervalChips {
        String name;
        boolean active;
        boolean selectable;
    }
}
