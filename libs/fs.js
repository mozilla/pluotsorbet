'use strict';

var DEBUG_FS = false;

var fs = (function() {
  var reportRequestError = function(type, request) {
    console.error(type + " error " + request.error);
  }

  var Store = function() {
    this.map = new Map();

    // Pending changes to the persistent datastore, indexed by record key.
    //
    // Changes can represent puts or deletes and comprise a type and (for puts)
    // the value to write:
    //   key: { type: "delete" } or key: { type: "put", value: <value> }
    //
    // We index by key, storing only the most recent change for a given key,
    // to coalesce multiple changes, so that we always sync only the most recent
    // change for a given record.
    this.changesToSync = new Map();

    // Transient paths are those that we store only in memory, so they vanish
    // on shutdown.  By default, we sync paths to the disk store, so they
    // survive shutdown and persist across restart.  Add paths to this map
    // to save disk space and processing power for temporary files, log files,
    // and other files that store transient data.
    //
    // To add paths to this map, call fs.addTransientPath().
    //
    this.transientPaths = new Map();

    this.db = null;
  };

  Store.DBNAME = "asyncStorage";
  Store.DBVERSION = 3;
  Store.OLDDBSTORENAME = "keyvaluepairs";
  Store.DBSTORENAME = "fs";

  Store.prototype.upgrade = {
    "1to2": function(db, transaction, next) {
      // Create new object store.
      var newObjectStore = db.createObjectStore(Store.DBSTORENAME);

      // Iterate the keys in the old object store and copy their values
      // to the new one, converting them from old- to new-style records.
      var oldObjectStore = transaction.objectStore(Store.OLDDBSTORENAME);
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

        db.deleteObjectStore(Store.OLDDBSTORENAME);
        next();
      };
    },

    "2to3": function(db, transaction, next) {
      var objectStore = transaction.objectStore(Store.DBSTORENAME);
      objectStore.createIndex("parentDir", "parentDir", { unique: false });

      // Convert records to the new format:
      // 1. Delete the obsolete "files" property from directory records.
      // 2. Add the new "parentDir" property to all records.
      objectStore.openCursor().onsuccess = function(event) {
        var cursor = event.target.result;
        if (cursor) {
          var newRecord = cursor.value;
          if (newRecord.isDir) {
            delete newRecord.files;
          }
          var path = cursor.key;
          newRecord.parentDir = (path === "/" ? null : dirname(path));
          cursor.update(newRecord);
          cursor.continue();
        } else {
          next();
        }
      };
    },
  };

  Store.prototype.init = function(cb) {
    var openreq = indexedDB.open(Store.DBNAME, Store.DBVERSION);

    openreq.onerror = function() {
      console.error("error opening database: " + openreq.error.name);
    };

    openreq.onupgradeneeded = (function(event) {
      if (DEBUG_FS) { console.log("upgrade needed from " + event.oldVersion + " to " + event.newVersion); }

      var db = event.target.result;
      var transaction = openreq.transaction;

      if (event.oldVersion == 0) {
        // If the database doesn't exist yet, then all we have to do
        // is create the object store for the latest version of the database.
        var objectStore = openreq.result.createObjectStore(Store.DBSTORENAME);
        objectStore.createIndex("parentDir", "parentDir", { unique: false });
      } else {
        var version = event.oldVersion;
        var next = (function() {
          if (version < event.newVersion) {
            if (DEBUG_FS) { console.log("upgrading from " + version + " to " + (version + 1)); }
            this.upgrade[version + "to" + ++version].bind(this)(db, transaction, next);
          }
        }).bind(this);
        next();
      }
    }).bind(this);

    openreq.onsuccess = (function() {
      this.db = openreq.result;
      cb();
    }).bind(this);
  };

  Store.prototype.getItem = function(key, cb) {
    if (this.map.has(key)) {
      var value = this.map.get(key);
      window.setZeroTimeout(function() { cb(value) });
    } else if (this.transientPaths.has(key)) {
      var value = null;
      this.map.set(key, value);
      window.setZeroTimeout(function() { cb(value) });
    } else {
      var transaction = this.db.transaction(Store.DBSTORENAME, "readonly");
      if (DEBUG_FS) { console.log("get " + key + " initiated"); }
      var objectStore = transaction.objectStore(Store.DBSTORENAME);
      var req = objectStore.get(key);
      req.onerror = function() {
        console.error("Error getting " + key + ": " + req.error.name);
      };
      transaction.oncomplete = (function() {
        if (DEBUG_FS) { console.log("get " + key + " completed"); }
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
    if (!this.transientPaths.has(key)) {
      this.changesToSync.set(key, { type: "put", value: value });
    }
  };

  Store.prototype.removeItem = function(key) {
    this.map.set(key, null);
    if (!this.transientPaths.has(key)) {
      this.changesToSync.set(key, { type: "delete" });
    }
  };

  Store.prototype.clear = function() {
    this.map.clear();
    this.changesToSync.clear();

    var transaction = this.db.transaction(Store.DBSTORENAME, "readwrite");
    if (DEBUG_FS) { console.log("clear initiated"); }
    var objectStore = transaction.objectStore(Store.DBSTORENAME);
    var req = objectStore.clear();
    req.onerror = function() {
      console.error("Error clearing store: " + req.error.name);
    };
    transaction.oncomplete = function() {
      if (DEBUG_FS) { console.log("clear completed"); }
    };
  }

  Store.prototype.purge = function(cb) {
    cb = cb || function() {};

    // We have to sync to the persistent store before we purge the memory cache
    // to ensure a caller who writes data to a file, purges the cache, and then
    // immediately reads the file will get the data.
    this.sync((function() {
      this.map.clear();
      cb();
    }).bind(this));
  }

  Store.prototype.sync = function(cb) {
    cb = cb || function() {};

    // If there are no changes to sync, merely call the callback
    // (in a timeout so the callback always gets called asynchronously).
    if (this.changesToSync.size == 0) {
      setZeroTimeout(cb);
      return;
    }

    var transaction = this.db.transaction(Store.DBSTORENAME, "readwrite");
    if (DEBUG_FS) { console.log("sync initiated"); }
    var objectStore = transaction.objectStore(Store.DBSTORENAME);

    this.changesToSync.forEach((function(change, key) {
      var req;
      if (change.type == "put") {
        req = objectStore.put(change.value, key);
        if (DEBUG_FS) { console.log("put " + key); }
        req.onerror = function() {
          console.error("Error putting " + key + ": " + req.error.name);
        };
      } else if (change.type == "delete") {
        req = objectStore.delete(key);
        if (DEBUG_FS) { console.log("delete " + key); }
        req.onerror = function() {
          console.error("Error deleting " + key + ": " + req.error.name);
        };
      }
    }).bind(this));

    this.changesToSync.clear();

    transaction.oncomplete = function() {
      if (DEBUG_FS) { console.log("sync completed"); }
      cb();
    };
  }

  Store.prototype.export = function(cb) {
    var records = {};
    var output = {};
    var promises = [];

    this.sync((function() {
      var transaction = this.db.transaction(Store.DBSTORENAME, "readonly");
      if (DEBUG_FS) { console.log("export initiated"); }
      var objectStore = transaction.objectStore(Store.DBSTORENAME);

      var req = objectStore.openCursor();
      req.onerror = function() {
        console.error("export error " + req.error);
      };
      req.onsuccess = function(event) {
        var cursor = event.target.result;
        if (cursor) {
          records[cursor.key] = cursor.value;
          cursor.continue();
        } else {
          Object.keys(records).forEach(function(key) {
            if (DEBUG_FS) { console.log("exporting " + key); }
            var record = records[key];
            if (record.isDir) {
              output[key] = record;
            } else {
              promises.push(new Promise(function(resolve, reject) {
                var reader = new FileReader();
                reader.addEventListener("loadend", function() {
                  record.data = Array.prototype.slice.call(new Int8Array(reader.result));
                  output[key] = record;
                  resolve();
                });
                reader.readAsArrayBuffer(record.data);
              }));
            }
          });

          Promise.all(promises).then(function() {
            var blob = new Blob([JSON.stringify(output)]);
            if (DEBUG_FS) { console.log("export completed"); }
            cb(blob);
          });
        }
      };
    }).bind(this));
  }

  Store.prototype.import = function(file, cb) {
    var reader = new FileReader();
    reader.onload = (function() {
      var input = JSON.parse(reader.result);
      var transaction = this.db.transaction(Store.DBSTORENAME, "readwrite");
      if (DEBUG_FS) { console.log("import initiated"); }
      this.map.clear();
      var objectStore = transaction.objectStore(Store.DBSTORENAME);
      var req = objectStore.clear();
      req.onerror = reportRequestError.bind(null, "import", req);
      Object.keys(input).forEach(function(key) {
        if (DEBUG_FS) { console.log("importing " + key); }
        var record = input[key];
        if (!record.isDir) {
          record.data = new Blob([new Int8Array(record.data)]);
        }
        var req = objectStore.put(record, key);
        req.onerror = reportRequestError.bind(null, "import", req);
      });
      transaction.oncomplete = function() {
        if (DEBUG_FS) { console.log("import completed"); }
        cb();
      };
    }).bind(this);
    reader.readAsText(file);
  }

  Store.prototype.getRecordsByParentDir = function(parentDir, cb) {
    this.sync((function() {
      var transaction = this.db.transaction(Store.DBSTORENAME, "readonly");
      if (DEBUG_FS) { console.log("getRecordsByParentDir initiated"); }
      var objectStore = transaction.objectStore(Store.DBSTORENAME);
      var index = objectStore.index("parentDir");
      var records = {};
      index.openCursor(IDBKeyRange.only(parentDir)).onsuccess = function(event) {
        var cursor = event.target.result;
        if (cursor) {
          records[cursor.primaryKey] = cursor.value;
          cursor.continue();
        }
      };
      transaction.oncomplete = function() {
        if (DEBUG_FS) { console.log("getRecordsByParentDir completed"); }
        cb(records);
      };
    }).bind(this));
  }

  Store.prototype.isEmpty = function(dir, cb) {
    this.sync((function() {
      var transaction = this.db.transaction(Store.DBSTORENAME, "readonly");
      if (DEBUG_FS) { console.log("isEmpty initiated"); }
      var objectStore = transaction.objectStore(Store.DBSTORENAME);
      var index = objectStore.index("parentDir");
      index.count(IDBKeyRange.only(dir)).onsuccess = function(event) {
        var count = event.target.result;
        if (count > 0) {
          cb(false);
        } else {
          cb(true);
        }
      };
      transaction.oncomplete = function() {
        if (DEBUG_FS) { console.log("isEmpty completed"); }
      };
    }).bind(this));
  }

  Store.prototype.addTransientPath = function(path) {
    this.transientPaths.set(path, true);
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
    // reaches 524288. After this threshold, it starts to grow
    // linearly in increments of 524288 bytes.
    if (newContentSize < 524288) {
      while (newContentSize > newBufferSize) {
        newBufferSize <<= 1;
      }
    } else {
      while (newContentSize > newBufferSize) {
        newBufferSize += 524288;
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
          parentDir: null,
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
      if (record == null || record.isDir) {
        cb(-1);
      } else {
        var reader = new FileReader();
        reader.addEventListener("loadend", function() {
          var fd = openedFiles.push({
            dirty: false,
            path: path,
            buffer: new FileBuffer(new Uint8Array(reader.result)),
            mtime: record.mtime,
            size: record.size,
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
    file.mtime = Date.now();
    file.size = buffer.contentSize;
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
    openedFile.record.mtime = openedFile.mtime;
    openedFile.record.size = openedFile.size;
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

    // After flushing to the in-memory datastore, sync it to the persistent one.
    // We might want to decouple this from the flushAll calls, so we can do them
    // at different interval (f.e. flushing to memory every five seconds
    // but only syncing to the persistent datastore every minute or so), though
    // we should continue to do both immediately on pagehide.
    syncStore();
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
      if (record == null) {
        return cb(new Error("Path does not exist"));
      }

      if (!record.isDir) {
        return cb(new Error("Path is not a directory"));
      }

      store.getRecordsByParentDir(path, function(records) {
        cb(null, Object.keys(records).map(function(v) { return basename(v) + (records[v].isDir ? "/" : "") }).sort());
      });
    });
  }

  function exists(path, cb) {
    path = normalizePath(path);

    store.getItem(path, function(record) {
      if (DEBUG_FS) { console.log("fs exists " + path + ": " + !!record); }
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
      file.mtime = Date.now();
      file.size = size;
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
      if (!record) {
        // If the path doesn't exist, then we can't remove it.
        cb(false);
      } else if (record.isDir) {
        // If the directory isn't empty, then we can't remove it.
        store.isEmpty(path, function(empty) {
          if (!empty) {
            cb(false);
            return;
          }

          store.removeItem(path);
          cb(true);
        });
      } else {
        store.removeItem(path);
        cb(true);
      }
    });
  }

  function createInternal(path, record, cb) {
    var name = basename(path);
    var dir = dirname(path);

    store.getItem(dir, function(parentRecord) {
      // If the parent directory doesn't exist or isn't a directory,
      // then we can't create the file.
      if (parentRecord == null || !parentRecord.isDir) {
        console.error("parent directory of file '" + path + "' doesn't exist or isn't a directory");
        cb(false);
        return;
      }

      store.getItem(path, function(existingRecord) {
        // If the file already exists, then we can't create it.
        if (existingRecord) {
          if (DEBUG_FS) { console.error("file '" + path + "' already exists"); }
          cb(false);
          return;
        }

        // Create the file.
        store.setItem(path, record);
        cb(true);
      });
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
      parentDir: dirname(path),
    };

    createInternal(path, record, cb);
  }

  function mkdir(path, cb) {
    path = normalizePath(path);
    if (DEBUG_FS) { console.log("fs mkdir " + path); }

    var record = {
      isDir: true,
      mtime: Date.now(),
      parentDir: dirname(path),
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
      if (record == null || record.isDir) {
        cb(-1);
      } else {
        cb(record.size);
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

      var moveItem = function() {
        store.removeItem(oldPath);
        oldRecord.parentDir = dirname(newPath);
        store.setItem(newPath, oldRecord);
        cb(true);
      };

      if (oldRecord.isDir) {
        store.isEmpty(oldPath, function(empty) {
          if (empty) {
            moveItem();
          } else {
            // If the old path is a dir with files in it, we don't move it.
            // We should move it along with its files
            console.error("rename directory containing files not implemented: " + oldPath + " to " + newPath);
            cb(false);
          }
        });
      } else {
        moveItem();
      }
    });
  }

  function stat(path, cb) {
    path = normalizePath(path);
    if (DEBUG_FS) { console.log("fs stat " + path); }

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

  function syncStore(cb) {
    store.sync(cb);
  }

  function purgeStore(cb) {
    store.purge(cb);
  }

  var _creatingFile = false;
  var _creatingQueue = [];
  function createUniqueFile(parentDir, completeName, blob, callback) {
    if (_creatingFile) {
      _creatingQueue.push({
        parentDir: parentDir,
        completeName: completeName,
        blob: blob,
        callback: callback
      });
      return;
    }

    _creatingFile = true;

    var name = completeName;
    var ext = "";
    var extIndex = name.lastIndexOf(".");
    if (extIndex !== -1) {
      ext = name.substring(extIndex);
      name = name.substring(0, extIndex);
    }

    var i = 0;
    function tryFile(fileName) {
      exists(parentDir + "/" + fileName, function(exists) {
        if (exists) {
          i++;
          tryFile(name + "-" + i + ext);
        } else {
          mkdir(parentDir, function() {
            create(parentDir + "/" + fileName, blob, function() {
              callback(fileName);
              _creatingFile = false;
              if (_creatingQueue.length > 0) {
                var tmp = _creatingQueue.shift();
                createUniqueFile(tmp.parentDir, tmp.completeName, tmp.blob, tmp.callback);
              }
            });
          });
        }
      });
    }

    tryFile(completeName);
  }

  function addTransientPath(path) {
    return store.addTransientPath(path);
  }

  function exportStore(cb) {
    return store.export(cb);
  }

  function importStore(blob, cb) {
    return store.import(blob, cb);
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
    syncStore: syncStore,
    purgeStore: purgeStore,
    exportStore: exportStore,
    importStore: importStore,
    createUniqueFile: createUniqueFile,
    addTransientPath: addTransientPath,
  };
})();
