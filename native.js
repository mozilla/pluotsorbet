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

    var signature = methodInfo.signature;
    var args = popArgs(signature.IN);
    if (!methodInfo.native)
        methodInfo.native = this.getMethod(methodInfo);
    result = methodInfo.native.apply(caller, args);
    if (signature.OUT.length)
        pushType(signature.OUT[0], result);
}

Native.prototype.getMethod = function (methodInfo) {
    var classInfo = methodInfo.classInfo;
    var cp = classInfo.getConstantPool();
    var className = classInfo.getClassName();
    var methodName = cp[methodInfo.name_index].bytes;
    var signature = cp[methodInfo.signature_index].bytes;
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
