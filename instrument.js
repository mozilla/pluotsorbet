/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var Instrument = {
  enter: {},
  exit: {},

  profiling: false,
  profile: null,
  asyncProfile: null,

  enabled: "instrument" in config && !/no|0/.test(config.instrument),

  callEnterHooks: function(methodInfo, caller, callee) {
    if (this.enabled) {
      var key = methodInfo.implKey;
      if (Instrument.enter[key]) {
        Instrument.enter[key](caller, callee);
      }
    }

    if (this.profiling) {
      var now = performance.now();

      if (caller.profileData && caller.profileData.then) {
        caller.profileData.cost += now - caller.profileData.then;
        caller.profileData.then = null;
      }

      callee.profileData = {
        cost: 0,
        then: now,
      };
    }
  },

  callExitHooks: function(methodInfo, caller, callee) {
    var key = methodInfo.implKey;

    if (this.profiling) {
      var now = performance.now();

      if (callee.profileData) {
        if (callee.profileData.then) {
          callee.profileData.cost += now - callee.profileData.then;
          callee.profileData.then = null;
        }

        var methodProfileData = this.profile[key] || (this.profile[key] = { count: 0, cost: 0 });
        methodProfileData.count++;
        methodProfileData.cost += callee.profileData.cost;
      }

      if (caller.profileData) {
        caller.profileData.then = now;
      }
    }

    if (this.enabled) {
      if (Instrument.exit[key]) {
        Instrument.exit[key](caller, callee);
      }
    }
  },

  callPauseHooks: function(frame) {
    if (this.profiling && frame.profileData && frame.profileData.then) {
        frame.profileData.cost += performance.now() - frame.profileData.then;
        frame.profileData.then = null;
    }
  },

  callResumeHooks: function(frame) {
    if (this.profiling && frame.profileData) {
      frame.profileData.then = performance.now();
    }
  },

  enterAsyncNative: function(key, promise) {
    var profileData = this.asyncProfile[key] || (this.asyncProfile[key] = { count: 0, cost: 0 });
    promise.startTime = performance.now();
  },

  exitAsyncNative: function(key, promise) {
    var profileData = this.asyncProfile[key];
    if (!profileData || !promise.startTime) {
      // Ignore native without profile data, which can happen when you start
      // profiling while the native is pending.
      return;
    }
    profileData.count++;
    profileData.cost += performance.now() - promise.startTime;
  },

  startProfile: function() {
    this.profile = {};
    this.asyncProfile = {};
    this.profiling = true;
  },

  printProfile: function(aProfile, aProfileName) {
    var methods = [];

    for (var key in aProfile) {
      methods.push({
        key: key,
        count: aProfile[key].count,
        cost: aProfile[key].cost,
      });
    }

    methods.sort(function(a, b) { return b.cost - a.cost });

    console.log(aProfileName + ":");
    methods.forEach(function(method) {
      console.log(Math.round(method.cost) + "ms " + method.count + " " + method.key);
    });
  },

  stopProfile: function() {
    this.printProfile(this.profile, "Profile");
    this.printProfile(this.asyncProfile, "Async natives profile");

    this.profiling = false;
  },

  measure: function(alternateImpl, ctx, methodInfo) {
    var key = methodInfo.implKey;
    if (this.profiling) {
      var then = performance.now();
      alternateImpl.call(null, ctx, ctx.current().stack, methodInfo.isStatic);
      var methodProfileData = this.profile[key] || (this.profile[key] = { count: 0, cost: 0 });
      methodProfileData.count++;
      methodProfileData.cost += performance.now() - then;
    } else {
      alternateImpl.call(null, ctx, ctx.current().stack, methodInfo.isStatic);
    }
  },
};

Instrument.enter["com/sun/midp/ssl/SSLStreamConnection.<init>.(Ljava/lang/String;ILjava/io/InputStream;Ljava/io/OutputStream;Lcom/sun/midp/pki/CertStore;)V"] = function(caller, callee) {
  var _this = callee.locals.read(6), port = callee.locals.read(4), host = util.fromJavaString(callee.locals.read(5));
  _this.logBuffer = "SSLStreamConnection to " + host + ":" + port + ":\n";
};

Instrument.enter["com/sun/midp/ssl/Out.write.(I)V"] = function(caller, callee) {
  var _this = callee.locals.read(3);
  var connection = _this.klass.classInfo.getField("I.ssc.Lcom/sun/midp/ssl/SSLStreamConnection;").get(_this);
  connection.logBuffer += String.fromCharCode(callee.stack.read(1));
};

Instrument.enter["com/sun/midp/ssl/Out.write.([BII)V"] = function(caller, callee) {
  var len = callee.locals.read(1), off = callee.locals.read(2), b = callee.locals.read(3), _this = callee.locals.read(4);
  var connection = _this.klass.classInfo.getField("I.ssc.Lcom/sun/midp/ssl/SSLStreamConnection;").get(_this);
  var range = b.subarray(off, off + len);
  for (var i = 0; i < range.length; i++) {
    if (range[i] == 0) {
      break;
    }
    connection.logBuffer += String.fromCharCode(range[i] & 0xff);
  }
};

Instrument.exit["com/sun/midp/ssl/In.read.()I"] = function(caller, callee) {
  // We can't use caller.stack.read() here, because the length of the caller's
  // stack differs depending on whether or not In.read threw an exception.
  var _this = caller.stack[2];

  var connection = _this.klass.classInfo.getField("I.ssc.Lcom/sun/midp/ssl/SSLStreamConnection;").get(_this);
  connection.logBuffer += String.fromCharCode(callee.stack.read(1));
};

Instrument.exit["com/sun/midp/ssl/In.read.([BII)I"] = function(caller, callee) {
  var len = callee.locals.read(4), off = callee.locals.read(5), b = callee.locals.read(6), _this = callee.locals.read(7);
  var connection = _this.klass.classInfo.getField("I.ssc.Lcom/sun/midp/ssl/SSLStreamConnection;").get(_this);
  var range = b.subarray(off, off + len);
  for (var i = 0; i < range.length; i++) {
    if (range[i] == 0) {
      break;
    }
    connection.logBuffer += String.fromCharCode(range[i] & 0xff);
  }
};

Instrument.enter["com/sun/midp/ssl/SSLStreamConnection.close.()V"] = function(caller, callee) {
  var _this = callee.locals.read(1);
  if ("logBuffer" in _this) {
    console.log(_this.logBuffer);
    delete _this.logBuffer;
  }
};
