package com.holger.mashpit;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.activeandroid.query.Select;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.holger.mashpit.model.MPServer;

import java.util.Calendar;
import java.util.List;

public class ServerEdit extends AppCompatActivity {
    private static final String DEBUG_TAG = "ServerEditActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String action = getIntent().getStringExtra("ACTION");
        final String type = getIntent().getStringExtra("adapter");
        final String server = getIntent().getStringExtra("server");
        Log.i(DEBUG_TAG, "Started with action: " + action + " and type: " + type);

        setContentView(R.layout.activity_confeditsrv);

        final EditText serverid = findViewById(R.id.confServer);
        final EditText alias_field = findViewById(R.id.confAlias);
        final EditText TS_field = findViewById(R.id.confUptime);

        FloatingActionButton actionButton = findViewById(R.id.editButton);

        final AlertDialog.Builder alertDialog;
        String alias = "";
        long TS = 0;
        List<MPServer> server_alias = new Select().from(MPServer.class).where("name = 'MashPit'").and("MPServer = ?", server).execute();
        if (server_alias.size() > 0) {
            alias = server_alias.get(0).alias;
            TS = server_alias.get(0).TS;
        }
        alias_field.setText(alias);
        serverid.setText(server);
        serverid.setEnabled(false);
        startTimer(TS);
        TS_field.setEnabled(false);

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

        alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(getString(R.string.pubConfig));
        alertDialog.setIcon(R.drawable.ic_launcher);

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        final long finalTS = TS;
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = createServerIntent(action, type, server, finalTS);
                setResult(1, intent);
                finish();
            }
        });

        final String finalAlias = alias;
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(DEBUG_TAG, "Clicked on FAB: Done");
                if(!(alias_field.getText().toString().equals(finalAlias)))
                {
                    alertDialog.setMessage(getString(R.string.confPublishAlert, server));
                    alertDialog.show();
                }
                else
                {
                    finish();
                }
            }
        });
    }

    private Intent createServerIntent(String action, String type, String server, long TS) {
        Intent intent = new Intent();
        intent.putExtra("ACTION", action);
        intent.putExtra("type", type);
        intent.putExtra("confName", "MashPit");
        intent.putExtra("confAlias", ((EditText) findViewById(R.id.confAlias)).getText().toString());
        intent.putExtra("server", server);
        intent.putExtra("confTS", TS);

        return (intent);
    }

    public void startTimer(final long TS) {
        new CountDownTimer(1800000, 1000) {
            final EditText TS_field = findViewById(R.id.confUptime);
            long uptime = 0;

            public void onTick(long millisUntilFinished) {
                long tsnow = System.currentTimeMillis() / 1000;
                uptime = tsnow - TS;

                TS_field.setText(getDate(uptime));
            }

            public void onFinish() {
                TS_field.setText(getDate(uptime));

            }
        }.start();
    }

    public static String getDate(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time * 1000);
        return (DateFormat.format("HH:mm:ss", cal).toString());
    }
}
