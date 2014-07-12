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
    var classData = new ClassData(bytes);
    this.classes[classData.getClassName()] = classData;
    return classData;
}

Classes.prototype.loadClassFile = function(fileName) {
    LOG.debug("loading " + fileName + " ...");
    var bytes = this.loadFile(fileName);
    if (!bytes)
        return null;
    var classData = this.loadClassBytes(bytes);
    var classes = classData.getClasses();
    for (var i=0; i<classes.length; i++) {
        if (!this.classes[classes[i]]) {
            this.loadClassFile(path.dirname(fileName) + path.sep + classes[i] + ".class");
        }
    }
    return classData;
}

Classes.prototype.getEntryPoint = function(className, methodName) {
    for(var name in this.classes) {
        var classData = this.classes[name];
        if (classData instanceof ClassData) {
            if (!className || (className === classData.getClassName())) {
                if (ACCESS_FLAGS.isPublic(classData.getAccessFlags())) {
                    var methods = classData.getMethods();
                    var cp = classData.getConstantPool();
                    for (var i=0; i<methods.length; i++) {
                        if (ACCESS_FLAGS.isPublic(methods[i].access_flags) &&
                            ACCESS_FLAGS.isStatic(methods[i].access_flags) &&
                            !ACCESS_FLAGS.isNative(methods[i].access_flags) &&
                            cp[methods[i].name_index].bytes === methodName) {
                            return new Frame(classData, methods[i]);
                        }
                    }
                }
            }
        }
    }
}

Classes.prototype.initClass = function(className) {
    var clinit = this.getStaticMethod(className, "<clinit>", "()V");
    if (!clinit)
        return;
    LOG.debug("call " + className + ".<clinit> ...");
    clinit.run(THREADS.current.stack);
}

Classes.prototype.getClass = function(className, initialize) {
    var classData = this.classes[className];
    if (classData)
        return classData;
    if (!!(classData = this.loadClassFile(className + ".class"))) {
        if (initialize) {
            classData.staticFields = {};
            this.initClass(className);
        }
        return classData;
    }
    throw new Error(util.format("Implementation of the %s class is not found.", className));
};

Classes.prototype.getStaticField = function(className, fieldName) {
    return this.getClass(className, true).staticFields[fieldName];
}

Classes.prototype.setStaticField = function(className, fieldName, value) {
    this.getClass(className, true).staticFields[fieldName] = value;
}

Classes.prototype.getMethod = function(className, methodName, signature, staticFlag) {
    // Only force initialization when accessing a static method.
    var classData = this.getClass(className, staticFlag);
    var methods = classData.getMethods();
    var cp = classData.getConstantPool();
    for (var i=0; i<methods.length; i++) {
        if (ACCESS_FLAGS.isStatic(methods[i].access_flags) === !!staticFlag) {
            if (cp[methods[i].name_index].bytes === methodName) {
                if (signature === cp[methods[i].signature_index].bytes) {
                    if (ACCESS_FLAGS.isNative(methods[i].access_flags)) {
                        return NATIVE.getMethod(className, methodName, signature);
                    }
                    return new Frame(classData, methods[i]);
                }
            }
        }
    }
    return null;
};

Classes.prototype.getStaticMethod = function(className, methodName, signature) {
    return this.getMethod(className, methodName, signature, true);
}

Classes.prototype.newObject = function(className) {
    // Force initialization of the class (if not already done).
    return { class: this.getClass(className, true) };
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

Classes.prototype.newString = function(s) {
    var obj = this.newObject("java/lang/String");
    var length = s.length;
    var chars = this.newArray(ARRAY_TYPE.T_CHAR, length);
    for (var n = 0; n < length; ++n)
        chars[n] = s.charCodeAt(n);
    obj.value = chars;
    obj.offset = 0;
    obj.count = length;
    return obj;
}
