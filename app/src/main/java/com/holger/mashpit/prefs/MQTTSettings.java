package com.holger.mashpit.prefs;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
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

                SettingsEdit editIP = (SettingsEdit) findPreference("broker_url");
                editIP.setSummary(IP);
                editIP.setText(IP);

                SettingsEdit editPort = (SettingsEdit) findPreference("broker_port");
                editPort.setSummary(port);
                editPort.setText(port);

                SettingsEdit seditIP = (SettingsEdit) findPreference("send_broker_url");
                seditIP.setSummary(IP);
                seditIP.setText(IP);

                SettingsEdit seditPort = (SettingsEdit) findPreference("send_broker_port");
                seditPort.setSummary(port);
                seditPort.setText(port);

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