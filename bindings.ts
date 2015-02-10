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
    "com/sun/javame/sensor/ChannelModel": {
      fields: {
        instanceSymbols: {
          "scale.I": "scale",
          "name.Ljava/lang/String;": "name",
          "unit.Ljava/lang/String;": "unit",
          "dataType.I": "dataType",
          "accuracy.I": "accuracy",
          "mrangeCount.I": "mrangeCount",
          "mrageArray.[J": "mrageArray"
        }
      }
    },
    "com/sun/javame/sensor/SensorModel": {
      fields: {
        instanceSymbols: {
          "description.Ljava/lang/String;": "description",
          "model.Ljava/lang/String;": "model",
          "quantity.Ljava/lang/String;": "quantity",
          "contextType.Ljava/lang/String;": "contextType",
          "connectionType.I": "connectionType",
          "maxBufferSize.I": "maxBufferSize",
          "availabilityPush.Z": "availabilityPush",
          "conditionPush.Z": "conditionPush",
          "channelCount.I": "channelCount",
          "errorCodes.[I": "errorCodes",
          "errorMsgs.[Ljava/lang/String;": "errorMsgs",
          "properties.[Ljava/lang/String;": "properties"
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
    },
    "com/sun/midp/events/Event": {
      fields: {
        instanceSymbols: {
          "type.I": "type",
          "next.Lcom/sun/midp/events/Event;": "next",
        }
      }
    },
    "com/sun/midp/events/NativeEvent": {
      fields: {
        instanceSymbols: {
          "intParam1.I": "intParam1",
          "intParam2.I": "intParam2",
          "intParam3.I": "intParam3",
          "intParam4.I": "intParam4",
          "intParam5.I": "intParam5",
          "intParam6.I": "intParam6",
          "intParam7.I": "intParam7",
          "intParam8.I": "intParam8",
          "intParam9.I": "intParam9",
          "intParam10.I": "intParam10",
          "intParam11.I": "intParam11",
          "intParam12.I": "intParam12",
          "intParam13.I": "intParam13",
          "intParam14.I": "intParam14",
          "intParam15.I": "intParam15",
          "intParam16.I": "intParam16",
          "floatParam1.F": "floatParam1",
          "stringParam1.Ljava/lang/String;": "stringParam1",
          "stringParam2.Ljava/lang/String;": "stringParam2",
          "stringParam3.Ljava/lang/String;": "stringParam3",
          "stringParam4.Ljava/lang/String;": "stringParam4",
          "stringParam5.Ljava/lang/String;": "stringParam5",
          "stringParam6.Ljava/lang/String;": "stringParam6",
        }
      }
    }
  };

  export module java.lang {
    export interface Object {
      /**
       * Reference to the runtime klass.
       */
      klass: Klass

      /**
       * All objects have an internal hash code.
       */
      _hashCode: number;

      /**
       * Some objects may have a lock.
       */
      _lock: Lock;

      clone(): java.lang.Object;
      equals(obj: java.lang.Object): boolean;
      finalize(): void;
      getClass(): java.lang.Class;
      hashCode(): number;
      notify(): void;
      notifyAll(): void;
      toString(): java.lang.String;
      notify(): void;
      notify(timeout: number): void;
      notify(timeout: number, nanos: number): void;
    }

    export interface Class extends java.lang.Object {
      /**
       * RuntimeKlass associated with this Class object.
       */
      runtimeKlass: RuntimeKlass;
    }

    export interface String extends java.lang.Object {
      str: string;
    }

    export interface Thread extends java.lang.Object {
      pid: number;
      alive: boolean;
      priority: number;
    }

    export interface Exception extends java.lang.Object {
      message: string;
    }

    export interface IllegalArgumentException extends java.lang.Exception {
    }

    export interface IllegalStateException extends java.lang.Exception {
    }

    export interface NullPointerException extends java.lang.Exception {
    }

    export interface RuntimeException extends java.lang.Exception {
    }

    export interface IndexOutOfBoundsException extends java.lang.Exception {
    }

    export interface ArrayIndexOutOfBoundsException extends java.lang.Exception {
    }

    export interface StringIndexOutOfBoundsException extends java.lang.Exception {
    }

    export interface ArrayStoreException extends java.lang.Exception {
    }

    export interface IllegalMonitorStateException extends java.lang.Exception {
    }

    export interface ClassCastException extends java.lang.Exception {
    }

    export interface NegativeArraySizeException extends java.lang.Exception {
    }

    export interface ArithmeticException extends java.lang.Exception {
    }

    export interface ClassNotFoundException extends java.lang.Exception {
    }

    export interface SecurityException extends java.lang.Exception {
    }

    export interface IllegalThreadStateException extends java.lang.Exception {
    }

  }

  export module java.io {

    export interface IOException extends java.lang.Exception {
    }

    export interface UTFDataFormatException extends java.lang.Exception {
    }

    export interface UnsupportedEncodingException extends java.lang.Exception {
    }

    export interface OutputStream extends java.lang.Object {
    }

    export interface ByteArrayOutputStream extends OutputStream {
      count: number;
      buf: Int8Array;
    }

    export interface Writer extends java.lang.Object {}

  }

  export module javax.microedition.media {

    export interface MediaException extends java.lang.Exception {
    }

  }

  export module com.sun.cldc.isolate {
    export interface Isolate extends java.lang.Object {
      id: number;
      runtime: Runtime;
    }
  }

  export module com.sun.cldc.i18n {
    export interface StreamWriter extends java.io.Writer {
    }
  }

  export module com.sun.cldc.i18n.j2me {
    export interface UTF_8_Writer extends com.sun.cldc.i18n.StreamWriter {
      pendingSurrogate: number;
    }
  }

  export module javax.microedition.lcdui {
    export interface Graphics extends java.lang.Object {
    }

    export interface ImageData extends java.lang.Object {
      width: number;
      height: number;
    }

    export interface Image extends java.lang.Object {
      width: number;
      height: number;
      imageData: javax.microedition.lcdui.ImageData;
    }
  }

  export module com.nokia.mid.ui {
    export interface DirectGraphicsImp extends java.lang.Object {
      graphics: javax.microedition.lcdui.Graphics;
    }
  }

  export module com.sun.midp.events {
    export interface Event {
      type: number;
      next: com.sun.midp.events.Event;
    }

    export interface NativeEvent extends com.sun.midp.events.Event {
      intParam1: number;
      intParam2: number;
      intParam3: number;
      intParam4: number;
      intParam5: number;
      intParam6: number;
      intParam7: number;
      intParam8: number;
      intParam9: number;
      intParam10: number;
      intParam11: number;
      intParam12: number;
      intParam13: number;
      intParam14: number;
      intParam15: number;
      intParam16: number;
      floatParam1: number;
      stringParam1: java.lang.String;
      stringParam2: java.lang.String;
      stringParam3: java.lang.String;
      stringParam4: java.lang.String;
      stringParam5: java.lang.String;
      stringParam6: java.lang.String;
    }
  }
}

