/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

if (!Math.fround) {
    Math.fround = (function() {
        var fa = new Float32Array(1);
        return function(v) {
            fa[0] = Math.round(v);
            return fa[0];
        }
    })();
}

if (!String.prototype.contains) {
    String.prototype.contains = function() {
        return String.prototype.indexOf.apply(this, arguments) !== -1;
    };
}
