module J2ME {

  import mangleClass = J2ME.C4.Backend.mangleClass;
  import mangleField = J2ME.C4.Backend.mangleField;
  import mangleMethod = J2ME.C4.Backend.mangleMethod;
  import mangleClassAndMethod = J2ME.C4.Backend.mangleClassAndMethod;

  declare var JVM, CLASSES, Context, release;

  export class Emitter {
    constructor(
      public writer: IndentingWriter,
      public closure: boolean,
      public debugInfo: boolean,
      public klassHeaderOnly: boolean = false
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

  export function emitKlass(emitter: Emitter, classInfo: ClassInfo) {
    var writer = emitter.writer;
    var mangledClassName = mangleClass(classInfo);
    if (emitter.closure) {
      writer.writeLn("/** @constructor */");
    }

    function emitFields(fields: FieldInfo [], emitStatic: boolean) {
      for (var i = 0; i < fields.length; i++) {
        var fieldInfo = fields[i];
        if (fieldInfo.isStatic !== emitStatic) {
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
    }

    // Emit class initializer.
    writer.enter("function " + mangledClassName + "() {");
    // Emit call to create hash code. We may also want to save the context that created this
    // object in debug builds for extra assertions.
    writer.writeLn("this.__hashCode__ = $.nextHashCode(this);");
    getClassInheritanceChain(classInfo).forEach(function (ci) {
      emitFields(ci.fields, false);
    });
    writer.leave("}");

    // Emit class static initializer if it has any static fields.
    if (classInfo.fields.some(f => f.isStatic)) {
      writer.enter(mangledClassName + ".staticInitializer = function() {");
      emitFields(classInfo.fields, true);
      writer.leave("}");
    }

    if (emitter.klassHeaderOnly) {
      return;
    }

    if (emitter.closure) {
      writer.writeLn("window[" + quote(mangledClassName) + "] = " + mangledClassName + ";");
    }

    if (classInfo.superClass) {
      var mangledSuperClassName = mangleClass(classInfo.superClass);
      writer.writeLn("$EK(" + mangledClassName + ", " + mangledSuperClassName + ")");
    } else {
      writer.writeLn("$EK(" + mangledClassName + ", null)");
    }

    writer.writeLn("$RK(" + mangledClassName + "," +
                   quote(mangledClassName) + "," +
                   quote(classInfo.className) + ")");
  }

  function compileClassInfo(emitter: Emitter, classInfo: ClassInfo, ctx: Context): CompiledMethodInfo [] {
    var writer = emitter.writer;
    var mangledClassName = mangleClass(classInfo);
    if (!J2ME.C4.Backend.isIdentifierName(mangledClassName)) {
      mangledClassName = quote(mangledClassName);
    }

    emitKlass(emitter, classInfo);

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
          if (method.name === "<clinit>") {
            writer.writeLn(mangledClassName + ".staticConstructor = " + mangledClassAndMethodName);
          } else if (!method.isStatic) {
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
        } else {
          writer.writeLn("trampoline(" +
                         quote(mangledClassName) + "," +
                         quote(mangledMethodName) + "," +
                         quote(mangledClassAndMethodName) + "," +
                         quote(classInfo.className) + "," +
                         quote(method.key) + "," +
                         method.isStatic +
                         ");");
        }
      } catch (x) {
        consoleWriter.writeLn("XXXX: " + x);
        consoleWriter.writeLn(x.stack);
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


































