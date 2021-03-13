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
import com.holger.mashpit.events.SensorDataEvent;
import com.holger.mashpit.events.SensorEvent;
import com.holger.mashpit.events.SensorStickyEvent;
import com.holger.mashpit.events.StatusEvent;
import com.holger.mashpit.model.Config;
import com.holger.mashpit.model.MPServer;
import com.holger.mashpit.model.MPStatus;
import com.holger.mashpit.model.Process;
import com.holger.mashpit.model.SensorStatus;
import com.holger.mashpit.model.Sensors;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Locale;

import com.holger.mashpit.tools.PreferenceHandler;
import com.holger.mashpit.tools.SubscriptionHandler;
import com.holger.share.Constants;

public class TemperatureService extends Service implements MqttCallback,DataClient.OnDataChangedListener {

    private static final String DEBUG_TAG = "TemperatureService";

    private static final String NOTIFICATION_CHANNEL_ID = "MashPitChannel_1";

    private final MqttDefaultFilePersistence mDataStore=null;

    private MqttClient mClient;                                        // Mqtt Client

    private Boolean isConnected=false;
    private Boolean isConnecting=false;
    private NotificationCompat.Builder builder;
    private String MQTT_DOMAIN="";

    private NetworkConnectionIntentReceiver networkConnectionMonitor;
    SharedPreferences prefs;

    SubscriptionHandler subsHandlerService;
    SubscriptionHandler subsHandlerChart;
    SensorStickyEvent stickyData;

    private PowerManager.WakeLock wakeLock;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(DEBUG_TAG, "onCreate()...");
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        subsHandlerService = new SubscriptionHandler("Service");
        subsHandlerChart = new SubscriptionHandler("Chart");
        stickyData = new SensorStickyEvent();
    }

    @SuppressLint("WakelockTimeout")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action;
        if(intent==null)
        {
                Log.i(DEBUG_TAG, "onStartCommand: received null intent");
                action=Constants.ACTION.STARTFOREGROUND_ACTION;
        }
        else
        {
                action = intent.getAction();
                Log.i(DEBUG_TAG, "onStartCommand: Received action of " + action);
        }

        if (action != null) {
            switch (action) {
                case Constants.ACTION.STARTFOREGROUND_ACTION:
                    Log.i(DEBUG_TAG, "Received Start Foreground Intent ");

                    Log.i(DEBUG_TAG, "Create WakeLock!");
                    PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
                    assert powerManager != null;
                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                            "MashPit::MQTTWakeLock");
                    wakeLock.acquire();

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

                    /*
                    if(!EventBus.getDefault().isRegistered(this)) {
                        Log.i(DEBUG_TAG, "EventBus register");
                        EventBus.getDefault().register(this);
                    }

                     */

                    registerBroadcastReceivers();
                    Wearable.getDataClient(this).addListener(this);

                    try {
                        connect();
                    } catch (MqttException e) {
                        e.printStackTrace();
                        return(START_STICKY);
                    }

                    new Delete()
                            .from(MPServer.class)
                            .execute();
                    new Delete()
                            .from(Sensors.class)
                            .execute();

                    List<String> delTopics = PreferenceHandler.getArrayList(this, "delTopics");
                    boolean failedDel = false;
                    for (String sub : delTopics) {
                        try {
                            Log.i(DEBUG_TAG, "Unsubscribe: " + MQTT_DOMAIN + sub);
                            mClient.unsubscribe(MQTT_DOMAIN + sub);
                        } catch (MqttException e) {
                            failedDel=true;
                            e.printStackTrace();
                        }
                    }
                    if(!failedDel)
                    {
                        PreferenceHandler.removePreference(this,"delTopics");
                    }

                    List<String> topics = subsHandlerService.getAllSubscription(false);
                    List<String> topics_durable = subsHandlerService.getAllSubscription(true);
                    topics.add("/SE/"+"+/"+"conf/#");
                    topics.add("/SE/"+"+/"+"status");
//                    topics.add("/MP/#");

                    try {
                        for (String sub : topics) {
                            Log.i(DEBUG_TAG, "Nondurable subsctiption to: " +MQTT_DOMAIN+ sub + " with qos: 0");
                            mClient.subscribe(MQTT_DOMAIN+sub, 0);
                        }
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }

                    try {
                        for (String sub : topics_durable) {
                            Log.i(DEBUG_TAG, "Durable subscription to: " +MQTT_DOMAIN+ sub + " with qos: 2");
                            mClient.subscribe(MQTT_DOMAIN+sub, 2);
                        }
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                    break;

                case Constants.ACTION.STOPFOREGROUND_ACTION:
                    Log.i(DEBUG_TAG, "Received Stop Foreground Intent");
                    if(wakeLock!=null)
                    {
                        Log.i(DEBUG_TAG, "Remove WakeLock");
                        wakeLock.release();
                    }
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
                        refreshRetainedMessages();
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

                case Constants.ACTION.SUBSCRIBE_ACTION:
                    Log.i(DEBUG_TAG, "Subscribe action");
                    subscribeTopic(intent.getStringExtra("TOPIC"),intent.getBooleanExtra("DURABLE",false));
                    break;

                case Constants.ACTION.UNSUBSCRIBE_ACTION:
                    Log.i(DEBUG_TAG, "Unsubscribe action");
                    unsubscribeTopic(intent.getStringExtra("TOPIC"));
                    break;
            }
        }
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if(wakeLock!=null)
        {
            wakeLock.release();
            Log.i(DEBUG_TAG, "Remove WakeLock");
        }
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
        StatusEvent statusEvent = new StatusEvent();

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
        StatusEvent statusEvent = new StatusEvent();
        Log.i(DEBUG_TAG, "checkConnection()");
        statusEvent.setTopic("mqttstatus");

        if(mClient==null)
        {
            isConnected=false;
            statusEvent.setMode("error");
            statusEvent.setStatus("Can't connect to broker");
            Log.i(DEBUG_TAG, "checkConnection()=false");
        }
        else {
            if (mClient.isConnected()) {
                isConnected = true;
                statusEvent.setMode("info");
                statusEvent.setStatus("Connected to broker");
                Log.i(DEBUG_TAG, "checkConnection()=true");
            } else {
                isConnected = false;
                statusEvent.setMode("error");
                statusEvent.setStatus("Can't connect to broker");
                Log.i(DEBUG_TAG, "checkConnection()=false");
            }
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
                                refreshRetainedMessages();
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
        boolean backgroundDataEnabled = true;
        return cm != null && cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected() && backgroundDataEnabled;
    }

    @Override
    public void connectionLost(Throwable arg0) {
        StatusEvent statusEvent = new StatusEvent();
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

    private void updateNotification(SensorDataEvent event) {
        Log.i(DEBUG_TAG, "updateNotification");
        builder.setColor(Color.BLACK);
        String server = subsHandlerService.getServerAlias(event.getServer());
        String sensor = subsHandlerService.getSensorAlias(event.getServer(),event.getSensor());
        builder.setContentTitle(event.getData("Temp") + "°");
        builder.setContentText(new StringBuilder().append(server).append(" / ").append(sensor).append("  ").append(event.getTimestamp()));
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null) {
            mNotificationManager.notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, builder.build());
        }

        sendDatatoWear(server, sensor, event.getData("Temp") + "°");
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        ProcessEvent processEvent = new ProcessEvent();
        MPStatusEvent mpstatusEvent = new MPStatusEvent();

        boolean exists;
        JSONObject obj;

        String mess = new String(message.getPayload());

        String[] parts = topic.split("/");
        processEvent.setTopic(parts[1]);

        Log.i(DEBUG_TAG, "'" + parts[2] + "/" + parts[3] + "' messageArrived with QoS: " + message.getQos());

        if(parts[1].equals("SE")){
            if(parts[3].equals("temp"))
            {
                Log.i(DEBUG_TAG, "'" + parts[4] + "/" + parts[5] + "' Temperature message arrived with QoS: " + message.getQos());
                submitSensorData(parts,mess);
                return;
            }
            handleSensorData(topic,mess);
            return;
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
                EventBus.getDefault().post(processEvent);
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
                        EventBus.getDefault().post(mpstatusEvent);
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
                    EventBus.getDefault().post(mpstatusEvent);
                    Log.i(DEBUG_TAG, "Status deleted!");
                }
                else {
                    obj = new JSONObject(mess);

                    try {
                        mpstatusEvent.setActive(!obj.getString("status").equals("0"));
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
                        EventBus.getDefault().post(mpstatusEvent);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void submitSensorData(String[] parts, String mess) {
        SensorDataEvent sensorData = new SensorDataEvent();
        sensorData.setServer(parts[2]);
        sensorData.setSensor(parts[4]);
        sensorData.setInterval(Integer.parseInt(parts[5]));
        sensorData.setType(parts[3]);
        sensorData.setData(mess);
        EventBus.getDefault().post(sensorData);

        if (subsHandlerService.checkSubscription(sensorData.getTopicString())) {
            Log.i(DEBUG_TAG, "Notification updated");
            updateNotification(sensorData);
        }

        if (subsHandlerChart.checkSubscription(sensorData.getTopicString())) {
            Log.i(DEBUG_TAG, "Chart data saved!");
        }

        stickyData.addSticky(sensorData);
        EventBus.getDefault().postSticky(stickyData);
    }

    private void handleSensorData(String topic,String mess)
    {
        String[] parts = topic.split("/");
        switch(parts[3])
        {
            case "conf":
                handleSensorConf(topic,mess);
                break;

            case "status":
                handleSensorStatus(parts,mess);
                break;

            default:
                break;
        }
    }

    private void handleSensorConf(String key,String mess)
    {
        JSONObject obj;
        SensorEvent sensorEvent = new SensorEvent();
        String family;
        String dir="IN";
        String reg="";
        String name="";
        int port=0;
        int sda=0;
        int scl=0;
        int alt=0;
        int hyst=0;
        int interval;
        String type="";
        String event="";
        String mcp="";

        String[] topic = key.split("/");
        String server=topic[2];
        String sensor;

        switch(topic[4]) {
            case "update":
                return;

            case "server":
                try {
                    obj = new JSONObject(mess);
                    if (obj.has("alias")) {
                        new Update(SensorStatus.class)
                                .set("alias=?", obj.getString("alias"))
                                .where("server=? and active=1", topic[2])
                                .execute();
                        Log.i(DEBUG_TAG, "Sensor server alias updated!");
                        return;

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            case "GPIO":
                family = "EV";
                dir = topic[5];
                interval = 0;
                sensor="GPIO";
                event=topic[6];
                break;

            case "MCP":
                family = "EV";
                dir = topic[5];
                interval = 0;
                sensor=topic[6];
                event=topic[6];
                break;

            default:
                family="SE";
                interval = Integer.parseInt(topic[6]);
                sensor=topic[5];
                event="";
                break;
        }

        if(mess.isEmpty()) {
                new Delete().from(Sensors.class)
                        .where("topic=?", key)
                        .execute();
                Log.i(DEBUG_TAG, key+ "deleted!");

            sensorEvent.setFamily(family);
            sensorEvent.setDir(dir);
            sensorEvent.setServer(server);
            sensorEvent.setSensor(sensor);
            sensorEvent.setEvent(event);
            sensorEvent.setInterval(interval);
            sensorEvent.setName("");
            sensorEvent.setType(topic[4]);
            EventBus.getDefault().post(sensorEvent);
            return;
        }

        try {
            obj = new JSONObject(mess);
            if(obj.has("reg"))
            {
                reg=obj.getString("reg");
            }
            if(obj.has("evtype"))
            {
                type=obj.getString("evtype");
            }
            if(obj.has("type"))
            {
                type=obj.getString("type");
            }
            if(obj.has("hyst"))
            {
                hyst=obj.getInt("hyst");
            }
            if(obj.has("PIN"))
            {
                port=obj.getInt("PIN");
            }
            if(obj.has("pin"))
            {
                port=obj.getInt("pin");
            }
            if(obj.has("name"))
            {
                name=obj.getString("name");
            }
            if(obj.has("SDA"))
            {
                sda=obj.getInt("SDA");
            }
            if(obj.has("SCL"))
            {
                scl=obj.getInt("SCL");
            }
            if(obj.has("ALT"))
            {
                alt=obj.getInt("ALT");
            }
            if(obj.has("mcp"))
            {
                sensor=obj.getString("mcp");
            }

            boolean exists = new Select()
                    .from(Sensors.class)
                    .where("topic=?",key)
                    .exists();

            if (exists) {
                new Update(Sensors.class)
                        .set("interval=?, active=?, name=?, port=?, hyst=?, sda=?, scl=?, alt=?, sensor=?, reg=?", interval, obj.getBoolean("active") ? 1 : 0, name,port,hyst,sda,scl,alt,sensor,reg)
                        .where("topic=?",key)
                        .execute();
                Log.i(DEBUG_TAG, key+" configuration updated!");

            } else {
                Sensors sensors = new Sensors(key,family,dir,server, sensor, event, obj.getBoolean("active"), "", type, name,
                        interval, reg,port,hyst,sda,scl,alt,"");
                sensors.save();
                Log.i(DEBUG_TAG, key+" configuration inserted!");
            }
            sensorEvent.setFamily(family);
            sensorEvent.setDir(dir);
            sensorEvent.setServer(server);
            sensorEvent.setEvent(event);
            sensorEvent.setSensor(sensor);
            sensorEvent.setInterval(interval);
            sensorEvent.setName(topic[6]);
            sensorEvent.setType(topic[4]);
            sensorEvent.setActive(obj.getBoolean("active"));
            EventBus.getDefault().post(sensorEvent);

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
            sensorEvent.setActive(false);
            EventBus.getDefault().post(sensorEvent);
            return;
        }

        try {
            obj = new JSONObject(mess);
            boolean status = obj.getInt("status") == 1;
            StringBuilder sensors = new StringBuilder();
            long TS=0;
            String system="--";
            String version="--";
            String alias="";
            String IPaddress="";
            if(obj.has("TS")) {TS = obj.getLong("TS");}
            if(obj.has("system")) {system = obj.getString("system"); }
            if(obj.has("version")) {version = obj.getString("version");}
            if(obj.has("alias")) {alias = obj.getString("alias");}
            if(obj.has("IP")) {IPaddress = obj.getString("IP");}

            boolean exists = new Select()
                    .from(SensorStatus.class)
                    .where("server=?", topic[2])
                    .exists();
            if (exists) {
                new Update(SensorStatus.class)
                        .set("active=?,TS=?,system=?,alias=?,version=?,IP=?", obj.getInt("status"),TS,system,alias,version,IPaddress)
                        .where("server=?", topic[2])
                        .execute();
                Log.i(DEBUG_TAG, "Sensor server status updated!");

            } else {
                Log.i(DEBUG_TAG, "New sensor server created!");
                SensorStatus newSensorStat = new SensorStatus(topic[2], "", status, "", "",TS,system,version,IPaddress);
                newSensorStat.save();
            }

            sensorEvent.setServer(topic[2]);
            sensorEvent.setActive(status);
            sensorEvent.setName(alias);
            sensorEvent.setTS(TS);
            sensorEvent.setVersion(version);
            sensorEvent.setSystem(system);
            sensorEvent.setIP(IPaddress);
            sensorEvent.setSensor(sensors.toString());

            if (status) {
                JSONArray sensors_json = obj.getJSONArray("sensors");
                for (int i = 0; i < sensors_json.length(); i++) {
                    sensors.append(sensors_json.get(i).toString());
                    if (i < sensors_json.length() - 1) {
                        sensors.append("/");
                    }
                }
                Log.i(DEBUG_TAG, "Sensors connected: " + sensors);

                new Update(SensorStatus.class)
                        .set("active=?, alias=?, sensor=?", obj.getInt("status"), obj.getString("alias"), sensors.toString())
                        .where("server=?", topic[2])
                        .execute();

                sensorEvent.setSensor(sensors.toString());
            }
            EventBus.getDefault().post(sensorEvent);
        } catch (JSONException e) {
            e.printStackTrace();
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

    private void sendDatatoWear(String mode, String sensor, String message) {
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

    private void subscribeTopic(String topic, boolean durable)
    {
        try {
            if(durable) {
                mClient.subscribe(MQTT_DOMAIN+topic, 2);
                Log.e(DEBUG_TAG, "Durable subscription: " + MQTT_DOMAIN+topic);
            }
            else
            {
                mClient.subscribe(MQTT_DOMAIN+topic, 0);
                Log.e(DEBUG_TAG, "Nondurable subscription: " + MQTT_DOMAIN+topic);

            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
        subsHandlerService.refreshSubscription();
    }

    private void unsubscribeTopic(String topic)
    {
        try {
            mClient.unsubscribe(MQTT_DOMAIN+topic);
        } catch (MqttException e) {
            e.printStackTrace();
            List<String> deltopics = PreferenceHandler.getArrayList(this,"delTopics");
            deltopics.add(topic);
            PreferenceHandler.saveArrayList(this, deltopics,"delTopics");
        }
        subsHandlerService.refreshSubscription();
    }

    private void refreshRetainedMessages() {
        List<String> retainedTopics = subsHandlerService.getRetainedSubscription();

        try {
            for (String sub : retainedTopics) {
                Log.i(DEBUG_TAG, "Refresh subscription: " + MQTT_DOMAIN + sub + " with qos: 0");
                mClient.subscribe(MQTT_DOMAIN + sub, 0);
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
