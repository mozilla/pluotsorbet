interface Array<T> {
  push2: (value) => void;
  pop2: () => any;
  pushKind: (kind: J2ME.Kind, value) => void;
  popKind: (kind: J2ME.Kind) => any;
  read: (i) => any;
}

module J2ME {
  import assert = Debug.assert;
  import Bytecodes = Bytecode.Bytecodes;
  declare var VM;
  declare var Instrument;
  declare var setZeroTimeout;

  export enum WriterFlags {
    None  = 0,
    Trace = 1,
    Link  = 2,
    Init  = 4,
    Perf  = 8,
    All   = Trace | Link | Init | Perf
  }

  /**
   * Toggle VM tracing here.
   */
  export var writers = WriterFlags.Perf;

  Array.prototype.push2 = function(value) {
    this.push(value);
    this.push(null);
    return value;
  }

  Array.prototype.pop2 = function() {
    this.pop();
    return this.pop();
  }

  Array.prototype.pushKind = function(kind: Kind, value) {
    if (isTwoSlot(kind)) {
      this.push2(value);
      return;
    }
    this.push(value);
  }

  Array.prototype.popKind = function(kind: Kind) {
    if (isTwoSlot(kind)) {
      return this.pop2();
    }
    return this.pop();
  }

  // A convenience function for retrieving values in reverse order
  // from the end of the stack.  stack.read(1) returns the topmost item
  // on the stack, while stack.read(2) returns the one underneath it.
  Array.prototype.read = function(i) {
    return this[this.length - i];
  };

  export class Frame {
    methodInfo: MethodInfo;
    local: any [];
    stack: any [];
    code: Uint8Array;
    pc: number;
    cp: any;
    localBase: number;
    lockObject: java.lang.Object;

    constructor(methodInfo: MethodInfo, local: any [], localBase: number) {
      this.methodInfo = methodInfo;
      this.cp = methodInfo.classInfo.constant_pool;
      this.code = methodInfo.code;
      this.pc = 0;
      this.stack = [];
      this.local = local;
      this.localBase = localBase;
      this.lockObject = null;
    }

    getLocal(i: number): any {
      return this.local[this.localBase + i];
    }

    setLocal(i: number, value: any) {
      this.local[this.localBase + i] = value;
    }

    read8(): number {
      return this.code[this.pc++];
    }

    peek8(): number {
      return this.code[this.pc];
    }

    read16(): number {
      var code = this.code
      return code[this.pc++] << 8 | code[this.pc++];
    }

    read32(): number {
      return this.read32Signed() >>> 0;
    }

    read8Signed(): number {
      return this.code[this.pc++] << 24 >> 24;
    }

    read16Signed(): number {
      var pc = this.pc;
      var code = this.code;
      this.pc = pc + 2
      return (code[pc] << 8 | code[pc + 1]) << 16 >> 16;
    }

    readTargetPC(): number {
      var pc = this.pc;
      var code = this.code;
      this.pc = pc + 2
      var offset = (code[pc] << 8 | code[pc + 1]) << 16 >> 16;
      return pc - 1 + offset;
    }

    read32Signed(): number {
      return this.read16() << 16 | this.read16();
    }

    tableSwitch(): number {
      var start = this.pc;
      while ((this.pc & 3) != 0) {
        this.pc++;
      }
      var def = this.read32Signed();
      var low = this.read32Signed();
      var high = this.read32Signed();
      var value = this.stack.pop();
      var pc;
      if (value < low || value > high) {
        pc = def;
      } else {
        this.pc += (value - low) << 2;
        pc = this.read32Signed();
      }
      return start - 1 + pc;
    }

    lookupSwitch(): number {
      var start = this.pc;
      while ((this.pc & 3) != 0) {
        this.pc++;
      }
      var pc = this.read32Signed();
      var size = this.read32();
      var value = this.stack.pop();
      lookup:
      for (var i = 0; i < size; i++) {
        var key = this.read32Signed();
        var offset = this.read32Signed();
        if (key === value) {
          pc = offset;
        }
        if (key >= value) {
          break lookup;
        }
      }
      return start - 1 + pc;
    }

    wide() {
      var stack = this.stack;
      var op = this.read8();
      switch (op) {
        case Bytecodes.ILOAD:
        case Bytecodes.FLOAD:
        case Bytecodes.ALOAD:
          stack.push(this.getLocal(this.read16()));
          break;
        case Bytecodes.LLOAD:
        case Bytecodes.DLOAD:
          stack.push2(this.getLocal(this.read16()));
          break;
        case Bytecodes.ISTORE:
        case Bytecodes.FSTORE:
        case Bytecodes.ASTORE:
          this.setLocal(this.read16(), stack.pop());
          break;
        case Bytecodes.LSTORE:
        case Bytecodes.DSTORE:
          this.setLocal(this.read16(), stack.pop2());
          break;
        case Bytecodes.IINC:
          var index = this.read16();
          var value = this.read16Signed();
          this.setLocal(index, this.getLocal(index) + value);
          break;
        case Bytecodes.RET:
          this.pc = this.getLocal(this.read16());
          break;
        default:
          var opName = Bytecodes[op];
          throw new Error("Wide opcode " + opName + " [" + op + "] not supported.");
      }
    }

    /**
     * Returns the |object| on which a call to the specified |methodInfo| would be
     * called.
     */
    peekInvokeObject(methodInfo: MethodInfo): java.lang.Object {
      release || assert(!methodInfo.isStatic);
      var i = this.stack.length - methodInfo.argumentSlots - 1;
      release || assert (i >= 0);
      release || assert (this.stack[i] !== undefined);
      return this.stack[i];
    }

    popArgumentsInto(signatureDescriptor: SignatureDescriptor, args): any [] {
      var stack = this.stack;
      var typeDescriptors = signatureDescriptor.typeDescriptors;
      var argumentSlotCount = signatureDescriptor.getArgumentSlotCount();
      for (var i = 1, j = stack.length - argumentSlotCount, k = 0; i < typeDescriptors.length; i++) {
        var typeDescriptor = typeDescriptors[i];
        args[k++] = stack[j++];
        if (isTwoSlot(typeDescriptor.kind)) {
          j++;
        }
      }
      release || assert(j === stack.length && k === signatureDescriptor.getArgumentCount());
      stack.length -= argumentSlotCount;
      args.length = k;
      return args;
    }

    trace(writer: IndentingWriter) {
      var localStr = this.local.map(function (x) {
        return toDebugString(x);
      }).join(", ");

      var stackStr = this.stack.map(function (x) {
        return toDebugString(x);
      }).join(", ");

      writer.writeLn(("" + this.pc).padLeft(" ", 4) + " " + localStr + " | " + stackStr);
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
    frames: Frame [];
    frameSets: Frame [][];
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

    /**
     * Sets global writers. Uncomment these if you want to see trace output.
     */
    static setWriters(writer: IndentingWriter) {
      traceWriter = writers & WriterFlags.Trace ? writer : null;
      perfWriter = writers & WriterFlags.Perf ? writer : null;
      linkWriter = writers & WriterFlags.Link ? writer : null;
      initWriter = writers & WriterFlags.Init ? writer : null;
    }

    kill() {
      if (this.thread) {
        this.thread.alive = false;
      }
      this.runtime.removeContext(this);
    }

    current(): Frame {
      var frames = this.frames;
      return frames[frames.length - 1];
    }

    executeNewFrameSet(frames: Frame []) {
      this.frameSets.push(this.frames);
      this.frames = frames;
      try {
        var returnValue = VM.execute();
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
      } catch (e) {
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

    createException(className: string, message?: string) {
      if (!message)
        message = "";
      message = "" + message;
      var classInfo = CLASSES.getClass(className);
      runtimeCounter && runtimeCounter.count("createException " + className);
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
        VM.execute();
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

    monitorEnter(object: java.lang.Object) {
      var lock = object._lock;
      if (!lock) {
        object._lock = new Lock(this.thread, 1);
        return;
      }
      if (lock.thread === this.thread) {
        ++lock.level;
        return;
      }
      this.block(object, "ready", 1);
    }

    monitorExit(object: java.lang.Object) {
      var lock = object._lock;
      if (lock.thread !== this.thread)
        throw $.newIllegalMonitorStateException();
      if (--lock.level > 0) {
        return;
      }
      object._lock = null;
      this.unblock(object, "ready", false, function (ctx) {
        ctx.wakeup(object);
      });
    }

    wait(object: java.lang.Object, timeout) {
      var lock = object._lock;
      if (timeout < 0)
        throw $.newIllegalArgumentException();
      if (!lock || lock.thread !== this.thread)
        throw $.newIllegalMonitorStateException();
      var lockLevel = lock.level;
      while (lock.level > 0)
        this.monitorExit(object);
      if (timeout) {
        var self = this;
        this.lockTimeout = window.setTimeout(function () {
          object.waiting.forEach(function (ctx, n) {
            if (ctx === self) {
              object.waiting[n] = null;
              ctx.wakeup(object);
            }
          });
        }, timeout);
      } else {
        this.lockTimeout = null;
      }
      this.block(object, "waiting", lockLevel);
    }

    notify(obj, notifyAll) {
      if (!obj._lock || obj._lock.thread !== this.thread)
        throw $.newIllegalMonitorStateException();

      this.unblock(obj, "waiting", notifyAll, function (ctx) {
        ctx.wakeup(obj);
      });
    }

    bailout(methodInfo: MethodInfo, pc: number, local: any [], stack: any []) {
      var frame = new Frame(methodInfo, local, 0);
      frame.stack = stack;
      frame.pc = pc;
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
            throw $.newRuntimeException(
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
            throw $.newRuntimeException(
              classInfo.className + "." + methodName + "." + signature + " not found");
          }
          break;
        default:
          throw new Error("not support constant type");
      }
      return cp[idx] = constant;
    }
  }
}

var Context = J2ME.Context;
var Frame = J2ME.Frame;
