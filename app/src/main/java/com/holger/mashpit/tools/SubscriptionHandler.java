package com.holger.mashpit.tools;

import android.database.Cursor;

import com.activeandroid.Cache;
import com.activeandroid.query.Select;
import com.holger.mashpit.model.Subscriptions;

import java.util.ArrayList;
import java.util.List;

public class SubscriptionHandler {
    private List<String> subscriptions;
    private List<String> subscriptions_durable;
    private String action;

    public SubscriptionHandler(String action)
    {
        this.action=action;
        refreshSubscription();
    }

    public void refreshSubscription()
    {
        subscriptions = new ArrayList<>();
        subscriptions_durable = new ArrayList<>();
        List<Subscriptions> dbresult = new Select().from(Subscriptions.class).where("action=?",action).orderBy("server ASC").execute();
        for (Subscriptions sub : dbresult) {
            subscriptions.add(sub.server+"/"+sub.sensor+"/"+sub.interval);
            if(sub.durable==1)
            {
                subscriptions.add(sub.server+"/"+sub.sensor+"/"+sub.interval);
            }
        }
    }

    public List<String> getAllSubscription(boolean durable) {
        List<String> sub_all = new ArrayList<>();
        final String sql="select server,sensor,interval, sum(durable) from Subscriptions group by server,sensor,interval";
        final Cursor resultCursor = Cache.openDatabase().rawQuery(sql, null);
        while (resultCursor.moveToNext()) {
            Subscriptions sub = new Subscriptions();
            sub.server=resultCursor.getString(0);
            sub.sensor=resultCursor.getString(1);
            sub.interval=resultCursor.getInt(2);
            sub.durable=resultCursor.getInt(3);
            if(durable) {
                if (sub.durable == 1) {
                    sub_all.add("/SE/" + sub.server + "/temp/" + sub.sensor + "/" + sub.interval);
                }
            }
            else {
                if (sub.durable == 0) {
                    sub_all.add("/SE/" + sub.server + "/temp/" + sub.sensor + "/" + sub.interval);
                }
            }
        }
        resultCursor.close();
        return sub_all;
    }

    public boolean checkSubscription(String topic)
    {
        return subscriptions.contains(topic);
    }
}
