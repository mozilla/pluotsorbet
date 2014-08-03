/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

console.log = function() {
  var s = Array.prototype.join.call(arguments, ",") +"\n";
  document.getElementById("log").textContent += s;
}

var urlParams = {};
location.search.substring(1).split("&").forEach(function (param) {
  param = param.split("=").map(function(v) {
      return v.replace(/\+/g, " ");
  }).map(decodeURIComponent);
  urlParams[param[0]] = param[1];
});

urlParams.args = (urlParams.args || "").split(",");

function load(file, cb) {
  var xhr = new XMLHttpRequest();
  xhr.open("GET", file, true);
  xhr.responseType = "arraybuffer";
  xhr.onload = function () {
    cb(xhr.response);
  }
  xhr.send(null);
}

function run(className, args) {
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
      jvm.run(className, args);
    }
  })();
}

// To launch the unit tests: ?main=RunTests
// To launch the MIDP demo: ?main=com/sun/midp/main/MIDletSuiteLoader&args=HelloCommandMIDlet

run(urlParams.main || "RunTests", urlParams.args);
