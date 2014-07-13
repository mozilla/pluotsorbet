/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var ClassInfo = function(classBytes) {
    if (this instanceof ClassInfo) {
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
            var method = {};
            method.classInfo = self;
            method.access_flags = m.access_flags;
            method.name = cp[m.name_index].bytes;
            method.signature = cp[m.signature_index].bytes;
            method.attributes = m.attributes;
            var s = Signature.parse(method.signature);
            method.IN = s.IN;
            method.OUT = s.OUT;
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
    } else {
        return new ClassInfo(classBytes);
    }
}
