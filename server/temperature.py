import xml.etree.ElementTree as ET
from threading import Timer,Thread,Event
import paho.mqtt.client as mqtt
import time
import glob
import re
import json

class mqtthandler(object):
    def __init__(self):
        self.mymqtt = mqtt.Client("")

    def mqtt_connect(self):
        self.mymqtt.connect("localhost", 1883, 60)
        self.mymqtt.subscribe("/command/temp/#", 0)
        return self.mymqtt

    def mqtt_publish(self,topic,message,persist):
        self.mymqtt.publish(topic,message.encode("iso-8859-1"),2,0)

class temperatureTimer():
   def __init__(self,t,hFunction,sensor,fname,name,topic,mode,persist):
      self.t=t
      self.hFunction = hFunction
      self.sensor=sensor
      self.fname=fname
      self.name=name
      self.topic=topic
      self.mode=mode
      self.persist=persist
      self.thread = Timer(self.t,self.handle_function)

   def handle_function(self):
      self.hFunction(self.sensor,self.fname,self.name,self.topic,self.t,self.mode,self.persist)
      self.thread = Timer(self.t,self.handle_function)
      self.thread.start()

   def start(self):
      self.thread.start()

   def cancel(self):
      self.thread.cancel()

def getTemp(fname):
   tfile = open(fname)
   line1=tfile.readline()
   line2=tfile.readline()
   tfile.close()
   if reline1.match(line1):
      temp=reline2.match(line2)
      if temp:
         temperature=str(float(temp.group(1)) / 1000.0)
         return(temperature)
   return "100"

def push1(sensor,fname,name,mtopic,interval,mode,persist):
    global last_temp
    temperature=getTemp(fname)
    correct_flag=False

    if float(temperature) - last_temp > 50:
       print "Lat temp: "+str(last_temp)+" wrong temp: "+temperature
       correct_flag=True

    if float(temperature) > 76.0:
       correct_flag=True

    if correct_flag:
       templist=[0.0,0.0,0.0]
       count=0
       while count<3:
          time.sleep(1)
          templist[count]=float(getTemp(fname))
          count=count+1
       temperature=str(min(templist))
       print "Corrected: "+temperature

    ts=int(time.time())
    topic="/temp/"+mtopic+"/"+str(interval)
    message={'TS': ts,'Name': name,'Temp': temperature,'Mode': mode}
    mymqtt.mqtt_publish(topic,myjson.encode(message),persist)
    last_temp=float(temperature)

def on_message(mqttc, obj, msg):
    t=0
    while t<2:
       for i in thread:
          i.cancel()
          time.sleep(1)
       t=t+1
    mqttc.disconnect()

myjson = json.JSONEncoder()
mymqtt=mqtthandler()
mqttc=mymqtt.mqtt_connect()
mqttc.on_message = on_message

last_temp=0.0

redev=re.compile('/sys/bus/w1/devices/(.*)')
reline1=re.compile('.*crc=.*YES')
reline2=re.compile('.*t=(.*)')
t=glob.glob('/sys/bus/w1/devices/28-*')

tree = ET.parse('sensors.xml')
root = tree.getroot()

if root.tag != 'sensors':
   print "Wrong  XML format!"
   exit

thread=[]
for i in t:
   fname=i+"/w1_slave"
   x1=redev.match(i)
   for sensor in root.findall('sensor'):
      if sensor.find('id').text == x1.group(1):
         for durat in sensor.findall('timer'):
            thread.append(temperatureTimer(int(durat.find('interval').text),push1,x1.group(1),fname,sensor.find('name').text,sensor.find('topic').text,sensor.find('mode').text,int(durat.find('persist').text)))
         break

for i in thread:
   i.start()

rc = 0
while rc == 0:
    rc = mqttc.loop_forever()

