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

  export class TypeDescriptor {

    private static canonicalTypeDescriptors: TypeDescriptor [] = [];

    constructor(public value: string, public kind: Kind) {
      assert (!TypeDescriptor.canonicalTypeDescriptors[value]);
      TypeDescriptor.canonicalTypeDescriptors[value] = this;
    }

    toString(): string {
      return this.value;
    }

    public static getArrayDimensions(descriptor: TypeDescriptor): number {
      var s = descriptor.toString();
      var dimension = 0;
      while (s.charAt(dimension) === '[') {
        dimension++;
      }
      return dimension;
    }

    public static getArrayDescriptorForDescriptor(descriptor: TypeDescriptor, dimensions: number): TypeDescriptor {
      assert (dimensions > 0);
      var componentString = descriptor.toString();
      if (TypeDescriptor.getArrayDimensions(descriptor) + dimensions > 255) {
        throw "Array type with more than 255 dimensions";
      }
      for (var i = 0; i !== dimensions; ++i) {
        componentString = "[" + componentString;
      }
      return TypeDescriptor.makeTypeDescriptor(componentString);
    }

    public static parseTypeDescriptor(value: string, startIndex: number): TypeDescriptor {
      switch (value[startIndex]) {
        case 'Z':
          return AtomicTypeDescriptor.Boolean;
        case 'B':
          return AtomicTypeDescriptor.Byte;
        case 'C':
          return AtomicTypeDescriptor.Char;
        case 'D':
          return AtomicTypeDescriptor.Double;
        case 'F':
          return AtomicTypeDescriptor.Float;
        case 'I':
          return AtomicTypeDescriptor.Int;
        case 'J':
          return AtomicTypeDescriptor.Long;
        case 'S':
          return AtomicTypeDescriptor.Short;
        case 'V':
          return AtomicTypeDescriptor.Void;
        case 'L': {
          // parse a slashified Java class name
          var endIndex = TypeDescriptor.parseClassName(value, startIndex, startIndex + 1, '/');
          if (endIndex > startIndex + 1 && endIndex < value.length && value.charAt(endIndex) === ';') {
            return TypeDescriptor.makeTypeDescriptor(value.substring(startIndex, endIndex + 1));
          }
          Debug.unexpected();
        }
        case '[': {
          // compute the number of dimensions
          var index = startIndex;
          while (index < value.length && value.charAt(index) === '[') {
            index++;
          }
          var dimensions = index - startIndex;
          if (dimensions > 255) {
            throw "array with more than 255 dimensions";;
          }
          var component = TypeDescriptor.parseTypeDescriptor(value, index);
          return TypeDescriptor.getArrayDescriptorForDescriptor(component, dimensions);
        }
        default:
          Debug.unexpected(value[startIndex]);
      }
    }

    private static parseClassName(value: string, startIndex: number, index: number, separator: string): number {
      var position = index;
      var length = value.length;
      while (position < length) {
        var nextch = value.charAt(position);
        if (nextch === '.' || nextch === '/') {
          if (separator !== nextch) {
            return position;
          }
        } else if (nextch === ';' || nextch === '[') {
          return position;
        }
        position++;
      }
      return position;
    }

    public static makeTypeDescriptor(value: string) {
      var typeDescriptor = TypeDescriptor.canonicalTypeDescriptors[value];
      if (!typeDescriptor) {
        // creating the type descriptor entry will add it to the canonical mapping.
        typeDescriptor = new TypeDescriptor(value, Kind.Reference);
      }
      return typeDescriptor;
    }
  }

  export class AtomicTypeDescriptor extends TypeDescriptor {
    constructor(public kind: Kind) {
      super(kindCharacter(kind), kind);
    }

    public static Boolean = new AtomicTypeDescriptor(Kind.Boolean);
    public static Byte = new AtomicTypeDescriptor(Kind.Byte);
    public static Char = new AtomicTypeDescriptor(Kind.Char);
    public static Double = new AtomicTypeDescriptor(Kind.Double);
    public static Float = new AtomicTypeDescriptor(Kind.Float);
    public static Int = new AtomicTypeDescriptor(Kind.Int);
    public static Long = new AtomicTypeDescriptor(Kind.Long);
    public static Short = new AtomicTypeDescriptor(Kind.Short);
    public static Void = new AtomicTypeDescriptor(Kind.Void);
  }

  export class SignatureDescriptor {
    private static canonicalSignatureDescriptors: SignatureDescriptor [] = [];

    public typeDescriptors: TypeDescriptor [];

    constructor(public value: string) {
      assert (!SignatureDescriptor.canonicalSignatureDescriptors[value]);
      SignatureDescriptor.canonicalSignatureDescriptors[value] = this;
      this.typeDescriptors = SignatureDescriptor.parse(value, 0);
    }

    toString(): string {
      return this.value;
    }

    public static makeSignatureDescriptor(value: string) {
      var signatureDescriptor = SignatureDescriptor.canonicalSignatureDescriptors[value];
      if (!signatureDescriptor) {
        // creating the signaature descriptor entry will add it to the canonical mapping.
        signatureDescriptor = new SignatureDescriptor(value);
      }
      return signatureDescriptor;
    }

    public static parse(value: string, startIndex: number): TypeDescriptor [] {
      if ((startIndex > value.length - 3) || value.charAt(startIndex) !== '(') {
        throw "Invalid method signature: " + value;
      }
      var typeDescriptors = [];
      typeDescriptors.push(AtomicTypeDescriptor.Void); // placeholder until the return type is parsed
      var i = startIndex + 1;
      while (value.charAt(i) !== ')') {
        var descriptor = TypeDescriptor.parseTypeDescriptor(value, i);
        typeDescriptors.push(descriptor);
        i = i + descriptor.toString().length;
        if (i >= value.length) {
          throw "Invalid method signature: " + value;
        }
      }
      i++;
      var descriptor = TypeDescriptor.parseTypeDescriptor(value, i);
      if (i + descriptor.toString().length !== value.length) {
        throw "Invalid method signature: " + value;
      }
      // Plug in the return type
      typeDescriptors[0] = descriptor;
      return typeDescriptors;
    }
  }
}