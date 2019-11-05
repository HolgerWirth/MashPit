package com.holger.mashpit;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.holger.mashpit.model.Config;
import com.holger.mashpit.tools.TextValidator;

public class ConfEdit extends AppCompatActivity {

    private static final String DEBUG_TAG = "ConfEditActivity";
    FloatingActionButton actionButton;
    FloatingActionButton deleteButton;
    FloatingActionButton cancelButton;
    int position;
    String action="";
    String type = "";
    boolean text1=true;
    boolean text2=true;
    String name;
    String server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        action = getIntent().getStringExtra("ACTION");
        type = getIntent().getStringExtra("adapter");
        server = getIntent().getStringExtra("server");

        EditText GPIO = null;
        EditText IRid = null;
        EditText IRcode = null;

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
        }

        final Switch active = findViewById(R.id.confActive);
        EditText topic = findViewById(R.id.confTopic);
        EditText temp = findViewById(R.id.confTemp);
        final Switch minmax = findViewById(R.id.confMaxTemp);
        EditText time = findViewById(R.id.confTime);
        EditText hysterese = findViewById(R.id.confHyst);
        final EditText confName =  findViewById(R.id.confName);

        deleteButton = findViewById(R.id.deleteButton);
        cancelButton = findViewById(R.id.cancelButton);
        actionButton = findViewById(R.id.editButton);

        buttonCheck(1, text1);
        buttonCheck(2, text2);

        final AlertDialog.Builder alertDialog;
        final AlertDialog.Builder deleteDialog;

        Log.i(DEBUG_TAG, "Started with action: " + action+" and type: "+type);
        if (action.equals("edit")) {
            actionButton.show();
            buttonCheck(1, false);
            buttonCheck(2, false);
            position = getIntent().getIntExtra("pos", 0);
            name = getIntent().getStringExtra("name");

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
        }

        cancelButton.show();

        Toolbar toolbar = findViewById(R.id.confedit_toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                overridePendingTransition(0, 0);
                setResult(0, null);
                finish();
            }
        });

        topic.addTextChangedListener(new TextValidator(topic) {
            @Override
            public void validate(TextView textView, String text) {
                buttonCheck(1, text.isEmpty());
            }
        });

        time.addTextChangedListener(new TextValidator(time) {
            @Override
            public void validate(TextView textView, String text) {
                buttonCheck(2, text.isEmpty());
                if (!text.isEmpty()) {
                    if (Integer.parseInt(text) == 0) {
                        buttonCheck(2, true);
                    }
                }
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
                Intent intent = createConfIntent();
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
                Intent intent = createConfIntent();
                setResult(2, intent);
                finish();
            }
        });

        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(DEBUG_TAG, "Clicked on FAB: Done");
                name = ((EditText) findViewById(R.id.confName)).getText().toString();
                alertDialog.setMessage(getString(R.string.confPublishAlert,name));
                alertDialog.show();
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
                name = ((EditText) findViewById(R.id.confName)).getText().toString();
                alertDialog.setMessage(getString(R.string.confPublishAlert,name));
                deleteDialog.show();
            }
        });
    }

    private void buttonCheck(int button,boolean check)
    {
        if(button==1)
        {
            text1=check;
        }
        if(button==2)
        {
            text2=check;
        }

        if(text1 || text2)
        {
            actionButton.hide();
        }
        else
        {
            actionButton.show();
        }
    }

    private Intent createConfIntent()
    {
        Intent intent = new Intent();
        intent.putExtra("ACTION",action);
        intent.putExtra("pos",position);
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
