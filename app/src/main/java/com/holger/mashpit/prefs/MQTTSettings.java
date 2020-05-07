package com.holger.mashpit.prefs;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.holger.mashpit.FindServerActivity;
import com.holger.mashpit.MashPit;
import com.holger.mashpit.R;

public class MQTTSettings extends PreferenceFragmentCompat {
    private static final String DEBUG_TAG = "MQTTSettings";
    private String IP;
    private String port;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.prefs_mqtt, rootKey);
        EditTextPreference editText = findPreference("device_id");
        if (editText != null) {
            editText.setSummary(MashPit.mDeviceId);
        }

        androidx.preference.Preference pref = findPreference("find server");
        if (pref != null) {
            pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivityForResult(new Intent(getActivity(), FindServerActivity.class), 0);
                    return true;
                }
            });
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            if (resultCode == 1) {
                IP = data.getStringExtra("IP");
                port = data.getStringExtra("port");

                Log.i(DEBUG_TAG, "Found MQTT Server IP: " + IP + " Port: " + port);

                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getPreferenceScreen().getContext());
                builder.setTitle("MQTT Server found!");
                builder.setMessage("Do you want to use the broker with IP: " + IP);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i(DEBUG_TAG, "Use broker with IP: " + IP);

                        EditTextPreference editIP = findPreference("broker_url");
                        assert editIP != null;
                        editIP.setSummary(IP);
                        editIP.setText(IP);

                        EditTextPreference editPort = findPreference("broker_port");
                        assert editPort != null;
                        editPort.setSummary(port);
                        editPort.setText(port);

                        EditTextPreference seditIP = findPreference("send_broker_url");
                        assert seditIP != null;
                        seditIP.setSummary(IP);
                        seditIP.setText(IP);

                        EditTextPreference seditPort = findPreference("send_broker_port");
                        assert seditPort != null;
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
}
