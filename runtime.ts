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
  export var traceWriter = null;
  export var linkWriter = null;
  export var initWriter = null;

  declare var Shumway;

  export var timeline;
  export var nativeCounter = new Metrics.Counter(true);
  export var runtimeCounter = new Metrics.Counter(true);
  export var jitMethodInfos = {};

  if (typeof Shumway !== "undefined") {
    timeline = new Shumway.Tools.Profiler.TimelineBuffer("Runtime");
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
        ClassNotFoundException: null,
        SecurityException: null,
        IllegalThreadStateException: null,
        NegativeArraySizeException: null,
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

  function hashString(s: string) {
    var data = new Int32Array(s.length);
    for (var i = 0; i < s.length; i++) {
      data[i] = s.charCodeAt(i);
    }
    return HashUtilities.hashBytesTo32BitsMD5(data, 0, s.length);
  }

  var friendlyMangledNames = true;

  export function escapeString(s: string) {
    var invalidChars = "[];/<>()";
    var replaceChars = "abc_defg";
    var result = "";
    for (var i = 0; i < s.length; i++) {
      if ((i === 0 && isIdentifierStart(s[i])) || (i > 0 && isIdentifierPart(s[i]))) {
        result += s[i];
      } else {
        release || assert (invalidChars.indexOf(s[i]) >= 0, s[i] + " " + s);
        result += replaceChars[invalidChars.indexOf(s[i])];
      }
    }
    return result;
  }

  var stringHashes = Object.create(null);
  var stringHasheCount = 0;

  export function hashStringToString(s: string) {
    if (stringHasheCount > 1024) {
      return StringUtilities.variableLengthEncodeInt32(hashString(s));
    }
    var c = stringHashes[s];
    if (c) {
      return c;
    }
    c = stringHashes[s] = StringUtilities.variableLengthEncodeInt32(hashString(s));
    stringHasheCount ++;
    return c;
  }

  export function mangleClassAndMethod(methodInfo: MethodInfo) {
    var name = methodInfo.classInfo.className + "_" + methodInfo.name + "_" + hashStringToString(methodInfo.signature);
    if (friendlyMangledNames) {
      return escapeString(name);
    }
    var hash = hashString(name);
    return StringUtilities.variableLengthEncodeInt32(hash);
  }

  export function mangleMethod(methodInfo: MethodInfo) {
    var name = methodInfo.name + "_" + hashStringToString(methodInfo.signature);
    if (friendlyMangledNames) {
      return escapeString(name);
    }
    var hash = hashString(name);
    return StringUtilities.variableLengthEncodeInt32(hash);
  }

  export function mangleClass(classInfo: ClassInfo) {
    if (classInfo instanceof PrimitiveArrayClassInfo) {
      switch (classInfo) {
        case PrimitiveArrayClassInfo.Z: return "Uint8Array";
        case PrimitiveArrayClassInfo.C: return "Uint16Array";
        case PrimitiveArrayClassInfo.F: return "Float32Array";
        case PrimitiveArrayClassInfo.D: return "Float64Array";
        case PrimitiveArrayClassInfo.B: return "Int8Array";
        case PrimitiveArrayClassInfo.S: return "Int16Array";
        case PrimitiveArrayClassInfo.I: return "Int32Array";
        case PrimitiveArrayClassInfo.J: return "Int64Array";
      }
    } else if (classInfo.isArrayClass) {
      return "$AK(" + mangleClass(classInfo.elementClass) + ")";
    } else {
      if (friendlyMangledNames) {
        return "$" + escapeString(classInfo.className);
      }
      var hash = hashString(classInfo.className);
      return "$" + StringUtilities.variableLengthEncodeInt32(hash);
    }
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
      this.initialized = {};
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

    newIOException(str: string): java.io.IOException {
      return <java.io.IOException>$.ctx.createException(
        "java/io/IOException", str);
    }

    newUnsupportedEncodingException(str: string): java.io.UnsupportedEncodingException {
      return <java.io.UnsupportedEncodingException>$.ctx.createException(
        "java/io/UnsupportedEncodingException", str);
    }

    newUTFDataFormatException(str: string): java.io.UTFDataFormatException {
      return <java.io.UTFDataFormatException>$.ctx.createException(
        "java/io/UTFDataFormatException", str);
    }

    newSecurityException(str: string): java.lang.SecurityException {
      return <java.lang.SecurityException>$.ctx.createException(
        "java/lang/SecurityException", str);
    }

    newIllegalThreadStateException(str: string): java.lang.IllegalThreadStateException {
      return <java.lang.IllegalThreadStateException>$.ctx.createException(
        "java/lang/IllegalThreadStateException", str);
    }

    newRuntimeException(str: string): java.lang.RuntimeException {
      return <java.lang.RuntimeException>$.ctx.createException(
        "java/lang/RuntimeException", str);
    }

    newIndexOutOfBoundsException(str: string): java.lang.IndexOutOfBoundsException {
      return <java.lang.IndexOutOfBoundsException>$.ctx.createException(
        "java/lang/IndexOutOfBoundsException", str);
    }

    newArrayIndexOutOfBoundsException(str: string): java.lang.ArrayIndexOutOfBoundsException {
      return <java.lang.ArrayIndexOutOfBoundsException>$.ctx.createException(
        "java/lang/ArrayIndexOutOfBoundsException", str);
    }

    newStringIndexOutOfBoundsException(str: string): java.lang.StringIndexOutOfBoundsException {
      return <java.lang.StringIndexOutOfBoundsException>$.ctx.createException(
        "java/lang/StringIndexOutOfBoundsException", str);
    }

    newArrayStoreException(str: string): java.lang.ArrayStoreException {
      return <java.lang.ArrayStoreException>$.ctx.createException(
        "java/lang/ArrayStoreException", str);
    }

    newClassNotFoundException(str: string): java.lang.ClassNotFoundException {
      return <java.lang.ClassNotFoundException>$.ctx.createException(
        "java/lang/ClassNotFoundException", str);
    }

    newIllegalArgumentException(str: string): java.lang.IllegalArgumentException {
      return <java.lang.IllegalArgumentException>$.ctx.createException(
        "java/lang/IllegalArgumentException", str);
    }

    newIllegalStateException(str: string): java.lang.IllegalStateException {
      return <java.lang.IllegalStateException>$.ctx.createException(
        "java/lang/IllegalStateException", str);
    }

    newNegativeArraySizeException(str: string): java.lang.NegativeArraySizeException {
      return <java.lang.NegativeArraySizeException>$.ctx.createException(
        "java/lang/NegativeArraySizeException", str);
    }

    newNullPointerException(str: string): java.lang.NullPointerException {
      return <java.lang.NullPointerException>$.ctx.createException(
        "java/lang/NullPointerException", str);
    }

    newMediaException(str: string): javax.microedition.media.MediaException {
      return <javax.microedition.media.MediaException>$.ctx.createException(
        "javax/microedition/media/MediaException", str);
    }

    newException(str: string): java.lang.Exception {
      return <java.lang.Exception>$.ctx.createException(
        "java/lang/Exception", str);
    }

  }

  export enum VMState {
    Running = 0,
    Yielding = 1,
    Pausing = 2
  }

  export class Runtime extends RuntimeTemplate {
    private static _nextId: number = 0;
    id: number;

    /**
     * Bailout callback whenever a JIT frame is unwound.
     */
    B(bci: number, local: any [], stack: any []) {
      var methodInfo = jitMethodInfos[(<any>arguments.callee.caller).name];
      release || assert(methodInfo !== undefined);
      $.ctx.bailout(methodInfo, bci, local, stack);
    }

    yield() {
      U = VMState.Yielding;
    }

    pause() {
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
    arrayKlass: ArrayKlass;

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

  export interface ArrayKlass extends Klass {
    elementKlass: Klass;
    isArrayKlass: boolean;
  }

  export class Lock {
    constructor(public thread: java.lang.Thread, public level: number) {
      // ...
    }
  }

  export module java.lang {
    export interface Object {
      /**
       * Reference to the runtime klass.
       */
      klass: Klass

      /**
       * All objects have an internal hash code.
       */
      _hashCode: number;

      /**
       * Some objects may have a lock.
       */
      _lock: Lock;

      clone(): java.lang.Object;
      equals(obj: java.lang.Object): boolean;
      finalize(): void;
      getClass(): java.lang.Class;
      hashCode(): number;
      notify(): void;
      notifyAll(): void;
      toString(): java.lang.String;
      notify(): void;
      notify(timeout: number): void;
      notify(timeout: number, nanos: number): void;
    }

    export interface Class extends java.lang.Object {
      /**
       * RuntimeKlass associated with this Class object.
       */
      runtimeKlass: RuntimeKlass;
    }

    export interface String extends java.lang.Object {
      str: string;
    }

    export interface Thread extends java.lang.Object {
      pid: number;
      alive: boolean;
    }

    export interface Exception extends java.lang.Object {
      message: string;
    }

    export interface IllegalArgumentException extends java.lang.Exception {
    }

    export interface IllegalStateException extends java.lang.Exception {
    }

    export interface NullPointerException extends java.lang.Exception {
    }

    export interface RuntimeException extends java.lang.Exception {
    }

    export interface IndexOutOfBoundsException extends java.lang.Exception {
    }

    export interface ArrayIndexOutOfBoundsException extends java.lang.Exception {
    }

    export interface StringIndexOutOfBoundsException extends java.lang.Exception {
    }

    export interface ArrayStoreException extends java.lang.Exception {
    }

    export interface ClassNotFoundException extends java.lang.Exception {
    }

    export interface SecurityException extends java.lang.Exception {
    }

    export interface IllegalThreadStateException extends java.lang.Exception {
    }

    export interface NegativeArraySizeException extends java.lang.Exception {
    }

  }

  export module java.io {

    export interface IOException extends java.lang.Exception {
    }

    export interface UTFDataFormatException extends java.lang.Exception {
    }

    export interface UnsupportedEncodingException extends java.lang.Exception {
    }

  }

  export module javax.microedition.media {

    export interface MediaException extends java.lang.Exception {
    }

  }

  export module com.sun.cldc.isolate {
    export interface Isolate extends java.lang.Object {
      id: number;
      runtime: Runtime;
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
        release || assert(!U);
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
    return jsGlobal[classInfo.mangledName];
  }

  export function registerKlassSymbol(className: string) {
    linkWriter && linkWriter.writeLn("Registering Klass: " + className);
    // TODO: This needs to be kept in sync to how mangleClass works.
    var mangledName = "$" + escapeString(className);
    if (RuntimeTemplate.prototype.hasOwnProperty(mangledName)) {
      return;
    }

    if (!RuntimeTemplate.prototype.hasOwnProperty(mangledName)) {
      Object.defineProperty(RuntimeTemplate.prototype, mangledName, {
        configurable: true,
        get: function lazyKlass() {
          linkWriter && linkWriter.writeLn("Initializing Klass: " + className);
          CLASSES.getClass(className);
          return this[mangledName]; // This should not be recursive at this point.
        }
      });
    }

    if (!jsGlobal.hasOwnProperty(mangledName)) {
      unresolvedSymbols[mangledName] = true;
      Object.defineProperty(jsGlobal, mangledName, {
        configurable: true,
        get: function () {
          linkWriter && linkWriter.writeLn("Initializing Klass: " + className);
          CLASSES.getClass(className);
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

  export function getKlass(classInfo: ClassInfo): Klass {
    if (!classInfo) {
      return null;
    }
    if (classInfo.klass) {
      return classInfo.klass;
    }
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
      var mangledName = mangleClass(classInfo);
      if (classInfo.isInterface) {
        klass = function () {
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
        klass = getArrayKlass(elementKlass);
      } else if (classInfo instanceof PrimitiveClassInfo) {
        klass = function () {
          Debug.unexpected("Should never be instantiated.")
        };
        klass.toString = function () {
          return "[Primitive Klass " + classInfo.className + "]";
        };
      } else {
        enterTimeline("emitKlass");
        // TODO: Creating and evaling a Klass here may be too slow at startup. Consider
        // creating a closure, which will probably be slower at runtime.
        var source = "";
        var writer = new IndentingWriter(false, x => source += x + "\n");
        var emitter = new Emitter(writer, false, true, true);
        J2ME.emitKlass(emitter, classInfo);
        (1, eval)(source);
        leaveTimeline("emitKlass");
        // consoleWriter.writeLn("Synthesizing Klass: " + classInfo.className);
        // consoleWriter.writeLn(source);
        klass = jsGlobal[mangledName];
        release || assert(klass, mangledName);
        klass.toString = function () {
          return "[Synthesized Klass " + classInfo.className + "]";
        };
      }
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

    if (!classInfo.isInterface) {
      initializeInterfaces(klass, classInfo);
    }

    return klass;
  }

  export function linkKlass(classInfo: ClassInfo) {
    enterTimeline("linkKlass", {classInfo: classInfo});
    var mangledName = mangleClass(classInfo);
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
          release || assert (false, "Method " + methodInfo.name + " is native but does not have an implementation.");
        }
      }
    } else if (implKey in Override) {
      return Override[implKey];
    }
    return null;
  }

  function prepareInterpretedMethod(methodInfo: MethodInfo): Function {
    return function interpreterFrameAdapter() {
      var frame = new Frame(methodInfo, [], 0);
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
      var caller = $.ctx.current();
      var callee = frame;
      if (methodInfo.isSynchronized) {
        if (!frame.lockObject) {
          frame.lockObject = methodInfo.isStatic
            ? methodInfo.classInfo.getStaticObject($.ctx)
            : frame.getLocal(0);
        }
        $.ctx.monitorEnter(frame.lockObject);
        if (U === VMState.Pausing) {
          $.ctx.frames.push(frame);
          return;
        }
      }
      return $.ctx.executeNewFrameSet([frame]);
    };
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
          ObjectUtilities.defineNonEnumerableForwardingProperty(object, symbolName, field.mangledName);
        }
      }
    }
  }

  function linkKlassMethods(klass: Klass) {
    linkWriter && linkWriter.enter("Link Klass Methods: " + klass);
    var methods = klass.classInfo.methods;
    for (var i = 0; i < methods.length; i++) {
      var methodInfo = methods[i];
      var fn;
      var methodType;
      var nativeMethod = findNativeMethodImplementation(methods[i]);
      var methodDescription = methods[i].name + methods[i].signature;
      var updateGlobalObject = true;
      if (nativeMethod) {
        linkWriter && linkWriter.writeLn("Method: " + methodDescription + " -> Native / Override");
        fn = nativeMethod;
        methodType = MethodType.Native;
      } else {
        fn = findCompiledMethod(klass, methodInfo);
        if (fn && !methodInfo.isSynchronized) {
          linkWriter && linkWriter.greenLn("Method: " + methodDescription + " -> Compiled");
          methodType = MethodType.Compiled;
          if (!traceWriter) {
            linkWriter && linkWriter.outdent();
          }
          // Save method info so that we can figure out where we are bailing
          // out from.
          jitMethodInfos[fn.name] = methodInfo;
          updateGlobalObject = false;
        } else {
          linkWriter && linkWriter.warnLn("Method: " + methodDescription + " -> Interpreter");
          methodType = MethodType.Interpreted;
          fn = prepareInterpretedMethod(methodInfo);
        }
      }

      if (false && timeline) {
        fn = profilingWrapper(fn, methodInfo, methodType);
        updateGlobalObject = true;
      }

      if (traceWriter && methodType !== MethodType.Interpreted) {
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
        var s = ops;
        try {
          enterTimeline(key);
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
          leaveTimeline(key, s !== ops ? { ops: ops - s } : undefined);
        } catch (e) {
          leaveTimeline(key, s !== ops ? { ops: ops - s } : undefined);
          throw e;
        }
        return r;
      };
    }

    function tracingWrapper(fn: Function, methodInfo: MethodInfo, methodType: MethodType) {
      return function() {
        var args = Array.prototype.slice.apply(arguments);
        var printArgs = args.map(function (x) {
          return toDebugString(x);
        }).join(", ");
        var printObj = "";
        if (!methodInfo.isStatic) {
          printObj = " <" + toDebugString(this) + "> ";
        }
        traceWriter.enter("> " + MethodType[methodType][0] + " " + methodInfo.classInfo.className + "/" + methodInfo.name + signatureToDefinition(methodInfo.signature, true, true) + printObj + ", arguments: " + printArgs);
        var s = performance.now();
        var value = fn.apply(this, args);
        var elapsedStr = " " + (performance.now() - s).toFixed(4);
        if (methodInfo.getReturnKind() !== Kind.Void) {
          traceWriter.leave("< " + toDebugString(value) + elapsedStr);
        } else {
          traceWriter.leave("<" + elapsedStr);
        }
        return value;
      };
    }
  }

  /**
   * Creates lookup tables used to efficiently implement type checks.
   */
  function initializeKlassTables(klass: Klass) {
    klass.depth = klass.superKlass ? klass.superKlass.depth + 1 : 0;
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

  export function isAssignableTo(from: Klass, to: Klass): boolean {
    if (to.isInterfaceKlass) {
      return from.interfaces.indexOf(to) >= 0;
    }
    return from.display[to.depth] === to;
  }

  export function instanceOfKlass(object: java.lang.Object, klass: Klass): boolean {
    return object !== null && object.klass.display[klass.depth] === klass;
  }

  export function instanceOfInterface(object: java.lang.Object, klass: Klass): boolean {
    release || assert(klass.isInterfaceKlass);
    return object !== null && object.klass.interfaces.indexOf(klass) >= 0;
  }

  export function checkCast(object: java.lang.Object, klass: Klass) {
    if (object !== null && !isAssignableTo(object.klass, klass)) {
      throw $.ctx.createException("java/lang/ClassCastException");
    }
  }

  export function checkCastKlass(object: java.lang.Object, klass: Klass) {
    if (object !== null && object.klass.display[klass.depth] !== klass) {
      throw $.ctx.createException("java/lang/ClassCastException");
    }
  }

  export function checkCastInterface(object: java.lang.Object, klass: Klass) {
    if (object !== null && object.klass.interfaces.indexOf(klass) < 0) {
      throw $.ctx.createException("java/lang/ClassCastException");
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

  export function newArray(klass: Klass, size: number) {
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
    var klass = <ArrayKlass><any> getArrayConstructor(elementKlass.classInfo.className);
    if (!klass) {
      klass = <ArrayKlass><any> function (size:number) {
        var array = createEmptyObjectArray(size);
        (<any>array).klass = klass;
        return array;
      };
      klass.toString = function () {
        return "[Array of " + elementKlass + "]";
      };
      if (elementKlass === Klasses.java.lang.Object) {
        extendKlass(klass, Klasses.java.lang.Object);
        extendKlass(<ArrayKlass><any>Array, Klasses.java.lang.Object);
      } else {
        extendKlass(klass, getArrayKlass(Klasses.java.lang.Object));
      }
    } else {
      release || assert(!klass.prototype.hasOwnProperty("klass"));
      klass.prototype.klass = klass;
      extendKlass(klass, Klasses.java.lang.Object);
      klass.toString = function () {
        return "[Array of " + elementKlass + "]";
      };
    }
    klass.isArrayKlass = true;
    elementKlass.arrayKlass = klass;
    klass.elementKlass = elementKlass;
    var className = elementKlass.classInfo.className;
    if (!(elementKlass.classInfo instanceof PrimitiveClassInfo) && className[0] !== "[")
      className = "L" + className + ";";
    className = "[" + className;
    klass.classInfo = CLASSES.getClass(className);

    registerKlass(klass, klass.classInfo);
    return klass;
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
      throw $.ctx.createException("java/lang/ArithmeticException", "/ by zero");
    }
  }

  export function checkDivideByZeroLong(value: Long) {
    if (value.isZero()) {
      throw $.ctx.createException("java/lang/ArithmeticException", "/ by zero");
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
 * Runtime exports for compiled code.
 */
var $IOK = J2ME.instanceOfKlass;
var $IOI = J2ME.instanceOfInterface;

var $CCK = J2ME.checkCastKlass;
var $CCI = J2ME.checkCastInterface;

var $AK = J2ME.getArrayKlass;
var $NA = J2ME.newArray;
var $S = function(str: string) {
  return $.newStringConstant(str);
};
var $CDZ = J2ME.checkDivideByZero;
var $CDZL = J2ME.checkDivideByZeroLong;

var $ME = function monitorEnter(object: J2ME.java.lang.Object) {
  console.info("ENTER " + J2ME.toDebugString(object));
  $.ctx.monitorEnter(object);
};

var $MX = function monitorExit(object: J2ME.java.lang.Object) {
  console.info("EXIT " + J2ME.toDebugString(object));
  $.ctx.monitorExit(object);
};
