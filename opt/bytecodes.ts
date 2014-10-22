module J2ME.Bytecode {
  import assert = Debug.assert;
  import Operator = C4.IR.Operator;

  export class Bytes {
    /**
     * Gets a signed 1-byte value.
     */
    public static beS1(data: Uint8Array, bci: number) {
      return (data[bci] << 24) >> 24;
    }

    /**
     * Gets a signed 2-byte big-endian value.
     */
    public static beS2(data: Uint8Array, bci: number) {
      return ((data[bci] << 8) | (data[bci + 1] & 0xff)) << 16 >> 16;
    }
    
    /**
     * Gets an unsigned 1-byte value.
     */
    public static beU1(data: Uint8Array, bci: number) {
      return data[bci] & 0xff;
    }
    
    /**
     * Gets an unsigned 2-byte big-endian value.
     */
    public static beU2(data: Uint8Array, bci: number) {
      return ((data[bci] & 0xff) << 8) | (data[bci + 1] & 0xff);
    }
    
    /**
     * Gets a signed 4-byte big-endian value.
     */
    public static beS4(data: Uint8Array, bci: number) {
      return (data[bci] << 24) | ((data[bci + 1] & 0xff) << 16) | ((data[bci + 2] & 0xff) << 8) | (data[bci + 3] & 0xff);
    }
    
    /**
     * Gets either a signed 2-byte or a signed 4-byte big-endian value.
     */
    public static beSVar(data: Uint8Array, bci: number, fourByte: boolean) {
      if (fourByte) {
        return Bytes.beS4(data, bci);
      } else {
        return Bytes.beS2(data, bci);
      }
    }
  }

  export enum Condition {
    /**
     * Equal.
     */
    EQ,

    /**
     * Not equal.
     */
    NE,

    /**
     * Signed less than.
     */
    LT,

    /**
     * Signed less than or equal.
     */
    LE,

    /**
     * Signed greater than.
     */
    GT,

    /**
     * Signed greater than or equal.
     */
    GE,

    /**
     * Unsigned greater than or equal ("above than or equal").
     */
    AE,

    /**
     * Unsigned less than or equal ("below than or equal").
     */
    BE,

    /**
     * Unsigned greater than ("above than").
     */
    AT,

    /**
     * Unsigned less than ("below than").
     */
    BT,

    /**
     * Operation produced an overflow.
     */
    OF,

    /**
     * Operation did not produce an overflow.
     */
    NOF
  }

  export function conditionToOperator(condition: Condition): Operator {
    switch (condition) {
      case Condition.EQ: return Operator.EQ;
      case Condition.NE: return Operator.NE;
      case Condition.LT: return Operator.LT;
      case Condition.LE: return Operator.LE;
      case Condition.GT: return Operator.GT;
      case Condition.GE: return Operator.GE;
      default: throw "TODO"
    }
  }

  /**
   * The definitions of the bytecodes that are valid input to the compiler and
   * related utility methods. This comprises two groups: the standard Java
   * bytecodes defined by <a href=
   * "http://java.sun.com/docs/books/jvms/second_edition/html/VMSpecTOC.doc.html">
   * Java Virtual Machine Specification</a>, and a set of <i>extended</i>
   * bytecodes that support low-level programming, for example, memory barriers.
   *
   * The extended bytecodes are one or three bytes in size. The one-byte bytecodes
   * follow the values in the standard set, with no gap. The three-byte extended
   * bytecodes share a common first byte and carry additional instruction-specific
   * information in the second and third bytes.
   */
  export enum Bytecodes {
    NOP                  =   0, // 0x00
    ACONST_NULL          =   1, // 0x01
    ICONST_M1            =   2, // 0x02
    ICONST_0             =   3, // 0x03
    ICONST_1             =   4, // 0x04
    ICONST_2             =   5, // 0x05
    ICONST_3             =   6, // 0x06
    ICONST_4             =   7, // 0x07
    ICONST_5             =   8, // 0x08
    LCONST_0             =   9, // 0x09
    LCONST_1             =  10, // 0x0A
    FCONST_0             =  11, // 0x0B
    FCONST_1             =  12, // 0x0C
    FCONST_2             =  13, // 0x0D
    DCONST_0             =  14, // 0x0E
    DCONST_1             =  15, // 0x0F
    BIPUSH               =  16, // 0x10
    SIPUSH               =  17, // 0x11
    LDC                  =  18, // 0x12
    LDC_W                =  19, // 0x13
    LDC2_W               =  20, // 0x14
    ILOAD                =  21, // 0x15
    LLOAD                =  22, // 0x16
    FLOAD                =  23, // 0x17
    DLOAD                =  24, // 0x18
    ALOAD                =  25, // 0x19
    ILOAD_0              =  26, // 0x1A
    ILOAD_1              =  27, // 0x1B
    ILOAD_2              =  28, // 0x1C
    ILOAD_3              =  29, // 0x1D
    LLOAD_0              =  30, // 0x1E
    LLOAD_1              =  31, // 0x1F
    LLOAD_2              =  32, // 0x20
    LLOAD_3              =  33, // 0x21
    FLOAD_0              =  34, // 0x22
    FLOAD_1              =  35, // 0x23
    FLOAD_2              =  36, // 0x24
    FLOAD_3              =  37, // 0x25
    DLOAD_0              =  38, // 0x26
    DLOAD_1              =  39, // 0x27
    DLOAD_2              =  40, // 0x28
    DLOAD_3              =  41, // 0x29
    ALOAD_0              =  42, // 0x2A
    ALOAD_1              =  43, // 0x2B
    ALOAD_2              =  44, // 0x2C
    ALOAD_3              =  45, // 0x2D
    IALOAD               =  46, // 0x2E
    LALOAD               =  47, // 0x2F
    FALOAD               =  48, // 0x30
    DALOAD               =  49, // 0x31
    AALOAD               =  50, // 0x32
    BALOAD               =  51, // 0x33
    CALOAD               =  52, // 0x34
    SALOAD               =  53, // 0x35
    ISTORE               =  54, // 0x36
    LSTORE               =  55, // 0x37
    FSTORE               =  56, // 0x38
    DSTORE               =  57, // 0x39
    ASTORE               =  58, // 0x3A
    ISTORE_0             =  59, // 0x3B
    ISTORE_1             =  60, // 0x3C
    ISTORE_2             =  61, // 0x3D
    ISTORE_3             =  62, // 0x3E
    LSTORE_0             =  63, // 0x3F
    LSTORE_1             =  64, // 0x40
    LSTORE_2             =  65, // 0x41
    LSTORE_3             =  66, // 0x42
    FSTORE_0             =  67, // 0x43
    FSTORE_1             =  68, // 0x44
    FSTORE_2             =  69, // 0x45
    FSTORE_3             =  70, // 0x46
    DSTORE_0             =  71, // 0x47
    DSTORE_1             =  72, // 0x48
    DSTORE_2             =  73, // 0x49
    DSTORE_3             =  74, // 0x4A
    ASTORE_0             =  75, // 0x4B
    ASTORE_1             =  76, // 0x4C
    ASTORE_2             =  77, // 0x4D
    ASTORE_3             =  78, // 0x4E
    IASTORE              =  79, // 0x4F
    LASTORE              =  80, // 0x50
    FASTORE              =  81, // 0x51
    DASTORE              =  82, // 0x52
    AASTORE              =  83, // 0x53
    BASTORE              =  84, // 0x54
    CASTORE              =  85, // 0x55
    SASTORE              =  86, // 0x56
    POP                  =  87, // 0x57
    POP2                 =  88, // 0x58
    DUP                  =  89, // 0x59
    DUP_X1               =  90, // 0x5A
    DUP_X2               =  91, // 0x5B
    DUP2                 =  92, // 0x5C
    DUP2_X1              =  93, // 0x5D
    DUP2_X2              =  94, // 0x5E
    SWAP                 =  95, // 0x5F
    IADD                 =  96, // 0x60
    LADD                 =  97, // 0x61
    FADD                 =  98, // 0x62
    DADD                 =  99, // 0x63
    ISUB                 = 100, // 0x64
    LSUB                 = 101, // 0x65
    FSUB                 = 102, // 0x66
    DSUB                 = 103, // 0x67
    IMUL                 = 104, // 0x68
    LMUL                 = 105, // 0x69
    FMUL                 = 106, // 0x6A
    DMUL                 = 107, // 0x6B
    IDIV                 = 108, // 0x6C
    LDIV                 = 109, // 0x6D
    FDIV                 = 110, // 0x6E
    DDIV                 = 111, // 0x6F
    IREM                 = 112, // 0x70
    LREM                 = 113, // 0x71
    FREM                 = 114, // 0x72
    DREM                 = 115, // 0x73
    INEG                 = 116, // 0x74
    LNEG                 = 117, // 0x75
    FNEG                 = 118, // 0x76
    DNEG                 = 119, // 0x77
    ISHL                 = 120, // 0x78
    LSHL                 = 121, // 0x79
    ISHR                 = 122, // 0x7A
    LSHR                 = 123, // 0x7B
    IUSHR                = 124, // 0x7C
    LUSHR                = 125, // 0x7D
    IAND                 = 126, // 0x7E
    LAND                 = 127, // 0x7F
    IOR                  = 128, // 0x80
    LOR                  = 129, // 0x81
    IXOR                 = 130, // 0x82
    LXOR                 = 131, // 0x83
    IINC                 = 132, // 0x84
    I2L                  = 133, // 0x85
    I2F                  = 134, // 0x86
    I2D                  = 135, // 0x87
    L2I                  = 136, // 0x88
    L2F                  = 137, // 0x89
    L2D                  = 138, // 0x8A
    F2I                  = 139, // 0x8B
    F2L                  = 140, // 0x8C
    F2D                  = 141, // 0x8D
    D2I                  = 142, // 0x8E
    D2L                  = 143, // 0x8F
    D2F                  = 144, // 0x90
    I2B                  = 145, // 0x91
    I2C                  = 146, // 0x92
    I2S                  = 147, // 0x93
    LCMP                 = 148, // 0x94
    FCMPL                = 149, // 0x95
    FCMPG                = 150, // 0x96
    DCMPL                = 151, // 0x97
    DCMPG                = 152, // 0x98
    IFEQ                 = 153, // 0x99
    IFNE                 = 154, // 0x9A
    IFLT                 = 155, // 0x9B
    IFGE                 = 156, // 0x9C
    IFGT                 = 157, // 0x9D
    IFLE                 = 158, // 0x9E
    IF_ICMPEQ            = 159, // 0x9F
    IF_ICMPNE            = 160, // 0xA0
    IF_ICMPLT            = 161, // 0xA1
    IF_ICMPGE            = 162, // 0xA2
    IF_ICMPGT            = 163, // 0xA3
    IF_ICMPLE            = 164, // 0xA4
    IF_ACMPEQ            = 165, // 0xA5
    IF_ACMPNE            = 166, // 0xA6
    GOTO                 = 167, // 0xA7
    JSR                  = 168, // 0xA8
    RET                  = 169, // 0xA9
    TABLESWITCH          = 170, // 0xAA
    LOOKUPSWITCH         = 171, // 0xAB
    IRETURN              = 172, // 0xAC
    LRETURN              = 173, // 0xAD
    FRETURN              = 174, // 0xAE
    DRETURN              = 175, // 0xAF
    ARETURN              = 176, // 0xB0
    RETURN               = 177, // 0xB1
    GETSTATIC            = 178, // 0xB2
    PUTSTATIC            = 179, // 0xB3
    GETFIELD             = 180, // 0xB4
    PUTFIELD             = 181, // 0xB5
    INVOKEVIRTUAL        = 182, // 0xB6
    INVOKESPECIAL        = 183, // 0xB7
    INVOKESTATIC         = 184, // 0xB8
    INVOKEINTERFACE      = 185, // 0xB9
    XXXUNUSEDXXX         = 186, // 0xBA
    NEW                  = 187, // 0xBB
    NEWARRAY             = 188, // 0xBC
    ANEWARRAY            = 189, // 0xBD
    ARRAYLENGTH          = 190, // 0xBE
    ATHROW               = 191, // 0xBF
    CHECKCAST            = 192, // 0xC0
    INSTANCEOF           = 193, // 0xC1
    MONITORENTER         = 194, // 0xC2
    MONITOREXIT          = 195, // 0xC3
    WIDE                 = 196, // 0xC4
    MULTIANEWARRAY       = 197, // 0xC5
    IFNULL               = 198, // 0xC6
    IFNONNULL            = 199, // 0xC7
    GOTO_W               = 200, // 0xC8
    JSR_W                = 201, // 0xC9
    BREAKPOINT           = 202, // 0xCA

    ILLEGAL              = 255,
    END                  = 256,

    /**
     * The last opcode defined by the JVM specification. To iterate over all JVM bytecodes:
     */
    LAST_JVM_OPCODE     = Bytecodes.JSR_W
  }

  enum Flags {
    /**
     * Denotes an instruction that ends a basic block and does not let control flow fall through to its lexical successor.
     */
    STOP = 0x00000001,

    /**
     * Denotes an instruction that ends a basic block and may let control flow fall through to its lexical successor.
     * In practice this means it is a conditional branch.
     */
    FALL_THROUGH = 0x00000002,

    /**
     * Denotes an instruction that has a 2 or 4 byte operand that is an offset to another instruction in the same method.
     * This does not include the {@link Bytecodes#TABLESWITCH} or {@link Bytecodes#LOOKUPSWITCH} instructions.
     */
    BRANCH = 0x00000004,

    /**
     * Denotes an instruction that reads the value of a static or instance field.
     */
    FIELD_READ = 0x00000008,

    /**
     * Denotes an instruction that writes the value of a static or instance field.
     */
    FIELD_WRITE = 0x00000010,

    /**
     * Denotes an instruction that is not defined in the JVM specification.
     */
    EXTENSION = 0x00000020,

    /**
     * Denotes an instruction that can cause aFlags.TRAP.
     */
    TRAP        = 0x00000080,

    /**
     * Denotes an instruction that is commutative.
     */
    COMMUTATIVE = 0x00000100,

    /**
     * Denotes an instruction that is associative.
     */
    ASSOCIATIVE = 0x00000200,

    /**
     * Denotes an instruction that loads an operand.
     */
    LOAD        = 0x00000400,

    /**
     * Denotes an instruction that stores an operand.
     */
    STORE       = 0x00000800,

    /**
     * Denotes the 4 INVOKE* instructions.
     */
    INVOKE       = 0x00001000
  }

  /**
   * A array that maps from a bytecode value to the set of {@link Flags} for the corresponding instruction.
   */
  export var flags = new Uint32Array(256);

  /**
   * A array that maps from a bytecode value to the length in bytes for the corresponding instruction.
   */
  export var length = new Uint32Array(256);

  var writer = new IndentingWriter();

  function define(opcode: number, name: string, format: string, flags: Flags = 0) {
    var instructionLength = format.length;
    length[opcode] = instructionLength;
    flags[opcode] = flags;
    assert (!isConditionalBranch(opcode) || isBranch(opcode), "a conditional branch must also be a branch");
  }

  define(Bytecodes.NOP                 , "nop"             , "b"    );
  define(Bytecodes.ACONST_NULL         , "aconst_null"     , "b"    );
  define(Bytecodes.ICONST_M1           , "iconst_m1"       , "b"    );
  define(Bytecodes.ICONST_0            , "iconst_0"        , "b"    );
  define(Bytecodes.ICONST_1            , "iconst_1"        , "b"    );
  define(Bytecodes.ICONST_2            , "iconst_2"        , "b"    );
  define(Bytecodes.ICONST_3            , "iconst_3"        , "b"    );
  define(Bytecodes.ICONST_4            , "iconst_4"        , "b"    );
  define(Bytecodes.ICONST_5            , "iconst_5"        , "b"    );
  define(Bytecodes.LCONST_0            , "lconst_0"        , "b"    );
  define(Bytecodes.LCONST_1            , "lconst_1"        , "b"    );
  define(Bytecodes.FCONST_0            , "fconst_0"        , "b"    );
  define(Bytecodes.FCONST_1            , "fconst_1"        , "b"    );
  define(Bytecodes.FCONST_2            , "fconst_2"        , "b"    );
  define(Bytecodes.DCONST_0            , "dconst_0"        , "b"    );
  define(Bytecodes.DCONST_1            , "dconst_1"        , "b"    );
  define(Bytecodes.BIPUSH              , "bipush"          , "bc"   );
  define(Bytecodes.SIPUSH              , "sipush"          , "bcc"  );
  define(Bytecodes.LDC                 , "ldc"             , "bi"   , Flags.TRAP);
  define(Bytecodes.LDC_W               , "ldc_w"           , "bii"  , Flags.TRAP);
  define(Bytecodes.LDC2_W              , "ldc2_w"          , "bii"  , Flags.TRAP);
  define(Bytecodes.ILOAD               , "iload"           , "bi"   , Flags.LOAD);
  define(Bytecodes.LLOAD               , "lload"           , "bi"   , Flags.LOAD);
  define(Bytecodes.FLOAD               , "fload"           , "bi"   , Flags.LOAD);
  define(Bytecodes.DLOAD               , "dload"           , "bi"   , Flags.LOAD);
  define(Bytecodes.ALOAD               , "aload"           , "bi"   , Flags.LOAD);
  define(Bytecodes.ILOAD_0             , "iload_0"         , "b"    , Flags.LOAD);
  define(Bytecodes.ILOAD_1             , "iload_1"         , "b"    , Flags.LOAD);
  define(Bytecodes.ILOAD_2             , "iload_2"         , "b"    , Flags.LOAD);
  define(Bytecodes.ILOAD_3             , "iload_3"         , "b"    , Flags.LOAD);
  define(Bytecodes.LLOAD_0             , "lload_0"         , "b"    , Flags.LOAD);
  define(Bytecodes.LLOAD_1             , "lload_1"         , "b"    , Flags.LOAD);
  define(Bytecodes.LLOAD_2             , "lload_2"         , "b"    , Flags.LOAD);
  define(Bytecodes.LLOAD_3             , "lload_3"         , "b"    , Flags.LOAD);
  define(Bytecodes.FLOAD_0             , "fload_0"         , "b"    , Flags.LOAD);
  define(Bytecodes.FLOAD_1             , "fload_1"         , "b"    , Flags.LOAD);
  define(Bytecodes.FLOAD_2             , "fload_2"         , "b"    , Flags.LOAD);
  define(Bytecodes.FLOAD_3             , "fload_3"         , "b"    , Flags.LOAD);
  define(Bytecodes.DLOAD_0             , "dload_0"         , "b"    , Flags.LOAD);
  define(Bytecodes.DLOAD_1             , "dload_1"         , "b"    , Flags.LOAD);
  define(Bytecodes.DLOAD_2             , "dload_2"         , "b"    , Flags.LOAD);
  define(Bytecodes.DLOAD_3             , "dload_3"         , "b"    , Flags.LOAD);
  define(Bytecodes.ALOAD_0             , "aload_0"         , "b"    , Flags.LOAD);
  define(Bytecodes.ALOAD_1             , "aload_1"         , "b"    , Flags.LOAD);
  define(Bytecodes.ALOAD_2             , "aload_2"         , "b"    , Flags.LOAD);
  define(Bytecodes.ALOAD_3             , "aload_3"         , "b"    , Flags.LOAD);
  define(Bytecodes.IALOAD              , "iaload"          , "b"    , Flags.TRAP);
  define(Bytecodes.LALOAD              , "laload"          , "b"    , Flags.TRAP);
  define(Bytecodes.FALOAD              , "faload"          , "b"    , Flags.TRAP);
  define(Bytecodes.DALOAD              , "daload"          , "b"    , Flags.TRAP);
  define(Bytecodes.AALOAD              , "aaload"          , "b"    , Flags.TRAP);
  define(Bytecodes.BALOAD              , "baload"          , "b"    , Flags.TRAP);
  define(Bytecodes.CALOAD              , "caload"          , "b"    , Flags.TRAP);
  define(Bytecodes.SALOAD              , "saload"          , "b"    , Flags.TRAP);
  define(Bytecodes.ISTORE              , "istore"          , "bi"   , Flags.STORE);
  define(Bytecodes.LSTORE              , "lstore"          , "bi"   , Flags.STORE);
  define(Bytecodes.FSTORE              , "fstore"          , "bi"   , Flags.STORE);
  define(Bytecodes.DSTORE              , "dstore"          , "bi"   , Flags.STORE);
  define(Bytecodes.ASTORE              , "astore"          , "bi"   , Flags.STORE);
  define(Bytecodes.ISTORE_0            , "istore_0"        , "b"    , Flags.STORE);
  define(Bytecodes.ISTORE_1            , "istore_1"        , "b"    , Flags.STORE);
  define(Bytecodes.ISTORE_2            , "istore_2"        , "b"    , Flags.STORE);
  define(Bytecodes.ISTORE_3            , "istore_3"        , "b"    , Flags.STORE);
  define(Bytecodes.LSTORE_0            , "lstore_0"        , "b"    , Flags.STORE);
  define(Bytecodes.LSTORE_1            , "lstore_1"        , "b"    , Flags.STORE);
  define(Bytecodes.LSTORE_2            , "lstore_2"        , "b"    , Flags.STORE);
  define(Bytecodes.LSTORE_3            , "lstore_3"        , "b"    , Flags.STORE);
  define(Bytecodes.FSTORE_0            , "fstore_0"        , "b"    , Flags.STORE);
  define(Bytecodes.FSTORE_1            , "fstore_1"        , "b"    , Flags.STORE);
  define(Bytecodes.FSTORE_2            , "fstore_2"        , "b"    , Flags.STORE);
  define(Bytecodes.FSTORE_3            , "fstore_3"        , "b"    , Flags.STORE);
  define(Bytecodes.DSTORE_0            , "dstore_0"        , "b"    , Flags.STORE);
  define(Bytecodes.DSTORE_1            , "dstore_1"        , "b"    , Flags.STORE);
  define(Bytecodes.DSTORE_2            , "dstore_2"        , "b"    , Flags.STORE);
  define(Bytecodes.DSTORE_3            , "dstore_3"        , "b"    , Flags.STORE);
  define(Bytecodes.ASTORE_0            , "astore_0"        , "b"    , Flags.STORE);
  define(Bytecodes.ASTORE_1            , "astore_1"        , "b"    , Flags.STORE);
  define(Bytecodes.ASTORE_2            , "astore_2"        , "b"    , Flags.STORE);
  define(Bytecodes.ASTORE_3            , "astore_3"        , "b"    , Flags.STORE);
  define(Bytecodes.IASTORE             , "iastore"         , "b"    , Flags.TRAP);
  define(Bytecodes.LASTORE             , "lastore"         , "b"    , Flags.TRAP);
  define(Bytecodes.FASTORE             , "fastore"         , "b"    , Flags.TRAP);
  define(Bytecodes.DASTORE             , "dastore"         , "b"    , Flags.TRAP);
  define(Bytecodes.AASTORE             , "aastore"         , "b"    , Flags.TRAP);
  define(Bytecodes.BASTORE             , "bastore"         , "b"    , Flags.TRAP);
  define(Bytecodes.CASTORE             , "castore"         , "b"    , Flags.TRAP);
  define(Bytecodes.SASTORE             , "sastore"         , "b"    , Flags.TRAP);
  define(Bytecodes.POP                 , "pop"             , "b"    );
  define(Bytecodes.POP2                , "pop2"            , "b"    );
  define(Bytecodes.DUP                 , "dup"             , "b"    );
  define(Bytecodes.DUP_X1              , "dup_x1"          , "b"    );
  define(Bytecodes.DUP_X2              , "dup_x2"          , "b"    );
  define(Bytecodes.DUP2                , "dup2"            , "b"    );
  define(Bytecodes.DUP2_X1             , "dup2_x1"         , "b"    );
  define(Bytecodes.DUP2_X2             , "dup2_x2"         , "b"    );
  define(Bytecodes.SWAP                , "swap"            , "b"    );
  define(Bytecodes.IADD                , "iadd"            , "b"    , Flags.COMMUTATIVE | Flags.ASSOCIATIVE);
  define(Bytecodes.LADD                , "ladd"            , "b"    , Flags.COMMUTATIVE | Flags.ASSOCIATIVE);
  define(Bytecodes.FADD                , "fadd"            , "b"    , Flags.COMMUTATIVE | Flags.ASSOCIATIVE);
  define(Bytecodes.DADD                , "dadd"            , "b"    , Flags.COMMUTATIVE | Flags.ASSOCIATIVE);
  define(Bytecodes.ISUB                , "isub"            , "b"    );
  define(Bytecodes.LSUB                , "lsub"            , "b"    );
  define(Bytecodes.FSUB                , "fsub"            , "b"    );
  define(Bytecodes.DSUB                , "dsub"            , "b"    );
  define(Bytecodes.IMUL                , "imul"            , "b"    , Flags.COMMUTATIVE | Flags.ASSOCIATIVE);
  define(Bytecodes.LMUL                , "lmul"            , "b"    , Flags.COMMUTATIVE | Flags.ASSOCIATIVE);
  define(Bytecodes.FMUL                , "fmul"            , "b"    , Flags.COMMUTATIVE | Flags.ASSOCIATIVE);
  define(Bytecodes.DMUL                , "dmul"            , "b"    , Flags.COMMUTATIVE | Flags.ASSOCIATIVE);
  define(Bytecodes.IDIV                , "idiv"            , "b"    , Flags.TRAP);
  define(Bytecodes.LDIV                , "ldiv"            , "b"    , Flags.TRAP);
  define(Bytecodes.FDIV                , "fdiv"            , "b"    );
  define(Bytecodes.DDIV                , "ddiv"            , "b"    );
  define(Bytecodes.IREM                , "irem"            , "b"    , Flags.TRAP);
  define(Bytecodes.LREM                , "lrem"            , "b"    , Flags.TRAP);
  define(Bytecodes.FREM                , "frem"            , "b"    );
  define(Bytecodes.DREM                , "drem"            , "b"    );
  define(Bytecodes.INEG                , "ineg"            , "b"    );
  define(Bytecodes.LNEG                , "lneg"            , "b"    );
  define(Bytecodes.FNEG                , "fneg"            , "b"    );
  define(Bytecodes.DNEG                , "dneg"            , "b"    );
  define(Bytecodes.ISHL                , "ishl"            , "b"    );
  define(Bytecodes.LSHL                , "lshl"            , "b"    );
  define(Bytecodes.ISHR                , "ishr"            , "b"    );
  define(Bytecodes.LSHR                , "lshr"            , "b"    );
  define(Bytecodes.IUSHR               , "iushr"           , "b"    );
  define(Bytecodes.LUSHR               , "lushr"           , "b"    );
  define(Bytecodes.IAND                , "iand"            , "b"    , Flags.COMMUTATIVE | Flags.ASSOCIATIVE);
  define(Bytecodes.LAND                , "land"            , "b"    , Flags.COMMUTATIVE | Flags.ASSOCIATIVE);
  define(Bytecodes.IOR                 , "ior"             , "b"    , Flags.COMMUTATIVE | Flags.ASSOCIATIVE);
  define(Bytecodes.LOR                 , "lor"             , "b"    , Flags.COMMUTATIVE | Flags.ASSOCIATIVE);
  define(Bytecodes.IXOR                , "ixor"            , "b"    , Flags.COMMUTATIVE | Flags.ASSOCIATIVE);
  define(Bytecodes.LXOR                , "lxor"            , "b"    , Flags.COMMUTATIVE | Flags.ASSOCIATIVE);
  define(Bytecodes.IINC                , "iinc"            , "bic"  , Flags.LOAD | Flags.STORE);
  define(Bytecodes.I2L                 , "i2l"             , "b"    );
  define(Bytecodes.I2F                 , "i2f"             , "b"    );
  define(Bytecodes.I2D                 , "i2d"             , "b"    );
  define(Bytecodes.L2I                 , "l2i"             , "b"    );
  define(Bytecodes.L2F                 , "l2f"             , "b"    );
  define(Bytecodes.L2D                 , "l2d"             , "b"    );
  define(Bytecodes.F2I                 , "f2i"             , "b"    );
  define(Bytecodes.F2L                 , "f2l"             , "b"    );
  define(Bytecodes.F2D                 , "f2d"             , "b"    );
  define(Bytecodes.D2I                 , "d2i"             , "b"    );
  define(Bytecodes.D2L                 , "d2l"             , "b"    );
  define(Bytecodes.D2F                 , "d2f"             , "b"    );
  define(Bytecodes.I2B                 , "i2b"             , "b"    );
  define(Bytecodes.I2C                 , "i2c"             , "b"    );
  define(Bytecodes.I2S                 , "i2s"             , "b"    );
  define(Bytecodes.LCMP                , "lcmp"            , "b"    );
  define(Bytecodes.FCMPL               , "fcmpl"           , "b"    );
  define(Bytecodes.FCMPG               , "fcmpg"           , "b"    );
  define(Bytecodes.DCMPL               , "dcmpl"           , "b"    );
  define(Bytecodes.DCMPG               , "dcmpg"           , "b"    );
  define(Bytecodes.IFEQ                , "ifeq"            , "boo"  , Flags.FALL_THROUGH | Flags.BRANCH);
  define(Bytecodes.IFNE                , "ifne"            , "boo"  , Flags.FALL_THROUGH | Flags.BRANCH);
  define(Bytecodes.IFLT                , "iflt"            , "boo"  , Flags.FALL_THROUGH | Flags.BRANCH);
  define(Bytecodes.IFGE                , "ifge"            , "boo"  , Flags.FALL_THROUGH | Flags.BRANCH);
  define(Bytecodes.IFGT                , "ifgt"            , "boo"  , Flags.FALL_THROUGH | Flags.BRANCH);
  define(Bytecodes.IFLE                , "ifle"            , "boo"  , Flags.FALL_THROUGH | Flags.BRANCH);
  define(Bytecodes.IF_ICMPEQ           , "if_icmpeq"       , "boo"  , Flags.COMMUTATIVE | Flags.FALL_THROUGH | Flags.BRANCH);
  define(Bytecodes.IF_ICMPNE           , "if_icmpne"       , "boo"  , Flags.COMMUTATIVE | Flags.FALL_THROUGH | Flags.BRANCH);
  define(Bytecodes.IF_ICMPLT           , "if_icmplt"       , "boo"  , Flags.FALL_THROUGH | Flags.BRANCH);
  define(Bytecodes.IF_ICMPGE           , "if_icmpge"       , "boo"  , Flags.FALL_THROUGH | Flags.BRANCH);
  define(Bytecodes.IF_ICMPGT           , "if_icmpgt"       , "boo"  , Flags.FALL_THROUGH | Flags.BRANCH);
  define(Bytecodes.IF_ICMPLE           , "if_icmple"       , "boo"  , Flags.FALL_THROUGH | Flags.BRANCH);
  define(Bytecodes.IF_ACMPEQ           , "if_acmpeq"       , "boo"  , Flags.COMMUTATIVE | Flags.FALL_THROUGH | Flags.BRANCH);
  define(Bytecodes.IF_ACMPNE           , "if_acmpne"       , "boo"  , Flags.COMMUTATIVE | Flags.FALL_THROUGH | Flags.BRANCH);
  define(Bytecodes.GOTO                , "goto"            , "boo"  , Flags.STOP | Flags.BRANCH);
  define(Bytecodes.JSR                 , "jsr"             , "boo"  , Flags.STOP | Flags.BRANCH);
  define(Bytecodes.RET                 , "ret"             , "bi"   , Flags.STOP);
  define(Bytecodes.TABLESWITCH         , "tableswitch"     , ""     , Flags.STOP);
  define(Bytecodes.LOOKUPSWITCH        , "lookupswitch"    , ""     , Flags.STOP);
  define(Bytecodes.IRETURN             , "ireturn"         , "b"    , Flags.TRAP | Flags.STOP);
  define(Bytecodes.LRETURN             , "lreturn"         , "b"    , Flags.TRAP | Flags.STOP);
  define(Bytecodes.FRETURN             , "freturn"         , "b"    , Flags.TRAP | Flags.STOP);
  define(Bytecodes.DRETURN             , "dreturn"         , "b"    , Flags.TRAP | Flags.STOP);
  define(Bytecodes.ARETURN             , "areturn"         , "b"    , Flags.TRAP | Flags.STOP);
  define(Bytecodes.RETURN              , "return"          , "b"    , Flags.TRAP | Flags.STOP);
  define(Bytecodes.GETSTATIC           , "getstatic"       , "bjj"  , Flags.TRAP | Flags.FIELD_READ);
  define(Bytecodes.PUTSTATIC           , "putstatic"       , "bjj"  , Flags.TRAP | Flags.FIELD_WRITE);
  define(Bytecodes.GETFIELD            , "getfield"        , "bjj"  , Flags.TRAP | Flags.FIELD_READ);
  define(Bytecodes.PUTFIELD            , "putfield"        , "bjj"  , Flags.TRAP | Flags.FIELD_WRITE);
  define(Bytecodes.INVOKEVIRTUAL       , "invokevirtual"   , "bjj"  , Flags.TRAP | Flags.INVOKE);
  define(Bytecodes.INVOKESPECIAL       , "invokespecial"   , "bjj"  , Flags.TRAP | Flags.INVOKE);
  define(Bytecodes.INVOKESTATIC        , "invokestatic"    , "bjj"  , Flags.TRAP | Flags.INVOKE);
  define(Bytecodes.INVOKEINTERFACE     , "invokeinterface" , "bjja_", Flags.TRAP | Flags.INVOKE);
  define(Bytecodes.XXXUNUSEDXXX        , "xxxunusedxxx"    , ""     );
  define(Bytecodes.NEW                 , "new"             , "bii"  , Flags.TRAP);
  define(Bytecodes.NEWARRAY            , "newarray"        , "bc"   , Flags.TRAP);
  define(Bytecodes.ANEWARRAY           , "anewarray"       , "bii"  , Flags.TRAP);
  define(Bytecodes.ARRAYLENGTH         , "arraylength"     , "b"    , Flags.TRAP);
  define(Bytecodes.ATHROW              , "athrow"          , "b"    , Flags.TRAP | Flags.STOP);
  define(Bytecodes.CHECKCAST           , "checkcast"       , "bii"  , Flags.TRAP);
  define(Bytecodes.INSTANCEOF          , "instanceof"      , "bii"  , Flags.TRAP);
  define(Bytecodes.MONITORENTER        , "monitorenter"    , "b"    , Flags.TRAP);
  define(Bytecodes.MONITOREXIT         , "monitorexit"     , "b"    , Flags.TRAP);
  define(Bytecodes.WIDE                , "wide"            , ""     );
  define(Bytecodes.MULTIANEWARRAY      , "multianewarray"  , "biic" , Flags.TRAP);
  define(Bytecodes.IFNULL              , "ifnull"          , "boo"  , Flags.FALL_THROUGH | Flags.BRANCH);
  define(Bytecodes.IFNONNULL           , "ifnonnull"       , "boo"  , Flags.FALL_THROUGH | Flags.BRANCH);
  define(Bytecodes.GOTO_W              , "goto_w"          , "boooo", Flags.STOP | Flags.BRANCH);
  define(Bytecodes.JSR_W               , "jsr_w"           , "boooo", Flags.STOP | Flags.BRANCH);
  define(Bytecodes.BREAKPOINT          , "breakpoint"      , "b"    , Flags.TRAP);

  /**
   * Gets the length of an instruction denoted by a given opcode.
   */
  export function lengthOf(opcode: Bytecodes): number {
    return length[opcode & 0xff];
  }

  export function lengthAt(code: Uint8Array, bci: number): number {
    var opcode = Bytes.beU1(code, bci);
    var _length = length[opcode & 0xff];
    if (_length == 0) {
      switch (opcode) {
        case Bytecodes.TABLESWITCH: {
          return new BytecodeTableSwitch(code, bci).size();
        }
        case Bytecodes.LOOKUPSWITCH: {
          return new BytecodeLookupSwitch(code, bci).size();
        }
        case Bytecodes.WIDE: {
          var opc = Bytes.beU1(code, bci + 1);
          if (opc == Bytecodes.RET) {
            return 4;
          } else if (opc == Bytecodes.IINC) {
            return 6;
          } else {
            return 4; // a load or store bytecode
          }
        }
        default:
          throw new Error("unknown variable-length bytecode: " + opcode);
      }
    }
    return _length;
  }

  /**
   * Determines if an opcode is commutative.
   */
  function isCommutative(opcode: Bytecodes): boolean {
    return (flags[opcode & 0xff] & Flags.COMMUTATIVE) != 0;
  }

  /**
   * Determines if a given opcode denotes an instruction that can cause an implicit exception.
   */
  export function canTrap(opcode: Bytecodes): boolean {
    return (flags[opcode & 0xff] & Flags.TRAP) != 0;
  }

  /**
   * Determines if a given opcode denotes an instruction that loads a local variable to the operand stack.
   */
  function isLoad(opcode: Bytecodes): boolean {
    return (flags[opcode & 0xff] & Flags.LOAD) != 0;
  }

  /**
   * Determines if a given opcode denotes an instruction that ends a basic block and does not let control flow fall
   * through to its lexical successor.
   */
  function isStop(opcode: Bytecodes): boolean {
    return (flags[opcode & 0xff] & Flags.STOP) != 0;
  }

  /**
   * Determines if a given opcode denotes an instruction that stores a value to a local variable
   * after popping it from the operand stack.
   */
  function isInvoke(opcode: Bytecodes): boolean {
    return (flags[opcode & 0xff] & Flags.INVOKE) != 0;
  }

  /**
   * Determines if a given opcode denotes an instruction that stores a value to a local variable
   * after popping it from the operand stack.
   */
  function isStore(opcode: Bytecodes): boolean {
    return (flags[opcode & 0xff] & Flags.STORE) != 0;
  }

  /**
   * Determines if a given opcode is an instruction that delimits a basic block.
   */
  function isBlockEnd(opcode: Bytecodes): boolean {
    return (flags[opcode & 0xff] & (Flags.STOP | Flags.FALL_THROUGH)) != 0;
  }

  /**
   * Determines if a given opcode is an instruction that has a 2 or 4 byte operand that is an offset to another
   * instruction in the same method. This does not include the {@linkplain #TABLESWITCH switch} instructions.
   */
  function isBranch(opcode: Bytecodes): boolean {
    return (flags[opcode & 0xff] & Flags.BRANCH) != 0;
  }

  /**
   * Determines if a given opcode denotes a conditional branch.
   */
  function isConditionalBranch(opcode: Bytecodes): boolean {
    return (flags[opcode & 0xff] & Flags.FALL_THROUGH) != 0;
  }

  /**
   * Determines if a given opcode denotes a standard bytecode. A standard bytecode is
   * defined in the JVM specification.
   */
  function isStandard(opcode: Bytecodes): boolean {
    return (flags[opcode & 0xff] & Flags.EXTENSION) == 0;
  }

  /**
   * Determines if a given opcode denotes an extended bytecode.
   */
  function isExtended(opcode: Bytecodes): boolean {
    return (flags[opcode & 0xff] & Flags.EXTENSION) != 0;
  }

  /**
   * Determines if a given opcode is a three-byte extended bytecode.
   */
  function isThreeByteExtended(opcode: Bytecodes): boolean {
    return (opcode & ~0xff) != 0;
  }
  
  export class BytecodeSwitch {
//    /**
//     * The {@link BytecodeStream} containing bytecode array or {@code null} if {@link #code} is not {@code null}.
//     */
//    private final BytecodeStream stream;
//
    /**
     * The bytecode array or {@code null} if {@link #stream} is not {@code null}.
     */
    private code: Uint8Array;

    /**
     * Index of start of switch instruction.
     */
    protected bci: number;

    /**
     * Index of the start of the additional data for the switch instruction, aligned to a multiple of four from the method start.
     */
    protected alignedBci: number;

    /**
     * Constructor for a bytecode array.
     * @param code the bytecode array containing the switch instruction.
     * @param bci the index in the array of the switch instruction
     */
    constructor(code: Uint8Array, bci: number) {
      this.alignedBci = (bci + 4) & 0xfffffffc;
      this.code = code;
      this.bci = bci;
    }

    /**
     * Gets the index of the instruction denoted by the {@code i}'th switch target.
     * @param i index of the switch target
     * @return the index of the instruction denoted by the {@code i}'th switch target
     */
    public targetAt(i: number): number {
      return this.bci + this.offsetAt(i);
    }

    /**
     * Gets the index of the instruction for the default switch target.
     * @return the index of the instruction for the default switch target
     */
    public defaultTarget(): number {
      return this.bci + this.defaultOffset();
    }

    /**
     * Gets the offset from the start of the switch instruction to the default switch target.
     * @return the offset to the default switch target
     */
    public defaultOffset(): number {
      throw Debug.abstractMethod("defaultOffset");
    }

    /**
     * Gets the key at {@code i}'th switch target index.
     * @param i the switch target index
     * @return the key at {@code i}'th switch target index
     */
    public keyAt(i: number): number {
      throw Debug.abstractMethod("defaultOffset");
    }

    /**
     * Gets the offset from the start of the switch instruction for the {@code i}'th switch target.
     * @param i the switch target index
     * @return the offset to the {@code i}'th switch target
     */
    public offsetAt(i: number): number {
      throw Debug.abstractMethod("defaultOffset");
    }

    /**
     * Gets the number of switch targets.
     * @return the number of switch targets
     */
    public numberOfCases(): number {
      throw Debug.abstractMethod("defaultOffset");
    }

    /**
     * Gets the total size in bytes of the switch instruction.
     * @return the total size in bytes of the switch instruction
     */
    public size(): number {
      throw Debug.abstractMethod("defaultOffset");
    }

    /**
     * Reads the signed value at given bytecode index.
     * @param bci the start index of the value to retrieve
     * @return the signed, 4-byte value in the bytecode array starting at {@code bci}
     */
    protected readWord(bci: number): number {
      return Bytes.beS4(this.code, bci);
    }
  }

  export class BytecodeTableSwitch extends BytecodeSwitch {
    private static OFFSET_TO_LOW_KEY = 4;
    private static OFFSET_TO_HIGH_KEY = 8;
    private static OFFSET_TO_FIRST_JUMP_OFFSET = 12;
    private static JUMP_OFFSET_SIZE = 4;

    /**
     * Constructor for a bytecode array.
     * @param code the bytecode array containing the switch instruction.
     * @param bci the index in the array of the switch instruction
     */
    constructor(code: Uint8Array, bci: number) {
      super(code, bci);
    }

    /**
     * Gets the low key of the table switch.
     */
    public lowKey() {
      return this.readWord(this.alignedBci + BytecodeTableSwitch.OFFSET_TO_LOW_KEY);
    }

    /**
     * Gets the high key of the table switch.
     */
    public highKey() {
      return this.readWord(this.alignedBci + BytecodeTableSwitch.OFFSET_TO_HIGH_KEY);
    }

    public keyAt(i: number) {
      return this.lowKey() + i;
    }

    public defaultOffset(): number {
      return this.readWord(this.alignedBci);
    }

    public offsetAt(i: number): number {
      return this.readWord(this.alignedBci + BytecodeTableSwitch.OFFSET_TO_FIRST_JUMP_OFFSET + BytecodeTableSwitch.JUMP_OFFSET_SIZE * i);
    }

    public numberOfCases(): number {
      return this.highKey() - this.lowKey() + 1;
    }

    public size(): number {
      return this.alignedBci + BytecodeTableSwitch.OFFSET_TO_FIRST_JUMP_OFFSET + BytecodeTableSwitch.JUMP_OFFSET_SIZE * this.numberOfCases() - this.bci;
    }
  }

  export class BytecodeLookupSwitch extends BytecodeSwitch {
    private static OFFSET_TO_NUMBER_PAIRS = 4;
    private static OFFSET_TO_FIRST_PAIR_MATCH = 8;
    private static OFFSET_TO_FIRST_PAIR_OFFSET = 12;
    private static PAIR_SIZE = 8;

    constructor(code: Uint8Array, bci: number) {
      super(code, bci);
    }

    public defaultOffset(): number {
      return this.readWord(this.alignedBci);
    }

    public offsetAt(i: number): number {
      return this.readWord(this.alignedBci + BytecodeLookupSwitch.OFFSET_TO_FIRST_PAIR_OFFSET + BytecodeLookupSwitch.PAIR_SIZE * i);
    }

    public keyAt(i): number {
      return this.readWord(this.alignedBci + BytecodeLookupSwitch.OFFSET_TO_FIRST_PAIR_MATCH + BytecodeLookupSwitch.PAIR_SIZE * i);
    }

    public numberOfCases(): number {
      return this.readWord(this.alignedBci + BytecodeLookupSwitch.OFFSET_TO_NUMBER_PAIRS);
    }

    public size(): number {
      return this.alignedBci + BytecodeLookupSwitch.OFFSET_TO_FIRST_PAIR_MATCH + BytecodeLookupSwitch.PAIR_SIZE * this.numberOfCases() - this.bci;
    }
  }

  /**
   * A utility class that makes iterating over bytecodes and reading operands
   * simpler and less error prone. For example, it handles the {@link Bytecodes#WIDE} instruction
   * and wide variants of instructions internally.
   */
  export class BytecodeStream {

    private _code: Uint8Array;
    private _opcode: Bytecodes;
    private _currentBCI: number;
    private _nextBCI: number;

    constructor(code: Uint8Array) {
      assert (code);
      this._code = code;
      this.setBCI(0);
    }

    /**
     * Advances to the next bytecode.
     */
    public next() {
      this.setBCI(this.nextBCI);
    }

    /**
     * Gets the bytecode index of the end of the code.
     */
    public endBCI(): number {
      return this._code.length;
    }

    /**
     * Gets the next bytecode index (no side-effects).
     */
    public get nextBCI(): number {
      return this._nextBCI;
    }

    /**
     * Gets the current bytecode index.
     */
    public get currentBCI(): number {
      return this._currentBCI;
    }


    /**
     * Gets the current opcode. This method will never return the
     * {@link Bytecodes#WIDE WIDE} opcode, but will instead
     * return the opcode that is modified by the {@code WIDE} opcode.
     * @return the current opcode; {@link Bytecodes#END} if at or beyond the end of the code
     */
    public currentBC(): Bytecodes {
      if (this._opcode === Bytecodes.WIDE) {
        return Bytes.beU1(this._code, this._currentBCI + 1);
      } else {
        return this._opcode;
      }
    }

    /**
     * Reads the index of a local variable for one of the load or store instructions.
     * The WIDE modifier is handled internally.
     */
    public readLocalIndex(): number {
      // read local variable index for load/store
      if (this._opcode == Bytecodes.WIDE) {
        return Bytes.beU2(this._code, this._currentBCI + 2);
      }
      return Bytes.beU1(this._code, this._currentBCI + 1);
    }

    /**
     * Read the delta for an {@link Bytecodes#IINC} bytecode.
     */
    public readIncrement(): number {
      // read the delta for the iinc bytecode
      if (this._opcode == Bytecodes.WIDE) {
        return Bytes.beS2(this._code, this._currentBCI + 4);
      }
      return Bytes.beS1(this._code, this._currentBCI + 2);
    }

    /**
     * Read the destination of a {@link Bytecodes#GOTO} or {@code IF} instructions.
     * @return the destination bytecode index
     */
    public readBranchDest(): number {
      // reads the destination for a branch bytecode
      return this._currentBCI + Bytes.beS2(this._code, this._currentBCI + 1);
    }

    /**
     * Read the destination of a {@link Bytecodes#GOTO_W} or {@link Bytecodes#JSR_W} instructions.
     * @return the destination bytecode index
     */
    public readFarBranchDest(): number {
      // reads the destination for a wide branch bytecode
      return this._currentBCI + Bytes.beS4(this._code, this._currentBCI + 1);
    }

    /**
     * Read a signed 4-byte integer from the bytecode stream at the specified bytecode index.
     * @param bci the bytecode index
     * @return the integer value
     */
    public readInt(bci: number): number {
      // reads a 4-byte signed value
      return Bytes.beS4(this._code, bci);
    }

    /**
     * Reads an unsigned, 1-byte value from the bytecode stream at the specified bytecode index.
     * @param bci the bytecode index
     * @return the byte
     */
    public readUByte(bci: number): number {
      return Bytes.beU1(this._code, bci);
    }

    /**
     * Reads a constant pool index for the current instruction.
     * @return the constant pool index
     */
    public readCPI(): number {
      if (this._opcode == Bytecodes.LDC) {
        return Bytes.beU1(this._code, this._currentBCI + 1);
      }
      return Bytes.beU2(this._code, this._currentBCI + 1) << 16 >> 16;
    }

    /**
     * Reads a signed, 1-byte value for the current instruction (e.g. BIPUSH).
     */
    public readByte(): number {
      return this._code[this._currentBCI + 1] << 24 >> 24;
    }

    /**
     * Reads a signed, 2-byte short for the current instruction (e.g. SIPUSH).
     */
    public readShort() {
      return Bytes.beS2(this._code, this._currentBCI + 1) << 16 >> 16;
    }

    /**
     * Sets the bytecode index to the specified value.
     * If {@code bci} is beyond the end of the array, {@link #currentBC} will return
     * {@link Bytecodes#END} and other methods may throw {@link ArrayIndexOutOfBoundsException}.
     * @param bci the new bytecode index
     */
    public setBCI(bci: number) {
      this._currentBCI = bci;
      if (this._currentBCI < this._code.length) {
        this._opcode = Bytes.beU1(this._code, bci);
        this._nextBCI = bci + lengthAt(this._code, bci);
      } else {
        this._opcode = Bytecodes.END;
        this._nextBCI = this._currentBCI;
      }
    }
  }
}