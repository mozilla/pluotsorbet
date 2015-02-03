module J2ME {
  import assert = Debug.assert;
  import unique = ArrayUtilities.unique;
  import Bytecodes = Bytecode.Bytecodes;
  import BytecodeStream = Bytecode.BytecodeStream;

  var yieldWriter = null; // stderrWriter;
  export var yieldCounter = null; // new Metrics.Counter(true);

  export var yieldGraph = null; // Object.create(null);

  export enum YieldReason {
    None = 0,
    Root = 1,
    Synchronized = 2,
    MonitorEnterExit = 3,
    Virtual = 4,
    Cycle = 5,
    Yield = 6,
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
    "com/sun/midp/io/j2me/push/ConnectionRegistry.poll0.(J)I": YieldReason.Root,
    "com/sun/midp/rms/RecordStoreUtil.exists.(Ljava/lang/String;Ljava/lang/String;I)Z": YieldReason.Root,
    "com/sun/midp/rms/RecordStoreFile.openRecordStoreFile.(Ljava/lang/String;Ljava/lang/String;I)I": YieldReason.Root,
    "com/sun/midp/rms/RecordStoreFile.commitWrite.(I)V": YieldReason.Root,
    "com/sun/midp/rms/RecordStoreFile.closeFile.(I)V": YieldReason.Root,
    "com/ibm/oti/connection/file/Connection.truncateImpl.([BJ)V": YieldReason.Root,
    "com/ibm/oti/connection/file/FCInputStream.openImpl.([B)I": YieldReason.Root,
    "com/ibm/oti/connection/file/FCOutputStream.openImpl.([B)I": YieldReason.Root,
    "com/ibm/oti/connection/file/FCOutputStream.openOffsetImpl.([BJ)I": YieldReason.Root,
    "com/sun/midp/io/j2me/storage/RandomAccessStream.open.(Ljava/lang/String;I)I": YieldReason.Root,
    "javax/microedition/lcdui/ImageDataFactory.createImmutableImageDecodeImage.(Ljavax/microedition/lcdui/ImageData;[BII)V": YieldReason.Root,
    "com/nokia/mid/ui/TextEditorThread.getNextDirtyEditor.()Lcom/nokia/mid/ui/TextEditor;": YieldReason.Root,
    "com/nokia/mid/ui/TextEditor.setFocus.(Z)V": YieldReason.Root,
    "com/nokia/mid/ui/VKVisibilityNotificationRunnable.sleepUntilVKVisibilityChange.()Z": YieldReason.Root,
    "com/nokia/mid/s40/bg/BGUtils.waitUserInteraction.()V": YieldReason.Root,
    "org/mozilla/io/LocalMsgConnection.init.(Ljava/lang/String;)V": YieldReason.Root,
    "org/mozilla/io/LocalMsgConnection.receiveData.([B)I": YieldReason.Root,
    "org/mozilla/io/LocalMsgConnection.waitConnection.()V": YieldReason.Root,
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
    "java/lang/Thread.start0.()V": YieldReason.Root,
    // Test Files:
    "gnu/testlet/vm/NativeTest.throwExceptionAfterPause.()V": YieldReason.Root,
    "gnu/testlet/vm/NativeTest.returnAfterPause.()I": YieldReason.Root,
    "gnu/testlet/vm/NativeTest.dumbPipe.()Z": YieldReason.Root,
    "gnu/testlet/TestHarness.getNumDifferingPixels.(Ljava/lang/String;)I": YieldReason.Root,


  };

  export var yieldVirtualMap = {
    // These can technically yield but are worth the risk.
    // XXX Determine the current status of this item.
    // "java/lang/Object.equals.(Ljava/lang/Object;)Z": YieldReason.None
  }

  export function isFinalClass(classInfo: ClassInfo): boolean {
    var result = classInfo.isFinal;
    if (!result) {
      result = classInfo.subClasses.length === 0;
    }
    // console.log(classInfo.className + " is final class " + result);
    return result;
  }

  export function isFinalMethod(methodInfo: MethodInfo): boolean {
    if (isFinalClass(methodInfo.classInfo)) {
      return true;
    }
    // XXX Determine whether we can start using the code in this function.
    return false;
    var result = methodInfo.isFinal;
    if (!result) {
      var classInfo = methodInfo.classInfo;
      var allSubClasses = classInfo.allSubClasses;
      result = true;
      for (var i = 0; i < allSubClasses.length; i++) {
        var subClassMethods = allSubClasses[i].methods;
        for (var j = 0; j < subClassMethods.length; j++) {
          var subClassMethodInfo = subClassMethods[j];
          if (methodInfo.name === subClassMethodInfo.name &&
              methodInfo.signature === subClassMethodInfo.signature) {
            result = false;
            break;
          }
        }
      }
    }
    return result;
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
    if (op === Bytecodes.INVOKEVIRTUAL && isFinalMethod(methodInfo)) {
      return true;
    }
    return false;
  }

  // Used to prevent cycles.
  var checkingForCanYield = Object.create(null);

  function addDependency(callee: MethodInfo, caller: MethodInfo, reason: YieldReason) {
    if (!yieldGraph) {
      return;
    }
    if (!yieldGraph[callee.implKey]) {
      yieldGraph[callee.implKey] = Object.create(null);
    }
    var node = yieldGraph[callee.implKey];
    node[caller.implKey] = reason;
  }

  function countDescendents(root) {
    var visited = Object.create(null);
    var visiting = Object.create(null);
    var w = new IndentingWriter();
    function visit(name) {
      if (!yieldGraph[name]) {
        return 0;
      }
      if (visiting[name]) {
        return 0;
      }
      var n = 0;
      visiting[name] = true;
      for (var k in yieldGraph[name]) {
        n ++;
        n += visit(k);
      }
      visiting[name] = false;
      return n;
    }
    return visit(root);
  }

  export function traceYieldGraph(writer: IndentingWriter) {
    writer.writeLn(JSON.stringify(yieldGraph, null, 2));
    var pairs = [];
    for (var k in yieldGraph) {
      pairs.push([k, countDescendents(k)]);
    }
    pairs.sort(function (a, b) {
      return b[1] - a[1];
    });
    for (var i = 0; i < pairs.length; i++) {
      var p = pairs[i];
      writer.writeLn(pairs[i][0] + ": " + pairs[i][1]);
    }

  }

  export function canYield(methodInfo: MethodInfo): YieldReason {
    yieldWriter && yieldWriter.enter("> " + methodInfo.implKey);
    if (yieldMap[methodInfo.implKey] !== undefined) {
      yieldWriter && yieldWriter.leave("< " + methodInfo.implKey + " " + YieldReason[yieldMap[methodInfo.implKey]] + " cached.");
      return yieldMap[methodInfo.implKey];
    }
    if (methodInfo.isSynchronized) {
      yieldCounter && yieldCounter.count("Method: " + methodInfo.implKey + " yields because it is synchronized.");
      yieldWriter && yieldWriter.leave("< " + methodInfo.implKey + " " + YieldReason[YieldReason.Synchronized]);
      return yieldMap[methodInfo.implKey] = YieldReason.Synchronized;
    }
    if (checkingForCanYield[methodInfo.implKey]) {
      yieldWriter && yieldWriter.leave("< " + methodInfo.implKey + " " + YieldReason[YieldReason.Cycle]);
      return YieldReason.Cycle;
    }
    if (!methodInfo.code) {
      assert (methodInfo.isNative || methodInfo.isAbstract);
      yieldWriter && yieldWriter.leave("< " + methodInfo.implKey + " Abstract");
      return yieldMap[methodInfo.implKey] = YieldReason.None;
    }

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
            yieldCounter && yieldCounter.count("Method: " + methodInfo.implKey + " yields because it has monitor enter/exit.");
            break;
          case Bytecodes.INVOKEINTERFACE:
            result = YieldReason.Virtual;
            if (result) {
              yieldCounter && yieldCounter.count("Method: " + methodInfo.implKey + " yields because it has an invoke interface.");
            }
            break;
          case Bytecodes.INVOKEVIRTUAL:
          case Bytecodes.RESOLVED_INVOKEVIRTUAL:
          case Bytecodes.INVOKESPECIAL:
          case Bytecodes.INVOKESTATIC:
            var cpi = stream.readCPI()
            var callee = methodInfo.classInfo.resolve(cpi, op === Bytecodes.INVOKESTATIC);

            if (op !== Bytecodes.INVOKESTATIC) {
              if (yieldVirtualMap[methodInfo.implKey] === YieldReason.None) {
                result = YieldReason.None;
                break;
              }
            }

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
                yieldCounter && yieldCounter.count("Method: " + methodInfo.implKey + " yields because callee: " + callee.implKey + " is not statically bound.");
                addDependency(callee, methodInfo, YieldReason.Virtual);
              }
              break;
            }
            result = canYield(callee);
            if (result) {
              yieldCounter && yieldCounter.count("Callee: " + callee.implKey + " yields.");
              yieldCounter && yieldCounter.count("Method: " + methodInfo.implKey + " yields because callee: " + callee.implKey + " can yield.");
              addDependency(callee, methodInfo, YieldReason.Yield);
            }
            break;
        }
        if (result) {
          break;
        }
        stream.next();
      }
    } catch (e) {
      result = YieldReason.Cycle;
      // stderrWriter.writeLn("ERROR: " + methodInfo.implKey + " Cycle");
      // stderrWriter.writeLn(e);
      // stderrWriter.writeLns(e.stack);
    }
    checkingForCanYield[methodInfo.implKey] = false;
    yieldWriter && yieldWriter.leave("< " + methodInfo.implKey + " " + YieldReason[result]);
    return yieldMap[methodInfo.implKey] = result;
  }
}