module J2ME {
  import assert = Debug.assert;
  import unique = ArrayUtilities.unique;
  import Bytecodes = Bytecode.Bytecodes;
  import BytecodeStream = Bytecode.BytecodeStream;

  var yieldWriter = null; // stderrWriter;
  export var yieldCounter = new Metrics.Counter(true);

  export enum YieldReason {
    None = 0,
    Root = 1,
    Synchronized = 2,
    MonitorEnterExit = 3,
    Virtual = 4,
    Cycle = 5
  }

  /**
   * Root set of methods that can yield. Keep this up to date or else the compiler will not generate yield code
   * at the right spots.
   */
  export var yieldMap = {
    "java/lang/Thread.sleep.(J)V": YieldReason.Root,
    "com/sun/cldc/isolate/Isolate.waitStatus.(I)V": YieldReason.Root,
    "com/sun/midp/links/LinkPortal.getLinkCount0.()I": YieldReason.Root,
    "com/sun/midp/links/Link.receive0.(Lcom/sun/midp/links/LinkMessage;Lcom/sun/midp/links/Link;)V": YieldReason.Root,
    "com/nokia/mid/impl/jms/core/Launcher.handleContent.(Ljava/lang/String;)V": YieldReason.Root,
    "com/sun/midp/util/isolate/InterIsolateMutex.lock0.(I)V": YieldReason.Root,
    "com/sun/midp/events/NativeEventMonitor.waitForNativeEvent.(Lcom/sun/midp/events/NativeEvent;)I": YieldReason.Root,
    "com/sun/midp/main/CommandState.exitInternal.(I)V": YieldReason.Root,
    "com/sun/midp/io/j2me/push/ConnectionRegistry.poll0.(J)I": YieldReason.Root,
    "com/sun/midp/rms/RecordStoreUtil.exists.(Ljava/lang/String;Ljava/lang/String;I)Z": YieldReason.Root,
    "com/sun/midp/rms/RecordStoreUtil.deleteFile.(Ljava/lang/String;Ljava/lang/String;I)V": YieldReason.Root,
    "com/sun/midp/rms/RecordStoreFile.openRecordStoreFile.(Ljava/lang/String;Ljava/lang/String;I)I": YieldReason.Root,
    "com/ibm/oti/connection/file/Connection.existsImpl.([B)Z": YieldReason.Root,
    "com/ibm/oti/connection/file/Connection.fileSizeImpl.([B)J": YieldReason.Root,
    "com/ibm/oti/connection/file/Connection.isDirectoryImpl.([B)Z": YieldReason.Root,
    "com/ibm/oti/connection/file/Connection.listImpl.([B[BZ)[[B": YieldReason.Root,
    "com/ibm/oti/connection/file/Connection.mkdirImpl.([B)I": YieldReason.Root,
    "com/ibm/oti/connection/file/Connection.newFileImpl.([B)I": YieldReason.Root,
    "com/ibm/oti/connection/file/Connection.deleteFileImpl.([B)Z": YieldReason.Root,
    "com/ibm/oti/connection/file/Connection.lastModifiedImpl.([B)J": YieldReason.Root,
    "com/ibm/oti/connection/file/Connection.renameImpl.([B[B)V": YieldReason.Root,
    "com/ibm/oti/connection/file/Connection.truncateImpl.([BJ)V": YieldReason.Root,
    "com/ibm/oti/connection/file/FCInputStream.openImpl.([B)I": YieldReason.Root,
    "com/ibm/oti/connection/file/FCOutputStream.openImpl.([B)I": YieldReason.Root,
    "com/ibm/oti/connection/file/FCOutputStream.openOffsetImpl.([BJ)I": YieldReason.Root,
    "com/sun/midp/io/j2me/storage/RandomAccessStream.open.(Ljava/lang/String;I)I": YieldReason.Root,
    "javax/microedition/lcdui/ImageDataFactory.createImmutableImageDecodeImage.(Ljavax/microedition/lcdui/ImageData;[BII)V": YieldReason.Root,
    "com/nokia/mid/ui/TextEditorThread.sleep.()V": YieldReason.Root,
    "com/nokia/mid/ui/VKVisibilityNotificationRunnable.sleepUntilVKVisibilityChange.()Z": YieldReason.Root,
    "org/mozilla/io/LocalMsgConnection.init.(Ljava/lang/String;)V": YieldReason.Root,
    "org/mozilla/io/LocalMsgConnection.receiveData.([B)I": YieldReason.Root,
    "com/sun/mmedia/PlayerImpl.nRealize.(ILjava/lang/String;)Z": YieldReason.Root,
    "com/sun/mmedia/DirectRecord.nPause.(I)I": YieldReason.Root,
    "com/sun/mmedia/DirectRecord.nStop.(I)I": YieldReason.Root,
    "com/sun/mmedia/DirectRecord.nClose.(I)I": YieldReason.Root,
    "com/sun/mmedia/DirectRecord.nStart.(I)I": YieldReason.Root,
    "com/sun/midp/io/j2me/socket/Protocol.open0.([BI)V": YieldReason.Root,
    "com/sun/midp/io/j2me/socket/Protocol.read0.([BII)I": YieldReason.Root,
    "com/sun/midp/io/j2me/socket/Protocol.write0.([BII)I": YieldReason.Root,
    "com/sun/midp/io/j2me/socket/Protocol.close0.()V": YieldReason.Root,
    "com/sun/midp/io/j2me/sms/Protocol.receive0.(IIILcom/sun/midp/io/j2me/sms/Protocol$SMSPacket;)I": YieldReason.Root,
    "com/sun/midp/io/j2me/sms/Protocol.send0.(IILjava/lang/String;II[B)I": YieldReason.Root,
    "com/sun/j2me/pim/PIMProxy.getNextItemDescription0.(I[I)Z": YieldReason.Root,
    "java/lang/Object.wait.(J)V": YieldReason.Root,
    "java/lang/Class.invoke_clinit.()V": YieldReason.Root,
    "java/lang/Class.newInstance.()Ljava/lang/Object;": YieldReason.Root,
    "java/lang/Thread.yield.()V": YieldReason.Root,
    "javax/microedition/lcdui/Graphics.drawString.(Ljava/lang/String;III)V": YieldReason.Root,
    "javax/microedition/lcdui/Graphics.drawSubstring.(Ljava/lang/String;IIIII)V": YieldReason.Root,
    "javax/microedition/lcdui/Graphics.drawChars.([CIIIII)V": YieldReason.Root,
    // Test Files:
    "gnu/testlet/vm/NativeTest.throwExceptionAfterPause.()V": YieldReason.Root,
    "gnu/testlet/vm/NativeTest.returnAfterPause.()I": YieldReason.Root,
    "gnu/testlet/vm/NativeTest.dumbPipe.()Z": YieldReason.Root,
    "gnu/testlet/TestHarness.getNumDifferingPixels.(Ljava/lang/String;)I": YieldReason.Root,
  };

  export function isFinal(classInfo: ClassInfo): boolean {
    if (classInfo.isFinal) {
      return true;
    }
    return false;
    // TODO: Be more clever here.
  }

  export function gatherCallees(callees: MethodInfo [], classInfo: ClassInfo, methodInfo: MethodInfo) {
    var methods = classInfo.methods;

    for (var i = 0; i < methods.length; i++) {
      var method = methods[i];
      if (method.name === methodInfo.name && method.signature === methodInfo.signature) {
        callees.push(method);
      }
    }
    var subClasses = classInfo.subClasses;
    for (var i = 0; i < subClasses.length; i++) {
      var subClass = subClasses[i];
      gatherCallees(callees, subClass, methodInfo);
    }
  }

  export function isStaticallyBound(op: Bytecodes, methodInfo: MethodInfo): boolean {
    // INVOKESPECIAL and INVOKESTATIC are always statically bound.
    if (op === Bytecodes.INVOKESPECIAL || op === Bytecodes.INVOKESTATIC) {
      return true;
    }
    // INVOKEVIRTUAL is only statically bound if its class is final.
    if (op === Bytecodes.INVOKEVIRTUAL && isFinal(methodInfo.classInfo)) {
      return true;
    }
    return false;
  }

  // Used to prevent cycles.
  var checkingForCanYield = Object.create(null);

  export function canYield(methodInfo: MethodInfo): YieldReason {
    yieldWriter && yieldWriter.writeLn("Calling: " + methodInfo.implKey);
    if (yieldMap[methodInfo.implKey] !== undefined) {
      return yieldMap[methodInfo.implKey];
    }
    if (methodInfo.isSynchronized) {
      return yieldMap[methodInfo.implKey] = YieldReason.Synchronized;
    }
    if (checkingForCanYield[methodInfo.implKey]) {
      return YieldReason.Cycle;
    }
    if (!methodInfo.code) {
      assert (methodInfo.isNative || methodInfo.isAbstract);
      return yieldMap[methodInfo.implKey] = YieldReason.None;
    }
    yieldWriter && yieldWriter.enter("> " + methodInfo.implKey);
    checkingForCanYield[methodInfo.implKey] = true;
    try {
      var result = YieldReason.None;
      var stream = new BytecodeStream(methodInfo.code);
      stream.setBCI(0);
      while (stream.currentBCI < methodInfo.code.length) {
        var op: Bytecodes = stream.currentBC();
        switch (op) {
          case Bytecodes.MONITORENTER:
          case Bytecodes.MONITOREXIT:
            result = YieldReason.MonitorEnterExit;
            break;
          case Bytecodes.INVOKEINTERFACE:
            result = YieldReason.Virtual;
            if (result) {
              yieldCounter.count("Method: " + methodInfo.implKey + " yields because it has an invoke interface.");
            }
            break;
          case Bytecodes.INVOKEVIRTUAL:
          case Bytecodes.INVOKESPECIAL:
          case Bytecodes.INVOKESTATIC:
            var cpi = stream.readCPI()
            var callee = methodInfo.classInfo.resolve(cpi, op === Bytecodes.INVOKESTATIC);
            if (!isStaticallyBound(op, callee)) {
              var callees = [];
              result = YieldReason.Virtual;
              if (false) { // Checking all possible callees, disabled for now until fully tested.
                result = YieldReason.None;
                gatherCallees(callees, callee.classInfo, callee);
                yieldWriter && yieldWriter.writeLn("Gather: " + callee.implKey + " " + callees.map(x => x.implKey).join(", "));
                for (var i = 0; i < callees.length; i++) {
                  if (canYield(callees[i])) {
                    yieldWriter && yieldWriter.writeLn("Gathered Method: " + callees[i].implKey + " yields.");
                    result = YieldReason.Virtual;
                    break;
                  }
                }
              }
              if (result !== YieldReason.None) {
                yieldCounter.count("Method: " + methodInfo.implKey + " yields because callee: " + callee.implKey + " is not statically bound.");
              }
              break;
            }
            result = canYield(callee);
            if (result) {
              yieldCounter.count("Callee: " + callee.implKey + " yields.");
              yieldCounter.count("Method: " + methodInfo.implKey + " yields because callee: " + callee.implKey + " can yield.");
            }
            break;
        }
        if (result) {
          break;
        }
        stream.next();
      }
    } catch (e) {
      stderrWriter.writeLn("ERROR: " + methodInfo.implKey + " Cycle");
      stderrWriter.writeLn(e);
      stderrWriter.writeLns(e.stack);
    }
    checkingForCanYield[methodInfo.implKey] = false;
    yieldWriter && yieldWriter.leave("< " + methodInfo.implKey + " " + YieldReason[result]);
    return yieldMap[methodInfo.implKey] = result;
  }
}