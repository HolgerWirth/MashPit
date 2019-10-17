package com.holger.mashpit.prefs;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.holger.mashpit.FindServerActivity;
import com.holger.mashpit.MashPit;
import com.holger.mashpit.R;

public class MQTTSettings extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String DEBUG_TAG = "MQTTSettings";
    String IP;
    String port;
    String domain;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs_mqtt);

        MashPit.reconnect_action = false;

        SettingsEdit editText = (SettingsEdit) findPreference("device_id");
        editText.setSummary(MashPit.mDeviceId);

        Preference pref = findPreference("find server");
        pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivityForResult(new Intent(getActivity(), FindServerActivity.class), 0);
                return true;
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            if (resultCode == 1) {
                IP = data.getStringExtra("IP");
                port = data.getStringExtra("port");
                domain = data.getStringExtra("domain");

                Log.i(DEBUG_TAG, "Found MQTT Server IP: " + IP + " Port: " + port);

                AlertDialog.Builder builder = new AlertDialog.Builder(getPreferenceScreen().getContext());
                builder.setTitle("MQTT Server found!");
                builder.setMessage("Do you want to use the broker with IP: " + IP);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i(DEBUG_TAG, "Use broker with IP: " + IP);

                        SettingsEdit MPdomain = (SettingsEdit) findPreference("mashpit_domain");
                        MPdomain.setSummary(domain);
                        MPdomain.setText(domain);

                        SettingsEdit editIP = (SettingsEdit) findPreference("broker_url");
                        editIP.setSummary(IP);
                        editIP.setText(IP);

                        SettingsEdit editPort = (SettingsEdit) findPreference("broker_port");
                        editPort.setSummary(port);
                        editPort.setText(port);

                        SettingsEdit sMPdomain = (SettingsEdit) findPreference("send_mashpit_domain");
                        sMPdomain.setSummary(domain);
                        sMPdomain.setText(domain);

                        SettingsEdit seditIP = (SettingsEdit) findPreference("send_broker_url");
                        seditIP.setSummary(IP);
                        seditIP.setText(IP);

                        SettingsEdit seditPort = (SettingsEdit) findPreference("send_broker_port");
                        seditPort.setSummary(port);
                        seditPort.setText(port);
                    }
                });
                builder.setNegativeButton(getString(R.string.MQTTchanged_cancel), null);
                builder.show();

            } else {
                Toast.makeText(getActivity(), "MQTT Server not found!", Toast.LENGTH_LONG).show();
            }
        }
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
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

    }
}