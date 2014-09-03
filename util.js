/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

var util = (function () {
  var Utf8TextDecoder;

  function decodeUtf8(arrayBuffer) {
    if (!Utf8TextDecoder) {
      Utf8TextDecoder = new TextDecoder("utf-8");
    }
    return Utf8TextDecoder.decode(new Uint8Array(arrayBuffer));
  }

  var out = "";
  function print(s) {
    out += s;
    var n;
    while ((n = out.indexOf("\n")) != -1) {
      console.log(out.substr(0, n));
      out = out.substr(n+1);
    }
  }

  function defaultValue(type) {
    if (type === 'J')
      return Long.ZERO;
    if (type[0] === '[' || type[0] === 'L')
      return null;
    return 0;
  }

  var INT_MAX = Math.pow(2, 31) - 1;
  var INT_MIN = -INT_MAX - 1;

  function double2int(d) {
    if (d > INT_MAX)
      return INT_MAX;
    if (d < INT_MIN)
      return INT_MIN;
    return d|0;
  }

  function double2long(d) {
    if (d === Number.POSITIVE_INFINITY)
      return Long.MAX_VALUE;
    if (d === Number.NEGATIVE_INFINITY)
      return Long.MIN_VALUE;
    return Long.fromNumber(d);
  }

  function fromJavaChars(chars, offset, count) {
    var s = "";
    if (!count)
      count = chars.length;
    if (!offset)
      offset = 0;
    for (var n = 0; n < count; ++n)
      s += String.fromCharCode(chars[offset+n]);
    return s;      
  }

  function fromJavaString(str) {
    if (!str)
      return null;
    var chars = CLASSES.java_lang_String.getField("value", "[C").get(str);
    var offset = CLASSES.java_lang_String.getField("offset", "I").get(str);
    var count = CLASSES.java_lang_String.getField("count", "I").get(str);
    return fromJavaChars(chars, offset, count);
  }

  var id = (function() {
    var gen = 0;
    return function() {
      return ++gen;
    }
  })();

  function tag(obj) {
    if (!obj.tag)
      obj.tag = id();
    return obj.tag;
  }

  /**
   * Compare two typed arrays, returning *true* if they have the same length
   * and values, *false* otherwise.  Note that we compare by value, not by byte,
   * so:
   *     compareTypedArrays(new Uint8Array([0x00, 0xFF]), new Uint8Array[0x00, 0xFF])
   * returns *true*;
   *
   * and:
   *     compareTypedArrays(new Uint8Array([0x00, 0xFF]), new Uint32Array[0x00000000, 0x000000FF])
   * also returns *true*;
   *
   * but:
   *     compareTypedArrays(new Uint8Array([0x00, 0xFF]), new Uint16Array([0x00FF]))
   * returns *false*.
   */
  function compareTypedArrays(ary1, ary2) {
    if (ary1.length != ary2.length) {
      return false;
    }

    for (var i = 0; i < ary1.length; i++) {
      if (ary1[i] !== ary2[i]) {
        return false;
      }
    }

    return true;
  }

  return {
    INT_MAX: INT_MAX,
    INT_MIN: INT_MIN,
    print: print,
    debug: console.info.bind(console),
    error: console.error.bind(console),
    info: console.info.bind(console),
    warn: console.warn.bind(console),
    decodeUtf8: decodeUtf8,
    defaultValue: defaultValue,
    double2int: double2int,
    double2long: double2long,
    fromJavaChars: fromJavaChars,
    fromJavaString: fromJavaString,
    id: id,
    tag: tag,
    compareTypedArrays: compareTypedArrays,
  };
})();
