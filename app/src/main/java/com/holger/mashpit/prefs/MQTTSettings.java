package com.holger.mashpit.prefs;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;
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

                SettingsEdit editIP = (SettingsEdit) findPreference("broker_url");
                editIP.setSummary(MashPit.broker_url);
                editIP.setText(MashPit.broker_url);

                SettingsEdit editPort = (SettingsEdit) findPreference("broker_port");
                editPort.setSummary(MashPit.broker_port);
                editPort.setText(MashPit.broker_port);

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
            public void onSharedPreferenceChanged (SharedPreferences prefs, String key){
                Log.i(DEBUG_TAG, "Key: " + key);

                if (key.equals("broker_url")) {
                    MashPit.broker_url = prefs.getString("broker_url", "");
                    Log.i(DEBUG_TAG, "Broker URL changed: "+ MashPit.broker_url);
                }
                if (key.equals("broker_port")) {
                    MashPit.broker_port = prefs.getString("broker_port", "1883");
                    Log.i(DEBUG_TAG, "Broker Port changed: "+ MashPit.broker_port);
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(getPreferenceScreen().getContext());
                builder.setTitle(getString(R.string.MQTTchanged_alert_title));
                builder.setMessage(getString(R.string.MQTTchanged_text));
                builder.setPositiveButton(getString(R.string.MQTTchanged_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i(DEBUG_TAG, "Reconnect pressed!");
                        MashPit.reconnect_action=true;
                    }
                });
                builder.setNegativeButton(getString(R.string.MQTTchanged_cancel), null);
                builder.show();

            }
        }