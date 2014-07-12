/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var TAGS = {
    CONSTANT_Class: 7,
    CONSTANT_Fieldref: 9,
    CONSTANT_Methodref: 10,
    CONSTANT_InterfaceMethodref: 11,
    CONSTANT_String: 8,
    CONSTANT_Integer: 3,
    CONSTANT_Float: 4,
    CONSTANT_Long: 5,
    CONSTANT_Double: 6,
    CONSTANT_NameAndType: 12,
    CONSTANT_Utf8: 1,
    CONSTANT_Unicode: 2,
    toString: function(tag) {
        for(var name in this) {
            if (this[name] === tag) {
                return name;
            }
        }
        return null;
    }
};
