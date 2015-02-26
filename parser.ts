module J2ME.Parser {
  declare var util;
  import assert = J2ME.Debug.assert;
  export class ByteStream {
    u8: Uint8Array;
    offset: number;

    static arrays: string [][] = ArrayUtilities.makeArrays(128);

    static getArray(length: number) {
      return Reader.arrays[length];
    }

    constructor(buffer: ArrayBuffer, offset: number = 0) {
      this.u8 = new Uint8Array(buffer);
      this.offset = offset;
    }

    readU8() {
      return this.u8[this.offset++];
    }

    readU16() {
      var u8 = this.u8;
      var o = this.offset;
      this.offset += 2;
      return u8[o] << 8 | u8[o + 1];
    }

    peekU16() {
      var u8 = this.u8;
      var o = this.offset;
      return u8[o] << 8 | u8[o + 1];
    }

    readU32() {
      return this.readI32() >>> 0;
    }

    readI32() {
      var o = this.offset;
      var u8 = this.u8;
      var a = u8[o + 0];
      var b = u8[o + 1];
      var c = u8[o + 2];
      var d = u8[o + 3];
      this.offset = o + 4;
      return (a << 24) | (b << 16) | (c << 8) | d;
    }

    //readF32() {
    //  var data = this.view.getFloat32(this.offset, false);
    //  this.offset += 4;
    //  return data;
    //}
    //
    //readF64() {
    //  var data = this.view.getFloat64(this.offset, false);
    //  this.offset += 8;
    //  return data;
    //}

    skip(length: number) {
      this.offset += length;
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
          this.offset++;
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
        var data = new Uint8Array(this.u8.buffer, this.offset, length);
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

    //static readU32(u8: Uint8Array, o: number): number {
    //  return Bytes.readI32(u8, o) >>> 0;
    //}
    //
    //static readI32(u8: Uint8Array, o: number): number {
    //  var a = u8[o + 0];
    //  var b = u8[o + 1];
    //  var c = u8[o + 2];
    //  var d = u8[o + 3];
    //  return (a << 24) | (b << 16) | (c << 8) | d;
    //}
    //
    static readU16(u8: Uint8Array, o: number): number {
      return u8[o] << 8 | u8[o + 1];
    }
  }

  export class ConstantPool {
    stream: ByteStream;
    offset: number;
    count: number;
    entries: Uint32Array;

    private static tagSize = new Int8Array([
      -1, // ?
      -1, // CONSTANT_Utf8 has a variable length and needs to be handled differently.
      -1, // TODO: CONSTANT_Unicode
       4, // CONSTANT_Integer
       4, // CONSTANT_Float
       8, // CONSTANT_Long
       8, // CONSTANT_Double
       2, // CONSTANT_Class
       2, // CONSTANT_String
       4, // CONSTANT_Fieldref,
       4, // CONSTANT_Methodref
       4, // CONSTANT_InterfaceMethodref
       4  // CONSTANT_NameAndType
    ]);

    constructor(stream: ByteStream) {
      this.stream = stream;
      this.offset = stream.offset;
      this.scanEntries();
    }

    /**
     * Quickly scan over the constant pool and record the position of each constant pool entry.
     */
    private scanEntries() {
      var s = this.stream;
      var c = this.count = s.readU16();
      this.entries = new Uint32Array(this.count);
      var S = ConstantPool.tagSize;
      var o = s.offset;
      var u8 = s.u8;
      var e = this.entries;
      for (var i = 1; i < c; i++) {
        e[i] = o;
        var t = u8[o++];
        if (t === TAGS.CONSTANT_Utf8) {
          o += 2 + ByteStream.readU16(u8, o);
        } else {
          o += S[t];
        }
        if (t === TAGS.CONSTANT_Long || t === TAGS.CONSTANT_Double) {
          i++;
        }
      }
      s.offset = o;
    }
  }

  export class FieldInfo {

  }

  export class MethodInfo {
    classInfo: ClassInfo;
    offset: number;
    name: Uint16Array;
    constructor(classInfo: ClassInfo, offset: number) {
      this.classInfo = classInfo;
      this.offset = offset;
    }
  }

  export class ClassInfo {
    stream: ByteStream;
    magic: number;
    minor_version: number;
    major_version: number;
    constantPool: ConstantPool;

    access_flags: number;
    this_class: number;
    super_class: number;

    fields: number | FieldInfo [];
    methods: number | MethodInfo [];
    interfaces: number | ClassInfo [];

    constructor(stream: ByteStream) {
      var s = this.stream = stream;

      this.magic = s.readU32();
      this.minor_version = s.readU16();
      this.major_version = s.readU16();
      this.constantPool = new ConstantPool(s);
      this.access_flags = s.readU16();
      this.this_class = s.readU16();
      this.super_class = s.readU16();

      this.scanInterfaces();
      this.scanFields();
      this.scanMethods();
      this.skipAttributes();
    }

    private scanInterfaces() {
      var s = this.stream;
      var count = s.readU16();
      this.interfaces = new Array(count);
      for (var i = 0; i < count; i++) {
        this.interfaces[i] = s.offset;
        s.readU16();
      }
    }

    private scanFields() {
      var s = this.stream;
      var count = s.readU16();
      var f = this.fields = new Array(count);
      for (var i = 0; i < count; i++) {
        f[i] = s.offset;
        s.skip(6);
        this.skipAttributes();
      }
    }

    private scanMethods() {
      var s = this.stream;
      var count = s.readU16();
      var m = this.methods = new Array(count);
      for (var i = 0; i < count; i++) {
        m[i] = s.offset;
        s.skip(6);
        this.skipAttributes();
      }
    }

    private skipAttributes() {
      var s = this.stream;
      var count = s.readU16();
      for (var i = 0; i < count; i++) {
        s.readU16();
        s.skip(s.readU32());
      }
    }

    getMethodInfo(i: number): MethodInfo {
      if (typeof this.methods[i] === "number") {
        this.methods[i] = new MethodInfo(this, this.methods[i]);
      }
      return this.methods[i];
    }
  }
}