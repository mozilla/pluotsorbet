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
    LOG.debug("loading " + fileName + " ...");
    var bytes = this.loadFile(fileName);
    if (!bytes)
        return null;
    var classInfo = this.loadClassBytes(bytes);
    var classes = classInfo.classes;
    for (var i=0; i<classes.length; i++) {
        if (!this.classes[classes[i]]) {
            this.loadClassFile(path.dirname(fileName) + path.sep + classes[i] + ".class");
        }
    }
    return classInfo;
}

Classes.prototype.getEntryPoint = function(className, methodName) {
    for(var name in this.classes) {
        var classInfo = this.classes[name];
        if (classInfo instanceof ClassInfo) {
            if (!className || (className === classInfo.className)) {
                if (ACCESS_FLAGS.isPublic(classInfo.access_flags)) {
                    var methods = classInfo.methods;
                    for (var i=0; i<methods.length; i++) {
                        if (ACCESS_FLAGS.isPublic(methods[i].access_flags) &&
                            ACCESS_FLAGS.isStatic(methods[i].access_flags) &&
                            !ACCESS_FLAGS.isNative(methods[i].access_flags) &&
                            methods[i].name === methodName) {
                            return methods[i];
                        }
                    }
                }
            }
        }
    }
}

Classes.prototype.getClass = function(caller, className) {
    var classInfo = this.classes[className];
    if (classInfo)
        return classInfo;
    if (className[0] === "[")
        return this.getArrayClass(caller, className);
    if (!!(classInfo = this.loadClassFile(className + ".class"))) {
        classInfo.staticFields = {};
        var clinit = this.getMethod(caller, className, "<clinit>", "()V", true);
        if (clinit)
            caller.invoke(clinit);
        return classInfo;
    }
    throw new Error("Implementation of class '" + className + "' not found.");
};

Classes.prototype.getArrayClass = function(caller, typeName) {
    var elementType = typeName.substr(1);
    switch (elementType) {
    case 'Z': return this.initPrimitiveArrayType(elementType, Uint8Array);
    case 'C': return this.initPrimitiveArrayType(elementType, Uint16Array);
    case 'B': return this.initPrimitiveArrayType(elementType, Int8Array);
    case 'S': return this.initPrimitiveArrayType(elementType, Int16Array);
    case 'I': return this.initPrimitiveArrayType(elementType, Int32Array);
    case 'F': return this.initPrimitiveArrayType(elementType, Float32Array);
    case 'D': return this.initPrimitiveArrayType(elementType, Float64Array);
    }
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

Classes.prototype.getMethod = function(caller, className, methodName, signature, staticFlag) {
    console.log(className, methodName, signature);
    // Only force initialization when accessing a static method.
    var classInfo = this.getClass(caller, className);
    var methods = classInfo.methods;
    for (var i=0; i<methods.length; i++) {
        if (ACCESS_FLAGS.isStatic(methods[i].access_flags) === !!staticFlag) {
            if (methods[i].name === methodName && methods[i].signature === signature) {
                return methods[i];
            }
        }
    }
    return null;
};

Classes.prototype.newObject = function(caller, className) {
    return { class: this.getClass(caller, className) };
}

Classes.prototype.newArray = function(type, size) {
    switch (type) {
    case ARRAY_TYPE.T_BOOLEAN: return new Uint8Array(size);
    case ARRAY_TYPE.T_CHAR: return new Uint16Array(size);
    case ARRAY_TYPE.T_FLOAT: return new Float32Array(size);
    case ARRAY_TYPE.T_DOUBLE: return new Float64Array(size);
    case ARRAY_TYPE.T_BYTE: return new Int8Array(size);
    case ARRAY_TYPE.T_SHORT: return new Int16Array(size);
    case ARRAY_TYPE.T_INT: return new Int32Array(size);
    case ARRAY_TYPE.T_LONG: return new Int64Array(size);
    }
}

Classes.prototype.newString = function(caller, s) {
    var obj = this.newObject(caller, "java/lang/String");
    var length = s.length;
    var chars = this.newArray(ARRAY_TYPE.T_CHAR, length);
    for (var n = 0; n < length; ++n)
        chars[n] = s.charCodeAt(n);
    obj.value = chars;
    obj.offset = 0;
    obj.count = length;
    return obj;
}
