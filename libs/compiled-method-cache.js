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
    return new Promise(function(resolve, reject) {
      DEBUG && debug("restore");

      var transaction = database.transaction(OBJECT_STORE, "readonly");
      var objectStore = transaction.objectStore(OBJECT_STORE);

      var request = objectStore.mozGetAll();

      request.onerror = function() {
        console.error("Error restoring: " + request.error.name);
        reject(request.error.name);
      };

      request.onsuccess = function() {
        for (var i = 0; i < request.result.length; i++) {
          cache.set(request.result[i][KEY_PATH], request.result[i]);
        }
      };

      transaction.oncomplete = function() {
        DEBUG && debug("restore complete");
      };
    });
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
      restore();
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
        resolve();
      };

      transaction.oncomplete = function() {
        DEBUG && debug("put " + obj[KEY_PATH] + " complete");
      };
    }));
  }

  function clear() {
    DEBUG && debug("clear");

    cache.clear();

    return openDatabase.then(new Promise(function(resolve, reject) {
      var transaction = database.transaction(OBJECT_STORE, "readwrite");
      var objectStore = transaction.objectStore(OBJECT_STORE);

      var request = objectStore.clear();

      request.onerror = function() {
        console.error("Error clearing store: " + request.error.name);
        reject(request.error.name);
      };

      request.onsuccess = function() {
        resolve();
      };

      transaction.oncomplete = function() {
        DEBUG && debug("clear complete");
      };
    }));
  }

  return {
    get: get,
    put: put,
    clear: clear,
  };

})();
