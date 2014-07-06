/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var LEVELS = {
    DEBUG:  1<<0,
    ERROR:  1<<1,
    INFO:   1<<2,
    WARN:   1<<3,
    check: function(levels, level) {
        return (levels & level) === level;
    }
};

var Logger = function(levels) {
    if (this instanceof Logger) {
        this.levels = levels || ( LEVELS.DEBUG | LEVELS.ERROR | LEVELS.INFO | LEVELS.WARN );
    } else {
        return new Logger(levels);
    }
}

Logger.prototype.setLogLevel = function(levels) {
    this.levels = levels;
}

Logger.prototype.debug = function(msg) {
    if (LEVELS.check(this.levels, LEVELS.DEBUG)) {
        util.debug(msg);
    }
}

Logger.prototype.error = function(msg) {
    if (LEVELS.check(this.levels, LEVELS.ERROR)) {
        util.error(msg);
    }
}

Logger.prototype.info = function(msg) {
    if (LEVELS.check(this.levels, LEVELS.INFO)) {
        util.print("INFO: " + msg);
    }
}

Logger.prototype.warn = function(msg) {
    if (LEVELS.check(this.levels, LEVELS.WARN)) {
        util.print("WARN: " + msg);
    }
}