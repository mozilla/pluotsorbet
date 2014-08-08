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
  throw VM.Pause;
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

Runtime.prototype.newPrimitiveArray = function(type, size) {
  var constructor = ARRAYS[type];
  if (!constructor.prototype.class)
    CLASSES.initPrimitiveArrayType(type, constructor);
  return new constructor(size);
}

Runtime.prototype.newArray = function(typeName, size) {
  return new (CLASSES.getClass(typeName).constructor)(size);
}

Runtime.prototype.newMultiArray = function(typeName, lengths) {
  var length = lengths[0];
  var array = this.newArray(typeName, length);
  if (lengths.length > 1) {
    lengths = lengths.slice(1);
    for (var i=0; i<length; i++)
      array[i] = this.newMultiArray(typeName.substr(1), lengths);
  }
  return array;
}

Runtime.prototype.newObject = function(classInfo) {
    return new (classInfo.constructor)();
}

Runtime.prototype.newString = function(s) {
  var obj = this.newObject(CLASSES.java_lang_String);
  var length = s.length;
  var chars = this.newPrimitiveArray("C", length);
  for (var n = 0; n < length; ++n)
    chars[n] = s.charCodeAt(n);
  CLASSES.java_lang_String.getField("value", "[C").set(obj, chars);
  CLASSES.java_lang_String.getField("offset", "I").set(obj, 0);
  CLASSES.java_lang_String.getField("count", "I").set(obj, length);
  return obj;
}

Runtime.prototype.setStatic = function(field, value) {
  this.staticFields[field.id] = value;
}

Runtime.prototype.getStatic = function(field) {
  return this.staticFields[field.id];
}
