import socket
import datetime
import time

logsocket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
logsocket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
logsocket.bind(("237.7.0.1", 8999))
while 1:
    print(str(time.time()) + ":" + str(logsocket.recv(1024)))

