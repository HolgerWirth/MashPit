package com.holger.mashpit.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class SensorPublishMQTT {
    private MemoryPersistence persistence = new MemoryPersistence();
    private static final String DEBUG_TAG = "SensorPublishMQTT";

    public boolean PublishSensorConf(Context context, String server, String sensor, String type,int interval, String send)
    {
        return Publish(context,server,sensor,send,"conf", type, interval);
    }

    private boolean Publish(Context context, String server, String sensor, String send, String status,String type, int interval) {
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

            int MQTT_PORT;
            String MQTT_PASSWORD;
            String MQTT_USER;
            String MQTT_BROKER;
            String MQTT_DOMAIN;

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

            String clientId=prefs.getString("device_id","");

            assert clientId != null;
            MqttClient sampleClient = new MqttClient("tcp://"+ MQTT_BROKER +":"+ MQTT_PORT, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            assert MQTT_USER != null;
            if(!MQTT_USER.isEmpty()) {
                connOpts.setUserName(MQTT_USER);
                assert MQTT_PASSWORD != null;
                connOpts.setPassword(MQTT_PASSWORD.toCharArray());
            }
            Log.i(DEBUG_TAG,"Connecting to broker: " + MQTT_BROKER +" as user: "+ MQTT_USER);
            sampleClient.connect(connOpts);
            Log.i(DEBUG_TAG,"Connected");
            MqttMessage message = new MqttMessage(send.getBytes());
            int qos = 2;
            message.setQos(qos);
            message.setRetained(true);
            sampleClient.publish(MQTT_DOMAIN+"/SE/"+server+"/"+status+"/"+type+"/"+sensor+"/"+interval, message);
            Log.i(DEBUG_TAG,"Message published");
            sampleClient.disconnect();
            Log.i(DEBUG_TAG,"Disconnected");
            return true;
        } catch (MqttException me) {
            Log.i(DEBUG_TAG,"Publish failed: "+me.getReasonCode());
            Log.i(DEBUG_TAG,"Cause: "+me.getCause());
            return false;
        }
    }
}