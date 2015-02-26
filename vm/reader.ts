/*
 node-jvm
 Copyright (c) 2013 Yaroslav Gaponov <yaroslav.gaponov@gmail.com>
*/

module J2ME {
  declare var util;
  export class Reader {
    view: DataView;
    // DataView is not optimized, use Uint8Array for the fast paths.
    u8: Uint8Array;
    offset: number;

    static arrays: string [][] = ArrayUtilities.makeArrays(128);

    static getArray(length: number) {
      return Reader.arrays[length];
    }

    constructor(buffer: ArrayBuffer, offset: number = 0) {
      this.view = new DataView(buffer);
      this.u8 = new Uint8Array(buffer);
      this.offset = offset;
    }

    read8() {
      return this.u8[this.offset++];
    }

    read16() {
      var u8 = this.u8;
      var o = this.offset;
      this.offset += 2;
      return u8[o] << 8 | u8[o + 1];
    }

    read32() {
      return this.readInteger() >>> 0;
    }

    readInteger() {
      var o = this.offset;
      var u8 = this.u8;
      var a = u8[o + 0];
      var b = u8[o + 1];
      var c = u8[o + 2];
      var d = u8[o + 3];
      this.offset = o + 4;
      return (a << 24) | (b << 16) | (c << 8) | d;
    }

    readFloat() {
      var data = this.view.getFloat32(this.offset, false);
      this.offset += 4;
      return data;
    }

    readDouble() {
      var data = this.view.getFloat64(this.offset, false);
      this.offset += 8;
      return data;
    }

    // Decode Java's modified UTF-8 (JVM specs, $ 4.4.7)
    // http://docs.oracle.com/javase/specs/jvms/se5.0/html/ClassFile.doc.html#7963
    readStringFast(length: number): string {
      var a = (length < 128) ? Reader.getArray(length) : new Array(length);
      var i = 0, j = 0;
      var o = this.offset;
      var e = o + length;
      var u8 = this.u8;
      while (o < e) {
        var x = u8[o++];
        if (x <= 0x7f) {
          // Code points in the range '\u0001' to '\u007F' are represented by a
          // single byte.
          // The 7 bits of data in the byte give the value of the code point
          // represented.
          a[j++] = String.fromCharCode(x);
        } else if (x <= 0xdf) {
          // The null code point ('\u0000') and code points in the range '\u0080'
          // to '\u07FF' are represented by a pair of bytes x and y.
          var y = u8[o++]
          a[j++] = String.fromCharCode(((x & 0x1f) << 6) + (y & 0x3f));
        } else {
          // Code points in the range '\u0800' to '\uFFFF' are represented by 3
          // bytes x, y, and z.
          var y = u8[o++];
          var z = u8[o++];
          a[j++] = String.fromCharCode(((x & 0xf) << 12) + ((y & 0x3f) << 6) + (z & 0x3f));
        }
      }
      this.offset = o;
      if (j !== a.length) {
        var b = (j < 128) ? Reader.getArray(j) : new Array(j);
        for (var i = 0; i < j; i++) {
          b[i] = a[i];
        }
        a = b;
      }
      return a.join("");
    }

    readString(length) {
      if (length === 1) {
        var c = this.u8[this.offset];
        if (c <= 0x7f) {
          this.offset ++;
          return String.fromCharCode(c);
        }
      } else if (length < 128) {
        return this.readStringFast(length);
      }
      return this.readStringSlow(length);
    }

    readStringSlow(length) {
      // First try w/ TextDecoder, fallback to manually parsing if there was an
      // error. This will handle parsing errors resulting from Java's modified
      // UTF-8 implementation.
      try {
        // NB: no need to create a new slice.
        var data = new Uint8Array(this.view.buffer, this.offset, length);
        var s = util.decodeUtf8Array(data);
        this.offset += length;
        return s;
      } catch (e) {
        return this.readStringFast(length);
      }
    }

    readBytes(length) {
      var data = this.u8.buffer.slice(this.offset, this.offset + length);
      this.offset += length;
      return data;
    }
  }
}
