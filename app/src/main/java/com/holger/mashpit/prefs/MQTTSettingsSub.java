package com.holger.mashpit.prefs;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.holger.mashpit.R;
import com.holger.mashpit.TemperatureService;
import com.holger.share.Constants;

public class MQTTSettingsSub extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String DEBUG_TAG = "MQTTSettingsSub";
    private boolean changed = false;
    private Context context;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.prefs_mqtt_sub, rootKey);
        context=getPreferenceScreen().getContext();
        Log.i(DEBUG_TAG, "onCreatePreferences()");
    }

    @Override
    public void onStart() {
        super.onStart();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        Log.i(DEBUG_TAG, "onStop");
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        if (changed) {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
            builder.setTitle(getString(R.string.MQTTchanged_alert_title));
            builder.setMessage(getString(R.string.MQTTchanged_text));
            builder.setPositiveButton(getString(R.string.MQTTchanged_button), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.i(DEBUG_TAG, "Reconnect pressed!");
                    Log.i(DEBUG_TAG, "Stop service!");
                    Intent serviceIntent = new Intent(context, TemperatureService.class);
                    serviceIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
                    context.startService(serviceIntent);
                    Log.i(DEBUG_TAG, "Start service!");
                    serviceIntent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
                    context.startService(serviceIntent);
                }
            });
            builder.setNegativeButton(getString(R.string.MQTTchanged_cancel), null);
            builder.show();
        }
        super.onStop();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        Log.i(DEBUG_TAG, "Key: " + key);

        if (key.equals("broker_url")) {
            changed = true;
        }
        if (key.equals("broker_port")) {
            changed = true;
        }
        if (key.equals("broker_user")) {
            changed = true;
        }
        if (key.equals("broker_password")) {
            changed = true;
        }
        if (key.equals("mashpit_domain")) {
            changed = true;
        }
        if (!prefs.getBoolean("broker_same", false)) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("send_mashpit_domain", prefs.getString("mashpit_domain", ""));
            editor.putString("send_broker_url", prefs.getString("broker_url", ""));
            editor.putString("send_broker_port", prefs.getString("broker_port", ""));
            editor.putString("send_broker_user", prefs.getString("broker_user", ""));
            editor.putString("send_broker_password", prefs.getString("broker_password", ""));
            editor.apply();
        }
    }
}
