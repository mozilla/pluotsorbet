'use strict';

/**
 * This module defines an asynchronous version of the localStorage API, backed by
 * an IndexedDB database.  It creates a global asyncStorage object that has
 * methods like the localStorage object.
 *
 * To store a value use setItem:
 *
 *   asyncStorage.setItem('key', 'value');
 *
 * If you want confirmation that the value has been stored, pass a callback
 * function as the third argument:
 *
 *  asyncStorage.setItem('key', 'newvalue', function() {
 *    console.log('new value stored');
 *  });
 *
 * To read a value, call getItem(), but note that you must supply a callback
 * function that the value will be passed to asynchronously:
 *
 *  asyncStorage.getItem('key', function(value) {
 *    console.log('The value of key is:', value);
 *  });
 *
 * Note that unlike localStorage, asyncStorage does not allow you to store and
 * retrieve values by setting and querying properties directly. You cannot just
 * write asyncStorage.key; you have to explicitly call setItem() or getItem().
 *
 * removeItem(), clear(), length(), and key() are like the same-named methods of
 * localStorage, but, like getItem() and setItem() they take a callback
 * argument.
 *
 * The asynchronous nature of getItem() makes it tricky to retrieve multiple
 * values. But unlike localStorage, asyncStorage does not require the values you
 * store to be strings.  So if you need to save multiple values and want to
 * retrieve them together, in a single asynchronous operation, just group the
 * values into a single object. The properties of this object may not include
 * DOM elements, but they may include things like Blobs and typed arrays.
 */

var asyncStorage = (function() {
  var indexedDB = window.indexedDB || window.webkitIndexedDB ||
                window.mozIndexedDB || window.msIndexedDB;

  var DBNAME = 'asyncStorage';
  var DBVERSION = 1;
  var STORENAME = 'keyvaluepairs';
  var db = null;

  function withDatabase(f) {
    if (db) {
      f();
    } else {
      var openreq = indexedDB.open(DBNAME, DBVERSION);
      openreq.onerror = function withStoreOnError() {
        console.error('asyncStorage: can\'t open database:',
            openreq.error.name);
      };
      openreq.onupgradeneeded = function withStoreOnUpgradeNeeded() {
        // First time setup: create an empty object store
        openreq.result.createObjectStore(STORENAME);
      };
      openreq.onsuccess = function withStoreOnSuccess() {
        db = openreq.result;
        f();
      };
    }
  }

  function withStore(type, callback, oncomplete) {
    withDatabase(function() {
      var transaction = db.transaction(STORENAME, type);
      if (oncomplete) {
        transaction.oncomplete = oncomplete;
      }
      callback(transaction.objectStore(STORENAME));
    });
  }

  function getItem(key, callback) {
    var req;
    withStore('readonly', function getItemBody(store) {
      req = store.get(key);
      req.onerror = function getItemOnError() {
        console.error('Error in asyncStorage.getItem(): ', req.error.name);
      };
    }, function onComplete() {
      var value = req.result;
      if (value === undefined) {
        value = null;
      }
      callback(value);
    });
  }

  function setItem(key, value, callback) {
    withStore('readwrite', function setItemBody(store) {
      var req = store.put(value, key);
      req.onerror = function setItemOnError() {
        console.error('Error in asyncStorage.setItem(): ', req.error.name);
      };
    }, callback);
  }

  function removeItem(key, callback) {
    withStore('readwrite', function removeItemBody(store) {
      var req = store.delete(key);
      req.onerror = function removeItemOnError() {
        console.error('Error in asyncStorage.removeItem(): ', req.error.name);
      };
    }, callback);
  }

  function clear(callback) {
    withStore('readwrite', function clearBody(store) {
      var req = store.clear();
      req.onerror = function clearOnError() {
        console.error('Error in asyncStorage.clear(): ', req.error.name);
      };
    }, callback);
  }

  function length(callback) {
    var req;
    withStore('readonly', function lengthBody(store) {
      req = store.count();
      req.onerror = function lengthOnError() {
        console.error('Error in asyncStorage.length(): ', req.error.name);
      };
    }, function onComplete() {
      callback(req.result);
    });
  }

  function key(n, callback) {
    if (n < 0) {
      callback(null);
      return;
    }

    var req;
    withStore('readonly', function keyBody(store) {
      var advanced = false;
      req = store.openCursor();
      req.onsuccess = function keyOnSuccess() {
        var cursor = req.result;
        if (!cursor) {
          // this means there weren't enough keys
          return;
        }
        if (n === 0 || advanced) {
          // Either 1) we have the first key, return it if that's what they
          // wanted, or 2) we've got the nth key.
          return;
        }

        // Otherwise, ask the cursor to skip ahead n records
        advanced = true;
        cursor.advance(n);
      };
      req.onerror = function keyOnError() {
        console.error('Error in asyncStorage.key(): ', req.error.name);
      };
    }, function onComplete() {
      var cursor = req.result;
      callback(cursor ? cursor.key : null);
    });
  }

  return {
    getItem: getItem,
    setItem: setItem,
    removeItem: removeItem,
    clear: clear,
    length: length,
    key: key
  };
})();

var fs = (function() {
  function dirname(path) {
    var index = path.lastIndexOf("/");
    if (index == -1) {
      return ".";
    }

    while (index >= 0 && path[index] == "/") {
      --index;
    }

    var dir = path.slice(0, index + 1);
    if (dir == "") {
      dir = "/";
    }
    return dir;
  }

  function basename(path) {
    return path.slice(path.lastIndexOf("/") + 1);
  }

  function init(cb) {
    asyncStorage.setItem("/", [], cb);
  }

  var openedFiles = [];

  function open(path, cb) {
    asyncStorage.getItem(path, function(blob) {
      if (blob == null || !(blob instanceof Blob)) {
        cb(-1);
      } else {
        var fd = openedFiles.push({
          path: path,
          blob: blob,
        }) - 1;
        cb(fd);
      }
    });    
  }

  function close(fd) {
    if (fd >= 0) {
      openedFiles.splice(fd, 1);
    }
  }

  function read(fd, from, to) {
    if (!openedFiles[fd]) {
      return null;
    }

    var blob = openedFiles[fd].blob;

    if (!from) {
      from = 0;
    }

    if (!to) {
      to = blob.size;
    }

    if (from > blob.size || to > blob.size) {
      return null;
    }

    return blob.slice(from, to);
  }

  function write(fd, data, from) {
    if (!from) {
      from = 0;
    }

    var oldBlob = openedFiles[fd].blob;

    var parts = new Array();

    if (from > 0) {
      parts.push(oldBlob.slice(0, from));
    }

    parts.push(data);

    if (from + data.size < oldBlob.size) {
      parts.push(oldBlob.slice(from + data.size, oldBlob.size));
    }

    openedFiles[fd].blob = new Blob(parts);
  }

  function flush(fd, cb) {
    asyncStorage.setItem(openedFiles[fd].path, openedFiles[fd].blob, cb);
  }

  function list(path, cb) {
    asyncStorage.getItem(path, function(files) {
      if (files == null || files instanceof Blob) {
        cb(null);
      } else {
        cb(files);
      }
    });
  }

  function exists(path, cb) {
    asyncStorage.getItem(path, function(data) {
      if (data == null) {
        cb(false);
      } else {
        cb(true);
      }
    });
  }

  function truncate(path, cb) {
    asyncStorage.getItem(path, function(data) {
      if (data == null || !(data instanceof Blob)) {
        cb(false);
      } else {
        asyncStorage.setItem(path, new Blob(), function() {
          cb(true);
        });
      }
    });
  }

  function remove(path, cb) {
    list(path, function(files) {
      if (files != null && files.length > 0) {
        cb(false);
        return;
      }

      var name = basename(path);
      var dir = dirname(path);

      list(dir, function(files) {
        var index = -1;

        if (files == null || (index = files.indexOf(name)) < 0) {
          cb(false);
          return;
        }

        files.splice(index, 1);
        asyncStorage.setItem(dir, files, function() {
          asyncStorage.removeItem(path, function() {
            cb(true);
          });
        });
      });
    });
  }

  function createInternal(path, data, cb) {
    var name = basename(path);
    var dir = dirname(path);

    list(dir, function(files) {
      if (files == null || files.indexOf(name) >= 0) {
        cb(false);
        return;
      }

      files.push(name);
      asyncStorage.setItem(dir, files, function() {
        asyncStorage.setItem(path, data, function() {
          cb(true);
        });
      });
    });
  }

  function create(path, blob, cb) {
    createInternal(path, blob, cb);
  }

  function mkdir(path, cb) {
    createInternal(path, [], cb);
  }

  function size(path, cb) {
    asyncStorage.getItem(path, function(blob) {
      if (blob == null || !(blob instanceof Blob)) {
        cb(-1);
      } else {
        cb(blob.size);
      }
    });
  }

  return {
    init: init,
    open: open,
    close: close,
    read: read,
    write: write,
    flush: flush,
    list: list,
    exists: exists,
    truncate: truncate,
    remove: remove,
    create: create,
    mkdir: mkdir,
    size: size,
  };
})();
