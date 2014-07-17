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
    classInfo.staticFields = {};
    this.classes[classInfo.className] = classInfo;
    return classInfo;
}

Classes.prototype.loadClassFile = function(fileName) {
    console.info("loading " + fileName + " ...");
    var bytes = this.loadFile(fileName);
    if (!bytes)
        return null;
    var classInfo = this.loadClassBytes(bytes);
    var classes = classInfo.classes;
    for (var i=0; i<classes.length; i++) {
        if (!this.classes[classes[i]]) {
            this.loadClassFile(fileName.replace(/[^/]*\.class$/, "") + "/" + classes[i] + ".class");
        }
    }
    return classInfo;
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

Classes.prototype.initClass = function(caller, classInfo) {
    var clinit = this.getMethod(caller, classInfo, "<clinit>", "()V", true, false);
    if (clinit)
        caller.invoke(OPCODES.invokestatic, clinit);
    classInfo.constructor = function () {
    }
    classInfo.constructor.prototype.class = classInfo;
}

Classes.prototype.getClass = function(caller, className) {
    var classInfo = this.classes[className];
    if (classInfo)
        return classInfo;
    if (className[0] === "[")
        return this.getArrayClass(caller, className);
    if (!!(classInfo = this.loadClassFile(className + ".class"))) {
        this.initClass(caller, classInfo);
        return classInfo;
    }
    return null;
};

Classes.prototype.getArrayClass = function(caller, typeName) {
    var elementType = typeName.substr(1);
    if (elementType in ARRAY_TYPE)
        return this.initPrimitiveArrayType(elementType, ARRAY_TYPE[elementType]);
    var classInfo = new ArrayClass(elementType);
    classInfo.constructor = function (size) {
        var array = new Array(size);
        array.class = classInfo;
        return array;
    }
    return classInfo;
}

Classes.prototype.initPrimitiveArrayType = function(elementType, constructor) {
    var classInfo = new ArrayClass(elementType);
    constructor.prototype.class = classInfo;
    classInfo.constructor = constructor;
    return classInfo;
}

Classes.prototype.getStaticField = function(caller, className, fieldName) {
    return this.getClass(caller, className).staticFields[fieldName];
}

Classes.prototype.setStaticField = function(caller, className, fieldName, value) {
    this.getClass(caller, className).staticFields[fieldName] = value;
}

Classes.prototype.getMethod = function(caller, classInfo, methodName, signature, staticFlag, inheritFlag) {
    // console.log(classInfo.className, methodName, signature);
    while (true) {
        var methods = classInfo.methods;
        for (var i=0; i<methods.length; i++) {
            if (ACCESS_FLAGS.isStatic(methods[i].access_flags) === !!staticFlag) {
                if (methods[i].name === methodName && methods[i].signature === signature) {
                    return methods[i];
                }
            }
        }
        var superClassName = classInfo.superClassName;
        if (!superClassName)
            return null;
        classInfo = this.getClass(caller, superClassName);
    }
};

Classes.prototype.newObject = function(caller, className) {
    return new (this.getClass(caller, className).constructor)();
}

Classes.prototype.newPrimitiveArray = function(constructor, size) {
    return constructor.call(null, size);
}

Classes.prototype.newArray = function(caller, typeName, size) {
    return this.getArrayClass(caller, typeName).constructor.call(null, size);
}

Classes.prototype.newMultiArray = function(caller, typeName, lengths) {
    var length = lengths[0];
    var array = this.newArray(caller, typeName, length);
    if (lengths.length > 0) {
        lengths = lengths.slice(1);
        for (var i=0; i<length; i++)
            array[i] = this.newMultiArray(caller, typeName.substr(1), lengths);
    }
    return array;
}

Classes.prototype.newString = function(caller, s) {
    var obj = this.newObject(caller, "java/lang/String");
    var length = s.length;
    var chars = this.newPrimitiveArray(Uint16Array, length);
    for (var n = 0; n < length; ++n)
        chars[n] = s.charCodeAt(n);
    obj.value = chars;
    obj.offset = 0;
    obj.count = length;
    return obj;
}
