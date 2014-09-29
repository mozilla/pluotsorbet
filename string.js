/* -*- tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */
'use strict';

/**
 * These methods reimplement java.lang.String using native JS strings,
 * stored as the `str` property on the java.lang.String instance.
 *
 * All methods on java.lang.String are implemented here in the same
 * order as java.lang.String, with the exception of a couple of the
 * String.valueOf() methods, whose absence is documented below.
 */

function isString(obj) {
  return obj && obj.str !== undefined;
}


//****************************************************************
// Constructors

Override.simple("java/lang/String.<init>.()V", function() {
  this.str = "";
});

Override.simple("java/lang/String.<init>.(Ljava/lang/String;)V", function(jStr) {
  if (!jStr) {
    throw new JavaException("java/lang/NullPointerException");
  }
  this.str = jStr.str;
});

Override.simple("java/lang/String.<init>.([C)V", function(chars) {
  if (!chars) {
    throw new JavaException("java/lang/NullPointerException");
  }
  this.str = util.fromJavaChars(chars);
});

Override.simple("java/lang/String.<init>.([CII)V", function(value, offset, count) {
  if (offset < 0 || count < 0 || offset > value.length - count) {
    throw new JavaException("java/lang/IndexOutOfBoundsException");
  }
  this.str = util.fromJavaChars(value, offset, count);
});

// Several constructors below share this implementation:
function constructFromByteArray(bytes, off, len, enc) {
  enc = normalizeEncoding(enc);
  bytes = bytes.subarray(off, off + len);
  try {
    this.str = new TextDecoder(enc).decode(bytes);
  } catch(e) {
    throw new JavaException("java/io/UnsupportedEncodingException");
  }
}

Override.simple(
  "java/lang/String.<init>.([BIILjava/lang/String;)V",
  function(bytes, off, len, enc) {
    constructFromByteArray.call(this, bytes, off, len, enc.str);
  });

Override.simple(
  "java/lang/String.<init>.([BLjava/lang/String;)V",
  function(bytes, enc) {
    constructFromByteArray.call(this, bytes, 0, bytes.length, enc.str);
  });

Override.simple(
  "java/lang/String.<init>.([BII)V",
  function(bytes, offset, len) {
    constructFromByteArray.call(this, bytes, offset, len, "UTF-8");
  });

Override.simple(
  "java/lang/String.<init>.([B)V",
  function(bytes) {
    constructFromByteArray.call(this, bytes, 0, bytes.length, "UTF-8");
  });

Override.simple(
  "java/lang/String.<init>.(Ljava/lang/StringBuffer;)V",
  function(buffer) {
    var value = buffer.class.getField("I.value.[C").get(buffer);
    var count = buffer.class.getField("I.count.I").get(buffer);
    this.str = util.fromJavaChars(value, 0, count);
  });

Override.simple(
  "java/lang/String.<init>.(II[C)V",
  function(offset, count, value) {
    this.str = util.fromJavaChars(value, offset, count);
  });

//****************************************************************
// Methods

Override.simple("java/lang/String.length.()I", function() {
  return this.str.length;
});

Override.simple("java/lang/String.charAt.(I)C", function(index) {
  if (index < 0 || index >= this.str.length) {
    throw new JavaException("java/lang/IndexOutOfBoundsException");
  }
  return this.str.charCodeAt(index);
});

Override.simple("java/lang/String.getChars.(II[CI)V", function(srcBegin, srcEnd, dst, dstBegin) {
  if (srcBegin < 0 || srcEnd > this.str.length || srcBegin > srcEnd ||
      dstBegin + (srcEnd - srcBegin) > dst.length || dstBegin < 0) {
    throw new JavaException("java/lang/IndexOutOfBoundsException");
  }
  var len = srcEnd - srcBegin;
  for (var i = 0; i < len; i++) {
    dst[dstBegin + i] = this.str.charCodeAt(srcBegin + i);
  }
});

// Java returns encodings like "UTF_16"; TextEncoder and friends only
// like hyphens, not underscores.
function normalizeEncoding(enc) {
  var encoding = enc.toLowerCase().replace(/_/g, '-');
  if (encoding == "utf-16") {
    encoding = "utf-16be"; // Java defaults to big-endian, JS to little-endian.
  }
  return encoding;
}

Override.simple("java/lang/String.getBytes.(Ljava/lang/String;)[B", function(jEnc) {
  try {
    var encoding = normalizeEncoding(jEnc.str);
    return new Int8Array(new TextEncoder(encoding).encode(this.str));
  } catch (e) {
    throw new JavaException("java/io/UnsupportedEncodingException");
  }
});

Override.simple("java/lang/String.getBytes.()[B", function() {
  return new Int8Array(new TextEncoder("utf-8").encode(this.str));
});

Override.simple("java/lang/String.equals.(Ljava/lang/Object;)Z", function(anObject) {
  return (isString(anObject) && anObject.str === this.str) ? 1 : 0;
});

Override.simple("java/lang/String.equalsIgnoreCase.(Ljava/lang/String;)Z", function(anotherString) {
  return (isString(anotherString) && (anotherString.str.toLowerCase() === this.str.toLowerCase())) ? 1 : 0;
});

Override.simple("java/lang/String.compareTo.(Ljava/lang/String;)I", function(anotherString) {
  // Sadly, JS String doesn't have a compareTo() method, so we must
  // replicate the Java algorithm. (There is String.localeCompare, but
  // that only returns {-1, 0, 1}, not a distance measure, which this
  // requires.
  var len1 = this.str.length;
  var len2 = anotherString.str.length;
  var n = Math.min(len1, len2);
  var v1 = this.str;
  var v2 = anotherString.str;
  for (var k = 0; k < n; k++) {
    var c1 = v1.charCodeAt(k);
    var c2 = v2.charCodeAt(k);
    if (c1 != c2) {
      return c1 - c2;
    }
  }
  return len1 - len2;
});

Override.simple("java/lang/String.regionMatches.(ZILjava/lang/String;II)Z", function(ignoreCase, toffset, other, ooffset, len) {
  var a = (ignoreCase ? this.str.toLowerCase() : this.str);
  var b = (ignoreCase ? other.str.toLowerCase() : other.str);
  return a.substr(toffset, len) === b.substr(ooffset, len);
});

Override.simple("java/lang/String.startsWith.(Ljava/lang/String;I)Z", function(prefix, toffset) {
  return this.str.substr(toffset, prefix.str.length) === prefix.str;
});

Override.simple("java/lang/String.startsWith.(Ljava/lang/String;)Z", function(prefix) {
  return this.str.substr(0, prefix.str.length) === prefix.str;
});

Override.simple("java/lang/String.endsWith.(Ljava/lang/String;)Z", function(suffix) {
  return this.str.indexOf(suffix.str, this.str.length - suffix.str.length) !== -1;
});

Override.simple("java/lang/String.hashCode.()I", function() {
  var hash = 0;
  for (var i = 0; i < this.str.length; i++) {
    hash = Math.imul(31, hash) + this.str.charCodeAt(i) | 0;
  }
  return hash;
});

Override.simple("java/lang/String.indexOf.(I)I", function(ch) {
  return this.str.indexOf(String.fromCharCode(ch));
});

Override.simple("java/lang/String.indexOf.(II)I", function(ch, fromIndex) {
  return this.str.indexOf(String.fromCharCode(ch), fromIndex);
});

Override.simple("java/lang/String.lastIndexOf.(I)I", function(ch) {
  return this.str.lastIndexOf(String.fromCharCode(ch));
});

Override.simple("java/lang/String.lastIndexOf.(II)I", function(ch, fromIndex) {
  return this.str.lastIndexOf(String.fromCharCode(ch), fromIndex);
});

Override.simple("java/lang/String.indexOf.(Ljava/lang/String;)I", function(s) {
  return this.str.indexOf(s.str);
});

Override.simple("java/lang/String.indexOf.(Ljava/lang/String;I)I", function(s, fromIndex) {
  return this.str.indexOf(s.str, fromIndex);
});

Override.simple("java/lang/String.substring.(I)Ljava/lang/String;", function(beginIndex) {
  if (beginIndex < 0 || beginIndex > this.str.length) {
    throw new JavaException("java/lang/IndexOutOfBoundsException");
  }
  return this.str.substring(beginIndex);
});

Override.simple("java/lang/String.substring.(II)Ljava/lang/String;", function(beginIndex, endIndex) {
  if (beginIndex < 0 || endIndex > this.str.length || beginIndex > endIndex) {
    throw new JavaException("java/lang/IndexOutOfBoundsException");
  }
  return this.str.substring(beginIndex, endIndex);
});

Override.simple("java/lang/String.concat.(Ljava/lang/String;)Ljava/lang/String;", function(s) {
  return this.str + s.str;
});

// via MDN:
function escapeRegExp(str) {
  return str.replace(/[\-\[\]\/\{\}\(\)\*\+\?\.\\\^\$\|]/g, "\\$&");
}

Override.simple("java/lang/String.replace.(CC)Ljava/lang/String;", function(oldChar, newChar) {
  // Using a RegExp here to replace all matches of oldChar, rather than just the first.
  return this.str.replace(
    new RegExp(escapeRegExp(String.fromCharCode(oldChar)), "g"),
    String.fromCharCode(newChar));
});

Override.simple("java/lang/String.toLowerCase.()Ljava/lang/String;", function() {
  return this.str.toLowerCase();
});

Override.simple("java/lang/String.toUpperCase.()Ljava/lang/String;", function() {
  return this.str.toUpperCase();
});

Override.simple("java/lang/String.trim.()Ljava/lang/String;", function() {
  // Java's String.trim() removes any character <= ASCII 32;
  // JavaScript's only removes a few whitespacey chars.
  var start = 0;
  var end = this.str.length;
  while (start < end && this.str.charCodeAt(start) <= 32) {
    start++;
  }
  while (start < end && this.str.charCodeAt(end - 1) <= 32) {
    end--;
  }

  return this.str.substring(start, end);
});

Override.simple("java/lang/String.toString.()Ljava/lang/String;", function() {
  return this; // Note: returning "this" so that we keep the same object.
});

Override.simple("java/lang/String.toCharArray.()[C", function() {
  return util.javaStringToArrayBuffer(this);
});

//****************************************************************
// String.valueOf() for various types

// NOTE: String.valueOf(Object) left in Java to avoid having to call
// back into Java for Object.toString().

Override.simple("java/lang/String.valueOf.([C)Ljava/lang/String;", function(chars) {
  if (!chars) {
    throw new JavaException("java/lang/NullPointerException");
  }
  return util.fromJavaChars(chars);
}, { static: true });

Override.simple("java/lang/String.valueOf.([CII)Ljava/lang/String;", function(chars, offset, count) {
  if (!chars) {
    throw new JavaException("java/lang/NullPointerException");
  }
  return util.fromJavaChars(chars, offset, count);
}, { static: true });

Override.simple("java/lang/String.valueOf.(Z)Ljava/lang/String;", function(bool) {
  return bool ? "true" : "false";
}, { static: true });

Override.simple("java/lang/String.valueOf.(C)Ljava/lang/String;", function(ch) {
  return String.fromCharCode(ch);
}, { static: true });

Override.simple("java/lang/String.valueOf.(I)Ljava/lang/String;", function(n) {
  return n.toString();
}, { static: true });

Override.simple("java/lang/String.valueOf.(J)Ljava/lang/String;", function(n, _) {
  // This function takes a dummy second argument, since we're taking a
  // Long and need to pop two items off the stack.
  return n.toString();
}, { static: true });


// String.valueOf(float) and String.valueOf(double) have been left in
// Java for now, as they follow complex formatting rules.
// Additionally, the test suite covers things like positive zero vs.
// negative zero, which we don't currently distinguish.

// Note: String.intern is implemented in `native.js`.


//****************************************************************
// StringBuffer

// Overriding StringBuffer.toString() avoids calling "new
// String(this)" via JVM bytecode.
Override.simple("java/lang/StringBuffer.toString.()Ljava/lang/String;", function() {
  var value = this.class.getField("I.value.[C").get(this);
  var count = this.class.getField("I.count.I").get(this);
  return util.fromJavaChars(value, 0, count);
});
