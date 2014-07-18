/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var Native = function() {
}

Native.prototype.JavaException = function(className, msg) {
    this.className = className;
    this.msg = msg;
}

Native.prototype.invokeNative = function(caller, methodInfo) {
    function pushType(type, value) {
        if (type === "long" || type === "double") {
            caller.stack.push2(value);
            return;
        }
        caller.stack.push(value);
    }

    function popType(type) {
        return (type === "long" || type === "double") ? caller.stack.pop2() : caller.stack.pop();
    }

    function popArgs(types) {
        var argc = types.length;
        if (!ACCESS_FLAGS.isStatic(methodInfo.access_flags))
            ++argc;
        var args = Array(argc);
        for (var i=types.length-1, j=args.length-1; i >= 0; --i, --j)
            args[j] = popType(types[i].type);
        if (j >= 0)
            args[0] = caller.stack.pop();
        return args;
    }

    var signature = Signature.parse(methodInfo.signature);
    var args = popArgs(signature.IN);
    if (!methodInfo.native)
        methodInfo.native = this.getNativeMethod(methodInfo);
    var result = methodInfo.native.apply(caller, args);
    if (signature.OUT.length)
        pushType(signature.OUT[0], result);
}

Native.prototype.getNativeMethod = function (methodInfo) {
    var classInfo = methodInfo.classInfo;
    var className = classInfo.className;
    var methodName = methodInfo.name;
    var signature = methodInfo.signature;
    return this[className + "." + methodName + "." + signature];
}

Native.prototype["java/lang/System.arraycopy.(Ljava/lang/Object;ILjava/lang/Object;II)V"] = function (src, srcOffset, dst, dstOffset, length) {
    if (!src || !dst) {
        throw new JavaException("java/lang/NullPointerException", "Cannot copy to/from a null array.");
        return;
    }
    var proto = Object.getPrototypeOf(src);
    if (proto !== Int8Array.prototype && proto !== Int16Array.prototype && proto !== Int32Array.prototype &&
        proto !== Uint16Array.prototype && proto !== Float32Array.prototype && proto !== Float64Array.prototype &&
        proto !== Array.prototype) {
        throw new JavaException("java/lang/ArrayStoreException", "Can only copy to/from array types.");
        return;
    }
    if (proto !== Object.getPrototypeOf(dst)) {
        throw new JavaException("java/lang/ArrayStoreException", "Incompatible component types.");
        return;
    }
    if (srcOffset < 0 || (srcOffset+length) > src.length || dstOffset < 0 || (dstOffset+length) > dst.length || length < 0) {
        throw new JavaException("java/lang/ArrayIndexOutOfBoundsException", "Invalid index.");
        return;
    }
    if (proto === Array.prototype) {
        // TODO: check casting
        throw new JavaException("java/lang/ArrayStoreException", "Invalid element type.");
        return;
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

Native.prototype["java/lang/System.getProperty0.(Ljava/lang/String;)Ljava/lang/String;"] = function (key) {
    switch (util.chars2string(key.value)) {
    case "microedition.encoding":
        return CLASSES.newString("ISO-8859-1");
    default:
        console.log("KEY: " + util.chars2string(key.value));
    }
}

Native.prototype["com/sun/cldchi/jvm/JVM.unchecked_char_arraycopy.([CI[CII)V"] = function (src, srcOffset, dst, dstOffset, length) {
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

Native.prototype["java/lang/Class.forName.(Ljava/lang/String;)Ljava/lang/Class;"] = function (name) {
    var className = util.chars2string(name.value, name.offset, name.count).replace(".", "/", "g");
    var classInfo = CLASSES.getClass(className);
    if (!classInfo) {
        throw new Native.JavaException("java/lang/ClassNotFoundException", "'" + className + "' not found.");
    }
    var classObject = CLASSES.newObject("java/lang/Class");
    classObject.vmClass = classInfo;
    return classObject;
}

Native.prototype["java/lang/Class.newInstance.()Ljava/lang/Object;"] = function (classObject) {
    var classInfo = classObject.vmClass;
    CLASSES.initClass(classInfo);
    return new (classInfo.constructor)();
};

Native.prototype["com/sun/cldchi/io/ConsoleOutputStream.write.(I)V"] = (function () {
    var s = "";
    return function (obj, ch) {
        if (ch === 10) {
            document.getElementById("output").textContent += s + "\n";
            s = "";
            return;
        }
        s += String.fromCharCode(ch);
    }
})();
