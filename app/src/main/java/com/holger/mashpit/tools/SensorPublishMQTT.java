package com.holger.mashpit.tools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class SensorPublishMQTT extends AsyncTask<Void, Void, Void>  {
    private final MemoryPersistence persistence = new MemoryPersistence();
    private static final String DEBUG_TAG = "SensorPublishMQTT";

    private final boolean MQTT_SSL;
    private String MQTT_PROTOCOL="tcp://";
    private final int MQTT_PORT;
    private final String MQTT_PASSWORD;
    private final String MQTT_USER;
    private final String MQTT_BROKER;
    private final String MQTT_DOMAIN;

    String DEVICE_ID_FORMAT = "TEX_%s";
    private final String clientId;
    private final MySSlSocketFactory factory;

    String topic;
    String send;
    int qos;
    boolean retained;
    boolean success;

    public OnPublishConfiguration mListener;

    @SuppressLint("HardwareIds")
    public SensorPublishMQTT(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        if(prefs.getBoolean("broker_same",false)) {
            MQTT_BROKER = prefs.getString("send_broker_url", "192.168.1.20");
            MQTT_PORT = Integer.parseInt(prefs.getString("send_broker_port", "1884"));
            MQTT_USER =prefs.getString("send_broker_user", "");
            MQTT_PASSWORD =prefs.getString("send_broker_password", "");
            MQTT_DOMAIN = prefs.getString("send_mashpit_domain","");
            MQTT_SSL = prefs.getBoolean("send_broker_ssl",false);
        }
        else {
            MQTT_BROKER = prefs.getString("broker_url", "192.168.1.20");
            MQTT_PORT = Integer.parseInt(prefs.getString("broker_port", "1884"));
            MQTT_USER =prefs.getString("broker_user", "");
            MQTT_PASSWORD =prefs.getString("broker_password", "");
            MQTT_DOMAIN= prefs.getString("mashpit_domain","");
            MQTT_SSL = prefs.getBoolean("broker_ssl",false);
        }

        mListener = (OnPublishConfiguration) context;
        clientId = String.format(DEVICE_ID_FORMAT, Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
        factory = new MySSlSocketFactory(context);

    }

    public interface OnPublishConfiguration {
        void PublishConfigurationCallback(Boolean success); // you can change the parameter here. depends on what you want.
    }

    private MqttClient ConnectMQTT()
    {
        MqttClient mqttClient = null;
        try {
            if(MQTT_SSL)
            {
                MQTT_PROTOCOL="ssl://";
            }
            mqttClient = new MqttClient(MQTT_PROTOCOL + MQTT_BROKER + ":" + MQTT_PORT, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            if(MQTT_SSL) {
                connOpts.setSocketFactory(factory.getSslSocketFactory(null));
            }
                assert MQTT_USER != null;
            if (!MQTT_USER.isEmpty()) {
                connOpts.setUserName(MQTT_USER);
                assert MQTT_PASSWORD != null;
                connOpts.setPassword(MQTT_PASSWORD.toCharArray());
            }
            Log.i(DEBUG_TAG, "Connecting to broker: " + MQTT_BROKER + " as user: " + MQTT_USER);
            mqttClient.connect(connOpts);
            Log.i(DEBUG_TAG, "Connected");
        } catch (MqttException e) {
            success=false;
            e.printStackTrace();
        }
        return mqttClient;
    }

    public void PublishServerUpdate(String server, String send)
    {
        this.send=send;
        this.topic=MQTT_DOMAIN+"/SE/"+server+"/conf/update";
        this.retained=false;
        this.qos=2;
        execute();
    }

    public void PublishServerStatus(String server, String send)
    {
        this.send=send;
        this.topic=MQTT_DOMAIN+"/SE/"+server+"/conf/server";
        this.qos=2;
        this.retained=false;
        execute();
    }

    public void PublishSensorConf(String topic, String send) {
        this.send=send;
        this.topic=topic;
        this.qos=2;
        this.retained=true;
        execute();
    }

    public void PublishEventConf(String server, String dir, String hw, String eventname, String send) {
        this.send=send;
        this.topic=MQTT_DOMAIN+"/SE/"+server+"/conf/"+hw+"/"+dir+"/"+eventname;
        this.qos=2;
        this.retained=true;
        execute();
    }

    public void publishMessage() {
        try {
            MqttClient mqttClient = ConnectMQTT();
            MqttMessage message = new MqttMessage(send.getBytes());
            message.setQos(qos);
            message.setRetained(retained);
            Log.i(DEBUG_TAG, "Configration topic: "+topic);
            mqttClient.publish(topic, message);
            Log.i(DEBUG_TAG, "Configration message published");
            mqttClient.disconnect();
            Log.i(DEBUG_TAG, "Disconnected");
            success=true;
        } catch (MqttException me) {
            Log.i(DEBUG_TAG, "Publish failed: " + me.getReasonCode());
            Log.i(DEBUG_TAG, "Cause: " + me.getCause());
            success=false;
        }
    }

    public String createSensorTopic(String device, String type, String sensor, int interval)
    {
        return(MQTT_DOMAIN+"/SE/"+device+"/conf/"+type+"/"+sensor+"/"+interval);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            publishMessage();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        mListener.PublishConfigurationCallback(success);
    }
}
