package com.holger.mashpit.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class SensorPublishMQTT extends AsyncTask<Void, Void, Void>  {
    private MemoryPersistence persistence = new MemoryPersistence();
    private static final String DEBUG_TAG = "SensorPublishMQTT";

    private int MQTT_PORT;
    private String MQTT_PASSWORD;
    private String MQTT_USER;
    private String MQTT_BROKER;
    private String MQTT_DOMAIN;
    private String clientId;

    String topic;
    String send;
    int qos;
    boolean retained;
    boolean success;
    int position=(-1);

    public OnPublishConfiguration mListener;

    public SensorPublishMQTT(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        if(prefs.getBoolean("same_broker",false)) {
            MQTT_BROKER = prefs.getString("send_broker_url", "192.168.1.20");
            MQTT_PORT = Integer.parseInt(prefs.getString("send_broker_port", "1884"));
            MQTT_USER =prefs.getString("send_broker_user", "");
            MQTT_PASSWORD =prefs.getString("send_broker_password", "");
            MQTT_DOMAIN = prefs.getString("send_mashpit_domain","");
        }
        else {
            MQTT_BROKER = prefs.getString("broker_url", "192.168.1.20");
            MQTT_PORT = Integer.parseInt(prefs.getString("broker_port", "1884"));
            MQTT_USER =prefs.getString("broker_user", "");
            MQTT_PASSWORD =prefs.getString("broker_password", "");
            MQTT_DOMAIN= prefs.getString("mashpit_domain","");
        }

        mListener = (OnPublishConfiguration) context;
        clientId=prefs.getString("device_id","");
    }

    public interface OnPublishConfiguration {
        void PublishConfigurationCallback(Boolean success, int position); // you can change the parameter here. depends on what you want.
    }

    private MqttClient ConnectMQTT()
    {
        MqttClient mqttClient = null;
        try {
            mqttClient = new MqttClient("tcp://" + MQTT_BROKER + ":" + MQTT_PORT, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
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

    public boolean PublishServerUpdate(String server, String send)
    {
        this.send=send;
        this.topic="/SE/"+server+"/conf/update";
        this.retained=false;
        this.qos=2;
        execute();
        return true;
    }

    public boolean PublishServerStatus(String server, String send)
    {
        this.send=send;
        this.topic="/SE/"+server+"/conf/server";
        this.qos=2;
        this.retained=false;
        execute();
        return true;
    }

    public void PublishSensorConf(String server, String sensor, String type, int interval, String send) {
        this.send=send;
        this.topic="/SE/"+server+"/conf/"+type+"/"+sensor+"/"+interval;
        this.qos=2;
        this.retained=true;
        execute();
    }

    public void PublishSensorConf(String server, String sensor, String type, int interval, int position) {
        this.send="";
        this.topic="/SE/"+server+"/conf/"+type+"/"+sensor+"/"+interval;
        this.qos=2;
        this.retained=true;
        this.position=position;
        execute();
    }

    public void publishMessage() {
        try {
            MqttClient mqttClient = ConnectMQTT();
            MqttMessage message = new MqttMessage(send.getBytes());
            message.setQos(qos);
            message.setRetained(retained);
            Log.i(DEBUG_TAG, "Configration topic: "+topic);
            mqttClient.publish(MQTT_DOMAIN + topic, message);
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
        mListener.PublishConfigurationCallback(success,position);
    }
}
