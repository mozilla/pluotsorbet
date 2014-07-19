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
