/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'Use strict';

var Native = {};

Native.invoke = function(ctx, methodInfo) {
    if (!methodInfo.native) {
        var key = methodInfo.classInfo.className + "." + methodInfo.name + "." + methodInfo.signature;
        methodInfo.native = Native[key];
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
    if (srcClass != dstClass && (!srcClass.elementClass || !dstClass.elementClass || !srcClass.elementClass.isAssignableTo(dstClass.elementClass)))
        ctx.raiseException("java/lang/ArrayStoreException", "Incompatible component types.");
    if (srcOffset < 0 || (srcOffset+length) > src.length || dstOffset < 0 || (dstOffset+length) > dst.length || length < 0)
        ctx.raiseException("java/lang/ArrayIndexOutOfBoundsException", "Invalid index.");
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
        value = ctx.newString("UTF-8");
        break;
    default:
        console.log("UNKNOWN PROPERTY: " + util.fromJavaString(key));
        value = null;
        break;
    }
    stack.push(value);
}

Native["java/lang/System.currentTimeMillis.()J"] = function(ctx, stack) {
    stack.push2(Long.fromNumber(Date.now()));
}

Native["com/sun/cldchi/jvm/JVM.unchecked_char_arraycopy.([CI[CII)V"] = function(ctx, stack) {
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

Native["java/lang/Class.getName.()Ljava/lang/String;"] = function(ctx, stack) {
    var obj = stack.pop();
    stack.push(util.cache(obj, "getName", function () {
        return ctx.newString(obj.vmClass.className.replace("/", ".", "g"));
    }));
}

Native["java/lang/Class.forName.(Ljava/lang/String;)Ljava/lang/Class;"] = function(ctx, stack) {
    var name = stack.pop();
    var className = util.fromJavaString(name).replace(".", "/", "g");
    var classInfo = (className[0] === "[") ? null : CLASSES.getClass(className);
    if (!classInfo) {
        ctx.raiseException("java/lang/ClassNotFoundException", "'" + className + "' not found.");
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
          { name_index: 2 },
          { bytes: className },
          { class_index: 1, name_and_type_index: 4 },
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

Native["java/lang/Float.floatToIntBits.(F)I"] = (function(ctx, stack) {
    var fa = Float32Array(1);
    var ia = Int32Array(fa.buffer);
    return function(ctx, stack) {
        fa[0] = stack.pop();
        stack.push(ia[0]);
    }
})();

Native["java/lang/Double.doubleToLongBits.(D)J"] = (function() {
    var da = Float64Array(1);
    var ia = Int32Array(da.buffer);
    return function(ctx, stack) {
        da[0] = stack.pop2();
        stack.push2(Long.fromBits(ia[0], ia[1]));
    }
})();

Native["java/lang/Float.intBitsToFloat.(I)F"] = (function() {
    var fa = Float32Array(1);
    var ia = Int32Array(fa.buffer);
    return function(ctx, stack) {
        ia[0] = stack.pop();
        stack.push(fa[0]);
    }
})();

Native["java/lang/Double.longBitsToDouble.(J)D"] = (function() {
    var da = Float64Array(1);
    var ia = Int32Array(da.buffer);
    return function(ctx, stack) {
        var l = stack.pop2();
        ia[0] = l.low_;
        ia[1] = l.high_;
        stack.push2(da[0]);
    }
})();

Native["java/lang/Throwable.fillInStackTrace.()V"] = (function(ctx, stack) {
});

Native["java/lang/Throwable.obtainBackTrace.()Ljava/lang/Object;"] = (function(ctx, stack) {
    stack.push(null);
});

Native["java/lang/Runtime.freeMemory.()J"] = function(ctx, stack) {
    stack.push2(Long.fromInt(0x800000));
}

Native["java/lang/Runtime.totalMemory.()J"] = function(ctx, stack) {
    stack.push2(Long.fromInt(0x1000000));
}

Native["java/lang/Runtime.gc.()V"] = function(ctx, stack) {
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
    if (thread === CLASSES.mainThread || thread.running)
        ctx.raiseException("java/lang/IllegalThreadStateException");
    thread.running = true;
    var run = CLASSES.getMethod(thread.class, "run", "()V", false, true);
    window.setZeroTimeout(function () {
        var ctx = new Context();
        ctx.thread = thread;
        var caller = new Frame();
        caller.stack.push(thread);
        ctx.frames.push(caller);
        ctx.pushFrame(run, 1);
        ctx.run(caller);
    });
}

Native["java/lang/Thread.sleep.(J)V"] = function(thread, delay) {
    var delay = stack.pop2(), thread = stack.pop();
    // FIXME
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
    }
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
