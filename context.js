/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

function Context(runtime) {
  this.frames = [];
  this.runtime = runtime;
  this.runtime.addContext(this);
}

Context.prototype.kill = function() {
  this.runtime.removeContext(this);
}

Context.prototype.current = function() {
  var frames = this.frames;
  return frames[frames.length - 1];
}

Context.prototype.pushFrame = function(methodInfo, consumes) {
  var caller = this.current();
  var callee = new Frame(methodInfo);
  callee.locals = caller.stack;
  callee.localsBase = caller.stack.length - consumes;
  this.frames.push(callee);
  Instrument.callEnterHooks(methodInfo, caller, callee);
  return callee;
}

Context.prototype.popFrame = function() {
  var callee = this.frames.pop();
  var caller = this.current();
  Instrument.callExitHooks(callee.methodInfo, caller, callee);
  if (callee.localsBase)
    caller.stack.length = callee.localsBase;
  return caller;
}

Context.prototype.pushClassInitFrame = function(classInfo) {
  if (this.runtime.initialized[classInfo.className])
    return;
  classInfo.thread = this.thread;
  var syntheticMethod = {
    syntheticKey: "ClassInitSynthetic:" + classInfo.className,
    classInfo: {
      constant_pool: [
        null,
        { tag: TAGS.CONSTANT_Methodref, class_index: 2, name_and_type_index: 4 },
        { tag: TAGS.CONSTANT_Class, name_index: 3 },
        { bytes: "java/lang/Class" },
        { name_index: 5, signature_index: 6 },
        { bytes: "invoke_clinit" },
        { bytes: "()V" },
        { tag: TAGS.CONSTANT_Methodref, class_index: 2, name_and_type_index: 8 },
        { name_index: 9, signature_index: 10 },
        { bytes: "init9" },
        { bytes: "()V" },
      ],
    },
    code: [
        0x2a,             // aload_0
        0x59,             // dup
        0x59,             // dup
        0x59,             // dup
        0xc2,             // monitorenter
        0xb7, 0x00, 0x01, // invokespecial <idx=1>
        0xb7, 0x00, 0x07, // invokespecial <idx=7>
        0xc3,             // monitorexit
        0xb1,             // return
    ],
    exception_table: [],
  };
  this.current().stack.push(classInfo.getClassObject(this));
  this.pushFrame(syntheticMethod, 1);
}

Context.prototype.raiseException = function(className, message) {
  if (!message)
    message = "";
  message = "" + message;
  var syntheticMethod = {
    syntheticKey: "RaiseExceptionSynthetic",
    classInfo: {
      constant_pool: [
        null,
        { tag: TAGS.CONSTANT_Class, name_index: 2 },
        { bytes: className },
        { tag: TAGS.CONSTANT_String, string_index: 4 },
        { bytes: message },
        { tag: TAGS.CONSTANT_Methodref, class_index: 1, name_and_type_index: 6 },
        { name_index: 7, signature_index: 8 },
        { bytes: "<init>" },
        { bytes: "(Ljava/lang/String;)V" },
      ],
    },
    code: [
      0xbb, 0x00, 0x01, // new <idx=1>
      0x59,             // dup
      0x12, 0x03,       // ldc <idx=2>
      0xb7, 0x00, 0x05, // invokespecial <idx=5>
      0xbf              // athrow
    ],
    exception_table: [],
  };
  this.pushFrame(syntheticMethod, 0);
}

Context.prototype.raiseExceptionAndYield = function(className, message) {
  this.raiseException(className, message);
  throw VM.Yield;
}

Context.prototype.execute = function(stopFrame) {
  Instrument.callResumeHooks(this.current());
  while (this.current() !== stopFrame) {
    try {
      VM.execute(this);
    } catch (e) {
      switch (e) {
      case VM.Yield:
        // Ignore the yield and continue executing instructions on this thread.
        break;
      case VM.Pause:
        Instrument.callPauseHooks(this.current());
        return;
      default:
        throw e;
      }
    }
  }
}

Context.prototype.start = function(stopFrame) {
  if (this.current() === stopFrame) {
    this.kill();
    return;
  }
  var ctx = this;
  ctx.stopFrame = stopFrame;
  window.setZeroTimeout(function() {
    Instrument.callResumeHooks(ctx.current());
    try {
      VM.execute(ctx);
    } catch (e) {
      switch (e) {
      case VM.Yield:
        break;
      case VM.Pause:
        Instrument.callPauseHooks(ctx.current());
        return;
      default:
        console.info(e);
        throw e;
      }
    }
    Instrument.callPauseHooks(ctx.current());
    ctx.start(stopFrame);
  });
}

Context.prototype.resume = function() {
  this.start(this.stopFrame);
}

Context.prototype.block = function(obj, queue, lockLevel) {
  if (!obj[queue])
    obj[queue] = [];
  obj[queue].push(this);
  this.lockLevel = lockLevel;
  throw VM.Pause;
}

Context.prototype.unblock = function(obj, queue, notifyAll, callback) {
  while (obj[queue] && obj[queue].length) {
    var ctx = obj[queue].pop();
    if (!ctx)
      continue;
    callback(ctx);
    if (!notifyAll)
      break;
  }
}

Context.prototype.wakeup = function(obj) {
  if (this.lockTimeout !== null) {
    window.clearTimeout(this.lockTimeout);
    this.lockTimeout = null;
  }
  if (obj.lock) {
    if (!obj.ready)
      obj.ready = [];
    obj.ready.push(this);
  } else {
    while (this.lockLevel-- > 0)
      this.monitorEnter(obj);
    this.resume();
  }
}

Context.prototype.monitorEnter = function(obj) {
  var lock = obj.lock;
  if (!lock) {
    obj.lock = { thread: this.thread, level: 1 };
    return;
  }
  if (lock.thread === this.thread) {
    ++lock.level;
    return;
  }
  this.block(obj, "ready", 1);
}

Context.prototype.monitorExit = function(obj) {
  var lock = obj.lock;
  if (lock.thread !== this.thread)
    this.raiseExceptionAndYield("java/lang/IllegalMonitorStateException");
  if (--lock.level > 0) {
    return;
  }
  obj.lock = null;
  this.unblock(obj, "ready", false, function(ctx) {
    ctx.wakeup(obj);
  });
}

Context.prototype.wait = function(obj, timeout) {
  var lock = obj.lock;
  if (!lock || lock.thread !== this.thread)
    this.raiseExceptionAndYield("java/lang/IllegalMonitorStateException");
  if (timeout < 0)
    this.raiseExceptionAndYield("java/lang/IllegalArgumentException");
  var lockLevel = lock.level;
  while (lock.level > 0)
    this.monitorExit(obj);
  if (timeout) {
    var self = this;
    this.lockTimeout = window.setTimeout(function() {
      obj.waiting.forEach(function(ctx, n) {
        if (ctx === self) {
          obj.waiting[n] = null;
          ctx.wakeup(obj);
        }
      });
    }, timeout);
  } else {
    this.lockTimeout = null;
  }
  this.block(obj, "waiting", lockLevel);
}

Context.prototype.notify = function(obj, notifyAll) {
  if (!obj.lock || obj.lock.thread !== this.thread)
    this.raiseExceptionAndYield("java/lang/IllegalMonitorStateException");
  this.unblock(obj, "waiting", notifyAll, function(ctx) {
    ctx.wakeup(obj);
  });
}

Context.prototype.newPrimitiveArray = function(type, size) {
  return this.runtime.newPrimitiveArray(type, size);
}

Context.prototype.newArray = function(typeName, size) {
  return this.runtime.newArray(typeName, size);
}

Context.prototype.newMultiArray = function(typeName, lengths) {
  return this.runtime.newMultiArray(typeName, lengths);
}

Context.prototype.newObject = function(classInfo) {
  return this.runtime.newObject(classInfo);
}

Context.prototype.newString = function(s) {
  return this.runtime.newString(s);
}

Context.prototype.newStringFromUint16Array = function(s) {
  return this.runtime.newStringFromUint16Array(s);
}
