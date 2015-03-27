module J2ME {
  import assert = Debug.assert;


  var writer = new IndentingWriter();


  export enum Kind {
    Boolean,
    Byte,
    Short,
    Char,
    Int,
    Float,
    Long,
    Double,
    Reference,
    Void,
    Illegal,
    Store
  }

  export function isTwoSlot(kind: Kind) {
    return kind === Kind.Long || kind === Kind.Double;
  }

  export var valueKinds = [
    Kind.Boolean,
    Kind.Char,
    Kind.Float,
    Kind.Double,
    Kind.Byte,
    Kind.Short,
    Kind.Int,
    Kind.Long
  ];

  export function stackKind(kind: Kind): Kind {
    switch (kind) {
      case Kind.Boolean: return Kind.Int;
      case Kind.Byte: return Kind.Int;
      case Kind.Short: return Kind.Int;
      case Kind.Char: return Kind.Int;
      case Kind.Int: return Kind.Int;
      case Kind.Float: return Kind.Float;
      case Kind.Long: return Kind.Long;
      case Kind.Double: return Kind.Double;
      case Kind.Reference: return Kind.Reference;
      default: throw Debug.unexpected("Unknown stack kind: " + kind);
    }
  }

  export function arrayTypeCodeToKind(typeCode): Kind {
    switch (typeCode) {
      case 4:  return Kind.Boolean;
      case 5:  return Kind.Char;
      case 6:  return Kind.Float;
      case 7:  return Kind.Double;
      case 8:  return Kind.Byte;
      case 9:  return Kind.Short;
      case 10: return Kind.Int;
      case 11: return Kind.Long;
      default: throw Debug.unexpected("Unknown array type code: " + typeCode);
    }
  }

  export function kindCharacter(kind: Kind): string {
    switch (kind) {
      case Kind.Boolean:
        return 'Z';
      case Kind.Byte:
        return 'B';
      case Kind.Short:
        return 'S';
      case Kind.Char:
        return 'C';
      case Kind.Int:
        return 'I';
      case Kind.Float:
        return 'F';
      case Kind.Long:
        return 'J';
      case Kind.Double:
        return 'D';
      case Kind.Reference:
        return 'R';
      case Kind.Void:
        return 'V';
    }
  }

  export function getKindCheck(kind: Kind): (x: any) => boolean {
    switch (kind) {
      case Kind.Boolean:
        return (x) => x === 0 || x === 1;
      case Kind.Byte:
        return (x) => (x | 0) === x && x >= Constants.BYTE_MIN && x <= Constants.BYTE_MAX;
      case Kind.Short:
        return (x) => (x | 0) === x && x >= Constants.SHORT_MIN && x <= Constants.SHORT_MAX;
      case Kind.Char:
        return (x) => (x | 0) === x && x >= Constants.CHAR_MIN && x <= Constants.CHAR_MAX;
      case Kind.Int:
        return (x) => (x | 0) === x;
      case Kind.Float:
        return (x) => isNaN(x) || Math.fround(x) === x;
      case Kind.Long:
        return (x) => x instanceof Long.constructor;
      case Kind.Double:
        return (x) => isNaN(x) || (+x) === x;
      case Kind.Reference:
        return (x) => x === null || x instanceof Object;
      case Kind.Void:
        return (x) => typeof x === "undefined";
      default:
        throw Debug.unexpected("Unknown kind: " + kind);
    }
  }

  export function getSignatureKind(signature: Uint8Array): Kind {
    switch (signature[0]) {
      case UTF8Chars.Z:
        return Kind.Boolean;
      case UTF8Chars.B:
        return Kind.Byte;
      case UTF8Chars.S:
        return Kind.Short;
      case UTF8Chars.C:
        return Kind.Char;
      case UTF8Chars.I:
        return Kind.Int;
      case UTF8Chars.F:
        return Kind.Float;
      case UTF8Chars.J:
        return Kind.Long;
      case UTF8Chars.D:
        return Kind.Double;
      case UTF8Chars.OpenBracket:
      case UTF8Chars.L:
        return Kind.Reference;
      case UTF8Chars.V:
        return Kind.Void;
    }
  }

  /**
   * MethodDescriptor:
   *    ( ParameterDescriptor* ) ReturnDescriptor
   *  ParameterDescriptor:
   *    FieldType
   *  ReturnDescriptor:
   *    FieldType
   *    VoidDescriptor
   *  VoidDescriptor:
   *    V
   *  FieldDescriptor:
   *    FieldType
   *  FieldType:
   *    BaseType
   *    ObjectType
   *    ArrayType
   *  BaseType:
   *    B
   *    C
   *    D
   *    F
   *    I
   *    J
   *    S
   *    Z
   *  ObjectType:
   *    L ClassName ;
   *  ArrayType:
   *    [ ComponentType
   *  ComponentType:
   *    FieldType
   */

  // Global state for signature parsing, kind of hackish but fast.
  var globalNextIndex = 0;
  var descriptorKinds = [];

  /**
   * Returns an array of kinds that appear in a method signature. The first element is always the
   * return kind. The returned array is shared, so you if you need a copy of it, you'll need to
   * clone it.
   *
   * The parsing algorithm needs some global state to keep track of the current position in the
   * descriptor, namely |globalNextIndex| which always points to the next index in the descriptor
   * after a token has been consumed.
   */
  export function parseMethodDescriptorKinds(value: Uint8Array, startIndex: number): Kind [] {
    globalNextIndex = 0;
    if ((startIndex > value.length - 3) || value[startIndex] !== UTF8Chars.OpenParenthesis) {
      assert(false, "Invalid method signature.");
    }
    descriptorKinds.length = 0;
    descriptorKinds.push(Kind.Void); // placeholder until the return type is parsed
    var i = startIndex + 1;
    while (value[i] !== UTF8Chars.CloseParenthesis) {
      var kind = parseTypeDescriptorKind(value, i);
      descriptorKinds.push(kind);
      i = globalNextIndex;
      if (i >= value.length) {
        assert(false, "Invalid method signature.");
      }
    }
    i++;
    var kind = parseTypeDescriptorKind(value, i);
    if (globalNextIndex !== value.length) {
      assert(false, "Invalid method signature.");
    }
    // Plug in the return type
    descriptorKinds[0] = kind;
    return descriptorKinds;
  }

  function parseTypeDescriptorKind(value: Uint8Array, startIndex: number): Kind {
    globalNextIndex = startIndex + 1;
    switch (value[startIndex]) {
      case UTF8Chars.Z:
        return Kind.Boolean;
      case UTF8Chars.B:
        return Kind.Byte;
      case UTF8Chars.C:
        return Kind.Char;
      case UTF8Chars.D:
        return Kind.Double;
      case UTF8Chars.F:
        return Kind.Float;
      case UTF8Chars.I:
        return Kind.Int;
      case UTF8Chars.J:
        return Kind.Long;
      case UTF8Chars.S:
        return Kind.Short;
      case UTF8Chars.V:
        return Kind.Void;
      case UTF8Chars.L: {
        // parse a slashified Java class name
        var endIndex = parseClassNameKind(value, startIndex + 1, UTF8Chars.Slash);
        if (endIndex > startIndex + 1 && endIndex < value.length && value[endIndex] === UTF8Chars.Semicolon) {
          globalNextIndex = endIndex + 1;
          return Kind.Reference;
        }
        Debug.unexpected("Invalid signature.");
      }
      case UTF8Chars.OpenBracket: {
        // compute the number of dimensions
        var index = startIndex;
        while (index < value.length && value[index] === UTF8Chars.OpenBracket) {
          index++;
        }
        var dimensions = index - startIndex;
        if (dimensions > 255) {
          Debug.unexpected("Array with more than 255 dimensions.");
        }
        var component = parseTypeDescriptorKind(value, index);
        return Kind.Reference;
      }
      default:
        Debug.unexpected("Unexpected type descriptor prefix: " + value[startIndex]);
    }
  }

  function parseClassNameKind(value: Uint8Array, index: number, separator: number): number {
    var position = index;
    var length = value.length;
    while (position < length) {
      var nextch = value[position];
      if (nextch === UTF8Chars.Dot || nextch === UTF8Chars.Slash) {
        if (separator !== nextch) {
          return position;
        }
      } else if (nextch === UTF8Chars.Semicolon || nextch === UTF8Chars.OpenBracket) {
        return position;
      }
      position++;
    }
    return position;
  }

  export function signatureHasTwoSlotArguments(signatureKinds: Kind []): boolean {
    for (var i = 1; i < signatureKinds.length; i++) {
      if (isTwoSlot(signatureKinds[i])) {
        return true;
      }
    }
    return false;
  }

  export function signatureArgumentSlotCount(signatureKinds: Kind []): number {
    var count = 0;
    for (var i = 1; i < signatureKinds.length; i++) {
      count += isTwoSlot(signatureKinds[i]) ? 2 : 1;
    }
    return count;
  }
}
