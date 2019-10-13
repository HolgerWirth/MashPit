package com.holger.mashpit.prefs;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;

import com.holger.mashpit.MashPit;
import com.holger.mashpit.R;

public class MQTTSettingsPub extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String DEBUG_TAG = "MQTTSettingsPub";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs_mqtt_pub);


        MashPit.reconnect_action=false;

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
                if (key.equals("send_mashpit_domain")) {
                    MashPit.MPDomain_send=prefs.getString("send_mashpit_domain","");
                }
    }
}