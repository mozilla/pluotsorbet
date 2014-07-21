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
    if (!bytes) {
        var frame = new Frame();
        frame.thread = CLASSES.mainThread;
        throw frame.newException("java/lang/ClassNotFoundException", fileName);
    }
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
        return this.initPrimitiveArrayType(typeName, constructor);
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

Classes.prototype.getField = function(className, fieldName, signature, staticFlag) {
    var classInfo = this.getClass(className, false);
    do {
        var fields = classInfo.fields;
        for (var i=0; i<fields.length; ++i) {
            var field = fields[i];
            if (ACCESS_FLAGS.isStatic(field.access_flags) === !!staticFlag) {
                if (field.name === fieldName && field.signature === signature) {
                    if (!field.id)
                        field.id = classInfo.className + "$" + fieldName;
                    return field;
                }
            }
        }
        classInfo = classInfo.superClass;
    } while (classInfo);
};

Classes.prototype.getMethod = function(classInfo, methodName, signature, staticFlag, inheritFlag) {
    do {
        var methods = classInfo.methods;
        for (var i=0; i<methods.length; ++i) {
            var method = methods[i];
            if (ACCESS_FLAGS.isStatic(method.access_flags) === !!staticFlag) {
                if (method.name === methodName && method.signature === signature)
                    return method;
            }
        }
        classInfo = classInfo.superClass;
    } while (classInfo);
};

Classes.prototype.newObject = function(classInfo) {
    return new (classInfo.constructor)();
}

Classes.prototype.newPrimitiveArray = function(type, size) {
    var constructor = ARRAYS[type];
    if (!constructor.prototype.class)
        this.initPrimitiveArrayType(type, constructor);
    return constructor.call(null, size);
}

Classes.prototype.newArray = function(typeName, size) {
    return this.getClass(typeName).constructor.call(null, size);
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

Classes.prototype.bootstrap = function() {
    var frame = new Frame();

    this.java_lang_Object = frame.initClass(this.loadClass("java/lang/Object"));
    this.java_lang_String = frame.initClass(this.loadClass("java/lang/String"));
    this.java_lang_Class = frame.initClass(this.loadClass("java/lang/Class"));
    this.java_lang_Thread = frame.initClass(this.loadClass("java/lang/Thread"));

    this.mainThread = this.newObject(this.java_lang_Thread);
    frame.thread = this.mainThread;
    frame.invokeConstructorWithString(this.mainThread, "main");
}
