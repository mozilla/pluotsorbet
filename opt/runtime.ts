var $: Runtime; // The currently-executing runtime.

var runtimeTemplate = {};

declare var internedStrings: Map<string, string>;
declare var util;

interface JVM {

}

interface Context {

}

/**
 * This class is abstract and should never be initialized. It only acts as a template for
 * actual runtime objects.
 */
class RuntimeTemplate {
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
    waiting.forEach(function(callback) {
      try {
        callback();
      } catch(ex) {
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

class Runtime extends RuntimeTemplate {
  constructor(jvm: JVM) {
    super(jvm);
  }
}

class Class {
  constructor(public klass: Klass) {
    // ...
  }
}

/**
 * Representation of a template class.
 */
interface Klass extends Function {
  superKlass: Klass;

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
  class: Class
}

interface Object {
  klass: Klass
}

declare var CLASSES;

/**
 * Called by compiled code to initialize the klass. Klass initializers are reflected as
 * memoizing getters on the |RuntimeTemplate.prototype|. Once they are first accessed,
 * concrete klasses are created.
 */
function registerKlass(klass: Klass, mangledClassName: string, className: string) {
  // Ensure each Runtime instance receives its own copy of the class
  // constructor, hoisted off the current runtime.
  Object.defineProperty(RuntimeTemplate.prototype, mangledClassName, {
    configurable: true,
    get: function() {
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

var $EK = function extendKlass(klass: Klass, superKlass: Klass) {
  klass.superKlass = superKlass;
  if (superKlass) {
    klass.prototype = Object.create(superKlass.prototype);
  }
  klass.prototype.klass = klass;
  initializeKlassTables(klass);
};

function instanceOfKlass(object: Object, klass: Klass) {
  return object.klass.display[klass.depth] === klass;
}

function instanceOfInterface(object: Object, klass: Klass) {
  return object.klass.display[klass.depth] === klass;
}

function instanceOfArray(object: Object, klass: Klass) {
  return object.klass.display[klass.depth] === klass;
}

function checkCastKlass(object: Object, klass: Klass) {
  if (!instanceOfKlass(object, klass)) {
    throw new TypeError();
  }
}

function checkCastInterface(object: Object, klass: Klass) {

}

function checkCastArray(object: Object, klass: Klass) {

}

/**
 * Runtime exports for compiled code.
 */
var $RK = registerKlass;

var $IOK = instanceOfKlass;
var $IOI = instanceOfInterface;
var $IOA = instanceOfArray;

var $CCK = checkCastKlass;
var $CCI = checkCastInterface;
var $CCA = checkCastArray;