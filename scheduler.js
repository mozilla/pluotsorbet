/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var MODE = {
    NORMAL: 0,
    SYNC:   1,
    YIELD:  2
};

var Scheduler = function(mticks) {
    if (this instanceof Scheduler) {
        this._ticks = 0;
        this._mode = MODE.NORMAL;
    } else {
        return new Scheduler(mticks);
    }
}

Scheduler.prototype.tick = function(pid, fn) {
    switch(this._mode) {
        case MODE.SYNC:
            fn();
            break;
        case MODE.YIELD:
            this._mode = MODE.NORMAL;
            this._ticks = 0;
            setTimeout(fn, 0);
            break;
        case MODE.NORMAL:
            if (++this._ticks > THREADS.getThread(pid).getPriority()) {
                this._ticks = 0;
                setTimeout(fn, 0);
            } else {
                fn();
            }
            break;
    }
}

Scheduler.prototype.yield = function() {
    this._mode = MODE.YIELD;
}

Scheduler.prototype.sync = function(fn) {
    this._mode = MODE.SYNC;
    fn();
    this._mode = MODE.NORMAL;
}

