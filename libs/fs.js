'use strict';

var fs = (function() {
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

  function init(cb) {
    asyncStorage.getItem("/", function(data) {
      if (data) {
        cb();
      } else {
        asyncStorage.setItem("/", [], function() {
          setStat("/", { mtime: Date.now(), isDir: true }, cb);
        });
      }
    });
  }

  var openedFiles = [];
  var fileStats = {};

  function open(path, cb) {
    path = normalizePath(path);

    asyncStorage.getItem(path, function(blob) {
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

  function close(fd, cb) {
    if (fd >= 0 && openedFiles[fd]) {
      flush(fd, function() {
        // Replace descriptor object with null value instead of removing it from
        // the array so we don't change the indexes of the other objects.
        openedFiles.splice(fd, 1, null);
        if (cb) {
          cb();
        }
      });
    } else {
      if (cb) {
        cb();
      }
    }
  }

  function read(fd, from, to) {
    if (!openedFiles[fd]) {
      return null;
    }

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
    fileStats[file.path] = file.stat;
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

  function flush(fd, cb) {
    // Bail early if the file has not been modified.
    if (!openedFiles[fd].dirty) {
      cb();
      return;
    }

    var blob = new Blob([openedFiles[fd].buffer.getContent()]);
    asyncStorage.setItem(openedFiles[fd].path, blob, function() {
      openedFiles[fd].dirty = false;
      if (openedFiles[fd].stat) {
        setStat(openedFiles[fd].path, openedFiles[fd].stat, cb);
      } else {
        cb();
      }
    });
  }

  function list(path, cb) {
    path = normalizePath(path);

    asyncStorage.getItem(path, function(files) {
      if (files == null || files instanceof Blob) {
        cb(null);
      } else {
        cb(files);
      }
    });
  }

  function exists(path, cb) {
    path = normalizePath(path);

    stat(path, function(stat) {
      cb(stat ? true : false);
    });
  }

  function truncate(path, cb) {
    path = normalizePath(path);

    stat(path, function(stat) {
      if (stat && !stat.isDir) {
        asyncStorage.setItem(path, new Blob(), function() {
          setStat(path, { mtime: Date.now(), isDir: false, size: 0 });
          cb(true);
        });
      } else {
        cb(false);
      }
    });
  }

  function ftruncate(fd, size) {
    var file = openedFiles[fd];
    if (size != file.buffer.contentSize) {
      file.buffer.setSize(size);
      file.dirty = true;
      fileStats[file.path] = file.stat = { mtime: Date.now(), isDir: false, size: size };
    }
  }

  function remove(path, cb) {
    path = normalizePath(path);

    if (openedFiles.findIndex(file => file && file.path === path) != -1) {
      setZeroTimeout(() => cb(false));
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
        asyncStorage.setItem(dir, files, function() {
          asyncStorage.removeItem(path, function() {
            removeStat(path, function() {
              cb(true);
            });
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
    path = normalizePath(path);

    createInternal(path, blob, function(created) {
      if (created) {
        setStat(path, { mtime: Date.now(), isDir: false, size: blob.size }, function() {
          cb(created);
        });
      } else {
        cb(created);
      }
    });
  }

  function mkdir(path, cb) {
    path = normalizePath(path);

    createInternal(path, [], function(created) {
      if (created) {
        setStat(path, { mtime: Date.now(), isDir: true }, function() {
          cb(created);
        });
      } else {
        cb(created);
      }
    });
  }

  function mkdirp(path, cb) {
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

    if (fileStats[path] && typeof fileStats[path].size != "undefined") {
      cb(fileStats[path].size);
      return;
    }

    asyncStorage.getItem(path, function(blob) {
      if (blob == null || !(blob instanceof Blob)) {
        cb(-1);
      } else {
        if (fileStats[path]) {
          fileStats[path].size = blob.size;
        }
        cb(blob.size);
      }
    });
  }

  // Callers of this function should make sure
  // newPath doesn't exist.
  function rename(oldPath, newPath, cb) {
    oldPath = normalizePath(oldPath);
    newPath = normalizePath(newPath);

    if (openedFiles.findIndex(file => file && file.path === oldPath) != -1) {
      setZeroTimeout(() => cb(false));
      return;
    }

    list(oldPath, function(files) {
      if (files != null && files.length > 0) {
        cb(false);
        return;
      }

      asyncStorage.getItem(oldPath, function(data) {
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

  function setStat(path, stat, cb) {
    fileStats[path] = stat;
    asyncStorage.setItem("!" + path, stat, cb);
  }

  function removeStat(path, cb) {
    delete fileStats[path];
    asyncStorage.removeItem("!" + path, cb);
  }

  function stat(path, cb) {
    path = normalizePath(path);

    var stat = fileStats[path];
    if (stat) {
      setTimeout(() => cb(stat), 0);
      return;
    }

    var file = openedFiles.find(file => file && file.stat && file.path === path);
    if (file) {
      setZeroTimeout(() => cb(file.stat));
      return;
    }

    asyncStorage.getItem("!" + path, function(stat) {
      if (stat) {
        fileStats[path] = stat;
      }
      cb(stat);
    });
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
  };
})();
