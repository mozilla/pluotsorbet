/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var STACK_SIZE = 100;
function Stack() {
    this.types = [];//new Uint8Array(STACK_SIZE);
    //    this.ints = new Int32Array(STACK_SIZE);
    //    this.floats = this.doubles = new Float64Array(STACK_SIZE);
    this.longs = this.refs = [];
    this.floats = this.doubles = [];
    this.ints = [];

    this.length = 0;
}

// Storage types:
Stack.INT = 0;
Stack.LONG = 1;
Stack.FLOAT = 2;
Stack.DOUBLE = 3;
Stack.REF = 4;

Stack.pool = [];
Stack.obtain = function(arr) {
    var stack = Stack.pool.pop() || new Stack();
    stack.setToArray(arr);
    return stack;
}

Stack.release = function(stack) {
    Stack.pool.push(stack);
}

function StackWord(type, value) {
    this.type = type;
    this.value = value;
}

Stack.prototype = {
    setToArray: function(arr) {
        this.length = 0;
        for (var i = 0; arr && i < arr.length; i++) {
            if (typeof arr[i] === "object") {
                this.pushRef(arr[i]);
            } else {
                throw new Error("stack not designed to push anything but refs in constructor");
            }
        }
    },

    debug: function() {
        console.log("stack:");
        for (var i = 0; i < this.length; i++) {
            var type = this.types[i];
            console.log(" ", i, type, "--", this.get(type, i));
        }
    },
    getInt: function(offset) {
        return this.ints[offset];
    },
    getLong: function(offset) {
        return this.longs[offset];
    },
    getFloat: function(offset) {
        return this.floats[offset];
    },
    getDouble: function(offset) {
        return this.doubles[offset];
    },
    getRef: function(offset) {
        return this.refs[offset];
    },
    get: function(type, idx) {
        switch(type) {
        case Stack.INT: return this.getInt(idx);
        case Stack.LONG: return this.getLong(idx);
        case Stack.FLOAT: return this.getFloat(idx);
        case Stack.DOUBLE: return this.getDouble(idx);
        case Stack.REF: return this.getRef(idx);
        default: throw new Error("invalid stack type: " + type);
        }
    },

    setInt: function(offset, val) {
        if (this.length < offset + 1) {
            this.length = offset + 1;
        }
        this.types[offset] = Stack.INT;
        this.ints[offset] = val;
    },
    setLong: function(offset, val) {
        if (this.length < offset + 2) {
            this.length = offset + 2;
        }
        this.types[offset] = Stack.LONG;
        this.types[offset + 1] = Stack.LONG;
        this.longs[offset] = val;
        //this.longs[offset + 1] = null;
    },
    setFloat: function(offset, val) {
        if (this.length < offset + 1) {
            this.length = offset + 1;
        }
        this.types[offset] = Stack.FLOAT;
        this.floats[offset] = val;
    },
    setDouble: function(offset, val) {
        if (this.length < offset + 2) {
            this.length = offset + 2;
        }
        this.types[offset] = Stack.DOUBLE;
        this.types[offset + 1] = Stack.DOUBLE;
        this.doubles[offset] = val;
        //this.doubles[offset + 1] = null;
    },
    setRef: function(offset, val) {
        if (this.length < offset + 1) {
            this.length = offset + 1;
        }
        this.types[offset] = Stack.REF;
        this.refs[offset] = val;
    },

    push: function(type, value) {
        switch(type) {
        case Stack.INT: this.pushInt(value); break;
        case Stack.LONG: this.pushLong(value); break;
        case Stack.FLOAT: this.pushFloat(value); break;
        case Stack.DOUBLE: this.pushDouble(value); break;
        case Stack.REF: this.pushRef(value); break;
        default: throw new Error("invalid stack type: " + type);
        }
    },

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
        default: throw new Error("invalid stack type: " + type);
        }
    },

    pushInt: function(value) {
        this.types[this.length] = Stack.INT;
        this.ints[this.length++] = value;
    },
    pushLong: function(value) {
        this.types[this.length] = Stack.LONG;
        this.types[this.length + 1] = Stack.LONG;
        this.longs[this.length++] = value;
        this.length++;
    },
    pushFloat: function(value) {
        this.types[this.length] = Stack.FLOAT;
        this.floats[this.length++] = value;
    },
    pushDouble: function(value) {
        this.types[this.length] = Stack.DOUBLE;
        this.types[this.length + 1] = Stack.DOUBLE;
        this.doubles[this.length++] = value;
        this.length++;
    },
    pushRef: function(value) {
        this.types[this.length] = Stack.REF;
        this.refs[this.length++] = value;
    },
    pushWord: function(word) {
        var idx = this.length++;
        this.types[idx] = word.type;
        switch(word.type) {
        case Stack.INT: this.ints[idx] = word.value; break;
        case Stack.LONG: this.longs[idx] = word.value; break;
        case Stack.FLOAT: this.floats[idx] = word.value; break;
        case Stack.DOUBLE: this.doubles[idx] = word.value; break;
        case Stack.REF: this.refs[idx] = word.value; break;
        default: throw new Error("invalid stack type: " + word.type);
        }
    },

    pop: function(type) {
        switch(type) {
        case Stack.INT: return this.popInt();
        case Stack.LONG: return this.popLong();
        case Stack.FLOAT: return this.popFloat();
        case Stack.DOUBLE: return this.popDouble();
        case Stack.REF: return this.popRef();
        default: throw new Error("invalid stack type: " + type);
        }
    },
    popType: function(type) {
        switch(type[0]) {
        case "B":
        case "C":
        case "S":
        case "Z":
        case "I": return this.popInt();
        case "J": return this.popLong();
        case "F": return this.popFloat();
        case "D": return this.popDouble();
        case "[":
        case "L": return this.popRef();
        default: throw new Error("invalid stack type: " + type);
        }
    },

    popInt: function() {
        return this.ints[--this.length];
    },
    popLong: function() {
        --this.length;
        return this.longs[--this.length];
    },
    popFloat: function() {
        return this.floats[--this.length];
    },
    popDouble: function() {
        --this.length;
        return this.doubles[--this.length];
    },
    popRef: function() {
        return this.refs[--this.length];
    },

    popWord: function() {
        var idx = --this.length;
        var type = this.types[idx];
        var value;
        switch(type) {
        case Stack.INT: value = this.ints[idx]; break;
        case Stack.LONG: value = this.longs[idx]; break;
        case Stack.FLOAT: value = this.floats[idx]; break;
        case Stack.DOUBLE: value = this.doubles[idx]; break;
        case Stack.REF: value = this.refs[idx]; break;
        default: throw new Error("invalid stack type: " + type);
        }
        return new StackWord(type, value);
    },

    readInt: function(count) { return this.getInt(this.length - count); },
    readLong: function(count) { return this.getLong(this.length - count); },
    readFloat: function(count) { return this.getFloat(this.length - count); },
    readDouble: function(count) { return this.getDouble(this.length - count); },
    readRef: function(count) { return this.getRef(this.length - count); },
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
    debug: function() {
        console.log("locals:");
        for (var i = 0; i < this.methodInfo.max_locals; i++) {
            var type = this.locals.types[this.localsBase + i];
            console.log(" ", i, this.locals.get(type, this.localsBase + i));
        }
    },
    getLocalInt: function(idx) { return this.locals.getInt(this.localsBase + idx); },
    getLocalLong: function(idx) { return this.locals.getLong(this.localsBase + idx); },
    getLocalFloat: function(idx) { return this.locals.getFloat(this.localsBase + idx); },
    getLocalDouble: function(idx) { return this.locals.getDouble(this.localsBase + idx); },
    getLocalRef: function(idx) { return this.locals.getRef(this.localsBase + idx); },

    setLocalInt: function(idx, value) { this.locals.setInt(this.localsBase + idx, value); },
    setLocalLong: function(idx, value) { this.locals.setLong(this.localsBase + idx, value); },
    setLocalFloat: function(idx, value) { this.locals.setFloat(this.localsBase + idx, value); },
    setLocalDouble: function(idx, value) { this.locals.setDouble(this.localsBase + idx, value); },
    setLocalRef: function(idx, value) { this.locals.setRef(this.localsBase + idx, value); },

    read8: function() {
        return this.code[this.ip++];
    },

    read16: function() {
        return this.read8()<<8 | this.read8();
    },

    read32: function() {
        return this.read16()<<16 | this.read16();
    },

    read8signed: function() {
        var x = this.read8();
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
