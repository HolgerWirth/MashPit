package com.holger.mashpit.prefs;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.holger.mashpit.FindServerActivity;
import com.holger.mashpit.MashPit;
import com.holger.mashpit.R;

public class MQTTSettings extends PreferenceFragmentCompat {
    private static final String DEBUG_TAG = "MQTTSettings";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.prefs_mqtt, rootKey);
        EditTextPreference editText = findPreference("device_id");
        if (editText != null) {
            editText.setSummary(MashPit.mDeviceId);
        }

        ActivityResultLauncher<Intent> myActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                myresult -> {
                    if (myresult.getResultCode() == 1) {
                        assert myresult.getData() != null;
                        String IP=myresult.getData().getStringExtra("IP");
                        String port=myresult.getData().getStringExtra("port");
                        onResult(IP,port);
                    }
                    else
                    {
                        Toast.makeText(getActivity(), "MQTT Server not found!", Toast.LENGTH_LONG).show();
                    }
                });

        androidx.preference.Preference pref = findPreference("find server");
        if (pref != null) {
            pref.setOnPreferenceClickListener(preference -> {
                myActivityResultLauncher.launch(new Intent(getActivity(), FindServerActivity.class));
                return true;
            });
        }
    }

    public void onResult(String IP, String port) {
        Log.i(DEBUG_TAG, "Found MQTT Server IP: " + IP + " Port: " + port);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getPreferenceScreen().getContext());
        builder.setTitle("MQTT Server found!");
        builder.setMessage("Do you want to use the broker with IP: " + IP);
        builder.setPositiveButton("OK", (dialog, which) -> {
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
        });
        builder.setNegativeButton(getString(R.string.MQTTchanged_cancel), null);
        builder.show();
    }
}
