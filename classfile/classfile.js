/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var getClassImage = function(classBytes) {
    var classImage = {};

    var getAttributes = function(attribute_name_index, bytes) {
        var reader = new Reader(bytes);
        var attribute = { attribute_name_index: attribute_name_index };

        var item = classImage.constant_pool[attribute_name_index];

        switch(item.tag) {
            case TAGS.CONSTANT_Long:
            case TAGS.CONSTANT_Float:
            case TAGS.CONSTANT_Double:
            case TAGS.CONSTANT_Integer:
            case TAGS.CONSTANT_String:
                attribute.type = ATTRIBUTE_TYPES.ConstantValue;
                attribute.info = reader.read16();
                return attribute;

            case TAGS.CONSTANT_Utf8:
                switch(item.bytes) {
                    case ATTRIBUTE_TYPES.Code:
                        attribute.type = ATTRIBUTE_TYPES.Code;
                        attribute.max_stack = reader.read16();
                        attribute.max_locals = reader.read16();
                        var code_length = reader.read32();
                        attribute.code = reader.readBytes(code_length);

                        var exception_table_length = reader.read16();
                        attribute.exception_table = [];
                        for (var i=0; i<exception_table_length; i++) {
                            attribute.exception_table.push({
                                start_pc: reader.read16(),
                                end_pc: reader.read16(),
                                handler_pc: reader.read16(),
                                catch_type: reader.read16()
                            });
                        }

                        var attributes_count = reader.read16();
                        attribute.attributes = [];
                        for(var i=0; i<attributes_count; i++) {
                            var attribute_name_index = reader.read16();
                            var attribute_length = reader.read32();
                            var info = reader.readBytes(attribute_length);
                            attribute.attributes.push({ attribute_name_index: attribute_name_index, info: info });
                        }
                        return attribute;

                    case ATTRIBUTE_TYPES.SourceFile:
                        attribute.type = ATTRIBUTE_TYPES.SourceFile;
                        attribute.sourcefile_index = reader.read16();
                        return attribute;

                    case ATTRIBUTE_TYPES.Exceptions:
                        attribute.type = ATTRIBUTE_TYPES.Exceptions;
                        var number_of_exceptions = reader.read16();
                        attribute.exception_index_table = [];
                        for(var i=0; i<number_of_exceptions; i++) {
                            attribute.exception_index_table.push(reader.read16());
                        }
                        return attribute;

                    case ATTRIBUTE_TYPES.InnerClasses:
                        attribute.type = ATTRIBUTE_TYPES.InnerClasses;
                        var number_of_classes = reader.read16();
                        attribute.classes = [];
                        for(var i=0; i<number_of_classes; i++) {
                            var inner = {};
                            inner.inner_class_info_index = reader.read16();
                            inner.outer_class_info_index = reader.read16();
                            inner.inner_name_index = reader.read16();
                            inner.inner_class_access_flags = reader.read16();
                            attribute.classes.push(inner);
                        }
                        return attribute;

                    default:
                        throw new Error("This attribute type is not supported yet. [" + JSON.stringify(item) + "]");
                }

            default:
                throw new Error("This attribute type is not supported yet. [" + JSON.stringify(item) + "]");
        }
    };

    var reader = Reader(classBytes);
    classImage.magic = reader.read32().toString(16);

    classImage.version = {
        minor_version: reader.read16(),
        major_version: reader.read16()
    };

    classImage.constant_pool = [ null ];
    var constant_pool_count = reader.read16();
    for(var i=1; i<constant_pool_count; i++) {
        var tag =  reader.read8();
        switch(tag) {
            case TAGS.CONSTANT_Class:
                var name_index = reader.read16();
                classImage.constant_pool.push({ tag: tag, name_index: name_index });
                break;
            case TAGS.CONSTANT_Utf8:
                var length = reader.read16();
                var bytes = reader.readString(length);
                classImage.constant_pool.push({ tag: tag, bytes: bytes });
                break;
            case TAGS.CONSTANT_Methodref:
                var class_index = reader.read16();
                var name_and_type_index = reader.read16();
                classImage.constant_pool.push({  tag: tag, class_index: class_index, name_and_type_index: name_and_type_index });
                break;
            case TAGS.CONSTANT_NameAndType:
                var name_index = reader.read16();
                var signature_index = reader.read16();
                classImage.constant_pool.push({ tag: tag, name_index: name_index, signature_index: signature_index });
                break;
            case TAGS.CONSTANT_Fieldref:
                var class_index = reader.read16();
                var name_and_type_index = reader.read16();
                classImage.constant_pool.push({ tag: tag, class_index: class_index, name_and_type_index: name_and_type_index });
                break;
            case TAGS.CONSTANT_String:
                var string_index = reader.read16();
                classImage.constant_pool.push({ tag: tag, string_index: string_index });
                break;
            case TAGS.CONSTANT_Integer:
                classImage.constant_pool.push({ tag: tag, integer: reader.readInteger() });
                break;
            case TAGS.CONSTANT_Float:
                classImage.constant_pool.push({ tag: tag, float: reader.readFloat() });
                break;
            case TAGS.CONSTANT_Double:
            case TAGS.CONSTANT_Long:
                var bytes = new Uint8Array(8);
                for (var b=0; b<8; b++) {
                    bytes[b] = reader.read8();
                }
                classImage.constant_pool.push({ tag: tag, bytes: bytes });
                classImage.constant_pool.push(null); i++;
                break;
            case TAGS.CONSTANT_Fieldref:
            case TAGS.CONSTANT_Methodref:
            case TAGS.CONSTANT_InterfaceMethodref:
                var class_index = reader.read16();
                var name_and_type_index = reader.read16();
                classImage.constant_pool.push({ tag: tag, class_index: class_index, name_and_type_index:name_and_type_index });
                break;
            default:
                throw new Error("tag " + tag + " not supported.");
        }
    }

    classImage.access_flags = reader.read16();

    classImage.this_class = reader.read16();

    classImage.super_class = reader.read16();

    classImage.interfaces = [];
    var interfaces_count = reader.read16();
    for(var i=0; i<interfaces_count; i++) {
        var index = reader.read16();
        if (index != 0){
            classImage.interfaces.push(index);
        }
    }

    classImage.fields = [];
    var fields_count = reader.read16();
    for(var i=0; i<fields_count; i++) {
        var field_info = { access_flags: reader.read16(), name_index: reader.read16(), descriptor_index: reader.read16(), attributes: [] };
        var attributes_count = reader.read16();
        for(var j=0; j <attributes_count; j++) {
            var attribute_name_index = reader.read16();
            var attribute_length = reader.read32();
            var info = reader.readBytes(attribute_length);
            field_info.attributes.push({ attribute_name_index: attribute_name_index, info: info });
        }
        classImage.fields.push(field_info);
    }

    classImage.methods = [];
    var methods_count = reader.read16();
    for(var i=0; i<methods_count; i++) {
        var method_info = { access_flags: reader.read16(), name_index: reader.read16(), signature_index: reader.read16(), attributes: [] };
        var attributes_count = reader.read16();
        for(var j=0; j <attributes_count; j++) {
            var attribute_name_index = reader.read16();
            var attribute_length = reader.read32();
            var info = getAttributes(attribute_name_index, reader.readBytes(attribute_length));
            method_info.attributes.push({ attribute_name_index: attribute_name_index, info: info });
        }
        classImage.methods.push(method_info);
    }

    classImage.attributes = [];
    var attributes_count = reader.read16();
    for(var i=0; i<attributes_count; i++) {
            var attribute_name_index = reader.read16();
            var attribute_length = reader.read32();
            var info = getAttributes(attribute_name_index, reader.readBytes(attribute_length));
            classImage.attributes.push({ attribute_name_index: attribute_name_index, info: info });
    }

    return classImage;
};
