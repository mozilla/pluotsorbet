/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var Locals = function(stack, base) {
    this.stack = stack;
    this.base = base;
}

Locals.prototype.set = function (idx, value) {
    this.stack[base + idx] = value;
}

Locals.prototype.get = function (idx) {
    return this.stack[base + idx];
}
