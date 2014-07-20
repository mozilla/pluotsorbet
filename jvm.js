/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var CLASSES, NATIVE;

var JVM = function() {
    if (this instanceof JVM) {
        CLASSES = new Classes();
        NATIVE = new Native();
    } else {
        return new JVM();
    }
}

JVM.prototype.addPath = function(path, data) {
    return CLASSES.addPath(path, data);
}

JVM.prototype.loadClassFile = function(fileName) {
    return CLASSES.loadClassFile(fileName);
}

JVM.prototype.loadJarFile = function(fileName) {
    return CLASSES.loadJarFile(fileName);
}

JVM.prototype.run = function(className) {
    var classInfo = CLASSES.getClass(className);
    if (!classInfo) {
        throw new Error("Could not find or load main class " + className);
    }
    var entryPoint = CLASSES.getEntryPoint(classInfo);
    if (!entryPoint) {
        throw new Error("Could not find main method in class " + className);
    }

    function ensureInitialized(className) {
        CLASSES.initClass(CLASSES.loadClass(className));
    }

    ensureInitialized("java/lang/Object");
    ensureInitialized("java/lang/Class");
    ensureInitialized("java/lang/String");
    ensureInitialized("java/lang/Thread");
    ensureInitialized("java/lang/Throwable");
    ensureInitialized("java/lang/Error");

    VM.invoke(entryPoint, [null]);
}
