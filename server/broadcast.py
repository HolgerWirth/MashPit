from socket import *
import time

s=socket(AF_INET, SOCK_DGRAM)
s.setsockopt(SOL_SOCKET, SO_BROADCAST, 1)
while 1:
   s.sendto('1883',('255.255.255.255',11111))
   time.sleep(5)
