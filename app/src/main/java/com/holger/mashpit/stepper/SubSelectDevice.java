package com.holger.mashpit.stepper;

import android.annotation.SuppressLint;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.holger.mashpit.R;
import com.holger.mashpit.events.SensorEvent;
import com.holger.mashpit.model.Devices;
import com.holger.mashpit.model.DevicesHandler;

import java.util.ArrayList;
import java.util.List;

import ernestoyaquello.com.verticalstepperform.Step;

public class SubSelectDevice extends Step<String> {

    private static final String DEBUG_TAG = "SelectDevice";
    DevicesHandler devicesHandler;

    View deviceSelect;
    LayoutInflater inflater;
    private String selDevice;
    private String selServer;
    ChipGroup deviceGroup;
    Boolean selected = false;
    SubscriptionHolder holder;

    public SubSelectDevice(String title) {
        super(title);
    }

    @Override
    public String getStepData() {
        return selServer;
    }

    @Override
    public String getStepDataAsHumanReadableString() {
        return selDevice;
    }

    @Override
    protected void restoreStepData(String data) {

    }

    @Override
    protected IsDataValid isStepDataValid(String stepData) {
        Log.i(DEBUG_TAG, "isStepDataValid: "+selected);
        return selected
                ? new IsDataValid(true)
                : new IsDataValid(false, "Bitte ein Device auswählen!");
    }

    @SuppressLint("InflateParams")
    @Override
    protected View createStepContentLayout() {
        Log.i(DEBUG_TAG, "createContentLayout");

        holder= SubscriptionHolder.getInstance();

        inflater = LayoutInflater.from(getContext());
        deviceSelect = inflater.inflate(R.layout.activity_subscription_stepper, null, false);
        devicesHandler = new DevicesHandler();
        deviceGroup = deviceSelect.findViewById(R.id.chipDevices);
        setDevices();
        return deviceSelect;
    }

    @Override
    protected void onStepOpened(boolean animated) {
        Log.i(DEBUG_TAG, "OnStepOpened");
    }

    @Override
    protected void onStepClosed(boolean animated) {
        Log.i(DEBUG_TAG, "OnStepClosed");
        Log.i(DEBUG_TAG, "Server: "+selServer+" Device: "+selDevice);
        holder.setDevice(selDevice);
        holder.setServer(selServer);
    }

    @Override
    protected void onStepMarkedAsCompleted(boolean animated) {
        Log.i(DEBUG_TAG, "onStepMarkedAsCompleted");
    }

    @Override
    protected void onStepMarkedAsUncompleted(boolean animated) {

    }

    private List<SensorEvent> updateServerList() {
        final List<SensorEvent> upresult = new ArrayList<>();
        List<Devices> devices = devicesHandler.getDeviceStatus();

        for (Devices status : devices) {
            SensorEvent sensorevent = new SensorEvent();
            sensorevent.setServer(status.device);
            sensorevent.setName(status.alias);
            sensorevent.setActive(status.active);
            sensorevent.setSensor(status.sensor);
            upresult.add(sensorevent);
        }
        return upresult;
    }

    public void setDeviceChips(ChipGroup chipGroup, List<DeviceChips> chips) {
        for (DeviceChips category : chips) {
            @SuppressLint("InflateParams") Chip mChip = (Chip) inflater.inflate(R.layout.sensor_chip_category, null, false);
            mChip.setText(category.name);
            mChip.setTag(category.server);
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
                    deviceSelect.getResources().getDisplayMetrics()
            );
            mChip.setPadding(paddingDp, 0, paddingDp, 0);
            mChip.setOnCheckedChangeListener((compoundButton, b) -> {
                if (b) {
                    Log.i(DEBUG_TAG, "Chip selected: Server: " + compoundButton.getTag() + " Chip pressed: " + compoundButton.getText());
                    ChipSelected(compoundButton.getTag().toString(), compoundButton.getText().toString());
                    selected=true;
                    markAsCompletedOrUncompleted(true);
                    getFormView().goToNextStep(true);
                } else {
                    Log.i(DEBUG_TAG, "Chip unselected: Server: " + compoundButton.getTag() + " Chip pressed: " + compoundButton.getText());
                    selected=false;
                }
                markAsCompletedOrUncompleted(true);
            });
            chipGroup.addView(mChip);
        }
    }

    private void setDevices() {
        List<DeviceChips> categories = new ArrayList<>();
        List<SensorEvent> serverresult = updateServerList();

        for (SensorEvent result : serverresult) {
            DeviceChips myChip = new DeviceChips();
            myChip.server = result.getServer();
            if (result.getName().isEmpty()) {
                myChip.name = result.getServer();
            } else {
                myChip.name = result.getName();
            }
            myChip.active = result.isActive();
            myChip.selectable = true;
            categories.add(myChip);
        }
        setDeviceChips(deviceGroup, categories);
    }

    public void ChipSelected(String server, String chip) {
        selDevice = chip;
        selServer = server;
    }

    private static class DeviceChips {
        String name;
        String server;
        boolean active;
        boolean selectable;
    }
}

