module J2ME {
  export var Bindings = {
    "java/lang/Object": {
      native: {
        "hashCode.()I": function (): number {
          var self: J2ME.java.lang.Object = this;
          return self.__hashCode__;
        }
      }
    }
  }
}