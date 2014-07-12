/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var Stack = function() {
    this.array = [];
}

Stack.prototype.push = function (value) {
    this.array.push(value);
}

Stack.prototype.pop = function (value) {
    return this.array.pop();
}

Stack.prototype.push2 = function (value) {
    this.array.push(null);
    this.array.push(value);
}

Stack.prototype.pop2 = function (value) {
    var result = this.array.pop();
    this.array.pop();
    return result;
}

