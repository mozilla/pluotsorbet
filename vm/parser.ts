/*
 node-jvm
 Copyright (c) 2013 Yaroslav Gaponov <yaroslav.gaponov@gmail.com>
*/

module J2ME {
  declare var util, Native;
  import assert = J2ME.Debug.assert;
  import concat3 = StringUtilities.concat3;
  import pushMany = ArrayUtilities.pushMany;
  import unique = ArrayUtilities.unique;
  import hashBytesTo32BitsMurmur = HashUtilities.hashBytesTo32BitsMurmur;
  export const enum UTF8Chars {
    a = 97,
    Z = 90,
    C = 67,
    F = 70,
    D = 68,
    B = 66,
    S = 83,
    I = 73,
    J = 74,
    V = 86,

    L = 76,
    OpenBracket = 91,
    Semicolon = 59,
    Dot = 46,
    Slash = 47,

    OpenParenthesis = 40,
    CloseParenthesis = 41,

  }

  module UTF8 {
    export var Code = new Uint8Array([67, 111, 100, 101]);
    export var ConstantValue = new Uint8Array([67, 111, 110, 115, 116, 97, 110, 116, 86, 97, 108, 117, 101]);
    export var Init = new Uint8Array([60, 105, 110, 105, 116, 62]);

    export var Z = new Uint8Array([UTF8Chars.Z]);
    export var C = new Uint8Array([UTF8Chars.C]);
    export var F = new Uint8Array([UTF8Chars.F]);
    export var D = new Uint8Array([UTF8Chars.D]);
    export var B = new Uint8Array([UTF8Chars.B]);
    export var S = new Uint8Array([UTF8Chars.S]);
    export var I = new Uint8Array([UTF8Chars.I]);
    export var J = new Uint8Array([UTF8Chars.J]);
  }

  export function strcmp(a: Uint8Array, b: Uint8Array): boolean {
    if (a === b) {
      return true;
    }
    if (a.length !== b.length) {
      return false;
    }
    var l = a.length;
    for (var i = 0; i < l; i++) {
      if (a[i] !== b[i]) {
        return false;
      }
    }
    return true;
  }

  var utf8Cache = Object.create(null);

  /**
   * Caches frequently used UTF8 strings. Only use this for a small set of frequently
   * used JS -> UTF8 conversions.
   */
  export function cacheUTF8(s: string) {
    var r = utf8Cache[s];
    if (r !== undefined) {
      return r;
    }
    return utf8Cache[s] = toUTF8(s);
  }

  export function toUTF8(s: string): Uint8Array {
    var r = new Uint8Array(s.length);
    for (var i = 0; i < s.length; i++) {
      var c = s.charCodeAt(i);
      release || assert(c <= 0x7f, "bad char in toUTF8");
      r[i] = c;
    }
    return r;
  }

  export function fromUTF8(s: Uint8Array): string {
    return ByteStream.readString(s);
  }

  function strcatSingle(a: number, b: Uint8Array): Uint8Array {
    var r = new Uint8Array(1 + b.length);

    r[0] = a;

    // For short strings, a for loop is faster than a call to TypedArray::set()
    for (var i = 1; i < b.length + 1; i++) {
      r[i] = b[i - 1];
    }

    return r;
  }

  function strcat4Single(a: number, b: number, c: Uint8Array, d: number): Uint8Array {
    var r = new Uint8Array(c.length + 3);

    r[0] = a;

    r[1] = b;

    // For short strings, a for loop is faster than a call to TypedArray::set()
    for (var i = 2; i < c.length + 2; i++) {
      r[i] = c[i - 2];
    }

    r[2 + c.length] = d;

    return r;
  }

  // Seal ClassInfo, MethodInfo and FieldInfo objects so their shapes are fixed. This should
  // not be enabled by default as it usually causes perf problems, but it's useful as a
  // debugging feature nonetheless.
  var sealObjects = false;

  /**
   * Base class of all class file structs.
   */
  export class ByteStream {

    private static internedOneByteArrays: Uint8Array [] = ArrayUtilities.makeDenseArray(256, null);

    // Most common tree byte arrays signatures, these must all be prefixed with "()". If you want
    // to support more complicated patterns, modify |readInternedBytes|.
    private static internedThreeByteArraySignatures: Uint8Array [] = [
      new Uint8Array([40, 41, 86]), // ()V
      new Uint8Array([40, 41, 73]), // ()I
      new Uint8Array([40, 41, 90]), // ()Z
      new Uint8Array([40, 41, 74]), // ()J
    ];

    private static internedMap = new TypedArrayHashtable(64);

    static UTF8toUTF16(utf8: Uint8Array): number {
      // This conversion is mainly used for symbols within a class file,
      // in which the large majority of strings are all ascii.
      var ascii = true;
      var utf8Length = utf8.length;
      var utf16Addr = newCharArray(utf8Length);
      for (var i = 0; i < utf8Length; i++) {
        var ch1 = utf8[i];
        if (ch1 === 0) {
          throw new Error("Bad utf16 value.");
        }
        if (ch1 >= 128) {
          ascii = false;
          break;
        }
        u16[(utf16Addr + Constants.ARRAY_HDR_SIZE >> 1) + i] = ch1;
      }
      if (ascii) {
        return utf16Addr;
      }
      var index = 0;
      var a = [];
      while (index < utf8Length) {
        var ch1 = utf8[index++];
        if (ch1 < 128) {
          a.push(ch1);
          continue;
        }

        switch (ch1 >> 4) {
          case 0x8:
          case 0x9:
          case 0xA:
          case 0xB:
          case 0xF:
            throw new Error("Bad utf16 value.");
          case 0xC:
          case 0xD:
            /* 110xxxxx  10xxxxxx */
            if (index < utf8Length) {
              var ch2 = utf8[index];
              index++;
              if ((ch2 & 0xC0) == 0x80) {
                var highFive = (ch1 & 0x1F);
                var lowSix = (ch2 & 0x3F);
                a.push(((highFive << 6) + lowSix));
              }
            }
            break;
          case 0xE:
            /* 1110xxxx 10xxxxxx 10xxxxxx */
            if (index < utf8Length) {
              var ch2 = utf8[index];
              index++;
              if ((ch2 & 0xC0) == 0x80 && index < utf8Length) {
                var ch3 = utf8[index];
                if ((ch3 & 0xC0) == 0x80) {
                  index++;
                  var highFour = (ch1 & 0x0f);
                  var midSix = (ch2 & 0x3f);
                  var lowSix = (ch3 & 0x3f);
                  a.push((((highFour << 6) + midSix) << 6) + lowSix);
                } else {
                  var highFour = (ch1 & 0x0f);
                  var lowSix = (ch2 & 0x3f);
                  a.push((highFour << 6) + lowSix);
                }
              }
            }
            break;
          default:
            break;
        }
      }

      var retAddr = newCharArray(a.length);
      u16.set(a, retAddr + Constants.ARRAY_HDR_SIZE >> 1);
      return retAddr;
    }

    constructor (
      public buffer: Uint8Array,
      public offset: number
    ) {
      // ...
    }

    u2(offset: number) {
      var b = this.buffer;
      var o = this.offset + offset;
      return b[o] << 8 | b[o + 1];
    }

    clone() {
      return new ByteStream(this.buffer, this.offset);
    }

    readU1() {
      return this.buffer[this.offset++];
    }

    peekU1() {
      return this.buffer[this.offset];
    }

    readU2() {
      var buffer = this.buffer;
      var o = this.offset;
      this.offset += 2;
      return buffer[o] << 8 | buffer[o + 1];
    }

    peekU16() {
      var buffer = this.buffer;
      var o = this.offset;
      return buffer[o] << 8 | buffer[o + 1];
    }

    readU4() {
      return this.readS4() >>> 0;
    }

    skipU4() {
      this.offset += 4;
    }

    readS4() {
      var o = this.offset;
      var buffer = this.buffer;
      var a = buffer[o + 0];
      var b = buffer[o + 1];
      var c = buffer[o + 2];
      var d = buffer[o + 3];
      this.offset = o + 4;
      return (a << 24) | (b << 16) | (c << 8) | d;
    }

    seek(offset: number): ByteStream {
      this.offset = offset;
      return this;
    }

    skip(length: number): ByteStream {
      this.offset += length;
      return this;
    }

    /**
     * Interns small and frequently used Uint8Array buffers.
     *
     * Relative frequencies of readByte sizes.
     *  2011: readBytes 2
     *  1853: readBytes 1 - Special cased.
     *  1421: readBytes 4
     *  1170: readBytes 5
     *  1042: readBytes 3 - Special cased, most three byte buffers are signatures of the form "()?".
     *  1022: readBytes 6
     *
     * All other sizes are interned using a hashtable.
     */
    internBytes(length: number) {
      var o = this.offset;
      var buffer = this.buffer;
      var a = buffer[o];
      if (length === 1) { // Intern all 1 byte buffers.
        var one = ByteStream.internedOneByteArrays;
        var r = one[a];
        if (r === null) {
          r = one[a] = new Uint8Array([a]);
        }
        return r;
      } else if (length === 3 && // Intern most common 3 byte buffers.
        a === UTF8Chars.OpenParenthesis) { // Check if first byte is "(".
        var b = buffer[o + 1];
        if (b === UTF8Chars.CloseParenthesis) {
          var three = ByteStream.internedThreeByteArraySignatures;
          var c = buffer[o + 2];
          for (var i = 0; i < three.length; i++) {
            if (three[i][2] === c) {
              return three[i]
            }
          }
        }
      } else {
        var data: Uint8Array = ByteStream.internedMap.getByRange(buffer, o, length);
        if (data) {
          return data;
        }
        var data = this.buffer.subarray(o, o + length);
        ByteStream.internedMap.put(data, data);
        return data;
      }
      return null;
    }

    readBytes(length: number): Uint8Array {
      var data = this.buffer.subarray(this.offset, this.offset + length);
      this.offset += length;
      return data;
    }

    readInternedBytes(length: number): Uint8Array {
      var data = length <= 4 ? this.internBytes(length) : null;
      if (data) {
        this.offset += data.length;
        return data;
      }
      return this.readBytes(length);
    }

    static arrays: string [][] = ArrayUtilities.makeArrays(128);

    static getArray(length: number) {
      return ByteStream.arrays[length];
    }

    // Decode Java's modified UTF-8 (JVM specs, $ 4.4.7)
    // http://docs.oracle.com/javase/specs/jvms/se5.0/html/ClassFile.doc.html#7963
    static readStringFast(buffer: Uint8Array): string {
      var length = buffer.length;
      var a = (length < 128) ? ByteStream.getArray(length) : new Array(length);
      var i = 0, j = 0;
      var o = 0;
      var e = o + length;
      var buffer = buffer;
      while (o < e) {
        var x = buffer[o++];
        if (x <= 0x7f) {
          // Code points in the range '\u0001' to '\u007F' are represented by a
          // single byte.
          // The 7 bits of data in the byte give the value of the code point
          // represented.
          a[j++] = String.fromCharCode(x);
        } else if (x <= 0xdf) {
          // The null code point ('\u0000') and code points in the range '\u0080'
          // to '\u07FF' are represented by a pair of bytes x and y.
          var y = buffer[o++]
          a[j++] = String.fromCharCode(((x & 0x1f) << 6) + (y & 0x3f));
        } else {
          // Code points in the range '\u0800' to '\uFFFF' are represented by 3
          // bytes x, y, and z.
          var y = buffer[o++];
          var z = buffer[o++];
          a[j++] = String.fromCharCode(((x & 0xf) << 12) + ((y & 0x3f) << 6) + (z & 0x3f));
        }
      }
      if (j !== a.length) {
        var b = (j < 128) ? ByteStream.getArray(j) : new Array(j);
        for (var i = 0; i < j; i++) {
          b[i] = a[i];
        }
        a = b;
      }
      return a.join("");
    }

    static readString(buffer: Uint8Array) {
      var length = buffer.length;
      if (length === 1) {
        var c = buffer[0];
        if (c <= 0x7f) {
          return String.fromCharCode(c);
        }
      } else if (length < 128) {
        return ByteStream.readStringFast(buffer);
      }
      return ByteStream.readStringSlow(buffer);
    }

    static readStringSlow(buffer: Uint8Array) {
      // First try w/ TextDecoder, fallback to manually parsing if there was an
      // error. This will handle parsing errors resulting from Java's modified
      // UTF-8 implementation.
      try {
        return util.decodeUtf8Array(buffer);
      } catch (e) {
        return this.readStringFast(buffer);
      }
    }

    static readU16(buffer: Uint8Array, o: number): number {
      return buffer[o] << 8 | buffer[o + 1];
    }
  }

  export const enum ACCESS_FLAGS {
    ACC_PUBLIC        = 0x0001,
    ACC_PRIVATE       = 0x0002,
    ACC_PROTECTED     = 0x0004,
    ACC_STATIC        = 0x0008,
    ACC_FINAL         = 0x0010,
    ACC_SYNCHRONIZED  = 0x0020,
    ACC_VOLATILE      = 0x0040,
    ACC_TRANSIENT     = 0x0080,
    ACC_NATIVE        = 0x0100,
    ACC_INTERFACE     = 0x0200,
    ACC_ABSTRACT      = 0x0400,

    J2ME_IMPLEMENTS_INTERFACE     = 0x10000

  }

  export const enum TAGS {
    CONSTANT_Class = 7,
    CONSTANT_Fieldref = 9,
    CONSTANT_Methodref = 10,
    CONSTANT_InterfaceMethodref = 11,
    CONSTANT_String = 8,
    CONSTANT_Integer = 3,
    CONSTANT_Float = 4,
    CONSTANT_Long = 5,
    CONSTANT_Double = 6,
    CONSTANT_NameAndType = 12,
    CONSTANT_Utf8 = 1,
    CONSTANT_Unicode = 2,
    CONSTANT_Any = 13 // NON-STANDARD
  }

  export function getTAGSName(tag: TAGS): string {
    return (<any>J2ME).TAGS[tag];
  }

  export class ConstantPool extends ByteStream {
    /**
     * Starting positions of each entry in the constant pool.
     */
    entries: Uint32Array;

    /**
     * Resolved constant pool references.
     */
    resolved: any [];

    /**
     * Size of each tag. This is used to jump over constant pool entries quickly.
     */
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
      super(stream.buffer, stream.offset);
      this.scanEntries();
    }

    /**
     * Quickly scan over the constant pool and record the position of each constant pool entry.
     */
    private scanEntries() {
      var s = this;
      var c  = s.readU2();
      this.entries = new Uint32Array(c);
      // We make this dense because the access pattern is pretty random, and it would otherwise
      // cause lots of ION bailouts.
      this.resolved = ArrayUtilities.makeDenseArray(c, undefined);
      var S = ConstantPool.tagSize;
      var o = s.offset;
      var buffer = s.buffer;
      var e = this.entries;
      for (var i = 1; i < c; i++) {
        e[i] = o;
        var t = buffer[o++];
        if (t === TAGS.CONSTANT_Utf8) {
          o += 2 + ByteStream.readU16(buffer, o);
        } else {
          o += S[t];
        }
        if (t === TAGS.CONSTANT_Long || t === TAGS.CONSTANT_Double) {
          i++;
        }
      }
      s.offset = o;
    }

    resolveUtf8(i: number): Uint8Array {
      return <Uint8Array>this.resolve(i, TAGS.CONSTANT_Utf8);
    }

    /**
     * Reads a 16-bit number at an offset from the constant pool entry index.
     */
    readTagU2(i: number, tag: TAGS, offset: number) {
      var b = this.buffer;
      release || assert(b[this.entries[i]] === tag, "readTagU2 failure");
      var o = this.entries[i] + offset;
      return b[o] << 8 | b[o + 1];
    }

    /**
     * Seeks the current stream position to a specified constant pool entry and
     * returns the tag value.
     */
    private seekTag(i: number): TAGS {
      this.seek(this.entries[i]);
      return <TAGS>this.peekU1();
    }

    public peekTag(i: number): TAGS {
      return this.buffer[this.entries[i]];
    }

    /**
     * This causes the Utf8 string to be redecoded each time so don't use it often.
     */
    resolveUtf8String(i: number): string {
      if (i === 0) return null;
      var u8 = this.resolveUtf8(i);
      return ByteStream.readString(u8);
    }

    resolveUtf8ClassNameString(i: number): string {
      if (i === 0) return null;
      return this.resolveUtf8String(this.readTagU2(i, TAGS.CONSTANT_Class, 1));
    }

    resolveUtf8ClassName(i: number): Uint8Array {
      if (i === 0) return null;
      return this.resolveUtf8(this.readTagU2(i, TAGS.CONSTANT_Class, 1));
    }

    getConstantTag(i: number) {
      return this.seekTag(i);
    }

    resolveString(i: number): string {
      var s = this;
      var tag = s.seekTag(i);
      release || assert(tag === TAGS.CONSTANT_String, "resolveString failure");
      s.readU1();
      return this.resolveUtf8String(s.readU2())
    }

    /**
     * Resolves a constant pool reference.
     */
    resolve(i: number, expectedTag: TAGS, isStatic: boolean = false): any {
      var s = this, r = this.resolved[i];
      if (r === undefined) {
        var tag = this.seekTag(i);
        release || Debug.assert(expectedTag === TAGS.CONSTANT_Any || expectedTag === tag ||
                                (expectedTag === TAGS.CONSTANT_Methodref && tag === TAGS.CONSTANT_InterfaceMethodref), "bad expectedTag in resolve");
        switch (s.readU1()) {
          case TAGS.CONSTANT_String:
            r = this.resolved[i] = $.newStringConstant(ByteStream.UTF8toUTF16(this.resolveUtf8(s.readU2())));
            break;
          case TAGS.CONSTANT_Utf8:
            r = this.resolved[i] = s.readInternedBytes(s.readU2());
            break;
          case TAGS.CONSTANT_Class:
            r = this.resolved[i] = CLASSES.getClass(ByteStream.readString(this.resolve(s.readU2(), TAGS.CONSTANT_Utf8)));
            break;
          case TAGS.CONSTANT_Fieldref:
          case TAGS.CONSTANT_Methodref:
          case TAGS.CONSTANT_InterfaceMethodref:
            var class_index = s.readU2();
            var name_and_type_index = s.readU2();
            var classInfo = this.resolveClass(class_index);
            var name_index = this.readTagU2(name_and_type_index, TAGS.CONSTANT_NameAndType, 1);
            var type_index = this.readTagU2(name_and_type_index, TAGS.CONSTANT_NameAndType, 3);
            var name = this.resolveUtf8(name_index);
            var type = this.resolveUtf8(type_index);
            if (tag === TAGS.CONSTANT_Fieldref) {
              r = classInfo.getFieldByName(name, type, isStatic);
            } else {
              r = classInfo.getMethodByName(name, type);
            }
            if (!r) {
              throw new JavaRuntimeException(classInfo.getClassNameSlow() + "." + fromUTF8(name) + "." + fromUTF8(type) + " not found");
            }
            // Set the method/field as resolved only if it was actually found, otherwise a new attempt to
            // resolve this method/field will not fail with a RuntimeException.
            this.resolved[i] = r;
            break;
          default:
            assert(false, "bad type (" + expectedTag + ") in resolve");
            break;
        }
      }
      return r;
    }

    resolveClass(index: number): ClassInfo {
      return <ClassInfo>this.resolve(index, TAGS.CONSTANT_Class);
    }

    resolveMethod(index: number, isStatic: boolean): MethodInfo {
      return <MethodInfo>this.resolve(index, TAGS.CONSTANT_Methodref, isStatic);
    }

    resolveField(index: number, isStatic: boolean): FieldInfo {
      return <FieldInfo>this.resolve(index, TAGS.CONSTANT_Fieldref, isStatic);
    }
  }

  export class FieldInfo extends ByteStream {
    public classInfo: ClassInfo;
    public kind: Kind;
    public byteOffset: number = 0;
    public utf8Name: Uint8Array;
    public utf8Signature: Uint8Array;
    public mangledName: string = null;
    public accessFlags: ACCESS_FLAGS;
    fTableIndex: number = -1;

    constructor(classInfo: ClassInfo, offset: number) {
      super(classInfo.buffer, offset);
      this.classInfo = classInfo;
      this.accessFlags = this.readU2();
      this.utf8Name = classInfo.constantPool.resolveUtf8(this.readU2());
      this.utf8Signature = classInfo.constantPool.resolveUtf8(this.readU2());
      this.kind = getSignatureKind(this.utf8Signature);
      this.scanFieldInfoAttributes();
      sealObjects && Object.seal(this);
    }

    public get isStatic(): boolean {
      return !!(this.accessFlags & ACCESS_FLAGS.ACC_STATIC);
    }

    private scanFieldInfoAttributes() {
      var s = this;
      var attributes_count = s.readU2();
      for (var i = 0; i < attributes_count; i++) {
        var attribute_name_index = s.readU2();
        var attribute_length = s.readU4();
        var o = s.offset;
        var attribute_name = this.classInfo.constantPool.resolveUtf8(attribute_name_index);
        if (strcmp(attribute_name, UTF8.ConstantValue)) {
          release || assert(attribute_length === 2, "Attribute length of ConstantValue must be 2.");
        }
        s.seek(o + attribute_length);
      }
    }
  }

  export class SourceLocation {
    constructor(public className: string,
                public sourceFile: string,
                public lineNumber: number) {
      // ...
    }
    toString() {
      return this.sourceFile + ":" + this.lineNumber;
    }
    equals(other: SourceLocation): boolean {
      if (!other) {
        return false;
      }
      return this.sourceFile === other.sourceFile &&
             this.lineNumber === other.lineNumber;
    }
  }

  export class MethodInfoStats {
    callCount: number = 0;
    bytecodeCount: number = 0;
    backwardsBranchCount: number = 0;
    interpreterCallCount: number = 0;
  }

  export class ExceptionEntryView extends ByteStream {
    get start_pc(): number {
      return this.u2(0);
    }

    get end_pc(): number {
      return this.u2(2);
    }

    get handler_pc(): number {
      return this.u2(4);
    }

    get catch_type(): number {
      return this.u2(6);
    }
  }

  export function mangleClassAndMethod(methodInfo: MethodInfo) {
    return methodInfo.classInfo.mangledName + "_" + methodInfo.index;
  }

  export function mangleMethod(methodInfo: MethodInfo) {
    var utf8Name = methodInfo.utf8Name;
    var utf8Signature = methodInfo.utf8Signature;
    var hash = hashBytesTo32BitsMurmur(utf8Name, 0, utf8Name.length);
    hash ^= hashBytesTo32BitsMurmur(utf8Signature, 0, utf8Signature.length);
    return "$" + StringUtilities.variableLengthEncodeInt32(hash);
  }

  /**
   * Encodes variable length utf8 alpha strings of the form [a-z]* to
   * 32 bit numbers. Below are some sample encodings:
   *
   *  "" => 0
   *  "a" => 1
   *  "b" => 2 ...
   *  "z" => 26
   *  "aa" => 27
   *  "ab" => 28 ...
   *  "zz" => 703
   *  "aaa" => 704
   *  "azz" => 1378
   *  "zzz" => 18278
   *
   *  The goal of this encoding is to map short strings to low numeric values
   *  that we can then use to index into tables.
   */
  export function lowerCaseAlphaToInt32(utf8String: Uint8Array): number {
    // We can't encode strings larger than 6 characters because we don't
    // have enough bits. Technically the limit is somewhere between 6 and 7
    // but we don't bother to check that here.
    if (utf8String.length > 6) {
      // It's okay to return |-1| as a fail value since we'll never use the
      // highest order bit for encoding.
      return -1;
    }
    var s = 0;
    for (var i = 0; i < utf8String.length; i++) {
      var v = utf8String[i] - UTF8Chars.a;
      if (v < 0 || v >= 26) { // Only 'a' ... 'z' is allowed.
        return -1;
      }
      s *= 26;
      s += (1 + v); // 'a' is mapped to 1.
    }
    return s;
  }

  export function mangleClassName(utf8Name: Uint8Array) {
    var hash = lowerCaseAlphaToInt32(utf8Name);
    if (hash > 0 && hash < 2048) {
      return "$" + fromUTF8(utf8Name);
    }
    var hash = hashBytesTo32BitsMurmur(utf8Name, 0, utf8Name.length);
    return concat3("$",
                   StringUtilities.variableLengthEncodeInt32(hash),
                   StringUtilities.toEncoding(utf8Name.length & 0x3f));
  }

  export class MethodInfo extends ByteStream {
    private static nextId: number = 1;

    public classInfo: ClassInfo;
    public accessFlags: ACCESS_FLAGS;

    public fn: any = null;
    public index: number;
    public id: number;
    public state: MethodState;
    public stats: MethodInfoStats;
    public codeAttribute: CodeAttribute;
    public utf8Name: Uint8Array;
    public utf8Signature: Uint8Array;
    public returnKind: Kind;
    public signatureKinds: Kind [];


    public argumentSlots: number;
    public signatureSlots: number;
    public hasTwoSlotArguments: boolean;

    vTableIndex: number;

    private _virtualName: string = null;
    private _mangledName: string = null;
    private _mangledClassAndMethodName: string = null;

    private _implKey: string = null;
    private _name: string = null;
    private _signature: string = null;

    ///// FIX THESE LATER ////
    onStackReplacementEntryPoints: number [] = null;

    exception_table_length: number = -1;
    exception_table_offset: number = -1;
    isOptimized: boolean = false;

    constructor(classInfo: ClassInfo, offset: number, index: number) {
      super(classInfo.buffer, offset);
      this.id = MethodInfo.nextId++;
      registerMethodId(this.id, this);
      this.index = index;
      this.accessFlags = this.u2(0);
      this.classInfo = classInfo;
      var cp = this.classInfo.constantPool;
      this.utf8Name = cp.resolveUtf8(this.u2(2));
      this.utf8Signature = cp.resolveUtf8(this.u2(4));
      this.vTableIndex = -1;

      this.state = MethodState.Cold;
      this.stats = new MethodInfoStats();
      this.codeAttribute = null;
      this.scanMethodInfoAttributes();

      // Parse signature and cache some useful information.
      var signatureKinds = this.signatureKinds = parseMethodDescriptorKinds(this.utf8Signature, 0).slice();
      this.returnKind = signatureKinds[0];
      this.hasTwoSlotArguments = signatureHasTwoSlotArguments(signatureKinds);
      this.signatureSlots = signatureArgumentSlotCount(signatureKinds);
      this.argumentSlots = this.signatureSlots;
      if (!this.isStatic) {
        this.argumentSlots ++;
      }
      sealObjects && Object.seal(this);
    }

    /**
     * Clones this method info.
     */
    cloneMethodInfo(): MethodInfo {
      return new MethodInfo(this.classInfo, this.offset, this.index);
    }

    get virtualName() {
      if (this.vTableIndex >= 0) {
        return this._virtualName || (this._virtualName = "v" + this.vTableIndex);
      }
      return undefined;
    }

    get mangledName() {
      return this._mangledName || (this._mangledName = mangleMethod(this));
    }

    get mangledClassAndMethodName() {
      return this._mangledClassAndMethodName || (this._mangledClassAndMethodName = mangleClassAndMethod(this));
    }

    get name(): string {
      return this._name || (this._name = ByteStream.readString(this.utf8Name));
    }

    get signature(): string {
      return this._signature || (this._signature = ByteStream.readString(this.utf8Signature));
    }

    get implementsInterface(): boolean {
      return !!(this.accessFlags & ACCESS_FLAGS.J2ME_IMPLEMENTS_INTERFACE);
    }

    get implKey(): string {
      return this._implKey || (this._implKey = this.classInfo.getClassNameSlow() + "." + this.name + "." + this.signature);
    }

    get isNative(): boolean {
      if (!release) {
        if (Native[this.implKey]) {
          return true;
        }
      }
      return !!(this.accessFlags & ACCESS_FLAGS.ACC_NATIVE);
    }

    get isFinal(): boolean {
      return !!(this.accessFlags & ACCESS_FLAGS.ACC_FINAL);
    }

    get isPublic(): boolean {
      return !!(this.accessFlags & ACCESS_FLAGS.ACC_PUBLIC);
    }

    get isStatic(): boolean {
      return !!(this.accessFlags & ACCESS_FLAGS.ACC_STATIC);
    }

    get isSynchronized(): boolean {
      return !!(this.accessFlags & ACCESS_FLAGS.ACC_SYNCHRONIZED);
    }

    get isAbstract(): boolean {
      return !!(this.accessFlags & ACCESS_FLAGS.ACC_ABSTRACT);
    }

    getSourceLocationForPC(pc: number): SourceLocation {
      return null;
    }

    getExceptionEntryViewByIndex(i: number): ExceptionEntryView {
      if (i >= this.exception_table_length) {
        return null;
      }

      return new ExceptionEntryView(this.buffer, this.exception_table_offset + i * 8);
    }

    scanMethodInfoAttributes() {
      var b = this.offset;
      var s = this.skip(6);
      var count = s.readU2();
      for (var i = 0; i < count; i++) {
        var attribute_name_index = s.readU2();
        var attribute_length = s.readU4();
        var o = s.offset;
        var attribute_name = this.classInfo.constantPool.resolveUtf8(attribute_name_index);
        if (strcmp(attribute_name, UTF8.Code)) {
          this.codeAttribute = new CodeAttribute(s);
          this.exception_table_length = s.readU2();
          this.exception_table_offset = s.offset;
        }
        s.seek(o + attribute_length);
      }
      this.seek(b);
    }
  }

  const enum ResolvedFlags {
    None          = 0,
    Fields        = 1,
    Methods       = 2,
    Interfaces    = 4
  }

  export class CodeAttribute {
    max_stack: number;
    max_locals: number;
    code: Uint8Array;
    constructor(s: ByteStream) {
      this.max_stack = s.readU2();
      this.max_locals = s.readU2();
      var code_length = s.readU4();
      // We don't call |readInternedBytes| because the returned bytes can be modified by the
      // interpreter, and interned bytes must be immutable.
      this.code = s.readBytes(code_length);
    }
  }

  function indexOfMethod(table: MethodInfo [], utf8Name: Uint8Array, utf8Signature: Uint8Array, indexHint: number): number {
    // Quick test using the index hint.
    if (indexHint >= 0) {
      if (strcmp(utf8Name, table[indexHint].utf8Name) && strcmp(utf8Signature, table[indexHint].utf8Signature)) {
        return indexHint;
      }
    }
    for (var i = 0; i < table.length; i++) {
      var methodInfo = table[i];
      var methodUTF8Name = methodInfo.utf8Name;
      if (utf8Name.length !== methodUTF8Name.length || utf8Name[0] !== methodUTF8Name[0]) { // Quick false test.
        continue;
      }
      if (strcmp(utf8Name, methodUTF8Name) && strcmp(utf8Signature, methodInfo.utf8Signature)) {
        return i;
      }
    }
    return -1;
  }

  // Very simple hash map that uses Uint8Array keys.
  var hashMapSizeMask = 0xff;
  function setHashMapValue(cache: Uint16Array, key: Uint8Array, value: number) {
    var hash = key[0] + Math.imul(key.length, 31);
    cache[hash & 0xff] = value;
  }
  function getHashMapValue(cache: Uint16Array, key: Uint8Array): number {
    var hash = key[0] + Math.imul(key.length, 31);
    return cache[hash & 0xff];
  }

  export class ClassInfo extends ByteStream {
    /**
     * We use this ID to map Java objects to their ClassInfo objects,
     * storing the ID for the Class in the first four bytes
     * of the memory allocated for the Java object in the ASM heap.
     *
     */
    private static nextId: number = 1;

    constantPool: ConstantPool = null;

    utf8Name: Uint8Array = null;
    utf8SuperName: Uint8Array = null;

    superClass: ClassInfo = null;
    elementClass: ClassInfo = null;
    subClasses: ClassInfo [] = [];
    allSubClasses: ClassInfo [] = [];

    // Class hierarchy depth.
    depth: number = 0;
    private display: ClassInfo [] = null;

    accessFlags: number = 0;
    vTable: MethodInfo [] = null;
    // This is not really a table per se, but rather a map.
    iTable: { [name: string]: MethodInfo; } = Object.create(null);

    // Custom hash map to make vTable name lookups quicker. It maps utf8 method names to indices in
    // the vTable. A zero value indicate no method by that name exists, while a value > 0 indicates
    // that a method entry at |value - 1| position exists in the vTable whose hash matches they
    // lookup key. We can use this map as a quick way to detect if a method doesn't exist in the
    // vTable.
    private vTableMap: Uint16Array = null;

    fTable: FieldInfo [] = null;

    sizeOfFields: number = 0;
    sizeOfStaticFields: number = 0;

    private resolvedFlags: ResolvedFlags = ResolvedFlags.None;
    private fields: (number | FieldInfo) [] = null;
    private methods: (number | MethodInfo) [] = null;
    private interfaces: (number | ClassInfo) [] = null;
    private allInterfaces: ClassInfo [] = null;

    sourceFile: string = null;
    mangledName: string = null;
    id: number;

    private _name: string = null;
    private _superName: string = null;

    constructor(buffer: Uint8Array) {
      super(buffer, 0);
      this.id = ClassInfo.nextId++;
      registerClassId(this.id, this);
      if (!buffer) {
        sealObjects && Object.seal(this);
        return;
      }
      enterTimeline("ClassInfo");
      var s = this;
      s.skipU4(); // magic
      s.skipU4(); // minor_version and major_version
      this.constantPool = new ConstantPool(s);
      s.seek(this.constantPool.offset);
      this.accessFlags = s.readU2();
      this.utf8Name = this.constantPool.resolveUtf8ClassName(s.readU2());
      this.utf8SuperName = this.constantPool.resolveUtf8ClassName(s.readU2());
      this.vTable = [];
      this.fTable = []
      this.scanInterfaces();
      this.scanFields();
      this.scanMethods();
      this.scanClassInfoAttributes();
      this.mangledName = mangleClassName(this.utf8Name);
      leaveTimeline("ClassInfo");
      sealObjects && Object.seal(this);
    }

    /**
     * Creates synthetic methodInfo objects in abstract classes for all unimplemented
     * interface methods. This is needed so that vTable entries are created correctly
     * for abstract classes that don't otherwise define methods for their implemented
     * interface.
     */
    private createAbstractMethods() {
      // We only do this for abstract classes. Sometimes, interfaces are also marked
      // as abstract but they aren't really.
      if (!this.isAbstract || this.isInterface) {
        return;
      }
      var methods = this.getMethods();
      var interfaces = this.getInterfaces();
      for (var i = 0; i < interfaces.length; i++) {
        var c = interfaces[i];
        for (var j = 0; j < c.methods.length; j++) {
          var methodInfo = c.getMethodByIndex(j);
          if (methodInfo.isStatic || strcmp(methodInfo.utf8Name, UTF8.Init)) {
            // Ignore static methods.
            continue;
          }
          var index = indexOfMethod(methods, methodInfo.utf8Name, methodInfo.utf8Signature, -1);
          if (index < 0) {
            // Make a copy of the interface method info and add it to the current list of
            // virtual class methods. The vTable construction will give this a proper
            // vTable index later.
            var abstractMethod = methodInfo.cloneMethodInfo();
            methods.push(abstractMethod);
          }
        }
      }
    }

    private scanInterfaces() {
      var b = this;
      var interfaces_count = b.readU2();
      this.interfaces = new Array(interfaces_count);
      for (var i = 0; i < interfaces_count; i++) {
        this.interfaces[i] = b.readU2();
      }
    }

    private scanFields() {
      var s = this;
      var fields_count = s.readU2();
      var f = this.fields = new Array(fields_count);
      for (var i = 0; i < fields_count; i++) {
        f[i] = s.offset;
        s.skip(6);
        this.skipAttributes();
      }
    }

    getClassNameSlow(): string {
      return this._name || (this._name = ByteStream.readString(this.utf8Name));
    }

    get superClassName(): string {
      if (this.utf8SuperName) {
        return this._superName || (this._superName = ByteStream.readString(this.utf8SuperName));
      }
      return null;
    }

    /**
     * Gets the class hierarchy in derived -> base order.
     */
    private getClassHierarchy(): ClassInfo [] {
      var classHierarchy = [];
      var classInfo: ClassInfo = this;
      do {
        classHierarchy.push(classInfo);
        classInfo = classInfo.superClass;
      } while (classInfo);
      return classHierarchy;
    }

    private trace(writer: IndentingWriter) {
      writer.enter(this.getClassNameSlow() + " VTable:");
      for (var i = 0; i < this.vTable.length; i++) {
        writer.writeLn(i + ": " + ByteStream.readString(this.vTable[i].utf8Name) + "." + ByteStream.readString(this.vTable[i].utf8Signature));
      }
      writer.leave("");
    }

    public complete() {
      this.createAbstractMethods();
      if (!this.isInterface) {
        this.buildVTable();
        this.buildITable();
        this.buildFTable();
      }
      // Notify the runtime so it can perform and necessary setup.
      if (RuntimeTemplate) {
        RuntimeTemplate.classInfoComplete(this);
      }
      loadWriter && this.trace(loadWriter);
    }

    /**
     * Constructs the VTable for this class by appending to or overriding methods
     * in the super class VTable.
     */
    private buildVTable() {
      var superClassVTable = this.superClass ? this.superClass.vTable : null;
      var vTable = this.vTable = superClassVTable ? superClassVTable.slice() : [];
      var vTableMap = this.vTableMap = new Uint16Array(hashMapSizeMask + 1);
      var superClassVTableMap = null;
      if (this.superClass) {
        superClassVTableMap = this.superClass.vTableMap;
        vTableMap.set(superClassVTableMap);
      }
      var methods = this.methods;
      if (!methods) {
        return;
      }
      for (var i = 0; i < methods.length; i++) {
        var methodInfo: MethodInfo = this.getMethodByIndex(i);
        if (!methodInfo.isStatic && !strcmp(methodInfo.utf8Name, UTF8.Init)) {
          var vTableIndex = -1;
          if (superClassVTable) {
            vTableIndex = getHashMapValue(superClassVTableMap, methodInfo.utf8Name) - 1;
            if (vTableIndex >= 0) { // May exist, but we need to check to make sure the index is correct.
              vTableIndex = indexOfMethod(superClassVTable, methodInfo.utf8Name, methodInfo.utf8Signature, vTableIndex);
            }
          }
          if (vTableIndex < 0) {
            methodInfo.vTableIndex = vTable.length;
            vTable.push(methodInfo); // Append
            setHashMapValue(vTableMap, methodInfo.utf8Name, methodInfo.vTableIndex + 1);
          } else {
            vTable[vTableIndex] = methodInfo; // Override
            methodInfo.vTableIndex = vTableIndex;
          }
        }
      }

      // Go through all the interfaces and mark all methods in the vTable that implement interface methods.
      var interfaces = this.getAllInterfaces();
      for (var i = 0; i < interfaces.length; i++) {
        var c = interfaces[i];
        for (var j = 0; j < c.methods.length; j++) {
          var methodInfo = c.getMethodByIndex(j);
          var vTableIndex = indexOfMethod(this.vTable, methodInfo.utf8Name, methodInfo.utf8Signature, -1);
          if (vTableIndex >= 0) {
            this.vTable[vTableIndex].accessFlags |= ACCESS_FLAGS.J2ME_IMPLEMENTS_INTERFACE;
          }
        }
      }
    }

    private buildITable() {
      var vTable = this.vTable;
      var iTable = this.iTable;
      for (var i = 0; i < vTable.length; i++) {
        var methodInfo = vTable[i];
        // TODO: Find out why only doing this when |methodInfo.implementsInterface| is |true|, fails.
        release || assert(methodInfo.mangledName, "methodInfo.mangledName");
        release || assert(!iTable[methodInfo.mangledName], "!iTable[methodInfo.mangledName]");
        iTable[methodInfo.mangledName] = methodInfo;
      }
    }

    private buildFTable() {
      if (this.superClass === null) {
        this.sizeOfFields = 0;
        this.sizeOfStaticFields = 0;
      } else {
        this.sizeOfFields = this.superClass.sizeOfFields;
        this.sizeOfStaticFields = this.superClass.sizeOfStaticFields;
      }
      var superClassFTable = this.superClass ? this.superClass.fTable : null;
      var fTable = this.fTable = superClassFTable ? superClassFTable.slice() : [];
      var fields = this.fields;
      if (!fields) {
        return;
      }
      for (var i = 0; i < fields.length; i++) {
        var fieldInfo: FieldInfo = this.getFieldByIndex(i);
        if (!fieldInfo.isStatic) {
          fieldInfo.fTableIndex = fTable.length;
          fTable.push(fieldInfo); // Append
          fieldInfo.mangledName = "f" + fieldInfo.fTableIndex;
          fieldInfo.byteOffset = Constants.OBJ_HDR_SIZE + this.sizeOfFields;
          this.sizeOfFields += kindSize(fieldInfo.kind);
        } else {
          fieldInfo.mangledName = "s" + i;
          fieldInfo.byteOffset = Constants.OBJ_HDR_SIZE + this.sizeOfStaticFields;
          this.sizeOfStaticFields += kindSize(fieldInfo.kind);
        }
      }
    }

    private scanMethods() {
      var s = this;
      var methods_count = s.readU2();
      var m = this.methods = new Array(methods_count);
      for (var i = 0; i < methods_count; i++) {
        m[i] = s.offset;
        s.skip(6);
        this.skipAttributes();
      }
    }

    private addVTableEntry(accessFlags, name_index, descriptor_index) {

    }

    private skipAttributes() {
      var s = this;
      var attributes_count = s.readU2();
      for (var i = 0; i < attributes_count; i++) {
        s.readU2();
        s.skip(s.readU4());
      }
    }

    scanClassInfoAttributes() {
      var s = this;
      var attributes_count = s.readU2();
      for (var i = 0; i < attributes_count; i++) {
        var attribute_name_index = s.readU2();
        var attribute_length = s.readU4();
        var o = s.offset;
        s.seek(o + attribute_length);
      }
    }

    getMethodByIndex(i: number): MethodInfo {
      if (typeof this.methods[i] === "number") {
        var methodInfo = this.methods[i] = new MethodInfo(this, <number>this.methods[i], i);
      }
      var methodInfo = <MethodInfo>this.methods[i];
      return methodInfo;
    }

    indexOfMethod(utf8Name: Uint8Array, utf8Signature: Uint8Array): number {
      var methods = this.methods;
      if (!methods) {
        return -1;
      }
      for (var i = 0; i < methods.length; i++) {
        var methodInfo = this.getMethodByIndex(i);
        var methodUTF8Name = methodInfo.utf8Name;
        if (utf8Name.length !== methodUTF8Name.length || utf8Name[0] !== methodUTF8Name[0]) { // Quick false test.
          continue;
        }
        if (strcmp(methodUTF8Name, utf8Name) && strcmp(methodInfo.utf8Signature, utf8Signature)) {
          return i;
        }
      }
      return -1;
    }

    // This should only ever be used from code where the name and signature originate from JS strings.
    getMethodByNameString(name: string, signature: string): MethodInfo {
      return this.getMethodByName(cacheUTF8(name), cacheUTF8(signature));
    }

    // This should only ever be used from code where the name and signature originate from JS strings.
    getLocalMethodByNameString(name: string, signature: string): MethodInfo {
      return this.getLocalMethodByName(toUTF8(name), toUTF8(signature));
    }

    getLocalMethodByName(utf8Name: Uint8Array, utf8Signature: Uint8Array): MethodInfo {
      var i = this.indexOfMethod(utf8Name, utf8Signature);
      if (i >= 0) {
        return this.getMethodByIndex(i);
      }
      return null;
    }

    getMethodByName(utf8Name: Uint8Array, utf8Signature: Uint8Array): MethodInfo {
      var c: ClassInfo = this;
      do {
        var i = c.indexOfMethod(utf8Name, utf8Signature);
        if (i >= 0) {
          return c.getMethodByIndex(i);
        }
        c = c.superClass;
      } while (c);

      if (this.isInterface) {
        var interfaces = this.getInterfaces();
        for (var n = 0; n < interfaces.length; ++n) {
          var method = interfaces[n].getMethodByName(utf8Name, utf8Signature);
          if (method) {
            return method;
          }
        }
      }
      return null;
    }

    getMethodCount(): number {
      return this.methods ? this.methods.length : 0;
    }

    getMethods(): MethodInfo [] {
      if (!this.methods) {
        return ArrayUtilities.EMPTY_ARRAY;
      }
      if (this.resolvedFlags & ResolvedFlags.Methods) {
        return <MethodInfo []>this.methods;
      }
      for (var i = 0; i < this.methods.length; i++) {
        this.getMethodByIndex(i);
      }
      this.resolvedFlags |= ResolvedFlags.Methods;
      return <MethodInfo []>this.methods;
    }

    getFieldByIndex(i: number): FieldInfo {
      if (typeof this.fields[i] === "number") {
        this.fields[i] = new FieldInfo(this, <number>this.fields[i]);
      }
      return <FieldInfo>this.fields[i]
    }

    indexOfField(utf8Name: Uint8Array, utf8Signature: Uint8Array): number {
      var fields = this.fields;
      if (!fields) {
        return -1;
      }
      for (var i = 0; i < fields.length; i++) {
        var fieldInfo = this.getFieldByIndex(i);
        var fieldUTF8Name = fieldInfo.utf8Name;
        if (utf8Name.length !== fieldUTF8Name.length || utf8Name[0] !== fieldUTF8Name[0]) { // Quick false check.
          continue;
        }
        if (strcmp(fieldUTF8Name, utf8Name) && strcmp(fieldInfo.utf8Signature, utf8Signature)) {
          return i;
        }
      }
      return -1;
    }

    getFieldByName(utf8Name: Uint8Array, utf8Signature: Uint8Array, isStatic: boolean): FieldInfo {
      var c: ClassInfo = this;
      do {
        var i = c.indexOfField(utf8Name, utf8Signature);
        if (i >= 0) {
          return c.getFieldByIndex(i);
        }

        if (isStatic) {
          var interfaces = c.getAllInterfaces();
          for (var n = 0; n < interfaces.length; ++n) {
            var field = interfaces[n].getFieldByName(utf8Name, utf8Signature, isStatic);
            if (field) {
              return field;
            }
          }
        }

        c = c.superClass;
      } while (c);
      return null;
    }

    getFields(): FieldInfo [] {
      if (!this.fields) {
        return ArrayUtilities.EMPTY_ARRAY;
      }
      if (this.resolvedFlags & ResolvedFlags.Fields) {
        return <FieldInfo []>this.fields;
      }
      for (var i = 0; i < this.fields.length; i++) {
        this.getFieldByIndex(i);
      }
      this.resolvedFlags |= ResolvedFlags.Fields;
      return <FieldInfo []>this.fields;
    }

    getInterface(i: number): ClassInfo {
      if (typeof this.interfaces[i] === "number") {
        this.interfaces[i] = this.constantPool.resolveClass(<number>this.interfaces[i]);
      }
      return <ClassInfo>this.interfaces[i];
    }

    getInterfaces(): ClassInfo [] {
      if (!this.interfaces) {
        return ArrayUtilities.EMPTY_ARRAY;
      }
      if (this.resolvedFlags & ResolvedFlags.Interfaces) {
        return <ClassInfo []>this.interfaces;
      }
      for (var i = 0; i < this.interfaces.length; i++) {
        this.getInterface(i);
      }
      this.resolvedFlags |= ResolvedFlags.Interfaces;
      return <ClassInfo []>this.interfaces;
    }

    getAllInterfaces(): ClassInfo [] {
      if (this.allInterfaces) {
        return this.allInterfaces;
      }
      var interfaces = this.getInterfaces();
      var list = interfaces.slice();
      for (var i = 0; i < interfaces.length; i++) {
        pushMany(list, interfaces[i].getAllInterfaces());
      }
      if (this.superClass) {
        pushMany(list, this.superClass.getAllInterfaces());
      }
      return this.allInterfaces = unique(list);
    }

    get staticInitializer(): MethodInfo {
      return this.getMethodByNameString("<clinit>", "()V");
    }

    get isInterface(): boolean {
      return !!(this.accessFlags & ACCESS_FLAGS.ACC_INTERFACE);
    }

    get isAbstract(): boolean {
      return !!(this.accessFlags & ACCESS_FLAGS.ACC_ABSTRACT);
    }

    get isFinal(): boolean {
      return !!(this.accessFlags & ACCESS_FLAGS.ACC_FINAL);
    }

    implementsInterface(i: ClassInfo): boolean {
      var classInfo: ClassInfo = this;
      do {
        var interfaces = classInfo.interfaces;
        for (var n = 0; n < interfaces.length; ++n) {
          if (interfaces[n] === i)
            return true;
        }
        classInfo = classInfo.superClass;
      } while (classInfo);
      return false;
    }

    isAssignableTo(toClass: ClassInfo): boolean {
      if (this === toClass) {
        return true;
      }
      if (toClass.isInterface) {
        return this.getAllInterfaces().indexOf(toClass) >= 0;
      } else if (toClass.elementClass) {
        if (!this.elementClass) {
          return false;
        }
        return this.elementClass.isAssignableTo(toClass.elementClass);
      }
      return this.getDisplay()[toClass.depth] === toClass;
    }

    /**
      * Creates lookup tables used to efficiently implement type checks.
      */
    getDisplay() {
      if (this.display !== null) {
        return this.display;
      }
      var display = this.display = new Array(32);

      var i = this.depth;
      var classInfo: ClassInfo = this;
      while (classInfo) {
        display[i--] = classInfo;
        classInfo = classInfo.superClass;
      }
      release || assert(i === -1, "i === -1");
      return this.display;
    }
  }

  export class PrimitiveClassInfo extends ClassInfo {

    constructor(utf8Name, mangledName) {
      super(null);
      this.utf8Name = utf8Name;
      this.mangledName = mangledName;
      this.complete();
    }

    static Z = new PrimitiveClassInfo(UTF8.Z, "boolean");
    static C = new PrimitiveClassInfo(UTF8.C, "char");
    static F = new PrimitiveClassInfo(UTF8.F, "float");
    static D = new PrimitiveClassInfo(UTF8.D, "double");
    static B = new PrimitiveClassInfo(UTF8.B, "byte");
    static S = new PrimitiveClassInfo(UTF8.S, "short");
    static I = new PrimitiveClassInfo(UTF8.I, "int");
    static J = new PrimitiveClassInfo(UTF8.J, "long");
  }

  export class ArrayClassInfo extends ClassInfo {

    // XXX: This constructor should not be called directly.
    constructor(elementClass: ClassInfo) {
      super(null);
      this.elementClass = elementClass;
      this.superClass = CLASSES.java_lang_Object;
      this.superClassName = CLASSES.java_lang_Object.getClassNameSlow();
      this.depth = 1;
    }
  }

  export class ObjectArrayClassInfo extends ArrayClassInfo {
    constructor(elementClass: ClassInfo) {
      super(elementClass);
      if (elementClass instanceof ArrayClassInfo) {
        this.utf8Name = strcatSingle(UTF8Chars.OpenBracket, elementClass.utf8Name);
      } else {
        this.utf8Name = strcat4Single(UTF8Chars.OpenBracket, UTF8Chars.L, elementClass.utf8Name, UTF8Chars.Semicolon);
      }
      this.mangledName = mangleClassName(this.utf8Name);
      this.complete();
    }
  }

  export class PrimitiveArrayClassInfo extends ArrayClassInfo {
    bytesPerElement: number;
    constructor(elementClass: ClassInfo, mangledName: string, bytesPerElement: number) {
      super(elementClass);
      this.utf8Name = strcatSingle(UTF8Chars.OpenBracket, elementClass.utf8Name);
      this.mangledName = mangledName;
      this.bytesPerElement = bytesPerElement;
      this.complete();
    }

    static initialize() {
      // Primitive array classes require the java_lang_Object to exists before they can be created.
      PrimitiveArrayClassInfo.Z = new PrimitiveArrayClassInfo(PrimitiveClassInfo.Z, "ZArray", 1);
      PrimitiveArrayClassInfo.C = new PrimitiveArrayClassInfo(PrimitiveClassInfo.C, "CArray", 2);
      PrimitiveArrayClassInfo.F = new PrimitiveArrayClassInfo(PrimitiveClassInfo.F, "FArray", 4);
      PrimitiveArrayClassInfo.D = new PrimitiveArrayClassInfo(PrimitiveClassInfo.D, "DArray", 8);
      PrimitiveArrayClassInfo.B = new PrimitiveArrayClassInfo(PrimitiveClassInfo.B, "BArray", 1);
      PrimitiveArrayClassInfo.S = new PrimitiveArrayClassInfo(PrimitiveClassInfo.S, "SArray", 2);
      PrimitiveArrayClassInfo.I = new PrimitiveArrayClassInfo(PrimitiveClassInfo.I, "IArray", 4);
      PrimitiveArrayClassInfo.J = new PrimitiveArrayClassInfo(PrimitiveClassInfo.J, "JArray", 8);
    }

    static Z: PrimitiveArrayClassInfo;
    static C: PrimitiveArrayClassInfo;
    static F: PrimitiveArrayClassInfo;
    static D: PrimitiveArrayClassInfo;
    static B: PrimitiveArrayClassInfo;
    static S: PrimitiveArrayClassInfo;
    static I: PrimitiveArrayClassInfo;
    static J: PrimitiveArrayClassInfo;
  }
}
