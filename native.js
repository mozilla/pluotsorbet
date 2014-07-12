/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var Native = function() {
}

Native.prototype = {
    getMethod: function (className, methodName, signature) {
        console.log("Native.getMethod", className, methodName, signature);
        return this[className + "." + methodName + "." + signature];
    },
    "java/lang/System.arraycopy.(Ljava/lang/Object;ILjava/lang/Object;II)V": function (src, srcOffset, dst, dstOffset, length) {
        console.log(src, srcOffset, dst, dstOffset, length);
        var srcProto = Object.getPrototypeOf(src);
        var dstProto = Object.getPrototypeOf(dst);
        if (srcProto === dstProto) {
        }
    },
}

