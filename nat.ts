/**
 * Asm.js native module declaration, this is defined by lib/native.js
 */
declare var ASM;

var Native = Object.create(null);

/**
 * Asm.js heap buffer and views.
 */
var buffer = ASM.buffer;
var u8: Uint32Array = ASM.HEAPU8;
var i32: Int32Array = ASM.HEAP32;
var u32: Uint32Array = ASM.HEAPU32;
var f32: Float32Array = ASM.HEAPF32;
var ref = J2ME.ArrayUtilities.makeDenseArray(buffer.byteLength >> 2, null);

var aliasedI32 = J2ME.IntegerUtilities.i32;
var aliasedF32 = J2ME.IntegerUtilities.f32;
var aliasedF64 = J2ME.IntegerUtilities.f64;

module J2ME {
  import assert = Debug.assert;
  import Bytecodes = Bytecode.Bytecodes;
  import toHEX = IntegerUtilities.toHEX;

  export function asyncImplOld(returnKind: string, promise: Promise<any>) {
    return asyncImpl(kindCharacterToKind(returnKind), promise);
  }

  /**
   * Suspends the execution of the current thread and resumes it later once the specified
   * |promise| is fulfilled.
   *
   * |onFulfilled| is called with one or two arguments |l| and |h|. |l| can be any
   * value, while |h| can only ever be the high bits of a long value.
   *
   * |onRejected| is called with a java.lang.Exception object.
   */
  export function asyncImpl(returnKind: Kind, promise: Promise<any>) {
    var ctx = $.ctx;

    promise.then(function onFulfilled(l: any, h?: number) {
      release || J2ME.Debug.assert(!(l instanceof Long.constructor), "Long objects are no longer supported, use low / high pairs.");
      var thread = ctx.nativeThread;

      // The caller's |pc| is currently at the invoke bytecode, we need to skip over the invoke when resuming.
      thread.advancePastInvokeBytecode();

      // Push return value.
      var sp = thread.sp;
      switch (returnKind) {
        case Kind.Double: // Doubles are passed in as a number value.
          aliasedF64[0] = l;
          i32[sp++] = aliasedI32[0];
          i32[sp++] = aliasedI32[1];
          break;
        case Kind.Float:
          f32[sp++] = l;
          break;
        case Kind.Long:
          i32[sp++] = l;
          i32[sp++] = h;
          break;
        case Kind.Int:
        case Kind.Byte:
        case Kind.Char:
        case Kind.Short:
        case Kind.Boolean:
          i32[sp++] = l;
          break;
        case Kind.Reference:
          ref[sp++] = l;
          break;
        case Kind.Void:
          break;
        default:
          release || J2ME.Debug.assert(false, "Invalid Kind: " + Kind[returnKind]);
      }
      thread.sp = sp;
      J2ME.Scheduler.enqueue(ctx);
    }, function onRejected(exception: java.lang.Exception) {
      var classInfo = CLASSES.getClass("org/mozilla/internal/Sys");
      var methodInfo = classInfo.getMethodByNameString("throwException", "(Ljava/lang/Exception;)V");
      ctx.nativeThread.pushFrame(methodInfo);
      ctx.nativeThread.frame.setParameter(J2ME.Kind.Reference, 0, exception);
      Scheduler.enqueue(ctx);
    });

    $.pause("Async");
  }

  Native["java/lang/Thread.sleep.(J)V"] = function(delayL: number, delayH: number) {
    asyncImpl(Kind.Void, new Promise(function(resolve, reject) {
      window.setTimeout(resolve, longToNumber(delayL, delayH));
    }));
  };

  Native["java/lang/Thread.isAlive.()Z"] = function() {
    return this.alive ? 1 : 0;
  };

  Native["java/lang/Thread.yield.()V"] = function() {
    $.yield("Thread.yield");
    $.ctx.nativeThread.advancePastInvokeBytecode();
  };

  Native["java/lang/Object.wait.(J)V"] = function(timeoutL: number, timeoutH: number) {
    $.ctx.wait(this, longToNumber(timeoutL, timeoutH));
    $.ctx.nativeThread.advancePastInvokeBytecode();
  };

  Native["java/lang/Object.notify.()V"] = function() {
    $.ctx.notify(this, false);
  };

  Native["java/lang/Object.notifyAll.()V"] = function() {
    $.ctx.notify(this, true);
  };

  Native["org/mozilla/internal/Sys.getUnwindCount.()I"] = function() {
    return unwindCount;
  };

  Native["org/mozilla/internal/Sys.constructCurrentThread.()V"] = function() {
    var methodInfo = CLASSES.java_lang_Thread.getMethodByNameString("<init>", "(Ljava/lang/String;)V");
    getLinkedMethod(methodInfo).call($.mainThread, J2ME.newString("main"));
  };

  Native["org/mozilla/internal/Sys.getIsolateMain.()Ljava/lang/String;"] = function(): java.lang.String {
    return $.isolate._mainClass;
  };

  Native["org/mozilla/internal/Sys.executeMain.(Ljava/lang/Class;)V"] = function(main: java.lang.Class) {
    var entryPoint = CLASSES.getEntryPoint(main.runtimeKlass.templateKlass.classInfo);
    if (!entryPoint)
      throw new Error("Could not find isolate main.");
    getLinkedMethod(entryPoint).call(null, $.isolate._mainArgs);
  };
}
