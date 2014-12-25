module J2ME {
  declare var Native, Override;
  declare var ATTRIBUTE_TYPES;
  declare var missingNativeImpl;
  declare var CC;
  declare var Signature;
  declare var getClassImage;
  declare var classObjects;
  declare var util;

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

    constructor(public classInfo: ClassInfo, public access_flags: number, public name: string, public signature: string) {
      this.id = FieldInfo._nextiId++;
      this.isStatic = AccessFlags.isStatic(access_flags);
      this.constantValue = undefined;
      this.mangledName = undefined;
      this.key = undefined;
    }

    get(object: java.lang.Object) {
      var value = object[this.mangledName];
      return value;
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
    exception_table: ExceptionHandler [];
    max_locals: number;
    max_stack: number;
    /**
     * If greater than -1, then the number of arguments to pop of the stack when calling this function.
     */
    argumentSlots: number;
    signatureDescriptor: SignatureDescriptor;
    signature: string;
    implKey: string;
    key: string;
    alternateImpl: {()};
    fn: {()};
    attributes: any [];
    mangledName: string;
    mangledClassAndMethodName: string;

    line_number_table: {start_pc: number; line_number: number} [];

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
      this.key = (this.isStatic ? "S." : "I.") + this.name + "." + this.signature;
      this.implKey = this.classInfo.className + "." + this.name + "." + this.signature;


      this.mangledName = mangleMethod(this);
      this.mangledClassAndMethodName = mangleClassAndMethod(this);

      this.signatureDescriptor = SignatureDescriptor.makeSignatureDescriptor(this.signature);
      if (this.signatureDescriptor.hasTwoSlotArguments()) {
        this.argumentSlots = -1;
      } else {
        this.argumentSlots = this.signatureDescriptor.getArgumentSlotCount();
      }
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

  export class ClassInfo {
    className: string;
    c: string;
    superClass: ClassInfo;
    superClassName: string;
    interfaces: ClassInfo [];
    fields: FieldInfo [];
    methods: MethodInfo [];
    classes: any [];
    constant_pool: ConstantPoolEntry [];
    isArrayClass: boolean;
    elementClass: ClassInfo;
    klass: Klass;
    access_flags: number;
    vmc: any;
    vfc: any;
    mangledName: string;
    thread: any;

    sourceFile: string;
    constructor(classBytes) {
      enterTimeline("getClassImage");
      var classImage = getClassImage(classBytes, this);
      leaveTimeline("getClassImage");
      var cp = classImage.constant_pool;
      this.className = cp[cp[classImage.this_class].name_index].bytes;
      this.superClassName = classImage.super_class ? cp[cp[classImage.super_class].name_index].bytes : null;
      this.access_flags = classImage.access_flags;
      this.constant_pool = cp;
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
        this.methods.push(new MethodInfo({
          name: cp[m.name_index].bytes,
          signature: cp[m.signature_index].bytes,
          classInfo: self,
          attributes: m.attributes,
          isNative: AccessFlags.isNative(m.access_flags),
          isPublic: AccessFlags.isPublic(m.access_flags),
          isStatic: AccessFlags.isStatic(m.access_flags),
          isSynchronized: AccessFlags.isSynchronized(m.access_flags)
        }));
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
      if (false) {
        // Safe mangling that includes className, fieldName and signature.
        var fields = this.fields;
        for (var j = 0; j < fields.length; j++) {
          var fieldInfo = fields[j];
          fieldInfo.mangledName = "$" + escapeString(fieldInfo.classInfo.className + "_" + fieldInfo.name + "_" + fieldInfo.signature);
        }
        return;
      }

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
    getClassObject(ctx: Context): java.lang.Class {
      return getRuntimeKlass(ctx.runtime, this.klass).classObject;
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
  }

  export class ArrayClassInfo extends ClassInfo {
    constructor(className: string, elementClass?) {
      false && super(null);
      this.className = className;
      // TODO this may need to change for compiled code.
      this.mangledName = className;
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
    constructor(className: string, elementClass?) {
      super(className, elementClass);
    }

    get superClass() {
      return CLASSES.java_lang_Object;
    }

    static Z = new PrimitiveArrayClassInfo("[Z", PrimitiveClassInfo.Z);
    static C = new PrimitiveArrayClassInfo("[C", PrimitiveClassInfo.C);
    static F = new PrimitiveArrayClassInfo("[F", PrimitiveClassInfo.F);
    static D = new PrimitiveArrayClassInfo("[D", PrimitiveClassInfo.D);
    static B = new PrimitiveArrayClassInfo("[B", PrimitiveClassInfo.B);
    static S = new PrimitiveArrayClassInfo("[S", PrimitiveClassInfo.S);
    static I = new PrimitiveArrayClassInfo("[I", PrimitiveClassInfo.I);
    static J = new PrimitiveArrayClassInfo("[J", PrimitiveClassInfo.J);
  }

  PrimitiveClassInfo.prototype.fields = [];
  PrimitiveClassInfo.prototype.methods = [];
  PrimitiveClassInfo.prototype.interfaces = [];
}

var FieldInfo = J2ME.FieldInfo;
var MethodInfo = J2ME.MethodInfo;
var ClassInfo = J2ME.ClassInfo;
