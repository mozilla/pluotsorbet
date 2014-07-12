/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var Stack = function() {
    this.array = [];
}

Stack.prototype.push = function (value) {
    this.array.push(value);
}

Stack.prototype.pop = function () {
    return this.array.pop();
}

Stack.prototype.push2 = function (value) {
    this.array.push(value);
    this.array.push(null);
}

Stack.prototype.pop2 = function () {
    this.array.pop();
    return this.array.pop();
}

Stack.prototype.popType = function (type) {
    return (type === "long" || type === "double") ? this.pop2() : this.pop();
}

Stack.prototype.pushType = function (type, value) {
    if (type === "long" || type === "double") {
        this.push2(value);
        return;
    }
    this.push(value);
}

Stack.prototype.popArgs = function (signature) {
    var IN = signature.IN;
    var args = Array(IN.length);
    for (var i=0; i<IN.length; i++) {
        args[args.length-1-i] = this.popType(IN[i].type);
    }
    return args;
}

Stack.prototype.top = function () {
    return this.array[this.array.lengt - 1];
}

Stack.prototype.reserveLocals = function (argc, max_locals) {
    var stackBase = this.array.length - argc;
    var locals = new Locals(this, stackBase, max_locals);
    this.array.length = stackBase + max_locals;
    return locals;
}

Stack.prototype.popLocals = function (locals) {
    this.array.length = locals.base;
}
