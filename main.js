/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

var urlParams = {};
for (var param of location.search.substring(1).split("&")) {
  var [name, value] = param.split("=").map(v => v.replace(/\+/g, " ")).map(decodeURIComponent);
  urlParams[name] = value;
}

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

  var jars = ["java/classes.jar", "tests/tests.jar"];
  if (urlParams.jars) {
    jars = jars.concat(urlParams.jars.split(":"));
  }

  (function loadNextJar() {
    if (jars.length) {
      var jar = jars.shift();
      load(jar, function (data) {
        jvm.addPath(jar, data);
        loadNextJar();
      });
    } else {
      jvm.run(className);
    }
  })();
}

console.log = function() {
  var s = Array.prototype.join.call(arguments, ",") +"\n";
  document.getElementById("log").textContent += s;
}

var idb_fs = new BrowserFS.FileSystem.IndexedDB(function() {
  BrowserFS.initialize(idb_fs);

  runTest("Launcher");
  //runTest("join");
  //runTest("RunTests");
  //runTest("gnu/testlet/vm/SystemTest");
  //runTest("TestThread");
  //runTest("TestRuntime");
  //runTest("Andreas");
  //runTest("TestDate");
  //runTest("RunAll");
  //runTest("TestArrays");
  //runTest("TestByteArrayOutputStream");
});

