#!/usr/bin/env python

import socket

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.bind(('', 50003))
s.listen(5)

while True:
  client, address = s.accept()

  while True:
    data = client.recv(1024)

    if data:
      client.send(data)
    else:
      break;

  client.close()
