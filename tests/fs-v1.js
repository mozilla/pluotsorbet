'use strict';

var DEBUG_FS = false;

var fs = (function() {
  var Store = function() {
    this.map = new Map();
    this.db = null;
  };

  Store.DBNAME = "asyncStorage";
  Store.DBVERSION = 1;
  Store.DBSTORENAME = "keyvaluepairs";

  Store.prototype.init = function(cb) {
    var openreq = indexedDB.open(Store.DBNAME, Store.DBVERSION);
    openreq.onerror = function() {
      console.error("error opening database: " + openreq.error.name);
    };
    openreq.onupgradeneeded = function() {
      openreq.result.createObjectStore(Store.DBSTORENAME);
    };
    openreq.onsuccess = (function() {
      this.db = openreq.result;
      cb();
    }).bind(this);
  };

  Store.prototype.getItem = function(key, cb) {
    if (this.map.has(key)) {
      var value = this.map.get(key);
      window.setZeroTimeout(function() { cb(value) });
    } else {
      var transaction = this.db.transaction(Store.DBSTORENAME, "readonly");
      var objectStore = transaction.objectStore(Store.DBSTORENAME);
      var req = objectStore.get(key);
      req.onerror = function() {
        console.error("Error getting " + key + ": " + req.error.name);
      };
      transaction.oncomplete = (function() {
        var value = req.result;
        if (value === undefined) {
          value = null;
        }
        this.map.set(key, value);
        cb(value);
      }).bind(this);
    }
  };

  Store.prototype.setItem = function(key, value) {
    this.map.set(key, value);

    var transaction = this.db.transaction(Store.DBSTORENAME, "readwrite");
    var objectStore = transaction.objectStore(Store.DBSTORENAME);
    var req = objectStore.put(value, key);
    req.onerror = function() {
      console.error("Error putting " + key + ": " + req.error.name);
    };
  };

  Store.prototype.removeItem = function(key) {
    this.map.delete(key);

    var transaction = this.db.transaction(Store.DBSTORENAME, "readwrite");
    var objectStore = transaction.objectStore(Store.DBSTORENAME);
    var req = objectStore.delete(key);
    req.onerror = function() {
      console.error("Error deleting " + key + ": " + req.error.name);
    };
  };

  Store.prototype.clear = function() {
    this.map.clear();

    var transaction = this.db.transaction(Store.DBSTORENAME, "readwrite");
    var objectStore = transaction.objectStore(Store.DBSTORENAME);
    var req = objectStore.clear();
    req.onerror = function() {
      console.error("Error clearing store: " + req.error.name);
    };
  }

  Store.prototype.sync = function(cb) {
    // Process a readwrite transaction to ensure previous writes have completed,
    // so we leave the datastore in a consistent state.  This is a bit hacky;
    // we should instead monitor ongoing transactions and call our callback
    // once they've all completed.
    var transaction = this.db.transaction(Store.DBSTORENAME, "readwrite");
    var objectStore = transaction.objectStore(Store.DBSTORENAME);
    objectStore.get("");
    transaction.oncomplete = function() {
      cb();
    };
  }

  var store = new Store();

  var FileBuffer = function(array) {
    this.array = array;
    this.contentSize = array.byteLength;
  }

  FileBuffer.prototype.setSize = function(newContentSize) {
    if (newContentSize < this.array.byteLength) {
      this.contentSize = newContentSize;
      return;
    }

    var newBufferSize = 512;

    // The buffer grows exponentially until the content size
    // reaches 65536. After this threshold, it starts to grow
    // linearly in increments of 65536 bytes.
    if (newContentSize < 65536) {
      while (newContentSize > newBufferSize) {
        newBufferSize <<= 1;
      }
    } else {
      while (newContentSize > newBufferSize) {
        newBufferSize += 65536;
      }
    }

    var newArray = new Uint8Array(newBufferSize);
    newArray.set(this.array);

    this.array = newArray;
    this.contentSize = newContentSize;
  }

  FileBuffer.prototype.getContent = function() {
    return this.array.subarray(0, this.contentSize);
  }

  function normalizePath(path) {
    // Remove a trailing slash.
    if (path.length != 1 && path.lastIndexOf("/") == path.length-1) {
      path = path.substring(0, path.length-1);
    }

    // Coalesce multiple consecutive slashes.
    path = path.replace(/\/{2,}/, "/");

    // XXX Replace "." and ".." parts.

    return path;
  }

  function dirname(path) {
    path = normalizePath(path);

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

  function initRootDir(cb) {
    store.getItem("/", function(data) {
      if (data) {
        cb();
      } else {
        store.setItem("/", []);
        setStat("/", { mtime: Date.now(), isDir: true });
        cb();
      }
    });
  }

  function init(cb) {
    store.init(function() {
      initRootDir(cb || function() {});
    });
  }

  var openedFiles = [null, null, null];

  function open(path, cb) {
    path = normalizePath(path);
    if (DEBUG_FS) { console.log("fs open " + path); }

    store.getItem(path, function(blob) {
      if (blob == null || !(blob instanceof Blob)) {
        cb(-1);
      } else {
        var reader = new FileReader();
        reader.addEventListener("loadend", function() {
          var fd = openedFiles.push({
            dirty: false,
            path: path,
            buffer: new FileBuffer(new Uint8Array(reader.result)),
            position: 0,
          }) - 1;
          cb(fd);
        });
        reader.readAsArrayBuffer(blob);
      }
    });
  }

  function close(fd) {
    if (fd >= 0 && openedFiles[fd]) {
      if (DEBUG_FS) { console.log("fs close " + openedFiles[fd].path); }
      flush(fd);
      openedFiles.splice(fd, 1, null);
    }
  }

  function read(fd, from, to) {
    if (!openedFiles[fd]) {
      return null;
    }
    if (DEBUG_FS) { console.log("fs read " + openedFiles[fd].path); }

    var buffer = openedFiles[fd].buffer;

    if (typeof from === "undefined") {
      from = openedFiles[fd].position;
    }

    if (!to || to > buffer.contentSize) {
      to = buffer.contentSize;
    }

    if (from > buffer.contentSize) {
      from = buffer.contentSize;
    }

    openedFiles[fd].position += to - from;
    return buffer.array.subarray(from, to);
  }

  function write(fd, data, from) {
    if (DEBUG_FS) { console.log("fs write " + openedFiles[fd].path); }

    if (typeof from == "undefined") {
      from = openedFiles[fd].position;
    }

    var buffer = openedFiles[fd].buffer;

    if (from > buffer.contentSize) {
      from = buffer.contentSize;
    }

    var newLength = (from + data.byteLength > buffer.contentSize) ? (from + data.byteLength) : (buffer.contentSize);

    buffer.setSize(newLength);

    buffer.array.set(data, from);

    var file = openedFiles[fd];
    file.position = from + data.byteLength;
    file.stat = { mtime: Date.now(), isDir: false, size: buffer.contentSize };
    file.dirty = true;
  }

  function getpos(fd) {
    return openedFiles[fd].position;
  }

  function setpos(fd, pos) {
    openedFiles[fd].position = pos;
  }

  function getsize(fd) {
    if (!openedFiles[fd]) {
      return -1;
    }

    return openedFiles[fd].buffer.contentSize;
  }

  function flush(fd) {
    if (DEBUG_FS) { console.log("fs flush " + openedFiles[fd].path); }

    var openedFile = openedFiles[fd];

    // Bail early if the file has not been modified.
    if (!openedFile.dirty) {
      return;
    }

    var blob = new Blob([openedFile.buffer.getContent()]);
    store.setItem(openedFile.path, blob);
    openedFile.dirty = false;
    if (openedFile.stat) {
      setStat(openedFile.path, openedFile.stat);
    }
  }

  function flushAll() {
    for (var fd = 0; fd < openedFiles.length; fd++) {
      if (!openedFiles[fd] || !openedFiles[fd].dirty) {
        continue;
      }
      flush(fd);
    }
  }

  // Due to bug #227, we don't support Object::finalize(). But the Java
  // filesystem implementation requires the `finalize` method to save cached
  // file data if user doesn't flush or close the file explicitly. To avoid
  // losing data, we flush files periodically.
  setInterval(flushAll, 5000);

  // Flush files when app goes into background.
  window.addEventListener("pagehide", flushAll);

  function list(path, cb) {
    path = normalizePath(path);
    if (DEBUG_FS) { console.log("fs list " + path); }

    store.getItem(path, function(files) {
      if (files == null || files instanceof Blob) {
        cb(null);
      } else {
        cb(files);
      }
    });
  }

  function exists(path, cb) {
    path = normalizePath(path);
    if (DEBUG_FS) { console.log("fs exists " + path); }

    stat(path, function(stat) {
      cb(stat ? true : false);
    });
  }

  function truncate(path, cb) {
    path = normalizePath(path);
    if (DEBUG_FS) { console.log("fs truncate " + path); }

    stat(path, function(stat) {
      if (stat && !stat.isDir) {
        store.setItem(path, new Blob());
        setStat(path, { mtime: Date.now(), isDir: false, size: 0 });
        cb(true);
      } else {
        cb(false);
      }
    });
  }

  function ftruncate(fd, size) {
    if (DEBUG_FS) { console.log("fs ftruncate " + openedFiles[fd].path); }

    var file = openedFiles[fd];
    if (size != file.buffer.contentSize) {
      file.buffer.setSize(size);
      file.dirty = true;
      file.stat = { mtime: Date.now(), isDir: false, size: size };
    }
  }

  function remove(path, cb) {
    path = normalizePath(path);
    if (DEBUG_FS) { console.log("fs remove " + path); }

    if (openedFiles.findIndex(function(file) { return file && file.path === path; }) != -1) {
      setZeroTimeout(function() { cb(false); });
      return;
    }

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
        store.setItem(dir, files);
        store.removeItem(path);
        removeStat(path);
        cb(true);
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
      store.setItem(dir, files);
      store.setItem(path, data);
      cb(true);
    });
  }

  function create(path, blob, cb) {
    path = normalizePath(path);
    if (DEBUG_FS) { console.log("fs create " + path); }

    createInternal(path, blob, function(created) {
      if (created) {
        setStat(path, { mtime: Date.now(), isDir: false, size: blob.size });
      }
      cb(created);
    });
  }

  function mkdir(path, cb) {
    path = normalizePath(path);
    if (DEBUG_FS) { console.log("fs mkdir " + path); }

    createInternal(path, [], function(created) {
      if (created) {
        setStat(path, { mtime: Date.now(), isDir: true });
      }
      cb(created);
    });
  }

  function mkdirp(path, cb) {
    if (DEBUG_FS) { console.log("fs mkdirp " + path); }

    if (path[0] !== "/") {
      console.error("mkdirp called on relative path: " + path);
      cb(false);
    }

    // Split the path into parts across "/", discarding the initial, empty part.
    var parts = normalizePath(path).split("/").slice(1);

    var partPath = "";

    function mkpart(created) {
      if (!created) {
        return cb(false);
      }

      if (!parts.length) {
        return cb(true);
      }

      partPath += "/" + parts.shift();

      stat(partPath, function(stat) {
        if (!stat) {
          // The part doesn't exist; make it, then continue to next part.
          mkdir(partPath, mkpart);
        }
        else if (stat.isDir) {
          // The part exists and is a directory; continue to next part.
          mkpart(true);
        }
        else {
          // The part exists but isn't a directory; fail.
          console.error("mkdirp called on path with non-dir part: " + partPath);
          cb(false);
        }
      });
    }

    mkpart(true);
  }

  function size(path, cb) {
    path = normalizePath(path);
    if (DEBUG_FS) { console.log("fs size " + path); }

    store.getItem(path, function(blob) {
      if (blob == null || !(blob instanceof Blob)) {
        cb(-1);
      } else {
        cb(blob.size);
      }
    });
  }

  // Callers of this function should make sure
  // newPath doesn't exist.
  function rename(oldPath, newPath, cb) {
    oldPath = normalizePath(oldPath);
    newPath = normalizePath(newPath);
    if (DEBUG_FS) { console.log("fs rename " + oldPath + " -> " + newPath); }

    if (openedFiles.findIndex(function(file) { return file && file.path === oldPath; }) != -1) {
      setZeroTimeout(function() { cb(false); });
      return;
    }

    list(oldPath, function(files) {
      if (files != null && files.length > 0) {
        cb(false);
        return;
      }

      store.getItem(oldPath, function(data) {
        if (data == null) {
          cb(false);
          return;
        }

        remove(oldPath, function(removed) {
          if (!removed) {
            cb(false);
            return;
          }

          if (data instanceof Blob) {
            create(newPath, data, cb);
          } else {
            mkdir(newPath, cb);
          }
        });
      });
    });
  }

  function setStat(path, stat) {
    if (DEBUG_FS) { console.log("fs setStat " + path); }

    store.setItem("!" + path, stat);
  }

  function removeStat(path) {
    if (DEBUG_FS) { console.log("fs removeStat " + path); }

    store.removeItem("!" + path);
  }

  function stat(path, cb) {
    path = normalizePath(path);
    if (DEBUG_FS) { console.log("fs stat " + path); }

    var file = openedFiles.find(function (file) { return file && file.stat && file.path === path });
    if (file) {
      setZeroTimeout(function() { cb(file.stat); });
      return;
    }

    store.getItem("!" + path, cb);
  }

  function clear(cb) {
    store.clear();
    initRootDir(cb || function() {});
  }

  function storeSync(cb) {
    store.sync(cb);
  },

  return {
    dirname: dirname,
    init: init,
    open: open,
    close: close,
    read: read,
    write: write,
    getpos: getpos,
    setpos: setpos,
    getsize: getsize,
    flush: flush,
    list: list,
    exists: exists,
    truncate: truncate,
    ftruncate: ftruncate,
    remove: remove,
    create: create,
    mkdir: mkdir,
    mkdirp: mkdirp,
    size: size,
    rename: rename,
    stat: stat,
    clear: clear,
    storeSync: storeSync,
  };
})();
