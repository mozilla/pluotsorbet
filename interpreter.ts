module J2ME {
  declare var util;
  declare var Instrument;
  declare var Promise;

  import BytecodeStream = Bytecode.BytecodeStream;
  import checkArrayBounds = J2ME.checkArrayBounds;
  import checkDivideByZero = J2ME.checkDivideByZero;
  import checkDivideByZeroLong = J2ME.checkDivideByZeroLong;

  import Bytecodes = Bytecode.Bytecodes;
  import assert = Debug.assert;
  import popManyInto = ArrayUtilities.popManyInto;

  export var interpreterCounter = new Metrics.Counter(true);

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
    interpreterCounter.count("optimize: " + methodInfo.implKey);
    var stream = new BytecodeStream(methodInfo.code);
    while (stream.currentBC() !== Bytecodes.END) {
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

  function resolve(index: number, cp: ConstantPoolEntry [], isStatic: boolean = false): any {
    var entry = cp[index];
    if (entry.tag) {
      entry = $.ctx.resolve(cp, index, isStatic);
    }
    return entry;
  }

  function resolveField(index: number, cp: ConstantPoolEntry [], isStatic: boolean): FieldInfo {
    return <FieldInfo><any>resolve(index, cp, isStatic);
  }

  function resolveClass(index: number, cp: ConstantPoolEntry [], isStatic: boolean): ClassInfo {
    return <ClassInfo><any>resolve(index, cp, isStatic);
  }

  function resolveMethod(index: number, cp: ConstantPoolEntry [], isStatic: boolean): MethodInfo {
    return <MethodInfo><any>resolve(index, cp, isStatic);
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
      U !== J2ME.VMState.Pausing) {
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
  export var ops = 0;

  /**
   * Temporarily used for fn.apply.
   */
  var argArray = [];

  export function interpret(ctx: Context) {
    var frame = ctx.current();

    var cp = frame.cp;
    var stack = frame.stack;
    var returnValue = null;

    function popFrame(consumes) {
      if (frame.lockObject)
        ctx.monitorExit(frame.lockObject);
      var callee = frame;
      ctx.frames.pop();
      var caller = frame = ctx.frames.length === 0 ? null : ctx.current();
      Instrument.callExitHooks(callee.methodInfo, caller, callee);
      if (frame === null) {
        returnValue = null;
        switch (consumes) {
          case 2:
            returnValue = callee.stack.pop2();
            break;
          case 1:
            returnValue = callee.stack.pop();
            break;
        }
        return true;
      }
      stack = frame.stack;
      cp = frame.cp;
      switch (consumes) {
        case 2:
          stack.push2(callee.stack.pop2());
          break;
        case 1:
          stack.push(callee.stack.pop());
          break;
      }
      return false;
    }


    function buildExceptionLog(ex, stackTrace) {
      var className = ex.klass.classInfo.className;
      var detailMessage = util.fromJavaString(CLASSES.getField(ex.klass.classInfo, "I.detailMessage.Ljava/lang/String;").get(ex));
      return className + ": " + (detailMessage || "") + "\n" + stackTrace.map(function(entry) {
        return " - " + entry.className + "." + entry.methodName + "(), pc=" + entry.offset;
      }).join("\n") + "\n\n";
    }

    function throw_(ex, ctx) {
      var exClass = ex.class;
      if (!ex.stackTrace) {
        ex.stackTrace = [];
      }

      var stackTrace = ex.stackTrace;

      var classInfo;

      do {
        var exception_table = frame.methodInfo.exception_table;
        var handler_pc = null;
        for (var i=0; exception_table && i<exception_table.length; i++) {
          if (frame.pc >= exception_table[i].start_pc && frame.pc <= exception_table[i].end_pc) {
            if (exception_table[i].catch_type === 0) {
              handler_pc = exception_table[i].handler_pc;
              break;
            } else {
              classInfo = resolve(exception_table[i].catch_type, cp, false);
              if (isAssignableTo(ex.klass, classInfo.klass)) {
                handler_pc = exception_table[i].handler_pc;
                break;
              }
            }
          }
        }

        classInfo = frame.methodInfo.classInfo;
        if (classInfo && classInfo.className) {
          stackTrace.push({
            className: classInfo.className,
            methodName: frame.methodInfo.name,
            offset: frame.pc
          });
        }

        if (handler_pc != null) {
          stack.length = 0;
          stack.push(ex);
          frame.pc = handler_pc;

          if (VM.DEBUG_PRINT_ALL_EXCEPTIONS) {
            console.error(buildExceptionLog(ex, stackTrace));
          }

          return;
        }
        popFrame(0);
      } while (frame);

      if (ctx.frameSets.length === 0) {
        ctx.kill();

        if (ctx.thread && ctx.thread.waiting && ctx.thread.waiting.length > 0) {
          console.error(buildExceptionLog(ex, stackTrace));

          ctx.thread.waiting.forEach(function(waitingCtx, n) {
            ctx.thread.waiting[n] = null;
            waitingCtx.wakeup(ctx.thread);
          });
        }
        throw new Error(buildExceptionLog(ex, stackTrace));
      } else {
        throw ex;
      }
    }

    function classInitCheck(classInfo, ip) {
      if (classInfo.isArrayClass || ctx.runtime.initialized[classInfo.className])
        return;
      ctx.pushClassInitFrame(classInfo);

      if (U) {
        frame.pc = ip;
        return;
      }
    }

    var traceBytecodes = false;
    var traceSourceLocation = true;
    var lastSourceLocation;

    var index: any, value: any, constant: any;
    var a: any, b: any, c: any;
    var pc: number, startPc: number;
    var type;
    var size;

    var array: any;
    var object: java.lang.Object;
    var fieldInfo: FieldInfo;
    var classInfo: ClassInfo;

    if (!frame.methodInfo.isOptimized && frame.methodInfo.opCount > 100) {
      optimizeMethodBytecode(frame.methodInfo);
    }

    while (true) {
      ops ++;
      frame.methodInfo.opCount ++;

      var n = frame.pc + Bytecode.lengthAt(frame.code, frame.pc);
      var op: Bytecodes = frame.read8();
      if (traceBytecodes) {
        if (traceSourceLocation) {
          if (frame.methodInfo) {
            var sourceLocation = frame.methodInfo.getSourceLocationForPC(frame.pc - 1);
            if (sourceLocation && !sourceLocation.equals(lastSourceLocation)) {
              traceWriter && traceWriter.greenLn(sourceLocation.toString() + " " + CLASSES.getSourceLine(sourceLocation));
              lastSourceLocation = sourceLocation;
            }
          }
        }
        if (traceWriter) {
          frame.trace(traceWriter);
        }
      }

      // frame.trace(new IndentingWriter());

      //if (interpreterCounter) {
      //  var key: any = "";
      //  key += frame.methodInfo.isSynchronized ? " Synchronized" : "";
      //  key += frame.methodInfo.exception_table.length ? " Has Exceptions" : "";
      //  key += " " + frame.methodInfo.implKey;
      //  interpreterCounter.count("OP " + key);
      //}


      // interpreterCounter && interpreterCounter.count("OP " + frame.methodInfo.implKey + " ");

      // interpreterCounter.count(frame.methodInfo.implKey);
      interpreterCounter && interpreterCounter.count("OP " + Bytecodes[op]);
      //interpreterCounter && interpreterCounter.count("DI " + Bytecodes[op] + " " + Bytecodes[frame.code[n]]);

      try {
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
            constant = resolve(index, cp, false);
            stack.push(constant);
            break;
          case Bytecodes.LDC2_W:
            index = frame.read16();
            constant = resolve(index, cp, false);
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
            frame.setLocal(index, frame.getLocal(index) + value);
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
            classInfo = resolveClass(index, cp, false);
            size = stack.pop();
            if (size < 0) {
              throw $.newNegativeArraySizeException();
            }
            stack.push(util.newArray(classInfo, size));
            break;
          case Bytecodes.MULTIANEWARRAY:
            index = frame.read16();
            classInfo = resolveClass(index, cp, false);
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
            fieldInfo = resolveField(index, cp, false);
            object = stack.pop();
            stack.pushKind(fieldInfo.kind, fieldInfo.get(object));
            break;
          case Bytecodes.PUTFIELD:
            index = frame.read16();
            fieldInfo = resolveField(index, cp, false);
            value = stack.popKind(fieldInfo.kind);
            object = stack.pop();
            fieldInfo.set(object, value);
            break;
          case Bytecodes.GETSTATIC:
            index = frame.read16();
            fieldInfo = resolveField(index, cp, true);
            classInitCheck(fieldInfo.classInfo, frame.pc - 3);
            if (U) {
              return;
            }
            value = fieldInfo.getStatic();
            stack.pushKind(fieldInfo.kind, value);
            break;
          case Bytecodes.PUTSTATIC:
            index = frame.read16();
            fieldInfo = resolveField(index, cp, true);
            classInitCheck(fieldInfo.classInfo, frame.pc - 3);
            if (U) {
              return;
            }
            fieldInfo.setStatic(stack.popKind(fieldInfo.kind));
            break;
          case Bytecodes.NEW:
            index = frame.read16();
            classInfo = resolveClass(index, cp, false);
            classInitCheck(classInfo, frame.pc - 3);
            if (U) {
              return;
            }
            stack.push(util.newObject(classInfo));
            break;
          case Bytecodes.CHECKCAST:
            index = frame.read16();
            classInfo = resolveClass(index, cp, false);
            object = stack[stack.length - 1];
            if (object && !isAssignableTo(object.klass, classInfo.klass)) {
              throw $.newClassCastException(
                  object.klass.classInfo.className + " is not assignable to " +
                  classInfo.className);
            }
            break;
          case Bytecodes.INSTANCEOF:
            index = frame.read16();
            classInfo = resolveClass(index, cp, false);
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
          case Bytecodes.INVOKEVIRTUAL:
          case Bytecodes.INVOKESPECIAL:
          case Bytecodes.INVOKESTATIC:
          case Bytecodes.INVOKEINTERFACE:
            var startPc = frame.pc - 1;
            index = frame.read16();
            if (op === Bytecodes.INVOKEINTERFACE) {
              var argsNumber = frame.read8();
              var zero = frame.read8();
            }
            var isStatic = (op === Bytecodes.INVOKESTATIC);
            var methodInfo = cp[index];
            if (methodInfo.tag) {
              methodInfo = resolve(index, cp, isStatic);
              if (isStatic) {
                classInitCheck(methodInfo.classInfo, startPc);
                if (U) {
                  return;
                }
              }
            }
            object = null;
            var fn;
            if (!isStatic) {
              object = frame.peekInvokeObject(methodInfo);
              switch (op) {
                case Bytecodes.INVOKEVIRTUAL:
                case Bytecodes.INVOKEINTERFACE:
                  fn = object[methodInfo.mangledName];
                  break;
                case Bytecodes.INVOKESPECIAL:
                  checkNull(object);
                  fn = methodInfo.fn;
                  break;
              }
            } else {
              fn = methodInfo.fn;
            }

            var returnValue;
            switch (methodInfo.argumentSlots) {
              case 0:
                returnValue = fn.call(object);
                break;
              case 1:
                a = stack.pop();
                returnValue = fn.call(object, a);
                break;
              case 2:
                b = stack.pop();
                a = stack.pop();
                returnValue = fn.call(object, a, b);
                break;
              case 3:
                c = stack.pop();
                b = stack.pop();
                a = stack.pop();
                returnValue = fn.call(object, a, b, c);
                break;
              default:
                if (methodInfo.hasTwoSlotArguments) {
                  frame.popArgumentsInto(methodInfo.signatureDescriptor, argArray);
                } else {
                  popManyInto(stack, methodInfo.argumentSlots, argArray);
                }
                var returnValue = fn.apply(object, argArray);
            }
            if (!isStatic) stack.pop();

            if (!release) {
              checkReturnValue(methodInfo, returnValue);
            }

            if (U) {
              return;
            }

            if (methodInfo.getReturnKind() !== Kind.Void) {
              release || assert(returnValue !== undefined, methodInfo.signatureDescriptor + " " + methodInfo.returnKind + " " + Kind.Void);
              if (isTwoSlot(methodInfo.getReturnKind())) {
                stack.push2(returnValue);
              } else {
                stack.push(returnValue);
              }
            }
            break;
          case Bytecodes.RETURN:
            var shouldReturn = popFrame(0);
            if (shouldReturn) {
              return returnValue;
            }
            break;
          case Bytecodes.IRETURN:
          case Bytecodes.FRETURN:
          case Bytecodes.ARETURN:
            var shouldReturn = popFrame(1);
            if (shouldReturn) {
              return returnValue;
            }
            break;
          case Bytecodes.LRETURN:
          case Bytecodes.DRETURN:
            var shouldReturn = popFrame(2);
            if (shouldReturn) {
              return returnValue;
            }
            break;
          default:
            var opName = Bytecodes[op];
            throw new Error("Opcode " + opName + " [" + op + "] not supported.");
        }
      } catch (e) {
        // This could potentially hide interpreter exceptions. Maybe we should only do this for
        // compiled/native functions.
        if (e.name === "TypeError") {
          // JavaScript's TypeError is analogous to a NullPointerException.
          e = $.newNullPointerException(e.message);
        }

        throw_(e, ctx);
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