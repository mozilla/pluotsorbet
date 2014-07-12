/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var Scheduler = function() {
    if (this instanceof Scheduler) {
        this._ticks = 0;
        this._sync = false;
        this._yieldException = {};
    } else {
        return new Scheduler();
    }
}

Scheduler.prototype.yield = function(pid) {
    if (!this._sync && ++this._ticks > THREADS.getThread(pid).getPriority()) {
        this._ticks = 0;
        throw this._yieldException;
    }
}

Scheduler.prototype.spawn = function(fn) {
    try {
        fn();
    } catch (e) {
        if (e !== this._yieldException)
            throw e;
        setTimeout(fn, 0);
    }
}

Scheduler.prototype.sync = function(fn) {
    this._sync = true;
    fn();
    this._sync = false;
}

