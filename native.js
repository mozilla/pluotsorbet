/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var Native = {};

Native.create = createAlternateImpl.bind(null, Native);

Native.create("java/lang/System.arraycopy.(Ljava/lang/Object;ILjava/lang/Object;II)V", function(src, srcOffset, dst, dstOffset, length) {
    if (!src || !dst)
        throw new JavaException("java/lang/NullPointerException", "Cannot copy to/from a null array.");
    var srcKlass = src.klass;
    var dstKlass = dst.klass;

    if (!srcKlass.isArrayKlass || !dstKlass.isArrayKlass)
        throw new JavaException("java/lang/ArrayStoreException", "Can only copy to/from array types.");
    if (srcOffset < 0 || (srcOffset+length) > src.length || dstOffset < 0 || (dstOffset+length) > dst.length || length < 0)
        throw new JavaException("java/lang/ArrayIndexOutOfBoundsException", "Invalid index.");
    var srcIsPrimitive = !(src instanceof Array);
    var dstIsPrimitive = !(dst instanceof Array);
    if ((srcIsPrimitive && dstIsPrimitive && srcKlass !== dstKlass) ||
        (srcIsPrimitive && !dstIsPrimitive) ||
        (!srcIsPrimitive && dstIsPrimitive)) {
        throw new JavaException("java/lang/ArrayStoreException",
                                "Incompatible component types: " + srcKlass + " -> " + dstKlass);
    }
    if (!dstIsPrimitive) {
        if (srcKlass != dstKlass && !J2ME.isAssignableTo(srcKlass.elementKlass, dstKlass.elementKlass)) {
            var copy = function(to, from) {
                var obj = src[from];
                if (obj && !J2ME.isAssignableTo(obj.klass, dstKlass.elementKlass)) {
                    throw new JavaException("java/lang/ArrayStoreException", "Incompatible component types.");
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
});

Native.create("java/lang/System.getProperty0.(Ljava/lang/String;)Ljava/lang/String;", function(key) {
    var value;
    switch (util.fromJavaString(key)) {
    case "microedition.encoding":
        value = "UTF-8";
        break;
    case "microedition.locale":
        value = navigator.language;
        break;
    case "microedition.platform":
        value = urlParams.platform ? urlParams.platform : "NOKIA503/JAVA_RUNTIME_VERSION=NOKIA_ASHA_1_2";
        break;
    case "microedition.platformimpl":
        value = null;
        break;
    case "microedition.profiles":
        value = "MIDP-2.0"
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
        value = "file:///";
        break;
    case "fileconn.dir.private":
        value = "file:///";
        break;
    case "fileconn.dir.applications.bookmarks":
        value = "file:///";
        break;
    case "fileconn.dir.received":
        value = "file:///";
        break;
    case "fileconn.dir.photos":
        value = "file:///Photos/";
        break;
    case "fileconn.dir.roots.names":
        // The names here should be localized.
        value = "Memory card;Phone memory;Private"
        break;
    case "fileconn.dir.roots.external":
        value = "file:///MemoryCard;file:///;file:///";
        break;
    case "fileconn.dir.photos.name":
        value = "Photos";
        break;
    case "fileconn.dir.videos.name":
        value = "Videos";
        break;
    case "fileconn.dir.recordings.name":
        value = "Recordings";
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
    case "com.nokia.multisim.slots":
        console.warn("Property 'com.nokia.multisim.slots' is a stub");
        value = "1";
        break;
    case "com.nokia.multisim.imsi.sim2":
        console.warn("Property 'com.nokia.multisim.imsi.sim2' is a stub");
        value = null;
        break;
    case "com.nokia.mid.batterylevel":
        // http://developer.nokia.com/community/wiki/Checking_battery_level_in_Java_ME
        value = Math.floor(navigator.battery.level * 100).toString();
        break;
    case "com.nokia.mid.imsi":
        console.warn("Property 'com.nokia.mid.imsi' is a stub");
        value = "000000000000000";
        break;
    case "com.nokia.mid.ui.version":
        console.warn("Property 'com.nokia.mid.ui.version' is a stub");
        value = "1.6";
        break;
    case "com.nokia.mid.mnc":
        // The concatenation of the MCC and MNC for the ICC (i.e. SIM card).
        value = util.pad(mobileInfo.icc.mcc, 3) + util.pad(mobileInfo.icc.mnc, 3);
        break;
    case "com.nokia.mid.networkID":
        // The concatenation of MCC and MNC for the network.
        value = util.pad(mobileInfo.network.mcc, 3) + util.pad(mobileInfo.network.mnc, 3);
        break;
    case "com.nokia.mid.imei":
        console.warn("Property 'com.nokia.mid.imei' is a stub");
        value = "";
        break;
    case "com.nokia.mid.ui.customfontsize":
        console.warn("Property 'com.nokia.mid.ui.customfontsize' is a stub");
        value = "false";
        break;
    case "classpathext":
        value = null;
        break;
    case "supports.audio.capture":
        value = "true";
        break;
    case "supports.recording":
        value = "true";
        break;
    case "audio.encodings":
        value = "audio/ogg";
        break;
    case "video.snapshot.encodings":
        value = "encoding=jpeg";
        break;
    default:
        console.warn("UNKNOWN PROPERTY (java/lang/System): " + util.fromJavaString(key));
        value = null;
        break;
    }
    return value;
});

Native.create("java/lang/System.currentTimeMillis.()J", function() {
    return Long.fromNumber(Date.now());
});

Native.create("com/sun/cldchi/jvm/JVM.unchecked_char_arraycopy.([CI[CII)V", function(src, srcOffset, dst, dstOffset, length) {
  dst.set(src.subarray(srcOffset, srcOffset + length), dstOffset);
});

Native.create("com/sun/cldchi/jvm/JVM.unchecked_int_arraycopy.([II[III)V", function(src, srcOffset, dst, dstOffset, length) {
  dst.set(src.subarray(srcOffset, srcOffset + length), dstOffset);
});

Native.create("com/sun/cldchi/jvm/JVM.unchecked_obj_arraycopy.([Ljava/lang/Object;I[Ljava/lang/Object;II)V", function(src, srcOffset, dst, dstOffset, length) {
    if (dst !== src || dstOffset < srcOffset) {
        for (var n = 0; n < length; ++n)
            dst[dstOffset++] = src[srcOffset++];
    } else {
        dstOffset += length;
        srcOffset += length;
        for (var n = 0; n < length; ++n)
            dst[--dstOffset] = src[--srcOffset];
    }
});

Native.create("com/sun/cldchi/jvm/JVM.monotonicTimeMillis.()J", function() {
    return Long.fromNumber(performance.now());
});

Native.create("java/lang/Object.getClass.()Ljava/lang/Class;", function(ctx) {
    return J2ME.runtimeKlass(ctx.runtime, this.klass).classObject;
});

Native.create("java/lang/Object.hashCode.()I", function() {
    var hashCode = this.hashCode;
    while (!hashCode)
        hashCode = this.hashCode = util.id();
    return hashCode;
});

Native.create("java/lang/Object.wait.(J)V", function(timeout, _, ctx) {
    ctx.wait(this, timeout.toNumber());
});

Native.create("java/lang/Object.notify.()V", function(ctx) {
    ctx.notify(this);
});

Native.create("java/lang/Object.notifyAll.()V", function(ctx) {
    ctx.notify(this, true);
});

Native.create("java/lang/Class.invoke_clinit.()V", function(ctx) {
    var classInfo = this.classInfo;
    var className = classInfo.className;
    var runtime = ctx.runtime;
    if (runtime.initialized[className] || runtime.pending[className])
        return;
    runtime.pending[className] = true;
    if (className === "com/sun/cldc/isolate/Isolate") {
        // The very first isolate is granted access to the isolate API.
        // ctx.runtime.setStatic(CLASSES.getField(classInfo, "S._API_access_ok.I"), 1);
        var isolate = classInfo.getClassObject(ctx).classObject;
        CLASSES.getField(classInfo, "S._API_access_ok.I").set(isolate, 1);
    }
    var clinit = CLASSES.getMethod(classInfo, "S.<clinit>.()V");
    var frames = [];
    if (clinit) {
        frames.push(new Frame(clinit, [], 0));
    }
    if (classInfo.superClass) {
        var classInitFrame = ctx.getClassInitFrame(classInfo.superClass);
        if (classInitFrame) {
            frames.push(classInitFrame);
        }
    }
    if (frames.length) {
        ctx.executeNewFrameSet(frames);
    }
});

Native.create("java/lang/Class.init9.()V", function(ctx) {
    var classInfo = this.classInfo;
    var className = classInfo.className;
    var runtime = ctx.runtime;
    if (runtime.initialized[className])
        return;
    runtime.pending[className] = false;
    runtime.initialized[className] = true;
});

Native.create("java/lang/Class.getName.()Ljava/lang/String;", function() {
    return this.runtimeKlass.templateKlass.classInfo.className.replace(/\//g, ".");
});

Native.create("java/lang/Class.forName.(Ljava/lang/String;)Ljava/lang/Class;", function(name, ctx) {
    try {
        if (!name)
            throw new J2ME.ClassNotFoundException();
        var className = util.fromJavaString(name).replace(/\./g, "/");
        var classInfo = null;
        classInfo = CLASSES.getClass(className);
    } catch (e) {
        if (e instanceof (J2ME.ClassNotFoundException))
            throw new JavaException("java/lang/ClassNotFoundException", "'" + className + "' not found.");
        throw e;
    }
    return classInfo.getClassObject(ctx);
});

Native.create("java/lang/Class.newInstance.()Ljava/lang/Object;", function(ctx) {
    var className = this.runtimeKlass.templateKlass.classInfo.className;
    var syntheticMethod = new MethodInfo({
      name: "ClassNewInstanceSynthetic",
      signature: "()Ljava/lang/Object;",
      isStatic: true,
      classInfo: {
        className: className,
        vmc: {},
        vfc: {},
        constant_pool: [
          null,
          { tag: TAGS.CONSTANT_Class, name_index: 2 },
          { bytes: className },
          { tag: TAGS.CONSTANT_Methodref, class_index: 1, name_and_type_index: 4 },
          { name_index: 5, signature_index: 6 },
          { bytes: "<init>" },
          { bytes: "()V" },
        ]
      },
      code: new Uint8Array([
        0xbb, 0x00, 0x01, // new <idx=1>
        0x59,             // dup
        0xb7, 0x00, 0x03, // invokespecial <idx=3>
        0xb0              // areturn
      ]),
    });
    return ctx.executeNewFrameSet([new Frame(syntheticMethod, [], 0)]);
});

Native.create("java/lang/Class.isInterface.()Z", function() {
    return ACCESS_FLAGS.isInterface(this.runtimeKlass.templateKlass.classInfo.access_flags);
});

Native.create("java/lang/Class.isArray.()Z", function() {
    return !!this.runtimeKlass.templateKlass.classInfo.isArrayClass;
});

Native.create("java/lang/Class.isAssignableFrom.(Ljava/lang/Class;)Z", function(fromClass) {
    if (!fromClass)
        throw new JavaException("java/lang/NullPointerException");
    return J2ME.isAssignableTo(fromClass.runtimeKlass.templateKlass, this.runtimeKlass.templateKlass);
});

Native.create("java/lang/Class.isInstance.(Ljava/lang/Object;)Z", function(obj) {
    return obj && J2ME.isAssignableTo(obj.klass, this.runtimeKlass.templateKlass);
});

Native.create("java/lang/Float.floatToIntBits.(F)I", (function() {
    var fa = new Float32Array(1);
    var ia = new Int32Array(fa.buffer);
    return function(val) {
        fa[0] = val;
        return ia[0];
    }
})());

Native.create("java/lang/Double.doubleToLongBits.(D)J", (function() {
    var da = new Float64Array(1);
    var ia = new Int32Array(da.buffer);
    return function(val, _) {
        da[0] = val;
        return Long.fromBits(ia[0], ia[1]);
    }
})());

Native.create("java/lang/Float.intBitsToFloat.(I)F", (function() {
    var fa = new Float32Array(1);
    var ia = new Int32Array(fa.buffer);
    return function(val) {
        ia[0] = val;
        return fa[0];
    }
})());

Native.create("java/lang/Double.longBitsToDouble.(J)D", (function() {
    var da = new Float64Array(1);
    var ia = new Int32Array(da.buffer);
    return function(l, _) {
        ia[0] = l.low_;
        ia[1] = l.high_;
        return da[0];
    }
})());

Native.create("java/lang/Throwable.fillInStackTrace.()V", function(ctx) {
    this.stackTrace = [];
    ctx.frames.forEach(function(frame) {
        if (!frame.methodInfo)
            return;
        var methodInfo = frame.methodInfo;
        var methodName = methodInfo.name;
        if (!methodName)
            return;
        var classInfo = methodInfo.classInfo;
        var className = classInfo.className;
        this.stackTrace.unshift({ className: className, methodName: methodName, offset: frame.bci });
    }.bind(this));
});

Native.create("java/lang/Throwable.obtainBackTrace.()Ljava/lang/Object;", function() {
    var result = null;
    if (this.stackTrace) {
        var depth = this.stackTrace.length;
        var classNames = J2ME.newObjectArray(depth);
        var methodNames = J2ME.newObjectArray(depth);
        var offsets = util.newPrimitiveArray("I", depth);
        this.stackTrace.forEach(function(e, n) {
            classNames[n] = util.newString(e.className);
            methodNames[n] = util.newString(e.methodName);
            offsets[n] = e.offset;
        });
        result = J2ME.newObjectArray(3);
        result[0] = classNames;
        result[1] = methodNames;
        result[2] = offsets;
    }
    return result;
});

Native.create("java/lang/Runtime.freeMemory.()J", function() {
    return Long.fromInt(0x800000);
});

Native.create("java/lang/Runtime.totalMemory.()J", function() {
    return Long.fromInt(0x1000000);
});

Native.create("java/lang/Runtime.gc.()V", function() {
});

Native.create("java/lang/Math.floor.(D)D", function(val, _) {
    return Math.floor(val);
});

Native.create("java/lang/Math.asin.(D)D", function(val, _) {
    return Math.asin(val);
});

Native.create("java/lang/Math.acos.(D)D", function(val, _) {
    return Math.acos(val);
});

Native.create("java/lang/Math.atan.(D)D", function(val, _) {
    return Math.atan(val);
});

Native.create("java/lang/Math.atan2.(DD)D", function(x, _1, y, _2) {
    return Math.atan2(x, y);
});

Native.create("java/lang/Math.sin.(D)D", function(val, _) {
    return Math.sin(val);
});

Native.create("java/lang/Math.cos.(D)D", function(val, _) {
    return Math.cos(val);
});

Native.create("java/lang/Math.tan.(D)D", function(val, _) {
    return Math.tan(val);
});

Native.create("java/lang/Math.sqrt.(D)D", function(val, _) {
    return Math.sqrt(val);
});

Native.create("java/lang/Math.ceil.(D)D", function(val, _) {
    return Math.ceil(val);
});

Native.create("java/lang/Math.floor.(D)D", function(val, _) {
    return Math.floor(val);
});

Native.create("java/lang/Thread.currentThread.()Ljava/lang/Thread;", function(ctx) {
    return ctx.thread;
});

Native.create("java/lang/Thread.setPriority0.(II)V", function(oldPriority, newPriority) {
});

Native.create("java/lang/Thread.start0.()V", function(ctx) {
    // The main thread starts during bootstrap and don't allow calling start()
    // on already running threads.
    if (this === ctx.runtime.mainThread || this.alive)
        throw new JavaException("java/lang/IllegalThreadStateException");
    this.alive = true;
    this.pid = util.id();
    var run = CLASSES.getMethod(this.klass.classInfo, "I.run.()V");
    // Create a context for the thread and start it.
    var ctx = new Context(ctx.runtime);
    ctx.thread = this;

    var syntheticMethod = new MethodInfo({
      name: "ThreadStart0Synthetic",
      signature: "()V",
      classInfo: {
        className: this.klass.classInfo.className,
        vmc: {},
        vfc: {},
        constant_pool: [
          null,
          { tag: TAGS.CONSTANT_Methodref, class_index: 2, name_and_type_index: 4 },
          { tag: TAGS.CONSTANT_Class, name_index: 3 },
          { bytes: "java/lang/Thread" },
          { tag: TAGS.CONSTANT_Methodref, name_index: 5, signature_index: 6 },
          { bytes: "run" },
          { bytes: "()V" },
          { tag: TAGS.CONSTANT_Methodref, class_index: 2, name_and_type_index: 8 },
          { name_index: 9, signature_index: 10 },
          { bytes: "internalExit" },
          { bytes: "()V" },
        ],
      },
      code: new Uint8Array([
        0x2a,             // aload_0
        0x59,             // dup
        0xb6, 0x00, 0x01, // invokespecial <idx=1>
        0xb7, 0x00, 0x07, // invokespecial <idx=7>
        0xb1,             // return
      ])
    });

    ctx.frames.push(new Frame(syntheticMethod, [ this ], 0));
    ctx.resume();
});

Native.create("java/lang/Thread.internalExit.()V", function() {
    this.alive = false;
});

Native.create("java/lang/Thread.isAlive.()Z", function() {
    return !!this.alive;
});

Native.create("java/lang/Thread.sleep.(J)V", function(delay, _) {
    return new Promise(function(resolve, reject) {
        window.setTimeout(resolve, delay.toNumber());
    })
}, true);

Native.create("java/lang/Thread.yield.()V", function() {
    throw VM.Yield;
});

Native.create("java/lang/Thread.activeCount.()I", function(ctx) {
    return ctx.runtime.threadCount;
});

Native.create("com/sun/cldchi/io/ConsoleOutputStream.write.(I)V", function(ch) {
    console.print(ch);
});

Native.create("com/sun/cldc/io/ResourceInputStream.open.(Ljava/lang/String;)Ljava/lang/Object;", function(name) {
    var fileName = util.fromJavaString(name);
    var data = CLASSES.loadFile(fileName);
    var obj = null;
    if (data) {
        obj = util.newObject(CLASSES.java_lang_Object);
        obj.data = new Uint8Array(data);
        obj.pos = 0;
    }
    return obj;
});

Native.create("com/sun/cldc/io/ResourceInputStream.clone.(Ljava/lang/Object;)Ljava/lang/Object;", function(source) {
    var obj = util.newObject(CLASSES.java_lang_Object);
    obj.data = new Uint8Array(source.data);
    obj.pos = source.pos;
    return obj;
});

Override.create("com/sun/cldc/io/ResourceInputStream.available.()I", function() {
    var handle = this.klass.classInfo.getField("I.fileDecoder.Ljava/lang/Object;").get(this);

    if (!handle) {
        throw new JavaException("java/io/IOException");
    }

    return handle.data.length - handle.pos;
});

Override.create("com/sun/cldc/io/ResourceInputStream.read.()I", function() {
    var handle = this.klass.classInfo.getField("I.fileDecoder.Ljava/lang/Object;").get(this);

    if (!handle) {
        throw new JavaException("java/io/IOException");
    }

    return (handle.data.length - handle.pos > 0) ? handle.data[handle.pos++] : -1;
});

Native.create("com/sun/cldc/io/ResourceInputStream.readBytes.(Ljava/lang/Object;[BII)I", function(handle, b, off, len) {
    var data = handle.data;
    var remaining = data.length - handle.pos;
    if (len > remaining)
        len = remaining;
    for (var n = 0; n < len; ++n)
        b[off+n] = data[handle.pos+n];
    handle.pos += len;
    return (len > 0) ? len : -1;
});

Native.create("com/sun/cldc/i18n/uclc/DefaultCaseConverter.toLowerCase.(C)C", function(char) {
    return String.fromCharCode(char).toLowerCase().charCodeAt(0);
});

Native.create("com/sun/cldc/i18n/uclc/DefaultCaseConverter.toUpperCase.(C)C", function(char) {
    return String.fromCharCode(char).toUpperCase().charCodeAt(0);
});

Native.create("java/lang/ref/WeakReference.initializeWeakReference.(Ljava/lang/Object;)V", function(target) {
    this.target = target;
});

Native.create("java/lang/ref/WeakReference.get.()Ljava/lang/Object;", function() {
    return this.target ? this.target : null;
});

Native.create("java/lang/ref/WeakReference.clear.()V", function() {
    this.target = null;
});

Native.create("com/sun/cldc/isolate/Isolate.registerNewIsolate.()V", function() {
    this.id = util.id();
});

Native.create("com/sun/cldc/isolate/Isolate.getStatus.()I", function() {
    return this.runtime ? this.runtime.status : J2ME.RuntimeStatus.New;
});

Native.create("com/sun/cldc/isolate/Isolate.nativeStart.()V", function(ctx) {
    ctx.runtime.jvm.startIsolate(this);
});

Native.create("com/sun/cldc/isolate/Isolate.waitStatus.(I)V", function(status) {
    return new Promise((function(resolve, reject) {
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
    }).bind(this));
}, true);

Native.create("com/sun/cldc/isolate/Isolate.currentIsolate0.()Lcom/sun/cldc/isolate/Isolate;", function(ctx) {
    return ctx.runtime.isolate;
});

Native.create("com/sun/cldc/isolate/Isolate.getIsolates0.()[Lcom/sun/cldc/isolate/Isolate;", function() {
    var isolates = J2ME.newObjectArray(Runtime.all.keys().length);
    var n = 0;
    Runtime.all.forEach(function (runtime) {
        isolates[n++] = runtime.isolate;
    });
    return isolates;
});

Native.create("com/sun/cldc/isolate/Isolate.id0.()I", function() {
    return this.id;
});

Native.create("com/sun/cldc/isolate/Isolate.setPriority0.(I)V", function(newPriority) {
});

var links = {};
var waitingForLinks = {};

Native.create("com/sun/midp/links/LinkPortal.getLinkCount0.()I", function(ctx) {
    return new Promise(function(resolve, reject) {
        var isolateId = ctx.runtime.isolate.id;

        if (!links[isolateId]) {
            waitingForLinks[isolateId] = function() {
                resolve(links[isolateId].length);
            }

            return;
        }

        resolve(links[isolateId].length);
    });
}, true);

Native.create("com/sun/midp/links/LinkPortal.getLinks0.([Lcom/sun/midp/links/Link;)V", function(linkArray, ctx) {
    var isolateId = ctx.runtime.isolate.id;

    for (var i = 0; i < links[isolateId].length; i++) {
        var nativePointer = links[isolateId][i].$nativePointer;
        linkArray[i].$nativePointer = nativePointer;
        linkArray[i].sender = links[isolateId][i].sender;
        linkArray[i].receiver = links[isolateId][i].receiver;
    }
});

Native.create("com/sun/midp/links/LinkPortal.setLinks0.(I[Lcom/sun/midp/links/Link;)V", function(id, linkArray) {
    links[id] = linkArray;

    if (waitingForLinks[id]) {
        waitingForLinks[id]();
    }
});

Native.create("com/sun/midp/links/Link.init0.(II)V", function(sender, receiver) {
    this.sender = sender;
    this.receiver = receiver;
    this.klass.classInfo.getField("I.nativePointer.I").set(this, util.id());
});

Native.create("com/sun/midp/links/Link.receive0.(Lcom/sun/midp/links/LinkMessage;Lcom/sun/midp/links/Link;)V", function(linkMessage, link) {
    // TODO: Implement when something hits send0
    console.warn("Called com/sun/midp/links/Link.receive0.(Lcom/sun/midp/links/LinkMessage;Lcom/sun/midp/links/Link;)V");
    return new Promise(function(){});
}, true);

Native.create("com/sun/cldc/i18n/j2me/UTF_8_Reader.init.([B)V", function(data) {
    this.decoded = new TextDecoder("UTF-8").decode(data);
});

Native.create("com/sun/cldc/i18n/j2me/UTF_8_Reader.readNative.([CII)I", function(cbuf, off, len) {
    if (this.decoded.length === 0) {
      return -1;
    }

    for (var i = 0; i < len; i++) {
      cbuf[i + off] = this.decoded.charCodeAt(i);
    }

    this.decoded = this.decoded.substring(len);

    return len;
});

Native.create("java/io/DataInputStream.bytesToUTF.([B)Ljava/lang/String;", function(bytearr) {
    var array = new Uint8Array(bytearr.buffer);
    try {
        return util.decodeUtf8Array(array);
    } catch(e) {
        try {
            return util.javaUTF8Decode(array);
        } catch (e) {
            throw new JavaException("java/io/UTFDataFormatException");
        }
    }
});

Native.create("java/io/DataOutputStream.UTFToBytes.(Ljava/lang/String;)[B", function(jStr) {
    var str = util.fromJavaString(jStr);

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
        throw new JavaException("java/io/UTFDataFormatException");
    }

    var count = 0;
    var bytearr = util.newPrimitiveArray("B", utflen + 2);
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
});

Native.create("com/sun/cldc/i18n/j2me/UTF_8_Writer.encodeUTF8.([CII)[B", function(cbuf, off, len) {
  var outputArray = [];

  var pendingSurrogate = this.klass.classInfo.getField("I.pendingSurrogate.I").get(this);

  var inputChar = 0;
  var outputSize = 0;
  var count = 0;

  while (count < len) {
    var outputByte = new Uint8Array(4);     // Never more than 4 encoded bytes
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

  this.klass.classInfo.getField("I.pendingSurrogate.I").set(this, pendingSurrogate);

  var totalSize = outputArray.reduce(function(total, cur) {
    return total + cur.length;
  }, 0);

  var res = J2ME.newByteArray(totalSize);
  outputArray.reduce(function(total, cur) {
    res.set(cur, total);
    return total + cur.length;
  }, 0);

  return res;
});

Native.create("com/sun/cldc/i18n/j2me/UTF_8_Writer.sizeOf.([CII)I", function(cbuf, off, len) {
  var inputChar = 0;
  var outputSize = 0;
  var outputCount = 0;
  var count = 0;
  var localPendingSurrogate = this.klass.classInfo.getField("I.pendingSurrogate.I").get(this);
  while (count < length) {
    inputChar = 0xffff & cbuf[offset + count];
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
});

Native.create("com/sun/j2me/content/AppProxy.midletIsAdded.(ILjava/lang/String;)V", function(suiteId, className) {
  // ???
});
