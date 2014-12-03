var $: J2ME.Runtime; // The currently-executing runtime.

interface Math {
  fround(value: number): number;
}

declare var throwHelper;

module J2ME {
  declare var Native, Override;
  declare var VM;
  declare var Long;


  export var traceWriter = null; // new IndentingWriter(false, IndentingWriter.stderr);
  export var linkingWriter = null; // new IndentingWriter(false, IndentingWriter.stderr);

  export var Klasses = {
    java: {
      lang: {
        Object: null,
        Class: null,
        String: null,
        Thread: null
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

  declare var internedStrings: Map<string, string>;
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
      this._runtimeId = RuntimeTemplate._nextRuntimeId ++;
      this._nextHashCode = this._runtimeId << 24;
    }

    /**
     * Generates a new hash code for the specified |object|.
     */
    nextHashCode(object: java.lang.Object): number {
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

    newStringConstant(s) {
      if (internedStrings.has(s)) {
        return internedStrings.get(s);
      }
      var obj = util.newString(s);
      internedStrings.set(s, obj);
      return obj;
    }

    setStatic(field, value) {
      this.staticFields[field.id] = value;
    }

    getStatic(field) {
      return this.staticFields[field.id];
    }
  }

  export class Runtime extends RuntimeTemplate {
    constructor(jvm: JVM) {
      super(jvm);
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
     * Initializes static fields to their default values, not all klasses have one.
     */
    staticInitializer: () => void;

    /**
     * Static constructor, not all klasses have one.
     */
    staticConstructor: () => void;

    /**
     * Java class object. This is only available on runtime klasses and it points to itself. We go trough
     * this indirection in VM code for now so that we can easily change it later if we need to.
     */
    classObject: java.lang.Class;

    /**
     * Whether this class is a runtime class.
     */
    isRuntimeKlass: boolean;

    templateKlass: Klass;

    /**
     * Whether this class is an interface class.
     */
    isInterfaceKlass: boolean;
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
      __hashCode__: number;

      /**
       * Some objects may have a lock.
       */
      __lock__: Lock;

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
      runtimeKlass: Klass;
    }

    export interface String extends java.lang.Object {
      str: string;
    }

    export interface Thread extends java.lang.Object {
      pid: number;
      alive: boolean;
    }
  }

  export module com.sun.cldc.isolate {
    export interface Isolate extends java.lang.Object {
      id: number;
      runtime: Runtime;
      $com_sun_cldc_isolate_Isolate_mainClass: java.lang.String;
      $com_sun_cldc_isolate_Isolate_mainArgs: java.lang.String [];
    }
  }

  function initializeClassObject(klass: Klass) {
    linkingWriter && linkingWriter.writeLn("Initializing Class Object For: " + klass);
    assert(klass.isRuntimeKlass, "Can only create class objects for runtime klasses.");
    assert(!klass.classObject);
    klass.classObject = <java.lang.Class><any>klass;
    (<any>Object).setPrototypeOf(klass.classObject, Klasses.java.lang.Class.prototype);
    // <java.lang.Class>newObject(Klasses.java.lang.Class);
    klass.classObject.runtimeKlass = klass;
    var fields = klass.templateKlass.classInfo.fields;
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
        field.set(klass.classObject, defaultValue);
      }
    }
  }

  export function registerRuntimeKlass(klass: Klass, classInfo: ClassInfo) {
    // Ensure each Runtime instance receives its own copy of the class
    // constructor, hoisted off the current runtime.
    linkingWriter && linkingWriter.writeLn("Registering Runtime Klass: " + classInfo.className + " as " + classInfo.mangledName);
    Object.defineProperty(RuntimeTemplate.prototype, classInfo.mangledName, {
      configurable: true,
      get: function () {
        linkingWriter && linkingWriter.writeLn("Initializing Runtime Klass: " + classInfo.className);
        assert(!klass.isRuntimeKlass);
        var runtimeKlass = klass.bind(null);
        runtimeKlass.templateKlass = klass;
        runtimeKlass.isRuntimeKlass = true;
        initializeClassObject(runtimeKlass);
        Object.defineProperty(this, classInfo.mangledName, {
          configurable: false,
          value: runtimeKlass
        });
        if (classInfo.className === "com/sun/cldc/i18n/StreamWriter") {
          debugger;
        }
        linkingWriter && linkingWriter.writeLn("Running Static Initializer: " + classInfo.className);
        $.ctx.pushClassInitFrame(classInfo);
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

  export function registerKlassSymbol(className: string) {
    linkingWriter && linkingWriter.writeLn("Registering Klass: " + className);
    var mangledName = J2ME.C4.Backend.escapeString(className);
    if (RuntimeTemplate.prototype.hasOwnProperty(mangledName)) {
      return;
    }
    Object.defineProperty(RuntimeTemplate.prototype, mangledName, {
      configurable: true,
      get: function () {
        linkingWriter && linkingWriter.writeLn("Initializing Klass: " + className);
        CLASSES.getClass(className);
        return this[mangledName]; // This should not be recursive.
      }
    });
  }

  export function registerKlassSymbols(classNames: string []) {
    for (var i = 0; i < classNames.length; i++) {
      var className = classNames[i];
      registerKlassSymbol(className);
    }
  }

  export function runtimeKlass(runtime: Runtime, klass: Klass): Klass {
    assert(!klass.isRuntimeKlass);
    assert(klass.classInfo.mangledName);
    var runtimeKlass = runtime[klass.classInfo.mangledName];
    assert(runtimeKlass.isRuntimeKlass);
    return runtimeKlass;
  }

  export function getKlass(classInfo: ClassInfo): Klass {
    if (!classInfo) {
      return null;
    }
    if (classInfo.klass) {
      return classInfo.klass;
    }
    var klass = jsGlobal[classInfo.mangledName];
    if (klass) {
      linkingWriter && linkingWriter.greenLn("Found Compiled Klass: " + classInfo.className);
      release || assert(!classInfo.klass);
      classInfo.klass = klass;
      klass.toString = function () {
        return "[Compiled Klass " + classInfo.className + "]";
      };
      if (klass.classSymbols) {
        registerKlassSymbols(klass.classSymbols);
      }
    } else {
      if (classInfo.isInterface) {
        klass = function () {
          Debug.unexpected("Should never be instantiated.")
        };
        klass.isInterfaceKlass = true;
        klass.toString = function () {
          return "[Interface Klass " + classInfo.className + "]";
        };
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
        // TODO: Creating and evaling a Klass here may be too slow at startup. Consider
        // creating a closure, which will probably be slower at runtime.
        var source = "";
        var writer = new IndentingWriter(false, x => source += x + "\n");
        var emitter = new Emitter(writer, false, true, true);
        J2ME.emitKlass(emitter, classInfo);
        (1, eval)(source);
        // consoleWriter.writeLn("Synthesizing Klass: " + classInfo.className);
        // consoleWriter.writeLn(source);
        var mangledName = J2ME.C4.Backend.mangleClass(classInfo);
        klass = jsGlobal[mangledName];
        assert(klass, mangledName);
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

    extendKlass(klass, superKlass);
    registerRuntimeKlass(klass, classInfo);

    if (!classInfo.isInterface) {
      initializeInterfaces(klass, classInfo);
    }

    return klass;
  }

  export function linkKlass(classInfo: ClassInfo) {
    var mangledName = J2ME.C4.Backend.mangleClass(classInfo);
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
      }
    }

    linkingWriter && linkingWriter.writeLn("Link: " + classInfo.className + " -> " + klass);

    linkKlassMethods(classInfo.klass);
  }

  function findNativeMethodImplementation(methodInfo: MethodInfo) {
    var implKey = methodInfo.implKey;
    if (methodInfo.isNative) {
      if (implKey in Native) {
        return Native[implKey];
      } else {
        // Some Native MethodInfos are constructed but never called;
        // that's fine, unless we actually try to call them.
        return function missingImplementation() {
          assert (false, "Method " + methodInfo.name + " is native but does not have an implementation.");
        }
      }
    } else if (implKey in Override) {
      return Override[implKey];
    }
    return null;
  }

  function prepareInterpreterMethod(methodInfo: MethodInfo): Function {
    return function interpreter() {
      var frame = new Frame(methodInfo, [], 0);
      var ctx = $.ctx;
      var args = Array.prototype.slice.call(arguments);

      if (!methodInfo.isStatic) {
        args.unshift(this);
      }
      for (var i = 0; i < args.length; i++) {
        frame.setLocal(i, args[i]);
      }
      if (methodInfo.isSynchronized) {
        if (!frame.lockObject) {
          frame.lockObject = methodInfo.isStatic
            ? methodInfo.classInfo.getClassObject(ctx)
            : frame.getLocal(0);
        }

        ctx.monitorEnter(frame.lockObject);
      }
      return ctx.executeNewFrameSet([frame]);
    };
  }

  function findCompiledMethod(klass: Klass, methodInfo: MethodInfo): Function {
    if (methodInfo.isStatic) {
      return jsGlobal[methodInfo.mangledClassAndMethodName];
    } else {
      if (klass.prototype.hasOwnProperty(methodInfo.mangledName)) {
        return klass.prototype[methodInfo.mangledName];
      }
      return null;
    }
  }

  function linkKlassMethods(klass: Klass) {
    linkingWriter && linkingWriter.enter("Link Klass Methods: " + klass);
    var methods = klass.classInfo.methods;
    for (var i = 0; i < methods.length; i++) {
      var methodInfo = methods[i];
      var fn;
      var methodType;
      var nativeMethod = findNativeMethodImplementation(methods[i]);
      var methodDescription = methods[i].name + methods[i].signature;
      if (nativeMethod) {
        linkingWriter && linkingWriter.writeLn("Method: " + methodDescription + " -> Native / Override");
        fn = nativeMethod;
        methodType = MethodType.Native;
      } else {
        fn = findCompiledMethod(klass, methodInfo);
        if (fn) {
          linkingWriter && linkingWriter.greenLn("Method: " + methodDescription + " -> Compiled");
          methodType = MethodType.Compiled;
          if (!traceWriter) {
            linkingWriter && linkingWriter.outdent();
            continue;
          }
        } else {
          linkingWriter && linkingWriter.warnLn("Method: " + methodDescription + " -> Interpreter");
          methodType = MethodType.Interpreted;
          fn = prepareInterpreterMethod(methodInfo);
        }
      }

      if (traceWriter && methodType !== MethodType.Interpreted) {
        fn = tracingWrapper(fn, methodInfo, methodType);
      }

      // Link even non-static methods globally so they can be invoked statically via invokespecial.
      jsGlobal[methodInfo.mangledClassAndMethodName] = fn;
      if (!methodInfo.isStatic) {
        klass.prototype[methodInfo.mangledName] = fn;
      }
    }

    linkingWriter && linkingWriter.outdent();

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
        traceWriter.enter("> " + MethodType[methodType][0] + " " + methodInfo.classInfo.className + "/" + methodInfo.name + signatureToDefinition(methodInfo.signature, true, true) + printObj + " (" + printArgs + ")");
        var value = fn.apply(this, args);
        traceWriter.leave("< " + toDebugString(value));
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
    J2ME.Debug.assert(i === -1, i);
  }

  function initializeInterfaces(klass: Klass, classInfo: ClassInfo) {
    assert (!klass.interfaces);
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
        linkingWriter && linkingWriter.writeLn("Extending: " + klass + " -> " + superKlass);
        (<any>Object).setPrototypeOf(klass.prototype, superKlass.prototype);
        assert((<any>Object).getPrototypeOf(klass.prototype) === superKlass.prototype);
      } else {
        // TODO: Copy properties over.
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

  export function instanceOf(object: java.lang.Object, klass: Klass): boolean {
    return object === null && isAssignableTo(object.klass, klass);
  }

  export function instanceOfKlass(object: java.lang.Object, klass: Klass): boolean {
    return object === null && object.klass.display[klass.depth] === klass;
  }

  export function instanceOfInterface(object: java.lang.Object, klass: Klass): boolean {
    assert(klass.isInterfaceKlass);
    return object === null && object.klass.interfaces.indexOf(klass) >= 0;
  }

  export function checkCast(object: java.lang.Object, klass: Klass) {
    if (object !== null && !isAssignableTo(object.klass, klass)) {
      throw new TypeError();
    }
  }

  export function checkCastKlass(object: java.lang.Object, klass: Klass) {
    if (object !== null && object.klass.display[klass.depth] !== klass) {
      throw new TypeError();
    }
  }

  export function checkCastInterface(object: java.lang.Object, klass: Klass) {
    if (object !== null && object.klass.interfaces.indexOf(klass) < 0) {
      throw new TypeError();
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
      if (elementKlass === Klasses.java.lang.Object) {
        extendKlass(klass, Klasses.java.lang.Object);
        extendKlass(<ArrayKlass><any>Array, Klasses.java.lang.Object);
      } else {
        extendKlass(klass, getArrayKlass(Klasses.java.lang.Object));
      }
    } else {
      assert(!klass.prototype.hasOwnProperty("klass"));
      klass.prototype.klass = klass;
      extendKlass(klass, Klasses.java.lang.Object);
    }
    klass.isArrayKlass = true;
    klass.toString = function () {
      return "[Array of " + elementKlass + "]";
    };
    elementKlass.arrayKlass = klass;
    klass.elementKlass = elementKlass;
    var className = elementKlass.classInfo.className;
    if (!(elementKlass.classInfo instanceof PrimitiveClassInfo) && className[0] !== "[")
      className = "L" + className + ";";
    className = "[" + className;
    klass.classInfo = CLASSES.getClass(className);

    registerRuntimeKlass(klass, klass.classInfo);
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
    if (value.__hashCode__) {
      hashcode = " 0x" + value.__hashCode__.toString(16).toUpperCase();
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
    // ...
  }
}

var Runtime = J2ME.Runtime;


/**
 * Runtime exports for compiled code.
 */
var $IOK = J2ME.instanceOfKlass;
var $IOI = J2ME.instanceOfInterface;

var $CCK = J2ME.checkCastKlass;
var $CCI = J2ME.checkCastInterface;

var $AK = J2ME.getArrayKlass;
var $NA = J2ME.newArray;
var $S = J2ME.newString;
var $CDZ = J2ME.checkDivideByZero;
