/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

var util = (function () {
  var formatRegExp = /%[sdj%]/g;
  function format(f) {
    if (typeof f !== "string") {
      var objects = [];
      for (var i = 0; i < arguments.length; i++) {
        objects.push(inspect(arguments[i]));
      }
      return objects.join(' ');
    }

    var i = 1;
    var args = arguments;
    var len = args.length;
    var str = String(f).replace(formatRegExp, function(x) {
        if (x === '%%') return '%';
        if (i >= len) return x;
        switch (x) {
        case '%s': return String(args[i++]);
        case '%d': return Number(args[i++]);
        case '%j':
          try {
            return JSON.stringify(args[i++]);
          } catch (_) {
            return '[Circular]';
          }
        default:
          return x;
        }
      });
    for (var x = args[i]; i < len; x = args[++i]) {
      if (isNull(x) || !isObject(x)) {
        str += ' ' + x;
      } else {
        str += ' ' + inspect(x);
      }
    }
    return str;
  };

  function inspect(v) {
    return "" + v;
  }

  function inherits(ctor, superCtor) {
    ctor.super_ = superCtor;
    ctor.prototype = Object.create(superCtor.prototype, {
        constructor: {
          value: ctor,
          enumerable: false,
          writable: true,
          configurable: true
        }
    });
  };

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
      return gLong.ZERO;
    if (type[0] === '[' || type[0] === 'L')
      return null;
    return 0;
  }

  function double2float(d) {
    if (a > 3.40282346638528860e+38)
      return Number.POSITIVE_INFINITY;
    if (0 < a < 1.40129846432481707e-45)
      return 0;
    if (a < -3.40282346638528860e+38)
      return Number.NEGATIVE_INFINITY;
    if (0 > a > -1.40129846432481707e-45)
      return 0;
    return a;
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
      return gLong.MAX_VALUE;
    if (d === Number.NEGATIVE_INFINITY)
      return gLong.MIN_VALUE;
    return gLong.fromNumber(d);
  }

  return {
    inherits: inherits,
    format: format,
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
    INT_MAX: INT_MAX,
    INT_MIN: INT_MIN
  };
})();
