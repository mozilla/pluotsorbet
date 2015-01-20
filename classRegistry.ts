module J2ME {
  declare var ZipFile;
  declare var snarf;

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

    jarFiles: Map<string, any>;
    classFiles: Map<string, any>;
    classes: Map<string, ClassInfo>;

    preInitializedClasses: ClassInfo [];

    java_lang_Object: ClassInfo;
    java_lang_Class: ClassInfo;
    java_lang_String: ClassInfo;
    java_lang_Thread: ClassInfo;

    constructor() {
      this.sourceDirectories = [];
      this.sourceFiles = Object.create(null);
      this.missingSourceFiles = Object.create(null);

      this.jarFiles = Object.create(null);
      this.classFiles = Object.create(null);
      this.classes = Object.create(null);
      this.preInitializedClasses = [];
    }

    initializeBuiltinClasses() {
      // These classes are guaranteed to not have a static initializer.
      enterTimeline("initializeBuiltinClasses");
      this.java_lang_Object = this.loadAndLinkClass("java/lang/Object");
      this.java_lang_Class = this.loadAndLinkClass("java/lang/Class");
      this.java_lang_String = this.loadAndLinkClass("java/lang/String");
      this.java_lang_Thread = this.loadAndLinkClass("java/lang/Thread");

      this.preInitializedClasses.push(this.java_lang_Object);
      this.preInitializedClasses.push(this.java_lang_Class);
      this.preInitializedClasses.push(this.java_lang_String);
      this.preInitializedClasses.push(this.java_lang_Thread);

      /**
       * Force these frequently used classes to be initialized eagerly. We can
       * skip the class initialization check for them. This is only possible
       * because they don't have any static state.
       */
      var classNames = [
        "java/lang/Integer",
        "java/lang/Character",
        "java/lang/Math",
        "java/util/HashtableEntry",
        "java/lang/StringBuffer",
        "java/util/Vector",
        "java/io/IOException",
        "java/lang/IllegalArgumentException"
      ];

      for (var i = 0; i < classNames.length; i++) {
        this.preInitializedClasses.push(this.loadAndLinkClass(classNames[i]));
      }

      // Link primitive values and primitive arrays.
      for (var i = 0; i < "ZCFDBSIJ".length; i++) {
        var typeName = "ZCFDBSIJ"[i];
        linkKlass(PrimitiveClassInfo[typeName]);
        this.getClass("[" + typeName);
      }
      leaveTimeline("initializeBuiltinClasses");
    }

    isPreInitializedClass(classInfo: ClassInfo) {
      return this.preInitializedClasses.indexOf(classInfo) >= 0;
    }

    addPath(name: string, buffer: ArrayBuffer) {
      if (name.substr(-4) === ".jar") {
        this.jarFiles[name] = new ZipFile(buffer);
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
            //stderrWriter.writeLn("" + x);
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
      var zip = this.jarFiles[jarName];
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
      if (data) {
        return data;
      }
      var jarFiles = this.jarFiles;
      for (var k in jarFiles) {
        var zip = jarFiles[k];
        if (fileName in zip.directory) {
          enterTimeline("ZIP", {file: fileName});
          var bytes = zip.read(fileName);
          data = bytes.buffer.slice(bytes.byteOffset, bytes.byteOffset + bytes.byteLength);
          leaveTimeline("ZIP");
          break;
        }
      }
      if (data) {
        classFiles[fileName] = data;
      }
      return data;
    }

    loadClassBytes(bytes: ArrayBuffer): ClassInfo {
      enterTimeline("loadClassBytes");
      var classInfo = new ClassInfo(bytes);
      leaveTimeline("loadClassBytes", {className: classInfo.className});
      this.classes[classInfo.className] = classInfo;
      return classInfo;
    }

    loadClassFile(fileName: string): ClassInfo {
      loadWriter && loadWriter.enter("> Loading Class File: " + fileName);
      var bytes = this.loadFile(fileName);
      if (!bytes) {
        loadWriter && loadWriter.leave("< ClassNotFoundException");
        throw new (ClassNotFoundException)(fileName);
      }
      var self = this;
      var classInfo = this.loadClassBytes(bytes);
      if (classInfo.superClassName) {
        classInfo.superClass = this.loadClass(classInfo.superClassName);
        classInfo.superClass.subClasses.push(classInfo);
      }
      var classes = classInfo.classes;
      classes.forEach(function (c, n) {
        classes[n] = self.loadClass(c);
      });
      classInfo.complete();
      loadWriter && loadWriter.leave("<");
      return classInfo;
    }

    /**
     * Used to test loading of all class files.
     */
    loadAllClassFiles() {
      var jarFiles = this.jarFiles;
      for (var k in jarFiles) {
        var zip = jarFiles[k];
        for (var fileName in zip.directory) {
          if (fileName.substr(-6) === ".class") {
            this.loadClassFile(fileName);
          }
        }
      }
    }

    loadClass(className: string): ClassInfo {
      var classInfo = this.classes[className];
      if (classInfo) {
        return classInfo;
      }
      return this.loadClassFile(className + ".class");
    }

    loadAndLinkClass(className: string): ClassInfo {
      var classInfo = this.loadClass(className);
      linkKlass(classInfo);
      return classInfo;
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
      var elementType = typeName.substr(1);
      var constructor = getArrayConstructor(elementType);
      var classInfo;
      if (constructor) {
        classInfo = PrimitiveArrayClassInfo[elementType];
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
            field.key = (AccessFlags.isStatic(field.access_flags) ? "S" : "I") + "." + field.name + "." + field.signature;
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
  
      if (AccessFlags.isInterface(classInfo.access_flags)) {
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

