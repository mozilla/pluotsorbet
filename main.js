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

function parseManifest(data) {
  data
  .replace(/\r\n|\r/g, "\n")
  .replace(/\n /g, "")
  .split("\n")
  .forEach(function(entry) {
    if (entry) {
      var keyval = entry.split(':');
      MIDP.manifest[keyval[0]] = keyval[1].trim();
    }
  });
}

function load(file, responseType, cb) {
  var xhr = new XMLHttpRequest();
  xhr.open("GET", file, true);
  xhr.responseType = responseType;
  xhr.onload = function () {
    cb(xhr.response);
  }
  xhr.send(null);
}

function run(className, args) {
  var jvm = new JVM();

  var jars = ["java/classes.jar", "tests/tests.jar"];
  if (urlParams.jars)
    jars = jars.concat(urlParams.jars.split(":"));

  (function loadNextJar() {
    if (jars.length) {
      var jar = jars.shift();
      load(jar, "arraybuffer", function (data) {
        jvm.addPath(jar, data);
        loadNextJar();
      });
    } else {
      jvm.initializeBuiltinClasses();
      if (urlParams.jad) {
        load(urlParams.jad, "text", function(data) {
          parseManifest(data);
          jvm.startIsolate0(className, args);
        });
      } else {
        jvm.startIsolate0(className, args);
      }
    }
  })();
}

// To launch the unit tests: ?main=RunTests
// To launch the MIDP demo: ?main=com/sun/midp/main/MIDletSuiteLoader&midletClassName=HelloCommandMIDlet
// To launch a JAR file: ?main=com/sun/midp/main/MIDletSuiteLoader&args=app.jar

fs.init(function() {
  run(urlParams.main || "RunTests", urlParams.args);
});

window.onload = function() {
 document.getElementById("clearstorage").onclick = function() {
   asyncStorage.clear();
 };
};
