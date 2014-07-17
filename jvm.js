/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var CLASSES, THREADS, NATIVE;

var JVM = function() {
    if (this instanceof JVM) {
        CLASSES = new Classes();
        THREADS = new Threads();
        NATIVE = new Native();

        THREADS.add(new Thread("main"));

        this.entryPoint = {
            className: null,
            methodName: "main"
        };
    } else {
        return new JVM();
    }
}

JVM.prototype.setEntryPointClassName = function(className) {
    this.entryPoint.className = className;
}

JVM.prototype.setEntryPointMethodName = function(methodName) {
    this.entryPoint.methodName = methodName;
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

JVM.prototype.start = function() {
    var frame = THREADS.current.frame;
    var entryPoint = CLASSES.getEntryPoint(frame, this.entryPoint.className, this.entryPoint.methodName);
    if (!entryPoint) {
        throw new Error("Entry point method is not found.");
    }
    frame.stack.push(null); // args
    frame.invoke(OPCODES.invokestatic, entryPoint);
}
