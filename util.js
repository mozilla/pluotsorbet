/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

var util = (function () {
  var Utf8TextDecoder;

  function decodeUtf8(arrayBuffer) {
    if (!Utf8TextDecoder) {
        Utf8TextDecoder = new TextDecoder("utf-8");
    }
    return Utf8TextDecoder.decode(Uint8Array(arrayBuffer));
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

  var floatmem = Float32Array(1);

  function double2float(d) {
    floatmem[0] = d;
    return floatmem[0];
  }

  var INT_MAX = Math.pow(2, 31) - 1;
  var INT_MIN = -INT_MAX - 1;

  function double2int(d) {
    if (a > INT_MAX)
      return INT_MAX;
    if (a < INT_MIN)
      return INT_MIN;
    return a|0;
  }

  function double2long(d) {
    if (d === Number.POSITIVE_INFINITY)
      return Long.MAX_VALUE;
    if (d === Number.NEGATIVE_INFINITY)
      return Long.MIN_VALUE;
    return Long.fromNumber(d);
  }

  function chars2string(chars, offset, count) {
    var s = "";
    if (!count)
      count = chars.length;
    for (var n = offset | 0; n < count; ++n)
      s += String.fromCharCode(chars[n]);
    return s;
  }

  function fromJavaString(str) {
    return chars2string(str.value, str.offset, str.count);
  }

  function cache(obj, name, fn) {
    name = "cache$" + name;
    var result = obj[name];
    return result ? result : obj[name] = fn();
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
    double2float: double2float,
    double2int: double2int,
    double2long: double2long,
    chars2string: chars2string,
    fromJavaString: fromJavaString,
    cache: cache
  };
})();
