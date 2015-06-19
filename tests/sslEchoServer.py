#!/usr/bin/env python

import socket, ssl

# This is a copy of _RESTRICTED_SERVER_CIPHERS from the current tip of ssl.py
# <https://hg.python.org/cpython/file/af793c7580f1/Lib/ssl.py#l174> except that
# RC4 has been added back in, since it was removed in Python 2.7.10,
# but SSLStreamConnection only supports RC4 ciphers.
CIPHERS = (
  'ECDH+AESGCM:DH+AESGCM:ECDH+AES256:DH+AES256:ECDH+AES128:DH+AES:ECDH+HIGH:'
  'DH+HIGH:ECDH+3DES:DH+3DES:RSA+AESGCM:RSA+AES:RSA+HIGH:RSA+3DES:!aNULL:'
  '!eNULL:!MD5:!DSS:RC4'
)

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
                                     ciphers=CIPHERS)
    except ssl.SSLError as e:
        # Catch occurrences of:
        #   ssl.SSLEOFError: EOF occurred in violation of protocol (_ssl.c:581)
        #
        # In theory, setting ssl_version to ssl.PROTOCOL_TLSv1 will resolve
        # the problem, but it didn't do so for me, and it caused the error:
        #   ssl.SSLError: [SSL: WRONG_VERSION_NUMBER] wrong version number (_ssl.c:581)
        #
        # Whereas the SSLEOFError doesn't prevent the server from working
        # (it seems to happen only when the server is first started, and it
        # stops happening if we simply ignore it and try again a few times)
        # so we leave ssl_version at ssl.PROTOCOL_SSLv3 and ignore that error.
        #
        # If we catch SSLEOFError specifically, then Travis fails with:
        #   AttributeError: 'module' object has no attribute 'SSLEOFError'
        # So we catch the more general exception SSLError.
        continue

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
