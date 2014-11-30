'use strict';

var DEBUG_FS = false;

var fs = (function() {
  var Store = function() {
    this.map = new Map();
    this.db = null;
  };

  Store.DBNAME = "asyncStorage";
  Store.DBVERSION = 2;
  Store.DBSTORENAME = "fs";

  Store.prototype.init = function(cb) {
    var openreq = indexedDB.open(Store.DBNAME, Store.DBVERSION);
    openreq.onerror = function() {
      console.error("error opening database: " + openreq.error.name);
    };
    openreq.onupgradeneeded = function(event) {
      if (DEBUG_FS) { console.log("upgrade needed from " + event.oldVersion + " to " + event.newVersion); }

      var db = event.target.result;
      var transaction = openreq.transaction;

      if (event.oldVersion == 0) {
        // If the database doesn't exist yet, then all we have to do
        // is create the object store for the latest version of the database.
        openreq.result.createObjectStore(Store.DBSTORENAME);
      } else if (event.oldVersion == 1) {
        // Create new object store.
        var newObjectStore = openreq.result.createObjectStore(Store.DBSTORENAME);

        // Iterate the keys in the old object store and copy their values
        // to the new one, converting them from old- to new-style records.
        var oldObjectStore = transaction.objectStore("keyvaluepairs");
        var oldRecords = {};
        oldObjectStore.openCursor().onsuccess = function(event) {
          var cursor = event.target.result;

          if (cursor) {
            oldRecords[cursor.key] = cursor.value;
            cursor.continue();
            return;
          }

          // Convert the old records to new ones.
          for (var key in oldRecords) {
            // Records that start with an exclamation mark are stats,
            // which we don't iterate (but do use below when processing
            // their equivalent data records).
            if (key[0] == "!") {
              continue;
            }

            var oldRecord = oldRecords[key];
            var oldStat = oldRecords["!" + key];
            var newRecord = oldStat;
            if (newRecord.isDir) {
              newRecord.files = oldRecord;
            } else {
              newRecord.data = oldRecord;
            }

            newObjectStore.put(newRecord, key);
          }

          db.deleteObjectStore("keyvaluepairs");
        };
      }
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
        store.setItem("/", {
          isDir: true,
          mtime: Date.now(),
          files: [],
        });
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

    store.getItem(path, function(record) {
      if (record == null || record.isDir || !(record.data instanceof Blob)) {
        cb(-1);
      } else {
        var reader = new FileReader();
        reader.addEventListener("loadend", function() {
          var fd = openedFiles.push({
            dirty: false,
            path: path,
            buffer: new FileBuffer(new Uint8Array(reader.result)),
            position: 0,
            record: record,
          }) - 1;
          cb(fd);
        });
        reader.readAsArrayBuffer(record.data);
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
    file.record.mtime = Date.now();
    file.record.size = buffer.contentSize;
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

    openedFile.record.data = new Blob([openedFile.buffer.getContent()]);
    store.setItem(openedFile.path, openedFile.record);
    openedFile.dirty = false;
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

    store.getItem(path, function(record) {
      if (record == null || !record.isDir) {
        cb(null);
      } else {
        cb(record.files);
      }
    });
  }

  function exists(path, cb) {
    path = normalizePath(path);
    if (DEBUG_FS) { console.log("fs exists " + path); }

    store.getItem(path, function(record) {
      cb(record ? true : false);
    });
  }

  function truncate(path, cb) {
    path = normalizePath(path);
    if (DEBUG_FS) { console.log("fs truncate " + path); }

    store.getItem(path, function(record) {
      if (record == null || record.isDir) {
        cb(false);
      } else {
        record.data = new Blob();
        record.mtime = Date.now();
        record.size = 0;
        store.setItem(path, record);
        cb(true);
      }
    });
  }

  function ftruncate(fd, size) {
    if (DEBUG_FS) { console.log("fs ftruncate " + openedFiles[fd].path); }

    var file = openedFiles[fd];
    if (size != file.buffer.contentSize) {
      file.buffer.setSize(size);
      file.dirty = true;
      file.record.mtime = Date.now();
      file.record.size = size;
    }
  }

  function remove(path, cb) {
    path = normalizePath(path);
    if (DEBUG_FS) { console.log("fs remove " + path); }

    if (openedFiles.findIndex(function(file) { return file && file.path === path; }) != -1) {
      setZeroTimeout(function() { cb(false); });
      return;
    }

    store.getItem(path, function(record) {
      // If it's a directory that isn't empty, then we can't remove it.
      if (record && record.isDir && record.files.length > 0) {
        cb(false);
        return;
      }

      var name = basename(path);
      var dir = dirname(path);

      store.getItem(dir, function(parentRecord) {
        var index = -1;

        // If it isn't in the parent directory, then we can't remove it.
        if (parentRecord == null || (index = parentRecord.files.indexOf(name)) < 0) {
          cb(false);
          return;
        }

        parentRecord.files.splice(index, 1);
        store.setItem(dir, parentRecord);
        store.removeItem(path);
        cb(true);
      });
    });
  }

  function createInternal(path, record, cb) {
    var name = basename(path);
    var dir = dirname(path);

    store.getItem(dir, function(parentRecord) {
      // If the parent directory doesn't exist or isn't a directory,
      // then we can't create the file.
      if (parentRecord == null || !parentRecord.isDir) {
        cb(false);
        return;
      }

      // If the file already exists, we can't create it.
      if (parentRecord.files.indexOf(name) >= 0) {
        cb(false);
        return;
      }

      parentRecord.files.push(name);
      store.setItem(dir, parentRecord);
      store.setItem(path, record);
      cb(true);
    });
  }

  function create(path, blob, cb) {
    path = normalizePath(path);
    if (DEBUG_FS) { console.log("fs create " + path); }

    var record = {
      isDir: false,
      mtime: Date.now(),
      data: blob,
      size: blob.size,
    };

    createInternal(path, record, cb);
  }

  function mkdir(path, cb) {
    path = normalizePath(path);
    if (DEBUG_FS) { console.log("fs mkdir " + path); }

    var record = {
      isDir: true,
      mtime: Date.now(),
      files: [],
    };

    createInternal(path, record, cb);
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

      store.getItem(partPath, function(record) {
        if (!record) {
          // The part doesn't exist; make it, then continue to next part.
          mkdir(partPath, mkpart);
        }
        else if (record.isDir) {
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

    store.getItem(path, function(record) {
      if (record == null || record.isDir || !(record.data instanceof Blob)) {
        cb(-1);
      } else {
        cb(record.data.size);
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

    store.getItem(oldPath, function(oldRecord) {
      // If the old path doesn't exist, we can't move it.
      if (oldRecord == null) {
        cb(false);
        return;
      }

      // If the old path is a dir with files in it, we don't move it.
      // XXX Shouldn't we move it along with its files?
      if (oldRecord.isDir && oldRecord.files.length > 0) {
        cb(false);
        return;
      }

      remove(oldPath, function(removed) {
        if (!removed) {
          cb(false);
          return;
        }

        if (oldRecord.isDir) {
          mkdir(newPath, cb);
        } else {
          create(newPath, oldRecord.data, cb);
        }
      });
    });
  }

  function stat(path, cb) {
    path = normalizePath(path);
    if (DEBUG_FS) { console.log("fs stat " + path); }

    var file = openedFiles.find(function (file) { return file && file.path === path });
    if (file) {
      var stat = {
        isDir: file.record.isDir,
        mtime: file.record.mtime,
        size: file.record.size,
      };
      setZeroTimeout(function() { cb(stat); });
      return;
    }

    store.getItem(path, function(record) {
      if (record == null) {
        cb(null);
        return;
      }

      var stat = {
        isDir: record.isDir,
        mtime: record.mtime,
        size: record.size,
      };
      cb(stat);
    });
  }

  function clear(cb) {
    store.clear();
    initRootDir(cb || function() {});
  }

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
  };
})();
