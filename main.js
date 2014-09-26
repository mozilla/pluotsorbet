/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

function load(file, responseType, cb) {
  var xhr = new XMLHttpRequest();
  xhr.open("GET", file, true);
  xhr.responseType = responseType;
  xhr.onload = function () {
    cb(xhr.response);
  }
  xhr.send(null);
}

// To launch the unit tests: ?main=RunTests
// To launch the MIDP demo: ?main=com/sun/midp/main/MIDletSuiteLoader&midletClassName=HelloCommandMIDlet
// To launch a JAR file: ?main=com/sun/midp/main/MIDletSuiteLoader&args=app.jar

var jvm = new JVM();

var main = urlParams.main || "com/sun/midp/main/MIDletSuiteLoader";
MIDP.midletClassName = urlParams.midletClassName ? urlParams.midletClassName.replace(/\//g, '.') : "RunTests";

var jars = ["java/classes.jar", "tests/tests.jar"];
if (urlParams.jars) {
  jars = jars.concat(urlParams.jars.split(":"));
}

if (urlParams.pushConn && urlParams.pushMidlet) {
  MIDP.pushRegistrations.push({
    connection: urlParams.pushConn,
    midlet: urlParams.pushMidlet,
    filter: "*",
    suiteId: "1",
    id: ++MIDP.lastRegistrationId,
  });
}

new Promise(function(resolve, reject) {
  fs.init(resolve);
}).then(function() {
  return Promise.all([
    new Promise(function(resolve, reject) {
      fs.mkdir("/Persistent", resolve);
    }),
    new Promise(function(resolve, reject) {
      fs.exists("/_main.ks", function(exists) {
        if (exists) {
          resolve();
        } else {
          load("certs/_main.ks", "blob", function(data) {
            fs.create("/_main.ks", data, function() {
              resolve();
            });
          });
        }
      });
    })
  ]);
}).then(function() {
  var jarPromises = [];
  jars.forEach(function(jar) {
      jarPromises.push(new Promise(function(resolve, reject) {
        load(jar, "arraybuffer", function(data) {
          console.log('loaded jar: ' + jar);
          jvm.addPath(jar, data);
          resolve();
        });
      }));
  });

  if (MIDP.midletClassName == "RunTests") {
    loadScript("tests/native.js");
    loadScript("tests/contacts.js");
    loadScript("tests/override.js");
  }

  if (urlParams.jad) {
    load(urlParams.jad, "text", function(data) {
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
    });
  }

  return Promise.all(jarPromises);
}).then(function() {
  jvm.initializeBuiltinClasses();
  jvm.startIsolate0(main, urlParams.args);
});

function loadScript(path, loadCallback) {
  var element = document.createElement('script');
  element.setAttribute("type", "text/javascript");
  element.setAttribute("src", path);
  document.getElementsByTagName("head")[0].appendChild(element);
  if (loadCallback) {
    element.onload = loadCallback;
  }
}
function getIsOff(button) {
  return button.textContent.contains("OFF");
}
function toggle(button) {
  var isOff = getIsOff(button);
  button.textContent = button.textContent.replace(isOff ? "OFF" : "ON", isOff ? "ON" : "OFF");
}

window.onload = function() {
 document.getElementById("clearstorage").onclick = function() {
   asyncStorage.clear();
 };
 document.getElementById("trace").onclick = function() {
   VM.DEBUG = !VM.DEBUG;
   toggle(this);
 };
 document.getElementById("printAllExceptions").onclick = function() {
   VM.DEBUG_PRINT_ALL_EXCEPTIONS = !VM.DEBUG_PRINT_ALL_EXCEPTIONS;
   toggle(this);
 };
 document.getElementById("profile").onclick = function() {
   if (getIsOff(this)) {
     Instrument.startProfile();
   } else {
     Instrument.stopProfile();
   }
   toggle(this);
 };
 if (Instrument.profiling) {
   toggle(document.getElementById("profile"));
 }
};

if (urlParams.profile && !/no|0/.test(urlParams.profile)) {
  Instrument.startProfile();
}
