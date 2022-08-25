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

import java.util.ArrayList;
import java.util.List;

import ernestoyaquello.com.verticalstepperform.Step;

public class SubSelectSensor extends Step<String> {

    private static final String DEBUG_TAG = "SelectSensor";
    SensorsHandler sensorsHandler;
    View sensorSelect;
    LayoutInflater inflater;
    ChipGroup sensorGroup;
    String sensorName;
    String sensorSensor;
    Boolean selected=false;
    SubscriptionHolder holder;

    public SubSelectSensor(String title) {
        super(title);
    }

    @Override
    public String getStepData() {
        return sensorSensor;
    }

    @Override
    public String getStepDataAsHumanReadableString() {
        return sensorName;
    }

    @Override
    protected void restoreStepData(String data) {
    }

    @Override
    protected IsDataValid isStepDataValid(String stepData) {
        Log.i(DEBUG_TAG, "isStepDataValid: "+selected);
        return selected
                ? new IsDataValid(true)
                : new IsDataValid(false, "Bitte einen Sensor auswählen!");
    }

    @SuppressLint("InflateParams")
    @Override
    protected View createStepContentLayout() {
        Log.i(DEBUG_TAG, "createContentLayout");
        holder = SubscriptionHolder.getInstance();
        inflater = LayoutInflater.from(getContext());
        sensorSelect = inflater.inflate(R.layout.activity_subscription_stepper, null, false);
        sensorsHandler = new SensorsHandler();
        return sensorSelect;
    }

    @Override
    protected void onStepOpened(boolean animated) {
        sensorGroup = sensorSelect.findViewById(R.id.chipSensors);
        setSensors(holder.getServer());
    }

    @Override
    protected void onStepClosed(boolean animated) {
        for(int i=0;i<sensorGroup.getChildCount();i++)
        {
            sensorGroup.getChildAt(i).setVisibility(View.GONE);
        }
        selected=false;
    }

    @Override
    protected void onStepMarkedAsCompleted(boolean animated) {
        Log.i(DEBUG_TAG, "onStepMarkedAsCompleted");
        holder.setSensor(sensorSensor);
    }

    @Override
    protected void onStepMarkedAsUncompleted(boolean animated) {

    }

    public void setSensorChips(ChipGroup chipGroup, List<SensorChips> chips) {
        for (SensorChips category : chips) {
            @SuppressLint("InflateParams") Chip mChip = (Chip) inflater.inflate(R.layout.sensor_chip_category, null, false);
            mChip.setText(category.name);
            mChip.setTag(category.sensor);
            if(category.sensor.equals(holder.getSensor()))
            {
                selected=true;
                mChip.setChecked(true);
                sensorName=mChip.getText().toString();
                sensorSensor=mChip.getText().toString();
                markAsCompletedOrUncompleted(true);
            }
//            mChip.setTextIsSelectable(category.selectable);
            if (!category.active) {
                mChip.setChipBackgroundColorResource(R.color.colorInactiveChip);
            }
            if (!category.selectable) {
                mChip.setEnabled(false);
//                mChip.setChipBackgroundColorResource(R.color.material_blue_500);
            }
            int paddingDp = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 10,
                    sensorSelect.getResources().getDisplayMetrics()
            );
            mChip.setPadding(paddingDp, 0, paddingDp, 0);
            mChip.setOnCheckedChangeListener((compoundButton, b) -> {
                if (b) {
                    Log.i(DEBUG_TAG, "Chip selected: Group: " + compoundButton.getTag() + " Chip pressed: " + compoundButton.getText());
                    sensorName=compoundButton.getText().toString();
                    sensorSensor=compoundButton.getTag().toString();
                    selected=true;
                    markAsCompletedOrUncompleted(true);
                    getFormView().goToNextStep(true);
                } else {
                    Log.i(DEBUG_TAG, "Chip unselected: Group: " + compoundButton.getTag() + " Chip pressed: " + compoundButton.getText());
                    selected=false;
                    markAsCompletedOrUncompleted(true);
                }
            });
            chipGroup.addView(mChip);
        }
    }

    private List<Sensors> updateSensorList(String server) {
        List<Sensors> sensorresult = sensorsHandler.getAllSensors(server);
        final List<Sensors> upresult = new ArrayList<>();
        String mySensor = "";
        for (Sensors sensors : sensorresult) {
            if (!sensors.sensor.equals(mySensor)) {
                upresult.add(sensors);
                mySensor = sensors.sensor;
            }
        }
        return upresult;
    }

    private void setSensors(String server) {
        List<SensorChips> categories = new ArrayList<>();
        List<Sensors> updateresult = updateSensorList(server);
        String sensorName;

        for (Sensors result : updateresult) {
            SensorChips myChip = new SensorChips();
            myChip.sensor=result.sensor;
            if (result.name.isEmpty()) {
                sensorName = result.sensor;
            } else {
                sensorName = result.name;
            }
            myChip.name = sensorName;
            myChip.active = true;
            myChip.selectable = true;
            categories.add(myChip);
        }
        setSensorChips(sensorGroup, categories);
    }

    private static class SensorChips {
        String name;
        String sensor;
        boolean active;
        boolean selectable;
    }
}
