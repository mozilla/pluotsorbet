module J2ME {
  declare var util;
  declare var Long;
  declare var JavaException;

  import Bytecodes = Bytecode.Bytecodes;
  import assert = Debug.assert;

  export var interpreterCounter = new Metrics.Counter(true);

  function traceArrayStore(idx: number, array: any [], value: any) {
    traceWriter && traceWriter.writeLn(toDebugString(array) + "[" + idx + "] = " + toDebugString(value));
  }

  function traceArrayLoad(idx: number, array: any []) {
    assert(array[idx] !== undefined);
    traceWriter && traceWriter.writeLn(toDebugString(array) + "[" + idx + "] (" + toDebugString(array[idx]) + ")");
  }

  export function interpret(ctx: Context) {
    var frame = ctx.current();

    var cp = frame.cp;
    var stack = frame.stack;
    var returnValue = null;

    if (traceWriter) {
      for (var i = 0; i < ctx.frames.length; i++) {
        var methodInfo = ctx.frames[i].methodInfo;
        var localsStr = ctx.frames[i].locals.map(function (x) {
          return toDebugString(x);
        }).join(", ");

        var printObj = "";
        //if (!methodInfo.isStatic) {
        //  printObj = " <" + toDebugString(this) + "> ";
        //}
        traceWriter.enter("> " + MethodType[MethodType.Interpreted][0] + " " + methodInfo.classInfo.className + "/" + methodInfo.name + signatureToDefinition(methodInfo.signature, true, true) + printObj + ", arguments: " + localsStr);
      }
    }

    interpreterCounter && interpreterCounter.count(frame.methodInfo.implKey);

    function popFrame(consumes) {
      if (frame.lockObject)
        ctx.monitorExit(frame.lockObject);
      var callee = frame;
      frame = ctx.popFrame();
      traceWriter && traceWriter.leave("< ");
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
      return className + ": " + (detailMessage || "") + "\n" + stackTrace.join("\n") + "\n\n";
    }

    function throw_(ex, ctx) {
      var exClass = ex.class;

      var stackTrace = [];

      do {
        var exception_table = frame.methodInfo.exception_table;
        var handler_pc = null;
        for (var i=0; exception_table && i<exception_table.length; i++) {
          if (frame.bci >= exception_table[i].start_pc && frame.bci <= exception_table[i].end_pc) {
            if (exception_table[i].catch_type === 0) {
              handler_pc = exception_table[i].handler_pc;
              break;
            } else {
              var classInfo = resolve(exception_table[i].catch_type);
              if (isAssignableTo(ex.klass, classInfo.klass)) {
                handler_pc = exception_table[i].handler_pc;
                break;
              }
            }
          }
        }

        var classInfo = frame.methodInfo.classInfo;
        if (classInfo && classInfo.className) {
          stackTrace.push(" - " + classInfo.className + "." + frame.methodInfo.name + "(), bci=" + frame.bci);
        }

        if (handler_pc != null) {
          stack.length = 0;
          stack.push(ex);
          frame.bci = handler_pc;

          if (VM.DEBUG_PRINT_ALL_EXCEPTIONS) {
            console.error(buildExceptionLog(ex, stackTrace));
          }

          return;
        }

        if (ctx.frames.length == 1) {
          break;
        }

        popFrame(0);
      } while (frame);
      ctx.kill();

      if (ctx.thread && ctx.thread.waiting && ctx.thread.waiting.length > 0) {
        console.error(buildExceptionLog(ex, stackTrace));

        ctx.thread.waiting.forEach(function(waitingCtx, n) {
          ctx.thread.waiting[n] = null;
          waitingCtx.wakeup(ctx.thread);
        });
      } else {
        throw new Error(buildExceptionLog(ex, stackTrace));
      }
    }

    function checkArrayAccess(refArray, idx) {
      if (!refArray) {
        ctx.raiseExceptionAndYield("java/lang/NullPointerException");
      }
      if (idx < 0 || idx >= refArray.length) {
        ctx.raiseExceptionAndYield("java/lang/ArrayIndexOutOfBoundsException", idx);
      }
    }

    function classInitCheck(classInfo, ip) {
      if (classInfo.isArrayClass || ctx.runtime.initialized[classInfo.className])
        return;
      try {
        ctx.pushClassInitFrame(classInfo);
      } catch (e) {
        frame.bci = ip;
        throwHelper(e);
      }
    }

    function resolve(idx: number, isStatic?: boolean) {
      try {
        return ctx.resolve(cp, idx, isStatic);
      } catch (e) {
        if (e instanceof JavaException) {
          ctx.raiseExceptionAndYield(e.javaClassName, e.message);
        } else {
          throwHelper(e);
        }
      }
    }

    var traceBytecodes = false;
    var traceSourceLocation = true;
    var lastSourceLocation;

    while (true) {
      var op: Bytecodes = frame.read8();
      if (traceBytecodes) {
        if (traceSourceLocation) {
          if (frame.methodInfo) {
            var sourceLocation = frame.methodInfo.getSourceLocationForBci(frame.bci - 1);
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

      // interpreterCounter && interpreterCounter.count(Bytecodes[op]);


      // console.trace(ctx.thread.pid, frame.methodInfo.classInfo.className + " " + frame.methodInfo.name + " " + (frame.bci - 1) + " " + OPCODES[op] + " " + stack.join(","));
      switch (op) {
        case Bytecodes.NOP:
          break;
        case Bytecodes.ACONST_NULL:
          stack.push(null);
          break;
        case Bytecodes.ICONST_M1:
          stack.push(-1);
          break;
        case Bytecodes.ICONST_0:
        case Bytecodes.FCONST_0:
          stack.push(0);
          break;
        case Bytecodes.DCONST_0:
          stack.push2(0);
          break;
        case Bytecodes.ICONST_1:
        case Bytecodes.FCONST_1:
          stack.push(1);
          break;
        case Bytecodes.DCONST_1:
          stack.push2(1);
          break;
        case Bytecodes.ICONST_2:
        case Bytecodes.FCONST_2:
          stack.push(2);
          break;
        case Bytecodes.ICONST_3:
          stack.push(3);
          break;
        case Bytecodes.ICONST_4:
          stack.push(4);
          break;
        case Bytecodes.ICONST_5:
          stack.push(5);
          break;
        case Bytecodes.LCONST_0:
          stack.push2(Long.fromInt(0));
          break;
        case Bytecodes.LCONST_1:
          stack.push2(Long.fromInt(1));
          break;
        case Bytecodes.BIPUSH:
          stack.push(frame.read8signed());
          break;
        case Bytecodes.SIPUSH:
          stack.push(frame.read16signed());
          break;
        case Bytecodes.LDC:
        case Bytecodes.LDC_W:
          var idx = (op === 0x12) ? frame.read8() : frame.read16();
          var constant = cp[idx];
          if (constant.tag)
            constant = resolve(idx);
          stack.push(constant);
          break;
        case Bytecodes.LDC2_W:
          var idx = frame.read16();
          var constant = cp[idx];
          if (constant.tag)
            constant = resolve(idx);
          stack.push2(constant);
          break;
        case Bytecodes.ILOAD:
        case Bytecodes.FLOAD:
        case Bytecodes.ALOAD:
          stack.push(frame.getLocal(frame.read8()));
          break;
        case Bytecodes.LLOAD:
        case Bytecodes.DLOAD:
          stack.push2(frame.getLocal(frame.read8()));
          break;
        case Bytecodes.ILOAD_0:
        case Bytecodes.FLOAD_0:
        case Bytecodes.ALOAD_0:
          stack.push(frame.getLocal(0));
          break;
        case Bytecodes.ILOAD_1:
        case Bytecodes.FLOAD_1:
        case Bytecodes.ALOAD_1:
          stack.push(frame.getLocal(1));
          break;
        case Bytecodes.ILOAD_2:
        case Bytecodes.FLOAD_2:
        case Bytecodes.ALOAD_2:
          stack.push(frame.getLocal(2));
          break;
        case Bytecodes.ILOAD_3:
        case Bytecodes.FLOAD_3:
        case Bytecodes.ALOAD_3:
          stack.push(frame.getLocal(3));
          break;
        case Bytecodes.LLOAD_0:
        case Bytecodes.DLOAD_0:
          stack.push2(frame.getLocal(0));
          break;
        case Bytecodes.LLOAD_1:
        case Bytecodes.DLOAD_1:
          stack.push2(frame.getLocal(1));
          break;
        case Bytecodes.LLOAD_2:
        case Bytecodes.DLOAD_2:
          stack.push2(frame.getLocal(2));
          break;
        case Bytecodes.LLOAD_3:
        case Bytecodes.DLOAD_3:
          stack.push2(frame.getLocal(3));
          break;
        case Bytecodes.IALOAD:
        case Bytecodes.FALOAD:
        case Bytecodes.AALOAD:
        case Bytecodes.BALOAD:
        case Bytecodes.CALOAD:
        case Bytecodes.SALOAD:
          var idx = stack.pop();
          var refArray = stack.pop();
          checkArrayAccess(refArray, idx);
          stack.push(refArray[idx]);
          traceArrayLoad(idx, refArray);
          break;
        case Bytecodes.LALOAD:
        case Bytecodes.DALOAD:
          var idx = stack.pop();
          var refArray = stack.pop();
          checkArrayAccess(refArray, idx);
          stack.push2(refArray[idx]);
          traceArrayLoad(idx, refArray);
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
          var val = stack.pop();
          var idx = stack.pop();
          var refArray = stack.pop();
          checkArrayAccess(refArray, idx);
          refArray[idx] = val;
          break;
        case Bytecodes.LASTORE:
        case Bytecodes.DASTORE:
          var val = stack.pop2();
          var idx = stack.pop();
          var refArray = stack.pop();
          checkArrayAccess(refArray, idx);
          refArray[idx] = val;
          traceArrayStore(idx, refArray, val);
          break;
        case Bytecodes.AASTORE:
          var val = stack.pop();
          var idx = stack.pop();
          var refArray = stack.pop();

          checkArrayAccess(refArray, idx);
          if (val && !isAssignableTo(val.klass, refArray.klass.elementKlass)) {
            ctx.raiseExceptionAndYield("java/lang/ArrayStoreException");
          }
          refArray[idx] = val;
          traceArrayStore(idx, refArray, val);
          break;
        case Bytecodes.POP:
          stack.pop();
          break;
        case Bytecodes.POP2:
          stack.pop2();
          break;
        case Bytecodes.DUP:
          var val = stack.pop();
          stack.push(val);
          stack.push(val);
          break;
        case Bytecodes.DUP_X1:
          var a = stack.pop();
          var b = stack.pop();
          stack.push(a);
          stack.push(b);
          stack.push(a);
          break;
        case Bytecodes.DUP_X2:
          var a = stack.pop();
          var b = stack.pop();
          var c = stack.pop();
          stack.push(a);
          stack.push(c);
          stack.push(b);
          stack.push(a);
          break;
        case Bytecodes.DUP2:
          var a = stack.pop();
          var b = stack.pop();
          stack.push(b);
          stack.push(a);
          stack.push(b);
          stack.push(a);
          break;
        case Bytecodes.DUP2_X1:
          var a = stack.pop();
          var b = stack.pop();
          var c = stack.pop();
          stack.push(b);
          stack.push(a);
          stack.push(c);
          stack.push(b);
          stack.push(a);
          break;
        case Bytecodes.DUP2_X2:
          var a = stack.pop();
          var b = stack.pop();
          var c = stack.pop();
          var d = stack.pop();
          stack.push(b);
          stack.push(a);
          stack.push(d);
          stack.push(c);
          stack.push(b);
          stack.push(a);
          break;
        case Bytecodes.SWAP:
          var a = stack.pop();
          var b = stack.pop();
          stack.push(a);
          stack.push(b);
          break;
        case Bytecodes.IINC:
          var idx = frame.read8();
          var val = frame.read8signed();
          frame.setLocal(idx, frame.getLocal(idx) + val);
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
          var b = stack.pop();
          var a = stack.pop();
          if (!b) {
            ctx.raiseExceptionAndYield("java/lang/ArithmeticException", "/ by zero");
          }
          stack.push((a === util.INT_MIN && b === -1) ? a : ((a / b) | 0));
          break;
        case Bytecodes.LDIV:
          var b = stack.pop2();
          var a = stack.pop2();
          if (b.isZero()) {
            ctx.raiseExceptionAndYield("java/lang/ArithmeticException", "/ by zero");
          }
          stack.push2(a.div(b));
          break;
        case Bytecodes.FDIV:
          var b = stack.pop();
          var a = stack.pop();
          stack.push(Math.fround(a / b));
          break;
        case Bytecodes.DDIV:
          var b = stack.pop2();
          var a = stack.pop2();
          stack.push2(a / b);
          break;
        case Bytecodes.IREM:
          var b = stack.pop();
          var a = stack.pop();
          if (!b) {
            ctx.raiseExceptionAndYield("java/lang/ArithmeticException", "/ by zero");
          }
          stack.push(a % b);
          break;
        case Bytecodes.LREM:
          var b = stack.pop2();
          var a = stack.pop2();
          if (b.isZero()) {
            ctx.raiseExceptionAndYield("java/lang/ArithmeticException", "/ by zero");
          }
          stack.push2(a.modulo(b));
          break;
        case Bytecodes.FREM:
          var b = stack.pop();
          var a = stack.pop();
          stack.push(Math.fround(a % b));
          break;
        case Bytecodes.DREM:
          var b = stack.pop2();
          var a = stack.pop2();
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
          var b = stack.pop();
          var a = stack.pop();
          stack.push(a << b);
          break;
        case Bytecodes.LSHL:
          var b = stack.pop();
          var a = stack.pop2();
          stack.push2(a.shiftLeft(b));
          break;
        case Bytecodes.ISHR:
          var b = stack.pop();
          var a = stack.pop();
          stack.push(a >> b);
          break;
        case Bytecodes.LSHR:
          var b = stack.pop();
          var a = stack.pop2();
          stack.push2(a.shiftRight(b));
          break;
        case Bytecodes.IUSHR:
          var b = stack.pop();
          var a = stack.pop();
          stack.push(a >>> b);
          break;
        case Bytecodes.LUSHR:
          var b = stack.pop();
          var a = stack.pop2();
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
          var b = stack.pop2();
          var a = stack.pop2();
          if (a.greaterThan(b)) {
            stack.push(1);
          } else if (a.lessThan(b)) {
            stack.push(-1);
          } else {
            stack.push(0);
          }
          break;
        case Bytecodes.FCMPL:
          var b = stack.pop();
          var a = stack.pop();
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
          var b = stack.pop();
          var a = stack.pop();
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
          var b = stack.pop2();
          var a = stack.pop2();
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
          var b = stack.pop2();
          var a = stack.pop2();
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
          var jmp = frame.bci - 1 + frame.read16signed();
          frame.bci = stack.pop() === 0 ? jmp : frame.bci;
          break;
        case Bytecodes.IFNE:
          var jmp = frame.bci - 1 + frame.read16signed();
          frame.bci = stack.pop() !== 0 ? jmp : frame.bci;
          break;
        case Bytecodes.IFLT:
          var jmp = frame.bci - 1 + frame.read16signed();
          frame.bci = stack.pop() < 0 ? jmp : frame.bci;
          break;
        case Bytecodes.IFGE:
          var jmp = frame.bci - 1 + frame.read16signed();
          frame.bci = stack.pop() >= 0 ? jmp : frame.bci;
          break;
        case Bytecodes.IFGT:
          var jmp = frame.bci - 1 + frame.read16signed();
          frame.bci = stack.pop() > 0 ? jmp : frame.bci;
          break;
        case Bytecodes.IFLE:
          var jmp = frame.bci - 1 + frame.read16signed();
          frame.bci = stack.pop() <= 0 ? jmp : frame.bci;
          break;
        case Bytecodes.IF_ICMPEQ:
          var jmp = frame.bci - 1 + frame.read16signed();
          frame.bci = stack.pop() === stack.pop() ? jmp : frame.bci;
          break;
        case Bytecodes.IF_ICMPNE:
          var jmp = frame.bci - 1 + frame.read16signed();
          frame.bci = stack.pop() !== stack.pop() ? jmp : frame.bci;
          break;
        case Bytecodes.IF_ICMPLT:
          var jmp = frame.bci - 1 + frame.read16signed();
          frame.bci = stack.pop() > stack.pop() ? jmp : frame.bci;
          break;
        case Bytecodes.IF_ICMPGE:
          var jmp = frame.bci - 1 + frame.read16signed();
          frame.bci = stack.pop() <= stack.pop() ? jmp : frame.bci;
          break;
        case Bytecodes.IF_ICMPGT:
          var jmp = frame.bci - 1 + frame.read16signed();
          frame.bci = stack.pop() < stack.pop() ? jmp : frame.bci;
          break;
        case Bytecodes.IF_ICMPLE:
          var jmp = frame.bci - 1 + frame.read16signed();
          frame.bci = stack.pop() >= stack.pop() ? jmp : frame.bci;
          break;
        case Bytecodes.IF_ACMPEQ:
          var jmp = frame.bci - 1 + frame.read16signed();
          frame.bci = stack.pop() === stack.pop() ? jmp : frame.bci;
          break;
        case Bytecodes.IF_ACMPNE:
          var jmp = frame.bci - 1 + frame.read16signed();
          frame.bci = stack.pop() !== stack.pop() ? jmp : frame.bci;
          break;
        case Bytecodes.IFNULL:
          var jmp = frame.bci - 1 + frame.read16signed();
          frame.bci = !stack.pop() ? jmp : frame.bci;
          break;
        case Bytecodes.IFNONNULL:
          var jmp = frame.bci - 1 + frame.read16signed();
          frame.bci = stack.pop() ? jmp : frame.bci;
          break;
        case Bytecodes.GOTO:
          frame.bci += frame.read16signed() - 1;
          break;
        case Bytecodes.GOTO_W:
          frame.bci += frame.read32signed() - 1;
          break;
        case Bytecodes.JSR:
          var jmp = frame.read16();
          stack.push(frame.bci);
          frame.bci = jmp;
          break;
        case Bytecodes.JSR_W:
          var jmp = frame.read32();
          stack.push(frame.bci);
          frame.bci = jmp;
          break;
        case Bytecodes.RET:
          frame.bci = frame.getLocal(frame.read8());
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
          var startip: number = frame.bci;
          while ((frame.bci & 3) != 0)
            frame.bci++;
          var def = frame.read32signed();
          var low = frame.read32signed();
          var high = frame.read32signed();
          var val = stack.pop();
          var jmp;
          if (val < low || val > high) {
            jmp = def;
          } else {
            frame.bci += (val - low) << 2;
            jmp = frame.read32signed();
          }
          frame.bci = startip - 1 + jmp;
          break;
        case Bytecodes.LOOKUPSWITCH:
          var startip: number = frame.bci;
          while ((frame.bci & 3) != 0)
            frame.bci++;
          var jmp = frame.read32signed();
          var size = frame.read32();
          var val = frame.stack.pop();
          lookup:
            for (var i = 0; i < size; i++) {
              var key = frame.read32signed();
              var offset = frame.read32signed();
              if (key === val) {
                jmp = offset;
              }
              if (key >= val) {
                break lookup;
              }
            }
          frame.bci = startip - 1 + jmp;
          break;
        case Bytecodes.NEWARRAY:
          var type = frame.read8();
          var size = stack.pop();
          if (size < 0) {
            ctx.raiseExceptionAndYield("java/lang/NegativeArraySizeException", size);
          }
          stack.push(util.newPrimitiveArray("????ZCFDBSIJ"[type], size));
          break;
        case Bytecodes.ANEWARRAY:
          var idx = frame.read16();
          var classInfo = cp[idx];
          if (classInfo.tag)
            classInfo = resolve(idx);
          var size = stack.pop();
          if (size < 0) {
            ctx.raiseExceptionAndYield("java/lang/NegativeArraySizeException", size);
          }
          stack.push(util.newArray(classInfo, size));
          break;
        case Bytecodes.MULTIANEWARRAY:
          var idx = frame.read16();
          var classInfo = cp[idx];
          if (classInfo.tag)
            classInfo = resolve(idx);
          var dimensions = frame.read8();
          var lengths = new Array(dimensions);
          for (var i = 0; i < dimensions; i++)
            lengths[i] = stack.pop();
          stack.push(util.newMultiArray(classInfo, lengths.reverse()));
          break;
        case Bytecodes.ARRAYLENGTH:
          var obj = stack.pop();
          if (!obj) {
            ctx.raiseExceptionAndYield("java/lang/NullPointerException");
          }
          stack.push(obj.length);
          break;
        case Bytecodes.GETFIELD:
          var idx = frame.read16();
          var field = cp[idx];
          if (field.tag)
            field = resolve(idx, false);
          var obj = stack.pop();
          if (!obj) {
            ctx.raiseExceptionAndYield("java/lang/NullPointerException");
          }
          stack.pushType(field.signature, field.get(obj));
          break;
        case Bytecodes.PUTFIELD:
          var idx = frame.read16();
          var field = cp[idx];
          if (field.tag)
            field = resolve(idx, false);
          var val = stack.popType(field.signature);
          var obj = stack.pop();
          if (!obj) {
            ctx.raiseExceptionAndYield("java/lang/NullPointerException");
          }
          field.set(obj, val);
          break;
        case Bytecodes.GETSTATIC:
          var idx = frame.read16();
          var field = cp[idx];
          if (field.tag)
            field = resolve(idx, true);
          classInitCheck(field.classInfo, frame.bci - 3);
          var value = field.getStatic();
          if (typeof value === "undefined") {
            value = util.defaultValue(field.signature);
          }
          stack.pushType(field.signature, value);
          break;
        case Bytecodes.PUTSTATIC:
          var idx = frame.read16();
          var field = cp[idx];
          if (field.tag)
            field = resolve(idx, true);
          classInitCheck(field.classInfo, frame.bci - 3);
          field.setStatic(stack.popType(field.signature));
          break;
        case Bytecodes.NEW:
          var idx = frame.read16();
          var classInfo = cp[idx];
          if (classInfo.tag)
            classInfo = resolve(idx);
          classInitCheck(classInfo, frame.bci - 3);
          stack.push(util.newObject(classInfo));
          break;
        case Bytecodes.CHECKCAST:
          var idx = frame.read16();
          var classInfo = cp[idx];
          if (classInfo.tag)
            classInfo = resolve(idx);
          var obj = stack[stack.length - 1];
          if (obj && !isAssignableTo(obj.klass, classInfo.klass)) {
            ctx.raiseExceptionAndYield("java/lang/ClassCastException",
              obj.klass.classInfo.className + " is not assignable to " +
              classInfo.className);
          }
          break;
        case Bytecodes.INSTANCEOF:
          var idx = frame.read16();
          var classInfo = cp[idx];
          if (classInfo.tag)
            classInfo = resolve(idx);
          var obj = stack.pop();
          var result = !obj ? false : isAssignableTo(obj.klass, classInfo.klass);
          stack.push(result ? 1 : 0);
          break;
        case Bytecodes.ATHROW:
          if (ctx.frameSets.length > 0) {
            // Compiled code can't handle exceptions, so throw a yield to make all the compiled code bailout.
            frame.bci--;
            throw VM.Yield;
          }
          var obj = stack.pop();
          if (!obj) {
            ctx.raiseExceptionAndYield("java/lang/NullPointerException");
          }
          throw_(obj, ctx);
          break;
        case Bytecodes.MONITORENTER:
          var obj = stack.pop();
          if (!obj) {
            ctx.raiseExceptionAndYield("java/lang/NullPointerException");
          }
          ctx.monitorEnter(obj);
          break;
        case Bytecodes.MONITOREXIT:
          var obj = stack.pop();
          if (!obj) {
            ctx.raiseExceptionAndYield("java/lang/NullPointerException");
          }
          ctx.monitorExit(obj);
          break;
        case Bytecodes.WIDE:
          switch (op = frame.read8()) {
            case Bytecodes.ILOAD:
            case Bytecodes.FLOAD:
            case Bytecodes.ALOAD:
              stack.push(frame.getLocal(frame.read16()));
              break;
            case Bytecodes.LLOAD:
            case Bytecodes.DLOAD:
              stack.push2(frame.getLocal(frame.read16()));
              break;
            case Bytecodes.ISTORE:
            case Bytecodes.FSTORE:
            case Bytecodes.ASTORE:
              frame.setLocal(frame.read16(), stack.pop());
              break;
            case Bytecodes.LSTORE:
            case Bytecodes.DSTORE:
              frame.setLocal(frame.read16(), stack.pop2());
              break;
            case Bytecodes.IINC:
              var idx = frame.read16();
              var val = frame.read16signed();
              frame.setLocal(idx, frame.getLocal(idx) + val);
              break;
            case Bytecodes.RET:
              frame.bci = frame.getLocal(frame.read16());
              break;
            default:
              var opName = Bytecodes[op];
              throw new Error("Wide opcode " + opName + " [" + op + "] not supported.");
          }
          break;
        case Bytecodes.INVOKEVIRTUAL:
        case Bytecodes.INVOKESPECIAL:
        case Bytecodes.INVOKESTATIC:
        case Bytecodes.INVOKEINTERFACE:
          var startip: number = frame.bci - 1;
          var idx = frame.read16();
          if (op === 0xb9) {
            var argsNumber = frame.read8();
            var zero = frame.read8();
          }
          var isStatic = (op === 0xb8);
          var methodInfo = cp[idx];
          if (methodInfo.tag) {
            methodInfo = resolve(idx, isStatic);
            if (isStatic)
              classInitCheck(methodInfo.classInfo, startip);
          }
          var obj = null;
          var fn;
          if (!isStatic) {
            obj = frame.peekInvokeObject(methodInfo);
            if (!obj) {
              ctx.raiseExceptionAndYield("java/lang/NullPointerException");
            }
            switch (op) {
              case Bytecodes.INVOKEVIRTUAL:
              case Bytecodes.INVOKEINTERFACE:
                fn = obj[methodInfo.mangledName];
                break;
              case Bytecodes.INVOKESPECIAL:
                fn = jsGlobal[methodInfo.mangledClassAndMethodName];
                break;
            }
          } else {
            fn = jsGlobal[methodInfo.mangledClassAndMethodName];
          }

          var args = frame.popArguments(methodInfo.signatureDescriptor);
          if (!isStatic) {
            stack.pop();
          }
          var returnValue = fn.apply(obj, args);
          if (methodInfo.getReturnKind() !== Kind.Void) {
            release || assert(returnValue !== undefined, methodInfo.signatureDescriptor + " " + methodInfo.returnKind + " " + Kind.Void);
            if (isTwoSlot(methodInfo.getReturnKind())) {
              stack.push2(returnValue);
            } else {
              stack.push(returnValue);
            }
          }
          /*
          // Take off the arguments from the stack.
          var args = stack.slice(stack.length - methodInfo.consumes + (obj ? 1 : 0));
          stack.length -= methodInfo.signatureDescriptor.getArgumentSlotCount();
          // Invoke the compiled function.
          var returnValue = fn.apply(obj, args);
          // Push return value back on the stack.
          var returnType = methodInfo.signature[methodInfo.signature.length - 1];
          var isArrayReturnType = methodInfo.signature[methodInfo.signature.length - 2] === "[";
          if (isArrayReturnType) {
            stack.push(returnValue);
          } else {
            switch (returnType) {
              case 'V':
                break;
              case 'J':
              case 'D':
                stack.push2(returnValue);
                break;
              default:
                stack.push(returnValue);
                break;
            }
          }
          */

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
    }
  }

  export class VM {
    static execute = interpret;
    static Yield = {};
    static Pause = {};
    static DEBUG = false;
    static DEBUG_PRINT_ALL_EXCEPTIONS = false;
  }
}

var VM = J2ME.VM;