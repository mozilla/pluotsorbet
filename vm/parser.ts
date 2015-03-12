/*
 node-jvm
 Copyright (c) 2013 Yaroslav Gaponov <yaroslav.gaponov@gmail.com>
*/

module J2ME {
  declare var util;
  import assert = J2ME.Debug.assert;
  import concat5 = StringUtilities.concat5;
  import pushUnique = ArrayUtilities.pushUnique;
  import pushMany = ArrayUtilities.pushMany;
  import unique = ArrayUtilities.unique;

  export enum UTF8Chars {
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
    export var OpenBracket = new Uint8Array([UTF8Chars.OpenBracket]);
    export var OpenBracketL = new Uint8Array([UTF8Chars.OpenBracket, UTF8Chars.L]);
    export var Semicolon = new Uint8Array([UTF8Chars.Semicolon]);

    export var Z = new Uint8Array([UTF8Chars.Z]);
    export var C = new Uint8Array([UTF8Chars.C]);
    export var F = new Uint8Array([UTF8Chars.F]);
    export var D = new Uint8Array([UTF8Chars.D]);
    export var B = new Uint8Array([UTF8Chars.B]);
    export var S = new Uint8Array([UTF8Chars.S]);
    export var I = new Uint8Array([UTF8Chars.I]);
    export var J = new Uint8Array([UTF8Chars.J]);

    // "<init>".split("").map(function (x) { return String.charCodeAt(x); })
  }

  export function strcmp(a: Uint8Array, b: Uint8Array): boolean {
    if (a === b) {
      return true;
    }
    if (a.length !== b.length) {
      return false;
    }
    for (var i = 0; i < a.length; i++) {
      if (a[i] !== b[i]) {
        return false;
      }
    }
    return true;
  }

  export function toUTF8(s: string): Uint8Array {
    var r = new Uint8Array(s.length);
    for (var i = 0; i < s.length; i++) {
      var c = s.charCodeAt(i);
      release || assert(c <= 0x7f);
      r[i] = c;
    }
    return r;
  }

  export function fromUTF8(s: Uint8Array): string {
    return ByteStream.readString(s);
  }

  function strcat(a: Uint8Array, b: Uint8Array): Uint8Array {
    var r = new Uint8Array(a.length + b.length);
    r.set(a, 0);
    r.set(b, a.length);
    return r;
  }

  function strcat3(a: Uint8Array, b: Uint8Array, c: Uint8Array): Uint8Array {
    var r = new Uint8Array(a.length + b.length + c.length);
    r.set(a, 0);
    r.set(b, a.length);
    r.set(c, a.length + b.length);
    return r;
  }

  /**
   * Base class of all class file structs.
   */
  export class ByteStream {

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

    readBytes(length): Uint8Array {
      var data = this.buffer.subarray(this.offset, this.offset + length);
      this.offset += length;
      return data;
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

  export enum ACCESS_FLAGS {
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

  export enum TAGS {
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
      this.resolved = new Array(c);
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
      release || assert(b[this.entries[i]] === tag);
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
      release || assert(tag === TAGS.CONSTANT_String);
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
        release || Debug.assert(expectedTag === TAGS.CONSTANT_Any || expectedTag === tag || (expectedTag === TAGS.CONSTANT_Methodref && tag === TAGS.CONSTANT_InterfaceMethodref));
        switch (s.readU1()) {
            case TAGS.CONSTANT_Integer:
              r = this.resolved[i] = s.readS4();
              break;
            case TAGS.CONSTANT_Float:
              r = this.resolved[i] = IntegerUtilities.int32ToFloat(s.readU4());
              break;
            case TAGS.CONSTANT_String:
              r = this.resolved[i] = $.newStringConstant(this.resolveUtf8String(s.readU2()));
              break;
            case TAGS.CONSTANT_Long:
              var high = s.readU4();
              var low = s.readU4();
              r = this.resolved[i] = Long.fromBits(low, high);
              break;
            case TAGS.CONSTANT_Double:
              r = this.resolved[i] = IntegerUtilities.int64ToDouble(s.readU4(), s.readU4());
              break;
          case TAGS.CONSTANT_Utf8:
            r = this.resolved[i] = s.readBytes(s.readU2());
            break;
          case TAGS.CONSTANT_Class:
            r = this.resolved[i] = CLASSES.getClass(util.decodeUtf8Array(this.resolve(s.readU2(), TAGS.CONSTANT_Utf8)));
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
              r = this.resolved[i] = classInfo.getFieldByName(name, type, isStatic);
            } else {
              r = this.resolved[i] = classInfo.getMethodByName(name, type);
            }
            if (!r) {
              throw $.newRuntimeException(classInfo.getClassNameSlow() + "." + fromUTF8(name) + "." + fromUTF8(type) + " not found");
            }
            break;
          default:
            assert(false);
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
    public utf8Name: Uint8Array;
    public utf8Signature: Uint8Array;
    public mangledName: string;
    public accessFlags: ACCESS_FLAGS;
    public constantValue: any;

    fTableIndex: number;

    constructor(classInfo: ClassInfo, offset: number) {
      super(classInfo.buffer, offset);
      this.classInfo = classInfo;
      this.accessFlags = this.readU2();
      this.utf8Name = classInfo.constantPool.resolveUtf8(this.readU2());
      this.utf8Signature = classInfo.constantPool.resolveUtf8(this.readU2());
      this.kind = getSignatureKind(this.utf8Signature);
      this.constantValue = undefined;
      this.scanFieldInfoAttributes();
    }

    public get isStatic(): boolean {
      return !!(this.accessFlags & ACCESS_FLAGS.ACC_STATIC);
    }

    public get(object: java.lang.Object) {
      return object[this.mangledName];
    }

    public set(object: java.lang.Object, value: any) {
      object[this.mangledName] = value
    }

    public getStatic() {
      return this.get(this.classInfo.getStaticObject($.ctx));
    }

    public setStatic(value: any) {
      return this.set(this.classInfo.getStaticObject($.ctx), value);
    }

    private scanFieldInfoAttributes() {
      var s = this;
      var attributes_count = s.readU2();
      var constantvalue_index = -1;
      for (var i = 0; i < attributes_count; i++) {
        var attribute_name_index = s.readU2();
        var attribute_length = s.readU4();
        var o = s.offset;
        var attribute_name = this.classInfo.constantPool.resolveUtf8(attribute_name_index);
        if (strcmp(attribute_name, UTF8.ConstantValue)) {
          release || assert(attribute_length === 2, "Attribute length of ConstantValue must be 2.")
          constantvalue_index = s.readU2();
        }
        s.seek(o + attribute_length);
      }
      if (phase !== ExecutionPhase.Compiler) {
        if (constantvalue_index >= 0) {
          this.constantValue = this.classInfo.constantPool.resolve(constantvalue_index, TAGS.CONSTANT_Any);
        }
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


  export function mangleClass(classInfo: ClassInfo): string {
    var name = StringUtilities.variableLengthEncodeInt32(hashUTF8String(classInfo.utf8Name));
    // Also use the length for some more precision.
    name += StringUtilities.toEncoding(classInfo.utf8Name.length & 0x3f);
    return "$" + name;
  }

  export function mangleMethod(methodInfo: MethodInfo) {
    // TODO: Get rid of JS strings in the computation of the hash.
    var name = methodInfo.name + "_" + hashStringToString(methodInfo.signature);
    if (!hashedMangledNames) {
      return escapeString(name);
    }
    return "$" + hashStringToString(name);
    assert(false);
  }

  export class MethodInfo extends ByteStream {
    public classInfo: ClassInfo;
    public accessFlags: ACCESS_FLAGS;

    public fn: any;
    public index: number;
    public state: MethodState;
    public stats: MethodInfoStats;
    public codeAttribute: CodeAttribute;
    public utf8Name: Uint8Array;
    public utf8Signature: Uint8Array;
    public returnKind: Kind;

    public argumentSlots: number;
    public consumeArgumentSlots: number;
    public hasTwoSlotArguments: boolean;

    vTableIndex: number;

    private _virtualName: string;
    private _mangledName: string;
    private _mangledClassAndMethodName: string;
    private _signatureDescriptor: SignatureDescriptor;

    private _implKey: string;
    private _name: string;
    private _signature: string;

    ///// FIX THESE LATER ////
    onStackReplacementEntryPoints: number [];

    exception_table_length: number;
    exception_table_offset: number;
    isOptimized: boolean;

    constructor(classInfo: ClassInfo, offset: number, index: number) {
      super(classInfo.buffer, offset);
      this.index = index;
      this.accessFlags = this.u2(0);
      this.classInfo = classInfo;
      var cp = this.classInfo.constantPool;
      this.utf8Name = cp.resolveUtf8(this.u2(2));
      this.utf8Signature = cp.resolveUtf8(this.u2(4));
      this.vTableIndex = -1;

      // Lazify;
      this.state = MethodState.Cold;

      // TODO: Make this lazy.
      this.stats = new MethodInfoStats();
      this.codeAttribute = null;
      this.scanMethodInfoAttributes();


      // Parse signature and cache some useful information.
      var signatureKinds = parseMethodDescriptorKinds(this.utf8Signature, 0);
      this.returnKind = signatureKinds[0];
      this.hasTwoSlotArguments = signatureHasTwoSlotArguments(signatureKinds);
      this.argumentSlots = signatureArgumentSlotCount(signatureKinds);
      this.consumeArgumentSlots = this.argumentSlots;
      if (!this.isStatic) {
        this.consumeArgumentSlots ++;
      }
    }

    get signatureDescriptor() {
      return this._signatureDescriptor || (this._signatureDescriptor = SignatureDescriptor.makeSignatureDescriptor(this.signature));
    }

    get virtualName() {
      if (this.vTableIndex >= 0) {
        return this._virtualName || (this._virtualName = "m" + this.vTableIndex);
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

  enum ResolvedFlags {
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
      this.code = s.readBytes(code_length);
    }
  }

  function indexOfMethod(table: MethodInfo [], utf8Name: Uint8Array, utf8Signature: Uint8Array): number {
    for (var i = 0; i < table.length; i++) {
      var methodInfo = table[i];
      if (strcmp(utf8Name, methodInfo.utf8Name) && strcmp(utf8Signature, methodInfo.utf8Signature)) {
        return i;
      }
    }
    return -1;
  }

  export enum Detail {
    Brief = 0x01,
    Full  = 0x03
  }

  export class ClassInfo extends ByteStream {
    constantPool: ConstantPool;

    utf8Name: Uint8Array;
    utf8SuperName: Uint8Array;

    superClass: ClassInfo = null;
    subClasses: ClassInfo [] = [];
    allSubClasses: ClassInfo [] = [];

    accessFlags: number;
    vTable: MethodInfo [];
    fTable: FieldInfo [];

    klass: Klass = null;
    private resolvedFlags: ResolvedFlags = ResolvedFlags.None;
    private fields: (number | FieldInfo) [] = null;
    private methods: (number | MethodInfo) [] = null;
    private interfaces: (number | ClassInfo) [] = null;
    private allInterfaces: ClassInfo [] = null;

    sourceFile: string;
    mangledName: string;

    // TODO: Remove me:

    private _name: string;

    constructor(buffer: Uint8Array) {
      super(buffer, 0);
      if (!buffer) {
        return;
      }
      enterTimeline("ClassInfo");
      var s = this;
      s.readU4(); // magic
      s.readU2(); // minor_version
      s.readU2(); // major_version
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
      this.mangledName = mangleClass(this);
      leaveTimeline("ClassInfo");
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
        return ByteStream.readString(this.utf8SuperName);
      }
      return null;
    }

    /**
     * Gets the class hierarchy in derived -> base order.
     */
    private getClassHierarchy(): ClassInfo [] {
      var classHierarchy = [];
      var classInfo = this;
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
      if (!this.isInterface) {
        this.buildVTable();
        this.buildFTable();
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
      var methods = this.methods;
      for (var i = 0; i < methods.length; i++) {
        var methodInfo: MethodInfo = this.getMethodByIndex(i);
        if (!methodInfo.isStatic && !strcmp(methodInfo.utf8Name, UTF8.Init)) {
          var vTableIndex = superClassVTable ? indexOfMethod(superClassVTable, methodInfo.utf8Name, methodInfo.utf8Signature) : -1;
          if (vTableIndex < 0) {
            methodInfo.vTableIndex = vTable.length;
            vTable.push(methodInfo); // Append
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
          var vTableIndex = indexOfMethod(this.vTable, methodInfo.utf8Name, methodInfo.utf8Signature);
          if (vTableIndex >= 0) {
            this.vTable[vTableIndex].accessFlags |= ACCESS_FLAGS.J2ME_IMPLEMENTS_INTERFACE;
          }
        }
      }
    }

    private buildFTable() {
      var superClassFTable = this.superClass ? this.superClass.fTable : null;
      var fTable = this.fTable = superClassFTable ? superClassFTable.slice() : [];
      var fields = this.fields;
      for (var i = 0; i < fields.length; i++) {
        var fieldInfo: FieldInfo = this.getFieldByIndex(i);
        if (!fieldInfo.isStatic) {
          fieldInfo.fTableIndex = fTable.length;
          fTable.push(fieldInfo); // Append
          fieldInfo.mangledName = "f" + fieldInfo.fTableIndex;
        } else {
          fieldInfo.mangledName = "s" + i;
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
        enterTimeline("MethodInfo");
        var methodInfo = this.methods[i] = new MethodInfo(this, <number>this.methods[i], i);
        leaveTimeline("MethodInfo");
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
        var method = this.getMethodByIndex(i);
        if (strcmp(method.utf8Name, utf8Name) && strcmp(method.utf8Signature, utf8Signature)) {
          return i;
        }
      }
      return -1;
    }

    // This should only ever be used from code where the name and signature originate from JS strings.
    getMethodByNameString(name: string, signature: string): MethodInfo {
      return this.getMethodByName(toUTF8(name), toUTF8(signature));
    }

    getMethodByName(utf8Name: Uint8Array, utf8Signature: Uint8Array): MethodInfo {
      if (typeof utf8Name === "string") {
        debugger;
      }
      var c = this;
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
        var field = this.getFieldByIndex(i);
        if (strcmp(field.utf8Name, utf8Name) &&
            strcmp(field.utf8Signature, utf8Signature)) {
          return i;
        }
      }
      return -1;
    }

    getFieldByName(utf8Name: Uint8Array, utf8Signature: Uint8Array, isStatic: boolean): FieldInfo {
      if (typeof utf8Name === "string") {
        debugger;
      }
      var c = this;
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

    // DEPRECATED use getFieldByName
    getField(key: string): FieldInfo {
      var isStatic = key[0] === "S";
      var secondDot = key.indexOf(".", 2);
      var name = key.substring(2, secondDot);
      var signature = key.substr(secondDot + 1);
      return this.getFieldByName(toUTF8(name), toUTF8(signature), isStatic);
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

    /**
     * Object that holds static properties for this class.
     */
    getStaticObject(ctx: Context): java.lang.Object {
      return <java.lang.Object><any>ctx.runtime.getRuntimeKlass(this.klass);
    }

    get isInterface(): boolean {
      return !!(this.accessFlags & ACCESS_FLAGS.ACC_INTERFACE);
    }

    get isFinal(): boolean {
      return !!(this.accessFlags & ACCESS_FLAGS.ACC_FINAL);
    }

    implementsInterface(i: ClassInfo): boolean {
      var classInfo = this;
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
      if (this === toClass || toClass === CLASSES.java_lang_Object)
        return true;
      if (toClass.isInterface && this.implementsInterface(toClass))
        return true;
      return this.superClass ? this.superClass.isAssignableTo(toClass) : false;
    }

    /**
     * java.lang.Class object for this class info. This is a not where static properties
     * are stored for this class.
     */
    getClassObject(): java.lang.Class {
      return $.getRuntimeKlass(this.klass).classObject;
    }
  }

  export class PrimitiveClassInfo extends ClassInfo {

    constructor(utf8Name, mangledName) {
      super(null);
      this.utf8Name = utf8Name;
      this.mangledName = mangledName;
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
    elementClass: ClassInfo;

    // XXX: This constructor should not be called directly.
    constructor(elementClass: ClassInfo) {
      super(null);
      this.elementClass = elementClass;
      this.superClass = CLASSES.java_lang_Object;
      this.superClassName = CLASSES.java_lang_Object.getClassNameSlow();
    }

    isAssignableTo(toClass: ClassInfo): boolean {
      if (this === toClass || toClass === CLASSES.java_lang_Object)
        return true;
      if (toClass.isInterface && this.implementsInterface(toClass))
        return true;
      if (toClass instanceof ArrayClassInfo) {
        if (this.elementClass && toClass.elementClass)
          return this.elementClass.isAssignableTo(toClass.elementClass);
      } else {
        return false;
      }
      return this.superClass ? this.superClass.isAssignableTo(toClass) : false;
    }
  }

  export class ObjectArrayClassInfo extends ArrayClassInfo {
    constructor(elementClass: ClassInfo) {
      super(elementClass);
      if (elementClass instanceof ArrayClassInfo) {
        this.utf8Name = strcat(UTF8.OpenBracket, elementClass.utf8Name);
      } else {
        this.utf8Name = strcat3(UTF8.OpenBracketL, elementClass.utf8Name, UTF8.Semicolon);
      }
      this.mangledName = mangleClassName(this.getClassNameSlow());
    }
  }

  export class PrimitiveArrayClassInfo extends ArrayClassInfo {
    constructor(elementClass: ClassInfo, mangledName: string) {
      super(elementClass);
      this.utf8Name = strcat(UTF8.OpenBracket, elementClass.utf8Name);
      this.mangledName = mangledName;
    }
    
    static initialize() {
      // Primitive array classes require the java_lang_Object to exists before they can be created.
      PrimitiveArrayClassInfo.Z = new PrimitiveArrayClassInfo(PrimitiveClassInfo.Z, "Uint8Array");
      PrimitiveArrayClassInfo.C = new PrimitiveArrayClassInfo(PrimitiveClassInfo.C, "Uint16Array");
      PrimitiveArrayClassInfo.F = new PrimitiveArrayClassInfo(PrimitiveClassInfo.F, "Float32Array");
      PrimitiveArrayClassInfo.D = new PrimitiveArrayClassInfo(PrimitiveClassInfo.D, "Float64Array");
      PrimitiveArrayClassInfo.B = new PrimitiveArrayClassInfo(PrimitiveClassInfo.B, "Int8Array");
      PrimitiveArrayClassInfo.S = new PrimitiveArrayClassInfo(PrimitiveClassInfo.S, "Int16Array");
      PrimitiveArrayClassInfo.I = new PrimitiveArrayClassInfo(PrimitiveClassInfo.I, "Int32Array");
      PrimitiveArrayClassInfo.J = new PrimitiveArrayClassInfo(PrimitiveClassInfo.J, "Int64Array");
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
