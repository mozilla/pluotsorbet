/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

var CompiledMethodCache = (function() {
  var DEBUG = true;
  var DATABASE = "CompiledMethodCache";
  var VERSION = 1;
  var OBJECT_STORE = "methods";
  var KEY_PATH = "key";

  var database;
  var cache = new Map();

  function debug(message) {
    console.log("CompiledMethodCache " + message);
  }

  var upgrade = {
    "0to1": function(database, transaction, next) {
      database.createObjectStore(OBJECT_STORE, { keyPath: KEY_PATH });
      next();
    },
  };

  function restore() {
    return openDatabase.then(new Promise(function(resolve, reject) {
      DEBUG && debug("restore");

      var then = performance.now();
      var transaction = database.transaction(OBJECT_STORE, "readonly");
      var objectStore = transaction.objectStore(OBJECT_STORE);
      var request = objectStore.getAll();

      request.onerror = function() {
        console.error("Error restoring: " + request.error.name);
        reject(request.error.name);
      };

      request.onsuccess = function() {
        var count = request.result.length;
        for (var i = 0; i < count; i++) {
          cache.set(request.result[i][KEY_PATH], request.result[i]);
        }
        DEBUG && debug("restore complete: " + count + " methods in " + (performance.now() - then) + "ms");
        resolve();
      };
    }));
  }

  function clear() {
    return openDatabase.then(new Promise(function(resolve, reject) {
      DEBUG && debug("clear");

      // First clear the in-memory cache, in case we've already restored it
      // from the database.
      cache.clear();

      var then = performance.now();
      var transaction = database.transaction(OBJECT_STORE, "readwrite");
      var objectStore = transaction.objectStore(OBJECT_STORE);
      var request = objectStore.clear();

      request.onerror = function() {
        console.error("Error clearing: " + request.error.name);
        reject(request.error.name);
      };

      request.onsuccess = function() {
        DEBUG && debug("clear complete in " + (performance.now() - then) + "ms");
        resolve();
      };
    }));
  }

  var openDatabase = new Promise(function(resolve, reject) {
    DEBUG && debug("open");

    var request = indexedDB.open(DATABASE, VERSION);

    request.onerror = function() {
      console.error("error opening database: " + request.error.name);
      reject(request.error.name);
    };

    request.onupgradeneeded = function(event) {
      DEBUG && debug("upgrade needed from " + event.oldVersion + " to " + event.newVersion);

      var database = request.result;
      var transaction = request.transaction;

      var version = event.oldVersion;
      (function next() {
        if (version < event.newVersion) {
          DEBUG && debug("upgrade from " + version + " to " + (version + 1));
          upgrade[version + "to" + ++version](database, transaction, next);
        }
      })();
    };

    request.onsuccess = function() {
      DEBUG && debug("open success");

      database = request.result;

      var oldVersion = localStorage.getItem("lastAppVersion");
      if (config.version === oldVersion) {
        DEBUG && debug("app version " + config.version + " === " + oldVersion + "; restore");
        restore().catch(console.error.bind(console));
      } else {
        DEBUG && debug("app version " + config.version + " !== " + oldVersion + "; clear");
        clear().catch(console.error.bind(console));
        localStorage.setItem("lastAppVersion", config.version);
      }

      resolve();
    };
  });

  function get(key) {
    return cache.get(key);
  }

  function put(obj) {
    DEBUG && debug("put " + obj[KEY_PATH]);

    cache.set(obj[KEY_PATH], obj);

    return openDatabase.then(new Promise(function(resolve, reject) {
      var transaction = database.transaction(OBJECT_STORE, "readwrite");
      var objectStore = transaction.objectStore(OBJECT_STORE);

      var request = objectStore.put(obj);

      request.onerror = function() {
        console.error("Error putting " + key + ": " + request.error.name);
        reject(request.error.name);
      };

      request.onsuccess = function() {
        DEBUG && debug("put " + obj[KEY_PATH] + " complete");
        resolve();
      };
    }));
  }

  return {
    get: get,
    put: put,
    clear: clear,
  };

})();
