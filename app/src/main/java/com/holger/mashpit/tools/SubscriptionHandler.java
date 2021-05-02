package com.holger.mashpit.tools;

import android.database.Cursor;

import com.activeandroid.Cache;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.holger.mashpit.model.SensorStatus;
import com.holger.mashpit.model.Sensors;
import com.holger.mashpit.model.Subscriptions;

import java.util.ArrayList;
import java.util.List;

public class SubscriptionHandler {

    public String getServerAlias(String server) {
        SensorStatus sensorStatus = new Select().from(SensorStatus.class).where("server=?", server).executeSingle();
        if (!sensorStatus.alias.isEmpty()) {
            return sensorStatus.alias;
        }
        return sensorStatus.server;
    }

    public String getSensorAlias(String server,String sensor) {
        Sensors sensors = new Select().from(Sensors.class).where("server = ?", server).and("sensor = ?",sensor).executeSingle();
        if(sensors==null)
        {
            return "";
        }
        if(sensors.name.isEmpty())
        {
            return sensors.sensor;
        }
        return sensors.name;
    }

    public void deleteSubsription(String topic)
    {
        new Delete().from(Subscriptions.class).where("topic = ?", topic).and("deleted = ?",1).execute();
    }

    public List<String> getDeletedSubscriptions()
    {
        List<String> sub_all = new ArrayList<>();
        final String sql="select topic,sum(durable) from Subscriptions where deleted = 1 group by topic";
        final Cursor resultCursor = Cache.openDatabase().rawQuery(sql, null);
        while (resultCursor.moveToNext()) {
            Subscriptions sub = new Subscriptions();
            sub.topic = resultCursor.getString(0);
            sub.durable = resultCursor.getInt(1);
            sub_all.add(sub.topic);
        }
        resultCursor.close();
        return sub_all;
    }

    public List<String> getAllSubscription(boolean durable) {
        List<String> sub_all = new ArrayList<>();
        final String sql="select topic,sum(durable) from Subscriptions where deleted = 0 group by topic";
        final Cursor resultCursor = Cache.openDatabase().rawQuery(sql, null);
        while (resultCursor.moveToNext()) {
            Subscriptions sub = new Subscriptions();
            sub.topic=resultCursor.getString(0);
            sub.durable=resultCursor.getInt(1);
            if(durable) {
                if (sub.durable > 0) {
                    sub_all.add(sub.topic);
                }
            }
            else {
                if (sub.durable == 0) {
                    sub_all.add(sub.topic);
                }
            }
        }
        resultCursor.close();
        return sub_all;
    }

    public boolean checkSubscription(String topic,String action)
    {
        return new Select().from(Subscriptions.class).where("action=?",action)
                .and("topic = ?",topic)
                .and("deleted = ?",0).exists();
    }

    public boolean checkSubscription(String topic)
    {
        return new Select().from(Subscriptions.class).where("topic = ?",topic)
                .and("deleted = ?",0).exists();
    }

    public List<String> getRetainedSubscription()
    {
        List<String> retained = new ArrayList<>();
        retained.add("/SE/+/conf/#");
        retained.add("/SE/+/status");
        return(retained);
    }
}
