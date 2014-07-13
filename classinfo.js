/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var ClassInfo = function(classBytes) {
    if (this instanceof ClassInfo) {
        this.classImage = getClassImage(classBytes, this);
    } else {
        return new ClassInfo(classBytes);
    }
}

ClassInfo.prototype.getClassName = function() {
    return this.classImage.constant_pool[this.classImage.constant_pool[this.classImage.this_class].name_index].bytes;
}

ClassInfo.prototype.getSuperClassName = function() {
    if (!this.classImage.super_class)
        return null;
    return this.classImage.constant_pool[this.classImage.constant_pool[this.classImage.super_class].name_index].bytes;
}

ClassInfo.prototype.getAccessFlags = function() {
    return this.classImage.access_flags;
}

ClassInfo.prototype.getConstantPool = function() {
    return this.classImage.constant_pool;
}

ClassInfo.prototype.getFields = function() {
    return this.classImage.fields;
}

ClassInfo.prototype.getMethods = function() {
    return this.classImage.methods;
}

ClassInfo.prototype.getClasses = function() {
    var self = this;
    var classes = [];
    this.classImage.attributes.forEach(function(a) {
        if (a.info.type === ATTRIBUTE_TYPES.InnerClasses) {
            a.info.classes.forEach(function(c) {
                classes.push(self.classImage.constant_pool[self.classImage.constant_pool[c.inner_class_info_index].name_index].bytes);
                classes.push(self.classImage.constant_pool[self.classImage.constant_pool[c.outer_class_info_index].name_index].bytes);
            });
        }
    });
    return classes;
}

var FieldInfo = function(classInfo, access_flags, name_index, descriptor_index) {
    this.classInfo = classInfo;
    this.access_flags = access_flags;
    this.name_index = name_index;
    this.descriptor_index = descriptor_index;
    this.attributes = [];
}

var MethodInfo = function(classInfo, access_flags, name_index, signature_index) {
    this.classInfo = classInfo;
    this.access_flags = access_flags;
    this.name_index = name_index;
    this.signature_index = signature_index;
    this.attributes = [];
}

var AttributeInfo = function(attribute_name_index, attribute_length, info) {
    this.attribute_name_index = attribute_name_index;
    this.attribute_length = attribute_length;
    this.info = info;
}

var ExceptionInfo = function(start_pc, end_pc, handler_pc, catch_type) {
    this.start_pc = start_pc;
    this.end_pc = end_pc;
    this.handler_pc = handler_pc;
    this.catch_type = catch_type;
}
