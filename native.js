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

Native["java/lang/System.arraycopy.(Ljava/lang/Object;ILjava/lang/Object;II)V"] =
function(addr, srcAddr, srcOffset, dstAddr, dstOffset, length) {
    if (srcAddr === J2ME.Constants.NULL || dstAddr === J2ME.Constants.NULL) {
        throw $.newNullPointerException("Cannot copy to/from a null array.");
    }

    var src = getHandle(srcAddr);
    var dst = getHandle(dstAddr);

    var srcKlass = src.klass;
    var dstKlass = dst.klass;

    if (!srcKlass.isArrayKlass || !dstKlass.isArrayKlass) {
        throw $.newArrayStoreException("Can only copy to/from array types.");
    }
    if (srcOffset < 0 || (srcOffset+length) > src.length || dstOffset < 0 || (dstOffset+length) > dst.length || length < 0) {
        throw $.newArrayIndexOutOfBoundsException("Invalid index.");
    }
    var srcIsPrimitive = srcKlass.classInfo instanceof J2ME.PrimitiveArrayClassInfo;
    var dstIsPrimitive = dstKlass.classInfo instanceof J2ME.PrimitiveArrayClassInfo;
    if ((srcIsPrimitive && dstIsPrimitive && srcKlass !== dstKlass) ||
        (srcIsPrimitive && !dstIsPrimitive) ||
        (!srcIsPrimitive && dstIsPrimitive)) {
        throw $.newArrayStoreException("Incompatible component types: " + srcKlass + " -> " + dstKlass);
    }

    if (!dstIsPrimitive) {
        if (srcKlass != dstKlass && !J2ME.isAssignableTo(srcKlass.elementKlass, dstKlass.elementKlass)) {
            var copy = function(to, from) {
                var addr = src[from];
                var obj = getHandle(addr);
                if (obj && !J2ME.isAssignableTo(obj.klass, dstKlass.elementKlass)) {
                    throw $.newArrayStoreException("Incompatible component types.");
                }
                dst[to] = addr;
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

Native["java/lang/System.getProperty0.(Ljava/lang/String;)Ljava/lang/String;"] = function(addr, keyAddr) {
    var key = J2ME.fromStringAddr(keyAddr);
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

Native["com/sun/cldchi/jvm/JVM.unchecked_char_arraycopy.([CI[CII)V"] =
function(addr, srcAddr, srcOffset, dstAddr, dstOffset, length) {
  var src = J2ME.getArrayFromAddr(srcAddr);
  var dst = J2ME.getArrayFromAddr(dstAddr);
  dst.set(src.subarray(srcOffset, srcOffset + length), dstOffset);
};

Native["com/sun/cldchi/jvm/JVM.unchecked_int_arraycopy.([II[III)V"] =
function(addr, srcAddr, srcOffset, dstAddr, dstOffset, length) {
  var src = J2ME.getArrayFromAddr(srcAddr);
  var dst = J2ME.getArrayFromAddr(dstAddr);
  dst.set(src.subarray(srcOffset, srcOffset + length), dstOffset);
};

Native["com/sun/cldchi/jvm/JVM.unchecked_obj_arraycopy.([Ljava/lang/Object;I[Ljava/lang/Object;II)V"] =
function(addr, srcAddr, srcOffset, dstAddr, dstOffset, length) {
    var src = J2ME.getArrayFromAddr(srcAddr);
    var dst = J2ME.getArrayFromAddr(dstAddr);

    if (dst !== src || dstOffset < srcOffset) {
        for (var n = 0; n < length; ++n) {
            dst[dstOffset++] = src[srcOffset++];
        }
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
    var self = getHandle(addr);
    return $.getRuntimeKlass(self.klass).classObject._address;
};

Native["java/lang/Class.getSuperclass.()Ljava/lang/Class;"] = function(addr) {
    var self = getHandle(addr);
    var superKlass = self.runtimeKlass.templateKlass.superKlass;
    if (!superKlass) {
      return null;
    }
    return superKlass.classInfo.getClassObject()._address;
};

Native["java/lang/Class.invoke_clinit.()V"] = function(addr) {
    var self = getHandle(addr);
    var classInfo = self.runtimeKlass.templateKlass.classInfo;
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
    var self = getHandle(addr);
    $.setClassInitialized(self.runtimeKlass);
    J2ME.preemptionLockLevel--;
};

Native["java/lang/Class.getName.()Ljava/lang/String;"] = function(addr) {
    var self = getHandle(addr);
    return J2ME.newString(self.runtimeKlass.templateKlass.classInfo.getClassNameSlow().replace(/\//g, "."));
};

Native["java/lang/Class.forName0.(Ljava/lang/String;)V"] = function(addr, nameAddr) {
  var classInfo = null;
  try {
    if (nameAddr === J2ME.Constants.NULL) {
      throw new J2ME.ClassNotFoundException();
    }
    var className = J2ME.fromStringAddr(nameAddr).replace(/\./g, "/");
    classInfo = CLASSES.getClass(className);
  } catch (e) {
    if (e instanceof (J2ME.ClassNotFoundException)) {
      throw $.newClassNotFoundException("'" + e.message + "' not found.");
    }
    throw e;
  }
  // The following can trigger an unwind.
  J2ME.classInitCheck(classInfo);
};

Native["java/lang/Class.forName1.(Ljava/lang/String;)Ljava/lang/Class;"] = function(addr, nameAddr) {
  var className = J2ME.fromStringAddr(nameAddr).replace(/\./g, "/");
  var classInfo = CLASSES.getClass(className);
  var classObject = classInfo.getClassObject();
  return classObject._address;
};

Native["java/lang/Class.newInstance0.()Ljava/lang/Object;"] = function(addr) {
  var self = getHandle(addr);
  if (self.runtimeKlass.templateKlass.classInfo.isInterface ||
      self.runtimeKlass.templateKlass.classInfo.isAbstract) {
    throw $.newInstantiationException("Can't instantiate interfaces or abstract classes");
  }

  if (self.runtimeKlass.templateKlass.classInfo instanceof J2ME.ArrayClassInfo) {
    throw $.newInstantiationException("Can't instantiate array classes");
  }

  return (new self.runtimeKlass.templateKlass)._address;
};

Native["java/lang/Class.newInstance1.(Ljava/lang/Object;)V"] = function(addr, oAddr) {
  var o = getHandle(oAddr);
  // The following can trigger an unwind.
  var methodInfo = o.klass.classInfo.getLocalMethodByNameString("<init>", "()V", false);
  if (!methodInfo) {
    throw $.newInstantiationException("Can't instantiate classes without a nullary constructor");
  }
  J2ME.getLinkedMethod(methodInfo)(oAddr);
};

Native["java/lang/Class.isInterface.()Z"] = function(addr) {
    var self = getHandle(addr);
    return self.runtimeKlass.templateKlass.classInfo.isInterface ? 1 : 0;
};

Native["java/lang/Class.isArray.()Z"] = function(addr) {
    var self = getHandle(addr);
    return self.runtimeKlass.templateKlass.classInfo instanceof J2ME.ArrayClassInfo ? 1 : 0;
};

Native["java/lang/Class.isAssignableFrom.(Ljava/lang/Class;)Z"] = function(addr, fromClassAddr) {
    var self = getHandle(addr);
    var fromClass = getHandle(fromClassAddr);
    if (!fromClass)
        throw $.newNullPointerException();
    return J2ME.isAssignableTo(fromClass.runtimeKlass.templateKlass, self.runtimeKlass.templateKlass) ? 1 : 0;
};

Native["java/lang/Class.isInstance.(Ljava/lang/Object;)Z"] = function(addr, objAddr) {
    var self = getHandle(addr);
    return objAddr !== J2ME.Constants.NULL && J2ME.isAssignableTo(getHandle(objAddr).klass, self.runtimeKlass.templateKlass) ? 1 : 0;
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
    J2ME.traceWriter && J2ME.traceWriter.writeLn("REDUX");
    //var stackTrace = [];
    //setNative(addr, stackTrace);
    //$.ctx.frames.forEach(function(frame) {
    //    if (!frame.methodInfo)
    //        return;
    //    var methodInfo = frame.methodInfo;
    //    var methodName = methodInfo.name;
    //    if (!methodName)
    //        return;
    //    var classInfo = methodInfo.classInfo;
    //    var className = classInfo.getClassNameSlow();
    //    stackTrace.unshift({ className: className, methodName: methodName, methodSignature: methodInfo.signature, offset: frame.bci });
    //});
};

Native["java/lang/Throwable.obtainBackTrace.()Ljava/lang/Object;"] = function(addr) {
    var resultAddr = J2ME.Constants.NULL;
    // XXX: Untested.
    var stackTrace = NativeMap.get(addr);
    if (stackTrace) {
        var depth = stackTrace.length;
        var classNamesAddr = J2ME.newStringArray(depth);
        var classNames = J2ME.getArrayFromAddr(classNamesAddr);
        var methodNamesAddr = J2ME.newStringArray(depth);
        var methodNames = J2ME.getArrayFromAddr(methodNamesAddr);
        var methodSignaturesAddr = J2ME.newStringArray(depth);
        var methodSignatures = J2ME.getArrayFromAddr(methodSignaturesAddr);
        var offsetsAddr = J2ME.newIntArray(depth);
        var offsets = J2ME.getArrayFromAddr(offsetsAddr);
        stackTrace.forEach(function(e, n) {
            classNames[n] = J2ME.newString(e.className);
            methodNames[n] = J2ME.newString(e.methodName);
            methodSignatures[n] = J2ME.newString(e.methodSignature);
            offsets[n] = e.offset;
        });
        resultAddr = J2ME.newObjectArray(3);
        var result = J2ME.getArrayFromAddr(resultAddr);
        result[0] = classNamesAddr;
        result[1] = methodNamesAddr;
        result[2] = methodSignaturesAddr;
        result[3] = offsetsAddr;
    }
    return resultAddr;
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
    return $.ctx.threadAddress;
};

Native["java/lang/Thread.setPriority0.(II)V"] = function(addr, oldPriority, newPriority) {
};

Native["java/lang/Thread.start0.()V"] = function(addr) {
    var self = getHandle(addr);

    // The main thread starts during bootstrap and don't allow calling start()
    // on already running threads.
    if (addr === $.ctx.runtime.mainThread || self.nativeAlive)
        throw $.newIllegalThreadStateException();
    self.nativeAlive = 1;
    // XXX self.pid seems to be unused, so remove it.
    self.pid = util.id();
    // Create a context for the thread and start it.
    var newCtx = new Context($.ctx.runtime);
    newCtx.threadAddress = addr;

    var classInfo = CLASSES.getClass("org/mozilla/internal/Sys");
    var run = classInfo.getMethodByNameString("runThread", "(Ljava/lang/Thread;)V", true);
    newCtx.nativeThread.pushFrame(null);
    newCtx.nativeThread.pushFrame(run);
    newCtx.nativeThread.frame.setParameter(J2ME.Kind.Reference, 0, addr);
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

Native["com/sun/cldc/io/ResourceInputStream.open.(Ljava/lang/String;)Ljava/lang/Object;"] = function(addr, nameAddr) {
    var fileName = J2ME.fromStringAddr(nameAddr);
    var data = JARStore.loadFile(fileName);
    var objAddr = J2ME.Constants.NULL;
    if (data) {
        objAddr = J2ME.allocObject(CLASSES.java_lang_Object.klass);
        setNative(objAddr, {
            data: data,
            pos: 0,
        });
    }
    return objAddr;
};

Native["com/sun/cldc/io/ResourceInputStream.clone.(Ljava/lang/Object;)Ljava/lang/Object;"] = function(addr, sourceAddr) {
    var source = getHandle(sourceAddr);
    var objAddr = J2ME.allocObject(CLASSES.java_lang_Object.klass);
    var sourceDecoder = getNative(source);
    setNative(objAddr, {
        data: new Uint8Array(sourceDecoder.data),
        pos: sourceDecoder.pos,
    });
    return objAddr;
};

Native["com/sun/cldc/io/ResourceInputStream.bytesRemain.(Ljava/lang/Object;)I"] = function(addr, fileDecoderAddr) {
    var handle = NativeMap.get(fileDecoderAddr);
    return handle.data.length - handle.pos;
};

Native["com/sun/cldc/io/ResourceInputStream.readByte.(Ljava/lang/Object;)I"] = function(addr, fileDecoderAddr) {
    var handle = NativeMap.get(fileDecoderAddr);
    return (handle.data.length - handle.pos > 0) ? handle.data[handle.pos++] : -1;
};

Native["com/sun/cldc/io/ResourceInputStream.readBytes.(Ljava/lang/Object;[BII)I"] =
function(addr, fileDecoderAddr, bAddr, off, len) {
    var b = J2ME.getArrayFromAddr(bAddr);
    var handle = NativeMap.get(fileDecoderAddr);
    var data = handle.data;
    var remaining = data.length - handle.pos;
    if (len > remaining)
        len = remaining;
    for (var n = 0; n < len; ++n)
        b[off+n] = data[handle.pos+n];
    handle.pos += len;
    return (len > 0) ? len : -1;
};

Native["java/lang/ref/WeakReference.initializeWeakReference.(Ljava/lang/Object;)V"] = function(addr, targetAddr) {
    // Store the (not actually) weak reference in NativeMap.
    //
    // This is technically a misuse of NativeMap, which is intended to store
    // native objects associated with Java objects, whereas here we're storing
    // the address of a Java object associated with another Java object.
    //
    // XXX Make these real weak references.
    //
    setNative(addr, targetAddr);
};

Native["java/lang/ref/WeakReference.get.()Ljava/lang/Object;"] = function(addr) {
    return NativeMap.has(addr) ? NativeMap.get(addr) : J2ME.Constants.NULL;
};

Native["java/lang/ref/WeakReference.clear.()V"] = function(addr) {
    NativeMap.delete(addr);
};

Native["com/sun/cldc/isolate/Isolate.registerNewIsolate.()V"] = function(addr) {
    var self = getHandle(addr);
    self._id = util.id();
};

Native["com/sun/cldc/isolate/Isolate.getStatus.()I"] = function(addr) {
    var runtime = NativeMap.get(addr);
    return runtime ? runtime.status : J2ME.RuntimeStatus.New;
};

Native["com/sun/cldc/isolate/Isolate.nativeStart.()V"] = function(addr) {
    var self = getHandle(addr);
    $.ctx.runtime.jvm.startIsolate(self);
};

Native["com/sun/cldc/isolate/Isolate.waitStatus.(I)V"] = function(addr, status) {
    var runtime = NativeMap.get(addr);
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
    return $.ctx.runtime.isolateAddress;
};

Native["com/sun/cldc/isolate/Isolate.getIsolates0.()[Lcom/sun/cldc/isolate/Isolate;"] = function(addr) {
    var isolatesAddr = J2ME.newObjectArray(Runtime.all.size);
    var isolates = J2ME.getArrayFromAddr(isolatesAddr);
    var n = 0;
    Runtime.all.forEach(function(runtime) {
        isolates[n++] = runtime.isolateAddress;
    });
    return isolatesAddr;
};

Native["com/sun/cldc/isolate/Isolate.setPriority0.(I)V"] = function(addr, newPriority) {
    // XXX Figure out if there's anything to do here.  If not, say so.
};

Native["com/sun/j2me/content/AppProxy.midletIsAdded.(ILjava/lang/String;)V"] = function(addr, suiteId, classNameAddr) {
  console.warn("com/sun/j2me/content/AppProxy.midletIsAdded.(ILjava/lang/String;)V not implemented");
};

Native["com/nokia/mid/impl/jms/core/Launcher.handleContent.(Ljava/lang/String;)V"] = function(addr, contentAddr) {
    var fileName = J2ME.fromStringAddr(contentAddr);

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

Native["org/mozilla/internal/Sys.eval.(Ljava/lang/String;)V"] = function(addr, srcAddr) {
    if (!release) {
        eval(J2ME.fromStringAddr(srcAddr));
    }
};

Native["java/lang/String.intern.()Ljava/lang/String;"] = function(addr) {
  var self = getHandle(addr);
  var value = J2ME.getArrayFromAddr(self.value);
  var internedStrings = J2ME.internedStrings;
  var internedString = internedStrings.getByRange(value, self.offset, self.count);
  if (internedString !== null) {
    return internedString._address;
  }
  internedStrings.put(value.subarray(self.offset, self.offset + self.count), self);
  return self._address;
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
