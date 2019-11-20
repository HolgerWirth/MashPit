package com.holger.mashpit;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
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

import java.util.List;
import java.util.Locale;

public class ServerEdit extends AppCompatActivity {
    private static final String DEBUG_TAG = "ServerEditActivity";
    private static String translation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        translation=getApplicationContext().getResources().getString(R.string.uptimeformat);

        final String action = getIntent().getStringExtra("ACTION");
        final String type = getIntent().getStringExtra("adapter");
        final String server = getIntent().getStringExtra("server");
        final boolean active = getIntent().getBooleanExtra("active", false);

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

        if (active) {
            startTimer(TS);
            TS_field.setEnabled(false);
        } else {
            TS_field.setText("");
        }
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
                String new_alias = alias_field.getText().toString();
                if (!(new_alias.equals(finalAlias))) {
                    alertDialog.setMessage(getString(R.string.confPublishAlert, new_alias));
                    alertDialog.show();
                } else {
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

                TS_field.setText(getFormattedTimeSpan(uptime));
            }

            public void onFinish() {
                TS_field.setText(getFormattedTimeSpan(uptime));

            }
        }.start();
    }

    public static String getFormattedTimeSpan(final long span) {
        long x = span;
        long seconds = x % 60;
        x /= 60;
        long minutes = x % 60;
        x /= 60;
        long hours = x % 24;
        x /= 24;
        long days = x;

        return String.format(Locale.getDefault(),"%d %s %02d:%02d:%02d", days, translation,hours, minutes, seconds);
    }
}
