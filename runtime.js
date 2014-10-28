/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

function Runtime(vm) {
  this.vm = vm;
  this.status = 1; // NEW
  this.waiting = [];
  this.threadCount = 0;
  this.initialized = {};
  this.pending = {};
  this.staticFields = {};
  this.classObjects = {};
}

Runtime.prototype.waitStatus = function(callback) {
  this.waiting.push(callback);
}

Runtime.prototype.updateStatus = function(status) {
  this.status = status;
  var waiting = this.waiting;
  this.waiting = [];
  waiting.forEach(function(callback) {
    try {
      callback();
    } catch(ex) {
      // If the callback calls Runtime.prototype.waitStatus to continue waiting,
      // then waitStatus will throw VM.Pause, which shouldn't propagate up to
      // the caller of Runtime.prototype.updateStatus, so we silently ignore it
      // (along with any other exceptions thrown by the callback, so they don't
      // propagate to the caller of updateStatus).
    }
  });
}

Runtime.all = new Set();

Runtime.prototype.addContext = function(ctx) {
  ++this.threadCount;
  Runtime.all.add(this);
}

Runtime.prototype.removeContext = function(ctx) {
  if (!--this.threadCount) {
    Runtime.all.delete(this);
    this.updateStatus(4); // STOPPED
  }
}

Runtime.prototype.setStatic = function(field, value) {
  this.staticFields[field.id] = value;
}

Runtime.prototype.getStatic = function(field) {
  return this.staticFields[field.id];
}
