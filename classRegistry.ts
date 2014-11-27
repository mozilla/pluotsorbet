//
//var Classes = function() {
//  if (this instanceof Classes) {
//    this.classFiles = [];
//    this.mainclass = [];
//    this.classes = {};
//  } else  {
//    return new Classes();
//  }
//}
module J2ME {
  declare var ZipFile;
  declare var ACCESS_FLAGS;
  declare var snarf;


  /**
   * To unify all class name handling, we give primitive type names valid
   * class names (which hopefully can't be created from source, if they can
   * we'll have to mangle them.
   */
  function convertPrimitiveArrayTypeName(typeName: string): string {
    switch (typeName) {
      case "[Z": return "[boolean";
      case "[B": return "[byte";
      case "[C": return "[char";
      case "[D": return "[double";
      case "[F": return "[float";
      case "[I": return "[int";
      case "[J": return "[long";
      case "[S": return "[short";
      default:
        return typeName;
    }
  }

  export class ClassRegistry {
    /**
     * List of directories to look for source files in.
     */
    sourceDirectories: string [];

    /**
     * All source code, only ever used for debugging.
     */
    sourceFiles: Map<string, string []>;

    /**
     * List of classes whose sources files were not found. We keep track
     * of them so we don't have to search for them over and over.
     */
    missingSourceFiles: Map<string, string []>;

    classFiles: Map<string, any>;
    classes: Map<string, ClassInfo>;

    java_lang_Object: ClassInfo;
    java_lang_Class: ClassInfo;
    java_lang_String: ClassInfo;
    java_lang_Thread: ClassInfo;

    booleanArray: ClassInfo;
    byteArray: ClassInfo;
    charArray: ClassInfo;
    doubleArray: ClassInfo;
    floatArray: ClassInfo;
    intArray: ClassInfo;
    longArray: ClassInfo;
    shortArray: ClassInfo;

    boolean$: ClassInfo;
    byte$: ClassInfo;
    char$: ClassInfo;
    double$: ClassInfo;
    float$: ClassInfo;
    int$: ClassInfo;
    long$: ClassInfo;
    short$: ClassInfo;

    constructor() {
      this.sourceDirectories = [];
      this.sourceFiles = Object.create(null);
      this.missingSourceFiles = Object.create(null);

      this.classFiles = Object.create(null);
      this.classes = Object.create(null);
    }

    initializeBuiltinClasses() {
      // These classes are guaranteed to not have a static initializer.


      this.java_lang_Object = this.loadClass("java/lang/Object");
      this.java_lang_Class = this.loadClass("java/lang/Class");
      this.java_lang_String = this.loadClass("java/lang/String");
      this.java_lang_Thread = this.loadClass("java/lang/Thread");

      for (var i = 0; i < valueKinds.length; i++) {
        var typeName = Kind[valueKinds[i]].toLowerCase();
        this.classes[typeName] = new PrimitiveClassInfo(typeName);
        linkKlass(this.classes[typeName]);
      }

      this.boolean$ = this.getClass("boolean");
      this.byte$ = this.getClass("byte");
      this.char$ = this.getClass("char");
      this.double$ = this.getClass("double");
      this.float$ = this.getClass("float");
      this.int$ = this.getClass("int");
      this.long$ = this.getClass("long");
      this.short$ = this.getClass("short");

      this.booleanArray = this.getClass("[boolean");
      this.byteArray = this.getClass("[byte");
      this.charArray = this.getClass("[char");
      this.doubleArray = this.getClass("[double");
      this.floatArray = this.getClass("[float");
      this.intArray = this.getClass("[int");
      this.longArray = this.getClass("[long");
      this.shortArray = this.getClass("[short");
    }

    addPath(name: string, buffer: ArrayBuffer) {
      if (name.substr(-4) === ".jar") {
        this.classFiles[name] = new ZipFile(buffer);
      } else {
        this.classFiles[name] = buffer;  
      }
    }

    addSourceDirectory(name: string) {
      this.sourceDirectories.push(name);
    }

    getSourceLine(sourceLocation: SourceLocation): string {
      if (typeof snarf === "undefined") {
        // Sorry, no snarf in the browser. Do async loading instead.
        return null;
      }
      var source = this.sourceFiles[sourceLocation.className];
      if (!source && !this.missingSourceFiles[sourceLocation.className]) {
        for (var i = 0; i < this.sourceDirectories.length; i++) {
          try {
            var path = this.sourceDirectories[i] + "/" + sourceLocation.className + ".java";
            var file = snarf(path);
            if (file) {
              source = this.sourceFiles[sourceLocation.className] = file.split("\n");
            }
          } catch (x) {
            // Keep looking.
            consoleWriter.writeLn("" + x);
          }
        }
      }
      if (source) {
        return source[sourceLocation.lineNumber - 1];
      }
      this.missingSourceFiles[sourceLocation.className] = true;
      return null;
    }

    loadFileFromJar(jarName: string, fileName: string): ArrayBuffer {
      var classFiles = this.classFiles;
      var zip = classFiles[jarName];
      if (!zip)
        return null;
      if (!(fileName in zip.directory))
        return null;
      var bytes = zip.read(fileName);
      return bytes.buffer.slice(bytes.byteOffset, bytes.byteOffset + bytes.byteLength);
    }

    loadFile(fileName: string): ArrayBuffer {
      var classFiles = this.classFiles;
      var data = classFiles[fileName];
      if (data)
        return data;
      Object.keys(classFiles).every(function (name) {
        if (name.substr(-4) !== ".jar")
          return true;
        var zip = classFiles[name];
        if (fileName in zip.directory) {
          var bytes = zip.read(fileName);
          data = bytes.buffer.slice(bytes.byteOffset, bytes.byteOffset + bytes.byteLength);
        }
        return !data;
      });
      if (data)
        classFiles[fileName] = data;
      return data;
    }

    loadClassBytes(bytes: ArrayBuffer): ClassInfo {
      var classInfo = new ClassInfo(bytes);
      this.classes[classInfo.className] = classInfo;
      return classInfo;
    }

    loadClassFile(fileName: string): ClassInfo {
      var bytes = this.loadFile(fileName);
      if (!bytes)
        throw new (ClassNotFoundException)(fileName);
      var self = this;
      var classInfo = this.loadClassBytes(bytes);
      if (classInfo.superClassName)
        classInfo.superClass = this.loadClass(classInfo.superClassName);
      var classes = classInfo.classes;
      classes.forEach(function (c, n) {
        classes[n] = self.loadClass(c);
      });
      if (J2ME.phase === J2ME.ExecutionPhase.Runtime) {
        J2ME.linkKlass(classInfo);
      }
      return classInfo;
    }

    loadClass (className: string): ClassInfo {
      var classInfo = this.classes[className];
      if (classInfo)
        return classInfo;
      return this.loadClassFile(className + ".class");
    }

    getEntryPoint(classInfo: ClassInfo): MethodInfo {
      var methods = classInfo.methods;
      for (var i=0; i<methods.length; i++) {
        var method = methods[i];
        if (method.isPublic && method.isStatic && !method.isNative &&
        method.name === "main" &&
        method.signature === "([Ljava/lang/String;)V") {
          return method;
        }
      }
    }

    getClass(className: string): ClassInfo {
      var classInfo = this.classes[className];
      if (!classInfo) {
        if (className[0] === "[") {
          classInfo = this.createArrayClass(className);
        } else {
          classInfo = this.loadClass(className);
        }
        if (!classInfo)
          return null;
      }
      return classInfo;
    }

    createArrayClass(typeName: string): ArrayClassInfo {
      typeName = convertPrimitiveArrayTypeName(typeName);
      var elementType = typeName.substr(1);
      var constructor = getArrayConstructor(elementType);
      var classInfo;
      if (constructor) {
        classInfo = new ArrayClassInfo(typeName, this.getClass(elementType));
      } else {
        if (elementType[0] === "L") {
          elementType = elementType.substr(1).replace(";", "");
        }
        classInfo = new ArrayClassInfo(typeName, this.getClass(elementType));
      }
      if (J2ME.phase === J2ME.ExecutionPhase.Runtime) {
        J2ME.linkKlass(classInfo);
      }
      return this.classes[typeName] = classInfo;
    }


    getField(classInfo, fieldKey) {
      if (classInfo.vfc[fieldKey]) {
        return classInfo.vfc[fieldKey];
      }
  
      do {
        var fields = classInfo.fields;
        for (var i=0; i<fields.length; ++i) {
          var field = fields[i];
          if (!field.key) {
            field.key = (ACCESS_FLAGS.isStatic(field.access_flags) ? "S" : "I") + "." + field.name + "." + field.signature;
          }
          if (field.key === fieldKey) {
            return classInfo.vfc[fieldKey] = field;
          }
        }
  
        if (fieldKey[0] === 'S') {
          for (var n = 0; n < classInfo.interfaces.length; ++n) {
            var field = this.getField(classInfo.interfaces[n], fieldKey);
            if (field) {
              return classInfo.vfc[fieldKey] = field;
            }
          }
        }
  
        classInfo = classInfo.superClass;
      } while (classInfo);
    }

    getMethod(classInfo, methodKey) {
      var c = classInfo;
      do {
        var methods = c.methods;
        for (var i=0; i<methods.length; ++i) {
          var method = methods[i];
          if (method.key === methodKey) {
            return classInfo.vmc[methodKey] = method;
          }
        }
        c = c.superClass;
      } while (c);
  
      if (ACCESS_FLAGS.isInterface(classInfo.access_flags)) {
        for (var n = 0; n < classInfo.interfaces.length; ++n) {
          var method = this.getMethod(classInfo.interfaces[n], methodKey);
          if (method) {
            return classInfo.vmc[methodKey] = method;
          }
        }
      }
    }

  }

  export var ClassNotFoundException = function(message) {
    this.message = message;
  };

  ClassNotFoundException.prototype = Object.create(Error.prototype);
  ClassNotFoundException.prototype.name = "ClassNotFoundException";
  ClassNotFoundException.prototype.constructor = ClassNotFoundException;
}

