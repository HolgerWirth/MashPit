package com.holger.mashpit.stepper;

import android.util.Log;

public class SubscriptionHolder {
    private static final String DEBUG_TAG = "DeviceHolder";

    private static SubscriptionHolder instance;
    private String device;
    private String server;
    private String sensor;
    private String interval;

    public void setServer(String server)
    {
        this.server=server;
    }
    public String getServer()
    {
        return(this.server);
    }
    public void setDevice(String device)
    {
        this.device=device;
    }
    public String getDevice()
    {
        return(this.device);
    }
    public void setSensor(String sensor)
    {
        this.sensor=sensor;
    }
    public String getSensor()
    {
        return(this.sensor);
    }
    public void setInterval(String interval)
    {
        this.interval=interval;
    }
    public String getInterval()
    {
        return(this.interval);
    }
    public void init()
    {
        this.server="";
        this.device="";
        this.sensor="";
        this.interval="";
    }

    static synchronized SubscriptionHolder getInstance(){
        if(instance==null){
            Log.i(DEBUG_TAG, "instance==null");
            instance=new SubscriptionHolder();
        }
        return instance;
    }
}
