/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var RECORD_STORE_BASE = "/RecordStore";

// The filesystem roots, which are used by both FileSystemRegistry.getRootsImpl
// and System.getProperty to provide inquiring midlets with the list.  Each root
// must have a trailing slash.  See FileSystemRegistry.listRoots for more info.
MIDP.fsRoots = [
    "MemoryCard/",
    "Phone/",
    "Private/",
];
// The names here should be localized.
MIDP.fsRootNames = [
    "Memory card",
    "Phone memory",
    "Private",
];

function getAbsolutePath(jPath) {
    return "/" + util.decodeUtf8(jPath);
}

Native["com/sun/midp/io/j2me/storage/File.initConfigRoot.(I)Ljava/lang/String;"] = function(storageId) {
    return J2ME.newString("assets/" + storageId + "/");
};

Native["com/sun/midp/midletsuite/MIDletSuiteStorage.getSecureFilenameBase.(I)Ljava/lang/String;"] = function(id) {
    return J2ME.newString("");
};

Native["com/sun/midp/rms/RecordStoreUtil.exists.(Ljava/lang/String;Ljava/lang/String;I)Z"] =
function(filenameBase, name, ext) {
    asyncImpl("Z", new Promise(function(resolve, reject) {
        var path = RECORD_STORE_BASE + "/" + util.fromJavaString(filenameBase) + "/" + util.fromJavaString(name) + "." + ext;
        fs.exists(path, function(exists) { resolve(exists ? 1 : 0) });
    }));
};

Native["com/sun/midp/rms/RecordStoreUtil.deleteFile.(Ljava/lang/String;Ljava/lang/String;I)V"] =
function(filenameBase, name, ext) {
    asyncImpl("V", new Promise(function(resolve, reject) {
        var path = RECORD_STORE_BASE + "/" + util.fromJavaString(filenameBase) + "/" + util.fromJavaString(name) + "." + ext;

        fs.remove(path, resolve);
    }));
};

Native["com/sun/midp/rms/RecordStoreFile.spaceAvailableNewRecordStore0.(Ljava/lang/String;I)I"] = function(filenameBase, storageId) {
    // Pretend there is 50MiB available.  Our implementation is backed
    // by IndexedDB, which has no actual limit beyond space available on device,
    // which I don't think we can determine.  But this should be sufficient
    // to convince the MIDlet to use the API as needed.
    return 50 * 1024 * 1024;
};

Native["com/sun/midp/rms/RecordStoreFile.spaceAvailableRecordStore.(ILjava/lang/String;I)I"] = function(handle, filenameBase, storageId) {
    // Pretend there is 50MiB available.  Our implementation is backed
    // by IndexedDB, which has no actual limit beyond space available on device,
    // which I don't think we can determine.  But this should be sufficient
    // to convince the MIDlet to use the API as needed.
    return 50 * 1024 * 1024;
};

Native["com/sun/midp/rms/RecordStoreFile.openRecordStoreFile.(Ljava/lang/String;Ljava/lang/String;I)I"] =
function(filenameBase, name, ext) {
    var ctx = $.ctx;
    asyncImpl("I", new Promise(function(resolve, reject) {
        var path = RECORD_STORE_BASE + "/" + util.fromJavaString(filenameBase) + "/" + util.fromJavaString(name) + "." + ext;

        function openCallback(fd) {
            ctx.setAsCurrentContext();
            if (fd == -1) {
                reject($.newIOException("openRecordStoreFile: open failed"));
            } else {
                resolve(fd); // handle
            }
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
                            ctx.setAsCurrentContext();
                            if (created) {
                                fs.open(path, openCallback);
                            }
                            else {
                                reject($.newIOException("openRecordStoreFile: create failed"));
                            }
                        });
                    } else {
                        ctx.setAsCurrentContext();
                        reject($.newIOException("openRecordStoreFile: mkdirp failed"));
                    }
                });
            }
        });
    }));
};

Native["com/sun/midp/rms/RecordStoreFile.setPosition.(II)V"] = function(handle, pos) {
    fs.setpos(handle, pos);
};

Native["com/sun/midp/rms/RecordStoreFile.readBytes.(I[BII)I"] = function(handle, buf, offset, numBytes) {
    var from = fs.getpos(handle);
    var to = from + numBytes;
    var readBytes = fs.read(handle, from, to);

    if (readBytes.byteLength <= 0) {
        throw $.newIOException("handle invalid or segment indices out of bounds");
    }

    var subBuffer = buf.subarray(offset, offset + readBytes.byteLength);
    for (var i = 0; i < readBytes.byteLength; i++) {
        subBuffer[i] = readBytes[i];
    }
    return readBytes.byteLength;
};

Native["com/sun/midp/rms/RecordStoreFile.writeBytes.(I[BII)V"] = function(handle, buf, offset, numBytes) {
    fs.write(handle, buf.subarray(offset, offset + numBytes));
};

Native["com/sun/midp/rms/RecordStoreFile.commitWrite.(I)V"] = function(handle) {
    fs.flush(handle);
};

Native["com/sun/midp/rms/RecordStoreFile.closeFile.(I)V"] = function(handle) {
    fs.close(handle);
};

Native["com/sun/midp/rms/RecordStoreFile.truncateFile.(II)V"] = function(handle, size) {
    fs.flush(handle);
    fs.ftruncate(handle, size);
};

MIDP.RecordStoreCache = [];

Native["com/sun/midp/rms/RecordStoreSharedDBHeader.getLookupId0.(ILjava/lang/String;I)I"] =
function(suiteId, jStoreName, headerDataSize) {
    var storeName = util.fromJavaString(jStoreName);

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

    return sharedHeader.lookupId;
};

Native["com/sun/midp/rms/RecordStoreSharedDBHeader.shareCachedData0.(I[BI)I"] = function(lookupId, headerData, headerDataSize) {
    var sharedHeader = MIDP.RecordStoreCache[lookupId];
    if (!sharedHeader) {
        throw $.newIllegalStateException("invalid header lookup ID");
    }

    if (!headerData) {
        throw $.newIllegalArgumentException("header data is null");
    }

    var size = headerDataSize;
    if (size > sharedHeader.headerDataSize) {
        size = sharedHeader.headerDataSize;
    }
    sharedHeader.headerData = headerData.buffer.slice(0, size);
    ++sharedHeader.headerVersion;

    return sharedHeader.headerVersion;
};

Native["com/sun/midp/rms/RecordStoreSharedDBHeader.updateCachedData0.(I[BII)I"] =
function(lookupId, headerData, headerDataSize, headerVersion) {
    var sharedHeader = MIDP.RecordStoreCache[lookupId];
    if (!sharedHeader) {
        throw $.newIllegalStateException("invalid header lookup ID");
    }

    if (!headerData) {
        throw $.newIllegalArgumentException("header data is null");
    }

    if (sharedHeader.headerVersion > headerVersion && sharedHeader.headerData) {
        var size = sharedHeader.headerDataSize;
        if (size > headerDataSize) {
            size = headerDataSize;
        }
        var sharedHeaderData = new Int8Array(sharedHeader.headerData);
        for (var i = 0; i < size; i++) {
            headerData[i] = sharedHeaderData[i];
        }
        return sharedHeader.headerVersion;
    }

    return headerVersion;
};

Native["com/sun/midp/rms/RecordStoreSharedDBHeader.getHeaderRefCount0.(I)I"] = function(lookupId) {
    var sharedHeader = MIDP.RecordStoreCache[lookupId];
    if (!sharedHeader) {
        throw $.newIllegalStateException("invalid header lookup ID");
    }

    return sharedHeader.refCount;
};

Native["com/sun/midp/rms/RecordStoreSharedDBHeader.cleanup0.()V"] = function() {
    var lookupId = this.klass.classInfo.getField("I.lookupId.I").get(this);
    if (MIDP.RecordStoreCache[lookupId] &&
        --MIDP.RecordStoreCache[lookupId].refCount <= 0) {
        // Set to null instead of removing from array to maintain
        // correspondence between lookup IDs and array indices.
        MIDP.RecordStoreCache[lookupId] = null;
    }
};

// In the reference implementation, finalize is identical to cleanup0.
Native["com/sun/midp/rms/RecordStoreSharedDBHeader.finalize.()V"] =
    Native["com/sun/midp/rms/RecordStoreSharedDBHeader.cleanup0.()V"];

Native["com/sun/midp/rms/RecordStoreRegistry.getRecordStoreListeners.(ILjava/lang/String;)[I"] =
function(suiteId, storeName) {
    console.warn("RecordStoreRegistry.getRecordStoreListeners.(IL...String;)[I not implemented (" +
                 suiteId + ", " + util.fromJavaString(storeName) + ")");
    return null;
};

Native["com/sun/midp/rms/RecordStoreRegistry.sendRecordStoreChangeEvent.(ILjava/lang/String;II)V"] =
function(suiteId, storeName, changeType, recordId) {
    console.warn("RecordStoreRegistry.sendRecordStoreChangeEvent.(IL...String;II)V not implemented (" +
                 suiteId + ", " + util.fromJavaString(storeName) + ", " + changeType + ", " + recordId + ")");
};

Native["com/sun/midp/rms/RecordStoreRegistry.startRecordStoreListening.(ILjava/lang/String;)V"] =
function(suiteId, storeName) {
    console.warn("RecordStoreRegistry.startRecordStoreListening.(IL...String;)V not implemented (" +
                 suiteId + ", " + util.fromJavaString(storeName) + ")");
};

Native["com/sun/midp/rms/RecordStoreRegistry.stopRecordStoreListening.(ILjava/lang/String;)V"] =
function(suiteId, storeName) {
    console.warn("RecordStoreRegistry.stopRecordStoreListening.(IL...String;)V not implemented (" +
                 suiteId + ", " + util.fromJavaString(storeName) + ")");
};

Native["com/sun/midp/rms/RecordStoreRegistry.stopAllRecordStoreListeners.(I)V"] = function(taskId) {
    console.warn("RecordStoreRegistry.stopAllRecordStoreListeners.(I)V not implemented (" + taskId + ")");
};

Native["com/ibm/oti/connection/file/Connection.isValidFilenameImpl.([B)Z"] = function(path) {
    var invalid = ['<', '>', ':', '"', '/', '\\', '|', '*', '?'].map(function(char) {
      return char.charCodeAt(0);
    });

    for (var i = 0; i < path.length; i++) {
        if (path[i] <= 31 || invalid.indexOf(path[i]) != -1) {
            return 0;
        }
    }

    return 1;
};

Native["com/ibm/oti/connection/file/Connection.totalSizeImpl.([B)J"] = function(root) {
    console.warn("Connection.totalSizeImpl.([B)J not implemented (" + util.decodeUtf8(root) + ")");
    return Long.fromNumber(-1);
};

Native["com/ibm/oti/connection/file/Connection.usedSizeImpl.([B)J"] = function(root) {
    console.warn("Connection.usedSizeImpl.([B)J not implemented (" + util.decodeUtf8(root) + ")");
    return Long.fromNumber(-1);
};

Native["com/ibm/oti/connection/file/Connection.availableSizeImpl.([B)J"] = function(root) {
    console.warn("Connection.availableSizeImpl.([B)J not implemented (" + util.decodeUtf8(root) + ")");
    // Pretend there is 1 GB available
    return Long.fromNumber(1024 * 1024 * 1024);
};

Native["com/ibm/oti/connection/file/Connection.setHiddenImpl.([BZ)V"] = function(path, value) {
    console.warn("Connection.setHiddenImpl.([BZ)V not implemented (" + util.decodeUtf8(path) + ")");
};

Native["com/ibm/oti/connection/file/Connection.setReadOnlyImpl.([BZ)V"] = function(path, value) {
    console.warn("Connection.setReadOnlyImpl.([BZ)V not implemented (" + util.decodeUtf8(path) + ")");
};

Native["com/ibm/oti/connection/file/Connection.setWriteOnlyImpl.([BZ)V"] = function(path, value) {
    console.warn("Connection.setWriteOnlyImpl.([BZ)V not implemented (" + util.decodeUtf8(path) + ")");
};

Native["com/ibm/oti/connection/file/Connection.existsImpl.([B)Z"] = function(path) {
    asyncImpl("Z", new Promise(function(resolve, reject) {
        fs.exists(getAbsolutePath(path), function(exists) { resolve(exists ? 1 : 0); } );
    }));
};

Native["com/ibm/oti/connection/file/Connection.directorySizeImpl.([BZ)J"] = function(path, includeSubDirs) {
    console.warn("Connection.directorySizeImpl.([BZ)J not implemented (" + getAbsolutePath(path) + ", " + includeSubDirs + ")");
    return Long.fromNumber(0);
};

Native["com/ibm/oti/connection/file/Connection.fileSizeImpl.([B)J"] = function(path) {
    asyncImpl("J", new Promise(function(resolve, reject) {
        fs.size(getAbsolutePath(path), function(size) {
            resolve(Long.fromNumber(size));
        });
    }));
};

Native["com/ibm/oti/connection/file/Connection.isDirectoryImpl.([B)Z"] = function(path) {
    asyncImpl("Z", new Promise(function(resolve, reject) {
        fs.stat(getAbsolutePath(path), function(stat) {
            resolve(!!stat && stat.isDir ? 1 : 0);
        });
    }));
};

Native["com/ibm/oti/connection/file/Connection.listImpl.([B[BZ)[[B"] =
function(jPath, filterArray, includeHidden) {
    var path = getAbsolutePath(jPath);
    var ctx = $.ctx;
    asyncImpl("[[B", new Promise(function(resolve, reject) {
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
            filter = filter.replace(/\*/g, ".*");

            // Require filter to match from the beginning to the end.
            filter = "^" + filter + "$";
        }

        fs.list(path, function(error, files) {
            // For these exceptions, we append a URL representation of the path
            // in Connection.listInternal, so we don't have to implement getURL
            // in native code.
            if (error && error.message == "Path does not exist") {
                ctx.setAsCurrentContext();
                return reject($.newIOException("Directory does not exist: "));
            }
            if (error && error.message == "Path is not a directory") {
                ctx.setAsCurrentContext();
                return reject($.newIOException("Connection is open on a file: "));
            }

            var regexp = new RegExp(filter);
            files = files.filter(regexp.test.bind(regexp));
            var filesArray = J2ME.newArray(J2ME.PrimitiveArrayClassInfo.B.klass, files.length);
            var encoder = new TextEncoder("utf-8");

            files.forEach(function(file, i) {
                var bytesFile = encoder.encode(file);
                var fileArray = util.newPrimitiveArray("B", bytesFile.byteLength);
                fileArray.set(bytesFile);
                filesArray[i] = fileArray;
            });

            resolve(filesArray);
        });
    }));
};

Native["com/ibm/oti/connection/file/Connection.mkdirImpl.([B)I"] = function(path) {
    asyncImpl("I", new Promise(function(resolve, reject) {
        fs.mkdir(getAbsolutePath(path), function(created) {
            // IBM's implementation returns different error numbers, we don't care
            resolve(created ? 0 : 42);
        });
    }));
};

Native["com/ibm/oti/connection/file/Connection.newFileImpl.([B)I"] = function(jPath) {
    var path = getAbsolutePath(jPath);

    asyncImpl("I", new Promise(function(resolve, reject) {
        fs.stat(path, function(stat) {
            if (stat !== null) {
                resolve(stat.isDir ? 3 : 1);
            } else {
                fs.create(path, new Blob(), function(created) {
                    resolve(created ? 0 : 42);
                });
            }
        });
    }));
};

Native["com/ibm/oti/connection/file/Connection.deleteFileImpl.([B)Z"] = function(path) {
    asyncImpl("Z", new Promise(function(resolve, reject) {
        fs.remove(getAbsolutePath(path), function(removed) { resolve(removed ? 1 : 0); });
    }));
};

Native["com/ibm/oti/connection/file/Connection.deleteDirImpl.([B)Z"] =
  Native["com/ibm/oti/connection/file/Connection.deleteFileImpl.([B)Z"];

Native["com/ibm/oti/connection/file/Connection.isReadOnlyImpl.([B)Z"] = function(path) {
    console.warn("Connection.isReadOnlyImpl.([B)Z not implemented (" + getAbsolutePath(path) + ")");
    return 0;
};

Native["com/ibm/oti/connection/file/Connection.isWriteOnlyImpl.([B)Z"] = function(path) {
    console.warn("Connection.isWriteOnlyImpl.([B)Z not implemented (" + getAbsolutePath(path) + ")");
    return 0;
};

Native["com/ibm/oti/connection/file/Connection.lastModifiedImpl.([B)J"] = function(path) {
    asyncImpl("J", new Promise(function(resolve, reject) {
        fs.stat(getAbsolutePath(path), function(stat) {
            resolve(Long.fromNumber(stat != null ? stat.mtime : 0));
        });
    }));
};

Native["com/ibm/oti/connection/file/Connection.renameImpl.([B[B)V"] = function(oldPath, newPath) {
    var ctx = $.ctx;
    asyncImpl("V", new Promise(function(resolve, reject) {
        fs.rename(getAbsolutePath(oldPath), getAbsolutePath(newPath), function(renamed) {
            ctx.setAsCurrentContext();
            if (!renamed) {
                reject($.newIOException("Rename failed"));
                return;
            }

            resolve();
        });
    }));
};

Native["com/ibm/oti/connection/file/Connection.truncateImpl.([BJ)V"] = function(path, newLength) {
    var ctx = $.ctx;
    asyncImpl("V", new Promise(function(resolve, reject) {
        fs.open(getAbsolutePath(path), function(fd) {
          ctx.setAsCurrentContext();
          if (fd == -1) {
            reject($.newIOException("truncate failed"));
            return;
          }

          fs.ftruncate(fd, newLength.toNumber());
          fs.close(fd);
          resolve();
        });
    }));
};

Native["com/ibm/oti/connection/file/FCInputStream.openImpl.([B)I"] = function(path) {
    asyncImpl("I", new Promise(function(resolve, reject) {
      fs.open(getAbsolutePath(path), resolve);
    }));
};

Native["com/ibm/oti/connection/file/FCInputStream.availableImpl.(I)I"] = function(fd) {
    return fs.getsize(fd) - fs.getpos(fd);
};

Native["com/ibm/oti/connection/file/FCInputStream.skipImpl.(JI)J"] = function(count, fd) {
    var curpos = fs.getpos(fd);
    var size = fs.getsize(fd);
    if (curpos + count.toNumber() > size) {
        fs.setpos(fd, size);
        return Long.fromNumber(size - curpos);
    }

    fs.setpos(fd, curpos + count.toNumber());
    return count;
};

Native["com/ibm/oti/connection/file/FCInputStream.readImpl.([BIII)I"] = function(buffer, offset, count, fd) {
    if (offset < 0 || count < 0 || offset > buffer.byteLength || (buffer.byteLength - offset) < count) {
        throw $.newIndexOutOfBoundsException();
    }

    if (buffer.byteLength == 0 || count == 0) {
        return 0;
    }

    var curpos = fs.getpos(fd);
    var data = fs.read(fd, curpos, curpos + count);
    buffer.set(data, offset);

    return (data.byteLength > 0) ? data.byteLength : -1;
};

Native["com/ibm/oti/connection/file/FCInputStream.readByteImpl.(I)I"] = function(fd) {
    var curpos = fs.getpos(fd);

    var data = fs.read(fd, curpos, curpos+1);

    return (data.byteLength > 0) ? data[0] : -1;
};

Native["com/ibm/oti/connection/file/FCInputStream.closeImpl.(I)V"] = function(fd) {
    if (fd >= 0) {
      fs.close(fd);
    }
};

Native["com/ibm/oti/connection/file/FCOutputStream.closeImpl.(I)V"] = function(fd) {
    fs.close(fd);
};

Native["com/ibm/oti/connection/file/FCOutputStream.openImpl.([B)I"] = function(jPath) {
    var path = getAbsolutePath(jPath);

    asyncImpl("I", new Promise(function(resolve, reject) {
        fs.exists(path, function(exists) {
            if (exists) {
                fs.open(path, function(fd) {
                    if (fd != -1) {
                        fs.ftruncate(fd, 0);
                    }
                    resolve(fd);
                });
            } else {
                fs.create(path, new Blob(), function(created) {
                    if (created) {
                        fs.open(path, resolve);
                    } else {
                        resolve(-1);
                    }
                });
            }
        });
    }));
};

Native["com/ibm/oti/connection/file/FCOutputStream.openOffsetImpl.([BJ)I"] = function(jPath, offset) {
    var path = getAbsolutePath(jPath);

    asyncImpl("I", new Promise(function(resolve, reject) {
        function open() {
            fs.open(path, function(fd) {
                fs.setpos(fd, offset.toNumber());
                resolve(fd);
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
                        resolve(-1);
                    }
                });
            }
        });
    }));
};

Native["com/ibm/oti/connection/file/FCOutputStream.syncImpl.(I)V"] = function(fd) {
    fs.flush(fd);
};

Native["com/ibm/oti/connection/file/FCOutputStream.writeByteImpl.(II)V"] = function(val, fd) {
    var buf = new Uint8Array(1);
    buf[0] = val;
    fs.write(fd, buf);
};

Native["com/ibm/oti/connection/file/FCOutputStream.writeImpl.([BIII)V"] =
function(byteArray, offset, count, fd) {
    fs.write(fd, byteArray.subarray(offset, offset+count));
};

Native["com/sun/midp/io/j2me/storage/RandomAccessStream.open.(Ljava/lang/String;I)I"] = function(fileName, mode) {
    var path = "/" + util.fromJavaString(fileName);

    var ctx = $.ctx;
    asyncImpl("I", new Promise(function(resolve, reject) {
        function open() {
            fs.open(path, function(fd) {
                ctx.setAsCurrentContext();
                if (fd == -1) {
                    reject($.newIOException("RandomAccessStream::open(" + path + ") failed opening the file"));
                } else {
                    resolve(fd);
                }
            });
        }

        fs.exists(path, function(exists) {
            ctx.setAsCurrentContext();
            if (exists) {
                open();
            } else if (mode == 1) {
                reject($.newIOException("RandomAccessStream::open(" + path + ") file doesn't exist"));
            } else {
                fs.create(path, new Blob(), function(created) {
                    ctx.setAsCurrentContext();
                    if (created) {
                        open();
                    } else {
                        reject($.newIOException("RandomAccessStream::open(" + path + ") failed creating the file"));
                    }
                });
            }
        });
    }));
};

Native["com/sun/midp/io/j2me/storage/RandomAccessStream.read.(I[BII)I"] =
function(handle, buffer, offset, length) {
    var from = fs.getpos(handle);
    var to = from + length;
    var readBytes = fs.read(handle, from, to);

    if (readBytes.byteLength <= 0) {
        return -1;
    }

    var subBuffer = buffer.subarray(offset, offset + readBytes.byteLength);
    for (var i = 0; i < readBytes.byteLength; i++) {
        subBuffer[i] = readBytes[i];
    }
    return readBytes.byteLength;
};

Native["com/sun/midp/io/j2me/storage/RandomAccessStream.write.(I[BII)V"] =
function(handle, buffer, offset, length) {
    fs.write(handle, buffer.subarray(offset, offset + length));
};

Native["com/sun/midp/io/j2me/storage/RandomAccessStream.commitWrite.(I)V"] = function(handle) {
    fs.flush(handle);
};

Native["com/sun/midp/io/j2me/storage/RandomAccessStream.position.(II)V"] = function(handle, position) {
    fs.setpos(handle, position);
};

Native["com/sun/midp/io/j2me/storage/RandomAccessStream.sizeOf.(I)I"] = function(handle) {
    var size = fs.getsize(handle);

    if (size == -1) {
        throw $.newIOException("RandomAccessStream::sizeOf(" + handle + ") failed");
    }

    return size;
};

Native["com/sun/midp/io/j2me/storage/RandomAccessStream.close.(I)V"] = function(handle) {
        fs.close(handle);
};

Native["javax/microedition/io/file/FileSystemRegistry.initImpl.()V"] = function() {
    console.warn("javax/microedition/io/file/FileSystemRegistry.initImpl.()V not implemented");
};

Native["javax/microedition/io/file/FileSystemRegistry.getRootsImpl.()[Ljava/lang/String;"] = function() {
    var array = J2ME.newStringArray(MIDP.fsRoots.length);

    for (var i = 0; i < MIDP.fsRoots.length; i++) {
        array[i] = J2ME.newString(MIDP.fsRoots[i]);
    }

    return array;
};
