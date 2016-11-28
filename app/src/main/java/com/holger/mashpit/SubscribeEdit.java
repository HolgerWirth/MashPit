package com.holger.mashpit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.holger.mashpit.tools.TextValidator;
import com.melnykov.fab.FloatingActionButton;

public class SubscribeEdit extends AppCompatActivity {

    private static final String DEBUG_TAG = "SubscribeEditActivity";
    FloatingActionButton actionButton;
    FloatingActionButton deleteButton;
    FloatingActionButton cancelButton;
    int position;
    String action="";
    boolean text1=true;
    boolean text2=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_subscriber_edit);

        EditText topicText = (EditText) findViewById(R.id.subTopic);
        EditText intervalText = (EditText) findViewById(R.id.subInterval);
        EditText remarkText = (EditText) findViewById(R.id.subRemark);

        deleteButton = (FloatingActionButton) findViewById(R.id.deleteButton);
        cancelButton = (FloatingActionButton) findViewById(R.id.cancelButton);
        actionButton = (FloatingActionButton) findViewById(R.id.editButton);

        buttonCheck(1,text1);
        buttonCheck(2,text2);

        action=getIntent().getStringExtra("ACTION");
        Log.i(DEBUG_TAG, "Started with action: "+action);
        if(action.equals("edit"))
        {
            actionButton.setVisibility(View.VISIBLE);
            buttonCheck(1,false);
            buttonCheck(2,false);
            position=getIntent().getIntExtra("pos",0);
            topicText.setText(getIntent().getStringExtra("subTopic"));
            intervalText.setText(getIntent().getStringExtra("subInterval"));
            remarkText.setText(getIntent().getStringExtra("remark"));
            if(getIntent().getBooleanExtra("persistent",false)) {
                ((CheckBox) findViewById(R.id.subDurable)).setChecked(true);
            }
            else
            {
                ((CheckBox) findViewById(R.id.subDurable)).setChecked(false);
            }
            deleteButton.setVisibility(View.VISIBLE);
        }

        cancelButton.setVisibility(View.VISIBLE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.subedit_toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                overridePendingTransition(0, 0);
                setResult(0, null);
                finish();
            }
        });

        topicText.addTextChangedListener(new TextValidator(topicText) {
            @Override public void validate(TextView textView, String text) {
                buttonCheck(1,text.isEmpty());
            }
        });

        intervalText.addTextChangedListener(new TextValidator(intervalText) {
            @Override public void validate(TextView textView, String text) {
                buttonCheck(2, text.isEmpty());
                if (!text.isEmpty()) {
                    if (Integer.parseInt(text) == 0) {
                        buttonCheck(2, true);
                    }
                }
            }
        });

        //start listeners
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(DEBUG_TAG, "Clicked on FAB: Done");
                Intent intent = new Intent();
                intent.putExtra("ACTION",action);
                intent.putExtra("pos",position);
                intent.putExtra("subTopic", ((EditText) findViewById(R.id.subTopic)).getText().toString());
                intent.putExtra("subInterval", ((EditText) findViewById(R.id.subInterval)).getText().toString());
                intent.putExtra("subRemark", ((EditText) findViewById(R.id.subRemark)).getText().toString());
                intent.putExtra("persistent", ((CheckBox) findViewById(R.id.subDurable)).isChecked());

                Log.i(DEBUG_TAG, "CheckBox: "+((CheckBox)findViewById(R.id.subDurable)).isChecked());
                setResult(1, intent);
                finish();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(DEBUG_TAG, "Clicked on FAB: Cancel");
                finish();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(DEBUG_TAG, "Clicked on FAB: Delete");
                Intent intent = new Intent();
                intent.putExtra("pos",position);
                setResult(2,intent);
                finish();
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

}
