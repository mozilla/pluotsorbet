var $: J2ME.Runtime; // The currently-executing runtime.

interface Math {
  fround(value: number): number;
}
interface Long {
  isZero(): boolean;
}
declare var Long: {
  new (low: number, high: number): Long;
  ZERO: Long;
  fromBits(lowBits: number, highBits: number): Long;
  fromInt(value: number);
  fromNumber(value: number);
}

declare var throwHelper;
declare var throwPause;
declare var throwYield;

module J2ME {
  declare var Native, Override;
  declare var VM;
  declare var Instrument;

  /**
   * Turns on just-in-time compilation of methods.
   */
  export var enableRuntimeCompilation = true;

  /**
   * Enables more compact mangled names. This helps reduce code size but may cause naming collisions.
   */
  var hashedMangledNames = false;

  /**
   * Traces method execution.
   */
  export var traceWriter = null;

  /**
   * Traces performance problems.
   */
  export var perfWriter = null;

  /**
   * Traces linking and class loading.
   */
  export var linkWriter = null;

  /**
   * Traces JIT compilation.
   */
  export var jitWriter = null;

  /**
   * Traces class loading.
   */
  export var loadWriter = null;

  /**
   * Traces winding and unwinding.
   */
  export var windingWriter = null;

  /**
   * Traces class initialization.
   */
  export var initWriter = null;


  export enum MethodState {
    /**
     * All methods start in this state.
     */
    Cold = 0,

    /**
     * Methods have this state if code has been compiled for them or
     * there is a native implementation that needs to be used.
     */
    Compiled = 1,

    /**
     * We don't want to compiled these methods, they may be too large
     * to benefit from JIT compilation.
     */
    NotCompiled = 2,

    /**
     * Methods are not compiled because of some exception.
     */
    CannotCompile = 3
  }

  declare var Shumway;

  export var timeline;
  export var methodTimeline;
  export var threadTimeline;
  export var nativeCounter = new Metrics.Counter(true);
  export var runtimeCounter = new Metrics.Counter(true);
  export var baselineMethodCounter = new Metrics.Counter(true);
  export var asyncCounter = new Metrics.Counter(true);
  export var jitMethodInfos = {};

  export var unwindCount = 0;

  if (typeof Shumway !== "undefined") {
    timeline = new Shumway.Tools.Profiler.TimelineBuffer("Runtime");
    methodTimeline = new Shumway.Tools.Profiler.TimelineBuffer("Methods");
    threadTimeline = new Shumway.Tools.Profiler.TimelineBuffer("Threads");
  }

  export function enterTimeline(name: string, data?: any) {
    timeline && timeline.enter(name, data);
  }

  export function leaveTimeline(name?: string, data?: any) {
    timeline && timeline.leave(name, data);
  }

  export var Klasses = {
    java: {
      lang: {
        Object: null,
        Class: null,
        String: null,
        Thread: null,
        IllegalArgumentException: null,
        IllegalStateException: null,
        NullPointerException: null,
        RuntimeException: null,
        IndexOutOfBoundsException: null,
        ArrayIndexOutOfBoundsException: null,
        StringIndexOutOfBoundsException: null,
        ArrayStoreException: null,
        IllegalMonitorStateException: null,
        ClassCastException: null,
        NegativeArraySizeException: null,
        ArithmeticException: null,
        ClassNotFoundException: null,
        SecurityException: null,
        IllegalThreadStateException: null,
        Exception: null
      },
      io: {
        IOException: null,
        UTFDataFormatException: null,
        UnsupportedEncodingException: null
      }
    },
    javax: {
      microedition: {
        media: {
          MediaException: null
        }
      }
    },
    boolean: null,
    char: null,
    float: null,
    double: null,
    byte: null,
    short: null,
    int: null,
    long: null
  };

  function Int64Array(size: number) {
    var array = Array(size);
    for (var i = 0; i < size; i++) {
      array[i] = Long.ZERO;
    }
    // We can't put the klass on the prototype.
    (<any>array).klass = Klasses.long;
    return array;
  }

  var arrays = {
    'Z': Uint8Array,
    'C': Uint16Array,
    'F': Float32Array,
    'D': Float64Array,
    'B': Int8Array,
    'S': Int16Array,
    'I': Int32Array,
    'J': Int64Array
  };

  export function getArrayConstructor(type: string): Function {
    return arrays[type];
  }

  /**
   * We can't always mutate the |__proto__|.
   */
  function isPrototypeOfFunctionMutable(fn: Function): boolean {
    // We don't list all builtins here, since not all of them are used in the object
    // hierarchy.
    switch (fn) {
      case Object:
      case Array:
      case Uint8Array:
      case Uint16Array:
      case Float32Array:
      case Float64Array:
      case Int8Array:
      case Int16Array:
      case Int32Array:
        return false;
      default:
        return true;
    }
  }

  export var stdoutWriter = new IndentingWriter();
  export var stderrWriter = new IndentingWriter(false, IndentingWriter.stderr);

  export enum ExecutionPhase {
    /**
     * Default runtime behaviour.
     */
    Runtime = 0,

    /**
     * When compiling code statically.
     */
    Compiler = 1
  }

  export var phase = ExecutionPhase.Runtime;

  export var internedStrings: Map<string, java.lang.String> = new Map<string, java.lang.String>();

  declare var util;

  import assert = J2ME.Debug.assert;
  import concat3 = StringUtilities.concat3;
  import concat5 = StringUtilities.concat5;

  export enum RuntimeStatus {
    New       = 1,
    Started   = 2,
    Stopping  = 3, // Unused
    Stopped   = 4
  }

  export enum MethodType {
    Interpreted,
    Native,
    Compiled
  }

  var hashMap = Object.create(null);

  var hashArray = new Int32Array(1024);

  function hashString(s: string) {
    if (hashArray.length < s.length) {
      hashArray = new Int32Array((hashArray.length * 2 / 3) | 0);
    }
    var data = hashArray;
    for (var i = 0; i < s.length; i++) {
      data[i] = s.charCodeAt(i);
    }
    var hash = HashUtilities.hashBytesTo32BitsMurmur(data, 0, s.length);

    if (!release) { // Check to see that no collisions have ever happened.
      if (hashMap[hash] && hashMap[hash] !== s) {
        assert(false, "This is very bad.")
      }
      hashMap[hash] = s;
    }

    return hash;
  }

  function isIdentifierChar(c: number): boolean {
    return (c >= 97   && c <= 122)   || // a .. z
           (c >= 65   && c <=  90)   || // A .. Z
           (c === 36) || (c === 95);    // $ && _
  }

  function isDigit(c: number): boolean {
    return c >= 48 && c <= 57;
  }

  var invalidChars = "[];/<>()";
  var replaceChars = "abc_defg";

  function needsEscaping(s: string): boolean {
    var l = s.length;
    for (var i = 0; i < l; i++) {
      var c = s.charCodeAt(i);
      if (!isIdentifierChar(c)) {
        return true;
      }
    }
    return false;
  }

  // Fast lookup table.
  var map = new Array(128);
  for (var i = 0; i < 128; i++) {
    map[i] = String.fromCharCode(i);
  }

  // Patch up some entries.
  var invalidChars = "[];/<>()";
  var replaceChars = "abc_defg";
  for (var i = 0; i < invalidChars.length; i++) {
    map[invalidChars.charCodeAt(i)] = replaceChars[i];
  }

  // Reuse array.
  var T = new Array(1024);

  export function escapeString(s: string): string {
    if (!needsEscaping(s)) {
      return s;
    }
    var l = s.length;
    var r = T;
    r.length = l;
    for (var i = 0; i < l; i++) {
      var c = s.charCodeAt(i);
      if (i === 0 && isDigit(c)) {
        r[i] = String.fromCharCode(c - 48 + 97); // Map 0 .. 9 to a .. j
      } else if (c < 128) {
        r[i] = map[c]
      } else {
        r[i] = s[i];
      }
    }
    return r.join("");
  }

  var stringHashes = Object.create(null);
  var stringHashCount = 0;

  function hashStringStrong(s): string {
    // Hash with Murmur hash.
    var result = StringUtilities.variableLengthEncodeInt32(hashString(s));
    // Also use the length for some more precision.
    result += StringUtilities.toEncoding(s.length & 0x3f);
    return result;
  }

  export function hashStringToString(s: string) {
    if (stringHashCount > 1024) {
      return hashStringStrong(s);
    }
    var c = stringHashes[s];
    if (c) {
      return c;
    }
    c = stringHashes[s] = hashStringStrong(s);
    stringHashCount ++;
    return c;
  }

  export function mangleClassAndMethod(methodInfo: MethodInfo) {
    var name = concat5(methodInfo.classInfo.className, "_", methodInfo.name, "_", hashStringToString(methodInfo.signature));
    if (!hashedMangledNames) {
      return escapeString(name);
    }
    return hashStringToString(name);
  }

  export function mangleMethod(methodInfo: MethodInfo) {
    var name = concat3(methodInfo.name, "_", hashStringToString(methodInfo.signature));
    if (!hashedMangledNames) {
      return escapeString(name);
    }
    return "$" + hashStringToString(name);
  }

  export function mangleClassName(name: string): string {
    if (!hashedMangledNames) {
      return "$" + escapeString(name);
    }
    return "$" + hashStringToString(name);
  }

  export function mangleClass(classInfo: ClassInfo) {
    if (classInfo.mangledName) {
      return classInfo.mangledName;
    }
    return mangleClassName(classInfo.className);
  }

  /**
   * This class is abstract and should never be initialized. It only acts as a template for
   * actual runtime objects.
   */
  export class RuntimeTemplate {
    static all = new Set();
    jvm: JVM;
    status: RuntimeStatus;
    waiting: any [];
    threadCount: number;
    initialized: any;
    pending: any;
    staticFields: any;
    classObjects: any;
    classInitLockObjects: any;
    ctx: Context;

    isolate: com.sun.cldc.isolate.Isolate;
    mainThread: java.lang.Thread;

    private static _nextRuntimeId: number = 0;
    private _runtimeId: number;
    private _nextHashCode: number;

    constructor(jvm: JVM) {
      this.jvm = jvm;
      this.status = RuntimeStatus.New;
      this.waiting = [];
      this.threadCount = 0;
      this.initialized = Object.create(null);
      this.pending = {};
      this.staticFields = {};
      this.classObjects = {};
      this.ctx = null;
      this.classInitLockObjects = {};
      this._runtimeId = RuntimeTemplate._nextRuntimeId ++;
      this._nextHashCode = this._runtimeId << 24;
    }

    /**
     * Generates a new hash code for the specified |object|.
     */
    nextHashCode(): number {
      return this._nextHashCode ++;
    }

    waitStatus(callback) {
      this.waiting.push(callback);
    }

    updateStatus(status: RuntimeStatus) {
      this.status = status;
      var waiting = this.waiting;
      this.waiting = [];
      waiting.forEach(function (callback) {
        try {
          callback();
        } catch (ex) {
          // If the callback calls Runtime.prototype.waitStatus to continue waiting,
          // then waitStatus will throw VM.Pause, which shouldn't propagate up to
          // the caller of Runtime.prototype.updateStatus, so we silently ignore it
          // (along with any other exceptions thrown by the callback, so they don't
          // propagate to the caller of updateStatus).
        }
      });
    }

    addContext(ctx) {
      ++this.threadCount;
      RuntimeTemplate.all.add(this);
    }

    removeContext(ctx) {
      if (!--this.threadCount) {
        RuntimeTemplate.all.delete(this);
        this.updateStatus(RuntimeStatus.Stopped);
      }
    }

    newStringConstant(s: string): java.lang.String {
      if (internedStrings.has(s)) {
        return internedStrings.get(s);
      }
      var obj = J2ME.newString(s);
      internedStrings.set(s, obj);
      return obj;
    }

    setStatic(field, value) {
      this.staticFields[field.id] = value;
    }

    getStatic(field) {
      return this.staticFields[field.id];
    }

    newIOException(str?: string): java.io.IOException {
      return <java.io.IOException>$.ctx.createException(
        "java/io/IOException", str);
    }

    newUnsupportedEncodingException(str?: string): java.io.UnsupportedEncodingException {
      return <java.io.UnsupportedEncodingException>$.ctx.createException(
        "java/io/UnsupportedEncodingException", str);
    }

    newUTFDataFormatException(str?: string): java.io.UTFDataFormatException {
      return <java.io.UTFDataFormatException>$.ctx.createException(
        "java/io/UTFDataFormatException", str);
    }

    newSecurityException(str?: string): java.lang.SecurityException {
      return <java.lang.SecurityException>$.ctx.createException(
        "java/lang/SecurityException", str);
    }

    newIllegalThreadStateException(str?: string): java.lang.IllegalThreadStateException {
      return <java.lang.IllegalThreadStateException>$.ctx.createException(
        "java/lang/IllegalThreadStateException", str);
    }

    newRuntimeException(str?: string): java.lang.RuntimeException {
      return <java.lang.RuntimeException>$.ctx.createException(
        "java/lang/RuntimeException", str);
    }

    newIndexOutOfBoundsException(str?: string): java.lang.IndexOutOfBoundsException {
      return <java.lang.IndexOutOfBoundsException>$.ctx.createException(
        "java/lang/IndexOutOfBoundsException", str);
    }

    newArrayIndexOutOfBoundsException(str?: string): java.lang.ArrayIndexOutOfBoundsException {
      return <java.lang.ArrayIndexOutOfBoundsException>$.ctx.createException(
        "java/lang/ArrayIndexOutOfBoundsException", str);
    }

    newStringIndexOutOfBoundsException(str?: string): java.lang.StringIndexOutOfBoundsException {
      return <java.lang.StringIndexOutOfBoundsException>$.ctx.createException(
        "java/lang/StringIndexOutOfBoundsException", str);
    }

    newArrayStoreException(str?: string): java.lang.ArrayStoreException {
      return <java.lang.ArrayStoreException>$.ctx.createException(
        "java/lang/ArrayStoreException", str);
    }

    newIllegalMonitorStateException(str?: string): java.lang.IllegalMonitorStateException {
      return <java.lang.IllegalMonitorStateException>$.ctx.createException(
        "java/lang/IllegalMonitorStateException", str);
    }

    newClassCastException(str?: string): java.lang.ClassCastException {
      return <java.lang.ClassCastException>$.ctx.createException(
        "java/lang/ClassCastException", str);
    }

    newArithmeticException(str?: string): java.lang.ArithmeticException {
      return <java.lang.ArithmeticException>$.ctx.createException(
        "java/lang/ArithmeticException", str);
    }

    newClassNotFoundException(str?: string): java.lang.ClassNotFoundException {
      return <java.lang.ClassNotFoundException>$.ctx.createException(
        "java/lang/ClassNotFoundException", str);
    }

    newIllegalArgumentException(str?: string): java.lang.IllegalArgumentException {
      return <java.lang.IllegalArgumentException>$.ctx.createException(
        "java/lang/IllegalArgumentException", str);
    }

    newIllegalStateException(str?: string): java.lang.IllegalStateException {
      return <java.lang.IllegalStateException>$.ctx.createException(
        "java/lang/IllegalStateException", str);
    }

    newNegativeArraySizeException(str?: string): java.lang.NegativeArraySizeException {
      return <java.lang.NegativeArraySizeException>$.ctx.createException(
        "java/lang/NegativeArraySizeException", str);
    }

    newNullPointerException(str?: string): java.lang.NullPointerException {
      return <java.lang.NullPointerException>$.ctx.createException(
        "java/lang/NullPointerException", str);
    }

    newMediaException(str?: string): javax.microedition.media.MediaException {
      return <javax.microedition.media.MediaException>$.ctx.createException(
        "javax/microedition/media/MediaException", str);
    }

    newException(str?: string): java.lang.Exception {
      return <java.lang.Exception>$.ctx.createException(
        "java/lang/Exception", str);
    }

  }

  export enum VMState {
    Running = 0,
    Yielding = 1,
    Pausing = 2
  }

  /** @const */ export var MAX_PRIORITY: number = 10;
  /** @const */ export var MIN_PRIORITY: number = 1;
  /** @const */ export var NORMAL_PRIORITY: number = 5;

  class PriorityQueue {
    private _top: number;
    private _queues: Context[][];

    constructor() {
      this._top = MIN_PRIORITY;
      this._queues = [];
      for (var i = MIN_PRIORITY; i <= MAX_PRIORITY; i++) {
        this._queues[i] = [];
      }
    }

    /*
     * @param jump If true, move the context to the first of others who have the
     * same priority.
     */
    enqueue(ctx: Context, jump: boolean) {
      var priority = ctx.getPriority();
      release || assert(priority >= MIN_PRIORITY && priority <= MAX_PRIORITY,
                        "Invalid priority: " + priority);
      if (jump) {
        this._queues[priority].unshift(ctx);
      } else {
        this._queues[priority].push(ctx);
      }
      this._top = Math.max(priority, this._top);
    }

    dequeue(): Context {
      if (this.isEmpty()) {
        return null;
      }
      var ctx = this._queues[this._top].shift();
      while (this._queues[this._top].length === 0 && this._top > MIN_PRIORITY) {
        this._top--;
      }
      return ctx;
    }

    isEmpty() {
      return this._top === MIN_PRIORITY && this._queues[this._top].length === 0;
    }
  }

  export class Runtime extends RuntimeTemplate {
    private static _nextId: number = 0;
    private static _runningQueue: PriorityQueue = new PriorityQueue();

    id: number;


    /*
     * The thread scheduler uses green thread algorithm, which a preemptive,
     * priority based algorithm.
     * All Java threads have a priority and the thread with he highest priority
     * is scheduled to run.
     * In case two threads have the same priority a FIFO ordering is followed.
     * A different thread is invoked to run only if
     *   1. The current thread blocks or terminates.
     *   2. A thread with a higher priority than the current thread enters the
     *      Runnable state. The lower priority thread is preempted and the
     *      higher priority thread is scheduled to run.
     */
    static scheduleRunningContext(ctx: Context) {
      var isEmpty = Runtime._runningQueue.isEmpty();
      // Preempt current thread if the new thread has higher priority
      if ($ && ctx.getPriority() > $.ctx.getPriority()) {
        Runtime._runningQueue.enqueue($.ctx, true);
        Runtime._runningQueue.enqueue(ctx, false);
        $.pause("preempt");
      } else {
        Runtime._runningQueue.enqueue(ctx, false);
      }
      if (isEmpty) {
        Runtime.processRunningQueue();
      }
    }

    private static processRunningQueue() {
      (<any>window).setZeroTimeout(function() {
        try {
          Runtime._runningQueue.dequeue().execute();
        } finally {
          if (!Runtime._runningQueue.isEmpty()) {
            Runtime.processRunningQueue();
          }
        }
      });
    }

    /**
     * Bailout callback whenever a JIT frame is unwound.
     */
    B(bci: number, nextBCI: number, local: any [], stack: any [], lockObject: java.lang.Object) {
      var methodInfo = jitMethodInfos[(<any>arguments.callee.caller).name];
      release || assert(methodInfo !== undefined);
      $.ctx.bailout(methodInfo, bci, nextBCI, local, stack, lockObject);
    }

    yield() {
      windingWriter && windingWriter.writeLn("yielding");
      unwindCount ++;
      runtimeCounter && runtimeCounter.count("yielding");
      U = VMState.Yielding;
    }

    pause(reason: string) {
      windingWriter && windingWriter.writeLn("pausing");
      unwindCount ++;
      runtimeCounter && runtimeCounter.count("pausing " + reason);
      U = VMState.Pausing;
    }

    constructor(jvm: JVM) {
      super(jvm);
      this.id = Runtime._nextId ++;
    }
  }

  export class Class {
    constructor(public klass: Klass) {
      // ...
    }
  }

  /**
   * Representation of a template class.
   */
  export interface Klass extends Function {
    new (): java.lang.Object;

    /**
     * Array klass of this klass, constructed via \arrayKlass\.
     */
    arrayKlass: Klass;

    superKlass: Klass;

    /**
     * Would be nice to remove this. So we try not to depend on it too much.
     */
    classInfo: ClassInfo;

    /**
     * Flattened array of super klasses. This makes type checking easy,
     * see |classInstanceOf|.
     */
    display: Klass [];

    /**
     * Flattened array of super klasses. This makes type checking easy,
     * see |classInstanceOf|.
     */
    interfaces: Klass [];

    /**
     * Depth in the class hierarchy.
     */
    depth: number;

    classSymbols: string [];

    /**
     * Static constructor, not all klasses have one.
     */
    staticConstructor: () => void;

    /**
     * Whether this class is an interface class.
     */
    isInterfaceKlass: boolean;

    isArrayKlass: boolean;

    elementKlass: Klass;
  }

  export class RuntimeKlass {
    templateKlass: Klass;

    /**
     * Java class object. This is only available on runtime klasses and it points to itself. We go trough
     * this indirection in VM code for now so that we can easily change it later if we need to.
     */
    classObject: java.lang.Class;

    /**
     * Whether this class is a runtime class.
     */
    // isRuntimeKlass: boolean;

    constructor(templateKlass: Klass) {
      this.templateKlass = templateKlass;
    }
  }

  export class Lock {
    constructor(public thread: java.lang.Thread, public level: number) {
      // ...
    }
  }

  function initializeClassObject(runtimeKlass: RuntimeKlass) {
    linkWriter && linkWriter.writeLn("Initializing Class Object For: " + runtimeKlass.templateKlass);
    release || assert(!runtimeKlass.classObject);
    runtimeKlass.classObject = <java.lang.Class><any>new Klasses.java.lang.Class();
    runtimeKlass.classObject.runtimeKlass = runtimeKlass;
    var fields = runtimeKlass.templateKlass.classInfo.fields;
    for (var i = 0; i < fields.length; i++) {
      var field = fields[i];
      if (field.isStatic) {
        var kind = TypeDescriptor.makeTypeDescriptor(field.signature).kind;
        var defaultValue;
        switch (kind) {
          case Kind.Reference:
            defaultValue = null;
            break;
          case Kind.Long:
            defaultValue = Long.ZERO;
            break;
          default:
            defaultValue = 0;
            break;
        }
        field.set(<java.lang.Object><any>runtimeKlass, defaultValue);
      }
    }
  }

  /**
   * Registers the klass as a getter on the runtime template. On first access, the getter creates a runtime klass and
   * adds it to the runtime.
   */
  export function registerKlass(klass: Klass, classInfo: ClassInfo) {
    linkWriter && linkWriter.writeLn("Registering Klass: " + classInfo.className);
    Object.defineProperty(RuntimeTemplate.prototype, classInfo.mangledName, {
      configurable: true,
      get: function () {
        linkWriter && linkWriter.writeLn("Creating Runtime Klass: " + classInfo.className);
        release || assert(!(klass instanceof RuntimeKlass));
        var runtimeKlass = new RuntimeKlass(klass);
        initializeClassObject(runtimeKlass);
        Object.defineProperty(this, classInfo.mangledName, {
          value: runtimeKlass
        });
        initWriter && initWriter.writeLn("Running Static Constructor: " + classInfo.className);
        $.ctx.pushClassInitFrame(classInfo);
        // release || assert(!U);

        //// TODO: monitorEnter
        //if (klass.staticInitializer) {
        //  klass.staticInitializer.call(runtimeKlass);
        //}
        //if (klass.staticConstructor) {
        //  klass.staticConstructor.call(runtimeKlass);
        //}
        return runtimeKlass;
      }
    });
  }

  var unresolvedSymbols = Object.create(null);

  function findKlass(classInfo: ClassInfo) {
    if (unresolvedSymbols[classInfo.mangledName]) {
      return null;
    }
    var klass = jsGlobal[classInfo.mangledName];
    if (klass) {
      return klass;
    }
    return null;
  }

  export function registerKlassSymbol(className: string) {
    // TODO: This needs to be kept in sync to how mangleClass works.
    var mangledName = mangleClassName(className);
    if (RuntimeTemplate.prototype.hasOwnProperty(mangledName)) {
      return;
    }
    linkWriter && linkWriter.writeLn("Registering Klass Symbol: " + className);
    if (!RuntimeTemplate.prototype.hasOwnProperty(mangledName)) {
      Object.defineProperty(RuntimeTemplate.prototype, mangledName, {
        configurable: true,
        get: function lazyKlass() {
          linkWriter && linkWriter.writeLn("Load Klass: " + className);
          CLASSES.loadAndLinkClass(className);
          return this[mangledName]; // This should not be recursive at this point.
        }
      });
    }

    if (!jsGlobal.hasOwnProperty(mangledName)) {
      unresolvedSymbols[mangledName] = true;
      Object.defineProperty(jsGlobal, mangledName, {
        configurable: true,
        get: function () {
          linkWriter && linkWriter.writeLn("Load Klass: " + className);
          CLASSES.loadAndLinkClass(className);
          return this[mangledName]; // This should not be recursive at this point.
        }
      });
    }
  }

  export function registerKlassSymbols(classNames: string []) {
    for (var i = 0; i < classNames.length; i++) {
      var className = classNames[i];
      registerKlassSymbol(className);
    }
  }

  export function getRuntimeKlass(runtime: Runtime, klass: Klass): RuntimeKlass {
    release || assert(!(klass instanceof RuntimeKlass));
    release || assert(klass.classInfo.mangledName);
    var runtimeKlass = runtime[klass.classInfo.mangledName];
    // assert(runtimeKlass instanceof RuntimeKlass);
    return runtimeKlass;
  }

  function setKlassSymbol(mangledName: string, klass: Klass) {
    Object.defineProperty(jsGlobal, mangledName, {
      value: klass
    });
  }

  function emitKlassConstructor(classInfo: ClassInfo, mangledName: string): Klass {
    var klass: Klass;
    enterTimeline("emitKlassConstructor");
    // TODO: Creating and evaling a Klass here may be too slow at startup. Consider
    // creating a closure, which will probably be slower at runtime.
    var source = "";
    var writer = new IndentingWriter(false, x => source += x + "\n");
    var emitter = new Emitter(writer, false, true, true);
    J2ME.emitKlass(emitter, classInfo);
    (1, eval)(source);
    leaveTimeline("emitKlassConstructor");
    // consoleWriter.writeLn("Synthesizing Klass: " + classInfo.className);
    // consoleWriter.writeLn(source);
    klass = <Klass>jsGlobal[mangledName];
    release || assert(klass, mangledName);
    klass.toString = function () {
      return "[Synthesized Klass " + classInfo.className + "]";
    };
    return klass;
  }

  export function getKlass(classInfo: ClassInfo): Klass {
    if (!classInfo) {
      return null;
    }
    if (classInfo.klass) {
      return classInfo.klass;
    }
    return makeKlass(classInfo);
  }

  function makeKlass(classInfo: ClassInfo): Klass {
    var klass = findKlass(classInfo);
    if (klass) {
      release || assert (!classInfo.isInterface, "Interfaces should not be compiled.");
      linkWriter && linkWriter.greenLn("Found Compiled Klass: " + classInfo.className);
      release || assert(!classInfo.klass);
      classInfo.klass = klass;
      klass.toString = function () {
        return "[Compiled Klass " + classInfo.className + "]";
      };
      if (klass.classSymbols) {
        registerKlassSymbols(klass.classSymbols);
      }
    } else {
      klass = makeKlassConstructor(classInfo);
      release || assert(!classInfo.klass);
      classInfo.klass = klass;
    }

    if (classInfo.superClass && !classInfo.superClass.klass &&
      J2ME.phase === J2ME.ExecutionPhase.Runtime) {
      J2ME.linkKlass(classInfo.superClass);
    }

    var superKlass = getKlass(classInfo.superClass);

    enterTimeline("extendKlass");
    extendKlass(klass, superKlass);
    leaveTimeline("extendKlass");

    enterTimeline("registerKlass");
    registerKlass(klass, classInfo);
    leaveTimeline("registerKlass");

    if (classInfo.isArrayClass) {
      klass.isArrayKlass = true;
      var elementKlass = getKlass(classInfo.elementClass);
      elementKlass.arrayKlass = klass;
      klass.elementKlass = elementKlass;
    }

    klass.classInfo = classInfo;

    if (!classInfo.isInterface) {
      initializeInterfaces(klass, classInfo);
    }

    return klass;
  }

  function makeKlassConstructor(classInfo: ClassInfo): Klass {
    var klass: Klass;
    var mangledName = classInfo.mangledName;
    if (classInfo.isInterface) {
      klass = <Klass><any>function () {
        Debug.unexpected("Should never be instantiated.")
      };
      klass.isInterfaceKlass = true;
      klass.toString = function () {
        return "[Interface Klass " + classInfo.className + "]";
      };
      setKlassSymbol(mangledName, klass);
    } else if (classInfo.isArrayClass) {
      var elementKlass = getKlass(classInfo.elementClass);
      // Have we already created one? We need to maintain pointer identity.
      if (elementKlass.arrayKlass) {
        return elementKlass.arrayKlass;
      }
      klass = makeArrayKlassConstructor(elementKlass);
    } else if (classInfo instanceof PrimitiveClassInfo) {
      klass = <Klass><any>function () {
        Debug.unexpected("Should never be instantiated.")
      };
      klass.toString = function () {
        return "[Primitive Klass " + classInfo.className + "]";
      };
    } else {
      klass = emitKlassConstructor(classInfo, mangledName);
    }
    return klass;
  }

  export function makeArrayKlassConstructor(elementKlass: Klass): Klass {
    var klass = <Klass><any> getArrayConstructor(elementKlass.classInfo.className);
    if (!klass) {
      klass = <Klass><any> function (size: number) {
        var array = createEmptyObjectArray(size);
        (<any>array).klass = klass;
        return array;
      };
      klass.toString = function () {
        return "[Array of " + elementKlass + "]";
      };
    } else {
      release || assert(!klass.prototype.hasOwnProperty("klass"));
      klass.prototype.klass = klass;
      klass.toString = function () {
        return "[Array of " + elementKlass + "]";
      };
    }
    return klass;
  }

  /**
   * TODO: Find out if we need to also run class initialization here, or if the
   * callers should be calling that instead of this.
   */
  export function linkKlass(classInfo: ClassInfo) {
    if (classInfo.klass) {
      return;
    }
    enterTimeline("linkKlass", {classInfo: classInfo});
    var mangledName = classInfo.mangledName;
    var klass;
    classInfo.klass = klass = getKlass(classInfo);
    classInfo.klass.classInfo = classInfo;
    if (classInfo instanceof PrimitiveClassInfo) {
      switch (classInfo) {
        case PrimitiveClassInfo.Z: Klasses.boolean = klass; break;
        case PrimitiveClassInfo.C: Klasses.char    = klass; break;
        case PrimitiveClassInfo.F: Klasses.float   = klass; break;
        case PrimitiveClassInfo.D: Klasses.double  = klass; break;
        case PrimitiveClassInfo.B: Klasses.byte    = klass; break;
        case PrimitiveClassInfo.S: Klasses.short   = klass; break;
        case PrimitiveClassInfo.I: Klasses.int     = klass; break;
        case PrimitiveClassInfo.J: Klasses.long    = klass; break;
        default: J2ME.Debug.assertUnreachable("linking primitive " + classInfo.className)
      }
    } else {
      switch (classInfo.className) {
        case "java/lang/Object": Klasses.java.lang.Object = klass; break;
        case "java/lang/Class" : Klasses.java.lang.Class  = klass; break;
        case "java/lang/String": Klasses.java.lang.String = klass; break;
        case "java/lang/Thread": Klasses.java.lang.Thread = klass; break;
        case "java/lang/Exception": Klasses.java.lang.Exception = klass; break;
        case "java/lang/IllegalArgumentException": Klasses.java.lang.IllegalArgumentException = klass; break;
        case "java/lang/NegativeArraySizeException": Klasses.java.lang.NegativeArraySizeException = klass; break;
        case "java/lang/IllegalStateException": Klasses.java.lang.IllegalStateException = klass; break;
        case "java/lang/NullPointerException": Klasses.java.lang.NullPointerException = klass; break;
        case "java/lang/RuntimeException": Klasses.java.lang.RuntimeException = klass; break;
        case "java/lang/IndexOutOfBoundsException": Klasses.java.lang.IndexOutOfBoundsException = klass; break;
        case "java/lang/ArrayIndexOutOfBoundsException": Klasses.java.lang.ArrayIndexOutOfBoundsException = klass; break;
        case "java/lang/StringIndexOutOfBoundsException": Klasses.java.lang.StringIndexOutOfBoundsException = klass; break;
        case "java/lang/ArrayStoreException": Klasses.java.lang.ArrayStoreException = klass; break;
        case "java/lang/IllegalMonitorStateException": Klasses.java.lang.IllegalMonitorStateException = klass; break;
        case "java/lang/ClassCastException": Klasses.java.lang.ClassCastException = klass; break;
        case "java/lang/ArithmeticException": Klasses.java.lang.ArithmeticException = klass; break;
        case "java/lang/NegativeArraySizeException": Klasses.java.lang.NegativeArraySizeException = klass; break;
        case "java/lang/ClassNotFoundException": Klasses.java.lang.ClassNotFoundException = klass; break;
        case "javax/microedition/media/MediaException": Klasses.javax.microedition.media.MediaException = klass; break;
        case "java/lang/SecurityException": Klasses.java.lang.SecurityException = klass; break;
        case "java/lang/IllegalThreadStateException": Klasses.java.lang.IllegalThreadStateException = klass; break;
        case "java/io/IOException": Klasses.java.io.IOException = klass; break;
        case "java/io/UnsupportedEncodingException": Klasses.java.io.UnsupportedEncodingException = klass; break;
        case "java/io/UTFDataFormatException": Klasses.java.io.UTFDataFormatException = klass; break;
      }
    }
    linkWriter && linkWriter.writeLn("Link: " + classInfo.className + " -> " + klass);

    enterTimeline("linkKlassMethods");
    linkKlassMethods(classInfo.klass);
    leaveTimeline("linkKlassMethods");

    enterTimeline("linkKlassFields");
    linkKlassFields(classInfo.klass);
    leaveTimeline("linkKlassFields");
    leaveTimeline("linkKlass");

    if (klass === Klasses.java.lang.Object) {
      extendKlass(<Klass><any>Array, Klasses.java.lang.Object);
    }
  }

  function findNativeMethodBinding(methodInfo: MethodInfo) {
    var classBindings = Bindings[methodInfo.classInfo.className];
    if (classBindings && classBindings.native) {
      var method = classBindings.native[methodInfo.name + "." + methodInfo.signature];
      if (method) {
        return method;
      }
    }
    return null;
  }

  function findNativeMethodImplementation(methodInfo: MethodInfo) {
    // Look in bindings first.
    var binding = findNativeMethodBinding(methodInfo);
    if (binding) {
      return binding;
    }
    var implKey = methodInfo.implKey;
    if (methodInfo.isNative) {
      if (implKey in Native) {
        return Native[implKey];
      } else {
        // Some Native MethodInfos are constructed but never called;
        // that's fine, unless we actually try to call them.
        return function missingImplementation() {
          stderrWriter.errorLn("implKey " + methodInfo.implKey + " is native but does not have an implementation.");
        }
      }
    } else if (implKey in Override) {
      return Override[implKey];
    }
    return null;
  }

  function prepareInterpretedMethod(methodInfo: MethodInfo): Function {

    // Adapter for the most common case.
    if (!methodInfo.isSynchronized && !methodInfo.hasTwoSlotArguments) {
      var method = function fastInterpreterFrameAdapter() {
        var frame = Frame.create(methodInfo, [], 0);
        var j = 0;
        if (!methodInfo.isStatic) {
          frame.setLocal(j++, this);
        }
        var slots = methodInfo.argumentSlots;
        for (var i = 0; i < slots; i++) {
          frame.setLocal(j++, arguments[i]);
        }
        return $.ctx.executeFrames([frame]);
      };
      (<any>method).methodInfo = methodInfo;
      return method;
    }

    var method = function interpreterFrameAdapter() {
      var frame = Frame.create(methodInfo, [], 0);
      var j = 0;
      if (!methodInfo.isStatic) {
        frame.setLocal(j++, this);
      }
      var typeDescriptors = methodInfo.signatureDescriptor.typeDescriptors;
      release || assert (arguments.length === typeDescriptors.length - 1,
        "Number of adapter frame arguments (" + arguments.length + ") does not match signature descriptor " +
        methodInfo.signatureDescriptor);
      for (var i = 1; i < typeDescriptors.length; i++) {
        frame.setLocal(j++, arguments[i - 1]);
        if (isTwoSlot(typeDescriptors[i].kind)) {
          frame.setLocal(j++, null);
        }
      }
      if (methodInfo.isSynchronized) {
        if (!frame.lockObject) {
          frame.lockObject = methodInfo.isStatic
            ? methodInfo.classInfo.getClassObject()
            : frame.getLocal(0);
        }
        $.ctx.monitorEnter(frame.lockObject);
        if (U === VMState.Pausing) {
          $.ctx.frames.push(frame);
          return;
        }
      }
      return $.ctx.executeFrames([frame]);
    };
    (<any>method).methodInfo = methodInfo;
    return method;
  }

  function findCompiledMethod(klass: Klass, methodInfo: MethodInfo): Function {
    return jsGlobal[methodInfo.mangledClassAndMethodName];
  }

  /**
   * Creates convenience getters / setters on Java objects.
   */
  function linkKlassFields(klass: Klass) {
    var classInfo = klass.classInfo;
    var fields = classInfo.fields;
    var classBindings = Bindings[klass.classInfo.className];
    if (classBindings && classBindings.fields) {
      for (var i = 0; i < fields.length; i++) {
        var field = fields[i];
        var key = field.name + "." + field.signature;
        var symbols = field.isStatic ? classBindings.fields.staticSymbols :
                                       classBindings.fields.instanceSymbols;
        if (symbols && symbols[key]) {
          release || assert(!field.isStatic, "Static fields are not supported yet.");
          var symbolName = symbols[key];
          var object = field.isStatic ? klass : klass.prototype;
          release || assert (!object.hasOwnProperty(symbolName), "Should not overwrite existing properties.");
          var getter = FunctionUtilities.makeForwardingGetter(field.mangledName);
          var setter;
          if (release) {
            setter = FunctionUtilities.makeForwardingSetter(field.mangledName);
          } else {
            setter = FunctionUtilities.makeDebugForwardingSetter(field.mangledName, getKindCheck(field.kind));
          }
          Object.defineProperty(object, symbolName, {
            get: getter,
            set: setter,
            configurable: true,
            enumerable: false
          });
        }
      }
    }
  }

  function linkKlassMethods(klass: Klass) {
    linkWriter && linkWriter.enter("Link Klass Methods: " + klass);
    var methods = klass.classInfo.methods;
    for (var i = 0; i < methods.length; i++) {
      var methodInfo = methods[i];
      if (methodInfo.isAbstract) {
        continue;
      }
      var fn;
      var methodType;
      var nativeMethod = findNativeMethodImplementation(methods[i]);
      var methodDescription = methods[i].name + methods[i].signature;
      var updateGlobalObject = true;
      if (nativeMethod) {
        linkWriter && linkWriter.writeLn("Method: " + methodDescription + " -> Native / Override");
        fn = nativeMethod;
        methodType = MethodType.Native;
        methodInfo.state = MethodState.Compiled;
      } else {
        fn = findCompiledMethod(klass, methodInfo);
        if (fn) {
          linkWriter && linkWriter.greenLn("Method: " + methodDescription + " -> Compiled");
          methodType = MethodType.Compiled;
          // Save method info so that we can figure out where we are bailing
          // out from.
          jitMethodInfos[fn.name] = methodInfo;
          updateGlobalObject = false;
          methodInfo.state = MethodState.Compiled;
        } else {
          linkWriter && linkWriter.warnLn("Method: " + methodDescription + " -> Interpreter");
          methodType = MethodType.Interpreted;
          fn = prepareInterpretedMethod(methodInfo);
        }
      }
      if (false && methodTimeline) {
        fn = profilingWrapper(fn, methodInfo, methodType);
        updateGlobalObject = true;
      }

      if (traceWriter) {
        fn = tracingWrapper(fn, methodInfo, methodType);
        updateGlobalObject = true;
      }

      methodInfo.fn = fn;

      // Link even non-static methods globally so they can be invoked statically via invokespecial.
      if (updateGlobalObject) {
        jsGlobal[methodInfo.mangledClassAndMethodName] = fn;
      }
      if (!methodInfo.isStatic) {
        klass.prototype[methodInfo.mangledName] = fn;
      }
    }

    linkWriter && linkWriter.outdent();

    function profilingWrapper(fn: Function, methodInfo: MethodInfo, methodType: MethodType) {
      return function (a, b, c, d) {
        var key = MethodType[methodType];
        if (methodType === MethodType.Interpreted) {
          nativeCounter.count(MethodType[MethodType.Interpreted]);
          key += methodInfo.isSynchronized ? " Synchronized" : "";
          key += methodInfo.exception_table.length ? " Has Exceptions" : "";
          // key += " " + methodInfo.implKey;
        }
        // var key = methodType !== MethodType.Interpreted ? MethodType[methodType] : methodInfo.implKey;
        // var key = MethodType[methodType] + " " + methodInfo.implKey;
        nativeCounter.count(key);
        var s = bytecodeCount;
        try {
          methodTimeline.enter(key);
          var r;
          switch (arguments.length) {
            case 0:
              r = fn.call(this);
              break;
            case 1:
              r = fn.call(this, a);
              break;
            case 2:
              r = fn.call(this, a, b);
              break;
            case 3:
              r = fn.call(this, a, b, c);
              break;
            default:
              r = fn.apply(this, arguments);
          }
          methodTimeline.leave(key, s !== bytecodeCount ? { bytecodeCount: bytecodeCount - s } : undefined);
        } catch (e) {
          methodTimeline.leave(key, s !== bytecodeCount ? { bytecodeCount: bytecodeCount - s } : undefined);
          throw e;
        }
        return r;
      };
    }

    function tracingWrapper(fn: Function, methodInfo: MethodInfo, methodType: MethodType) {
      return function() {
        var args = Array.prototype.slice.apply(arguments);
        traceWriter.enter("> " + MethodType[methodType][0] + " " + methodInfo.implKey + " " + (methodInfo.callCount ++));
        var s = performance.now();
        var value = fn.apply(this, args);
        traceWriter.outdent();
        return value;
      };
    }
  }

  /**
   * Creates lookup tables used to efficiently implement type checks.
   */
  function initializeKlassTables(klass: Klass) {
    linkWriter && linkWriter.writeLn("initializeKlassTables: " + klass);
    klass.depth = klass.superKlass ? klass.superKlass.depth + 1 : 0;
    assert (klass.display === undefined, "Display should only be defined once.")
    var display = klass.display = new Array(32);

    var i = klass.depth;
    while (klass) {
      display[i--] = klass;
      klass = klass.superKlass;
    }
    release || assert(i === -1, i);
  }

  function initializeInterfaces(klass: Klass, classInfo: ClassInfo) {
    release || assert (!klass.interfaces);
    var interfaces = klass.interfaces = klass.superKlass ? klass.superKlass.interfaces.slice() : [];

    var interfaceClassInfos = classInfo.interfaces;
    for (var j = 0; j < interfaceClassInfos.length; j++) {
      ArrayUtilities.pushUnique(interfaces, getKlass(interfaceClassInfos[j]));
    }
  }

  export function extendKlass(klass: Klass, superKlass: Klass) {
    klass.superKlass = superKlass;
    if (superKlass) {
      if (isPrototypeOfFunctionMutable(klass)) {
        linkWriter && linkWriter.writeLn("Extending: " + klass + " -> " + superKlass);
        klass.prototype = Object.create(superKlass.prototype);
        // (<any>Object).setPrototypeOf(klass.prototype, superKlass.prototype);
        release || assert((<any>Object).getPrototypeOf(klass.prototype) === superKlass.prototype);
      } else {
        release || assert(!superKlass.superKlass, "Should not have a super-super-klass.");
          for (var key in superKlass.prototype) {
              klass.prototype[key] = superKlass.prototype[key];
          }
      }
    }
    klass.prototype.klass = klass;
    initializeKlassTables(klass);
  }

  /**
   * Number of methods that have been compiled thus far.
   */
  export var compiledCount = 0;

  /**
   * Number of ms that have been spent compiled code thus far.
   */
  var totalJITTime = 0;

  /**
   * Compiles method and links it up at runtime.
   */
  export function compileAndLinkMethod(methodInfo: MethodInfo) {
    // Don't do anything if we're past the compiled state.
    if (methodInfo.state >= MethodState.Compiled) {
      return;
    }

    // Don't compile methods that are too large.
    if (methodInfo.code.length > 2000) {
      jitWriter && jitWriter.writeLn("Not compiling: " + methodInfo.implKey + " because it's too large. " + methodInfo.code.length);
      methodInfo.state = MethodState.NotCompiled;
      return;
    }

    var mangledClassAndMethodName = methodInfo.mangledClassAndMethodName;

    compiledCount ++;

    jitWriter && jitWriter.enter("Compiling: " + methodInfo.implKey + ", currentBytecodeCount: " + methodInfo.bytecodeCount);
    var s = performance.now();

    var compiledMethod;
    enterTimeline("Compiling");
    try {
      compiledMethod = baselineCompileMethod(methodInfo, CompilationTarget.Runtime);
    } catch (e) {
      methodInfo.state = MethodState.CannotCompile;
      jitWriter && jitWriter.writeLn("Cannot compile: " + methodInfo.implKey + " because of " + e);
      leaveTimeline("Compiling");
      return;
    }
    leaveTimeline("Compiling");
    var compiledMethodName = mangledClassAndMethodName;
    var source = "function " + compiledMethodName +
                 "(" + compiledMethod.args.join(",") + ") {\n" +
                   compiledMethod.body +
                 "\n}";

    enterTimeline("Eval Compiled Code");
    // This overwrites the method on the global object.
    (1, eval)(source);
    leaveTimeline("Eval Compiled Code");

    // Attach the compiled method to the method info object.
    var fn = jsGlobal[mangledClassAndMethodName];
    methodInfo.fn = fn;
    methodInfo.state = MethodState.Compiled;

    // Link member methods on the prototype.
    if (!methodInfo.isStatic) {
      methodInfo.classInfo.klass.prototype[methodInfo.mangledName] = fn;
    }

    // Make JITed code available in the |jitMethodInfos| so that bailout
    // code can figure out the caller.
    jitMethodInfos[mangledClassAndMethodName] = methodInfo;

    // Make sure all the referenced symbols are registered.
    var referencedClasses = compiledMethod.referencedClasses;
    for (var i = 0; i < referencedClasses.length; i++) {
      var referencedClass = referencedClasses[i];
      registerKlassSymbol(referencedClass.className);
    }

    var methodJITTime = (performance.now() - s);
    totalJITTime += methodJITTime;
    if (jitWriter) {
      jitWriter.leave(
        "Compilation Done: " + methodJITTime.toFixed(2) + " ms, " +
        "codeSize: " + methodInfo.code.length + ", " +
        "sourceSize: " + compiledMethod.body.length);
      jitWriter.writeLn("Total: " + totalJITTime.toFixed(2) + " ms");
    }
  }

  export function isAssignableTo(from: Klass, to: Klass): boolean {
    if (to.isInterfaceKlass) {
      return from.interfaces.indexOf(to) >= 0;
    } else if (to.isArrayKlass) {
      if (!from.isArrayKlass) {
        return false;
      }
      return isAssignableTo(from.elementKlass, to.elementKlass);
    }
    return from.display[to.depth] === to;
  }

  export function instanceOfKlass(object: java.lang.Object, klass: Klass): boolean {
    return object !== null && isAssignableTo(object.klass, klass);
  }

  export function instanceOfInterface(object: java.lang.Object, klass: Klass): boolean {
    release || assert(klass.isInterfaceKlass);
    return object !== null && isAssignableTo(object.klass, klass);
  }

  export function checkCastKlass(object: java.lang.Object, klass: Klass) {
    if (object !== null && !isAssignableTo(object.klass, klass)) {
      throw $.newClassCastException();
    }
  }

  export function checkCastInterface(object: java.lang.Object, klass: Klass) {
    if (object !== null && !isAssignableTo(object.klass, klass)) {
      throw $.newClassCastException();
    }
  }

  function createEmptyObjectArray(size: number) {
    var array = new Array(size);
    for (var i = 0; i < size; i++) {
      array[i] = null;
    }
    return array;
  }

  export function newObject(klass: Klass): java.lang.Object {
    return new klass();
  }

  export function newString(str: string): java.lang.String {
    if (str === null || str === undefined) {
      return null;
    }
    var object = <java.lang.String>newObject(Klasses.java.lang.String);
    object.str = str;
    return object;
  }

  export function newStringConstant(str: string): java.lang.String {
    return $.newStringConstant(str);
  };

  export function newArray(klass: Klass, size: number) {
    if (size < 0) {
      throw $.newNegativeArraySizeException();
    }
    var constructor: any = getArrayKlass(klass);
    return new constructor(size);
  }

  export function newObjectArray(size: number): java.lang.Object[] {
    return newArray(Klasses.java.lang.Object, size);
  }

  export function newStringArray(size: number): java.lang.String[]  {
    return newArray(Klasses.java.lang.String, size);
  }

  export function newByteArray(size: number): number[]  {
    return newArray(Klasses.byte, size);
  }

  export function getArrayKlass(elementKlass: Klass): Klass {
    // Have we already created one? We need to maintain pointer identity.
    if (elementKlass.arrayKlass) {
      return elementKlass.arrayKlass;
    }
    var className = elementKlass.classInfo.className;
    if (!(elementKlass.classInfo instanceof PrimitiveClassInfo) && className[0] !== "[") {
      className = "L" + className + ";";
    }
    className = "[" + className;
    return getKlass(CLASSES.getClass(className));
  }

  export function toDebugString(value: any): string {
    if (typeof value !== "object") {
      return String(value);
    }
    if (value === undefined) {
      return "undefined";
    }
    if (!value) {
      return "null";
    }
    if (!value.klass) {
      return "no klass";
    }
    if (!value.klass.classInfo) {
      return value.klass + " no classInfo"
    }
    var hashcode = "";
    if (value._hashCode) {
      hashcode = " 0x" + value._hashCode.toString(16).toUpperCase();
    }
    if (value instanceof Klasses.java.lang.String) {
      return "\"" + value.str + "\"";
    }
    return "[" + value.klass.classInfo.className + hashcode + "]";
  }

  export function fromJavaString(value: java.lang.String): string {
    if (!value) {
      return null;
    }
    return value.str;
  }

  export function checkDivideByZero(value: number) {
    if (value === 0) {
      throw $.newArithmeticException("/ by zero");
    }
  }

  export function checkDivideByZeroLong(value: Long) {
    if (value.isZero()) {
      throw $.newArithmeticException("/ by zero");
    }
  }

  /**
   * Do bounds check using only one branch. The math works out because array.length
   * can't be larger than 2^31 - 1. So |index| >>> 0 will be larger than
   * array.length if it is less than zero. We need to make the right side unsigned
   * as well because otherwise the SM optimization that converts this to an
   * unsinged branch doesn't kick in.
   */
  export function checkArrayBounds(array: any [], index: number) {
    if ((index >>> 0) >= (array.length >>> 0)) {
      throw $.newArrayIndexOutOfBoundsException(String(index));
    }
  }

  export function checkArrayStore(array: java.lang.Object, value: any) {
    var arrayKlass = array.klass;
    if (value && !isAssignableTo(value.klass, arrayKlass.elementKlass)) {
      throw $.newArrayStoreException();
    }
  }

  export function checkNull(object: java.lang.Object) {
    if (!object) {
      throw $.newNullPointerException();
    }
  }

  export enum Constants {
    BYTE_MIN = -128,
    BYTE_MAX = 127,
    SHORT_MIN = -32768,
    SHORT_MAX = 32767,
    CHAR_MIN = 0,
    CHAR_MAX = 65535,
    INT_MIN = -2147483648,
    INT_MAX =  2147483647
  }

  export function monitorEnter(object: J2ME.java.lang.Object) {
    $.ctx.monitorEnter(object);
  }

  export function monitorExit(object: J2ME.java.lang.Object) {
    $.ctx.monitorExit(object);
  }

  export function translateException(e) {
    if (e.name === "TypeError") {
      // JavaScript's TypeError is analogous to a NullPointerException.
      return $.newNullPointerException(e.message);
    }
    return e;
  }

  export function classInitCheck(classInfo: ClassInfo, pc: number) {
    if (classInfo.isArrayClass) {
      return;
    }
    $.ctx.pushClassInitFrame(classInfo);
    if (U) {
      $.ctx.current().pc = pc;
      return;
    }
  }
}

var Runtime = J2ME.Runtime;


/**
 * Are we currently unwinding the stack because of a Yield? This technically
 * belonges to a context but we store it in the global object because it is
 * read very often.
 */
var U: J2ME.VMState = J2ME.VMState.Running;

/**
 * OSR Frame.
 */
var O: J2ME.Frame = null;

/**
 * Runtime exports for compiled code.
 */
var $IOK = J2ME.instanceOfKlass;
var $IOI = J2ME.instanceOfInterface;

var $CCK = J2ME.checkCastKlass;
var $CCI = J2ME.checkCastInterface;

var $AK = J2ME.getArrayKlass;
var $NA = J2ME.newArray;
var $S = J2ME.newStringConstant;

var $CDZ = J2ME.checkDivideByZero;
var $CDZL = J2ME.checkDivideByZeroLong;

var $CAB = J2ME.checkArrayBounds;
var $CAS = J2ME.checkArrayStore;

var $ME = J2ME.monitorEnter;
var $MX = J2ME.monitorExit;
var $TE = J2ME.translateException;
