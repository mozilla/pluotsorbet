var $: J2ME.Runtime; // The currently-executing runtime.

var runtimeTemplate = {};

module J2ME {
  export enum ExecutionMode {
    Runtime = 0,
    Compiler = 1
  }

  export var mode = ExecutionMode.Runtime;

  declare var internedStrings: Map<string, string>;
  declare var util;

  import assert = J2ME.Debug.assert;

  export interface JVM {sub

  }

  export interface Context {

  }

  /**
   * This class is abstract and should never be initialized. It only acts as a template for
   * actual runtime objects.
   */
  export class RuntimeTemplate {
    static all = new Set();
    vm: JVM;
    status: number;
    waiting: any [];
    threadCount: number;
    initialized: any;
    pending: any;
    staticFields: any;
    classObjects: any;
    ctx: Context;

    constructor(vm: JVM) {
      this.vm = vm;
      this.status = 1; // NEW
      this.waiting = [];
      this.threadCount = 0;
      this.initialized = {};
      this.pending = {};
      this.staticFields = {};
      this.classObjects = {};
      this.ctx = null;
    }

    waitStatus(callback) {
      this.waiting.push(callback);
    }

    updateStatus(status) {
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
        this.updateStatus(4); // STOPPED
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
    superKlass: Klass;

    /**
     * Would be nice to remove this.
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
    class: java.lang.Class
  }

  export interface ArrayKlass extends Klass {
    elementKlass: Klass;
  }

  export module java.lang {
    export interface Object {
      /**
       * Reference to the runtime klass.
       */
      klass: Klass

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

    }

    export interface String extends java.lang.Object {

    }
  }

  declare var CLASSES;

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
        var runtimeKlass = klass.bind(null);
        runtimeKlass.klass = klass;
        runtimeKlass.class = new Class(runtimeKlass);
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

  export function createKlass(classInfo: ClassInfo): Klass {
    if (!classInfo) {
      return null;
    }
    if (classInfo.klass) {
      return classInfo.klass;
    }
    // TODO: This should initialize the instance fields.
    var klass = null;
    var ww = new IndentingWriter();

    ww.writeLn(classInfo.className);
    if (classInfo.isInterface) {
      klass = <Klass><any> function () {};
    } else {
      var s = "";
      var writer = new IndentingWriter(false, x => s += x);
      var emitter = new Emitter(writer, false, true, true);

      J2ME.emitKlass(emitter, classInfo);
      (1, eval)(s);
      ww.writeLn("C: " + classInfo.className);
      if (classInfo.superClass) {
        ww.writeLn("S: " + classInfo.superClass.className);
      }
      ww.writeLn(s);
      ww.writeLn("");
      var mangledName = J2ME.C4.Backend.mangleClass(classInfo);
      klass = jsGlobal[mangledName];
      assert(klass, mangledName);
    }

    var mangledClassName = J2ME.C4.Backend.mangleClass(classInfo);
    var superKlass = createKlass(classInfo.superClass);
    extendKlass(klass, superKlass);
    registerKlass(klass, mangledClassName, classInfo.className);
    return klass;
  }

  export function linkKlass(classInfo: ClassInfo) {
    if (mode !== ExecutionMode.Runtime) {
      return;
    }
    var mangledName = J2ME.C4.Backend.mangleClass(classInfo);
    var klass = jsGlobal[mangledName];
    if (klass) {
      release || assert(klass, "Cannot find klass for " + classInfo.className);
      release || assert(!classInfo.klass);
      classInfo.klass = klass;
    } else {
      classInfo.klass = createKlass(classInfo);
    }
    classInfo.klass.classInfo = classInfo;
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

  export function instanceOfArray(object: java.lang.Object, klass: Klass): boolean {
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

  export function checkCastArray(object: java.lang.Object, klass: Klass) {

  }
}

/**
 * Runtime exports for compiled code.
 */
var $EK = J2ME.extendKlass;
var $RK = J2ME.registerKlass;

var $IOK = J2ME.instanceOfKlass;
var $IOI = J2ME.instanceOfInterface;
var $IOA = J2ME.instanceOfArray;

var $CCK = J2ME.checkCastKlass;
var $CCI = J2ME.checkCastInterface;
var $CCA = J2ME.checkCastArray;