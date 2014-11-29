#!/usr/bin/env python

import socket, time

def wait_server(port):
    end = time.time() + 30 # Timeout of 30 seconds

    while True:
        try:
            s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            s.connect(('', port))
            s.close()
            return
        except:
            if end < time.time():
                raise Exception("Can't connect to " + str(port))
            else:
                time.sleep(1)

wait_server(8000)
wait_server(50003)
wait_server(4443)
wait_server(54443)
