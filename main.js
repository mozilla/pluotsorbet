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
    CLASSES.addPath(jar, data);
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
                CLASSES.addPath("midlet.jar", fs.read(fd).buffer.slice(0));
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

            CLASSES.addPath("midlet.jar", data.jarData);
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

function getIsOff(button) {
  return button.textContent.contains("OFF");
}
function toggle(button) {
  var isOff = getIsOff(button);
  button.textContent = button.textContent.replace(isOff ? "OFF" : "ON", isOff ? "ON" : "OFF");
}

var bigBang = 0;

function start() {
  CLASSES.initializeBuiltinClasses();
  profiler && profiler.start(2000, false);
  bigBang = performance.now();
  jvm.startIsolate0(main, config.args);
}

Promise.all(loadingPromises).then(function() {
  setTimeout(function () {
    start();
  }, 1000);
});

document.getElementById("start").onclick = function() {
  start();
};

function loadAllClasses() {
  profiler.start(5000, false);
  for (var i = 0; i < 1; i++) {
    var s = performance.now();
    CLASSES.loadAllClassFiles();
    console.info("Loaded all classes in: " + (performance.now() - s));
  }
}

document.getElementById("loadAllClasses").onclick = function() {
  loadAllClasses();
};

window.onload = function() {
 document.getElementById("clearstorage").onclick = function() {
   fs.clear();
 };
 document.getElementById("deleteDatabase").onclick = function() {
   indexedDB.deleteDatabase("asyncStorage");
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
 document.getElementById("clearCounters").onclick = function() {
   J2ME.interpreterCounter.clear();
 };
 document.getElementById("dumpCounters").onclick = function() {
   if (J2ME.interpreterCounter) {
     J2ME.interpreterCounter.traceSorted(new J2ME.IndentingWriter());
   }
   if (J2ME.nativeCounter) {
     J2ME.nativeCounter.traceSorted(new J2ME.IndentingWriter());
   }
   if (J2ME.runtimeCounter) {
     J2ME.runtimeCounter.traceSorted(new J2ME.IndentingWriter());
   }
 };
  document.getElementById("dumpCountersTime").onclick = function() {
    J2ME.interpreterCounter && J2ME.interpreterCounter.clear();
    J2ME.nativeCounter && J2ME.nativeCounter.clear();
    setTimeout(function () {
      if (J2ME.interpreterCounter) {
        J2ME.interpreterCounter.traceSorted(new J2ME.IndentingWriter());
      }
      if (J2ME.nativeCounter) {
        J2ME.nativeCounter.traceSorted(new J2ME.IndentingWriter());
      }
    }, 1000);
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

function requestTimelineBuffers(fn) {
  if (J2ME.timeline) {
    fn([
      J2ME.timeline,
      J2ME.methodTimeline
    ]);
    return;
  }
  return fn([]);
}

var profiler = typeof Shumway !== "undefined" ? (function() {

  var elProfilerContainer = document.getElementById("profilerContainer");
  var elProfilerToolbar = document.getElementById("profilerToolbar");
  var elProfilerMessage = document.getElementById("profilerMessage");
  var elProfilerPanel = document.getElementById("profilePanel");
  var elBtnMinimize = document.getElementById("profilerMinimizeButton");
  var elBtnStartStop = document.getElementById("profilerStartStop");

  var controller;
  var startTime;
  var timerHandle;
  var timeoutHandle;

  var Profiler = function() {
    controller = new Shumway.Tools.Profiler.Controller(elProfilerPanel);
    elBtnStartStop.addEventListener("click", this._onStartStopClick.bind(this));

    var self = this;
    window.addEventListener("keypress", function (event) {
      if (event.altKey && event.keyCode === 114) { // Alt + R
        self._onStartStopClick();
      }
    }, false);
  }

  Profiler.prototype.start = function(maxTime, resetTimelines) {
    window.profile = true;
    requestTimelineBuffers(function (buffers) {
      for (var i = 0; i < buffers.length; i++) {
        buffers[i].reset();
      }
    });
    controller.deactivateProfile();
    maxTime = maxTime || 0;
    elProfilerToolbar.classList.add("withEmphasis");
    elBtnStartStop.textContent = "Stop";
    startTime = Date.now();
    timerHandle = setInterval(showTimeMessage, 1000);
    if (maxTime) {
      timeoutHandle = setTimeout(this.createProfile.bind(this), maxTime);
    }
    showTimeMessage();
  }

  Profiler.prototype.createProfile = function() {
    requestTimelineBuffers(function (buffers) {
      controller.createProfile(buffers);
      elProfilerToolbar.classList.remove("withEmphasis");
      elBtnStartStop.textContent = "Start";
      clearInterval(timerHandle);
      clearTimeout(timeoutHandle);
      timerHandle = 0;
      timeoutHandle = 0;
      window.profile = false;
      showTimeMessage(false);
    });
  }

  Profiler.prototype.openPanel = function() {
    elProfilerContainer.classList.remove("collapsed");
  }

  Profiler.prototype.closePanel = function() {
    elProfilerContainer.classList.add("collapsed");
  }

  Profiler.prototype.resize = function() {
    controller.resize();
  }

  Profiler.prototype._onMinimizeClick = function(e) {
    if (elProfilerContainer.classList.contains("collapsed")) {
      this.openPanel();
    } else {
      this.closePanel();
    }
  }

  Profiler.prototype._onStartStopClick = function(e) {
    if (timerHandle) {
      this.createProfile();
      this.openPanel();
    } else {
      this.start(0, true);
    }
  }

  function showTimeMessage(show) {
    show = typeof show === "undefined" ? true : show;
    var time = Math.round((Date.now() - startTime) / 1000);
    elProfilerMessage.textContent = show ? "Running: " + time + " Seconds" : "";
  }

  return new Profiler();

})() : undefined;
