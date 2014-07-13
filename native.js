/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var Native = function() {
}

Native.prototype.invokeNative = function(caller, native, signature) {
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
        var args = Array(types.length);
        console.log(types.length-1);
        for (var i=types.length-1; i >= 0; --i)
            args[i] = popType(types[i].type);
        console.log("popArgs", types, args);
        return args;
    }

    signature = Signature.parse(signature);
    var args = popArgs(signature.IN);
    var instance = null;
    if (!ACCESS_FLAGS.isStatic(method.access_flags))
        instance = this.stack.pop();
    result = native.apply(instance, args);
    if (signature.OUT.length)
        pushType(signature.OUT[0], result);
}

Native.prototype.getMethod = function (className, methodName, signature) {
    console.log("Native.getMethod", className, methodName, signature);
    return this[className + "." + methodName + "." + signature];
}

Native.prototype["java/lang/System.arraycopy.(Ljava/lang/Object;ILjava/lang/Object;II)V"] = function (src, srcOffset, dst, dstOffset, length) {
    console.log(src, srcOffset, dst, dstOffset, length);
    var srcProto = Object.getPrototypeOf(src);
    var dstProto = Object.getPrototypeOf(dst);
    if (srcProto === dstProto) {
    }
}
