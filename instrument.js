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

    // Since we're (temporarily) exiting the caller, calculate how much time
    // we've spent in it so far.
    if (caller.profileData) {
      caller.profileData.callTime += now - caller.profileData.lastTime;
    }

    // Now initialize the profile data structure for the callee we're entering.
    callee.profileData = {
      callTime: 0,
      lastTime: now,
    };
  },

  callExitHooks: function(methodInfo, caller, callee) {
    var key = this.getKey(methodInfo);
    var now = Date.now();

    // Now that we're exiting the callee, calculate the last amount of time
    // we spent in it and then record the total time.
    callee.profileData.callTime += now - callee.profileData.lastTime;
    var times = this.profile[key] || (this.profile[key] = []);
    times.push(callee.profileData.callTime);

    // Now that we're re-entering the caller, start tracking the amount of time
    // we spend in it again.
    if (caller.profileData) {
      caller.profileData.lastTime = now;
    }

    if (Instrument.exit[key]) {
      Instrument.exit[key](caller, callee);
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
