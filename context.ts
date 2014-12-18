module J2ME {
  import assert = Debug.assert;
  declare var VM;
  declare var Instrument;
  declare var setZeroTimeout;
  declare var Long;
  declare var JavaException;

  export class Frame {
    methodInfo: MethodInfo;
    locals: any [];
    stack: any [];
    code: Uint8Array;
    bci: number;
    cp: any;
    localsBase: number;
    lockObject: java.lang.Object;
    profileData: any;

    constructor(methodInfo: MethodInfo, locals: any [], localsBase: number) {
      this.methodInfo = methodInfo;
      this.cp = methodInfo.classInfo.constant_pool;
      this.code = methodInfo.code;
      this.bci = 0;
      this.stack = [];
      this.locals = locals;
      this.localsBase = localsBase;
      this.lockObject = null;
      this.profileData = null;
    }

    getLocal(i: number): any {
      return this.locals[this.localsBase + i];
    }

    setLocal(i: number, value: any) {
      this.locals[this.localsBase + i] = value;
    }

    read8(): number {
      return this.code[this.bci++];
    }

    read16(): number {
      return this.read8() << 8 | this.read8();
    }

    read32(): number {
      return this.read32signed() >>> 0;
    }

    read8signed(): number {
      return this.read8() << 24 >> 24;
    }

    read16signed(): number {
      return this.read16() << 16 >> 16;
    }

    read32signed(): number {
      return this.read16() << 16 | this.read16();
    }

    /**
     * Returns the |object| on which a call to the specified |methodInfo| would be
     * called.
     */
    peekInvokeObject(methodInfo: MethodInfo): java.lang.Object {
      release || assert(!methodInfo.isStatic);
      var argumentSlotCount = methodInfo.signatureDescriptor.getArgumentSlotCount();
      var i = this.stack.length - argumentSlotCount - 1;
      release || assert (i >= 0);
      release || assert (this.stack[i] !== undefined);
      return this.stack[i];
    }

    popArguments(signatureDescriptor: SignatureDescriptor): any [] {
      var stack = this.stack;
      var typeDescriptors = signatureDescriptor.typeDescriptors;
      var argumentSlotCount = signatureDescriptor.getArgumentSlotCount();
      var args = new Array(signatureDescriptor.getArgumentCount());
      for (var i = 1, j = stack.length - argumentSlotCount, k = 0; i < typeDescriptors.length; i++) {
        var typeDescriptor = typeDescriptors[i];
        args[k++] = stack[j++];
        if (isTwoSlot(typeDescriptor.kind)) {
          j++;
        }
      }
      release || assert(j === stack.length && k === signatureDescriptor.getArgumentCount());
      stack.length -= argumentSlotCount;
      return args;
    }

    trace(writer: IndentingWriter) {
      var localsStr = this.locals.map(function (x) {
        return toDebugString(x);
      }).join(", ");

      var stackStr = this.stack.map(function (x) {
        return toDebugString(x);
      }).join(", ");

      writer.writeLn(this.bci + " " + localsStr + " | " + stackStr);
    }
  }

  export class Context {
    private static _nextId: number = 0;
    private static _colors = [
      IndentingWriter.PURPLE,
      IndentingWriter.YELLOW,
      IndentingWriter.GREEN,
      IndentingWriter.RED,
      IndentingWriter.BOLD_RED
    ];
    private static writer: IndentingWriter = new IndentingWriter(false, function (s) {
      console.log(s);
    });

    id: number
    frames: any [];
    frameSets: any [];
    bailoutFrames: any [];
    lockTimeout: number;
    lockLevel: number;
    thread: java.lang.Thread;
    writer: IndentingWriter;
    constructor(public runtime: Runtime) {
      var id = this.id = Context._nextId ++;
      this.frames = [];
      this.frameSets = [];
      this.bailoutFrames = [];
      this.runtime = runtime;
      this.runtime.addContext(this);
      this.writer = new IndentingWriter(false, function (s) {
        console.log(s);
      });
    }

    public static color(id) {
      if (inBrowser) {
        return id;
      }
      return Context._colors[id % Context._colors.length] + id + IndentingWriter.ENDC;
    }
    public static currentContextPrefix() {
      if ($) {
        return Context.color($.id) + ":" + Context.color($.ctx.id);
      }
      return "";
    }

    static setWriters(writer: IndentingWriter) {
      traceWriter = null; // writer;
      linkWriter = null; // writer;
      initWriter = null; // writer;
    }

    kill() {
      if (this.thread) {
        this.thread.alive = false;
      }
      this.runtime.removeContext(this);
    }

    current() {
      var frames = this.frames;
      return frames[frames.length - 1];
    }

    executeNewFrameSet(frames: Frame []) {
      this.frameSets.push(this.frames);
      this.frames = frames;
      try {
        if (traceWriter) {
          var firstFrame = frames[0];
          var frameDetails = firstFrame.methodInfo.classInfo.className + "/" + firstFrame.methodInfo.name + signatureToDefinition(firstFrame.methodInfo.signature, true, true);
          traceWriter.enter("> " + MethodType[MethodType.Interpreted][0] + " " + frameDetails);
        }
        var returnValue = VM.execute(this);
        if (U) {
          // Append all the current frames to the parent frame set, so a single frame stack
          // exists when the bailout finishes.
          var currentFrames = this.frames;
          this.frames = this.frameSets.pop();
          for (var i = currentFrames.length - 1; i >= 0; i--) {
            this.bailoutFrames.unshift(currentFrames[i]);
          }
          return;
        }
        if (traceWriter) {
          traceWriter.leave("<");
        }
      } catch (e) {
        if (traceWriter) {
          traceWriter.leave("< " + e);
        }
        assert(this.frames.length === 0);
        this.frames = this.frameSets.pop();
        throwHelper(e);
      }
      this.frames = this.frameSets.pop();
      return returnValue;
    }

    getClassInitFrame(classInfo: ClassInfo) {
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
      return new Frame(syntheticMethod, [classInfo.getClassInitLockObject(this)], 0);
    }

    pushClassInitFrame(classInfo: ClassInfo) {
      if (this.runtime.initialized[classInfo.className])
        return;
      var classInitFrame = this.getClassInitFrame(classInfo);
      this.executeNewFrameSet([classInitFrame]);
    }

    createException(className, message?) {
      if (!message)
        message = "";
      message = "" + message;
      var classInfo = CLASSES.getClass(className);

      var exception = new classInfo.klass();
      var methodInfo = CLASSES.getMethod(classInfo, "I.<init>.(Ljava/lang/String;)V");
      jsGlobal[methodInfo.mangledClassAndMethodName].call(exception, message ? newString(message) : null);

      return exception;
    }

    setAsCurrentContext() {
      $ = this.runtime;
      if ($.ctx === this) {
        return;
      }
      $.ctx = this;
      Context.setWriters(this.writer);
    }

    clearCurrentContext() {
      $ = null;
      Context.setWriters(Context.writer);
    }

    start(frame: Frame) {
      this.frames = [frame];
      this.resume();
    }

    private execute() {
      Instrument.callResumeHooks(this.current());
      this.setAsCurrentContext();
      do {
        VM.execute(this);
        if (U) {
          Array.prototype.push.apply(this.frames, this.bailoutFrames);
          this.bailoutFrames = [];
          switch (U) {
            case VMState.Yielding:
              this.resume();
              break;
            case VMState.Pausing:
              Instrument.callPauseHooks(this.current());
              break;
          }
          U = VMState.Running;
          this.clearCurrentContext();
          return;
        }
      } while (this.frames.length !== 0);
      this.kill();
    }

    resume() {
      (<any>window).setZeroTimeout(this.execute.bind(this));
    }

    block(obj, queue, lockLevel) {
      if (!obj[queue])
        obj[queue] = [];
      obj[queue].push(this);
      this.lockLevel = lockLevel;
      $.pause();
    }

    unblock(obj, queue, notifyAll, callback) {
      while (obj[queue] && obj[queue].length) {
        var ctx = obj[queue].pop();
        if (!ctx)
          continue;
        // Wait until next tick, so that we are sure to notify all waiting.
        (<any>window).setZeroTimeout(callback.bind(null, ctx));
        if (!notifyAll)
          break;
      }
    }

    wakeup(obj) {
      if (this.lockTimeout !== null) {
        window.clearTimeout(this.lockTimeout);
        this.lockTimeout = null;
      }
      if (obj._lock) {
        if (!obj.ready)
          obj.ready = [];
        obj.ready.push(this);
      } else {
        while (this.lockLevel-- > 0) {
          this.monitorEnter(obj);
          if (U === VMState.Pausing) {
            return;
          }
        }
        this.resume();
      }
    }

    monitorEnter(obj: java.lang.Object) {
      var lock = obj._lock;
      if (!lock) {
        obj._lock = new Lock(this.thread, 1);
        return;
      }
      if (lock.thread === this.thread) {
        ++lock.level;
        return;
      }
      this.block(obj, "ready", 1);
    }

    monitorExit(obj: java.lang.Object) {
      var lock = obj._lock;
      if (lock.thread !== this.thread)
        throw this.createException("java/lang/IllegalMonitorStateException");
      if (--lock.level > 0) {
        return;
      }
      obj._lock = null;
      this.unblock(obj, "ready", false, function (ctx) {
        ctx.wakeup(obj);
      });
    }

    wait(obj, timeout) {
      var lock = obj._lock;
      if (timeout < 0)
        throw this.createException("java/lang/IllegalArgumentException");
      if (!lock || lock.thread !== this.thread)
        throw this.createException("java/lang/IllegalMonitorStateException");
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
      if (!obj._lock || obj._lock.thread !== this.thread)
        throw this.createException("java/lang/IllegalMonitorStateException");

      this.unblock(obj, "waiting", notifyAll, function (ctx) {
        ctx.wakeup(obj);
      });
    }

    bailout(methodInfo: MethodInfo, bci: number, local: any [], stack: any []) {
      var frame = new Frame(methodInfo, local, 0);
      frame.stack = stack;
      frame.bci = bci;
      this.bailoutFrames.unshift(frame);
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
  }
}

var Context = J2ME.Context;
var Frame = J2ME.Frame;
