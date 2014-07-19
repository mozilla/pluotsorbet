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

Classes.prototype.addPath = function(name, data) {
    if (name.substr(-4) === ".jar") {
        data = new ZipFile(data);
    }
    this.classfiles[name] = data;
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
        throw Error("File " + fileName + " not found.");
    var self = this;
    var classInfo = this.loadClassBytes(bytes);
    if (classInfo.superClassName)
        classInfo.superClass = this.loadClass(classInfo.superClassName);
    var interfaces = classInfo.interfaces;
    interfaces.forEach(function (i, n) {
        interfaces[n] = self.loadClass(i);
    });
    var classes = classInfo.classes;
    classes.forEach(function (c, n) {
        classes[n] = self.loadClass(util.withPath(fileName, c));
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
    if (ACCESS_FLAGS.isPublic(classInfo.access_flags)) {
        var methods = classInfo.methods;
        for (var i=0; i<methods.length; i++) {
            if (ACCESS_FLAGS.isPublic(methods[i].access_flags) &&
                ACCESS_FLAGS.isStatic(methods[i].access_flags) &&
                !ACCESS_FLAGS.isNative(methods[i].access_flags) &&
                methods[i].name === "main" &&
                methods[i].signature === "([Ljava/lang/String;)V") {
                return methods[i];
            }
        }
    }
}

Classes.prototype.initClass = function(classInfo) {
    if (classInfo.staticFields)
        return;
    if (classInfo.superClass)
        this.initClass(classInfo.superClass);
    classInfo.staticFields = {};
    classInfo.constructor = function () {
    }
    classInfo.constructor.prototype.class = classInfo;
    var clinit = this.getMethod(classInfo, "<clinit>", "()V", true, false);
    if (clinit) {
        VM.invoke(clinit);
    }
}

Classes.prototype.getClass = function(className, init) {
    var classInfo = this.classes[className];
    if (!classInfo) {
        if (className[0] === "[") {
            classInfo = this.getArrayClass(className);
        } else {
            classInfo = this.loadClass(className);
        }
        if (!classInfo)
            return null;
    }
    if (init)
        this.initClass(classInfo);
    return classInfo;
};

Classes.prototype.getArrayClass = function(typeName) {
    var elementType = typeName.substr(1);
    var constructor = ARRAYS[elementType];
    if (constructor)
        return this.initPrimitiveArrayType(elementType, constructor);
    if (elementType[0] === "L")
        elementType = elementType.substr(1).replace(/;$/, "");
    var classInfo = new ArrayClass(typeName, this.getClass(elementType));
    classInfo.superClass = this.loadClass("java/lang/Object");
    classInfo.constructor = function (size) {
        var array = new Array(size);
        array.class = classInfo;
        return array;
    }
    return this.classes[typeName] = classInfo;
}

Classes.prototype.initPrimitiveArrayType = function(elementType, constructor) {
    var classInfo = new ArrayClass("[" + elementType);
    classInfo.superClass = this.loadClass("java/lang/Object");
    constructor.prototype.class = classInfo;
    classInfo.constructor = constructor;
    return classInfo;
}

Classes.prototype.getStaticField = function(className, fieldName) {
    return this.getClass(className, true).staticFields[fieldName];
}

Classes.prototype.setStaticField = function(className, fieldName, value) {
    this.getClass(className, true).staticFields[fieldName] = value;
}

Classes.prototype.getMethod = function(classInfo, methodName, signature, staticFlag, inheritFlag) {
    do {
        var methods = classInfo.methods;
        for (var i=0; i<methods.length; i++) {
            if (ACCESS_FLAGS.isStatic(methods[i].access_flags) === !!staticFlag) {
                if (methods[i].name === methodName && methods[i].signature === signature) {
                    return methods[i];
                }
            }
        }
        classInfo = classInfo.superClass;
    } while (classInfo);
};

Classes.prototype.newObject = function(className) {
    return new (this.getClass(className, true).constructor)();
}

Classes.prototype.newPrimitiveArray = function(type, size) {
    var constructor = ARRAYS[type];
    if (!constructor.prototype.class)
        this.initPrimitiveArrayType(type, constructor);
    return constructor.call(null, size);
}

Classes.prototype.newArray = function(typeName, size) {
    return this.getArrayClass(typeName).constructor.call(null, size);
}

Classes.prototype.newMultiArray = function(typeName, lengths) {
    var length = lengths[0];
    var array = this.newArray(typeName, length);
    if (lengths.length > 1) {
        lengths = lengths.slice(1);
        for (var i=0; i<length; i++)
            array[i] = this.newMultiArray(typeName.substr(1), lengths);
    }
    return array;
}

Classes.prototype.newString = function(s) {
    var obj = this.newObject("java/lang/String");
    var length = s.length;
    var chars = this.newPrimitiveArray("C", length);
    for (var n = 0; n < length; ++n)
        chars[n] = s.charCodeAt(n);
    obj.value = chars;
    obj.offset = 0;
    obj.count = length;
    return obj;
}
