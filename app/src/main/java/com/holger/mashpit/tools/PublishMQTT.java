package com.holger.mashpit.tools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class PublishMQTT {
    private final MemoryPersistence persistence = new MemoryPersistence();
    private static final String DEBUG_TAG = "PublishMQTT";

    public void PublishStatus(Context context, String server, String topic, String send) {
        Publish(context, server, topic, send, "status");
    }

    public boolean PublishConf(Context context, String server, String topic, String send)
    {
        return Publish(context,server,topic,send,"conf");
    }

    private boolean Publish(Context context, String server, String topic, String send, String status) {
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

            String MQTT_PROTOCOL="tcp://";
            int MQTT_PORT;
            String MQTT_PASSWORD;
            String MQTT_USER;
            String MQTT_BROKER;
            String MQTT_DOMAIN;
            boolean MQTT_SSL;
            String DEVICE_ID_FORMAT = "TEX_%s";
            final MySSlSocketFactory factory = new MySSlSocketFactory(context);


            if(prefs.getBoolean("same_broker",false)) {
                MQTT_BROKER = prefs.getString("send_broker_url", "192.168.1.20");
                MQTT_PORT = Integer.parseInt(prefs.getString("send_broker_port", "1883"));
                MQTT_USER =prefs.getString("send_broker_user", "");
                MQTT_PASSWORD =prefs.getString("send_broker_password", "");
                MQTT_DOMAIN = prefs.getString("send_mashpit_domain","");
                MQTT_SSL = prefs.getBoolean("send_broker_ssl",false);
            }
            else {
                MQTT_BROKER = prefs.getString("broker_url", "192.168.1.20");
                MQTT_PORT = Integer.parseInt(prefs.getString("broker_port", "1883"));
                MQTT_USER =prefs.getString("broker_user", "");
                MQTT_PASSWORD =prefs.getString("broker_password", "");
                MQTT_DOMAIN= prefs.getString("mashpit_domain","");
                MQTT_SSL = prefs.getBoolean("broker_ssl",false);
            }

            if(MQTT_SSL)
            {
                MQTT_PROTOCOL="ssl://";
            }
            @SuppressLint("HardwareIds") String clientId = String.format(DEVICE_ID_FORMAT,
                    Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));

            MqttClient sampleClient = new MqttClient(MQTT_PROTOCOL+ MQTT_BROKER +":"+ MQTT_PORT, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            if(MQTT_SSL) {
                connOpts.setSocketFactory(factory.getSslSocketFactory(null));
            }
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
            sampleClient.publish(MQTT_DOMAIN+"/MP/"+server+"/"+status+"/"+topic, message);
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
