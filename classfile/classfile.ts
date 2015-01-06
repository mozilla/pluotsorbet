module J2ME {
  export var ATTRIBUTE_TYPES = {
    ConstantValue:  "ConstantValue",
    Code: "Code",
    Exceptions: "Exceptions",
    InnerClasses: "InnerClasses",
    Synthetic: "Synthetic",
    SourceFile: "SourceFile",
    LineNumberTable: "LineNumberTable",
    LocalVariableTable: "LocalVariableTable",
    Deprecated: "Deprecated",
    StackMap: "StackMap"
  };

  export enum ACCESS_FLAGS {
    ACC_PUBLIC = 0x0001,
    ACC_PRIVATE = 0x0002,
    ACC_PROTECTED = 0x0004,
    ACC_STATIC = 0x0008,
    ACC_FINAL = 0x0010,
    ACC_SYNCHRONIZED = 0x0020,
    ACC_VOLATILE = 0x0040,
    ACC_TRANSIENT = 0x0080,
    ACC_NATIVE = 0x0100,
    ACC_INTERFACE = 0x0200,
    ACC_ABSTRACT = 0x0400
  }

  export module AccessFlags {
    export function isPublic(flags) {
      return (flags & ACCESS_FLAGS.ACC_PUBLIC) === ACCESS_FLAGS.ACC_PUBLIC;
    }
    export function isPrivate(flags) {
      return (flags & ACCESS_FLAGS.ACC_PRIVATE) === ACCESS_FLAGS.ACC_PRIVATE;
    }
    export function isProtected(flags) {
      return (flags & ACCESS_FLAGS.ACC_PROTECTED) === ACCESS_FLAGS.ACC_PROTECTED;
    }
    export function isStatic(flags) {
      return (flags & ACCESS_FLAGS.ACC_STATIC) === ACCESS_FLAGS.ACC_STATIC;
    }
    export function isFinal(flags) {
      return (flags & ACCESS_FLAGS.ACC_FINAL) === ACCESS_FLAGS.ACC_FINAL;
    }
    export function isSynchronized(flags) {
      return (flags & ACCESS_FLAGS.ACC_SYNCHRONIZED) === ACCESS_FLAGS.ACC_SYNCHRONIZED;
    }
    export function isVolatile(flags) {
      return (flags & ACCESS_FLAGS.ACC_VOLATILE) === ACCESS_FLAGS.ACC_VOLATILE;
    }
    export function isTransient(flags) {
      return (flags & ACCESS_FLAGS.ACC_TRANSIENT) === ACCESS_FLAGS.ACC_TRANSIENT;
    }
    export function isNative(flags) {
      return (flags & ACCESS_FLAGS.ACC_NATIVE) === ACCESS_FLAGS.ACC_NATIVE;
    }
    export function isInterface(flags) {
      return (flags & ACCESS_FLAGS.ACC_INTERFACE) === ACCESS_FLAGS.ACC_INTERFACE;
    }
    export function isAbstract(flags) {
      return (flags & ACCESS_FLAGS.ACC_ABSTRACT) === ACCESS_FLAGS.ACC_ABSTRACT;
    }
  }

  export function getClassImage(classBytes) {
    var classImage: any = {};

    var getAttributes = function(attribute_name_index: number, bytes) {
      var reader = new Reader(bytes);
      var attribute: any = { attribute_name_index: attribute_name_index };

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
                var info = getAttributes(attribute_name_index, reader.readBytes(attribute_length));
                attribute.attributes.push({ attribute_name_index: attribute_name_index, info: info });
              }
              return attribute;

            case ATTRIBUTE_TYPES.SourceFile:
              attribute.type = ATTRIBUTE_TYPES.SourceFile;
              attribute.sourcefile_index = reader.read16();
              return attribute;

            case ATTRIBUTE_TYPES.LineNumberTable:
              attribute.type = ATTRIBUTE_TYPES.LineNumberTable;
              if (!release) {
                var line_number_table_length = reader.read16();
                attribute.line_number_table = [];
                for (var i = 0; i < line_number_table_length; i++) {
                  attribute.line_number_table.push({
                    start_pc:  reader.read16(),
                    line_number: reader.read16()
                  });
                }
              }
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
                var inner: any = {};
                inner.inner_class_info_index = reader.read16();
                inner.outer_class_info_index = reader.read16();
                inner.inner_name_index = reader.read16();
                inner.inner_class_access_flags = reader.read16();
                attribute.classes.push(inner);
              }
              return attribute;

            case ATTRIBUTE_TYPES.Synthetic:
              attribute.type = ATTRIBUTE_TYPES.Synthetic;
              return attribute;

            case ATTRIBUTE_TYPES.Deprecated:
              attribute.type = ATTRIBUTE_TYPES.Deprecated;
              return attribute;

            case ATTRIBUTE_TYPES.StackMap:
              attribute.type = ATTRIBUTE_TYPES.StackMap;
              return attribute;

            default:
              throw new Error("This attribute type is not supported yet. [" + JSON.stringify(item) + "]");
          }

        default:
          throw new Error("This attribute type is not supported yet. [" + JSON.stringify(item) + "]");
      }
    };

    var reader = new J2ME.Reader(classBytes);
    classImage.magic = reader.read32().toString(16);

    classImage.version = {
      minor_version: reader.read16(),
      major_version: reader.read16()
    };

    classImage.constant_pool = [ null ];
    var constant_pool_count = reader.read16();
    for(var i=1; i<constant_pool_count; i++) {
      var tag = reader.read8();
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
          classImage.constant_pool.push({ tag: tag, class_index: class_index, name_and_type_index: name_and_type_index });
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
          classImage.constant_pool.push({ tag: tag, double: reader.readDouble() });
          classImage.constant_pool.push(null);
          ++i;
          break;
        case TAGS.CONSTANT_Long:
          classImage.constant_pool.push({ tag: tag, highBits: reader.readInteger(), lowBits: reader.readInteger() });
          classImage.constant_pool.push(null);
          ++i;
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
      if (index != 0) {
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
        var info: any = reader.readBytes(attribute_length);
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
        var info: any = getAttributes(attribute_name_index, reader.readBytes(attribute_length));
        method_info.attributes.push({ attribute_name_index: attribute_name_index, info: info });
      }
      classImage.methods.push(method_info);
    }

    classImage.attributes = [];
    var attributes_count = reader.read16();
    for(var i=0; i<attributes_count; i++) {
      var attribute_name_index = reader.read16();
      var attribute_length = reader.read32();
      var info: any = getAttributes(attribute_name_index, reader.readBytes(attribute_length));
      classImage.attributes.push({ attribute_name_index: attribute_name_index, info: info });
    }

    return classImage;
  };

}