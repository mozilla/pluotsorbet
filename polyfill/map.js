/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

if (!Map.prototype.clear) {
  Map.prototype.clear = function() {
    for (var keyVal of this) {
      this.delete(keyVal[0]);
    }
  };
}

if (!Map.prototype.forEach) {
  Map.prototype.forEach = function(callback, thisArg) {
    for (var keyVal of this) {
      callback.call(thisArg || null, keyVal[1], keyVal[0], this);
    }
  }
}
