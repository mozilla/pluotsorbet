/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'Use strict';

var Native = {};

Native.invoke = function(ctx, methodInfo) {
    if (!methodInfo.native) {
        var key = methodInfo.classInfo.className + "." + methodInfo.name + "." + methodInfo.signature;
        methodInfo.native = Native[key];
        if (!methodInfo.native)
            console.log(key);
    }
    methodInfo.native.call(null, ctx, ctx.current().stack);
}

Native["java/lang/System.arraycopy.(Ljava/lang/Object;ILjava/lang/Object;II)V"] = function(ctx, stack) {
    var length = stack.pop(), dstOffset = stack.pop(), dst = stack.pop(), srcOffset = stack.pop(), src = stack.pop();
    if (!src || !dst)
        ctx.raiseException("java/lang/NullPointerException", "Cannot copy to/from a null array.");
    var srcClass = src.class;
    var dstClass = dst.class;
    if (!srcClass.isArrayClass || !dstClass.isArrayClass)
        ctx.raiseException("java/lang/ArrayStoreException", "Can only copy to/from array types.");
    if (srcOffset < 0 || (srcOffset+length) > src.length || dstOffset < 0 || (dstOffset+length) > dst.length || length < 0)
        ctx.raiseException("java/lang/ArrayIndexOutOfBoundsException", "Invalid index.");
    if ((!!srcClass.elementClass != !!dstClass.elementClass) ||
        (!srcClass.elementClass && srcClass != dstClass)) {
        ctx.raiseException("java/lang/ArrayStoreException", "Incompatible component types.");
    }
    if (dstClass.elementClass) {
        if (srcClass != dstClass && !srcClass.elementClass.isAssignableTo(dstClass.elementClass)) {
            function copy(to, from) {
                var obj = src[from];
                if (obj && !obj.class.isAssignableTo(dstClass.elementClass))
                    ctx.raiseException("java/lang/ArrayStoreException", "Incompatible component types.");
                dst[to] = obj;
            }
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
}

Native["java/lang/System.getProperty0.(Ljava/lang/String;)Ljava/lang/String;"] = function(ctx, stack) {
    var key = stack.pop();
    var value;
    switch (util.fromJavaString(key)) {
    case "microedition.encoding":
        value = "UTF-8";
        break;
    case "microedition.locale":
        value = navigator.language;
        break;
    case "microedition.platform":
        value = "NOKIA503/JAVA_RUNTIME_VERSION=NOKIA_ASHA_1_2";
        break;
    case "fileconn.dir.memorycard":
        value = "fcfile:///tmp/";
        break;
    case "fileconn.dir.private":
        value = "fcfile:///tmp/";
    break;
    default:
        console.log("UNKNOWN PROPERTY (java/lang/System): " + util.fromJavaString(key));
        value = null;
        break;
    }
    stack.push(value ? CLASSES.newString(value) : null);
}

Native["java/lang/System.currentTimeMillis.()J"] = function(ctx, stack) {
    stack.push2(Long.fromNumber(Date.now()));
}

Native["com/sun/cldchi/jvm/JVM.unchecked_char_arraycopy.([CI[CII)V"] =
Native["com/sun/cldchi/jvm/JVM.unchecked_int_arraycopy.([II[III)V"] =
Native["com/sun/cldchi/jvm/JVM.unchecked_obj_arraycopy.([Ljava/lang/Object;I[Ljava/lang/Object;II)V"] = function(ctx, stack) {
    var length = stack.pop(), dstOffset = stack.pop(), dst = stack.pop(), srcOffset = stack.pop(), src = stack.pop();
    if (dst !== src || dstOffset < srcOffset) {
        for (var n = 0; n < length; ++n)
            dst[dstOffset++] = src[srcOffset++];
    } else {
        dstOffset += length;
        srcOffset += length;
        for (var n = 0; n < length; ++n)
            dst[--dstOffset] = src[--srcOffset];
    }
}

Native["java/lang/Object.getClass.()Ljava/lang/Class;"] = function(ctx, stack) {
    stack.push(stack.pop().class.getClassObject());
}

Native["java/lang/Object.hashCode.()I"] = function(ctx, stack) {
    var obj = stack.pop();
    var hashCode = obj.hashCode;
    while (!hashCode)
        hashCode = obj.hashCode = util.id();
    stack.push(hashCode);
}

Native["java/lang/Object.wait.(J)V"] = function(ctx, stack) {
    var timeout = stack.pop2(), obj = stack.pop();
    ctx.wait(obj, timeout.toNumber());
}

Native["java/lang/Object.notify.()V"] = function(ctx, stack) {
    var obj = stack.pop();
    ctx.notify(obj);
}

Native["java/lang/Object.notifyAll.()V"] = function(ctx, stack) {
    var obj = stack.pop();
    ctx.notify(obj, true);
}

Native["java/lang/Class.invoke_clinit.()V"] = function(ctx, stack) {
    var classObject = stack.pop();
    var classInfo = classObject.vmClass;
    if (classInfo.initialized || classInfo.pending)
        return;
    classInfo.pending = true;
    var clinit = CLASSES.getMethod(classInfo, "<clinit>", "()V", true);
    if (clinit)
        ctx.pushFrame(clinit, 0);
    if (classInfo.superClass)
        ctx.pushClassInitFrame(classInfo.superClass);
    throw VM.Yield;
}

Native["java/lang/Class.init9.()V"] = function(ctx, stack) {
    var classObject = stack.pop();
    var classInfo = classObject.vmClass;
    if (classInfo.initialized)
        return;
    classInfo.pending = false;
    classInfo.initialized = true;
}

Native["java/lang/Class.getName.()Ljava/lang/String;"] = function(ctx, stack) {
    var classObject = stack.pop();
    stack.push(util.cache(classObject, "getName", function () {
        return CLASSES.newString(classObject.vmClass.className.replace("/", ".", "g"));
    }));
}

Native["java/lang/Class.forName.(Ljava/lang/String;)Ljava/lang/Class;"] = function(ctx, stack) {
    var name = stack.pop();
    try {
        if (!name)
            throw new Classes.ClassNotFoundException();
        var className = util.fromJavaString(name).replace(".", "/", "g");
        var classInfo = null;
        classInfo = CLASSES.getClass(className);
    } catch (e) {
        if (e instanceof (Classes.ClassNotFoundException))
            ctx.raiseException("java/lang/ClassNotFoundException", "'" + className + "' not found.");
        throw e;
    }
    stack.push(classInfo.getClassObject());
}

Native["java/lang/Class.newInstance.()Ljava/lang/Object;"] = function(ctx, stack) {
    var classObject = stack.pop();
    var className = classObject.vmClass.className;
    var syntheticMethod = {
      classInfo: {
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
      code: [
        0xbb, 0x00, 0x01, // new <idx=1>
        0x59,             // dup
        0xb7, 0x00, 0x03, // invokespecial <idx=3>
        0xb0              // areturn
      ],
    };
    ctx.pushFrame(syntheticMethod, 0);
    throw VM.Yield;
};

Native["java/lang/Class.isInterface.()Z"] = function(ctx, stack) {
    var classObject = stack.pop();
    var classInfo = classObject.vmClass;
    stack.push(ACCESS_FLAGS.isInterface(classInfo.access_flags) ? 1 : 0);
}

Native["java/lang/Class.isArray.()Z"] = function(ctx, stack) {
    var classObject = stack.pop();
    var classInfo = classObject.vmClass;
    stack.push(classInfo.isArrayClass ? 1 : 0);
}

Native["java/lang/Class.isAssignableFrom.(Ljava/lang/Class;)Z"] = function(ctx, stack) {
    var fromClass = stack.pop(), classObject = stack.pop();
    if (!fromClass)
        ctx.raiseException("java/lang/NullPointerException");
    stack.push(fromClass.vmClass.isAssignableTo(classObject.vmClass) ? 1 : 0);
}

Native["java/lang/Class.isInstance.(Ljava/lang/Object;)Z"] = function(ctx, stack) {
    var obj = stack.pop(), classObject = stack.pop();
    stack.push((obj && obj.class.isAssignableTo(classObject.vmClass)) ? 1 : 0);
}

Native["java/lang/Float.floatToIntBits.(F)I"] = (function() {
    var fa = new Float32Array(1);
    var ia = new Int32Array(fa.buffer);
    return function(ctx, stack) {
        fa[0] = stack.pop();
        stack.push(ia[0]);
    }
})();

Native["java/lang/Double.doubleToLongBits.(D)J"] = (function() {
    var da = new Float64Array(1);
    var ia = new Int32Array(da.buffer);
    return function(ctx, stack) {
        da[0] = stack.pop2();
        stack.push2(Long.fromBits(ia[0], ia[1]));
    }
})();

Native["java/lang/Float.intBitsToFloat.(I)F"] = (function() {
    var fa = new Float32Array(1);
    var ia = new Int32Array(fa.buffer);
    return function(ctx, stack) {
        ia[0] = stack.pop();
        stack.push(fa[0]);
    }
})();

Native["java/lang/Double.longBitsToDouble.(J)D"] = (function() {
    var da = new Float64Array(1);
    var ia = new Int32Array(da.buffer);
    return function(ctx, stack) {
        var l = stack.pop2();
        ia[0] = l.low_;
        ia[1] = l.high_;
        stack.push2(da[0]);
    }
})();

Native["java/lang/Throwable.fillInStackTrace.()V"] = (function(ctx, stack) {
    var throwable = stack.pop();
    throwable.stackTrace = [];
    ctx.frames.forEach(function(frame) {
        if (!frame.methodInfo)
            return;
        var methodInfo = frame.methodInfo;
        var methodName = methodInfo.name;
        if (!methodName)
            return;
        var classInfo = methodInfo.classInfo;
        var className = classInfo.className;
        throwable.stackTrace.unshift({ className: className, methodName: methodName, offset: frame.ip });
    });
});

Native["java/lang/Throwable.obtainBackTrace.()Ljava/lang/Object;"] = (function(ctx, stack) {
    var obj = stack.pop();
    var result = null;
    if (obj.stackTrace) {
        var depth = obj.stackTrace.length;
        var classNames = CLASSES.newArray("[Ljava/lang/Object;", depth);
        var methodNames = CLASSES.newArray("[Ljava/lang/Object;", depth);
        var offsets = CLASSES.newPrimitiveArray("I", depth);
        obj.stackTrace.forEach(function(e, n) {
            classNames[n] = CLASSES.newString(e.className);
            methodNames[n] = CLASSES.newString(e.methodName);
            offsets[n] = e.offset;
        });
        result = CLASSES.newArray("[Ljava/lang/Object;", 3);
        result[0] = classNames;
        result[1] = methodNames;
        result[2] = offsets;
    }
    stack.push(result);
});

Native["java/lang/Runtime.freeMemory.()J"] = function(ctx, stack) {
    var runtime = stack.pop();
    stack.push2(Long.fromInt(0x800000));
}

Native["java/lang/Runtime.totalMemory.()J"] = function(ctx, stack) {
    var runtime = stack.pop();
    stack.push2(Long.fromInt(0x1000000));
}

Native["java/lang/Runtime.gc.()V"] = function(ctx, stack) {
    var runtime = stack.pop();
}

Native["java/lang/Math.floor.(D)D"] = function(ctx, stack) {
    stack.push2(Math.floor(stack.pop2()));
}

Native["java/lang/Math.asin.(D)D"] = function(ctx, stack) {
    stack.push2(Math.asin(stack.pop2()));
}

Native["java/lang/Math.acos.(D)D"] = function(ctx, stack) {
    stack.push2(Math.acos(stack.pop2()));
}

Native["java/lang/Math.atan.(D)D"] = function(ctx, stack) {
    stack.push2(Math.atan(stack.pop2()));
}

Native["java/lang/Math.atan2.(DD)D"] = function(ctx, stack) {
    var y = stack.pop2(), x = stack.pop2();
    stack.push2(Math.atan2(x, y));
}

Native["java/lang/Math.sin.(D)D"] = function(ctx, stack) {
    stack.push2(Math.sin(stack.pop2()));
}

Native["java/lang/Math.cos.(D)D"] = function(ctx, stack) {
    stack.push2(Math.cos(stack.pop2()));
}

Native["java/lang/Math.tan.(D)D"] = function(ctx, stack) {
    stack.push2(Math.tan(stack.pop2()));
}

Native["java/lang/Math.sqrt.(D)D"] = function(ctx, stack) {
    stack.push2(Math.sqrt(stack.pop2()));
}

Native["java/lang/Math.ceil.(D)D"] = function(ctx, stack) {
    stack.push2(Math.ceil(stack.pop2()));
}

Native["java/lang/Math.floor.(D)D"] = function(ctx, stack) {
    stack.push2(Math.floor(stack.pop2()));
}

Native["java/lang/Thread.currentThread.()Ljava/lang/Thread;"] = function(ctx, stack) {
    stack.push(ctx.thread);
}

Native["java/lang/Thread.setPriority0.(II)V"] = function(ctx, stack) {
    var newPriority = stack.pop(), oldPriority = stack.pop(), thread = stack.pop();
}

Native["java/lang/Thread.start0.()V"] = function(ctx, stack) {
    var thread = stack.pop();
    // The main thread starts during bootstrap and don't allow calling start()
    // on already running threads.
    if (thread === CLASSES.mainThread || thread.Thread$running)
        ctx.raiseException("java/lang/IllegalThreadStateException");
    thread.alive = true;
    thread.pid = util.id();
    var run = CLASSES.getMethod(thread.class, "run", "()V", false, true);
    // Create a context for the thread and start it.
    var ctx = new Context();
    ctx.thread = thread;
    var caller = new Frame();
    ctx.frames.push(caller);
    caller.stack.push(thread);
    var syntheticMethod = {
      classInfo: {
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
      code: [
        0x2a,             // aload_0
        0x59,             // dup
        0xb6, 0x00, 0x01, // invokespecial <idx=1>
        0xb7, 0x00, 0x07, // invokespecial <idx=7>
        0xb1,             // return
      ],
      exception_table: [],
    };
    ctx.pushFrame(syntheticMethod, 1);
    ctx.start(caller);
}

Native["java/lang/Thread.internalExit.()V"] = function(ctx, stack) {
    stack.pop().alive = false;
}

Native["java/lang/Thread.isAlive.()Z"] = function(ctx, stack) {
    stack.push(stack.pop().alive ? 1 : 0);
}

Native["java/lang/Thread.sleep.(J)V"] = function(ctx, stack) {
    var delay = stack.pop2().toNumber();
    window.setTimeout(function() {
        ctx.resume();
    }, delay);
    throw VM.Pause;
}

Native["java/lang/Thread.yield.()V"] = function(ctx, stack) {
    throw VM.Yield;
}

Native["com/sun/cldchi/io/ConsoleOutputStream.write.(I)V"] = (function() {
    var s = "";
    return function(ctx, stack) {
        var ch = stack.pop(), obj = stack.pop();
        if (ch === 10) {
            document.getElementById("output").textContent += s + "\n";
            s = "";
            return;
        }
        s += String.fromCharCode(ch);
    };
})();

Native["com/sun/cldc/io/ResourceInputStream.open.(Ljava/lang/String;)Ljava/lang/Object;"] = function(ctx, stack) {
    var name = stack.pop();
    var fileName = util.fromJavaString(name);
    var data = CLASSES.loadFile(fileName);
    var obj = null;
    if (data) {
        obj = CLASSES.newObject(CLASSES.java_lang_Object);
        obj.data = Uint8Array(data);
        obj.pos = 0;
    }
    stack.push(obj);
};

Native["com/sun/cldc/io/ResourceInputStream.bytesRemain.(Ljava/lang/Object;)I"] = function(ctx, stack) {
    var handle = stack.pop();
    stack.push(handle.data.length - handle.pos);
}

Native["com/sun/cldc/io/ResourceInputStream.readByte.(Ljava/lang/Object;)I"] = function(ctx, stack) {
    var handle = stack.pop();
    stack.push(handle.data[handle.pos++]);
}

Native["com/sun/cldc/io/ResourceInputStream.readBytes.(Ljava/lang/Object;[BII)I"] = function(ctx, stack) {
    var len = stack.pop(), off = stack.pop(), b = stack.pop(), handle = stack.pop();
    var data = handle.data;
    var remaining = data.length - handle.pos;
    if (remaining > len)
        len = remaining;
    for (var n = 0; n < len; ++n)
        b[off+n] = data[n];
    handle.pos += len;
    stack.push(len);
}

Native["com/sun/cldc/i18n/uclc/DefaultCaseConverter.toLowerCase.(C)C"] = function(ctx, stack) {
    stack.push(String.fromCharCode(stack.pop()).toLowerCase().charCodeAt(0));
}

Native["com/sun/cldc/i18n/uclc/DefaultCaseConverter.toUpperCase.(C)C"] = function(ctx, stack) {
    stack.push(String.fromCharCode(stack.pop()).toUpperCase().charCodeAt(0));
}

Native["com/sun/midp/main/MIDletProxyList.resetForegroundInNativeState.()V"] = function(ctx, stack) {
    var _this = stack.pop();
}

Native["com/sun/midp/main/MIDletProxyList.setForegroundInNativeState.(II)V"] = function(ctx, stack) {
    var displayId = stack.pop(), isolateId = stack.pop(), _this = stack.pop();
}

Native["com/sun/midp/io/j2me/push/ConnectionRegistry.poll0.(J)I"] = function(ctx, stack) {
    var time = stack.pop(), _this = stack.pop();
    // Wait for incoming connections
    throw VM.Pause;
}
