module J2ME {
  declare var Frame;
  declare var VM;
  declare var Instrument;
  declare var setZeroTimeout;
  declare var CLASSES;
  declare var Long;
  declare var JavaException;

  export class Context {
    runtime: any;
    frames: any [];
    frameSets: any [];
    lockTimeout: number;
    lockLevel: number;
    thread: any;
    methodInfos: any;

    constructor(runtime: Runtime) {
      this.frames = [];
      this.frameSets = [];
      this.runtime = runtime;
      this.runtime.addContext(this);
    }

    kill() {
      this.runtime.removeContext(this);
    }

    current() {
      var frames = this.frames;
      return frames[frames.length - 1];
    }

    pushFrame(methodInfo: MethodInfo) {
      var caller = this.current();
      var callee;
      if (caller === undefined) {
        if (methodInfo.consumes !== 0) {
          throw new Error("A frame cannot consume arguments from a compiled frame.");
        }
        callee = new Frame(methodInfo, [], 0);
      } else {
        callee = new Frame(methodInfo, caller.stack.slice(caller.stack.length - methodInfo.consumes), 0);
        caller.stack.length -= methodInfo.consumes;
      }
      this.frames.push(callee);
      Instrument.callEnterHooks(methodInfo, caller, callee);
      return callee;
    }

    popFrame() {
      var callee = this.frames.pop();
      if (this.frames.length === 0) {
        return null;
      }
      var caller = this.current();
      Instrument.callExitHooks(callee.methodInfo, caller, callee);
      return caller;
    }

    executeNewFrameSet(frames) {
      this.frameSets.push(this.frames);
      this.frames = frames;
      try {
        var returnValue = VM.execute(this);
      } catch (e) {
        // Append all the current frames to the parent frame set, so a single frame stack
        // exists when the bailout finishes.
        var currentFrames = this.frames;
        this.frames = this.frameSets.pop();
        for (var i = 0; i < currentFrames.length; i++) {
          this.frames.push(currentFrames[i]);
        }
        throw e;
      }
      this.frames = this.frameSets.pop();
      return returnValue;
    }

    getClassInitFrame(classInfo) {
      if (this.runtime.initialized[classInfo.className])
        return;
      classInfo.thread = this.thread;
      var syntheticMethod = new MethodInfo({
        name: "ClassInitSynthetic",
        signature: "()V",
        isStatic: false,
        classInfo: {
          className: classInfo.className,
          vmc: {},
          vfc: {},
          constant_pool: [
            null,
            {tag: TAGS.CONSTANT_Methodref, class_index: 2, name_and_type_index: 4},
            {tag: TAGS.CONSTANT_Class, name_index: 3},
            {bytes: "java/lang/Class"},
            {name_index: 5, signature_index: 6},
            {bytes: "invoke_clinit"},
            {bytes: "()V"},
            {tag: TAGS.CONSTANT_Methodref, class_index: 2, name_and_type_index: 8},
            {name_index: 9, signature_index: 10},
            {bytes: "init9"},
            {bytes: "()V"},
          ],
        },
        code: new Uint8Array([
          0x2a,             // aload_0
          0x59,             // dup
          0x59,             // dup
          0x59,             // dup
          0xc2,             // monitorenter
          0xb7, 0x00, 0x01, // invokespecial <idx=1>
          0xb7, 0x00, 0x07, // invokespecial <idx=7>
          0xc3,             // monitorexit
          0xb1,             // return
        ])
      });
      return new Frame(syntheticMethod, [classInfo.getClassObject(this)], 0);
    }

    pushClassInitFrame(classInfo) {
      if (this.runtime.initialized[classInfo.className])
        return;
      var classInitFrame = this.getClassInitFrame(classInfo);
      this.executeNewFrameSet([classInitFrame]);
    }

    raiseException(className, message) {
      if (!message)
        message = "";
      message = "" + message;
      var syntheticMethod = new MethodInfo({
        name: "RaiseExceptionSynthetic",
        signature: "()V",
        isStatic: true,
        classInfo: {
          className: className,
          vmc: {},
          vfc: {},
          constant_pool: [
            null,
            {tag: TAGS.CONSTANT_Class, name_index: 2},
            {bytes: className},
            {tag: TAGS.CONSTANT_String, string_index: 4},
            {bytes: message},
            {tag: TAGS.CONSTANT_Methodref, class_index: 1, name_and_type_index: 6},
            {name_index: 7, signature_index: 8},
            {bytes: "<init>"},
            {bytes: "(Ljava/lang/String;)V"},
          ],
        },
        code: new Uint8Array([
          0xbb, 0x00, 0x01, // new <idx=1>
          0x59,             // dup
          0x12, 0x03,       // ldc <idx=2>
          0xb7, 0x00, 0x05, // invokespecial <idx=5>
          0xbf              // athrow
        ])
      });
      //  pushFrame() is not used since the invoker may be a compiled frame.
      var callee = new Frame(syntheticMethod, [], 0);
      this.frames.push(callee);
    }

    raiseExceptionAndYield(className, message?) {
      this.raiseException(className, message);
      throw VM.Yield;
    }

    invokeCompiledFn(methodInfo, args) {
      args.unshift(this);
      var fn = methodInfo.fn;
      this.frameSets.push(this.frames);
      this.frames = [];
      var returnValue = fn.apply(null, args);
      this.frames = this.frameSets.pop();
      return returnValue;
    }

    compileMethodInfo(methodInfo) {
      var fn = J2ME.compileMethodInfo(methodInfo, this, J2ME.CompilationTarget.Runtime);
      if (fn) {
        methodInfo.fn = fn;
      } else {
        methodInfo.dontCompile = true;
      }
    }

    setCurrent() {
      $ = this.runtime;
      $.ctx = this;
    }

    execute() {
      Instrument.callResumeHooks(this.current());
      this.setCurrent();
      do {
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
      } while (this.frames.length !== 0);
    }

    start() {
      var ctx = this;
      this.setCurrent();
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

      if (ctx.frames.length === 0) {
        ctx.kill();
        return;
      }

      ctx.resume();
    }

    resume() {
      (<any>window).setZeroTimeout(this.start.bind(this));
    }

    block(obj, queue, lockLevel) {
      if (!obj[queue])
        obj[queue] = [];
      obj[queue].push(this);
      this.lockLevel = lockLevel;
      throw VM.Pause;
    }

    unblock(obj, queue, notifyAll, callback) {
      while (obj[queue] && obj[queue].length) {
        var ctx = obj[queue].pop();
        if (!ctx)
          continue;
        callback(ctx);
        if (!notifyAll)
          break;
      }
    }

    wakeup(obj) {
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

    monitorEnter(obj) {
      var lock = obj.lock;
      if (!lock) {
        obj.lock = {thread: this.thread, level: 1};
        return;
      }
      if (lock.thread === this.thread) {
        ++lock.level;
        return;
      }
      this.block(obj, "ready", 1);
    }

    monitorExit(obj) {
      var lock = obj.lock;
      if (lock.thread !== this.thread)
        this.raiseExceptionAndYield("java/lang/IllegalMonitorStateException");
      if (--lock.level > 0) {
        return;
      }
      obj.lock = null;
      this.unblock(obj, "ready", false, function (ctx) {
        ctx.wakeup(obj);
      });
    }

    wait(obj, timeout) {
      var lock = obj.lock;
      if (timeout < 0)
        this.raiseExceptionAndYield("java/lang/IllegalArgumentException");
      if (!lock || lock.thread !== this.thread)
        this.raiseExceptionAndYield("java/lang/IllegalMonitorStateException");
      var lockLevel = lock.level;
      while (lock.level > 0)
        this.monitorExit(obj);
      if (timeout) {
        var self = this;
        this.lockTimeout = window.setTimeout(function () {
          obj.waiting.forEach(function (ctx, n) {
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

    notify(obj, notifyAll) {
      if (!obj.lock || obj.lock.thread !== this.thread)
        this.raiseExceptionAndYield("java/lang/IllegalMonitorStateException");
      this.unblock(obj, "waiting", notifyAll, function (ctx) {
        ctx.wakeup(obj);
      });
    }

    resolve(cp, idx: number, isStatic: boolean) {
      var constant = cp[idx];
      if (!constant.tag)
        return constant;
      switch (constant.tag) {
        case 3: // TAGS.CONSTANT_Integer
          constant = constant.integer;
          break;
        case 4: // TAGS.CONSTANT_Float
          constant = constant.float;
          break;
        case 8: // TAGS.CONSTANT_String
          constant = this.runtime.newStringConstant(cp[constant.string_index].bytes);
          break;
        case 5: // TAGS.CONSTANT_Long
          constant = Long.fromBits(constant.lowBits, constant.highBits);
          break;
        case 6: // TAGS.CONSTANT_Double
          constant = constant.double;
          break;
        case 7: // TAGS.CONSTANT_Class
          constant = CLASSES.getClass(cp[constant.name_index].bytes);
          break;
        case 9: // TAGS.CONSTANT_Fieldref
          var classInfo = this.resolve(cp, constant.class_index, isStatic);
          var fieldName = cp[cp[constant.name_and_type_index].name_index].bytes;
          var signature = cp[cp[constant.name_and_type_index].signature_index].bytes;
          constant = CLASSES.getField(classInfo, (isStatic ? "S" : "I") + "." + fieldName + "." + signature);
          if (!constant) {
            throw new JavaException("java/lang/RuntimeException",
              classInfo.className + "." + fieldName + "." + signature + " not found");
          }
          break;
        case 10: // TAGS.CONSTANT_Methodref
        case 11: // TAGS.CONSTANT_InterfaceMethodref
          var classInfo = this.resolve(cp, constant.class_index, isStatic);
          var methodName = cp[cp[constant.name_and_type_index].name_index].bytes;
          var signature = cp[cp[constant.name_and_type_index].signature_index].bytes;
          constant = CLASSES.getMethod(classInfo, (isStatic ? "S" : "I") + "." + methodName + "." + signature);
          if (!constant) {
            throw new JavaException("java/lang/RuntimeException",
              classInfo.className + "." + methodName + "." + signature + " not found");
          }
          break;
        default:
          throw new Error("not support constant type");
      }
      return constant;
    }

    triggerBailout(e, methodInfoId, compiledDepth, cpi, locals, stack) {
      throw VM.Yield;
    }

    JVMBailout(e, methodInfoId, compiledDepth, cpi, locals, stack) {
      var methodInfo = this.methodInfos[methodInfoId];
      var frame = new Frame(methodInfo, locals, 0);
      frame.stack = stack;
      frame.ip = cpi;
      this.frames.unshift(frame);
      if (compiledDepth === 0 && this.frameSets.length) {
        // Append all the current frames to the parent frame set, so a single frame stack
        // exists when the bailout finishes.
        var currentFrames = this.frames;
        this.frames = this.frameSets.pop();
        for (var i = 0; i < currentFrames.length; i++) {
          this.frames.push(currentFrames[i]);
        }
      }
    }

    classInitCheck(className) {
      if (this.runtime.initialized[className])
        return;
      throw VM.Yield;
    }
  }
}

var Context = J2ME.Context;