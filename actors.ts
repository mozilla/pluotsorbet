module J2ME {
  declare var ACCESS_FLAGS;
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
    isStatic: boolean;
    mangledName: string;
    constantValue: any;

    constructor(public classInfo: ClassInfo, public access_flags: number, public name: string, public signature: string) {
      this.id = FieldInfo._nextiId++;
      this.isStatic = ACCESS_FLAGS.isStatic(access_flags);
      this.mangledName = J2ME.C4.Backend.mangleField(this);
    }

    get(object: java.lang.Object) {
      traceWriter && traceWriter.writeLn("get " + J2ME.toDebugString(object) + "." + this.mangledName);
      var value = object[this.mangledName];
      release || J2ME.Debug.assert(value !== undefined, this.name + " - " + object[this.id]);
      return value;
    }

    set(object: java.lang.Object, value: any) {
      traceWriter && traceWriter.writeLn("set " + J2ME.toDebugString(object) + "." + this.mangledName + " = " + value);
      release || J2ME.Debug.assert(value !== undefined);
      object[this.mangledName] = value
    }

    getStatic() {
      return this.get(this.classInfo.getClassObject($.ctx));
    }

    setStatic(value: any) {
      return this.set(this.classInfo.getClassObject($.ctx), value);
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
    consumes: number;
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


      this.mangledName = J2ME.C4.Backend.mangleMethod(this);
      this.mangledClassAndMethodName = J2ME.C4.Backend.mangleClassAndMethod(this);

      this.consumes = Signature.getINSlots(this.signature);
      if (!this.isStatic) {
        this.consumes++;
      }
    }

    getSourceLocationForBci(bci: number): SourceLocation {
      var sourceFile = this.classInfo.sourceFile || null;
      if (!sourceFile) {
        return null;
      }
      var lineNumber = -1;
      if (this.line_number_table && this.line_number_table.length) {
        var table = this.line_number_table;
        for (var i = 0; i < table.length; i++) {
          if (bci >= table[i].start_pc) {
            lineNumber = table[i].line_number;
          } else if (bci < table[i].start_pc) {
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
      var classImage = getClassImage(classBytes, this);
      var cp = classImage.constant_pool;
      this.className = cp[cp[classImage.this_class].name_index].bytes;
      this.superClassName = classImage.super_class ? cp[cp[classImage.super_class].name_index].bytes : null;
      this.access_flags = classImage.access_flags;
      this.constant_pool = cp;
      // Cache for virtual methods and fields
      this.vmc = {};
      this.vfc = {};

      this.mangledName = J2ME.C4.Backend.mangleClass(this);

      /*
       if (jsGlobal[this.mangledName]) {
       this.constructor = jsGlobal[this.mangledName];
       } else {
       this.constructor = function () {};
       }

       this.constructor.prototype.class = this;
       this.constructor.prototype.toString = function() {
       return '[instance ' + this.class.className + ']';
       };
       */


      var self = this;

      this.interfaces = [];
      classImage.interfaces.forEach(function (i) {
        var int = CLASSES.loadClass(cp[cp[i].name_index].bytes);
        self.interfaces.push(int);
        self.interfaces = self.interfaces.concat(int.interfaces);
      });

      this.fields = [];
      classImage.fields.forEach(function (f) {
        var field = new FieldInfo(self, f.access_flags, cp[f.name_index].bytes, cp[f.descriptor_index].bytes);
        f.attributes.forEach(function (attribute) {
          if (cp[attribute.attribute_name_index].bytes === "ConstantValue")
            field.constantValue = new DataView(attribute.info).getUint16(0, false);
        });
        self.fields.push(field);
      });

      this.methods = [];
      classImage.methods.forEach(function (m) {
        self.methods.push(new MethodInfo({
          name: cp[m.name_index].bytes,
          signature: cp[m.signature_index].bytes,
          classInfo: self,
          attributes: m.attributes,
          isNative: ACCESS_FLAGS.isNative(m.access_flags),
          isPublic: ACCESS_FLAGS.isPublic(m.access_flags),
          isStatic: ACCESS_FLAGS.isStatic(m.access_flags),
          isSynchronized: ACCESS_FLAGS.isSynchronized(m.access_flags)
        }));
      });

      var classes = this.classes = [];
      classImage.attributes.forEach(function (a) {
        if (a.info.type === ATTRIBUTE_TYPES.InnerClasses) {
          a.info.classes.forEach(function (c) {
            classes.push(cp[cp[c.inner_class_info_index].name_index].bytes);
            if (c.outer_class_info_index)
              classes.push(cp[cp[c.outer_class_info_index].name_index].bytes);
          });
        } else if (a.info.type === ATTRIBUTE_TYPES.SourceFile) {
          self.sourceFile = cp[a.info.sourcefile_index].bytes;
        }
      });
    }

    get isInterface() : boolean {
      return ACCESS_FLAGS.isInterface(this.access_flags);
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
      if (ACCESS_FLAGS.isInterface(toClass.access_flags) && this.implementsInterface(toClass))
        return true;
      if (this.elementClass && toClass.elementClass)
        return this.elementClass.isAssignableTo(toClass.elementClass);
      return this.superClass ? this.superClass.isAssignableTo(toClass) : false;
    }

    getClassObject(ctx: Context): java.lang.Class {
      return runtimeKlass(ctx.runtime, this.klass).classObject;
    }

    getField(fieldKey: string) : FieldInfo {
      return CLASSES.getField(this, fieldKey);
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
