/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

var Override = {};

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
    ctx.frames.push(Frame.create(methodInfo, [exception], 0));
    ctx.execute();
  });
  $.pause("Async");
}

