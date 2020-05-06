package com.holger.mashpit;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;

import com.activeandroid.query.Select;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.holger.mashpit.model.Config;

public class ConfEdit extends AppCompatActivity {
    private static final String DEBUG_TAG = "ConfEditActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String action = getIntent().getStringExtra("ACTION");
        final String type = getIntent().getStringExtra("adapter");
        final String server = getIntent().getStringExtra("server");
        final String name = getIntent().getStringExtra("name");

        EditText GPIO = null;
        EditText IRid = null;
        EditText IRcode = null;

        assert type != null;
        switch(type)
        {
            case "SSR":
                setContentView(R.layout.activity_confeditssr);
                GPIO=findViewById(R.id.confGPIO);
                break;

            case "PWR":
                setContentView(R.layout.activity_confeditpwr);
                IRid=findViewById(R.id.confIRid);
                IRcode=findViewById(R.id.confIRcode);
                break;

            default:
                return;
        }

        FloatingActionButton actionButton = findViewById(R.id.editButton);
        FloatingActionButton deleteButton = findViewById(R.id.deleteButton);
        FloatingActionButton cancelButton = findViewById(R.id.cancelButton);

        final Switch active = findViewById(R.id.confActive);
        final EditText topic = findViewById(R.id.confTopic);
        EditText temp = findViewById(R.id.confTemp);
        final Switch minmax = findViewById(R.id.confMaxTemp);
        EditText time = findViewById(R.id.confTime);
        EditText hysterese = findViewById(R.id.confHyst);
        final EditText confName =  findViewById(R.id.confName);

        final MaterialAlertDialogBuilder alertDialog;
        final MaterialAlertDialogBuilder deleteDialog;

        Toolbar toolbar = findViewById(R.id.confedit_toolbar);
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
        assert action != null;
        if (action.equals("edit")) {
            actionButton.show();
            Config conf = new Select().from(Config.class).where("name = ?", name).and("MPServer = ?",server).orderBy("topic ASC").executeSingle();
            if(conf==null)
            {
                return;
            }

            active.setChecked(false);
            if(conf.active)
            {
                active.setChecked(true);
            }

            minmax.setChecked(false);
            if(conf.minmax)
            {
                minmax.setChecked(true);
            }
            topic.setText(conf.topic);
            temp.setText(conf.temp);
            time.setText(conf.time);
            hysterese.setText(conf.hysterese);
            confName.setText(conf.name);
            confName.setEnabled(false);

            if(type.equals("SSR"))
            {
                assert GPIO != null;
                GPIO.setText(conf.GPIO);
            }
            if(type.equals("PWR"))
            {
                assert IRid != null;
                IRid.setText(conf.IRid);
                assert IRcode != null;
                IRcode.setText(conf.IRcode);
            }

            if((getIntent().getBooleanExtra("active",false)))
            {
                deleteButton.hide();
            }
        }

        if (action.equals("insert")) {
            confName.setEnabled(true);
            active.setChecked(true);
            minmax.setChecked(true);
            actionButton.show();
            deleteButton.hide();
        }

        cancelButton.show();

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                overridePendingTransition(0, 0);
                setResult(0, null);
                finish();
            }
        });

        alertDialog = new MaterialAlertDialogBuilder(this);
        alertDialog.setTitle(getString(R.string.pubConfig));
        alertDialog.setMessage(getString(R.string.confPublishAlert,name));
        alertDialog.setIcon(R.drawable.ic_launcher);

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = createConfIntent(action, type, server);
                setResult(1, intent);
                finish();
            }
        });

        deleteDialog = new MaterialAlertDialogBuilder(this);
        deleteDialog.setTitle(getString(R.string.pubConfig));
        deleteDialog.setMessage(getString(R.string.confdelAlert,name));
        deleteDialog.setIcon(R.drawable.ic_launcher);

        deleteDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        deleteDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = createConfIntent(action, type, server);
                setResult(2, intent);
                finish();
            }
        });

        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(DEBUG_TAG, "Clicked on FAB: Done");
                if(checkConfig(action,type, server)) {
                    String confname = ((EditText) findViewById(R.id.confName)).getText().toString();
                    alertDialog.setMessage(getString(R.string.confPublishAlert, confname));
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
                String confname = ((EditText) findViewById(R.id.confName)).getText().toString();
                alertDialog.setMessage(getString(R.string.confPublishAlert,confname));
                deleteDialog.show();
            }
        });
    }

    private boolean checkConfig(String action,String type, String server) {

        boolean flag=true;
        TextInputLayout confField;

        confField = findViewById(R.id.confTopicField);
        confField.setErrorEnabled(false);
        if(((EditText) findViewById(R.id.confTopic)).getText().toString().isEmpty())
        {
            confField.setError(getString(R.string.confTopicError));
            flag=false;
        }

        confField = findViewById(R.id.confNameField);
        confField.setErrorEnabled(false);
        if(((EditText) findViewById(R.id.confName)).getText().toString().isEmpty())
        {
            confField.setError(getString(R.string.confNameError));
            flag=false;
        }

        confField = findViewById(R.id.confTempField);
        confField.setErrorEnabled(false);
        if(((EditText) findViewById(R.id.confTemp)).getText().toString().isEmpty())
        {
            confField.setError(getString(R.string.confTempError));
            flag=false;
        }
        confField = findViewById(R.id.confTimeField);
        confField.setErrorEnabled(false);
        if(((EditText) findViewById(R.id.confTime)).getText().toString().isEmpty())
        {
            confField.setError(getString(R.string.confTimeError));
            flag=false;
        }
        confField = findViewById(R.id.confHystField);
        confField.setErrorEnabled(false);
        if(((EditText) findViewById(R.id.confHyst)).getText().toString().isEmpty())
        {
            confField.setError(getString(R.string.confHystError));
            flag=false;
        }

        if(action.equals("insert"))
        {
            confField = findViewById(R.id.confNameField);
            confField.setErrorEnabled(false);
            boolean exists = new Select()
                    .from(Config.class)
                    .where("name = ?", ((EditText) findViewById(R.id.confName)).getText().toString())
                    .and("MPServer = ?", server)
                    .exists();
            if(exists)
            {
                confField.setError(getString(R.string.confNameExistsError));
                flag=false;
            }
        }
        if(type.equals("SSR"))
        {
            confField = findViewById(R.id.confGPIOField);
            confField.setErrorEnabled(false);
            if (((EditText) findViewById(R.id.confGPIO)).getText().toString().isEmpty()) {
                confField.setError("Please enter a GPIO port");
                flag = false;
            }
        }
        if(type.equals("PWR"))
        {
            confField = findViewById(R.id.confIRidField);
            confField.setErrorEnabled(false);
            if (((EditText) findViewById(R.id.confIRid)).getText().toString().isEmpty()) {
                confField.setError(getString(R.string.confIRidError));
                flag = false;
            }
            confField = findViewById(R.id.confIRcodeField);
            confField.setErrorEnabled(false);
            if (((EditText) findViewById(R.id.confIRcode)).getText().toString().isEmpty()) {
                confField.setError(getString(R.string.confIRcodeError));
                flag = false;
            }
        }
        return flag;
    }

    private Intent createConfIntent(String action, String type, String server)
    {
        Intent intent = new Intent();
        intent.putExtra("ACTION",action);
        intent.putExtra("type",type);
        intent.putExtra("confName",((EditText) findViewById(R.id.confName)).getText().toString());
        intent.putExtra("server",server);
        Switch active = findViewById(R.id.confActive);
        Switch minmax = findViewById(R.id.confMaxTemp);
        if(active.isChecked())
        {
            intent.putExtra("confActive",true);
        }
        else {
            intent.putExtra("confActive",false);
        }
        if(minmax.isChecked())
        {
            intent.putExtra("confMinMax", true);
        }
        else {
            intent.putExtra("confMinMax", false);
        }
        intent.putExtra("confTopic", ((EditText) findViewById(R.id.confTopic)).getText().toString());
        intent.putExtra("confTemp", ((EditText) findViewById(R.id.confTemp)).getText().toString());
        intent.putExtra("confTime", ((EditText) findViewById(R.id.confTime)).getText().toString());
        intent.putExtra("confHyst", ((EditText) findViewById(R.id.confHyst)).getText().toString());

        intent.putExtra("confGPIO","");
        intent.putExtra("confIRid","");
        intent.putExtra("confIRcode","");
        if(type.equals("SSR"))
        {
            intent.putExtra("confGPIO",((EditText) findViewById(R.id.confGPIO)).getText().toString());
        }
        if(type.equals("PWR"))
        {
            intent.putExtra("confIRid",((EditText) findViewById(R.id.confIRid)).getText().toString());
            intent.putExtra("confIRcode",((EditText) findViewById(R.id.confIRcode)).getText().toString());
        }
        return intent;
    }
}
