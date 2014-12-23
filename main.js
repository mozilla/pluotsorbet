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

var main = urlParams.main || "com/sun/midp/main/MIDletSuiteLoader";
MIDP.midletClassName = urlParams.midletClassName ? urlParams.midletClassName.replace(/\//g, '.') : "RunTests";

if ("gamepad" in urlParams && !/no|0/.test(urlParams.gamepad)) {
  document.documentElement.classList.add('gamepad');
}

var jars = ["java/classes.jar"];

if (MIDP.midletClassName == "RunTests") {
  jars.push("tests/tests.jar");
}

if (urlParams.jars) {
  jars = jars.concat(urlParams.jars.split(":"));
}

if (urlParams.pushConn && urlParams.pushMidlet) {
  MIDP.ConnectionRegistry.addConnection({
    connection: urlParams.pushConn,
    midlet: urlParams.pushMidlet,
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

if (urlParams.jad) {
  loadingPromises.push(load(urlParams.jad, "text").then(function(data) {
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
      }
    });
  }));
}

function performDownload(dialog, callback) {
  var dialogText = dialog.querySelector('h1.download-dialog-text');
  dialogText.textContent = "Downloading " + MIDlet.name + "…";

  var progressBar = dialog.querySelector('progress.pack-activity');

  var sender = DumbPipe.open("JARDownloader", {}, function(message) {
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

          performDownload(dialog, callback);
        });

        break;
    }
  });
}

if (urlParams.downloadJAD) {
  loadingPromises.push(new Promise(function(resolve, reject) {
    initFS.then(function() {
      fs.exists("/app.jar", function(exists) {
        if (exists) {
          fs.open("/app.jar", function(fd) {
            var data = fs.read(fd);
            fs.close(fd);
            jvm.addPath("app.jar", data.buffer);
            resolve();
          });
        } else {
          var dialog = document.getElementById('download-progress-dialog').cloneNode(true);
          dialog.style.display = 'block';
          dialog.classList.add('visible');
          document.body.appendChild(dialog);

          performDownload(dialog, function(data) {
            dialog.parentElement.removeChild(dialog);

            jvm.addPath("app.jar", data);

            fs.create("/app.jar", new Blob([ data ]), function() {});

            resolve();
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
  jvm.startIsolate0(main, urlParams.args);
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

if (urlParams.profile && !/no|0/.test(urlParams.profile)) {
  Instrument.startProfile();
}
