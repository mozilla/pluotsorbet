/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var Thread = function(name) {
    if (this instanceof Thread) {
        this.name = name || "noname";
        this.priority = (Thread.MAX_PRIORITY + Thread.MIN_PRIORITY) >> 1;
    } else {
        return new Thread(name);
    }
}

Thread.prototype.setName = function(name) {
    this.name = name;
}

Thread.prototype.getName = function(name) {
    return this.name;
}

Thread.MIN_PRIORITY = 0;
Thread.MAX_PRIORITY = 100;

Thread.prototype.setPriority = function(priority) {
    this.priority = priority;
}

Thread.prototype.getPriority = function() {
    return this.priority;
}

