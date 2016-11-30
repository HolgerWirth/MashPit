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
    logger.debug("Freezer connected to MQTT")
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
    logger.debug("Freezer: %s ",str(min(templist)))
    count = count+1

    temperature=min(templist)
    maxtemp=float(readxml("freezer.xml","config","maxtemp"))
#    if temperature > 1.5:
#    if temperature > 8.4:
#    if temperature > 0.8:
#    if temperature > 0.4:
#    if temperature > 2.0:
#    if temperature > 12.5:
#    if temperature > 12.8:
#    if temperature > 2.5:
#    if temperature > 21.7:
#    if temperature > 5.2:
#    if temperature > 6.2:
    if temperature > maxtemp:
       logger.debug("Freezer temperature reached: %s ",str(min(templist)))
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
logger = logging.getLogger("freezer.py")
logger.setLevel(logging.DEBUG)
ch = logging.FileHandler("freezer.log")
ch.setLevel(logging.DEBUG)
# create formatter
formatter = logging.Formatter("%(asctime)s - %(name)s - %(levelname)-5s - %(message)s")
# add formatter to ch
ch.setFormatter(formatter)
# add ch to logger
logger.addHandler(ch)

myjson = json.JSONEncoder()

GPIO.setmode(GPIO.BCM)
GPIO.setwarnings(False)
GPIO.setup(11, GPIO.OUT)
GPIO.output(11, GPIO.LOW) 

while True:
   templist=[0.0,0.0,0.0]
   count=0
   freeze=int(readxml("freezer.xml","config","freeze"))
   mqttc = mqtt.Client()
   mqttc.connect("localhost", 1883, 10)
   mqtt_connect()
   topic=readxml("freezer.xml","config","topic")
   mqttc.subscribe(topic, 0)
#   mqttc.subscribe("/temp/W1/600", 0)

   mqttc.loop_forever()
   time.sleep(1)
   logger.debug("Freezer starts freezing for %s minutes",str(freeze))
   GPIO.output(11, GPIO.HIGH) 
   os.system("send 11111 2 1")

   t=0
   while t < freeze:
      ts=int(time.time())
      logger.debug("Freezer will cool %s minutes",str(freeze-t))
      time.sleep(60) 
      t=t+1
   time.sleep(1)
   logger.debug("Freezer stopped freezing")
   GPIO.output(11, GPIO.LOW) 
   os.system("send 11111 2 0")
   time.sleep(600)
  
