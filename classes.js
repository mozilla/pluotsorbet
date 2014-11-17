/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var Classes = function() {
    if (this instanceof Classes) {
        this.classfiles = [];
        this.mainclass = [];
        this.classes = {};
    } else  {
        return new Classes();
    }
}

Classes.ClassNotFoundException = function(message) {
    this.message = message;
};
Classes.ClassNotFoundException.prototype = Object.create(Error.prototype);
Classes.ClassNotFoundException.prototype.name = "ClassNotFoundException";
Classes.ClassNotFoundException.prototype.constructor = Classes.ClassNotFoundException;

Classes.prototype.addPath = function(name, data) {
    if (name.substr(-4) === ".jar") {
        data = new ZipFile(data);
    }
    this.classfiles[name] = data;
}

Classes.prototype.loadFileFromJar = function(jar, fileName) {
    var classfiles = this.classfiles;
    var zip = classfiles[jar];
    if (!zip)
        return null;
    if (!(fileName in zip.directory))
        return null;
    var bytes = zip.read(fileName);
    return bytes.buffer.slice(0, bytes.length);
}

Classes.prototype.loadFile = function(fileName) {
    var classfiles = this.classfiles;
    var data = classfiles[fileName];
    if (data)
        return data;
    Object.keys(classfiles).every(function (name) {
        if (name.substr(-4) !== ".jar")
            return true;
        var zip = classfiles[name];
        if (fileName in zip.directory) {
            var bytes = zip.read(fileName);
            data = bytes.buffer.slice(0, bytes.length);
        }
        return !data;
    });
    if (data)
        classfiles[fileName] = data;
    return data;
}

Classes.prototype.loadClassBytes = function(bytes) {
    var classInfo = new ClassInfo(bytes);
    this.classes[classInfo.className] = classInfo;
    return classInfo;
}

Classes.prototype.loadClassFile = function(fileName) {
    var bytes = this.loadFile(fileName);
    if (!bytes)
        throw new (Classes.ClassNotFoundException)(fileName);
    var self = this;
    var classInfo = this.loadClassBytes(bytes);
    if (classInfo.superClassName)
        classInfo.superClass = this.loadClass(classInfo.superClassName);
    var classes = classInfo.classes;
    classes.forEach(function (c, n) {
        classes[n] = self.loadClass(c);
    });
    return classInfo;
}

Classes.prototype.loadClass = function(className) {
    var classInfo = this.classes[className];
    if (classInfo)
        return classInfo;
    return this.loadClassFile(className + ".class");
}

Classes.prototype.getEntryPoint = function(classInfo) {
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

Classes.prototype.getClass = function(className) {
    var classInfo = this.classes[className];
    if (!classInfo) {
        if (className[0] === "[") {
            classInfo = this.initArrayClass(className);
        } else {
            classInfo = this.loadClass(className);
        }
        if (!classInfo)
            return null;
    }
    return classInfo;
};

Classes.prototype.initArrayClass = function(typeName) {
    var elementType = typeName.substr(1);
    var constructor = ARRAYS[elementType];
    if (constructor)
        return this.classes[typeName] = this.initPrimitiveArrayType(typeName, constructor);
    if (elementType[0] === "L")
        elementType = elementType.substr(1).replace(";", "");
    var classInfo = new ArrayClass(typeName, this.getClass(elementType));
    classInfo.superClass = this.java_lang_Object;
    classInfo.constructor = function (size) {
        var array = new Array(size);
        array.class = classInfo;
        return array;
    }
    return this.classes[typeName] = classInfo;
}

Classes.prototype.initPrimitiveArrayType = function(typeName, constructor) {
    var classInfo = new ArrayClass(typeName);
    classInfo.superClass = this.java_lang_Object;
    constructor.prototype.class = classInfo;
    classInfo.constructor = constructor;
    return classInfo;
}

Classes.prototype.getField = function(classInfo, fieldKey) {
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
};

Classes.prototype.getMethod = function(classInfo, methodKey) {
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
};


Classes.prototype.compileAll = function(runtime) {
  var all = 'var PRECOMPILED = {};';
  var classfiles = this.classfiles;
  var methodsCompiled = 0;
  var methodsFailed = 0;
  var methodsFailedHard = 0;
  //var dependencies = new J2ME.Dependencies();
  var dependencies = null;
  var ctx = new Context(runtime);

  var code = "";
  var writer = new J2ME.IndentingWriter(false, function (s) {
    code += s + "\n";
  });

  writer.enter("var code = {");
  Object.keys(classfiles).every(function (name) {
    if (name.substr(-4) !== ".jar") {
      return true;
    }
    var zip = classfiles[name];
    var kk = 0;

    writer.enter(J2ME.quote(name) + ": {");
    Object.keys(zip.directory).every(function (fileName) {
      if (fileName.substr(-6) !== '.class') {
        return true;
      }
      writer.enter(J2ME.quote(fileName) + ": {");
      J2ME.compileClassInfo(writer, this.loadClassFile(fileName), ctx);
      writer.leave("},");
      return true;
    }.bind(this));

    writer.leave("},");
    return true;
  }.bind(this));
  writer.leave("}");

  //var dependenciesJSON = JSON.stringify(dependencies);
  //all += "PRECOMPILED.dependencies = " + dependenciesJSON;
  //console.log("Number of methods compiled: " + methodsCompiled);
  //console.log("Number of methods failed: " + methodsFailed);
  //console.log("Number of methods hard fail: " + methodsFailedHard);
  // var bl = new Blob([all], {type : 'text/plain'});
  // console.log(URL.createObjectURL(bl));
  console.log(code);
};
