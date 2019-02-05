import xml.etree.ElementTree as ET
import paho.mqtt.client as mqtt
import time
import json
import logging

def on_connect(mqttc, obj, rc):
    global online
    logger.debug("Process connected to MQTT")
    online=True

def on_disconnect(mqttc, obj, rc):
    logger.debug("Process discconnected from MQTT")

def on_message(mqttc, obj, msg):
    global reached

    decjson = json.JSONDecoder()

    mytemp=decjson.decode(msg.payload)
    temperature=float(mytemp['Temp'])
    if temperature>=(float(temperatur)-0.2):
       logger.debug("(%s) Temperatur erreicht (%s)",mytemp['Temp'],str(reached))
       reached = reached+1
       if reached > 4:
          mqttc.disconnect()
          online=False
    else:
       reached=0

def on_subscribe(mqttc, obj, mid, granted_qos):
    logger.debug("Successfully subscribed")

def mqtt_connect(): 
   mqttc.on_message = on_message
   mqttc.on_connect = on_connect
   mqttc.on_subscribe = on_subscribe
   mqttc.on_disconnect = on_disconnect

def mqtt_publish(topic,message):
   mqttc.publish(topic,message.encode("iso-8859-1"),0,1)

def rast_on_connect(mqttc2, obj1, rc1):
    global rast_online
    logger.debug("Rast: Process connected to MQTT")
    rast_online=True

def rast_on_message(mqttc2, obj1, msg):
    global reached
    global status

    decjson = json.JSONDecoder()

    mytemp=decjson.decode(msg.payload)
    temperature=float(mytemp['Temp'])
    if temperature<(float(temperatur)-0.4):
       reached = reached+1
       if reached > 4:
          logger.debug("Rast: (%s) Nachheizen!",mytemp['Temp'])
          status="an"
          reached=0
    else:
       status="aus"
       reached=0

def rast_on_subscribe(mqttc2, obj1, mid, granted_qos):
    logger.debug("Rast: Successfully subscribed")

def rast_mqtt_connect(): 
   mqttc2.on_message = rast_on_message
   mqttc2.on_connect = rast_on_connect
   mqttc2.on_subscribe = rast_on_subscribe

def rast_mqtt_publish(topic,message):
   mqttc2.publish(topic,message.encode("iso-8859-1"),0,1)


tree = ET.parse('rezept.xml')
root = tree.getroot()

if root.tag != 'rezept':
   print "Wrong  XML format!"
   print "Exiting..."
   exit

# create logger
logger = logging.getLogger("process.py")
logger.setLevel(logging.DEBUG)
ch = logging.FileHandler("process.log")
ch.setLevel(logging.DEBUG)
# create formatter
formatter = logging.Formatter("%(asctime)s - %(name)s - %(levelname)-5s - %(message)s")
# add formatter to ch
ch.setFormatter(formatter)
# add ch to logger
logger.addHandler(ch)

myjson = json.JSONEncoder()
topic="/process"
reached=0

title=root.find('name').text
print title

allrast=[]
for rast in root.findall('rast'):
   name = rast.find('name').text
   temp=rast.find('starttemp').text
   length=rast.find('dauer').text
   allrast.append({'name': name,'temp': temp,'length': length})

for rast in root.findall('rast'):
   ignore=False
   online=False
   mqttc = mqtt.Client()
   mqttc.connect("localhost", 1883, 10)
   mqtt_connect()
   mqttc.subscribe("/temp/#", 0)
   status="an"
   minute=0
   name=rast.find('name').text
   temperatur=rast.find('starttemp').text
   dauer=rast.find('dauer').text
   ts=int(time.time())
   message={'title': title,'TS': ts,'Rast': name,'Dauer': dauer,'Minute': minute,'Heizen': status,'Ziel': temperatur,'Proc': allrast}
   mqtt_publish(topic,myjson.encode(message))
   reached = 0
   logger.debug("Warte auf %s bei Temperatur %s",name,temperatur)

   mqttc.loop_forever()
   logger.debug("%s erreicht bei Temperatur %s erreicht!",name,temperatur)
   mqttc.disconnect()

   rast_online=False
   mqttc2 = mqtt.Client()
   mqttc2.reinitialise()
   rast_mqtt_connect()
   mqttc2.connect("localhost", 1883, 10)
   mqttc2.subscribe("/temp/#", 0)
   reached = 0
   status="aus"
 
   mqttc2.loop_start()
   t=0
   while t < int(dauer):
      ts=int(time.time())
      logger.debug("Warte noch %s Minuten",str(int(dauer)-t))
      if status == "an":
         logger.debug("Nacheizen: an")
      tmessage={'title': title,'TS': ts,'Rast': name,'Dauer': dauer,'Minute': str(t),'Heizen': status,'Ziel': temperatur,'Proc': allrast}
      message=myjson.encode(tmessage)
      mqttc2.publish(topic,message.encode("iso-8859-1"),0,1)
      time.sleep(60) 
      t=t+1
   mqttc2.disconnect()
   reached = 0
   status="aus"
   time.sleep(1)
status="ende"
mqttc = mqtt.Client()
mqttc.connect("localhost", 1883, 10)
mqtt_connect()
tmessage={'title': title,'TS': ts,'Rast': name,'Dauer': dauer,'Minute': str(t),'Heizen': status,'Ziel': temperatur,'Proc': allrast}
message=myjson.encode(tmessage)
mqttc.publish(topic,message.encode("iso-8859-1"),0,1)
mqttc.disconnect()
logger.debug("Prozessende!")
