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
    console.info("loading " + fileName + " ...");
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
        if (ACCESS_FLAGS.isPublic(method.access_flags) &&
            ACCESS_FLAGS.isStatic(method.access_flags) &&
            !ACCESS_FLAGS.isNative(method.access_flags) &&
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

Classes.prototype.getField = function(classInfo, fieldName, signature, staticFlag) {
    do {
        var fields = classInfo.fields;
        for (var i=0; i<fields.length; ++i) {
            var field = fields[i];
            if (ACCESS_FLAGS.isStatic(field.access_flags) === !!staticFlag) {
                if (field.name === fieldName && field.signature === signature)
                    return field;
            }
        }
        if (staticFlag) {
            for (var n = 0; n < classInfo.interfaces.length; ++n) {
                var field = this.getField(classInfo.interfaces[n], fieldName, signature, staticFlag);
                if (field)
                    return field;
            }
        }
        classInfo = classInfo.superClass;
    } while (classInfo);
};

Classes.prototype.getMethod = function(classInfo, methodName, signature, staticFlag, inheritFlag) {
    var c = classInfo;
    do {
        var methods = c.methods;
        for (var i=0; i<methods.length; ++i) {
            var method = methods[i];
            if (ACCESS_FLAGS.isStatic(method.access_flags) === !!staticFlag) {
                if (method.name === methodName && method.signature === signature)
                    return method;
            }
        }
        c = c.superClass;
    } while (c);
    if (ACCESS_FLAGS.isInterface(classInfo.access_flags)) {
        for (var n = 0; n < classInfo.interfaces.length; ++n) {
            var method = this.getMethod(classInfo.interfaces[n], methodName, signature, staticFlag, inheritFlag);
            if (method)
                return method;
        }
    }
};
