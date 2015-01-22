/*
 * Copyright 2014 Mozilla Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

var jsGlobal = (function() { return this || (1, eval)('this'); })();
var inBrowser = typeof console !== "undefined" && console.info;

declare var putstr;
declare var printErr;

/** @const */ var release: boolean = true;
/** @const */ var profile: boolean = true;

declare var dateNow: () => number;

declare var dump: (message: string) => void;

function dumpLine(s) {
  if (typeof dump !== "undefined") {
    dump(s + "\n");
  }
}

if (!jsGlobal.performance) {
  jsGlobal.performance = {};
}

if (!jsGlobal.performance.now) {
  jsGlobal.performance.now = typeof dateNow !== 'undefined' ? dateNow : Date.now;
}

function log(message?: any, ...optionalParams: any[]): void {
  if (inBrowser) {
    console.log.apply(console, arguments);
  } else {
    jsGlobal.print.apply(jsGlobal, arguments);
  }
}

function warn(message?: any, ...optionalParams: any[]): void {
  if (inBrowser) {
    console.warn.apply(console, arguments);
  } else {
    jsGlobal.print(J2ME.IndentingWriter.YELLOW + message + J2ME.IndentingWriter.ENDC);
  }
}

interface String {
  padRight(c: string, n: number): string;
  padLeft(c: string, n: number): string;
  endsWith(s: string): boolean;
}

interface Function {
  boundTo: boolean;
}

interface Math {
  imul(a: number, b: number): number;
  /**
   * Returns the number of leading zeros of a number.
   * @param x A numeric expression.
   */
  clz32(x: number): number;
}

interface Error {
  stack: string;
}

interface Uint8ClampedArray extends ArrayBufferView {
  BYTES_PER_ELEMENT: number;
  length: number;
  [index: number]: number;
  get(index: number): number;
  set(index: number, value: number): void;
  set(array: Uint8Array, offset?: number): void;
  set(array: number[], offset?: number): void;
  subarray(begin: number, end?: number): Uint8ClampedArray;
}

declare var Uint8ClampedArray: {
  prototype: Uint8ClampedArray;
  new (length: number): Uint8ClampedArray;
  new (array: Uint8Array): Uint8ClampedArray;
  new (array: number[]): Uint8ClampedArray;
  new (buffer: ArrayBuffer, byteOffset?: number, length?: number): Uint8ClampedArray;
  BYTES_PER_ELEMENT: number;
}

module J2ME {

  export function isIdentifierStart(c) {
    return (c === '$') || (c === '_') || (c === '\\') || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
  }

  export function isIdentifierPart(c) {
    return (c === '$') || (c === '_') || (c === '\\') || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || ((c >= '0') && (c <= '9'));
  }

  export function isIdentifierName(s) {
    if (!isIdentifierStart(s[0])) {
      return false;
    }
    for (var i = 1; i < s.length; i++) {
      if (!isIdentifierPart(s[i])) {
        return false;
      }
    }
    return true;
  }

  export enum CharacterCodes {
    _0 = 48,
    _1 = 49,
    _2 = 50,
    _3 = 51,
    _4 = 52,
    _5 = 53,
    _6 = 54,
    _7 = 55,
    _8 = 56,
    _9 = 57
  }

  /**
   * The buffer length required to contain any unsigned 32-bit integer.
   */
  /** @const */ export var UINT32_CHAR_BUFFER_LENGTH = 10; // "4294967295".length;
  /** @const */ export var UINT32_MAX = 0xFFFFFFFF;
  /** @const */ export var UINT32_MAX_DIV_10 = 0x19999999; // UINT32_MAX / 10;
  /** @const */ export var UINT32_MAX_MOD_10 = 0x5; // UINT32_MAX % 10

  export function isString(value): boolean {
    return typeof value === "string";
  }

  export function isFunction(value): boolean {
    return typeof value === "function";
  }

  export function isNumber(value): boolean {
    return typeof value === "number";
  }

  export function isInteger(value): boolean {
    return (value | 0) === value;
  }

  export function isArray(value): boolean {
    return value instanceof Array;
  }

  export function isNumberOrString(value): boolean {
    return typeof value === "number" || typeof value === "string";
  }

  export function isObject(value): boolean {
    return typeof value === "object" || typeof value === 'function';
  }

  export function toNumber(x): number {
    return +x;
  }

  export function isNumericString(value: string): boolean {
    // ECMAScript 5.1 - 9.8.1 Note 1, this expression is true for all
    // numbers x other than -0.
    return String(Number(value)) === value;
  }

  /**
   * Whether the specified |value| is a number or the string representation of a number.
   */
  export function isNumeric(value: any): boolean {
    if (typeof value === "number") {
      return true;
    }
    if (typeof value === "string") {
      // |value| is rarely numeric (it's usually an identifier), and the
      // isIndex()/isNumericString() pair is slow and expensive, so we do a
      // quick check for obvious non-numericalness first. Just checking if the
      // first char is a 7-bit identifier char catches most cases.
      var c = value.charCodeAt(0);
      if ((65 <= c && c <= 90) ||     // 'A'..'Z'
          (97 <= c && c <= 122) ||    // 'a'..'z'
          (c === 36) ||               // '$'
          (c === 95)) {               // '_'
        return false;
      }
      return isIndex(value) || isNumericString(value);
    }
    // Debug.notImplemented(typeof value);
    return false;
  }

  /**
   * Whether the specified |value| is an unsigned 32 bit number expressed as a number
   * or string.
   */
  export function isIndex(value: any): boolean {
    // js/src/vm/String.cpp JSFlatString::isIndexSlow
    // http://dxr.mozilla.org/mozilla-central/source/js/src/vm/String.cpp#474
    var index = 0;
    if (typeof value === "number") {
      index = (value | 0);
      if (value === index && index >= 0) {
        return true;
      }
      return value >>> 0 === value;
    }
    if (typeof value !== "string") {
      return false;
    }
    var length = value.length;
    if (length === 0) {
      return false;
    }
    if (value === "0") {
      return true;
    }
    // Is there any way this will fit?
    if (length > UINT32_CHAR_BUFFER_LENGTH) {
      return false;
    }
    var i = 0;
    index = value.charCodeAt(i++) - CharacterCodes._0;
    if (index < 1 || index > 9) {
      return false;
    }
    var oldIndex = 0;
    var c = 0;
    while (i < length) {
      c = value.charCodeAt(i++) - CharacterCodes._0;
      if (c < 0 || c > 9) {
        return false;
      }
      oldIndex = index;
      index = 10 * index + c;
    }
    /*
     * Look out for "4294967296" and larger-number strings that fit in UINT32_CHAR_BUFFER_LENGTH.
     * Only unsigned 32-bit integers shall pass.
     */
    if ((oldIndex < UINT32_MAX_DIV_10) || (oldIndex === UINT32_MAX_DIV_10 && c <= UINT32_MAX_MOD_10)) {
      return true;
    }
    return false;
  }

  export function isNullOrUndefined(value) {
    return value == undefined;
  }

  export module Debug {
    export function backtrace() {
      try {
        throw new Error();
      } catch (e) {
        return e.stack ? e.stack.split('\n').slice(2).join('\n') : '';
      }
    }

    export function error(message: string) {
      throw new Error(message);
    }

    export function assert(condition: any, message: any = "assertion failed") {
      if (condition === "") {     // avoid inadvertent false positive
        condition = true;
      }
      if (!condition) {
        Debug.error(message.toString());
      }
    }

    export function assertUnreachable(msg: string): void {
      var location = new Error().stack.split('\n')[1];
      throw new Error("Reached unreachable location " + location + msg);
    }

    export function assertNotImplemented(condition: boolean, message: string) {
      if (!condition) {
        Debug.error("notImplemented: " + message);
      }
    }

    export function warning(message: string) {
      release || warn(message);
    }

    export function notUsed(message: string) {
      release || Debug.assert(false, "Not Used " + message);
    }

    export function notImplemented(message: string) {
      release || Debug.assert(false, "Not Implemented " + message);
    }

    export function abstractMethod(message: string) {
      Debug.assert(false, "Abstract Method " + message);
    }

    var somewhatImplementedCache = {};

    export function somewhatImplemented(message: string) {
      if (somewhatImplementedCache[message]) {
        return;
      }
      somewhatImplementedCache[message] = true;
      Debug.warning("somewhatImplemented: " + message);
    }

    export function unexpected(message?: any) {
      Debug.assert(false, "Unexpected: " + message);
    }

    export function untested(message?: any) {
      Debug.warning("Congratulations, you've found a code path for which we haven't found a test case. Please submit the test case: " + message);
    }
  }

  export function getTicks(): number {
    return performance.now();
  }

  export module ArrayUtilities {
    import assert = Debug.assert;

    export function makeArrays(length: number): any [][] {
      var arrays = [];
      for (var i = 0; i < length; i ++) {
        arrays.push(new Array(i));
      }
      return arrays;
    }

    /**
     * Pops elements from a source array into a destination array. This avoids
     * allocations and should be faster. The elements in the destination array
     * are pushed in the same order as they appear in the source array:
     *
     * popManyInto([1, 2, 3], 2, dst) => dst = [2, 3]
     */
    export function popManyInto(src: any [], count: number, dst: any []) {
      release || assert(src.length >= count);
      for (var i = count - 1; i >= 0; i--) {
        dst[i] = src.pop();
      }
      dst.length = count;
    }

    export function popMany<T>(array: T [], count: number): T [] {
      release || assert(array.length >= count);
      var start = array.length - count;
      var result = array.slice(start, this.length);
      array.splice(start, count);
      return result;
    }

    /**
     * Just deletes several array elements from the end of the list.
     */
    export function popManyIntoVoid(array: any [], count: number) {
      release || assert(array.length >= count);
      array.length = array.length - count;
    }

    export function pushMany(dst: any [], src: any []) {
      for (var i = 0; i < src.length; i++) {
        dst.push(src[i]);
      }
    }

    export function top(array: any []) {
      return array.length && array[array.length - 1]
    }

    export function last(array: any []) {
      return array.length && array[array.length - 1]
    }

    export function peek(array: any []) {
      release || assert(array.length > 0);
      return array[array.length - 1];
    }

    export function indexOf<T>(array: T [], value: T): number {
      for (var i = 0, j = array.length; i < j; i++) {
        if (array[i] === value) {
          return i;
        }
      }
      return -1;
    }

    export function pushUnique<T>(array: T [], value: T): number {
      for (var i = 0, j = array.length; i < j; i++) {
        if (array[i] === value) {
          return i;
        }
      }
      array.push(value);
      return array.length - 1;
    }

    export function unique<T>(array: T []): T [] {
      var result = [];
      for (var i = 0; i < array.length; i++) {
        pushUnique(result, array[i]);
      }
      return result;
    }

    export function copyFrom(dst: any [], src: any []) {
      dst.length = 0;
      ArrayUtilities.pushMany(dst, src);
    }

    /**
     * Makes sure that a typed array has the requested capacity. If required, it creates a new
     * instance of the array's class with a power-of-two capacity at least as large as required.
     *
     * Note: untyped because generics with constraints are pretty annoying.
     */
    export function ensureTypedArrayCapacity(array: any, capacity: number): any {
      if (array.length < capacity) {
        var oldArray = array;
        array = new array.constructor(IntegerUtilities.nearestPowerOfTwo(capacity));
        array.set(oldArray, 0);
      }
      return array;
    }

  }

  export module ObjectUtilities {
    export function boxValue(value) {
      if (isNullOrUndefined(value) || isObject(value)) {
        return value;
      }
      return Object(value);
    }

    export function toKeyValueArray(object: Object) {
      var hasOwnProperty = Object.prototype.hasOwnProperty;
      var array = [];
      for (var k in object) {
        if (hasOwnProperty.call(object, k)) {
          array.push([k, object[k]]);
        }
      }
      return array;
    }

    export function isPrototypeWriteable(object: Object) {
      return Object.getOwnPropertyDescriptor(object, "prototype").writable;
    }

    export function hasOwnProperty(object: Object, name: string): boolean {
      return Object.prototype.hasOwnProperty.call(object, name);
    }

    export function propertyIsEnumerable(object: Object, name: string): boolean {
      return Object.prototype.propertyIsEnumerable.call(object, name);
    }

    export function getOwnPropertyDescriptor(object: Object, name: string): PropertyDescriptor {
      return Object.getOwnPropertyDescriptor(object, name);
    }

    export function hasOwnGetter(object: Object, name: string): boolean {
      var d = Object.getOwnPropertyDescriptor(object, name);
      return !!(d && d.get);
    }

    export function getOwnGetter(object: Object, name: string): () => any {
      var d = Object.getOwnPropertyDescriptor(object, name);
      return d ? d.get : null;
    }

    export function hasOwnSetter(object: Object, name: string): boolean {
      var d = Object.getOwnPropertyDescriptor(object, name);
      return !!(d && !!d.set);
    }

    export function createObject(prototype: Object) {
      return Object.create(prototype);
    }

    export function createEmptyObject() {
      return Object.create(null);
    }

    export function createMap<K, V>(): Map<K, V> {
      return Object.create(null);
    }

    export function createArrayMap<K, V>(): Map<K, V> {
      return <Map<K, V>><any>[];
    }

    export function defineReadOnlyProperty(object: Object, name: string, value: any) {
      Object.defineProperty(object, name, {
        value: value,
        writable: false,
        configurable: true,
        enumerable: false
      });
    }

    export function getOwnPropertyDescriptors(object: Object): Map<string, PropertyDescriptor> {
      var o = ObjectUtilities.createMap<string, PropertyDescriptor>();
      var properties = Object.getOwnPropertyNames(object);
      for (var i = 0; i < properties.length; i++) {
        o[properties[i]] = Object.getOwnPropertyDescriptor(object, properties[i]);
      }
      return o;
    }

    export function cloneObject(object: Object): Object {
      var clone = Object.create(Object.getPrototypeOf(object));
      copyOwnProperties(clone, object);
      return clone;
    }

    export function copyProperties(object: Object, template: Object) {
      for (var property in template) {
        object[property] = template[property];
      }
    }

    export function copyOwnProperties(object: Object, template: Object) {
      for (var property in template) {
        if (hasOwnProperty(template, property)) {
          object[property] = template[property];
        }
      }
    }

    export function copyOwnPropertyDescriptors(object: Object, template: Object, overwrite = true) {
      for (var property in template) {
        if (hasOwnProperty(template, property)) {
          var descriptor = Object.getOwnPropertyDescriptor(template, property);
          if (!overwrite && hasOwnProperty(object, property)) {
            continue
          }
          release || Debug.assert (descriptor);
          try {
            Object.defineProperty(object, property, descriptor);
          } catch (e) {
            // log("Can't define " + property);
          }
        }
      }
    }

    export function getLatestGetterOrSetterPropertyDescriptor(object, name) {
      var descriptor: PropertyDescriptor = {};
      while (object) {
        var tmp = Object.getOwnPropertyDescriptor(object, name);
        if (tmp) {
          descriptor.get = descriptor.get || tmp.get;
          descriptor.set = descriptor.set || tmp.set;
        }
        if (descriptor.get && descriptor.set) {
          break;
        }
        object = Object.getPrototypeOf(object);
      }
      return descriptor;
    }

    export function defineNonEnumerableGetterOrSetter(obj, name, value, isGetter) {
      var descriptor = ObjectUtilities.getLatestGetterOrSetterPropertyDescriptor(obj, name);
      descriptor.configurable = true;
      descriptor.enumerable = false;
      if (isGetter) {
        descriptor.get = value;
      } else {
        descriptor.set = value;
      }
      Object.defineProperty(obj, name, descriptor);
    }

    export function defineNonEnumerableGetter(obj, name, getter) {
      Object.defineProperty(obj, name, { get: getter,
        configurable: true,
        enumerable: false
      });
    }

    export function defineNonEnumerableSetter(obj, name, setter) {
      Object.defineProperty(obj, name, { set: setter,
        configurable: true,
        enumerable: false
      });
    }

    export function defineNonEnumerableProperty(obj, name, value) {
      Object.defineProperty(obj, name, { value: value,
        writable: true,
        configurable: true,
        enumerable: false
      });
    }

    export function defineNonEnumerableForwardingProperty(obj, name, otherName) {
      Object.defineProperty(obj, name, {
        get: FunctionUtilities.makeForwardingGetter(otherName),
        set: FunctionUtilities.makeForwardingSetter(otherName),
        configurable: true,
        enumerable: false
      });
    }

    export function defineNewNonEnumerableProperty(obj, name, value) {
      release || Debug.assert (!Object.prototype.hasOwnProperty.call(obj, name), "Property: " + name + " already exits.");
      ObjectUtilities.defineNonEnumerableProperty(obj, name, value);
    }
  }

  export module FunctionUtilities {
    export function makeForwardingGetter(target: string): () => any {
      return <() => any> new Function("return this[\"" + target + "\"]");
    }

    export function makeForwardingSetter(target: string): (any) => void {
      return <(any) => void> new Function("value", "this[\"" + target + "\"] = value;");
    }
  }

  export module StringUtilities {
    import assert = Debug.assert;

    export function repeatString(c: string, n: number): string {
      var s = "";
      for (var i = 0; i < n; i++) {
        s += c;
      }
      return s;
    }

    export function memorySizeToString(value: number) {
      value |= 0;
      var K = 1024;
      var M = K * K;
      if (value < K) {
        return value + " B";
      } else if (value < M) {
        return (value / K).toFixed(2) + "KB";
      } else {
        return (value / M).toFixed(2) + "MB";
      }
    }

    /**
     * Returns a reasonably sized description of the |value|, to be used for debugging purposes.
     */
    export function toSafeString(value) {
      if (typeof value === "string") {
        return "\"" + value + "\"";
      }
      if (typeof value === "number" || typeof value === "boolean") {
        return String(value);
      }
      if (value instanceof Array) {
        return "[] " + value.length;
      }
      return typeof value;
    }

    export function toSafeArrayString(array) {
      var str = [];
      for (var i = 0; i < array.length; i++) {
        str.push(toSafeString(array[i]));
      }
      return str.join(", ");
    }

    export function escapeString(str: string) {
      if (str !== undefined) {
        str = str.replace(/[^\w$]/gi,"$"); /* No dots, colons, dashes and /s */
        if (/^\d/.test(str)) { /* No digits at the beginning */
          str = '$' + str;
        }
      }
      return str;
    }

    export function quote(s: string): string {
      return "\"" + s + "\"";
    }

    var json = false;
    var escapeless = false;
    var hexadecimal = false;
    var renumber = false;
    var quotes = "double";

    function stringToArray(str) {
      var length = str.length,
        result = [],
        i;
      for (i = 0; i < length; ++i) {
        result[i] = str.charAt(i);
      }
      return result;
    }

    function escapeAllowedCharacter(ch, next) {
      var code = ch.charCodeAt(0), hex = code.toString(16), result = '\\';

      switch (ch) {
        case '\b':
          result += 'b';
          break;
        case '\f':
          result += 'f';
          break;
        case '\t':
          result += 't';
          break;
        default:
          if (json || code > 0xff) {
            result += 'u' + '0000'.slice(hex.length) + hex;
          } else if (ch === '\u0000' && '0123456789'.indexOf(next) < 0) {
            result += '0';
          } else if (ch === '\x0B') { // '\v'
            result += 'x0B';
          } else {
            result += 'x' + '00'.slice(hex.length) + hex;
          }
          break;
      }

      return result;
    }

    function escapeDisallowedCharacter(ch) {
      var result = '\\';
      switch (ch) {
        case '\\':
          result += '\\';
          break;
        case '\n':
          result += 'n';
          break;
        case '\r':
          result += 'r';
          break;
        case '\u2028':
          result += 'u2028';
          break;
        case '\u2029':
          result += 'u2029';
          break;
        default:
          throw new Error('Incorrectly classified character');
      }

      return result;
    }

    var escapeStringCacheCount = 0;
    var escapeStringCache = Object.create(null);

    export function escapeStringLiteral(str) {
      var result, i, len, ch, singleQuotes = 0, doubleQuotes = 0, single, original = str;
      result = escapeStringCache[original];
      if (result) {
        return result;
      }
      if (escapeStringCacheCount === 1024) {
        escapeStringCache = Object.create(null);
        escapeStringCacheCount = 0;
      }
      result = '';

      if (typeof str[0] === 'undefined') {
        str = stringToArray(str);
      }

      for (i = 0, len = str.length; i < len; ++i) {
        ch = str[i];
        if (ch === '\'') {
          ++singleQuotes;
        } else if (ch === '"') {
          ++doubleQuotes;
        } else if (ch === '/' && json) {
          result += '\\';
        } else if ('\\\n\r\u2028\u2029'.indexOf(ch) >= 0) {
          result += escapeDisallowedCharacter(ch);
          continue;
        } else if ((json && ch < ' ') || !(json || escapeless || (ch >= ' ' && ch <= '~'))) {
          result += escapeAllowedCharacter(ch, str[i + 1]);
          continue;
        }
        result += ch;
      }

      single = !(quotes === 'double' || (quotes === 'auto' && doubleQuotes < singleQuotes));
      str = result;
      result = single ? '\'' : '"';

      if (typeof str[0] === 'undefined') {
        str = stringToArray(str);
      }

      for (i = 0, len = str.length; i < len; ++i) {
        ch = str[i];
        if ((ch === '\'' && single) || (ch === '"' && !single)) {
          result += '\\';
        }
        result += ch;
      }

      result += (single ? '\'' : '"');
      escapeStringCache[original] = result;
      escapeStringCacheCount ++;
      return result;
    }

    /**
     * Workaround for max stack size limit.
     */
    export function fromCharCodeArray(buffer: Uint8Array): string {
      var str = "", SLICE = 1024 * 16;
      for (var i = 0; i < buffer.length; i += SLICE) {
        var chunk = Math.min(buffer.length - i, SLICE);
        str += String.fromCharCode.apply(null, buffer.subarray(i, i + chunk));
      }
      return str;
    }

    var _encoding = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789$_';
    var arrays = [null, new Array(1), new Array(2), new Array(3), new Array(4), new Array(5), new Array(6)];
    export function variableLengthEncodeInt32(n) {
      var e = _encoding;
      var bitCount = (32 - Math.clz32(n));
      release || assert (bitCount <= 32, bitCount);
      var l = Math.ceil(bitCount / 6);
      var a = arrays[l];
      // Encode length followed by six bit chunks.
      a[0] = e[l];
      for (var i = l - 1, j = 1; i >= 0; i--) {
        var offset = (i * 6);
        a[j++] = e[(n >> offset) & 0x3F];
      }
      var s = a.join("");
      return s;
    }

    export function toEncoding(n) {
      return _encoding[n];
    }

    export function fromEncoding(s) {
      var c = s.charCodeAt(0);
      var e = 0;
      if (c >= 65 && c <= 90) {
        return c - 65;
      } else if (c >= 97 && c <= 122) {
        return c - 71;
      } else if (c >= 48 && c <= 57) {
        return c + 4;
      } else if (c === 36) {
        return 62;
      } else if (c === 95) {
        return 63;
      }
      release || assert (false, "Invalid Encoding");
    }

    export function variableLengthDecodeInt32(s) {
      var l = StringUtilities.fromEncoding(s[0]);
      var n = 0;
      for (var i = 0; i < l; i++) {
        var offset = ((l - i - 1) * 6);
        n |= StringUtilities.fromEncoding(s[1 + i]) << offset;
      }
      return n;
    }

    export function trimMiddle(s: string, maxLength: number): string {
      if (s.length <= maxLength) {
        return s;
      }
      var leftHalf = maxLength >> 1;
      var rightHalf = maxLength - leftHalf - 1;
      return s.substr(0, leftHalf) + "\u2026" + s.substr(s.length - rightHalf, rightHalf);
    }

    export function multiple(s: string, count: number): string {
      var o = "";
      for (var i = 0; i < count; i++) {
        o += s;
      }
      return o;
    }

    export function indexOfAny(s: string, chars: string [], position: number) {
      var index = s.length;
      for (var i = 0; i < chars.length; i++) {
        var j = s.indexOf(chars[i], position);
        if (j >= 0) {
          index = Math.min(index, j);
        }
      }
      return index === s.length ? -1 : index;
    }

    var _concat3array = new Array(3);
    var _concat4array = new Array(4);
    var _concat5array = new Array(5);
    var _concat6array = new Array(6);
    var _concat7array = new Array(7);
    var _concat8array = new Array(8);
    var _concat9array = new Array(9);

    /**
     * The concatN() functions concatenate multiple strings in a way that
     * avoids creating intermediate strings, unlike String.prototype.concat().
     *
     * Note that these functions don't have identical behaviour to using '+',
     * because they will ignore any arguments that are |undefined| or |null|.
     * This usually doesn't matter.
     */

    export function concat3(s0: any, s1: any, s2: any) {
        _concat3array[0] = s0;
        _concat3array[1] = s1;
        _concat3array[2] = s2;
        return _concat3array.join('');
    }

    export function concat4(s0: any, s1: any, s2: any, s3: any) {
        _concat4array[0] = s0;
        _concat4array[1] = s1;
        _concat4array[2] = s2;
        _concat4array[3] = s3;
        return _concat4array.join('');
    }

    export function concat5(s0: any, s1: any, s2: any, s3: any, s4: any) {
        _concat5array[0] = s0;
        _concat5array[1] = s1;
        _concat5array[2] = s2;
        _concat5array[3] = s3;
        _concat5array[4] = s4;
        return _concat5array.join('');
    }
  }

  export module HashUtilities {
    export function hashBytesTo32BitsAdler(data: Uint8Array, offset: number, length: number): number {
      var a = 1;
      var b = 0;
      var end = offset + length;
      for (var i = offset; i < end; ++i) {
        a = (a + (data[i] & 0xff)) % 65521;
        b = (b + a) % 65521;
      }
      return (b << 16) | a;
    }
  }

  /**
   * Marsaglia's algorithm, adapted from V8. Use this if you want a deterministic random number.
   */
  export class Random {
    private static _state: Uint32Array = new Uint32Array([0xDEAD, 0xBEEF]);

    public static seed(seed: number) {
      Random._state[0] = seed;
      Random._state[1] = seed;
    }

    public static next(): number {
      var s = this._state;
      var r0 = (Math.imul(18273, s[0] & 0xFFFF) + (s[0] >>> 16)) | 0;
      s[0] = r0;
      var r1 = (Math.imul(36969, s[1] & 0xFFFF) + (s[1] >>> 16)) | 0;
      s[1] = r1;
      var x = ((r0 << 16) + (r1 & 0xFFFF)) | 0;
      // Division by 0x100000000 through multiplication by reciprocal.
      return (x < 0 ? (x + 0x100000000) : x) * 2.3283064365386962890625e-10;
    }
  }

  Math.random = function random(): number {
    return Random.next();
  };

  export module NumberUtilities {
    export function pow2(exponent: number): number {
      if (exponent === (exponent | 0)) {
        if (exponent < 0) {
          return 1 / (1 << -exponent);
        }
        return 1 << exponent;
      }
      return Math.pow(2, exponent);
    }

    export function clamp(value: number, min: number, max: number) {
      return Math.max(min, Math.min(max, value));
    }

    /**
     * Rounds *.5 to the nearest even number.
     * See https://en.wikipedia.org/wiki/Rounding#Round_half_to_even for details.
     */
    export function roundHalfEven(value: number): number {
      if (Math.abs(value % 1) === 0.5) {
        var floor = Math.floor(value);
        return floor % 2 === 0 ? floor : Math.ceil(value);
      }
      return Math.round(value);
    }

    export function epsilonEquals(value: number, other: number): boolean {
      return Math.abs(value - other) < 0.0000001;
    }
  }

  export enum Numbers {
    MaxU16 = 0xFFFF,
    MaxI16 = 0x7FFF,
    MinI16 = -0x8000
  }

  export module IntegerUtilities {
    var sharedBuffer = new ArrayBuffer(8);
    export var i8 = new Int8Array(sharedBuffer);
    export var u8 = new Uint8Array(sharedBuffer);
    export var i32 = new Int32Array(sharedBuffer);
    export var f32 = new Float32Array(sharedBuffer);
    export var f64 = new Float64Array(sharedBuffer);
    export var nativeLittleEndian = new Int8Array(new Int32Array([1]).buffer)[0] === 1;

    /**
     * Convert a float into 32 bits.
     */
    export function floatToInt32(v: number) {
      f32[0] = v; return i32[0];
    }

    /**
     * Convert 32 bits into a float.
     */
    export function int32ToFloat(i: number) {
      i32[0] = i; return f32[0];
    }

    /**
     * Swap the bytes of a 16 bit number.
     */
    export function swap16(i: number) {
      return ((i & 0xFF) << 8) | ((i >> 8) & 0xFF);
    }

    /**
     * Swap the bytes of a 32 bit number.
     */
    export function swap32(i: number) {
      return ((i & 0xFF) << 24) | ((i & 0xFF00) << 8) | ((i >> 8) & 0xFF00) | ((i >> 24) & 0xFF);
    }

    /**
     * Converts a number to s8.u8 fixed point representation.
     */
    export function toS8U8(v: number) {
      return ((v * 256) << 16) >> 16;
    }

    /**
     * Converts a number from s8.u8 fixed point representation.
     */
    export function fromS8U8(i: number) {
      return i / 256;
    }

    /**
     * Round trips a number through s8.u8 conversion.
     */
    export function clampS8U8(v: number) {
      return fromS8U8(toS8U8(v));
    }

    /**
     * Converts a number to signed 16 bits.
     */
    export function toS16(v: number) {
      return (v << 16) >> 16;
    }

    export function bitCount(i: number): number {
      i = i - ((i >> 1) & 0x55555555);
      i = (i & 0x33333333) + ((i >> 2) & 0x33333333);
      return (((i + (i >> 4)) & 0x0F0F0F0F) * 0x01010101) >> 24;
    }

    export function ones(i: number): number {
      i = i - ((i >> 1) & 0x55555555);
      i = (i & 0x33333333) + ((i >> 2) & 0x33333333);
      return ((i + (i >> 4) & 0xF0F0F0F) * 0x1010101) >> 24;
    }

    export function trailingZeros(i: number): number {
      return IntegerUtilities.ones((i & -i) - 1);
    }

    export function getFlags(i: number, flags: string[]): string {
      var str = "";
      for (var i = 0; i < flags.length; i++) {
        if (i & (1 << i)) {
          str += flags[i] + " ";
        }
      }
      if (str.length === 0) {
        return "";
      }
      return str.trim();
    }

    export function isPowerOfTwo(x: number) {
      return x && ((x & (x - 1)) === 0);
    }

    export function roundToMultipleOfFour(x: number) {
      return (x + 3) & ~0x3;
    }

    export function nearestPowerOfTwo(x: number) {
      x --;
      x |= x >> 1;
      x |= x >> 2;
      x |= x >> 4;
      x |= x >> 8;
      x |= x >> 16;
      x ++;
      return x;
    }

    export function roundToMultipleOfPowerOfTwo(i: number, powerOfTwo: number) {
      var x = (1 << powerOfTwo) - 1;
      return (i + x) & ~x; // Round up to multiple of power of two.
    }

    /**
     * Polyfill imul.
     */
    if (!Math.imul) {
      Math.imul = function imul(a, b) {
        var ah  = (a >>> 16) & 0xffff;
        var al = a & 0xffff;
        var bh  = (b >>> 16) & 0xffff;
        var bl = b & 0xffff;
        // the shift by 0 fixes the sign on the high part
        // the final |0 converts the unsigned value into a signed value
        return ((al * bl) + (((ah * bl + al * bh) << 16) >>> 0) | 0);
      }
    }

    /**
     * Polyfill clz32.
     */
    if (!Math.clz32) {
      Math.clz32 = function clz32(i: number) {
        i |= (i >> 1);
        i |= (i >> 2);
        i |= (i >> 4);
        i |= (i >> 8);
        i |= (i >> 16);
        return 32 - IntegerUtilities.ones(i);
      }
    }
  }

  export enum LogLevel {
    Error = 0x1,
    Warn = 0x2,
    Debug = 0x4,
    Log = 0x8,
    Info = 0x10,
    All = 0x1f
  }

  export class IndentingWriter {
    public static PURPLE = '\033[94m';
    public static YELLOW = '\033[93m';
    public static GREEN = '\033[92m';
    public static RED = '\033[91m';
    public static BOLD_RED = '\033[1;91m';
    public static ENDC = '\033[0m';

    public static logLevel: LogLevel = LogLevel.All;

    public static stdout = inBrowser ? console.info.bind(console) : print;
    public static stdoutNoNewline = inBrowser ? console.info.bind(console) : putstr;
    public static stderr = inBrowser ? console.error.bind(console) : printErr;

    private _tab: string;
    private _padding: string;
    private _suppressOutput: boolean;
    private _out: (s: string) => void;
    private _outNoNewline: (s: string) => void;

    constructor(suppressOutput: boolean = false, out?) {
      this._tab = " ";
      this._padding = "";
      this._suppressOutput = suppressOutput;
      this._out = out || IndentingWriter.stdout;
      this._outNoNewline = out || IndentingWriter.stdoutNoNewline;
    }

    write(str: string = "", writePadding = false) {
      if (!this._suppressOutput) {
        this._outNoNewline((writePadding ? this._padding : "") + str);
      }
    }

    writeLn(str: string = "") {
      if (!this._suppressOutput) {
        this._out(this._padding + str);
      }
    }

    writeTimeLn(str: string = "") {
      if (!this._suppressOutput) {
        this._out(this._padding + performance.now().toFixed(2) + " " + str);
      }
    }

    writeComment(str: string) {
      var lines = str.split("\n");
      if (lines.length === 1) {
        this.writeLn("// " + lines[0]);
      } else {
        this.writeLn("/**");
        for (var i = 0; i < lines.length; i++) {
          this.writeLn(" * " + lines[i]);
        }
        this.writeLn(" */");
      }
    }

    writeLns(str: string) {
      var lines = str.split("\n");
      for (var i = 0; i < lines.length; i++) {
        this.writeLn(lines[i]);
      }
    }

    errorLn(str: string) {
      if (IndentingWriter.logLevel & LogLevel.Error) {
        this.boldRedLn(str);
      }
    }

    warnLn(str: string) {
      if (IndentingWriter.logLevel & LogLevel.Warn) {
        this.yellowLn(str);
      }
    }

    debugLn(str: string) {
      if (IndentingWriter.logLevel & LogLevel.Debug) {
        this.purpleLn(str);
      }
    }

    logLn(str: string) {
      if (IndentingWriter.logLevel & LogLevel.Log) {
        this.writeLn(str);
      }
    }

    infoLn(str: string) {
      if (IndentingWriter.logLevel & LogLevel.Info) {
        this.writeLn(str);
      }
    }

    yellowLn(str: string) {
      this.colorLn(IndentingWriter.YELLOW, str);
    }

    greenLn(str: string) {
      this.colorLn(IndentingWriter.GREEN, str);
    }

    boldRedLn(str: string) {
      this.colorLn(IndentingWriter.BOLD_RED, str);
    }

    redLn(str: string) {
      this.colorLn(IndentingWriter.RED, str);
    }

    purpleLn(str: string) {
      this.colorLn(IndentingWriter.PURPLE, str);
    }

    colorLn(color: string, str: string) {
      if (!this._suppressOutput) {
        if (!inBrowser) {
          this._out(this._padding + color + str + IndentingWriter.ENDC);
        } else {
          this._out(this._padding + str);
        }
      }
    }

    redLns(str: string) {
      this.colorLns(IndentingWriter.RED, str);
    }

    colorLns(color: string, str: string) {
      var lines = str.split("\n");
      for (var i = 0; i < lines.length; i++) {
        this.colorLn(color, lines[i]);
      }
    }

    enter(str: string) {
      if (!this._suppressOutput) {
        this._out(this._padding + str);
      }
      this.indent();
    }

    leaveAndEnter(str: string) {
      this.leave(str);
      this.indent();
    }

    leave(str: string) {
      this.outdent();
      if (!this._suppressOutput) {
        this._out(this._padding + str);
      }
    }

    indent() {
      this._padding += this._tab;
    }

    outdent() {
      if (this._padding.length > 0) {
        this._padding = this._padding.substring(0, this._padding.length - this._tab.length);
      }
    }

    writeArray(arr: any[], detailed: boolean = false, noNumbers: boolean = false) {
      detailed = detailed || false;
      for (var i = 0, j = arr.length; i < j; i++) {
        var prefix = "";
        if (detailed) {
          if (arr[i] === null) {
            prefix = "null";
          } else if (arr[i] === undefined) {
            prefix = "undefined";
          } else {
            prefix = arr[i].constructor.name;
          }
          prefix += " ";
        }
        var number = noNumbers ? "" : ("" + i).padRight(' ', 4);
        this.writeLn(number + prefix + arr[i]);
      }
    }
  }

  /**
   * Insertion sort SortedList backed by a linked list.
   */
  class SortedListNode<T> {
    value: T;
    next: SortedListNode<T>;
    constructor(value: T, next: SortedListNode<T>) {
      this.value = value;
      this.next = next;
    }
  }

  export class SortedList<T>  {
    public static RETURN = 1;
    public static DELETE = 2;
    private _compare: (l: T, r: T) => number;
    private _head: SortedListNode<T>;
    private _length: number;

    constructor (compare: (l: T, r: T) => number) {
      release || Debug.assert(compare);
      this._compare = compare;
      this._head = null;
      this._length = 0;
    }

    public push(value: T) {
      release || Debug.assert(value !== undefined);
      this._length ++;
      if (!this._head) {
        this._head = new SortedListNode<T>(value, null);
        return;
      }

      var curr = this._head;
      var prev = null;
      var node = new SortedListNode<T>(value, null);
      var compare = this._compare;
      while (curr) {
        if (compare(curr.value, node.value) > 0) {
          if (prev) {
            node.next = curr;
            prev.next = node;
          } else {
            node.next = this._head;
            this._head = node;
          }
          return;
        }
        prev = curr;
        curr = curr.next;
      }
      prev.next = node;
    }

    /**
     * Visitors can return RETURN if they wish to stop the iteration or DELETE if they need to delete the current node.
     * NOTE: DELETE most likley doesn't work if there are multiple active iterations going on.
     */
    public forEach(visitor: (value: T) => any) {
      var curr = this._head;
      var last = null;
      while (curr) {
        var result = visitor(curr.value);
        if (result === SortedList.RETURN) {
          return;
        } else if (result === SortedList.DELETE) {
          if (!last) {
            curr = this._head = this._head.next;
          } else {
            curr = last.next = curr.next;
          }
        } else {
          last = curr;
          curr = curr.next;
        }
      }
    }

    public isEmpty(): boolean {
      return !this._head;
    }

    public pop(): T {
      if (!this._head) {
        return undefined;
      }
      this._length --;
      var ret = this._head;
      this._head = this._head.next;
      return ret.value;
    }

    public contains(value: T): boolean {
      var curr = this._head;
      while (curr) {
        if (curr.value === value) {
          return true;
        }
        curr = curr.next;
      }
      return false;
    }

    public toString(): string {
      var str = "[";
      var curr = this._head;
      while (curr) {
        str += curr.value.toString();
        curr = curr.next;
        if (curr) {
          str += ",";
        }
      }
      str += "]";
      return str;
    }
  }

  export module BitSets {
    import assert = Debug.assert;

    export var ADDRESS_BITS_PER_WORD = 5;
    export var BITS_PER_WORD = 1 << ADDRESS_BITS_PER_WORD;
    export var BIT_INDEX_MASK = BITS_PER_WORD - 1;

    function getSize(length): number {
      return ((length + (BITS_PER_WORD - 1)) >> ADDRESS_BITS_PER_WORD) << ADDRESS_BITS_PER_WORD;
    }

    export interface BitSet {
      set: (i) => void;
      setAll: () => void;
      assign: (set: BitSet) => void;
      clear: (i: number) => void;
      get: (i: number) => boolean;
      clearAll: () => void;
      intersect: (other: BitSet) => void;
      subtract: (other: BitSet) => void;
      negate: () => void;
      forEach: (fn) => void;
      toArray: () => boolean [];
      equals: (other: BitSet) => boolean;
      contains: (other: BitSet) => boolean;
      isEmpty: () => boolean;
      clone: () => BitSet;
      recount: () => void;
      toString: (names: string []) => string;
      toBitString: (on: string, off: string) => string;
    }

    function toBitString(on: string, off: string) {
      var self: BitSet = this;
      on = on || "1";
      off = off || "0";
      var str = "";
      for (var i = 0; i < length; i++) {
        str += self.get(i) ? on : off;
      }
      return str;
    }

    function toString(names: any[]) {
      var self: BitSet = this;
      var set = [];
      for (var i = 0; i < length; i++) {
        if (self.get(i)) {
          set.push(names ? names[i] : i);
        }
      }
      return set.join(", ");
    }

    export class Uint32ArrayBitSet implements BitSet {
      size: number;
      bits: Uint32Array;
      count: number;
      dirty: number;
      length: number;

      constructor(length: number) {
        this.size = getSize(length);
        this.count = 0;
        this.dirty = 0;
        this.length = length;
        this.bits = new Uint32Array(this.size >> ADDRESS_BITS_PER_WORD);
      }

      recount() {
        if (!this.dirty) {
          return;
        }

        var bits = this.bits;
        var c = 0;
        for (var i = 0, j = bits.length; i < j; i++) {
          var v = bits[i];
          v = v - ((v >> 1) & 0x55555555);
          v = (v & 0x33333333) + ((v >> 2) & 0x33333333);
          c += ((v + (v >> 4) & 0xF0F0F0F) * 0x1010101) >> 24;
        }

        this.count = c;
        this.dirty = 0;
      }

      set(i) {
        var n = i >> ADDRESS_BITS_PER_WORD;
        var old = this.bits[n];
        var b = old | (1 << (i & BIT_INDEX_MASK));
        this.bits[n] = b;
        this.dirty |= old ^ b;
      }

      setAll() {
        var bits = this.bits;
        for (var i = 0, j = bits.length; i < j; i++) {
          bits[i] = 0xFFFFFFFF;
        }
        this.count = this.size;
        this.dirty = 0;
      }

      assign(set) {
        this.count = set.count;
        this.dirty = set.dirty;
        this.size = set.size;
        for (var i = 0, j = this.bits.length; i < j; i++) {
          this.bits[i] = set.bits[i];
        }
      }

      nextSetBit(from: number, to: number): number {
        if (from === to) {
          return -1;
        }
        var bits = this.bits;
        for (var i = from; i < to; i++) {
          var word = bits[i >> ADDRESS_BITS_PER_WORD];
          if (((word & 1 << (i & BIT_INDEX_MASK))) !== 0) {
            return i;
          }
        }
      }

      clear(i) {
        var n = i >> ADDRESS_BITS_PER_WORD;
        var old = this.bits[n];
        var b = old & ~(1 << (i & BIT_INDEX_MASK));
        this.bits[n] = b;
        this.dirty |= old ^ b;
      }

      get(i): boolean {
        var word = this.bits[i >> ADDRESS_BITS_PER_WORD];
        return ((word & 1 << (i & BIT_INDEX_MASK))) !== 0;
      }


      clearAll() {
        var bits = this.bits;
        for (var i = 0, j = bits.length; i < j; i++) {
          bits[i] = 0;
        }
        this.count = 0;
        this.dirty = 0;
      }

      private _union(other: Uint32ArrayBitSet) {
        var dirty = this.dirty;
        var bits = this.bits;
        var otherBits = other.bits;
        for (var i = 0, j = bits.length; i < j; i++) {
          var old = bits[i];
          var b = old | otherBits[i];
          bits[i] = b;
          dirty |= old ^ b;
        }
        this.dirty = dirty;
      }

      intersect(other: Uint32ArrayBitSet) {
        var dirty = this.dirty;
        var bits = this.bits;
        var otherBits = other.bits;
        for (var i = 0, j = bits.length; i < j; i++) {
          var old = bits[i];
          var b = old & otherBits[i];
          bits[i] = b;
          dirty |= old ^ b;
        }
        this.dirty = dirty;
      }

      subtract(other: Uint32ArrayBitSet) {
        var dirty = this.dirty;
        var bits = this.bits;
        var otherBits = other.bits;
        for (var i = 0, j = bits.length; i < j; i++) {
          var old = bits[i];
          var b = old & ~otherBits[i];
          bits[i] = b;
          dirty |= old ^ b;
        }
        this.dirty = dirty;
      }

      negate() {
        var dirty = this.dirty;
        var bits = this.bits;
        for (var i = 0, j = bits.length; i < j; i++) {
          var old = bits[i];
          var b = ~old;
          bits[i] = b;
          dirty |= old ^ b;
        }
        this.dirty = dirty;
      }

      forEach(fn) {
        release || assert(fn);
        var bits = this.bits;
        for (var i = 0, j = bits.length; i < j; i++) {
          var word = bits[i];
          if (word) {
            for (var k = 0; k < BITS_PER_WORD; k++) {
              if (word & (1 << k)) {
                fn(i * BITS_PER_WORD + k);
              }
            }
          }
        }
      }

      toArray(): boolean[] {
        var set = [];
        var bits = this.bits;
        for (var i = 0, j = bits.length; i < j; i++) {
          var word = bits[i];
          if (word) {
            for (var k = 0; k < BITS_PER_WORD; k++) {
              if (word & (1 << k)) {
                set.push(i * BITS_PER_WORD + k);
              }
            }
          }
        }
        return set;
      }

      equals(other: Uint32ArrayBitSet) {
        if (this.size !== other.size) {
          return false;
        }
        var bits = this.bits;
        var otherBits = other.bits;
        for (var i = 0, j = bits.length; i < j; i++) {
          if (bits[i] !== otherBits[i]) {
            return false;
          }
        }
        return true;
      }

      contains(other: Uint32ArrayBitSet) {
        if (this.size !== other.size) {
          return false;
        }
        var bits = this.bits;
        var otherBits = other.bits;
        for (var i = 0, j = bits.length; i < j; i++) {
          if ((bits[i] | otherBits[i]) !== bits[i]) {
            return false;
          }
        }
        return true;
      }

      toBitString: (on: string, off: string) => string;
      toString: (names: string []) => string;

      isEmpty(): boolean {
        this.recount();
        return this.count === 0;
      }

      clone(): Uint32ArrayBitSet {
        var set = new Uint32ArrayBitSet(this.length);
        set._union(this);
        return set;
      }
    }

    export class Uint32BitSet implements BitSet {
      size: number;
      bits: number;
      count: number;
      dirty: number;
      singleWord: boolean;
      length: number;
      constructor(length: number) {
        this.count = 0;
        this.dirty = 0;
        this.size = getSize(length);
        this.bits = 0;
        this.singleWord = true;
        this.length = length;
      }

      recount() {
        if (!this.dirty) {
          return;
        }

        var c = 0;
        var v = this.bits;
        v = v - ((v >> 1) & 0x55555555);
        v = (v & 0x33333333) + ((v >> 2) & 0x33333333);
        c += ((v + (v >> 4) & 0xF0F0F0F) * 0x1010101) >> 24;

        this.count = c;
        this.dirty = 0;
      }

      set(i) {
        var old = this.bits;
        var b = old | (1 << (i & BIT_INDEX_MASK));
        this.bits = b;
        this.dirty |= old ^ b;
      }

      setAll() {
        this.bits = 0xFFFFFFFF;
        this.count = this.size;
        this.dirty = 0;
      }

      assign(set: Uint32BitSet) {
        this.count = set.count;
        this.dirty = set.dirty;
        this.size = set.size;
        this.bits = set.bits;
      }

      clear(i: number) {
        var old = this.bits;
        var b = old & ~(1 << (i & BIT_INDEX_MASK));
        this.bits = b;
        this.dirty |= old ^ b;
      }

      get(i: number): boolean {
        return ((this.bits & 1 << (i & BIT_INDEX_MASK))) !== 0;
      }

      clearAll() {
        this.bits = 0;
        this.count = 0;
        this.dirty = 0;
      }

      private _union(other: Uint32BitSet) {
        var old = this.bits;
        var b = old | other.bits;
        this.bits = b;
        this.dirty = old ^ b;
      }

      intersect(other: Uint32BitSet) {
        var old = this.bits;
        var b = old & other.bits;
        this.bits = b;
        this.dirty = old ^ b;
      }

      subtract(other: Uint32BitSet) {
        var old = this.bits;
        var b = old & ~other.bits;
        this.bits = b;
        this.dirty = old ^ b;
      }

      negate() {
        var old = this.bits;
        var b = ~old;
        this.bits = b;
        this.dirty = old ^ b;
      }

      forEach(fn) {
        release || assert(fn);
        var word = this.bits;
        if (word) {
          for (var k = 0; k < BITS_PER_WORD; k++) {
            if (word & (1 << k)) {
              fn(k);
            }
          }
        }
      }

      toArray(): boolean [] {
        var set = [];
        var word = this.bits;
        if (word) {
          for (var k = 0; k < BITS_PER_WORD; k++) {
            if (word & (1 << k)) {
              set.push(k);
            }
          }
        }
        return set;
      }

      equals(other: Uint32BitSet) {
        return this.bits === other.bits;
      }

      contains(other: Uint32BitSet) {
        var bits = this.bits;
        return (bits | other.bits) === bits;
      }

      toBitString: (on: string, off: string) => string;
      toString: (names: string []) => string;

      isEmpty(): boolean {
        this.recount();
        return this.count === 0;
      }

      clone(): Uint32BitSet {
        var set = new Uint32BitSet(this.length);
        set._union(this);
        return set;
      }
    }

    Uint32BitSet.prototype.toString = toString;
    Uint32BitSet.prototype.toBitString = toBitString;
    Uint32ArrayBitSet.prototype.toString = toString;
    Uint32ArrayBitSet.prototype.toBitString = toBitString;

    export function BitSetFunctor(length: number) {
      var shouldUseSingleWord = (getSize(length) >> ADDRESS_BITS_PER_WORD) === 1;
      var type = (shouldUseSingleWord ? <any>Uint32BitSet : <any>Uint32ArrayBitSet);
      return function () {
        return new type(length);
      }
    }
  }

  /**
   * Simple pool allocator for ArrayBuffers. This reduces memory usage in data structures
   * that resize buffers.
   */
  export class ArrayBufferPool {
    private _list: ArrayBuffer [];
    private _maxSize: number;
    private static _enabled = true;

    /**
     * Creates a pool that manages a pool of a |maxSize| number of array buffers.
     */
    constructor(maxSize: number = 32) {
      this._list = [];
      this._maxSize = maxSize;
    }

    /**
     * Creates or reuses an existing array buffer that is at least the
     * specified |length|.
     */
    public acquire(length: number): ArrayBuffer {
      if (ArrayBufferPool._enabled) {
        var list = this._list;
        for (var i = 0; i < list.length; i++) {
          var buffer = list[i];
          if (buffer.byteLength >= length) {
            list.splice(i, 1);
            return buffer;
          }
        }
      }
      return new ArrayBuffer(length);
    }

    /**
     * Releases an array buffer that is no longer needed back to the pool.
     */
    public release(buffer: ArrayBuffer) {
      if (ArrayBufferPool._enabled) {
        var list = this._list;
        release || Debug.assert(ArrayUtilities.indexOf(list, buffer) < 0);
        if (list.length === this._maxSize) {
          list.shift();
        }
        list.push(buffer);
      }
    }

    /**
     * Resizes a Uint8Array to have the given length.
     */
    public ensureUint8ArrayLength(array: Uint8Array, length: number): Uint8Array {
      if (array.length >= length) {
        return array;
      }
      var newLength = Math.max(array.length + length, ((array.length * 3) >> 1) + 1);
      var newArray = new Uint8Array(this.acquire(newLength), 0, newLength);
      newArray.set(array);
      this.release(array.buffer);
      return newArray;
    }

    /**
     * Resizes a Float64Array to have the given length.
     */
    public ensureFloat64ArrayLength(array: Float64Array, length: number): Float64Array {
      if (array.length >= length) {
        return array;
      }
      var newLength = Math.max(array.length + length, ((array.length * 3) >> 1) + 1);
      var newArray = new Float64Array(this.acquire(newLength * Float64Array.BYTES_PER_ELEMENT), 0, newLength);
      newArray.set(array);
      this.release(array.buffer);
      return newArray;
    }
  }
}

/**
 * Extend builtin prototypes.
 *
 * TODO: Go through the code and remove all references to these.
 */
(function () {
  function extendBuiltin(prototype, property, value) {
    if (!prototype[property]) {
      Object.defineProperty(prototype, property,
        { value: value,
          writable: true,
          configurable: true,
          enumerable: false });
    }
  }

  function removeColors(s) {
    return s.replace(/\033\[[0-9]*m/g, "");
  }

  extendBuiltin(String.prototype, "padRight", function (c, n) {
    var str = this;
    var length = removeColors(str).length;
    if (!c || length >= n) {
      return str;
    }
    var max = (n - length) / c.length;
    for (var i = 0; i < max; i++) {
      str += c;
    }
    return str;
  });

  extendBuiltin(String.prototype, "padLeft", function (c, n) {
    var str = this;
    var length = str.length;
    if (!c || length >= n) {
      return str;
    }
    var max = (n - length) / c.length;
    for (var i = 0; i < max; i++) {
      str = c + str;
    }
    return str;
  });

  extendBuiltin(String.prototype, "trim", function () {
    return this.replace(/^\s+|\s+$/g,"");
  });

  extendBuiltin(String.prototype, "endsWith", function (str) {
    return this.indexOf(str, this.length - str.length) !== -1;
  });

  extendBuiltin(Array.prototype, "replace", function(x, y) {
    if (x === y) {
      return 0;
    }
    var count = 0;
    for (var i = 0; i < this.length; i++) {
      if (this[i] === x) {
        this[i] = y;
        count ++;
      }
    }
    return count;
  });

})();
