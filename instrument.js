/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var Instrument = {
  enter: {},
  exit: {},

  getKey: function(methodInfo) {
    return methodInfo.classInfo.className + "." + methodInfo.name + "." + methodInfo.signature;
  },

  callEnterHooks: function(methodInfo, caller, callee) {
    var key = this.getKey(methodInfo);
    if (Instrument.enter[key]) {
      Instrument.enter[key](caller, callee);
    }
  },

  callExitHooks: function(methodInfo, caller, callee) {
    var key = this.getKey(methodInfo);
    if (Instrument.exit[key]) {
      Instrument.exit[key](caller, callee);
    }
  },
};

Instrument.enter["com/sun/midp/ssl/Out.write.(I)V"] = function(caller, callee) {
  console.print(String.fromCharCode(caller.stack.read(1)) & 0xff);
};

Instrument.enter["com/sun/midp/ssl/Out.write.([BII)V"] = function(caller, callee) {
  var len = caller.stack.read(1), off = caller.stack.read(2), b = caller.stack.read(3);
  var range = b.subarray(off, off + len);
  for (var i = 0; i < range.length; i++) {
    console.print(range[i] & 0xff);
  }
};

Instrument.exit["com/sun/midp/ssl/In.read.()I"] = function(caller, callee) {
  console.print(caller.stack.read(1));
};

Instrument.enter["com/sun/midp/ssl/In.read.([BII)I"] = function(caller, callee) {
  var len = caller.stack.read(1), off = caller.stack.read(2), b = caller.stack.read(3);
  var range = b.subarray(off, off + len);
  for (var i = 0; i < range.length; i++) {
    console.print(range[i] & 0xff);
  }
};
