/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var Native = function() {
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
        var args = Array(types.length);
        for (var i=types.length-1, j=args.length-1; i >= 0; --i, --j)
            args[j] = popType(types[i].type);
        if (j >= 0)
            args[0] = caller.stack.pop();
        return args;
    }

    var meta = methodInfo.meta();
    var args = popArgs(meta.IN);
    if (!methodInfo.native)
        methodInfo.native = this.getNativeMethod(methodInfo);
    var result = methodInfo.native.apply(caller, args);
    if (meta.OUT.length)
        pushType(meta.OUT[0], result);
}

Native.prototype.getNativeMethod = function (methodInfo) {
    var classInfo = methodInfo.classInfo;
    var className = classInfo.className;
    var methodName = methodInfo.name;
    var signature = methodInfo.signature;
    console.log("Native.getNativeMethod", className, methodName, signature);
    return this[className + "." + methodName + "." + signature];
}

Native.prototype["java/lang/System.arraycopy.(Ljava/lang/Object;ILjava/lang/Object;II)V"] = function (src, srcOffset, dst, dstOffset, length) {
    console.log(src, srcOffset, dst, dstOffset, length);
    if (!src || !dst) {
        this.raiseException("java/lang/NullPointerException", "Cannot copy to/from a null array.");
        return;
    }
    var proto = Object.getPrototypeOf(src);
    if (proto !== Int8Array.prototype && proto !== Int16Array.prototype && proto !== Int32Array.prototype &&
        proto !== Uint16Array.prototype && proto !== Float32Array.prototype && proto !== Float64Array.prototype &&
        proto !== Array.prototype) {
        this.raiseException("java/lang/ArrayStoreException", "Can only copy to/from array types.");
        return;
    }
    if (proto !== Object.getPrototypeOf(dst)) {
        this.raiseException("java/lang/ArrayStoreException", "Incompatible component types.");
        return;
    }
    if (srcOffset < 0 || (srcOffset+length) > src.length || dstOffset < 0 || (dstOffset+length) > dst.length || length < 0) {
        this.raiseException("java/lang/ArrayIndexOutOfBoundsException", "Invalid index.");
        return;
    }
    if (proto === Array.prototype) {
        // TODO: check casting
        this.raiseException("java/lang/ArrayStoreException", "Invalid element type.");
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
