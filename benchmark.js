var Benchmark = (function() {

  function mean(array) {
    function add(a, b) {
      return a + b;
    }
    return array.reduce(add, 0) / array.length;
  }

  var defaultStorage = {
    numRounds: 10,
    roundDelay: 5000, // ms to delay starting next round of tests
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
    // To enable chrome privileges use a separate profile and enable the pref:
    // security.turn_off_all_security_so_that_viruses_can_take_over_this_computer
    netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
  }

  function forceCollectors() {
    if (!NO_SECURITY) {
      return Promise.resolve();
    }
    return new Promise(function(resolve, reject) {
      enableSuperPowers();
      console.log("Starting minimize memory.");
      var gMgr = Components.classes["@mozilla.org/memory-reporter-manager;1"].getService(Components.interfaces.nsIMemoryReporterManager);
      Components.utils.import("resource://gre/modules/Services.jsm");
      Services.obs.notifyObservers(null, "child-mmu-request", null);
      gMgr.minimizeMemoryUsage(function() {
        console.log("Finished minimize memory.");
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
        maxLength = Math.max(rows[rowIndex][colIndex].toString().length, maxLength);
      }
      maxColumnLengths[colIndex] = maxLength;
    }
    var out = "";
    for (var rowIndex = 0; rowIndex < rows.length; rowIndex++) {
      out += "| ";
      for (var colIndex = 0; colIndex < numColumns; colIndex++) {
        out += pad(rows[rowIndex][colIndex].toString(), " ", maxColumnLengths[colIndex], rowIndex === 0 ? CENTER : alignment[colIndex]) + " | ";
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
    totalSize: byteFormatter,
    domSize: byteFormatter,
    styleSize: byteFormatter,
    jsObjectsSize: byteFormatter,
    jsStringsSize: byteFormatter,
    jsOtherSize: byteFormatter,
    otherSize: byteFormatter,
  };

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
        console.log(e);
      }

      return {
        totalSize: totalSize.value,
        domSize: domSize.value,
        styleSize: styleSize.value,
        jsObjectsSize: jsObjectsSize.value,
        jsStringsSize: jsStringsSize.value,
        jsOtherSize: jsOtherSize.value,
        otherSize: otherSize.value,
      };
    });
  }

  var startup = {
    run: function(settings) {
      storage.round = 0;
      var current = storage.current = {};
      current.startupTime = [];
      if (settings.recordMemory) {
        storage.recordMemory = true;
        current.totalSize     = [];
        current.domSize       = [];
        current.styleSize     = [];
        current.jsObjectsSize = [];
        current.jsStringsSize = [];
        current.jsOtherSize   = [];
        current.otherSize     = [];
      }
      storage.running = true;
      storage.numRounds = "numRounds" in settings ? settings.numRounds : defaultStorage.numRounds;
      storage.roundDelay = "roundDelay" in settings ? settings.roundDelay : defaultStorage.roundDelay;
      storage.deleteFs = "deleteFs" in settings ? settings.deleteFs : defaultStorage.deleteFs;
      storage.deleteJitCache = "deleteJitCache" in settings ? settings.deleteJitCache : defaultStorage.deleteJitCache;
      storage.buildBaseline = "buildBaseline" in settings ? settings.buildBaseline : defaultStorage.buildBaseline;
      if (storage.buildBaseline) {
        storage.baseline = {};
      }
      saveStorage();
      this.runNextRound();
    },
    startTimer: function() {
      if (!storage.running) {
        console.log("startTimer called while benchmark not running");
        return;
      }
      this.startTime = performance.now();
    },
    stopTimer: function() {
      if (!storage.running) {
        console.log("stopTimer called while benchmark not running");
        return;
      }
      if (this.startTime === null) {
        console.log("stopTimer called without previous call to startTimer");
        return;
      }
      var took = performance.now() - this.startTime;
      this.startTime = null;
      storage.current.startupTime.push(took);
      storage.round++;
      saveStorage();
      this.runNextRound();
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
        console.log("Deleting fs.");
        indexedDB.deleteDatabase("asyncStorage");
      }
      if (storage.deleteJitCache) {
        console.log("Deleting jit cache.");
        indexedDB.deleteDatabase("CompiledMethodCache");
      }
      if (storage.round !== 0) {
        console.log("Scheduling round " + (storage.round) + " of " + storage.numRounds + " finalization in " + storage.roundDelay + "ms");
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
            pMessage = "INSIGNIFICANT";
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
        console.log("FINISHED BUILDING BASELINE");
      }
      console.log("Raw Values:\n" + "Current: " + JSON.stringify(storage.current) + "\nBaseline: " + JSON.stringify(storage.baseline))
      console.log("\n" + prettyTable(rows, [LEFT, RIGHT, RIGHT, RIGHT, RIGHT, RIGHT, RIGHT, RIGHT]));
      saveStorage();
    }

  };

  // Start right away instead of in init() so we can see any speedups in script loading.
  if (storage.running) {
    startup.startTimer();
  }

  var numRoundsEl;
  var roundDelayEl;
  var deleteFsEl;
  var deleteJitCacheEl;
  var startButton;
  var baselineButton;

  function getSettings() {
    return {
      numRounds: numRoundsEl.value | 0,
      roundDelay: roundDelayEl.value | 0,
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
      deleteFsEl = document.getElementById("benchmark-delete-fs");
      deleteJitCacheEl = document.getElementById("benchmark-delete-jit-cache");
      startButton = document.getElementById("benchmark-startup-run");
      baselineButton = document.getElementById("benchmark-startup-baseline");

      numRoundsEl.value = storage.numRounds;
      roundDelayEl.value = storage.roundDelay;
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
      init: function() {
        if (!storage.running) {
          return;
        }
        var implKey = "com/sun/midp/lcdui/DisplayDevice.gainedForeground0.(II)V";
        var originalFn = Native[implKey];
        Native[implKey] = function() {
          startup.stopTimer();
          originalFn.apply(null, arguments);
        };
      },
      run: startup.run.bind(startup),
    }
  };
})();
