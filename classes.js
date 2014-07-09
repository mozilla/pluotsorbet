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
    var classArea = new ClassArea(bytes);
    this.classes[classArea.getClassName()] = classArea;
    return classArea;
}

Classes.prototype.loadClassFile = function(fileName) {
    LOG.debug("loading " + fileName + " ...");
    var bytes = this.loadFile(fileName);
    if (!bytes)
        return null;
    var ca = this.loadClassBytes(bytes);
    var classes = ca.getClasses();
    for (var i=0; i<classes.length; i++) {
        if (!this.classes[classes[i]]) {
            this.loadClassFile(path.dirname(fileName) + path.sep + classes[i] + ".class");
        }
    }
    return ca;
}

Classes.prototype.getEntryPoint = function(className, methodName) {
    for(var name in this.classes) {
        var ca = this.classes[name];
        if (ca instanceof ClassArea) {
            if (!className || (className === ca.getClassName())) {
                if (ACCESS_FLAGS.isPublic(ca.getAccessFlags())) {
                    var methods = ca.getMethods();
                    var cp = ca.getConstantPool();
                    for(var i=0; i<methods.length; i++) {
                        if
                        (
                         ACCESS_FLAGS.isPublic(methods[i].access_flags) &&
                         ACCESS_FLAGS.isStatic(methods[i].access_flags) &&
                         cp[methods[i].name_index].bytes === methodName
                        )
                        { return new Frame(ca, methods[i]); }
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
    SCHEDULER.sync(function() {
        LOG.debug("call " + className + ".<clinit> ...");
        clinit.run([], function() {
            LOG.debug("call " + className + ".<clinit> ... done");
        });
    });
}

Classes.prototype.getClass = function(className, initialize) {
    var ca = this.classes[className];
    if (ca)
        return ca;
    if (!!(ca = this.loadClassFile(className + ".class"))) {
        if (initialize) {
            ca.staticFields = {};
            this.initClass(className);
        }
        return ca;
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
    var ca = this.getClass(className, staticFlag);
    if (ca instanceof ClassArea) {
        var methods = ca.getMethods();
        var cp = ca.getConstantPool();
        for(var i=0; i<methods.length; i++) {
            if (ACCESS_FLAGS.isStatic(methods[i].access_flags) === !!staticFlag)
                if (cp[methods[i].name_index].bytes === methodName)
                    if (signature.toString() === cp[methods[i].signature_index].bytes)
                        return new Frame(ca, methods[i]);
        }
    } else {
        if (methodName in ca) {
            return ca[methodName];
        }
    }
    return null;
};

Classes.prototype.getStaticMethod = function(className, methodName, signature) {
    return this.getMethod(className, methodName, signature, true);
}

Classes.prototype.newObject = function(className) {
    // Force initialization of the class (if not already done).
    this.getClass(className, true);

    return {};
}
