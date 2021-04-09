package com.holger.mashpit.prefs;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;

import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.SummaryProvider;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.holger.mashpit.R;
import com.holger.mashpit.TemperatureService;
import com.holger.share.Constants;

import java.util.Objects;

public class MQTTSettingsSub extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String DEBUG_TAG = "MQTTSettingsSub";
    private boolean changed = false;
    private Context context;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.prefs_mqtt_sub, rootKey);
        context=getPreferenceScreen().getContext();
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);

        Preference broker_url = findPreference("broker_url");
        Preference broker_port = findPreference("broker_port");
        Preference domain = findPreference("mashpit_domain");
        Preference username = findPreference("broker_user");
        final EditTextPreference password = findPreference("broker_password");

        assert password != null;
        password.setOnBindEditTextListener(
                editText -> {
                    editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    password.setSummaryProvider((SummaryProvider<Preference>) preference -> setAsterisks(editText.getText().toString().length()));
                });

        assert broker_url != null;
        broker_url.setSummary(p.getString("broker_url",""));
        assert broker_port != null;
        broker_port.setSummary(p.getString("broker_port","1883"));
        assert domain != null;
        domain.setSummary(p.getString("mashpit_domain",""));
        assert username != null;
        username.setSummary(p.getString("broker_user",""));
        try {
            password.setSummary(setAsterisks(password.getText().length()));
        }
        catch (Exception exp)
        {
            Log.i(DEBUG_TAG,"Password not set!");
        }
        Log.i(DEBUG_TAG, "onCreatePreferences()");
    }

    private String setAsterisks(int length) {
        StringBuilder sb = new StringBuilder();
        for (int s = 0; s < length; s++) {
            sb.append("*");
        }
        return sb.toString();
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
            builder.setPositiveButton(getString(R.string.MQTTchanged_button), (dialog, which) -> {
                Log.i(DEBUG_TAG, "Reconnect pressed!");
                Log.i(DEBUG_TAG, "Stop service!");
                Intent serviceIntent = new Intent(context, TemperatureService.class);
                serviceIntent.setAction(Constants.ACTION.RESTART_ACTION);
                context.startService(serviceIntent);
            });
            builder.setNegativeButton(getString(R.string.MQTTchanged_cancel), null);
            builder.show();
        }
        super.onStop();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        Log.i(DEBUG_TAG, "Key: " + key);
        SharedPreferences.Editor editor = prefs.edit();
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);

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
        if(key.equals("broker_ssl")) {
            changed=true;
        }
        try {
            findPreference(key).setSummary(p.getString(key, ""));
        }
        catch (Exception e)
        {
            Log.i(DEBUG_TAG, "Summary not set: " + key);
        }

        if (!prefs.getBoolean("broker_same", false)) {
            editor.putString("send_mashpit_domain", prefs.getString("mashpit_domain", ""));
            editor.putString("send_broker_url", prefs.getString("broker_url", ""));
            editor.putString("send_broker_port", prefs.getString("broker_port", ""));
            editor.putString("send_broker_user", prefs.getString("broker_user", ""));
            editor.putString("send_broker_password", prefs.getString("broker_password", ""));
            editor.putBoolean("send_broker_ssl", prefs.getBoolean("broker_ssl", false));
            editor.apply();
        }
    }
}
