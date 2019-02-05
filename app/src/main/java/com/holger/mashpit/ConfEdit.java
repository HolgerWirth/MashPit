package com.holger.mashpit;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.holger.mashpit.tools.TextValidator;
import com.melnykov.fab.FloatingActionButton;

public class ConfEdit extends AppCompatActivity {

    private static final String DEBUG_TAG = "ConfEditActivity";
    FloatingActionButton actionButton;
    FloatingActionButton deleteButton;
    FloatingActionButton cancelButton;
    int position;
    String action="";
    boolean text1=true;
    boolean text2=true;
    String name;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_conf_edit);

        final Switch active = findViewById(R.id.confActive);
        EditText topic = findViewById(R.id.confTopic);
        EditText temp = findViewById(R.id.confTemp);
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

        action = getIntent().getStringExtra("ACTION");
        Log.i(DEBUG_TAG, "Started with action: " + action);
        if (action.equals("edit")) {
            actionButton.setVisibility(View.VISIBLE);
            buttonCheck(1, false);
            buttonCheck(2, false);
            position = getIntent().getIntExtra("pos", 0);
            if(getIntent().getStringExtra("confActive").equals("1"))
            {
                active.setChecked(true);
            }
            else
            {
                active.setChecked(false);
            }
            topic.setText(getIntent().getStringExtra("confTopic"));
            temp.setText(getIntent().getStringExtra("confTemp"));
            time.setText(getIntent().getStringExtra("confTime"));
            hysterese.setText(getIntent().getStringExtra("confHyst"));
            confName.setText(getIntent().getStringExtra("confName"));
            confName.setEnabled(false);
            name = getIntent().getStringExtra("confName");

            deleteButton.setVisibility(View.VISIBLE);
        }
        if (action.equals("insert")) {
            confName.setEnabled(true);
            actionButton.setVisibility(View.VISIBLE);
        }

        cancelButton.setVisibility(View.VISIBLE);

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
                Intent intent = createConfIntent();
                setResult(3, intent);
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
            actionButton.setVisibility(View.GONE);
        }
        else
        {
            actionButton.setVisibility(View.VISIBLE);
        }
    }

    private Intent createConfIntent()
    {
        Intent intent = new Intent();
        intent.putExtra("ACTION",action);
        intent.putExtra("pos",position);

        intent.putExtra("confName",((EditText) findViewById(R.id.confName)).getText().toString());
        Switch active = findViewById(R.id.confActive);
        if(active.isChecked())
        {
            intent.putExtra("confActive", "1");
        }
        else {
            intent.putExtra("confActive", "0");
        }
        intent.putExtra("confTopic", ((EditText) findViewById(R.id.confTopic)).getText().toString());
        intent.putExtra("confTemp", ((EditText) findViewById(R.id.confTemp)).getText().toString());
        intent.putExtra("confTime", ((EditText) findViewById(R.id.confTime)).getText().toString());
        intent.putExtra("confHyst", ((EditText) findViewById(R.id.confHyst)).getText().toString());

        return intent;
    }
}
