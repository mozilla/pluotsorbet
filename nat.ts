/**
 * Asm.js native module declaration, this is defined by lib/native.js
 */
declare var ASM;

var Native = Object.create(null);

/**
 * Asm.js heap buffer and views.
 */
var buffer = ASM.buffer;
var i8: Int8Array = ASM.HEAP8;
var u8: Uint8Array = ASM.HEAPU8;
var i16: Int16Array = ASM.HEAP16;
var u16: Uint16Array = ASM.HEAPU16;
var i32: Int32Array = ASM.HEAP32;
var u32: Uint32Array = ASM.HEAPU32;
var f32: Float32Array = ASM.HEAPF32;
var f64: Float64Array = ASM.HEAPF64;

var aliasedI32 = J2ME.IntegerUtilities.i32;
var aliasedF32 = J2ME.IntegerUtilities.f32;
var aliasedF64 = J2ME.IntegerUtilities.f64;

module J2ME {
  import assert = Debug.assert;
  import Bytecodes = Bytecode.Bytecodes;
  import toHEX = IntegerUtilities.toHEX;

  export function asyncImplOld(returnKind: string, promise: Promise<any>, cleanup?: Function) {
    return asyncImpl(kindCharacterToKind(returnKind), promise, cleanup);
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
  export function asyncImpl(returnKind: Kind, promise: Promise<any>, cleanup?: Function) {
    var ctx = $.ctx;

    promise.then(function onFulfilled(l: any, h?: number) {
      var thread = ctx.nativeThread;
      thread.pushPendingNativeFrames();

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
          release || assert(l !== "number", "async native return value is a number");
          i32[sp++] = l;
          break;
        case Kind.Void:
          break;
        default:
          release || J2ME.Debug.assert(false, "Invalid Kind: " + Kind[returnKind]);
      }
      thread.sp = sp;

      cleanup && cleanup();

      Scheduler.enqueue(ctx);
    }, function onRejected(exception: java.lang.Exception) {
      var thread = ctx.nativeThread;
      thread.pushPendingNativeFrames();
      var classInfo = CLASSES.getClass("org/mozilla/internal/Sys");
      var methodInfo = classInfo.getMethodByNameString("throwException", "(Ljava/lang/Exception;)V");

      thread.pushMarkerFrame(FrameType.Interrupt);
      thread.pushFrame(methodInfo);
      thread.frame.setParameter(J2ME.Kind.Reference, 0, exception._address);

      cleanup && cleanup();

      Scheduler.enqueue(ctx);
    });

    $.pause("Async");
    $.nativeBailout(returnKind);
  }

  Native["java/lang/Thread.sleep.(J)V"] = function(addr: number, delayL: number, delayH: number) {
    asyncImpl(Kind.Void, new Promise(function(resolve, reject) {
      window.setTimeout(resolve, longToNumber(delayL, delayH));
    }));
  };

  Native["java/lang/Thread.isAlive.()Z"] = function(addr: number) {
    var self = <java.lang.Thread>getHandle(addr);
    return self.nativeAlive ? 1 : 0;
  };

  Native["java/lang/Thread.yield.()V"] = function(addr: number) {
    $.yield("Thread.yield");
    $.nativeBailout(Kind.Void);
  };

  Native["java/lang/Object.wait.(J)V"] = function(addr: number, timeoutL: number, timeoutH: number) {
    $.ctx.wait(addr, longToNumber(timeoutL, timeoutH));
    if (U) {
      $.nativeBailout(Kind.Void);
    }
  };

  Native["java/lang/Object.notify.()V"] = function(addr: number) {
    $.ctx.notify(addr, false);
    // TODO Remove this assertion after investigating why wakeup on another ctx can unwind see comment in Context.notify..
    release || assert(!U, "Unexpected unwind in java/lang/Object.notify.()V.");
  };

  Native["java/lang/Object.notifyAll.()V"] = function(addr: number) {
    $.ctx.notify(addr, true);
    // TODO Remove this assertion after investigating why wakeup on another ctx can unwind see comment in Context.notify.
    release || assert(!U, "Unexpected unwind in java/lang/Object.notifyAll.()V.");
  };

  Native["java/lang/ref/WeakReference.initializeWeakReference.(Ljava/lang/Object;)V"] = function(addr: number, targetAddr: number): void {
      if (targetAddr === J2ME.Constants.NULL) {
        return;
      }

      var weakRef = (<java.lang.ref.WeakReference>getHandle(addr));
      weakRef.holder = gcMallocAtomic(4);
      i32[weakRef.holder >> 2] = targetAddr;
      ASM._gcRegisterDisappearingLink(weakRef.holder, targetAddr);
  };

  Native["java/lang/ref/WeakReference.get.()Ljava/lang/Object;"] = function(addr: number): number {
    var weakRef = (<java.lang.ref.WeakReference>getHandle(addr));
    if (weakRef.holder === J2ME.Constants.NULL) {
      return J2ME.Constants.NULL;
    }
    return i32[weakRef.holder >> 2];
  };

  Native["java/lang/ref/WeakReference.clear.()V"] = function(addr: number): void {
    var weakRef = (<java.lang.ref.WeakReference>getHandle(addr));
    ASM._gcUnregisterDisappearingLink(weakRef.holder);
    weakRef.holder = J2ME.Constants.NULL;
  };

  Native["org/mozilla/internal/Sys.getUnwindCount.()I"] = function(addr: number) {
    return unwindCount;
  };

  Native["org/mozilla/internal/Sys.constructCurrentThread.()V"] = function(addr: number) {
    var methodInfo = CLASSES.java_lang_Thread.getMethodByNameString("<init>", "(Ljava/lang/String;)V");
    getLinkedMethod(methodInfo)($.mainThread, J2ME.newString("main"));
    if (U) {
      $.nativeBailout(J2ME.Kind.Void, J2ME.Bytecode.Bytecodes.INVOKESPECIAL);
    }

    // We've already set this in JVM.createIsolateCtx, but calling the instance
    // initializer above resets it, so we set it again here.
    //
    // We used to store this state on the persistent native object, which was
    // unaffected by the instance initializer; but now we store it on the Java
    // object, which is susceptible to it, since there is no persistent native
    // object anymore).
    //
    // XXX Figure out a less hacky approach.
    //
    var thread = <java.lang.Thread>getHandle($.mainThread);
    thread.nativeAlive = true;
  };

  Native["org/mozilla/internal/Sys.getIsolateMain.()Ljava/lang/String;"] = function(addr: number): number {
    var isolate = <com.sun.cldc.isolate.Isolate>getHandle($.isolateAddress);
    return isolate._mainClass;
  };

  Native["org/mozilla/internal/Sys.executeMain.(Ljava/lang/Class;)V"] = function(addr: number, mainAddr: number) {
    var main = <java.lang.Class>getHandle(mainAddr);
    var entryPoint = CLASSES.getEntryPoint(J2ME.classIdToClassInfoMap[main.vmClass]);
    if (!entryPoint)
      throw new Error("Could not find isolate main.");

    var isolate = <com.sun.cldc.isolate.Isolate>getHandle($.isolateAddress);

    getLinkedMethod(entryPoint)(Constants.NULL, isolate._mainArgs);
    if (U) {
      $.nativeBailout(J2ME.Kind.Void, J2ME.Bytecode.Bytecodes.INVOKESTATIC);
    }
  };

  Native["java/lang/Throwable.fillInStackTrace.()V"] = function(addr: number) {
    var frame = $.ctx.nativeThread.frame;
    var tp = $.ctx.nativeThread.tp;
    var fp = frame.fp;
    var sp = frame.sp;
    var pc = frame.pc;
    var stackTrace = [];
    setNative(addr, stackTrace);
    while (true) {
      release || assert(fp >= (tp >> 2), "Invalid frame pointer.");
      if (frame.fp === (tp >> 2)) {
        break;
      }
      stackTrace.push({
        frameType: frame.type,
        methodInfo: frame.methodInfo,
        pc: frame.pc
      });

      frame.set(frame.thread, i32[frame.fp + FrameLayout.CallerFPOffset],
        frame.fp + frame.parameterOffset,
        i32[frame.fp + FrameLayout.CallerRAOffset]);
    }
    frame.fp = fp;
    frame.sp = sp;
    frame.pc = pc;
  };

  Native["java/lang/Throwable.obtainBackTrace.()Ljava/lang/Object;"] = function(addr: number): number {
    var resultAddr = J2ME.Constants.NULL;
    var stackTrace = <[any]>NativeMap.get(addr);
    if (stackTrace) {
      var depth = stackTrace.length;
      var classNamesAddr = J2ME.newStringArray(depth);
      var classNames = J2ME.getArrayFromAddr(classNamesAddr);
      var methodNamesAddr = J2ME.newStringArray(depth);
      var methodNames = J2ME.getArrayFromAddr(methodNamesAddr);
      var methodSignaturesAddr = J2ME.newStringArray(depth);
      var methodSignatures = J2ME.getArrayFromAddr(methodSignaturesAddr);
      var offsetsAddr = J2ME.newIntArray(depth);
      var offsets = J2ME.getArrayFromAddr(offsetsAddr);
      stackTrace.forEach(function(e, n) {
        if (e.frameType === FrameType.Interpreter) {
          var methodInfo = <MethodInfo>e.methodInfo;
          classNames[n] = J2ME.newString(methodInfo.classInfo.getClassNameSlow());
          methodNames[n] = J2ME.newString(methodInfo.name);
          methodSignatures[n] = J2ME.newString(methodInfo.signature);
          offsets[n] = e.pc;
        } else {
          classNames[n] = J2ME.newString("MARKER FRAME " + FrameType[e.frameType]);
          methodNames[n] = J2ME.newString("");
          methodSignatures[n] = J2ME.newString("");
          offsets[n] = e.pc;
        }
      });
      resultAddr = J2ME.newObjectArray(4);
      var result = J2ME.getArrayFromAddr(resultAddr);
      result[0] = classNamesAddr;
      result[1] = methodNamesAddr;
      result[2] = methodSignaturesAddr;
      result[3] = offsetsAddr;
    }
    return resultAddr;
  };

  Native["java/lang/Runtime.totalMemory.()J"] = function(addr) {
    return J2ME.returnLongValue(asmJsTotalMemory);
  };
}
