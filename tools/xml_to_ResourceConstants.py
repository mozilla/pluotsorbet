#!/usr/bin/env python

import sys
from xml.dom import minidom

def convert(element):
  output = ""

  constant = 0

  entries = element.getElementsByTagName("localized_string");
  for entry in entries:
    output = output + "public final static int " + entry.getAttribute("Key") + " = " + str(constant) + ";"
    constant = constant + 1

  return output

def main(argv):
  if not len(sys.argv) == 3:
    print("xml_to_ResourceConstants.py <xml> <java source file>")
    return

  dom = minidom.parse(sys.argv[1])
  f = open(sys.argv[2], 'w')
  output = "package com.sun.midp.i18n;\npublic class ResourceConstants {\n" + convert(dom) + "}"
  f.write(output)
  f.close()

if __name__ == "__main__":
  main(sys.argv)
