/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var ClassInfo = function(classBytes) {
    var classImage = getClassImage(classBytes, this);
    var cp = classImage.constant_pool;
    this.className = cp[cp[classImage.this_class].name_index].bytes;
    this.superClassName = classImage.super_class ? cp[cp[classImage.super_class].name_index].bytes : null;
    this.access_flags = classImage.access_flags;
    this.constant_pool = cp;
    this.staticFields = {};
    this.constructor = function () {
    }
    this.constructor.prototype.class = this;

    var self = this;

    this.interfaces = [];
    classImage.interfaces.forEach(function(i) {
        self.interfaces.push(cp[cp[i].name_index].bytes);
    });

    this.fields = [];
    classImage.fields.forEach(function(f) {
        self.fields.push({
            classInfo: self,
            access_flags: f.access_flags,
            name: cp[f.name_index].bytes,
            signature: cp[f.descriptor_index].bytes,
            attributes: f.attributes
        });
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

ClassInfo.prototype.getClassObject = function() {
    var self = this;
    return util.cache(this, "classObject", function () {
        var classObject = CLASSES.newObject(CLASSES.java_lang_Class);
        classObject.vmClass = self;
        return classObject;
    });
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

ArrayClass.prototype.initialized = true;
