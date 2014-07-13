/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

function load(file, cb) {
  var xhr = new XMLHttpRequest();
  xhr.open("GET", file, true);
  xhr.responseType = "arraybuffer";
  xhr.onload = function () {
    cb(xhr.response);
  }
  xhr.send(null);
}

var jvm = new JVM();
jvm.setLogLevel(7);

load("java/cldc1.1.1.jar", function (data) {
  jvm.addPath("java/cldc1.1.1.jar", data);
  load("tests/TestPrintln.class", function (data) {
    jvm.addPath("TestPrintln.class", data);
    jvm.loadClassFile("TestPrintln.class");
    jvm.start();
  });
});
