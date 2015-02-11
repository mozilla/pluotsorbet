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
    baseline: [],
    running: false,
    round: 0,
    times: [],
    deleteFs: true,
    deleteJitCache: true,
    buildBaseline: false
  };

  function Storage(key, defaults) {
    this.key = key;
    if (!(key in localStorage)) {
      this.storage = defaults;
      this.save();
    }
    this.storage = JSON.parse(localStorage[key]);
    Object.keys(defaultStorage).forEach(function(key) {
      Object.defineProperty(this, key, {
        get: function() { return this.storage[key]; },
        set: function(newValue) { this.storage[key] = newValue; this.save() },
        enumerable: true,
        configurable: true
      });
    }, this);
  }

  Storage.prototype = {
    save: function() {
      localStorage[this.key] = JSON.stringify(this.storage);
    }
  };

  var storage = new Storage("benchmark", defaultStorage);

  var startup = {
    run: function(settings) {
      storage.round = 0;
      storage.times = [];
      storage.running = true;
      storage.numRounds = "numRounds" in settings ? settings.numRounds : defaultStorage.numRounds;
      storage.roundDelay = "roundDelay" in settings ? settings.roundDelay : defaultStorage.roundDelay;
      storage.deleteFs = "deleteFs" in settings ? settings.deleteFs : defaultStorage.deleteFs;
      storage.deleteJitCache = "deleteJitCache" in settings ? settings.deleteJitCache : defaultStorage.deleteJitCache;
      storage.buildBaseline = "buildBaseline" in settings ? settings.buildBaseline : defaultStorage.buildBaseline;
      if (storage.buildBaseline) {
        storage.baseline = [];
      }
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
      var times = storage.times;
      times.push(took);
      storage.times = times;
      storage.round++;
      if (storage.round >= storage.numRounds) {
        this.finish();
        return;
      }
      this.runNextRound();
    },
    runNextRound: function() {
      function run() {
        DumbPipe.close(DumbPipe.open("reload", {}));
      }
      if (typeof netscape !== "undefined" && netscape.security.PrivilegeManager) {
        // To enable GC use a seperate profile and enable the pref:
        // security.turn_off_all_security_so_that_viruses_can_take_over_this_computer
        netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
        console.log("Forcing CC/GC.");
        for (var i = 0; i < 3; i++) {
          Components.utils.forceCC();
          Components.utils.forceGC();
        }
      }
      if (storage.deleteFs) {
        console.log("Deleting fs.");
        indexedDB.deleteDatabase("asyncStorage");
      }
      if (storage.deleteJitCache) {
        console.log("Deleting jit cache.");
        indexedDB.deleteDatabase("CompiledMethodCache");
      }
      console.log("Scheduling round " + (storage.round + 1) + " of " + storage.numRounds + " to run in " + storage.roundDelay + "ms");
      setTimeout(run, storage.roundDelay);
    },
    finish: function() {
      storage.running = false;
      var times = storage.times;
      var message = "Current times: " + JSON.stringify(times) + "\n";
      var baselineMean = mean(storage.baseline);
      var currentMean = mean(times);
      message += "Current mean : " + Math.round(currentMean) + "ms\n";
      if (storage.baseline.length) {
        message +=
          "Baseline mean: " + Math.round(baselineMean) + "ms\n" +
          "+/-          : " + Math.round(currentMean - baselineMean) + "ms\n" +
          "%            : " + (100 * (currentMean - baselineMean) / baselineMean).toFixed(2) + "\n";
      }
      if (storage.baseline.length) {
        var p = (storage.baseline.length < 2) ? 1 : ttest(storage.baseline, times).pValue();
        if (p < 0.05) {
          message += currentMean < baselineMean ? "FASTER" : "SLOWER";
        } else {
          message += "INSIGNIFICANT RESULT";
        }
      }
      if (storage.buildBaseline) {
        storage.baseline = times;
        storage.buildBaseline = false;
        message = "FINISHED BUILDING BASELINE\n" + message;
      }
      message = "-------------------------------------------------------------\n" +
                message + "\n" +
                "-------------------------------------------------------------\n";
      console.log(message);
    }

  };

  // Start right away instead of in init() so we can see any speedups in script loading.
  if (storage.running) {
    startup.startTimer();
  }

  return {
    initUI: function() {
      var numRoundsEl = document.getElementById("benchmark-num-rounds");
      var roundDelayEl = document.getElementById("benchmark-round-delay");
      var deleteFsEl = document.getElementById("benchmark-delete-fs");
      var deleteJitCacheEl = document.getElementById("benchmark-delete-jit-cache");
      var startButton = document.getElementById("benchmark-startup-run");
      var baselineButton = document.getElementById("benchmark-startup-baseline");

      numRoundsEl.value = storage.numRounds;
      roundDelayEl.value = storage.roundDelay;
      deleteFsEl.checked = storage.deleteFs;
      deleteJitCacheEl.checked = storage.deleteJitCache;

      if (storage.baseline.length === 0) {
        startButton.disabled = true;
      }

      function getSettings() {
        return {
          numRounds: numRoundsEl.value | 0,
          roundDelay: roundDelayEl.value | 0,
          deleteFs: !!deleteFsEl.checked,
          deleteJitCache: !!deleteJitCacheEl.checked,
        };
      }

      startButton.onclick = function() {
        startup.run(getSettings());
      };

      baselineButton.onclick = function() {
        var settings = getSettings();
        settings["buildBaseline"] = true;
        startup.run(settings);
      };
    },
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
