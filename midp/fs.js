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

Native["com/sun/midp/io/j2me/storage/File.initStorageRoot.(I)Ljava/lang/String;"] = function(storageId) {
    return J2ME.newString("assets/" + storageId + "/");
};

Native["com/sun/midp/midletsuite/MIDletSuiteStorage.getSecureFilenameBase.(I)Ljava/lang/String;"] = function(id) {
    return J2ME.newString("");
};

Native["com/sun/midp/rms/RecordStoreUtil.exists.(Ljava/lang/String;Ljava/lang/String;I)Z"] =
function(filenameBase, name, ext) {
    var path = RECORD_STORE_BASE + "/" + util.fromJavaString(filenameBase) + "/" + util.fromJavaString(name) + "." + ext;
    return fs.exists(path) ? 1 : 0;
};

Native["com/sun/midp/rms/RecordStoreUtil.deleteFile.(Ljava/lang/String;Ljava/lang/String;I)V"] =
function(filenameBase, name, ext) {
    var path = RECORD_STORE_BASE + "/" + util.fromJavaString(filenameBase) + "/" + util.fromJavaString(name) + "." + ext;
    fs.remove(path);
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


        if (fs.exists(path)) {
            fs.open(path, openCallback);
        } else {
            // Per the reference impl, create the file if it doesn't exist.
            var dirname = fs.dirname(path);
            if (fs.mkdirp(dirname)) {
                if (fs.create(path, new Blob())) {
                    fs.open(path, openCallback);
                } else {
                    // TODO: determine if this is actually necessary, as I think
                    // we're still in synchronous execution of the native here.
                    ctx.setAsCurrentContext();

                    reject($.newIOException("openRecordStoreFile: create failed"));
                }
            } else {
                // TODO: determine if this is actually necessary, as I think
                // we're still in synchronous execution of the native here.
                ctx.setAsCurrentContext();

                reject($.newIOException("openRecordStoreFile: mkdirp failed"));
            }
        }
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

Native["com/ibm/oti/connection/file/Connection.existsImpl.([B)Z"] = function(path) {
    return fs.exists(getAbsolutePath(path)) ? 1 : 0;
};

Native["com/ibm/oti/connection/file/Connection.fileSizeImpl.([B)J"] = function(path) {
    return Long.fromNumber(fs.size(getAbsolutePath(path)));
};

Native["com/ibm/oti/connection/file/Connection.isDirectoryImpl.([B)Z"] = function(path) {
    var stat = fs.stat(getAbsolutePath(path));
    return !!stat && stat.isDir ? 1 : 0;
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

Native["com/ibm/oti/connection/file/Connection.directorySizeImpl.([BZ)J"] = function(path, includeSubDirs) {
    console.warn("Connection.directorySizeImpl.([BZ)J not implemented (" + getAbsolutePath(path) + ", " + includeSubDirs + ")");
    return Long.fromNumber(0);
};

Native["com/ibm/oti/connection/file/Connection.listImpl.([B[BZ)[[B"] = function(jPath, filterArray, includeHidden) {
    var path = getAbsolutePath(jPath);
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

        // Transform * to .*
        filter = filter.replace(/\*/g, ".*");

        // Require filter to match from the beginning to the end.
        filter = "^" + filter + "$";
    }

    var files;

    try {
        files = fs.list(path);
    } catch (ex) {
        // For these exceptions, we append a URL representation of the path
        // in Connection.listInternal, so we don't have to implement getURL
        // in native code.
        if (ex.message == "Path does not exist") {
            throw $.newIOException("Directory does not exist: ");
        }
        if (ex.message == "Path is not a directory") {
            throw $.newIOException("Connection is open on a file: ");
        }
    }

    var regexp = new RegExp(filter);
    files = files.filter(regexp.test.bind(regexp));
    var filesArray = J2ME.newArray(J2ME.PrimitiveArrayClassInfo.B.klass, files.length);
    var encoder = new TextEncoder("utf-8");

    files.forEach(function(file, i) {
        var bytesFile = encoder.encode(file);
        var fileArray = J2ME.newByteArray(bytesFile.byteLength);
        fileArray.set(bytesFile);
        filesArray[i] = fileArray;
    });

    return filesArray;
};

Native["com/ibm/oti/connection/file/Connection.mkdirImpl.([B)I"] = function(path) {
    // IBM's implementation returns different error numbers, we don't care
    return fs.mkdir(getAbsolutePath(path)) ? 0 : 42;
};

Native["com/ibm/oti/connection/file/Connection.newFileImpl.([B)I"] = function(jPath) {
    var path = getAbsolutePath(jPath);

    var stat = fs.stat(path);
    if (stat !== null) {
        return stat.isDir ? 3 : 1;
    }

    return fs.create(path, new Blob()) ? 0 : 42;
};

Native["com/ibm/oti/connection/file/Connection.deleteFileImpl.([B)Z"] = function(path) {
    return fs.remove(getAbsolutePath(path)) ? 1 : 0;
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
    var stat = fs.stat(getAbsolutePath(path));
    return Long.fromNumber(stat != null ? stat.mtime : 0);
};

Native["com/ibm/oti/connection/file/Connection.renameImpl.([B[B)V"] = function(oldPath, newPath) {
    if (!fs.rename(getAbsolutePath(oldPath), getAbsolutePath(newPath))) {
        throw $.newIOException("Rename failed");
    }
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

    return (data.byteLength > 0) ? (data[0] & 0xFF) : -1;
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
        if (fs.exists(path)) {
            fs.open(path, function(fd) {
                if (fd != -1) {
                    fs.ftruncate(fd, 0);
                }
                resolve(fd);
            });
        } else if (fs.create(path, new Blob())) {
            fs.open(path, resolve);
        } else {
            resolve(-1);
        }
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

        if (fs.exists(path)) {
            open();
        } else if (fs.create(path, new Blob())) {
            open();
        } else {
            resolve(-1);
        }
    }));
};

Native["com/ibm/oti/connection/file/FCOutputStream.syncImpl.(I)V"] = function(fd) {
    fs.flush(fd);
};

Native["com/ibm/oti/connection/file/FCOutputStream.writeByteImpl.(II)V"] = function(val, fd) {
    var buf = new Int8Array(1);
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

        if (fs.exists(path)) {
            open();
        } else if (mode == 1) {
            ctx.setAsCurrentContext();
            reject($.newIOException("RandomAccessStream::open(" + path + ") file doesn't exist"));
        } else if (fs.create(path, new Blob())) {
            open();
        } else {
            ctx.setAsCurrentContext();
            reject($.newIOException("RandomAccessStream::open(" + path + ") failed creating the file"));
        }
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

addUnimplementedNative("com/sun/cdc/io/j2me/file/DefaultFileHandler.initialize.()V");

Native["com/sun/cdc/io/j2me/file/DefaultFileHandler.illegalFileNameChars0.()Ljava/lang/String;"] = function() {
    return J2ME.newString('<>:"\\|?');
};

Native["com/sun/cdc/io/j2me/file/DefaultFileHandler.getFileSeparator.()C"] = function() {
    return "/".charCodeAt(0);
}

Native["com/sun/cdc/io/j2me/file/DefaultFileHandler.getNativePathForRoot.(Ljava/lang/String;)Ljava/lang/String;"] =
function(root) {
// XXX Ensure root is in MIDP.fsRoots?
console.log("getNativePathForRoot: " + util.fromJavaString(root));
    var nativePath = J2ME.newString("/" + util.fromJavaString(root));
    return nativePath;
};

MIDP.nativeFileNameToPointer = new Map();
MIDP.nativeFilePointerToName = new Map();
MIDP.lastNativeFilePointer = 0;
Native["com/sun/cdc/io/j2me/file/DefaultFileHandler.getNativeName.(Ljava/lang/String;J)J"] = function(name, oldName) {
    name = fs.normalize(util.fromJavaString(name));
    var pointer;
    if (MIDP.nativeFileNameToPointer.has(name)) {
        pointer = MIDP.nativeFileNameToPointer.get(name);
    } else {
        pointer = ++MIDP.lastNativeFilePointer;
        MIDP.nativeFileNameToPointer.set(name, pointer);
        MIDP.nativeFilePointerToName.set(pointer, name);
    }
console.log(name + " has pointer " + pointer);
    return Long.fromNumber(pointer);
};

Native["com/sun/cdc/io/j2me/file/DefaultFileHandler.getSuiteIdString.(I)Ljava/lang/String;"] = function(id) {
console.log("getSuiteIdString: " + id);
    // return J2ME.newString(id.toString());
    // The implementation adds this to the path of the file, presumably
    // to segregate files by midlet, but we only run a single midlet
    // per installation, so presumably we don't have to do that.
    return J2ME.newString("");
};

Native["com/sun/cdc/io/j2me/file/DefaultFileHandler.exists.()Z"] = function() {
    // console.log("exists: " + [p for (p in this)].join(","));
    // console.log("exists: " + (this["$fileName"].toNumber()));
    var pathname = util.fromJavaString(this.$nativePath);
    var exists = fs.exists(pathname);
    console.log("DefaultFileHandler.exists: " + pathname + " " + exists);
    return exists ? 1 : 0;
};

Native["com/sun/cdc/io/j2me/file/DefaultFileHandler.mkdir.()V"] = function() {
    var pathname = util.fromJavaString(this.$nativePath);
    console.log("DefaultFileHandler.mkdir: " + pathname);

    if (!fs.mkdir(pathname)) {
        throw $.newIOException("error creating " + pathname);
    };
};

Native["com/sun/cdc/io/j2me/file/DefaultFileHandler.getMountedRoots.()Ljava/lang/String;"] = function() {
    return J2ME.newString(MIDP.fsRoots.join("\n"));
};

Native["com/sun/cdc/io/j2me/file/DefaultFileHandler.isDirectory.()Z"] = function() {
    var pathname = util.fromJavaString(this.$nativePath);
    var stat = fs.stat(pathname);
    var isDirectory = !!stat && stat.isDir;
    console.log("DefaultFileHandler.isDirectory: " + pathname + " " + (isDirectory));
    return isDirectory ? 1 : 0;
}

MIDP.openDirs = new Map();
MIDP.openDirHandle = 0;

Native["com/sun/cdc/io/j2me/file/DefaultFileHandler.openDir.()J"] = function() {
    var pathname = util.fromJavaString(this.$nativePath);
    console.log("DefaultFileHandler.openDir: " + pathname);

    try {
        var files = fs.list(pathname);
    } catch(ex) {
        if (ex.message == "Path does not exist") {
            throw $.newIOException("Directory does not exist: file://" + pathname);
        }
        if (ex.message == "Path is not a directory") {
            throw $.newIOException("Connection is open on a file: file://" + pathname);
        }
    }

    var openDirHandle = ++MIDP.openDirHandle;

    MIDP.openDirs.set(openDirHandle, {
        files: files,
        index: -1,
    });

    return Long.fromNumber(openDirHandle);
};

Native["com/sun/cdc/io/j2me/file/DefaultFileHandler.closeDir.(J)V"] = function(dirHandle) {
    MIDP.openDirs.delete(dirHandle.toNumber());
};

Native["com/sun/cdc/io/j2me/file/DefaultFileHandler.dirGetNextFile.(JZ)Ljava/lang/String;"] =
function(dirHandle, includeHidden) {
    var iterator = MIDP.openDirs.get(dirHandle.toNumber());
    var nextFile = iterator.files[++iterator.index];
console.log(iterator.index + " " + nextFile);
    return nextFile ? J2ME.newString(nextFile) : null;
};

Native["com/sun/cdc/io/j2me/file/DefaultFileHandler.create.()V"] = function() {
    var pathname = util.fromJavaString(this.$nativePath);
    console.log("DefaultFileHandler.create: " + pathname);

    var stat = fs.stat(pathname);

    if (stat !== null || !fs.create(pathname, new Blob())) {
        throw $.newIOException("error creating " + pathname);
    }
};

Native["com/sun/cdc/io/j2me/file/DefaultFileHandler.fileSize.()J"] = function() {
    var pathname = util.fromJavaString(this.$nativePath);
    console.log("DefaultFileHandler.fileSize: " + pathname);

    return Long.fromNumber(fs.size(pathname));
};

Native["com/sun/cdc/io/j2me/file/DefaultFileHandler.canWrite.()Z"] = function() {
    var pathname = util.fromJavaString(this.$nativePath);
    console.log("DefaultFileHandler.canWrite: " + pathname);
    return fs.exists(pathname) ? 1 : 0;
};

MIDP.openFiles = new Map();

Native["com/sun/cdc/io/j2me/file/DefaultFileHandler.openForWrite.()V"] = function() {
    var pathname = util.fromJavaString(this.$nativePath);
    console.log("DefaultFileHandler.openForWrite: " + pathname);

    var fileNumber = this.$fileName;
    if (MIDP.openFiles.has(fileNumber)) {
        // Already open.
        return;
    }

    var stat = fs.stat(pathname);
    if (!stat) {
        throw $.newIOException("file doesn't exist");
    }
    if (stat.isDir) {
        throw $.newIOException("file is a directory");
    }

    asyncImpl("I", new Promise(function(resolve, reject) {
        fs.open(pathname, function(fd) {
            // The key is the $fileName Long, not the primitive number
            // it represents, so a file can be opened by multiple handlers.
            MIDP.openFiles.set(fileNumber, fd);
            resolve();
        });
    }));
};

Native["com/sun/cdc/io/j2me/file/DefaultFileHandler.positionForWrite.(J)V"] = function(offset) {
    var pathname = util.fromJavaString(this.$nativePath);
    console.log("DefaultFileHandler.positionForWrite: " + pathname);
    var fd = MIDP.openFiles.get(this.$fileName);
    fs.setpos(fd, offset.toNumber());
};

Native["com/sun/cdc/io/j2me/file/DefaultFileHandler.write.([BII)I"] = function(b, off, len) {
    var pathname = util.fromJavaString(this.$nativePath);
    console.log("DefaultFileHandler.write: " + pathname);
    var fd = MIDP.openFiles.get(this.$fileName);
    fs.write(fd, b.subarray(off, off + len));
    // The "length of data really written," which is always the length requested
    // in our implementation.
    return len;
};

Native["com/sun/cdc/io/j2me/file/DefaultFileHandler.flush.()V"] = function() {
    var pathname = util.fromJavaString(this.$nativePath);
    console.log("DefaultFileHandler.flush: " + pathname);
    var fd = MIDP.openFiles.get(this.$fileName);
    if (fd) {
        fs.flush(fd);
    }
};

Native["com/sun/cdc/io/j2me/file/DefaultFileHandler.close.()V"] = function() {
    var pathname = util.fromJavaString(this.$nativePath);
    console.log("DefaultFileHandler.close: " + pathname);
    var fd = MIDP.openFiles.get(this.$fileName);
    fs.close(fd);
    MIDP.openFiles.delete(this.$fileName);
};

Native["com/sun/cdc/io/j2me/file/DefaultFileHandler.closeForWrite.()V"] = function() {
    var pathname = util.fromJavaString(this.$nativePath);
    console.log("DefaultFileHandler.closeForWrite: " + pathname);
    var fd = MIDP.openFiles.get(this.$fileName);
    fs.close(fd);
    MIDP.openFiles.delete(this.$fileName);
};

Native["com/sun/cdc/io/j2me/file/DefaultFileHandler.canRead.()Z"] = function() {
    var pathname = util.fromJavaString(this.$nativePath);
    console.log("DefaultFileHandler.canRead: " + pathname);
    return fs.exists(pathname) ? 1 : 0;
};

Native["com/sun/cdc/io/j2me/file/DefaultFileHandler.openForRead.()V"] = function() {
    var pathname = util.fromJavaString(this.$nativePath);
    console.log("DefaultFileHandler.openForRead: " + pathname);

    var fileNumber = this.$fileName;
    if (MIDP.openFiles.has(fileNumber)) {
        // Already open.
        // XXX Should we track position separately for the input and output
        // streams?
        var fd = MIDP.openFiles.get(fileNumber);
        fs.setpos(fd, 0);
        return;
    }

    var stat = fs.stat(pathname);
    if (!stat) {
        throw $.newIOException("file doesn't exist");
    }
    if (stat.isDir) {
        throw $.newIOException("file is a directory");
    }

    asyncImpl("I", new Promise(function(resolve, reject) {
        fs.open(pathname, function(fd) {
            // The key is the $fileName Long, not the primitive number
            // it represents, so a file can be opened by multiple handlers.
            MIDP.openFiles.set(fileNumber, fd);
            resolve();
        });
    }));
};

Native["com/sun/cdc/io/j2me/file/DefaultFileHandler.read.([BII)I"] = function(b, off, len) {
    var pathname = util.fromJavaString(this.$nativePath);
    console.log("DefaultFileHandler.read: " + pathname);
    var fd = MIDP.openFiles.get(this.$fileName);

    if (off < 0 || len < 0 || off > b.byteLength || (b.byteLength - off) < len) {
        throw $.newIOException();
    }

    if (b.byteLength == 0 || len == 0) {
        return 0;
    }

    var curpos = fs.getpos(fd);
    var data = fs.read(fd, curpos, curpos + len);
    b.set(data, off);

    return (data.byteLength > 0) ? data.byteLength : -1;
};

Native["com/sun/cdc/io/j2me/file/DefaultFileHandler.closeForRead.()V"] = function() {
    var pathname = util.fromJavaString(this.$nativePath);
    console.log("DefaultFileHandler.closeForRead: " + pathname);
    var fd = MIDP.openFiles.get(this.$fileName);
    fs.close(fd);
    MIDP.openFiles.delete(this.$fileName);
};

Native["com/sun/cdc/io/j2me/file/DefaultFileHandler.closeForReadWrite.()V"] = function() {
    var pathname = util.fromJavaString(this.$nativePath);
    console.log("DefaultFileHandler.closeForReadWrite: " + pathname);
    var fd = MIDP.openFiles.get(this.$fileName);
    fs.close(fd);
    MIDP.openFiles.delete(this.$fileName);
};

Native["com/sun/cdc/io/j2me/file/DefaultFileHandler.delete.()V"] = function() {
    var pathname = util.fromJavaString(this.$nativePath);
    console.log("DefaultFileHandler.delete: " + pathname);
    if (!fs.remove(pathname)) {
        throw $.newIOException();
    }
};

Native["com/sun/cdc/io/j2me/file/DefaultFileHandler.lastModified.()J"] = function() {
    var pathname = util.fromJavaString(this.$nativePath);
    console.log("DefaultFileHandler.lastModified: " + pathname);
    var stat = fs.stat(pathname);
    return Long.fromNumber(stat != null ? stat.mtime : 0);
};

Native["com/sun/cdc/io/j2me/file/DefaultFileHandler.truncate.(J)V"] = function(byteOffset) {
    var pathname = util.fromJavaString(this.$nativePath);
    console.log("DefaultFileHandler.lastModified: " + pathname);

    var stat = fs.stat(pathname);

    if (!stat) {
        throw $.newIOException("file does not exist");
    }

    if (stat.isDir) {
        throw $.newIOException("file is directory");
    }

    // TODO If the file is open, flush it first.

    fs.truncate(pathname, byteOffset.toNumber());
};

Native["com/sun/cdc/io/j2me/file/Protocol.available.()I"] = function() {
    var pathname = util.fromJavaString(this.$fileHandler.$nativePath);
    var fd = MIDP.openFiles.get(this.$fileHandler.$fileName);
    var available = fs.getsize(fd) - fs.getpos(fd);
    console.log("Protocol.available: " + pathname + ": " + available);
    return available;
};

Native["com/sun/cdc/io/j2me/file/DefaultFileHandler.rename0.(Ljava/lang/String;)V"] = function(newName) {
    var pathname = util.fromJavaString(this.$nativePath);
    console.log("current nativePath: " + util.fromJavaString(this.$nativePath));
    console.log("DefaultFileHandler.rename0: " + pathname);
    console.log("newName: " + fs.normalize(util.fromJavaString(newName)));
    if (fs.exists(util.fromJavaString(newName)) || !fs.rename(pathname, util.fromJavaString(newName))) {
        throw $.newIOException("Rename failed");
    }
};

// Pretend there is 1GiB in total and available.
addUnimplementedNative("com/sun/cdc/io/j2me/file/DefaultFileHandler.totalSize.()J",
                       Long.fromNumber(1024 * 1024 * 1024));
addUnimplementedNative("com/sun/cdc/io/j2me/file/DefaultFileHandler.availableSize.()J",
                       Long.fromNumber(1024 * 1024 * 1024));

Native["com/sun/cdc/io/j2me/file/DefaultFileHandler.setReadable.(Z)V"] = function() {
    var pathname = util.fromJavaString(this.$nativePath);
    if (!fs.exists(pathname)) {
        throw $.newIOException("file does not exist");
    }
};

Native["com/sun/cdc/io/j2me/file/DefaultFileHandler.setWritable.(Z)V"] = function() {
    var pathname = util.fromJavaString(this.$nativePath);
    if (!fs.exists(pathname)) {
        throw $.newIOException("file does not exist");
    }
};

addUnimplementedNative("com/sun/cdc/io/j2me/file/DefaultFileHandler.directorySize.(Z)J", Long.fromNumber(0));
addUnimplementedNative("com/sun/cdc/io/j2me/file/DefaultFileHandler.setHidden0.(Z)V");

Native["com/sun/cdc/io/j2me/file/DefaultFileHandler.isHidden0.()Z"] = function() {
    // Per the comment in DefaultFileHandler.isHidden, we pretend we're Unix
    // and always return false.
    return 0;
};
