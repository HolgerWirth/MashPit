from socket import *
import time
import os

s=socket(AF_INET, SOCK_DGRAM)
s.setsockopt(SOL_SOCKET, SO_BROADCAST, 1)
while 1:
   myIP = os.popen('hostname -I').read()
   myIP = "%s:%s" % (myIP.rstrip('\n\r '),1883)
   s.sendto(myIP,('255.255.255.255',11111))
   time.sleep(5)
