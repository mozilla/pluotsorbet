/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

function Promise() {
}

Promise.prototype.fulfill = function(q, args) {
  if (!q)
    return this;
  var args = arguments;
  window.setZeroTimeout(function() {
    q.forEach(function(fn) {
      fn.call(null, args);
    });
  });
  return this;
}

Promise.prototype.enqueue = function(n, fn) {
  if (!fn)
    return;
  var q = this[n];
  if (!q)
    q = this[n] = [];
  q.push(fn);
}

Promise.prototype.done = function() {
  return this.fulfill(this.s, arguments);
}

Promise.prototype.error = function() {
  return this.fulfill(this.f, arguments);
}

Promise.prototype.then = function(success, fail) {
  this.enqueue("s", success);
  this.enqueue("f", fail);
  return this;
}
