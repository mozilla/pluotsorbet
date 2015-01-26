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
  Store.DBVERSION = 4;
  Store.DBSTORENAME_1 = "keyvaluepairs";
  Store.DBSTORENAME_2 = "fs";
  Store.DBSTORENAME_4 = "fs4";
  Store.DBSTORENAME = Store.DBSTORENAME_4;

  Store.prototype.upgrade = {
    "1to2": function(db, transaction, next) {
      // Create new object store.
      var newObjectStore = db.createObjectStore(Store.DBSTORENAME_2);

      // Iterate the keys in the old object store and copy their values
      // to the new one, converting them from old- to new-style records.
      var oldObjectStore = transaction.objectStore(Store.DBSTORENAME_1);
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

        db.deleteObjectStore(Store.DBSTORENAME_1);
        next();
      };
    },

    "2to3": function(db, transaction, next) {
      var objectStore = transaction.objectStore(Store.DBSTORENAME_2);
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

    "3to4": function(db, transaction, next) {
      // Create new object store.
      var newObjectStore = db.createObjectStore(Store.DBSTORENAME_4, { keyPath: "pathname" });
      newObjectStore.createIndex("parentDir", "parentDir", { unique: false });

      // Iterate the keys in the old object store and copy their values
      // to the new one, converting them from old- to new-style records.
      var oldObjectStore = transaction.objectStore(Store.DBSTORENAME_2);
      oldObjectStore.openCursor().onsuccess = function(event) {
        var cursor = event.target.result;

        if (cursor) {
          var newRecord = cursor.value;
          newRecord.pathname = cursor.key;
          newObjectStore.put(newRecord);
          cursor.continue();
          return;
        }

        db.deleteObjectStore(Store.DBSTORENAME_2);
        next();
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
        // XXX This is brittle, because there are two places where the latest
        // version of the database is created: here and in the most recent
        // upgrade function.  So we should refactor this code, perhaps moving
        // the initial object store creation into a "0to1" upgrade function,
        // or perhaps moving the latest object store creation into a function.
        var objectStore = openreq.result.createObjectStore(Store.DBSTORENAME, { keyPath: "pathname" });
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

      // Retrieve all records and put them into the in-memory map.
      var transaction = this.db.transaction(Store.DBSTORENAME, "readonly");
      if (DEBUG_FS) { console.log("getAll initiated"); }
      var objectStore = transaction.objectStore(Store.DBSTORENAME);
      var then = performance.now();
      objectStore.mozGetAll().onsuccess = (function(event) {
        var records = event.target.result;
        for (var i = 0; i < records.length; ++i) {
          this.map.set(records[i].pathname, records[i]);
        };
        if (DEBUG_FS) { console.log("getAll completed in " + (performance.now() - then) + "ms"); }
        cb();
      }).bind(this);
    }).bind(this);
  };

  Store.prototype.getItem = function(key) {
    if (this.map.has(key)) {
      return this.map.get(key);
    }

    var value = null;
    this.map.set(key, value);
    return value;
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

  Store.prototype.purge = function() {
    // Now that we keep all records in-memory, only transient paths get purged.
    this.transientPaths.forEach((function(value, key) {
      this.map.delete(key);
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
        change.value.pathname = key;
        req = objectStore.put(change.value);
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
      Object.keys(input).forEach((function(key) {
        if (DEBUG_FS) { console.log("importing " + key); }
        var record = input[key];
        if (!record.isDir) {
          record.data = new Blob([new Int8Array(record.data)]);
        }
        this.map.set(key, record);
        record.pathname = key;
        var req = objectStore.put(record);
        req.onerror = reportRequestError.bind(null, "import", req);
      }).bind(this));
      transaction.oncomplete = function() {
        if (DEBUG_FS) { console.log("import completed"); }
        cb();
      };
    }).bind(this);
    reader.readAsText(file);
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

  function initRootDir() {
    if (!store.getItem("/")) {
      store.setItem("/", {
        isDir: true,
        mtime: Date.now(),
        parentDir: null,
      });
    }
  }

  function init(cb) {
    store.init(function() {
      initRootDir();
      cb();
    });
  }

  var openedFiles = [null, null, null];

  function open(path, cb) {
    path = normalizePath(path);
    if (DEBUG_FS) { console.log("fs open " + path); }

    var record = store.getItem(path);
    if (record == null || record.isDir) {
      setZeroTimeout(function() { cb(-1) });
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

  function list(path) {
    path = normalizePath(path);
    if (DEBUG_FS) { console.log("fs list " + path); }

    var record = store.getItem(path);
    if (record == null) {
      throw new Error("Path does not exist");
    }

    if (!record.isDir) {
      throw new Error("Path is not a directory");
    }

    var files = [];

    store.map.forEach(function(value, key) {
      if (value && value.parentDir === path) {
        files.push(basename(key) + (value.isDir ? "/" : ""));
      }
    });

    return files.sort();
  }

  function exists(path) {
    path = normalizePath(path);

    var record = store.getItem(path);
    if (DEBUG_FS) { console.log("fs exists " + path + ": " + !!record); }

    return !!record;
  }

  function truncate(path) {
    path = normalizePath(path);
    if (DEBUG_FS) { console.log("fs truncate " + path); }

    var record = store.getItem(path);
    if (record == null || record.isDir) {
      return false;
    }

    record.data = new Blob();
    record.mtime = Date.now();
    record.size = 0;
    store.setItem(path, record);
    return true;
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

  function remove(path) {
    path = normalizePath(path);
    if (DEBUG_FS) { console.log("fs remove " + path); }

    if (openedFiles.findIndex(function(file) { return file && file.path === path; }) != -1) {
      return false;
    }

    var record = store.getItem(path);

    if (!record) {
      return false;
    }

    // If it's a directory that isn't empty, then we can't remove it.
    if (record.isDir) {
      for (var value of store.map.values()) {
        if (value && value.parentDir === path) {
          return false;
        }
      }
    }

    store.removeItem(path);
    return true;
  }

  function createInternal(path, record) {
    var name = basename(path);
    var dir = dirname(path);

    var parentRecord = store.getItem(dir);

    // If the parent directory doesn't exist or isn't a directory,
    // then we can't create the file.
    if (parentRecord === null || !parentRecord.isDir) {
      console.error("parent directory '" + dir + "' doesn't exist or isn't a directory");
      return false;
    }

    var existingRecord = store.getItem(path);

    // If the file already exists, then we can't create it.
    if (existingRecord) {
      if (DEBUG_FS) { console.error("file '" + path + "' already exists"); }
      return false;
    }

    // Create the file.
    store.setItem(path, record);

    return true;
  }

  function create(path, blob) {
    path = normalizePath(path);
    if (DEBUG_FS) { console.log("fs create " + path); }

    var record = {
      isDir: false,
      mtime: Date.now(),
      data: blob,
      size: blob.size,
      parentDir: dirname(path),
    };

    return createInternal(path, record);
  }

  function mkdir(path) {
    path = normalizePath(path);
    if (DEBUG_FS) { console.log("fs mkdir " + path); }

    var record = {
      isDir: true,
      mtime: Date.now(),
      parentDir: dirname(path),
    };

    return createInternal(path, record);
  }

  function mkdirp(path) {
    if (DEBUG_FS) { console.log("fs mkdirp " + path); }

    if (path[0] !== "/") {
      console.error("mkdirp called on relative path: " + path);
      return false;
    }

    // Split the path into parts across "/", discarding the initial, empty part.
    var parts = normalizePath(path).split("/").slice(1);

    var partPath = "";

    function mkpart(created) {
      if (!created) {
        return false;
      }

      if (!parts.length) {
        return true;
      }

      partPath += "/" + parts.shift();

      var record = store.getItem(partPath);

      if (!record) {
        // The part doesn't exist; make it, then continue to next part.
        return mkpart(mkdir(partPath));
      } else if (record.isDir) {
        // The part exists and is a directory; continue to next part.
        return mkpart(true);
      } else {
        // The part exists but isn't a directory; fail.
        console.error("mkdirp called on path with non-dir part: " + partPath);
        return false;
      }
    }

    return mkpart(true);
  }

  function size(path) {
    path = normalizePath(path);
    if (DEBUG_FS) { console.log("fs size " + path); }

    var record = store.getItem(path);
    if (record == null || record.isDir) {
      return -1;
    } else {
      return record.size;
    }
  }

  // Callers of this function should make sure
  // newPath doesn't exist.
  function rename(oldPath, newPath) {
    oldPath = normalizePath(oldPath);
    newPath = normalizePath(newPath);
    if (DEBUG_FS) { console.log("fs rename " + oldPath + " -> " + newPath); }

    if (openedFiles.findIndex(function(file) { return file && file.path === oldPath; }) != -1) {
      return false;
    }

    var oldRecord = store.getItem(oldPath);

    // If the old path doesn't exist, we can't move it.
    if (oldRecord == null) {
      return false;
    }

    // If the old path is a dir with files in it, then we don't move it.
    // XXX Move it along with its files.
    if (oldRecord.isDir) {
      for (var value of store.map.values()) {
        if (value && value.parentDir === oldPath) {
          console.error("rename directory containing files not implemented: " + oldPath + " to " + newPath);
          return false;
        }
      }
    }

    store.removeItem(oldPath);
    oldRecord.parentDir = dirname(newPath);
    store.setItem(newPath, oldRecord);
    return true;
  }

  function stat(path) {
    path = normalizePath(path);
    if (DEBUG_FS) { console.log("fs stat " + path); }

    var record = store.getItem(path);

    if (record === null) {
      return null;
    }

    return {
      isDir: record.isDir,
      mtime: record.mtime,
      size: record.size,
    };
  }

  function clear() {
    store.clear();
    initRootDir();
  }

  function syncStore(cb) {
    store.sync(cb);
  }

  function purgeStore() {
    store.purge();
  }

  function createUniqueFile(parentDir, completeName, blob) {
    var name = completeName;
    var ext = "";
    var extIndex = name.lastIndexOf(".");
    if (extIndex !== -1) {
      ext = name.substring(extIndex);
      name = name.substring(0, extIndex);
    }

    var i = 0;
    function tryFile(fileName) {
      if (exists(parentDir + "/" + fileName)) {
        i++;
        return tryFile(name + "-" + i + ext);
      } else {
        // XXX Shouldn't this be mkdirp if we really want to ensure
        // that the parent directory exists?
        mkdir(parentDir);
        create(parentDir + "/" + fileName, blob);
        return fileName;
      }
    }
    return tryFile(completeName);
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
