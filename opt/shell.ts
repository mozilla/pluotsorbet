///<reference path='build/opt.d.ts' />

var jsGlobal = (function() { return this || (1, eval)('this'); })();
var CC = {};

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
      getBoundingClientRect: function() {
        return { top: 0, left: 0, width: 0, height: 0 };
      }
    };
  },
  addEventListener: function() {
  },
};

jsGlobal.urlParams = {
  logConsole: "native",
  args: "",
};

module J2ME {
  declare var load: (string) => void;
  declare var process, require, global, quit, help, scriptArgs, arguments, snarf;
  declare var JVM, Runtime, CLASSES, Context, release;

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
  var classpathOption: Options.Option;
  var classFilterOption: Options.Option;
  var debuggerOption: Options.Option;
  var releaseOption: Options.Option;


  function main(commandLineArguments: string []) {
    var options = new Options.OptionSet("J2ME");
    var shellOptions = options.register(new Options.OptionSet(""));

    verboseOption = shellOptions.register(new Options.Option("v", "verbose", "boolean", false, "Verbose"));
    classpathOption = shellOptions.register(new Options.Option("cp", "classpath", "string []", [], "Compile ClassPath"));
    classFilterOption = shellOptions.register(new Options.Option("f", "filter", "string", ".*", "Compile Filter"));
    debuggerOption = shellOptions.register(new Options.Option("d", "debugger", "boolean", false, "Emit Debug Information"));
    releaseOption = shellOptions.register(new Options.Option("r", "release", "boolean", false, "Release mode"));

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
      argumentParser.parse(commandLineArguments);
      classpathOption.value.filter(function (value, index, array) {
        if (value.endsWith(".jar")) {
          files.push(value);
        } else {
          return true;
        }
      });
    } catch (x) {
      writer.writeLn(x.message);
      writer.writeLns(x.stack);
      quit();
    }

    release = releaseOption.value;

    var jvm = new JVM();
    for (var i = 0; i < files.length; i++) {
      var file = files[i];
      if (file.endsWith(".jar")) {
        if (verboseOption.value) {
          writer.writeLn("Loading: " + file);
        }
        jvm.addPath(file, snarf(file, "binary").buffer);
        // jvm.addPath("java/tests.jar", snarf("tests/tests.jar", "binary").buffer);
      }
    }
    if (classFilterOption.value) {
      if (verboseOption.value) {
        writer.writeLn("Compiling Pattern: " + classFilterOption.value);
      }
      compile(jvm, classFilterOption.value);
    }
    jvm.initializeBuiltinClasses();
  }

  function getClassInheritanceChain(classInfo: ClassInfo): ClassInfo [] {
    var list = [];
    var klass = classInfo;
    while (klass) {
      list.unshift(klass);
      klass = klass.superClass;
    }
    return list;
  }

  function compileClassInfo(codeWriter: IndentingWriter, classInfo: ClassInfo, ctx: Context) {
    var mangledClassName = J2ME.C4.Backend.mangleClass(classInfo);
    if (!J2ME.C4.Backend.isIdentifierName(mangledClassName)) {
      mangledClassName = quote(mangledClassName);
    }
    codeWriter.enter(mangledClassName + ": {");
    codeWriter.enter("initializer: function () {");
    getClassInheritanceChain(classInfo).forEach(function (klass) {
      if (debuggerOption.value) {
        codeWriter.writeLn("// " + klass.className);
      }
      for (var i = 0; i < klass.fields.length; i++) {
        var fieldInfo = klass.fields[i];
        var signature = TypeDescriptor.makeTypeDescriptor(fieldInfo.signature);
        var kind = signature.kind;
        var defaultValue;
        switch (kind) {
          case Kind.Reference:
            defaultValue = "null";
            break;
          case Kind.Long:
            defaultValue = "Long.ZERO";
            break;
          default:
            defaultValue = "0";
            break;
        }
        codeWriter.writeLn("this." + J2ME.C4.Backend.mangleField(fieldInfo) + " = " + defaultValue + ";");
      }
    });
    codeWriter.leave("}");
    codeWriter.enter("methods: {");
    var methods = classInfo.methods;
    for (var i = 0; i < methods.length; i++) {
      var method = methods[i];
      var mangledMethodName = J2ME.C4.Backend.mangleMethod(method);
      if (!J2ME.C4.Backend.isIdentifierName(mangledMethodName)) {
        mangledMethodName = quote(mangledMethodName);
      }
      try {
        var fn = compileMethodInfo(method, ctx, CompilationTarget.Static);
        if (fn) {
          if (debuggerOption.value) {
            codeWriter.writeLn("// " + method.name);
          }
          codeWriter.enter(mangledMethodName + ": ");
          codeWriter.write(fn);
          codeWriter.leave(",");
          if (verboseOption.value) {
            writer.write(fn);
          }
        }
      } catch (x) {

        codeWriter.writeLn(mangledMethodName + ": undefined,");
      }
    }
    codeWriter.leave("}");
    codeWriter.leave("},");
  }

  function compile(jvm: any, classFilter: string) {
    var runtime = new Runtime(jvm);
    var classFiles = CLASSES.classfiles;
    var ctx = new Context(runtime);

    var code = "";
    var codeWriter = new J2ME.IndentingWriter(false, function (s) {
      code += s + "\n";
    });

    codeWriter.enter("var CC = {");
    Object.keys(classFiles).every(function (name) {
      if (name.substr(-4) !== ".jar") {
        return true;
      }
      var zip = classFiles[name];
      // codeWriter.enter(J2ME.quote(name) + ": {");
      Object.keys(zip.directory).every(function (fileName) {
        if (fileName.substr(-6) !== '.class') {
          return true;
        }
        if (!fileName.match(classFilter)) {
          return true;
        }
        if (verboseOption.value) {
          writer.writeLn("Compiling Class: " + fileName);
        }
        if (debuggerOption.value) {
          codeWriter.writeLn("// " + fileName);
        }
        compileClassInfo(codeWriter, CLASSES.loadClassFile(fileName), ctx);
        // codeWriter.leave("},");
        return true;
      }.bind(this));

      // codeWriter.leave("},");
      return true;
    }.bind(this));
    codeWriter.leave("}");
    writer.writeLn(code);
    if (verboseOption.value) {
      J2ME.printResults();
    }
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