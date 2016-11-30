import paho.mqtt.client as mqtt
import time
import json
import os
import logging
import RPi.GPIO as GPIO

class mqtthandler(object):
    def __init__(self):
        self.mymqtt = mqtt.Client("")

    def mqtt_connect(self):
        self.mymqtt.connect("localhost", 1883, 60)
        self.mymqtt.subscribe("/process", 0)
        return self.mymqtt

def on_message(mqttc, obj, msg):
    decjson = json.JSONDecoder()
    status=decjson.decode(msg.payload)
    if status['Heizen'] == "an":
       logger.debug("Hendi an!")
       GPIO.output(11, GPIO.HIGH)
    if status['Heizen'] == "aus":
       logger.debug("Hendi aus!")
       GPIO.output(11, GPIO.LOW)

myjson = json.JSONEncoder()

GPIO.setmode(GPIO.BCM)
GPIO.setwarnings(False)
GPIO.setup(11, GPIO.OUT)

# create logger
logger = logging.getLogger("hendi.py")
logger.setLevel(logging.DEBUG)
ch = logging.FileHandler("hendi.log")
ch.setLevel(logging.DEBUG)
# create formatter
formatter = logging.Formatter("%(asctime)s - %(name)s - %(levelname)-5s - %(message)s")
# add formatter to ch
ch.setFormatter(formatter)
# add ch to logger
logger.addHandler(ch)

mymqtt=mqtthandler()
mqttc=mymqtt.mqtt_connect()
mqttc.on_message = on_message

rc = 0
while rc == 0:
    rc = mqttc.loop_forever()

