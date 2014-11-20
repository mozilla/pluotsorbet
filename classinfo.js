/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var FieldInfo = (function() {
    var idgen = 0;
    return function(classInfo, access_flags, name, signature) {
        this.classInfo = classInfo;
        this.access_flags = access_flags;
        this.name = name;
        this.signature = signature;
        this.id = idgen++;
<<<<<<< HEAD
        this.isStatic = ACCESS_FLAGS.isStatic(access_flags);
=======
        this.mangledName = J2ME.C4.Backend.mangleField(this);
>>>>>>> 1060e6c538636ce13dfc2c0f586de55b5e005613
    }
})();

FieldInfo.prototype.get = function(obj) {
    var value = obj[this.id];
    if (typeof value === "undefined") {
        value = util.defaultValue(this.signature);
    }
    return value;
}

FieldInfo.prototype.set = function(obj, value) {
    obj[this.id] = value;
}

FieldInfo.prototype.toString = function() {
    return "[field " + this.name + "]";
}

function missingNativeImpl(key, ctx, stack) {
    console.error("Attempted to invoke missing native:", key);
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
function MethodInfo(opts) {
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

    if (this.isNative) {
        if (this.implKey in Native) {
            this.alternateImpl = Native[this.implKey];
        } else {
            // Some Native MethodInfos are constructed but never called;
            // that's fine, unless we actually try to call them.
            this.alternateImpl = missingNativeImpl.bind(null, this.implKey);
        }
    } else if (this.implKey in Override) {
        this.alternateImpl = Override[this.implKey];
    } else {
        this.alternateImpl = null;

        if (typeof CC !== "undefined") {
            var compiledMethod = null;
            var classMangledName = J2ME.C4.Backend.mangleClass(this.classInfo);
            var compiledClass = CC[classMangledName];
            if (compiledClass) {
                var methodMangledName = J2ME.C4.Backend.mangleMethod(this);
                compiledMethod = compiledClass.methods[methodMangledName];
                if (this.isStatic) {
                    jsGlobal[methodMangledName] = compiledMethod;
                }
                console.log("HERE: " + compiledMethod + " : ");

            }
            this.fn = compiledMethod;
        }
    }

    this.mangledName = J2ME.C4.Backend.mangleMethod(this);

    this.consumes = Signature.getINSlots(this.signature);
    if (!this.isStatic) {
      this.consumes++;
    }
}

var ClassInfo = function(classBytes) {
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

    var self = this;

    this.interfaces = [];
    classImage.interfaces.forEach(function(i) {
        var int = CLASSES.loadClass(cp[cp[i].name_index].bytes);
        self.interfaces.push(int);
        self.interfaces = self.interfaces.concat(int.interfaces);
    });

    this.fields = [];
    classImage.fields.forEach(function(f) {
        var field = new FieldInfo(self, f.access_flags, cp[f.name_index].bytes, cp[f.descriptor_index].bytes);
        f.attributes.forEach(function(attribute) {
            if (cp[attribute.attribute_name_index].bytes === "ConstantValue")
                field.constantValue = new DataView(attribute.info).getUint16(0, false);
        });
        self.fields.push(field);
    });

    this.methods = [];
    classImage.methods.forEach(function(m) {
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
    classImage.attributes.forEach(function(a) {
        if (a.info.type === ATTRIBUTE_TYPES.InnerClasses) {
            a.info.classes.forEach(function(c) {
                classes.push(cp[cp[c.inner_class_info_index].name_index].bytes);
                if (c.outer_class_info_index)
                    classes.push(cp[cp[c.outer_class_info_index].name_index].bytes);
            });
        }
    });
}

ClassInfo.prototype.initPrototypeChain = function() {
    if (this.superClass) {
        this.superClass.initPrototypeChain();
    }

    var constructor = this.constructor = function() { };
    constructor.prototype = Object.create(
        this.superClass ?
            this.superClass.constructor.prototype :
            null);
    constructor.prototype.class = this;
    constructor.prototype.toString = function() {
        return '[instance ' + this.class.className + ']';
    };

    this.methods.forEach(function(methodInfo) {
        if (methodInfo.fn) {
            if (methodInfo.isStatic) {
                constructor[methodInfo.mangledName] = methodInfo.fn;
            } else {
                constructor.prototype[methodInfo.mangledName] = methodInfo.fn;
            }
        }
    });
}

ClassInfo.prototype.implementsInterface = function(iface) {
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

ClassInfo.prototype.isAssignableTo = function(toClass) {
    if (this === toClass || toClass === ClassInfo.java_lang_Object)
        return true;
    if (ACCESS_FLAGS.isInterface(toClass.access_flags) && this.implementsInterface(toClass))
        return true;
    if (this.elementClass && toClass.elementClass)
        return this.elementClass.isAssignableTo(toClass.elementClass);
    return this.superClass ? this.superClass.isAssignableTo(toClass) : false;
}

ClassInfo.prototype.getClassObject = function(ctx) {
    var className = this.className;
    var classObjects = ctx.runtime.classObjects;
    var classObject = classObjects[className];
    if (!classObject) {
        classObject = util.newObject(CLASSES.java_lang_Class);
        classObject.vmClass = this;
        classObjects[className] = classObject;
    }
    return classObject;
}

ClassInfo.prototype.getField = function(fieldKey) {
    return CLASSES.getField(this, fieldKey);
}

ClassInfo.prototype.toString = function() {
    return "[class " + this.className + "]";
}

var ArrayClass = function(className, elementClass) {
    this.className = className;
    this.superClassName = "java/lang/Object";
    this.access_flags = 0;
    this.elementClass = elementClass;
    this.vmc = {};
    this.vfc = {};
}

ArrayClass.prototype.methods = [];

ArrayClass.prototype.isArrayClass = true;

ArrayClass.prototype.implementsInterface = function(iface) {
    return false;
}

ArrayClass.prototype.isAssignableTo = ClassInfo.prototype.isAssignableTo;

ArrayClass.prototype.getClassObject = ClassInfo.prototype.getClassObject;
