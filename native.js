/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var asyncImpl = J2ME.asyncImplOld;

function preemptingImpl(returnKind, returnValue) {
  if (J2ME.Scheduler.shouldPreempt()) {
      asyncImpl(returnKind, Promise.resolve(returnValue));
      return;
  }
  return returnValue;
}

var Override = {};

/**
 * A map from Java object addresses to native objects.
 *
 * Use getNative and setNative to simplify accessing this map.  getNative takes
 * a handle, so callers don't need to dereference its address, while setNative
 * returns the native, so callers can chain it with a local variable assignment.
 *
 * Currently this only supports mapping an address to a single native.
 * Will we ever want to map multiple natives to an address?  If so, we'll need
 * to do something more sophisticated here.
 *
 * XXX Figure out how to collect a native when its Java object is collected.
 */
var NativeMap = new Map();

function getNative(javaObj) {
    return NativeMap.get(javaObj._address);
}

function setNative(javaObj, nativeObj) {
    NativeMap.set(javaObj._address, nativeObj);
    return nativeObj;
}

function deleteNative(javaObj) {
    NativeMap.delete(javaObj._address);
}

Native["java/lang/System.arraycopy.(Ljava/lang/Object;ILjava/lang/Object;II)V"] = function(addr, src, srcOffset, dst, dstOffset, length) {
    if (!src || !dst)
        throw $.newNullPointerException("Cannot copy to/from a null array.");
    var srcKlass = src.klass;
    var dstKlass = dst.klass;

    if (!srcKlass.isArrayKlass || !dstKlass.isArrayKlass)
        throw $.newArrayStoreException("Can only copy to/from array types.");
    if (srcOffset < 0 || (srcOffset+length) > src.length || dstOffset < 0 || (dstOffset+length) > dst.length || length < 0)
        throw $.newArrayIndexOutOfBoundsException("Invalid index.");
    var srcIsPrimitive = !(src instanceof Array);
    var dstIsPrimitive = !(dst instanceof Array);
    if ((srcIsPrimitive && dstIsPrimitive && srcKlass !== dstKlass) ||
        (srcIsPrimitive && !dstIsPrimitive) ||
        (!srcIsPrimitive && dstIsPrimitive)) {
        throw $.newArrayStoreException("Incompatible component types: " + srcKlass + " -> " + dstKlass);
    }
    if (!dstIsPrimitive) {
        if (srcKlass != dstKlass && !J2ME.isAssignableTo(srcKlass.elementKlass, dstKlass.elementKlass)) {
            var copy = function(to, from) {
                var obj = src[from];
                if (obj && !J2ME.isAssignableTo(obj.klass, dstKlass.elementKlass)) {
                    throw $.newArrayStoreException("Incompatible component types.");
                }
                dst[to] = obj;
            };
            if (dst !== src || dstOffset < srcOffset) {
                for (var n = 0; n < length; ++n)
                    copy(dstOffset++, srcOffset++);
            } else {
                dstOffset += length;
                srcOffset += length;
                for (var n = 0; n < length; ++n)
                    copy(--dstOffset, --srcOffset);
            }
            return;
        }
    }
    if (dst !== src || dstOffset < srcOffset) {
        for (var n = 0; n < length; ++n)
            dst[dstOffset++] = src[srcOffset++];
    } else {
        dstOffset += length;
        srcOffset += length;
        for (var n = 0; n < length; ++n)
            dst[--dstOffset] = src[--srcOffset];
    }
};

var stubProperties = {
  "com.nokia.multisim.slots": "1",
  "com.nokia.mid.imsi": "000000000000000",
  "com.nokia.mid.imei": "",
};

Native["java/lang/System.getProperty0.(Ljava/lang/String;)Ljava/lang/String;"] = function(addr, key) {
    key = J2ME.fromJavaString(key);
    var value;
    switch (key) {
    case "microedition.encoding":
        // The value of this property is different than the value on a real Nokia Asha 503 phone.
        // On the phone, it is: ISO8859_1.
        // If we changed this, we would need to remove the optimizations for UTF_8_Reader and
        // UTF_8_Writer and optimize the ISO8859_1 alternatives.
        value = "UTF-8";
        break;
    case "microedition.io.file.FileConnection.version":
        value = "1.0";
        break;
    case "microedition.locale":
        value = navigator.language;
        break;
    case "microedition.platform":
        value = config.platform ? config.platform : "Nokia503/14.0.4/java_runtime_version=Nokia_Asha_1_2";
        break;
    case "microedition.platformimpl":
        value = null;
        break;
    case "microedition.profiles":
        value = "MIDP-2.1"
        break;
    case "microedition.pim.version":
        value = "1.0";
        break;
    case "microedition.amms.version":
        value = "1.1";
        break;
    case "microedition.media.version":
        value = '1.2';
        break;
    case "mmapi-configuration":
        value = null;
        break;
    case "fileconn.dir.memorycard":
        value = "file:///MemoryCard/";
        break;
    // The names here should be localized.
    case "fileconn.dir.memorycard.name":
        value = "Memory card";
        break;
    case "fileconn.dir.private":
        value = "file:///Private/";
        break;
    case "fileconn.dir.private.name":
        value = "Private";
        break;
    case "fileconn.dir.applications.bookmarks":
        value = null;
        break;
    case "fileconn.dir.received":
        value = "file:///Phone/_my_downloads/";
        break;
    case "fileconn.dir.received.name":
        value = "Downloads";
        break;
    case "fileconn.dir.photos":
        value = "file:///Phone/_my_pictures/";
        break;
    case "fileconn.dir.photos.name":
        value = "Photos";
        break;
    case "fileconn.dir.videos":
        value = "file:///Phone/_my_videos/";
        break;
    case "fileconn.dir.videos.name":
        value = "Videos";
        break;
    case "fileconn.dir.recordings":
        value = "file:///Phone/_my_recordings/";
        break;
    case "fileconn.dir.recordings.name":
        value = "Recordings";
        break;
    case "fileconn.dir.roots.names":
        value = MIDP.fsRootNames.join(";");
        break;
    case "fileconn.dir.roots.external":
        value = MIDP.fsRoots.map(function(v) { return "file:///" + v }).join("\n");
        break;
    case "file.separator":
        value = "/";
        break;
    case "com.sun.cldc.util.j2me.TimeZoneImpl.timezone":
        // Date.toString() returns something like the following:
        //    "Wed Sep 17 2014 12:11:23 GMT-0700 (PDT)"
        //
        // Per http://www.spectrum3847.org/frc2013api/com/sun/cldc/util/j2me/TimeZoneImpl.html,
        // timezones can be of the format GMT+0600, which is what this
        // regex currently matches. (Those actually in GMT would not
        // match the regex, causing the default "GMT" to be returned.)
        // If we find this to be a problem, we could alternately return the
        // zone name as provided in parenthesis, but that seems locale-specific.
        var match = /GMT[+-]\d+/.exec(new Date().toString());
        value = (match && match[0]) || "GMT";
        break;
    case "javax.microedition.io.Connector.protocolpath":
        value = "com.sun.midp.io";
        break;
    case "javax.microedition.io.Connector.protocolpath.fallback":
        value = "com.sun.cldc.io";
        break;
    case "com.nokia.keyboard.type":
        value = "None";
        break;
    case "com.nokia.mid.batterylevel":
        // http://developer.nokia.com/community/wiki/Checking_battery_level_in_Java_ME
        value = Math.floor(navigator.battery.level * 100).toString();
        break;
    case "com.nokia.mid.ui.version":
        value = "1.7";
        break;
    case "com.nokia.mid.mnc":
        if (mobileInfo.icc.mcc && mobileInfo.icc.mnc) {
            // The concatenation of the MCC and MNC for the ICC (i.e. SIM card).
            value = util.pad(mobileInfo.icc.mcc, 3) + util.pad(mobileInfo.icc.mnc, 3);
        } else {
            value = null;
        }
        break;
    case "com.nokia.mid.networkID":
        if (mobileInfo.network.mcc && mobileInfo.network.mnc) {
            // The concatenation of MCC and MNC for the network.
            value = util.pad(mobileInfo.network.mcc, 3) + util.pad(mobileInfo.network.mnc, 3);
        } else {
            value = null;
        }
        break;
    case "com.nokia.mid.ui.customfontsize":
        value = "true";
        break;
    case "classpathext":
        value = null;
        break;
    case "supports.audio.capture":
        value = "true";
        break;
    case "supports.video.capture":
        value = "true";
        break;
    case "supports.recording":
        value = "true";
        break;
    case "audio.encodings":
        value = "encoding=audio/amr";
        break;
    case "video.snapshot.encodings":
        // FIXME Some MIDlets pass a string that contains lots of constraints
        // as the `imageType` which is not yet handled in DirectVideo.jpp, let's
        // just put the whole string here as a workaround and fix this in issue #688.
        value = "encoding=jpeg&quality=80&progressive=true&type=jfif&width=400&height=400";
        break;
    default:
        if (MIDP.additionalProperties[key]) {
            value = MIDP.additionalProperties[key];
        } else if (typeof stubProperties[key] !== "undefined") {
            value = stubProperties[key];
        } else {
            console.warn("UNKNOWN PROPERTY (java/lang/System): " + key);
            stubProperties[key] = value = null;
        }
        break;
    }

    return J2ME.newString(value);
};

Native["java/lang/System.currentTimeMillis.()J"] = function(addr) {
    return J2ME.returnLongValue(Date.now());
};

Native["com/sun/cldchi/jvm/JVM.unchecked_char_arraycopy.([CI[CII)V"] = function(addr, src, srcOffset, dst, dstOffset, length) {
  dst.set(src.subarray(srcOffset, srcOffset + length), dstOffset);
};

Native["com/sun/cldchi/jvm/JVM.unchecked_int_arraycopy.([II[III)V"] = function(addr, src, srcOffset, dst, dstOffset, length) {
  dst.set(src.subarray(srcOffset, srcOffset + length), dstOffset);
};

Native["com/sun/cldchi/jvm/JVM.unchecked_obj_arraycopy.([Ljava/lang/Object;I[Ljava/lang/Object;II)V"] = function(addr, src, srcOffset, dst, dstOffset, length) {
    if (dst !== src || dstOffset < srcOffset) {
        for (var n = 0; n < length; ++n)
            dst[dstOffset++] = src[srcOffset++];
    } else {
        dstOffset += length;
        srcOffset += length;
        for (var n = 0; n < length; ++n)
            dst[--dstOffset] = src[--srcOffset];
    }
};

Native["com/sun/cldchi/jvm/JVM.monotonicTimeMillis.()J"] = function(addr) {
    return J2ME.returnLongValue(performance.now());
};

Native["java/lang/Object.getClass.()Ljava/lang/Class;"] = function(addr) {
    return $.getRuntimeKlass(this.klass).classObject;
};

Native["java/lang/Class.getSuperclass.()Ljava/lang/Class;"] = function(addr) {
    var superKlass = this.runtimeKlass.templateKlass.superKlass;
    if (!superKlass) {
      return null;
    }
    return superKlass.classInfo.getClassObject();
};

Native["java/lang/Class.invoke_clinit.()V"] = function(addr) {
    var classInfo = this.runtimeKlass.templateKlass.classInfo;
    var className = classInfo.getClassNameSlow();
    var clinit = classInfo.staticInitializer;
    J2ME.preemptionLockLevel++;
    if (clinit && clinit.classInfo.getClassNameSlow() === className) {
        $.ctx.executeMethod(clinit);
    }
};

Native["java/lang/Class.invoke_verify.()V"] = function(addr) {
    // There is currently no verification.
};

Native["java/lang/Class.init9.()V"] = function(addr) {
    $.setClassInitialized(this.runtimeKlass);
    J2ME.preemptionLockLevel--;
};

Native["java/lang/Class.getName.()Ljava/lang/String;"] = function(addr) {
    return J2ME.newString(this.runtimeKlass.templateKlass.classInfo.getClassNameSlow().replace(/\//g, "."));
};

Native["java/lang/Class.forName0.(Ljava/lang/String;)V"] = function(addr, name) {
  var classInfo = null;
  try {
    if (!name)
      throw new J2ME.ClassNotFoundException();
    var className = J2ME.fromJavaString(name).replace(/\./g, "/");
    classInfo = CLASSES.getClass(className);
  } catch (e) {
    if (e instanceof (J2ME.ClassNotFoundException))
      throw $.newClassNotFoundException("'" + e.message + "' not found.");
    throw e;
  }
  // The following can trigger an unwind.
  J2ME.classInitCheck(classInfo);
};

Native["java/lang/Class.forName1.(Ljava/lang/String;)Ljava/lang/Class;"] = function(addr, name) {
  var className = J2ME.fromJavaString(name).replace(/\./g, "/");
  var classInfo = CLASSES.getClass(className);
  var classObject = classInfo.getClassObject();
  return classObject;
};

Native["java/lang/Class.newInstance0.()Ljava/lang/Object;"] = function(addr) {
  if (this.runtimeKlass.templateKlass.classInfo.isInterface ||
      this.runtimeKlass.templateKlass.classInfo.isAbstract) {
    throw $.newInstantiationException("Can't instantiate interfaces or abstract classes");
  }

  if (this.runtimeKlass.templateKlass.classInfo instanceof J2ME.ArrayClassInfo) {
    throw $.newInstantiationException("Can't instantiate array classes");
  }

  return new this.runtimeKlass.templateKlass;
};

Native["java/lang/Class.newInstance1.(Ljava/lang/Object;)V"] = function(addr, o) {
  // The following can trigger an unwind.
  var methodInfo = o.klass.classInfo.getLocalMethodByNameString("<init>", "()V", false);
  if (!methodInfo) {
    throw $.newInstantiationException("Can't instantiate classes without a nullary constructor");
  }
  J2ME.getLinkedMethod(methodInfo).call(o);
};

Native["java/lang/Class.isInterface.()Z"] = function(addr) {
    return this.runtimeKlass.templateKlass.classInfo.isInterface ? 1 : 0;
};

Native["java/lang/Class.isArray.()Z"] = function(addr) {
    return this.runtimeKlass.templateKlass.classInfo instanceof J2ME.ArrayClassInfo ? 1 : 0;
};

Native["java/lang/Class.isAssignableFrom.(Ljava/lang/Class;)Z"] = function(addr, fromClass) {
    if (!fromClass)
        throw $.newNullPointerException();
    return J2ME.isAssignableTo(fromClass.runtimeKlass.templateKlass, this.runtimeKlass.templateKlass) ? 1 : 0;
};

Native["java/lang/Class.isInstance.(Ljava/lang/Object;)Z"] = function(addr, obj) {
    return obj && J2ME.isAssignableTo(obj.klass, this.runtimeKlass.templateKlass) ? 1 : 0;
};

Native["java/lang/Float.floatToIntBits.(F)I"] = function(addr, f) {
    return aliasedF32[0] = f, aliasedI32[0];
}

Native["java/lang/Float.intBitsToFloat.(I)F"] = function(addr, i) {
    return aliasedI32[0] = i, aliasedF32[0];
}

Native["java/lang/Double.doubleToLongBits.(D)J"] = function(addr, d) {
    aliasedF64[0] = d;
    return J2ME.returnLong(aliasedI32[0], aliasedI32[1]);
}

Native["java/lang/Double.longBitsToDouble.(J)D"] = function(addr, l, h) {
    aliasedI32[0] = l;
    aliasedI32[1] = h;
    return aliasedF64[0];
}

Native["java/lang/Throwable.fillInStackTrace.()V"] = function(addr) {
    this.stackTrace = [];
    J2ME.traceWriter && J2ME.traceWriter.writeLn("REDUX");
    //$.ctx.frames.forEach(function(frame) {
    //    if (!frame.methodInfo)
    //        return;
    //    var methodInfo = frame.methodInfo;
    //    var methodName = methodInfo.name;
    //    if (!methodName)
    //        return;
    //    var classInfo = methodInfo.classInfo;
    //    var className = classInfo.getClassNameSlow();
    //    this.stackTrace.unshift({ className: className, methodName: methodName, methodSignature: methodInfo.signature, offset: frame.bci });
    //}.bind(this));
};

Native["java/lang/Throwable.obtainBackTrace.()Ljava/lang/Object;"] = function(addr) {
    var result = null;
    if (this.stackTrace) {
        var depth = this.stackTrace.length;
        var classNames = J2ME.newObjectArray(depth);
        var methodNames = J2ME.newObjectArray(depth);
        var methodSignatures = J2ME.newObjectArray(depth);
        var offsets = J2ME.newIntArray(depth);
        this.stackTrace.forEach(function(e, n) {
            classNames[n] = J2ME.newString(e.className);
            methodNames[n] = J2ME.newString(e.methodName);
            methodSignatures[n] = J2ME.newString(e.methodSignature);
            offsets[n] = e.offset;
        });
        result = J2ME.newObjectArray(3);
        result[0] = classNames;
        result[1] = methodNames;
        result[2] = methodSignatures;
        result[3] = offsets;
    }
    return result;
};

Native["java/lang/Runtime.freeMemory.()J"] = function(addr) {
    return J2ME.returnLong(0x800000, 0);
};

Native["java/lang/Runtime.totalMemory.()J"] = function(addr) {
    return J2ME.returnLong(0x1000000, 0);
};

Native["java/lang/Runtime.gc.()V"] = function(addr) {
};

Native["java/lang/Math.floor.(D)D"] = function(addr, val) {
    return Math.floor(val);
};

Native["java/lang/Math.asin.(D)D"] = function(addr, val) {
    return Math.asin(val);
};

Native["java/lang/Math.acos.(D)D"] = function(addr, val) {
    return Math.acos(val);
};

Native["java/lang/Math.atan.(D)D"] = function(addr, val) {
    return Math.atan(val);
};

Native["java/lang/Math.atan2.(DD)D"] = function(addr, x, y) {
    return Math.atan2(x, y);
};

Native["java/lang/Math.sin.(D)D"] = function(addr, val) {
    return Math.sin(val);
};

Native["java/lang/Math.cos.(D)D"] = function(addr, val) {
    return Math.cos(val);
};

Native["java/lang/Math.tan.(D)D"] = function(addr, val) {
    return Math.tan(val);
};

Native["java/lang/Math.sqrt.(D)D"] = function(addr, val) {
    return Math.sqrt(val);
};

Native["java/lang/Math.ceil.(D)D"] = function(addr, val) {
    return Math.ceil(val);
};

Native["java/lang/Math.floor.(D)D"] = function(addr, val) {
    return Math.floor(val);
};

Native["java/lang/Thread.currentThread.()Ljava/lang/Thread;"] = function(addr) {
    return getHandle($.ctx.threadAddress);
};

Native["java/lang/Thread.setPriority0.(II)V"] = function(addr, oldPriority, newPriority) {
};

Native["java/lang/Thread.start0.()V"] = function(addr) {
    // The main thread starts during bootstrap and don't allow calling start()
    // on already running threads.
    if (this._address === $.ctx.runtime.mainThread || this.nativeAlive)
        throw $.newIllegalThreadStateException();
    this.nativeAlive = 1;
    // XXX this.pid seems to be unused, so remove it.
    this.pid = util.id();
    // Create a context for the thread and start it.
    var newCtx = new Context($.ctx.runtime);
    newCtx.threadAddress = this._address;

    var classInfo = CLASSES.getClass("org/mozilla/internal/Sys");
    var run = classInfo.getMethodByNameString("runThread", "(Ljava/lang/Thread;)V", true);
    newCtx.nativeThread.pushFrame(null);
    newCtx.nativeThread.pushFrame(run);
    newCtx.nativeThread.frame.setParameter(J2ME.Kind.Reference, 0, this);
    newCtx.start();
}

Native["java/lang/Thread.activeCount.()I"] = function(addr) {
    return $.ctx.runtime.threadCount;
};

var consoleBuffer = "";

function flushConsoleBuffer() {
    if (consoleBuffer.length) {
        var temp = consoleBuffer;
        consoleBuffer = "";
        console.info(temp);
    }
}

console.print = function(ch) {
    if (ch === 10) {
        flushConsoleBuffer();
    } else {
        consoleBuffer += String.fromCharCode(ch);
    }
};

Native["com/sun/cldchi/io/ConsoleOutputStream.write.(I)V"] = function(addr, ch) {
    console.print(ch);
};

Native["com/sun/cldc/io/ResourceInputStream.open.(Ljava/lang/String;)Ljava/lang/Object;"] = function(addr, name) {
    var fileName = J2ME.fromJavaString(name);
    var data = JARStore.loadFile(fileName);
    var obj = null;
    if (data) {
        obj = J2ME.newObject(CLASSES.java_lang_Object.klass);
        setNative(obj, {
            data: data,
            pos: 0,
        });
    }
    return obj;
};

Native["com/sun/cldc/io/ResourceInputStream.clone.(Ljava/lang/Object;)Ljava/lang/Object;"] = function(addr, source) {
    var obj = J2ME.newObject(CLASSES.java_lang_Object.klass);
    var sourceDecoder = getNative(source);
    setNative(obj, {
        data: new Uint8Array(sourceDecoder.data),
        pos: sourceDecoder.pos,
    });
    return obj;
};

Native["com/sun/cldc/io/ResourceInputStream.bytesRemain.(Ljava/lang/Object;)I"] = function(addr, fileDecoder) {
    var handle = getNative(fileDecoder);
    return handle.data.length - handle.pos;
};

Native["com/sun/cldc/io/ResourceInputStream.readByte.(Ljava/lang/Object;)I"] = function(addr, fileDecoder) {
    var handle = getNative(fileDecoder);
    return (handle.data.length - handle.pos > 0) ? handle.data[handle.pos++] : -1;
};

Native["com/sun/cldc/io/ResourceInputStream.readBytes.(Ljava/lang/Object;[BII)I"] = function(addr, fileDecoder, b, off, len) {
    var handle = getNative(fileDecoder);
    var data = handle.data;
    var remaining = data.length - handle.pos;
    if (len > remaining)
        len = remaining;
    for (var n = 0; n < len; ++n)
        b[off+n] = data[handle.pos+n];
    handle.pos += len;
    return (len > 0) ? len : -1;
};

Native["java/lang/ref/WeakReference.initializeWeakReference.(Ljava/lang/Object;)V"] = function(addr, target) {
    // XXX Make these real weak references.
    setNative(this, target);
};

Native["java/lang/ref/WeakReference.get.()Ljava/lang/Object;"] = function(addr) {
    var target = getNative(this);
    return target ? target : null;
};

Native["java/lang/ref/WeakReference.clear.()V"] = function(addr) {
    deleteNative(this);
};

Native["com/sun/cldc/isolate/Isolate.registerNewIsolate.()V"] = function(addr) {
    this._id = util.id();
};

Native["com/sun/cldc/isolate/Isolate.getStatus.()I"] = function(addr) {
    var runtime = Runtime.isolateMap[this._address];
    return runtime ? runtime.status : J2ME.RuntimeStatus.New;
};

Native["com/sun/cldc/isolate/Isolate.nativeStart.()V"] = function(addr) {
    $.ctx.runtime.jvm.startIsolate(this);
};

Native["com/sun/cldc/isolate/Isolate.waitStatus.(I)V"] = function(addr, status) {
    var runtime = Runtime.isolateMap[this._address];
    asyncImpl("V", new Promise(function(resolve, reject) {
        if (runtime.status >= status) {
            resolve();
            return;
        }
        function waitForStatus() {
            if (runtime.status >= status) {
                resolve();
                return;
            }
            runtime.waitStatus(waitForStatus);
        }
        waitForStatus();
    }));
};

Native["com/sun/cldc/isolate/Isolate.currentIsolate0.()Lcom/sun/cldc/isolate/Isolate;"] = function(addr) {
    return getHandle($.ctx.runtime.isolateAddress);
};

Native["com/sun/cldc/isolate/Isolate.getIsolates0.()[Lcom/sun/cldc/isolate/Isolate;"] = function(addr) {
    var isolates = J2ME.newObjectArray(Runtime.all.keys().length);
    var n = 0;
    Runtime.all.forEach(function (runtime) {
        var address = runtime.isolateAddress;
        var isolate = getHandle(address);
        isolates[n++] = isolate;
    });
    return isolates;
};

Native["com/sun/cldc/isolate/Isolate.setPriority0.(I)V"] = function(addr, newPriority) {
    // XXX Figure out if there's anything to do here.  If not, say so.
};

Native["com/sun/j2me/content/AppProxy.midletIsAdded.(ILjava/lang/String;)V"] = function(addr, suiteId, className) {
  console.warn("com/sun/j2me/content/AppProxy.midletIsAdded.(ILjava/lang/String;)V not implemented");
};

Native["com/nokia/mid/impl/jms/core/Launcher.handleContent.(Ljava/lang/String;)V"] = function(addr, content) {
    var fileName = J2ME.fromJavaString(content);

    var ext = fileName.split('.').pop().toLowerCase();
    // https://developer.mozilla.org/en-US/docs/Web/HTML/Element/img#Supported_image_formats
    if (["jpg", "jpeg", "gif", "apng", "png", "bmp", "ico"].indexOf(ext) == -1) {
        console.error("File not supported: " + fileName);
        throw $.newException("File not supported: " + fileName);
    }

    // `fileName` is supposed to be a full path, but we don't support
    // partition, e.g. `C:` or `E:` etc, so the `fileName` we got here
    // is something like: `Photos/sampleImage.jpg`, we need to prepend
    // the root dir to make sure it's valid.
    var imgData = fs.getBlob("/" + fileName);
    if (!imgData) {
        console.error("File not found: " + fileName);
        throw $.newException("File not found: " + fileName);
    }

    var maskId = "image-launcher";
    var mask = document.getElementById(maskId);

    function _revokeImageURL() {
        URL.revokeObjectURL(/url\((.+)\)/ig.exec(mask.style.backgroundImage)[1]);
    }

    if (mask) {
        _revokeImageURL();
    } else {
        mask = document.createElement("div");
        mask.id = maskId;
        mask.onclick = mask.ontouchstart = function() {
            _revokeImageURL();
            mask.parentNode.removeChild(mask);
        };

        document.getElementById("main").appendChild(mask);
    }

    mask.style.backgroundImage = "url(" +
      URL.createObjectURL(imgData) + ")";
};

function addUnimplementedNative(signature, returnValue) {
    var doNotWarn;

    if (typeof returnValue === "function") {
      doNotWarn = returnValue;
    } else {
      doNotWarn = function() { return returnValue };
    }

    var warnOnce = function() {
        console.warn(signature + " not implemented");
        warnOnce = doNotWarn;
        return doNotWarn();
    };

    Native[signature] = function(addr) { return warnOnce() };
}

Native["org/mozilla/internal/Sys.eval.(Ljava/lang/String;)V"] = function(addr, src) {
    if (!release) {
        eval(J2ME.fromJavaString(src));
    }
};

Native["java/lang/String.intern.()Ljava/lang/String;"] = function(addr) {
  var internedStrings = J2ME.internedStrings;
  var internedString = internedStrings.getByRange(this.value, this.offset, this.count);
  if (internedString !== null) {
    return internedString;
  }
  internedStrings.put(this.value.subarray(this.offset, this.offset + this.count), this);
  return this;
};

var profileStarted = false;
Native["org/mozilla/internal/Sys.startProfile.()V"] = function(addr) {
    if (profile === 4) {
        if (!profileStarted) {
            profileStarted = true;

            console.log("Start profile at: " + performance.now());
            startTimeline();
        }
    }
};

var profileSaved = false;
Native["org/mozilla/internal/Sys.stopProfile.()V"] = function(addr) {
    if (profile === 4) {
        if (!profileSaved) {
            profileSaved = true;

            console.log("Stop profile at: " + performance.now());
            setZeroTimeout(function() {
                stopAndSaveTimeline();
            });
        }
    }
};
