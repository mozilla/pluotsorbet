/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

function MethodInfo() {
}

MethodInfo.prototype.meta = function() {
    if (this._meta)
        return this._meta;

    var s = Signature.parse(this.signature);
    var IN = s.IN;
    var OUT = s.OUT;

    var consumes = 0;
    var isStatic = ACCESS_FLAGS.isStatic(this.access_flags);
    if (!isStatic) {
        ++consumes;
    }
    for (var i=0; i<IN.length; i++) {
        var type = IN[i].type;
        ++consumes;
        if (type === "long" || type === "double")
            ++consumes;
    }
    var produces = 0;
    if (OUT.length)
        produces = (OUT[0] === "long" || OUT[0] === "double") ? 2 : 1;

    return this._meta = { IN: IN, OUT: OUT, consumes: consumes, products: produces };
}

var ClassInfo = function(classBytes) {
    var classImage = getClassImage(classBytes, this);
    var cp = classImage.constant_pool;
    this.className = cp[cp[classImage.this_class].name_index].bytes;
    this.superClassName = classImage.super_class ? cp[cp[classImage.super_class].name_index].bytes : null;
    this.access_flags = classImage.access_flags;
    this.constant_pool = cp;

    var self = this;

    this.fields = [];
    classImage.fields.forEach(function(f) {
        self.fields.push({
            access_flags: f.access_flags,
            name: cp[f.name_index].bytes,
            descriptor: cp[f.descriptor_index].bytes,
            attributes: f.attributes
        });
    });

    this.methods = [];
    classImage.methods.forEach(function(m) {
        var method = new MethodInfo();
        method.classInfo = self;
        method.access_flags = m.access_flags;
        method.name = cp[m.name_index].bytes;
        method.signature = cp[m.signature_index].bytes;
        method.attributes = m.attributes;
        method.attributes.forEach(function(a) {
            if (a.info.type === ATTRIBUTE_TYPES.Code) {
                method.code = Uint8Array(a.info.code);
                method.exception_table = a.info.exception_table;
                method.max_locals = a.info.max_locals;
            }
        });
        self.methods.push(method);
    });

    this.classes = [];
    classImage.attributes.forEach(function(a) {
        if (a.info.type === ATTRIBUTE_TYPES.InnerClasses) {
            a.info.classes.forEach(function(c) {
                classes.push(cp[cp[c.inner_class_info_index].name_index].bytes);
                classes.push(cp[cp[c.outer_class_info_index].name_index].bytes);
            });
        }
    });
}

var ArrayClass = function(elementType) {
    this.className = "[" + elementType;
    this.superClassName = "java/lang/Object";
    this.access_flags = 0;
    this.elementType = elementType;
}
