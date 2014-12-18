/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

function load(file, responseType) {
  return new Promise(function(resolve, reject) {
    var xhr = new XMLHttpRequest();
    xhr.open("GET", file, true);
    xhr.responseType = responseType;
    xhr.onload = function () {
      resolve(xhr.response);
    };
    xhr.onerror = function() {
      reject();
    };
    xhr.send(null);
  });
}

function loadScript(path) {
  return new Promise(function(resolve, reject) {
    var element = document.createElement('script');
    element.setAttribute("type", "text/javascript");
    element.setAttribute("src", path);
    document.getElementsByTagName("head")[0].appendChild(element);
    element.onload = resolve;
  });
}

// To launch the unit tests: ?main=RunTests
// To launch the MIDP demo: ?main=com/sun/midp/main/MIDletSuiteLoader&midletClassName=HelloCommandMIDlet
// To launch a JAR file: ?main=com/sun/midp/main/MIDletSuiteLoader&args=app.jar

var jvm = new JVM();

var main = urlParams.main || "com/sun/midp/main/MIDletSuiteLoader";
MIDP.midletClassName = urlParams.midletClassName ? urlParams.midletClassName.replace(/\//g, '.') : "RunTests";

if ("gamepad" in urlParams && !/no|0/.test(urlParams.gamepad)) {
  document.documentElement.classList.add('gamepad');
}

var jars = ["java/classes.jar", "bench/scimark2.jar"];

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

var initFS = new Promise(function(resolve, reject) {
  fs.init(resolve);
}).then(function() {
  var fsPromises = [
    new Promise(function(resolve, reject) {
      fs.mkdir("/Persistent", resolve);
    }),

    new Promise(function(resolve, reject) {
      fs.exists("/_main.ks", function(exists) {
        if (exists) {
          resolve();
        } else {
          load("certs/_main.ks", "blob").then(function(data) {
            fs.create("/_main.ks", data, function() {
              resolve();
            });
          });
        }
      });
    }),
  ];

  if (MIDP.midletClassName == "RunTests") {
    fsPromises.push(
      new Promise(function(resolve, reject) {
        fs.exists("/_test.ks", function(exists) {
          if (exists) {
            resolve();
          } else {
            load("certs/_test.ks", "blob").then(function(data) {
              fs.create("/_test.ks", data, function() {
                resolve();
              });
            });
          }
        });
      })
    );
  }

  return Promise.all(fsPromises);
});

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
    CLASSES.addPath(jar, data);
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
            fs.close();
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
  CLASSES.initializeBuiltinClasses();
  jvm.startIsolate0(main, []);
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
 document.getElementById("trace").onclick = function() {
   VM.DEBUG = !VM.DEBUG;
   toggle(this);
 };
 document.getElementById("printAllExceptions").onclick = function() {
   VM.DEBUG_PRINT_ALL_EXCEPTIONS = !VM.DEBUG_PRINT_ALL_EXCEPTIONS;
   toggle(this);
 };
 document.getElementById("clearCounters").onclick = function() {
   J2ME.interpreterCounter.clear();
 };
 document.getElementById("dumpCounters").onclick = function() {
   J2ME.interpreterCounter.traceSorted(new J2ME.IndentingWriter());
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
