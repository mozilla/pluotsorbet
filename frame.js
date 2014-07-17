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

Frame.prototype.throw = function(ex) {
    var handler_pc = null;

    for (var i=0; i<this.exception_table.length; i++) {
        if (this.ip >= this.exception_table[i].start_pc && this.ip <= this.exception_table[i].end_pc) {
            if (this.exception_table[i].catch_type === 0) {
                handler_pc = this.exception_table[i].handler_pc;
            } else {
                var name = this.cp[this.cp[this.exception_table[i].catch_type].name_index].bytes;
                if (name === ex.className) {
                    handler_pc = this.exception_table[i].handler_pc;
                    break;
                }
            }
        }
    }

    if (handler_pc != null) {
        stack.push(ex);
        this.ip = handler_pc;
    } else {
        throw ex;
    }
}

Frame.prototype.raiseException = function(className, message) {
    var ex = CLASSES.newObject(this, className);
    var ctor = CLASSES.getMethod(this, ex.class, "<init>", "(Ljava/lang/String;)V", false, false);
    this.stack.push(ex);
    this.stack.push(message);
    this.invoke(OPCODES.invokespecial, ctor);
    this.throw(ex);
}

Frame.prototype.checkArrayAccess = function(refArray, idx) {
    if (!refArray) {
        this.raiseException("java/lang/NullPointerException");
        return false;
    }
    if (idx < 0 || idx >= refArray.length) {
        this.raiseException("java/lang/ArrayIndexOutOfBoundsException", idx);
        return false;
    }
    return true;
}

Frame.prototype.invoke = function(op, methodInfo) {
    var consumes = Signature.parse(methodInfo.signature).IN.slots;

    if (op !== OPCODES.invokestatic) {
        ++consumes;
        var obj = this.stack[this.stack.length - consumes];
        if (!obj) {
            this.raiseException("java/lang/NullPointerException");
            return;
        }
        switch (op) {
        case OPCODES.invokevirtual:
            // console.log("virtual dispatch", methodInfo.classInfo.className, obj.class.className, methodInfo.name, methodInfo.signature);
            if (methodInfo.classInfo != obj.class)
                methodInfo = CLASSES.getMethod(this, obj.class, methodInfo.name, methodInfo.signature, op === OPCODES.invokestatic);
            break;
        }
    }

    if (ACCESS_FLAGS.isNative(methodInfo.access_flags)) {
        NATIVE.invokeNative(this, methodInfo);
        return;
    }

    var callee = this.pushFrame(methodInfo, consumes);

    VM.execute(callee);
}
