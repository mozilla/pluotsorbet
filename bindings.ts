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
    },
    "java/lang/Thread": {
      fields: {
        instanceSymbols: {
          "priority.I": "priority"
        }
      }
    },
    "java/io/ByteArrayOutputStream": {
      fields: {
        instanceSymbols: {
          "count.I": "count",
          "buf.[B": "buf"
        }
      }
    },
    "com/sun/cldc/i18n/j2me/UTF_8_Writer": {
      fields: {
        instanceSymbols: {
          "pendingSurrogate.I": "pendingSurrogate"
        }
      }
    },
    "com/nokia/mid/ui/DirectGraphicsImp": {
      fields: {
        instanceSymbols: {
          "graphics.Ljavax/microedition/lcdui/Graphics;": "graphics"
        }
      }
    },
    "javax/microedition/lcdui/Image": {
      fields: {
        instanceSymbols: {
          "imageData.Ljavax/microedition/lcdui/ImageData;": "imageData",
          "width.I": "width",
          "height.I": "height"
        }
      }
    },
    "javax/microedition/lcdui/ImageData": {
      fields: {
        instanceSymbols: {
          "width.I": "width",
          "height.I": "height"
        }
      }
    }
  }
}
