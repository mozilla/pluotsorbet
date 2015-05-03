/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

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

if (scriptArgs.length !== 1) {
  print("error: One main class name must be specified.");
  print("usage: jsshell <main class name>");
  quit(1);
}

var callbacks = [];
var window = {
  setZeroTimeout: function(callback) {
    callbacks.push(callback);
  },
  addEventListener: function() {
  },
  crypto: {
    getRandomValues: function() {
    },
  },
};

var navigator = {
  language: "en-US",
};

function Promise() {
  // ...
}

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
    };
  },
  addEventListener: function() {
  },
};

var Event = function() {
}

var config = {
  logConsole: "native",
  args: "",
};

var profileTimeline = false;

try {
  if (profileTimeline) {
    load("bld/shumway.js");
  }
  load("libs/relooper.js", "bld/j2me.js","libs/zipfile.js", "blackBox.js",
    "libs/encoding.js", "util.js", "libs/jarstore.js",
    "native.js", "string.js", "midp/midp.js",
    "libs/long.js", "midp/crypto.js", "libs/forge/md5.js", "libs/forge/util.js",
    "bld/classes.jar.js");

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
  var jvm = new JVM();

  J2ME.writers = J2ME.WriterFlags.None;
  start = dateNow();
  var runtime = jvm.startIsolate0(scriptArgs[0], config.args);
  while (callbacks.length) {
    (callbacks.shift())();
  }
  print("Time: " + (dateNow() - start).toFixed(4) + " ms");
  if (profileTimeline) {
    J2ME.timeline.createSnapshot().trace(new J2ME.IndentingWriter());
  }
  // J2ME.interpreterCounter.traceSorted(new J2ME.IndentingWriter());
} catch (x) {
  print(x);
  print(x.stack);
}
