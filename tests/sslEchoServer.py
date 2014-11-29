#!/usr/bin/env python

import socket, ssl

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
s.bind(('localhost', 54443))
s.listen(5)

while True:
    try:
        newsocket, fromaddr = s.accept()

        connstream = ssl.wrap_socket(newsocket,
                                     server_side=True,
                                     certfile="cert.pem",
                                     keyfile="cert.pem",
                                     ssl_version=ssl.PROTOCOL_SSLv3)

        try:
            data = connstream.read()
            while data:
                connstream.write(data)
                data = connstream.read()
        finally:
            try:
                connstream.shutdown(socket.SHUT_RDWR)
            except socket.error as e:
                # On Mac, if the other side has already closed the connection,
                # then socket.shutdown will fail, but we can ignore this failure.
                pass

            connstream.close()
    except:
        pass
