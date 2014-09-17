#!/usr/bin/env python

import fcntl
import os
import sys

def make_blocking(fd):
  flags = fcntl.fcntl(fd, fcntl.F_GETFL)
  if flags & os.O_NONBLOCK:
    fcntl.fcntl(fd, fcntl.F_SETFL, flags & ~os.O_NONBLOCK)

def main(argv):
  make_blocking(sys.stdin.fileno())
  make_blocking(sys.stdout.fileno())
  print open("test.log").read()

if __name__ == "__main__":
  main(sys.argv)
