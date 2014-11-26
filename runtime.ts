var $: J2ME.Runtime; // The currently-executing runtime.

var runtimeTemplate = {};

module J2ME {

  export var Klasses = {
    java: {
      lang: {
        Object: null,
        Class: null,
        String: null,
        Thread: null
      }
    },
    long: null
  };

  function Int64Array(size: number) {
    var array = Array(size);
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

  export var consoleWriter = new IndentingWriter();

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
     * Depth in the class hierarchy.
     */
    depth: number;

    /**
     * Initializes static fields to their default values, not all klasses have one.
     */
    staticInitializer: () => void;

    /**
     * Static constructor, not all klasses have one.
     */
    staticConstructor: () => void;

    /**
     * Java class object. This is only available on runtime klasses.
     */
    classObject: java.lang.Class;

    /**
     * Wether this class is a runtime class.
     */
    isRuntimeKlass: boolean;
  }

  export interface ArrayKlass extends Klass {
    elementKlass: Klass;
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
      $_mainClass: java.lang.String;
      $_mainArgs: java.lang.String [];
    }
  }

  function initializeClassObject(klass: Klass) {
    assert(klass.isRuntimeKlass, "Can only create class objects for runtime klasses.");
    assert(!klass.classObject);
    klass.classObject = <java.lang.Class>newObject(Klasses.java.lang.Class);
    klass.classObject.runtimeKlass = klass;
  }

  /**
   * Called by compiled code to initialize the klass. Klass initializers are reflected as
   * memoizing getters on the |RuntimeTemplate.prototype|. Once they are first accessed,
   * concrete klasses are created.
   */
  export function registerKlass(klass: Klass, mangledClassName: string, className: string) {
    // Ensure each Runtime instance receives its own copy of the class
    // constructor, hoisted off the current runtime.
    Object.defineProperty(RuntimeTemplate.prototype, mangledClassName, {
      configurable: true,
      get: function () {
        assert(!klass.isRuntimeKlass);
        var runtimeKlass = klass.bind(null);
        runtimeKlass.klass = klass;
        runtimeKlass.isRuntimeKlass = true;
        initializeClassObject(runtimeKlass);
        var classInfo = CLASSES.getClass(className);
        Object.defineProperty(this, mangledClassName, {
          configurable: false,
          value: runtimeKlass
        });
        // TODO: monitorEnter
        if (klass.staticInitializer) {
          klass.staticInitializer.call(runtimeKlass);
        }
        if (klass.staticConstructor) {
          klass.staticConstructor.call(runtimeKlass);
        }
        return runtimeKlass;
      }
    });
  }
  export function runtimeKlass(runtime: Runtime, klass: Klass): Klass {
    assert(!klass.isRuntimeKlass);
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
    var klass;
    if (classInfo.isInterface) {
      klass = <Klass><any> new Function();
    } else if (classInfo.isArrayClass) {
      var elementKlass = getKlass(classInfo.elementClass);
      // Have we already created one? We need to maintain pointer identity.
      if (elementKlass.arrayKlass) {
        return elementKlass.arrayKlass;
      }
      klass = getArrayConstructor(classInfo.elementClass.className);
      if (!klass) {
        klass = <Klass><any> function (size: number) {
          var array = new Array(size);
          (<any>array).klass = klass;
          return array;
        };
      } else {
        assert(!klass.prototype.hasOwnProperty("klass"));
        klass.prototype.klass = klass;
      }
      klass.toString = function () {
        return "[Array of " + elementKlass + "]";
      };
      elementKlass.arrayKlass = klass;
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

    var mangledClassName = J2ME.C4.Backend.mangleClass(classInfo);
    var superKlass = getKlass(classInfo.superClass);
    extendKlass(klass, superKlass);
    registerKlass(klass, mangledClassName, classInfo.className);
    return klass;
  }

  export function linkKlass(classInfo: ClassInfo) {
    var mangledName = J2ME.C4.Backend.mangleClass(classInfo);
    var klass = jsGlobal[mangledName];
    if (klass) {
      release || assert(klass, "Cannot find klass for " + classInfo.className);
      release || assert(!classInfo.klass);
      classInfo.klass = klass;
      klass.toString = function () {
        return "[Compiled Klass " + classInfo.className + "]";
      };
    } else {
      classInfo.klass = klass = getKlass(classInfo);
    }
    classInfo.klass.classInfo = classInfo;
    switch (classInfo.className) {
      case "java/lang/Object": Klasses.java.lang.Object = klass; break;
      case "java/lang/Class": Klasses.java.lang.Class = klass; break;
      case "java/lang/String": Klasses.java.lang.String = klass; break;
      case "java/lang/Thread": Klasses.java.lang.Thread = klass; break;
    }

    consoleWriter.writeLn("Link: " + classInfo.className + " -> " + klass);
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

  export function extendKlass(klass: Klass, superKlass: Klass) {
    klass.superKlass = superKlass;
    if (superKlass) {
      klass.prototype = Object.create(superKlass.prototype);
    }
    klass.prototype.klass = klass;
    initializeKlassTables(klass);
  }

  export function isAssignableTo(from: Klass, to: Klass): boolean {
    // TODO: This needs to deal with arrays an interfaces too.
    return from.display[to.depth] === to;
  }

  export function instanceOf(object: java.lang.Object, klass: Klass): boolean {
    return false; // TODO: Generic
  }

  export function instanceOfKlass(object: java.lang.Object, klass: Klass): boolean {
    return object.klass.display[klass.depth] === klass;
  }

  export function instanceOfInterface(object: java.lang.Object, klass: Klass): boolean {
    return object.klass.display[klass.depth] === klass;
  }

  export function checkCast(object: java.lang.Object, klass: Klass) {
    return false; // TODO: Generic
  }

  export function checkCastKlass(object: java.lang.Object, klass: Klass) {
    if (!instanceOfKlass(object, klass)) {
      throw new TypeError();
    }
  }

  export function checkCastInterface(object: java.lang.Object, klass: Klass) {

  }

  export function newObject(klass: Klass): java.lang.Object {
    return new klass();
  }

  export function newArray(klass: Klass, size: number) {
    var constructor: any = getArrayKlass(klass);
    return new constructor(size);
  }

  export function getArrayKlass(klass: Klass): Klass {
    if (klass.arrayKlass) {
      return klass.arrayKlass;
    }
    var arrayKlass = klass.arrayKlass = <ArrayKlass><any>function (size: number) {
      var array = new Array(size);
      (<any>array).klass = arrayKlass;
      return array;
    };
    arrayKlass.elementKlass = klass;
    arrayKlass.superKlass = Klasses.java.lang.Object;
    arrayKlass.toString = function () {
      return "[Array of " + klass + "]";
    };
    initializeKlassTables(arrayKlass);
    return arrayKlass;
  }

  export function toDebugString(value: any): string {
    if (typeof value !== "object") {
      return String(value);
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
    return "[" + value.klass.classInfo.className + " 0x" + value.__hashCode__.toString(16).toUpperCase() + "]";
  }

  export function fromJavaString(value: java.lang.String): string {
    if (!value) {
      return null;
    }
    return value.str;
  }
}

var Runtime = J2ME.Runtime;


/**
 * Runtime exports for compiled code.
 */
var $EK = J2ME.extendKlass;
var $RK = J2ME.registerKlass;

var $IOK = J2ME.instanceOfKlass;
var $IOI = J2ME.instanceOfInterface;

var $CCK = J2ME.checkCastKlass;
var $CCI = J2ME.checkCastInterface;

var $AK = J2ME.getArrayKlass;
var $NA = J2ME.newArray;