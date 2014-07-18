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

function runTest(className, cb) {
  var jvm = new JVM();
  // This is a hack. We should eliminate CLASSES instead.
  CLASSES.classes = {};
  load("java/cldc1.1.1.jar", function (data) {
    jvm.addPath("java/cldc1.1.1.jar", data);
    var fileName = className + ".class";
    load(fileName, function (data) {
      jvm.addPath(fileName, data);
      jvm.run(className);
      cb && cb();
    });
  });
}

//runTest("tests/TestDup");
//runTest("tests/TestOps");
//runTest("tests/TestLong");
//runTest("tests/TestPrintln");
runTest("tests/TestException");
