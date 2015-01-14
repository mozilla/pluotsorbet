/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

// To launch the unit tests: ?main=RunTests
// To launch the MIDP demo: ?main=com/sun/midp/main/MIDletSuiteLoader&midletClassName=HelloCommandMIDlet
// To launch a JAR file: ?main=com/sun/midp/main/MIDletSuiteLoader&args=app.jar

// The base directory of the app, relative to the current page.  Normally this
// is the directory from which the page was loaded, but some test pages load
// from a subdirectory, like tests/fs/, and they set this accordingly such that
// code loads files, like libs/fs-init.js, can load them from the right place.
var APP_BASE_DIR = "./";

var jvm = new JVM();

var main = config.main || "com/sun/midp/main/MIDletSuiteLoader";
MIDP.midletClassName = config.midletClassName ? config.midletClassName.replace(/\//g, '.') : "RunTests";

if ("gamepad" in config && !/no|0/.test(config.gamepad)) {
  document.documentElement.classList.add('gamepad');
}

var jars = ["java/classes.jar"];

if (MIDP.midletClassName == "RunTests") {
  jars.push("tests/tests.jar");
}

if (config.jars) {
  jars = jars.concat(config.jars.split(":"));
}

if (config.pushConn && config.pushMidlet) {
  MIDP.ConnectionRegistry.addConnection({
    connection: config.pushConn,
    midlet: config.pushMidlet,
    filter: "*",
    suiteId: "1"
  });
}

// Mobile info gets accessed a lot, so we cache it on startup.
var mobileInfo;
var getMobileInfo = new Promise(function(resolve, reject) {
  var sender = DumbPipe.open("mobileInfo", {}, function(message) {
    mobileInfo = message;
    DumbPipe.close(sender);
    resolve();
  });
});

var loadingPromises = [initFS, getMobileInfo];
jars.forEach(function(jar) {
  loadingPromises.push(load(jar, "arraybuffer").then(function(data) {
    jvm.addPath(jar, data);
  }));
});

function processJAD(data) {
  data
  .replace(/\r\n|\r/g, "\n")
  .replace(/\n /g, "")
  .split("\n")
  .forEach(function(entry) {
    if (entry) {
      var keyEnd = entry.indexOf(":");
      var key = entry.substring(0, keyEnd);
      var val = entry.substring(keyEnd + 1).trim();
      MIDP.manifest[key] = val;

      if (key == "MIDlet-Name") {
        var title = document.getElementById("splash-screen").querySelector(".title");
        title.textContent = "Loading " + val;
      }
    }
  });
}

if (config.jad) {
  loadingPromises.push(load(config.jad, "text").then(processJAD));
}

function performDownload(url, dialog, callback) {
  var dialogText = dialog.querySelector('h1.download-dialog-text');
  dialogText.textContent = "Downloading " + MIDlet.name + "…";

  var progressBar = dialog.querySelector('progress.pack-activity');

  var sender = DumbPipe.open("JARDownloader", url, function(message) {
    switch (message.type) {
      case "done":
        DumbPipe.close(sender);

        callback(message.data);

        break;

      case "progress":
        progressBar.value = message.progress;
        break;

      case "fail":
        DumbPipe.close(sender);

        progressBar.value = 0;
        progressBar.style.display = "none";

        var dialogText = dialog.querySelector('h1.download-dialog-text');
        dialogText.textContent = "Download failure";

        var btnRetry = dialog.querySelector('button.recommend');
        btnRetry.style.display = '';

        btnRetry.addEventListener('click', function onclick(e) {
          e.preventDefault();
          btnRetry.removeEventListener('click', onclick);

          btnRetry.style.display = "none";

          progressBar.style.display = '';

          performDownload(url, dialog, callback);
        });

        break;
    }
  });
}

if (config.downloadJAD) {
  loadingPromises.push(initFS.then(function() {
    return new Promise(function(resolve, reject) {
      fs.exists("/midlet.jar", function(exists) {
        if (exists) {
          Promise.all([
            new Promise(function(resolve, reject) {
              fs.open("/midlet.jar", function(fd) {
                jvm.addPath("midlet.jar", fs.read(fd).buffer.slice(0));
                fs.close(fd);
                resolve();
              });
            }),
            new Promise(function(resolve, reject) {
              fs.open("/midlet.jad", function(fd) {
                processJAD(util.decodeUtf8(fs.read(fd)));
                fs.close(fd);
                resolve();
              });
            }),
          ]).then(resolve);
        } else {
          var dialog = document.getElementById('download-progress-dialog').cloneNode(true);
          dialog.style.display = 'block';
          dialog.classList.add('visible');
          document.body.appendChild(dialog);

          performDownload(config.downloadJAD, dialog, function(data) {
            dialog.parentElement.removeChild(dialog);

            jvm.addPath("midlet.jar", data.jarData);
            processJAD(data.jadData);

            Promise.all([
              new Promise(function(resolve, reject) {
                fs.create("/midlet.jad", new Blob([ data.jadData ]), resolve);
              }),
              new Promise(function(resolve, reject) {
                fs.create("/midlet.jar", new Blob([ data.jarData ]), resolve);
              }),
            ]).then(resolve);
          });
        }
      });
    });
  }));
}

if (MIDP.midletClassName == "RunTests") {
  loadingPromises.push(loadScript("tests/native.js"),
                       loadScript("tests/override.js"),
                       loadScript("tests/mozactivitymock.js"));
}

Promise.all(loadingPromises).then(function() {
  jvm.initializeBuiltinClasses();
  jvm.startIsolate0(main, config.args);
});

function getIsOff(button) {
  return button.textContent.contains("OFF");
}
function toggle(button) {
  var isOff = getIsOff(button);
  button.textContent = button.textContent.replace(isOff ? "OFF" : "ON", isOff ? "ON" : "OFF");
}

window.onload = function() {
 document.getElementById("clearstorage").onclick = function() {
   fs.clear();
 };
 document.getElementById("exportstorage").onclick = function() {
   fs.exportStore(function(blob) {
     saveAs(blob, "fs-" + Date.now() + ".json");
   });
 };
 document.getElementById("importstorage").addEventListener("change", function(event) {
   fs.importStore(event.target.files[0], function() {
     DumbPipe.close(DumbPipe.open("alert", "Import completed."));
   });
 }, false);
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

if (config.profile && !/no|0/.test(config.profile)) {
  Instrument.startProfile();
}
