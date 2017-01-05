/*
 node-jvm
 Copyright (c) 2013 Yaroslav Gaponov <yaroslav.gaponov@gmail.com>
*/

var $: J2ME.Runtime; // The currently-executing runtime.

var tempReturn0 = 0;

interface Math {
  fround(value: number): number;
}

interface CompiledMethodCache {
  get(key: string): {
    key: string;
    args: string[];
    body: string;
    referencedClasses: string[];
    onStackReplacementEntryPoints: any;
  };
  put(obj: {
    key: string;
    args: string[];
    body: string;
    referencedClasses: string[];
    onStackReplacementEntryPoints: any;
  }): Promise<any>;
}

interface AOTMetaData {
  /**
   * On stack replacement pc entry points.
   */
  osr: number [];
}

declare var throwHelper;
declare var throwPause;
declare var throwYield;

module J2ME {

  export function returnLong(l: number, h: number) {
    tempReturn0 = h;
    return l;
  }

  export function returnDouble(l: number, h: number) {
    tempReturn0 = h;
    return l;
  }

  export function returnDoubleValue(v: number) {
    aliasedF64[0] = v;
    return returnDouble(aliasedI32[0], aliasedI32[1]);
  }

  declare var Native, config;
  declare var VM;
  declare var CompiledMethodCache;

  export var aotMetaData = <{string: AOTMetaData}>Object.create(null);

  /**
   * Turns on just-in-time compilation of methods.
   */
  export var enableRuntimeCompilation = true;

  /**
   * Turns on onStackReplacement
   */
  export var enableOnStackReplacement = true;

  /**
   * Turns on caching of JIT-compiled methods.
   */
  export var enableCompiledMethodCache = true && typeof CompiledMethodCache !== "undefined";

  /**
   * Traces method execution.
   */
  export var traceWriter = null;

  /**
   * Traces bytecode execution.
   */
  export var traceStackWriter = null;

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

  /**
   * Traces thread execution.
   */
  export var threadWriter = null;

  /**
   * Traces generated code.
   */
  export var codeWriter = null;

  export const enum MethodState {
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
  export var threadTimeline;
  export var methodTimelines = [];
  export var gcCounter = release ? null : new Metrics.Counter(true);
  export var nativeCounter = release ? null : new Metrics.Counter(true);
  export var runtimeCounter = release ? null : new Metrics.Counter(true);
  export var baselineMethodCounter = release ? null : new Metrics.Counter(true);
  export var asyncCounter = release ? null : new Metrics.Counter(true);

  export var unwindCount = 0;

  if (typeof Shumway !== "undefined") {
    timeline = new Shumway.Tools.Profiler.TimelineBuffer("Runtime");
    threadTimeline = new Shumway.Tools.Profiler.TimelineBuffer("Threads");
  }

  export function enterTimeline(name: string, data?: any) {
    timeline && timeline.enter(name, data);
  }

  export function leaveTimeline(name?: string, data?: any) {
    timeline && timeline.leave(name, data);
  }

  function Int64Array(buffer: ArrayBuffer, offset: number, length: number) {
    this.length = length;
    this.byteOffset = offset;
    this.buffer = buffer;
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
      case Int64Array:
        return false;
      default:
        return true;
    }
  }

  export var stdoutWriter = new IndentingWriter();
  export var stderrWriter = new IndentingWriter(false, IndentingWriter.stderr);

  export const enum ExecutionPhase {
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

  // Initial capacity of the interned strings is the capacity of a large midlet after startup.
  export var internedStrings: TypedArrayHashtable = new TypedArrayHashtable(767);

  declare var util;

  import assert = J2ME.Debug.assert;

  export const enum RuntimeStatus {
    New       = 1,
    Started   = 2,
    Stopping  = 3, // Unused
    Stopped   = 4
  }

  export const enum MethodType {
    Interpreted,
    Native,
    Compiled
  }

  export function getMethodTypeName(methodType: MethodType) {
    return (<any>J2ME).MethodType[methodType];
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
        assert(false, "Collision detected!!!")
      }
      hashMap[hash] = s;
    }

    return hash;
  }

  export function hashUTF8String(s: Uint8Array): number {
    var hash = HashUtilities.hashBytesTo32BitsMurmur(s, 0, s.length);
    if (!release) { // Check to see that no collisions have ever happened.
      if (hashMap[hash] && hashMap[hash] !== s) {
        assert(false, "Collision detected in hashUTF8String!!!")
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
    initialized: Int8Array;
    staticObjectAddresses: Int32Array;
    classObjectAddresses: Int32Array;

    I: Int8Array; // Compiler alias for initialized
    SA: Int32Array; // Compiler alias for staticObjectAddresses
    CO: Int32Array; // Compiler alias for classObjectAddresses

    ctx: Context;
    allCtxs: Set<Context>;

    isolateId: number;
    isolateAddress: number;
    priority: number = ISOLATE_NORM_PRIORITY;
    // XXX Rename mainThread to mainThreadAddress so it's clearly an address.
    mainThread: number;

    private static _nextRuntimeId: number = 0;
    private _runtimeId: number;
    private _nextHashCode: number;

    constructor(jvm: JVM) {
      this.jvm = jvm;
      this.status = RuntimeStatus.New;
      this.waiting = [];
      this.threadCount = 0;
      this.I = this.initialized = new Int8Array(Constants.MAX_CLASS_ID);
      this.SA = this.staticObjectAddresses = new Int32Array(Constants.INITIAL_MAX_CLASS_ID + 1);
      this.CO = this.classObjectAddresses = new Int32Array(Constants.INITIAL_MAX_CLASS_ID + 1);
      this.ctx = null;
      this.allCtxs = new Set();
      this._runtimeId = RuntimeTemplate._nextRuntimeId++;
      this._nextHashCode = (this._runtimeId << 24) | 1; // Increase by one so the first hashcode isn't zero.
    }

    preInitializeClasses(ctx: Context) {
      var prevCtx = $ ? $.ctx : null;
      var preInit = CLASSES.preInitializedClasses;
      ctx.setAsCurrentContext();
      for (var i = 0; i < preInit.length; i++) {
        preemptionLockLevel++;
        var classInfo = preInit[i];
        classInitCheck(classInfo);
        release || Debug.assert(!U, "Unexpected unwind during preInitializeClasses.");
        preemptionLockLevel-- ;
      }
      ctx.clearCurrentContext();
      if (prevCtx) {
        prevCtx.setAsCurrentContext();
      }
    }

    /**
     * After class initialization is finished the init9 method will invoke this so
     * any further initialize calls can be avoided. This isn't set on the first call
     * to a class initializer because there can be multiple calls into initialize from
     * different threads that need trigger the Class.initialize() code so they block.
     */
    setClassInitialized(classId: number) {
      this.initialized[classId] = 1;
    }

    getClassObjectAddress(classInfo: ClassInfo): number {
      var id = classInfo.id;
      if (!this.classObjectAddresses[classInfo.id]) {
        var addr = allocUncollectableObject(CLASSES.java_lang_Class);
        var handle = <java.lang.Class>getHandle(addr);
        handle.vmClass = id;
        // Ensure that maps are large enough.
        this.SA = this.staticObjectAddresses = ArrayUtilities.ensureInt32ArrayLength(this.staticObjectAddresses, id + 1);
        this.CO = this.classObjectAddresses = ArrayUtilities.ensureInt32ArrayLength(this.classObjectAddresses, id + 1);
        this.classObjectAddresses[id] = addr;
        this.staticObjectAddresses[id] = gcMallocUncollectable(J2ME.Constants.OBJ_HDR_SIZE + classInfo.sizeOfStaticFields);
        linkWriter && linkWriter.writeLn("Initializing Class Object For: " + classInfo.getClassNameSlow());
        if (classInfo === CLASSES.java_lang_Object ||
            classInfo === CLASSES.java_lang_Class ||
            classInfo === CLASSES.java_lang_String ||
            classInfo === CLASSES.java_lang_Thread) {
          handle.status = 4;
          this.setClassInitialized(id);
        }
      }
      return this.classObjectAddresses[id];
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
      this.allCtxs.add(ctx);
    }

    removeContext(ctx) {
      if (!--this.threadCount) {
        RuntimeTemplate.all.delete(this);
        this.updateStatus(RuntimeStatus.Stopped);
      }
      this.allCtxs.delete(ctx);
    }

    newStringConstant(utf16ArrayAddr: number): number {
      var utf16Array = getArrayFromAddr(utf16ArrayAddr);
      var javaStringAddr = internedStrings.get(utf16Array);
      if (javaStringAddr !== null) {
        return javaStringAddr;
      }

      setUncollectable(utf16ArrayAddr);

      // It's ok to create and intern an object here, because we only return it
      // to ConstantPool.resolve, which itself is only called by a few callers,
      // which should be able to convert it into an address if needed.  But we
      // should confirm that all callers of ConstantPool.resolve really do that.
      javaStringAddr = allocUncollectableObject(CLASSES.java_lang_String);
      var javaString = <java.lang.String>getHandle(javaStringAddr);
      javaString.value = utf16ArrayAddr;
      javaString.offset = 0;
      javaString.count = utf16Array.length;
      internedStrings.put(utf16Array, javaStringAddr);

      unsetUncollectable(utf16ArrayAddr);

      return javaStringAddr;
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

    newInstantiationException(str?: string): java.lang.InstantiationException {
      return <java.lang.InstantiationException>$.ctx.createException(
        "java/lang/InstantiationException", str);
    }

    newException(str?: string): java.lang.Exception {
      return <java.lang.Exception>$.ctx.createException(
        "java/lang/Exception", str);
    }

    static classInfoComplete(classInfo: ClassInfo) {
      if (phase !== ExecutionPhase.Runtime) {
        return;
      }

      if (!classInfo.isInterface) {
        // Pre-allocate linkedVTableMap.
        ensureDenseObjectMapLength(linkedVTableMap, classInfo.id + 1);
        ensureDenseObjectMapLength(flatLinkedVTableMap, (classInfo.id + 1) << Constants.LOG_MAX_FLAT_VTABLE_SIZE);
        linkedVTableMap[classInfo.id] = ArrayUtilities.makeDenseArray(classInfo.vTable.length, null);
      }
    }
  }

  export const enum VMState {
    Running = 0,
    Yielding = 1,
    Pausing = 2,
    Stopping = 3
  }

  export function getVMStateName(vmState: VMState): string {
    return (<any>J2ME).VMState[vmState];
  }

  export const enum Constants {
    BYTE_MIN = -128,
    BYTE_MAX = 127,
    SHORT_MIN = -32768,
    SHORT_MAX = 32767,
    CHAR_MIN = 0,
    CHAR_MAX = 65535,
    INT_MIN = -2147483648,
    INT_MAX =  2147483647,

    LONG_MAX_LOW = 0xFFFFFFFF,
    LONG_MAX_HIGH = 0x7FFFFFFF,

    LONG_MIN_LOW = 0,
    LONG_MIN_HIGH = 0x80000000,

    MAX_STACK_SIZE = 4 * 1024,

    TWO_PWR_32_DBL = 4294967296,
    TWO_PWR_63_DBL = 9223372036854776000,

    // The size in bytes of the header in the memory allocated to the object.
    OBJ_HDR_SIZE = 8,

    // The offset in bytes from the beginning of the allocated memory
    // to the location of the class id.
    OBJ_CLASS_ID_OFFSET = 0,
    // The offset in bytes from the beginning of the allocated memory
    // to the location of the hash code.
    HASH_CODE_OFFSET = 4,

    ARRAY_HDR_SIZE = 8,

    ARRAY_LENGTH_OFFSET = 4,
    NULL = 0,

    MAX_METHOD_ID = 16383,
    INITIAL_MAX_METHOD_ID = 511,

    MAX_CLASS_ID = 4095,
    INITIAL_MAX_CLASS_ID = 511,

    LOG_MAX_FLAT_VTABLE_SIZE = 6 // 64
  }

  export class Runtime extends RuntimeTemplate {
    private static _nextId: number = 0;
    id: number;
    /**
     * Bailout callback whenever a JIT frame is unwound.
     */
    B(methodId: number, pc: number, lockObjectAddress: number) {
      var methodInfo = methodIdToMethodInfoMap[methodId];
      var localCount = methodInfo.codeAttribute.max_locals;
      var argumentCount = 3;
      // Figure out the |stackCount| from the number of arguments.
      var stackCount = arguments.length - argumentCount - localCount;
      // TODO: use specialized $.B functions based on the local and stack size so we don't have to use the arguments variable.
      var bailoutFrameAddress = createBailoutFrame(methodId, pc, localCount, stackCount, lockObjectAddress);
      for (var j = 0; j < localCount; j++) {
        i32[(bailoutFrameAddress + BailoutFrameLayout.HeaderSize >> 2) + j] = arguments[argumentCount + j];
      }
      for (var j = 0; j < stackCount; j++) {
        i32[(bailoutFrameAddress + BailoutFrameLayout.HeaderSize >> 2) + j + localCount] = arguments[argumentCount + localCount + j];
      }
      this.ctx.bailout(bailoutFrameAddress);
    }

    yield(reason: string) {
      unwindCount ++;
      threadWriter && threadWriter.writeLn("yielding " + reason);
      runtimeCounter && runtimeCounter.count("yielding " + reason);
      U = VMState.Yielding;
      profile && $.ctx.pauseMethodTimeline();
      this.ctx.nativeThread.beginUnwind();
    }

    nativeBailout(returnKind: Kind, opCode?: Bytecode.Bytecodes) {
      var pc = returnKind === Kind.Void ? 0 : 1;
      var methodInfo = CLASSES.getUnwindMethodInfo(returnKind, opCode);
      var bailoutFrameAddress = createBailoutFrame(methodInfo.id, pc, 0, 0, Constants.NULL);
      this.ctx.bailout(bailoutFrameAddress);
    }

    pause(reason: string) {
      unwindCount ++;
      threadWriter && threadWriter.writeLn("pausing " + reason);
      runtimeCounter && runtimeCounter.count("pausing " + reason);
      U = VMState.Pausing;
      profile && $.ctx.pauseMethodTimeline();
      this.ctx.nativeThread.beginUnwind();
    }

    stop() {
      U = VMState.Stopping;
    }

    constructor(jvm: JVM) {
      super(jvm);
      this.id = Runtime._nextId ++;
    }
  }

  export var classIdToClassInfoMap = [];
  export var methodIdToMethodInfoMap = [];
  export var linkedMethods = [];

  function ensureDenseObjectMapLength(array: Array<Object>, length: number) {
    while (array.length < length) {
      array.push(null);
    }
    release || Debug.assertNonDictionaryModeObject(array);
  }

  export function registerClassId(classId: number, classInfo: ClassInfo) {
    release || assert(phase === ExecutionPhase.Compiler || classId <= Constants.MAX_CLASS_ID, "Maximum class id was exceeded, " + classId);
    ensureDenseObjectMapLength(classIdToClassInfoMap, classId + 1);
    classIdToClassInfoMap[classId] = classInfo;
  }

  export function registerMethodId(methodId: number, methodInfo: MethodInfo) {
    release || assert(phase === ExecutionPhase.Compiler || methodId <= Constants.MAX_METHOD_ID, "Maximum method id was exceeded, " + methodId);
    ensureDenseObjectMapLength(methodIdToMethodInfoMap, methodId + 1);
    ensureDenseObjectMapLength(linkedMethods, methodId + 1);
    methodIdToMethodInfoMap[methodId] = methodInfo;
  }

  /**
   * Maps classIds to vTables containing JS functions.
   */
  export var linkedVTableMap = [];

  /**
   * Flat map of classId and vTableIndex to JS functions. This allows the compiler to
   * emit a single memory load to lookup a vTable entry
   *  flatLinkedVTableMap[classId << LOG_MAX_FLAT_VTABLE_SIZE + vTableIndex]
   * instead of the slower more general
   *  linkedVTableMap[classId][vTableIndex]
   */
  export var flatLinkedVTableMap = [];

  export function getClassInfo(addr: number) {
    release || assert(addr !== Constants.NULL, "addr !== Constants.NULL");
    release || assert(i32[addr + Constants.OBJ_CLASS_ID_OFFSET >> 2] != 0,
                      "i32[addr + Constants.OBJ_CLASS_ID_OFFSET >> 2] != 0");
    return classIdToClassInfoMap[i32[addr + Constants.OBJ_CLASS_ID_OFFSET >> 2]];
  }

  /**
   * A map from addresses to monitors, which are JS objects that we use to track
   * the lock state of Java objects.
   *
   * In most cases, we create the JS objects via Object.create(null), but we use
   * java.lang.Class objects for classes, since those continue to be represented
   * by JS objects in the runtime.  We also overload this map to retrieve those
   * class objects for other purposes.
   *
   * XXX Consider storing lock state in the ASM heap.
   */
  export var monitorMap = Object.create(null);

  // XXX Figure out correct return type(s).
  export function getMonitor(ref: number): Lock {
    release || assert(typeof ref === "number", "monitor reference is a number");

    var hash = i32[ref + Constants.HASH_CODE_OFFSET >> 2];
    if (hash === Constants.NULL) {
      hash = i32[ref + Constants.HASH_CODE_OFFSET >> 2] = $.nextHashCode()
    }

    return monitorMap[hash] || (monitorMap[hash] = new Lock(Constants.NULL, 0));
  }

  /**
   * Representation of a Java class with JS object.
   */
  export interface Handle extends Function {
    new (address?: number): java.lang.Object;

    _address: number;
    classInfo: ClassInfo;
  }

  export class Lock {
    ready: Context [];
    waiting: Context [];

    constructor(public threadAddress: number, public level: number) {
      this.ready = [];
      this.waiting = [];
    }
  }

  function findNativeMethodBinding(methodInfo: MethodInfo) {
    var classBindings = BindingsMap.get(methodInfo.classInfo.utf8Name);
    if (classBindings && classBindings.native) {
      var method = classBindings.native[methodInfo.name + "." + methodInfo.signature];
      if (method) {
        return method;
      }
    }
    return null;
  }

  function reportError(method, key) {
    return function() {
      try {
        return method.apply(this, arguments);
      } catch (e) {
        // Filter JAVA exception and only report the native js exception, which
        // cannnot be handled properly by the JAVA code.
        if (!e.classInfo) {
          stderrWriter.errorLn("Native " + key + " throws: " + e);
        }
        throw e;
      }
    };
  }

  function findNativeMethodImplementation(methodInfo: MethodInfo) {
    // Look in bindings first.
    var binding = findNativeMethodBinding(methodInfo);
    if (binding) {
      return release ? binding : reportError(binding, methodInfo.implKey);
    }
    if (methodInfo.isNative) {
      var implKey = methodInfo.implKey;
      if (implKey in Native) {
        return release ? Native[implKey] : reportError(Native[implKey], implKey);
      } else {
        // Some Native MethodInfos are constructed but never called;
        // that's fine, unless we actually try to call them.
        return function missingImplementation() {
          stderrWriter.errorLn("implKey " + implKey + " is native but does not have an implementation.");
        }
      }
    }
    return null;
  }

  var frameView = new FrameView();

  function findCompiledMethod(methodInfo: MethodInfo): Function {
    return;
    // Use aotMetaData to find AOT methods instead of jsGlobal because runtime compiled methods may
    // be on the jsGlobal.
    //var mangledClassAndMethodName = methodInfo.mangledClassAndMethodName;
    //if (aotMetaData[mangledClassAndMethodName]) {
    //  aotMethodCount++;
    //  methodInfo.onStackReplacementEntryPoints = aotMetaData[methodInfo.mangledClassAndMethodName].osr;
    //  release || assert(jsGlobal[mangledClassAndMethodName], "function must be present when aotMetaData exists");
    //  return jsGlobal[mangledClassAndMethodName];
    //}
    //if (enableCompiledMethodCache) {
    //  var cachedMethod;
    //  if ((cachedMethod = CompiledMethodCache.get(methodInfo.implKey))) {
    //    cachedMethodCount ++;
    //    linkMethod(methodInfo, cachedMethod.source, cachedMethod.referencedClasses, cachedMethod.onStackReplacementEntryPoints);
    //  }
    //}
    //
    //return jsGlobal[mangledClassAndMethodName];
  }

  /**
   * Creates convenience getters / setters on Java objects.
   */
  function linkHandleFields(handleConstructor, classInfo: ClassInfo) {
    // Get all the parent classes so their fields are linked first.
    var classes = [classInfo];
    var superClass = classInfo.superClass;
    while (superClass) {
      classes.unshift(superClass);
      superClass = superClass.superClass;
    }
    for (var i = 0; i < classes.length; i++) {
      var classInfo = classes[i];
      var classBindings = BindingsMap.get(classInfo.utf8Name);
      if (classBindings && classBindings.fields) {
        release || assert(!classBindings.fields.staticSymbols, "Static fields are not supported yet");

        var instanceSymbols = classBindings.fields.instanceSymbols;

        for (var fieldName in instanceSymbols) {
          var fieldSignature = instanceSymbols[fieldName];

          var field = classInfo.getFieldByName(toUTF8(fieldName), toUTF8(fieldSignature), false);

          release || assert(!field.isStatic, "Static field was defined as instance in BindingsMap");
          var object = field.isStatic ? handleConstructor : handleConstructor.prototype;
          release || assert(!object.hasOwnProperty(fieldName), "Should not overwrite existing properties.");
          var getter;
          var setter;
          if (true || release) {
            switch (field.kind) {
              case Kind.Reference:
                setter = new Function("value", "i32[this._address + " + field.byteOffset + " >> 2] = value;");
                getter = new Function("return i32[this._address + " + field.byteOffset + " >> 2];");
                break;
              case Kind.Boolean:
                setter = new Function("value", "i32[this._address + " + field.byteOffset + " >> 2] = value ? 1 : 0;");
                getter = new Function("return i32[this._address + " + field.byteOffset + " >> 2];");
                break;
              case Kind.Byte:
              case Kind.Short:
              case Kind.Int:
                setter = new Function("value", "i32[this._address + " + field.byteOffset + " >> 2] = value;");
                getter = new Function("return i32[this._address + " + field.byteOffset + " >> 2];");
                break;
              case Kind.Float:
                setter = new Function("value", "f32[this._address + " + field.byteOffset + " >> 2] = value;");
                getter = new Function("return f32[this._address + " + field.byteOffset + " >> 2];");
                break;
              case Kind.Long:
                setter = new Function("value",
                  "i32[this._address + " + field.byteOffset + " >> 2] = J2ME.returnLongValue(value);" +
                  "i32[this._address + " + field.byteOffset + " + 4 >> 2] = tempReturn0;");
                getter = new Function("return J2ME.longToNumber(i32[this._address + " + field.byteOffset + " >> 2]," +
                  "                         i32[this._address + " + field.byteOffset + " + 4 >> 2]);");
                break;
              case Kind.Double:
                setter = new Function("value",
                  "aliasedF64[0] = value;" +
                  "i32[this._address + " + field.byteOffset + " >> 2] = aliasedI32[0];" +
                  "i32[this._address + " + field.byteOffset + " + 4 >> 2] = aliasedI32[1];");
                getter = new Function("aliasedI32[0] = i32[this._address + " + field.byteOffset + " >> 2];" +
                  "aliasedI32[1] = i32[this._address + " + field.byteOffset + " + 4 >> 2];" +
                  "return aliasedF64[0];");
                break;
              default:
                Debug.assert(false, getKindName(field.kind));
                break;
            }
          } else {
            setter = FunctionUtilities.makeDebugForwardingSetter(field.mangledName, getKindCheck(field.kind));
          }
          Object.defineProperty(object, fieldName, {
            get: getter,
            set: setter,
            configurable: true,
            enumerable: false
          });
        }
      }
    }
  }

  function profilingWrapper(fn: Function, methodInfo: MethodInfo, methodType: MethodType) {
    if (methodType === MethodType.Interpreted) {
      // Profiling for interpreted functions is handled by the context.
      return fn;
    }
    var code;
    if (methodInfo.isNative) {
      if (methodInfo.returnKind === Kind.Void) {
        code = new Uint8Array([Bytecode.Bytecodes.RETURN]);
      } else if (isTwoSlot(methodInfo.returnKind)) {
        code = new Uint8Array([Bytecode.Bytecodes.LRETURN]);
      } else {
        code = new Uint8Array([Bytecode.Bytecodes.IRETURN]);
      }
    }


    return function (a, b, c, d) {
      var key = methodInfo.implKey;
      try {
        var ctx = $.ctx;
        ctx.enterMethodTimeline(key, methodType);
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
        if (U) {
          release || assert(ctx.paused, "context is paused");

          if (methodInfo.isNative) {
            // A fake frame that just returns is pushed so when the ctx resumes from the unwind
            // the frame will be popped triggering a leaveMethodTimeline.
            //REDUX
            //var fauxFrame = Frame.create(null, []);
            //fauxFrame.methodInfo = methodInfo;
            //fauxFrame.code = code;
            //ctx.bailoutFrames.unshift(fauxFrame);
          }
        } else {
          ctx.leaveMethodTimeline(key, methodType);
        }
      } catch (e) {
        ctx.leaveMethodTimeline(key, methodType);
        throw e;
      }
      return r;
    };
  }

  function tracingWrapper(fn: Function, methodInfo: MethodInfo, methodType: MethodType) {
    var wrapper = function() {
      // jsGlobal.getBacktrace && traceWriter.writeLn(jsGlobal.getBacktrace());
      var args = Array.prototype.slice.apply(arguments);
      traceWriter.enter("> " + getMethodTypeName(methodType)[0] + " " + methodInfo.implKey);
      var s = performance.now();
      try {
        var value = fn.apply(this, args);
      } catch (e) {
        traceWriter.leave("< " + getMethodTypeName(methodType)[0] + " Throwing");
        throw e;
      }
      traceWriter.leave("< " + getMethodTypeName(methodType)[0] + " " + methodInfo.implKey);
      return value;
    };
    (<any>wrapper).methodInfo = methodInfo;
    return wrapper;
  }

  export function getLinkedMethod(methodInfo: MethodInfo) {
    if (methodInfo.fn) {
      return methodInfo.fn;
    }
    linkMethod(methodInfo);
    release || assert (methodInfo.fn, "bad fn in getLinkedMethod");
    return methodInfo.fn;
  }

  export function getLinkedMethodById(methodId: number) {
    return getLinkedMethod(methodIdToMethodInfoMap[methodId]);
  }

  export function getLinkedVirtualMethodById(classId: number, vTableIndex: number) {
    var methodInfo = classIdToClassInfoMap[classId].vTable[vTableIndex];
    var fn = getLinkedMethod(methodInfo);
    // Only cache compiled methods in the |linkedVTableMap| and |flatLinkedVTableMap|.
    if (methodInfo.state === MethodState.Compiled) {
      var vTable = linkedVTableMap[classId];
      release || Debug.assertNonDictionaryModeObject(vTable);
      vTable[vTableIndex] = fn;
      // Only cache methods in the |flatLinkedVTableMap| if there is room.
      if (vTableIndex < (1 << Constants.LOG_MAX_FLAT_VTABLE_SIZE)) {
        release || Debug.assertNonDictionaryModeObject(flatLinkedVTableMap);
        flatLinkedVTableMap[(classId << Constants.LOG_MAX_FLAT_VTABLE_SIZE) + vTableIndex] = fn;
      }
    }
    return fn;
  }

  function linkMethod(methodInfo: MethodInfo) {
    runtimeCounter && runtimeCounter.count("linkMethod");
    var fn;
    var methodType;
    var nativeMethod = findNativeMethodImplementation(methodInfo);
    if (nativeMethod) {
      linkWriter && linkWriter.writeLn("Method: " + methodInfo.name + methodInfo.signature + " -> Native");
      fn = nativeMethod;
      methodType = MethodType.Native;
      methodInfo.state = MethodState.Compiled;
    } else {
      fn = findCompiledMethod(methodInfo);
      if (fn) {
        linkWriter && linkWriter.greenLn("Method: " + methodInfo.name + methodInfo.signature + " -> Compiled");
        methodType = MethodType.Compiled;
        methodInfo.state = MethodState.Compiled;
      } else {
        linkWriter && linkWriter.warnLn("Method: " + methodInfo.name + methodInfo.signature + " -> Interpreter");
        methodType = MethodType.Interpreted;
        fn = prepareInterpretedMethod(methodInfo);
      }
    }
    linkMethodFunction(methodInfo, fn, methodType);
  }

  /**
   * Number of methods that have been compiled thus far.
   */
  export var compiledMethodCount = 0;

  /**
   * Maximum number of methods to compile.
   */
  export var maxCompiledMethodCount = -1;

  /**
   * Number of methods that have not been compiled thus far.
   */
  export var notCompiledMethodCount = 0;

  /**
   * Number of methods that have been loaded from the code cache thus far.
   */
  export var cachedMethodCount = 0;

  /**
   * Number of methods that have been loaded from ahead of time compiled code thus far.
   */
  export var aotMethodCount = 0;

  /**
   * Number of ms that have been spent compiled code thus far.
   */
  var totalJITTime = 0;

  /**
   * Compiles method and links it up at runtime.
   */
  export function compileAndLinkMethod(methodInfo: MethodInfo) {
    if (!enableRuntimeCompilation) {
      return;
    }

    // Don't do anything if we're past the compiled state.
    if (methodInfo.state >= MethodState.Compiled) {
      return;
    }

    // Don't compile if we've compiled too many methods.
    if (maxCompiledMethodCount >= 0 && compiledMethodCount >= maxCompiledMethodCount) {
      return;
    }
    // Don't compile methods that are too large.
    if (methodInfo.codeAttribute.code.length > 4000 && !config.forceRuntimeCompilation) {
      jitWriter && jitWriter.writeLn("Not compiling: " + methodInfo.implKey + " because it's too large. " + methodInfo.codeAttribute.code.length);
      methodInfo.state = MethodState.NotCompiled;
      notCompiledMethodCount ++;
      return;
    }

    if (enableCompiledMethodCache) {
      var cachedMethod;
      if (cachedMethod = CompiledMethodCache.get(methodInfo.implKey)) {
        cachedMethodCount ++;
        jitWriter && jitWriter.writeLn("Retrieved " + methodInfo.implKey + " from compiled method cache");

        var referencedClasses = [];
        // Ensure referenced classes are loaded.
        // We only need to do this for cached methods, since referenced classes
        // get loaded automatically during JIT compilation.
        for (var i = 0; i < cachedMethod.referencedClasses.length; i++) {
          referencedClasses.push(CLASSES.getClass(cachedMethod.referencedClasses[i]));
        }
        linkMethodSource(methodInfo, cachedMethod.args, cachedMethod.body, referencedClasses, cachedMethod.onStackReplacementEntryPoints);
        return;
      }
    }

    var mangledClassAndMethodName = methodInfo.mangledClassAndMethodName;

    jitWriter && jitWriter.enter("Compiling: " + compiledMethodCount + " " + methodInfo.implKey + ", interpreterCallCount: " + methodInfo.stats.interpreterCallCount + " backwardsBranchCount: " + methodInfo.stats.backwardsBranchCount + " currentBytecodeCount: " + methodInfo.stats.bytecodeCount);
    var s = performance.now();

    var compiledMethod;
    enterTimeline("Compiling");
    try {
      compiledMethod = baselineCompileMethod(methodInfo, enableCompiledMethodCache ? CompilationTarget.Static : CompilationTarget.Runtime);
      compiledMethodCount ++;
    } catch (e) {
      methodInfo.state = MethodState.CannotCompile;
      jitWriter && jitWriter.writeLn("Cannot compile: " + methodInfo.implKey + " because of " + e);
      leaveTimeline("Compiling");
      return;
    }
    leaveTimeline("Compiling");
    if (codeWriter) {
      codeWriter.writeLn("// Method: " + methodInfo.implKey);
      codeWriter.writeLn("// Arguments: " + compiledMethod.args.join(", "));
      codeWriter.writeLn("// Referenced Classes: ");
      for (var i = 0; i < compiledMethod.referencedClasses.length; i++) {
        codeWriter.writeLn("// " + i + ": " + compiledMethod.referencedClasses[i].getClassNameSlow());
      }
      codeWriter.writeLns(compiledMethod.body)
    }

    if (enableCompiledMethodCache) {
      CompiledMethodCache.put({
        key: methodInfo.implKey,
        args: compiledMethod.args,
        body: compiledMethod.body,
        referencedClasses: compiledMethod.referencedClasses.map(function(v) { return v.getClassNameSlow() }),
        onStackReplacementEntryPoints: compiledMethod.onStackReplacementEntryPoints
      });
    }

    linkMethodSource(methodInfo, compiledMethod.args, compiledMethod.body, compiledMethod.referencedClasses, compiledMethod.onStackReplacementEntryPoints);
    var methodJITTime = (performance.now() - s);
    totalJITTime += methodJITTime;
    if (jitWriter) {
      jitWriter.leave(
        "Compilation Done: " + methodJITTime.toFixed(2) + " ms, " +
        "codeSize: " + methodInfo.codeAttribute.code.length + ", " +
        "sourceSize: " + compiledMethod.body.length);
      jitWriter.writeLn("Total: " + totalJITTime.toFixed(2) + " ms");
    }
  }

  function wrapMethod(fn, methodInfo: MethodInfo, methodType: MethodType) {
    if (profile) {
      fn = profilingWrapper(fn, methodInfo, methodType);
    }

    if (traceWriter) {
      fn = tracingWrapper(fn, methodInfo, methodType);
    }
    return fn;
  }

  function linkMethodFunction(methodInfo: MethodInfo, fn: Function, methodType: MethodType) {
    if (profile || traceWriter) {
      fn = wrapMethod(fn, methodInfo, methodType);
    }

    methodInfo.fn = fn;
    linkedMethods[methodInfo.id] = fn;
  }

  // Make sure class and method symbol references can be parsed as identifiers. This allows closure and other tools
  // to process this code as JS files.
  export var classInfoSymbolPrefix =  "$C"; // "$C123
  export var methodInfoSymbolPrefix = "$M"; // "$M123_456
  var classInfoSymbolPrefixPattern =  /\$C(\d+)/g;
  var methodInfoSymbolPrefixPattern = /\$M(\d+)_(\d+)/g;

  /**
   * Enable this if you want your profiles to have nice function names. Naming eval'ed functions
   * using: |new Function("return function displayName {}");| can cause performance problems and
   * we keep it disabled by default.
   */
  var nameJITFunctions = false;

  /**
   * Links up compiled method at runtime.
   */
  export function linkMethodSource(methodInfo: MethodInfo, args: string[], body: string, referencedClasses: ClassInfo [], onStackReplacementEntryPoints: any) {
    jitWriter && jitWriter.writeLn("Link method: " + methodInfo.implKey);
    // TODO: Don't use RegExp ever ever.
    // Patch class and method symbols in relocatable code.
    body = body.replace(classInfoSymbolPrefixPattern, <any>function (match, symbol) {
      // jitWriter && jitWriter.writeLn("Linking Class Symbol: " + symbol + " to " + referencedClasses[symbol]);
      return referencedClasses[symbol].id;
    }).replace(methodInfoSymbolPrefixPattern, <any>function (match, symbol, index) {
      // jitWriter && jitWriter.writeLn("Linking Method Symbol: " + symbol + ":" + index + " to " + referencedClasses[symbol].getMethodByIndex(index));
      return referencedClasses[symbol].getMethodByIndex(index).id;
    });
    enterTimeline("Eval Compiled Code");
    // This overwrites the method on the global object.

    var fn = null;
    if (!release || nameJITFunctions) {
      fn = new Function("return function fn_" + methodInfo.implKey.replace(/\W+/g, "_") + "(" + args.join(",") + "){ " + body + "}")();
    } else {
      fn = new Function(args.join(','), body);
    }

    leaveTimeline("Eval Compiled Code");

    methodInfo.state = MethodState.Compiled;
    methodInfo.onStackReplacementEntryPoints = onStackReplacementEntryPoints;

    linkMethodFunction(methodInfo, fn, MethodType.Compiled);
  }

  export function isAssignableTo(from: ClassInfo, to: ClassInfo): boolean {
    return from.isAssignableTo(to);
  }

  export function instanceOfKlass(objectAddr: number, classId: number): boolean {
    release || assert(typeof classId === "number", "Class id must be a number.");
    return objectAddr !== Constants.NULL && isAssignableTo(classIdToClassInfoMap[i32[objectAddr + Constants.OBJ_CLASS_ID_OFFSET >> 2]], classIdToClassInfoMap[classId]);
  }

  export function instanceOfInterface(objectAddr: number, classId: number): boolean {
    release || assert(typeof classId === "number", "Class id must be a number.");
    release || assert(classIdToClassInfoMap[classId].isInterface, "instanceOfInterface called on non interface");
    return objectAddr !== Constants.NULL && isAssignableTo(classIdToClassInfoMap[i32[objectAddr + Constants.OBJ_CLASS_ID_OFFSET >> 2]], classIdToClassInfoMap[classId]);
  }

  export function checkCastKlass(objectAddr: number, classId: number) {
    release || assert(typeof classId === "number", "Class id must be a number.");
    if (objectAddr !== Constants.NULL && !isAssignableTo(classIdToClassInfoMap[i32[objectAddr + Constants.OBJ_CLASS_ID_OFFSET >> 2]], classIdToClassInfoMap[classId])) {
       throw $.newClassCastException();
     }
   }

  export function checkCastInterface(objectAddr: number, classId: number) {
    if (objectAddr !== Constants.NULL && !isAssignableTo(classIdToClassInfoMap[i32[objectAddr + Constants.OBJ_CLASS_ID_OFFSET >> 2]], classIdToClassInfoMap[classId])) {
      throw $.newClassCastException();
    }
  }

  var handleConstructors = Object.create(null);

  export function allocUncollectableObject(classInfo: ClassInfo): number {
    var address = gcMallocUncollectable(Constants.OBJ_HDR_SIZE + classInfo.sizeOfFields);
    i32[address >> 2] = classInfo.id | 0;
    return address;
  }

  export function allocObject(classInfo: ClassInfo): number {
    var address = gcMalloc(Constants.OBJ_HDR_SIZE + classInfo.sizeOfFields);
    i32[address >> 2] = classInfo.id | 0;
    return address;
  }

  export function getFreeMemory(): number {
    return asmJsTotalMemory - ASM._getUsedHeapSize();
  }

  export function onFinalize(addr: number): void {
    NativeMap.delete(addr);
  }

  export const enum BailoutFrameLayout {
    MethodIdOffset = 0,
    PCOffset = 4,
    LocalCountOffset = 8,
    StackCountOffset = 12,
    LockOffset = 16,
    HeaderSize = 20
  }

  export function createBailoutFrame(methodId: number, pc: number, localCount: number, stackCount: number, lockObjectAddress: number): number {
    var address = gcMallocUncollectable(BailoutFrameLayout.HeaderSize + ((localCount + stackCount) << 2));
    release || assert(typeof methodId === "number" && methodIdToMethodInfoMap[methodId], "Must be valid method info.");
    i32[address + BailoutFrameLayout.MethodIdOffset >> 2] = methodId;
    i32[address + BailoutFrameLayout.PCOffset >> 2] = pc;
    i32[address + BailoutFrameLayout.LocalCountOffset >> 2] = localCount;
    i32[address + BailoutFrameLayout.StackCountOffset >> 2] = stackCount;
    i32[address + BailoutFrameLayout.LockOffset >> 2] = lockObjectAddress;
    return address;
  }

  /**
   * A map from Java object addresses to native objects.
   *
   * Currently this only supports mapping an address to a single native.
   * Will we ever want to map multiple natives to an address?  If so, we'll need
   * to do something more sophisticated here.
   */
  export var NativeMap = new Map<number,Object>();

  export function setNative(addr: number, obj: Object): void {
    NativeMap.set(addr, obj);
    ASM._registerFinalizer(addr);
  }

  /**
   * Get a handle for an object in the ASM heap.
   *
   * Currently, we implement this using JS constructors (i.e. Klass instances)
   * with a prototype chain that reflects the Java class hierarchy and getters/
   * setters for fields.
   */
  export function getHandle(address: number): java.lang.Object {
    if (address === Constants.NULL) {
      return null;
    }

    release || assert(typeof address === "number", "address is number");

    var classId = i32[address + Constants.OBJ_CLASS_ID_OFFSET >> 2];

    var classInfo = classIdToClassInfoMap[classId];
    release || assert(classInfo, "object has class info");
    release || assert(!classInfo.elementClass, "object isn't an array");

    if (!handleConstructors[classId]) {
      var constructor = function(address) {
        this._address = address;
      };
      constructor.prototype.classInfo = classInfo;
      // Link the field bindings.
      linkHandleFields(constructor, classInfo);
      handleConstructors[classId] = constructor;
    }
    return new handleConstructors[classId](address);
  }

  // TODO: TextEncoder('utf-16') was removed, this is some polyfil code,
  // but there probably is a faster way to do this.
  function encode_utf16(jsString: string, littleEndian: boolean) {
    let a = new Uint8Array(jsString.length * 2);
    let view = new DataView(a.buffer);
    jsString.split('').forEach(function(c, i) {
      view.setUint16(i * 2, c.charCodeAt(0), littleEndian);
    });
    return a;
  }

  export function newString(jsString: string): number {
    if (jsString === null || jsString === undefined) {
      return Constants.NULL;
    }

    var objectAddr = allocObject(CLASSES.java_lang_String);
    setUncollectable(objectAddr);
    var object = <java.lang.String>getHandle(objectAddr);

    var encoded = new Uint16Array(encode_utf16(jsString, true).buffer);
    var arrayAddr = newCharArray(encoded.length);
    u16.set(encoded, Constants.ARRAY_HDR_SIZE + arrayAddr >> 1);

    object.value = arrayAddr;
    object.offset = 0;
    object.count = encoded.length;
    unsetUncollectable(objectAddr);
    return objectAddr;
  }

  export function getArrayFromAddr(addr: number) {
    if (addr === Constants.NULL) {
      return null;
    }

    release || assert(typeof addr === "number", "addr is number");
    var classInfo = classIdToClassInfoMap[i32[addr + Constants.OBJ_CLASS_ID_OFFSET >> 2]];
    var constructor;
    if (classInfo instanceof PrimitiveArrayClassInfo) {
      switch (classInfo) {
        case PrimitiveArrayClassInfo.Z:
          constructor = Uint8Array;
          break;
        case PrimitiveArrayClassInfo.C:
          constructor = Uint16Array;
          break;
        case PrimitiveArrayClassInfo.F:
          constructor = Float32Array;
          break;
        case PrimitiveArrayClassInfo.D:
          constructor = Float64Array;
          break;
        case PrimitiveArrayClassInfo.B:
          constructor = Int8Array;
          break;
        case PrimitiveArrayClassInfo.S:
          constructor = Int16Array;
          break;
        case PrimitiveArrayClassInfo.I:
          constructor = Int32Array;
          break;
        case PrimitiveArrayClassInfo.J:
          constructor = Int64Array;
          break;
        default:
          Debug.assertUnreachable("Bad primitive array" + classInfo.getClassNameSlow());
          break;
      }
    } else {
      constructor = Int32Array;
    }
    var arrayObject = new constructor(ASM.buffer, Constants.ARRAY_HDR_SIZE + addr, i32[addr + Constants.ARRAY_LENGTH_OFFSET >> 2]);
    arrayObject.classInfo = classInfo;
    return arrayObject;
  }

  var uncollectableMaxNumber = 16;
  var uncollectableAddress = gcMallocUncollectable(uncollectableMaxNumber << 2);
  export function setUncollectable(addr: number) {
    for (var i = 0; i < uncollectableMaxNumber; i++) {
      var address = (uncollectableAddress >> 2) + i;
      if (i32[address] === Constants.NULL) {
        i32[address] = addr;
        return;
      }
    }
    release || Debug.assertUnreachable("There must be a free slot.");
  }
  export function unsetUncollectable(addr: number) {
    for (var i = 0; i < uncollectableMaxNumber; i++) {
      var address = (uncollectableAddress >> 2) + i;
      if (i32[address] === addr) {
        i32[address] = Constants.NULL;
        return;
      }
    }
    release || Debug.assertUnreachable("The adddress was not found in the uncollectables.");
  }

  export function newArray(elementClassInfo: ClassInfo, size: number): number {
    release || assert(elementClassInfo instanceof ClassInfo, "elementClassInfo instanceof ClassInfo");
    if (size < 0) {
      throwNegativeArraySizeException();
    }

    var arrayClassInfo = CLASSES.getClass("[" + elementClassInfo.getClassNameSlow());
    var addr;

    if (elementClassInfo instanceof PrimitiveClassInfo) {
      addr = gcMallocAtomic(Constants.ARRAY_HDR_SIZE + size * (<PrimitiveArrayClassInfo>arrayClassInfo).bytesPerElement);
    } else {
      // We need to hold an integer to define the length of the array
      // and *size* references.
      addr = gcMalloc(Constants.ARRAY_HDR_SIZE + size * 4);
    }

    i32[addr + Constants.OBJ_CLASS_ID_OFFSET >> 2] = arrayClassInfo.id;
    i32[addr + Constants.ARRAY_LENGTH_OFFSET >> 2] = size;

    return addr;
  }

  export function newMultiArray(classInfo: ClassInfo, lengths: number[]): number {
    var length = lengths[0];
    var arrayAddr = newArray(classInfo.elementClass, length);
    if (lengths.length > 1) {
      setUncollectable(arrayAddr);

      lengths = lengths.slice(1);

      var start = (arrayAddr + Constants.ARRAY_HDR_SIZE >> 2);
      for (var i = start; i < start + length; i++) {
        i32[i] = newMultiArray(classInfo.elementClass, lengths);
      }

      unsetUncollectable(arrayAddr);
    }
    return arrayAddr;
  }

  export var JavaRuntimeException = function(message) {
    this.message = message;
  };

  JavaRuntimeException.prototype = Object.create(Error.prototype);
  JavaRuntimeException.prototype.name = "JavaRuntimeException";
  JavaRuntimeException.prototype.constructor = JavaRuntimeException;

  export function throwNegativeArraySizeException() {
    throw $.newNegativeArraySizeException();
  }

  export function throwNullPointerException() {
    throw $.newNullPointerException();
  }

  export function newObjectArray(size: number): number {
    return newArray(CLASSES.java_lang_Object, size);
  }

  export function newStringArray(size: number): number {
    return newArray(CLASSES.java_lang_String, size);
  }

  export function newByteArray(size: number): number {
    return newArray(PrimitiveClassInfo.B, size);
  }

  export function newCharArray(size: number): number {
    return newArray(PrimitiveClassInfo.C, size);
  }

  export function newIntArray(size: number): number {
    return newArray(PrimitiveClassInfo.I, size);
  }

  var jStringDecoder = new TextDecoder('utf-16');

  export function fromJavaChars(charsAddr, offset, count): string {
    release || assert(charsAddr !== Constants.NULL, "charsAddr !== Constants.NULL");

    var start = (Constants.ARRAY_HDR_SIZE + charsAddr >> 1) + offset;

    return jStringDecoder.decode(u16.subarray(start, start + count));
  }

  export function fromStringAddr(stringAddr: number): string {
    if (stringAddr === Constants.NULL) {
      return null;
    }

    // XXX Retrieve the characters directly from memory, without indirecting
    // through getHandle.
    var javaString = <java.lang.String>getHandle(stringAddr);
    return fromJavaChars(javaString.value, javaString.offset, javaString.count);
  }

  export function checkDivideByZero(value: number) {
    if (value === 0) {
      throwArithmeticException();
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
    // XXX: This function is unused, should be updated if we're
    // ever going to use it
    if ((index >>> 0) >= (array.length >>> 0)) {
      throw $.newArrayIndexOutOfBoundsException(String(index));
    }
  }

  export function throwArrayIndexOutOfBoundsException(index: number) {
    throw $.newArrayIndexOutOfBoundsException(String(index));
  }

  export function throwArithmeticException() {
    throw $.newArithmeticException("/ by zero");
  }

  export function checkArrayStore(arrayAddr: number, valueAddr: number) {
    if (valueAddr === Constants.NULL) {
      return;
    }

    var arrayClassInfo = classIdToClassInfoMap[i32[arrayAddr + Constants.OBJ_CLASS_ID_OFFSET >> 2]];
    var valueClassInfo = classIdToClassInfoMap[i32[valueAddr + Constants.OBJ_CLASS_ID_OFFSET >> 2]];

    if (!isAssignableTo(valueClassInfo, arrayClassInfo.elementClass)) {
      throw $.newArrayStoreException();
    }
  }

  export function checkNull(object: java.lang.Object) {
    if (!object) {
      throw $.newNullPointerException();
    }
  }

  export class ConfigThresholds {
    static InvokeThreshold = config.invokeThreshold;
    static BackwardBranchThreshold = config.backwardBranchThreshold;
  }

  export function monitorEnter(lock: Lock) {
    $.ctx.monitorEnter(lock);
  }

  export function monitorExit(lock: Lock) {
    $.ctx.monitorExit(lock);
  }

  export function translateException(e) {
    if (e.name === "TypeError") {
      // JavaScript's TypeError is analogous to a NullPointerException.
      return $.newNullPointerException(e.message);
    } else if (e.name === "JavaRuntimeException") {
      return $.newRuntimeException(e.message);
    }
    return e;
  }

  export function classInitCheck(classInfo: ClassInfo) {
    if (classInfo instanceof ArrayClassInfo || $.initialized[classInfo.id]) {
      return;
    }

    // TODO: make this more efficient when we decide on how to invoke code.
    var thread = $.ctx.nativeThread;
    thread.pushMarkerFrame(FrameType.Interrupt);
    thread.pushMarkerFrame(FrameType.Native);
    var frameTypeOffset = thread.fp + FrameLayout.FrameTypeOffset;
    getLinkedMethod(CLASSES.java_lang_Class.getMethodByNameString("initialize", "()V"))($.getClassObjectAddress(classInfo));
    if (U) {
      i32[frameTypeOffset] = FrameType.PushPendingFrames;
      thread.nativeFrameCount--;
      thread.unwoundNativeFrames.push(null);
      return;
    }
    thread.popMarkerFrame(FrameType.Native);
    thread.popMarkerFrame(FrameType.Interrupt);
  }

  export function preempt() {
    if (Scheduler.shouldPreempt()) {
      $.yield("preemption");
    }
  }

  export class UnwindThrowLocation {
    static instance: UnwindThrowLocation = new UnwindThrowLocation();
    pc: number;
    sp: number;
    nextPC: number;
    constructor() {
      this.pc = 0;
      this.sp = 0;
      this.nextPC = 0;
    }
    setLocation(pc: number, nextPC: number, sp: number) {
      this.pc = pc;
      this.sp = sp;
      this.nextPC = nextPC;
      return this;
    }
    getPC() {
      return this.pc;
    }
    getSP() {
      return this.sp;
    }
  }

  /**
   * Helper methods used by the compiler.
   */

  /**
   * Generic unwind throw.
   */
  export function throwUnwind(pc: number, nextPC: number = pc + 3, sp: number = 0) {
    throw UnwindThrowLocation.instance.setLocation(pc, nextPC, sp);
  }

  /**
   * Unwind throws with different stack heights. This is useful so we can
   * save a few bytes encoding the stack height in the function name.
   */
  export function throwUnwind0(pc: number, nextPC: number = pc + 3) {
    throwUnwind(pc, nextPC, 0);
  }

  export function throwUnwind1(pc: number, nextPC: number = pc + 3) {
    throwUnwind(pc, nextPC, 1);
  }

  export function throwUnwind2(pc: number, nextPC: number = pc + 3) {
    throwUnwind(pc, nextPC, 2);
  }

  export function throwUnwind3(pc: number, nextPC: number = pc + 3) {
    throwUnwind(pc, nextPC, 3);
  }

  export function throwUnwind4(pc: number, nextPC: number = pc + 3) {
    throwUnwind(pc, nextPC, 4);
  }

  export function throwUnwind5(pc: number, nextPC: number = pc + 3) {
    throwUnwind(pc, nextPC, 5);
  }

  export function throwUnwind6(pc: number, nextPC: number = pc + 3) {
    throwUnwind(pc, nextPC, 6);
  }

  export function throwUnwind7(pc: number, nextPC: number = pc + 3) {
    throwUnwind(pc, nextPC, 7);
  }

  export function fadd(a: number, b: number): number {
    aliasedI32[0] = a;
    aliasedI32[1] = b;
    aliasedF32[2] = aliasedF32[0] + aliasedF32[1];
    return aliasedI32[2];
  }

  export function fsub(a: number, b: number): number {
    aliasedI32[0] = a;
    aliasedI32[1] = b;
    aliasedF32[2] = aliasedF32[0] - aliasedF32[1];
    return aliasedI32[2];
  }

  export function fmul(a: number, b: number): number {
    aliasedI32[0] = a;
    aliasedI32[1] = b;
    aliasedF32[2] = aliasedF32[0] * aliasedF32[1];
    return aliasedI32[2];
  }

  export function fdiv(a: number, b: number): number {
    aliasedI32[0] = a;
    aliasedI32[1] = b;
    aliasedF32[2] = Math.fround(aliasedF32[0] / aliasedF32[1]);
    return aliasedI32[2];
  }

  export function frem(a: number, b: number): number {
    aliasedI32[0] = a;
    aliasedI32[1] = b;
    aliasedF32[2] = Math.fround(aliasedF32[0] % aliasedF32[1]);
    return aliasedI32[2];
  }

  export function fcmp(a: number, b: number, isLessThan: boolean): number {
    var x = (aliasedI32[0] = a, aliasedF32[0]);
    var y = (aliasedI32[0] = b, aliasedF32[0]);
    if (x !== x || y !== y) {
      return isLessThan ? -1 : 1;
    } else if (x > y) {
      return 1;
    } else if (x < y) {
      return -1;
    } else {
      return 0;
    }
  }

  export function fneg(a: number): number {
    aliasedF32[0] = -(aliasedI32[0] = a, aliasedF32[0]);
    return aliasedI32[0];
  }

  export function dcmp(al: number, ah: number, bl: number, bh: number, isLessThan: boolean) {
    var x = (aliasedI32[0] = al, aliasedI32[1] = ah, aliasedF64[0]);
    var y = (aliasedI32[0] = bl, aliasedI32[1] = bh, aliasedF64[0]);
    if (x !== x || y !== y) {
      return isLessThan ? -1 : 1;
    } else if (x > y) {
      return 1;
    } else if (x < y) {
      return -1;
    } else {
      return 0;
    }
  }

  export function dadd(al: number, ah: number, bl: number, bh: number): number {
    aliasedF64[0] = (aliasedI32[0] = al, aliasedI32[1] = ah, aliasedF64[0]) +
                    (aliasedI32[0] = bl, aliasedI32[1] = bh, aliasedF64[0]);
    return (tempReturn0 = aliasedI32[1], aliasedI32[0]);
  }

  export function dsub(al: number, ah: number, bl: number, bh: number): number {
    aliasedF64[0] = (aliasedI32[0] = al, aliasedI32[1] = ah, aliasedF64[0]) -
                    (aliasedI32[0] = bl, aliasedI32[1] = bh, aliasedF64[0]);
    return (tempReturn0 = aliasedI32[1], aliasedI32[0]);
  }

  export function dmul(al: number, ah: number, bl: number, bh: number): number {
    aliasedF64[0] = (aliasedI32[0] = al, aliasedI32[1] = ah, aliasedF64[0]) *
                    (aliasedI32[0] = bl, aliasedI32[1] = bh, aliasedF64[0]);
    return (tempReturn0 = aliasedI32[1], aliasedI32[0]);
  }

  export function ddiv(al: number, ah: number, bl: number, bh: number): number {
    aliasedF64[0] = (aliasedI32[0] = al, aliasedI32[1] = ah, aliasedF64[0]) /
                    (aliasedI32[0] = bl, aliasedI32[1] = bh, aliasedF64[0]);
    return (tempReturn0 = aliasedI32[1], aliasedI32[0]);
  }

  export function drem(al: number, ah: number, bl: number, bh: number): number {
    aliasedF64[0] = (aliasedI32[0] = al, aliasedI32[1] = ah, aliasedF64[0]) %
                    (aliasedI32[0] = bl, aliasedI32[1] = bh, aliasedF64[0]);
    return (tempReturn0 = aliasedI32[1], aliasedI32[0]);
  }

  export function dneg(al: number, ah: number): number {
    aliasedF64[0] = - (aliasedI32[0] = al, aliasedI32[1] = ah, aliasedF64[0]);
    tempReturn0 = aliasedI32[1];
    return aliasedI32[0];
  }

  export function f2i(a: number): number {
    var x = (aliasedI32[0] = a, aliasedF32[0]);
    if (x > Constants.INT_MAX) {
      return Constants.INT_MAX;
    } else if (x < Constants.INT_MIN) {
      return Constants.INT_MIN;
    } else {
      return x | 0;
    }
  }

  export function d2i(al: number, ah: number): number {
    var x = (aliasedI32[0] = al, aliasedI32[1] = ah, aliasedF64[0]);
    if (x > Constants.INT_MAX) {
      return Constants.INT_MAX;
    } else if (x < Constants.INT_MIN) {
      return Constants.INT_MIN;
    } else {
      return x | 0;
    }
  }

  export function i2f(a: number): number {
    aliasedF32[0] = a;
    return aliasedI32[0];
  }

  export function i2d(a: number): number {
    aliasedF64[0] = a;
    tempReturn0 = aliasedI32[1];
    return aliasedI32[0];
  }

  export function l2d(al: number, ah: number): number {
    aliasedF64[0] = longToNumber(al, ah);
    tempReturn0 = aliasedI32[1];
    return aliasedI32[0];
  }

  export function l2f(al: number, ah: number): number {
    aliasedF32[0] = Math.fround(longToNumber(al, ah));
    return aliasedI32[0];
  }

  export function f2d(l: number): number {
    aliasedF64[0] = (aliasedI32[0] = l, aliasedF32[0]);
    tempReturn0 = aliasedI32[1];
    return aliasedI32[0];
  }

  export function f2l(l: number): number {
    var x = (aliasedI32[0] = l, aliasedF32[0]);
    return returnLongValue(x);
  }

  export function d2l(al: number, ah: number): number {
    var x = (aliasedI32[0] = al, aliasedI32[1] = ah, aliasedF64[0]);
    if (x === Number.POSITIVE_INFINITY) {
      tempReturn0 = Constants.LONG_MAX_HIGH;
      return Constants.LONG_MAX_LOW;
    } else if (x === Number.NEGATIVE_INFINITY) {
      tempReturn0 = Constants.LONG_MIN_HIGH;
      return Constants.LONG_MIN_LOW;
    } else {
      return returnLongValue(x);
    }
  }

  export function d2f(al: number, ah: number): number {
    var x = (aliasedI32[0] = al, aliasedI32[1] = ah, aliasedF64[0]);
    aliasedF32[0] = Math.fround(x);
    return aliasedI32[0];
  }

  export function gcMalloc(size: number): number {
    release || gcCounter.count("gcMalloc");
    return ASM._gcMalloc(size);
  }

  export function gcMallocAtomic(size: number): number {
    release || gcCounter.count("gcMallocAtomic");
    return ASM._gcMallocAtomic(size);
  }

  export function gcMallocUncollectable(size: number): number {
    release || gcCounter.count("gcMallocUncollectable");
    return ASM._gcMallocUncollectable(size);
  }

  var tmpAddress = gcMallocUncollectable(32);

  export function lcmp(al: number, ah: number, bl: number, bh: number) {
    i32[tmpAddress +  0 >> 2] = al;
    i32[tmpAddress +  4 >> 2] = ah;
    i32[tmpAddress +  8 >> 2] = bl;
    i32[tmpAddress + 12 >> 2] = bh;
    ASM._lCmp(tmpAddress, tmpAddress, tmpAddress + 8);
    return i32[tmpAddress >> 2];
  }

  export function ladd(al: number, ah: number, bl: number, bh: number) {
    i32[tmpAddress +  0 >> 2] = al;
    i32[tmpAddress +  4 >> 2] = ah;
    i32[tmpAddress +  8 >> 2] = bl;
    i32[tmpAddress + 12 >> 2] = bh;
    ASM._lAdd(tmpAddress, tmpAddress, tmpAddress + 8);
    tempReturn0 = i32[tmpAddress + 4 >> 2];
    return i32[tmpAddress >> 2];
  }

  export function lsub(al: number, ah: number, bl: number, bh: number) {
    i32[tmpAddress +  0 >> 2] = al;
    i32[tmpAddress +  4 >> 2] = ah;
    i32[tmpAddress +  8 >> 2] = bl;
    i32[tmpAddress + 12 >> 2] = bh;
    ASM._lSub(tmpAddress, tmpAddress, tmpAddress + 8);
    tempReturn0 = i32[tmpAddress + 4 >> 2];
    return i32[tmpAddress >> 2];
  }

  export function lmul(al: number, ah: number, bl: number, bh: number) {
    i32[tmpAddress +  0 >> 2] = al;
    i32[tmpAddress +  4 >> 2] = ah;
    i32[tmpAddress +  8 >> 2] = bl;
    i32[tmpAddress + 12 >> 2] = bh;
    ASM._lMul(tmpAddress, tmpAddress, tmpAddress + 8);
    tempReturn0 = i32[tmpAddress + 4 >> 2];
    return i32[tmpAddress >> 2];
  }

  export function ldiv(al: number, ah: number, bl: number, bh: number) {
    i32[tmpAddress +  0 >> 2] = al;
    i32[tmpAddress +  4 >> 2] = ah;
    i32[tmpAddress +  8 >> 2] = bl;
    i32[tmpAddress + 12 >> 2] = bh;
    ASM._lDiv(tmpAddress, tmpAddress, tmpAddress + 8);
    tempReturn0 = i32[tmpAddress + 4 >> 2];
    return i32[tmpAddress >> 2];
  }

  export function lrem(al: number, ah: number, bl: number, bh: number) {
    i32[tmpAddress +  0 >> 2] = al;
    i32[tmpAddress +  4 >> 2] = ah;
    i32[tmpAddress +  8 >> 2] = bl;
    i32[tmpAddress + 12 >> 2] = bh;
    ASM._lRem(tmpAddress, tmpAddress, tmpAddress + 8);
    tempReturn0 = i32[tmpAddress + 4 >> 2];
    return i32[tmpAddress >> 2];
  }

  export function lneg(al: number, ah: number): number {
    i32[tmpAddress + 0 >> 2] = al;
    i32[tmpAddress + 4 >> 2] = ah;
    ASM._lNeg(tmpAddress, tmpAddress);
    tempReturn0 = i32[tmpAddress + 4 >> 2];
    return i32[tmpAddress >> 2];
  }

  export function lshl(al: number, ah: number, shift: number): number {
    i32[tmpAddress + 0 >> 2] = al;
    i32[tmpAddress + 4 >> 2] = ah;
    ASM._lShl(tmpAddress, tmpAddress, shift);
    tempReturn0 = i32[tmpAddress + 4 >> 2];
    return i32[tmpAddress >> 2];
  }

  export function lshr(al: number, ah: number, shift: number): number {
    i32[tmpAddress + 0 >> 2] = al;
    i32[tmpAddress + 4 >> 2] = ah;
    ASM._lShr(tmpAddress, tmpAddress, shift);
    tempReturn0 = i32[tmpAddress + 4 >> 2];
    return i32[tmpAddress >> 2];
  }

  export function lushr(al: number, ah: number, shift: number): number {
    i32[tmpAddress + 0 >> 2] = al;
    i32[tmpAddress + 4 >> 2] = ah;
    ASM._lUshr(tmpAddress, tmpAddress, shift);
    tempReturn0 = i32[tmpAddress + 4 >> 2];
    return i32[tmpAddress >> 2];
  }
}

var Runtime = J2ME.Runtime;

var AOTMD = J2ME.aotMetaData;

/**
 * Are we currently unwinding the stack because of a Yield? This technically
 * belonges to a context but we store it in the global object because it is
 * read very often.
 */
var U: J2ME.VMState = J2ME.VMState.Running;

// To enable breaking when it is set in Chrome, define it as a getter/setter:
// http://stackoverflow.com/questions/11618278/how-to-break-on-property-change-in-chrome
// var _U: J2ME.VMState = J2ME.VMState.Running;
// declare var U;
// Object.defineProperty(jsGlobal, 'U', {
//     get: function () {
//         return jsGlobal._U;
//     },
//     set: function (value) {
//         jsGlobal._U = value;
//     }
// });

// Several unwind throws for different stack heights.

var B0 = J2ME.throwUnwind0;
var B1 = J2ME.throwUnwind1;
var B2 = J2ME.throwUnwind2;
var B3 = J2ME.throwUnwind3;
var B4 = J2ME.throwUnwind4;
var B5 = J2ME.throwUnwind5;
var B6 = J2ME.throwUnwind6;
var B7 = J2ME.throwUnwind7;

/**
 * OSR Frame.
 */
// REDUX
var O: J2ME.MethodInfo = null;

/**
 * Runtime exports for compiled code.
 * DO NOT use these short names outside of compiled code.
 */

var CI = J2ME.classIdToClassInfoMap;
var MI = J2ME.methodIdToMethodInfoMap;
var LM = J2ME.linkedMethods;
var GLM = J2ME.getLinkedMethodById;
var GLVM = J2ME.getLinkedVirtualMethodById;
var VT = J2ME.linkedVTableMap;
var FT = J2ME.flatLinkedVTableMap;

var CIC = J2ME.classInitCheck;
var GH = J2ME.getHandle;
var AO = J2ME.allocObject;

var IOK = J2ME.instanceOfKlass;
var IOI = J2ME.instanceOfInterface;

var CCK = J2ME.checkCastKlass;
var CCI = J2ME.checkCastInterface;

//var AK = J2ME.getArrayKlass;

var NA = J2ME.newArray;
var NM = J2ME.newMultiArray;



var CAB = J2ME.checkArrayBounds;
var CAS = J2ME.checkArrayStore;

// XXX Ensure these work with new monitor objects.
var GM = J2ME.getMonitor;
var ME = J2ME.monitorEnter;
var MX = J2ME.monitorExit;

var TE = J2ME.translateException;
var TI = J2ME.throwArrayIndexOutOfBoundsException;
var TA = J2ME.throwArithmeticException;
var TS = J2ME.throwNegativeArraySizeException;
var TN = J2ME.throwNullPointerException;

var PE = J2ME.preempt;
var PS = 0; // Preemption samples.

var MA = J2ME.gcMallocAtomic;

var fadd = J2ME.fadd;
var fsub = J2ME.fsub;
var fmul = J2ME.fmul;
var fdiv = J2ME.fdiv;
var frem = J2ME.frem;
var fneg = J2ME.fneg;

var f2i = J2ME.f2i;
var f2l = J2ME.f2l;
var f2d = J2ME.f2d;
var i2f = J2ME.i2f;
var i2d = J2ME.i2d;
var d2i = J2ME.d2i;
var d2l = J2ME.d2l;
var d2f = J2ME.d2f;
var l2f = J2ME.l2f;
var l2d = J2ME.l2d;
var fcmp = J2ME.fcmp;

var dneg = J2ME.dneg;
var dcmp = J2ME.dcmp;
var dadd = J2ME.dadd;
var dsub = J2ME.dsub;
var dmul = J2ME.dmul;
var ddiv = J2ME.ddiv;
var drem = J2ME.drem;

var lneg = J2ME.lneg;
var ladd = J2ME.ladd;
var lsub = J2ME.lsub;
var lmul = J2ME.lmul;
var ldiv = J2ME.ldiv;
var lrem = J2ME.lrem;
var lcmp = J2ME.lcmp;
var lshl = J2ME.lshl;
var lshr = J2ME.lshr;
var lushr = J2ME.lushr;

var getHandle = J2ME.getHandle;

var NativeMap = J2ME.NativeMap;
var setNative = J2ME.setNative;
