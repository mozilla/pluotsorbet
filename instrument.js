/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var Instrument = {
  enter: {},
  exit: {},

  profile: {},

  getKey: function(methodInfo) {
    return methodInfo.classInfo.className + "." + methodInfo.name + "." + methodInfo.signature;
  },

  callEnterHooks: function(methodInfo, caller, callee) {
    var key = this.getKey(methodInfo);
    if (Instrument.enter[key]) {
      Instrument.enter[key](caller, callee);
    }

    var now = Date.now();

    if (caller.profileData) {
      caller.profileData.cost += now - caller.profileData.then;
    }

    callee.profileData = {
      cost: 0,
      then: now,
    };
  },

  callExitHooks: function(methodInfo, caller, callee) {
    var key = this.getKey(methodInfo);
    var now = Date.now();

    callee.profileData.cost += now - callee.profileData.then;
    var times = this.profile[key] || (this.profile[key] = []);
    times.push(callee.profileData.cost);

    if (caller.profileData) {
      caller.profileData.then = now;
    }

    if (Instrument.exit[key]) {
      Instrument.exit[key](caller, callee);
    }
  },

  callPauseHooks: function(frame) {
    if (frame.profileData) {
      frame.profileData.cost += Date.now() - frame.profileData.then;
    }
  },

  callResumeHooks: function(frame) {
    if (frame.profileData) {
      frame.profileData.then = Date.now();
    }
  },

  reportProfile: function() {
    var methods = [];

    for (var key in this.profile) {
      var time = this.profile[key].reduce(function(p, c) { return p + c }, 0);
      methods.push({
        key: key,
        count: this.profile[key].length,
        time: time,
      });
    }

    methods.sort(function(a, b) { return b.time - a.time });

    methods.forEach(function(method) {
      console.log(method.time + " " + method.count + " " + method.key);
    });
  }
};

Instrument.enter["com/sun/midp/ssl/SSLStreamConnection.<init>.(Ljava/lang/String;ILjava/io/InputStream;Ljava/io/OutputStream;Lcom/sun/midp/pki/CertStore;)V"] = function(caller, callee) {
  var _this = caller.stack.read(6), port = caller.stack.read(4), host = util.fromJavaString(caller.stack.read(5));
  _this.logBuffer = "SSLStreamConnection to " + host + ":" + port + ":\n";
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
    console.log(_this.logBuffer);
    delete _this.logBuffer;
  }
};
