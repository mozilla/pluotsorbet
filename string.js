/* -*- tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */
'use strict';

/**
 * string.js: Native implementations of String and StringBuffer.
 *
 * Methods are defined in the same order as the Java source.
 * Any missing methods have been noted in comments.
 */

//################################################################
// java.lang.String (manipulated via the 'str' property)

function isString(obj) {
  return obj && obj.str !== undefined;
}

//****************************************************************
// Constructors

Override.create("java/lang/String.<init>.()V", function(ctx) {
  this.str = "";
});

Override.create("java/lang/String.<init>.(Ljava/lang/String;)V", function(ctx, jStr) {
  if (!jStr) {
    throw new JavaException("java/lang/NullPointerException");
  }
  this.str = jStr.str;
});

Override.create("java/lang/String.<init>.([C)V", function(ctx, chars) {
  if (!chars) {
    throw new JavaException("java/lang/NullPointerException");
  }
  this.str = util.fromJavaChars(chars);
});

Override.create("java/lang/String.<init>.([CII)V", function(ctx, value, offset, count) {
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

Override.create(
  "java/lang/String.<init>.([BIILjava/lang/String;)V",
  function(ctx, bytes, off, len, enc) {
    constructFromByteArray.call(this, bytes, off, len, enc.str);
  });

Override.create(
  "java/lang/String.<init>.([BLjava/lang/String;)V",
  function(ctx, bytes, enc) {
    constructFromByteArray.call(this, bytes, 0, bytes.length, enc.str);
  });

Override.create(
  "java/lang/String.<init>.([BII)V",
  function(ctx, bytes, offset, len) {
    constructFromByteArray.call(this, bytes, offset, len, "UTF-8");
  });

Override.create(
  "java/lang/String.<init>.([B)V",
  function(ctx, bytes) {
    constructFromByteArray.call(this, bytes, 0, bytes.length, "UTF-8");
  });

Override.create(
  "java/lang/String.<init>.(Ljava/lang/StringBuffer;)V",
  function(ctx, jBuffer) {
    this.str = util.fromJavaChars(jBuffer.buf, 0, jBuffer.count);
  });

Override.create(
  "java/lang/String.<init>.(II[C)V",
  function(ctx, offset, count, value) {
    this.str = util.fromJavaChars(value, offset, count);
  });

//****************************************************************
// Methods

Override.create("java/lang/String.length.()I", function(ctx) {
  return this.str.length;
});

Override.create("java/lang/String.charAt.(I)C", function(ctx, index) {
  if (index < 0 || index >= this.str.length) {
    throw new JavaException("java/lang/IndexOutOfBoundsException");
  }
  return this.str.charCodeAt(index);
});

Override.create("java/lang/String.getChars.(II[CI)V", function(ctx, srcBegin, srcEnd, dst, dstBegin) {
  if (srcBegin < 0 || srcEnd > this.str.length || srcBegin > srcEnd ||
      dstBegin + (srcEnd - srcBegin) > dst.length || dstBegin < 0) {
    throw new JavaException("java/lang/IndexOutOfBoundsException");
  }
  dst.set(util.stringToCharArray(this.str.substring(srcBegin, srcEnd)), dstBegin);
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

Override.create("java/lang/String.getBytes.(Ljava/lang/String;)[B", function(ctx, jEnc) {
  try {
    var encoding = normalizeEncoding(jEnc.str);
    return new Int8Array(new TextEncoder(encoding).encode(this.str));
  } catch (e) {
    throw new JavaException("java/io/UnsupportedEncodingException");
  }
});

Override.create("java/lang/String.getBytes.()[B", function(ctx) {
  return new Int8Array(new TextEncoder("utf-8").encode(this.str));
});

Override.create("java/lang/String.equals.(Ljava/lang/Object;)Z", function(ctx, anObject) {
  return !!(isString(anObject) && anObject.str === this.str);
});

Override.create("java/lang/String.equalsIgnoreCase.(Ljava/lang/String;)Z", function(ctx, anotherString) {
  return !!(isString(anotherString) && anotherString.str.toLowerCase() === this.str.toLowerCase());
});

Override.create("java/lang/String.compareTo.(Ljava/lang/String;)I", function(ctx, anotherString) {
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

Override.create("java/lang/String.regionMatches.(ZILjava/lang/String;II)Z", function(ctx, ignoreCase, toffset, other, ooffset, len) {
  var a = (ignoreCase ? this.str.toLowerCase() : this.str);
  var b = (ignoreCase ? other.str.toLowerCase() : other.str);
  return a.substr(toffset, len) === b.substr(ooffset, len);
});

Override.create("java/lang/String.startsWith.(Ljava/lang/String;I)Z", function(ctx, prefix, toffset) {
  return this.str.substr(toffset, prefix.str.length) === prefix.str;
});

Override.create("java/lang/String.startsWith.(Ljava/lang/String;)Z", function(ctx, prefix) {
  return this.str.substr(0, prefix.str.length) === prefix.str;
});

Override.create("java/lang/String.endsWith.(Ljava/lang/String;)Z", function(ctx, suffix) {
  return this.str.indexOf(suffix.str, this.str.length - suffix.str.length) !== -1;
});

Override.create("java/lang/String.hashCode.()I", function(ctx) {
  var hash = 0;
  for (var i = 0; i < this.str.length; i++) {
    hash = Math.imul(31, hash) + this.str.charCodeAt(i) | 0;
  }
  return hash;
});

Override.create("java/lang/String.indexOf.(I)I", function(ctx, ch) {
  return this.str.indexOf(String.fromCharCode(ch));
});

Override.create("java/lang/String.indexOf.(II)I", function(ctx, ch, fromIndex) {
  return this.str.indexOf(String.fromCharCode(ch), fromIndex);
});

Override.create("java/lang/String.lastIndexOf.(I)I", function(ctx, ch) {
  return this.str.lastIndexOf(String.fromCharCode(ch));
});

Override.create("java/lang/String.lastIndexOf.(II)I", function(ctx, ch, fromIndex) {
  return this.str.lastIndexOf(String.fromCharCode(ch), fromIndex);
});

Override.create("java/lang/String.indexOf.(Ljava/lang/String;)I", function(ctx, s) {
  return this.str.indexOf(s.str);
});

Override.create("java/lang/String.indexOf.(Ljava/lang/String;I)I", function(ctx, s, fromIndex) {
  return this.str.indexOf(s.str, fromIndex);
});

Override.create("java/lang/String.substring.(I)Ljava/lang/String;", function(ctx, beginIndex) {
  if (beginIndex < 0 || beginIndex > this.str.length) {
    throw new JavaException("java/lang/IndexOutOfBoundsException");
  }
  return this.str.substring(beginIndex);
});

Override.create("java/lang/String.substring.(II)Ljava/lang/String;", function(ctx, beginIndex, endIndex) {
  if (beginIndex < 0 || endIndex > this.str.length || beginIndex > endIndex) {
    throw new JavaException("java/lang/IndexOutOfBoundsException");
  }
  return this.str.substring(beginIndex, endIndex);
});

Override.create("java/lang/String.concat.(Ljava/lang/String;)Ljava/lang/String;", function(ctx, s) {
  return this.str + s.str;
});

// via MDN:
function escapeRegExp(str) {
  return str.replace(/[\-\[\]\/\{\}\(\)\*\+\?\.\\\^\$\|]/g, "\\$&");
}

Override.create("java/lang/String.replace.(CC)Ljava/lang/String;", function(ctx, oldChar, newChar) {
  // Using a RegExp here to replace all matches of oldChar, rather than just the first.
  return this.str.replace(
    new RegExp(escapeRegExp(String.fromCharCode(oldChar)), "g"),
    String.fromCharCode(newChar));
});

Override.create("java/lang/String.toLowerCase.()Ljava/lang/String;", function(ctx) {
  return this.str.toLowerCase();
});

Override.create("java/lang/String.toUpperCase.()Ljava/lang/String;", function(ctx) {
  return this.str.toUpperCase();
});

Override.create("java/lang/String.trim.()Ljava/lang/String;", function(ctx) {
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

Override.create("java/lang/String.toString.()Ljava/lang/String;", function(ctx) {
  return this; // Note: returning "this" so that we keep the same object.
});

Override.create("java/lang/String.toCharArray.()[C", function(ctx) {
  return util.stringToCharArray(this.str);
});

//****************************************************************
// String.valueOf() for various types

// NOTE: String.valueOf(Object) left in Java to avoid having to call
// back into Java for Object.toString().

Override.create("java/lang/String.valueOf.([C)Ljava/lang/String;", function(ctx, chars) {
  if (!chars) {
    throw new JavaException("java/lang/NullPointerException");
  }
  return util.fromJavaChars(chars);
});

Override.create("java/lang/String.valueOf.([CII)Ljava/lang/String;", function(ctx, chars, offset, count) {
  if (!chars) {
    throw new JavaException("java/lang/NullPointerException");
  }
  return util.fromJavaChars(chars, offset, count);
});

Override.create("java/lang/String.valueOf.(Z)Ljava/lang/String;", function(ctx, bool) {
  return bool ? "true" : "false";
});

Override.create("java/lang/String.valueOf.(C)Ljava/lang/String;", function(ctx, ch) {
  return String.fromCharCode(ch);
});

Override.create("java/lang/String.valueOf.(I)Ljava/lang/String;", function(ctx, n) {
  return n.toString();
});

Override.create("java/lang/String.valueOf.(J)Ljava/lang/String;", function(ctx, n, _) {
  // This function takes a dummy second argument, since we're taking a
  // Long and need to pop two items off the stack.
  return n.toString();
});


// String.valueOf(float) and String.valueOf(double) have been left in
// Java for now, as they require support for complex formatting rules.
// Additionally, their tests check for coverage of nuanced things like
// positive zero vs. negative zero, which we don't currently support.

var internedStrings = new Map();

Native["java/lang/String.intern.()Ljava/lang/String;"] = function(ctx, stack) {
    var javaString = stack.pop();
    var string = util.fromJavaString(javaString);

    var internedString = internedStrings.get(string);

    if (internedString) {
        stack.push(internedString);
    } else {
        internedStrings.set(string, javaString);
        stack.push(javaString);
    }
}



//################################################################
// java.lang.StringBuffer (manipulated via the 'buf' property)

Override.create("java/lang/StringBuffer.<init>.()V", function(ctx) {
  this.buf = new Uint16Array(16); // Initial buffer size: 16, per the Java implementation.
  this.count = 0;
});

Override.create("java/lang/StringBuffer.<init>.(I)V", function(ctx, length) {
  if (length < 0) {
    throw new JavaException("java/lang/NegativeArraySizeException");
  }
  this.buf = new Uint16Array(length);
  this.count = 0;
});

Override.create("java/lang/StringBuffer.<init>.(Ljava/lang/String;)V", function(ctx, jStr) {
  var stringBuf = util.stringToCharArray(jStr.str);
  this.buf = new Uint16Array(stringBuf.length + 16); // Add 16, per the Java implementation.
  this.buf.set(stringBuf, 0);
  this.count = stringBuf.length;
});

Override.create("java/lang/StringBuffer.length.()I", function(ctx) {
  return this.count;
});

Override.create("java/lang/StringBuffer.capacity.()I", function(ctx) {
  return this.buf.length;
});

Override.create("java/lang/StringBuffer.copy.()V", function(ctx) {
  // We don't support copying (there's no need unless we also support shared buffers).
});

/**
 * Expand capacity to max(minCapacity, (capacity + 1) * 2).
 *
 * @this StringBuffer
 * @param {number} minCapacity
 */
function expandCapacity(minCapacity) {
  var newCapacity = (this.buf.length + 1) << 1;
  if (minCapacity > newCapacity) {
    newCapacity = minCapacity;
  }

  var oldBuf = this.buf;
  this.buf = new Uint16Array(newCapacity);
  this.buf.set(oldBuf, 0);
}

Override.create("java/lang/StringBuffer.ensureCapacity.(I)V", function(ctx, minCapacity) {
  if (this.buf.length < minCapacity) {
    expandCapacity.call(this, minCapacity);
  }
});

// StringBuffer.expandCapacity is private and not needed with these overrides.

Override.create("java/lang/StringBuffer.setLength.(I)V", function(ctx, newLength) {
  if (newLength < 0) {
    throw new JavaException("java/lang/StringIndexOutOfBoundsException");
  }

  if (newLength > this.buf.length) {
    expandCapacity.call(this, newLength);
  }
  for (; this.count < newLength; this.count++) {
    this.buf[this.count] = '\0';
  }
  this.count = newLength;
});


Override.create("java/lang/StringBuffer.charAt.(I)C", function(ctx, index) {
  if (index < 0 || index >= this.count) {
    throw new JavaException("java/lang/StringIndexOutOfBoundsException");
  }
  return this.buf[index];
});

Override.create("java/lang/StringBuffer.getChars.(II[CI)V", function(ctx, srcBegin, srcEnd, dst, dstBegin) {
  if (srcBegin < 0 || srcEnd < 0 || srcEnd > this.count || srcBegin > srcEnd) {
    throw new JavaException("java/lang/StringIndexOutOfBoundsException");
  }
  if (dstBegin + (srcEnd - srcBegin) > dst.length || dstBegin < 0) {
    throw new JavaException("java/lang/ArrayIndexOutOfBoundsException");
  }
  dst.set(this.buf.subarray(srcBegin, srcEnd), dstBegin);
});

Override.create("java/lang/StringBuffer.setCharAt.(IC)V", function(ctx, index, ch) {
  if (index < 0 || index >= this.count) {
    throw new JavaException("java/lang/StringIndexOutOfBoundsException");
  }
  this.buf[index] = ch;
});


/**
 * Append `data`, which should be either a JS String or a Uint16Array.
 * Data must not be null.
 *
 * @this StringBuffer
 * @param {Uint16Array|string} data
 * @return this
 */
function stringBufferAppend(data) {
  if (data == null) {
    throw new JavaException("java/lang/NullPointerException");
  }
  if (!(data instanceof Uint16Array)) {
    data = util.stringToCharArray(data);
  }
  if (this.buf.length < this.count + data.length) {
    expandCapacity.call(this, this.count + data.length);
  }
  this.buf.set(data, this.count);
  this.count += data.length;
  return this;
}

// StringBuffer.append(java.lang.Object) left in Java to avoid Object.toString().

Override.create("java/lang/StringBuffer.append.(Ljava/lang/String;)Ljava/lang/StringBuffer;", function(ctx, jStr) {
  return stringBufferAppend.call(this, jStr ? jStr.str : "null");
});

Override.create("java/lang/StringBuffer.append.([C)Ljava/lang/StringBuffer;", function(ctx, chars) {
  if (chars == null) {
    throw new JavaException("java/lang/NullPointerException");
  }
  return stringBufferAppend.call(this, chars);
});

Override.create("java/lang/StringBuffer.append.([CII)Ljava/lang/StringBuffer;", function(ctx, chars, offset, length) {
  if (chars == null) {
    throw new JavaException("java/lang/NullPointerException");
  }
  if (offset < 0 || offset + length > chars.length) {
    throw new JavaException("java/lang/ArrayIndexOutOfBoundsException");
  }
  return stringBufferAppend.call(this, chars.subarray(offset, offset + length));
});

Override.create("java/lang/StringBuffer.append.(Z)Ljava/lang/StringBuffer;", function(ctx, bool) {
  return stringBufferAppend.call(this, bool ? "true" : "false");
});

Override.create("java/lang/StringBuffer.append.(C)Ljava/lang/StringBuffer;", function(ctx, ch) {
  if (this.buf.length < this.count + 1) {
    expandCapacity.call(this, this.count + 1);
  }
  this.buf[this.count++] = ch;
  return this;
});

Override.create("java/lang/StringBuffer.append.(I)Ljava/lang/StringBuffer;", function(ctx, n) {
  return stringBufferAppend.call(this, n + "");
});

Override.create("java/lang/StringBuffer.append.(J)Ljava/lang/StringBuffer;", function(ctx, n, _) {
  return stringBufferAppend.call(this, n + "");
});

// StringBuffer.append(float) left in Java (see String.valueOf(float) above).

// StringBuffer.append(double) left in Java (see String.valueOf(double) above).

/**
 * Delete characters between [start, end).
 *
 * @this StringBuffer
 * @param {number} start
 * @param {number} end
 * @return this
 */
function stringBufferDelete(ctx, start, end) {
  if (start < 0) {
    throw new JavaException("java/lang/StringIndexOutOfBoundsException");
  }
  if (end > this.count) {
    end = this.count;
  }
  if (start > end) {
    throw new JavaException("java/lang/StringIndexOutOfBoundsException");
  }

  var len = end - start;
  if (len > 0) {
    // When Gecko 34 is released, we can use TypedArray.copyWithin() instead.
    this.buf.set(this.buf.subarray(end, this.count), start);
    this.count -= len;
  }
  return this;
}

Override.create("java/lang/StringBuffer.delete.(II)Ljava/lang/StringBuffer;",
                stringBufferDelete);

Override.create("java/lang/StringBuffer.deleteCharAt.(I)Ljava/lang/StringBuffer;", function(ctx, index) {
  if (index >= this.count) {
    // stringBufferDelete handles the other boundary checks; this check is specific to deleteCharAt.
    throw new JavaException("java/lang/StringIndexOutOfBoundsException");
  }
  return stringBufferDelete.call(this, ctx, index, index + 1);
});

/**
 * Insert `data` at the given `offset`.
 *
 * @this StringBuffer
 * @param {number} offset
 * @param {Uint16Array|string} data
 * @return this
 */
function stringBufferInsert(offset, data) {
  if (data == null) {
    throw new JavaException("java/lang/NullPointerException");
  }
  if (offset < 0 || offset > this.count) {
    throw new JavaException("java/lang/ArrayIndexOutOfBoundsException");
  }
  if (!(data instanceof Uint16Array)) {
    data = util.stringToCharArray(data);
  }
  if (this.buf.length < this.count + data.length) {
    expandCapacity.call(this, this.count + data.length);
  }
  // When Gecko 34 is released, we can use TypedArray.copyWithin() instead.
  this.buf.set(this.buf.subarray(offset, this.count), offset + data.length);
  this.buf.set(data, offset);
  this.count += data.length;
  return this;
}

// StringBuffer.insert(Object) left in Java (for String.valueOf()).

Override.create("java/lang/StringBuffer.insert.(ILjava/lang/String;)Ljava/lang/StringBuffer;", function(ctx, offset, jStr) {
  return stringBufferInsert.call(this, offset, jStr ? jStr.str : "null");
});

Override.create("java/lang/StringBuffer.insert.(I[C)Ljava/lang/StringBuffer;", function(ctx, offset, chars) {
  return stringBufferInsert.call(this, offset, chars);
});

Override.create("java/lang/StringBuffer.insert.(IZ)Ljava/lang/StringBuffer;", function(ctx, offset, bool) {
  return stringBufferInsert.call(this, offset, bool ? "true" : "false");
});

Override.create("java/lang/StringBuffer.insert.(IC)Ljava/lang/StringBuffer;", function(ctx, offset, ch) {
  return stringBufferInsert.call(this, offset, String.fromCharCode(ch));
});

Override.create("java/lang/StringBuffer.insert.(II)Ljava/lang/StringBuffer;", function(ctx, offset, n) {
  return stringBufferInsert.call(this, offset, n + "");
});

Override.create("java/lang/StringBuffer.insert.(IJ)Ljava/lang/StringBuffer;", function(ctx, offset, n, _) {
  return stringBufferInsert.call(this, offset, n + "");
});

// StringBuffer.insert(float) left in Java.

// StringBuffer.insert(double) left in Java.

Override.create("java/lang/StringBuffer.reverse.()Ljava/lang/StringBuffer;", function(ctx) {
  var buf = this.buf;
  for (var i = 0, j = this.count - 1; i < j; i++, j--) {
    var tmp = buf[i];
    buf[i] = buf[j];
    buf[j] = tmp;
  }
  return this;
});

Override.create("java/lang/StringBuffer.toString.()Ljava/lang/String;", function(ctx) {
  return util.fromJavaChars(this.buf, 0, this.count);
});

Override.create("java/lang/StringBuffer.setShared.()V", function(ctx) {
  // Our StringBuffers are never shared. Everyone gets their very own!
});

Override.create("java/lang/StringBuffer.getValue.()[C", function(ctx) {
  // In theory, this method should only be called by String (which
  // we've overridden to not do), so it should never be called. In any
  // case, mutating this buf would have the same effect here as it
  // would in Java.
  return this.buf;
});
