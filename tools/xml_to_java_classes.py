#!/usr/bin/env python

import sys
from xml.dom import minidom

def convert(element):
  output = ""

  constant = 0

  entries = element.getElementsByTagName("localized_string");
  for entry in entries:
    output = output + "    public final static int " + entry.getAttribute("Key") + " = " + str(constant) + ";\n"
    constant = constant + 1

  return output

def main(argv):
  if not len(sys.argv) == 2:
    print("xml_to_java_classes.py <xml>")
    return

  dom = minidom.parse(sys.argv[1])
  f = open("java/custom/com/sun/midp/i18n/ResourceConstants.java", 'w')
  output = "package com.sun.midp.i18n;\n\npublic class ResourceConstants {\n" + convert(dom) + "}\n"
  f.write(output)
  f.close()

  f = open("java/custom/com/sun/midp/l10n/LocalizedStringsBase.java", 'w')
  output = "package com.sun.midp.l10n;\n\nabstract class LocalizedStringsBase {\n    native static String getContent(int index);\n}\n"
  f.write(output)
  f.close()

if __name__ == "__main__":
  main(sys.argv)
