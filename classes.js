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
        throw this.newException(this.mainThread, "java/lang/ClassNotFoundException", "fileName");
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

Classes.prototype.initClass = function(classInfo) {
    if (classInfo.initialized)
        return;
    classInfo.initialized = true;
    if (classInfo.superClass)
        this.initClass(classInfo.superClass);
    classInfo.staticFields = {};
    classInfo.constructor = function () {
    }
    classInfo.constructor.prototype.class = classInfo;
    var clinit = this.getMethod(classInfo, "<clinit>", "()V", true, false);
    if (clinit) {
        // Static initializers always run on the main thread.
        VM.invoke(this.mainThread, clinit);
    }
    return classInfo;
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

Classes.prototype.newObject = function(classNameOrClassInfo) {
    var classInfo = (typeof classNameOrClassInfo === "string")
                  ? this.getClass(classNameOrClassInfo)
                  : classNameOrClassInfo;
    this.initClass(classInfo);
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

Classes.prototype.newString = function(s) {
    var obj = this.newObject(this.java_lang_String);
    var length = s.length;
    var chars = this.newPrimitiveArray("C", length);
    for (var n = 0; n < length; ++n)
        chars[n] = s.charCodeAt(n);
    obj["java/lang/String$value"] = chars;
    obj["java/lang/String$offset"] = 0;
    obj["java/lang/String$count"] = length;
    return obj;
}

Classes.prototype.newException = function(thread, className, message) {
    if (!message)
        message = "";
    message = "" + message;
    var ex = this.newObject(className);
    VM.invokeConstructorWithString(thread, ex, message);
    return ex;
}

Classes.prototype.bootstrap = function() {
    this.java_lang_Object = this.initClass(this.loadClass("java/lang/Object"));
    this.java_lang_String = this.initClass(this.loadClass("java/lang/String"));
    this.java_lang_Class = this.initClass(this.loadClass("java/lang/Class"));

    this.mainThread = this.newObject("java/lang/Thread");
    VM.invokeConstructorWithString(this.mainThread, this.mainThread, "main");
}
