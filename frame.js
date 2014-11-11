/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

function Stack(arr) {
    this.items = arr || [];
}

Stack.INT = 0;
Stack.LONG = 1;
Stack.FLOAT = 2;
Stack.DOUBLE = 3;
Stack.REF = 4;
Stack.BYTE = 5;
Stack.CHAR = 6;
Stack.SHORT = 7;
Stack.BOOLEAN = 8;
Stack.UNKNOWN = 0; // XXX
Stack.UNKNOWN_WIDE = 1; // XXX

var SIGNATURE_TO_STACK_TYPE = "IJFDLBCSZ??";

Stack.prototype = {
    push: function(type, value) {
        this.items.push(value);
        if (type === Stack.LONG || type === Stack.DOUBLE) {
            this.items.push(null);
        }
    },
    pop: function(type) {
        var value = this.items.pop();
        if (type === Stack.LONG || type === Stack.DOUBLE) {
            value = this.items.pop();
        }
        return value;
    },
    get length() {
        return this.items.length;
    },
    set length(n) {
        this.items.length = n;
    },
    pushType: function(type, val) {
        this.push(SIGNATURE_TO_STACK_TYPE.indexOf(type), val);
    },
    popType: function(type) {
        return this.pop(SIGNATURE_TO_STACK_TYPE.indexOf(type));
    },
    read: function(type, count) {
        return this.get(type, this.items.length - count);
    },
    get: function(type, offset) {
        return this.items[offset];
    },
    set: function(type, offset, val) {
        this.items[offset] = val;
    }
}

var Frame = function(methodInfo, locals, localsBase) {
    if (!(locals instanceof Stack)) {
        locals = new Stack(locals);
    }
    this.methodInfo = methodInfo;
    this.cp = methodInfo.classInfo.constant_pool;
    this.code = methodInfo.code;
    this.ip = 0;

    this.stack = new Stack();

    this.locals = locals;
    this.localsBase = localsBase;

    this.lockObject = null;

    this.profileData = null;
}

Frame.prototype.getLocal = function(type, idx) {
    return this.locals.get(type, this.localsBase + idx);
}

Frame.prototype.setLocal = function(type, idx, value) {
    this.locals.set(type, this.localsBase + idx, value);
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
