/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

// Only add setZeroTimeout to the window object, and hide everything
// else in a closure.
(function() {
    var resolved = Promise.resolve();

    // Like setTimeout, but only takes a function argument.  There's
    // no time argument (always zero) and no arguments (you have to
    // use a closure).
    function setZeroTimeout(fn) {
        resolved.then(fn);
    }

    // Add the one thing we want added to the window object.
    window.setZeroTimeout = setZeroTimeout;
})();
