package com.holger.mashpit.prefs;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.holger.mashpit.R;
import com.holger.mashpit.TemperatureService;
import com.holger.share.Constants;


public class MQTTSettingsPub extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String DEBUG_TAG = "MQTTSettingsPub";
    private boolean changed = false;
    private Context context;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.prefs_mqtt_pub, rootKey);
        context=getPreferenceScreen().getContext();
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);

        Preference broker_url = findPreference("send_broker_url");
        Preference broker_port = findPreference("send_broker_port");
        Preference domain = findPreference("send_mashpit_domain");
        Preference username = findPreference("send_broker_user");
        final EditTextPreference password = findPreference("send_broker_password");

        password.setOnBindEditTextListener(
                new EditTextPreference.OnBindEditTextListener() {
                    @Override
                    public void onBindEditText(@NonNull final EditText editText) {
                        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        password.setSummaryProvider(new Preference.SummaryProvider() {
                            @Override
                            public CharSequence provideSummary(Preference preference) {
                                return setAsterisks(editText.getText().toString().length());
                            }
                        });
                    }
                });

        broker_url.setSummary(p.getString("send_broker_url",""));
        broker_port.setSummary(p.getString("send_broker_port","1883"));
        domain.setSummary(p.getString("send_mashpit_domain",""));
        username.setSummary(p.getString("send_broker_user",""));
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
        super.onStop();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        Log.i(DEBUG_TAG, "Key: " + key);
        SharedPreferences.Editor editor = prefs.edit();
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);

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
