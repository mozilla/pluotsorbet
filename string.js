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

Override["java/lang/String.<init>.()V"] = function() {
  this.str = "";
};

Override["java/lang/String.<init>.(Ljava/lang/String;)V"] = function(jStr) {
  if (!jStr) {
    throw $.newNullPointerException();
  }
  this.str = jStr.str;
};

Override["java/lang/String.<init>.([C)V"] = function(chars) {
  if (!chars) {
    throw $.newNullPointerException();
  }
  this.str = util.fromJavaChars(chars);
};

Override["java/lang/String.<init>.([CII)V"] = function(value, offset, count) {
  if (offset < 0 || count < 0 || offset > value.length - count) {
    throw $.newIndexOutOfBoundsException();
  }
  this.str = util.fromJavaChars(value, offset, count);
};

// Several constructors below share this implementation:
function constructFromByteArray(bytes, off, len, enc) {
  enc = normalizeEncoding(enc);
  bytes = bytes.subarray(off, off + len);
  try {
    this.str = new TextDecoder(enc).decode(bytes);
  } catch(e) {
    throw $.newUnsupportedEncodingException();
  }
}

Override["java/lang/String.<init>.([BIILjava/lang/String;)V"] =
  function(bytes, off, len, enc) {
    constructFromByteArray.call(this, bytes, off, len, enc.str);
  };

Override["java/lang/String.<init>.([BLjava/lang/String;)V"] =
  function(bytes, enc) {
    constructFromByteArray.call(this, bytes, 0, bytes.length, enc.str);
  };

Override["java/lang/String.<init>.([BII)V"] =
  function(bytes, offset, len) {
    constructFromByteArray.call(this, bytes, offset, len, "UTF-8");
  };

Override["java/lang/String.<init>.([B)V"] =
  function(bytes) {
    constructFromByteArray.call(this, bytes, 0, bytes.length, "UTF-8");
  };

Override["java/lang/String.<init>.(Ljava/lang/StringBuffer;)V"] =
  function(jBuffer) {
    this.str = util.fromJavaChars(jBuffer.buf, 0, jBuffer.count);
  };

Override["java/lang/String.<init>.(II[C)V"] =
  function(offset, count, value) {
    this.str = util.fromJavaChars(value, offset, count);
  };

//****************************************************************
// Methods

Override["java/lang/String.length.()I"] = function() {
  return this.str.length;
};

Override["java/lang/String.charAt.(I)C"] = function(index) {
  if (index < 0 || index >= this.str.length) {
    throw $.newIndexOutOfBoundsException();
  }
  return this.str.charCodeAt(index);
};

Override["java/lang/String.getChars.(II[CI)V"] = function(srcBegin, srcEnd, dst, dstBegin) {
  if (srcBegin < 0 || srcEnd > this.str.length || srcBegin > srcEnd ||
      dstBegin + (srcEnd - srcBegin) > dst.length || dstBegin < 0) {
    throw $.newIndexOutOfBoundsException();
  }
  dst.set(util.stringToCharArray(this.str.substring(srcBegin, srcEnd)), dstBegin);
};

// Java returns encodings like "UTF_16"; TextEncoder and friends only
// like hyphens, not underscores.
function normalizeEncoding(enc) {
  var encoding = enc.toLowerCase().replace(/_/g, '-');
  if (encoding == "utf-16") {
    encoding = "utf-16be"; // Java defaults to big-endian, JS to little-endian.
  }
  return encoding;
}

Override["java/lang/String.getBytes.(Ljava/lang/String;)[B"] = function(jEnc) {
  try {
    var encoding = normalizeEncoding(jEnc.str);
    return new Int8Array(new TextEncoder(encoding).encode(this.str));
  } catch (e) {
    throw $.newUnsupportedEncodingException();
  }
};

Override["java/lang/String.getBytes.()[B"] = function() {
  return new Int8Array(new TextEncoder("utf-8").encode(this.str));
};

Override["java/lang/String.equals.(Ljava/lang/Object;)Z"] = function(anObject) {
  return (isString(anObject) && anObject.str === this.str) ? 1 : 0;
};

Override["java/lang/String.equalsIgnoreCase.(Ljava/lang/String;)Z"] = function(anotherString) {
  return (isString(anotherString) && anotherString.str.toLowerCase() === this.str.toLowerCase()) ? 1 : 0;
};

Override["java/lang/String.compareTo.(Ljava/lang/String;)I"] = function(anotherString) {
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
};

Override["java/lang/String.regionMatches.(ZILjava/lang/String;II)Z"] = function(ignoreCase, toffset, other, ooffset, len) {
  var a = (ignoreCase ? this.str.toLowerCase() : this.str);
  var b = (ignoreCase ? other.str.toLowerCase() : other.str);
  return a.substr(toffset, len) === b.substr(ooffset, len) ? 1 : 0;
};

Override["java/lang/String.startsWith.(Ljava/lang/String;I)Z"] = function(prefix, toffset) {
  return this.str.substr(toffset, prefix.str.length) === prefix.str ? 1 : 0;
};

Override["java/lang/String.startsWith.(Ljava/lang/String;)Z"] = function(prefix) {
  return this.str.substr(0, prefix.str.length) === prefix.str ? 1 : 0;
};

Override["java/lang/String.endsWith.(Ljava/lang/String;)Z"] = function(suffix) {
  return this.str.indexOf(suffix.str, this.str.length - suffix.str.length) !== -1 ? 1 : 0;
};

Override["java/lang/String.hashCode.()I"] = function() {
  var hash = 0;
  for (var i = 0; i < this.str.length; i++) {
    hash = Math.imul(31, hash) + this.str.charCodeAt(i) | 0;
  }
  return hash;
};

Override["java/lang/String.indexOf.(I)I"] = function(ch) {
  return this.str.indexOf(String.fromCharCode(ch));
};

Override["java/lang/String.indexOf.(II)I"] = function(ch, fromIndex) {
  return this.str.indexOf(String.fromCharCode(ch), fromIndex);
};

Override["java/lang/String.lastIndexOf.(I)I"] = function(ch) {
  return this.str.lastIndexOf(String.fromCharCode(ch));
};

Override["java/lang/String.lastIndexOf.(II)I"] = function(ch, fromIndex) {
  return this.str.lastIndexOf(String.fromCharCode(ch), fromIndex);
};

Override["java/lang/String.indexOf.(Ljava/lang/String;)I"] = function(s) {
  return this.str.indexOf(s.str);
};

Override["java/lang/String.indexOf.(Ljava/lang/String;I)I"] = function(s, fromIndex) {
  return this.str.indexOf(s.str, fromIndex);
};

Override["java/lang/String.substring.(I)Ljava/lang/String;"] = function(beginIndex) {
  if (beginIndex < 0 || beginIndex > this.str.length) {
    throw $.newIndexOutOfBoundsException();
  }
  return J2ME.newString(this.str.substring(beginIndex));
};

Override["java/lang/String.substring.(II)Ljava/lang/String;"] = function(beginIndex, endIndex) {
  if (beginIndex < 0 || endIndex > this.str.length || beginIndex > endIndex) {
    throw $.newIndexOutOfBoundsException();
  }
  return J2ME.newString(this.str.substring(beginIndex, endIndex));
};

Override["java/lang/String.concat.(Ljava/lang/String;)Ljava/lang/String;"] = function(s) {
  return J2ME.newString(this.str + s.str);
};

// via MDN:
function escapeRegExp(str) {
  return str.replace(/[\-\[\]\/\{\}\(\)\*\+\?\.\\\^\$\|]/g, "\\$&");
}

Override["java/lang/String.replace.(CC)Ljava/lang/String;"] = function(oldChar, newChar) {
  // Using a RegExp here to replace all matches of oldChar, rather than just the first.
  return J2ME.newString(this.str.replace(
    new RegExp(escapeRegExp(String.fromCharCode(oldChar)), "g"),
    String.fromCharCode(newChar)));
};

Override["java/lang/String.toLowerCase.()Ljava/lang/String;"] = function() {
  return J2ME.newString(this.str.toLowerCase());
};

Override["java/lang/String.toUpperCase.()Ljava/lang/String;"] = function() {
  return J2ME.newString(this.str.toUpperCase());
};

Override["java/lang/String.trim.()Ljava/lang/String;"] = function() {
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

  return J2ME.newString(this.str.substring(start, end));
};

Override["java/lang/String.toString.()Ljava/lang/String;"] = function() {
  return this; // Note: returning "this" so that we keep the same object.
};

Override["java/lang/String.toCharArray.()[C"] = function() {
  return util.stringToCharArray(this.str);
};

//****************************************************************
// String.valueOf() for various types

// NOTE: String.valueOf(Object) left in Java to avoid having to call
// back into Java for Object.toString().

Override["java/lang/String.valueOf.([C)Ljava/lang/String;"] = function(chars) {
  if (!chars) {
    throw $.newNullPointerException();
  }
  return J2ME.newString(util.fromJavaChars(chars));
};

Override["java/lang/String.valueOf.([CII)Ljava/lang/String;"] = function(chars, offset, count) {
  if (!chars) {
    throw $.newNullPointerException();
  }
  return J2ME.newString(util.fromJavaChars(chars, offset, count));
};

Override["java/lang/String.valueOf.(Z)Ljava/lang/String;"] = function(bool) {
  return J2ME.newString(bool ? "true" : "false");
};

Override["java/lang/String.valueOf.(C)Ljava/lang/String;"] = function(ch) {
  return J2ME.newString(String.fromCharCode(ch));
};

Override["java/lang/String.valueOf.(I)Ljava/lang/String;"] = function(n) {
  return J2ME.newString(n.toString());
};

Override["java/lang/String.valueOf.(J)Ljava/lang/String;"] = function(n) {
  return J2ME.newString(n.toString());
};


// String.valueOf(float) and String.valueOf(double) have been left in
// Java for now, as they require support for complex formatting rules.
// Additionally, their tests check for coverage of nuanced things like
// positive zero vs. negative zero, which we don't currently support.

var internedStrings = J2ME.internedStrings;

Native["java/lang/String.intern.()Ljava/lang/String;"] = function() {
    var string = util.fromJavaString(this);

    var internedString = internedStrings.get(string);

    if (internedString) {
        return internedString;
    } else {
        internedStrings.set(string, this);
        return this;
    }
};



//################################################################
// java.lang.StringBuffer (manipulated via the 'buf' property)

Override["java/lang/StringBuffer.<init>.()V"] = function() {
  this.buf = new Uint16Array(16); // Initial buffer size: 16, per the Java implementation.
  this.count = 0;
};

Override["java/lang/StringBuffer.<init>.(I)V"] = function(length) {
  if (length < 0) {
    throw $.newNegativeArraySizeException();
  }
  this.buf = new Uint16Array(length);
  this.count = 0;
};

Override["java/lang/StringBuffer.<init>.(Ljava/lang/String;)V"] = function(jStr) {
  var stringBuf = util.stringToCharArray(jStr.str);
  this.buf = new Uint16Array(stringBuf.length + 16); // Add 16, per the Java implementation.
  this.buf.set(stringBuf, 0);
  this.count = stringBuf.length;
};

Override["java/lang/StringBuffer.length.()I"] = function() {
  return this.count;
};

Override["java/lang/StringBuffer.capacity.()I"] = function() {
  return this.buf.length;
};

Override["java/lang/StringBuffer.copy.()V"] = function() {
  // We don't support copying (there's no need unless we also support shared buffers).
};

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

Override["java/lang/StringBuffer.ensureCapacity.(I)V"] = function(minCapacity) {
  if (this.buf.length < minCapacity) {
    expandCapacity.call(this, minCapacity);
  }
};

// StringBuffer.expandCapacity is private and not needed with these overrides.

Override["java/lang/StringBuffer.setLength.(I)V"] = function(newLength) {
  if (newLength < 0) {
    throw $.newStringIndexOutOfBoundsException();
  }

  if (newLength > this.buf.length) {
    expandCapacity.call(this, newLength);
  }
  for (; this.count < newLength; this.count++) {
    this.buf[this.count] = '\0';
  }
  this.count = newLength;
};


Override["java/lang/StringBuffer.charAt.(I)C"] = function(index) {
  if (index < 0 || index >= this.count) {
    throw $.newStringIndexOutOfBoundsException();
  }
  return this.buf[index];
};

Override["java/lang/StringBuffer.getChars.(II[CI)V"] = function(srcBegin, srcEnd, dst, dstBegin) {
  if (srcBegin < 0 || srcEnd < 0 || srcEnd > this.count || srcBegin > srcEnd) {
    throw $.newStringIndexOutOfBoundsException();
  }
  if (dstBegin + (srcEnd - srcBegin) > dst.length || dstBegin < 0) {
    throw $.newArrayIndexOutOfBoundsException();
  }
  dst.set(this.buf.subarray(srcBegin, srcEnd), dstBegin);
};

Override["java/lang/StringBuffer.setCharAt.(IC)V"] = function(index, ch) {
  if (index < 0 || index >= this.count) {
    throw $.newStringIndexOutOfBoundsException();
  }
  this.buf[index] = ch;
};


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
    throw $.newNullPointerException();
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

Override["java/lang/StringBuffer.append.(Ljava/lang/String;)Ljava/lang/StringBuffer;"] = function(jStr) {
  return stringBufferAppend.call(this, jStr ? jStr.str : "null");
};

Override["java/lang/StringBuffer.append.([C)Ljava/lang/StringBuffer;"] = function(chars) {
  if (chars == null) {
    throw $.newNullPointerException();
  }
  return stringBufferAppend.call(this, chars);
};

Override["java/lang/StringBuffer.append.([CII)Ljava/lang/StringBuffer;"] = function(chars, offset, length) {
  if (chars == null) {
    throw $.newNullPointerException();
  }
  if (offset < 0 || offset + length > chars.length) {
    throw $.newArrayIndexOutOfBoundsException();
  }
  return stringBufferAppend.call(this, chars.subarray(offset, offset + length));
};

Override["java/lang/StringBuffer.append.(Z)Ljava/lang/StringBuffer;"] = function(bool) {
  return stringBufferAppend.call(this, bool ? "true" : "false");
};

Override["java/lang/StringBuffer.append.(C)Ljava/lang/StringBuffer;"] = function(ch) {
  if (this.buf.length < this.count + 1) {
    expandCapacity.call(this, this.count + 1);
  }
  this.buf[this.count++] = ch;
  return this;
};

Override["java/lang/StringBuffer.append.(I)Ljava/lang/StringBuffer;"] = function(n) {
  return stringBufferAppend.call(this, n + "");
};

Override["java/lang/StringBuffer.append.(J)Ljava/lang/StringBuffer;"] = function(n) {
  return stringBufferAppend.call(this, n + "");
};

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
function stringBufferDelete(start, end) {
  if (start < 0) {
    throw $.newStringIndexOutOfBoundsException();
  }
  if (end > this.count) {
    end = this.count;
  }
  if (start > end) {
    throw $.newStringIndexOutOfBoundsException();
  }

  var len = end - start;
  if (len > 0) {
    // When Gecko 34 is released, we can use TypedArray.copyWithin() instead.
    this.buf.set(this.buf.subarray(end, this.count), start);
    this.count -= len;
  }
  return this;
}

Override["java/lang/StringBuffer.delete.(II)Ljava/lang/StringBuffer;"] = stringBufferDelete;

Override["java/lang/StringBuffer.deleteCharAt.(I)Ljava/lang/StringBuffer;"] = function(index) {
  if (index >= this.count) {
    // stringBufferDelete handles the other boundary checks; this check is specific to deleteCharAt.
    throw $.newStringIndexOutOfBoundsException();
  }
  return stringBufferDelete.call(this, index, index + 1);
};

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
    throw $.newNullPointerException();
  }
  if (offset < 0 || offset > this.count) {
    throw $.newArrayIndexOutOfBoundsException();
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

Override["java/lang/StringBuffer.insert.(ILjava/lang/String;)Ljava/lang/StringBuffer;"] = function(offset, jStr) {
  return stringBufferInsert.call(this, offset, jStr ? jStr.str : "null");
};

Override["java/lang/StringBuffer.insert.(I[C)Ljava/lang/StringBuffer;"] = function(offset, chars) {
  return stringBufferInsert.call(this, offset, chars);
};

Override["java/lang/StringBuffer.insert.(IZ)Ljava/lang/StringBuffer;"] = function(offset, bool) {
  return stringBufferInsert.call(this, offset, bool ? "true" : "false");
};

Override["java/lang/StringBuffer.insert.(IC)Ljava/lang/StringBuffer;"] = function(offset, ch) {
  return stringBufferInsert.call(this, offset, String.fromCharCode(ch));
};

Override["java/lang/StringBuffer.insert.(II)Ljava/lang/StringBuffer;"] = function(offset, n) {
  return stringBufferInsert.call(this, offset, n + "");
};

Override["java/lang/StringBuffer.insert.(IJ)Ljava/lang/StringBuffer;"] = function(offset, n) {
  return stringBufferInsert.call(this, offset, n + "");
};

// StringBuffer.insert(float) left in Java.

// StringBuffer.insert(double) left in Java.

Override["java/lang/StringBuffer.reverse.()Ljava/lang/StringBuffer;"] = function() {
  var buf = this.buf;
  for (var i = 0, j = this.count - 1; i < j; i++, j--) {
    var tmp = buf[i];
    buf[i] = buf[j];
    buf[j] = tmp;
  }
  return this;
};

Override["java/lang/StringBuffer.toString.()Ljava/lang/String;"] = function() {
  return J2ME.newString(util.fromJavaChars(this.buf, 0, this.count));
};

Override["java/lang/StringBuffer.setShared.()V"] = function() {
  // Our StringBuffers are never shared. Everyone gets their very own!
};

Override["java/lang/StringBuffer.getValue.()[C"] = function() {
  // In theory, this method should only be called by String (which
  // we've overridden to not do), so it should never be called. In any
  // case, mutating this buf would have the same effect here as it
  // would in Java.
  return this.buf;
};
