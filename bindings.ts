module J2ME {
  export var Bindings = {
    "java/lang/Object": {
      native: {
        "hashCode.()I": function (): number {
          var self: J2ME.java.lang.Object = this;
          if (self._hashCode) {
            return self._hashCode;
          }
          return self._hashCode = $.nextHashCode();
        }
      }
    }
  }
}
