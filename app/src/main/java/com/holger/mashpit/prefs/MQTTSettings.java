package com.holger.mashpit.prefs;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.holger.mashpit.FindServerActivity;
import com.holger.mashpit.MashPit;
import com.holger.mashpit.R;

public class MQTTSettings extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String DEBUG_TAG = "MQTTSettings";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs_mqtt);

        MashPit.reconnect_action=false;

        SettingsEdit editText = (SettingsEdit) findPreference("device_id");
        editText.setSummary(MashPit.mDeviceId);

        Preference pref = findPreference("find server");
        pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivityForResult(new Intent(getActivity(),FindServerActivity.class), 0);
                return true;
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            if (resultCode == 1) {
                String IP = data.getStringExtra("IP");
                String port= data.getStringExtra("port");

                Log.i(DEBUG_TAG, "Found MQTT Server IP: "+IP+" Port: "+port);

                MashPit.broker_url=IP;
                MashPit.broker_port=port;
                MashPit.send_broker_url=IP;
                MashPit.send_broker_port=port;

                SettingsEdit editIP = (SettingsEdit) findPreference("broker_url");
                editIP.setSummary(MashPit.broker_url);
                editIP.setText(MashPit.broker_url);

                SettingsEdit editPort = (SettingsEdit) findPreference("broker_port");
                editPort.setSummary(MashPit.broker_port);
                editPort.setText(MashPit.broker_port);

                SettingsEdit seditIP = (SettingsEdit) findPreference("send_broker_url");
                seditIP.setSummary(MashPit.send_broker_url);
                seditIP.setText(MashPit.send_broker_url);

                SettingsEdit seditPort = (SettingsEdit) findPreference("send_broker_port");
                seditPort.setSummary(MashPit.send_broker_port);
                seditPort.setText(MashPit.send_broker_port);

                Toast.makeText(getActivity(),"Found MQTT Server!",Toast.LENGTH_LONG).show();
            }
            else
            {
                Toast.makeText(getActivity(),"MQTT Server not found!",Toast.LENGTH_LONG).show();
            }
        }
    }

            @Override
            public void onStop () {
                super.onStop();
                getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
            }

            @Override
            public void onStart () {
                super.onStart();
                getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
            }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

    }
}