#!/usr/bin/env python

import socket, ssl

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
s.bind(('localhost', 54443))
s.listen(5)

while True:
    newsocket, fromaddr = s.accept()

    try:
        connstream = ssl.wrap_socket(newsocket,
                                     server_side=True,
                                     certfile="cert.pem",
                                     keyfile="cert.pem",
                                     ssl_version=ssl.PROTOCOL_SSLv3)
    except ssl.SSLError as e:
        # Catch occurrences of:
        #   ssl.SSLEOFError: EOF occurred in violation of protocol (_ssl.c:581)
        #
        # In theory, setting ssl_version to ssl.PROTOCOL_TLSv1 will resolve
        # the problem, but it didn't do so for me, and it caused the error:
        #   ssl.SSLError: [SSL: WRONG_VERSION_NUMBER] wrong version number (_ssl.c:581)
        #
        # Whereas the SSLEOFError doesn't prevent the server from working,
        # so we leave ssl_version at ssl.PROTOCOL_SSLv3 and ignore that error.
        #
        # If we catch SSLEOFError specifically, then Travis fails with:
        #   AttributeError: 'module' object has no attribute 'SSLEOFError'
        # So we catch the more general exception SSLError.
        pass

    if (connstream):
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
