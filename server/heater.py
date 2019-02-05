import xml.etree.ElementTree as ET
import paho.mqtt.client as mqtt
import time
import json
import logging
import os
import RPi.GPIO as GPIO

def readxml(xmlfile,name1,name2):
   tree = ET.parse(xmlfile)
   root = tree.getroot()

   for data in root.findall(name1):
      value = data.find(name2).text

   return value

def on_connect(mqttc, obj, rc):
    global online
    logger.debug("Heater connected to MQTT")
    online=True

def on_message(mqttc, obj, msg):
    global templist
    global count

    if count > 2:
       count=0

    decjson = json.JSONDecoder()
    mytemp=decjson.decode(msg.payload)
    temperature=float(mytemp['Temp'])
    templist[count]=temperature
    logger.debug("Heater: %s ",str(max(templist)))
    count = count+1

    temperature=max(templist)
    mintemp=float(readxml("heater.xml","config","mintemp"))

    if temperature < mintemp:
       logger.debug("Heater temperature reached: %s ",str(max(templist)))
       mqttc.disconnect()

def on_subscribe(mqttc, obj, mid, granted_qos):
    logger.debug("Successfully subscribed")

def mqtt_connect(): 
   mqttc.on_message = on_message
   mqttc.on_connect = on_connect
   mqttc.on_subscribe = on_subscribe

def mqtt_publish(topic,message):
   mqttc.publish(topic,message.encode("iso-8859-1"),0,0)

# create logger
logger = logging.getLogger("heater.py")
logger.setLevel(logging.DEBUG)
ch = logging.FileHandler("heater.log")
ch.setLevel(logging.DEBUG)
# create formatter
formatter = logging.Formatter("%(asctime)s - %(name)s - %(levelname)-5s - %(message)s")
# add formatter to ch
ch.setFormatter(formatter)
# add ch to logger
logger.addHandler(ch)

myjson = json.JSONEncoder()

#GPIO.setmode(GPIO.BCM)
#GPIO.setwarnings(False)
#GPIO.setup(11, GPIO.OUT)
#GPIO.output(11, GPIO.LOW) 

while True:
   templist=[99.0,99.0,99.0]
   count=0
   freeze=10
   mqttc = mqtt.Client()
   mqttc.connect("localhost", 1883, 10)
   mqtt_connect()
   topic=readxml("heater.xml","config","topic")
   mqttc.subscribe(topic, 0)

   mqttc.loop_forever()
   time.sleep(1)
   freeze=int(readxml("heater.xml","config","heat"))
   logger.debug("Heater starts heating for %s minutes",str(freeze))
 #  GPIO.output(11, GPIO.HIGH) 
   os.system("send 11111 2 1")

   t=0
   while t < freeze:
      ts=int(time.time())
      logger.debug("Heater will heat for %s minutes",str(freeze-t))
      time.sleep(60) 
      t=t+1
   time.sleep(1)
   logger.debug("Heater stopped heating")
#   GPIO.output(11, GPIO.LOW) 
   os.system("send 11111 2 0")
   time.sleep(600)
  
