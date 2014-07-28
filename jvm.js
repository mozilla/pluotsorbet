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

    var ctx = new Context();

    var caller = new Frame();
    ctx.frames.push(caller);

    // These classes are guaranteed to not have a static initializer.
    CLASSES.java_lang_Object = CLASSES.loadClass("java/lang/Object");
    CLASSES.java_lang_Class = CLASSES.loadClass("java/lang/Class");
    CLASSES.java_lang_String = CLASSES.loadClass("java/lang/String");
    CLASSES.java_lang_Thread = CLASSES.loadClass("java/lang/Thread");

    ctx.thread = CLASSES.mainThread = CLASSES.newObject(CLASSES.java_lang_Thread);
    ctx.thread.pid = util.id();
    ctx.thread.running = true;
    caller.stack.push(CLASSES.mainThread);
    caller.stack.push(ctx.newString("main"));
    ctx.pushFrame(CLASSES.getMethod(CLASSES.java_lang_Thread, "<init>", "(Ljava/lang/String;)V"), 2);
    ctx.execute(caller);

    caller.stack.push(CLASSES.newArray("[Ljava/lang/String;", 0));
    ctx.pushFrame(entryPoint, 1);
    ctx.start(caller);
}
