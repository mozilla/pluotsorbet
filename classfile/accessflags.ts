module J2ME {
  enum ACCESS_FLAGS {
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
}
