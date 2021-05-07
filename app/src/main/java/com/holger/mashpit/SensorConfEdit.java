package com.holger.mashpit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.holger.mashpit.events.SensorEvent;
import com.holger.mashpit.model.Sensors;
import com.holger.mashpit.model.SensorsHandler;
import com.holger.mashpit.tools.SensorPublishMQTT;
import com.holger.mashpit.tools.SnackBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class SensorConfEdit extends AppCompatActivity implements SensorConfEditAdapter.IntervalChangeCallback,SensorPublishMQTT.OnPublishConfiguration {
    private static final String DEBUG_TAG = "SensorConfEdit";
    SensorConfEditAdapter sa;
    RecyclerView intervalList;
    MaterialAlertDialogBuilder alertDialog;
    CoordinatorLayout coordinatorLayout;
    String sensor;
    String type;
    String name;
    String server;
    int GPIO;
    int ALT;
    List<Sensors> sensors;
    Context context = this;
    String action;

    int resultCode=0;
    boolean intervalInsert=false;
    FloatingActionButton actionButton;
    SnackBar snb;
    EditText gpio = null;
    EditText altField = null;
    EditText typeField = null;
    AutoCompleteTextView typeDropdown;
    SensorsHandler sensorsHandler;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sensorsHandler = new SensorsHandler();
        action = getIntent().getStringExtra("ACTION");
        sensor = getIntent().getStringExtra("sensor");
        type = getIntent().getStringExtra("type");
        name = getIntent().getStringExtra("name");
        server = getIntent().getStringExtra("server");
        GPIO = getIntent().getIntExtra("GPIO",0);
        ALT = getIntent().getIntExtra("ALT",0);

        alertDialog = new MaterialAlertDialogBuilder(this);

        switch (type) {
            case "ds18b20":
                setContentView(R.layout.activity_sensoredit_ds18b20);
                coordinatorLayout = findViewById(R.id.layout_ds18b20);
                typeField = findViewById(R.id.sensorType);
                typeField.setText(type);
                typeField.setEnabled(false);
                break;

            case "bme280":
                setContentView(R.layout.activity_sensoredit_bme280);
                typeField = findViewById(R.id.sensorType);
                typeField.setText(type);
                typeField.setEnabled(false);
                altField = findViewById(R.id.sensorALT);
                altField.setText(Integer.toString(ALT));
                altField.setEnabled(false);
                coordinatorLayout = findViewById(R.id.layout_bme280);
                break;

            case "bh1750":
                setContentView(R.layout.activity_sensoredit_i2c);
                typeField = findViewById(R.id.sensorType);
                typeField.setText(type);
                typeField.setEnabled(false);
                coordinatorLayout = findViewById(R.id.layout_i2c);
                break;

            default:
                setContentView(R.layout.activity_sensoredit_gpio);
                gpio = findViewById(R.id.sensorGPIO);
                coordinatorLayout = findViewById(R.id.layout_gpio);
                gpio.setEnabled(false);
                gpio.setText(Integer.toString(GPIO));
                typeDropdown = findViewById(R.id.sensorTypeDropdown);
                typeDropdown.setText(type,true);
                typeDropdown.setEnabled(false);
                break;
        }

        actionButton = findViewById(R.id.editButton);
        final FloatingActionButton cancelButton = findViewById(R.id.cancelButton);
        final FloatingActionButton addButton = findViewById(R.id.intervalfabadd);

        final EditText sensorId = findViewById(R.id.sensorId);
        final EditText sensorName = findViewById(R.id.sensorName);

        snb = new SnackBar(coordinatorLayout);

        Toolbar toolbar = findViewById(R.id.sensoredit_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        final ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setTitle(name);

        intervalList = findViewById(R.id.sensorIntervalList);
        intervalList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        intervalList.setLayoutManager(llm);

        Log.i(DEBUG_TAG, "Started with action: " + action + " and type: " + type);

        sensors = refreshIntervalList(sensor);
        sa = new SensorConfEditAdapter(sensors);
        sa.setOnItemClickListener(this);
        intervalList.setAdapter(sa);

        sensorName.setText(name);
        sensorId.setText(sensor);
        sensorId.setEnabled(false);

        sensorName.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                Log.i(DEBUG_TAG, "Name edited!");
                assert action != null;
                if (action.equals("edit")) {
                    addButton.hide();
                    cancelButton.show();
                }
                if (!editable.toString().isEmpty()) {
                    name = editable.toString();
                    if(!sensors.isEmpty()) {
                        actionButton.show();
                    }
                    else
                    {
                        addButton.show();
                    }
                }
            }
        });

        if (action.equals("insert")) {
            if (type.equals("gpio")) {
                type="";
                Log.i(DEBUG_TAG, "Add new " + type);
                typeDropdown.setEnabled(true);
                String[] GPIODevices = new String[] {"dht11","oled","Test 3"};

                ArrayAdapter<String> adapter =
                        new ArrayAdapter<>(
                                context,
                                R.layout.sensortype_dropdown_item,
                                GPIODevices);

                AutoCompleteTextView editTextFilledExposedDropdown =
                        typeDropdown;
                editTextFilledExposedDropdown.setAdapter(adapter);
                typeDropdown.setText("",true);

                typeDropdown.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        Log.i(DEBUG_TAG, "Sensor type changed");
                        type=editable.toString();
                        sensor=type;
                        sensorId.setText(sensor);
                    }
                });

                gpio.setEnabled(true);
                gpio.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        Log.i(DEBUG_TAG, "GPIO port changed");
                        GPIO = 0;
                        if (!editable.toString().equals("0")) {
                            gpio.setError(null);
                            sensor = type + "-" + editable.toString();
                            sensorId.setText(sensor);
                            if (!editable.toString().isEmpty()) {
                                GPIO = Integer.parseInt(editable.toString());
                            }
                        } else {
                            gpio.setError(getString(R.string.devGPIOWarning));
                        }
                    }
                });
                Log.i(DEBUG_TAG, "Add new DHT11");
            }
        }

        if(sensors.isEmpty()) {
            if (type.equals("bme280")) {
                altField.setEnabled(true);
                altField.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        Log.i(DEBUG_TAG, "Altitude changed");
                        ALT = 0;
                        if (!editable.toString().equals("0")) {
                            altField.setError(null);
                            if (!editable.toString().isEmpty()) {
                                ALT = Integer.parseInt(editable.toString());
                            }
                        } else {
                            altField.setError(getString(R.string.devGPIOWarning));
                        }
                    }
                });
            }
        }
        cancelButton.show();
        addButton.show();
        actionButton.hide();

        toolbar.setNavigationOnClickListener(v -> {
            overridePendingTransition(0, 0);
            setResult(resultCode, null);
            finish();
        });

        addButton.setOnClickListener(view -> {
            Log.i(DEBUG_TAG, "Clicked on add FAB!");
            Sensors newInterval = new Sensors();
            newInterval.server = server;
            newInterval.type = type;
            newInterval.name = name;
            newInterval.active = false;
            newInterval.interval = 0;
            newInterval.sensor = sensor;
            newInterval.port=GPIO;
            sensors.add(0, newInterval);
            intervalInsert = true;
            sa.notifyItemInserted(0);
            addButton.hide();
            cancelButton.show();
            actionButton.hide();
        });

        actionButton.setOnClickListener(v -> {
            Log.i(DEBUG_TAG, "Clicked on FAB: Done");
            if(type.equals("dht11"))
            {
                if(action.equals("insert")) {
                    if (GPIO == 0) {
                        gpio.setError(getString(R.string.devGPIOWarning));
                        return;
                    }
                    if (sensorsHandler.checkGPIO(server, GPIO)) {
                        gpio.setError(getString(R.string.devGPIOExists));
                        return;
                    }
                }
            }
            alertDialog.setTitle(getString(R.string.pubConfig));
            alertDialog.setMessage(getString(R.string.confPublishAlert, name));
            alertDialog.setIcon(R.drawable.ic_launcher);

            alertDialog.setNegativeButton("Cancel", (dialog, which) -> {
                Log.i(DEBUG_TAG, "Clicked on OK! - Cancel");
                sa.setIntervalList(refreshIntervalList(sensor));
                sa.notifyDataSetChanged();
            });

            alertDialog.setPositiveButton("OK", (dialog, which) -> {
                Log.i(DEBUG_TAG, "Clicked on OK! - OK");
                if(intervalInsert) {
                    Log.i(DEBUG_TAG, "New interval added!");
                    sa.getItem(0).name = name;
                    sa.getItem(0).port = GPIO;
                    sa.getItem(0).alt = ALT;
                    SensorPublishMQTT pubMQTT = new SensorPublishMQTT(context);
                    pubMQTT.PublishSensorConf(server, sensor, type, sa.getItem(0).interval, createJSONConfig(0));
                }
                else {
                    Log.i(DEBUG_TAG, "Number of defined intervals: " + sa.getItemCount());
                    for (int i = 0; i < sa.getItemCount(); i++) {
                        SensorPublishMQTT pubMQTT = new SensorPublishMQTT(context);
                        if (!(name.equals(sa.getItem(i).name))) {
                            Log.i(DEBUG_TAG, "Sensor name change at position: " + i);
                            sa.getItem(i).name = name;
                            pubMQTT.PublishSensorConf(server, sensor, type, sa.getItem(i).interval, createJSONConfig(i));
                        }
                    }
                }
                sensors.remove(0);
                sa.notifyItemRemoved(0);
                intervalList.setAdapter(sa);
                intervalInsert = false;
                cancelButton.hide();
                actionButton.hide();
                addButton.show();
                resultCode = 1;
            });
            alertDialog.show();
            sa.notifyDataSetChanged();
        });

        cancelButton.setOnClickListener(v -> {
            Log.i(DEBUG_TAG, "Clicked on FAB: Cancel");
            if (intervalInsert) {
                sensors.remove(0);
                sa.notifyItemRemoved(0);
                cancelButton.hide();
                actionButton.hide();
                addButton.show();
                intervalInsert = false;
            } else {
                setResult(resultCode,null);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        setResult(resultCode,null);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        Log.i(DEBUG_TAG, "onStart()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        snb.stopEvents();
        Log.i(DEBUG_TAG, "onStop()");
    }

    private List<Sensors> refreshIntervalList(String sensor)
    {
        List<Sensors> intervalList = sensorsHandler.getIntervals(server,sensor);
        return  intervalList;
    }

    @Override
    public void onIntervalDeleted(final int position) {
        Log.i(DEBUG_TAG, "Clicked on Delete! - Pos: "+position);
        alertDialog.setTitle(getString(R.string.pubConfig));
        alertDialog.setMessage(getString(R.string.confdelIntervalAlert,Integer.toString(sensors.get(position).interval), name));
        alertDialog.setIcon(R.drawable.ic_launcher);

        alertDialog.setNegativeButton("Cancel", (dialog, which) -> Log.i(DEBUG_TAG, "Clicked on Delete! - Cancel"));

        alertDialog.setPositiveButton("OK", (dialog, which) -> {
            Log.i(DEBUG_TAG, "Clicked on Delete! - OK");

            SensorPublishMQTT pubMQTT = new SensorPublishMQTT(context);
            pubMQTT.PublishSensorConf(server, sensor, type, sa.getItem(position).interval, position);
        });
        alertDialog.show();
    }

    @Override
    public void onIntervalCreated(int interval,boolean active) {
        Log.i(DEBUG_TAG, "New interval created!");
            actionButton.show();
    }

    @Override
    public void onIntervalActivated(final int position, boolean active) {
        if (active) {
            Log.i(DEBUG_TAG, "Clicked on Switch position "+position+": active");
            sa.getItem(position).active=true;
        } else {
            Log.i(DEBUG_TAG, "Clicked on Switch position "+position+": not active");
            sa.getItem(position).active=false;
        }

        if (!intervalInsert) {
            alertDialog.setTitle(getString(R.string.pubConfig));
            alertDialog.setMessage(getString(R.string.confPublishAlert, name));
            alertDialog.setNegativeButton("Cancel", (dialog, which) -> {
                Log.i(DEBUG_TAG, "Clicked on Publish! - Cancel");
                sa.setIntervalList(refreshIntervalList(sensor));
                sa.notifyDataSetChanged();
            });

            alertDialog.setPositiveButton("OK", (dialog, which) -> {
                Log.i(DEBUG_TAG, "Clicked on Publish! - OK");

                SensorPublishMQTT pubMQTT = new SensorPublishMQTT(context);
                pubMQTT.PublishSensorConf(server, sensor, type, sa.getItem(position).interval, createJSONConfig(position));
            });

            alertDialog.show();
        }
    }

    private String createJSONConfig(int position)
    {
        JSONObject obj = new JSONObject();
        try {
            obj.put("type", type);
            obj.put("name", sa.getItem(position).name);
            assert type != null;
            if(type.equals("dht11"))
            {
                obj.put("PIN",sa.getItem(position).port);
            }
            if(type.equals("bme280"))
            {
                obj.put("ALT",sa.getItem(position).alt);
            }
            obj.put("active", sa.getItem(position).active);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i(DEBUG_TAG, "Config: " + obj.toString());
        return obj.toString();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getSensorStatusEvent(SensorEvent sensorEvent) {
        String xSensor=sensorEvent.getSensor();
        Log.i(DEBUG_TAG, "SensorEvent arrived: " + xSensor);
        if(sensor.equals(xSensor)) {
            sa.setIntervalList(refreshIntervalList(xSensor));
            sa.notifyDataSetChanged();
        }
    }

    @Override
    public void PublishConfigurationCallback(Boolean success,int position) {
        if (success) {
            snb.displayInfo(R.string.pubConfOK);
            if(position>=0)
            {
                sensors.remove(position);
                sa.notifyItemRemoved(position);
            }
            resultCode = 1;
        } else {
            snb.displayInfo(R.string.pubConfNOK);
        }
        sa.notifyDataSetChanged();
    }
}
