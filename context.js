/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

function Context() {
  this.frames = [];
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
  return callee;
}

Context.prototype.popFrame = function() {
  var callee = this.frames.pop();
  var caller = this.current();
  if (callee.localsBase)
    caller.stack.length = callee.localsBase;
  return caller;
}

Context.prototype.pushClassInitFrame = function(classInfo) {
  if (classInfo.initialized)
    return;
  classInfo.thread = this.thread;
  var syntheticMethod = {
    classInfo: {
      constant_pool: [
        null,
        { class_index: 2, name_and_type_index: 4 },
        { name_index: 3 },
        { bytes: "java/lang/Class" },
        { name_index: 5, signature_index: 6 },
        { bytes: "invoke_clinit" },
        { bytes: "()V" },
        { class_index: 2, name_and_type_index: 8 },
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
  this.current().stack.push(classInfo.getClassObject());
  this.pushFrame(syntheticMethod, 1);
}

Context.prototype.backTrace = function() {
  var stack = [];
  this.frames.forEach(function(frame) {
    var methodInfo = frame.methodInfo;
    if (!methodInfo || !methodInfo.name)
      return;
    var className = methodInfo.classInfo.className;
    var methodName = methodInfo.name;
    var signature = Signature.parse(methodInfo.signature);
    var IN = signature.IN;
    var args = [];
    var lp = 0;
    for (var n = 0; n < IN.length; ++n) {
      var arg = frame.locals[frame.localsBase + lp];
      ++lp;
      switch (IN[n].type) {
      case "long":
      case "double":
        ++lp;
        break;
      case "object":
        if (arg === null)
          arg = "null";
        else if (arg.class.className === "java/lang/String")
          arg = "'" + util.fromJavaString(arg) + "'";
        else
          arg = "<" + arg.class.className + ">";
      }
      args.push(arg);
    }
    stack.push(methodInfo.classInfo.className + "." + methodInfo.name + ":" + frame.ip +
               "(" + args.join(",") + ")");
  });
  return stack.join("\n");
}

Context.prototype.raiseException = function(className, message) {
  if (!message)
    message = "";
  message = "" + message;
  var syntheticMethod = {
    classInfo: {
      constant_pool: [
        null,
        { name_index: 2 },
        { bytes: className },
        { tag: TAGS.CONSTANT_String, string_index: 4 },
        { bytes: message },
        { class_index: 1, name_and_type_index: 6 },
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
  throw VM.Yield;
}

Context.prototype.newString = function(s) {
  var obj = CLASSES.newObject(CLASSES.java_lang_String);
  var length = s.length;
    var chars = CLASSES.newPrimitiveArray("C", length);
  for (var n = 0; n < length; ++n)
    chars[n] = s.charCodeAt(n);
  obj["java/lang/String$value"] = chars;
  obj["java/lang/String$offset"] = 0;
  obj["java/lang/String$count"] = length;
  return obj;
}

Context.prototype.execute = function(stopFrame) {
  while (this.current() !== stopFrame) {
    try {
      VM.execute(this);
    } catch (e) {
      switch (e) {
      case VM.Yield:
        break;
      case VM.Pause:
        return;
      default:
        throw e;
      }
    }
  }
}

Context.prototype.start = function(stopFrame) {
  if (this.current() === stopFrame)
    return;
  var ctx = this;
  ctx.stopFrame = stopFrame;
  window.setZeroTimeout(function() {
    try {
      VM.execute(ctx);
    } catch (e) {
      switch (e) {
      case VM.Yield:
        break;
      case VM.Pause:
        return;
      default:
        throw e;
      }
    }
    ctx.start(stopFrame);
  });
}

Context.prototype.resume = function() {
  this.start(this.stopFrame);
}

Context.prototype.block = function(obj, queue) {
  if (!obj[queue])
    obj[queue] = [];
  obj[queue].push(this);
  throw VM.Pause;
}

Context.prototype.monitorEnter = function(obj) {
  var lock = obj.lock;
  if (!lock) {
    obj.lock = { thread: this.thread, count: 1 };
    return;
  }
  if (lock.thread === this.thread) {
    ++lock.count;
    return;
  }
  this.block(obj, "ready");
}

Context.prototype.monitorExit = function(obj) {
  var lock = obj.lock;
  if (lock.thread !== this.thread)
    this.raiseException("java/lang/IllegalMonitorStateException");
  if (--lock.count > 0) {
    return;
  }
  obj.lock = null;
  if (obj.ready && obj.ready.length) {
    var ctx = obj.ready.pop();
    ctx.monitorEnter(obj);
    ctx.resume();
  }
}

Context.prototype.wait = function(obj, timeout) {
  if (!obj.lock || obj.lock.thread !== this.thread || obj.lock.count !== 1)
    this.raiseException("java/lang/IllegalMonitorStateException");
  this.monitorExit(obj);
  this.block(obj, "waiting");
}

Context.prototype.notify = function(obj, notifyAll) {
  if (!obj.lock || obj.lock.thread !== this.thread)
    this.raiseException("java/lang/IllegalMonitorStateException");
  while (obj.waiting && obj.waiting.length) {
    if (!obj.ready)
      obj.ready = [];
    obj.ready.push(obj.waiting.pop());
    if (!notifyAll)
      break;
  }
}
