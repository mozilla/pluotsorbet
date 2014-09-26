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

Override["java/io/ByteArrayOutputStream.write.([BII)V"] = function(ctx, stack) {
  var len = stack.pop(), off = stack.pop(), b = stack.pop(), _this = stack.pop();

  if ((off < 0) || (off > b.length) || (len < 0) ||
      ((off + len) > b.length) || ((off + len) < 0)) {
      ctx.raiseExceptionAndYield("java/lang/IndexOutOfBoundsException");
  } else if (len == 0) {
      return;
  }

  var count = _this.class.getField("count", "I").get(_this);
  var buf = _this.class.getField("buf", "[B").get(_this);

  var newcount = count + len;
  if (newcount > buf.length) {
    var newbuf = ctx.newPrimitiveArray("B", Math.max(buf.length << 1, newcount));
    newbuf.set(buf);
    buf = newbuf;
    _this.class.getField("buf", "[B").set(_this, buf);
  }

  buf.set(b.subarray(off, off + len), count);
  _this.class.getField("count", "I").set(_this, newcount);
}
