/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var Classes = function() {
    if (this instanceof Classes) {
        this.classfiles = [];
        this.mainclass = [];
        this.classes = {};
        this.staticFields = {};
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
        if (fileName in zip.directory)
            data = zip.read(fileName).buffer;
        return !!data;
    });
    classfiles[fileName] = data;
    return data;
}

Classes.prototype.clinit = function() {
    for(var className in this.classes) {
        var classArea = this.classes[className];
        var clinit = this.getStaticMethod(className, "<clinit>", "()V");
        if (clinit instanceof Frame) {
            SCHEDULER.sync(function() {
                LOG.debug("call " + className + ".<clinit> ...");
                clinit.run([], function() {
                    LOG.debug("call " + className + ".<clinit> ... done");
                });
            });
        }
    }
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

Classes.prototype.loadJSFile = function(fileName) {
    LOG.debug("loading " + fileName + " ...");
    var bytes = this.loadFile(fileName);
    if (!bytes)
        return null;
    var src =
      "(function () {\n" +
      "  var module = {};\n" +
      util.decodeUtf8(bytes) +
      "  return module.export;\n" +
      "})();";
    var classArea = eval(src);
    this.classes[classArea.getClassName()] = classArea;
    return classArea;
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

Classes.prototype.getClass = function(className) {
    var ca = this.classes[className];
    if (ca)
        return ca;
    if (!!(ca = this.loadJSFile(className + ".js")))
        return ca;
    if (!!(ca = this.loadClassFile(className + ".class")))
        return ca;
    throw new Error(util.format("Implementation of the %s class is not found.", className));
};

Classes.prototype.getStaticField = function(className, fieldName) {
    return this.staticFields[className + '.' + fieldName]; 
}

Classes.prototype.setStaticField = function(className, fieldName, value) {
    this.staticFields[className + '.' + fieldName] = value;
}

Classes.prototype.getStaticMethod = function(className, methodName, signature) {
    var ca = this.getClass(className);  
    if (ca instanceof ClassArea) {
        var methods = ca.getMethods();
        var cp = ca.getConstantPool();
        for(var i=0; i<methods.length; i++) 
            if (ACCESS_FLAGS.isStatic(methods[i].access_flags)) 
                if (cp[methods[i].name_index].bytes === methodName)
                    if (signature.toString() === cp[methods[i].signature_index].bytes)
                        return new Frame(ca, methods[i]);
    } else {
        if (methodName in ca) {
            return ca[methodName];
        }
    }
    return null;
};
        
Classes.prototype.getMethod = function(className, methodName, signature) {
    var ca = this.getClass(className);
    if (ca instanceof ClassArea) {
        var methods = ca.getMethods();
        var cp = ca.getConstantPool();
        for(var i=0; i<methods.length; i++)
            if (!ACCESS_FLAGS.isStatic(methods[i].access_flags)) 
                if (cp[methods[i].name_index].bytes === methodName) 
                    if (signature.toString() === cp[methods[i].signature_index].bytes) 
                        return new Frame(ca, methods[i]);
    } else {
        var o = new ca();
        if (methodName in o) {
           return o[methodName];
        }
    }
    return null;
};
        
Classes.prototype.newObject = function(className) {
    var ca = this.getClass(className);
    if (ca instanceof ClassArea) {
        
        var ctor = function() {};
        ctor.prototype = this.newObject(ca.getSuperClassName());
        var o = new ctor();
        
        o.getClassName = new Function(util.format("return \"%s\"", className));
        
        var cp = ca.getConstantPool();
        
        ca.getFields().forEach(function(field) {
            var fieldName = cp[field.name_index].bytes;
            o[fieldName] = null;
        });
        
        ca.getMethods().forEach(function(method) {
            var methodName = cp[method.name_index].bytes;
            o[methodName] = new Frame(ca, method);
        });
        
        return o;
    } else {
        var o = new ca();
        o.getClassName = new Function(util.format("return \"%s\"", className));
        return o;
    }
}

Classes.prototype.newException = function(className, message, cause) {
    var ex = this.newObject(className);
    ex["<init>"](message, cause);
    return ex;
}

