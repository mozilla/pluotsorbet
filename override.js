/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

var Override = {};

Override.invoke = function(ctx, methodInfo) {
    if (!methodInfo.override) {
        var key = methodInfo.classInfo.className + "." + methodInfo.name + "." + methodInfo.signature;
        methodInfo.override = Override[key];
        if (!methodInfo.override) {
            console.error("Missing override: " + key);
            ctx.raiseExceptionAndYield("java/lang/RuntimeException", key + " not found");
        }
    }
    methodInfo.override.call(null, ctx, ctx.current().stack);
}
