'use strict';

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
