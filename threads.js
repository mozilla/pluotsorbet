/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var Threads = function() {
    this.threads = [];
    this.empty = [];
    this.ready = [];
    var mainThread = new Thread("main");
    this.add(mainThread);
    this.current = mainThread;
    window.addEventListener("message", function () {
        this.resume();
    }, false);
}

Threads.prototype.add = function(thread) {
    if (this.empty.length > 0) {
        var pid = this.empty.pop();
        this.threads[pid] = thread;
        return pid;
    } else {
        return this.threads.push(thread) - 1;
    }
}

Threads.prototype.remove = function(pid) {
    this.empty.push(pid);
    this.threads[pid] = null;
}

Threads.prototype.count = function() {
    return this.threads.length - this.empty.length;
}

Threads.prototype.getThread = function(pid) {
    return this.threads[pid];
}

Threads.prototype.yield = function(frame) {
    if (this.ready.length) {
        this.current.frame = frame;
        this.ready.unshift(this.current);
        this.current = this.ready[this.ready.length - 1];
    }
    window.postMessage(null, "*");
}

Threads.prototype.resume = function() {
    VM.resume(this.current.frame);
}
