module J2ME {

  import mangleClass = J2ME.C4.Backend.mangleClass;
  import mangleField = J2ME.C4.Backend.mangleField;
  import mangleMethod = J2ME.C4.Backend.mangleMethod;
  import mangleClassAndMethod = J2ME.C4.Backend.mangleClassAndMethod;

  declare var JVM, Runtime, CLASSES, Context, release;

  var consoleWriter = new IndentingWriter();

  export class Emitter {
    constructor(
      public writer: IndentingWriter,
      public closure: boolean,
      public debugInfo: boolean
    ) {
      // ...
    }
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

  function compileConstructor(emitter: Emitter, classInfo: ClassInfo) {
    var writer = emitter.writer;
    var mangledClassName = mangleClass(classInfo);
    if (emitter.closure) {
      writer.writeLn("/** @constructor */");
    }

    writer.enter("function " + mangledClassName + "() {");
    getClassInheritanceChain(classInfo).forEach(function (klass) {
      for (var i = 0; i < klass.fields.length; i++) {
        var fieldInfo = klass.fields[i];
        if (fieldInfo.isStatic) {
          continue;
        }
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
        if (emitter.closure) {
          writer.writeLn("this[" + quote(mangleField(fieldInfo)) + "] = " + defaultValue + ";");
        } else {
          writer.writeLn("this." + mangleField(fieldInfo) + " = " + defaultValue + ";");
        }
      }
    });
    writer.leave("}");

    if (emitter.closure) {
      writer.writeLn("window[" + quote(mangledClassName) + "] = " + mangledClassName + ";");
    }

    if (classInfo.superClass) {
      var mangledSuperClassName = mangleClass(classInfo.superClass);
      writer.writeLn(mangledClassName + ".prototype = Object.create(" + mangledSuperClassName + ".prototype);");
    }
  }

  function compileClassInfo(emitter: Emitter, classInfo: ClassInfo, ctx: Context): CompiledMethodInfo [] {
    var writer = emitter.writer;
    var mangledClassName = mangleClass(classInfo);
    if (!J2ME.C4.Backend.isIdentifierName(mangledClassName)) {
      mangledClassName = quote(mangledClassName);
    }

    compileConstructor(emitter, classInfo);

    var methods = classInfo.methods;
    var compiledMethods: CompiledMethodInfo [] = [];
    for (var i = 0; i < methods.length; i++) {
      var method = methods[i];
      var mangledMethodName = mangleMethod(method);
      if (!J2ME.C4.Backend.isIdentifierName(mangledMethodName)) {
        mangledMethodName = quote(mangledMethodName);
      }
      try {
        if (emitter.debugInfo) {
          writer.writeLn("// " + classInfo.className + "/" + method.name);
        }
        var mangledClassAndMethodName = mangleClassAndMethod(method);
        var compiledMethod = compileMethodInfo(method, ctx, CompilationTarget.Static);
        if (compiledMethod && compiledMethod.body) {
          writer.enter("function " + mangledClassAndMethodName + "(" + compiledMethod.args.join(",") + ") {");
          writer.writeLns(compiledMethod.body);
          writer.leave("}");
          if (!method.isStatic) {
            if (emitter.closure) {
              writer.writeLn(mangledClassName + ".prototype[" + quote(mangledMethodName) + "] = " + mangledClassAndMethodName + ";");
            } else {
              writer.writeLn(mangledClassName + ".prototype." + mangledMethodName + " = " + mangledClassAndMethodName + ";");
            }
            if (emitter.closure) {
              writer.writeLn("window[" + quote(mangledClassAndMethodName) + "] = " + mangledClassAndMethodName + ";");
            }
          }
          compiledMethods.push(compiledMethod);
        }
      } catch (x) {
        consoleWriter.writeLn("XXXX: " + x);
      }
    }

    return compiledMethods;
  }

  export function compile(jvm: any, classFilter: string, debugInfo: boolean) {
    var runtime = new Runtime(jvm);
    var classFiles = CLASSES.classfiles;
    var ctx = new Context(runtime);

    var code = "";
    var writer = new J2ME.IndentingWriter(false, function (s) {
      code += s + "\n";
    });

    var emitter = new Emitter(writer, false, debugInfo);

    var compiledMethods: CompiledMethodInfo [] = [];
    var classInfoList: ClassInfo [] = [];
    Object.keys(classFiles).every(function (path) {
      if (path.substr(-4) !== ".jar") {
        return true;
      }
      var zipFile = classFiles[path];
      Object.keys(zipFile.directory).every(function (fileName) {
        if (fileName.substr(-6) !== '.class') {
          return true;
        }
        var classInfo = CLASSES.loadClassFile(fileName);
        if (!classInfo.className.match(classFilter)) {
          return true;
        }
        classInfoList.push(classInfo);
        return true;
      }.bind(this));
      return true;
    }.bind(this));

    var orderedClassInfoList: ClassInfo [] = [];

    function indexOf(list, classInfo) {
      if (!classInfo) {
        return -1;
      }
      for (var i = 0; i < list.length; i++) {
        if (list[i].className === classInfo.className) {
          return i;
        }
      }
      return -1;
    }
    while (classInfoList.length) {
      for (var i = 0; i < classInfoList.length; i++) {
        var classInfo = classInfoList[i];
        // writer.writeLn(nameOf(classInfo) + " " + nameOf(classInfo.superClass) + " " + classInfoList.indexOf(classInfo.superClass));
        if (indexOf(classInfoList, classInfo.superClass) < 0) {
          orderedClassInfoList.push(classInfo);
          classInfoList.splice(i--, 1);
          break;
        }
      }
    }

    for (var i = 0; i < orderedClassInfoList.length; i++) {
      var classInfo = orderedClassInfoList[i];
      if (emitter.debugInfo) {
        writer.writeLn("// " + classInfo.className + (classInfo.superClass ? " extends " + classInfo.superClass.className : ""));
      }
      ArrayUtilities.pushMany(compiledMethods, compileClassInfo(emitter, classInfo, ctx));
    }

    /*
    writer.enter("referencedClasses: [");
    var referencedClasses = Object.create(null);
    compiledMethods.forEach(compiledMethod => {
      compiledMethod.referencedClasses.forEach(classInfo => {
        referencedClasses[classInfo.className] = true;
      });
    });
    var classNames = Object.keys(referencedClasses);
    for (var i = 0; i < classNames.length; i++) {
      writer.writeLn(quote(classNames[i]) + (i < classNames.length - 1 ? "," : ""));
    }
    writer.leave("]");
    */
    consoleWriter.writeLn(code);
  }
}


































