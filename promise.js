/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

function Promise() {
}

Promise.prototype.return = function() {
  if (this.s) {
    this.s.forEach(function (fn) {
        fn();
    });
  }
}

Promise.prototype.throw = function() {
  if (this.s) {
    this.s.forEach(function (fn) {
        fn();
    });
  }
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
