///<reference path='build/opt.d.ts' />

var jsGlobal = (function() { return this || (1, eval)('this'); })();

jsGlobal.window = {
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

jsGlobal.navigator = {
  language: "en-US",
};

jsGlobal.document = {
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
    };
  },
};

jsGlobal.urlParams = {
  logConsole: "native",
  args: "",
};

module J2ME {
  declare var load: (string) => void;
  declare var process, require, global, quit, help, scriptArgs, arguments, snarf;
  declare var JVM, Runtime, CLASSES, Context;

  var isNode = typeof process === 'object';
  var writer: IndentingWriter;
  var rootPath = "";

  function loadFiles(...files: string[]) {
    for (var i = 0; i < files.length; i++) {
      load(rootPath + files[i]);
    }
  }

  loadFiles("jvm.js", "classes.js", "libs/zipfile.js", "classinfo.js", "classfile/classfile.js",
    "classfile/reader.js", "classfile/tags.js", "classfile/attributetypes.js", "runtime.js",
    "context.js", "libs/encoding.js", "util.js", "frame.js", "arrays.js",
    "classfile/accessflags.js", "instrument.js", "vm.js", "signature.js", "opcodes.js",
    "override.js", "native.js", "string.js", "libs/console.js", "midp/midp.js",
    "libs/long.js", "midp/crypto.js", "libs/forge/md5.js", "libs/forge/util.js", "opt/build/opt.js");

  writer = new IndentingWriter();

  var verboseOption: Options.Option;
  var compileOption: Options.Option;

  function main(commandLineArguments: string []) {
    var options = new Options.OptionSet("J2ME");
    var shellOptions = options.register(new Options.OptionSet(""));

    verboseOption = shellOptions.register(new Options.Option("v", "verbose", "boolean", false, "Verbose"));
    compileOption = shellOptions.register(new Options.Option("c", "compile", "string", ".*", "Compile Filter"));

    var argumentParser = new Options.ArgumentParser();
    argumentParser.addBoundOptionSet(shellOptions);

    function printUsage() {
      writer.enter("J2ME Command Line Interface");
      options.trace(writer);
      writer.leave("");
    }

    argumentParser.addArgument("h", "help", "boolean", {
      parse: function (x) {
        printUsage();
      }
    });

    var files = [];

    // Try and parse command line arguments.

    try {
      argumentParser.parse(commandLineArguments).filter(function (value, index, array) {
        if (value.endsWith(".jar")) {
          files.push(value);
        } else {
          return true;
        }
      });
    } catch (x) {
      writer.writeLn(x.message);
      quit();
    }

    var jvm = new JVM();
    for (var i = 0; i < files.length; i++) {
      var file = files[i];
      if (file.endsWith(".jar")) {
        if (verboseOption) {
          writer.writeLn("Loading: " + file);
        }
        jvm.addPath(file, snarf(file, "binary").buffer);
        // jvm.addPath("java/tests.jar", snarf("tests/tests.jar", "binary").buffer);
      }
    }
    if (compileOption.value) {
      if (verboseOption) {
        writer.writeLn("Compiling Pattern: " + compileOption.value);
      }
      compile(jvm, compileOption.value);
    }
    jvm.initializeBuiltinClasses();
  }

  function compile(jvm: any, classFilter: string) {
    var runtime = new Runtime(jvm);
    var classFiles = CLASSES.classfiles;
    var ctx = new Context(runtime);

    var code = "";
    var codeWriter = new J2ME.IndentingWriter(false, function (s) {
      code += s + "\n";
    });

    codeWriter.enter("var code = {");
    Object.keys(classFiles).every(function (name) {
      if (name.substr(-4) !== ".jar") {
        return true;
      }
      var zip = classFiles[name];
      codeWriter.enter(J2ME.quote(name) + ": {");
      Object.keys(zip.directory).every(function (fileName) {
        if (fileName.substr(-6) !== '.class') {
          return true;
        }
        if (!fileName.match(classFilter)) {
          return true;
        }
        if (verboseOption) {
          writer.writeLn("Compiling Class: " + fileName);
        }
        codeWriter.enter(J2ME.quote(fileName) + ": {");
        J2ME.compileClassInfo(codeWriter, CLASSES.loadClassFile(fileName), ctx, CompilationTarget.Static);
        codeWriter.leave("},");
        return true;
      }.bind(this));

      codeWriter.leave("},");
      return true;
    }.bind(this));
    codeWriter.leave("}");
    writer.writeLn(code);
  }

  var commandLineArguments: string [];
  // Shell Entry Point
  if (typeof help === "function") {
    // SpiderMonkey
    if (typeof scriptArgs === "undefined") {
      commandLineArguments = arguments;
    } else {
      commandLineArguments = scriptArgs;
    }
  } else if (isNode) {
    // node.js
    var commandLineArguments: string[] =
      Array.prototype.slice.call(process.argv, 2);
  }

  main(commandLineArguments);
}

  /*
  var dump = print;
  var console = window.console;

  var start = dateNow();

  var jvm = new JVM();
  jvm.addPath("java/classes.jar", snarf("java/classes.jar", "binary").buffer);
  jvm.addPath("java/tests.jar", snarf("tests/tests.jar", "binary").buffer);
  jvm.initializeBuiltinClasses();

  print("INITIALIZATION TIME: " + (dateNow() - start));

  start = dateNow();
  // var runtime = jvm.startIsolate0(scriptArgs[0], urlParams.args);

  var runtime = new Runtime(jvm);
  CLASSES.compileAll(runtime);

  J2ME.printResults();
  print("RUNNING TIME: " + (dateNow() - start));
} catch (x) {
  print(x);
  print(x.stack);
}
*/

//
//'use strict';
//
//if (scriptArgs.length !== 1) {
//  print("error: One main class name must be specified.");
//  print("usage: jsshell <main class name>");
//  quit(1);
//}
//
//var window = {
//  setZeroTimeout: function(callback) {
//    callback();
//  },
//  addEventListener: function() {
//  },
//  crypto: {
//    getRandomValues: function() {
//    },
//  },
//};
//
//var navigator = {
//  language: "en-US",
//};
//
//var document = {
//  documentElement: {
//    classList: {
//      add: function() {
//      },
//    },
//  },
//  querySelector: function() {
//    return {
//      addEventListener: function() {
//      },
//    };
//  },
//  getElementById: function() {
//    return {
//      addEventListener: function() {
//      },
//      getContext: function() {
//      },
//    };
//  },
//};
//
//var urlParams = {
//  logConsole: "native",
//  args: "",
//};
//
//try {
//  load("jvm.js", "classes.js", "libs/zipfile.js", "classinfo.js", "classfile/classfile.js",
//    "classfile/reader.js", "classfile/tags.js", "classfile/attributetypes.js", "runtime.js",
//    "context.js", "libs/encoding.js", "util.js", "frame.js", "arrays.js",
//    "classfile/accessflags.js", "instrument.js", "vm.js", "signature.js", "opcodes.js",
//    "override.js", "native.js", "string.js", "libs/console.js", "midp/midp.js",
//    "libs/long.js", "midp/crypto.js", "libs/forge/md5.js", "libs/forge/util.js", "opt/build/opt.js");
//
//  var dump = print;
//  var console = window.console;
//
//  var start = dateNow();
//
//  var jvm = new JVM();
//  jvm.addPath("java/classes.jar", snarf("java/classes.jar", "binary").buffer);
//  jvm.addPath("java/tests.jar", snarf("tests/tests.jar", "binary").buffer);
//  jvm.initializeBuiltinClasses();
//
//  print("INITIALIZATION TIME: " + (dateNow() - start));
//
//  start = dateNow();
//  // var runtime = jvm.startIsolate0(scriptArgs[0], urlParams.args);
//
//  var runtime = new Runtime(jvm);
//  CLASSES.compileAll(runtime);
//
//  J2ME.printResults();
//  print("RUNNING TIME: " + (dateNow() - start));
//} catch (x) {
//  print(x);
//  print(x.stack);
//}
