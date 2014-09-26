/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

var Override = {};

Override.getKey = function(methodInfo) {
  return methodInfo.classInfo.className + "." + methodInfo.name + "." + methodInfo.signature;
}

Override.hasMethod = function(methodInfo) {
  return ("override" in methodInfo || Override.getKey(methodInfo) in Override);
}

Override.invoke = function(ctx, methodInfo) {
  if (!methodInfo.override) {
    var key = Override.getKey(methodInfo);
    methodInfo.override = Override[key];
    if (!methodInfo.override) {
      console.error("Missing override: " + key);
      ctx.raiseExceptionAndYield("java/lang/RuntimeException", key + " not found");
    }
  }
  methodInfo.override.call(null, ctx, ctx.current().stack);
}

Override["com/ibm/oti/connection/file/Connection.decode.(Ljava/lang/String;)Ljava/lang/String;"] = function(ctx, stack) {
  var string = util.fromJavaString(stack.pop());
  stack.push(ctx.newString(decodeURIComponent(string)));
}

Override["com/ibm/oti/connection/file/Connection.encode.(Ljava/lang/String;)Ljava/lang/String;"] = function(ctx, stack) {
  var string = util.fromJavaString(stack.pop());
  stack.push(ctx.newString(string.replace(/[^a-zA-Z0-9-_\.!~\*\\'()/:]/g, encodeURIComponent)));
}

Override["java/lang/Math.min.(II)I"] = function(ctx, stack) {
  var b = stack.pop(), a = stack.pop();
  stack.push(a <= b ? a : b);
}

Override["java/io/ByteArrayInputStream.read.()I"] = function(ctx, stack) {
  var _this = stack.pop();

  var pos = _this.class.getField("pos", "I").get(_this);
  var count = _this.class.getField("count", "I").get(_this);
  var buf = _this.class.getField("buf", "[B").get(_this);

  if (pos < count) {
      var value = buf[pos++] & 0xFF;
      _this.class.getField("pos", "I").set(_this, pos);
      stack.push(value);
  } else {
      stack.push(-1);
  }
}
