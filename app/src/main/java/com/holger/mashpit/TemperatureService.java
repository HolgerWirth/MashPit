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
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.holger.mashpit.events.ProcessEvent;
import com.holger.mashpit.events.StatusEvent;
import com.holger.mashpit.events.TemperatureEvent;
import com.holger.mashpit.model.Process;
import com.holger.mashpit.model.Subscriber;
import com.holger.mashpit.model.Temperature;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
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

public class TemperatureService extends Service implements MqttCallback {

    private static final String DEBUG_TAG = "TemperatureService";

    private static final String NOTIFICATION_CHANNEL_ID = "MashPitChannel_1";

    private MqttDefaultFilePersistence mDataStore=null;

    private MqttAsyncClient mClient;                                        // Mqtt Client

    private Boolean isConnected=false;
    private Boolean isConnecting=false;

    private NotificationCompat.Builder builder;
    private StatusEvent statusEvent = new StatusEvent();

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
                            .setContentTitle("Temperature Title")
                            .setTicker("Temperature Ticker")
                            .setContentText("My Temperature")
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

                    try {
                        connect();
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }

                    break;
                case Constants.ACTION.STOPFOREGROUND_ACTION:
                    Log.i(DEBUG_TAG, "Received Stop Foreground Intent");
                    unregisterBroadcastReceivers();
                    disconnect();
                    stopForeground(true);
                    stopSelf();
                    break;
                case Constants.ACTION.CANCEL_ACTION:
                    Log.i(DEBUG_TAG, "Clicked Cancel");
                    unregisterBroadcastReceivers();
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
        String MQTT_URL_FORMAT = "tcp://%s:%d";
        String DEVICE_ID_FORMAT = "TE_%s";

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        MQTT_BROKER=prefs.getString("broker_url","192.168.1.50");
        MQTT_PORT= Integer.parseInt(prefs.getString("broker_port","1884"));

        Log.i(DEBUG_TAG, "Preferences read: MQTT Server: "+MQTT_BROKER+" Port: "+MQTT_PORT);

        String url = String.format(Locale.US, MQTT_URL_FORMAT, MQTT_BROKER, MQTT_PORT);
        Log.i(DEBUG_TAG,"Connecting with URL: " + url);

        @SuppressLint("HardwareIds") String mDeviceId = String.format(DEVICE_ID_FORMAT,
                Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
        Log.i(DEBUG_TAG,"Devcice ID: " + mDeviceId);

        isConnecting=true;

        MqttConnectOptions mOpts = new MqttConnectOptions();
//        mOpts.setKeepAliveInterval(0);
        mOpts.setConnectionTimeout(5000);
        mOpts.setCleanSession(false);

        mClient = new MqttAsyncClient(url,mDeviceId,mDataStore);
        mClient.connect(mOpts,null, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                try {
                    isConnected=true;
                    Log.i(DEBUG_TAG, "Successfully connected");
                    mClient.setCallback(TemperatureService.this);

                    statusEvent.setTopic("mqttstatus");
                    statusEvent.setMode("info");
                    statusEvent.setStatus("Connected to broker");
                    EventBus.getDefault().post(statusEvent);

                    List<Subscriber> delresult = new ArrayList<>();
                    String delsubs=prefs.getString("delsublist","");
                    int subs_count;
                    if(delsubs.length()>0) {
                        try {
                            JSONObject subscribers = new JSONObject(delsubs);
                            JSONArray subarray = subscribers.getJSONArray("delsubscriber");
                            subs_count=subarray.length();
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
                    }
                    String[] topic = new String[delresult.size()];
                    for(int i=0;i<delresult.size();i++)
                    {
                        Subscriber sub = delresult.get(i);
                        String mytopic="/temp/"+sub.topic+"/"+sub.interval;
                        topic[i]=mytopic;
                    }
                    for (String aTopic : topic) {
                        Log.i(DEBUG_TAG, "Unubscribe from: " + aTopic);
                    }
                    try {
                        mClient.unsubscribe(topic);
                    }
                    catch(MqttException e)
                    {
                        Log.i(DEBUG_TAG, "Can't unsubscribe");
                    }
                    prefs.edit().putString("delsublist","").apply();
                    Log.i(DEBUG_TAG, "Successfully unsubscribed");

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
                    topic = new String[result.size()+1];
                    int[] qos = new int[result.size()+1];
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
                    topic[result.size()]="/process";
                    qos[result.size()]=0;

                    for(int i=0;i<topic.length;i++)
                    {
                        Log.i(DEBUG_TAG, "Subscribe to: "+topic[i]+" with qos: "+qos[i]);
                    }

                    mClient.subscribe(topic,qos);
                    Log.i(DEBUG_TAG, "Successfully subscribed");

                    isConnecting=false;
                } catch (MqttException e) {
                    isConnected=true;
                    isConnecting=false;
                    Log.i(DEBUG_TAG,"Error during connect");
                    Log.i(DEBUG_TAG, "Connection lost from broker! Reason: ",e);

                    statusEvent.setTopic("mqttstatus");
                    statusEvent.setMode("error");
                    statusEvent.setStatus("Connection lost!");
                    EventBus.getDefault().post(statusEvent);
                }
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                isConnected=false;
                isConnecting=false;
                Log.i(DEBUG_TAG,"Connect failure");
                Log.i(DEBUG_TAG, String.valueOf(exception));
                statusEvent.setTopic("mqttstatus");
                statusEvent.setMode("error");
                statusEvent.setStatus("Can't connect to broker");
                EventBus.getDefault().post(statusEvent);
                if (mClient==null) {
                    disconnect();
                }
            }
        });
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

    @SuppressWarnings("deprecation")
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
                wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MQTT");
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

        String mess= new String(message.getPayload());
        JSONObject obj = new JSONObject(mess);

        String[] parts = topic.split("/");
        tempEvent.setTopic(parts[1]);
        processEvent.setTopic(parts[1]);

        Log.i(DEBUG_TAG, "'"+parts[1]+"' messageArrived with QoS: "+message.getQos()+" Message: "+mess);

        if(parts[1].equals("temp")) {
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
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
        if(parts[1].equals("process"))
        {
            Log.i(DEBUG_TAG, "Process: ");
//            processEvent.setTemp(obj.getString("temp") + "°");

            Process proc = Process.load(Process.class,1);
            if(proc==null) {
                Process nproc = new Process(mess);
                nproc.save();
                Log.i(DEBUG_TAG, "Process inserted");
            }
            else
            {
                proc.myJSONString=mess;
                proc.save();
                Log.i(DEBUG_TAG, "Process updated");
            }
            EventBus.getDefault().postSticky(processEvent);
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.BACKGROUND)
    public void onEventMainThread(TemperatureEvent myEvent) {
        Set<String> prefdefaults = prefs.getStringSet("service_topics", new HashSet<String>());
        if(prefdefaults.contains(myEvent.getSensor()+"/"+String.valueOf(myEvent.getInterval())))
        {
            Log.i(DEBUG_TAG, "Notification updated");
            updateNotification(myEvent.getTimestamp(),myEvent.getStatus(),myEvent.getSensor(), myEvent.getEvent());
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }
}
