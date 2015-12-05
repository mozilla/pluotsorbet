/**
 * Copyright 2014 Mozilla Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

'use strict';

var evalScript;
if (typeof evalScript === "undefined") {
  evalScript = load;
}

// Define objects and functions that j2me.js expects
// but are unavailable in the shell environment.
evalScript("shell/polyfill.js");

var START_TIME = dateNow();

function parseArguments(options, tokens) {
  var leftover = [];
  for (var i = 0; i < tokens.length; i++) {
    var tokenParts = tokens[i].split(":");
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
        if (options[name].eatsNextToken && typeof value === "undefined") {
          value = tokens[++i];
        }
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
        quit(1);
      }
    } else {
      leftover.push(tokens[i]);
    }
  }
  return leftover;
}

evalScript("config/default.js");

var options = {
  "jar": {
    value: "",
    type: "string",
    standard: true,
    eatsNextToken: true,
  },
  "classpath": {
    short: "cp",
    eatsNextToken: true,
    value: "",
    type: "string",
    standard: true,
    description: "<class search path of directories and zip/jar files>\n" +
    "\t\tA : separated list of directories, JAR archives,\n" +
    "\t\tand ZIP archives to search for class files."
  },
  // PUT ALL NON STANDARD OPTIONS BELOW HERE
  "Xbootclasspath": {
    value: "java/classes.jar",
    type: "string",
  },
  "writers": {
    short: "w",
    value: "",
    type: "string",
    description: "Char flags to enable debugging writers:\n" +
      "\t\tt=trace\n" +
      "\t\ts=stack trace\n" +
      "\t\th=thread\n" +
      "\t\tl=link\n" +
      "\t\tj=jit\n" +
      "\t\tc=compiled code"
  },
  "maxCompiledMethodCount": {
    short: "m",
    value: -1,
    type: "number"
  },
  "backwardBranchThreshold": {
    short: "bbt",
    value: config.backwardBranchThreshold,
    type: "number"
  },
  "invokeThreshold": {
    short: "it",
    value: config.invokeThreshold,
    type: "number"
  },
  "enableOnStackReplacement": {
    short: "osr",
    value: true,
    type: "boolean"
  },
  "emitCheckArrayBounds": {
    short: "cab",
    value: true,
    type: "boolean"
  },
  "emitCheckArrayStore": {
    short: "cas",
    value: true,
    type: "boolean"
  },
  "stats": {
    short: "stats",
    value: true,
    type: "boolean",
  },
};

function printUsage() {
  print("Usage: pluot [-options] class [args...]\n" +
    "           (to execute a class)\n" +
    "   or  pluot [-options] -jar jarfile [args...]\n" +
    "           (to execute a jar file))\n" +
    "where options include:"
  );
  var foundFirstNonStandard = false;
  for (var key in options) {
    var option = options[key];
    if (!foundFirstNonStandard && !option.standard) {
      print("pluot sorbet specifc options include:");
      foundFirstNonStandard = true;
    }
    var description = "    -" + key;
    if (!option.eatsNextToken) {
      description += ":<" + option.type + ">";
    }
    if (option.short) {
      description += " | -" + option.short
    }
    if (option.description) {
      description += " " + option.description;
    }
    print(description);
  }
}

var leftoverArguments = parseArguments(options, scriptArgs);

var mainInJar = false;
var mainClass;
var mainArgs;
if (options.jar.value !== "") {
  if (typeof options.jar.value === "undefined") {
    print("Error: -jar requires jar file specification");
    printUsage();
    quit(1);
  }
  mainInJar = true;
  mainArgs = leftoverArguments;
} else if (leftoverArguments.length < 1) {
  printUsage();
  quit(1);
} else {
  mainClass = leftoverArguments[0];
  mainArgs = leftoverArguments.slice(1);
}

var config = {
  logConsole: "native",
  args: mainArgs
};

var profileTimeline = false;

function loadJar(filename) {
  if (filename.substr(-4) !== ".jar") {
    print("Only jar's are currently supported by classpath.");
    quit(1);
  }
  JARStore.addBuiltIn(filename, snarf(filename, "binary").buffer);
}


if (profileTimeline) {
  evalScript("bld/shumway.js");
}

evalScript("polyfill/promise.js", "libs/encoding.js", "bld/native.js", "bld/j2me.js");

microTaskQueue = new J2ME.Shell.MicroTasksQueue();
evalScript("bld/main-all.js");

// Define this down here so it overrides the version defined by native.js.
console.print = function (c) {
  putstr(String.fromCharCode(c));
};

var dump = putstr;

var bootClassPath = options["Xbootclasspath"].value;
loadJar(bootClassPath);
if (options.classpath.value !== "") {
  var classPath = options.classpath.value.split(":");
  for (var i = 0; i < classPath.length; i++) {
    loadJar(classPath[i]);
  }
}

if (mainInJar) {
  var mainJar = options.jar.value;
  loadJar(mainJar);
  var manifest = J2ME.parseManifest(util.decodeUtf8Array(JARStore.loadFileFromJAR(mainJar, "META-INF/MANIFEST.MF")));
  mainClass = manifest["Main-Class"];
  if (!mainClass) {
    print("no main manifest attribute, in " + options.jar.value);
    quit(1);
  }
}

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
J2ME.enableRuntimeCompilation = true;
J2ME.maxCompiledMethodCount = options.maxCompiledMethodCount.value;

J2ME.ConfigThresholds.InvokeThreshold = options.invokeThreshold.value;
J2ME.ConfigThresholds.BackwardBranchThreshold = options.backwardBranchThreshold.value;
J2ME.enableOnStackReplacement = options.enableOnStackReplacement.value;
J2ME.emitCheckArrayBounds = options.emitCheckArrayBounds.value;
J2ME.emitCheckArrayStore = options.emitCheckArrayStore.value;

start = dateNow();
var runtime = jvm.startIsolate0(mainClass, config.args);

// Pump Event Queue
microTaskQueue.run(100000, 10000, false, function () {
  return true;
});

if (options.stats.value) {
  print("-------------------------------------------------------");
  print("Total Time: " + (dateNow() - start).toFixed(4) + " ms");
  print("bytecodeCount: " + J2ME.bytecodeCount);
  print("interpreterCount: " + J2ME.interpreterCount);
  print("compiledMethodCount: " + J2ME.compiledMethodCount);
  print("onStackReplacementCount: " + J2ME.onStackReplacementCount);
  print("-------------------------------------------------------");
  var writer = new J2ME.IndentingWriter(false, function (x) {
    print(x);
  });
  if (J2ME.runtimeCounter) {
    J2ME.runtimeCounter.traceSorted(writer);
  }
  if (J2ME.gcCounter) {
    J2ME.gcCounter.traceSorted(writer);
  }
  J2ME.interpreterCounter.traceSorted(writer);
}
if (profileTimeline) {
  J2ME.timeline.createSnapshot().trace(new J2ME.IndentingWriter());
}
