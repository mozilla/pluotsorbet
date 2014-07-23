/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

function Promise() {
}

Promise.prototype.done = function() {
  var s = this.s;
  if (!s)
    return;
  window.setZeroTimeout(function() {
    s.forEach(function(fn) {
      fn();
    });
  });
}

Promise.prototype.error = function() {
  var f = this.f;
  if (!f)
    return;
  window.setZeroTimeout(function() {
    f.forEach(function(fn) {
      fn();
    });
  });
}

Promise.prototype.then = function(success, fail) {
  if (success) {
    if (!this.s)
      this.s = [];
    this.s.push(success);
  }
  if (fail) {
    if (!this.f)
      this.f = [];
    this.f.push(fail);
  }
}
