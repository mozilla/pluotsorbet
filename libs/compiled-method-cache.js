/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

var CompiledMethodCache = (function() {
  var DEBUG = false;
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
    var elem = cache.get(key);
    if (elem) {
      cache.delete(key);
    }
    return elem;
  }

  var recordsToFlush = [];
  var flushTimer = null;

  function flush() {
    flushTimer = null;
    openDatabase.then(function() {
      var then = performance.now();
      var transaction = database.transaction(OBJECT_STORE, "readwrite");
      var objectStore = transaction.objectStore(OBJECT_STORE);
      var numRecords = recordsToFlush.length;
      for (var i = 0; i < numRecords; i++) {
        var request = objectStore.put(recordsToFlush[i]);
      }
      recordsToFlush = [];
      DEBUG && (transaction.oncomplete = function(event) {
        debug("flushed " + numRecords + " in " + (performance.now() - then) + "ms");
      });
      transaction.onerror = function(event) {
        console.error("error flushing " + event.target.name);
      };
    });
  }

  function put(obj) {
    DEBUG && debug("put " + obj[KEY_PATH]);
    recordsToFlush.push(obj);
    if (!flushTimer) {
      flushTimer = setTimeout(flush, 3000 /* ms; 3 seconds */);
    }
  }

  function deleteDatabase() {
    return new Promise(function(resolve, reject) {
      database = null;
      var request = indexedDB.deleteDatabase(DATABASE);
      request.onsuccess = resolve;
      request.onerror = function() { reject(request.error.name) };
    });
  }

  return {
    get: get,
    put: put,
    clear: clear,
    deleteDatabase: deleteDatabase,
  };

})();
