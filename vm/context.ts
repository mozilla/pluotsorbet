/*
 node-jvm
 Copyright (c) 2013 Yaroslav Gaponov <yaroslav.gaponov@gmail.com>
*/

declare var Shumway;
declare var profiling;

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
  declare var setZeroTimeout;

  export enum WriterFlags {
    None          = 0x00,
    Trace         = 0x01,
    Link          = 0x02,
    Init          = 0x04,
    Perf          = 0x08,
    Load          = 0x10,
    JIT           = 0x20,
    Code          = 0x40,
    Thread        = 0x80,
    TraceStack    = 0x100,

    All           = Trace | TraceStack | Link | Init | Perf | Load | JIT | Code | Thread
  }

  /**
   * Toggle VM tracing here.
   */
  export var writers = WriterFlags.None;

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

  //export class Frame {
  //  methodInfo: MethodInfo;
  //  local: any [];
  //  stack: any [];
  //  code: Uint8Array;
  //  pc: number;
  //  opPC: number;
  //  lockObject: java.lang.Object;
  //
  //  static dirtyStack: Frame [] = [];
  //
  //  /**
  //   * Denotes the start of the context frame stack.
  //   */
  //  static Start: Frame = Frame.create(null, null);
  //
  //  /**
  //   * Marks a frame set.
  //   */
  //  static Marker: Frame = Frame.create(null, null);
  //
  //  static isMarker(frame: Frame) {
  //    return frame.methodInfo === null;
  //  }
  //
  //  constructor(methodInfo: MethodInfo, local: any []) {
  //    frameCount ++;
  //    this.stack = [];
  //    this.reset(methodInfo, local);
  //  }
  //
  //  reset(methodInfo: MethodInfo, local: any []) {
  //    this.methodInfo = methodInfo;
  //    this.code = methodInfo ? methodInfo.codeAttribute.code : null;
  //    this.pc = 0;
  //    this.opPC = 0;
  //    this.stack.length = 0;
  //    this.local = local;
  //    this.lockObject = null;
  //  }
  //
  //  static create(methodInfo: MethodInfo, local: any []): Frame {
  //    var dirtyStack = Frame.dirtyStack;
  //    if (dirtyStack.length) {
  //      var frame = dirtyStack.pop();
  //      frame.reset(methodInfo, local);
  //      return frame;
  //    } else {
  //      return new Frame(methodInfo, local);
  //    }
  //  }
  //
  //  free() {
  //    release || assert(!Frame.isMarker(this));
  //    Frame.dirtyStack.push(this);
  //  }
  //
  //  incLocal(i: number, value: any) {
  //    this.local[i] += value | 0;
  //  }
  //
  //  read8(): number {
  //    return this.code[this.pc++];
  //  }
  //
  //  peek8(): number {
  //    return this.code[this.pc];
  //  }
  //
  //  read16(): number {
  //    var code = this.code
  //    return code[this.pc++] << 8 | code[this.pc++];
  //  }
  //
  //  patch(offset: number, oldValue: Bytecodes, newValue: Bytecodes) {
  //    release || assert(this.code[this.pc - offset] === oldValue);
  //    this.code[this.pc - offset] = newValue;
  //  }
  //
  //  read32(): number {
  //    return this.read32Signed() >>> 0;
  //  }
  //
  //  read8Signed(): number {
  //    return this.code[this.pc++] << 24 >> 24;
  //  }
  //
  //  read16Signed(): number {
  //    var pc = this.pc;
  //    var code = this.code;
  //    this.pc = pc + 2
  //    return (code[pc] << 8 | code[pc + 1]) << 16 >> 16;
  //  }
  //
  //  readTargetPC(): number {
  //    var pc = this.pc;
  //    var code = this.code;
  //    this.pc = pc + 2
  //    var offset = (code[pc] << 8 | code[pc + 1]) << 16 >> 16;
  //    return pc - 1 + offset;
  //  }
  //
  //  read32Signed(): number {
  //    return this.read16() << 16 | this.read16();
  //  }
  //
  //  tableSwitch(): number {
  //    var start = this.pc;
  //    while ((this.pc & 3) != 0) {
  //      this.pc++;
  //    }
  //    var def = this.read32Signed();
  //    var low = this.read32Signed();
  //    var high = this.read32Signed();
  //    var value = this.stack.pop();
  //    var pc;
  //    if (value < low || value > high) {
  //      pc = def;
  //    } else {
  //      this.pc += (value - low) << 2;
  //      pc = this.read32Signed();
  //    }
  //    return start - 1 + pc;
  //  }
  //
  //  lookupSwitch(): number {
  //    var start = this.pc;
  //    while ((this.pc & 3) != 0) {
  //      this.pc++;
  //    }
  //    var pc = this.read32Signed();
  //    var size = this.read32();
  //    var value = this.stack.pop();
  //    lookup:
  //    for (var i = 0; i < size; i++) {
  //      var key = this.read32Signed();
  //      var offset = this.read32Signed();
  //      if (key === value) {
  //        pc = offset;
  //      }
  //      if (key >= value) {
  //        break lookup;
  //      }
  //    }
  //    return start - 1 + pc;
  //  }
  //
  //  wide() {
  //    var stack = this.stack;
  //    var op = this.read8();
  //    switch (op) {
  //      case Bytecodes.ILOAD:
  //      case Bytecodes.FLOAD:
  //      case Bytecodes.ALOAD:
  //        stack.push(this.local[this.read16()]);
  //        break;
  //      case Bytecodes.LLOAD:
  //      case Bytecodes.DLOAD:
  //        stack.push2(this.local[this.read16()]);
  //        break;
  //      case Bytecodes.ISTORE:
  //      case Bytecodes.FSTORE:
  //      case Bytecodes.ASTORE:
  //        this.local[this.read16()] = stack.pop();
  //        break;
  //      case Bytecodes.LSTORE:
  //      case Bytecodes.DSTORE:
  //        this.local[this.read16()] = stack.pop2();
  //        break;
  //      case Bytecodes.IINC:
  //        var index = this.read16();
  //        var value = this.read16Signed();
  //        this.local[index] += value;
  //        break;
  //      case Bytecodes.RET:
  //        this.pc = this.local[this.read16()];
  //        break;
  //      default:
  //        var opName = Bytecodes[op];
  //        throw new Error("Wide opcode " + opName + " [" + op + "] not supported.");
  //    }
  //  }
  //
  //  /**
  //   * Returns the |object| on which a call to the specified |methodInfo| would be
  //   * called.
  //   */
  //  peekInvokeObject(methodInfo: MethodInfo): java.lang.Object {
  //    release || assert(!methodInfo.isStatic);
  //    var i = this.stack.length - methodInfo.argumentSlots - 1;
  //    release || assert (i >= 0);
  //    release || assert (this.stack[i] !== undefined);
  //    return this.stack[i];
  //  }
  //
  //  popArgumentsInto(methodInfo: MethodInfo, args): any [] {
  //    var stack = this.stack;
  //    var signatureKinds = methodInfo.signatureKinds;
  //    var argumentSlots = methodInfo.argumentSlots;
  //    for (var i = 1, j = stack.length - argumentSlots, k = 0; i < signatureKinds.length; i++) {
  //      args[k++] = stack[j++];
  //      if (isTwoSlot(signatureKinds[i])) {
  //        j++;
  //      }
  //    }
  //    release || assert(j === stack.length && k === signatureKinds.length - 1);
  //    stack.length -= argumentSlots;
  //    args.length = k;
  //    return args;
  //  }
  //
  //  toString() {
  //    return this.methodInfo.implKey + " " + this.pc;
  //  }
  //
  //  trace(writer: IndentingWriter) {
  //    var localStr = this.local.map(function (x) {
  //      return toDebugString(x);
  //    }).join(", ");
  //
  //    var stackStr = this.stack.map(function (x) {
  //      return toDebugString(x);
  //    }).join(", ");
  //
  //    writer.writeLn(("" + this.pc).padLeft(" ", 4) + " " + localStr + " | " + stackStr);
  //  }
  //}

  export var CLASSES = new ClassRegistry();
  declare var util;

  import Isolate = com.sun.cldc.isolate.Isolate;
  export class JVM {
    constructor() {
      // ...
    }

    private createIsolateCtx(): Context {
      var runtime = new Runtime(this);
      var ctx = new Context(runtime);
      ctx.thread = runtime.mainThread = <java.lang.Thread>newObject(CLASSES.java_lang_Thread.klass);
      ctx.thread.pid = util.id();
      ctx.thread.alive = true;
      // The constructor will set the real priority, however one is needed for the scheduler.
      ctx.thread.priority = NORMAL_PRIORITY;
      runtime.preInitializeClasses(ctx);
      return ctx;
    }

    startIsolate0(className: string, args: string []) {
      var ctx = this.createIsolateCtx();

      var sys = CLASSES.getClass("org/mozilla/internal/Sys");

      var array = newStringArray(args.length);
      for (var n = 0; n < args.length; ++n)
        array[n] = args[n] ? J2ME.newString(args[n]) : null;

      ctx.nativeThread.pushFrame(null);
      ctx.nativeThread.pushFrame(sys.getMethodByNameString("isolate0Entry", "(Ljava/lang/String;[Ljava/lang/String;)V"));
      ctx.nativeThread.frame.setParameter(Kind.Reference, 0, J2ME.newString(className.replace(/\./g, "/")));
      ctx.nativeThread.frame.setParameter(Kind.Reference, 1, array);
      ctx.start();
      release || Debug.assert(!U, "Unexpected unwind during isolate initialization.");
    }

    startIsolate(isolate: Isolate) {
      var ctx = this.createIsolateCtx();
      var runtime = ctx.runtime;
      isolate.runtime = runtime;
      runtime.isolate = isolate;

      var sys = CLASSES.getClass("org/mozilla/internal/Sys");

      runtime.updateStatus(RuntimeStatus.Started);
      runtime.priority = isolate._priority;

      var entryPoint = sys.getMethodByNameString("isolateEntryPoint", "(Lcom/sun/cldc/isolate/Isolate;)V");
      if (!entryPoint)
        throw new Error("Could not find isolate entry point.");

      ctx.nativeThread.pushFrame(null);
      ctx.nativeThread.pushFrame(entryPoint);
      ctx.nativeThread.frame.setParameter(Kind.Reference, 0, isolate);
      ctx.start();
      release || Debug.assert(!U, "Unexpected unwind during isolate initialization.");
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

    id: number;

    /**
     * Whether or not the context is currently paused.  The profiler uses this
     * to distinguish execution time from paused time in an async method.
     */
    paused: boolean = true;

    lockTimeout: number;
    lockLevel: number;
    nativeThread: Thread;
    thread: java.lang.Thread;
    writer: IndentingWriter;
    methodTimeline: any;
    virtualRuntime: number;
    constructor(public runtime: Runtime) {
      var id = this.id = Context._nextId ++;
      this.runtime = runtime;
      this.runtime.addContext(this);
      this.nativeThread = new Thread(this);
      this.virtualRuntime = 0;
      this.writer = new IndentingWriter(false, function (s) {
        console.log(s);
      });
      if (profile && typeof Shumway !== "undefined") {
        this.methodTimeline = new Shumway.Tools.Profiler.TimelineBuffer("Thread " + this.runtime.id + ":" + this.id);
        methodTimelines.push(this.methodTimeline);
      }
    }

    public static color(id) {
      if (inBrowser) {
        return id;
      }
      return Context._colors[id % Context._colors.length] + id + IndentingWriter.ENDC;
    }
    public static currentContextPrefix() {
      if ($) {
        return Context.color($.id) + "." + $.ctx.runtime.priority + ":" + Context.color($.ctx.id) + "." + $.ctx.getPriority();
      }
      return "";
    }

    /**
     * Sets global writers. Uncomment these if you want to see trace output.
     */
    static setWriters(writer: IndentingWriter) {
      traceStackWriter = writers & WriterFlags.TraceStack ? writer : null;
      traceWriter = writers & WriterFlags.Trace ? writer : null;
      perfWriter = writers & WriterFlags.Perf ? writer : null;
      linkWriter = writers & WriterFlags.Link ? writer : null;
      jitWriter = writers & WriterFlags.JIT ? writer : null;
      codeWriter = writers & WriterFlags.Code ? writer : null;
      initWriter = writers & WriterFlags.Init ? writer : null;
      threadWriter = writers & WriterFlags.Thread ? writer : null;
      loadWriter = writers & WriterFlags.Load ? writer : null;
    }

    getPriority() {
      if (this.thread) {
        return this.thread.priority;
      }
      return NORMAL_PRIORITY;
    }

    kill() {
      if (this.thread) {
        this.thread.alive = false;
      }
      this.runtime.removeContext(this);
    }

    executeMethod(methodInfo: MethodInfo) {
      return getLinkedMethod(methodInfo)();
      // this.nativeThread.pushFrame(methodInfo);
      // var returnValue = J2ME.interpret(this.nativeThread);

      //try {
      //  var returnValue = J2ME.interpret(this.nativeThread);
      //  if (U) {
      //    // Prepend all frames up until the first marker to the bailout frames.
      //    while (true) {
      //      var frame = frames.pop();
      //      if (Frame.isMarker(frame)) {
      //        break;
      //      }
      //      this.bailoutFrames.unshift(frame);
      //    }
      //    return;
      //  }
      //} catch (e) {
      //  this.popMarkerFrame();
      //  throwHelper(e);
      //}
      //this.popMarkerFrame();
      // return returnValue;
    }

    createException(className: string, message?: string) {
      if (!message) {
        message = "";
      }
      message = "" + message;
      var classInfo = CLASSES.loadAndLinkClass(className);
      classInitCheck(classInfo);
      release || Debug.assert(!U, "Unexpected unwind during createException.");
      runtimeCounter && runtimeCounter.count("createException " + className);
      var exception = new classInfo.klass();
      var methodInfo = classInfo.getMethodByNameString("<init>", "(Ljava/lang/String;)V");
      preemptionLockLevel++;
      getLinkedMethod(methodInfo).call(exception, message ? newString(message) : null);
      release || Debug.assert(!U, "Unexpected unwind during createException.");
      preemptionLockLevel--;
      return exception;
    }

    setAsCurrentContext() {
      if ($) {
        threadTimeline && threadTimeline.leave();
      }
      threadTimeline && threadTimeline.enter(this.runtime.id + ":" + this.id);
      $ = this.runtime;
      if ($.ctx === this) {
        return;
      }
      $.ctx = this;
      Context.setWriters(this.writer);
    }

    clearCurrentContext() {
      if ($) {
        threadTimeline && threadTimeline.leave();
      }
      $ = null;
      Context.setWriters(Context.writer);
    }

    start() {
      this.resume();
    }

    execute() {
      this.setAsCurrentContext();
      profile && this.resumeMethodTimeline();
      try {
        this.nativeThread.run();
      } catch (e) {
        // The exception was never caught and the thread must be terminated.
        this.kill();
        this.clearCurrentContext();
        // Rethrow so the exception is not silent.
        throw e;
      }
      if (U) {
        //if (this.bailoutFrames.length) {
        //  Array.prototype.push.apply(this.frames, this.bailoutFrames);
        //  this.bailoutFrames = [];
        //}
        switch (U) {
          case VMState.Yielding:
            this.resume();
            break;
          case VMState.Pausing:
            break;
          case VMState.Stopping:
            this.clearCurrentContext();
            this.kill();
            return;
        }
        U = VMState.Running;
        this.clearCurrentContext();
        return;
      }
      this.clearCurrentContext();
      this.kill();
    }

    resume() {
      Scheduler.enqueue(this);
    }

    block(object: java.lang.Object, queue, lockLevel: number) {
      object._lock[queue].push(this);
      this.lockLevel = lockLevel;
      $.pause("block");
    }

    unblock(object: java.lang.Object, queue, notifyAll: boolean) {
      while (object._lock[queue].length) {
        var ctx = object._lock[queue].pop();
        if (!ctx)
          continue;
          ctx.wakeup(object)
        if (!notifyAll)
          break;
      }
    }

    wakeup(object: java.lang.Object) {
      if (this.lockTimeout !== null) {
        window.clearTimeout(this.lockTimeout);
        this.lockTimeout = null;
      }
      if (object._lock.level !== 0) {
        object._lock.ready.push(this);
      } else {
        while (this.lockLevel-- > 0) {
          this.monitorEnter(object);
          if (U === VMState.Pausing || U === VMState.Stopping) {
            return;
          }
        }
        this.resume();
      }
    }

    monitorEnter(object: java.lang.Object) {
      var lock = object._lock;
      if (lock && lock.level === 0) {
        lock.thread = this.thread;
        lock.level = 1;
        return;
      }
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
      if (lock.level === 1 && lock.ready.length === 0) {
        lock.level = 0;
        return;
      }
      if (lock.thread !== this.thread)
        throw $.newIllegalMonitorStateException();
      if (--lock.level > 0) {
        return;
      }

      if (lock.level < 0) {
        throw $.newIllegalMonitorStateException("Unbalanced monitor enter/exit.");
      }
      this.unblock(object, "ready", false);
    }

    wait(object: java.lang.Object, timeout: number) {
      var lock = object._lock;
      if (timeout < 0)
        throw $.newIllegalArgumentException();
      if (!lock || lock.thread !== this.thread)
        throw $.newIllegalMonitorStateException();
      var lockLevel = lock.level;
      for (var i = lockLevel; i > 0; i--) {
        this.monitorExit(object);
      }
      if (timeout) {
        var self = this;
        this.lockTimeout = window.setTimeout(function () {
          for (var i = 0; i < lock.waiting.length; i++) {
            var ctx = lock.waiting[i];
            if (ctx === self) {
              lock.waiting[i] = null;
              ctx.wakeup(object);
            }
          }
        }, timeout);
      } else {
        this.lockTimeout = null;
      }
      this.block(object, "waiting", lockLevel);
    }

    notify(object: java.lang.Object, notifyAll: boolean) {
      if (!object._lock || object._lock.thread !== this.thread)
        throw $.newIllegalMonitorStateException();

      this.unblock(object, "waiting", notifyAll);
    }

    bailout(methodInfo: MethodInfo, pc: number, nextPC: number, local: any [], stack: any [], lockObject: java.lang.Object) {
      // perfWriter && perfWriter.writeLn("C Unwind: " + methodInfo.implKey);
      //REDUX
      //var frame = Frame.create(methodInfo, local);
      //frame.stack = stack;
      //frame.pc = nextPC;
      //frame.opPC = pc;
      //frame.lockObject = lockObject;
      //this.bailoutFrames.unshift(frame);
    }

    pauseMethodTimeline() {
      release || assert(!this.paused, "context is not paused");

      if (profiling) {
        this.methodTimeline.enter("<pause>", MethodType.Interpreted);
      }

      this.paused = true;
    }

    resumeMethodTimeline() {
      release || assert(this.paused, "context is paused");

      if (profiling) {
        this.methodTimeline.leave("<pause>", MethodType.Interpreted);
      }

      this.paused = false;
    }

    /**
     * Re-enters all the frames that are currently on the stack so the full stack
     * trace shows up in the profiler.
     */
    restartMethodTimeline() {
      //REDUX
      //for (var i = 0; i < this.frames.length; i++) {
      //  var frame = this.frames[i];
      //  if (J2ME.Frame.isMarker(frame)) {
      //    continue;
      //  }
      //  this.methodTimeline.enter(frame.methodInfo.implKey, MethodType.Interpreted);
      //}
      //
      // if (this.paused) {
      //   this.methodTimeline.enter("<pause>", MethodType.Interpreted);
      // }
    }

    enterMethodTimeline(key: string, methodType: MethodType) {
      if (profiling) {
        this.methodTimeline.enter(key, MethodType[methodType]);
      }
    }

    leaveMethodTimeline(key: string, methodType: MethodType) {
      if (profiling) {
        this.methodTimeline.leave(key, MethodType[methodType]);
      }
    }
  }
}

var Context = J2ME.Context;

Object.defineProperty(jsGlobal, "CLASSES", {
  get: function () {
    return J2ME.CLASSES;
  }
});

var JVM = J2ME.JVM;
