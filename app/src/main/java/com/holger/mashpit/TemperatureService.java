package com.holger.mashpit;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.activeandroid.query.Update;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.holger.mashpit.events.MPStatusEvent;
import com.holger.mashpit.events.ProcessEvent;
import com.holger.mashpit.events.SensorEvent;
import com.holger.mashpit.events.StatusEvent;
import com.holger.mashpit.events.TemperatureEvent;
import com.holger.mashpit.model.Config;
import com.holger.mashpit.model.MPServer;
import com.holger.mashpit.model.MPStatus;
import com.holger.mashpit.model.Process;
import com.holger.mashpit.model.SensorStatus;
import com.holger.mashpit.model.Sensors;
import com.holger.mashpit.model.Subscriber;
import com.holger.mashpit.model.Temperature;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.holger.share.Constants;

public class TemperatureService extends Service implements MqttCallback,DataClient.OnDataChangedListener {

    private static final String DEBUG_TAG = "TemperatureService";

    private static final String NOTIFICATION_CHANNEL_ID = "MashPitChannel_1";

    private MqttDefaultFilePersistence mDataStore=null;

    private MqttClient mClient;                                        // Mqtt Client

    private Boolean isConnected=false;
    private Boolean isConnecting=false;

    private NotificationCompat.Builder builder;
    private StatusEvent statusEvent = new StatusEvent();

    private String MQTT_DOMAIN="";

    private volatile boolean backgroundDataEnabled;
    {
        backgroundDataEnabled = true;
    }
    private NetworkConnectionIntentReceiver networkConnectionMonitor;
    SharedPreferences prefs;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(DEBUG_TAG, "onCreate()...");
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(intent==null)
        {
                Log.i(DEBUG_TAG, "onStartCommand: received null intent");
        }

        String action;
        if(intent!=null)
        {
                action = intent.getAction();
                Log.i(DEBUG_TAG, "onStartCommand: Received action of " + action);
        }
        else
        {
            action=Constants.ACTION.STARTFOREGROUND_ACTION;
        }

        if (action != null) {
            switch (action) {
                case Constants.ACTION.STARTFOREGROUND_ACTION:
                    Log.i(DEBUG_TAG, "Received Start Foreground Intent ");
                    Intent notificationIntent = new Intent(this, MainActivity.class);
                    notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
                    notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                            notificationIntent, 0);

                    Intent previousIntent = new Intent(this, TemperatureService.class);
                    previousIntent.setAction(Constants.ACTION.CANCEL_ACTION);
                    PendingIntent ppreviousIntent = PendingIntent.getService(this, 0,
                            previousIntent, 0);

                    Bitmap icon = BitmapFactory.decodeResource(getResources(),
                            R.drawable.ic_launcher);

                    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_DEFAULT);
                        if (notificationManager != null) {
                            notificationManager.createNotificationChannel(notificationChannel);
                        }
                    }

                    builder = new NotificationCompat.Builder(this,NOTIFICATION_CHANNEL_ID)
                            .setContentTitle("")
                            .setTicker("")
                            .setContentText("")
                            .setSmallIcon(R.drawable.ic_stat_name)
                            .setLargeIcon(
                                    Bitmap.createScaledBitmap(icon, 128, 128, false))
                            .setContentIntent(pendingIntent)
                            .setOngoing(true)
                            .addAction(android.R.drawable.ic_menu_close_clear_cancel,
                                    "Stop Service", ppreviousIntent);

                    startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                            builder.build());

                    if(!EventBus.getDefault().isRegistered(this)) {
                        Log.i(DEBUG_TAG, "EventBus register");
                        EventBus.getDefault().register(this);
                    }

                    TemperatureEvent myEvent = EventBus.getDefault().getStickyEvent(TemperatureEvent.class);
                    if (myEvent != null) {
                        Log.i(DEBUG_TAG, "Found sticky event!");
                        updateNotification(myEvent.getTimestamp(),myEvent.getStatus(),myEvent.getSensor(), myEvent.getEvent());
                    }

                    registerBroadcastReceivers();
                    Wearable.getDataClient(this).addListener(this);


                    try {
                        connect();
                    } catch (MqttException e) {
                        e.printStackTrace();
                        return(START_STICKY);
                    }

                    List<Subscriber> delresult = new ArrayList<>();
                    String delsubs=prefs.getString("delsublist","");
                    int subs_count;
                    if(delsubs.length()>0) {
                        try {
                            JSONObject subscribers = new JSONObject(delsubs);
                            JSONArray subarray = subscribers.getJSONArray("delsubscriber");
                            subs_count = subarray.length();
                            for (int i = 0; i < subs_count; i++) {
                                JSONObject subobj = subarray.getJSONObject(i);
                                Subscriber sub = new Subscriber();
                                sub.topic = subobj.getString("topic");
                                sub.interval = subobj.getString("interval");
                                delresult.add(sub);
                            }
                        } catch (JSONException e) {
                            Log.i(DEBUG_TAG, "delsublist preference does not exist");
                        }
                        String[] topic = new String[delresult.size()];
                        for (int i = 0; i < delresult.size(); i++) {
                            Subscriber sub = delresult.get(i);
                            String mytopic = "/temp/" + sub.topic + "/" + sub.interval;
                            topic[i] = mytopic;
                        }
                        for (String aTopic : topic) {
                            Log.i(DEBUG_TAG, "Unubscribe from: " + aTopic);
                        }
                        try {
                            mClient.unsubscribe(topic);
                        } catch (MqttException e) {
                            Log.i(DEBUG_TAG, "Can't unsubscribe");
                        }
                        prefs.edit().putString("delsublist", "").apply();
                        Log.i(DEBUG_TAG, "Successfully unsubscribed");
                    }

                    List<Subscriber> result = new ArrayList<>();
                    String subs=prefs.getString("sublist","");
                    if(subs.length()>0) {
                        try {
                            JSONObject subscribers = new JSONObject(subs);
                            JSONArray subarray = subscribers.getJSONArray("subscriber");
                            subs_count=subarray.length();
                            for (int i = 0; i < subs_count; i++) {
                                JSONObject subobj = subarray.getJSONObject(i);
                                Subscriber sub = new Subscriber();
                                sub.topic = subobj.getString("topic");
                                sub.interval = subobj.getString("interval");
                                sub.persistent = subobj.getBoolean("durable");
                                result.add(sub);
                            }
                        } catch (JSONException e) {
                            Log.i(DEBUG_TAG, "sublist preference does not exist");
                        }
                    }

                    String[] topic = new String[result.size()+2];
                    int[] qos = new int[result.size()+2];
                    for(int i=0;i<result.size();i++)
                    {
                        Subscriber sub = result.get(i);
                        String mytopic="/temp/"+sub.topic+"/"+sub.interval;
                        topic[i]=mytopic;
                        if(sub.persistent) {
                            qos[i] = 2;
                        }
                        else
                        {
                            qos[i]=0;
                        }
                    }
//                    topic[result.size()]=MQTT_DOMAIN+"/MP/process";
//                    qos[result.size()]=0;

                    // Delete all status messages from the database
                    topic[result.size()]=MQTT_DOMAIN+"/MP/#";

                    new Delete()
                            .from(MPServer.class)
                            .execute();

                    topic[result.size()]=MQTT_DOMAIN+"/MP/#";
                    topic[result.size()+1]=MQTT_DOMAIN+"/SE/#";

/*                    new Delete().from(Sensors.class)
                            .execute();
*/
                    qos[result.size()+1]=0;

                    for(int i=0;i<topic.length;i++)
                    {
                        Log.i(DEBUG_TAG, "Subscribe to: "+topic[i]+" with qos: "+qos[i]);
                    }

                    try {
                        mClient.subscribe(topic,qos);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                    Log.i(DEBUG_TAG, "Successfully subscribed");

                    break;
                case Constants.ACTION.STOPFOREGROUND_ACTION:
                    Log.i(DEBUG_TAG, "Received Stop Foreground Intent");
                    unregisterBroadcastReceivers();
                    Wearable.getDataClient(this).removeListener(this);
                    disconnect();
                    stopForeground(true);
                    stopSelf();
                    break;
                case Constants.ACTION.CANCEL_ACTION:
                    Log.i(DEBUG_TAG, "Clicked Cancel");
                    unregisterBroadcastReceivers();
                    Wearable.getDataClient(this).removeListener(this);
                    disconnect();
                    stopForeground(true);
                    stopSelf();
                    break;
                case Constants.ACTION.CONNECT_ACTION:
                    Log.i(DEBUG_TAG, "Clicked Connect");
                    try {
                        connect();
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                    break;
                case Constants.ACTION.RECONNECT_ACTION:
                    Log.i(DEBUG_TAG, "Clicked Reconnect");
                    disconnect();
                    try {
                        connect();
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                    break;
                case Constants.ACTION.CHECK_ACTION:
                    Log.i(DEBUG_TAG, "Check action");
                    checkConnection();
                    break;

            }
        }
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        Log.i(DEBUG_TAG, "In onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private synchronized void connect() throws MqttException {

        Log.i(DEBUG_TAG,"Connect!");
        String MQTT_BROKER;
        int MQTT_PORT;
        String MQTT_USER;
        String MQTT_PASSWORD;
        String MQTT_URL_FORMAT = "tcp://%s:%d";
        String DEVICE_ID_FORMAT = "TE_%s";

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        MQTT_DOMAIN=prefs.getString("mashpit_domain","");
        MQTT_BROKER=prefs.getString("broker_url","192.168.1.50");
        MQTT_PORT= Integer.parseInt(prefs.getString("broker_port","1884"));
        MQTT_USER = prefs.getString("broker_user","");
        MQTT_PASSWORD = prefs.getString("broker_password","");

        Log.i(DEBUG_TAG, "Preferences read: MQTT Server: "+MQTT_BROKER+" Port: "+MQTT_PORT);

        String url = String.format(Locale.US, MQTT_URL_FORMAT, MQTT_BROKER, MQTT_PORT);
        Log.i(DEBUG_TAG,"Connecting with URL: " + url);

        @SuppressLint("HardwareIds") String mDeviceId = String.format(DEVICE_ID_FORMAT,
                Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
        Log.i(DEBUG_TAG,"Devcice ID: " + mDeviceId);

        isConnecting=true;

        MqttConnectOptions mOpts = new MqttConnectOptions();
//        mOpts.setKeepAliveInterval(0);
        mOpts.setConnectionTimeout(2);
        mOpts.setCleanSession(false);
        if(!MQTT_USER.isEmpty()) {
            mOpts.setUserName(MQTT_USER);
            mOpts.setPassword(MQTT_PASSWORD.toCharArray());
        }

        mClient = new MqttClient(url,mDeviceId,mDataStore);
        mClient.setCallback(this);

        try {
            mClient.connect(mOpts);
        } catch (MqttException e) {
            e.printStackTrace();

            isConnected = false;
            isConnecting = false;
            Log.i(DEBUG_TAG, "Connect failure");
            Log.i(DEBUG_TAG, String.valueOf(e));
            statusEvent.setTopic("mqttstatus");
            statusEvent.setMode("error");
            statusEvent.setStatus("Can't connect to broker");
            EventBus.getDefault().post(statusEvent);
            if (mClient == null) {
                disconnect();
            }
            return;
        }

        isConnected = true;
        Log.i(DEBUG_TAG, "Successfully connected");
        statusEvent.setTopic("mqttstatus");
        statusEvent.setMode("info");
        statusEvent.setStatus("Connected to broker");
        EventBus.getDefault().post(statusEvent);

        isConnecting = false;
    }
    private void checkConnection()
    {
        Log.i(DEBUG_TAG, "checkConnection()");
        statusEvent.setTopic("mqttstatus");
        if(isConnected)
        {
            statusEvent.setMode("info");
            statusEvent.setStatus("Connected to broker");
            Log.i(DEBUG_TAG, "checkConnection()=true");
        }
        else
        {
            statusEvent.setMode("error");
            statusEvent.setStatus("Can't connect to broker");
            Log.i(DEBUG_TAG, "checkConnection()=false");
        }
        EventBus.getDefault().post(statusEvent);
    }

    /**
     * Checkes the current connectivity
     * and reconnects if it is required.
     */
    private synchronized void reconnectIfNecessary() {
        Log.i(DEBUG_TAG, "reconnectIfNecessary()");
        if(mClient== null) {
                Log.i(DEBUG_TAG, "reconnectIfNecessary(): try to connect");
            try {
                                connect();
                        } catch (MqttException e) {
                                e.printStackTrace();
                        }
        }
    }

    private void disconnect()
    {
        if(mClient!=null)
        {
            try {
                mClient.disconnect();
            } catch (MqttException ignored) {
            }
        }
    }

    private void registerBroadcastReceivers() {
                  if (networkConnectionMonitor == null) {
                          networkConnectionMonitor = new NetworkConnectionIntentReceiver();
                          registerReceiver(networkConnectionMonitor, new IntentFilter(
                                          ConnectivityManager.CONNECTIVITY_ACTION));
                  }
    }

    private void unregisterBroadcastReceivers(){
          if(networkConnectionMonitor != null){
                  unregisterReceiver(networkConnectionMonitor);
                  networkConnectionMonitor = null;
          }
    }

    private class NetworkConnectionIntentReceiver extends BroadcastReceiver {

        @SuppressLint("Wakelock")
                @Override
        public void onReceive(Context context, Intent intent)
        {
                // we protect against the phone switching off
                // by requesting a wake lock - we request the minimum possible wake
                // lock - just enough to keep the CPU running until we've finished
                PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            PowerManager.WakeLock wl = null;
            if (pm != null) {
                wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MQTT:MashPit");
            }
            if (wl != null) {
                wl.acquire(1);
            }

            Log.i(DEBUG_TAG, "Internal network status receive called with: "+intent);

                boolean noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
                String reason = intent.getStringExtra(ConnectivityManager.EXTRA_REASON);
                boolean isFailover = intent.getBooleanExtra(ConnectivityManager.EXTRA_IS_FAILOVER, false);

                        Log.i(DEBUG_TAG, "Extra Reason: "+reason);
                        if(noConnectivity)
                        {
                                Log.i(DEBUG_TAG, "NotifClientsOffline");
                                mClient=null;
                        }

                        if(isFailover)
                {
                        Log.i(DEBUG_TAG,"IsFailover: TRUE");
                }
                else
                {
                        Log.i(DEBUG_TAG,"IsFailover: FALSE");
                }

               if (!isConnecting) {
                   if (isOnline()) {
                       Log.i(DEBUG_TAG, "isOnline:TRUE");
                       if (!isConnected) {
                           mClient = null;
                           Log.i(DEBUG_TAG, "Not connected: trying to reconnect");
                           reconnectIfNecessary();
                       }
                   } else {
                       Log.i(DEBUG_TAG, "isOnline:FALSE");
                   }
            }

            if (wl != null) {
                wl.release();
            }
        }
    }

        /**
     * @return whether the android service can be regarded as online
     */
    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        return cm != null && cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected() && backgroundDataEnabled;
    }

    @Override
    public void connectionLost(Throwable arg0) {
        if (!isConnecting) {
            Log.i(DEBUG_TAG, "Connection lost from broker! Reason: ", arg0);
            isConnected = false;
            statusEvent.setTopic("mqttstatus");
            statusEvent.setMode("error");
            statusEvent.setStatus("Connection lost!");
            EventBus.getDefault().post(statusEvent);

            if (mClient != null) {

                try {
                    Log.i(DEBUG_TAG, "Disconnecting...");
                    mClient.disconnect();
                    mClient = null;
                    reconnectIfNecessary();
                } catch (MqttException e) {
                    Log.i(DEBUG_TAG, "Disconnect failed");
                    mClient = null;
                }
            }

            reconnectIfNecessary();
        }
    }

    private void updateNotification(long timestamp,String status,String sensor,String event) {
        Log.i(DEBUG_TAG, sensor + ": " + event);
        SimpleDateFormat fmtout = new SimpleDateFormat("HH:mm:ss", Locale.GERMANY);
        Date df = new java.util.Date(timestamp*1000);
        builder.setColor(Color.BLACK);
        if(status.equals("NOK")) {
            builder.setColor(Color.RED);
        }
        builder.setContentTitle(event);
        builder.setContentText(new StringBuilder().append(sensor).append(" / ").append(fmtout.format(df)));
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null) {
            mNotificationManager.notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, builder.build());
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        TemperatureEvent tempEvent = new TemperatureEvent();
        ProcessEvent processEvent = new ProcessEvent();
        MPStatusEvent mpstatusEvent = new MPStatusEvent();
        boolean exists;
        JSONObject obj;

        String mess = new String(message.getPayload());

        String[] parts = topic.split("/");
        tempEvent.setTopic(parts[1]);
        processEvent.setTopic(parts[1]);

        Log.i(DEBUG_TAG, "'" + parts[1] + "/" + parts[2] + "' messageArrived with QoS: " + message.getQos());

        if(parts[1].equals("SE")){
            handleSensorData(parts,mess);
            return;
        }
        if (parts[1].equals("temp")) {
            obj = new JSONObject(mess);
            try {
                tempEvent.setEvent(obj.getString("Temp") + "°");
                tempEvent.setTemperature(obj.getString("Temp"));
                tempEvent.setMode(obj.getString("Mode"));
                tempEvent.setSensor(parts[2]);
                tempEvent.setInterval(Integer.parseInt(parts[3]));
                tempEvent.setTimestamp(Long.parseLong(obj.getString("TS")));
                tempEvent.setQoS(message.getQos());
                tempEvent.setStatus("OK");
                Temperature temp = new Temperature(tempEvent.getTimestamp(), tempEvent.getTemperature(), tempEvent.getSensor(), tempEvent.getMode());
                if (message.getQos() > 0) {
                    temp.save();
                    Log.i(DEBUG_TAG, "Mode: " + tempEvent.getMode() + " Temperature: " + tempEvent.getEvent() + " inserted!");
                }
                EventBus.getDefault().postSticky(tempEvent);

                sendData(tempEvent.getMode(), tempEvent.getSensor(), tempEvent.getEvent());

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (parts[1].equals("MP")) {
            if (parts[2].equals("process")) {
                Log.i(DEBUG_TAG, "Process: ");
                Process proc = Process.load(Process.class, 1);
                if (proc == null) {
                    Process nproc = new Process(mess);
                    nproc.save();
                    Log.i(DEBUG_TAG, "Process inserted");
                } else {
                    proc.myJSONString = mess;
                    proc.save();
                    Log.i(DEBUG_TAG, "Process updated");
                }
                EventBus.getDefault().postSticky(processEvent);
            }

            if (parts[3].equals("conf")) {
                Log.i(DEBUG_TAG, "Configuration: " + parts[2] + "/" + parts[4]);

                if (mess.isEmpty()) {
                    new Delete()
                            .from(Config.class)
                            .where("name = ?", parts[4])
                            .and("MPServer = ?", parts[2])
                            .execute();
                    Log.i(DEBUG_TAG, "Configuration deleted!");
                } else {
                    obj = new JSONObject(mess);

                    if (parts[4].equals("MashPit")) {
                        exists = new Select()
                                .from(MPServer.class)
                                .where("name = 'MashPit'")
                                .and("MPServer = ?", parts[2])
                                .exists();
                        if (exists) {
                            new Update(MPServer.class)
                                    .set("alias = ?,TS = ?", obj.getString("alias"), obj.getLong("TS"))
                                    .where("name = ? and " + "MPServer = ?", parts[4], parts[2])
                                    .execute();
                        } else {
                            MPServer mpServer = new MPServer(parts[4], parts[2], obj.getString("alias"), obj.getLong("TS"));
                            mpServer.save();
                        }
                        mpstatusEvent.setAlias(obj.getString("alias"));
                        mpstatusEvent.setMPServer(parts[2]);
                        mpstatusEvent.setType("SRV");
                        mpstatusEvent.setStatusTopic("MashPit");
                        EventBus.getDefault().postSticky(mpstatusEvent);
                    } else {
                        exists = new Select()
                                .from(Config.class)
                                .where("name = ?", parts[4])
                                .and("MPServer = ?", parts[2])
                                .exists();
                        if (exists) {
                            new Update(Config.class)
                                    .set("type = ?,topic = ?,active = ?,temp = ?,minmax = ?,time = ?,hysterese = ?,GPIO = ?,IRid = ?,IRcode = ?",
                                            obj.getString("type"), obj.getString("topic"), obj.getBoolean("active") ? 1 : 0, obj.getString("temp"),
                                            obj.getBoolean("minmax") ? 1 : 0, obj.getString("time"), obj.getString("hysterese"), obj.getString("GPIO"), obj.getString("IRid"),
                                            obj.getString("IRcode"))
                                    .where("name = ? and " + "MPServer = ?", parts[4], parts[2])
                                    .execute();
                        } else {
                            Config config = new Config(parts[4], parts[2], obj.getString("type"), obj.getString("topic"), obj.getBoolean("active"), obj.getString("temp"),
                                    obj.getBoolean("minmax"), obj.getString("time"), obj.getString("hysterese"), obj.getString("GPIO"), obj.getString("IRid"),
                                    obj.getString("IRcode"));
                            config.save();
                        }
                    }
                }
            }
            if (parts[3].equals("status")) {
                Log.i(DEBUG_TAG, "Status: " + parts[2] + "/" + parts[4]);
                mpstatusEvent.setMPServer(parts[2]);
                mpstatusEvent.setStatusTopic(parts[4]);

                if(mess.isEmpty())
                {
                    new Delete()
                            .from(MPStatus.class)
                            .where("topic = ?", parts[4])
                            .and("MPServer = ?", parts[2])
                            .execute();
                    mpstatusEvent.setPID("DEL");
                    EventBus.getDefault().postSticky(mpstatusEvent);
                    Log.i(DEBUG_TAG, "Status deleted!");
                }
                else {
                    obj = new JSONObject(mess);

                    try {
                        if (obj.getString("status").equals("0")) {
                            mpstatusEvent.setActive(false);
                        } else {
                            mpstatusEvent.setActive(true);
                        }
                        mpstatusEvent.setPID(obj.getString("PID"));
                        mpstatusEvent.setType(obj.getString("Type"));

                        exists = new Select()
                                .from(MPStatus.class)
                                .where("topic = ?", mpstatusEvent.getStatusTopic())
                                .and("MPServer = ?", mpstatusEvent.getMPServer())
                                .exists();

                        if (exists) {
                            new Update(MPStatus.class)
                                    .set("active = ?," + "PID = ?," + "Type = ?", obj.getString("status"), mpstatusEvent.getPID(), mpstatusEvent.getType())
                                    .where("topic = ? and " + "MPServer = ?", mpstatusEvent.getStatusTopic(), mpstatusEvent.getMPServer())
                                    .execute();
                        } else {
                            MPStatus mpstatus = new MPStatus(mpstatusEvent.getStatusTopic(), mpstatusEvent.getMPServer(), mpstatusEvent.isActive(), mpstatusEvent.getPID(), mpstatusEvent.getType());
                            mpstatus.save();
                        }
                        EventBus.getDefault().postSticky(mpstatusEvent);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void handleSensorData(String[] topic,String mess)
    {
        switch(topic[3])
        {
            case "conf":
                handleSensorConf(topic,mess);
                break;

            case "status":
                handleSensorStatus(topic,mess);
                break;

            default:
                break;
        }
    }

    private void handleSensorConf(String[] topic,String mess)
    {
        JSONObject obj;
        SensorEvent sensorEvent = new SensorEvent();
        int port=0;

        if(mess.isEmpty())
        {
            new Delete().from(Sensors.class)
                    .where("server=?", topic[2])
                    .and("sensor=?", topic[5])
                    .and("interval=?", topic[6])
                    .execute();

            Log.i(DEBUG_TAG, "Configuration deleted!");
            sensorEvent.setServer(topic[2]);
            sensorEvent.setSensor(topic[5]);
            sensorEvent.setInterval(Integer.parseInt(topic[6]));
            sensorEvent.setType(topic[4]);
            EventBus.getDefault().postSticky(sensorEvent);
            return;
        }

        try {
            obj = new JSONObject(mess);
            if(obj.has("PIN"))
            {
                port=obj.getInt("PIN");
            }

            boolean exists = new Select()
                    .from(Sensors.class)
                    .where("server=?", topic[2])
                    .and("sensor=?", topic[5])
                    .and("interval=?", topic[6])
                    .exists();
            if (exists) {
                new Update(Sensors.class)
                        .set("active=?, name=?, port=?", obj.getBoolean("active") ? 1 : 0, obj.getString("name"),port)
                        .where("server=? and sensor=? and interval=?", topic[2], topic[5], topic[6])
                        .execute();
            } else {
                Sensors sensors = new Sensors(topic[2], topic[5], false, obj.getBoolean("active"), "", obj.getString("type"), obj.getString("name"),
                        Integer.parseInt(topic[6]), port, "");
                sensors.save();
            }
            sensorEvent.setServer(topic[2]);
            sensorEvent.setSensor(topic[5]);
            sensorEvent.setInterval(Integer.parseInt(topic[6]));
            sensorEvent.setType(topic[4]);
            sensorEvent.setActive(obj.getBoolean("active"));
            sensorEvent.setName(obj.getString("name"));
            EventBus.getDefault().postSticky(sensorEvent);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handleSensorStatus(String[] topic, String mess) {
        JSONObject obj;
        SensorEvent sensorEvent = new SensorEvent();

        if (mess.isEmpty()) {
            new Delete()
                    .from(SensorStatus.class)
                    .where("server = ?", topic[2])
                    .execute();
            Log.i(DEBUG_TAG, "Sensor status deleted!");
            sensorEvent.setServer(topic[2]);
            sensorEvent.setType(topic[4]);
            sensorEvent.setActive(false);
            EventBus.getDefault().postSticky(sensorEvent);
            return;
        }

        try {
            obj = new JSONObject(mess);
            boolean status = obj.getInt("status") == 1;
            long TS=0;
            String system="--";
            String version="--";
            if(obj.has("TS")) {TS = obj.getLong("TS");}
            if(obj.has("system")) {system = obj.getString("system"); }
            if(obj.has("version")) {version = obj.getString("version");}

            boolean exists = new Select()
                    .from(SensorStatus.class)
                    .where("server=?", topic[2])
                    .exists();
            if (exists) {
                new Update(SensorStatus.class)
                        .set("active=?,TS=?,system=?,version=?", obj.getInt("status"),TS,system,version)
                        .where("server=?", topic[2])
                        .execute();
                Log.i(DEBUG_TAG, "Sensor server status updated!");

            } else {
                Log.i(DEBUG_TAG, "New sensor server created!");
                SensorStatus newSensorStat = new SensorStatus(topic[2], "", status, "", "",TS,system,version);
                newSensorStat.save();
            }

            sensorEvent.setServer(topic[2]);
            sensorEvent.setActive(status);

            if (!status) {
                EventBus.getDefault().postSticky(sensorEvent);
            }
            else {
                JSONArray sensors_json = obj.getJSONArray("sensors");
                StringBuilder sensors = new StringBuilder();
                for (int i = 0; i < sensors_json.length(); i++) {
                    sensors.append(sensors_json.get(i).toString());
                    if (i < sensors_json.length() - 1) {
                        sensors.append("/");
                    }
                }
                Log.i(DEBUG_TAG, "Sensors connected: "+sensors);

                new Update(SensorStatus.class)
                        .set("active=?, alias=?, sensor=?", obj.getInt("status"), obj.getString("alias"),sensors.toString())
                        .where("server=?", topic[2])
                        .execute();

                sensorEvent.setSensor(sensors.toString());
                sensorEvent.setName(obj.getString("alias"));
                EventBus.getDefault().postSticky(sensorEvent);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.BACKGROUND)
    public void onEventMainThread(TemperatureEvent myEvent) {
        Set<String> prefdefaults = prefs.getStringSet("service_topics", new HashSet<String>());
        if(prefdefaults.contains(myEvent.getSensor()+"/"+myEvent.getInterval()))
        {
            Log.i(DEBUG_TAG, "Notification updated");
            updateNotification(myEvent.getTimestamp(),myEvent.getStatus(),myEvent.getSensor(), myEvent.getEvent());
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    @Override
    public void onDataChanged(@NonNull DataEventBuffer dataEventBuffer) {
        Log.d(DEBUG_TAG, "onDataChanged: " + dataEventBuffer);
        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                String path = event.getDataItem().getUri().getPath();
                if (Constants.WEAR.RUN_UPDATE_NOTIFICATION.equals(path)) {
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    String message = dataMapItem.getDataMap().getString(Constants.WEAR.KEY_CONTENT);
                    Log.d(DEBUG_TAG, "Wear activity received message: " + message);
                } else {
                    Log.d(DEBUG_TAG, "Unrecognized path: " + path);
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                Log.d(DEBUG_TAG, "Data deleted : " + event.getDataItem().toString());
            } else {
                Log.d(DEBUG_TAG, "Unknown data event Type = " + event.getType());
            }
        }
    }

    private void sendData(String mode, String sensor, String message) {
        PutDataMapRequest dataMap = PutDataMapRequest.create(Constants.WEAR.RUN_UPDATE_NOTIFICATION);
        dataMap.getDataMap().putString(Constants.WEAR.KEY_TITLE, "Temperature");
        dataMap.getDataMap().putString(Constants.WEAR.KEY_SENSOR, sensor);
        dataMap.getDataMap().putString(Constants.WEAR.KEY_MODE, mode);
        dataMap.getDataMap().putString(Constants.WEAR.KEY_CONTENT, message);
        PutDataRequest request = dataMap.asPutDataRequest();
        request.setUrgent();

        Task<DataItem> dataItemTask = Wearable.getDataClient(this).putDataItem(request);
        dataItemTask
                .addOnSuccessListener(new OnSuccessListener<DataItem>() {
                    @Override
                    public void onSuccess(DataItem dataItem) {
                        Log.d(DEBUG_TAG, "Sending message was successful: " + dataItem);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(DEBUG_TAG, "Sending message failed: " + e);
                    }
                });
    }
}
