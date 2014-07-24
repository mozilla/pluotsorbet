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

function runTest(className) {
  var jvm = new JVM();
  // This is a hack. We should eliminate CLASSES instead.
  CLASSES.classes = {};
  load("java/classes.jar", function (data) {
    jvm.addPath("java/classes.jar", data);
    load("tests/tests.jar", function (data) {
      jvm.addPath("tests/tests.jar", data);
      jvm.run(className);
    });
  });
}


//runTest("TestThread");
runTest("RunAll");
//runTest("Andreas");
//runTest("TestArrays");
//runTest("TestDate");
//runTest("TestByteArrayOutputStream");