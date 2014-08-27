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
        var method = {};
        method.classInfo = self;
        method.access_flags = m.access_flags;
        method.name = cp[m.name_index].bytes;
        method.signature = cp[m.signature_index].bytes;
        method.attributes = m.attributes;
        method.attributes.forEach(function(a) {
            if (a.info.type === ATTRIBUTE_TYPES.Code) {
                method.code = new Uint8Array(a.info.code);
                method.exception_table = a.info.exception_table;
                method.max_locals = a.info.max_locals;
            }
        });
        self.methods.push(method);
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

ClassInfo.prototype.getField = function(name, signature, isStatic) {
    return CLASSES.getField(this, name, signature, isStatic);
}

var ArrayClass = function(className, elementClass) {
    this.className = className;
    this.superClassName = "java/lang/Object";
    this.access_flags = 0;
    this.elementClass = elementClass;
}

ArrayClass.prototype.methods = [];

ArrayClass.prototype.isArrayClass = true;

ArrayClass.prototype.implementsInterface = function(iface) {
    return false;
}

ArrayClass.prototype.isAssignableTo = ClassInfo.prototype.isAssignableTo;

ArrayClass.prototype.getClassObject = ClassInfo.prototype.getClassObject;
