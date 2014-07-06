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

load("Main.class", function (data) {
  jvm.addPath("Main.class", data);
  jvm.loadClassFile("Main.class");
  jvm.run();
});
