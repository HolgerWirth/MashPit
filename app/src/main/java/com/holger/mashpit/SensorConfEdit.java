package com.holger.mashpit;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.activeandroid.query.Select;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.holger.mashpit.model.Sensors;

public class SensorConfEdit extends AppCompatActivity {
    private static final String DEBUG_TAG = "SensorConfEdit";

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String action = getIntent().getStringExtra("ACTION");
        final String sensor = getIntent().getStringExtra("sensor");
        final int interval = getIntent().getIntExtra("interval",0);
        final String type = getIntent().getStringExtra("type");
        final String name = getIntent().getStringExtra("name");
        final String server = getIntent().getStringExtra("server");

        EditText gpio = null;

        switch(type)
        {
            case "ds18b20":
                setContentView(R.layout.activity_sensoredit_ds18b20);
                break;

            case "bme280":
                setContentView(R.layout.activity_sensoredit_bme280);
                gpio =  findViewById(R.id.sensorGPIO);
                break;

            default:
                return;
        }

        FloatingActionButton actionButton = findViewById(R.id.editButton);
        FloatingActionButton deleteButton = findViewById(R.id.deleteButton);
        FloatingActionButton cancelButton = findViewById(R.id.cancelButton);

        final Switch active = findViewById(R.id.sensorActive);
        EditText interval_edit = findViewById(R.id.sensorInterval);
        EditText sensorId = findViewById(R.id.sensorId);
        final EditText sensorName =  findViewById(R.id.sensorName);

        final AlertDialog.Builder alertDialog;
        final AlertDialog.Builder deleteDialog;

        Toolbar toolbar = findViewById(R.id.sensoredit_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        final ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setTitle(name);

        Log.i(DEBUG_TAG, "Started with action: " + action+" and type: "+type);
        if (action.equals("edit")) {
            actionButton.show();
            Sensors sensors = new Select().from(Sensors.class).where("server = ?", server)
                    .and("sensor = ?",sensor)
                    .and("interval=?",interval)
                    .orderBy("name ASC").executeSingle();

            if(sensors==null)
            {
                return;
            }

            active.setChecked(false);
            if(sensors.active)
            {
                active.setChecked(true);
            }

            interval_edit.setText(Integer.toString(sensors.interval));
            sensorName.setText(sensors.name);
            sensorId.setText(sensors.sensor);
            sensorId.setEnabled(false);
            interval_edit.setEnabled(false);
            if(type.equals("bme280"))
            {
                gpio.setText(Integer.toString(sensors.port));
                gpio.setEnabled(false);
            }

            if((getIntent().getBooleanExtra("active",false)))
            {
                deleteButton.hide();
            }
        }

        /*
        if (action.equals("insert")) {
            confName.setEnabled(true);
            active.setChecked(true);
            minmax.setChecked(true);
            actionButton.show();
            deleteButton.hide();
        }

*/
        cancelButton.show();

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                overridePendingTransition(0, 0);
                setResult(0, null);
                finish();
            }
        });

        alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(getString(R.string.pubConfig));
        alertDialog.setMessage(getString(R.string.confPublishAlert,name));
        alertDialog.setIcon(R.drawable.ic_launcher);

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = createConfIntent(action, type, server,sensor,interval);
                setResult(1, intent);
                finish();
            }
        });

        deleteDialog = new AlertDialog.Builder(this);
        deleteDialog.setTitle(getString(R.string.pubConfig));
        deleteDialog.setMessage(getString(R.string.confdelAlert,name));
        deleteDialog.setIcon(R.drawable.ic_launcher);

        deleteDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        deleteDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = createConfIntent(action, type, server,sensor, interval);
                setResult(2, intent);
                finish();
            }
        });

        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(DEBUG_TAG, "Clicked on FAB: Done");
                if(checkConfig(action,type, server)) {
                    String sensorname=((EditText) findViewById(R.id.sensorName)).getText().toString();
                    if(sensorname.isEmpty()) {
                        sensorname = ((EditText) findViewById(R.id.sensorId)).getText().toString();
                    }
                    alertDialog.setMessage(getString(R.string.confPublishAlert, sensorname));
                    alertDialog.show();
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(DEBUG_TAG, "Clicked on FAB: Cancel");
                setResult(0, null);
                finish();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(DEBUG_TAG, "Clicked on FAB: Delete");
                    String sensorname=((EditText) findViewById(R.id.sensorName)).getText().toString();
                    if(sensorname.isEmpty()) {
                        sensorname = ((EditText) findViewById(R.id.sensorId)).getText().toString();
                    }
                alertDialog.setMessage(getString(R.string.confPublishAlert,sensorname));
                deleteDialog.show();
            }
        });
    }

    private boolean checkConfig(String action,String type, String server) {

        boolean flag=true;
        TextInputLayout sensorField;

        sensorField = findViewById(R.id.sensorIntervalField);
        sensorField.setErrorEnabled(false);
        if(((EditText) findViewById(R.id.sensorInterval)).getText().toString().isEmpty())
        {
            sensorField.setError(getString(R.string.sensorIntervalError));
            flag=false;
        }
        if(type.equals("bme280")) {
            if (((EditText) findViewById(R.id.sensorGPIO)).getText().toString().isEmpty()) {
                flag = false;
            }
        }
        return flag;
    }

    private Intent createConfIntent(String action, String type, String server, String sensor, int interval)
    {
        Intent intent = new Intent();
        intent.putExtra("ACTION",action);
        intent.putExtra("type",type);
        intent.putExtra("sensor",sensor);
        intent.putExtra("interval",interval);
        intent.putExtra("name",((EditText) findViewById(R.id.sensorName)).getText().toString());
        intent.putExtra("server",server);
        Switch active = findViewById(R.id.sensorActive);
        if(active.isChecked())
        {
            intent.putExtra("active",true);
        }
        else {
            intent.putExtra("active",false);
        }
        intent.putExtra("GPIO","");
        if(type.equals("bme280"))
        {
            intent.putExtra("GPIO",Integer.parseInt(((EditText) findViewById(R.id.sensorGPIO)).getText().toString()));
        }
        if(type.equals("PWR"))
        {
            intent.putExtra("confIRid",((EditText) findViewById(R.id.confIRid)).getText().toString());
            intent.putExtra("confIRcode",((EditText) findViewById(R.id.confIRcode)).getText().toString());
        }
        return intent;
    }
}
