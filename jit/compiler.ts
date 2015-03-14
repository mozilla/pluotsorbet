module J2ME {

  import quote = StringUtilities.quote;

  declare var optimizerCompileMethod;

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

  export enum CompilationTarget {
    Runtime,
    Static
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
    var mangledClassName = classInfo.mangledName;
    if (emitter.closure) {
      writer.writeLn("/** @constructor */");
    }

    function emitFields(fields: FieldInfo [], emitStatic: boolean) {
      for (var i = 0; i < fields.length; i++) {
        var fieldInfo = fields[i];
        if (fieldInfo.isStatic !== emitStatic) {
          continue;
        }
        var kind = getSignatureKind(fieldInfo.utf8Signature);
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
          writer.writeLn("this[" + quote(fieldInfo.mangledName) + "] = " + defaultValue + ";");
        } else {
          writer.writeLn("this." + fieldInfo.mangledName + " = " + defaultValue + ";");
        }
      }
    }

    // Emit class initializer.
    writer.enter("function " + mangledClassName + "() {");
    //
    // Should we or should we not generate hash codes at this point? Eager or lazy, we should at least
    // initialize it zero to keep object shapes fixed.
    // writer.writeLn("this._hashCode = $.nextHashCode(this);");
    writer.writeLn("this._hashCode = 0;");
    emitFields(classInfo.fTable, false);
    writer.leave("}");

    if (emitter.klassHeaderOnly) {
      return;
    }

    if (emitter.closure) {
      writer.writeLn("window[" + quote(mangledClassName) + "] = " + mangledClassName + ";");
    }
  }

  function classNameWithDots(classInfo: ClassInfo) {
    return classInfo.getClassNameSlow().replace(/\//g, '.');
  }

  export function emitMethodMetaData(emitter: Emitter, methodInfo: MethodInfo, compiledMethodInfo: CompiledMethodInfo) {
    var metaData = <AOTMetaData>Object.create(null);
    metaData.osr = compiledMethodInfo.onStackReplacementEntryPoints;
    emitter.writer.writeLn("AOTMD[\"" + methodInfo.mangledClassAndMethodName + "\"] = " + JSON.stringify(metaData) + ";");
  }

  export function emitReferencedSymbols(emitter: Emitter, classInfo: ClassInfo, compiledMethods: CompiledMethodInfo []) {
    var referencedClasses = [];
    for (var i = 0; i < compiledMethods.length; i++) {
      var compiledMethod = compiledMethods[i];
      compiledMethod.referencedClasses.forEach(classInfo => {
        ArrayUtilities.pushUnique(referencedClasses, classInfo);
      });
    }

    var mangledClassName = classInfo.mangledName;

    emitter.writer.writeLn(mangledClassName + ".classSymbols = [" + referencedClasses.map(classInfo => {
      return quote(classInfo.getClassNameSlow());
    }).join(", ") + "];");
  }

  var failedCompilations = 0;

  function compileClassInfo(emitter: Emitter, classInfo: ClassInfo,
                            methodFilterList: string[],
                            ctx: Context): CompiledMethodInfo [] {
    var writer = emitter.writer;
    var mangledClassName = classInfo.mangledName;
    if (!isIdentifierName(mangledClassName)) {
      mangledClassName = quote(mangledClassName);
    }

    var classNameParts;

    emitKlass(emitter, classInfo);

    var methods = classInfo.getMethods();
    var compiledMethods: CompiledMethodInfo [] = [];
    for (var i = 0; i < methods.length; i++) {
      var method = methods[i];
      if (method.isNative) {
        continue;
      }
      if (!method.codeAttribute) {
        continue;
      }
      if (methodFilterList !== null && methodFilterList.indexOf(method.implKey) < 0) {
        continue;
      }
      var mangledMethodName = method.mangledName;
      if (!isIdentifierName(mangledMethodName)) {
        mangledMethodName = quote(mangledMethodName);
      }
      try {
        var mangledClassAndMethodName = method.mangledClassAndMethodName;
        if (emitter.debugInfo) {
          writer.writeLn("// " + method.implKey + " (" + mangledClassAndMethodName + ")");
        }
        var compiledMethod = undefined;
        try {
          compiledMethod = compileMethod(method, ctx, CompilationTarget.Static);
        } catch (e) {
          stderrWriter.errorLn("Compiler Exception: " + method.implKey + " " + e.toString());
          failedCompilations ++;
        }
        if (compiledMethod && compiledMethod.body) {
          if (methodFilterList) {
            methodFilterList.splice(methodFilterList.indexOf(method.implKey), 1);
          }
          var compiledMethodName = mangledClassAndMethodName;
          writer.enter("function " + compiledMethodName + "(" + compiledMethod.args.join(",") + ") {");
          writer.writeLns(compiledMethod.body);
          writer.leave("}");
          if (method.name === "<clinit>") {
            writer.writeLn(mangledClassName + ".staticConstructor = " + mangledClassAndMethodName);
          } else if (!method.isStatic) {
            //if (emitter.closure) {
            //  writer.writeLn(mangledClassName + ".prototype[" + quote(mangledMethodName) + "] = " + mangledClassAndMethodName + ";");
            //} else {
            //  writer.writeLn(mangledClassName + ".prototype." + mangledMethodName + " = " + mangledClassAndMethodName + ";");
            //}
            if (emitter.closure) {
              writer.writeLn("window[" + quote(mangledClassAndMethodName) + "] = " + mangledClassAndMethodName + ";");
            }
          }
          emitMethodMetaData(emitter, method, compiledMethod);
          compiledMethods.push(compiledMethod);
        }
      } catch (x) {
        stderrWriter.writeLn("XXXX: " + x);
        stderrWriter.writeLn(x.stack);
      }
    }

    emitReferencedSymbols(emitter, classInfo, compiledMethods);

    return compiledMethods;
  }

  export class CompiledMethodInfo {
    constructor(public args: string [],
                public body: string,
                public referencedClasses: ClassInfo [],
                public onStackReplacementEntryPoints: number [] = null) {
      // ...
    }
  }

  export function compileMethod(methodInfo: MethodInfo, ctx: Context, target: CompilationTarget): CompiledMethodInfo {
    var method;
    method = baselineCompileMethod(methodInfo, target);
    return method;
  }

  export function compile(jvm: any,
                          jarFiles: Map<string, any>,
                          jarFilter: (jarFile: string) => boolean,
                          classFilter: (classInfo: ClassInfo) => boolean,
                          methodFilterList: string[],
                          fileFilter: string, debugInfo: boolean) {
    var runtime = new Runtime(jvm);
    var ctx = new Context(runtime);
    var code = "";
    var writer = new J2ME.IndentingWriter(false, function (s) {
      code += s + "\n";
    });

    var emitter = new Emitter(writer, false, debugInfo, false);

    var compiledMethods: CompiledMethodInfo [] = [];
    var classInfoList: ClassInfo [] = [];

    Object.keys(jarFiles).every(function (path) {
      if (path.substr(-4) !== ".jar" || !jarFilter(path)) {
        return true;
      }
      var zipFile = jarFiles[path];
      Object.keys(zipFile.directory).every(function (fileName) {
        if (fileName.substr(-6) !== '.class') {
          return true;
        }
        try {
          var className = fileName.substring(0, fileName.length - 6);
          var classInfo = CLASSES.getClass(className);
          if (classInfo.sourceFile && !classInfo.sourceFile.match(fileFilter)) {
            return true;
          }
          if (!classFilter(classInfo)) {
            return true;
          }
          classInfoList.push(classInfo);
        } catch (e) {
          stderrWriter.writeLn(e + ": " + e.stack);
        }
        return true;
      }.bind(this));
      return true;
    }.bind(this));

    var orderedClassInfoList: ClassInfo [] = [];

    function hasDependencies(list, classInfo) {
      var superClass = classInfo.superClass;
      var interfaces = classInfo.getAllInterfaces();
      if (!superClass && interfaces.length === 0) {
        return false;
      }
      for (var i = 0; i < list.length; i++) {
        if (list[i].getClassNameSlow() === superClass.getClassNameSlow()) {
          return true;
        }
      }

      for (var j = 0; j < interfaces; j++) {
        for (var i = 0; i < list.length; i++) {
          if (list[i].getClassNameSlow() === interfaces[j].getClassNameSlow()) {
            return true;
          }
        }
      }

      return false;
    }
    while (classInfoList.length) {
      for (var i = 0; i < classInfoList.length; i++) {
        var classInfo = classInfoList[i];
        if (!hasDependencies(classInfoList, classInfo)) {
          orderedClassInfoList.push(classInfo);
          classInfoList.splice(i--, 1);
          break;
        }
      }
    }

    var filteredClassInfoList: ClassInfo [] = [];
    for (var i = 0; i < orderedClassInfoList.length; i++) {
      var classInfo = orderedClassInfoList[i];
      var methods = classInfo.getMethods();
      for (var j = 0; j < methods.length; j++) {
        var method = methods[j];
        if (methodFilterList === null || methodFilterList.indexOf(method.implKey) >= 0) {
          // If at least one method is found, compile the class.
          filteredClassInfoList.push(classInfo);
          break;
        }
      }
    }

    for (var i = 0; i < filteredClassInfoList.length; i++) {
      var classInfo = filteredClassInfoList[i];

      if (emitter.debugInfo) {
        writer.writeLn("// " + classInfo.getClassNameSlow() + (classInfo.superClass ? " extends " + classInfo.superClass.getClassNameSlow() : ""));
      }
      // Don't compile interfaces.
      if (classInfo.isInterface) {
        continue;
      }
      ArrayUtilities.pushMany(compiledMethods, compileClassInfo(emitter, classInfo, methodFilterList, ctx));
    }

    var color = failedCompilations ? IndentingWriter.YELLOW : IndentingWriter.GREEN;
    stderrWriter.colorLn(color, "Compiled " + compiledMethods.length + " methods OK, " + failedCompilations + " failed.");

    stdoutWriter.writeLn(code);

    stdoutWriter.enter("/*");
    baselineCounter && baselineCounter.traceSorted(stdoutWriter);
    yieldCounter && yieldCounter.traceSorted(stdoutWriter);
    yieldGraph && traceYieldGraph(stdoutWriter);
    stdoutWriter.enter("*/");
    // yieldCounter.traceSorted(stdoutWriter);
  }
}


































