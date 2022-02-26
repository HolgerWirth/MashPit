package com.holger.mashpit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.holger.mashpit.tools.SensorPublishMQTT;
import com.holger.mashpit.tools.SnackBar;

import org.json.JSONException;
import org.json.JSONObject;

public class SensorEventEditActvity extends AppCompatActivity implements SensorPublishMQTT.OnPublishConfiguration {
    private static final String DEBUG_TAG = "SensorEventEdit";
    MaterialAlertDialogBuilder alertDialog;
    CoordinatorLayout coordinatorLayout;
    String sensor;
    String type;
    String event;
    String server;
    String family;
    int GPIO;
    String reg;
    String dir;
    int hyst;
    Context context = this;
    String action;
    String hw;

    FloatingActionButton actionButton;
    FloatingActionButton deleteButton;
    SnackBar snb;
    boolean active;
    boolean eventChanged;
    SwitchMaterial eventActive;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        family = getIntent().getStringExtra("family");
        action = getIntent().getStringExtra("ACTION");
        sensor = getIntent().getStringExtra("sensor");
        type = getIntent().getStringExtra("type");
        event = getIntent().getStringExtra("event");
        server = getIntent().getStringExtra("server");
        GPIO = getIntent().getIntExtra("GPIO",0);
        dir = getIntent().getStringExtra("dir");
        active = getIntent().getBooleanExtra("active",false);
        reg = getIntent().getStringExtra(("reg"));
        hyst=getIntent().getIntExtra("hyst",0);
        alertDialog = new MaterialAlertDialogBuilder(this);
        String name = getIntent().getStringExtra("name");
        hw = getIntent().getStringExtra("hw");

        setContentView(R.layout.activity_eventedit);
        coordinatorLayout = findViewById(R.id.layout_eventedit);
        snb = new SnackBar(coordinatorLayout);

        ImageView eventIcon = findViewById(R.id.eventIcon);
        EditText eventDir = findViewById(R.id.eventDir);
        AutoCompleteTextView eventType = findViewById(R.id.eventTypeDropdown);
        AutoCompleteTextView eventReg = findViewById(R.id.eventRegDropdown);
        final EditText eventValue = findViewById(R.id.eventValue);
        final EditText eventHyst = findViewById(R.id.eventHyst);
        final EditText eventName = findViewById(R.id.eventName);
        eventActive = findViewById(R.id.eventActive);
        actionButton = findViewById(R.id.editButton);
        final FloatingActionButton cancelButton = findViewById(R.id.cancelButton);
        deleteButton = findViewById(R.id.deleteButton);

        eventChanged=false;
        Resources res = context.getResources();
        int resID = res.getIdentifier("ic_event_in_icon", "drawable", "com.holger.mashpit");
        eventDir.setText("INBOUND");
        if(dir.equals("OUT"))
        {
            resID = res.getIdentifier("ic_event_out_icon", "drawable", "com.holger.mashpit");
            eventDir.setText("OUTBOUND");
        }

        eventIcon.setImageResource(resID);

        String[] MCPRegister = new String[] {"GPA","GPB"};
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        context,
                        R.layout.sensortype_dropdown_item,
                        MCPRegister);

        eventReg.setAdapter(adapter);
        eventReg.setText(reg,true);

        eventReg.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                Log.i(DEBUG_TAG, "MCP register changed");
                reg=editable.toString();
                eventChanged=true;
                actionButton.show();
            }
        });

        String[] MCPType = new String[] {"ONOFF","ON","SWITCH"};
        ArrayAdapter<String> adapterType =
                new ArrayAdapter<>(
                        context,
                        R.layout.sensortype_dropdown_item,
                        MCPType);

        eventType.setAdapter(adapterType);
        eventType.setText(type,true);

        eventType.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                Log.i(DEBUG_TAG, "Event type changed");
                type=editable.toString();
                eventChanged=true;
                actionButton.show();
            }
        });

        String abTitle=name+"/"+event;
        eventDir.setEnabled(false);
        if(action.equals("edit"))
        {
            eventName.setText(event);
            eventName.setEnabled(false);
            eventValue.setText(Integer.toString(GPIO));
            eventHyst.setText(Integer.toString(hyst));
            eventType.setEnabled(true);
            eventReg.setEnabled(true);
            eventActive.setChecked(active);
            cancelButton.show();
            deleteButton.show();
            actionButton.hide();

            if(eventActive.isChecked())
            {
                eventValue.setEnabled(false);
                eventHyst.setEnabled(false);
                eventType.setEnabled(false);
                eventReg.setEnabled(false);
                deleteButton.hide();
            }
        }

        if(action.equals("new"))
        {
            abTitle=name+"/"+"New Event";
            eventReg.setEnabled(true);
            eventType.setEnabled(true);
            eventName.setEnabled(true);
            cancelButton.show();
            actionButton.hide();
            deleteButton.hide();
        }

        Toolbar toolbar = findViewById(R.id.eventedit_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        final ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setTitle(abTitle);

        Log.i(DEBUG_TAG, "Started with action: " + action + " and type: " + type);

        eventActive.setOnClickListener(view -> {
            Log.i(DEBUG_TAG, "onClick() eventActive");
            active=eventActive.isChecked();
            deleteButton.hide();
            eventChanged=true;
            actionButton.show();
        });

        eventName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                Log.i(DEBUG_TAG, "Name edited!");
                eventChanged=true;
                actionButton.show();
                assert action != null;
                if (action.equals("edit")) {
                    cancelButton.show();
                }
                if (!editable.toString().isEmpty()) {
                    event = editable.toString();
                }
            }
        });

        eventValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                eventChanged=true;
                actionButton.show();
            }
        });

        eventHyst.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                eventChanged=true;
                actionButton.show();
            }
        });

        toolbar.setNavigationOnClickListener(v -> {
            overridePendingTransition(0, 0);
            setResult(0, null);
            finish();
        });

        actionButton.setOnClickListener(v -> {
            Log.i(DEBUG_TAG, "Clicked on Done");
            if (eventChanged) {
                hyst= Integer.parseInt(String.valueOf(eventHyst.getText()));
                GPIO = Integer.parseInt(String.valueOf(eventValue.getText()));
                alertDialog.setTitle(getString(R.string.pubConfig));
                alertDialog.setMessage(getString(R.string.confPublishAlert, event));
                alertDialog.setIcon(R.drawable.ic_launcher);

                alertDialog.setNegativeButton("Cancel", (dialog, which) -> Log.i(DEBUG_TAG, "Clicked on OK! - Cancel"));

                alertDialog.setPositiveButton("OK", (dialog, which) -> {
                    Log.i(DEBUG_TAG, "Clicked on OK! - OK");
                    SensorPublishMQTT pubMQTT = new SensorPublishMQTT(context);
                    pubMQTT.PublishEventConf(server, dir, hw, event, createJSONConfig());
                    setResult(1, null);
                    finish();
                });
                alertDialog.show();
            }
        });

        deleteButton.setOnClickListener(v -> {
            Log.i(DEBUG_TAG, "Clicked on Delete");
            alertDialog.setTitle(getString(R.string.pubConfig));
            alertDialog.setMessage(getString(R.string.confPublishAlert, event));
            alertDialog.setIcon(R.drawable.ic_launcher);

            alertDialog.setNegativeButton("Cancel", (dialog, which) -> Log.i(DEBUG_TAG, "Clicked on OK! - Cancel"));

            alertDialog.setPositiveButton("OK", (dialog, which) -> {
                Log.i(DEBUG_TAG, "Clicked on OK! - OK");
                SensorPublishMQTT pubMQTT = new SensorPublishMQTT(context);
                pubMQTT.PublishEventConf(server, dir, hw, event,"");
                setResult(1, null);
                finish();
            });
            alertDialog.show();
        });

        cancelButton.setOnClickListener(v -> {
            Log.i(DEBUG_TAG, "Clicked on FAB: Cancel");
                setResult(0,null);
                finish();
            });
    }

    @Override
    public void onBackPressed() {
        setResult(0,null);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        snb.stopEvents();
        Log.i(DEBUG_TAG, "onStop()");
    }

    private String createJSONConfig()
    {
        JSONObject obj = new JSONObject();
        try {
            obj.put("reg", reg);
            obj.put("pin", GPIO);
            obj.put("hyst", hyst);
            obj.put("evtype", type);
            obj.put("active", active);
            obj.put("mcp", sensor);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i(DEBUG_TAG, "Config: " + obj);
        return obj.toString();
    }

    @Override
    public void PublishConfigurationCallback(Boolean success) {
        if (success) {
            snb.displayInfo(R.string.pubConfOK);
        } else {
            snb.displayInfo(R.string.pubConfNOK);
        }
    }
}
