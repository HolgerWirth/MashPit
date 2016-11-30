import paho.mqtt.client as mqtt
import time
import json
from rrdtool import update as rrd_update


def on_connect(mqttc, obj, rc):
    return

def on_message(mqttc, obj, msg):
    global counter
    global air
    global liquid

    decjson = json.JSONDecoder()
    mytemp=decjson.decode(msg.payload)
    temperature=mytemp['Temp']
    sensor=mytemp['Name']
    if(sensor=='1. Sensor'):
       counter=counter+1
       print "Liquid"
       liquid=temperature
       print temperature
    if(sensor=='2. Sensor'):
       counter=counter+1
       air=temperature
       print "Air"
       print temperature
    if(counter==2):
       rrd_update('freezertemp.rrd','N:%s:%s' %(str(air), str(liquid)));
       print "Update!"
       counter=0
       
def on_subscribe(mqttc, obj, mid, granted_qos):
    return

def mqtt_connect(): 
    mqttc.on_message = on_message
    mqttc.on_connect = on_connect
    mqttc.on_subscribe = on_subscribe

def mqtt_publish(topic,message):
    mqttc.publish(topic,message.encode("iso-8859-1"),0,0)

myjson = json.JSONEncoder()
counter=0
air='0.0'
liquid='0.0'

while True:
    mqttc = mqtt.Client()
    mqttc.connect("localhost", 1883, 10)
    mqtt_connect()
    mqttc.subscribe("/temp/+/600", 0)

    mqttc.loop_forever()
