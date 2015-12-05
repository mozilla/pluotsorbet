/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

// Only add nextTickBeforeEvents to the window object, and hide everything
// else in a closure.
(function() {
    var resolved = Promise.resolve();

    // Like setTimeout, but only takes a function argument.  There's
    // no time argument (always zero) and no arguments (you have to
    // use a closure).
    function nextTickBeforeEvents(fn) {
        resolved.then(fn);
    }

    // Add the one thing we want added to the window object.
    window.nextTickBeforeEvents = nextTickBeforeEvents;
})();

(function() {
  var cbs = [];
  var msg = 135921;

  function nextTickDuringEvents(fn) {
    cbs.push(fn);
    window.postMessage(msg, "*");
  }

  function recv(ev) {
    if (window !== ev.source || ev.data !== msg) {
      return;
    }

    ev.stopPropagation();
    cbs.shift()();
  }

  window.addEventListener("message", recv, true);

  window.nextTickDuringEvents = window.postMessage ? nextTickDuringEvents : setTimeout;
})();
