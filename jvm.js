/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var LOG, CLASSES, THREADS, SCHEDULER;

var JVM = function() {
    if (this instanceof JVM) {
        LOG = new Logger();
        CLASSES = new Classes();
        THREADS = new Threads();
        SCHEDULER = new Scheduler();
        
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

JVM.prototype.setLogLevel = function(level) {
    LOG.setLogLevel(level);
}

JVM.prototype.addPath = function(path, data) {
    return CLASSES.addPath(path, data);
}

JVM.prototype.loadClassFile = function(fileName) {
    return CLASSES.loadClassFile(fileName);
}

JVM.prototype.loadJSFile = function(fileName) {
    return CLASSES.loadJSFile(fileName);
}

JVM.prototype.loadJarFile = function(fileName) {
    return CLASSES.loadJarFile(fileName);
}

JVM.prototype.run = function() {
    var self = this;

    var entryPoint = CLASSES.getEntryPoint(this.entryPoint.className, this.entryPoint.methodName);
    if (!entryPoint) {
        throw new Error("Entry point method is not found.");
    }

    entryPoint.run(arguments, function(code) {
        var exit = function() {
            SCHEDULER.tick(0, function() {
                if (THREADS.count() === 1) {
                    THREADS.remove(0);
                    if (typeof code !== "undefined")
                        console.log("exit(" + code + ")");
                } else {
                    exit();
                }
            });
        };
        exit();
    });
}

