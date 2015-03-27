#!/usr/bin/env python

import sys
from xml.dom import minidom
import json

def convert(element):
  output = dict()

  constant = 0

  entries = element.getElementsByTagName("localized_string");
  for entry in entries:
    output[constant] = entry.getAttribute("Value")
    constant = constant + 1

  return output

def main(argv):
  if not len(sys.argv) == 3:
    print("xml_to_json.py <xml> <json>")
    return

  dom = minidom.parse(sys.argv[1])
  f = open(sys.argv[2], 'w')
  f.write(json.dumps(convert(dom), sort_keys=True))
  f.close()

if __name__ == "__main__":
  main(sys.argv)
