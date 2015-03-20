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

declare var dateNow: () => number;

if (!jsGlobal.performance) {
  jsGlobal.performance = {};
}

if (!jsGlobal.performance.now) {
  jsGlobal.performance.now = typeof dateNow !== 'undefined' ? dateNow : Date.now;
}

interface String {
  padRight(c: string, n: number): string;
  padLeft(c: string, n: number): string;
  endsWith(s: string): boolean;
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

  export function isObject(value): boolean {
    return typeof value === "object" || typeof value === 'function';
  }

  export function isNullOrUndefined(value) {
    return value == undefined;
  }

  export module Debug {
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

    export function abstractMethod(message: string) {
      Debug.assert(false, "Abstract Method " + message);
    }

    export function unexpected(message?: any) {
      Debug.assert(false, "Unexpected: " + message);
    }
  }

  export function getTicks(): number {
    return performance.now();
  }

  export module ArrayUtilities {
    import assert = Debug.assert;
    
    export var EMPTY_ARRAY = [];

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

    export function pushMany(dst: any [], src: any []) {
      for (var i = 0; i < src.length; i++) {
        dst.push(src[i]);
      }
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

    /**
     * You'd hope that new Array() triggers different heuristics about how and when it should fall back
     * to dictionary mode. I've experienced ION bailouts from non-dense new Arrays(), hence this helper
     * method.
     */
    export function makeDenseArray(length, value) {
      var array = new Array(length);
      for (var i = 0; i < length; i++) {
        array[i] = value;
      }
      return array;
    }
  }

  export module ObjectUtilities {
    export function createMap<K, V>(): Map<K, V> {
      return Object.create(null);
    }
  }

  export module FunctionUtilities {
    export function makeForwardingGetter(target: string): () => any {
      return <() => any> new Function("return this[\"" + target + "\"]");
    }

    export function makeForwardingSetter(target: string): (any) => void {
      return <(any) => void> new Function("value", "this[\"" + target + "\"] = value;");
    }

    export function makeDebugForwardingSetter(target: string, checker: (x: any) => boolean): (any) => void {
      return function (value) {
        Debug.assert(checker(value), "Unexpected value for target " + target);
        this[target] = value;
      }
    }
  }

  export module StringUtilities {
    import assert = Debug.assert;

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

    var _concat3array = new Array(3);

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
  }

  export module HashUtilities {
    // Copyright (c) 2011 Gary Court
    //
    // Permission is hereby granted, free of charge, to any person obtaining
    // a copy of this software and associated documentation files (the
    // "Software"), to deal in the Software without restriction, including
    // without limitation the rights to use, copy, modify, merge, publish,
    // distribute, sublicense, and/or sell copies of the Software, and to
    // permit persons to whom the Software is furnished to do so, subject to
    // the following conditions:
    //
    // The above copyright notice and this permission notice shall be
    // included in all copies or substantial portions of the Software.
    //
    // THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
    // EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
    // MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
    // IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
    // CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
    // TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
    // SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

    // https://github.com/garycourt/murmurhash-js
    export function hashBytesTo32BitsMurmur(data: Uint8Array, offset: number, length: number) {
      var l = length, h = 0x12345678 ^ l, i = offset, k;
      while (l >= 4) {
        k =
          (data[i]) |
          (data[++i] << 8) |
          (data[++i] << 16) |
          (data[++i] << 24);
        k = (((k & 0xffff) * 0x5bd1e995) + ((((k >>> 16) * 0x5bd1e995) & 0xffff) << 16));
        k ^= k >>> 24;
        k = (((k & 0xffff) * 0x5bd1e995) + ((((k >>> 16) * 0x5bd1e995) & 0xffff) << 16));
        h = (((h & 0xffff) * 0x5bd1e995) + ((((h >>> 16) * 0x5bd1e995) & 0xffff) << 16)) ^ k;
        l -= 4;
        ++i;
      }
      switch (l) {
        case 3: h ^= data[i + 2] << 16;
        case 2: h ^= data[i + 1] <<  8;
        case 1: h ^= data[i];
          h = (((h & 0xffff) * 0x5bd1e995) + ((((h >>> 16) * 0x5bd1e995) & 0xffff) << 16));
      }
      h ^= h >>> 13;
      h = (((h & 0xffff) * 0x5bd1e995) + ((((h >>> 16) * 0x5bd1e995) & 0xffff) << 16));
      h ^= h >>> 15;
      return h >>> 0;
    }
    // end of imported section
  }

  export enum Numbers {
    MaxU16 = 0xFFFF,
    MaxI16 = 0x7FFF,
    MinI16 = -0x8000
  }

  export module IntegerUtilities {
    var sharedBuffer = new ArrayBuffer(8);
    export var i32 = new Int32Array(sharedBuffer);
    export var f32 = new Float32Array(sharedBuffer);
    export var f64 = new Float64Array(sharedBuffer);

    /**
     * Convert 32 bits into a float.
     */
    export function int32ToFloat(i: number) {
      i32[0] = i; return f32[0];
    }

    export function int64ToDouble(high: number, low: number) {
      i32[0] = low; i32[1] = high; return f64[0];
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

    Uint32ArrayBitSet.prototype.toString = toString;
    Uint32ArrayBitSet.prototype.toBitString = toBitString;
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
})();
