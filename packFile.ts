module J2ME {
  declare var util;

  /**
   * Unpacks source files packed with the pack.js utility.
   */
  export class PackFile {
    private u8: Uint8Array;
    private index: Map<string, any>;
    constructor(buffer: ArrayBuffer) {
      this.u8 = new Uint8Array(buffer);
      this.index = Object.create(null);
      var i = 0;
      while (i < this.u8.length) {
        var keyLength = this.readInt32(i);
        var key = this.readString(i + 4, keyLength);
        this.index[key] = i + 4 + keyLength;
        i += 4 + keyLength;
        var dataLength = this.readInt32(i);
        i += 4 + dataLength;
      }
    }

    get(key: string): string {
      var data = this.index[key];
      if (data === undefined) {
        return null;
      }
      if (typeof data === "number") {
        var i = data;
        var length = this.readInt32(i);
        var data = util.decodeUtf8Array(this.u8.subarray(i + 4, i + 4 + length));
        return this.index[key] = data;
      }
      return this.index[key];
    }

    private readInt32(offset: number) {
      var u8 = this.u8;
      var a = u8[offset + 3];
      var b = u8[offset + 2];
      var c = u8[offset + 1];
      var d = u8[offset + 0];
      return (a << 24) | (b << 16) | (c << 8) | d;
    }

    private readString(offset: number, length: number) {
      var s = new Array(length);
      var u8 = this.u8;
      for (var i = 0; i < length; i++) {
        s[i] = String.fromCharCode(u8[offset + i]);
      }
      return s.join("");
    }
  }
}