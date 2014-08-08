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

JVM.prototype.initializeBuiltinClasses = function() {
    // These classes are guaranteed to not have a static initializer.
    CLASSES.java_lang_Object = CLASSES.loadClass("java/lang/Object");
    CLASSES.java_lang_Class = CLASSES.loadClass("java/lang/Class");
    CLASSES.java_lang_String = CLASSES.loadClass("java/lang/String");
    CLASSES.java_lang_Thread = CLASSES.loadClass("java/lang/Thread");
}

JVM.prototype.startIsolate0 = function(className, args) {
    var runtime = new Runtime(this);
    var ctx = new Context(runtime);

    var com_sun_cldc_isolate_Isolate = CLASSES.getClass("com/sun/cldc/isolate/Isolate");

    var isolate = ctx.newObject(com_sun_cldc_isolate_Isolate);
    isolate.id = util.id();

    var caller = new Frame();
    ctx.frames.push(caller);

    var array = ctx.newArray("[Ljava/lang/String;", args.length);
    for (var n = 0; n < args.length; ++n)
        array[n] = args[n] ? ctx.newString(args[n]) : null;

    caller.stack.push(isolate);
    caller.stack.push(ctx.newString(className.replace(/\./g, "/")));
    caller.stack.push(array);
    ctx.pushFrame(CLASSES.getMethod(com_sun_cldc_isolate_Isolate, "<init>", "(Ljava/lang/String;[Ljava/lang/String;)V"), 3);
    ctx.execute(caller);

    caller.stack.push(isolate);
    ctx.pushFrame(CLASSES.getMethod(com_sun_cldc_isolate_Isolate, "start", "()V"), 1);
    ctx.start(caller);
}

JVM.prototype.startIsolate = function(isolate) {
    var mainClass = util.fromJavaString(isolate.class.getField("_mainClass", "Ljava/lang/String;", false).get(isolate)).replace(/\./g, "/");
    var mainArgs = isolate.class.getField("_mainArgs", "[Ljava/lang/String;", false).get(isolate);
    mainArgs.forEach(function(str, n) {
        mainArgs[n] = util.fromJavaString(str);
    });

    var runtime = new Runtime(this);
    var ctx = new Context(runtime);

    isolate.runtime = runtime;
    runtime.isolate = isolate;

    runtime.updateStatus(2); // STARTED

    var classInfo = CLASSES.getClass(mainClass);
    if (!classInfo)
        throw new Error("Could not find or load main class " + mainName);

    var entryPoint = CLASSES.getEntryPoint(classInfo);
    if (!entryPoint)
        throw new Error("Could not find main method in class " + mainName);

    var caller = new Frame();
    ctx.frames.push(caller);

    ctx.thread = runtime.mainThread = ctx.newObject(CLASSES.java_lang_Thread);
    ctx.thread.pid = util.id();
    ctx.thread.alive = true;
    caller.stack.push(runtime.mainThread);
    caller.stack.push(ctx.newString("main"));
    ctx.pushFrame(CLASSES.getMethod(CLASSES.java_lang_Thread, "<init>", "(Ljava/lang/String;)V"), 2);
    ctx.execute(caller);

    var args = ctx.newArray("[Ljava/lang/String;", mainArgs.length);
    for (var n = 0; n < mainArgs.length; ++n)
        args[n] = mainArgs[n] ? ctx.newString(mainArgs[n]) : null;
    caller.stack.push(args);
    ctx.pushFrame(entryPoint, 1);
    ctx.start(caller);
}
