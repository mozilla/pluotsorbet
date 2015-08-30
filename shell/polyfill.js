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

if (typeof console === "undefined") {
  var console = {
    log: print,
  }
}

console.info = function (c) {
  putstr(String.fromCharCode(c));
};

console.error = function (c) {
  putstr(String.fromCharCode(c));
};

var performance = {
  now: function () {
    return dateNow();
  }
};

function check() {

}

var navigator = {
  language: "en-US",
  userAgent: "jsshell",
};

function Image() {}

function alert() {}

var document = {
  documentElement: {
    classList: {
      add: function() {
      },
    },
  },
  querySelector: function() {
    return {
      addEventListener: function() {
      },
    };
  },
  getElementById: function() {
    return {
      addEventListener: function() {
      },
      getContext: function() {
        return {
          save: function() {
          },
        };
      },
      getBoundingClientRect: function() {
        return { top: 0, left: 0, width: 0, height: 0 };
      },
      querySelector: function() {
        return { style: "" };
      },
      dispatchEvent: function(event) {
      },
      style: "",
      removeChild: function(elem) {
      }
    };
  },
  addEventListener: function() {
  },
  createElementNS: function() {
    return {}
  },
};

var microTaskQueue = null;

var window = {
  addEventListener: function() {
  },
  crypto: {
    getRandomValues: function() {
    },
  },
  document: document,
  console: console,
};
window.parent = window;

this.nextTickBeforeEvents = window.nextTickBeforeEvents =
  this.nextTickDuringEvents = window.nextTickDuringEvents =
    this.setTimeout = window.setTimeout = function (fn, interval) {
      var args = arguments.length > 2 ? Array.prototype.slice.call(arguments, 2) : [];
      var task = microTaskQueue.scheduleInterval(fn, args, interval, false);
      return task.id;
    };

window.setInterval = function (fn, interval) {
  var args = arguments.length > 2 ? Array.prototype.slice.call(arguments, 2) : [];
  var task = microTaskQueue.scheduleInterval(fn, args, interval, true);
  return task.id;
};
window.clearTimeout = function (id) {
  microTaskQueue.remove(id);
};

var Event = function() {
};

var DumbPipe = {
  open: function() {
  },
};
