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

JVM.prototype.run = function(className, args) {
    var classInfo = CLASSES.getClass(className.replace(/\./g, "/"));
    if (!classInfo)
        throw new Error("Could not find or load main class " + className);

    var entryPoint = CLASSES.getEntryPoint(classInfo);
    if (!entryPoint)
        throw new Error("Could not find main method in class " + className);

    var runtime = new Runtime(this);
    var ctx = new Context(runtime);

    var caller = new Frame();
    ctx.frames.push(caller);

    // These classes are guaranteed to not have a static initializer.
    CLASSES.java_lang_Object = CLASSES.loadClass("java/lang/Object");
    CLASSES.java_lang_Class = CLASSES.loadClass("java/lang/Class");
    CLASSES.java_lang_String = CLASSES.loadClass("java/lang/String");
    CLASSES.java_lang_Thread = CLASSES.loadClass("java/lang/Thread");

    ctx.thread = runtime.mainThread = ctx.newObject(CLASSES.java_lang_Thread);
    ctx.thread.pid = util.id();
    ctx.thread.alive = true;
    caller.stack.push(runtime.mainThread);
    caller.stack.push(ctx.newString("main"));
    ctx.pushFrame(CLASSES.getMethod(CLASSES.java_lang_Thread, "<init>", "(Ljava/lang/String;)V"), 2);
    ctx.execute(caller);

    var arr = ctx.newArray("[Ljava/lang/String;", args.length);
    for (var n = 0; n < args.length; ++n)
        arr[n] = ctx.newString(args[n]);
    caller.stack.push(arr);
    ctx.pushFrame(entryPoint, 1);
    ctx.start(caller);

    return runtime;
}
