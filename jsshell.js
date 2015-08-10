'use strict';

// Define objects and functions that j2me.js expects
// but are unavailable in the shell environment.

if (typeof console === "undefined") {
  var console = {
    log: print,
  }
}

console.info = function (c) {
  putstr(String.fromCharCode(c));
};

console.error = function (c) {
  putstr(String.fromCharCode(c));
};

var START_TIME = dateNow();
var performance = {
  now: function () {
    return dateNow();
  }
};

function check() {

}

function parseArguments(options, tokens) {
  var leftover = [];
  for (var i = 0; i < tokens.length; i++) {
    var tokenParts = tokens[i].split("=");
    var name = null;
    var value = tokenParts[1];
    if (tokenParts[0].indexOf("--") === 0) {
      name = tokenParts[0].substring(2);
    } else if (tokenParts[0].indexOf("-") === 0) {
      var shortName = tokenParts[0].substring(1);
      name = shortName;
      for (var longName in options) {
        if (options[longName].short === shortName) {
          name = longName;
          break;
        }
      }
    }
    if (tokens[i][0] === "-") {
      if (options[name]) {
        switch (options[name].type) {
          case "number":
            options[name].value = Number(value);
            break;
          case "string":
            options[name].value = value;
            break;
          case "boolean":
            options[name].value = value == "true" || value == "yes";
            break;
        }
      } else {
        print("Illegal option: " + name);
        quit();
      }
    } else {
      leftover.push(tokens[i]);
    }
  }
  return leftover;
}

var options = {
  "writers": {
    short: "w",
    value: "",
    type: "string"
  },
  "maxCompiledMethodCount": {
    short: "m",
    value: -1,
    type: "number"
  },
  "backwardBranchThreshold": {
    short: "bbt",
    value: 1,
    type: "number"
  },
  "invokeThreshold": {
    short: "it",
    value: 1,
    type: "number"
  },
  "enableOnStackReplacement": {
    short: "osr",
    value: true,
    type: "boolean"
  }
};

var files = parseArguments(options, scriptArgs);

if (files.length !== 1) {
  print("error: One main class name must be specified.");
  print("usage: jsshell <main class name>");
  quit(1);
}

var navigator = {
  language: "en-US",
  userAgent: "jsshell",
};

function Image() {}

function alert() {}

var document = {
  documentElement: {
    classList: {
      add: function() {
      },
    },
  },
  querySelector: function() {
    return {
      addEventListener: function() {
      },
    };
  },
  getElementById: function() {
    return {
      addEventListener: function() {
      },
      getContext: function() {
        return {
          save: function() {
          },
        };
      },
      getBoundingClientRect: function() {
        return { top: 0, left: 0, width: 0, height: 0 };
      },
      querySelector: function() {
        return { style: "" };
      },
      dispatchEvent: function(event) {
      },
      style: "",
      removeChild: function(elem) {
      }
    };
  },
  addEventListener: function() {
  },
  createElementNS: function() {
    return {}
  },
};

var microTaskQueue = null;

var window = {
  addEventListener: function() {
  },
  crypto: {
    getRandomValues: function() {
    },
  },
  document: document,
  console: console,
};
window.parent = window;

this.nextTickBeforeEvents = window.nextTickBeforeEvents =
this.nextTickDuringEvents = window.nextTickDuringEvents =
this.setTimeout = window.setTimeout = function (fn, interval) {
  var args = arguments.length > 2 ? Array.prototype.slice.call(arguments, 2) : [];
  var task = microTaskQueue.scheduleInterval(fn, args, interval, false);
  return task.id;
};

window.setInterval = function (fn, interval) {
  var args = arguments.length > 2 ? Array.prototype.slice.call(arguments, 2) : [];
  var task = microTaskQueue.scheduleInterval(fn, args, interval, true);
  return task.id;
};
window.clearTimeout = function (id) {
  microTaskQueue.remove(id);
};

var Event = function() {
}

var config = {
  logConsole: "native",
  args: "",
};

var DumbPipe = {
  open: function() {
  },
};

var profileTimeline = false;

try {
  if (profileTimeline) {
    load("bld/shumway.js");
  }
  load("polyfill/promise.js", "libs/encoding.js", "libs/relooper.js", "bld/native.js", "bld/j2me.js");
  microTaskQueue = new J2ME.Shell.MicroTasksQueue();
  load("bld/main-all.js");


  // load("bld/classes.jar.js");
  // load("bld/program.jar.js");
  // load("bld/tests.jar.js");

  // Define this down here so it overrides the version defined by native.js.
  console.print = function (c) {
    putstr(String.fromCharCode(c));
  };

  var dump = putstr;

  CLASSES.addSourceDirectory("java/cldc1.1.1");
  CLASSES.addSourceDirectory("java/midp");
  // CLASSES.addSourceDirectory("bench/scimark2src");

  JARStore.addBuiltIn("java/classes.jar", snarf("java/classes.jar", "binary").buffer);
  JARStore.addBuiltIn("tests/tests.jar", snarf("tests/tests.jar", "binary").buffer);
  JARStore.addBuiltIn("bench/benchmark.jar", snarf("bench/benchmark.jar", "binary").buffer);
  //JARStore.addBuiltIn("program.jar", snarf("program.jar", "binary").buffer);

  CLASSES.initializeBuiltinClasses();

  var start = dateNow();

  var writers = J2ME.WriterFlags.None;
  if (options.writers.value.indexOf("t") >= 0) {
    writers |= J2ME.WriterFlags.Trace;
  }
  if (options.writers.value.indexOf("s") >= 0) {
    writers |= J2ME.WriterFlags.TraceStack;
  }
  if (options.writers.value.indexOf("h") >= 0) {
    writers |= J2ME.WriterFlags.Thread;
  }
  if (options.writers.value.indexOf("l") >= 0) {
    writers |= J2ME.WriterFlags.Link;
  }
  if (options.writers.value.indexOf("j") >= 0) {
    writers |= J2ME.WriterFlags.JIT;
  }
  if (options.writers.value.indexOf("c") >= 0) {
    writers |= J2ME.WriterFlags.Code;
  }
  J2ME.writers = writers;
  J2ME.enableRuntimeCompilation = false;
  J2ME.maxCompiledMethodCount = options.maxCompiledMethodCount.value;

  J2ME.ConfigThresholds.InvokeThreshold = options.invokeThreshold.value;
  J2ME.ConfigThresholds.BackwardBranchThreshold = options.backwardBranchThreshold.value;
  J2ME.enableOnStackReplacement = options.enableOnStackReplacement.value;

  start = dateNow();
  var runtime = jvm.startIsolate0(files[0], config.args);

  // Pump Event Queue
  microTaskQueue.run(100000, 10000, false, function () {
    return true;
  });

  print("Time: " + (dateNow() - start).toFixed(4) + " ms");
  J2ME.bytecodeCount && print("Bytecodes: " + J2ME.bytecodeCount);
  J2ME.interpreterCounter.traceSorted(new J2ME.IndentingWriter(false, function (x) {
    print(x);
  }));
  if (profileTimeline) {
    J2ME.timeline.createSnapshot().trace(new J2ME.IndentingWriter());
  }
} catch (x) {
  print(x);
  print(x.stack);
}
