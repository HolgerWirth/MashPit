import RPi.GPIO as GPIO
import time

GPIO.setmode(GPIO.BCM)
GPIO.setwarnings(False)
GPIO.setup(11, GPIO.OUT)

GPIO.output(11, GPIO.HIGH) 

time.sleep(900) 
GPIO.output(11, GPIO.LOW) 

