/**
 * Copyright 2014 Mozilla Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

declare function print(s: string);
declare function read(path: string, mode?: string);
declare function getBacktrace();

module J2ME.Shell {
  export class MicroTask {
    runAt: number;
    constructor(public id: number, public fn: () => any, public args: any[],
                public interval: number, public repeat: boolean) {
    }
  }

  var RealDate = Date;
  var fakeTime = 1428107694580; // 3-Apr-2015
  var jsGlobal = (function() { return this || (1, eval)('this//# sourceURL=jsGlobal-getter'); })();

  /**
   * This should only be called if you need fake time.
   */
  export function installTimeWarper() {

    // Go back in time.
    fakeTime = 1428107694580; // 3-Apr-2015

    // Overload
    jsGlobal.Date = function (yearOrTimevalue, month, date, hour, minute, second, millisecond) {
      switch (arguments.length) {
        case  0: return new RealDate(fakeTime); break;
        case  1: return new RealDate(yearOrTimevalue); break;
        case  2: return new RealDate(yearOrTimevalue, month); break;
        case  3: return new RealDate(yearOrTimevalue, month, date); break;
        case  4: return new RealDate(yearOrTimevalue, month, date, hour); break;
        case  5: return new RealDate(yearOrTimevalue, month, date, hour, minute); break;
        case  6: return new RealDate(yearOrTimevalue, month, date, hour, minute, second); break;
        default: return new RealDate(yearOrTimevalue, month, date, hour, minute, second, millisecond); break;
      }
    }

    // Make date now deterministic.
    jsGlobal.Date.now = function () {
      return fakeTime += 10; // Advance time.
    }
  }

  export class MicroTasksQueue {
    private tasks: MicroTask[] = [];
    private nextId: number = 1;
    private time: number = 1388556000000; // 1-Jan-2014
    private stopped: boolean = true;

    constructor() {
    }

    public get isEmpty(): boolean {
      return this.tasks.length === 0;
    }

    public scheduleInterval(fn: () => any, args: any[], interval: number, repeat: boolean) {
      var MIN_INTERVAL = 4;
      interval = Math.round((interval || 0)/10) * 10;
      if (interval < MIN_INTERVAL) {
        interval = MIN_INTERVAL;
      }
      var taskId = this.nextId++;
      var task = new MicroTask(taskId, fn, args, interval, repeat);
      this.enqueue(task);
      return task;
    }

    public enqueue(task: MicroTask) {
      var tasks = this.tasks;
      task.runAt = this.time + task.interval;
      var i = tasks.length;
      while (i > 0 && tasks[i - 1].runAt > task.runAt) {
        i--;
      }
      if (i === tasks.length) {
        tasks.push(task);
      } else {
        tasks.splice(i, 0, task);
      }
    }

    public dequeue(): MicroTask {
      var task = this.tasks.shift();
      this.time = task.runAt;
      return task;
    }

    public remove(id: number) {
      var tasks = this.tasks;
      for (var i = 0; i < tasks.length; i++) {
        if (tasks[i].id === id) {
          tasks.splice(i, 1);
          return;
        }
      }
    }

    public clear() {
      this.tasks.length = 0;
    }

    /**
     * Runs micro tasks for a certain |duration| and |count| whichever comes first. Optionally,
     * if the |clear| option is specified, the micro task queue is cleared even if not all the
     * tasks have been executed.
     *
     * If a |preCallback| function is specified, only continue execution if |preCallback()| returns true.
     */
    run(duration: number = 0, count: number = 0, clear: boolean = false, preCallback: Function = null) {
      this.stopped = false;
      var executedTasks = 0;
      var stopAt = Date.now() + duration;
      while (!this.isEmpty && !this.stopped) {
        if (duration > 0 && Date.now() >= stopAt) {
          break;
        }
        if (count > 0 && executedTasks >= count) {
          break;
        }
        var task = this.dequeue();
        if (preCallback && !preCallback(task)) {
          return;
        }
        task.fn.apply(null, task.args);
        print("" + this.isEmpty);
        executedTasks ++;
      }
      if (clear) {
        this.clear();
      }
      this.stopped = true;
    }

    stop() {
      this.stopped = true;
    }
  }
}

module J2ME.Shell {

  export class IndexedDB {
    open() {
      return {};
    }
  }

  export class HTMLCanvasElement {
    classList
  }

  export class Console {
    info(s: string) {
      print(s);
    }
    log(s: string) {
      print(s);
    }
    error(s: string) {
      print(s);
    }
  }

  export class Window {
    addEventListener = function (type) {
      print('Add Listener Ignored: ' + type);
    };
  }

  class DOMTokenList {
    length: number;
    contains(token: string): boolean {
      return false;
    }
    remove(token: string): void {

    }
    toggle(token: string): boolean {
      return false;
    }
    add(token: string): void {

    }
    item(index: number): string {
      return null;
    }
    toString(): string {
      return null;
    }
  }


  // document.documentElement.classList.add("debug-mode");

  export class Event {

  }

  class ClientRect {
    left: number;
    width: number;
    right: number;
    top: number;
    bottom: number;
    height: number;
  }

  export class HTMLElement {
    classList: DOMTokenList = new DOMTokenList();
    style = {};
    querySelector(name: string) {
      return new HTMLElement();
    }
    dispatchEvent(event: Event) {

    }
    getBoundingClientRect(): ClientRect {
      return new ClientRect();
    }
    addEventListener() {

    }
  }

  export class Canvas2DRenderingContext {
    constructor(public canvas: HTMLCanvas) {

    }
    save() {

    }
    restore() {

    }
  }

  export class HTMLCanvas extends HTMLElement {
    getContext() {
      return new Canvas2DRenderingContext(this);
    }
  }


  export class HTMLDocument extends HTMLElement {
    documentElement = new HTMLElement();
    getElementById(name: string) {
      switch (name) {
        case "canvas":
          return new HTMLCanvas();
        case "display":
        case "sidebar":
        case "drawer":
        case "up":
        case "down":
          return new HTMLElement();
        case "textarea-editor":
          return new HTMLElement();
        default:
          return new HTMLElement();
          // print(getBacktrace());
          // throw new Error('getElementById, ' + name);
      }
    }
    createElementNS(ns: string, qname: string) {
      switch (qname) {
        case "svg": return {
          createSVGMatrix: function () {
            return {a: 0, b: 0, c: 0, d: 0, e: 0, f: 0};
          }
        };
        case "a":
          return {};
        default:
          print(getBacktrace());
          throw new Error('createElementNS, ' + qname);
      }
    }
    createElement(name) {
      switch (name) {
        case "canvas":
          return new HTMLCanvas();
        case "textarea-editor":
          return new HTMLElement();
        default:
          print(getBacktrace());
          throw new Error('createElement, ' + name);
      }
    }
    location = {
      href: {
        resource: ""
      }
    };
  }
}

// XMLHttpRequest

this.XMLHttpRequest = function () {};
this.XMLHttpRequest.prototype = {
  open: function (method, url, async) {
    this.url = url;
    if (async === false) {
      throw new Error('Unsupported sync');
    }
  },
  send: function (data) {
    setTimeout(function () {
      try {
        console.log('XHR: ' + this.url);
        var response = this.responseType === 'arraybuffer' ?
          read(this.url, 'binary').buffer : read(this.url);
        if (this.responseType === 'json') {
          response = JSON.parse(response);
        }
        this.response = response;
        this.readyState = 4;
        this.status = 200;
        this.onreadystatechange && this.onreadystatechange();
        this.onload && this.onload();
      } catch (e) {
        this.error = e;
        this.readyState = 4;
        this.status = 404;
        this.onreadystatechange && this.onreadystatechange();
        this.onerror && this.onerror();
      }
    }.bind(this));
  }
};


// Polyfill for Promises
(function PromiseClosure() {
  /*jshint -W061 */
  var global = Function("return this")();
  if (global.Promise) {
    // Promises existing in the DOM/Worker, checking presence of all/resolve
    if (typeof global.Promise.all !== 'function') {
      global.Promise.all = function (iterable) {
        var count = 0, results = [], resolve, reject;
        var promise = new global.Promise(function (resolve_, reject_) {
          resolve = resolve_;
          reject = reject_;
        });
        iterable.forEach(function (p, i) {
          count++;
          p.then(function (result) {
            results[i] = result;
            count--;
            if (count === 0) {
              resolve(results);
            }
          }, reject);
        });
        if (count === 0) {
          resolve(results);
        }
        return promise;
      };
    }
    if (typeof global.Promise.resolve !== 'function') {
      global.Promise.resolve = function (x) {
        return new global.Promise(function (resolve) { resolve(x); });
      };
    }
    return;
  }

  function getDeferred(C) {
    if (typeof C !== 'function') {
      throw new TypeError('Invalid deferred constructor');
    }
    var resolver = createDeferredConstructionFunctions();
    var promise = new C(resolver);
    var resolve = resolver.resolve;
    if (typeof resolve !== 'function') {
      throw new TypeError('Invalid resolve construction function');
    }
    var reject = resolver.reject;
    if (typeof reject !== 'function') {
      throw new TypeError('Invalid reject construction function');
    }
    return {promise: promise, resolve: resolve, reject: reject};
  }

  function updateDeferredFromPotentialThenable(x, deferred) {
    if (typeof x !== 'object' || x === null) {
      return false;
    }
    try {
      var then = x.then;
      if (typeof then !== 'function') {
        return false;
      }
      var thenCallResult = then.call(x, deferred.resolve, deferred.reject);
    } catch (e) {
      var reject = deferred.reject;
      reject(e);
    }
    return true;
  }

  function isPromise(x) {
    return typeof x === 'object' && x !== null &&
      typeof x.promiseStatus !== 'undefined';
  }

  function rejectPromise(promise, reason) {
    if (promise.promiseStatus !== 'unresolved') {
      return;
    }
    var reactions = promise.rejectReactions;
    promise.result = reason;
    promise.resolveReactions = undefined;
    promise.rejectReactions = undefined;
    promise.promiseStatus = 'has-rejection';
    triggerPromiseReactions(reactions, reason);
  }

  function resolvePromise(promise, resolution) {
    if (promise.promiseStatus !== 'unresolved') {
      return;
    }
    var reactions = promise.resolveReactions;
    promise.result = resolution;
    promise.resolveReactions = undefined;
    promise.rejectReactions = undefined;
    promise.promiseStatus = 'has-resolution';
    triggerPromiseReactions(reactions, resolution);
  }

  function triggerPromiseReactions(reactions, argument) {
    for (var i = 0; i < reactions.length; i++) {
      queueMicrotask({reaction: reactions[i], argument: argument});
    }
  }

  function queueMicrotask(task) {
    if (microtasksQueue.length === 0) {
      setTimeout(handleMicrotasksQueue, 0);
    }
    microtasksQueue.push(task);
  }

  function executePromiseReaction(reaction, argument) {
    var deferred = reaction.deferred;
    var handler = reaction.handler;
    var handlerResult, updateResult;
    try {
      handlerResult = handler(argument);
    } catch (e) {
      var reject = deferred.reject;
      return reject(e);
    }

    if (handlerResult === deferred.promise) {
      var reject = deferred.reject;
      return reject(new TypeError('Self resolution'));
    }

    try {
      updateResult = updateDeferredFromPotentialThenable(handlerResult,
        deferred);
      if (!updateResult) {
        var resolve = deferred.resolve;
        return resolve(handlerResult);
      }
    } catch (e) {
      var reject = deferred.reject;
      return reject(e);
    }
  }

  var microtasksQueue = [];

  function handleMicrotasksQueue() {
    while (microtasksQueue.length > 0) {
      var task = microtasksQueue[0];
      try {
        executePromiseReaction(task.reaction, task.argument);
      } catch (e) {
        // unhandler onFulfillment/onRejection exception
        if (typeof (<any>Promise).onerror === 'function') {
          (<any>Promise).onerror(e);
        }
      }
      microtasksQueue.shift();
    }
  }

  function throwerFunction(e) {
    throw e;
  }

  function identityFunction(x) {
    return x;
  }

  function createRejectPromiseFunction(promise) {
    return function (reason) {
      rejectPromise(promise, reason);
    };
  }

  function createResolvePromiseFunction(promise) {
    return function (resolution) {
      resolvePromise(promise, resolution);
    };
  }

  function createDeferredConstructionFunctions(): any {
    var fn: any = function (resolve, reject) {
      fn.resolve = resolve;
      fn.reject = reject;
    };
    return fn;
  }

  function createPromiseResolutionHandlerFunctions(promise,
                                                   fulfillmentHandler, rejectionHandler) {
    return function (x) {
      if (x === promise) {
        return rejectionHandler(new TypeError('Self resolution'));
      }
      var cstr = promise.promiseConstructor;
      if (isPromise(x)) {
        var xConstructor = x.promiseConstructor;
        if (xConstructor === cstr) {
          return x.then(fulfillmentHandler, rejectionHandler);
        }
      }
      var deferred = getDeferred(cstr);
      var updateResult = updateDeferredFromPotentialThenable(x, deferred);
      if (updateResult) {
        var deferredPromise = deferred.promise;
        return deferredPromise.then(fulfillmentHandler, rejectionHandler);
      }
      return fulfillmentHandler(x);
    };
  }

  function createPromiseAllCountdownFunction(index, values, deferred,
                                             countdownHolder) {
    return function (x) {
      values[index] = x;
      countdownHolder.countdown--;
      if (countdownHolder.countdown === 0) {
        deferred.resolve(values);
      }
    };
  }

  function Promise(resolver) {
    if (typeof resolver !== 'function') {
      throw new TypeError('resolver is not a function');
    }
    var promise = this;
    if (typeof promise !== 'object') {
      throw new TypeError('Promise to initialize is not an object');
    }
    promise.promiseStatus = 'unresolved';
    promise.resolveReactions = [];
    promise.rejectReactions = [];
    promise.result = undefined;

    var resolve = createResolvePromiseFunction(promise);
    var reject = createRejectPromiseFunction(promise);

    try {
      var result = resolver(resolve, reject);
    } catch (e) {
      rejectPromise(promise, e);
    }

    promise.promiseConstructor = Promise;
    return promise;
  }

  (<any>Promise).all = function (iterable) {
    var deferred = getDeferred(this);
    var values = [];
    var countdownHolder = {countdown: 0};
    var index = 0;
    iterable.forEach(function (nextValue) {
      var nextPromise = this.cast(nextValue);
      var fn = createPromiseAllCountdownFunction(index, values,
        deferred, countdownHolder);
      nextPromise.then(fn, deferred.reject);
      index++;
      countdownHolder.countdown++;
    }, this);
    if (index === 0) {
      deferred.resolve(values);
    }
    return deferred.promise;
  };
  (<any>Promise).cast = function (x) {
    if (isPromise(x)) {
      return x;
    }
    var deferred = getDeferred(this);
    deferred.resolve(x);
    return deferred.promise;
  };
  (<any>Promise).reject = function (r) {
    var deferred = getDeferred(this);
    var rejectResult = deferred.reject(r);
    return deferred.promise;
  };
  (<any>Promise).resolve = function (x) {
    var deferred = getDeferred(this);
    var rejectResult = deferred.resolve(x);
    return deferred.promise;
  };
  Promise.prototype = {
    'catch': function (onRejected) {
      this.then(undefined, onRejected);
    },
    then: function (onFulfilled, onRejected) {
      var promise = this;
      if (!isPromise(promise)) {
        throw new TypeError('this is not a Promises');
      }
      var cstr = promise.promiseConstructor;
      var deferred = getDeferred(cstr);

      var rejectionHandler = typeof onRejected === 'function' ? onRejected :
        throwerFunction;
      var fulfillmentHandler = typeof onFulfilled === 'function' ? onFulfilled :
        identityFunction;
      var resolutionHandler = createPromiseResolutionHandlerFunctions(promise,
        fulfillmentHandler, rejectionHandler);

      var resolveReaction = {deferred: deferred, handler: resolutionHandler};
      var rejectReaction = {deferred: deferred, handler: rejectionHandler};

      switch (promise.promiseStatus) {
        case 'unresolved':
          promise.resolveReactions.push(resolveReaction);
          promise.rejectReactions.push(rejectReaction);
          break;
        case 'has-resolution':
          var resolution = promise.result;
          queueMicrotask({reaction: resolveReaction, argument: resolution});
          break;
        case 'has-rejection':
          var rejection = promise.result;
          queueMicrotask({reaction: rejectReaction, argument: rejection});
          break;
      }
      return deferred.promise;
    }
  };

  global.Promise = Promise;
})();

this.HTMLCanvasElement = J2ME.Shell.HTMLCanvasElement;
this.window = new J2ME.Shell.Window();
this.console = this.window.console = new J2ME.Shell.Console();
this.document = this.window.document = new J2ME.Shell.HTMLDocument();
this.Event = J2ME.Shell.Event
this.indexedDB = new J2ME.Shell.IndexedDB();

// Global micro task queue.
var microTaskQueue = new J2ME.Shell.MicroTasksQueue();

var defaultTimerArgs = [];

this.setTimeout = function (fn, interval) {
  print("setTimeout: " + fn + " " + interval);
  // print(getBacktrace());
  var args = arguments.length > 2 ? Array.prototype.slice.call(arguments, 2) : defaultTimerArgs;
  var task = microTaskQueue.scheduleInterval(fn, args, interval, false);
  return task.id;
};

this.setZeroTimeout = function (fn, interval) {
  return this.setTimeout(fn, 0);
}

this.setInterval = function (fn, interval) {
  var args = arguments.length > 2 ? Array.prototype.slice.call(arguments, 2) : defaultTimerArgs;
  var task = microTaskQueue.scheduleInterval(fn, args, interval, true);
  return task.id;
};

this.clearTimeout = function (id) {
  microTaskQueue.remove(id);
};

this.clearInterval = clearTimeout;

this.navigator = {
  userAgent: 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10.6; rv:4.0) Gecko/20100101 Firefox/4.0'
};