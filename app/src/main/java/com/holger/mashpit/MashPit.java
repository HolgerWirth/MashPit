package com.holger.mashpit;

import android.app.Application;
import android.provider.Settings;
import android.util.Log;

import com.holger.mashpit.tools.ObjectBox;

import io.objectbox.android.AndroidObjectBrowser;

public class MashPit extends Application {
    private static final String DEBUG_TAG = "MashPit";
    static Boolean menu_action=false;
    public static String mDeviceId;

    @Override
    public void onCreate() {
        super.onCreate();

        ObjectBox.init(this);

        if (BuildConfig.DEBUG) {
            boolean started = new AndroidObjectBrowser(ObjectBox.get()).start(this);
            Log.i(DEBUG_TAG, "Object Browser Started: " + started);
            Log.i(DEBUG_TAG, "Use: adb forward tcp:8090 tcp:8090");
        }

        mDeviceId = String.format("MP_%s", Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
        Log.i(DEBUG_TAG, "Initialized!");
    }
}
