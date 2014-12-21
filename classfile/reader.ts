module J2ME {
  declare var util;
  export class Reader {
    view: DataView;
    // DataView is not optimized, use Uint8Array for the fast paths.
    u8: Uint8Array;
    offset: number;
    constructor(buffer: ArrayBuffer, offset: number) {
      this.view = new DataView(buffer);
      this.u8 = new Uint8Array(buffer);
      this.offset = offset || 0;
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

    readString(length) {
      if (length === 1) {
        var c = this.u8[this.offset];
        if (c <= 0x7f) {
          this.offset ++;
          return String.fromCharCode(c);
        }
      }
      return this.readStringSlow(length);
    }

    readStringSlow(length) {
      // NB: no need to create a new slice.
      var data = new Uint8Array(this.view.buffer, this.offset, length);
      this.offset += length;

      // First try w/ TextDecoder, fallback to manually parsing if there was an
      // error. This will handle parsing errors resulting from Java's modified
      // UTF-8 implementation.
      try {
        var s = util.decodeUtf8Array(data);
        return s;
      } catch (e) {
        return util.javaUTF8Decode(data);
      }
    }

    readBytes(length) {
      var data = this.u8.buffer.slice(this.offset, this.offset + length);
      this.offset += length;
      return data;
    }
  }
}
