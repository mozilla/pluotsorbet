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
    }

    this.consumes = Signature.getINSlots(this.signature);
    if (!this.isStatic) {
      this.consumes++;
    }

    this.numCalled = urlParams.numCalled || 0;
    this.compiled = null;
    this.dontCompile = false;
}

var ClassInfo = function(classBytes) {
    var classImage = getClassImage(classBytes, this);
    var cp = classImage.constant_pool;
    this.className = cp.get(cp.get(classImage.this_class).name_index).bytes;
    this.superClassName = classImage.super_class ? cp.get(cp.get(classImage.super_class).name_index).bytes : null;
    this.access_flags = classImage.access_flags;
    this.constant_pool = cp;
    this.constructor = function () {
    }
    this.constructor.prototype.class = this;
    this.constructor.prototype.toString = function() {
        return "[instance " + this.class.className + "]";
    }
    // Cache for virtual methods and fields
    this.vmc = {};
    this.vfc = {};

    var self = this;

    this.interfaces = [];
    classImage.interfaces.forEach(function(i) {
        var int = CLASSES.loadClass(cp.get(cp.get(i).name_index).bytes);
        self.interfaces.push(int);
        self.interfaces = self.interfaces.concat(int.interfaces);
    });

    this.fields = [];
    classImage.fields.forEach(function(f) {
        var field = new FieldInfo(self, f.access_flags, cp.get(f.name_index).bytes, cp.get(f.descriptor_index).bytes);
        f.attributes.forEach(function(attribute) {
            if (cp.get(attribute.attribute_name_index).bytes === "ConstantValue")
                field.constantValue = new DataView(attribute.info).getUint16(0, false);
        });
        self.fields.push(field);
    });

    this.methods = [];
    classImage.methods.forEach(function(m) {
        self.methods.push(new MethodInfo({
            name: cp.get(m.name_index).bytes,
            signature: cp.get(m.signature_index).bytes,
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
                classes.push(cp.get(cp.get(c.inner_class_info_index).name_index).bytes);
                if (c.outer_class_info_index)
                    classes.push(cp.get(cp.get(c.outer_class_info_index).name_index).bytes);
            });
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
