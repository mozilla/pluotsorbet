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

Override["java/io/ByteArrayOutputStream.write.([BII)V"] = function(b, off, len) {
  if ((off < 0) || (off > b.length) || (len < 0) ||
      ((off + len) > b.length)) {
    throw $.newIndexOutOfBoundsException();
  }

  if (len == 0) {
    return;
  }

  var count = this.count;
  var buf = this.buf;

  var newcount = count + len;
  if (newcount > buf.length) {
    var newbuf = J2ME.newByteArray(Math.max(buf.length << 1, newcount));
    newbuf.set(buf);
    buf = newbuf;
    this.buf = buf;
  }

  buf.set(b.subarray(off, off + len), count);
  this.count = newcount;
};

Override["java/io/ByteArrayOutputStream.write.(I)V"] = function(value) {
  var count = this.count;
  var buf = this.buf;

  var newcount = count + 1;
  if (newcount > buf.length) {
    var newbuf = J2ME.newByteArray(Math.max(buf.length << 1, newcount));
    newbuf.set(buf);
    buf = newbuf;
    this.buf = buf;
  }

  buf[count] = value;
  this.count = newcount;
};

Override["java/io/ByteArrayInputStream.<init>.([B)V"] = function(buf) {
  if (!buf) {
    throw $.newNullPointerException();
  }

  this.buf = buf;
  this.pos = this.mark = 0;
  this.count = buf.length;
};

Override["java/io/ByteArrayInputStream.<init>.([BII)V"] = function(buf, offset, length) {
  if (!buf) {
    throw $.newNullPointerException();
  }

  this.buf = buf;
  this.pos = this.mark = offset;
  this.count = (offset + length <= buf.length) ? (offset + length) : buf.length;
};

Override["java/io/ByteArrayInputStream.read.()I"] = function() {
  return (this.pos < this.count) ? (this.buf[this.pos++] & 0xFF) : -1;
};

Override["java/io/ByteArrayInputStream.read.([BII)I"] = function(b, off, len) {
  if (!b) {
    throw $.newNullPointerException();
  }

  if ((off < 0) || (off > b.length) || (len < 0) ||
      ((off + len) > b.length)) {
    throw $.newIndexOutOfBoundsException();
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
};

Override["java/io/ByteArrayInputStream.skip.(J)J"] = function(long) {
  var n = long.toNumber();

  if (this.pos + n > this.count) {
    n = this.count - this.pos;
  }

  if (n < 0) {
    return Long.fromNumber(0);
  }

  this.pos += n;

  return Long.fromNumber(n);
};

Override["java/io/ByteArrayInputStream.available.()I"] = function() {
  return this.count - this.pos;
};

Override["java/io/ByteArrayInputStream.mark.(I)V"] = function(readAheadLimit) {
  this.mark = this.pos;
};

Override["java/io/ByteArrayInputStream.reset.()V"] = function() {
  this.pos = this.mark;
};
