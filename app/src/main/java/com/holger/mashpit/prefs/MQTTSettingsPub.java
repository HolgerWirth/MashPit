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

                if (key.equals("send_broker_url")) {
                    MashPit.send_broker_url = prefs.getString("send_broker_url", "");
                    Log.i(DEBUG_TAG, "Send Broker URL changed: "+ MashPit.send_broker_url);
                }
                if (key.equals("send_broker_port")) {
                    MashPit.send_broker_port = prefs.getString("send_broker_port", "1883");
                    Log.i(DEBUG_TAG, "Send Broker Port changed: "+ MashPit.send_broker_port);
                }
                if (key.equals("send_broker_user")) {
                    Log.i(DEBUG_TAG, "Send Broker User changed: "+ MashPit.send_broker_port);
                }
                if (key.equals("send_broker_password")) {
                    Log.i(DEBUG_TAG, "Send Broker Password changed: "+ MashPit.send_broker_port);
                }

            }
        }