module J2ME {
  declare var util;
  import assert = J2ME.Debug.assert;
  var utf8ToString = util.decodeUtf8Array;

  module UTF8 {
    export var Code = new Uint8Array([67, 111, 100, 101]);
    export var InnerClasses = new Uint8Array([67, 111, 100, 101]);
  }

  function strcmp(a: Uint8Array, b: Uint8Array): boolean {
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

  export class ByteStream {
    static arrays: string [][] = ArrayUtilities.makeArrays(128);

    static getArray(length: number) {
      return Reader.arrays[length];
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
      return this.readI32() >>> 0;
    }

    readI32() {
      var o = this.offset;
      var buffer = this.buffer;
      var a = buffer[o + 0];
      var b = buffer[o + 1];
      var c = buffer[o + 2];
      var d = buffer[o + 3];
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

    seek(offset: number): ByteStream {
      this.offset = offset;
      return this;
    }

    skip(length: number): ByteStream {
      this.offset += length;
      return this;
    }

    // Decode Java's modified UTF-8 (JVM specs, $ 4.4.7)
    // http://docs.oracle.com/javase/specs/jvms/se5.0/html/ClassFile.doc.html#7963
    readStringFast(length: number): string {
      var a = (length < 128) ? Reader.getArray(length) : new Array(length);
      var i = 0, j = 0;
      var o = this.offset;
      var e = o + length;
      var buffer = this.buffer;
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
        var c = this.buffer[this.offset];
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
        var data = new Uint8Array(this.buffer.buffer, this.offset, length);
        var s = util.decodeUtf8Array(data);
        this.offset += length;
        return s;
      } catch (e) {
        return this.readStringFast(length);
      }
    }

    readBytes(length): Uint8Array {
      var data = this.buffer.subarray(this.offset, this.offset + length);
      this.offset += length;
      return data;
    }

    //static readU4(buffer: Uint8Array, o: number): number {
    //  return ByteStream.readI32(buffer, o) >>> 0;
    //}
    //
    //static readI32(buffer: Uint8Array, o: number): number {
    //  var a = buffer[o + 0];
    //  var b = buffer[o + 1];
    //  var c = buffer[o + 2];
    //  var d = buffer[o + 3];
    //  return (a << 24) | (b << 16) | (c << 8) | d;
    //}
    //
    static readU16(buffer: Uint8Array, o: number): number {
      return buffer[o] << 8 | buffer[o + 1];
    }
  }

  export class ExceptionHandler {
    start_pc: number;
    end_pc: number;
    handler_pc: number;
    catch_type: number;
  }

  export enum ACCESS_FLAGS {
    ACC_PUBLIC = 0x0001,
    ACC_PRIVATE = 0x0002,
    ACC_PROTECTED = 0x0004,
    ACC_STATIC = 0x0008,
    ACC_FINAL = 0x0010,
    ACC_SYNCHRONIZED = 0x0020,
    ACC_VOLATILE = 0x0040,
    ACC_TRANSIENT = 0x0080,
    ACC_NATIVE = 0x0100,
    ACC_INTERFACE = 0x0200,
    ACC_ABSTRACT = 0x0400
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
    CONSTANT_Unicode = 2
  }

  export class ConstantPool extends ByteStream {
    entries: Uint32Array;
    resolved: any [];

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

    readTagU2(i: number, tag: TAGS, offset: number) {
      var b = this.buffer;
      release || assert(b[this.entries[i]] === tag);
      var o = this.entries[i] + offset;
      return b[o] << 8 | b[o + 1];
    }

    seekTag(i: number, tag: TAGS) {
      this.seek(this.entries[i]);
      release || assert(this.peekU1() === tag);
    }

    /**
     * This causes the Utf8 string to be redecoded each time.
     */
    resolveUtf8String(i: number): string {
      if (i === 0) return null;
      return utf8ToString(this.resolveUtf8(i));
    }

    resolveUtf8ClassNameString(i: number): string {
      if (i === 0) return null;
      return this.resolveUtf8String(this.readTagU2(i, TAGS.CONSTANT_Class, 1));
    }

    resolve(i: number, tag: TAGS, isStatic: boolean = false): any {
      var s = this, r = this.resolved[i];
      if (r === undefined) {
        this.seekTag(i, tag);
        switch (s.readU1()) {
          case TAGS.CONSTANT_Utf8:
            r = this.resolved[i] = s.readBytes(s.readU2());
            break;
          case TAGS.CONSTANT_Class:
            r = this.resolved[i] = CLASSES.loadClass(utf8ToString(this.resolve(s.readU2(), TAGS.CONSTANT_Utf8)));
            break;
          case TAGS.CONSTANT_Fieldref:
          case TAGS.CONSTANT_Methodref:
          case TAGS.CONSTANT_InterfaceMethodref:
            var class_index = s.readU2();
            var name_and_type_index = s.readU2();
            var classInfo = this.resolveClass(class_index);
            var name_index = this.readTagU2(name_and_type_index, TAGS.CONSTANT_NameAndType, 1);
            var type_index = this.readTagU2(name_and_type_index, TAGS.CONSTANT_NameAndType, 3);
            var name = this.resolveUtf8String(name_index);
            var type = this.resolveUtf8String(type_index);
            if (tag === TAGS.CONSTANT_Fieldref) {
              r = this.resolved[i] = classInfo.getFieldByName(name, type, isStatic);
            } else {
              r = this.resolved[i] = classInfo.getMethodByName(name, type, isStatic);
            }
            if (!r) {
              throw $.newRuntimeException(classInfo.className + "." + name + "." + type + " not found");
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
    classInfo: ClassInfo;
    name: string;
    mangledName: string;
    signature: string;
    kind: Kind;

    access_flags: ACCESS_FLAGS;

    constructor(classInfo: ClassInfo, offset: number) {
      super(classInfo.buffer, offset);
      this.classInfo = classInfo;
      this.access_flags = this.u2(0);
      this.name = classInfo.constantPool.resolveUtf8String(this.u2(2));
      this.signature = classInfo.constantPool.resolveUtf8String(this.u2(4));
      this.kind = getSignatureKind(this.signature);
    }

    get isStatic(): boolean {
      return !!(this.access_flags & ACCESS_FLAGS.ACC_STATIC);
    }

    get(object: java.lang.Object) {
      return object[this.mangledName];
    }

    set(object: java.lang.Object, value: any) {
      object[this.mangledName] = value
    }

    getStatic() {
      return this.get(this.classInfo.getStaticObject($.ctx));
    }

    setStatic(value: any) {
      return this.set(this.classInfo.getStaticObject($.ctx), value);
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

  export class MethodInfo extends ByteStream {
    classInfo: ClassInfo;
    name: string;
    signature: string;

    ///// FIX THESE LATER ////
    fn: any;

    offset: number;
    code: Uint8Array;
    codeAttribute: CodeAttribute;

    state: MethodState;
    mangledName: string;
    mangledClassAndMethodName: string;

    onStackReplacementEntryPoints: number [];

    callCount: number;
    bytecodeCount: number;
    backwardsBranchCount: number;
    interpreterCallCount: number;

    argumentSlots: number;

    /**
     * The number of arguments to pop of the stack when calling this function.
     */
    consumeArgumentSlots: number;

    hasTwoSlotArguments: boolean;

    // Remove these
    max_locals: number;
    max_stack: number;

    exception_table: any [];
    implKey: string;
    isOptimized: boolean;
    signatureDescriptor: SignatureDescriptor;

    constructor(classInfo: ClassInfo, offset: number) {
      super(classInfo.buffer, offset);
      this.classInfo = classInfo;
      this.name = classInfo.constantPool.resolveUtf8String(this.name_index);
      this.signature = classInfo.constantPool.resolveUtf8String(this.descriptor_index);
      this.implKey = this.classInfo.className + "." + this.name + "." + this.signature;
      this.scanMethodInfoAttributes();

      // TODO: make this lazy
      this.signatureDescriptor = SignatureDescriptor.makeSignatureDescriptor(this.signature);
      this.hasTwoSlotArguments = this.signatureDescriptor.hasTwoSlotArguments();
      this.argumentSlots = this.signatureDescriptor.getArgumentSlotCount();
      this.consumeArgumentSlots = this.argumentSlots;
      this.mangledName = mangleMethod(this);
      if (!this.isStatic) {
        this.consumeArgumentSlots ++;
      }
    }

    get access_flags(): number {
      return this.u2(0);
    }

    get name_index(): number {
      return this.u2(2);
    }

    get descriptor_index(): number {
      return this.u2(4);
    }

    public getReturnKind(): Kind {
      return this.signatureDescriptor.typeDescriptors[0].kind;
    }

    get isNative(): boolean {
      return !!(this.access_flags & ACCESS_FLAGS.ACC_NATIVE);
    }

    get isFinal(): boolean {
      return !!(this.access_flags & ACCESS_FLAGS.ACC_FINAL);
    }

    get isPublic(): boolean {
      return !!(this.access_flags & ACCESS_FLAGS.ACC_PUBLIC);
    }

    get isStatic(): boolean {
      return !!(this.access_flags & ACCESS_FLAGS.ACC_STATIC);
    }

    get isSynchronized(): boolean {
      return !!(this.access_flags & ACCESS_FLAGS.ACC_SYNCHRONIZED);
    }

    get isAbstract(): boolean {
      return !!(this.access_flags & ACCESS_FLAGS.ACC_ABSTRACT);
    }

    getSourceLocationForPC(pc: number): SourceLocation {
      return null;
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
        }
        s.seek(o + attribute_length);
      }
      this.seek(b);
    }
  }

  //export class MethodInfo {
  //  access_flags: number;
  //  name_index: number;
  //  descriptor_index: number;
  //
  //  fn: any;
  //
  //
  //  classInfo: ClassInfo;
  //  offset: number;
  //  code: Uint8Array;
  //  codeAttribute: CodeAttribute;
  //
  //  name: string;
  //
  //  state: MethodState;
  //  signature: string;
  //  mangledName: string;
  //  mangledClassAndMethodName: string;
  //
  //  onStackReplacementEntryPoints: number [];
  //
  //  callCount: number;
  //  bytecodeCount: number;
  //  backwardsBranchCount: number;
  //  interpreterCallCount: number;
  //
  //  argumentSlots: number;
  //
  //  /**
  //   * The number of arguments to pop of the stack when calling this function.
  //   */
  //  consumeArgumentSlots: number;
  //
  //  hasTwoSlotArguments: boolean;
  //
  //  // Remove these
  //  max_locals: number;
  //  max_stack: number;
  //
  //  exception_table: any [];
  //  implKey: string;
  //  isOptimized: boolean;
  //  signatureDescriptor: SignatureDescriptor;
  //
  //  constructor(classInfo: ClassInfo, offset: number) {
  //    this.classInfo = classInfo;
  //    this.offset = offset;
  //
  //    var b = classInfo.bytes.seek(offset);
  //    this.access_flags = b.readU2();
  //    this.name_index = b.readU2();
  //    this.descriptor_index = b.readU2();
  //    this.scanMethodInfoAttributes(b);
  //  }
  //
  //  scanMethodInfoAttributes(s: ByteStream) {
  //    var count = s.readU2();
  //    for (var i = 0; i < count; i++) {
  //      var attribute_name_index = s.readU2();
  //      var attribute_length = s.readU4();
  //      var o = s.offset;
  //      var attribute_name = this.classInfo.constantPool.utf8(attribute_name_index);
  //      if (strcmp(attribute_name, UTF8.Code)) {
  //        this.codeAttribute = new CodeAttribute(this.classInfo, o);
  //      }
  //      s.seek(o + attribute_length);
  //    }
  //  }
  //
  //  public getReturnKind(): Kind {
  //    return this.signatureDescriptor.typeDescriptors[0].kind;
  //  }
  //
  //  get isNative(): boolean {
  //    return !!(this.access_flags & ACCESS_FLAGS.ACC_NATIVE);
  //  }
  //
  //  get isFinal(): boolean {
  //    return !!(this.access_flags & ACCESS_FLAGS.ACC_FINAL);
  //  }
  //
  //  get isPublic(): boolean {
  //    return !!(this.access_flags & ACCESS_FLAGS.ACC_PUBLIC);
  //  }
  //
  //  get isStatic(): boolean {
  //    return !!(this.access_flags & ACCESS_FLAGS.ACC_STATIC);
  //  }
  //
  //  get isSynchronized(): boolean {
  //    return !!(this.access_flags & ACCESS_FLAGS.ACC_SYNCHRONIZED);
  //  }
  //
  //  get isAbstract(): boolean {
  //    return !!(this.access_flags & ACCESS_FLAGS.ACC_ABSTRACT);
  //  }
  //
  //  getSourceLocationForPC(pc: number): SourceLocation {
  //    return null;
  //  }
  //
  //
  //}

  enum ResolvedFlags {
    None          = 0,
    Fields        = 1,
    Methods       = 2,
    Classes       = 4,
    Interfaces    = 8
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
      var exception_table_length = s.readU2();
    }
  }

  export class ClassInfo extends ByteStream {
    constantPool: ConstantPool;
    className: string;
    superClassName: string;
    superClass: ClassInfo = null;
    subClasses: ClassInfo [] = [];
    allSubClasses: ClassInfo [] = [];

    ////////// Clean Up ////////////

    access_flags: number;
    // this_class: number;
    // super_class: number;




    staticInitializer: MethodInfo;

    klass: Klass = null;
    private resolvedFlags: ResolvedFlags = ResolvedFlags.None;
    private fields: (number | FieldInfo) [] = null;
    private methods: (number | MethodInfo) [] = null;
    private classes: (number | ClassInfo) [] = null;
    private interfaces: (number | ClassInfo) [] = null;

    sourceFile: string;
    mangledName: string;

    // Delete me:
    constant_pool: any;
    thread: any;

    constructor(buffer: Uint8Array) {
      super(buffer, 0);
      if (!buffer) {
        return;
      }
      var s = this;
      s.readU4(); // magic
      s.readU2(); // minor_version
      s.readU2(); // major_version
      this.constantPool = new ConstantPool(s);
      s.seek(this.constantPool.offset);
      this.access_flags = s.readU2();

      this.className = this.constantPool.resolveUtf8ClassNameString(s.readU2());
      this.superClassName = this.constantPool.resolveUtf8ClassNameString(s.readU2());

      this.scanInterfaces();
      this.scanFields();
      this.scanMethods();
      this.scanClassInfoAttributes();

      this.mangledName = mangleClass(this);
      this.mangleFields();
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

    private mangleFields() {
      // Keep track of how many times a field name was used and resolve conflicts by
      // prefixing filed names with numbers.
      var classInfo: ClassInfo;
      var classHierarchy = this.getClassHierarchy();
      var count = Object.create(null);
      for (var i = classHierarchy.length - 1; i >= 0; i--) {
        classInfo = classHierarchy[i];
        var fields = classInfo.getFields();
        for (var j = 0; j < fields.length; j++) {
          var field = fields[j];
          var fieldName = field.name;
          if (count[field.name] === undefined) {
            count[fieldName] = 0;
          }
          var fieldCount = count[fieldName];
          // Only mangle this classInfo's fields.
          if (i === 0) {
            field.mangledName = "$" + (fieldCount ? "$" + fieldCount : "") + field.name;
          }
          count[fieldName] ++;
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
        var attribute_name = this.constantPool.resolveUtf8(attribute_name_index);
        if (strcmp(attribute_name, UTF8.InnerClasses)) {
          var number_of_classes = s.readU2();
          assert (!this.classes);
          this.classes = new Array(number_of_classes);
          for(var i = 0; i < number_of_classes; i++) {
            this.classes[i] = s.offset;
            s.skip(8);
          }
        }
        s.seek(o + attribute_length);
      }
    }

    getMethod(i: number): MethodInfo {
      if (typeof this.methods[i] === "number") {
        this.methods[i] = new MethodInfo(this, <number>this.methods[i]);
      }
      return <MethodInfo>this.methods[i];
    }

    indexOfMethod(name: string, signature: string, isStatic: boolean): number {
      var methods = this.methods;
      if (!methods) {
        return -1;
      }
      for (var i = 0; i < methods.length; i++) {
        var method = this.getMethod(i);
        if (method.name === name && method.signature === signature && method.isStatic === isStatic) {
          return i;
        }
      }
      return -1;
    }

    getMethodByName(name: string, signature: string, isStatic: boolean): MethodInfo {
      var c = this;
      do {
        var i = c.indexOfMethod(name, signature, isStatic);
        if (i >= 0) {
          return c.getMethod(i);
        }
        c = c.superClass;
      } while (c);
      return null;
    }

    getMethods(): MethodInfo [] {
      if (!this.methods) {
        return null;
      }
      if (this.resolvedFlags & ResolvedFlags.Methods) {
        return <MethodInfo []>this.methods;
      }
      for (var i = 0; i < this.methods.length; i++) {
        this.getMethod(i);
      }
      this.resolvedFlags |= ResolvedFlags.Methods;
      return <MethodInfo []>this.methods;
    }

    getField(i: number): FieldInfo {
      if (typeof this.fields[i] === "number") {
        this.fields[i] = new FieldInfo(this, <number>this.fields[i]);
      }
      return <FieldInfo>this.fields[i];
    }

    indexOfField(name: string, signature: string, isStatic: boolean): number {
      var fields = this.fields;
      if (!fields) {
        return -1;
      }
      for (var i = 0; i < fields.length; i++) {
        var field = this.getField(i);
        if (field.name === name && field.signature === signature && field.isStatic === isStatic) {
          return i;
        }
      }
      return -1;
    }

    getFieldByName(name: string, signature: string, isStatic: boolean): FieldInfo {
      var c = this;
      do {
        var i = c.indexOfField(name, signature, isStatic);
        if (i >= 0) {
          return c.getField(i);
        }
        c = c.superClass;
      } while (c);
      return null;
    }

    getFieldByKey(key: string) {
      return null;
    }

    getFields(): FieldInfo [] {
      if (!this.fields) {
        return null;
      }
      if (this.resolvedFlags & ResolvedFlags.Fields) {
        return <FieldInfo []>this.fields;
      }
      for (var i = 0; i < this.fields.length; i++) {
        this.getField(i);
      }
      this.resolvedFlags |= ResolvedFlags.Fields;
      return <FieldInfo []>this.fields;
    }

    getClass(i: number): ClassInfo {
      if (typeof this.classes[i] === "number") {
        this.classes[i] = new ClassInfo(null);
      }
      return <ClassInfo>this.classes[i];
    }

    getClasses(): ClassInfo [] {
      if (!this.classes) {
        return null;
      }
      if (this.resolvedFlags & ResolvedFlags.Classes) {
        return <ClassInfo []>this.classes;
      }
      for (var i = 0; i < this.classes.length; i++) {
        this.getClass(i);
      }
      this.resolvedFlags |= ResolvedFlags.Classes;
      return <ClassInfo []>this.classes;
    }

    getInterface(i: number): ClassInfo {
      if (typeof this.interfaces[i] === "number") {
        this.interfaces[i] = this.constantPool.resolveClass(<number>this.interfaces[i]);
      }
      return <ClassInfo>this.interfaces[i];
    }

    getInterfaces(): ClassInfo [] {
      if (!this.interfaces) {
        return null;
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

    /**
     * Object that holds static properties for this class.
     */
    getStaticObject(ctx: Context): java.lang.Object {
      return <java.lang.Object><any>ctx.runtime.getRuntimeKlass(this.klass);
    }

    get isInterface(): boolean {
      return !!(this.access_flags & ACCESS_FLAGS.ACC_INTERFACE);
    }

    get isFinal(): boolean {
      return !!(this.access_flags & ACCESS_FLAGS.ACC_FINAL);
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

    resolveClass(i: number): ClassInfo {
      return null;
    }

    resolveMethod(i: number, isStatic: boolean): MethodInfo {
      return null;
    }

    resolveField(i: number): FieldInfo {
      return null;
    }

    /**
     * Resolves a constant pool reference.
     */
    resolve(index: number, isStatic: boolean) {
      // return this.constantPool.resolve(index);

      //var rp = this.resolved_constant_pool;
      //var constant: any = rp[index];
      //if (constant !== undefined) {
      //  return constant;
      //}
      //var cp = this.constant_pool;
      //var entry = this.constant_pool[index];
      //switch (entry.tag) {
      //  case TAGS.CONSTANT_Integer:
      //    constant = entry.integer;
      //    break;
      //  case TAGS.CONSTANT_Float:
      //    constant = entry.float;
      //    break;
      //  case TAGS.CONSTANT_String:
      //    constant = $.newStringConstant(cp[entry.string_index].bytes);
      //    break;
      //  case TAGS.CONSTANT_Long:
      //    constant = Long.fromBits(entry.lowBits, entry.highBits);
      //    break;
      //  case TAGS.CONSTANT_Double:
      //    constant = entry.double;
      //    break;
      //  case TAGS.CONSTANT_Class:
      //    constant = CLASSES.getClass(cp[entry.name_index].bytes);
      //    break;
      //  case TAGS.CONSTANT_Fieldref:
      //    var classInfo = this.resolve(entry.class_index, isStatic);
      //    var fieldName = cp[cp[entry.name_and_type_index].name_index].bytes;
      //    var signature = cp[cp[entry.name_and_type_index].signature_index].bytes;
      //    constant = CLASSES.getField(classInfo, (isStatic ? "S" : "I") + "." + fieldName + "." + signature);
      //    if (!constant) {
      //      throw $.newRuntimeException(
      //        classInfo.className + "." + fieldName + "." + signature + " not found");
      //    }
      //    break;
      //  case TAGS.CONSTANT_Methodref:
      //  case TAGS.CONSTANT_InterfaceMethodref:
      //    var classInfo = this.resolve(entry.class_index, isStatic);
      //    var methodName = cp[cp[entry.name_and_type_index].name_index].bytes;
      //    var signature = cp[cp[entry.name_and_type_index].signature_index].bytes;
      //    constant = CLASSES.getMethod(classInfo, (isStatic ? "S" : "I") + "." + methodName + "." + signature);
      //    if (!constant) {
      //      constant = CLASSES.getMethod(classInfo, (isStatic ? "S" : "I") + "." + methodName + "." + signature);
      //      throw $.newRuntimeException(
      //        classInfo.className + "." + methodName + "." + signature + " not found");
      //    }
      //    break;
      //  default:
      //    throw new Error("not support constant type");
      //}
      //return rp[index] = constant;
    }
  }

  export class PrimitiveClassInfo extends ClassInfo {
    private primitiveClassName: string;

    constructor(className, mangledName) {
      super(null);
      this.primitiveClassName = className;
      this.mangledName = mangledName;
    }

    getClassNameString(): string {
      return this.primitiveClassName;
    }

    static Z = new PrimitiveClassInfo("Z", "boolean");
    static C = new PrimitiveClassInfo("C", "char");
    static F = new PrimitiveClassInfo("F", "float");
    static D = new PrimitiveClassInfo("D", "double");
    static B = new PrimitiveClassInfo("B", "byte");
    static S = new PrimitiveClassInfo("S", "short");
    static I = new PrimitiveClassInfo("I", "int");
    static J = new PrimitiveClassInfo("J", "long");
  }

  export class ArrayClassInfo extends ClassInfo {
    elementClass: ClassInfo;

    constructor(elementClass: ClassInfo) {
      super(null);
      this.elementClass = elementClass;
      this.className = "[" + elementClass.className;
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

  export class PrimitiveArrayClassInfo extends ArrayClassInfo {
    constructor(elementClass: ClassInfo, mangledName: string) {
      super(elementClass);
      this.mangledName = mangledName;
    }
    static Z = new PrimitiveArrayClassInfo(PrimitiveClassInfo.Z, "Uint8Array");
    static C = new PrimitiveArrayClassInfo(PrimitiveClassInfo.C, "Uint16Array");
    static F = new PrimitiveArrayClassInfo(PrimitiveClassInfo.F, "Float32Array");
    static D = new PrimitiveArrayClassInfo(PrimitiveClassInfo.D, "Float64Array");
    static B = new PrimitiveArrayClassInfo(PrimitiveClassInfo.B, "Int8Array");
    static S = new PrimitiveArrayClassInfo(PrimitiveClassInfo.S, "Int16Array");
    static I = new PrimitiveArrayClassInfo(PrimitiveClassInfo.I, "Int32Array");
    static J = new PrimitiveArrayClassInfo(PrimitiveClassInfo.J, "Int64Array");
  }
}