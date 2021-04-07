package com.holger.mashpit.events;

import android.content.res.Resources;
import androidx.core.os.ConfigurationCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SensorDataEvent
{
    private String type;
    private String server;
    private String sensor;
    private int interval;
    private String data;

    public String getTopicString()
    {
        return("/SE/"+getServer()+"/temp/"+getSensor()+"/"+getInterval());

    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getSensor() {
        return sensor;
    }

    public void setSensor(String sensor) {
        this.sensor = sensor;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getTimestamp()
    {
        Locale locale = ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration()).get(0);
        SimpleDateFormat fmtout = new SimpleDateFormat("HH:mm:ss", locale);
        Date df = new java.util.Date((Long.parseLong(getData("TS"))*1000));
        return (fmtout.format(df));
    }

    public String getData(String field)
    {
        try {
            JSONObject jobj = new JSONObject(getData());
            return(jobj.getString(field));
        } catch (JSONException e) {
            return("");
        }
    }
}
