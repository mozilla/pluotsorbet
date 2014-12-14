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

  /**
   * Provides a UTF-8 decoder that will throw an exception on error rather
   * than silently sanitizing the output.
   */
  var fallibleUtf8Decoder = new TextDecoder("utf-8", { fatal: true });

  /**
   * Decodes a UTF-8 string stored in an ArrayBufferView.
   *
   * @param arr An ArrayBufferView to decode (such as a Uint8Array).
   * @returns The decoded string.
   * @throws An invalid enoding is encountered, see
   *         TextDecoder.prototype.decode().
   */
  function decodeUtf8Array(arr) {
    return fallibleUtf8Decoder.decode(arr);
  }

  // Decode Java's modified UTF-8 (JVM specs, $ 4.4.7)
  // http://docs.oracle.com/javase/specs/jvms/se5.0/html/ClassFile.doc.html#7963
  function javaUTF8Decode(arr) {
    var str = '';
    var i = 0;
    while (i < arr.length) {
        var x = arr[i++];

        if (x <= 0x7f) {
            // Code points in the range '\u0001' to '\u007F' are represented by a
            // single byte.
            // The 7 bits of data in the byte give the value of the code point
            // represented.
            str += String.fromCharCode(x);
        } else if (x <= 0xdf) {
            // The null code point ('\u0000') and code points in the range '\u0080'
            // to '\u07FF' are represented by a pair of bytes x and y.
            var y = arr[i++];
            str += String.fromCharCode(((x & 0x1f) << 6) + (y & 0x3f));
        } else {
            // Code points in the range '\u0800' to '\uFFFF' are represented by 3
            // bytes x, y, and z.
            var y = arr[i++];
            var z = arr[i++];
            str += String.fromCharCode(((x & 0xf) << 12) + ((y & 0x3f) << 6) + (z & 0x3f));
        }
    }

    return str;
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

  var jStringEncoder = new TextEncoder('utf-16');
  var jStringDecoder = new TextDecoder('utf-16');

  function fromJavaChars(chars, offset, count) {
    if (!chars) {
      return null;
    }
    if (typeof count !== 'number')
      count = chars.length;
    if (typeof offset !== 'number')
      offset = 0;
    return jStringDecoder.decode(chars.subarray(offset, offset + count));
  }

  function fromJavaString(jStr) {
    if (!jStr)
      return null;
    return jStr.str;
  }

  function newPrimitiveArray(type, size) {
    var constructor = ARRAYS[type];
    if (!constructor.prototype.class)
      CLASSES.initPrimitiveArrayType(type, constructor);
    return new constructor(size);
  }

  function newArray(typeName, size) {
    return new (CLASSES.getClass(typeName).constructor)(size);
  }

  function newMultiArray(typeName, lengths) {
    var length = lengths[0];
    var array = newArray(typeName, length);
    if (lengths.length > 1) {
      lengths = lengths.slice(1);
      for (var i=0; i<length; i++)
        array[i] = newMultiArray(typeName.substr(1), lengths);
    }
    return array;
  }

  function newObject(classInfo) {
      return new (classInfo.constructor)();
  }

  function newString(s) {
    var obj = newObject(CLASSES.java_lang_String);
    obj.str = s;
    return obj;
  }

  /**
   * Returns an ArrayBufferView of the underlying code points
   * represented by the given Java string.
   *
   * NOTE: Do not modify the ArrayBuffer; it may be shared between
   * multiple string instances.
   */
  function stringToCharArray(str) {
    return new Uint16Array(jStringEncoder.encode(str).buffer);
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

  function pad(num, len) {
    return "0".repeat(len - num.toString().length) + num;
  }

  function toCodePointArray(str) {
    var chars = [];

    var str = str.slice();

    while (str.length > 0) {
      var ucsChars = String.fromCodePoint(str.codePointAt(0));
      chars.push(ucsChars);
      str = str.substr(ucsChars.length);
    }

    return chars;
  }

  return {
    INT_MAX: INT_MAX,
    INT_MIN: INT_MIN,
    decodeUtf8: decodeUtf8,
    decodeUt8Array: decodeUtf8Array,
    javaUTF8Decode: javaUTF8Decode,
    defaultValue: defaultValue,
    double2int: double2int,
    double2long: double2long,
    fromJavaChars: fromJavaChars,
    fromJavaString: fromJavaString,
    newPrimitiveArray: newPrimitiveArray,
    newArray: newArray,
    newMultiArray: newMultiArray,
    newObject: newObject,
    newString: newString,
    stringToCharArray: stringToCharArray,
    id: id,
    tag: tag,
    compareTypedArrays: compareTypedArrays,
    pad: pad,
    toCodePointArray: toCodePointArray,
  };
})();
