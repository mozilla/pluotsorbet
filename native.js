/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var asyncImplStringAsync = "Async";
function asyncImpl(returnKind, promise) {
  var ctx = $.ctx;

  promise.then(function(res) {
    if (returnKind === "J" || returnKind === "D") {
      ctx.current().stack.push2(res);
    } else if (returnKind !== "V") {
      ctx.current().stack.push(res);
    } else {
      // void, do nothing
    }
    ctx.execute();
  }, function(exception) {
    var classInfo = CLASSES.getClass("org/mozilla/internal/Sys");
    var methodInfo = classInfo.getMethodByNameString("throwException", "(Ljava/lang/Exception;)V", true);
    ctx.pushFrame(Frame.create(methodInfo, [exception]));
    ctx.execute();
  });
  $.pause(asyncImplStringAsync);
}

var Native = {};

Native["java/lang/System.arraycopy.(Ljava/lang/Object;ILjava/lang/Object;II)V"] = function(src, srcOffset, dst, dstOffset, length) {
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

Native["java/lang/System.getProperty0.(Ljava/lang/String;)Ljava/lang/String;"] = function(key) {
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

Native["java/lang/System.currentTimeMillis.()J"] = function() {
    return Long.fromNumber(Date.now());
};

Native["com/sun/cldchi/jvm/JVM.unchecked_char_arraycopy.([CI[CII)V"] = function(src, srcOffset, dst, dstOffset, length) {
  dst.set(src.subarray(srcOffset, srcOffset + length), dstOffset);
};

Native["com/sun/cldchi/jvm/JVM.unchecked_int_arraycopy.([II[III)V"] = function(src, srcOffset, dst, dstOffset, length) {
  dst.set(src.subarray(srcOffset, srcOffset + length), dstOffset);
};

Native["com/sun/cldchi/jvm/JVM.unchecked_obj_arraycopy.([Ljava/lang/Object;I[Ljava/lang/Object;II)V"] = function(src, srcOffset, dst, dstOffset, length) {
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

Native["com/sun/cldchi/jvm/JVM.monotonicTimeMillis.()J"] = function() {
    return Long.fromNumber(performance.now());
};

Native["java/lang/Object.getClass.()Ljava/lang/Class;"] = function() {
    return $.getRuntimeKlass(this.klass).classObject;
};

Native["java/lang/Object.wait.(J)V"] = function(timeout) {
    $.ctx.wait(this, timeout.toNumber());
};

Native["java/lang/Object.notify.()V"] = function() {
    $.ctx.notify(this);
};

Native["java/lang/Object.notifyAll.()V"] = function() {
    $.ctx.notify(this, true);
};

Native["java/lang/Class.getSuperclass.()Ljava/lang/Class;"] = function() {
    var superKlass = this.runtimeKlass.templateKlass.superKlass;
    if (!superKlass) {
      return null;
    }
    return superKlass.classInfo.getClassObject();
};

Native["java/lang/Class.invoke_clinit.()V"] = function() {
    var classInfo = this.runtimeKlass.templateKlass.classInfo;
    var className = classInfo.getClassNameSlow();
    var clinit = classInfo.staticInitializer;
    if (clinit && clinit.classInfo.getClassNameSlow() === className) {
        $.ctx.executeFrame(Frame.create(clinit, []));
    }
};

Native["java/lang/Class.invoke_verify.()V"] = function() {
    // There is currently no verification.
};

Native["java/lang/Class.init9.()V"] = function() {
    $.setClassInitialized(this.runtimeKlass);
};

Native["java/lang/Class.getName.()Ljava/lang/String;"] = function() {
    return J2ME.newString(this.runtimeKlass.templateKlass.classInfo.getClassNameSlow().replace(/\//g, "."));
};

Native["java/lang/Class.forName0.(Ljava/lang/String;)V"] = function(name) {
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

Native["java/lang/Class.forName1.(Ljava/lang/String;)Ljava/lang/Class;"] = function(name) {
  var className = J2ME.fromJavaString(name).replace(/\./g, "/");
  var classInfo = CLASSES.getClass(className);
  var classObject = classInfo.getClassObject();
  return classObject;
};

Native["java/lang/Class.newInstance0.()Ljava/lang/Object;"] = function() {
  if (this.runtimeKlass.templateKlass.classInfo.isInterface ||
      this.runtimeKlass.templateKlass.classInfo.isAbstract) {
    throw $.newInstantiationException("Can't instantiate interfaces or abstract classes");
  }

  if (this.runtimeKlass.templateKlass.classInfo instanceof J2ME.ArrayClassInfo) {
    throw $.newInstantiationException("Can't instantiate array classes");
  }

  return new this.runtimeKlass.templateKlass;
};

Native["java/lang/Class.newInstance1.(Ljava/lang/Object;)V"] = function(o) {
  // The following can trigger an unwind.
  var methodInfo = o.klass.classInfo.getLocalMethodByNameString("<init>", "()V", false);
  if (!methodInfo) {
    throw $.newInstantiationException("Can't instantiate classes without a nullary constructor");
  }
  J2ME.getLinkedMethod(methodInfo).call(o);
};

Native["java/lang/Class.isInterface.()Z"] = function() {
    return this.runtimeKlass.templateKlass.classInfo.isInterface ? 1 : 0;
};

Native["java/lang/Class.isArray.()Z"] = function() {
    return this.runtimeKlass.templateKlass.classInfo instanceof J2ME.ArrayClassInfo ? 1 : 0;
};

Native["java/lang/Class.isAssignableFrom.(Ljava/lang/Class;)Z"] = function(fromClass) {
    if (!fromClass)
        throw $.newNullPointerException();
    return J2ME.isAssignableTo(fromClass.runtimeKlass.templateKlass, this.runtimeKlass.templateKlass) ? 1 : 0;
};

Native["java/lang/Class.isInstance.(Ljava/lang/Object;)Z"] = function(obj) {
    return obj && J2ME.isAssignableTo(obj.klass, this.runtimeKlass.templateKlass) ? 1 : 0;
};

Native["java/lang/Float.floatToIntBits.(F)I"] = (function() {
    var fa = new Float32Array(1);
    var ia = new Int32Array(fa.buffer);
    return function(val) {
        fa[0] = val;
        return ia[0];
    }
})();

Native["java/lang/Double.doubleToLongBits.(D)J"] = (function() {
    var da = new Float64Array(1);
    var ia = new Int32Array(da.buffer);
    return function(val) {
        da[0] = val;
        return Long.fromBits(ia[0], ia[1]);
    }
})();

Native["java/lang/Float.intBitsToFloat.(I)F"] = (function() {
    var fa = new Float32Array(1);
    var ia = new Int32Array(fa.buffer);
    return function(val) {
        ia[0] = val;
        return fa[0];
    }
})();

Native["java/lang/Double.longBitsToDouble.(J)D"] = (function() {
    var da = new Float64Array(1);
    var ia = new Int32Array(da.buffer);
    return function(l) {
        ia[0] = l.low_;
        ia[1] = l.high_;
        return da[0];
    }
})();

Native["java/lang/Throwable.fillInStackTrace.()V"] = function() {
    this.stackTrace = [];
    $.ctx.frames.forEach(function(frame) {
        if (!frame.methodInfo)
            return;
        var methodInfo = frame.methodInfo;
        var methodName = methodInfo.name;
        if (!methodName)
            return;
        var classInfo = methodInfo.classInfo;
        var className = classInfo.getClassNameSlow();
        this.stackTrace.unshift({ className: className, methodName: methodName, methodSignature: methodInfo.signature, offset: frame.bci });
    }.bind(this));
};

Native["java/lang/Throwable.obtainBackTrace.()Ljava/lang/Object;"] = function() {
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

Native["java/lang/Runtime.freeMemory.()J"] = function() {
    return Long.fromInt(0x800000);
};

Native["java/lang/Runtime.totalMemory.()J"] = function() {
    return Long.fromInt(0x1000000);
};

Native["java/lang/Runtime.gc.()V"] = function() {
};

Native["java/lang/Math.floor.(D)D"] = function(val) {
    return Math.floor(val);
};

Native["java/lang/Math.asin.(D)D"] = function(val) {
    return Math.asin(val);
};

Native["java/lang/Math.acos.(D)D"] = function(val) {
    return Math.acos(val);
};

Native["java/lang/Math.atan.(D)D"] = function(val) {
    return Math.atan(val);
};

Native["java/lang/Math.atan2.(DD)D"] = function(x, y) {
    return Math.atan2(x, y);
};

Native["java/lang/Math.sin.(D)D"] = function(val) {
    return Math.sin(val);
};

Native["java/lang/Math.cos.(D)D"] = function(val) {
    return Math.cos(val);
};

Native["java/lang/Math.tan.(D)D"] = function(val) {
    return Math.tan(val);
};

Native["java/lang/Math.sqrt.(D)D"] = function(val) {
    return Math.sqrt(val);
};

Native["java/lang/Math.ceil.(D)D"] = function(val) {
    return Math.ceil(val);
};

Native["java/lang/Math.floor.(D)D"] = function(val) {
    return Math.floor(val);
};

Native["java/lang/Thread.currentThread.()Ljava/lang/Thread;"] = function() {
    return $.ctx.thread;
};

Native["java/lang/Thread.setPriority0.(II)V"] = function(oldPriority, newPriority) {
};

Native["java/lang/Thread.start0.()V"] = function() {
    // The main thread starts during bootstrap and don't allow calling start()
    // on already running threads.
    if (this === $.ctx.runtime.mainThread || this.alive)
        throw $.newIllegalThreadStateException();
    this.alive = true;
    this.pid = util.id();
    // Create a context for the thread and start it.
    var newCtx = new Context($.ctx.runtime);
    newCtx.thread = this;

    var classInfo = CLASSES.getClass("org/mozilla/internal/Sys");
    var run = classInfo.getMethodByNameString("runThread", "(Ljava/lang/Thread;)V", true);
    newCtx.start([Frame.create(run, [ this ])]);
}

Native["java/lang/Thread.isAlive.()Z"] = function() {
    return this.alive ? 1 : 0;
};

Native["java/lang/Thread.sleep.(J)V"] = function(delay) {
    asyncImpl("V", new Promise(function(resolve, reject) {
        window.setTimeout(resolve, delay.toNumber());
    }));
};

Native["java/lang/Thread.yield.()V"] = function() {
    $.yield("Thread.yield");
};

Native["java/lang/Thread.activeCount.()I"] = function() {
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

Native["com/sun/cldchi/io/ConsoleOutputStream.write.(I)V"] = function(ch) {
    console.print(ch);
};

Native["com/sun/cldc/io/ResourceInputStream.open.(Ljava/lang/String;)Ljava/lang/Object;"] = function(name) {
    var fileName = J2ME.fromJavaString(name);
    var data = JARStore.loadFile(fileName);
    var obj = null;
    if (data) {
        obj = J2ME.newObject(CLASSES.java_lang_Object.klass);
        obj.data = data;
        obj.pos = 0;
    }
    return obj;
};

Native["com/sun/cldc/io/ResourceInputStream.clone.(Ljava/lang/Object;)Ljava/lang/Object;"] = function(source) {
    var obj = J2ME.newObject(CLASSES.java_lang_Object.klass);
    obj.data = new Uint8Array(source.data);
    obj.pos = source.pos;
    return obj;
};

Native["com/sun/cldc/io/ResourceInputStream.bytesRemain.(Ljava/lang/Object;)I"] = function(handle) {
    return handle.data.length - handle.pos;
};

Native["com/sun/cldc/io/ResourceInputStream.readByte.(Ljava/lang/Object;)I"] = function(handle) {
    return (handle.data.length - handle.pos > 0) ? handle.data[handle.pos++] : -1;
};

Native["com/sun/cldc/io/ResourceInputStream.readBytes.(Ljava/lang/Object;[BII)I"] = function(handle, b, off, len) {
    var data = handle.data;
    var remaining = data.length - handle.pos;
    if (len > remaining)
        len = remaining;
    for (var n = 0; n < len; ++n)
        b[off+n] = data[handle.pos+n];
    handle.pos += len;
    return (len > 0) ? len : -1;
};

Native["java/lang/ref/WeakReference.initializeWeakReference.(Ljava/lang/Object;)V"] = function(target) {
    this.target = target;
};

Native["java/lang/ref/WeakReference.get.()Ljava/lang/Object;"] = function() {
    return this.target ? this.target : null;
};

Native["java/lang/ref/WeakReference.clear.()V"] = function() {
    this.target = null;
};

Native["com/sun/cldc/isolate/Isolate.registerNewIsolate.()V"] = function() {
    this.id = util.id();
};

Native["com/sun/cldc/isolate/Isolate.getStatus.()I"] = function() {
    return this.runtime ? this.runtime.status : J2ME.RuntimeStatus.New;
};

Native["com/sun/cldc/isolate/Isolate.nativeStart.()V"] = function() {
    $.ctx.runtime.jvm.startIsolate(this);
};

Native["com/sun/cldc/isolate/Isolate.waitStatus.(I)V"] = function(status) {
    asyncImpl("V", new Promise((function(resolve, reject) {
        var runtime = this.runtime;
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
    }).bind(this)));
};

Native["com/sun/cldc/isolate/Isolate.currentIsolate0.()Lcom/sun/cldc/isolate/Isolate;"] = function() {
    return $.ctx.runtime.isolate;
};

Native["com/sun/cldc/isolate/Isolate.getIsolates0.()[Lcom/sun/cldc/isolate/Isolate;"] = function() {
    var isolates = J2ME.newObjectArray(Runtime.all.keys().length);
    var n = 0;
    Runtime.all.forEach(function (runtime) {
        isolates[n++] = runtime.isolate;
    });
    return isolates;
};

Native["com/sun/cldc/isolate/Isolate.id0.()I"] = function() {
    return this.id;
};

Native["com/sun/cldc/isolate/Isolate.setPriority0.(I)V"] = function(newPriority) {
};

Native["com/sun/cldc/i18n/j2me/UTF_8_Reader.init.([B)V"] = function(data) {
    this.decoded = new TextDecoder("UTF-8").decode(data);
};

Native["com/sun/cldc/i18n/j2me/UTF_8_Reader.readNative.([CII)I"] = function(cbuf, off, len) {
    if (this.decoded.length === 0) {
      return -1;
    }

    for (var i = 0; i < len; i++) {
      cbuf[i + off] = this.decoded.charCodeAt(i);
    }

    this.decoded = this.decoded.substring(len);

    return len;
};

Native["java/io/DataOutputStream.UTFToBytes.(Ljava/lang/String;)[B"] = function(jStr) {
    var str = J2ME.fromJavaString(jStr);

    var utflen = 0;

    for (var i = 0; i < str.length; i++) {
        var c = str.charCodeAt(i);
        if ((c >= 0x0001) && (c <= 0x007F)) {
            utflen++;
        } else if (c > 0x07FF) {
            utflen += 3;
        } else {
            utflen += 2;
        }
    }

    if (utflen > 65535) {
        throw $.newUTFDataFormatException();
    }

    var count = 0;
    var bytearr = J2ME.newByteArray(utflen + 2);
    bytearr[count++] = (utflen >>> 8) & 0xFF;
    bytearr[count++] = (utflen >>> 0) & 0xFF;
    for (var i = 0; i < str.length; i++) {
        var c = str.charCodeAt(i);
        if ((c >= 0x0001) && (c <= 0x007F)) {
            bytearr[count++] = c;
        } else if (c > 0x07FF) {
            bytearr[count++] = 0xE0 | ((c >> 12) & 0x0F);
            bytearr[count++] = 0x80 | ((c >>  6) & 0x3F);
            bytearr[count++] = 0x80 | ((c >>  0) & 0x3F);
        } else {
            bytearr[count++] = 0xC0 | ((c >>  6) & 0x1F);
            bytearr[count++] = 0x80 | ((c >>  0) & 0x3F);
        }
    }

    return bytearr;
};

Native["com/sun/cldc/i18n/j2me/UTF_8_Writer.encodeUTF8.([CII)[B"] = function(cbuf, off, len) {
  var outputArray = [];

  var pendingSurrogate = this.pendingSurrogate;

  var inputChar = 0;
  var outputSize = 0;
  var count = 0;

  while (count < len) {
    var outputByte = new Int8Array(4);     // Never more than 4 encoded bytes
    inputChar = 0xffff & cbuf[off + count];
    if (0 != pendingSurrogate) {
      if (0xdc00 <= inputChar && inputChar <= 0xdfff) {
        //000u uuuu xxxx xxxx xxxx xxxx
        //1101 10ww wwxx xxxx   1101 11xx xxxx xxxx
        var highHalf = (pendingSurrogate & 0x03ff) + 0x0040;
        var lowHalf = inputChar & 0x03ff;
        inputChar = (highHalf << 10) | lowHalf;
      } else {
        // write replacement value instead of unpaired surrogate
        outputByte[0] = replacementValue;
        outputSize = 1;
        outputArray.push(outputByte.subarray(0, outputSize));
      }
      pendingSurrogate = 0;
    }
    if (inputChar < 0x80) {
      outputByte[0] = inputChar;
      outputSize = 1;
    } else if (inputChar < 0x800) {
      outputByte[0] = 0xc0 | ((inputChar >> 6) & 0x1f);
      outputByte[1] = 0x80 | (inputChar & 0x3f);
      outputSize = 2;
    } else if (0xd800 <= inputChar && inputChar <= 0xdbff) {
      pendingSurrogate = inputChar;
      outputSize = 0;
    } else if (0xdc00 <= inputChar && inputChar <= 0xdfff) {
      // unpaired surrogate
      outputByte[0] = replacementValue;
      outputSize = 1;
    } else if (inputChar < 0x10000) {
      outputByte[0] = 0xe0 | ((inputChar >> 12) & 0x0f);
      outputByte[1] = 0x80 | ((inputChar >> 6) & 0x3f);
      outputByte[2] = 0x80 | (inputChar & 0x3f);
      outputSize = 3;
    } else {
      /* 21 bits: 1111 0xxx  10xx xxxx  10xx xxxx  10xx xxxx
       * a aabb  bbbb cccc  ccdd dddd
       */
      outputByte[0] = 0xf0 | ((inputChar >> 18) & 0x07);
      outputByte[1] = 0x80 | ((inputChar >> 12) & 0x3f);
      outputByte[2] = 0x80 | ((inputChar >> 6) & 0x3f);
      outputByte[3] = 0x80 | (inputChar & 0x3f);
      outputSize = 4;
    }
    outputArray.push(outputByte.subarray(0, outputSize));
    count++;
  }

  this.pendingSurrogate = pendingSurrogate;

  var totalSize = outputArray.reduce(function(total, cur) {
    return total + cur.length;
  }, 0);

  var res = J2ME.newByteArray(totalSize);
  outputArray.reduce(function(total, cur) {
    res.set(cur, total);
    return total + cur.length;
  }, 0);

  return res;
};

Native["com/sun/cldc/i18n/j2me/UTF_8_Writer.sizeOf.([CII)I"] = function(cbuf, off, len) {
  var inputChar = 0;
  var outputSize = 0;
  var outputCount = 0;
  var count = 0;
  var localPendingSurrogate = this.pendingSurrogate;
  while (count < len) {
    inputChar = 0xffff & cbuf[off + count];
    if (0 != localPendingSurrogate) {
      if (0xdc00 <= inputChar && inputChar <= 0xdfff) {
        //000u uuuu xxxx xxxx xxxx xxxx
        //1101 10ww wwxx xxxx   1101 11xx xxxx xxxx
        var highHalf = (localPendingSurrogate & 0x03ff) + 0x0040;
        var lowHalf = inputChar & 0x03ff;
        inputChar = (highHalf << 10) | lowHalf;
      } else {
        // going to write replacement value instead of unpaired surrogate
        outputSize = 1;
        outputCount += outputSize;
      }
      localPendingSurrogate = 0;
    }
    if (inputChar < 0x80) {
      outputSize = 1;
    } else if (inputChar < 0x800) {
      outputSize = 2;
    } else if (0xd800 <= inputChar && inputChar <= 0xdbff) {
      localPendingSurrogate = inputChar;
      outputSize = 0;
    } else if (0xdc00 <= inputChar && inputChar <= 0xdfff) {
      // unpaired surrogate
      // going to output replacementValue;
      outputSize = 1;
    } else if (inputChar < 0x10000) {
      outputSize = 3;
    } else {
      /* 21 bits: 1111 0xxx  10xx xxxx  10xx xxxx  10xx xxxx
       * a aabb  bbbb cccc  ccdd dddd
       */
      outputSize = 4;
    }
    outputCount += outputSize;
    count++;
  }

  return outputCount;
};

Native["com/sun/j2me/content/AppProxy.midletIsAdded.(ILjava/lang/String;)V"] = function(suiteId, className) {
  console.warn("com/sun/j2me/content/AppProxy.midletIsAdded.(ILjava/lang/String;)V not implemented");
};

Native["com/nokia/mid/impl/jms/core/Launcher.handleContent.(Ljava/lang/String;)V"] = function(content) {
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

    Native[signature] = function() { return warnOnce() };
}

Native["org/mozilla/internal/Sys.eval.(Ljava/lang/String;)V"] = function(src) {
    if (!release) {
        eval(J2ME.fromJavaString(src));
    }
};

Native["java/io/ByteArrayOutputStream.write.([BII)V"] = function(b, off, len) {
  if ((off < 0) || (off > b.length) || (len < 0) ||
      ((off + len) > b.length)) {
    throw $.newIndexOutOfBoundsException();
  }

  if (len == 0) {
    return;
  }

  var count = this.count;
  var buf = this.buf;

  var newcount = count + len;
  if (newcount > buf.length) {
    var newbuf = J2ME.newByteArray(Math.max(buf.length << 1, newcount));
    newbuf.set(buf);
    buf = newbuf;
    this.buf = buf;
  }

  buf.set(b.subarray(off, off + len), count);
  this.count = newcount;
};

Native["java/io/ByteArrayOutputStream.write.(I)V"] = function(value) {
  var count = this.count;
  var buf = this.buf;

  var newcount = count + 1;
  if (newcount > buf.length) {
    var newbuf = J2ME.newByteArray(Math.max(buf.length << 1, newcount));
    newbuf.set(buf);
    buf = newbuf;
    this.buf = buf;
  }

  buf[count] = value;
  this.count = newcount;
};

Native["java/io/ByteArrayInputStream.init.([BII)V"] = function(buf, offset, length) {
  if (!buf) {
    throw $.newNullPointerException();
  }

  this.buf = buf;
  this.pos = this.mark = offset;
  this.count = (offset + length <= buf.length) ? (offset + length) : buf.length;
};

Native["java/io/ByteArrayInputStream.read.()I"] = function() {
  return (this.pos < this.count) ? (this.buf[this.pos++] & 0xFF) : -1;
};

Native["java/io/ByteArrayInputStream.read.([BII)I"] = function(b, off, len) {
  if (!b) {
    throw $.newNullPointerException();
  }

  if ((off < 0) || (off > b.length) || (len < 0) ||
      ((off + len) > b.length)) {
    throw $.newIndexOutOfBoundsException();
  }

  if (this.pos >= this.count) {
    return -1;
  }
  if (this.pos + len > this.count) {
    len = this.count - this.pos;
  }
  if (len === 0) {
    return 0;
  }

  b.set(this.buf.subarray(this.pos, this.pos + len), off);

  this.pos += len;
  return len;
};

Native["java/io/ByteArrayInputStream.skip.(J)J"] = function(long) {
  var n = long.toNumber();

  if (this.pos + n > this.count) {
    n = this.count - this.pos;
  }

  if (n < 0) {
    return Long.fromNumber(0);
  }

  this.pos += n;

  return Long.fromNumber(n);
};

Native["java/io/ByteArrayInputStream.available.()I"] = function() {
  return this.count - this.pos;
};

Native["java/io/ByteArrayInputStream.mark.(I)V"] = function(readAheadLimit) {
  this.mark = this.pos;
};

Native["java/io/ByteArrayInputStream.reset.()V"] = function() {
  this.pos = this.mark;
};
