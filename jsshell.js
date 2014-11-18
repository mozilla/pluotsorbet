/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

function check() {

}

if (scriptArgs.length !== 1) {
  print("error: One main class name must be specified.");
  print("usage: jsshell <main class name>");
  quit(1);
}

var window = {
  setZeroTimeout: function(callback) {
    callback();
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
      },
      getBoundingClientRect: function() {
        return { top: 0, left: 0, width: 0, height: 0 };
      }
    };
  },
  addEventListener: function() {
  },
};

var urlParams = {
  logConsole: "native",
  args: "",
};

try {
  load("jvm.js", "classes.js", "libs/zipfile.js", "classinfo.js", "classfile/classfile.js",
       "classfile/reader.js", "classfile/tags.js", "classfile/attributetypes.js", "runtime.js",
       "context.js", "libs/encoding.js", "util.js", "frame.js", "arrays.js",
       "classfile/accessflags.js", "instrument.js", "vm.js", "signature.js", "opcodes.js",
       "override.js", "native.js", "string.js", "libs/console.js", "midp/midp.js",
       "libs/long.js", "midp/crypto.js", "libs/forge/md5.js", "libs/forge/util.js", "opt/build/opt.js", "compiled.js");

  var dump = print;
  var console = window.console;

  var start = dateNow();

  var jvm = new JVM();
  jvm.addPath("java/classes.jar", snarf("java/classes.jar", "binary").buffer);
  jvm.addPath("java/tests.jar", snarf("tests/tests.jar", "binary").buffer);
  jvm.initializeBuiltinClasses();

  print("INITIALIZATION TIME: " + (dateNow() - start));

  start = dateNow();
  var runtime = jvm.startIsolate0(scriptArgs[0], urlParams.args);

  J2ME.printResults();
  print("RUNNING TIME: " + (dateNow() - start));
} catch (x) {
  print(x);
  print(x.stack);
}
