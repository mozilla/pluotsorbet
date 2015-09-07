/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

// The real profile and release variable declaration in config.ts are folded away by closure. Until we
// make closure process this file also, make sure that |profile| is defined in this file.
var release;
var profile;

var jvm = new JVM();

if ("gamepad" in config && !/no|0/.test(config.gamepad)) {
  document.documentElement.classList.add('gamepad');
}

var jars = [];

if (typeof Benchmark !== "undefined") {
  Benchmark.startup.init();
}

if (config.jars) {
  jars = jars.concat(config.jars.split(":"));
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

var loadingMIDletPromises = [getMobileInfo];
var loadingPromises = [initFS];

loadingPromises.push(load("java/classes.jar", "arraybuffer").then(function(data) {
  JARStore.addBuiltIn("java/classes.jar", data);
  CLASSES.initializeBuiltinClasses();
}));

jars.forEach(function(jar) {
  loadingMIDletPromises.push(load(jar, "arraybuffer").then(function(data) {
    JARStore.addBuiltIn(jar, data);
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
    }
  });
}

function performDownload(url, callback) {
  showDownloadScreen();
  var progressBar = downloadDialog.querySelector('progress.pack-activity');

  var sender = DumbPipe.open("JARDownloader", url, function(message) {
    switch (message.type) {
      case "done":
        DumbPipe.close(sender);
        hideDownloadScreen();
        progressBar.value = 0;
        callback(message.data);

        break;

      case "progress":
        progressBar.value = message.progress;
        break;

      case "fail":
        DumbPipe.close(sender);
        hideDownloadScreen();
        progressBar.value = 0;

        var failureDialog = document.getElementById('download-failure-dialog');
        failureDialog.style.display = '';

        var btnRetry = failureDialog.querySelector('button.recommend');
        btnRetry.addEventListener('click', function onclick(e) {
          e.preventDefault();
          btnRetry.removeEventListener('click', onclick);
          failureDialog.style.display = 'none';
          performDownload(url, callback);
        });

        break;
    }
  });
}

if (config.downloadJAD) {
  loadingMIDletPromises.push(new Promise(function(resolve, reject) {
    JARStore.loadJAR("midlet.jar").then(function(loaded) {
      if (loaded) {
        // Show the splash screen as soon as possible.
        showSplashScreen();

        processJAD(JARStore.getJAD());
        resolve();
        return;
      }

      performDownload(config.downloadJAD, function(data) {
        // Show the splash screen as soon as possible after showing
        // the download screen while downloading the JAD/JAR files.
        showSplashScreen();

        JARStore.installJAR("midlet.jar", data.jarData, data.jadData).then(function() {
          processJAD(JARStore.getJAD());
          resolve();
        });
      });
    });
  }));
} else if (config.jad) {
  loadingMIDletPromises.push(load(config.jad, "text").then(processJAD));
}

if (config.jad || config.downloadJAD) {
  Promise.all(loadingMIDletPromises).then(backgroundCheck);
}

var loadingFGPromises = [ emoji.loadData() ];

if (jars.indexOf("tests/tests.jar") !== -1) {
  loadingPromises.push(loadScript("tests/native.js"),
                       loadScript("tests/mozactivitymock.unprivileged.js"),
                       loadScript("tests/config.js"));
}

function getIsOff(button) {
  return button.textContent.contains("OFF");
}
function toggle(button) {
  var isOff = getIsOff(button);
  button.textContent = button.textContent.replace(isOff ? "OFF" : "ON", isOff ? "ON" : "OFF");
}

var bigBang = 0;
var profiling = false;

function startTimeline() {
  jsGlobal.START_TIME = performance.now();
  jsGlobal.profiling = true;
  requestTimelineBuffers(function (buffers) {
    for (var i = 0; i < buffers.length; i++) {
      buffers[i].reset(jsGlobal.START_TIME);
    }
    for (var runtime of J2ME.RuntimeTemplate.all) {
      for (var ctx of runtime.allCtxs) {
        ctx.restartMethodTimeline();
      }
    }
  });
}

function stopTimeline(cb) {
  jsGlobal.profiling = false;
  requestTimelineBuffers(function(buffers) {
    // Some of the methods may have not exited yet. Leave them
    // so they show up in the profile.
    for (var i = 0; i < buffers.length; i++) {
      while(buffers[i].depth > 0) {
        buffers[i].leave();
      }
    }
    cb(buffers);
  });
}

function stopAndSaveTimeline() {
  console.log("Saving profile, please wait ...");
  var traceFormat = Shumway.Tools.Profiler.TraceFormat[profileFormat.toUpperCase()];
  var output = [];
  var writer = new J2ME.IndentingWriter(false, function (s) {
    output.push(s);
  });
  if (traceFormat === Shumway.Tools.Profiler.TraceFormat.CSV) {
    writer.writeLn("Name,Count,Self (ms),Total (ms)");
  }
  stopTimeline(function (buffers) {
    var snapshots = [];
    for (var i = 0; i < buffers.length; i++) {
      snapshots.push(buffers[i].createSnapshot());
    }
    // Trace Statistics
    for (var i = 0; i < snapshots.length; i++) {
      writer.writeLn("Timeline Statistics: " + snapshots[i].name);
      snapshots[i].traceStatistics(writer, 1, traceFormat); // Don't trace any totals below 1 ms.
    }
    // Trace Aggregate Method Statistics
    writer.writeLn("Timeline Statistics: All Threads");
    var methodSnapshots = snapshots.slice(2);
    new Shumway.Tools.Profiler.TimelineBufferSnapshotSet(methodSnapshots).traceStatistics(writer, 1, traceFormat);
    // Trace Events
    for (var i = 0; i < snapshots.length; i++) {
      writer.writeLn("Timeline Events: " + snapshots[i].name);
      snapshots[i].trace(writer, 0.1); // Don't trace anything below 0.1 ms.
    }
  });
  var text = output.join("\n");

  var fileExtension, mediaType;
  switch (traceFormat) {
    case Shumway.Tools.Profiler.TraceFormat.CSV:
      fileExtension = "csv";
      mediaType = "text/csv";
      break;
    case Shumway.Tools.Profiler.PLAIN:
    default:
      fileExtension = "txt";
      mediaType = "text/plain";
      break;
  }
  var profileFilename = "profile." + fileExtension;
  var blob = new Blob([text], {type : mediaType});
  saveAs(blob, profileFilename);
  console.log("Saved profile in: adb pull /sdcard/downloads/" + profileFilename);
}

function start() {
  var deferStartup = config.deferStartup | 0;
  if (deferStartup && typeof Benchmark !== "undefined") {
    setTimeout(function () {
      Benchmark.startup.setStartTime(performance.now());
      run();
    }, deferStartup);
  } else {
    run();
  }
  function run() {
    J2ME.Context.setWriters(new J2ME.IndentingWriter());
    // For profile mode 1, we start the profiler and wait 2 seconds and show the flame chart UI.
    profile === 1 && profiler.start(2000, false);
    bigBang = performance.now();
    // For profiler mode 2, we start the timeline and stop it later by calling |stopAndSaveTimeline|.
    profile === 2 && startTimeline();
    jvm.startIsolate0(config.main, config.args);
  }
}

// If we're not running a MIDlet, we need to wait everything to be loaded.
if (!config.midletClassName) {
  loadingPromises = loadingPromises.concat(loadingMIDletPromises);
}

Promise.all(loadingPromises).then(start, function (reason) {
  console.error("Loading failed: \"" + reason + "\"");
});

document.getElementById("start").onclick = function() {
  start();
};

document.getElementById("canvasSize").onchange = function() {
  Array.prototype.forEach.call(document.body.classList, function(c) {
    if (c.indexOf('size-') == 0) {
      document.body.classList.remove(c);
    }
  });

  if (this.value) {
    document.body.classList.add(this.value);
  }

  MIDP.updatePhysicalScreenSize();
  MIDP.updateCanvas();
  start();
};

if (typeof Benchmark !== "undefined") {
  Benchmark.initUI("benchmark");
}

window.onload = function() {
 document.getElementById("deleteDatabases").onclick = function() {
   fs.deleteDatabase().then(function() {
     console.log("Deleted fs database.");
   }).catch(function(error) {
     console.log("Error deleting fs database: " + error);
   });

   CompiledMethodCache.deleteDatabase().then(function() {
     console.log("Deleted CompiledMethodCache database.");
   }).catch(function(error) {
     console.log("Error deleting CompiledMethodCache database: " + error);
   });

   JARStore.deleteDatabase().then(function() {
     console.log("Deleted JARStore database.");
   }).catch(function(error) {
     console.log("Error deleting JARStore database: " + error);
   });
 };
 document.getElementById("exportstorage").onclick = function() {
   fs.exportStore(function(blob) {
     saveAs(blob, "fs-" + Date.now() + ".json");
   });
 };
 document.getElementById("importstorage").onclick = function() {
   function performImport(file) {
     fs.importStore(file, function() {
       DumbPipe.close(DumbPipe.open("alert", "Import completed."));
     });
   }

   var file = document.getElementById("importstoragefile").files[0];
   if (file) {
     performImport(file);
   } else {
     load(document.getElementById("importstorageurl").value, "blob").then(function(blob) {
       performImport(blob);
     });
   }
 };
 document.getElementById("clearCompiledMethodCache").onclick = function() {
   CompiledMethodCache.clear().then(function() { console.log("cleared compiled method cache") });
 };
 document.getElementById("printAllExceptions").onclick = function() {
   VM.DEBUG_PRINT_ALL_EXCEPTIONS = !VM.DEBUG_PRINT_ALL_EXCEPTIONS;
   toggle(this);
 };
 document.getElementById("clearCounters").onclick = function() {
   clearCounters();
 };

  function numberWithCommas(x) {
    return x.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
  }

  setInterval(function () {
    var el = document.getElementById("bytecodeCount");
    el.textContent = numberWithCommas(J2ME.bytecodeCount);

    var el = document.getElementById("interpreterCount");
    el.textContent = numberWithCommas(J2ME.interpreterCount);

    var el = document.getElementById("compiledCount");
    el.textContent = numberWithCommas(J2ME.compiledMethodCount) + "/" +
                     numberWithCommas(J2ME.cachedMethodCount) + "/" +
                     numberWithCommas(J2ME.aotMethodCount) + "/" +
                     numberWithCommas(J2ME.notCompiledMethodCount);

    var el = document.getElementById("onStackReplacementCount");
    el.textContent = numberWithCommas(J2ME.onStackReplacementCount);

    var el = document.getElementById("unwindCount");
    el.textContent = numberWithCommas(J2ME.unwindCount);

    var el = document.getElementById("preemptionCount");
    el.textContent = numberWithCommas(J2ME.preemptionCount);

  }, 500);

  function dumpCounters() {
    var writer = new J2ME.IndentingWriter();

    writer.writeLn("Frame Count: " + J2ME.frameCount);
    writer.writeLn("Unwind Count: " + J2ME.unwindCount);
    writer.writeLn("Bytecode Count: " + J2ME.bytecodeCount);
    writer.writeLn("OSR Count: " + J2ME.onStackReplacementCount);

    if (J2ME.interpreterCounter) {
      writer.enter("interpreterCounter");
      J2ME.interpreterCounter.traceSorted(writer);
      writer.outdent();
    }
    if (J2ME.interpreterMethodCounter) {
      writer.enter("interpreterMethodCounter");
      J2ME.interpreterMethodCounter.traceSorted(writer);
      writer.outdent();
    }
    if (J2ME.baselineMethodCounter) {
      writer.enter("baselineMethodCounter");
      J2ME.baselineMethodCounter.traceSorted(writer);
      writer.outdent();
    }
    if (J2ME.baselineCounter) {
      writer.enter("baselineCounter");
      J2ME.baselineCounter.traceSorted(writer);
      writer.outdent();
    }
    if (J2ME.nativeCounter) {
      writer.enter("nativeCounter");
      J2ME.nativeCounter.traceSorted(writer);
      writer.outdent();
    }
    if (J2ME.runtimeCounter) {
      writer.enter("runtimeCounter");
      J2ME.runtimeCounter.traceSorted(writer);
      writer.outdent();
    }
    if (J2ME.asyncCounter) {
      writer.enter("asyncCounter");
      J2ME.asyncCounter.traceSorted(writer);
      writer.outdent();
    }
  }
  function clearCounters() {
    J2ME.frameCount = 0;
    J2ME.unwindCount = 0;
    J2ME.bytecodeCount = 0;
    J2ME.interpreterCount = 0;
    J2ME.onStackReplacementCount = 0;

    J2ME.interpreterCounter && J2ME.interpreterCounter.clear();
    J2ME.interpreterMethodCounter && J2ME.interpreterMethodCounter.clear();
    J2ME.nativeCounter && J2ME.nativeCounter.clear();
    J2ME.runtimeCounter && J2ME.runtimeCounter.clear();
    J2ME.asyncCounter && J2ME.asyncCounter.clear();
    J2ME.baselineMethodCounter && J2ME.baselineMethodCounter.clear();
    J2ME.baselineCounter && J2ME.baselineCounter.clear();
  }

  document.getElementById("dumpCounters").onclick = function() {
    dumpCounters();
  };
  document.getElementById("sampleCounters1").onclick = function() {
    clearCounters();
    dumpCounters();
    setTimeout(function () {
      dumpCounters();
    }, 1000);
  };
  document.getElementById("sampleCounters2").onclick = function() {
    clearCounters();
    function sample() {
      var c = 1;
      function tick() {
        if (c-- > 0) {
          dumpCounters();
          clearCounters();
          setTimeout(tick, 16);
        }
      }

      setTimeout(tick, 100);
    }
    setTimeout(sample, 2000); // Wait 2s before starting.
  };
};

function requestTimelineBuffers(fn) {
  if (J2ME.timeline) {
    // If you change the position at which method timelines begin in this array,
    // then also update the method timeline aggregation in stopAndSaveTimeline.
    var activeTimeLines = [
      J2ME.threadTimeline,
      J2ME.timeline,
    ];
    var methodTimeLines = J2ME.methodTimelines;
    for (var i = 0; i < methodTimeLines.length; i++) {
      activeTimeLines.push(methodTimeLines[i]);
    }
    fn(activeTimeLines);
    return;
  }
  return fn([]);
}

var perfWriterCheckbox = document.querySelector('#perfWriter');

perfWriterCheckbox.checked = !!(J2ME.writers & J2ME.WriterFlags.Perf);
perfWriterCheckbox.addEventListener('change', function() {
  if (perfWriterCheckbox.checked) {
    J2ME.writers |= J2ME.WriterFlags.Perf;
  } else {
    J2ME.writers &= !J2ME.WriterFlags.Perf;
  }
});

var profiler = profile === 1 ? (function() {

  var elPageContainer = document.getElementById("pageContainer");
  elPageContainer.classList.add("profile-mode");

  var elProfilerContainer = document.getElementById("profilerContainer");
  var elProfilerToolbar = document.getElementById("profilerToolbar");
  var elProfilerMessage = document.getElementById("profilerMessage");
  var elProfilerPanel = document.getElementById("profilePanel");
  var elBtnStartStop = document.getElementById("profilerStartStop");
  var elBtnAdjustHeight = document.getElementById("profilerAdjustHeight");

  var controller;
  var startTime;
  var timerHandle;
  var timeoutHandle;

  var Profiler = function() {
    controller = new Shumway.Tools.Profiler.Controller(elProfilerPanel);
    elBtnStartStop.addEventListener("click", this._onStartStopClick.bind(this));
    elBtnAdjustHeight.addEventListener("click", this._onAdjustHeightClick.bind(this));

    var self = this;
    window.addEventListener("keypress", function (event) {
      if (event.altKey && event.keyCode === 114) { // Alt + R
        self._onStartStopClick();
      }
    }, false);
  }

  Profiler.prototype.start = function(maxTime, resetTimelines) {
    startTimeline();
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
    stopTimeline(function (buffers) {
      controller.createProfile(buffers);
      elProfilerToolbar.classList.remove("withEmphasis");
      elBtnStartStop.textContent = "Start";
      clearInterval(timerHandle);
      clearTimeout(timeoutHandle);
      timerHandle = 0;
      timeoutHandle = 0;
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

  Profiler.prototype._onAdjustHeightClick = function(e) {
    elProfilerContainer.classList.toggle("max");
  };

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
