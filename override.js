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
      ((off + len) > b.length)) {
      ctx.raiseExceptionAndYield("java/lang/IndexOutOfBoundsException");
  }

  if (len == 0) {
      return;
  }

  var count = _this.class.getField("I.count.I").get(_this);
  var buf = _this.class.getField("I.buf.[B").get(_this);

  var newcount = count + len;
  if (newcount > buf.length) {
    var newbuf = ctx.newPrimitiveArray("B", Math.max(buf.length << 1, newcount));
    newbuf.set(buf);
    buf = newbuf;
    _this.class.getField("I.buf.[B").set(_this, buf);
  }

  buf.set(b.subarray(off, off + len), count);
  _this.class.getField("I.count.I").set(_this, newcount);
}

Override["java/io/ByteArrayOutputStream.write.(I)V"] = function(ctx, stack) {
  var value = stack.pop(), _this = stack.pop();

  var count = _this.class.getField("I.count.I").get(_this);
  var buf = _this.class.getField("I.buf.[B").get(_this);

  var newcount = count + 1;
  if (newcount > buf.length) {
    var newbuf = ctx.newPrimitiveArray("B", Math.max(buf.length << 1, newcount));
    newbuf.set(buf);
    buf = newbuf;
    _this.class.getField("I.buf.[B").set(_this, buf);
  }

  buf[count] = value;
  _this.class.getField("I.count.I").set(_this, newcount);
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
  }

  if ((off < 0) || (off > b.length) || (len < 0) ||
      ((off + len) > b.length)) {
    ctx.raiseExceptionAndYield("java/lang/IndexOutOfBoundsException");
  }

  if (_this.pos >= _this.count) {
    stack.push(-1);
    return;
  }
  if (_this.pos + len > _this.count) {
    len = _this.count - _this.pos;
  }
  if (len === 0) {
    stack.push(0);
    return;
  }

  b.set(_this.buf.subarray(_this.pos, _this.pos + len), off);

  _this.pos += len;
  stack.push(len);
}

Override["java/io/ByteArrayInputStream.skip.(J)J"] = function(ctx, stack) {
  var n = stack.pop2().toNumber(), _this = stack.pop();

  if (_this.pos + n > _this.count) {
      n = _this.count - _this.pos;
  }

  if (n < 0) {
      stack.push2(Long.fromNumber(0));
      return;
  }

  _this.pos += n;

  stack.push2(Long.fromNumber(n));
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

function JavaException(className, message) {
  this.javaClassName = className;
  this.message = message;
}
JavaException.prototype = Object.create(Error.prototype);

/**
 * A simple wrapper for overriding JVM functions to avoid logic errors
 * and simplify implementation:
 *
 * - Arguments are pushed off the stack based upon the number of
 *   arguments listed on `fn`.
 *
 * - The return value is automatically pushed back onto the stack, if
 *   the method signature does not return void. CAUTION: If you want to
 *   return a Long or Double, this code needs to be modified
 *   accordingly to do a `push2`. (Ideally, we'd just scrape the
 *   method signature and always do the right thing.)
 *
 * - The object reference ("this") is automatically bound to `fn`,
 *   unless you specify { static: true } in opts.
 *
 * - JavaException instances are caught and propagated as Java
     exceptions; JS TypeError propagates as a NullPointerException.
 *
 * Simple overrides don't currently have access to `ctx` or `stack`.
 *
 * @param {string} key
 *   The fully-qualified JVM method signature.
 * @param {function(args)} fn
 *   A function taking any number of args. The number of arguments
 *   this function takes is the number of args popped off of the stack.
 * @param {object} opts
 *   { static: true } if the method is static (and should not receive
 *   and pop the `this` argument off the stack).
 */
Override.simple = function(key, fn, opts) {
  var isStatic = opts && opts.static;
  var isVoid = key[key.length - 1] === 'V';
  var numArgs = fn.length;
  Override[key] = function(ctx, stack) {
    var args = new Array(numArgs);
    // NOTE: If your function accepts a Long/Double, you must specify
    // two arguments (since they take up two stack positions); we
    // could sugar this someday.
    for (var i = numArgs - 1; i >= 0; i--) {
      args[i] = stack.pop();
    }
    try {
      var self = isStatic ? null : stack.pop();
      var ret = fn.apply(self, args);
      if (!isVoid) {
        if (ret === true) {
          stack.push(1);
        } else if (ret === false) {
          stack.push(0);
        } else if (typeof ret === "string") {
          stack.push(ctx.newString(ret));
        } else {
          stack.push(ret);
        }
      }
    } catch(e) {
      if (e.name === "TypeError") {
        // JavaScript's TypeError is analogous to a NullPointerException.
        ctx.raiseExceptionAndYield("java/lang/NullPointerException", e);
      } else if (e.javaClassName) {
        ctx.raiseExceptionAndYield(e.javaClassName, e.message);
      } else {
        ctx.raiseExceptionAndYield("java/lang/RuntimeError", e);
      }
    }
  };
}

Override["com/sun/midp/security/Permissions.forDomain.(Ljava/lang/String;)[[B"] = function(ctx, stack) {
  var name = stack.pop();

  // NUMBER_OF_PERMISSIONS = PermissionsStrings.PERMISSION_STRINGS.length + 2
  // The 2 is the two hardcoded MIPS and AMS permissions.
  var NUMBER_OF_PERMISSIONS = 61;
  var ALLOW = 1;

  var maximums = ctx.newPrimitiveArray("B", NUMBER_OF_PERMISSIONS);
  var defaults = ctx.newPrimitiveArray("B", NUMBER_OF_PERMISSIONS);

  for (var i = 0; i < NUMBER_OF_PERMISSIONS; i++) {
    maximums[i] = defaults[i] = ALLOW;
  }

  var permissions = ctx.newArray("[[B", 2);
  permissions[0] = maximums;
  permissions[0] = defaults;

  stack.push(permissions);
}

Override["com/sun/midp/security/Permissions.isTrusted.(Ljava/lang/String;)Z"] = function(ctx, stack) {
  var name = stack.pop();
  stack.push(1);
}

Override["com/sun/midp/security/Permissions.getId.(Ljava/lang/String;)I"] = function(ctx, stack) {
  var name = stack.pop();
  stack.push(0);
}

Override["com/sun/midp/security/Permissions.getName.(I)Ljava/lang/String;"] = function(ctx, stack) {
  var id = stack.pop();
  stack.push("com.sun.midp");
}
