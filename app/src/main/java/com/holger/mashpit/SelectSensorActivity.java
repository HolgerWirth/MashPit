package com.holger.mashpit;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.holger.mashpit.events.SensorEvent;
import com.holger.mashpit.model.Devices;
import com.holger.mashpit.model.DevicesHandler;
import com.holger.mashpit.model.Sensors;
import com.holger.mashpit.model.SensorsHandler;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class SelectSensorActivity extends AppCompatActivity {

    private static final String DEBUG_TAG = "SelectSensorActivity";
    private static ChipGroup deviceGroup = null;
    private static ChipGroup sensorGroup = null;
    private static ChipGroup intervalGroup = null;
    private static List<SensorEvent> serverresult = null;
    private static List<Sensors> sensorresult = null;
    private LinearLayout sensorLayout;
    private LinearLayout intervalLayout;

    private String selDevice;
    private String selSensor;
    private int selInterval;

    FloatingActionButton actionButton;
    String topicString = "";
    DevicesHandler devicesHandler;
    SensorsHandler sensorsHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_sensor);

        devicesHandler = new DevicesHandler();
        sensorsHandler = new SensorsHandler();
        actionButton = findViewById(R.id.editButton);
        final FloatingActionButton cancelButton = findViewById(R.id.cancelButton);
        cancelButton.show();
        actionButton.hide();

        actionButton.setOnClickListener(v -> {
            Log.i(DEBUG_TAG, "Clicked on FAB: Done");
            Intent returnIntent = new Intent();
            returnIntent.putExtra("server", selDevice);
            returnIntent.putExtra("sensor", selSensor);
            returnIntent.putExtra("interval", selInterval);
            setResult(1, returnIntent);
            finish();
        });

        cancelButton.setOnClickListener(v -> {
            Log.i(DEBUG_TAG, "Clicked on FAB: Cancel");
            setResult(0);
            finish();
        });

        Toolbar toolbar = findViewById(R.id.sensoredit_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            setResult(0);
            onBackPressed();
        });
        final ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setTitle("Select Sensor");

        sensorLayout = findViewById(R.id.sensor_layout);
        intervalLayout = findViewById(R.id.interval_layout);
        deviceGroup = findViewById(R.id.chipDevices);
        sensorGroup = findViewById(R.id.chipSensors);
        intervalGroup = findViewById(R.id.chipInterval);

        sensorLayout.setVisibility(View.GONE);
        intervalLayout.setVisibility(View.GONE);

        setDevices();
    }

    @Override
    public void onBackPressed() {
        setResult(0, null);
        finish();
    }

    public void ChipUnselected(String currentGroup, String chip) {
        if (currentGroup.equals("devices")) {
            if (selDevice.equals(chip)) {
                sensorGroup.removeAllViews();
                intervalGroup.removeAllViews();
                sensorLayout.setVisibility(View.GONE);
                intervalLayout.setVisibility(View.GONE);
                actionButton.hide();
            }
        }
        if (currentGroup.equals("sensors")) {
            if (selSensor.equals(chip)) {
                intervalLayout.setVisibility(View.GONE);
                actionButton.hide();
            }
        }
        if (currentGroup.equals("intervals")) {
            if (selInterval == Integer.parseInt(chip.trim()))
                actionButton.hide();
        }
    }

    public void ChipSelected(String currentGroup, String chip) {
        if (currentGroup.equals("devices")) {
            selDevice = chip;
            sensorGroup.removeAllViews();
            intervalGroup.removeAllViews();
            actionButton.hide();
            sensorLayout.setVisibility(View.VISIBLE);
            intervalLayout.setVisibility(View.GONE);
            setSensors(chip);
        }
        if (currentGroup.equals("sensors")) {
            selSensor = chip;
            intervalGroup.removeAllViews();
            intervalLayout.setVisibility(View.VISIBLE);
            actionButton.hide();
            setIntervals(chip);
        }
        if (currentGroup.equals("intervals")) {
            selInterval = Integer.parseInt(chip.trim());
            topicString = getTopic(selDevice, selSensor, chip);
            actionButton.show();
        }
    }

    public void setCategoryChips(ChipGroup chipsPrograms, String group, List<Chips> chips) {
        for (Chips category : chips) {
            @SuppressLint("InflateParams") Chip mChip = (Chip) this.getLayoutInflater().inflate(R.layout.sensor_chip_category, null, false);
            mChip.setText(category.name);
            mChip.setTag(group);
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
                    getResources().getDisplayMetrics()
            );
            mChip.setPadding(paddingDp, 0, paddingDp, 0);
            mChip.setOnCheckedChangeListener((compoundButton, b) -> {
                if (b) {
                    Log.i(DEBUG_TAG, "Chip selected: Group: " + compoundButton.getTag() + " Chip pressed: " + compoundButton.getText());
                    ChipSelected(compoundButton.getTag().toString(), compoundButton.getText().toString());
                } else {
                    Log.i(DEBUG_TAG, "Chip unselected: Group: " + compoundButton.getTag() + " Chip pressed: " + compoundButton.getText());
                    ChipUnselected(compoundButton.getTag().toString(), compoundButton.getText().toString());
                }

            });
            chipsPrograms.addView(mChip);
        }
    }

    private void setDevices() {
        List<Chips> categories = new ArrayList<>();
        serverresult = updateServerList();

        for (SensorEvent result : serverresult) {
            Chips myChip = new Chips();
            if (result.getName().isEmpty()) {
                myChip.name = result.getServer();
            } else {
                myChip.name = result.getName();
            }
            myChip.active = result.isActive();
            myChip.selectable = true;
            categories.add(myChip);
        }
        setCategoryChips(deviceGroup, "devices", categories);
    }

    private void setSensors(String device) {
        List<Chips> categories = new ArrayList<>();

        for (SensorEvent result : serverresult) {
            if (result.getName().equals(device)) {
                device = result.getServer();
                break;
            }
        }

        List<Sensors> updateresult = updateSensorList(device);
        String sensorName;

        for (Sensors result : updateresult) {
            Chips myChip = new Chips();
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
        setCategoryChips(sensorGroup, "sensors", categories);
    }

    private void setIntervals(String sensor) {
        List<Chips> categories = new ArrayList<>();
        List<Sensors> intervallist = updateIntervalList(sensor);

        DecimalFormat formatter = new DecimalFormat("####");
        for (Sensors result : intervallist) {
            Chips myChip = new Chips();
            myChip.name = String.format("%4s", formatter.format(result.interval));
            myChip.active = true;
            myChip.selectable = result.active;
            categories.add(myChip);
        }
        setCategoryChips(intervalGroup, "intervals", categories);
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

    private List<Sensors> updateSensorList(String device) {
        sensorresult = sensorsHandler.getAllSensors(device);
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

    private List<Sensors> updateIntervalList(String sensorName) {
        final List<Sensors> upresult = new ArrayList<>();
        for (Sensors sensors : sensorresult) {
            if (sensors.name.equals(sensorName) || sensors.sensor.equals(sensorName)) {
                upresult.add(sensors);
            }
        }
        return upresult;
    }

    private String getTopic(String device, String sensor, String interval) {
        String topic = "/SE/";

        for (SensorEvent result : serverresult) {
            if (result.getName().equals(device) || result.getServer().equals(device)) {
                selDevice = result.getServer();
                topic += result.getServer();
                break;
            }
        }
        for (Sensors sensors : sensorresult) {
            if (sensors.name.equals(sensor) || sensors.sensor.equals(sensor)) {
                selSensor = sensors.sensor;
                topic += "/temp/" + sensors.sensor;
                break;
            }
        }
        selInterval = Integer.parseInt(interval.trim());
        topic += "/" + interval.trim();
        Log.i(DEBUG_TAG, "Topic: " + topic);
        return topic;
    }
}

class Chips {
    String name;
    boolean active;
    boolean selectable;
}