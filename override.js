/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

var Override = {};

function JavaException(className, message) {
  this.javaClassName = className;
  this.message = message;
}
JavaException.prototype = Object.create(Error.prototype);

function stringReturnType(stack, ret) {
  if (typeof ret === "string") {
    stack.pushRef(util.newString(ret));
  } else {
    // already a native string or null
    stack.pushRef(ret);
  }
}

var RETURN_FUNCTIONS = {
  "B": function(stack, ret) { stack.pushInt(ret); },
  "C": function(stack, ret) { stack.pushInt(ret); },
  "S": function(stack, ret) { stack.pushInt(ret); },
  "Z": function(stack, ret) { stack.pushInt(ret ? 1 : 0); },
  "I": function(stack, ret) { stack.pushInt(ret); },
  "J": function(stack, ret) { stack.pushLong(ret); },
  "F": function(stack, ret) { stack.pushFloat(ret); },
  "D": function(stack, ret) { stack.pushDouble(ret); },
  "[": function(stack, ret) { stack.pushRef(ret); },
  "L": function(stack, ret) { stack.pushRef(ret); },
  "V": function(stack, ret) { /* nothing */ },
}

function getReturnFunction(sig) {
  var retType = sig.substring(sig.lastIndexOf(")") + 1);

  if (retType === "Ljava/lang/String;") {
    return stringReturnType;
  } else {
    return RETURN_FUNCTIONS[retType[0]];
  }
}

function executePromise(stack, ret, doReturn, ctx, key) {
  if (ret && ret.then) { // ret.constructor.name == "Promise"
    ret.then(function(res) {
      if (Instrument.profiling) {
        Instrument.exitAsyncNative(key);
      }

      doReturn(stack, res);
    }, function(e) {
      ctx.raiseException(e.javaClassName, e.message);
    }).then(ctx.start.bind(ctx));

    if (Instrument.profiling) {
      Instrument.enterAsyncNative(key);
    }

    throw VM.Pause;
  } else {
    doReturn(stack, ret);
  }
}

/**
 * A simple wrapper for overriding JVM functions to avoid logic errors
 * and simplify implementation:
 *
 * - Arguments are pushed off the stack based upon the signature of the
 *   function.
 *
 * - The return value is automatically pushed back onto the stack, if
 *   the method signature does not return void.
 *
 * - The object reference ("this") is automatically bound to `fn`.
 *
 * - JavaException instances are caught and propagated as Java
     exceptions; JS TypeError propagates as a NullPointerException.
 *
 * @param {object} object
 *   Native or Override.
 * @param {string} key
 *   The fully-qualified JVM method signature.
 * @param {function(args)} fn
 *   A function taking any number of args.
 */
function createAlternateImpl(object, key, fn, usesPromise) {
  var retType = key[key.length - 1];
  var numArgs = Signature.getINSlots(key.substring(key.lastIndexOf(".") + 1)) + 1;
  var doReturn = getReturnFunction(key);
  var postExec = usesPromise ? executePromise : doReturn;

  object[key] = function(ctx, stack, isStatic) {
    var args = new Array(numArgs);

    args[numArgs - 1] = ctx;

    // NOTE: If your function accepts a Long/Double, you must specify
    // two arguments (since they take up two stack positions); we
    // could sugar this someday.
    for (var i = numArgs - 2; i >= 0; i--) {
      args[i] = stack.popWord().value;
    }

    try {
      var self = isStatic ? null : stack.popRef();
      var ret = fn.apply(self, args);
      postExec(stack, ret, doReturn, ctx, key);
    } catch(e) {
      if (e === VM.Pause || e === VM.Yield) {
        throw e;
      } else if (e.name === "TypeError") {
        console.error(e);
        // JavaScript's TypeError is analogous to a NullPointerException.
        ctx.raiseExceptionAndYield("java/lang/NullPointerException", e);
      } else if (e.javaClassName) {
        ctx.raiseExceptionAndYield(e.javaClassName, e.message);
      } else {
        console.error(e, e.stack);
        ctx.raiseExceptionAndYield("java/lang/RuntimeException", e);
      }
    }
  };
}

Override.create = createAlternateImpl.bind(null, Override);

Override.create("com/ibm/oti/connection/file/Connection.decode.(Ljava/lang/String;)Ljava/lang/String;", function(string) {
  return decodeURIComponent(string.str);
});

Override.create("com/ibm/oti/connection/file/Connection.encode.(Ljava/lang/String;)Ljava/lang/String;", function(string) {
  return string.str.replace(/[^a-zA-Z0-9-_\.!~\*\\'()/:]/g, encodeURIComponent);
});

Override.create("java/lang/Math.min.(II)I", function(a, b) {
  return Math.min(a, b);
});

Override.create("java/io/ByteArrayOutputStream.write.([BII)V", function(b, off, len) {
  if ((off < 0) || (off > b.length) || (len < 0) ||
      ((off + len) > b.length)) {
    throw new JavaException("java/lang/IndexOutOfBoundsException");
  }

  if (len == 0) {
    return;
  }

  var count = this.class.getField("I.count.I").get(this);
  var buf = this.class.getField("I.buf.[B").get(this);

  var newcount = count + len;
  if (newcount > buf.length) {
    var newbuf = util.newPrimitiveArray("B", Math.max(buf.length << 1, newcount));
    newbuf.set(buf);
    buf = newbuf;
    this.class.getField("I.buf.[B").set(this, buf);
  }

  buf.set(b.subarray(off, off + len), count);
  this.class.getField("I.count.I").set(this, newcount);
});

Override.create("java/io/ByteArrayOutputStream.write.(I)V", function(value) {
  var count = this.class.getField("I.count.I").get(this);
  var buf = this.class.getField("I.buf.[B").get(this);

  var newcount = count + 1;
  if (newcount > buf.length) {
    var newbuf = util.newPrimitiveArray("B", Math.max(buf.length << 1, newcount));
    newbuf.set(buf);
    buf = newbuf;
    this.class.getField("I.buf.[B").set(this, buf);
  }

  buf[count] = value;
  this.class.getField("I.count.I").set(this, newcount);
});

Override.create("java/io/ByteArrayInputStream.<init>.([B)V", function(buf) {
  if (!buf) {
    throw new JavaException("java/lang/NullPointerException");
  }

  this.buf = buf;
  this.pos = this.mark = 0;
  this.count = buf.length;
});

Override.create("java/io/ByteArrayInputStream.<init>.([BII)V", function(buf, offset, length) {
  if (!buf) {
    throw new JavaException("java/lang/NullPointerException");
  }

  this.buf = buf;
  this.pos = this.mark = offset;
  this.count = (offset + length <= buf.length) ? (offset + length) : buf.length;
});

Override.create("java/io/ByteArrayInputStream.read.()I", function() {
  return (this.pos < this.count) ? (this.buf[this.pos++] & 0xFF) : -1;
});

Override.create("java/io/ByteArrayInputStream.read.([BII)I", function(b, off, len) {
  if (!b) {
    throw new JavaException("java/lang/NullPointerException");
  }

  if ((off < 0) || (off > b.length) || (len < 0) ||
      ((off + len) > b.length)) {
    throw new JavaException("java/lang/IndexOutOfBoundsException");
  }

  if (this.pos >= this.count) {
    return -1;
  }
  if (this.pos + len > this.count) {
    len = this.count - this.pos;
  }
  if (len === 0) {
    return 0;
  }

  b.set(this.buf.subarray(this.pos, this.pos + len), off);

  this.pos += len;
  return len;
});

Override.create("java/io/ByteArrayInputStream.skip.(J)J", function(long, _) {
  var n = long.toNumber();

  if (this.pos + n > this.count) {
    n = this.count - this.pos;
  }

  if (n < 0) {
    return Long.fromNumber(0);
  }

  this.pos += n;

  return Long.fromNumber(n);
});

Override.create("java/io/ByteArrayInputStream.available.()I", function() {
  return this.count - this.pos;
});

Override.create("java/io/ByteArrayInputStream.mark.(I)V", function(readAheadLimit) {
  this.mark = this.pos;
});

Override.create("java/io/ByteArrayInputStream.reset.()V", function() {
  this.pos = this.mark;
});

// The following Permissions methods are overriden to avoid expensive calls to
// DomainPolicy.loadValues. This has the added benefit that we avoid many other
// computations.

Override.create("com/sun/midp/security/Permissions.forDomain.(Ljava/lang/String;)[[B", function(name) {
  // NUMBER_OF_PERMISSIONS = PermissionsStrings.PERMISSION_STRINGS.length + 2
  // The 2 is the two hardcoded MIPS and AMS permissions.
  var NUMBER_OF_PERMISSIONS = 61;
  var ALLOW = 1;

  var maximums = util.newPrimitiveArray("B", NUMBER_OF_PERMISSIONS);
  var defaults = util.newPrimitiveArray("B", NUMBER_OF_PERMISSIONS);

  for (var i = 0; i < NUMBER_OF_PERMISSIONS; i++) {
    maximums[i] = defaults[i] = ALLOW;
  }

  var permissions = util.newArray("[[B", 2);
  permissions[0] = maximums;
  permissions[1] = defaults;

  return permissions;
});

// Always return true to make Java think the MIDlet domain is trusted.
Override.create("com/sun/midp/security/Permissions.isTrusted.(Ljava/lang/String;)Z", function(name) {
  return true;
});

// Returns the ID of the permission. The callers will use this ID to check the
// permission in the permissions array returned by Permissions::forDomain.
Override.create("com/sun/midp/security/Permissions.getId.(Ljava/lang/String;)I", function(name) {
  return 0;
});

// The Java code that uses this method doesn't actually use the return value, but
// passes it to Permissions.getId. So we can return anything.
Override.create("com/sun/midp/security/Permissions.getName.(I)Ljava/lang/String;", function(id) {
  return "com.sun.midp";
});
