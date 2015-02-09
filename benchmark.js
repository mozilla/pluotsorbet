var Benchmark = (function() {

  function mean(array) {
    function add(a, b) {
      return a + b;
    }
    return array.reduce(add, 0) / array.length;
  }

  var startup = {
    numRounds: 10,
    roundDelay: 5000, // ms to delay starting next round of tests
    startTime: null,
    baseline: [],
    run: function() {
      this.round = 0;
      this.times = [];
      this.running = true;
      this.runNextRound();
    },
    get round() {
      return localStorage["round"] | 0;
    },
    set round(value) {
      localStorage["round"] = value;
    },
    get running() {
      return !!localStorage["running"];
    },
    set running(value) {
      localStorage["running"] = value ? "true" : "";
    },
    get times() {
      return localStorage["times"] ? JSON.parse(localStorage["times"]) : [];
    },
    set times(values) {
      localStorage["times"] = JSON.stringify(values);
    },
    startTimer: function() {
      if (!this.running) {
        console.log("startTimer called while benchmark not running");
        return;
      }
      this.startTime = performance.now();
    },
    stopTimer: function() {
      if (!this.running) {
        console.log("stopTimer called while benchmark not running");
        return;
      }
      if (this.startTime === null) {
        console.log("stopTimer called without previous call to startTimer");
        return;
      }
      var took = performance.now() - this.startTime;
      this.startTime = null;
      var times = this.times;
      times.push(took);
      this.times = times;
      this.round++;
      if (this.round >= this.numRounds) {
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
      console.log("Scheduling round " + (this.round + 1) + " of " + this.numRounds + " to run in " + this.roundDelay + "ms");
      setTimeout(run, this.roundDelay);
    },
    finish: function() {
      this.running = false;
      var times = this.times;
      var message = "Current times: " + JSON.stringify(times) + "\n";
      var baselineMean = mean(this.baseline);
      var currentMean = mean(times);
      message += "Current mean : " + Math.round(currentMean) + "ms\n";
      if (this.baseline.length) {
        message +=
          "Baseline mean: " + Math.round(baselineMean) + "ms\n" +
          "+/-          : " + Math.round(currentMean - baselineMean) + "ms\n" +
          "%            : " + (100 * (currentMean - baselineMean) / baselineMean).toFixed(2) + "\n";
      }
      if (this.baseline.length) {
        var p = (this.baseline.length < 2) ? 1 : ttest(this.baseline, times).pValue();
        if (p < 0.05) {
          message += currentMean < baselineMean ? "FASTER" : "SLOWER";
        } else {
          message += "INSIGNIFICANT RESULT";
        }
      }
      message = "-------------------------------------------------------------\n" +
                message + "\n" +
                "-------------------------------------------------------------\n";
      console.log(message);

      delete localStorage["running"];
      delete localStorage["times"];
      delete localStorage["round"];
    }

  };

  // Start right away instead of in init() so we can see any speedups in script loading.
  startup.startTimer();

  return {
    startup: {
      init: function() {
        var stoppedTimer = false;
        Native["com/sun/midp/lcdui/DisplayDevice.refresh0.(IIIIII)V"] = function() {
          if (stoppedTimer) {
            return;
          }
          stoppedTimer = true;
          startup.stopTimer();
        };
      },
      run: startup.run.bind(startup),
    }
  };
})();
