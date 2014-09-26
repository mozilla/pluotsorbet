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

Override["java/io/ByteArrayInputStream.<init>.([B)V"] = function(ctx, stack) {
  var buf = stack.pop(), _this = stack.pop();

  if (!buf) {
    ctx.raiseExceptionAndYield("java/lang/NullPointerException");
  }

  _this.buf = buf;
  _this.pos = _this.mark = 0;
  _this.count = buf.length;
}

Override["java/io/ByteArrayInputStream.<init>.([BII)V"] = function(ctx, stack) {
  var length = stack.pop(), offset = stack.pop(), buf = stack.pop(), _this = stack.pop();

  if (!buf) {
    ctx.raiseExceptionAndYield("java/lang/NullPointerException");
  }

  _this.buf = buf;
  _this.pos = _this.mark = offset;
  _this.count = (offset + length <= buf.length) ? (offset + length) : buf.length;
}

Override["java/io/ByteArrayInputStream.read.()I"] = function(ctx, stack) {
  var _this = stack.pop();
  stack.push((_this.pos < _this.count) ? (_this.buf[_this.pos++] & 0xFF) : -1);
}

Override["java/io/ByteArrayInputStream.read.([BII)I"] = function(ctx, stack) {
  var len = stack.pop(), off = stack.pop(), b = stack.pop(), _this = stack.pop();

  if (!b) {
    ctx.raiseExceptionAndYield("java/lang/NullPointerException");
  } else if ((off < 0) || (off > b.length) || (len < 0) ||
             ((off + len) > b.length) || ((off + len) < 0)) {
    ctx.raiseExceptionAndYield("java/lang/IndexOutOfBoundsException");
  }

  if (_this.pos >= _this.count) {
    stack.push(-1);
    return;
  }
  if (_this.pos + len > _this.count) {
    len = _this.count - _this.pos;
  }
  if (len <= 0) {
    stack.push(0);
    return;
  }

  b.set(_this.buf.subarray(_this.pos, _this.pos + len), off);

  _this.pos += len;
  stack.push(len);
}

Override["java/io/ByteArrayInputStream.skip.(J)J"] = function(ctx, stack) {
  var nLong = stack.pop2(), _this = stack.pop();

  var n = nLong.toNumber();

  if (_this.pos + n > _this.count) {
      n = _this.count - _this.pos;
  }

  if (n < 0) {
      stack.push2(Long.fromNumber(0));
      return;
  }

  _this.pos += n;

  stack.push2(nLong);
}

Override["java/io/ByteArrayInputStream.available.()I"] = function(ctx, stack) {
  var _this = stack.pop();
  stack.push(_this.count - _this.pos);
}

Override["java/io/ByteArrayInputStream.mark.(I)V"] = function(ctx, stack) {
  var readAheadLimit = stack.pop(), _this = stack.pop();
  _this.mark = _this.pos;
}

Override["java/io/ByteArrayInputStream.reset.()V"] = function(ctx, stack) {
  var _this = stack.pop();
  _this.pos = _this.mark;
}
