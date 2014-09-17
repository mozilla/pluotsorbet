/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

const RECORD_STORE_BASE = "/RecordStore";

Native["com/sun/midp/io/j2me/storage/File.initConfigRoot.(I)Ljava/lang/String;"] = function(ctx, stack) {
    var storageId = stack.pop();
    stack.push(ctx.newString("assets/" + storageId + "/"));
}

Native["com/sun/midp/midletsuite/MIDletSuiteStorage.getSecureFilenameBase.(I)Ljava/lang/String;"] = function(ctx, stack) {
    var id = stack.pop(), _this = stack.pop();
    stack.push(ctx.newString(""));
}

Native["com/sun/midp/rms/RecordStoreUtil.exists.(Ljava/lang/String;Ljava/lang/String;I)Z"] = function(ctx, stack) {
    var ext = stack.pop(), name = util.fromJavaString(stack.pop()), filenameBase = util.fromJavaString(stack.pop());

    var path = RECORD_STORE_BASE + "/" + filenameBase + "/" + name + "." + ext;

    fs.exists(path, function(exists) {
        stack.push(exists ? 1 : 0);
        ctx.resume();
    });

    throw VM.Pause;
}

Native["com/sun/midp/rms/RecordStoreUtil.deleteFile.(Ljava/lang/String;Ljava/lang/String;I)V"] = function(ctx, stack) {
    var ext = stack.pop(), name = util.fromJavaString(stack.pop()), filenameBase = util.fromJavaString(stack.pop());

    var path = RECORD_STORE_BASE + "/" + filenameBase + "/" + name + "." + ext;

    fs.remove(path, function(removed) {
        ctx.resume();
    });

    throw VM.Pause;
}

Native["com/sun/midp/rms/RecordStoreFile.spaceAvailableNewRecordStore0.(Ljava/lang/String;I)I"] = function(ctx, stack) {
    var storageId = stack.pop(), filenameBase = util.fromJavaString(stack.pop());

    // Pretend there is 50MiB available.  Our implementation is backed
    // by IndexedDB, which has no actual limit beyond space available on device,
    // which I don't think we can determine.  But this should be sufficient
    // to convince the MIDlet to use the API as needed.
    stack.push(50 * 1024 * 1024);
}

Native["com/sun/midp/rms/RecordStoreFile.spaceAvailableRecordStore.(ILjava/lang/String;I)I"] = function(ctx, stack) {
    var storageId = stack.pop(), filenameBase = util.fromJavaString(stack.pop()), handle = stack.pop();

    // Pretend there is 50MiB available.  Our implementation is backed
    // by IndexedDB, which has no actual limit beyond space available on device,
    // which I don't think we can determine.  But this should be sufficient
    // to convince the MIDlet to use the API as needed.
    stack.push(50 * 1024 * 1024);
}

Native["com/sun/midp/rms/RecordStoreFile.openRecordStoreFile.(Ljava/lang/String;Ljava/lang/String;I)I"] = function(ctx, stack) {
    var ext = stack.pop(), name = util.fromJavaString(stack.pop()), filenameBase = util.fromJavaString(stack.pop()), _this = stack.pop();

    var path = RECORD_STORE_BASE + "/" + filenameBase + "/" + name + "." + ext;

    function openCallback(fd) {
        if (fd == -1) {
            ctx.raiseException("java/io/IOException", "openRecordStoreFile: open failed");
        } else {
            stack.push(fd); // handle
        }
        ctx.resume();
    }

    fs.exists(path, function(exists) {
        if (exists) {
            fs.open(path, openCallback);
        } else {
            // Per the reference impl, create the file if it doesn't exist.
            var dirname = fs.dirname(path);
            fs.mkdirp(dirname, function(created) {
                if (created) {
                    fs.create(path, new Blob(), function(created) {
                        if (created) {
                            fs.open(path, openCallback);
                        }
                        else {
                            ctx.raiseException("java/io/IOException", "openRecordStoreFile: create failed");
                            ctx.resume();
                        }
                    });
                } else {
                    ctx.raiseException("java/io/IOException", "openRecordStoreFile: mkdirp failed");
                    ctx.resume();
                }
            });
        }
    });

    throw VM.Pause;
}

Native["com/sun/midp/rms/RecordStoreFile.setPosition.(II)V"] = function(ctx, stack) {
    var pos = stack.pop(), handle = stack.pop();

    fs.setpos(handle, pos);
}

Native["com/sun/midp/rms/RecordStoreFile.readBytes.(I[BII)I"] = function(ctx, stack) {
    var numBytes = stack.pop(), offset = stack.pop(), buf = stack.pop(), handle = stack.pop();

    var from = fs.getpos(handle);
    var to = from + numBytes;
    var readBytes = fs.read(handle, from, to);

    if (readBytes.byteLength <= 0) {
        ctx.raiseExceptionAndYield("java/io/IOException", "handle invalid or segment indices out of bounds");
    }

    var subBuffer = buf.subarray(offset, offset + readBytes.byteLength);
    for (var i = 0; i < readBytes.byteLength; i++) {
        subBuffer[i] = readBytes[i];
    }
    stack.push(readBytes.byteLength);
}

Native["com/sun/midp/rms/RecordStoreFile.writeBytes.(I[BII)V"] = function(ctx, stack) {
    var numBytes = stack.pop(), offset = stack.pop(), buf = stack.pop(), handle = stack.pop();
    fs.write(handle, buf.subarray(offset, offset + numBytes));
}

Native["com/sun/midp/rms/RecordStoreFile.commitWrite.(I)V"] = function(ctx, stack) {
    var handle = stack.pop();
    fs.flush(handle, function() {
        ctx.resume();
    });

    throw VM.Pause;
}

Native["com/sun/midp/rms/RecordStoreFile.closeFile.(I)V"] = function(ctx, stack) {
    var handle = stack.pop();

    fs.flush(handle, function() {
        fs.close(handle);
        ctx.resume();
    });

    throw VM.Pause;
}

Native["com/sun/midp/rms/RecordStoreFile.truncateFile.(II)V"] = function(ctx, stack) {
    var size = stack.pop(), handle = stack.pop();

    fs.flush(handle, function() {
        fs.ftruncate(handle, size);
        ctx.resume();
    });

    throw VM.Pause;
}

MIDP.RecordStoreCache = [];

Native["com/sun/midp/rms/RecordStoreSharedDBHeader.getLookupId0.(ILjava/lang/String;I)I"] = function(ctx, stack) {
    var headerDataSize = stack.pop(), storeName = util.fromJavaString(stack.pop()), suiteId = stack.pop();

    var sharedHeader =
        MIDP.RecordStoreCache.filter(function(v) { return (v && v.suiteId == suiteId && v.storeName == storeName); })[0];
    if (!sharedHeader) {
        sharedHeader = {
            suiteId: suiteId,
            storeName: storeName,
            headerVersion: 0,
            headerData: null,
            headerDataSize: headerDataSize,
            refCount: 0,
            // Use cache indices as IDs, so we can look up objects by index.
            lookupId: MIDP.RecordStoreCache.length,
        };
        MIDP.RecordStoreCache.push(sharedHeader);
    }
    ++sharedHeader.refCount;

    stack.push(sharedHeader.lookupId);
}

Native["com/sun/midp/rms/RecordStoreSharedDBHeader.shareCachedData0.(I[BI)I"] = function(ctx, stack) {
    var headerDataSize = stack.pop(), headerData = stack.pop(), lookupId = stack.pop();

    var sharedHeader = MIDP.RecordStoreCache[lookupId];
    if (!sharedHeader) {
        ctx.raiseExceptionAndYield("java/lang/IllegalStateException", "invalid header lookup ID");
    }

    if (!headerData) {
      ctx.raiseExceptionAndYield("java/lang/IllegalArgumentException", "header data is null");
    }

    var size = headerDataSize;
    if (size > sharedHeader.headerDataSize) {
        size = sharedHeader.headerDataSize;
    }
    sharedHeader.headerData = headerData.buffer.slice(0, size);
    ++sharedHeader.headerVersion;

    stack.push(sharedHeader.headerVersion);
}

Native["com/sun/midp/rms/RecordStoreSharedDBHeader.updateCachedData0.(I[BII)I"] = function(ctx, stack) {
    var headerVersion = stack.pop(), headerDataSize = stack.pop(), headerData = stack.pop(), lookupId = stack.pop();

    var sharedHeader = MIDP.RecordStoreCache[lookupId];
    if (!sharedHeader) {
        ctx.raiseExceptionAndYield("java/lang/IllegalStateException", "invalid header lookup ID");
    }

    if (!headerData) {
      ctx.raiseExceptionAndYield("java/lang/IllegalArgumentException", "header data is null");
    }

    if (sharedHeader.headerVersion > headerVersion && sharedHeader.headerData) {
        var size = sharedHeader.headerDataSize;
        if (size > headerDataSize) {
            size = headerDataSize;
        }
        for (var i = 0; i < size; i++) {
            headerData[i] = sharedHeader.headerData[i];
        }
        stack.push(sharedHeader.headerVersion);
    } else {
        stack.push(headerVersion);
    }
}

Native["com/sun/midp/rms/RecordStoreSharedDBHeader.getHeaderRefCount0.(I)I"] = function(ctx, stack) {
    var lookupId = stack.pop();

    var sharedHeader = MIDP.RecordStoreCache[lookupId];
    if (!sharedHeader) {
        ctx.raiseExceptionAndYield("java/lang/IllegalStateException", "invalid header lookup ID");
    }

    stack.push(sharedHeader.refCount);
}

Native["com/sun/midp/rms/RecordStoreSharedDBHeader.cleanup0.()V"] = function(ctx, stack) {
    var _this = stack.pop();

    for (var i = 0; i < MIDP.RecordStoreCache.length; i++) {
        if (MIDP.RecordStoreCache[i] == null) {
            continue;
        }
        if (--MIDP.RecordStoreCache[i].refCount <= 0) {
            // Set to null instead of removing from array to maintain
            // correspondence between lookup IDs and array indices.
            MIDP.RecordStoreCache[i] = null;
        }
    }
}

// In the reference implementation, finalize is identical to cleanup0.
Native["com/sun/midp/rms/RecordStoreSharedDBHeader.finalize.()V"] =
    Native["com/sun/midp/rms/RecordStoreSharedDBHeader.cleanup0.()V"];

Native["com/sun/midp/rms/RecordStoreRegistry.getRecordStoreListeners.(ILjava/lang/String;)[I"] = function(ctx, stack) {
    var storeName = util.fromJavaString(stack.pop()), suiteId = stack.pop();
    stack.push(null);
    console.warn("RecordStoreRegistry.getRecordStoreListeners.(IL...String;)[I not implemented (" +
                 suiteId + ", " + storeName + ")");
}

Native["com/sun/midp/rms/RecordStoreRegistry.sendRecordStoreChangeEvent.(ILjava/lang/String;II)V"] = function(ctx, stack) {
    var recordId = stack.pop(), changeType = stack.pop(), storeName = util.fromJavaString(stack.pop()), suiteId = stack.pop();
    console.warn("RecordStoreRegistry.sendRecordStoreChangeEvent.(IL...String;II)V not implemented (" +
                 suiteId + ", " + storeName + ", " + changeType + ", " + recordId + ")");
}

Native["com/sun/midp/rms/RecordStoreRegistry.startRecordStoreListening.(ILjava/lang/String;)V"] = function(ctx, stack) {
    var storeName = util.fromJavaString(stack.pop()), suiteId = stack.pop();
    console.warn("RecordStoreRegistry.startRecordStoreListening.(IL...String;)V not implemented (" +
                 suiteId + ", " + storeName + ")");
}

Native["com/sun/midp/rms/RecordStoreRegistry.stopRecordStoreListening.(ILjava/lang/String;)V"] = function(ctx, stack) {
    var storeName = util.fromJavaString(stack.pop()), suiteId = stack.pop();
    console.warn("RecordStoreRegistry.stopRecordStoreListening.(IL...String;)V not implemented (" +
                 suiteId + ", " + storeName + ")");
}

Native["com/sun/midp/rms/RecordStoreRegistry.stopAllRecordStoreListeners.(I)V"] = function(ctx, stack) {
    var taskId = stack.pop();
    console.warn("RecordStoreRegistry.stopAllRecordStoreListeners.(I)V not implemented (" + taskId + ")");
}

Native["com/ibm/oti/connection/file/Connection.isValidFilenameImpl.([B)Z"] = function(ctx, stack) {
    var path = stack.pop(), _this = stack.pop();

    var invalid = ['<', '>', ':', '"', '/', '\\', '|', '*', '?'].map(function(char) {
      return char.charCodeAt(0);
    });

    for (var i = 0; i < path.length; i++) {
        if (path[i] <= 31 || invalid.indexOf(path[i]) != -1) {
            stack.push(0);
            return;
        }
    }

    stack.push(1);
}

Native["com/ibm/oti/connection/file/Connection.availableSizeImpl.([B)J"] = function(ctx, stack) {
    var path = util.decodeUtf8(stack.pop()), _this = stack.pop();
    // Pretend there is 1 GB available
    stack.push2(Long.fromNumber(1024 * 1024 * 1024));
}

Native["com/ibm/oti/connection/file/Connection.setHiddenImpl.([BZ)V"] = function(ctx, stack) {
    var value = stack.pop(), path = util.decodeUtf8(stack.pop()), _this = stack.pop();
    console.warn("Connection.setHiddenImpl.([BZ)V not implemented (" + path + ")");
}

Native["com/ibm/oti/connection/file/Connection.existsImpl.([B)Z"] = function(ctx, stack) {
    var path = util.decodeUtf8(stack.pop()), _this = stack.pop();

    fs.exists(path, function(exists) {
        stack.push(exists ? 1 : 0);
        ctx.resume();
    });

    throw VM.Pause;
}

Native["com/ibm/oti/connection/file/Connection.fileSizeImpl.([B)J"] = function(ctx, stack) {
    var path = util.decodeUtf8(stack.pop()), _this = stack.pop();

    fs.size(path, function(size) {
        stack.push2(Long.fromNumber(size));
        ctx.resume();
    });

    throw VM.Pause;
}

Native["com/ibm/oti/connection/file/Connection.isDirectoryImpl.([B)Z"] = function(ctx, stack) {
    var path = util.decodeUtf8(stack.pop()), _this = stack.pop();

    fs.list(path, function(files) {
        stack.push(files ? 1 : 0);
        ctx.resume();
    });

    throw VM.Pause;
}

Native["com/ibm/oti/connection/file/Connection.listImpl.([B[BZ)[[B"] = function(ctx, stack) {
    var includeHidden = stack.pop(), filterArray = stack.pop(), path = util.decodeUtf8(stack.pop()),
        _this = stack.pop();

    var filter = "";
    if (filterArray) {
        filter = util.decodeUtf8(filterArray);
        if (filter.contains("?")) {
            console.warn("Our implementation of Connection::listImpl assumes the filter doesn't contain the ? wildcard character");
        }

        // Translate the filter to a regular expression

        // Escape regular expression (everything but * and ?)
        // Source of the regexp: https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide/Regular_Expressions
        filter = filter.replace(/([.+^${}()|\[\]\/\\])/g, "\\$1");

        // Transform * to .+
        filter = filter.replace(/\*/g, ".+");

        filter += "$";
    }

    fs.list(path, function(files) {
        var regexp = new RegExp(filter);

        files = files.filter(regexp.test.bind(regexp));

        var pathsArray = ctx.newArray("[[B", files.length);
        for (var i = 0; i < files.length; i++) {
            var curPath = "";
            if (path == "/") {
                curPath += files[i];
            } else {
                curPath += path.substring(1) + "/" + files[i];
            }

            var bytesCurPath = new TextEncoder("utf-8").encode(curPath);

            var pathArray = ctx.newPrimitiveArray("B", bytesCurPath.byteLength);
            pathArray.set(bytesCurPath);

            pathsArray[i] = pathArray;
        }

        stack.push(pathsArray);
        ctx.resume();
    });

    throw VM.Pause;
}

Native["com/ibm/oti/connection/file/Connection.mkdirImpl.([B)I"] = function(ctx, stack) {
    var path = util.decodeUtf8(stack.pop()), _this = stack.pop();

    // IBM's implementation returns different error numbers, we don't care

    fs.mkdir(path, function(created) {
        stack.push(created ? 0 : 42);
        ctx.resume();
    });

    throw VM.Pause;
}

Native["com/ibm/oti/connection/file/Connection.newFileImpl.([B)I"] = function(ctx, stack) {
    var path = util.decodeUtf8(stack.pop()), _this = stack.pop();

    // IBM's implementation returns different error numbers, we don't care

    fs.exists(path, function(exists) {
        if (exists) {
            fs.truncate(path, function(truncated) {
                stack.push(truncated ? 0 : 42);
                ctx.resume();
            });
        } else {
            fs.create(path, new Blob(), function(created) {
                stack.push(created ? 0 : 42);
                ctx.resume();
            });
        }
    });

    throw VM.Pause;
}

Native["com/ibm/oti/connection/file/Connection.deleteFileImpl.([B)Z"] =
Native["com/ibm/oti/connection/file/Connection.deleteDirImpl.([B)Z"] = function(ctx, stack) {
    var path = util.decodeUtf8(stack.pop());
    fs.remove(path, function(removed) {
        stack.push(removed ? 1 : 0);
        ctx.resume();
    });
    throw VM.Pause;
}

Native["com/ibm/oti/connection/file/Connection.isReadOnlyImpl.([B)Z"] = function(ctx, stack) {
    var path = util.decodeUtf8(stack.pop()), _this = stack.pop();
    stack.push(0);
    console.warn("Connection.isReadOnlyImpl.([B)Z not implemented (" + path + ")");
}

Native["com/ibm/oti/connection/file/Connection.isWriteOnlyImpl.([B)Z"] = function(ctx, stack) {
    var path = util.decodeUtf8(stack.pop()), _this = stack.pop();
    stack.push(0);
    console.warn("Connection.isWriteOnlyImpl.([B)Z not implemented (" + path + ")");
}

Native["com/ibm/oti/connection/file/Connection.lastModifiedImpl.([B)J"] = function(ctx, stack) {
    var path = util.decodeUtf8(stack.pop()), _this = stack.pop();
    stack.push2(Long.fromNumber(Date.now()));
    console.warn("Connection.lastModifiedImpl.([B)J not implemented");
}

Native["com/ibm/oti/connection/file/Connection.renameImpl.([B[B)V"] = function(ctx, stack) {
    var newPath = util.decodeUtf8(stack.pop()), oldPath = util.decodeUtf8(stack.pop()), _this = stack.pop();
    fs.rename(oldPath, newPath, function() {
      ctx.resume();
    });
    throw VM.Pause;
}

Native["com/ibm/oti/connection/file/Connection.truncateImpl.([BJ)V"] = function(ctx, stack) {
    var newLength = stack.pop2().toNumber(), path = util.decodeUtf8(stack.pop()), _this = stack.pop();

    // IBM's implementation returns different error numbers, we don't care

    fs.open(path, function(fd) {
      if (fd == -1) {
        ctx.raiseException("java/io/IOException", "truncate failed");
        ctx.resume();
      } else {
        var data = fs.read(fd);
        fs.truncate(path, function(truncated) {
          if (truncated) {
            fs.write(fd, data.subarray(0, newLength));
          } else {
            ctx.raiseException("java/io/IOException", "truncate failed");
          }
          ctx.resume();
        });
      }
    });

    throw VM.Pause;
}

Native["com/ibm/oti/connection/file/FCInputStream.openImpl.([B)I"] = function(ctx, stack) {
    var path = util.decodeUtf8(stack.pop()), _this = stack.pop();

    fs.open(path, function(fd) {
        stack.push(fd);
        ctx.resume();
    });

    throw VM.Pause;
}

Native["com/ibm/oti/connection/file/FCInputStream.availableImpl.(I)I"] = function(ctx, stack) {
    var fd = stack.pop(), _this = stack.pop();
    stack.push(fs.getsize(fd) - fs.getpos(fd));
}

Native["com/ibm/oti/connection/file/FCInputStream.skipImpl.(JI)J"] = function(ctx, stack) {
    var fd = stack.pop(), count = stack.pop2(), _this = stack.pop();

    var curpos = fs.getpos(fd);
    var size = fs.getsize(fd);
    if (curpos + count.toNumber() > size) {
        fs.setpos(fd, size);
        stack.push2(Long.fromNumber(size - curpos));
    } else {
        fs.setpos(fd, curpos + count.toNumber());
        stack.push2(count);
    }
}

Native["com/ibm/oti/connection/file/FCInputStream.readImpl.([BIII)I"] = function(ctx, stack) {
    var fd = stack.pop(), count = stack.pop(), offset = stack.pop(), buffer = stack.pop(), _this = stack.pop();

    if (offset < 0 || count < 0 || offset > buffer.byteLength || (buffer.byteLength - offset) < count) {
        ctx.raiseExceptionAndYield("java/lang/IndexOutOfBoundsException");
    }

    if (buffer.byteLength == 0 || count == 0) {
        stack.push(0);
        return;
    }

    var curpos = fs.getpos(fd);
    var data = fs.read(fd, curpos, curpos + count);
    buffer.set(data, offset);

    stack.push((data.byteLength > 0) ? data.byteLength : -1);
}

Native["com/ibm/oti/connection/file/FCInputStream.readByteImpl.(I)I"] = function(ctx, stack) {
    var fd = stack.pop(), _this = stack.pop();

    var curpos = fs.getpos(fd);

    var data = fs.read(fd, curpos, curpos+1);

    stack.push((data.byteLength > 0) ? data[0] : -1);
}

Native["com/ibm/oti/connection/file/FCInputStream.closeImpl.(I)V"] = function(ctx, stack) {
    var fd = stack.pop(), _this = stack.pop();

    if (fd >= 0) {
      fs.close(fd);
    }
}

Native["com/ibm/oti/connection/file/FCOutputStream.closeImpl.(I)V"] = function(ctx, stack) {
    var fd = stack.pop(), _this = stack.pop();

    if (fd <= -1) {
        return;
    }

    fs.flush(fd, function() {
        fs.close(fd);
        ctx.resume();
    });

    throw VM.Pause;
}

Native["com/ibm/oti/connection/file/FCOutputStream.openImpl.([B)I"] = function(ctx, stack) {
    var path = util.decodeUtf8(stack.pop()), _this = stack.pop();

    function open() {
        fs.open(path, function(fd) {
            stack.push(fd);
            ctx.resume();
        });
    }

    fs.exists(path, function(exists) {
        if (exists) {
            fs.truncate(path, function(truncated) {
                if (truncated) {
                    open();
                } else {
                    stack.push(-1);
                    ctx.resume();
                }
            });
        } else {
            fs.create(path, function(created) {
                if (created) {
                    open();
                } else {
                    stack.push(-1);
                    ctx.resume();
                }
            });
        }
    });

    throw VM.Pause;
}

Native["com/ibm/oti/connection/file/FCOutputStream.openOffsetImpl.([BJ)I"] = function(ctx, stack) {
    var offset = stack.pop2(), path = util.decodeUtf8(stack.pop()), _this = stack.pop();

    function open() {
        fs.open(path, function(fd) {
            stack.push(fd);
            fs.setpos(fd, offset.toNumber());
            ctx.resume();
        });
    }

    fs.exists(path, function(exists) {
        if (exists) {
            open();
        } else {
            fs.create(path, function(created) {
                if (created) {
                    open();
                } else {
                    stack.push(-1);
                    ctx.resume();
                }
            });
        }
    });

    throw VM.Pause;
}

Native["com/ibm/oti/connection/file/FCOutputStream.syncImpl.(I)V"] = function(ctx, stack) {
    var fd = stack.pop(), _this = stack.pop();

    fs.flush(fd, function() {
        ctx.resume();
    });

    throw VM.Pause;
}

Native["com/ibm/oti/connection/file/FCOutputStream.writeByteImpl.(II)V"] = function(ctx, stack) {
    var fd = stack.pop(), val = stack.pop(), _this = stack.pop();

    var buf = new Uint8Array(1);
    buf[0] = val;

    fs.write(fd, buf);
}

Native["com/ibm/oti/connection/file/FCOutputStream.writeImpl.([BIII)V"] = function(ctx, stack) {
    var fd = stack.pop(), count = stack.pop(), offset = stack.pop(), byteArray = stack.pop(), _this = stack.pop();

    fs.write(fd, byteArray.subarray(offset, offset+count));
}

Native["com/sun/midp/io/j2me/storage/RandomAccessStream.open.(Ljava/lang/String;I)I"] = function(ctx, stack) {
    var mode = stack.pop(), fileName = util.fromJavaString(stack.pop()), _this = stack.pop();

    var path = "/" + fileName;

    function open() {
        fs.open(path, function(fd) {
            if (fd == -1) {
                ctx.raiseException("java/io/IOException",
                                   "RandomAccessStream::open(" + fileName + ") failed opening the file");
            } else {
                stack.push(fd);
            }
            ctx.resume();
        });
    }

    fs.exists(path, function(exists) {
        if (exists) {
            open();
        } else {
            fs.create(path, new Blob(), function(created) {
                if (created) {
                    open();
                } else {
                    ctx.raiseException("java/io/IOException",
                                       "RandomAccessStream::open(" + fileName + ") failed creating the file");
                    ctx.resume();
                }
            });
        }
    });

    throw VM.Pause;
}

Native["com/sun/midp/io/j2me/storage/RandomAccessStream.read.(I[BII)I"] = function(ctx, stack) {
    var length = stack.pop(), offset = stack.pop(), buffer = stack.pop(), handle = stack.pop();

    var from = fs.getpos(handle);
    var to = from + length;
    var readBytes = fs.read(handle, from, to);

    if (readBytes.byteLength <= 0) {
        stack.push(-1);
        return;
    }

    var subBuffer = buffer.subarray(offset, offset + readBytes.byteLength);
    for (var i = 0; i < readBytes.byteLength; i++) {
        subBuffer[i] = readBytes[i];
    }
    stack.push(readBytes.byteLength);
}

Native["com/sun/midp/io/j2me/storage/RandomAccessStream.write.(I[BII)V"] = function(ctx, stack) {
    var length = stack.pop(), offset = stack.pop(), buffer = stack.pop(), handle = stack.pop();
    fs.write(handle, buffer.subarray(offset, offset + length));
}

Native["com/sun/midp/io/j2me/storage/RandomAccessStream.commitWrite.(I)V"] = function(ctx, stack) {
    var handle = stack.pop();

    fs.flush(handle, function() {
        ctx.resume();
    });

    throw VM.Pause;
}

Native["com/sun/midp/io/j2me/storage/RandomAccessStream.position.(II)V"] = function(ctx, stack) {
    var position = stack.pop(), handle = stack.pop();
    fs.setpos(handle, position);
}

Native["com/sun/midp/io/j2me/storage/RandomAccessStream.sizeOf.(I)I"] = function(ctx, stack) {
    var handle = stack.pop();

    var size = fs.getsize(handle);

    if (size == -1) {
        ctx.raiseExceptionAndYield("java/io/IOException", "RandomAccessStream::sizeOf(" + handle + ") failed");
    }

    stack.push(size);
}

Native["com/sun/midp/io/j2me/storage/RandomAccessStream.close.(I)V"] = function(ctx, stack) {
    var handle = stack.pop();

    fs.flush(handle, function() {
        fs.close(handle);
        ctx.resume();
    });

    throw VM.Pause;
}
