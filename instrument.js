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

Instrument.enter["com/sun/midp/ssl/SSLStreamConnection.<init>.(Ljava/lang/String;ILjava/io/InputStream;Ljava/io/OutputStream;Lcom/sun/midp/pki/CertStore;)V"] = function(caller, callee) {
  var _this = caller.stack.read(6);
  _this.logBuffer = "";
};

Instrument.enter["com/sun/midp/ssl/Out.write.(I)V"] = function(caller, callee) {
  var _this = caller.stack.read(3);
  var connection = _this.class.getField("ssc", "Lcom/sun/midp/ssl/SSLStreamConnection;").get(_this);
  connection.logBuffer += String.fromCharCode(callee.stack.read(1));
};

Instrument.enter["com/sun/midp/ssl/Out.write.([BII)V"] = function(caller, callee) {
  var len = caller.stack.read(1), off = caller.stack.read(2), b = caller.stack.read(3), _this = caller.stack.read(4);
  var connection = _this.class.getField("ssc", "Lcom/sun/midp/ssl/SSLStreamConnection;").get(_this);
  var range = b.subarray(off, off + len);
  for (var i = 0; i < range.length; i++) {
    connection.logBuffer += String.fromCharCode(range[i] & 0xff);
  }
};

Instrument.exit["com/sun/midp/ssl/In.read.()I"] = function(caller, callee) {
  var _this = caller.stack.read(3);
  var connection = _this.class.getField("ssc", "Lcom/sun/midp/ssl/SSLStreamConnection;").get(_this);
  connection.logBuffer += String.fromCharCode(callee.stack.read(1));
};

Instrument.exit["com/sun/midp/ssl/In.read.([BII)I"] = function(caller, callee) {
  var len = caller.stack.read(4), off = caller.stack.read(5), b = caller.stack.read(6), _this = caller.stack.read(7);
  var connection = _this.class.getField("ssc", "Lcom/sun/midp/ssl/SSLStreamConnection;").get(_this);
  var range = b.subarray(off, off + len);
  for (var i = 0; i < range.length; i++) {
    connection.logBuffer += String.fromCharCode(range[i] & 0xff);
  }
};

Instrument.enter["com/sun/midp/ssl/SSLStreamConnection.close.()V"] = function(caller, callee) {
  var _this = caller.stack.read(1);
  if ("logBuffer" in _this) {
    console.log("SSLStreamConnection:\n" + _this.logBuffer);
    delete _this.logBuffer;
  }
};
