/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

var JARStore = (function() {
  var DATABASE = "JARStore";
  var VERSION = 2;
  var OBJECT_STORE_OLD = "files";
  var OBJECT_STORE_WITH_UNCOMPRESSED_LEN = "files_v2"
  var KEY_PATH = "jarName";

  var database;
  var jars = new Map();
  var jad;

  var upgrade = {
    "0to1": function(database, transaction, next) {
      database.createObjectStore(OBJECT_STORE_OLD, { keyPath: KEY_PATH });
      next();
    },
    "1to2": function(database, transaction, next) {
      database.deleteObjectStore(OBJECT_STORE_OLD);
      // We don't migrate data from the old format to the new one, but
      // rely on JARStore users to recreate the needed data.
      database.createObjectStore(OBJECT_STORE_WITH_UNCOMPRESSED_LEN, { keyPath: KEY_PATH });
      next();
    },
  };

  var openDatabase = new Promise(function(resolve, reject) {
    var request = indexedDB.open(DATABASE, VERSION);

    request.onerror = function() {
      console.error("error opening database: " + request.error.name);
      reject(request.error.name);
    };

    request.onupgradeneeded = function(event) {
      var database = request.result;
      var transaction = request.transaction;

      var version = event.oldVersion;
      (function next() {
        if (version < event.newVersion) {
          upgrade[version + "to" + ++version](database, transaction, next);
        }
      })();
    };

    request.onsuccess = function() {
      database = request.result;
      resolve();
    };
  });

  function addBuiltIn(jarName, jarData) {
    var zip = new ZipFile(jarData, false);

    jars.set(jarName, {
      directory: zip.directory,
      isBuiltIn: true,
    });
  }

  function installJAR(jarName, jarData, jadData) {
    return openDatabase.then(function() {
      return new Promise(function(resolve, reject) {
        var zip = new ZipFile(jarData, true);

        var transaction = database.transaction(OBJECT_STORE_WITH_UNCOMPRESSED_LEN, "readwrite");
        var objectStore = transaction.objectStore(OBJECT_STORE_WITH_UNCOMPRESSED_LEN);
        var request = objectStore.put({
          jarName: jarName,
          jar: zip.directory,
          jad: jadData || null,
        });

        request.onerror = function() {
          console.error("Error installing " + jarName + ": " + request.error.name);
          reject(request.error.name);
        };

        transaction.oncomplete = function() {
          jars.set(jarName, {
            directory: zip.directory,
            isBuiltIn: false,
          });
          resolve();
        };
      });
    });
  }

  function loadJAR(jarName) {
    return openDatabase.then(function() {
      return new Promise(function(resolve, reject) {
        var transaction = database.transaction(OBJECT_STORE_WITH_UNCOMPRESSED_LEN, "readonly");
        var objectStore = transaction.objectStore(OBJECT_STORE_WITH_UNCOMPRESSED_LEN);
        var request = objectStore.get(jarName);

        request.onerror = function() {
          console.error("Error loading " + jarName + ": " + request.error.name);
          reject(request.error.name);
        };

        transaction.oncomplete = function() {
          if (request.result) {
            jars.set(jarName, {
              directory: request.result.jar,
              isBuiltIn: false,
            });

            if (request.result.jad) {
              jad = request.result.jad;
            }

            resolve(true);
          } else {
            resolve(false);
          }
        };
      });
    });
  }

  function loadFileFromJAR(jarName, fileName) {
    var jar = jars.get(jarName);
    if (!jar) {
      return null;
    }

    var entry = jar.directory[fileName];

    if (!entry) {
      return null;
    }

    var bytes;
    if (entry.compression_method === 0) {
      bytes = entry.compressed_data;
    } else if (entry.compression_method === 8) {
      bytes = inflate(entry.compressed_data, entry.uncompressed_len);
    } else {
      return null;
    }

    if (!jar.isBuiltIn && fileName.endsWith(".class")) {
      // Classes are loaded just once and then are cached in ClassRegistry::classes
      delete jar.directory[fileName];
    }

    return bytes;
  }

  function loadFile(fileName) {
    for (var jarName of jars.keys()) {
      var data = loadFileFromJAR(jarName, fileName);
      if (data) {
        return data;
      }
    }
  }

  function getJAD() {
    return jad;
  }

  function clear() {
    return openDatabase.then(function() {
      return new Promise(function(resolve, reject) {
        jars.clear();

        var transaction = database.transaction(OBJECT_STORE_WITH_UNCOMPRESSED_LEN, "readwrite");
        var objectStore = transaction.objectStore(OBJECT_STORE_WITH_UNCOMPRESSED_LEN);
        var request = objectStore.clear();

        request.onerror = function() {
          console.error("Error clearing: " + request.error.name);
          reject(request.error.name);
        };

        request.onsuccess = function() {
          resolve();
        };
      });
    });
  }

  return {
    addBuiltIn: addBuiltIn,
    installJAR: installJAR,
    loadJAR: loadJAR,
    loadFileFromJAR: loadFileFromJAR,
    loadFile: loadFile,
    getJAD: getJAD,
    clear: clear,
  };
})();

if (typeof module === 'object') {
  module.exports.JARStore = JARStore;
}
