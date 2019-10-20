package com.holger.mashpit.prefs;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import com.holger.mashpit.MashPit;
import com.holger.mashpit.R;
import com.holger.mashpit.TemperatureService;

public class MQTTSettingsSub extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String DEBUG_TAG = "MQTTSettingsPub";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs_mqtt_sub);

        MashPit.reconnect_action = false;

    }

    @Override
    public void onStop() {
        super.onStop();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        Log.i(DEBUG_TAG, "Key: " + key);
        boolean changed = false;

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
            MashPit.MPDomain = prefs.getString("mashpit_domain", "");
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

        if (changed) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getPreferenceScreen().getContext());
            builder.setTitle(getString(R.string.MQTTchanged_alert_title));
            builder.setMessage(getString(R.string.MQTTchanged_text));
            builder.setPositiveButton(getString(R.string.MQTTchanged_button), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.i(DEBUG_TAG, "Reconnect pressed!");
                    MashPit.reconnect_action = true;
//                    getActivity().stopService(new Intent(getActivity(), TemperatureService.class));
//                    getActivity().startService(new Intent(getActivity(), TemperatureService.class));
                }
            });
            builder.setNegativeButton(getString(R.string.MQTTchanged_cancel), null);
            builder.show();
        }
    }
}