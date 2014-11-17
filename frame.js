/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var MAX_STACK_SIZE = 256;
function Stack() {
    this.types = new Uint8Array(MAX_STACK_SIZE);
    this.longs = this.refs = [];
    this.floats = this.doubles = new Float64Array(MAX_STACK_SIZE);
    this.ints = new Int32Array(MAX_STACK_SIZE);
    this.length = 0;
}

// Storage types:
var STACK_INT = 0;
var STACK_LONG = 1;
var STACK_FLOAT = 2;
var STACK_DOUBLE = 3;
var STACK_REF = 4;

Stack.pool = [];
Stack.obtain = function(arr) {
    var stack = Stack.pool.pop() || new Stack();
    stack.length = 0;
    for (var i = 0; arr && i < arr.length; i++) {
        stack.pushRef(arr[i]);
    }
    return stack;
}

Stack.release = function(stack) {
    Stack.pool.push(stack);
}

Stack.signatureToStackType = function(signature) {
    switch(signature[0]) {
    case "B":
    case "C":
    case "S":
    case "Z":
    case "I": return STACK_INT;
    case "J": return STACK_LONG;
    case "F": return STACK_FLOAT;
    case "D": return STACK_DOUBLE;
    case "[":
    case "L": return STACK_REF;
    }
}

Stack.prototype = {
    pushType: function(type, value) {
        switch(type[0]) {
        case "B":
        case "C":
        case "S":
        case "Z":
        case "I": this.pushInt(value); break;
        case "J": this.pushLong(value); break;
        case "F": this.pushFloat(value); break;
        case "D": this.pushDouble(value); break;
        case "[":
        case "L": this.pushRef(value); break;
        }
    },

    pushInt: function(value) {
        this.types[this.length] = STACK_INT;
        this.ints[this.length++] = value;
    },
    pushLong: function(value) {
        this.types[this.length] = STACK_LONG;
        this.types[this.length + 1] = STACK_LONG;
        this.longs[this.length++] = value;
        this.length++;
    },
    pushFloat: function(value) {
        this.types[this.length] = STACK_FLOAT;
        this.floats[this.length++] = value;
    },
    pushDouble: function(value) {
        this.types[this.length] = STACK_DOUBLE;
        this.types[this.length + 1] = STACK_DOUBLE;
        this.doubles[this.length++] = value;
        this.length++;
    },
    pushRef: function(value) {
        this.types[this.length] = STACK_REF;
        this.refs[this.length++] = value;
    },
    pushWord: function(word) {
        var idx = this.length++;
        var type = word[0];
        var value = word[1];
        this.types[idx] = type;
        switch(type) {
        case STACK_INT: this.ints[idx] = value; break;
        case STACK_LONG: this.longs[idx] = value; break;
        case STACK_FLOAT: this.floats[idx] = value; break;
        case STACK_DOUBLE: this.doubles[idx] = value; break;
        case STACK_REF: this.refs[idx] = value; break;
        }
    },

    pop: function(type) {
        switch(type) {
        case STACK_INT: return this.ints[--this.length];
        case STACK_LONG: this.length--; return this.longs[--this.length];
        case STACK_FLOAT: return this.floats[--this.length];
        case STACK_DOUBLE: this.length--; return this.doubles[--this.length];
        case STACK_REF: return this.refs[--this.length];
        }
    },
    popType: function(type) {
        switch(type[0]) {
        case "B":
        case "C":
        case "S":
        case "Z":
        case "I": return this.ints[--this.length];
        case "J": this.length--; return this.longs[--this.length];
        case "F": return this.floats[--this.length];
        case "D": this.length--; return this.doubles[--this.length];
        case "[":
        case "L": return this.refs[--this.length];
        }
    },

    popLong: function() {
        --this.length;
        return this.longs[--this.length];
    },
    popDouble: function() {
        --this.length;
        return this.doubles[--this.length];
    },

    popWord: function() {
        var idx = --this.length;
        var type = this.types[idx];
        var value;
        switch(type) {
        case STACK_INT: value = this.ints[idx]; break;
        case STACK_LONG: value = this.longs[idx]; break;
        case STACK_FLOAT: value = this.floats[idx]; break;
        case STACK_DOUBLE: value = this.doubles[idx]; break;
        case STACK_REF: value = this.refs[idx]; break;
        }
        return [type, value];
    },

    readInt: function(count) { return this.ints[this.length - count]; },
    readLong: function(count) { return this.longs[this.length - count]; },
    readFloat: function(count) { return this.floats[this.length - count]; },
    readDouble: function(count) { return this.doubles[this.length - count]; },
    readRef: function(count) { return this.refs[this.length - count]; },

    loadArgsAtIndex: function(args, idx) {
        for (var i = idx; i >= 0; i--) {
            var idx = --this.length;
            switch (this.types[idx]) {
            case STACK_INT: args[i] = this.ints[idx]; break;
            case STACK_LONG: args[i] = this.longs[idx]; break;
            case STACK_FLOAT: args[i] = this.floats[idx]; break;
            case STACK_DOUBLE: args[i] = this.doubles[idx]; break;
            case STACK_REF: args[i] = this.refs[idx]; break;
            }
        }
    }
}

var Frame = function(methodInfo, locals, localsBase) {
    if (!(locals instanceof Stack)) {
        locals = Stack.obtain(locals);
    }
    this.methodInfo = methodInfo;
    this.cp = methodInfo.classInfo.constant_pool;
    this.code = methodInfo.code;
    this.ip = 0;

    this.stack = Stack.obtain();

    this.locals = locals;
    this.localsBase = localsBase;

    this.lockObject = null;

    this.profileData = null;
}

Frame.prototype = {

    setLocal: function(type, idx, value) {
        this.locals.types[this.localsBase + idx] = type;
        switch(type) {
        case STACK_INT: this.locals.ints[this.localsBase + idx] = value; break;
        case STACK_LONG: this.locals.longs[this.localsBase + idx] = value; break;
        case STACK_FLOAT: this.locals.floats[this.localsBase + idx] = value; break;
        case STACK_DOUBLE: this.locals.doubles[this.localsBase + idx] = value; break;
        case STACK_REF: this.locals.refs[this.localsBase + idx] = value; break;
        }
    },

    getLocal: function(type, idx) {
        var ret;
        switch(type) {
        case STACK_INT: ret = this.locals.ints[this.localsBase + idx]; break;
        case STACK_LONG: ret = this.locals.longs[this.localsBase + idx]; break;
        case STACK_FLOAT: ret = this.locals.floats[this.localsBase + idx]; break;
        case STACK_DOUBLE: ret = this.locals.doubles[this.localsBase + idx]; break;
        case STACK_REF: ret = this.locals.refs[this.localsBase + idx]; break;
        }
        //        console.log("getLocal", type, idx, "=", ret);
        return ret;
    },

    read8: function() {
        return this.code[this.ip++];
    },

    read16: function() {
        return this.code[this.ip++] << 8 | this.code[this.ip++];
    },

    read32: function() {
        return this.read16()<<16 | this.read16();
    },

    read8signed: function() {
        var x = this.code[this.ip++];
        return (x > 0x7f) ? (x - 0x100) : x;
    },

    read16signed: function() {
        var x = this.read16();
        return (x > 0x7fff) ? (x - 0x10000) : x;
    },

    read32signed: function() {
        var x = this.read32();
        return (x > 0x7fffffff) ? (x - 0x100000000) : x;
    },
};
