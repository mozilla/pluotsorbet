/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var Object = require("java/lang/Object");

var StringBuilder = module.exports = function(p) {
    if (this instanceof StringBuilder) {
        if (typeof p === "number") {
            this._buf = new Array(p).join(' ');
        } else {
            this._buf = p || "";
        }
    } else {
        return new StringBuilder(p);
    }
}

util.inherits(StringBuilder, Object);

StringBuilder.getClassName = function() {
    return "java/lang/StringBuilder";
}
 

StringBuilder.prototype["<init>"] = function() {
    for(var i=0; i<arguments.length; i++) {
        this._buf += arguments[i].toString();
    }
    return this;
}

StringBuilder.prototype["append"] = function() {
    for(var i=0; i<arguments.length; i++) {
        this._buf += arguments[i].toString();
    }
    return this;
}

StringBuilder.prototype["toString"] = function() {
    return this._buf.toString();
}