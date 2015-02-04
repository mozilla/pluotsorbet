/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

var CompiledMethodCache = (function() {
  var DEBUG = false;

  function debug(message) {
    console.log("CompiledMethodCache " + message);
  }

  function clear() {
    DEBUG && debug("clear");
    var then = DEBUG ? performance.now() : null;

    var keys = [];
    for (var i = 0; i < localStorage.length; ++i) {
      var key = localStorage.key(i);
      if (key.indexOf("compiledMethod:") === 0) {
        keys.push(key);
      }
    }
    keys.forEach(function(key) { localStorage.removeItem(key) });

    DEBUG && debug("cleared in " + (performance.now() - then) + "ms");
  }

  var oldVersion = localStorage.getItem("lastAppVersion");
  if (config.version !== oldVersion) {
    DEBUG && debug("app version " + config.version + " !== " + oldVersion + "; clear");
    clear();
    localStorage.setItem("lastAppVersion", config.version);
  }

  function get(key) {
    DEBUG && debug("get " + key);
    var value = localStorage.getItem("compiledMethod:" + key);
    return value ? JSON.parse(value) : null;
  }

  function put(key, value) {
    DEBUG && debug("put " + key);
    localStorage.setItem("compiledMethod:" + key, JSON.stringify(value));
  }

  return {
    get: get,
    put: put,
    clear: clear,
  };

})();
