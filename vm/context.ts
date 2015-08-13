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
      ctx.threadAddress = runtime.mainThread = allocObject(CLASSES.java_lang_Thread);
      var thread = <java.lang.Thread>getHandle(ctx.threadAddress);
      // XXX thread.pid seems to be unused, so remove it.
      thread.pid = util.id();
      thread.nativeAlive = true;
      // The constructor will set the real priority, however one is needed for the scheduler.
      thread.priority = NORMAL_PRIORITY;
      runtime.preInitializeClasses(ctx);
      return ctx;
    }

    startIsolate0(className: string, args: string []) {
      var ctx = this.createIsolateCtx();

      var sys = CLASSES.getClass("org/mozilla/internal/Sys");

      ctx.nativeThread.pushMarkerFrame(FrameType.ExitInterpreter);
      ctx.nativeThread.pushFrame(sys.getMethodByNameString("isolate0Entry", "(Ljava/lang/String;[Ljava/lang/String;)V"));
      ctx.nativeThread.frame.setParameter(Kind.Reference, 0, J2ME.newString(className.replace(/\./g, "/")));

      var arrayAddr = newStringArray(args.length);
      setUncollectable(arrayAddr);
      var array = getArrayFromAddr(arrayAddr);
      for (var n = 0; n < args.length; ++n) {
        array[n] = args[n] ? J2ME.newString(args[n]) : Constants.NULL;
      }
      unsetUncollectable(arrayAddr);

      ctx.nativeThread.frame.setParameter(Kind.Reference, 1, arrayAddr);
      ctx.start();
      release || Debug.assert(!U, "Unexpected unwind during isolate initialization.");
    }

    startIsolate(isolateAddr: number) {
      var isolate = <Isolate>getHandle(isolateAddr);

      var ctx = this.createIsolateCtx();
      var runtime = ctx.runtime;
      runtime.isolateAddress = isolateAddr;

      // We could look this up from the address, but we use it a lot,
      // so we cache it here.
      runtime.isolateId = isolate._id;

      setNative(isolateAddr, runtime);

      var sys = CLASSES.getClass("org/mozilla/internal/Sys");

      runtime.updateStatus(RuntimeStatus.Started);
      runtime.priority = isolate._priority;

      var entryPoint = sys.getMethodByNameString("isolateEntryPoint", "(Lcom/sun/cldc/isolate/Isolate;)V");
      if (!entryPoint)
        throw new Error("Could not find isolate entry point.");

      ctx.nativeThread.pushMarkerFrame(FrameType.ExitInterpreter);
      ctx.nativeThread.pushFrame(entryPoint);
      ctx.nativeThread.frame.setParameter(Kind.Reference, 0, isolateAddr);
      ctx.start();
      release || Debug.assert(!U, "Unexpected unwind during isolate initialization.");
    }

  }

  enum ThreadDataLayout {
    AddressOffset = 0,
    StackTopOffset = 1,
    Size = 2
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
    threadData: Int32Array;
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

      setUncollectable(this.nativeThread.tp);
      this.threadData = new Int32Array(ASM.buffer, gcMallocUncollectable(ThreadDataLayout.Size << 2), ThreadDataLayout.Size);
      this.threadData[ThreadDataLayout.StackTopOffset] = this.nativeThread.tp;
      unsetUncollectable(this.nativeThread.tp);
    }

    public set threadAddress(addr: number) {
      this.threadData[ThreadDataLayout.AddressOffset] = addr;
    }

    public get threadAddress() {
      return this.threadData[ThreadDataLayout.AddressOffset];
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
      if (this.threadAddress) {
        var thread = <java.lang.Thread>getHandle(this.threadAddress);
        return thread.priority;
      }
      return NORMAL_PRIORITY;
    }

    kill() {
      if (this.threadAddress) {
        var thread = <java.lang.Thread>getHandle(this.threadAddress);
        thread.nativeAlive = false;
      }
      this.runtime.removeContext(this);

      ASM._gcFree(this.threadData.byteOffset);
    }

    executeMethod(methodInfo: MethodInfo) {
      return getLinkedMethod(methodInfo)();
    }

    createException(className: string, message?: string): java.lang.Object {
      if (!message) {
        message = "";
      }
      message = "" + message;
      var classInfo = CLASSES.loadClass(className);
      classInitCheck(classInfo);
      release || Debug.assert(!U, "Unexpected unwind during createException.");
      runtimeCounter && runtimeCounter.count("createException " + className);
      var exceptionAddress = allocObject(classInfo);
      setUncollectable(exceptionAddress);
      var methodInfo = classInfo.getMethodByNameString("<init>", "(Ljava/lang/String;)V");
      preemptionLockLevel++;
      getLinkedMethod(methodInfo)(exceptionAddress, message ? newString(message) : Constants.NULL);
      release || Debug.assert(!U, "Unexpected unwind during createException.");
      preemptionLockLevel--;
      unsetUncollectable(exceptionAddress);
      return getHandle(exceptionAddress);
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
        throw "classInfo" in e ? e.classInfo : e;
      }
      release || assert(this.nativeThread.nativeFrameCount === 0, "All native frames should be gone.")
      if (U) {
        this.nativeThread.endUnwind();
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

    block(monitor: java.lang.Object, queue, lockLevel: number) {
      monitor._lock[queue].push(this);
      this.lockLevel = lockLevel;
      $.pause("block");
    }

    unblock(monitor: java.lang.Object, queue, notifyAll: boolean) {
      while (monitor._lock[queue].length) {
        var ctx = monitor._lock[queue].pop();
        if (!ctx) {
          continue;
        }
        ctx.wakeup(monitor);
        if (!notifyAll) {
          break;
        }
      }
    }

    wakeup(monitor: java.lang.Object) {
      if (this.lockTimeout !== null) {
        window.clearTimeout(this.lockTimeout);
        this.lockTimeout = null;
      }
      if (monitor._lock.level !== 0) {
        monitor._lock.ready.push(this);
      } else {
        while (this.lockLevel-- > 0) {
          this.monitorEnter(monitor);
          if (U === VMState.Pausing || U === VMState.Stopping) {
            return;
          }
        }
        this.resume();
      }
    }

    monitorEnter(monitor: java.lang.Object) {
      var lock = monitor._lock;
      if (lock && lock.level === 0) {
        lock.threadAddress = this.threadAddress;
        lock.level = 1;
        return;
      }
      if (!lock) {
        monitor._lock = new Lock(this.threadAddress, 1);
        return;
      }
      if (lock.threadAddress === this.threadAddress) {
        ++lock.level;
        return;
      }
      this.block(monitor, "ready", 1);
    }

    monitorExit(monitor: java.lang.Object) {
      var lock = monitor._lock;
      if (lock.level === 1 && lock.ready.length === 0) {
        lock.level = 0;
        return;
      }
      if (lock.threadAddress !== this.threadAddress)
        throw $.newIllegalMonitorStateException();
      if (--lock.level > 0) {
        return;
      }

      if (lock.level < 0) {
        throw $.newIllegalMonitorStateException("Unbalanced monitor enter/exit.");
      }
      this.unblock(monitor, "ready", false);
    }

    wait(objectAddr: number, timeout: number) {
      var monitor = getMonitor(objectAddr);
      var lock = monitor._lock;
      if (timeout < 0)
        throw $.newIllegalArgumentException();
      if (!lock || lock.threadAddress !== this.threadAddress)
        throw $.newIllegalMonitorStateException();
      var lockLevel = lock.level;
      for (var i = lockLevel; i > 0; i--) {
        this.monitorExit(monitor);
      }
      if (timeout) {
        var self = this;
        this.lockTimeout = window.setTimeout(function () {
          for (var i = 0; i < lock.waiting.length; i++) {
            var ctx = lock.waiting[i];
            if (ctx === self) {
              lock.waiting[i] = null;
              ctx.wakeup(monitor);
            }
          }
        }, timeout);
      } else {
        this.lockTimeout = null;
      }
      this.block(monitor, "waiting", lockLevel);
    }

    notify(objectAddr: number, notifyAll: boolean) {
      var monitor = getMonitor(objectAddr);
      if (!monitor._lock || monitor._lock.threadAddress !== this.threadAddress)
        throw $.newIllegalMonitorStateException();
      // TODO Unblock can call wakeup on a different ctx which in turn calls monitorEnter and can cause unwinds
      // on another ctx, but we shouldn't unwind this ctx. After figuring out why this is, remove assertions in
      // "java/lang/Object.notify.()V" and "java/lang/Object.notifyAll.()V"
      this.unblock(monitor, "waiting", notifyAll);
    }

    bailout(bailoutFrameAddress: number) {
      traceWriter && traceWriter.writeLn("Bailout: " + methodIdToMethodInfoMap[i32[bailoutFrameAddress + BailoutFrameLayout.MethodIdOffset >> 2]].implKey);
      this.nativeThread.unwoundNativeFrames.push(bailoutFrameAddress);
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
