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

function MethodInfo(m, classInfo, constantPool) {
    this.classInfo = classInfo;
    this.name = constantPool[m.name_index].bytes;
    this.signature = constantPool[m.signature_index].bytes;
    this.attributes = m.attributes;

    for (var i = 0; i < this.attributes.length; i++) {
        var a = this.attributes[i];
        if (a.info.type === ATTRIBUTE_TYPES.Code) {
            this.code = new Uint8Array(a.info.code);
            this.exception_table = a.info.exception_table;
            this.max_locals = a.info.max_locals;
            break;
        }
    }

    this.isNative = ACCESS_FLAGS.isNative(m.access_flags);
    this.isPublic = ACCESS_FLAGS.isPublic(m.access_flags);
    this.isStatic = ACCESS_FLAGS.isStatic(m.access_flags);
    this.isSynchronized = ACCESS_FLAGS.isSynchronized(m.access_flags);
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
    if (this.isStatic) {
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
        self.methods.push(new MethodInfo(m, self, cp));
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
        classObject = ctx.newObject(CLASSES.java_lang_Class);
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
