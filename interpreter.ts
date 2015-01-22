module J2ME {
  declare var util;
  declare var Instrument;
  declare var Promise;

  import BytecodeStream = Bytecode.BytecodeStream;
  import BlockMap = Bytecode.BlockMap;
  import checkArrayBounds = J2ME.checkArrayBounds;
  import checkDivideByZero = J2ME.checkDivideByZero;
  import checkDivideByZeroLong = J2ME.checkDivideByZeroLong;

  import Bytecodes = Bytecode.Bytecodes;
  import assert = Debug.assert;
  import popManyInto = ArrayUtilities.popManyInto;

  export var interpreterCounter = null; // new Metrics.Counter(true);
  export var interpreterMethodCounter = new Metrics.Counter(true);

  var traceArrayAccess = false;

  function traceArrayStore(index: number, array: any [], value: any) {
    traceWriter.writeLn(toDebugString(array) + "[" + index + "] = " + toDebugString(value));
  }

  function traceArrayLoad(index: number, array: any []) {
    assert(array[index] !== undefined);
    traceWriter.writeLn(toDebugString(array) + "[" + index + "] (" + toDebugString(array[index]) + ")");
  }

  /**
   * Optimize method bytecode.
   */
  function optimizeMethodBytecode(methodInfo: MethodInfo) {
    interpreterCounter && interpreterCounter.count("optimize: " + methodInfo.implKey);
    var stream = new BytecodeStream(methodInfo.code);
    while (stream.currentBC() !== Bytecodes.END) {
      if (stream.rawCurrentBC() === Bytecodes.WIDE) {
        stream.next();
        continue;
      }
      switch (stream.currentBC()) {
        case Bytecodes.ALOAD:
          if (stream.nextBC() === Bytecodes.ILOAD) {
            stream.writeCurrentBC(Bytecodes.ALOAD_ILOAD);
          }
          break;
        case Bytecodes.IINC:
          if (stream.nextBC() === Bytecodes.GOTO) {
           stream.writeCurrentBC(Bytecodes.IINC_GOTO);
          }
          break;
        case Bytecodes.ARRAYLENGTH:
          if (stream.nextBC() === Bytecodes.IF_ICMPGE) {
            stream.writeCurrentBC(Bytecodes.ARRAYLENGTH_IF_ICMPGE);
          }
          break;
      }
      stream.next();
    }
    methodInfo.isOptimized = true;
  }

  function resolve(index: number, classInfo: ClassInfo, isStatic: boolean = false): any {
    return classInfo.resolve(index, isStatic);
  }

  function resolveField(index: number, classInfo: ClassInfo, isStatic: boolean): FieldInfo {
    return <FieldInfo><any>classInfo.resolve(index, isStatic);
  }

  function resolveClass(index: number, classInfo: ClassInfo, isStatic: boolean): ClassInfo {
    var classInfo: ClassInfo = <any>classInfo.resolve(index, isStatic);
    linkKlass(classInfo);
    return classInfo;
  }

  function resolveMethod(index: number, classInfo: ClassInfo, isStatic: boolean): MethodInfo {
    return <MethodInfo><any>classInfo.resolve(index, isStatic);
  }

  /**
   * Debugging helper to make sure native methods were implemented correctly.
   */
  function checkReturnValue(methodInfo: MethodInfo, returnValue: any) {
    if (returnValue instanceof Promise) {
      console.error("You forgot to call asyncImpl():", methodInfo.implKey);
    } else if (methodInfo.getReturnKind() === Kind.Void && returnValue) {
      console.error("You returned something in a void method:", methodInfo.implKey);
    } else if (methodInfo.getReturnKind() !== Kind.Void && (returnValue === undefined) &&
      !U) {
      console.error("You returned undefined in a non-void method:", methodInfo.implKey);
    } else if (typeof returnValue === "string") {
      console.error("You returned a non-wrapped string:", methodInfo.implKey);
    } else if (returnValue === true || returnValue === false) {
      console.error("You returned a JS boolean:", methodInfo.implKey);
    }
  }

  /**
   * The number of opcodes executed thus far.
   */
  export var bytecodeCount = 0;

  /**
   * The number of times the interpreter method was called thus far.
   */
  export var interpreterCount = 0;

  export var onStackReplacementCount = 0;

  export var onStackReplacementIsEnabled = true;

  /**
   * Temporarily used for fn.apply.
   */
  var argArray = [];

  function buildExceptionLog(ex, stackTrace) {
    var className = ex.klass.classInfo.className;
    var detailMessage = util.fromJavaString(CLASSES.getField(ex.klass.classInfo, "I.detailMessage.Ljava/lang/String;").get(ex));
    return className + ": " + (detailMessage || "") + "\n" + stackTrace.map(function(entry) {
      return " - " + entry.className + "." + entry.methodName + "(), pc=" + entry.offset;
    }).join("\n") + "\n\n";
  }

  function tryCatch(e) {
    var ctx = $.ctx;
    var frame = ctx.current();
    var frames = ctx.frames;
    var stack = frame.stack;

    var exClass = e.class;
    if (!e.stackTrace) {
      e.stackTrace = [];
    }

    var stackTrace = e.stackTrace;

    do {
      var exception_table = frame.methodInfo.exception_table;
      var handler_pc = null;
      for (var i = 0; exception_table && i < exception_table.length; i++) {
        if (frame.opPC >= exception_table[i].start_pc && frame.opPC < exception_table[i].end_pc) {
          if (exception_table[i].catch_type === 0) {
            handler_pc = exception_table[i].handler_pc;
            break;
          } else {
            classInfo = resolveClass(exception_table[i].catch_type, frame.methodInfo.classInfo, false);
            if (isAssignableTo(e.klass, classInfo.klass)) {
              handler_pc = exception_table[i].handler_pc;
              break;
            }
          }
        }
      }

      var classInfo = frame.methodInfo.classInfo;
      if (classInfo && classInfo.className) {
        stackTrace.push({
          className: classInfo.className,
          methodName: frame.methodInfo.name,
          offset: frame.pc
        });
      }

      if (handler_pc != null) {
        stack.length = 0;
        stack.push(e);
        frame.pc = handler_pc;

        if (VM.DEBUG_PRINT_ALL_EXCEPTIONS) {
          console.error(buildExceptionLog(e, stackTrace));
        }

        return;
      }
      frames.pop();
      frame = frames[frames.length - 1];
      if (Frame.isMarker(frame)) {
        break;
      }
      stack = frame.stack;
    } while (true);

    if (ctx.current() === Frame.Start) {
      ctx.kill();
      if (ctx.thread && ctx.thread.waiting && ctx.thread.waiting.length > 0) {
        console.error(buildExceptionLog(e, stackTrace));
        ctx.thread.waiting.forEach(function(waitingCtx, n) {
          ctx.thread.waiting[n] = null;
          waitingCtx.wakeup(ctx.thread);
        });
      }
      throw new Error(buildExceptionLog(e, stackTrace));
    } else {
      throw e;
    }
  }

  export function interpret() {
    var ctx = $.ctx;

    // These must always be kept up to date with the current frame.
    var frame = ctx.current();
    release || assert (!Frame.isMarker(frame));
    var frames = ctx.frames;
    var mi = frame.methodInfo;
    var ci = mi.classInfo;
    var rp = ci.resolved_constant_pool;
    var stack = frame.stack;


    var returnValue = null;


    var traceBytecodes = false;
    var traceSourceLocation = true;

    var index: any, value: any, constant: any;
    var a: any, b: any, c: any;
    var pc: number;

    /**
     * This is used to detect backwards branches for the purpose of on stack replacement.
     */
    var lastPC: number = -1;

    var type;
    var size;

    var array: any;
    var object: java.lang.Object;
    var fieldInfo: FieldInfo;
    var classInfo: ClassInfo;

    // We don't want to optimize methods for interpretation if we're going to be using the JIT until
    // we teach the Baseline JIT about the new bytecodes.
    if (!enableRuntimeCompilation && !frame.methodInfo.isOptimized && frame.methodInfo.bytecodeCount > 100) {
      optimizeMethodBytecode(frame.methodInfo);
    }

    mi.interpreterCallCount ++;

    profile && interpreterCount ++;

    while (true) {
      profile && bytecodeCount ++;
      mi.bytecodeCount ++;

      // TODO: Make sure this works even if we JIT everything. At the moment it fails
      // for synthetic method frames which have bad max_local counts.

      // Inline heuristics that trigger JIT compilation here.
      if (enableRuntimeCompilation &&
          mi.state < MethodState.Compiled && // Give up if we're at this state.
          mi.backwardsBranchCount + mi.interpreterCallCount > 10) {
        compileAndLinkMethod(mi);
      }

      try {
        if (frame.pc < lastPC) {
          mi.backwardsBranchCount ++;
          if (onStackReplacementIsEnabled && mi.state === MethodState.Compiled) {
            // Just because we've jumped backwards doesn't mean we are at a loop header but it does mean that we are
            // at the beggining of a basic block. This is a really cheap test and a convenient place to perform an
            // on stack replacement.

            profile && onStackReplacementCount ++;

            // The current frame will be swapped out for a JIT frame, so pop it off the interpreter stack.
            frames.pop();

            // Remember the return kind since we'll need it later.
            var returnKind = mi.getReturnKind();

            // Set the global OSR frame to the current frame.
            O = frame;

            var blockMap = O.methodInfo.blockMap;
            if (!blockMap) {
              blockMap = new BlockMap(O.methodInfo);
              blockMap.build();
              O.methodInfo.blockMap = blockMap;
            }

            // Convert between PC and block ID which is what the baseline JIT expects as the PC.
            O.pc = blockMap.getBlock(O.pc).blockID;

            // Set the current frame before doing the OSR in case an exception is thrown.
            frame = frames[frames.length - 1];

            // Perform OSR, the callee reads the frame stored in |O| and updates its own state.
            returnValue = O.methodInfo.fn();
            if (U) {
              return;
            }

            // Usual code to return from the interpreter or push the return value.
            if (Frame.isMarker(frame)) {
              return returnValue;
            }
            mi = frame.methodInfo;
            ci = mi.classInfo;
            rp = ci.resolved_constant_pool;
            stack = frame.stack;
            lastPC = -1;

            if (returnKind !== Kind.Void) {
              if (isTwoSlot(returnKind)) {
                stack.push2(returnValue);
              } else {
                stack.push(returnValue);
              }
            }
          }
        }

        lastPC = frame.opPC = frame.pc;
        var op: Bytecodes = frame.read8();

        switch (op) {
          case Bytecodes.NOP:
            break;
          case Bytecodes.ACONST_NULL:
            stack.push(null);
            break;
          case Bytecodes.ICONST_M1:
          case Bytecodes.ICONST_0:
          case Bytecodes.ICONST_1:
          case Bytecodes.ICONST_2:
          case Bytecodes.ICONST_3:
          case Bytecodes.ICONST_4:
          case Bytecodes.ICONST_5:
            stack.push(op - Bytecodes.ICONST_0);
            break;
          case Bytecodes.FCONST_0:
          case Bytecodes.FCONST_1:
          case Bytecodes.FCONST_2:
            stack.push(op - Bytecodes.FCONST_0);
            break;
          case Bytecodes.DCONST_0:
          case Bytecodes.DCONST_1:
            stack.push2(op - Bytecodes.DCONST_0);
            break;
          case Bytecodes.LCONST_0:
          case Bytecodes.LCONST_1:
            stack.push2(Long.fromInt(op - Bytecodes.LCONST_0));
            break;
          case Bytecodes.BIPUSH:
            stack.push(frame.read8Signed());
            break;
          case Bytecodes.SIPUSH:
            stack.push(frame.read16Signed());
            break;
          case Bytecodes.LDC:
          case Bytecodes.LDC_W:
            index = (op === Bytecodes.LDC) ? frame.read8() : frame.read16();
            constant = resolve(index, ci, false);
            stack.push(constant);
            break;
          case Bytecodes.LDC2_W:
            index = frame.read16();
            constant = resolve(index, ci, false);
            stack.push2(constant);
            break;
          case Bytecodes.ILOAD:
            stack.push(frame.getLocal(frame.read8()));
            break;
          case Bytecodes.FLOAD:
            stack.push(frame.getLocal(frame.read8()));
            break;
          case Bytecodes.ALOAD:
            stack.push(frame.getLocal(frame.read8()));
            break;
          case Bytecodes.ALOAD_ILOAD:
            stack.push(frame.getLocal(frame.read8()));
            frame.pc ++;
            stack.push(frame.getLocal(frame.read8()));
            break;
          case Bytecodes.LLOAD:
          case Bytecodes.DLOAD:
            stack.push2(frame.getLocal(frame.read8()));
            break;
          case Bytecodes.ILOAD_0:
          case Bytecodes.ILOAD_1:
          case Bytecodes.ILOAD_2:
          case Bytecodes.ILOAD_3:
            stack.push(frame.getLocal(op - Bytecodes.ILOAD_0));
            break;
          case Bytecodes.FLOAD_0:
          case Bytecodes.FLOAD_1:
          case Bytecodes.FLOAD_2:
          case Bytecodes.FLOAD_3:
            stack.push(frame.getLocal(op - Bytecodes.FLOAD_0));
            break;
          case Bytecodes.ALOAD_0:
          case Bytecodes.ALOAD_1:
          case Bytecodes.ALOAD_2:
          case Bytecodes.ALOAD_3:
            stack.push(frame.getLocal(op - Bytecodes.ALOAD_0));
            break;
          case Bytecodes.LLOAD_0:
          case Bytecodes.LLOAD_1:
          case Bytecodes.LLOAD_2:
          case Bytecodes.LLOAD_3:
            stack.push2(frame.getLocal(op - Bytecodes.LLOAD_0));
            break;
          case Bytecodes.DLOAD_0:
          case Bytecodes.DLOAD_1:
          case Bytecodes.DLOAD_2:
          case Bytecodes.DLOAD_3:
            stack.push2(frame.getLocal(op - Bytecodes.DLOAD_0));
            break;
          case Bytecodes.IALOAD:
          case Bytecodes.FALOAD:
          case Bytecodes.AALOAD:
          case Bytecodes.BALOAD:
          case Bytecodes.CALOAD:
          case Bytecodes.SALOAD:
            index = stack.pop();
            array = stack.pop();
            checkArrayBounds(array, index);
            stack.push(array[index]);
            break;
          case Bytecodes.LALOAD:
          case Bytecodes.DALOAD:
            index = stack.pop();
            array = stack.pop();
            checkArrayBounds(array, index);
            stack.push2(array[index]);
            break;
          case Bytecodes.ISTORE:
          case Bytecodes.FSTORE:
          case Bytecodes.ASTORE:
            frame.setLocal(frame.read8(), stack.pop());
            break;
          case Bytecodes.LSTORE:
          case Bytecodes.DSTORE:
            frame.setLocal(frame.read8(), stack.pop2());
            break;
          case Bytecodes.ISTORE_0:
          case Bytecodes.FSTORE_0:
          case Bytecodes.ASTORE_0:
            frame.setLocal(0, stack.pop());
            break;
          case Bytecodes.ISTORE_1:
          case Bytecodes.FSTORE_1:
          case Bytecodes.ASTORE_1:
            frame.setLocal(1, stack.pop());
            break;
          case Bytecodes.ISTORE_2:
          case Bytecodes.FSTORE_2:
          case Bytecodes.ASTORE_2:
            frame.setLocal(2, stack.pop());
            break;
          case Bytecodes.ISTORE_3:
          case Bytecodes.FSTORE_3:
          case Bytecodes.ASTORE_3:
            frame.setLocal(3, stack.pop());
            break;
          case Bytecodes.LSTORE_0:
          case Bytecodes.DSTORE_0:
            frame.setLocal(0, stack.pop2());
            break;
          case Bytecodes.LSTORE_1:
          case Bytecodes.DSTORE_1:
            frame.setLocal(1, stack.pop2());
            break;
          case Bytecodes.LSTORE_2:
          case Bytecodes.DSTORE_2:
            frame.setLocal(2, stack.pop2());
            break;
          case Bytecodes.LSTORE_3:
          case Bytecodes.DSTORE_3:
            frame.setLocal(3, stack.pop2());
            break;
          case Bytecodes.IASTORE:
          case Bytecodes.FASTORE:
          case Bytecodes.BASTORE:
          case Bytecodes.CASTORE:
          case Bytecodes.SASTORE:
            value = stack.pop();
            index = stack.pop();
            array = stack.pop();
            checkArrayBounds(array, index);
            array[index] = value;
            break;
          case Bytecodes.LASTORE:
          case Bytecodes.DASTORE:
            value = stack.pop2();
            index = stack.pop();
            array = stack.pop();
            checkArrayBounds(array, index);
            array[index] = value;
            break;
          case Bytecodes.AASTORE:
            value = stack.pop();
            index = stack.pop();
            array = stack.pop();
            checkArrayBounds(array, index);
            checkArrayStore(array, value);
            array[index] = value;
            break;
          case Bytecodes.POP:
            stack.pop();
            break;
          case Bytecodes.POP2:
            stack.pop2();
            break;
          case Bytecodes.DUP:
            stack.push(stack[stack.length - 1]);
            break;
          case Bytecodes.DUP_X1:
            a = stack.pop();
            b = stack.pop();
            stack.push(a);
            stack.push(b);
            stack.push(a);
            break;
          case Bytecodes.DUP_X2:
            a = stack.pop();
            b = stack.pop();
            c = stack.pop();
            stack.push(a);
            stack.push(c);
            stack.push(b);
            stack.push(a);
            break;
          case Bytecodes.DUP2:
            a = stack.pop();
            b = stack.pop();
            stack.push(b);
            stack.push(a);
            stack.push(b);
            stack.push(a);
            break;
          case Bytecodes.DUP2_X1:
            a = stack.pop();
            b = stack.pop();
            c = stack.pop();
            stack.push(b);
            stack.push(a);
            stack.push(c);
            stack.push(b);
            stack.push(a);
            break;
          case Bytecodes.DUP2_X2:
            a = stack.pop();
            b = stack.pop();
            c = stack.pop();
            var d = stack.pop();
            stack.push(b);
            stack.push(a);
            stack.push(d);
            stack.push(c);
            stack.push(b);
            stack.push(a);
            break;
          case Bytecodes.SWAP:
            a = stack.pop();
            b = stack.pop();
            stack.push(a);
            stack.push(b);
            break;
          case Bytecodes.IINC:
            index = frame.read8();
            value = frame.read8Signed();
            frame.incLocal(index, value);
            break;
          case Bytecodes.IINC_GOTO:
            index = frame.read8();
            value = frame.read8Signed();
            frame.setLocal(index, frame.getLocal(index) + value);
            frame.pc ++;
            frame.pc = frame.readTargetPC();
            break;
          case Bytecodes.IADD:
            stack.push((stack.pop() + stack.pop()) | 0);
            break;
          case Bytecodes.LADD:
            stack.push2(stack.pop2().add(stack.pop2()));
            break;
          case Bytecodes.FADD:
            stack.push(Math.fround(stack.pop() + stack.pop()));
            break;
          case Bytecodes.DADD:
            stack.push2(stack.pop2() + stack.pop2());
            break;
          case Bytecodes.ISUB:
            stack.push((-stack.pop() + stack.pop()) | 0);
            break;
          case Bytecodes.LSUB:
            stack.push2(stack.pop2().negate().add(stack.pop2()));
            break;
          case Bytecodes.FSUB:
            stack.push(Math.fround(-stack.pop() + stack.pop()));
            break;
          case Bytecodes.DSUB:
            stack.push2(-stack.pop2() + stack.pop2());
            break;
          case Bytecodes.IMUL:
            stack.push(Math.imul(stack.pop(), stack.pop()));
            break;
          case Bytecodes.LMUL:
            stack.push2(stack.pop2().multiply(stack.pop2()));
            break;
          case Bytecodes.FMUL:
            stack.push(Math.fround(stack.pop() * stack.pop()));
            break;
          case Bytecodes.DMUL:
            stack.push2(stack.pop2() * stack.pop2());
            break;
          case Bytecodes.IDIV:
            b = stack.pop();
            a = stack.pop();
            checkDivideByZero(b);
            stack.push((a === Constants.INT_MIN && b === -1) ? a : ((a / b) | 0));
            break;
          case Bytecodes.LDIV:
            b = stack.pop2();
            a = stack.pop2();
            checkDivideByZeroLong(b);
            stack.push2(a.div(b));
            break;
          case Bytecodes.FDIV:
            b = stack.pop();
            a = stack.pop();
            stack.push(Math.fround(a / b));
            break;
          case Bytecodes.DDIV:
            b = stack.pop2();
            a = stack.pop2();
            stack.push2(a / b);
            break;
          case Bytecodes.IREM:
            b = stack.pop();
            a = stack.pop();
            checkDivideByZero(b);
            stack.push(a % b);
            break;
          case Bytecodes.LREM:
            b = stack.pop2();
            a = stack.pop2();
            checkDivideByZeroLong(b);
            stack.push2(a.modulo(b));
            break;
          case Bytecodes.FREM:
            b = stack.pop();
            a = stack.pop();
            stack.push(Math.fround(a % b));
            break;
          case Bytecodes.DREM:
            b = stack.pop2();
            a = stack.pop2();
            stack.push2(a % b);
            break;
          case Bytecodes.INEG:
            stack.push((-stack.pop()) | 0);
            break;
          case Bytecodes.LNEG:
            stack.push2(stack.pop2().negate());
            break;
          case Bytecodes.FNEG:
            stack.push(-stack.pop());
            break;
          case Bytecodes.DNEG:
            stack.push2(-stack.pop2());
            break;
          case Bytecodes.ISHL:
            b = stack.pop();
            a = stack.pop();
            stack.push(a << b);
            break;
          case Bytecodes.LSHL:
            b = stack.pop();
            a = stack.pop2();
            stack.push2(a.shiftLeft(b));
            break;
          case Bytecodes.ISHR:
            b = stack.pop();
            a = stack.pop();
            stack.push(a >> b);
            break;
          case Bytecodes.LSHR:
            b = stack.pop();
            a = stack.pop2();
            stack.push2(a.shiftRight(b));
            break;
          case Bytecodes.IUSHR:
            b = stack.pop();
            a = stack.pop();
            stack.push(a >>> b);
            break;
          case Bytecodes.LUSHR:
            b = stack.pop();
            a = stack.pop2();
            stack.push2(a.shiftRightUnsigned(b));
            break;
          case Bytecodes.IAND:
            stack.push(stack.pop() & stack.pop());
            break;
          case Bytecodes.LAND:
            stack.push2(stack.pop2().and(stack.pop2()));
            break;
          case Bytecodes.IOR:
            stack.push(stack.pop() | stack.pop());
            break;
          case Bytecodes.LOR:
            stack.push2(stack.pop2().or(stack.pop2()));
            break;
          case Bytecodes.IXOR:
            stack.push(stack.pop() ^ stack.pop());
            break;
          case Bytecodes.LXOR:
            stack.push2(stack.pop2().xor(stack.pop2()));
            break;
          case Bytecodes.LCMP:
            b = stack.pop2();
            a = stack.pop2();
            if (a.greaterThan(b)) {
              stack.push(1);
            } else if (a.lessThan(b)) {
              stack.push(-1);
            } else {
              stack.push(0);
            }
            break;
          case Bytecodes.FCMPL:
            b = stack.pop();
            a = stack.pop();
            if (isNaN(a) || isNaN(b)) {
              stack.push(-1);
            } else if (a > b) {
              stack.push(1);
            } else if (a < b) {
              stack.push(-1);
            } else {
              stack.push(0);
            }
            break;
          case Bytecodes.FCMPG:
            b = stack.pop();
            a = stack.pop();
            if (isNaN(a) || isNaN(b)) {
              stack.push(1);
            } else if (a > b) {
              stack.push(1);
            } else if (a < b) {
              stack.push(-1);
            } else {
              stack.push(0);
            }
            break;
          case Bytecodes.DCMPL:
            b = stack.pop2();
            a = stack.pop2();
            if (isNaN(a) || isNaN(b)) {
              stack.push(-1);
            } else if (a > b) {
              stack.push(1);
            } else if (a < b) {
              stack.push(-1);
            } else {
              stack.push(0);
            }
            break;
          case Bytecodes.DCMPG:
            b = stack.pop2();
            a = stack.pop2();
            if (isNaN(a) || isNaN(b)) {
              stack.push(1);
            } else if (a > b) {
              stack.push(1);
            } else if (a < b) {
              stack.push(-1);
            } else {
              stack.push(0);
            }
            break;
          case Bytecodes.IFEQ:
            pc = frame.readTargetPC();
            if (stack.pop() === 0) {
              frame.pc = pc;
            }
            break;
          case Bytecodes.IFNE:
            pc = frame.readTargetPC();
            if (stack.pop() !== 0) {
              frame.pc = pc;
            }
            break;
          case Bytecodes.IFLT:
            pc = frame.readTargetPC();
            if (stack.pop() < 0) {
              frame.pc = pc;
            }
            break;
          case Bytecodes.IFGE:
            pc = frame.readTargetPC();
            if (stack.pop() >= 0) {
              frame.pc = pc;
            }
            break;
          case Bytecodes.IFGT:
            pc = frame.readTargetPC();
            if (stack.pop() > 0) {
              frame.pc = pc;
            }
            break;
          case Bytecodes.IFLE:
            pc = frame.readTargetPC();
            if (stack.pop() <= 0) {
              frame.pc = pc;
            }
            break;
          case Bytecodes.IF_ICMPEQ:
            pc = frame.readTargetPC();
            if (stack.pop() === stack.pop()) {
              frame.pc = pc;
            }
            break;
          case Bytecodes.IF_ICMPNE:
            pc = frame.readTargetPC();
            if (stack.pop() !== stack.pop()) {
              frame.pc = pc;
            }
            break;
          case Bytecodes.IF_ICMPLT:
            pc = frame.readTargetPC();
            if (stack.pop() > stack.pop()) {
              frame.pc = pc;
            }
            break;
          case Bytecodes.IF_ICMPGE:
            pc = frame.readTargetPC();
            if (stack.pop() <= stack.pop()) {
              frame.pc = pc;
            }
            break;
          case Bytecodes.IF_ICMPGT:
            pc = frame.readTargetPC();
            if (stack.pop() < stack.pop()) {
              frame.pc = pc;
            }
            break;
          case Bytecodes.IF_ICMPLE:
            pc = frame.readTargetPC();
            if (stack.pop() >= stack.pop()) {
              frame.pc = pc;
            }
            break;
          case Bytecodes.IF_ACMPEQ:
            pc = frame.readTargetPC();
            if (stack.pop() === stack.pop()) {
              frame.pc = pc;
            }
            break;
          case Bytecodes.IF_ACMPNE:
            pc = frame.readTargetPC();
            if (stack.pop() !== stack.pop()) {
              frame.pc = pc;
            }
            break;
          case Bytecodes.IFNULL:
            pc = frame.readTargetPC();
            if (!stack.pop()) {
              frame.pc = pc;
            }
            break;
          case Bytecodes.IFNONNULL:
            pc = frame.readTargetPC();
            if (stack.pop()) {
              frame.pc = pc;
            }
            break;
          case Bytecodes.GOTO:
            frame.pc = frame.readTargetPC();
            break;
          case Bytecodes.GOTO_W:
            frame.pc = frame.read32Signed() - 1;
            break;
          case Bytecodes.JSR:
            pc = frame.read16();
            stack.push(frame.pc);
            frame.pc = pc;
            break;
          case Bytecodes.JSR_W:
            pc = frame.read32();
            stack.push(frame.pc);
            frame.pc = pc;
            break;
          case Bytecodes.RET:
            frame.pc = frame.getLocal(frame.read8());
            break;
          case Bytecodes.I2L:
            stack.push2(Long.fromInt(stack.pop()));
            break;
          case Bytecodes.I2F:
            break;
          case Bytecodes.I2D:
            stack.push2(stack.pop());
            break;
          case Bytecodes.L2I:
            stack.push(stack.pop2().toInt());
            break;
          case Bytecodes.L2F:
            stack.push(Math.fround(stack.pop2().toNumber()));
            break;
          case Bytecodes.L2D:
            stack.push2(stack.pop2().toNumber());
            break;
          case Bytecodes.F2I:
            stack.push(util.double2int(stack.pop()));
            break;
          case Bytecodes.F2L:
            stack.push2(Long.fromNumber(stack.pop()));
            break;
          case Bytecodes.F2D:
            stack.push2(stack.pop());
            break;
          case Bytecodes.D2I:
            stack.push(util.double2int(stack.pop2()));
            break;
          case Bytecodes.D2L:
            stack.push2(util.double2long(stack.pop2()));
            break;
          case Bytecodes.D2F:
            stack.push(Math.fround(stack.pop2()));
            break;
          case Bytecodes.I2B:
            stack.push((stack.pop() << 24) >> 24);
            break;
          case Bytecodes.I2C:
            stack.push(stack.pop() & 0xffff);
            break;
          case Bytecodes.I2S:
            stack.push((stack.pop() << 16) >> 16);
            break;
          case Bytecodes.TABLESWITCH:
            frame.pc = frame.tableSwitch();
            break;
          case Bytecodes.LOOKUPSWITCH:
            frame.pc = frame.lookupSwitch();
            break;
          case Bytecodes.NEWARRAY:
            type = frame.read8();
            size = stack.pop();
            if (size < 0) {
              throw $.newNegativeArraySizeException();
            }
            stack.push(util.newPrimitiveArray("????ZCFDBSIJ"[type], size));
            break;
          case Bytecodes.ANEWARRAY:
            index = frame.read16();
            classInfo = resolveClass(index, mi.classInfo, false);
            classInitCheck(classInfo, frame.pc - 3);
            size = stack.pop();
            if (size < 0) {
              throw $.newNegativeArraySizeException();
            }
            stack.push(util.newArray(classInfo, size));
            break;
          case Bytecodes.MULTIANEWARRAY:
            index = frame.read16();
            classInfo = resolveClass(index, mi.classInfo, false);
            var dimensions = frame.read8();
            var lengths = new Array(dimensions);
            for (var i = 0; i < dimensions; i++)
              lengths[i] = stack.pop();
            stack.push(util.newMultiArray(classInfo, lengths.reverse()));
            break;
          case Bytecodes.ARRAYLENGTH:
            array = stack.pop();
            stack.push(array.length);
            break;
          case Bytecodes.ARRAYLENGTH_IF_ICMPGE:
            array = stack.pop();
            stack.push(array.length);
            frame.pc ++;
            pc = frame.readTargetPC();
            if (stack.pop() <= stack.pop()) {
              frame.pc = pc;
            }
            break;
          case Bytecodes.GETFIELD:
            index = frame.read16();
            fieldInfo = resolveField(index, mi.classInfo, false);
            object = stack.pop();
            stack.pushKind(fieldInfo.kind, fieldInfo.get(object));
            frame.patch(3, Bytecodes.GETFIELD, Bytecodes.RESOLVED_GETFIELD);
            break;
          case Bytecodes.RESOLVED_GETFIELD:
            fieldInfo = <FieldInfo><any>rp[frame.read16()];
            object = stack.pop();
            stack.pushKind(fieldInfo.kind, fieldInfo.get(object));
            break;
          case Bytecodes.PUTFIELD:
            index = frame.read16();
            fieldInfo = resolveField(index, mi.classInfo, false);
            value = stack.popKind(fieldInfo.kind);
            object = stack.pop();
            fieldInfo.set(object, value);
            frame.patch(3, Bytecodes.PUTFIELD, Bytecodes.RESOLVED_PUTFIELD);
            break;
          case Bytecodes.RESOLVED_PUTFIELD:
            fieldInfo = <FieldInfo><any>rp[frame.read16()];
            value = stack.popKind(fieldInfo.kind);
            object = stack.pop();
            fieldInfo.set(object, value);
            break;
          case Bytecodes.GETSTATIC:
            index = frame.read16();
            fieldInfo = resolveField(index, mi.classInfo, true);
            classInitCheck(fieldInfo.classInfo, frame.pc - 3);
            if (U) {
              return;
            }
            value = fieldInfo.getStatic();
            stack.pushKind(fieldInfo.kind, value);
            break;
          case Bytecodes.PUTSTATIC:
            index = frame.read16();
            fieldInfo = resolveField(index, mi.classInfo, true);
            classInitCheck(fieldInfo.classInfo, frame.pc - 3);
            if (U) {
              return;
            }
            fieldInfo.setStatic(stack.popKind(fieldInfo.kind));
            break;
          case Bytecodes.NEW:
            index = frame.read16();
            classInfo = resolveClass(index, mi.classInfo, false);
            classInitCheck(classInfo, frame.pc - 3);
            if (U) {
              return;
            }
            stack.push(util.newObject(classInfo));
            break;
          case Bytecodes.CHECKCAST:
            index = frame.read16();
            classInfo = resolveClass(index, mi.classInfo, false);
            object = stack[stack.length - 1];
            if (object && !isAssignableTo(object.klass, classInfo.klass)) {
              throw $.newClassCastException(
                  object.klass.classInfo.className + " is not assignable to " +
                  classInfo.className);
            }
            break;
          case Bytecodes.INSTANCEOF:
            index = frame.read16();
            classInfo = resolveClass(index, mi.classInfo, false);
            object = stack.pop();
            var result = !object ? false : isAssignableTo(object.klass, classInfo.klass);
            stack.push(result ? 1 : 0);
            break;
          case Bytecodes.ATHROW:
            object = stack.pop();
            if (!object) {
              throw $.newNullPointerException();
            }
            throw object;
            break;
          case Bytecodes.MONITORENTER:
            object = stack.pop();
            ctx.monitorEnter(object);
            if (U === VMState.Pausing) {
              return;
            }
            break;
          case Bytecodes.MONITOREXIT:
            object = stack.pop();
            ctx.monitorExit(object);
            break;
          case Bytecodes.WIDE:
            frame.wide();
            break;
          case Bytecodes.RESOLVED_INVOKEVIRTUAL:
            index = frame.read16();
            var calleeMethodInfo = <MethodInfo><any>rp[index];
            var object = frame.peekInvokeObject(calleeMethodInfo);

            calleeMethod = object[calleeMethodInfo.mangledName];
            var calleeTargetMethodInfo: MethodInfo = calleeMethod.methodInfo;

            if (calleeTargetMethodInfo && !calleeTargetMethodInfo.isNative && calleeTargetMethodInfo.state !== MethodState.Compiled) {
              var calleeFrame = Frame.create(calleeTargetMethodInfo, [], 0);
              ArrayUtilities.popManyInto(stack, calleeTargetMethodInfo.consumeArgumentSlots, calleeFrame.local);
              frames.push(calleeFrame);
              frame = calleeFrame;
              mi = frame.methodInfo;
              ci = mi.classInfo;
              rp = ci.resolved_constant_pool;
              stack = frame.stack;
              lastPC = -1;
              continue;
            }

            // Call directy.
            var returnValue;
            var argumentSlots = calleeMethodInfo.argumentSlots;
            switch (argumentSlots) {
              case 0:
                returnValue = calleeMethod.call(object);
                break;
              case 1:
                a = stack.pop();
                returnValue = calleeMethod.call(object, a);
                break;
              case 2:
                b = stack.pop();
                a = stack.pop();
                returnValue = calleeMethod.call(object, a, b);
                break;
              case 3:
                c = stack.pop();
                b = stack.pop();
                a = stack.pop();
                returnValue = calleeMethod.call(object, a, b, c);
                break;
              default:
                debugger;
            }
            stack.pop();
            if (!release) {
              checkReturnValue(calleeMethodInfo, returnValue);
            }
            if (U) {
              return;
            }
            if (calleeMethodInfo.getReturnKind() !== Kind.Void) {
              if (isTwoSlot(calleeMethodInfo.getReturnKind())) {
                stack.push2(returnValue);
              } else {
                stack.push(returnValue);
              }
            }
            break;
          case Bytecodes.INVOKEVIRTUAL:
          case Bytecodes.INVOKESPECIAL:
          case Bytecodes.INVOKESTATIC:
          case Bytecodes.INVOKEINTERFACE:
            index = frame.read16();
            if (op === Bytecodes.INVOKEINTERFACE) {
              frame.read16(); // Args Number & Zero
            }
            var isStatic = (op === Bytecodes.INVOKESTATIC);

            // Resolve method and do the class init check if necessary.
            var calleeMethodInfo = resolveMethod(index, mi.classInfo, isStatic);

            // Fast path for some of the most common interpreter call targets.
            if (calleeMethodInfo.implKey === "java/lang/Object.<init>.()V") {
              stack.pop();
              continue;
            }

            if (isStatic) {
              classInitCheck(calleeMethodInfo.classInfo, lastPC);
              if (U) {
                return;
              }
            }

            // Figure out the target method.
            var calleeTargetMethodInfo: MethodInfo = calleeMethodInfo;
            object = null;
            var calleeMethod: any;
            if (!isStatic) {
              object = frame.peekInvokeObject(calleeMethodInfo);
              switch (op) {
                case Bytecodes.INVOKEVIRTUAL:
                  if (!calleeTargetMethodInfo.hasTwoSlotArguments &&
                      !calleeTargetMethodInfo.isSynchronized &&
                      calleeTargetMethodInfo.argumentSlots < 4) {
                    frame.patch(3, Bytecodes.INVOKEVIRTUAL, Bytecodes.RESOLVED_INVOKEVIRTUAL);
                  }
                case Bytecodes.INVOKEINTERFACE:
                  calleeMethod = object[calleeMethodInfo.mangledName];
                  calleeTargetMethodInfo = calleeMethod.methodInfo;
                  break;
                case Bytecodes.INVOKESPECIAL:
                  checkNull(object);
                  calleeMethod = calleeMethodInfo.fn;
                  break;
              }
            } else {
              calleeMethod = calleeMethodInfo.fn;
            }
            // Call method directly in the interpreter if we can.
            if (calleeTargetMethodInfo && !calleeTargetMethodInfo.isNative && calleeTargetMethodInfo.state !== MethodState.Compiled) {
              var calleeFrame = Frame.create(calleeTargetMethodInfo, [], 0);
              ArrayUtilities.popManyInto(stack, calleeTargetMethodInfo.consumeArgumentSlots, calleeFrame.local);
              frames.push(calleeFrame);
              frame = calleeFrame;
              mi = frame.methodInfo;
              ci = mi.classInfo;
              rp = ci.resolved_constant_pool;
              stack = frame.stack;
              lastPC = -1;
              if (calleeTargetMethodInfo.isSynchronized) {
                if (!calleeFrame.lockObject) {
                  frame.lockObject = calleeTargetMethodInfo.isStatic
                    ? calleeTargetMethodInfo.classInfo.getClassObject()
                    : frame.getLocal(0);
                }
                ctx.monitorEnter(calleeFrame.lockObject);
                if (U === VMState.Pausing) {
                  return;
                }
              }
              continue;
            }

            // Call directy.
            var returnValue;
            var argumentSlots = calleeMethodInfo.hasTwoSlotArguments ? -1 : calleeMethodInfo.argumentSlots;
            switch (argumentSlots) {
              case 0:
                returnValue = calleeMethod.call(object);
                break;
              case 1:
                a = stack.pop();
                returnValue = calleeMethod.call(object, a);
                break;
              case 2:
                b = stack.pop();
                a = stack.pop();
                returnValue = calleeMethod.call(object, a, b);
                break;
              case 3:
                c = stack.pop();
                b = stack.pop();
                a = stack.pop();
                returnValue = calleeMethod.call(object, a, b, c);
                break;
              default:
                if (calleeMethodInfo.hasTwoSlotArguments) {
                  frame.popArgumentsInto(calleeMethodInfo.signatureDescriptor, argArray);
                } else {
                  popManyInto(stack, calleeMethodInfo.argumentSlots, argArray);
                }
                var returnValue = calleeMethod.apply(object, argArray);
            }

            if (!isStatic) {
              stack.pop();
            }

            if (!release) {
              checkReturnValue(calleeMethodInfo, returnValue);
            }

            if (U) {
              return;
            }

            if (calleeMethodInfo.getReturnKind() !== Kind.Void) {
              if (isTwoSlot(calleeMethodInfo.getReturnKind())) {
                stack.push2(returnValue);
              } else {
                stack.push(returnValue);
              }
            }
            break;

          case Bytecodes.LRETURN:
          case Bytecodes.DRETURN:
            returnValue = stack.pop();
          case Bytecodes.IRETURN:
          case Bytecodes.FRETURN:
          case Bytecodes.ARETURN:
            returnValue = stack.pop();
          case Bytecodes.RETURN:
            var callee = frames.pop();
            if (callee.lockObject) {
              ctx.monitorExit(callee.lockObject);
            }
            callee.free();
            frame = frames[frames.length - 1];
            if (Frame.isMarker(frame)) { // Marker or Start Frame
              if (op === Bytecodes.RETURN) {
                return undefined;
              }
              return returnValue;
            }
            mi = frame.methodInfo;
            ci = mi.classInfo;
            rp = ci.resolved_constant_pool;
            stack = frame.stack;
            lastPC = -1;
            if (op === Bytecodes.RETURN) {
              // Nop.
            } else if (op === Bytecodes.LRETURN || op === Bytecodes.DRETURN) {
              stack.push2(returnValue);
            } else {
              stack.push(returnValue);
            }
            break;
          default:
            var opName = Bytecodes[op];
            throw new Error("Opcode " + opName + " [" + op + "] not supported.");
        }
      } catch (e) {
        e = translateException(e);
        if (!e.klass) {
          // A non-java exception was thrown. Rethrow so it is not handled by tryCatch.
          throw e;
        }
        tryCatch(e);
        frame = ctx.current();
        assert (!Frame.isMarker(frame));
        mi = frame.methodInfo;
        ci = mi.classInfo;
        rp = ci.resolved_constant_pool;
        stack = frame.stack;
        lastPC = -1;
        continue;
      }
    }
  }

  export class VM {
    static execute = interpret;
    static Yield = {toString: function () { return "YIELD" }};
    static Pause = {toString: function () { return "PAUSE" }};
    static DEBUG = false;
    static DEBUG_PRINT_ALL_EXCEPTIONS = false;
  }
}

var VM = J2ME.VM;
