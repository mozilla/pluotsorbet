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

var nodes = new Set();

function executeNextBatch(completedNode) {
  nodes.delete(completedNode);

  nodes.forEach(function(node) {
    if (node.parents.size === 0) {
      node.execute();
    } else if (node.parents.has(completedNode)) {
      node.parentDone(completedNode);
    }
  });
}

var InitNode = function(func, parents) {
  this.func = func;
  this.parents = parents;
  this.parentsDone = 0;

  nodes.add(this);
}

InitNode.prototype.execute = function() {
  this.func(executeNextBatch.bind(null, this));
}

InitNode.prototype.parentDone = function(parent) {
  if (++this.parentsDone === this.parents.size) {
    this.execute();
  }
}

var initFS = new InitNode(function(callback) {
  fs.init(callback);
}, new Set());

var mkdirPersistent = new InitNode(function(callback) {
  fs.mkdir("/Persistent", callback);
}, new Set([ initFS ]));

var createKeystore = new InitNode(function(callback) {
  fs.exists("/_main.ks", function(exists) {
    if (exists) {
      callback();
    } else {
      load("certs/_main.ks", "blob", function(data) {
        fs.create("/_main.ks", data, function() {
          callback();
        });
      });
    }
  });
}, new Set([ initFS ]));

var startJVM = new InitNode(function(callback) {
  jvm.initializeBuiltinClasses();
  jvm.startIsolate0(main, urlParams.args);
}, new Set([ mkdirPersistent, createKeystore ]));

jars.forEach(function(jar) {
  startJVM.parents.add(new InitNode(function(callback) {
    load(jar, "arraybuffer", function(data) {
      jvm.addPath(jar, data);
      callback();
    });
  }, new Set()));
});

if (MIDP.midletClassName == "RunTests") {
  startJVM.parents.add(new InitNode(function(callback) {
    var element = document.createElement('script');
    element.setAttribute("type", "text/javascript");
    element.setAttribute("src", "tests/native.js");
    document.getElementsByTagName("head")[0].appendChild(element);

    var testContactsScript = document.createElement('script');
    testContactsScript.setAttribute("type", "text/javascript");
    testContactsScript.setAttribute("src", "tests/contacts.js");
    document.getElementsByTagName("head")[0].appendChild(testContactsScript);

    testContactsScript.onload = callback;
  }, new Set()));
}

if (urlParams.jad) {
  startJVM.parents.add(new InitNode(function(callback) {
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

      callback();
    });
  }, new Set()));
}

executeNextBatch();

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
