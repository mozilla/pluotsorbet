/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

var Override = {};

function JavaException(className, message) {
  this.javaClassName = className;
  this.message = message;
}
JavaException.prototype = Object.create(Error.prototype);

function boolReturnType(ret) {
  var value;
  if (ret) {
    value = 1;
  } else {
    value = 0;
  }
  return value;
}
boolReturnType.slotSize = 1;

function doubleReturnType(ret) {
  return ret;
}
doubleReturnType.slotSize = 2;

function voidReturnType(ret) {
  // no-op
}
voidReturnType.slotSize = 0;

function stringReturnType(ret) {
  var value;
  if (typeof ret === "string") {
    value = J2ME.newString(ret);
  } else {
    // already a native string or null
    value = ret;
  }
  return value;
}
stringReturnType.slotSize = 1;

function defaultReturnType(ret) {
    return ret;
}
defaultReturnType.slotSize = 1;

function intReturnType(ret) {
    var value = ret | 0;
    return value;
}
intReturnType.slotSize = 1;

function getReturnFunction(sig) {
  var retType = sig.substring(sig.lastIndexOf(")") + 1);
  var fxn;
  switch (retType) {
    case 'V': fxn = voidReturnType; break;
    case 'I': fxn = intReturnType; break;
    case 'Z': fxn = boolReturnType; break;
    case 'J':
    case 'D': fxn = doubleReturnType; break;
    case 'Ljava/lang/String;': fxn = stringReturnType; break;
    default: fxn = defaultReturnType; break;
  }

  return fxn;
}

function executePromise(ret, doReturn, ctx, key) {
  ret.then(function(res) {
    if (Instrument.profiling) {
      Instrument.exitAsyncNative(key, ret);
    }
    var stack = ctx.current().stack;
    var convertedValue = doReturn(res);
    switch (doReturn.slotSize) {
      case 0:
        break;
      case 1:
        stack.push(convertedValue);
        break;
      case 2:
        stack.push2(convertedValue);
        break;
    }
  }, function(e) {
    var syntheticMethod = new MethodInfo({
      name: "RaiseExceptionSynthetic",
      signature: "()V",
      isStatic: true,
      classInfo: {
        className: e.javaClassName,
        vmc: {},
        vfc: {},
        constant_pool: [
          null,
          {tag: TAGS.CONSTANT_Class, name_index: 2},
          {bytes: e.javaClassName},
          {tag: TAGS.CONSTANT_String, string_index: 4},
          {bytes: e.message},
          {tag: TAGS.CONSTANT_Methodref, class_index: 1, name_and_type_index: 6},
          {name_index: 7, signature_index: 8},
          {bytes: "<init>"},
          {bytes: "(Ljava/lang/String;)V"},
        ],
      },
      code: new Uint8Array([
        0xbb, 0x00, 0x01, // new <idx=1>
        0x59,             // dup
        0x12, 0x03,       // ldc <idx=2>
        0xb7, 0x00, 0x05, // invokespecial <idx=5>
        0xbf              // athrow
      ])
    });
    var callee = new Frame(syntheticMethod, [], 0);
    ctx.frames.push(callee);
  }).then(ctx.resume.bind(ctx));

  if (Instrument.profiling) {
    Instrument.enterAsyncNative(key, ret);
  }

  $.pause();
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

  object[key] = function() {
    var ctx = $.ctx;
    try {
      var args = Array.prototype.slice.apply(arguments);
      args.push(ctx);
      var ret = fn.apply(this, args);
      return postExec(ret, doReturn, ctx, key);
    } catch(e) {
      if (e.name === "TypeError") {
        // JavaScript's TypeError is analogous to a NullPointerException.
        console.log(e.stack);
        throw ctx.createException("java/lang/NullPointerException", e);
      } else if (e.javaClassName) {
        throw ctx.createException(e.javaClassName, e.message);
      } else if (e.klass) {
        throw e;
      } else {
        console.error(e, e.stack);
        throw ctx.createException("java/lang/RuntimeException", e);
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

  var count = this.klass.classInfo.getField("I.count.I").get(this);
  var buf = this.klass.classInfo.getField("I.buf.[B").get(this);

  var newcount = count + len;
  if (newcount > buf.length) {
    var newbuf = J2ME.newByteArray(Math.max(buf.length << 1, newcount));
    newbuf.set(buf);
    buf = newbuf;
    this.klass.classInfo.getField("I.buf.[B").set(this, buf);
  }

  buf.set(b.subarray(off, off + len), count);
  this.klass.classInfo.getField("I.count.I").set(this, newcount);
});

Override.create("java/io/ByteArrayOutputStream.write.(I)V", function(value) {
  var count = this.klass.classInfo.getField("I.count.I").get(this);
  var buf = this.klass.classInfo.getField("I.buf.[B").get(this);

  var newcount = count + 1;
  if (newcount > buf.length) {
    var newbuf = J2ME.newByteArray(Math.max(buf.length << 1, newcount));
    newbuf.set(buf);
    buf = newbuf;
    this.klass.classInfo.getField("I.buf.[B").set(this, buf);
  }

  buf[count] = value;
  this.klass.classInfo.getField("I.count.I").set(this, newcount);
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

Override.create("java/io/ByteArrayInputStream.skip.(J)J", function(long) {
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

  var maximums = J2ME.newByteArray(NUMBER_OF_PERMISSIONS);
  var defaults = J2ME.newByteArray(NUMBER_OF_PERMISSIONS);

  for (var i = 0; i < NUMBER_OF_PERMISSIONS; i++) {
    maximums[i] = defaults[i] = ALLOW;
  }

  var permissions = J2ME.newArray(J2ME.PrimitiveArrayClassInfo.B.klass, 2);
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
