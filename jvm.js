/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var CLASSES;

var JVM = function() {
    if (this instanceof JVM) {
        CLASSES = new Classes();
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

    var frame = new Frame();
    frame.initClass(CLASSES.java_lang_Object = CLASSES.loadClass("java/lang/Object"), function() {
        frame.initClass(CLASSES.java_lang_String = CLASSES.loadClass("java/lang/String"), function() {
            frame.initClass(CLASSES.java_lang_Class = CLASSES.loadClass("java/lang/Class"), function() {
                frame.initClass(CLASSES.java_lang_Thread = CLASSES.loadClass("java/lang/Thread"), function() {
                    CLASSES.mainThread = CLASSES.newObject(CLASSES.java_lang_Thread);
                    frame.thread = CLASSES.mainThread;
                    frame.invokeConstructorWithString(CLASSES.mainThread, "main", function() {
                        VM.invoke(CLASSES.mainThread, entryPoint, [null]);
                    });
                });
            });
        });
    });
}
