package com.holger.mashpit;

import android.app.Application;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

import com.holger.mashpit.model.Subscriptions;
import com.holger.mashpit.model.Subscriptions_;
import com.holger.mashpit.tools.ObjectBox;

import io.objectbox.Box;
import io.objectbox.android.AndroidObjectBrowser;
import io.objectbox.query.Query;

public class MashPit extends Application {
    private static final String DEBUG_TAG = "MashPit";
    static Boolean menu_action=false;
    public static String mDeviceId;

    @Override
    public void onCreate() {
        super.onCreate();

        ObjectBox.init(this);
/*
        if (BuildConfig.DEBUG) {
            boolean started = new AndroidObjectBrowser(ObjectBox.get()).start(this);
            Log.i(DEBUG_TAG, "Object Browser Started: " + started);
        }
*/
        mDeviceId = String.format("MP_%s", Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
        Log.i(DEBUG_TAG, "Initialized!");
    }

    public static Intent selectTempChart(Intent k,String name,String desc)
    {
        Box<Subscriptions> dataBox = ObjectBox.get().boxFor(Subscriptions.class);
        Log.i(DEBUG_TAG, "SelectTempChart: "+name);

        Query<Subscriptions> query = dataBox.query(Subscriptions_.action.equal("Chart")
                .and(Subscriptions_.name.equal(name)))
                .build();

        String[] topics = new String[query.find().size()];
        int i=0;
        for(Subscriptions subs : query.find())
        {
            topics[i]=subs.topic;
            i++;
        }
        k.putExtra("title",desc);
        k.putExtra("topics",topics);
        return(k);
    }
}
