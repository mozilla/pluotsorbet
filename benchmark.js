var Benchmark = (function() {

  function mean(array) {
    function add(a, b) {
      return a + b;
    }
    return array.reduce(add, 0) / array.length;
  }

  function logBenchmark(inString) {
    console.log('bench: ' + inString);
  }

  var defaultStorage = {
    // 30 is usually considered a large enough sample size for the central limit theorem
    // to take effect, unless the distribution is too weird
    numRounds: 30,
    roundDelay: 5000, // ms to delay starting next round of tests
    warmBench: false,
    startFGDelay: 10000, // ms to delay starting FG MIDlet
    baseline: {},
    current: {},
    running: false,
    round: 0,
    deleteFs: false,
    deleteJitCache: false,
    buildBaseline: false,
    recordMemory: true
  };

  var NO_SECURITY = typeof netscape !== "undefined" && netscape.security.PrivilegeManager;

  function enableSuperPowers() {
    // To enable chrome privileges use a separate profile and set the pref
    // security.turn_off_all_security_so_that_viruses_can_take_over_this_computer
    // to boolean true.  To do this on a device, see:
    // https://wiki.mozilla.org/B2G/QA/Tips_And_Tricks#For_changing_the_preference:
    netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
  }

  function forceCollectors() {
    if (!NO_SECURITY) {
      return Promise.resolve();
    }
    return new Promise(function(resolve, reject) {
      enableSuperPowers();
      logBenchmark("Starting minimize memory.");
      var gMgr = Components.classes["@mozilla.org/memory-reporter-manager;1"].getService(Components.interfaces.nsIMemoryReporterManager);
      Components.utils.import("resource://gre/modules/Services.jsm");
      Services.obs.notifyObservers(null, "child-mmu-request", null);
      gMgr.minimizeMemoryUsage(function() {
        logBenchmark("Finished minimize memory.");
        resolve();
      });
    });
  }

  var STORAGE_KEY = "benchmark";
  var storage;
  function initStorage(defaults) {
    if (!(STORAGE_KEY in localStorage)) {
      storage = defaults;
    } else {
      storage = JSON.parse(localStorage[STORAGE_KEY]);
      for (var key in defaults) {
        if (key in storage) {
          continue;
        }
        storage[key] = defaults[key];
      }
    }
  }

  function saveStorage() {
    localStorage[STORAGE_KEY] = JSON.stringify(storage);
  }
  
  initStorage(defaultStorage);
  var LEFT = 0; var CENTER = 1; var RIGHT = 2;
  function prettyTable(rows, alignment) {
    function pad(str, repeat, n, align) {
      if (align === LEFT) {
        return str.padRight(repeat, n);
      } else if (align === CENTER) {
        var middle = ((n - str.length) / 2) | 0;
        return str.padRight(repeat, middle + str.length).padLeft(repeat, n);
      } else if (align === RIGHT) {
        return str.padLeft(repeat, n);
      }
      throw new Error("Bad align value." + align);
    }
    var maxColumnLengths = [];
    var numColumns = rows[0].length;
    for (var colIndex = 0; colIndex < numColumns; colIndex++) {
      var maxLength = 0;
      for (var rowIndex = 0; rowIndex < rows.length; rowIndex++) {
        var strLen = rows[rowIndex][colIndex].toString().length;
        if (rows[rowIndex].untrusted) {
          strLen += 2;
        }
        maxLength = Math.max(strLen, maxLength);
      }
      maxColumnLengths[colIndex] = maxLength;
    }
    var out = "";
    for (var rowIndex = 0; rowIndex < rows.length; rowIndex++) {
      out += "| ";
      for (var colIndex = 0; colIndex < numColumns; colIndex++) {
        var str = rows[rowIndex][colIndex].toString();
        if (rows[rowIndex].untrusted) {
          str = "*" + str + "*";
        }
        out += pad(str, " ", maxColumnLengths[colIndex], rowIndex === 0 ? CENTER : alignment[colIndex]) + " | ";
      }
      out += "\n";
      if (rowIndex === 0) {
        out += "|";
        for (var colIndex = 0; colIndex < numColumns; colIndex++) {
          var align = alignment[colIndex];
          if (align === 0) {
            out += ":".padRight("-", maxColumnLengths[colIndex] + 2);
          } else if (align === 1) {
            out += ":".padLeft("-", maxColumnLengths[colIndex] + 1) + ":";
          } else if (align === 2) {
            out += ":".padLeft("-", maxColumnLengths[colIndex] + 2);
          }
          out += "|";
        }
        out += "\n";
      }
    }
    return out;
  }

  function numberWithCommas(x) {
    return x.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
  }

  function msFormatter(x) {
    return numberWithCommas(Math.round(x)) + "ms";
  }

  function byteFormatter(x) {
    return numberWithCommas(Math.round(x / 1024)) + "kb";
  }

  var valueFormatters = {
    startupTime: msFormatter,
    vmStartupTime: msFormatter,
    bgStartupTime: msFormatter,
    fgStartupTime: msFormatter,
    fgRefreshStartupTime: msFormatter,
    fgRestartTime: msFormatter,
    totalSize: byteFormatter,
    domSize: byteFormatter,
    styleSize: byteFormatter,
    jsObjectsSize: byteFormatter,
    jsStringsSize: byteFormatter,
    jsOtherSize: byteFormatter,
    otherSize: byteFormatter,
    USS: byteFormatter,
    peakRSS: byteFormatter,
  };

  var untrustedValues = [
    "totalSize", "domSize", "styleSize", "jsObjectsSize",
    "jsStringsSize", "jsOtherSize", "otherSize", "USS",
    "peakRSS",
  ];

  function sampleMemory() {
    if (!NO_SECURITY) {
      return Promise.resolve({});
    }
    return forceCollectors().then(function() {
      var memoryReporter = Components.classes["@mozilla.org/memory-reporter-manager;1"].getService(Components.interfaces.nsIMemoryReporterManager);

      var jsObjectsSize = {};
      var jsStringsSize = {};
      var jsOtherSize = {};
      var domSize = {};
      var styleSize = {};
      var otherSize = {};
      var totalSize = {};
      var jsMilliseconds = {};
      var nonJSMilliseconds = {};

      try {
        memoryReporter.sizeOfTab(window.parent.window, jsObjectsSize, jsStringsSize, jsOtherSize,
          domSize, styleSize, otherSize, totalSize, jsMilliseconds, nonJSMilliseconds);
      } catch (e) {
        logBenchmark(e);
      }

      var memValues = {
        totalSize: totalSize.value,
        domSize: domSize.value,
        styleSize: styleSize.value,
        jsObjectsSize: jsObjectsSize.value,
        jsStringsSize: jsStringsSize.value,
        jsOtherSize: jsOtherSize.value,
        otherSize: otherSize.value,
      };

      // residentUnique is not available on all platforms.
      try {
        memValues.USS = memoryReporter.residentUnique;
      } catch (e) {
      }

      // residentPeak is not available on all platforms.
      try {
        memValues.peakRSS = memoryReporter.residentPeak;
      } catch (e) {
      }

      return memValues;
    });
  }

  var startup = {
    run: function(settings) {
      storage.round = 0;
      var current = storage.current = {};
      current.startupTime = [];
      current.vmStartupTime = [];
      current.bgStartupTime = [];
      current.fgStartupTime = [];
      current.fgRefreshStartupTime = [];
      current.fgRestartTime = [];
      if (settings.recordMemory) {
        storage.recordMemory = true;
        current.totalSize     = [];
        current.domSize       = [];
        current.styleSize     = [];
        current.jsObjectsSize = [];
        current.jsStringsSize = [];
        current.jsOtherSize   = [];
        current.otherSize     = [];
        current.USS           = [];
        current.peakRSS       = [];
      }
      storage.running = true;
      storage.numRounds = "numRounds" in settings ? settings.numRounds : defaultStorage.numRounds;
      storage.roundDelay = "roundDelay" in settings ? settings.roundDelay : defaultStorage.roundDelay;
      storage.warmBench = "warmBench" in settings ? settings.warmBench : defaultStorage.warmBench;
      storage.startFGDelay = "startFGDelay" in settings ? settings.startFGDelay : defaultStorage.startFGDelay;
      storage.deleteFs = "deleteFs" in settings ? settings.deleteFs : defaultStorage.deleteFs;
      storage.deleteJitCache = "deleteJitCache" in settings ? settings.deleteJitCache : defaultStorage.deleteJitCache;
      storage.buildBaseline = "buildBaseline" in settings ? settings.buildBaseline : defaultStorage.buildBaseline;
      if (storage.buildBaseline) {
        storage.baseline = {};
      }
      saveStorage();
      this.runNextRound();
    },
    startTimer: function(which, now) {
      if (!storage.running) {
        logBenchmark("startTimer called while benchmark not running");
        return;
      }
      if (!this.startTime) {
        this.startTime = {};
      }
      this.startTime[which] = now;
    },
    stopTimer: function(which, now) {
      if (!storage.running) {
        logBenchmark("stopTimer called while benchmark not running");
        return;
      }
      if (this.startTime[which] === null) {
        logBenchmark("stopTimer called without previous call to startTimer");
        return;
      }
      var took = now - this.startTime[which];
      logBenchmark(which + " took: " + took + "ms");
      storage.current[which].push(took);

      var endWhich = storage.warmBench ? "fgRestartTime" : "startupTime";
      if (which === endWhich) {
        storage.round++;
        saveStorage();
        this.runNextRound();
      }
    },
    sampleMemoryToStorage: function() {
      return sampleMemory().then(function(mem) {
        for (var p in mem) {
          storage.current[p].push(mem[p]);
        }
        saveStorage();
      });
    },
    runNextRound: function() {
      var self = this;
      var done = storage.round >= storage.numRounds;
      function run() {
        var promise;
        if (storage.round === 0) {
          promise = Promise.resolve();
        } else {
          promise = self.sampleMemoryToStorage();
        }

        promise.then(function() {
          if (done) {
            self.finish();
            return;
          }
          DumbPipe.close(DumbPipe.open("gcReload", {}));
        }).catch(function (e) {
          console.error(e)
        });
      }
      if (storage.deleteFs) {
        logBenchmark("Deleting fs.");
        indexedDB.deleteDatabase("asyncStorage");
      }
      if (storage.deleteJitCache) {
        logBenchmark("Deleting jit cache.");
        indexedDB.deleteDatabase("CompiledMethodCache");
      }
      if (storage.round !== 0) {
        logBenchmark("Scheduling round " + (storage.round) + " of " + storage.numRounds + " finalization in " + storage.roundDelay + "ms");
        setTimeout(run, storage.roundDelay);
      } else {
        run();
      }
    },
    finish: function() {
      storage.running = false;
      saveStorage();
      var labels = ["Test", "Baseline Mean", "Mean", "+/-", "%", "P", "Min", "Max"];
      var rows = [labels];
      for (var key in storage.current) {
        var samples = storage.current[key];
        var baselineSamples = storage.baseline[key] || [];
        var hasBaseline = baselineSamples.length > 0;
        var formatter = valueFormatters[key];

        var row = [key];
        row.untrusted = untrustedValues.indexOf(key) != -1;
        rows.push(row);
        var currentMean = mean(samples);
        var baselineMean = mean(baselineSamples);
        row.push(hasBaseline ? formatter(baselineMean) + "" : "n/a");
        row.push(formatter(currentMean) + "");
        row.push(hasBaseline ? formatter(currentMean - baselineMean) + "" : "n/a");
        row.push(hasBaseline ? (100 * (currentMean - baselineMean) / baselineMean).toFixed(2) : "n/a");
        var pMessage = "n/a";
        if (hasBaseline) {
          var p = (baselineSamples.length < 2) ? 1 : ttest(baselineSamples, samples).pValue();
          if (p < 0.05) {
            pMessage = currentMean < baselineMean ? "BETTER" : "WORSE";
          } else {
            pMessage = "SAME";
          }
        } else {
          pMessage = "n/a";
        }
        row.push(pMessage);
        row.push(formatter(Math.min.apply(null, samples)));
        row.push(formatter(Math.max.apply(null, samples)));
      }
      if (storage.buildBaseline) {
        storage.baseline = storage.current;
        storage.buildBaseline = false;
        logBenchmark("FINISHED BUILDING BASELINE");
      }
      logBenchmark("Raw Values:\n" + "Current: " + JSON.stringify(storage.current) + "\nBaseline: " + JSON.stringify(storage.baseline))
      var configRows = [
        ["Config", "Value"],
        ["User Agent", window.navigator.userAgent],
        ["Rounds", storage.numRounds],
        ["Delay(ms)", storage.roundDelay],
        ["Delete FS", storage.deleteFs ? "yes" : "no"],
        ["Delete JIT CACHE", storage.deleteJitCache ? "yes" : "no"],
        ["Warm startup", storage.warmBench ? "yes" : "no"],
      ];
      var out = "\n" +
                prettyTable(configRows, [LEFT, LEFT]) + "\n" +
                prettyTable(rows, [LEFT, RIGHT, RIGHT, RIGHT, RIGHT, RIGHT, RIGHT, RIGHT]);
      logBenchmark(out);
      saveStorage();
    }

  };

  // Start right away instead of in init() so we can see any speedups in script loading.
  if (storage.running) {
    var now = performance.now();
    startup.startTimer("startupTime", now);
    startup.startTimer("vmStartupTime", now);
  }

  var numRoundsEl;
  var roundDelayEl;
  var warmBenchEl;
  var startFGDelayEl;
  var deleteFsEl;
  var deleteJitCacheEl;
  var startButton;
  var baselineButton;

  function getSettings() {
    return {
      numRounds: numRoundsEl.value | 0,
      roundDelay: roundDelayEl.value | 0,
      warmBench: !!warmBenchEl.checked,
      startFGDelay: startFGDelayEl.value | 0,
      deleteFs: !!deleteFsEl.checked,
      deleteJitCache: !!deleteJitCacheEl.checked,
      recordMemory: NO_SECURITY
    };
  }

  function start() {
    startup.run(getSettings());
  }

  function buildBaseline() {
    var settings = getSettings();
    settings.buildBaseline = true;
    startup.run(settings);
  }

  return {
    initUI: function() {
      numRoundsEl = document.getElementById("benchmark-num-rounds");
      roundDelayEl = document.getElementById("benchmark-round-delay");
      warmBenchEl = document.getElementById("benchmark-warm-startup");
      startFGDelayEl = document.getElementById("benchmark-startfg-delay");
      deleteFsEl = document.getElementById("benchmark-delete-fs");
      deleteJitCacheEl = document.getElementById("benchmark-delete-jit-cache");
      startButton = document.getElementById("benchmark-startup-run");
      baselineButton = document.getElementById("benchmark-startup-baseline");

      numRoundsEl.value = storage.numRounds;
      roundDelayEl.value = storage.roundDelay;
      warmBenchEl.checked = storage.warmBench;
      startFGDelayEl.value = storage.startFGDelay;
      deleteFsEl.checked = storage.deleteFs;
      deleteJitCacheEl.checked = storage.deleteJitCache;

      startButton.onclick = start;
      baselineButton.onclick = buildBaseline;
    },
    start: start,
    buildBaseline: buildBaseline,
    sampleMemory: sampleMemory,
    forceCollectors: forceCollectors,
    prettyTable: prettyTable,
    LEFT: LEFT,
    CENTER: CENTER,
    RIGHT: RIGHT,
    startup: {
      setStartTime: function () {
        startup.startTime["startupTime"] = startup.startTime["vmStartupTime"] = performance.now();
      },
      init: function() {
        if (!storage.running) {
          return;
        }

        var vmImplKey = "com/sun/midp/main/MIDletSuiteUtils.vmEndStartUp.(I)V";
        var vmOriginalFn = Native[vmImplKey];
        var vmCalled = false;
        Native[vmImplKey] = function(addr) {
          if (!vmCalled) {
            vmCalled = true;
            var now = performance.now();
            startup.stopTimer("vmStartupTime", now);
            startup.startTimer("bgStartupTime", now);
          }
          vmOriginalFn.apply(null, arguments);
        };

        var bgImplKey = "com/nokia/mid/s40/bg/BGUtils.getFGMIDletClass.()Ljava/lang/String;";
        var bgOriginalFn = Native[bgImplKey];
        Native[bgImplKey] = function(addr) {
          var now = performance.now();
          startup.stopTimer("bgStartupTime", now);
          startup.startTimer("fgStartupTime", now);
          return bgOriginalFn.apply(null, arguments);
        };

        var restartCalled = false;

        function restartFGMIDlet() {
          setTimeout(function() {
            MIDP.setDestroyedForRestart(true);
            MIDP.sendDestroyMIDletEvent(fgMidletClass);
            MIDP.registerDestroyedListener(function() {
              MIDP.registerDestroyedListener(null);
              MIDP.sendExecuteMIDletEvent();
              startup.startTimer("fgRestartTime", performance.now());
            });
          }, storage.startFGDelay);
        }

        var fgImplKey = "com/sun/midp/lcdui/DisplayDevice.gainedForeground0.(II)V";
        var fgOriginalFn = Native[fgImplKey];
        var refresh0Count = 0;
        // First refresh0 call: the first FG MIDlet startup
        // Second refresh0 call: the BG MIDlet temporarily goes to the foreground
        // Third refresh0 call: the FG MIDlet is restarted
        Native[fgImplKey] = function(addr) {
          if (storage.warmBench) {
            if (!restartCalled) {
              restartCalled = true;
              restartFGMIDlet();
            } else if (refresh0Count === 2) {
              startup.stopTimer("fgRestartTime", performance.now());
            }
          }

          if (refresh0Count === 0) {
            var now = performance.now();
            startup.stopTimer("fgStartupTime", now);
            startup.startTimer("fgRefreshStartupTime", now);
          }

          refresh0Count++;

          fgOriginalFn.apply(null, arguments);
        };

        var refreshImplKey = "com/sun/midp/lcdui/DisplayDevice.refresh0.(IIIIII)V";
        var refreshOriginalFn = Native[refreshImplKey];
        var refreshCalled = false;
        Native[refreshImplKey] = function(addr) {
          if (!refreshCalled) {
            refreshCalled = true;
            var now = performance.now();
            startup.stopTimer("fgRefreshStartupTime", now);
            startup.stopTimer("startupTime", now);
          }

          refreshOriginalFn.apply(null, arguments);
        }
      },
      run: startup.run.bind(startup),
    }
  };
})();
