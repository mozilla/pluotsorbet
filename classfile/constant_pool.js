"use strict";
/*global Stack, Long, CLASSES */

(function(exports) {

  function ConstantPool(items) {
    this.items = items;
    this.resolved = new Array(items.length);
  }

  function ResolvedConstant(type, value) {
    this.type = type;
    this.value = value;
    this.processed = false; // for shape
  }

  ConstantPool.prototype = {
    push: function(item) {
      this.items.push(item);
      this.resolved.push(undefined);
    },

    get: function(index) {
      return this.items[index];
    },

    /**
     * @return {ResolvedConstant}
     */
    resolve: function(ctx, index, isStatic) {
      if (this.resolved[index] !== undefined) {
        return this.resolved[index];
      }
      var raw = this.items[index];
      var type;
      var value;
      switch(raw.tag) {
      case 3: // TAGS.CONSTANT_Integer
        value = raw.integer;
        type = STACK_INT;
        break;
      case 4: // TAGS.CONSTANT_Float
        value = raw.float;
        type = STACK_FLOAT;
        break;
      case 8: // TAGS.CONSTANT_String
        value = util.newString(this.items[raw.string_index].bytes);
        type = STACK_REF;
        break;
      case 5: // TAGS.CONSTANT_Long
        value = Long.fromBits(raw.lowBits, raw.highBits);
        type = STACK_LONG;
        break;
      case 6: // TAGS.CONSTANT_Double
        type = STACK_DOUBLE;
        value = raw.double;
        break;
      case 7: // TAGS.CONSTANT_Class
        type = STACK_REF;
        value = CLASSES.getClass(this.items[raw.name_index].bytes);
        break;
      case 9: // TAGS.CONSTANT_Fieldref
        var classInfo = this.resolve(ctx, raw.class_index, isStatic).value;
        var fieldName = this.items[this.items[raw.name_and_type_index].name_index].bytes;
        var signature = this.items[this.items[raw.name_and_type_index].signature_index].bytes;
        type = STACK_REF;
        value = CLASSES.getField(classInfo, (isStatic ? "S" : "I") + "." + fieldName + "." + signature);
        if (!value)
          ctx.raiseExceptionAndYield("java/lang/RuntimeException",
                                     classInfo.className + "." + fieldName + "." + signature + " not found");
        break;
      case 10: // TAGS.CONSTANT_Methodref
      case 11: // TAGS.CONSTANT_InterfaceMethodref
        type = STACK_REF;
        var classInfo = this.resolve(ctx, raw.class_index, isStatic).value;
        var methodName = this.items[this.items[raw.name_and_type_index].name_index].bytes;
        var signature = this.items[this.items[raw.name_and_type_index].signature_index].bytes;
        value = CLASSES.getMethod(classInfo, (isStatic ? "S" : "I") + "." + methodName + "." + signature);
        if (!value)
          ctx.raiseExceptionAndYield("java/lang/RuntimeException",
                                     classInfo.className + "." + methodName + "." + signature + " not found");
        break;
      default:
        throw new Error("not support constant type");
      }

      return (this.resolved[index] = new ResolvedConstant(type, value));
    }
  };

  exports.ConstantPool = ConstantPool;
})(this);
