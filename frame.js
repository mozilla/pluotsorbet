/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

Array.prototype.push2 = function(value) {
    this.push(value);
    this.push(null);
}

Array.prototype.pop2 = function() {
    this.pop();
    return this.pop();
}

Array.prototype.pushType = function(signature, value) {
    if (signature === "J" || signature === "D") {
        this.push2(value);
        return;
    }
    this.push(value);
}

Array.prototype.popType = function(signature) {
    return (signature === "J" || signature === "D") ? this.pop2() : this.pop();
}

var Frame = function(methodInfo) {
    if (methodInfo) {
        this.methodInfo = methodInfo;
        this.cp = methodInfo.classInfo.constant_pool;
        this.code = methodInfo.code;
        this.ip = 0;
    }
    this.stack = [];
}

Frame.prototype.pushFrame = function(methodInfo, consumes) {
    var callee = new Frame(methodInfo);
    callee.locals = this.stack;
    callee.localsBase = this.stack.length - consumes;
    callee.caller = this;
    return callee;
}

Frame.prototype.popFrame = function() {
    var caller = this.caller;
    caller.stack.length = this.localsBase;
    return caller;
}

Frame.prototype.getLocal = function(idx) {
    return this.locals[this.localsBase + idx];
}

Frame.prototype.setLocal = function(idx, value) {
    this.locals[this.localsBase + idx] = value;
}

Frame.prototype.isWide = function() {
    return this.code[this.ip - 2] === OPCODES.wide;
}

Frame.prototype.getOp = function() {
    return this.code[this.ip - 1];
}

Frame.prototype.read8 = function() {
    return this.code[this.ip++];
};

Frame.prototype.read16 = function() {
    return this.read8()<<8 | this.read8();
};

Frame.prototype.read32 = function() {
    return this.read16()<<16 | this.read16();
};

Frame.prototype.read8signed = function() {
    var x = this.read8();
    return (x > 0x7f) ? (x - 0x100) : x;
}

Frame.prototype.read16signed = function() {
    var x = this.read16();
    return (x > 0x7fff) ? (x - 0x10000) : x;
}

Frame.prototype.read32signed = function() {
    var x = this.read32();
    return (x > 0x7fffffff) ? (x - 0x100000000) : x;
}

Frame.prototype.backTrace = function() {
    var stack = [];
    var frame = this;
    while (frame.caller) {
        var methodInfo = frame.methodInfo;
        var className = methodInfo.classInfo.className;
        var methodName = methodInfo.name;
        var signature = Signature.parse(methodInfo.signature);
        var IN = signature.IN;
        var args = [];
        var lp = 0;
        for (var n = 0; n < IN.length; ++n) {
            var arg = frame.locals[frame.localsBase + lp];
            ++lp;
            switch (IN[n].type) {
            case "long":
            case "double":
                ++lp;
                break;
            case "object":
                if (arg === null)
                    arg = "null";
                else if (arg.class.className === "java/lang/String")
                    arg = "'" + util.fromJavaString(arg) + "'";
                else
                    arg = "<" + arg.class.className + ">";
            }
            args.push(arg);
        }
        stack.push(methodInfo.classInfo.className + "." + methodInfo.name + "(" + args.join(",") + ")");
        frame = frame.caller;
    }
    return stack.join("\n");
}

Frame.prototype.getThread = function() {
    var frame = this;
    do {
        var thread = frame.thread;
        if (thread)
            return thread;
        frame = frame.caller;
    } while (frame);
    return null;
}

Frame.prototype.monitorEnter = function(obj, callback) {
    var lock = obj.lock;
    if (!lock) {
        obj.lock = { thread: this.getThread(), count: 1, waiters: [] };
        return true;
    }
    if (lock.thread === this.getThread()) {
        ++lock.count;
        return true;
    }
    lock.waiters.push(VM.resume.bind(VM, this, callback));
    return false;
}

Frame.prototype.monitorLeave = function(obj) {
    var lock = obj.lock;
    if (lock.thread !== this.getThread()) {
        console.log("WARNING: thread tried to unlock a monitor it didn't own");
        return;
    }
    if (--lock.count > 0) {
        return;
    }
    var waiters = lock.waiters;
    obj.lock = null;
    for (var n = 0; n < waiters.length; ++n)
        window.setZeroTimeout.call(window, waiters[n]);
}

Frame.prototype.invokeConstructor = function(obj) {
    var ctor = CLASSES.getMethod(obj.class, "<init>", "()V", false, false);
    VM.invoke(this.getThread(), ctor, [obj]);
}

Frame.prototype.invokeConstructorWithString = function(obj, str) {
    var ctor = CLASSES.getMethod(obj.class, "<init>", "(Ljava/lang/String;)V", false, false);
    VM.invoke(this.getThread(), ctor, [obj, this.newString(str)]);
}

Frame.prototype.initClass = function(classInfo) {
    if (classInfo.initialized)
        return;
    classInfo.initialized = true;
    if (classInfo.superClass)
        this.initClass(classInfo.superClass);
    classInfo.staticFields = {};
    classInfo.constructor = function () {
    }
    classInfo.constructor.prototype.class = classInfo;
    var clinit = CLASSES.getMethod(classInfo, "<clinit>", "()V", true, false);
    if (clinit)
        VM.invoke(this.getThread(), clinit);
    return classInfo;
}

Frame.prototype.newString = function(s) {
    var obj = CLASSES.newObject(CLASSES.java_lang_String);
    var length = s.length;
    var chars = CLASSES.newPrimitiveArray("C", length);
    for (var n = 0; n < length; ++n)
        chars[n] = s.charCodeAt(n);
    obj["java/lang/String$value"] = chars;
    obj["java/lang/String$offset"] = 0;
    obj["java/lang/String$count"] = length;
    return obj;
}

Frame.prototype.newException = function(className, message) {
    if (!message)
        message = "";
    message = "" + message;
    var classInfo = CLASSES.getClass(className);
    if (!classInfo.initialized)
        this.initClass(classInfo);
    var ex = CLASSES.newObject(classInfo);
    this.invokeConstructorWithString(ex, message);
    return ex;
}
