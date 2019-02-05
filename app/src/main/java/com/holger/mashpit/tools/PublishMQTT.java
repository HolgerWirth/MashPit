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

public class PublishMQTT {
    private MemoryPersistence persistence = new MemoryPersistence();


    private static final String DEBUG_TAG = "PublishMQTT";

    public boolean PublishConf(Context context, String topic, String XML) {
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

            String MQTT_BROKER=prefs.getString("send_broker_url","192.168.1.20");
            @SuppressWarnings("ConstantConditions") int MQTT_PORT= Integer.parseInt(prefs.getString("send_broker_port","1884"));
            String clientId=prefs.getString("device_id","");

            assert clientId != null;
            MqttClient sampleClient = new MqttClient("tcp://"+MQTT_BROKER+":"+MQTT_PORT, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            Log.i(DEBUG_TAG,"Connecting to broker: " + MQTT_BROKER);
            sampleClient.connect(connOpts);
            Log.i(DEBUG_TAG,"Connected");
            MqttMessage message = new MqttMessage(XML.getBytes());
            int qos = 2;
            message.setQos(qos);
            message.setRetained(true);
            sampleClient.publish("/conf/"+topic, message);
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
