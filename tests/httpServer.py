#!/usr/bin/env python

import sys
import BaseHTTPServer, SimpleHTTPServer

class CustomRequestHandler(SimpleHTTPServer.SimpleHTTPRequestHandler):
    def end_headers(self):
        self.send_header("Cache-Control", "no-cache, no-store, must-revalidate")
        self.send_header("Pragma", "no-cache")
	self.send_header("Expires", "0")
        SimpleHTTPServer.SimpleHTTPRequestHandler.end_headers(self)

port = 8000
if len(sys.argv) > 1:
  port = int(sys.argv[1])

httpd = BaseHTTPServer.HTTPServer(('localhost', port), CustomRequestHandler)
httpd.serve_forever()
