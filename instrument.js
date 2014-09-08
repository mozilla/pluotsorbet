/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var Instrument = {
  enter: {},
  exit: {},
};

Instrument.enter["com/sun/midp/ssl/Out.write.(I)V"] = function(caller, callee) {
  console.print(String.fromCharCode(caller.stack[caller.stack.length - 1]) & 0xff);
};

Instrument.enter["com/sun/midp/ssl/Out.write.([BII)V"] = function(caller, callee) {
  var len = caller.stack[caller.stack.length - 1],
      off = caller.stack[caller.stack.length - 2],
      b = caller.stack[caller.stack.length - 3];
  var range = b.subarray(off, off + len);

  for (var i = 0; i < range.length; i++) {
    console.print(range[i] & 0xff);
  }
};

Instrument.exit["com/sun/midp/ssl/In.read.()I"] = function(caller, callee) {
  console.print(callee.stack[0]);
};

Instrument.enter["com/sun/midp/ssl/In.read.([BII)I"] = function(caller, callee) {
  var len = caller.stack[caller.stack.length - 1],
      off = caller.stack[caller.stack.length - 2],
      b = caller.stack[caller.stack.length - 3];
  var range = b.subarray(off, off + len);

  for (var i = 0; i < range.length; i++) {
    console.print(range[i] & 0xff);
  }
};
