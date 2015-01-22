module J2ME {
  declare var Native, Override;
  declare var missingNativeImpl;
  declare var CC;
  declare var Signature;
  declare var classObjects;
  declare var util;

  import BlockMap = Bytecode.BlockMap;

  export interface ConstantPoolEntry {
    tag: TAGS;
    name_index: number;
    bytes: string;
    class_index: number;
    name_and_type_index: number;
    signature_index: number;
    string_index: number;
    integer: number;
    float: number;
    double: number;
    highBits: number;
    lowBits: number;
  }

  export interface ExceptionHandler {
    start_pc: number;
    end_pc: number;
    handler_pc: number;
    catch_type: number;
  }

  export class SourceLocation {
    constructor(public className: string, public sourceFile: string, public lineNumber: number) {
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

  export class FieldInfo {
    private static _nextiId = 0;
    id: number;
    isStatic: boolean ;
    constantValue: any;
    mangledName: string;
    key: string;
    kind: Kind;

    constructor(public classInfo: ClassInfo, public access_flags: number, public name: string, public signature: string) {
      this.id = FieldInfo._nextiId++;
      this.isStatic = AccessFlags.isStatic(access_flags);
      this.constantValue = undefined;
      this.mangledName = undefined;
      this.key = undefined;
      this.kind = getSignatureKind(signature);
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

    toString() {
      return "[field " + this.name + "]";
    }
  }

  /**
   * Required params:
   *   - name
   *   - signature
   *   - classInfo
   *
   * Optional params:
   *   - attributes (defaults to [])
   *   - code (if not provided, pulls from attributes)
   *   - isNative, isPublic, isStatic, isSynchronized
   */
  export class MethodInfo {
    name: string;
    classInfo: ClassInfo;
    code: Uint8Array;
    isNative: boolean;
    isPublic: boolean;
    isStatic: boolean;
    isSynchronized: boolean;
    isAbstract: boolean;
    isFinal: boolean;

    /**
     * There is a compiled version of this method.?
     */
    state: MethodState;

    exception_table: ExceptionHandler [];
    max_locals: number;
    max_stack: number;

    argumentSlots: number;

    /**
     * The number of arguments to pop of the stack when calling this function.
     */
    consumeArgumentSlots: number;

    hasTwoSlotArguments: boolean;
    signatureDescriptor: SignatureDescriptor;
    signature: string;
    implKey: string;
    key: string;
    alternateImpl: {()};
    fn: {()};
    attributes: any [];
    mangledName: string;
    mangledClassAndMethodName: string;

    blockMap: BlockMap;

    line_number_table: {start_pc: number; line_number: number} [];

    /**
     * Approximate number of bytecodes executed in this method.
     */
    bytecodeCount: number;

    /**
     * Approximate number of times this method was called.
     */
    callCount: number;

    /**
     * Approximate number of times this method was called.
     */
    interpreterCallCount: number;

    /**
     * Approximate number of times a backward branch was taken.
     */
    backwardsBranchCount: number;

    /**
     * Number of times this method's counters were reset.
     */
    resetCount: number;

    /**
     * Whether this method's bytecode has been optimized for quicker interpretation.
     */
    isOptimized: boolean;

    constructor(opts) {
      this.name = opts.name;
      this.signature = opts.signature;
      this.classInfo = opts.classInfo;
      this.attributes = opts.attributes || [];

      // Use code if provided, otherwise search for the code within attributes.
      if (opts.code) {
        this.code = opts.code;
        this.exception_table = [];
        this.max_locals = undefined; // Unused for now.
      } else {
        for (var i = 0; i < this.attributes.length; i++) {
          var a = this.attributes[i];
          if (a.info.type === ATTRIBUTE_TYPES.Code) {
            this.code = new Uint8Array(a.info.code);
            this.exception_table = a.info.exception_table;
            this.max_locals = a.info.max_locals;
            this.max_stack = a.info.max_stack;

            var codeAttributes = a.info.attributes;
            for (var j = 0; j < codeAttributes.length; j++) {
              var b = codeAttributes[j];
              if (b.info.type === ATTRIBUTE_TYPES.LineNumberTable) {
                this.line_number_table = b.info.line_number_table;
              }
            }
            break;
          }
        }
      }

      this.isNative = opts.isNative;
      this.isPublic = opts.isPublic;
      this.isStatic = opts.isStatic;
      this.isSynchronized = opts.isSynchronized;
      this.isAbstract = opts.isAbstract;
      this.isFinal = opts.isAbstract;
      this.state = MethodState.Cold;
      this.key = (this.isStatic ? "S." : "I.") + this.name + "." + this.signature;
      this.implKey = this.classInfo.className + "." + this.name + "." + this.signature;


      this.mangledName = mangleMethod(this);
      this.mangledClassAndMethodName = mangleClassAndMethod(this);

      this.signatureDescriptor = SignatureDescriptor.makeSignatureDescriptor(this.signature);
      this.hasTwoSlotArguments = this.signatureDescriptor.hasTwoSlotArguments();
      this.argumentSlots = this.signatureDescriptor.getArgumentSlotCount();
      this.consumeArgumentSlots = this.argumentSlots;
      if (!this.isStatic) {
        this.consumeArgumentSlots ++;
      }

      this.callCount = 0;
      this.resetCount = 0;
      this.interpreterCallCount = 0;
      this.backwardsBranchCount = 0;
      this.bytecodeCount = 0;

      this.isOptimized = false;
      this.blockMap = null;
    }

    public getReturnKind(): Kind {
      return this.signatureDescriptor.typeDescriptors[0].kind;
    }

    getSourceLocationForPC(pc: number): SourceLocation {
      var sourceFile = this.classInfo.sourceFile || null;
      if (!sourceFile) {
        return null;
      }
      var lineNumber = -1;
      if (this.line_number_table && this.line_number_table.length) {
        var table = this.line_number_table;
        for (var i = 0; i < table.length; i++) {
          if (pc >= table[i].start_pc) {
            lineNumber = table[i].line_number;
          } else if (pc < table[i].start_pc) {
            break;
          }
        }
      }
      return new SourceLocation(this.classInfo.className, sourceFile, lineNumber)
    }
  }

  var classID = 0;

  export class ClassInfo {
    className: string;
    c: string;
    superClass: ClassInfo;
    superClassName: string;
    interfaces: ClassInfo [];
    fields: FieldInfo [];
    methods: MethodInfo [];
    staticInitializer: MethodInfo;
    classes: any [];
    subClasses: ClassInfo [];
    allSubClasses: ClassInfo [];
    constant_pool: ConstantPoolEntry [];
    resolved_constant_pool: any [];
    isArrayClass: boolean;
    elementClass: ClassInfo;
    klass: Klass;
    access_flags: number;
    vmc: any;
    vfc: any;
    mangledName: string;
    thread: any;
    id: number;

    sourceFile: string;

    static createFromObject(object) {
      var classInfo = Object.create(ClassInfo.prototype, object);
      classInfo.resolved_constant_pool = new Array(classInfo.constant_pool.length);
      classInfo.mangledName = mangleClass(classInfo);
      return classInfo;
    }

    constructor(classBytes) {
      this.id = classID ++;
      enterTimeline("getClassImage");
      var classImage = getClassImage(classBytes);
      leaveTimeline("getClassImage");
      var cp = classImage.constant_pool;
      this.className = cp[cp[classImage.this_class].name_index].bytes;
      this.superClassName = classImage.super_class ? cp[cp[classImage.super_class].name_index].bytes : null;
      this.access_flags = classImage.access_flags;
      this.constant_pool = cp;
      this.resolved_constant_pool = new Array(cp.length);
      this.subClasses = [];
      this.allSubClasses = [];
      // Cache for virtual methods and fields
      this.vmc = {};
      this.vfc = {};

      this.mangledName = mangleClass(this);

      var self = this;

      this.interfaces = [];
      for (var i = 0; i < classImage.interfaces.length; i++) {
        var j = classImage.interfaces[i];
        var int = CLASSES.loadClass(cp[cp[j].name_index].bytes);
        self.interfaces.push(int);
        self.interfaces = self.interfaces.concat(int.interfaces);
      }

      this.fields = [];
      for (var i = 0; i < classImage.fields.length; i++) {
        var f = classImage.fields[i];
        var field = new FieldInfo(self, f.access_flags, cp[f.name_index].bytes, cp[f.descriptor_index].bytes);
        f.attributes.forEach(function (attribute) {
          if (cp[attribute.attribute_name_index].bytes === "ConstantValue")
            field.constantValue = new DataView(attribute.info).getUint16(0, false);
        });
        self.fields.push(field);
      }

      enterTimeline("methods");
      this.methods = [];

      for (var i = 0; i < classImage.methods.length; i++) {
        var m = classImage.methods[i];
        var methodInfo = new MethodInfo({
          name: cp[m.name_index].bytes,
          signature: cp[m.signature_index].bytes,
          classInfo: self,
          attributes: m.attributes,
          isNative: AccessFlags.isNative(m.access_flags),
          isPublic: AccessFlags.isPublic(m.access_flags),
          isStatic: AccessFlags.isStatic(m.access_flags),
          isSynchronized: AccessFlags.isSynchronized(m.access_flags),
          isAbstract: AccessFlags.isAbstract(m.access_flags),
          isFinal: AccessFlags.isFinal(m.access_flags)
        });
        this.methods.push(methodInfo);
        if (methodInfo.name === "<clinit>") {
          this.staticInitializer = methodInfo;
        }
      }
      leaveTimeline("methods");

      var classes = this.classes = [];
      for (var i = 0; i < classImage.attributes.length; i++) {
        var a = classImage.attributes[i];
        if (a.info.type === ATTRIBUTE_TYPES.InnerClasses) {
          a.info.classes.forEach(function (c) {
            classes.push(cp[cp[c.inner_class_info_index].name_index].bytes);
            if (c.outer_class_info_index)
              classes.push(cp[cp[c.outer_class_info_index].name_index].bytes);
          });
        } else if (a.info.type === ATTRIBUTE_TYPES.SourceFile) {
          self.sourceFile = cp[a.info.sourcefile_index].bytes;
        }
      }
    }

    public complete() {
      enterTimeline("mangleFields");
      this._mangleFields();
      leaveTimeline("mangleFields");
    }

    /**
     * Gets the class hierarchy in derived -> base order.
     */
    private _getClassHierarchy(): ClassInfo [] {
      var classHierarchy = [];
      var classInfo = this;
      do {
        classHierarchy.push(classInfo);
        classInfo = classInfo.superClass;
      } while (classInfo);
      return classHierarchy;
    }

    private _mangleFields() {
      // Keep track of how many times a field name was used and resolve conflicts by
      // prefixing filed names with numbers.
      var classInfo: ClassInfo;
      var classHierarchy = this._getClassHierarchy();
      var count = Object.create(null);
      for (var i = classHierarchy.length - 1; i >= 0; i--) {
        classInfo = classHierarchy[i];
        var fields = classInfo.fields;
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

    get isInterface() : boolean {
      return AccessFlags.isInterface(this.access_flags);
    }

    get isFinal() : boolean {
      return AccessFlags.isFinal(this.access_flags);
    }

    implementsInterface(iface) : boolean {
      var classInfo = this;
      do {
        var interfaces = classInfo.interfaces;
        for (var n = 0; n < interfaces.length; ++n) {
          if (interfaces[n] === iface)
            return true;
        }
        classInfo = classInfo.superClass;
      } while (classInfo);
      return false;
    }

    isAssignableTo(toClass: ClassInfo) : boolean {
      if (this === toClass || toClass === CLASSES.java_lang_Object)
        return true;
      if (AccessFlags.isInterface(toClass.access_flags) && this.implementsInterface(toClass))
        return true;
      if (this.elementClass && toClass.elementClass)
        return this.elementClass.isAssignableTo(toClass.elementClass);
      return this.superClass ? this.superClass.isAssignableTo(toClass) : false;
    }

    /**
     * java.lang.Class object for this class info. This is a not where static properties
     * are stored for this class.
     */
    getClassObject(): java.lang.Class {
      return getRuntimeKlass($, this.klass).classObject;
    }

    /**
     * Object that holds static properties for this class.
     */
    getStaticObject(ctx: Context): java.lang.Object {
      return <java.lang.Object><any>getRuntimeKlass(ctx.runtime, this.klass);
    }

    getField(fieldKey: string) : FieldInfo {
      return CLASSES.getField(this, fieldKey);
    }

    getClassInitLockObject(ctx: Context) {
      if (!(this.className in ctx.runtime.classInitLockObjects)) {
        ctx.runtime.classInitLockObjects[this.className] = {
          classInfo: this
        };
      }
      return ctx.runtime.classInitLockObjects[this.className];
    }

    toString() {
      return "[class " + this.className + "]";
    }

    /**
     * Resolves a constant pool reference.
     */
    resolve(index: number, isStatic: boolean) {
      var rp = this.resolved_constant_pool;
      var constant: any = rp[index];
      if (constant !== undefined) {
        return constant;
      }
      var cp = this.constant_pool;
      var entry = this.constant_pool[index];
      switch (entry.tag) {
        case TAGS.CONSTANT_Integer:
          constant = entry.integer;
          break;
        case TAGS.CONSTANT_Float:
          constant = entry.float;
          break;
        case TAGS.CONSTANT_String:
          constant = $.newStringConstant(cp[entry.string_index].bytes);
          break;
        case TAGS.CONSTANT_Long:
          constant = Long.fromBits(entry.lowBits, entry.highBits);
          break;
        case TAGS.CONSTANT_Double:
          constant = entry.double;
          break;
        case TAGS.CONSTANT_Class:
          constant = CLASSES.getClass(cp[entry.name_index].bytes);
          break;
        case TAGS.CONSTANT_Fieldref:
          var classInfo = this.resolve(entry.class_index, isStatic);
          var fieldName = cp[cp[entry.name_and_type_index].name_index].bytes;
          var signature = cp[cp[entry.name_and_type_index].signature_index].bytes;
          constant = CLASSES.getField(classInfo, (isStatic ? "S" : "I") + "." + fieldName + "." + signature);
          if (!constant) {
            throw $.newRuntimeException(
              classInfo.className + "." + fieldName + "." + signature + " not found");
          }
          break;
        case TAGS.CONSTANT_Methodref:
        case TAGS.CONSTANT_InterfaceMethodref:
          var classInfo = this.resolve(entry.class_index, isStatic);
          var methodName = cp[cp[entry.name_and_type_index].name_index].bytes;
          var signature = cp[cp[entry.name_and_type_index].signature_index].bytes;
          constant = CLASSES.getMethod(classInfo, (isStatic ? "S" : "I") + "." + methodName + "." + signature);
          if (!constant) {
            constant = CLASSES.getMethod(classInfo, (isStatic ? "S" : "I") + "." + methodName + "." + signature);
            throw $.newRuntimeException(
              classInfo.className + "." + methodName + "." + signature + " not found");
          }
          break;
        default:
          throw new Error("not support constant type");
      }
      return rp[index] = constant;
    }
  }

  export class ArrayClassInfo extends ClassInfo {
    constructor(className: string, elementClass: ClassInfo, mangledName?: string) {
      false && super(null);
      this.className = className;
      // TODO this may need to change for compiled code.
      this.mangledName = mangledName;
      this.superClass = CLASSES.java_lang_Object;
      this.superClassName = "java/lang/Object";
      this.access_flags = 0;
      this.elementClass = elementClass;
      this.vmc = {};
      this.vfc = {};
    }
    implementsInterface(iface) {
      return false;
    }
  }

  ArrayClassInfo.prototype.fields = [];
  ArrayClassInfo.prototype.methods = [];
  ArrayClassInfo.prototype.interfaces = [];
  ArrayClassInfo.prototype.isArrayClass = true;

  export class PrimitiveClassInfo extends ClassInfo {
    constructor(className: string, mangledName: string) {
      false && super(null);
      this.className = className;
      this.mangledName = mangledName;
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

  PrimitiveClassInfo.prototype.fields = [];
  PrimitiveClassInfo.prototype.methods = [];
  PrimitiveClassInfo.prototype.interfaces = [];

  export class PrimitiveArrayClassInfo extends ArrayClassInfo {
    constructor(className: string, elementClass: ClassInfo, mangledName: string) {
      super(className, elementClass, mangledName);
    }

    get superClass() {
      return CLASSES.java_lang_Object;
    }

    static Z = new PrimitiveArrayClassInfo("[Z", PrimitiveClassInfo.Z, "Uint8Array");
    static C = new PrimitiveArrayClassInfo("[C", PrimitiveClassInfo.C, "Uint16Array");
    static F = new PrimitiveArrayClassInfo("[F", PrimitiveClassInfo.F, "Float32Array");
    static D = new PrimitiveArrayClassInfo("[D", PrimitiveClassInfo.D, "Float64Array");
    static B = new PrimitiveArrayClassInfo("[B", PrimitiveClassInfo.B, "Int8Array");
    static S = new PrimitiveArrayClassInfo("[S", PrimitiveClassInfo.S, "Int16Array");
    static I = new PrimitiveArrayClassInfo("[I", PrimitiveClassInfo.I, "Int32Array");
    static J = new PrimitiveArrayClassInfo("[J", PrimitiveClassInfo.J, "Int64Array");
  }

  PrimitiveClassInfo.prototype.fields = [];
  PrimitiveClassInfo.prototype.methods = [];
  PrimitiveClassInfo.prototype.interfaces = [];
}

var FieldInfo = J2ME.FieldInfo;
var MethodInfo = J2ME.MethodInfo;
var ClassInfo = J2ME.ClassInfo;
