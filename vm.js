/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

/*global Stack */

'use strict';

var VM = {};

VM.Yield = {};
VM.Pause = {};

VM.DEBUG = false;
VM.DEBUG_PRINT_ALL_EXCEPTIONS = false;

VM.traceLog = "";
VM.trace = function(type, pid, methodInfo, returnVal) {
    VM.traceLog += type + " " + pid + " " + methodInfo.classInfo.className + "." +
                   methodInfo.name + ":" + methodInfo.signature +
                   (returnVal ? (" " + returnVal) : "") + "\n";
}

function checkArrayAccess(ctx, refArray, idx) {
    if (!refArray) {
        ctx.raiseExceptionAndYield("java/lang/NullPointerException");
    }
    if (idx < 0 || idx >= refArray.length) {
        ctx.raiseExceptionAndYield("java/lang/ArrayIndexOutOfBoundsException", idx);
    }
}

function classInitCheck(ctx, frame, classInfo, ip) {
    if (classInfo.isArrayClass || ctx.runtime.initialized[classInfo.className])
        return;
    frame.ip = ip;
    ctx.pushClassInitFrame(classInfo);
    throw VM.Yield;
}

function pushFrame(ctx, methodInfo) {
    var frame = ctx.pushFrame(methodInfo);
    if (methodInfo.isSynchronized) {
        if (!frame.lockObject) {
            frame.lockObject =
                methodInfo.isStatic ?
                methodInfo.classInfo.getClassObject(ctx) :
                frame.locals.refs[frame.localsBase + 0];
        }

        ctx.monitorEnter(frame.lockObject);
    }
    return frame;
}

function popFrame(ctx, callee, returnType) {
    if (callee.lockObject)
        ctx.monitorExit(callee.lockObject);
    var frame = ctx.popFrame();
    var stack = frame.stack;

    switch (returnType) {
    case STACK_INT:
        stack.types[stack.length] = STACK_INT;
        stack.ints[stack.length++] = callee.stack.ints[--callee.stack.length];
        break;
    case STACK_LONG:
        stack.pushLong(callee.stack.popLong());
        break;
    case STACK_FLOAT:
        stack.types[stack.length] = STACK_FLOAT;
        stack.floats[stack.length++] = callee.stack.floats[--callee.stack.length];
        break;
    case STACK_DOUBLE:
        stack.types[stack.length] = STACK_DOUBLE;
        stack.types[stack.length + 1] = STACK_DOUBLE;
        stack.doubles[stack.length] = callee.stack.doubles[callee.stack.length - 2];
        callee.stack.length -= 2;
        stack.length += 2;
        break;
    case STACK_REF:
        stack.types[stack.length] = STACK_REF;
        stack.refs[stack.length++] = callee.stack.refs[--callee.stack.length];
        break;
    }
    Stack.release(callee.stack);
    return frame;
}

function buildExceptionLog(ex, stackTrace) {
  var className = ex.class.className;
  var detailMessage = util.fromJavaString(CLASSES.getField(ex.class, "I.detailMessage.Ljava/lang/String;").get(ex));
  return className + ": " + (detailMessage || "") + "\n" + stackTrace.join("\n") + "\n\n";
}

function throw_(ex, ctx) {
    var exClass = ex.class;

    var frame = ctx.current();
    var stack = frame.stack;
    var cp = frame.cp;

    var stackTrace = [];

    do {
        var exception_table = frame.methodInfo.exception_table;
        var handler_pc = null;
        for (var i=0; exception_table && i<exception_table.length; i++) {
            if (frame.ip >= exception_table[i].start_pc && frame.ip <= exception_table[i].end_pc) {
                if (exception_table[i].catch_type === 0) {
                    handler_pc = exception_table[i].handler_pc;
                } else {
                    var classInfo = cp.resolve(ctx, exception_table[i].catch_type).value;
                    if (ex.class.isAssignableTo(classInfo)) {
                        handler_pc = exception_table[i].handler_pc;
                        break;
                    }
                }
            }
        }

        var classInfo = frame.methodInfo.classInfo;
        if (classInfo && classInfo.className) {
            stackTrace.push(" - " + classInfo.className + "." + frame.methodInfo.name + "(), bci=" + frame.ip);
        }

        if (handler_pc != null) {
            stack.length = 0;
            stack.pushRef(ex);
            frame.ip = handler_pc;

            if (VM.DEBUG_PRINT_ALL_EXCEPTIONS) {
                console.error(buildExceptionLog(ex, stackTrace));
            }

            while (frame.methodInfo.compiled) {
              frame = frame.methodInfo.compiled(ctx);
            }

            return frame;
        }

        if (ctx.frames.length == 1) {
            break;
        }

        frame = popFrame(ctx, frame, -1);
        stack = frame.stack;
        cp = frame.cp;
    } while (frame.methodInfo);

    ctx.kill();

    if (ctx.thread && ctx.thread.waiting && ctx.thread.waiting.length > 0) {
        console.error(buildExceptionLog(ex, stackTrace));

        ctx.thread.waiting.forEach(function(waitingCtx, n) {
            ctx.thread.waiting[n] = null;
            waitingCtx.wakeup(ctx.thread);
        });

        return frame;
    } else {
        throw new Error(buildExceptionLog(ex, stackTrace));
    }
}

// Many JVM instructions are sorted by type so that you can do math on
// them and map them to different operations; map each instruction to
// the proper stack storage for that type.
var INSTR_TO_STACK_STORAGE = [STACK_INT, STACK_LONG, STACK_FLOAT, STACK_DOUBLE,
                              STACK_REF, STACK_INT, STACK_INT, STACK_INT];

VM.execute = function(ctx) {
    var frame = ctx.current();

    while (frame && frame.methodInfo.compiled) {
      frame = frame.methodInfo.compiled(ctx);
    }


    if (!frame) {
      return;
    }

    var cp = frame.cp;
    var stack = frame.stack;

    while (true) {
        var op = frame.code[frame.ip++];
        switch (op) {
        case 0x00: // nop
            break;
        case 0x01: // aconst_null
            stack.types[stack.length] = STACK_REF;
            stack.refs[stack.length++] = null;
            break;
        case 0x02: // iconst_m1
        case 0x03: // iconst_0
        case 0x04: // iconst_1
        case 0x05: // iconst_2
        case 0x06: // iconst_3
        case 0x07: // iconst_4
        case 0x08: // iconst_5
            stack.types[stack.length] = STACK_INT;
            stack.ints[stack.length] = op - 0x03;
            stack.length += 1;
            break;
        case 0x09: // lconst_0
            stack.types[stack.length] = STACK_LONG;
            stack.longs[stack.length] = Long.ZERO;
            stack.types[stack.length + 1] = STACK_LONG;
            stack.length += 2;
            break;
        case 0x0a: // lconst_1
            stack.types[stack.length] = STACK_LONG;
            stack.longs[stack.length] = Long.ONE;
            stack.types[stack.length + 1] = STACK_LONG;
            stack.length += 2;
            break;
        case 0x0b: // fconst_0
        case 0x0c: // fconst_1
        case 0x0d: // fconst_2
            stack.types[stack.length] = STACK_FLOAT;
            stack.floats[stack.length++] = op - 0x0b;
            break;
        case 0x0e: // dconst_0
        case 0x0f: // dconst_1
            stack.types[stack.length] = STACK_DOUBLE;
            stack.doubles[stack.length] = op - 0x0e;
            stack.types[stack.length + 1] = STACK_DOUBLE;
            stack.length += 2;
            break;
        case 0x10: // bipush
            stack.types[stack.length] = STACK_INT;
            stack.ints[stack.length++] = frame.read8signed();
            break;
        case 0x11: // sipush
            stack.types[stack.length] = STACK_INT;
            stack.ints[stack.length++] = frame.read16signed();
            break;
        case 0x12: // ldc
        case 0x13: // ldc_w
        case 0x14: // ldc2_w
            var idx = (op === 0x12) ? frame.code[frame.ip++] : frame.read16();
            var constant = cp.resolve(ctx, idx);

            // Same as `stack.push(constant.value)`
            switch(constant.type) {
            case STACK_INT:
                stack.types[stack.length] = STACK_INT;
                stack.ints[stack.length++] = constant.value;
                break;
            case STACK_LONG:
                stack.types[stack.length] = stack.types[stack.length + 1] = STACK_LONG;
                stack.longs[stack.length++] = constant.value;
                stack.length++;
                break;
            case STACK_FLOAT:
                stack.types[stack.length] = STACK_FLOAT;
                stack.floats[stack.length++] = constant.value;
                break;
            case STACK_DOUBLE:
                stack.types[stack.length] = stack.types[stack.length + 1] = STACK_DOUBLE;
                stack.doubles[stack.length++] = constant.value;
                stack.length++;
                break;
            case STACK_REF:
                stack.types[stack.length] = STACK_REF;
                stack.refs[stack.length++] = constant.value;
                break;
            }
            break;
        case 0x15: // iload
            stack.types[stack.length] = STACK_INT;
            stack.ints[stack.length++] = frame.locals.ints[frame.localsBase + frame.code[frame.ip++]];
            break;
        case 0x16: // lload
            stack.pushLong(frame.locals.longs[frame.localsBase + frame.code[frame.ip++]]);
            break;
        case 0x17: // fload
            stack.types[stack.length] = STACK_FLOAT;
            stack.floats[stack.length++] = frame.locals.floats[frame.localsBase + frame.code[frame.ip++]];
            break;
        case 0x18: // dload
            stack.types[stack.length] = stack.types[stack.length + 1] = STACK_DOUBLE;
            stack.doubles[stack.length] = frame.locals.doubles[frame.localsBase + frame.code[frame.ip++]];
            stack.length += 2;
            break;
        case 0x19: // aload
            stack.types[stack.length] = STACK_REF;
            stack.refs[stack.length++] = frame.locals.refs[frame.localsBase + frame.code[frame.ip++]];
            break;
        case 0x1a: // iload_0
        case 0x1b: // iload_1
        case 0x1c: // iload_2
        case 0x1d: // iload_3
            stack.types[stack.length] = STACK_INT;
            stack.ints[stack.length++] = frame.locals.ints[frame.localsBase + (op - 0x1a)];
            break;
        case 0x1e: // lload_0
        case 0x1f: // lload_1
        case 0x20: // lload_2
        case 0x21: // lload_3
            stack.types[stack.length] = stack.types[stack.length + 1] = STACK_LONG;
            stack.longs[stack.length++] = frame.locals.longs[frame.localsBase + (op - 0x1e)];
            stack.length++; // empty second slot
            break;
        case 0x22: // fload_0
        case 0x23: // fload_1
        case 0x24: // fload_2
        case 0x25: // fload_3
            stack.types[stack.length] = STACK_FLOAT;
            stack.floats[stack.length++] = frame.locals.floats[frame.localsBase + (op - 0x22)];
            break;
        case 0x26: // dload_0
        case 0x27: // dload_1
        case 0x28: // dload_2
        case 0x29: // dload_3
            stack.types[stack.length] = stack.types[stack.length + 1] = STACK_DOUBLE;
            stack.doubles[stack.length++] = frame.locals.doubles[frame.localsBase + (op - 0x26)];
            stack.length++; // empty second slot
            break;
        case 0x2a: // aload_0
        case 0x2b: // aload_1
        case 0x2c: // aload_2
        case 0x2d: // aload_3
            stack.types[stack.length] = STACK_REF;
            stack.refs[stack.length++] = frame.locals.refs[frame.localsBase + (op - 0x2a)];
            break;
        case 0x2e: // iaload
        case 0x2f: // laload
        case 0x30: // faload
        case 0x31: // daload
        case 0x32: // aaload
        case 0x33: // baload
        case 0x34: // caload
        case 0x35: // saload
            var type = INSTR_TO_STACK_STORAGE[op - 0x2e];
            var idx = stack.ints[--stack.length];
            var arr = stack.refs[--stack.length];
            checkArrayAccess(ctx, arr, idx);

            // Same as `stack.push(arr[idx])`
            switch(type) {
            case STACK_INT:
                stack.types[stack.length] = STACK_INT;
                stack.ints[stack.length++] = arr[idx];
                break;
            case STACK_LONG:
                stack.types[stack.length] = stack.types[stack.length + 1] = STACK_LONG;
                stack.longs[stack.length++] = arr[idx];
                stack.length++;
                break;
            case STACK_FLOAT:
                stack.types[stack.length] = STACK_FLOAT;
                stack.floats[stack.length++] = arr[idx];
                break;
            case STACK_DOUBLE:
                stack.types[stack.length] = stack.types[stack.length + 1] = STACK_DOUBLE;
                stack.doubles[stack.length++] = arr[idx];
                stack.length++;
                break;
            case STACK_REF:
                stack.types[stack.length] = STACK_REF;
                stack.refs[stack.length++] = arr[idx];
                break;
            }
            break;
        case 0x36: // istore
            frame.locals.ints[frame.localsBase + frame.code[frame.ip++]] = stack.ints[--stack.length];
            break;
        case 0x37: // lstore
            --stack.length; // Remove the second Long word.
            frame.locals.longs[frame.localsBase + frame.code[frame.ip++]] = stack.longs[--stack.length];
            break;
        case 0x38: // fstore
            frame.locals.floats[frame.localsBase + frame.code[frame.ip++]] = stack.floats[--stack.length];
            break;
        case 0x39: // dstore
            --stack.length; // Remove the second double word.
            frame.locals.doubles[frame.localsBase + frame.code[frame.ip++]] = stack.doubles[--stack.length];
            break;
        case 0x3a: // astore
            frame.locals.refs[frame.localsBase + frame.code[frame.ip++]] = stack.refs[--stack.length];
            break;
        case 0x3b: // istore_0
        case 0x3c: // istore_1
        case 0x3d: // istore_2
        case 0x3e: // istore_3
            frame.locals.ints[frame.localsBase + (op - 0x3b)] = stack.ints[--stack.length];
            break;
        case 0x3f: // lstore_0
        case 0x40: // lstore_1
        case 0x41: // lstore_2
        case 0x42: // lstore_3
            --stack.length; // Remove the second word.
            frame.locals.longs[frame.localsBase + (op - 0x3f)] = stack.longs[--stack.length];
            break;
        case 0x43: // fstore_0
        case 0x44: // fstore_1
        case 0x45: // fstore_2
        case 0x46: // fstore_3
            frame.locals.floats[frame.localsBase + (op - 0x43)] = stack.floats[--stack.length];
            break;
        case 0x47: // dstore_0
        case 0x48: // dstore_1
        case 0x49: // dstore_2
        case 0x4a: // dstore_3
            --stack.length; // Remove the second word.
            frame.locals.doubles[frame.localsBase + (op - 0x47)] = stack.doubles[--stack.length];
            break;
        case 0x4b: // astore_0
        case 0x4c: // astore_1
        case 0x4d: // astore_2
        case 0x4e: // astore_3
            frame.locals.refs[frame.localsBase + (op - 0x4b)] = stack.refs[--stack.length];
            break;
        case 0x4f: // iastore
        case 0x50: // lastore
        case 0x51: // fastore
        case 0x52: // dastore
        case 0x53: // aastore
        case 0x54: // bastore
        case 0x55: // castore
        case 0x56: // sastore
            var type = INSTR_TO_STACK_STORAGE[op - 0x4f];
            var val = stack.pop(type);
            var idx = stack.ints[--stack.length];
            var arr = stack.refs[--stack.length];
            checkArrayAccess(ctx, arr, idx);
            if (type === STACK_REF) {
                if (val && !val.class.isAssignableTo(arr.class.elementClass)) {
                    ctx.raiseExceptionAndYield("java/lang/ArrayStoreException");
                }
            }

            arr[idx] = val;
            break;
        case 0x57: // pop
            --stack.length;
            break;
        case 0x58: // pop2
            stack.length -= 2;
            break;
        case 0x59: // dup
            var oldIdx = stack.length - 1;
            var newIdx = stack.length;
            stack.length++;
            var type = stack.types[newIdx] = stack.types[oldIdx];
            switch(type) {
            case STACK_INT: stack.ints[newIdx] = stack.ints[oldIdx]; break;
            case STACK_LONG: stack.longs[newIdx] = stack.longs[oldIdx]; break;
            case STACK_FLOAT: stack.floats[newIdx] = stack.floats[oldIdx]; break;
            case STACK_DOUBLE: stack.doubles[newIdx] = stack.doubles[oldIdx]; break;
            case STACK_REF: stack.refs[newIdx] = stack.refs[oldIdx]; break;
            }
            break;
        case 0x5a: // dup_x1
            var a = stack.popWord();
            var b = stack.popWord();
            stack.pushWord(a);
            stack.pushWord(b);
            stack.pushWord(a);
            break;
        case 0x5b: // dup_x2
            var a = stack.popWord();
            var b = stack.popWord();
            var c = stack.popWord();
            stack.pushWord(a);
            stack.pushWord(c);
            stack.pushWord(b);
            stack.pushWord(a);
            break;
        case 0x5c: // dup2
            var a = stack.popWord();
            var b = stack.popWord();
            stack.pushWord(b);
            stack.pushWord(a);
            stack.pushWord(b);
            stack.pushWord(a);
            break;
        case 0x5d: // dup2_x1
            var a = stack.popWord();
            var b = stack.popWord();
            var c = stack.popWord();
            stack.pushWord(b);
            stack.pushWord(a);
            stack.pushWord(c);
            stack.pushWord(b);
            stack.pushWord(a);
            break;
        case 0x5e: // dup2_x2
            var a = stack.popWord();
            var b = stack.popWord();
            var c = stack.popWord();
            var d = stack.popWord();
            stack.pushWord(b);
            stack.pushWord(a);
            stack.pushWord(d);
            stack.pushWord(c);
            stack.pushWord(b);
            stack.pushWord(a);
            break;
        case 0x5f: // swap
            var a = stack.popWord();
            var b = stack.popWord();
            stack.pushWord(a);
            stack.pushWord(b);
            break;
        case 0x84: // iinc
            var idx = frame.code[frame.ip++];
            frame.locals.ints[frame.localsBase + idx] += frame.read8signed();
            break;
        case 0x60: // iadd
            stack.ints[stack.length - 2] += stack.ints[stack.length - 1];
            stack.length--;
            break;
        case 0x61: // ladd
            stack.pushLong(stack.popLong().add(stack.popLong()));
            break;
        case 0x62: // fadd
            stack.floats[stack.length - 2] = Math.fround(stack.floats[stack.length - 2] +
                                                         stack.floats[stack.length - 1]);
            stack.length--;
            break;
        case 0x63: // dadd
            // [d1 _ d2 _] => [(d1 + d2) _]
            stack.doubles[stack.length - 4] += stack.doubles[stack.length - 2];
            stack.length -= 2; // Drop the last double.
            break;
        case 0x64: // isub
            stack.ints[stack.length - 2] -= stack.ints[stack.length - 1];
            stack.length--;
            break;
        case 0x65: // lsub
            stack.pushLong(stack.popLong().negate().add(stack.popLong()));
            break;
        case 0x66: // fsub
            stack.floats[stack.length - 2] = Math.fround(stack.floats[stack.length - 2] -
                                                         stack.floats[stack.length - 1]);
            stack.length--;
            break;
        case 0x67: // dsub
            stack.doubles[stack.length - 4] -= stack.doubles[stack.length - 2];
            stack.length -= 2; // Drop the last double.
            break;
        case 0x68: // imul
            stack.ints[stack.length - 2] = Math.imul(stack.ints[stack.length - 2],
                                                     stack.ints[stack.length - 1]);
            stack.length--;
            break;
        case 0x69: // lmul
            stack.pushLong(stack.popLong().multiply(stack.popLong()));
            break;
        case 0x6a: // fmul
            stack.floats[stack.length - 2] = Math.fround(stack.floats[stack.length - 2] *
                                                         stack.floats[stack.length - 1]);
            stack.length--;
            break;
        case 0x6b: // dmul
            stack.doubles[stack.length - 4] *= stack.doubles[stack.length - 2];
            stack.length -= 2; // Drop the last double.
            break;
        case 0x6c: // idiv
            var b = stack.ints[--stack.length];
            var a = stack.ints[--stack.length];
            if (!b) {
                ctx.raiseExceptionAndYield("java/lang/ArithmeticException", "/ by zero");
            }
            stack.types[stack.length] = STACK_INT;
            stack.ints[stack.length++] = ((a === util.INT_MIN && b === -1) ?
                                          a :
                                          ((a / b)|0));
            break;
        case 0x6d: // ldiv
            var b = stack.popLong();
            var a = stack.popLong();
            if (b.isZero()) {
                ctx.raiseExceptionAndYield("java/lang/ArithmeticException", "/ by zero");
            }
            stack.pushLong(a.div(b));
            break;
        case 0x6e: // fdiv
            var b = stack.floats[--stack.length];
            var a = stack.floats[--stack.length];
            stack.pushFloat(Math.fround(a / b));
            break;
        case 0x6f: // ddiv
            stack.doubles[stack.length - 4] /= stack.doubles[stack.length - 2];
            stack.length -= 2; // Drop the last double.
            break;
        case 0x70: // irem
            var b = stack.ints[--stack.length];
            var a = stack.ints[--stack.length];
            if (!b) {
                ctx.raiseExceptionAndYield("java/lang/ArithmeticException", "/ by zero");
            }
            stack.types[stack.length] = STACK_INT;
            stack.ints[stack.length++] = a % b;
            break;
        case 0x71: // lrem
            var b = stack.popLong();
            var a = stack.popLong();
            if (b.isZero()) {
                ctx.raiseExceptionAndYield("java/lang/ArithmeticException", "/ by zero");
            }
            stack.pushLong(a.modulo(b));
            break;
        case 0x72: // frem
            var b = stack.floats[--stack.length];
            var a = stack.floats[--stack.length];
            stack.pushFloat(Math.fround(a % b));
            break;
        case 0x73: // drem
            stack.doubles[stack.length - 4] %= stack.doubles[stack.length - 2];
            stack.length -= 2; // Drop the last double.
            break;
        case 0x74: // ineg
            stack.ints[stack.length - 1] *= -1;
            break;
        case 0x75: // lneg
            stack.pushLong(stack.popLong().negate());
            break;
        case 0x76: // fneg
            stack.floats[stack.length - 1] *= -1;
            break;
        case 0x77: // dneg
            stack.doubles[stack.length - 2] *= -1;
            break;
        case 0x78: // ishl
            stack.ints[stack.length - 2] <<= stack.ints[stack.length - 1];
            stack.length--;
            break;
        case 0x79: // lshl
            var b = stack.ints[--stack.length];
            var a = stack.popLong();
            stack.pushLong(a.shiftLeft(b));
            break;
        case 0x7a: // ishr
            stack.ints[stack.length - 2] >>= stack.ints[stack.length - 1];
            stack.length--;
            break;
        case 0x7b: // lshr
            var b = stack.ints[--stack.length];
            var a = stack.popLong();
            stack.pushLong(a.shiftRight(b));
            break;
        case 0x7c: // iushr
            stack.ints[stack.length - 2] >>>= stack.ints[stack.length - 1];
            stack.length--;
            break;
        case 0x7d: // lushr
            var b = stack.ints[--stack.length];
            var a = stack.popLong();
            stack.pushLong(a.shiftRightUnsigned(b));
            break;
        case 0x7e: // iand
            stack.ints[stack.length - 2] &= stack.ints[stack.length - 1];
            stack.length--;
            break;
        case 0x7f: // land
            stack.pushLong(stack.popLong().and(stack.popLong()));
            break;
        case 0x80: // ior
            stack.ints[stack.length - 2] |= stack.ints[stack.length - 1];
            stack.length--;
            break;
        case 0x81: // lor
            stack.pushLong(stack.popLong().or(stack.popLong()));
            break;
        case 0x82: // ixor
            stack.ints[stack.length - 2] ^= stack.ints[stack.length - 1];
            stack.length--;
            break;
        case 0x83: // lxor
            stack.pushLong(stack.popLong().xor(stack.popLong()));
            break;

        default:
            if (VM.handleSimpleFlowOp(op, ctx, frame, cp, stack)) {
                break;
            }

            frame = VM.handleComplexOp(op, ctx, frame, cp, stack);
            if (frame) {
                cp = frame.cp;
                stack = frame.stack;
                // Return if the caller is compiled
                if (frame.methodInfo.compiled) {
                    return frame;
                }
            } else {
                return null;
            }
        }
    }
}

VM.handleSimpleFlowOp = function(op, ctx, frame, cp, stack) {
    switch(op) {
        case 0x94: // lcmp
            var b = stack.popLong();
            var a = stack.popLong();
            stack.types[stack.length] = STACK_INT;
            if (a.greaterThan(b)) {
                stack.ints[stack.length++] = 1;
            } else if (a.lessThan(b)) {
                stack.ints[stack.length++] = -1;
            } else {
                stack.ints[stack.length++] = 0;
            }
            break;
        case 0x95: // fcmpl
            var b = stack.floats[--stack.length];
            var a = stack.floats[--stack.length];
            stack.types[stack.length] = STACK_INT;
            if (isNaN(a) || isNaN(b)) {
                stack.ints[stack.length++] = -1;
            } else if (a > b) {
                stack.ints[stack.length++] = 1;
            } else if (a < b) {
                stack.ints[stack.length++] = -1;
            } else {
                stack.ints[stack.length++] = 0;
            }
            break;
        case 0x96: // fcmpg
            var b = stack.floats[--stack.length];
            var a = stack.floats[--stack.length];
            stack.types[stack.length] = STACK_INT;
            if (isNaN(a) || isNaN(b)) {
                stack.ints[stack.length++] = 1;
            } else if (a > b) {
                stack.ints[stack.length++] = 1;
            } else if (a < b) {
                stack.ints[stack.length++] = -1;
            } else {
                stack.ints[stack.length++] = 0;
            }
            break;
        case 0x97: // dcmpl
            --stack.length; // remove dummy double
            var b = stack.doubles[--stack.length];
            --stack.length; // remove dummy double
            var a = stack.doubles[--stack.length];
            stack.types[stack.length] = STACK_INT;
            if (isNaN(a) || isNaN(b)) {
                stack.ints[stack.length++] = -1;
            } else if (a > b) {
                stack.ints[stack.length++] = 1;
            } else if (a < b) {
                stack.ints[stack.length++] = -1;
            } else {
                stack.ints[stack.length++] = 0;
            }
            break;
        case 0x98: // dcmpg
            --stack.length; // remove dummy double
            var b = stack.doubles[--stack.length];
            --stack.length; // remove dummy double
            var a = stack.doubles[--stack.length];
            stack.types[stack.length] = STACK_INT;
            if (isNaN(a) || isNaN(b)) {
                stack.ints[stack.length++] = 1;
            } else if (a > b) {
                stack.ints[stack.length++] = 1;
            } else if (a < b) {
                stack.ints[stack.length++] = -1;
            } else {
                stack.ints[stack.length++] = 0;
            }
            break;
        case 0x99: // ifeq
            var jmp = frame.ip - 1 + frame.read16signed();
            frame.ip = stack.ints[--stack.length] === 0 ? jmp : frame.ip;
            break;
        case 0x9a: // ifne
            var jmp = frame.ip - 1 + frame.read16signed();
            frame.ip = stack.ints[--stack.length] !== 0 ? jmp : frame.ip;
            break;
        case 0x9b: // iflt
            var jmp = frame.ip - 1 + frame.read16signed();
            frame.ip = stack.ints[--stack.length] < 0 ? jmp : frame.ip;
            break;
        case 0x9c: // ifge
            var jmp = frame.ip - 1 + frame.read16signed();
            frame.ip = stack.ints[--stack.length] >= 0 ? jmp : frame.ip;
            break;
        case 0x9d: // ifgt
            var jmp = frame.ip - 1 + frame.read16signed();
            frame.ip = stack.ints[--stack.length] > 0 ? jmp : frame.ip;
            break;
        case 0x9e: // ifle
            var jmp = frame.ip - 1 + frame.read16signed();
            frame.ip = stack.ints[--stack.length] <= 0 ? jmp : frame.ip;
            break;
        case 0x9f: // if_icmpeq
            var jmp = frame.ip - 1 + frame.read16signed();
            frame.ip = stack.ints[--stack.length] === stack.ints[--stack.length] ? jmp : frame.ip;
            break;
        case 0xa0: // if_cmpne
            var jmp = frame.ip - 1 + frame.read16signed();
            frame.ip = stack.ints[--stack.length] !== stack.ints[--stack.length] ? jmp : frame.ip;
            break;
        case 0xa1: // if_icmplt
            var jmp = frame.ip - 1 + frame.read16signed();
            frame.ip = stack.ints[--stack.length] > stack.ints[--stack.length] ? jmp : frame.ip;
            break;
        case 0xa2: // if_icmpge
            var jmp = frame.ip - 1 + frame.read16signed();
            frame.ip = stack.ints[--stack.length] <= stack.ints[--stack.length] ? jmp : frame.ip;
            break;
        case 0xa3: // if_icmpgt
            var jmp = frame.ip - 1 + frame.read16signed();
            frame.ip = stack.ints[--stack.length] < stack.ints[--stack.length] ? jmp : frame.ip;
            break;
        case 0xa4: // if_icmple
            var jmp = frame.ip - 1 + frame.read16signed();
            frame.ip = stack.ints[--stack.length] >= stack.ints[--stack.length] ? jmp : frame.ip;
            break;
        case 0xa5: // if_acmpeq
            var jmp = frame.ip - 1 + frame.read16signed();
            frame.ip = stack.refs[--stack.length] === stack.refs[--stack.length] ? jmp : frame.ip;
            break;
        case 0xa6: // if_acmpne
            var jmp = frame.ip - 1 + frame.read16signed();
            frame.ip = stack.refs[--stack.length] !== stack.refs[--stack.length] ? jmp : frame.ip;
            break;
        case 0xa7: // goto
            frame.ip += frame.read16signed() - 1;
            break;
        case 0xa8: // jsr
            var jmp = frame.read16();
            stack.types[stack.length] = STACK_INT;
            stack.ints[stack.length++] = frame.ip;
            frame.ip = jmp;
            break;
        case 0xa9: // ret
            frame.ip = frame.locals.ints[frame.localsBase + frame.code[frame.ip++]];
            break;
        case 0xc6: // ifnull
            var jmp = frame.ip - 1 + frame.read16signed();
            frame.ip = !stack.refs[--stack.length] ? jmp : frame.ip;
            break;
        case 0xc7: // ifnonnull
            var jmp = frame.ip - 1 + frame.read16signed();
            frame.ip = stack.refs[--stack.length] ? jmp : frame.ip;
            break;
        case 0xc8: // goto_w
            frame.ip += frame.read32signed() - 1;
            break;
        case 0xc9: // jsr_w
            var jmp = frame.read32();
            stack.types[stack.length] = STACK_INT;
            stack.ints[stack.length++] = frame.ip;
            frame.ip = jmp;
            break;
        case 0x85: // i2l
            stack.pushLong(Long.fromInt(stack.ints[--stack.length]));
            break;
        case 0x86: // i2f
            var idx = stack.length - 1;
            stack.types[idx] = STACK_FLOAT;
            stack.floats[idx] = Math.fround(stack.ints[idx]);
            break;
        case 0x87: // i2d
            var idx = stack.length - 1;
            stack.types[idx] = STACK_DOUBLE;
            stack.doubles[idx] = stack.ints[idx];
            stack.length++; // one because double is twice as wide
            break;
        case 0x88: // l2i
            var idx = stack.length - 2;
            stack.types[idx] = STACK_INT;
            stack.ints[idx] = stack.longs[idx].toInt();
            stack.length--; // long is twice as wide
            break;
        case 0x89: // l2f
            stack.pushFloat(Math.fround(stack.popLong().toNumber()));
            break;
        case 0x8a: // l2d
            stack.pushDouble(stack.popLong().toNumber());
            break;
        case 0x8b: // f2i
            var idx = stack.length - 1;
            stack.types[idx] = STACK_INT;
            stack.ints[idx] = util.double2int(stack.floats[idx]);
            break;
        case 0x8c: // f2l
            stack.pushLong(Long.fromNumber(stack.floats[--stack.length]));
            break;
        case 0x8d: // f2d
            stack.pushDouble(stack.floats[--stack.length]);
            break;
        case 0x8e: // d2i
            stack.types[stack.length - 2] = STACK_INT;
            stack.ints[stack.length - 2] = util.double2int(stack.doubles[stack.length - 2]);
            stack.length--; // double is twice as wide
            break;
        case 0x8f: // d2l
            stack.types[stack.length - 2] = STACK_LONG;
            stack.longs[stack.length - 2] = util.double2long(stack.doubles[stack.length - 2]);
            // no length change; same size
            break;
        case 0x90: // d2f
            stack.types[stack.length - 2] = STACK_FLOAT;
            stack.floats[stack.length - 2] = Math.fround(stack.doubles[stack.length - 2]);
            stack.length--; // double is twice as wide
            break;
        case 0x91: // i2b
            var val = stack.ints[stack.length - 1];
            // No need to set stack.types; int === int
            stack.ints[stack.length - 1] = (val << 24) >> 24;
            break;
        case 0x92: // i2c
            // No need to set stack.types; int === int
            stack.ints[stack.length - 1] &= 0xffff;
            break;
        case 0x93: // i2s
            var val = stack.ints[stack.length - 1];
            // No need to set stack.types; int === int
            stack.ints[stack.length - 1] = (val << 16) >> 16;
            break;
        default:
        return false;
    }
    return true; // handled
}

VM.handleComplexOp = function(op, ctx, frame, cp, stack) {
    switch(op) {
        case 0xaa: // tableswitch
            var startip = frame.ip;
            while ((frame.ip & 3) != 0) {
                frame.ip++;
            }
            var def = frame.read32signed();
            var low = frame.read32signed();
            var high = frame.read32signed();
            var val = stack.ints[--stack.length];
            var jmp;
            if (val < low || val > high) {
                jmp = def;
            } else {
                frame.ip  += (val - low) << 2;
                jmp = frame.read32signed();
            }
            frame.ip = startip - 1 + jmp;
            break;
        case 0xab: // lookupswitch
            var startip = frame.ip;
            while ((frame.ip & 3) != 0)
                frame.ip++;
            var jmp = frame.read32signed();
            var size = frame.read32();
            var val = frame.stack.ints[--stack.length];
          lookup:
            for (var i=0; i<size; i++) {
                var key = frame.read32signed();
                var offset = frame.read32signed();
                if (key === val) {
                    jmp = offset;
                }
                if (key >= val) {
                    break lookup;
                }
            }
            frame.ip = startip - 1 + jmp;
            break;
        case 0xbc: // newarray
            var type = frame.code[frame.ip++];
            var size = stack.ints[--stack.length];
            if (size < 0) {
                ctx.raiseExceptionAndYield("java/lang/NegativeArraySizeException", size);
            }
            stack.pushRef(util.newPrimitiveArray("????ZCFDBSIJ"[type], size));
            break;
        case 0xbd: // anewarray
            var idx = frame.read16();
            var classInfo = cp.resolve(ctx, idx).value;
            var size = stack.ints[--stack.length];
            if (size < 0) {
                ctx.raiseExceptionAndYield("java/lang/NegativeArraySizeException", size);
            }
            var className = classInfo.className;
            if (className[0] !== "[")
                className = "L" + className + ";";
            className = "[" + className;
            stack.pushRef(util.newArray(className, size));
            break;
        case 0xc5: // multianewarray
            var idx = frame.read16();
            var classInfo = cp.resolve(ctx, idx).value;
            var dimensions = frame.code[frame.ip++];
            var lengths = new Array(dimensions);
            for (var i=0; i<dimensions; i++)
                lengths[i] = stack.ints[--stack.length];
            stack.pushRef(util.newMultiArray(classInfo.className, lengths.reverse()));
            break;
        case 0xbe: // arraylength
            var obj = stack.refs[--stack.length];
            if (!obj) {
                ctx.raiseExceptionAndYield("java/lang/NullPointerException");
            }
            stack.types[stack.length] = STACK_INT;
            stack.ints[stack.length++] = obj.length;
            break;
        case 0xb4: // getfield
            var idx = frame.read16();
            var field = cp.resolve(ctx, idx).value;
            var obj = stack.refs[--stack.length];
            if (!obj) {
                ctx.raiseExceptionAndYield("java/lang/NullPointerException");
            }
            stack.pushType(field.signature, field.get(obj));
            break;
        case 0xb5: // putfield
            var idx = frame.read16();
            var field = cp.resolve(ctx, idx).value;
            var val = stack.popType(field.signature);
            var obj = stack.refs[--stack.length];
            if (!obj) {
                ctx.raiseExceptionAndYield("java/lang/NullPointerException");
            }
            field.set(obj, val);
            break;
        case 0xb2: // getstatic
            var idx = frame.read16();
            var field = cp.resolve(ctx, idx, true).value;
            classInitCheck(ctx, frame, field.classInfo, frame.ip-3);
            var value = ctx.runtime.getStatic(field);
            if (value === undefined) {
                value = util.defaultValue(field.signature);
            }
            stack.pushType(field.signature, value);
            break;
        case 0xb3: // putstatic
            var idx = frame.read16();
            var field = cp.resolve(ctx, idx, true).value;
            classInitCheck(ctx, frame, field.classInfo, frame.ip-3);
            ctx.runtime.setStatic(field, stack.popType(field.signature));
            break;
        case 0xbb: // new
            var idx = frame.read16();
            var classInfo = cp.resolve(ctx, idx).value;
            classInitCheck(ctx, frame, classInfo, frame.ip-3);
            stack.pushRef(util.newObject(classInfo));
            break;
        case 0xc0: // checkcast
            var idx = frame.read16();
            var classInfo = cp.resolve(ctx, idx).value;
            var obj = stack.readRef(1);
            if (obj && !obj.class.isAssignableTo(classInfo)) {
                ctx.raiseExceptionAndYield("java/lang/ClassCastException",
                                           obj.class.className + " is not assignable to " +
                                           classInfo.className);
            }
            break;
        case 0xc1: // instanceof
            var idx = frame.read16();
            var classInfo = cp.resolve(ctx, idx).value;
            var obj = stack.refs[--stack.length];
            var result = !obj ? false : obj.class.isAssignableTo(classInfo);
            stack.types[stack.length] = STACK_INT;
            stack.ints[stack.length++] = result ? 1 : 0;
            break;
        case 0xbf: // athrow
            var obj = stack.refs[--stack.length];
            if (!obj) {
                ctx.raiseExceptionAndYield("java/lang/NullPointerException");
            }
            frame = throw_(obj, ctx);
            return frame;
        case 0xc2: // monitorenter
            var obj = stack.refs[--stack.length];
            if (!obj) {
                ctx.raiseExceptionAndYield("java/lang/NullPointerException");
            }
            ctx.monitorEnter(obj);
            break;
        case 0xc3: // monitorexit
            var obj = stack.refs[--stack.length];
            if (!obj) {
                ctx.raiseExceptionAndYield("java/lang/NullPointerException");
            }
            ctx.monitorExit(obj);
            break;
        case 0xc4: // wide
            switch (op = frame.code[frame.ip++]) {
            case 0x15: // iload
                stack.types[stack.length] = STACK_INT;
                stack.ints[stack.length++] = frame.locals.ints[frame.localsBase + frame.read16()];
                break;
            case 0x16: // lload
                stack.pushLong(frame.locals.longs[frame.localsBase + frame.read16()]);
                break;
            case 0x17: // fload
                stack.types[stack.length] = STACK_FLOAT;
                stack.floats[stack.length++] = frame.locals.floats[frame.localsBase + frame.read16()];
                break;
            case 0x18: // dload
                stack.types[stack.length] = stack.types[stack.length + 1] = STACK_DOUBLE;
                stack.doubles[stack.length] = frame.locals.doubles[frame.localsBase + frame.read16()];
                stack.length += 2;
                break;
            case 0x19: // aload
                stack.types[stack.length] = STACK_REF;
                stack.refs[stack.length++] = frame.locals.refs[frame.localsBase + frame.read16()];
                break;
            case 0x36: // istore
                frame.locals.ints[frame.localsBase + frame.read16()] = stack.ints[--stack.length];
                break;
            case 0x37: // lstore
                --stack.length; // remove the dummy long first
                frame.locals.longs[frame.localsBase + frame.read16()] = stack.longs[--stack.length];
                break;
            case 0x38: // fstore
                frame.locals.floats[frame.localsBase + frame.read16()] = stack.floats[--stack.length];
                break;
            case 0x39: // dstore
                --stack.length; // remove the dummy double first
                frame.locals.doubles[frame.localsBase + frame.read16()] = stack.doubles[--stack.length];
                break;
            case 0x3a: // astore
                frame.locals.refs[frame.localsBase + frame.read16()] = stack.refs[--stack.length];
                break;
            case 0x84: // iinc
                var idx = frame.read16();
                frame.locals.ints[frame.localsBase + idx] += frame.read16signed();
                break;
            case 0xa9: // ret
                frame.ip = frame.locals.ints[frame.localsBase + frame.read16()];
                break;
            default:
                var opName = OPCODES[op];
                throw new Error("Wide opcode " + opName + " [" + op + "] not supported.");
            }
            break;
        case 0xb6: // invokevirtual
        case 0xb7: // invokespecial
        case 0xb8: // invokestatic
        case 0xb9: // invokeinterface
            var startip = frame.ip - 1;
            var idx = frame.read16();
            if (op === 0xb9) {
                var argsNumber = frame.code[frame.ip++];
                var zero = frame.code[frame.ip++];
            }
            var isStatic = (op === 0xb8);
            var methodInfoConstant = cp.resolve(ctx, idx, isStatic);
            var methodInfo = methodInfoConstant.value;
            if (!methodInfoConstant.processed) {
                methodInfoConstant.processed = true;
                if (isStatic) {
                    classInitCheck(ctx, frame, methodInfo.classInfo, startip);
                }
            }
            if (!isStatic) {
                var obj = stack.readRef(methodInfo.consumes);
                if (!obj) {
                    ctx.raiseExceptionAndYield("java/lang/NullPointerException");
                }

                switch (op) {
                case OPCODES.invokevirtual:
                case OPCODES.invokeinterface:
                    if (methodInfo.classInfo != obj.class) {
                        // Check if the method is already in the virtual method cache
                        if (obj.class.vmc[methodInfo.key]) {
                          methodInfo = obj.class.vmc[methodInfo.key];
                        } else {
                          methodInfo = CLASSES.getMethod(obj.class, methodInfo.key);
                        }
                    }
                }
            }

            if (VM.DEBUG) {
                VM.trace("invoke", ctx.thread.pid, methodInfo);
            }

            var alternateImpl = methodInfo.alternateImpl;
            if (alternateImpl) {
                Instrument.callPauseHooks(ctx.current());
                Instrument.measure(alternateImpl, ctx, methodInfo);
                Instrument.callResumeHooks(ctx.current());
                break;
            }

            Instrument.callPauseHooks(frame);
            if (!methodInfo.compiled && methodInfo.numCalled >= 10 && !methodInfo.dontCompile) {
                try {
                  methodInfo.compiled = new Function("ctx", VM.compile(methodInfo, ctx, true));
                } catch (e) {
                  methodInfo.dontCompile = true;
                  console.warn("Can't compile function: " + e);
                  console.log("CeERRRRRRRRROMPILED", VM.compile(methodInfo, ctx, true));
                }
            }
            Instrument.callResumeHooks(frame);

            frame = pushFrame(ctx, methodInfo);

            if (methodInfo.compiled) {
              frame = methodInfo.compiled(ctx);
            }
            return frame;
        case 0xb1: // return
            if (VM.DEBUG) {
                VM.trace("return", ctx.thread.pid, frame.methodInfo);
            }
            if (ctx.frames.length == 1)
                return;
            frame.methodInfo.numCalled++;
            frame = popFrame(ctx, frame, -1);
            return frame;
        case 0xac: // ireturn
        case 0xad: // lreturn
        case 0xae: // freturn
        case 0xaf: // dreturn
        case 0xb0: // areturn
            var returnType = INSTR_TO_STACK_STORAGE[op - 0xac];
            if (VM.DEBUG) {
                VM.trace("return", ctx.thread.pid, frame.methodInfo);
            }
            if (ctx.frames.length == 1)
                return null;
            frame.methodInfo.numCalled++;
            frame = popFrame(ctx, frame, returnType);
            return frame;
        default:
            var opName = OPCODES[op];
            throw new Error("Opcode " + opName + " [" + op + "] not supported. (" + ctx.thread.pid + ")");
    }
    return frame;
}

VM.compile = function(methodInfo, ctx, debugStmtEnabled) {
  var depth = 0, maxDepth = 0;
  var locals = 0, maxLocals = 0;
  var ip = 0;

  var frame = new Frame(methodInfo);
  var cp = frame.cp;

  var targetIPs = new Set([0]);
  var stackLayout = new Map();
  var generatedCases = [];

  function generateStackPush(type, val) {
    var gen = " /* push */  ST" + depth + " = " + type + ", S" + depth + " = " + val + ";\n";
    depth++;
    if (type === STACK_LONG || type === STACK_DOUBLE) {
      depth++;
    }
    if (depth > maxDepth) {
      maxDepth = depth;
    }
    return gen;
  }

  function generateStackPop(type) {
    if (type === STACK_LONG || type === STACK_DOUBLE) {
      depth--;
    }
    return "/* pop */ S" + (--depth);
  }

  function generateStackPopWord() {
    var idx = --depth;
    return "[ST" + idx + ", S" + idx + "]";
  }

  function generateStackPushWord(word) {
    var gen = (
      "ST" + depth + " = " + word + "[0];" +
      "S" + depth + " = " + word + "[1];");
    depth++;
    return gen;
  }

  function generateSetLocal(type, num, val) {
    if (num > maxLocals) {
      maxLocals = num;
    }
    return (
      "        LT" + num + " = " + type + ";\n" +
      "        L" + num + " = " + val + ";\n"
    );
  }

  function generateGetLocal(type, num) {
    if (num > maxLocals) {
      maxLocals = num;
    }
    return "L" + num;
  }

  function generateIf(jmp, cond) {
    stackLayout.set(jmp, depth);
    targetIPs.add(jmp);
    return "        if (" + cond + ") {\n\
          ip = " + jmp + ";\n\
          continue;\n\
        }\n";
  }

  function generateStoreLocals() {
    var gen = "";
    for (var i = 0; i <= maxLocals; i++) {
      gen += "        frame.setLocal(LT" + i + ", " + i + ", L" + i + ");\n";
    }
    return gen;
  }

  function generateStoreState(ip) {
    targetIPs.add(ip);

    var gen = "        frame.ip = " + ip + ";\n";
    for (var i = 0; i < depth; i++) {
      // NB: Each "S" variable stores one WORD, so only push one, even for doubles/longs.
      gen += "        /* save S" + i + " */ switch(ST" + i + ") {";
      gen += "        case STACK_INT: frame.stack.ints[frame.stack.length++] = S"+i+"; break;";
      gen += "        case STACK_LONG: frame.stack.longs[frame.stack.length++] = S"+i+"; break;";
      gen += "        case STACK_FLOAT: frame.stack.floats[frame.stack.length++] = S"+i+"; break;";
      gen += "        case STACK_DOUBLE: frame.stack.doubles[frame.stack.length++] = S"+i+"; break;";
      gen += "        case STACK_REF: frame.stack.refs[frame.stack.length++] = S"+i+"; break;";
      gen += "        }\n";
    }
    gen += generateStoreLocals();

    return gen + "\n";
  }

  function generateCheckArrayAccess(ip, idx, refArray) {
    return "\
    if (!" + refArray + ") {\n\
      frame.ip = " + ip + "\n" +
      generateStoreLocals() + "\
      ctx.raiseExceptionAndYield('java/lang/NullPointerException');\n\
    }\n\
    if (" + idx + " < 0 || " + idx + " >= " + refArray + ".length) {\n\
      frame.ip = " + ip + "\n" +
      generateStoreLocals() + "\
      ctx.raiseExceptionAndYield('java/lang/ArrayIndexOutOfBoundsException', " + idx + ");\n\
    }\n";
  }

  var exception_table = frame.methodInfo.exception_table;
  var handlerIPs = new Set();
  for (var i = 0; exception_table && i < exception_table.length; i++) {
    targetIPs.add(exception_table[i].handler_pc);
    handlerIPs.add(exception_table[i].handler_pc);
    // On handler IPs, an exception is put on the stack
    stackLayout.set(exception_table[i].handler_pc, 1);
  }

  while (frame.ip !== frame.code.byteLength) {
    ip = frame.ip;

    var op = frame.read8();

    var code = "";

    if (debugStmtEnabled) {
      code += "      // " + OPCODES[op] + " [0x" + op.toString(16) + "]\n";
      // code += "console.log(OPCODES["+op+']);';
      // code += "console.log(OPCODES["+op+'], ' +
      //   '(typeof S0 !== "undefined" && S0 || "-"),' +
      //   '(typeof S1 !== "undefined" && S1 || "-"),' +
      //   '(typeof S2 !== "undefined" && S2 || "-"),' +
      //   '(typeof S3 !== "undefined" && S3 || "-"),' +
      //   '(typeof S4 !== "undefined" && S4 || "-"),' +
      //   '(typeof S5 !== "undefined" && S5 || "-"),' +
      //   '(typeof S6 !== "undefined" && S6 || "-"),' +
      //   '(typeof S7 !== "undefined" && S7 || "-"),' +
      //   '(typeof S8 !== "undefined" && S8 || "-"),' +
      //   '(typeof S9 !== "undefined" && S9 || "-")' +
      //   ');\n';
      // code += "console.log(OPCODES["+op+'], "LOCALS = ",' +
      //   '(typeof L0 !== "undefined" && L0 || "-"),' +
      //   '(typeof L1 !== "undefined" && L1 || "-"),' +
      //   '(typeof L2 !== "undefined" && L2 || "-"),' +
      //   '(typeof L3 !== "undefined" && L3 || "-"),' +
      //   '(typeof L4 !== "undefined" && L4 || "-"),' +
      //   '(typeof L5 !== "undefined" && L5 || "-"),' +
      //   '(typeof L6 !== "undefined" && L6 || "-"),' +
      //   '(typeof L7 !== "undefined" && L7 || "-"),' +
      //   '(typeof L8 !== "undefined" && L8 || "-"),' +
      //   '(typeof L9 !== "undefined" && L9 || "-")' +
      //   ');\n';
    }

    var newDepth = stackLayout.get(ip);
    if (typeof newDepth !== "undefined") {
      depth = newDepth;
    }

    switch (op) {
      case 0x00: // nop
        break;
      case 0x01: // aconst_null
        code += generateStackPush(STACK_REF, "null");
        break;
      case 0x02: // iconst_m1
        code += generateStackPush(STACK_INT, -1);
        break;
      case 0x03: // iconst_0
        code += generateStackPush(STACK_INT, 0);
        break;
      case 0x0b: // fconst_0
        code += generateStackPush(STACK_FLOAT, 0);
        break;
      case 0x0e: // dconst_0
        code += generateStackPush(STACK_DOUBLE, 0);
        break;
      case 0x04: // iconst_1
        code += generateStackPush(STACK_INT, 1);
        break;
      case 0x0c: // fconst_1
        code += generateStackPush(STACK_FLOAT, 1);
        break;
      case 0x0f: // dconst_1
        code += generateStackPush(STACK_DOUBLE, 1);
        break;
      case 0x05: // iconst_2
        code += generateStackPush(STACK_INT, 2);
        break;
      case 0x0d: // fconst_2
        code += generateStackPush(STACK_FLOAT, 2);
        break;
      case 0x06: // iconst_3
        code += generateStackPush(STACK_INT, 3);
        break;
      case 0x07: // iconst_4
        code += generateStackPush(STACK_INT, 4);
        break;
      case 0x08: // iconst_5
        code += generateStackPush(STACK_INT, 5);
        break;
      case 0x09: // lconst_0
        code += generateStackPush(STACK_LONG, "Long.ZERO");
        break;
      case 0x0a: // lconst_1
        code += generateStackPush(STACK_LONG, "Long.ONE");
        break;
      case 0x10: // bipush
        code += generateStackPush(STACK_INT, frame.read8signed());
        break;
      case 0x11: // sipush
        code += generateStackPush(STACK_INT, frame.read16signed());
        break;
      case 0x12: // ldc
      case 0x13: // ldc_w
      case 0x14: // ldc2_w
        var idx = (op === 0x12) ? frame.read8() : frame.read16();
        var constant = cp.resolve(null, idx);
        code += generateStackPush(constant.type, "cp.resolve(null, "+idx+").value");
        break;
      case 0x15: // iload
        code += generateStackPush(STACK_INT, generateGetLocal(STACK_INT, frame.read8()));
        break;
      case 0x17: // fload
        code += generateStackPush(STACK_FLOAT, generateGetLocal(STACK_FLOAT, frame.read8()));
        break;
      case 0x19: // aload
        code += generateStackPush(STACK_REF, generateGetLocal(STACK_REF, frame.read8()));
        break;
      case 0x16: // lload
        code += generateStackPush(STACK_LONG, generateGetLocal(STACK_LONG, frame.read8()));
        break;
      case 0x18: // dload
        code += generateStackPush(STACK_DOUBLE, generateGetLocal(STACK_DOUBLE, frame.read8()));
        break;
      case 0x1a: // iload_0
      case 0x1b: // iload_1
      case 0x1c: // iload_2
      case 0x1d: // iload_3
        code += generateStackPush(STACK_INT, generateGetLocal(STACK_INT, op - 0x1a));
        break;
      case 0x1e: // lload_0
      case 0x1f: // lload_1
      case 0x20: // lload_2
      case 0x21: // lload_3
        code += generateStackPush(STACK_LONG, generateGetLocal(STACK_LONG, op - 0x1e));
        break;
      case 0x22: // fload_0
      case 0x23: // fload_1
      case 0x24: // fload_2
      case 0x25: // fload_3
        code += generateStackPush(STACK_FLOAT, generateGetLocal(STACK_FLOAT, op - 0x22));
        break;
      case 0x26: // dload_0
      case 0x27: // dload_1
      case 0x28: // dload_2
      case 0x29: // dload_3
        code += generateStackPush(STACK_DOUBLE, generateGetLocal(STACK_DOUBLE, op - 0x26));
        break;
      case 0x2a: // aload_0
      case 0x2b: // aload_1
      case 0x2c: // aload_2
      case 0x2d: // aload_3
        code += generateStackPush(STACK_REF, generateGetLocal(STACK_REF, op - 0x2a));
        break;
      case 0x2e: // iaload
      case 0x2f: // laload
      case 0x30: // faload
      case 0x31: // daload
      case 0x32: // aaload
      case 0x33: // baload
      case 0x34: // caload
      case 0x35: // saload
        var type = INSTR_TO_STACK_STORAGE[op - 0x2e];
        var idx = generateStackPop(STACK_INT);
        var refArray = generateStackPop(STACK_REF);
        code += generateCheckArrayAccess(ip, idx, refArray) +
                generateStackPush(type, refArray + "[" + idx + "]");
        break;
      case 0x36: // istore
      case 0x37: // lstore
      case 0x38: // fstore
      case 0x39: // dstore
      case 0x3a: // astore
        var type = INSTR_TO_STACK_STORAGE[op - 0x36];
        code += generateSetLocal(type, frame.read8(), generateStackPop(type));
        break;
      case 0x3b: // istore_0
      case 0x3c: // istore_1
      case 0x3d: // istore_2
      case 0x3e: // istore_3
        code += generateSetLocal(STACK_INT, op - 0x3b, generateStackPop(STACK_INT));
        break;
      case 0x43: // fstore_0
      case 0x44: // fstore_1
      case 0x45: // fstore_2
      case 0x46: // fstore_3
        code += generateSetLocal(STACK_FLOAT, op - 0x43, generateStackPop(STACK_FLOAT));
        break;
      case 0x4b: // astore_0
      case 0x4c: // astore_1
      case 0x4d: // astore_2
      case 0x4e: // astore_3
        code += generateSetLocal(STACK_REF, op - 0x4b, generateStackPop(STACK_REF));
        break;
      case 0x3f: // lstore_0
      case 0x40: // lstore_1
      case 0x41: // lstore_2
      case 0x42: // lstore_3
        code += generateSetLocal(STACK_LONG, op - 0x3f, generateStackPop(STACK_LONG));
        break;
      case 0x47: // dstore_0
      case 0x48: // dstore_1
      case 0x49: // dstore_2
      case 0x4a: // dstore_3
        code += generateSetLocal(STACK_DOUBLE, op - 0x47, generateStackPop(STACK_DOUBLE));
        break;
      case 0x4f: // iastore
      case 0x50: // lastore
      case 0x51: // fastore
      case 0x52: // dastore
      case 0x53: // aastore
      case 0x54: // bastore
      case 0x55: // castore
      case 0x56: // sastore
        var type = INSTR_TO_STACK_STORAGE[op - 0x4f];
        var val = generateStackPop(type);
        var idx = generateStackPop(STACK_INT);
        var refArray = generateStackPop(STACK_REF);
        code += generateCheckArrayAccess(ip, idx, refArray);
        if (type === STACK_REF) {
          code += "if (" + val + " && !" + val + ".class.isAssignableTo(" +
            refArray + ".class.elementClass)) {\n\
            ctx.raiseExceptionAndYield('java/lang/ArrayStoreException');\n\
          }\n";
        }
        code += refArray + "[" + idx + "] = " + val + ";\n";
        break;
      case 0x57: // pop
        depth--;
        break;
      case 0x58: // pop2
        depth -= 2;
        break;
      case 0x59: // dup
        code += "var a = " + generateStackPopWord() + ";\n";
        code += generateStackPushWord("a");
        code += generateStackPushWord("a");
        break;
      case 0x5a: // dup_x1
        code += "var a = " + generateStackPopWord() + ";\n";
        code += "var b = " + generateStackPopWord() + ";\n";
        code += generateStackPushWord("a");
        code += generateStackPushWord("b");
        code += generateStackPushWord("a");
        break;
      case 0x5b: // dup_x2
        code += "var a = " + generateStackPopWord() + ";\n";
        code += "var b = " + generateStackPopWord() + ";\n";
        code += "var c = " + generateStackPopWord() + ";\n";
        code += generateStackPushWord("a");
        code += generateStackPushWord("c");
        code += generateStackPushWord("b");
        code += generateStackPushWord("a");
        break;
      case 0x5c: // dup2
        code += "var a = " + generateStackPopWord() + ";\n";
        code += "var b = " + generateStackPopWord() + ";\n";
        code += generateStackPushWord("b");
        code += generateStackPushWord("a");
        code += generateStackPushWord("b");
        code += generateStackPushWord("a");
        break;
      case 0x5d: // dup2_x1
        code += "var a = " + generateStackPopWord() + ";\n";
        code += "var b = " + generateStackPopWord() + ";\n";
        code += "var c = " + generateStackPopWord() + ";\n";
        code += generateStackPushWord("b");
        code += generateStackPushWord("a");
        code += generateStackPushWord("c");
        code += generateStackPushWord("b");
        code += generateStackPushWord("a");
        break;
      case 0x5e: // dup2_x2
        code += "var a = " + generateStackPopWord() + ";\n";
        code += "var b = " + generateStackPopWord() + ";\n";
        code += "var c = " + generateStackPopWord() + ";\n";
        code += "var d = " + generateStackPopWord() + ";\n";
        code += generateStackPushWord("b");
        code += generateStackPushWord("a");
        code += generateStackPushWord("d");
        code += generateStackPushWord("c");
        code += generateStackPushWord("b");
        code += generateStackPushWord("a");
        break;
      case 0x5f: // swap
        code += "var a = " + generateStackPopWord() + ";\n";
        code += "var b = " + generateStackPopWord() + ";\n";
        code += generateStackPushWord("a");
        code += generateStackPushWord("b");
        break;
      case 0x84: // iinc
        var idx = frame.read8();
        var val = frame.read8signed();
        code += generateSetLocal(STACK_INT, idx, generateGetLocal(STACK_INT, idx) + " + " + val);
        break;
      case 0x60: // iadd
        code += generateStackPush(STACK_INT, "(" + generateStackPop(STACK_INT) + " + " + generateStackPop(STACK_INT) + ") | 0");
        break;
      case 0x61: // ladd
        code += generateStackPush(STACK_LONG, generateStackPop(STACK_LONG) + ".add(" + generateStackPop(STACK_LONG) + ")");
        break;
      case 0x62: // fadd
        code += generateStackPush(STACK_FLOAT, "Math.fround(" + generateStackPop(STACK_FLOAT) + " + " + generateStackPop(STACK_FLOAT) + ")");
        break;
      case 0x63: // dadd
        code += generateStackPush(STACK_DOUBLE, generateStackPop(STACK_DOUBLE) + " + " + generateStackPop(STACK_DOUBLE));
        break;
      case 0x64: // isub
        code += generateStackPush(STACK_INT, "(-" + generateStackPop(STACK_INT) + " + " + generateStackPop(STACK_INT) + ") | 0");
        break;
      case 0x65: // lsub
        code += generateStackPush(STACK_LONG, generateStackPop(STACK_LONG) + ".negate().add(" + generateStackPop(STACK_LONG) + ")");
        break;
      case 0x66: // fsub
        code += generateStackPush(STACK_FLOAT, "Math.fround(-" + generateStackPop(STACK_FLOAT) + " + " + generateStackPop(STACK_FLOAT) + ")");
        break;
      case 0x67: // dsub
        code += generateStackPush(STACK_DOUBLE, "-" + generateStackPop(STACK_DOUBLE) + " + " + generateStackPop(STACK_DOUBLE));
        break;
      case 0x68: // imul
        code += generateStackPush(STACK_INT, "Math.imul(" + generateStackPop(STACK_INT) + ", " + generateStackPop(STACK_INT) + ")");
        break;
      case 0x69: // lmul
        code += generateStackPush(STACK_LONG, generateStackPop(STACK_LONG) + ".multiply(" + generateStackPop(STACK_LONG) + ")");
        break;
      case 0x6a: // fmul
        code += generateStackPush(STACK_FLOAT, "Math.fround(" + generateStackPop(STACK_FLOAT) + " * " + generateStackPop(STACK_FLOAT) + ")");
        break;
      case 0x6b: // dmul
        code += generateStackPush(generateStackPop(STACK_DOUBLE) + " * " + generateStackPop(STACK_DOUBLE));
        break;
      case 0x6c: // idiv
        var b = generateStackPop(STACK_INT);
        var a = generateStackPop(STACK_INT);
        code += "\
        if (!" + b + ") {\n\
          frame.ip = " + ip + "\n" +
          generateStoreLocals() + "\
          ctx.raiseExceptionAndYield('java/lang/ArithmeticException', '/ by zero');\n\
        }\n";
        code += generateStackPush(STACK_INT, "(" + a + " === util.INT_MIN && " + b + " === -1) ? " + a + " : ((" + a + " / " + b + ")|0)");
        break;
      case 0x6d: // ldiv
        var b = generateStackPop(STACK_LONG);
        var a = generateStackPop(STACK_LONG);
        code += "\
        if (" + b + ".isZero()) {\n\
          frame.ip = " + ip + "\n" +
          generateStoreLocals() + "\
          ctx.raiseExceptionAndYield('java/lang/ArithmeticException', '/ by zero');\n\
        }\n";
        code += generateStackPush(STACK_LONG, a + ".div(" + b + ")");
        break;
      case 0x6e: // fdiv
        var b = generateStackPop(STACK_FLOAT);
        var a = generateStackPop(STACK_FLOAT);
        code += generateStackPush(STACK_FLOAT, "Math.fround(" + a + " / " + b + ")");
        break;
      case 0x6f: // ddiv
        var b = generateStackPop(STACK_DOUBLE);
        var a = generateStackPop(STACK_DOUBLE);
        code += generateStackPush(STACK_DOUBLE, a + " / " + b);
        break;
      case 0x70: // irem
        var b = generateStackPop(STACK_INT);
        var a = generateStackPop(STACK_INT);
        code += "\
        if (!" + b + ") {\n\
          frame.ip = " + ip + "\n" +
          generateStoreLocals() + "\
          ctx.raiseExceptionAndYield('java/lang/ArithmeticException', '/ by zero');\n\
        }\n";
        code += generateStackPush(STACK_INT, a + " % " + b);
        break;
      case 0x71: // lrem
        var b = generateStackPop(STACK_LONG);
        var a = generateStackPop(STACK_LONG);
        code += "\
        if (" + b + ".isZero()) {\n\
          frame.ip = " + ip + "\n" +
          generateStoreLocals() + "\
          ctx.raiseExceptionAndYield('java/lang/ArithmeticException', '/ by zero');\n\
        }\n";
        code += generateStackPush(STACK_LONG, a + ".modulo(" + b + ")");
        break;
      case 0x72: // frem
        var b = generateStackPop(STACK_FLOAT);
        var a = generateStackPop(STACK_FLOAT);
        code += generateStackPush(STACK_FLOAT, "Math.fround(" + a + " % " + b + ")");
        break;
      case 0x73: // drem
        var b = generateStackPop(STACK_DOUBLE);
        var a = generateStackPop(STACK_DOUBLE);
        code += generateStackPush(STACK_DOUBLE, a + " % " + b);
        break;
      case 0x74: // ineg
        code += generateStackPush(STACK_INT, "(-" + generateStackPop(STACK_INT) + ") | 0");
        break;
      case 0x75: // lneg
        code += generateStackPush(STACK_LONG, generateStackPop(STACK_LONG) + ".negate()");
        break;
      case 0x76: // fneg
        code += generateStackPush(STACK_FLOAT, "-" + generateStackPop(STACK_FLOAT));
        break;
      case 0x77: // dneg
        code += generateStackPush(STACK_DOUBLE, "-" + generateStackPop(STACK_DOUBLE));
        break;
      case 0x78: // ishl
        var b = generateStackPop(STACK_INT);
        var a = generateStackPop(STACK_INT);
        code += generateStackPush(STACK_INT, a + " << " + b);
        break;
      case 0x79: // lshl
        var b = generateStackPop(STACK_INT);
        var a = generateStackPop(STACK_LONG);
        code += generateStackPush(STACK_LONG, a + ".shiftLeft(" + b + ")");
        break;
      case 0x7a: // ishr
        var b = generateStackPop(STACK_INT);
        var a = generateStackPop(STACK_INT);
        code += generateStackPush(STACK_INT, a + " >> " + b);
        break;
      case 0x7b: // lshr
        var b = generateStackPop(STACK_INT);
        var a = generateStackPop(STACK_LONG);
        code += generateStackPush(STACK_LONG, a + ".shiftRight(" + b + ")");
        break;
      case 0x7c: // iushr
        var b = generateStackPop(STACK_INT);
        var a = generateStackPop(STACK_INT);
        code += generateStackPush(STACK_INT, a + " >>> " + b);
        break;
      case 0x7d: // lushr
        var b = generateStackPop(STACK_INT);
        var a = generateStackPop(STACK_LONG);
        code += generateStackPush(STACK_LONG, a + ".shiftRightUnsigned(" + b + ")");
        break;
      case 0x7e: // iand
        code += generateStackPush(STACK_INT, generateStackPop(STACK_INT) + " & " + generateStackPop(STACK_INT));
        break;
      case 0x7f: // land
        code += generateStackPush(STACK_LONG, generateStackPop(STACK_LONG) + ".and(" + generateStackPop(STACK_LONG) + ")");
        break;
      case 0x80: // ior
        code += generateStackPush(STACK_INT, generateStackPop(STACK_INT) + " | " + generateStackPop(STACK_INT));
        break;
      case 0x81: // lor
        code += generateStackPush(STACK_LONG, generateStackPop(STACK_LONG) + ".or(" + generateStackPop(STACK_LONG) + ")");
        break;
      case 0x82: // ixor
        code += generateStackPush(STACK_INT, generateStackPop(STACK_INT) + " ^ " + generateStackPop(STACK_INT));
        break;
      case 0x83: // lxor
        code += generateStackPush(STACK_LONG, generateStackPop(STACK_LONG) + ".xor(" + generateStackPop(STACK_LONG) + ")");
        break;
      case 0x94: // lcmp
        var b = generateStackPop(STACK_LONG);
        var a = generateStackPop(STACK_LONG);

        code += "\
        ST" + depth + " = STACK_INT;\n\
        if (" + a + ".greaterThan(" + b + ")) {\n\
          S" + depth + " = 1;\n\
        } else if (" + a + ".lessThan(" + b + ")) {\n\
          S" + depth + " = -1;\n\
        } else {\n\
          S" + depth + " = 0;\n\
        }\n";

        depth += 1;

        break;
      case 0x95: // fcmpl
        var b = generateStackPop(STACK_FLOAT);
        var a = generateStackPop(STACK_FLOAT);

        code += "\
        ST" + depth + " = STACK_INT;\n\
        if (isNaN(" + a + ") || isNaN(" + b + ")) {\n\
          S" + depth + " = -1;\n\
        } else if (" + a + " > " + b + ") {\n\
          S" + depth + " = 1;\n\
        } else if (" + a + " < " + b + "){\n\
          S" + depth + " = -1;\n\
        } else {\n\
          S" + depth + " = 0;\n\
        }\n";

        depth += 1;

        break;
      case 0x96: // fcmpg
        var b = generateStackPop(STACK_FLOAT);
        var a = generateStackPop(STACK_FLOAT);

        code += "\
        ST" + depth + " = STACK_INT;\n\
        if (isNaN(" + a + ") || isNaN(" + b + ")) {\n\
          S" + depth + " = 1;\n\
        } else if (" + a + " > " + b + ") {\n\
          S" + depth + " = 1;\n\
        } else if (" + a + " < " + b + "){\n\
          S" + depth + " = -1;\n\
        } else {\n\
          S" + depth + " = 0;\n\
        }\n";

        depth += 1;

        break;
      case 0x97: // dcmpl
        var b = generateStackPop(STACK_DOUBLE);
        var a = generateStackPop(STACK_DOUBLE);

        code += "\
        ST" + depth + " = STACK_INT;\n\
        if (isNaN(" + a + ") || isNaN(" + b + ")) {\n\
          S" + depth + " = -1;\n\
        } else if (" + a + " > " + b + ") {\n\
          S" + depth + " = 1;\n\
        } else if (" + a + " < " + b + "){\n\
          S" + depth + " = -1;\n\
        } else {\n\
          S" + depth + " = 0;\n\
        }\n";

        depth += 1;

        break;
      case 0x98: // dcmpg
        var b = generateStackPop(STACK_DOUBLE);
        var a = generateStackPop(STACK_DOUBLE);

        code += "\
        ST" + depth + " = STACK_INT;\n\
        if (isNaN(" + a + ") || isNaN(" + b + ")) {\n\
          S" + depth + " = 1;\n\
        } else if (" + a + " > " + b + ") {\n\
          S" + depth + " = 1;\n\
        } else if (" + a + " < " + b + "){\n\
          S" + depth + " = -1;\n\
        } else {\n\
          S" + depth + " = 0;\n\
        }\n";

        depth += 1;

        break;
      case 0x99: // ifeq
        var jmp = ip + frame.read16signed();
        code += generateIf(jmp, generateStackPop(STACK_INT) + " === " + 0);
        break;
      case 0x9a: // ifne
        var jmp = ip + frame.read16signed();
        code += generateIf(jmp, generateStackPop(STACK_INT) + " !== " + 0);
        break;
      case 0x9b: // iflt
        var jmp = ip + frame.read16signed();
        code += generateIf(jmp, generateStackPop(STACK_INT) + " < " + 0);
        break;
      case 0x9c: // ifge
        var jmp = ip + frame.read16signed();
        code += generateIf(jmp, generateStackPop(STACK_INT) + " >= " + 0);
        break;
      case 0x9d: // ifgt
        var jmp = ip + frame.read16signed();
        code += generateIf(jmp, generateStackPop(STACK_INT) + " > " + 0);
        break;
      case 0x9e: // ifle
        var jmp = ip + frame.read16signed();
        code += generateIf(jmp, generateStackPop(STACK_INT) + " <= " + 0);
        break;
      case 0x9f: // if_icmpeq
        var jmp = ip + frame.read16signed();
        code += generateIf(jmp, generateStackPop(STACK_INT) + " === " + generateStackPop(STACK_INT));
        break;
      case 0xa0: // if_cmpne
        var jmp = ip + frame.read16signed();
        code += generateIf(jmp, generateStackPop(STACK_INT) + " !== " + generateStackPop(STACK_INT));
        break;
      case 0xa1: // if_icmplt
        var jmp = ip + frame.read16signed();
        code += generateIf(jmp, generateStackPop(STACK_INT) + " > " + generateStackPop(STACK_INT));
        break;
      case 0xa2: // if_icmpge
        var jmp = ip + frame.read16signed();
        code += generateIf(jmp, generateStackPop(STACK_INT) + " <= " + generateStackPop(STACK_INT));
        break;
      case 0xa3: // if_icmpgt
        var jmp = ip + frame.read16signed();
        code += generateIf(jmp, generateStackPop(STACK_INT) + " < " + generateStackPop(STACK_INT));
        break;
      case 0xa4: // if_icmple
        var jmp = ip + frame.read16signed();
        code += generateIf(jmp, generateStackPop(STACK_INT) + " >= " + generateStackPop(STACK_INT));
        break;
      case 0xa5: // if_acmpeq
        var jmp = ip + frame.read16signed();
        code += generateIf(jmp, generateStackPop(STACK_REF) + " === " + generateStackPop(STACK_REF));
        break;
      case 0xa6: // if_acmpne
        var jmp = ip + frame.read16signed();
        code += generateIf(jmp, generateStackPop(STACK_REF) + " !== " + generateStackPop(STACK_REF));
        break;
      case 0xc6: // ifnull
        var jmp = ip + frame.read16signed();
        code += generateIf(jmp, "!" + generateStackPop(STACK_REF));
        break;
      case 0xc7: // ifnonnull
        var jmp = ip + frame.read16signed();
        code += generateIf(jmp, generateStackPop(STACK_REF));
        break;
      case 0xa7: // goto
        var target = ip + frame.read16signed();
        code += "        ip = " + target + ";\n        continue;\n";
        targetIPs.add(target);
        stackLayout.set(target, depth);
        break;
      case 0xc8: // goto_w
        var target = ip + frame.read32signed();
        code += "        ip = " + target + ";\n        continue;\n";
        targetIPs.add(target);
        stackLayout.set(target, depth);
        break;
      case 0xa8: // jsr
        var jmp = frame.read16();
        targetIPs.add(frame.ip); // ret will return here
        code += generateStackPush(STACK_INT, ip) + "\n\
          ip = " + jmp + ";\n\
          continue;\n";
        targetIPs.add(jmp);
        stackLayout.set(jmp, depth);
        stackLayout.set(frame.ip, depth);
        break;
      case 0xc9: // jsr_w
        var jmp = frame.read32();
        targetIPs.add(frame.ip); // ret will return here
        code += generateStackPush(STACK_INT, ip) + "\n\
          ip = " + jmp + ";\n\
          continue;\n";
        targetIPs.add(jmp);
        stackLayout.set(jmp, depth);
        stackLayout.set(frame.ip, depth);
        break;
      case 0xa9: // ret
        code += "        ip = " + generateGetLocal(STACK_INT, frame.read8()) + ";\n        continue;\n";
        break;
      case 0x85: // i2l
        code += generateStackPush(STACK_LONG, "Long.fromInt(" + generateStackPop(STACK_INT) + ")");
        break;
      case 0x86: // i2f
        code += generateStackPush(STACK_FLOAT, generateStackPop(STACK_INT));
        break;
      case 0x87: // i2d
        code += generateStackPush(STACK_DOUBLE, generateStackPop(STACK_INT));
        break;
      case 0x88: // l2i
        code += generateStackPush(STACK_INT, generateStackPop(STACK_LONG) + ".toInt()");
        break;
      case 0x89: // l2f
        code += generateStackPush(STACK_FLOAT, "Math.fround(" + generateStackPop(STACK_LONG) + ".toNumber())");
        break;
      case 0x8a: // l2d
        code += generateStackPush(STACK_DOUBLE, generateStackPop(STACK_LONG) + ".toNumber()");
        break;
      case 0x8b: // f2i
        code += generateStackPush(STACK_INT, "util.double2int(" + generateStackPop(STACK_FLOAT) + ")")
        break;
      case 0x8c: // f2l
        code += generateStackPush(STACK_LONG, "Long.fromNumber(" + generateStackPop(STACK_FLOAT) + ")")
        break;
      case 0x8d: // f2d
        code += generateStackPush(STACK_DOUBLE, generateStackPop(STACK_FLOAT));
        break;
      case 0x8e: // d2i
        code += generateStackPush(STACK_INT, "util.double2int(" + generateStackPop(STACK_DOUBLE) + ")");
        break;
      case 0x8f: // d2l
        code += generateStackPush(STACK_LONG, "util.double2long(" + generateStackPop(STACK_DOUBLE) + ")");
        break;
      case 0x90: // d2f
        code += generateStackPush(STACK_FLOAT, "Math.fround(" + generateStackPop(STACK_DOUBLE) + ")");
        break;
      case 0x91: // i2b
        code += generateStackPush(STACK_INT, "(" + generateStackPop(STACK_INT) + " << 24) >> 24");
        break;
      case 0x92: // i2c
        code += generateStackPush(STACK_INT, generateStackPop(STACK_INT) + " & 0xffff");
        break;
      case 0x93: // i2s
        code += generateStackPush(STACK_INT, "(" + generateStackPop(STACK_INT) + " << 16) >> 16");
        break;
      case 0xaa: // tableswitch TODO: ADD STACKLAYOUT STUFF
        var startip = frame.ip - 1;

        while ((frame.ip & 3) != 0) {
          frame.ip++;
        }

        var def = frame.read32signed() + startip;
        var low = frame.read32signed();
        var high = frame.read32signed();

        var numJumpOffsets = high - low + 1;

        code += "\
        var offsets = new Int32Array(" + numJumpOffsets + ");\n";
        for (var i = 0; i < numJumpOffsets; i++) {
          var jmp = frame.read32signed() + startip;
          code += "        offsets[" + i + "] = " + jmp + ";\n";
          targetIPs.add(jmp);
        }

        targetIPs.add(def);

        var val = generateStackPop(STACK_INT);

        code += "\
        var jmp;\n\
        if (" + val + " < " + low + " || " + val + " > " + high + ") {\n\
          ip = " + def + ";\n\
        } else {\n\
          ip = offsets[" + val + " - " + low + "];\n\
        }\n\
        continue;\n";
        break;
      case 0xab: // lookupswitch TODO: ADD STACKLAYOUT STUFF
          var startip = frame.ip - 1;

          while ((frame.ip & 3) != 0) {
            frame.ip++;
          }

          var def = frame.read32signed() + startip;
          var size = frame.read32();

          code += "\
          var offsets = {};\n";
          for (var i = 0; i < size; i++) {
            var key = frame.read32signed();
            var jmp = frame.read32signed() + startip;
            code += "        offsets[" + key + "] = " + jmp + ";\n";
            targetIPs.add(jmp);
          }

          targetIPs.add(def);

          var val = generateStackPop(STACK_INT);

          code += "\
          var jmp = offsets[" + val + "];\n\
          if (!jmp) {\n\
            jmp = " + def + "\n\
          }\n\
          ip = jmp;\n\
          continue;\n";
          break;
      case 0xbc: // newarray
        var type = "????ZCFDBSIJ"[frame.read8()];
        var size = generateStackPop(STACK_INT);
        code += "\
        if (" + size + " < 0) {\n\
          frame.ip = " + ip + "\n" +
          generateStoreLocals() + "\
          ctx.raiseExceptionAndYield('java/lang/NegativeArraySizeException', " + size + ");\n\
        }\n" + generateStackPush(STACK_REF, "util.newPrimitiveArray('" + type + "', " + size + ")");
        break;
      case 0xbd: // anewarray
        var idx = frame.read16();

        // Resolve the classinfo in advance.
        var classInfo = cp.resolve(null, idx).value;

        var className = classInfo.className;
        if (className[0] !== "[") {
          className = "L" + className + ";";
        }
        className = "[" + className;

        var size = generateStackPop(STACK_INT);

        code += "\
        if (" + size + " < 0) {\n\
          frame.ip = " + ip + "\n" +
          generateStoreLocals() + "\
          ctx.raiseExceptionAndYield('java/lang/NegativeArraySizeException', " + size + ");\n\
        }\n" + generateStackPush(STACK_REF, "util.newArray('" + className + "', " + size + ")");
        break;
      case 0xc5: // multianewarray
        var idx = frame.read16();

        // Resolve the classinfo in advance.
        var classInfo = cp.resolve(null, idx).value;

        var dimensions = frame.read8();

        code += "        var lengths = new Array(" + dimensions + ");\n";
        for (var i = 0; i < dimensions; i++) {
          code += "        lengths[" + i + "] = " + generateStackPop(STACK_INT) + ";\n";
        }
        code += generateStackPush(STACK_REF, "util.newMultiArray('" + classInfo.className + "', lengths.reverse())");
        break;
      case 0xbe: // arraylength
        var obj = generateStackPop(STACK_REF);

        code += "\
        if (!" + obj + ") {\n\
          frame.ip = " + ip + "\n" +
          generateStoreLocals() + "\
          ctx.raiseExceptionAndYield('java/lang/NullPointerException');\n\
        }\n";
        code += generateStackPush(STACK_INT, obj + ".length");
        break;
      case 0xb4: // getfield
        var idx = frame.read16();

        // Resolve the field in advance.
        var field = cp.resolve(null, idx).value;
        var obj = generateStackPop(STACK_REF);

        code += "\
        if (!" + obj + ") {\n\
          frame.ip = " + ip + "\n" +
          generateStoreLocals() + "\
          ctx.raiseExceptionAndYield('java/lang/NullPointerException');\n\
        }\n";

        var type = Stack.signatureToStackType(field.signature);
        code += generateStackPush(type, "cp.resolve(null, " + idx + ").value.get(" + obj + ")");

        break;
      case 0xb5: // putfield
        var idx = frame.read16();

        // Resolve the field in advance.
        var field = cp.resolve(null, idx).value;

        var type = Stack.signatureToStackType(field.signature);
        var val = generateStackPop(type);
        var obj = generateStackPop(STACK_REF);

        code += "\
        if (!" + obj + ") {\n\
          frame.ip = " + ip + "\n" +
          generateStoreLocals() + "\
          ctx.raiseExceptionAndYield('java/lang/NullPointerException');\n\
        }\n\
        cp.resolve(null, " + idx + ").value.set(" + obj + ", " + val + ");\n"
        break;
      case 0xb2: // getstatic
        var idx = frame.read16();

        // Resolve the field in advance.
        var field = cp.resolve(null, idx, true).value;

        code += "\
        var field = cp.resolve(null, " + idx + ", true).value;\n";

        if (!field.classInfo.isArrayClass) {
          code +="\
        if (!ctx.runtime.initialized[field.classInfo.className]) {\n" +
          generateStoreState(ip) + "\
          ctx.pushClassInitFrame(field.classInfo);\n\
          throw VM.Yield;\n\
        }\n";
        }

        code += "\
        var value = ctx.runtime.getStatic(field);\n\
        if (typeof value === 'undefined') {\n\
          value = util.defaultValue(field.signature);\n\
        }\n";

        var type = Stack.signatureToStackType(field.signature);
        code += generateStackPush(type, "value");

        break;
      case 0xb3: // putstatic
        var idx = frame.read16();

        // Resolve the field in advance.
        var field = cp.resolve(null, idx, true).value;

        code += "\
        var field = cp.resolve(null, " + idx + ", true).value;\n";

        if (!field.classInfo.isArrayClass) {
          code +="\
        if (!ctx.runtime.initialized[field.classInfo.className]) {\n" +
          generateStoreState(ip) + "\
          ctx.pushClassInitFrame(field.classInfo);\n\
          throw VM.Yield;\n\
        }\n";
        }

        var type = Stack.signatureToStackType(field.signature);
        val = generateStackPop(type);

        code += "\
        ctx.runtime.setStatic(field, " + val + ");\n";
        break;
      case 0xbb: // new
        var idx = frame.read16();

        // Resolve class in advance.
        var classInfo = cp.resolve(null, idx).value;

        code += "\
        var classInfo = cp.resolve(null, " + idx + ").value;\n";

        if (!classInfo.isArrayClass) {
          code +="\
        if (!ctx.runtime.initialized[classInfo.className]) {\n" +
          generateStoreState(ip) + "\
          ctx.pushClassInitFrame(classInfo);\n\
          throw VM.Yield;\n\
        }\n";
        }

        code += generateStackPush(STACK_REF, "util.newObject(classInfo)");
        break;
      case 0xc0: // checkcast
        var idx = frame.read16();

        // Resolve class in advance.
        var classInfo = cp.resolve(null, idx).value;
        var obj = "S" + (depth-1);

        code += "\
        var classInfo = cp.resolve(null, " + idx + ").value;\n\
        if (" + obj + ") {\n\
          if (!" + obj + ".class.isAssignableTo(classInfo)) {\n\
            frame.ip = " + ip + "\n" +
            generateStoreLocals() + "\
            ctx.raiseExceptionAndYield('java/lang/ClassCastException',\n\
                                       " + obj + ".class.className + ' is not assignable to ' +\n\
                                       classInfo.className);\n\
            }\n\
        }\n";
        break;
      case 0xc1: // instanceof
        var idx = frame.read16();

        // Resolve class in advance.
        var classInfo = cp.resolve(null, idx);
        var obj = generateStackPop(STACK_REF);

        code += "\
        var classInfo = cp.resolve(null, " + idx + ").value;\n\
        var result = !" + obj + " ? false : " + obj + ".class.isAssignableTo(classInfo);\n" +
        generateStackPush(STACK_INT, "result ? 1 : 0");
        break;
      case 0xbf: // athrow
        var obj = generateStackPop(STACK_REF);

        code += generateStoreState(ip) + "\
        if (!" + obj + ") {\n\
          ctx.raiseExceptionAndYield('java/lang/NullPointerException');\n\
        }\n" + "\
        return throw_(" + obj + ", ctx);\n";
        break;
      case 0xc2: // monitorenter
        var obj = generateStackPop(STACK_REF);

        code += "\
        if (!" + obj + ") {\n\
          frame.ip = " + ip + "\n" +
          generateStoreLocals() + "\
          ctx.raiseExceptionAndYield('java/lang/NullPointerException');\n\
        }\n\
        try {\n\
          ctx.monitorEnter(" + obj + ");\n\
        }\n\
        catch (e) {\n\
          " + generateStoreState(frame.ip) + "\
          throw e;\n\
        }\n";
        break;
      case 0xc3: // monitorexit
        var obj = generateStackPop(STACK_REF);

        code += "\
        if (!" + obj + ") {\n\
          frame.ip = " + ip + "\n" +
          generateStoreLocals() + "\
          ctx.raiseExceptionAndYield('java/lang/NullPointerException');\n\
        }\n\
        ctx.monitorExit(" + obj + ");\n";
        break;
      case 0xc4: // wide
        var op = frame.read8();
        var opName = OPCODES[op];
        code += "// " + opName + " [0x" + op.toString(16) + "]\n";
        switch (op) {
          case 0x15: // iload
            code += generateStackPush(STACK_INT, generateGetLocal(STACK_INT, frame.read16()));
            break;
          case 0x17: // fload
            code += generateStackPush(STACK_FLOAT, generateGetLocal(STACK_FLOAT, frame.read16()));
            break;
          case 0x19: // aload
            code += generateStackPush(STACK_REF, generateGetLocal(STACK_REF, frame.read16()));
            break;
          case 0x16: // lload
            code += generateStackPush(STACK_LONG, generateGetLocal(STACK_LONG, frame.read16()));
            break;
          case 0x18: // dload
            code += generateStackPush(STACK_DOUBLE, generateGetLocal(STACK_DOUBLE, frame.read16()));
            break;
          case 0x36: // istore
          case 0x37: // lstore
          case 0x38: // fstore
          case 0x39: // dstore
          case 0x3a: // astore
            var type = INSTR_TO_STACK_STORAGE[op - 0x36];
            code += generateSetLocal(type, frame.read16(), generateStackPop(type));
            break;
          case 0x84: // iinc
            var idx = frame.read16();
            var val = frame.read16signed();
            code += generateSetLocal(STACK_INT, idx, generateGetLocal(STACK_INT, idx) + " + " + val);
            break;
          case 0xa9: // ret
            code += "        ip = " + generateGetLocal(STACK_INT, frame.read16()) + ";\n        continue;\n";
            continue;
            break;
          default:
            var opName = OPCODES[op];
            throw new Error("Wide opcode " + opName + " [" + op + "] not supported.");
        }
        break;
      case 0xb6: // invokevirtual
      case 0xb7: // invokespecial
      case 0xb8: // invokestatic
      case 0xb9: // invokeinterface
        var idx = frame.read16();
        if (op === 0xb9) {
          /*var argsNumber =*/ frame.read8();
          /*var zero =*/ frame.read8();
        }
        var isStatic = (op === 0xb8);
        var toCallMethodInfo = cp.resolve(null, idx, isStatic).value;

        code += "        var toCallMethodInfo = cp.resolve(null, " + idx + ").value;\n";
//      code +="console.warn(toCallMethodInfo.implKey);";
        if (isStatic && !toCallMethodInfo.classInfo.isArrayClass) {
            code +="\
          if (!ctx.runtime.initialized[toCallMethodInfo.classInfo.className]) {\n" +
            generateStoreState(ip) + "\
            ctx.pushClassInitFrame(toCallMethodInfo.classInfo);\n\
            throw VM.Yield;\n\
          }\n";
        }

        if (!isStatic) {
          var obj = "S" + (depth - toCallMethodInfo.consumes);

          code += "\
        if (!" + obj + ") {\n\
          frame.ip = " + ip + "\n" +
          generateStoreLocals() + "\
          ctx.raiseExceptionAndYield('java/lang/NullPointerException');\n\
        }\n";

          if (op === 0xb6 || op === 0xb9) {
            code += "\
        if (toCallMethodInfo.classInfo != " + obj + ".class) {\n\
          if (" + obj + ".class.vmc['" + toCallMethodInfo.key + "']) {\n\
            toCallMethodInfo = " + obj + ".class.vmc['" + toCallMethodInfo.key + "'];\n\
          } else {\n\
            toCallMethodInfo = CLASSES.getMethod(" + obj + ".class, '" + toCallMethodInfo.key + "');\n\
          }\n\
        }\n";
          }
        }

        code += generateStoreState(frame.ip);

        depth -= toCallMethodInfo.consumes;

        var alternateImplCall = "\
          Instrument.callPauseHooks(frame);\n\
          Instrument.measure(alternateImpl, ctx, toCallMethodInfo);\n\
          Instrument.callResumeHooks(frame);\n";

        var normalCall = "\
          Instrument.callPauseHooks(frame);\n\
          if (!toCallMethodInfo.compiled && toCallMethodInfo.numCalled >= 10 && !toCallMethodInfo.dontCompile) {\n\
            try {\n\
              toCallMethodInfo.compiled = new Function('ctx', VM.compile(toCallMethodInfo, ctx));\n\
            } catch (e) {\n\
              toCallMethodInfo.dontCompile = true;\n\
              console.warn('Can\\'t compile function: ' + e);\n\
            }\n\
          }\n\
          Instrument.callResumeHooks(frame);\n\
\n\
          var callee = ctx.pushFrame(toCallMethodInfo);\n";
          if (toCallMethodInfo.isSynchronized) {
            normalCall += "\
            if (!callee.lockObject) {\n\
              callee.lockObject = " + (toCallMethodInfo.isStatic
                                         ? "toCallMethodInfo.classInfo.getClassObject(ctx)\n"
                                         : "callee.locals.refs[callee.localsBase + 0]\n") + "\
            }\n\
            ctx.monitorEnter(callee.lockObject);\n";
          }

          normalCall += "\n\
          var newFrame;\n\
\n\
          if (toCallMethodInfo.compiled) {\n\
            newFrame = toCallMethodInfo.compiled(ctx);\n\
          } else {\n\
            newFrame = VM.execute(ctx);\n\
          }\n\
\n\
          if (newFrame !== frame) {\n\
            return newFrame;\n\
          }\n";

        if (op === 0xb6 || op == 0xb9) {
          code += "\
          var alternateImpl = toCallMethodInfo.alternateImpl;\n\
          if (alternateImpl) {\n" +
            alternateImplCall + "\
          } else {\n" +
            normalCall + "\
          }\n";
        } else if (toCallMethodInfo.alternateImpl) {
          code += "\
          var alternateImpl = toCallMethodInfo.alternateImpl;\n" + alternateImplCall;
        } else {
          code += normalCall;
        }

        var returnType = toCallMethodInfo.signature[toCallMethodInfo.signature.length - 1];

        switch(returnType) {
        case "B":
        case "C":
        case "S":
        case "Z":
        case "I": code += generateStackPush(STACK_INT, "frame.stack.ints[--frame.stack.length]"); break;
        case "J": code += generateStackPush(STACK_LONG, "frame.stack.popLong()"); break;
        case "F": code += generateStackPush(STACK_FLOAT, "frame.stack.floats[--frame.stack.length]"); break;
        case "D": code += generateStackPush(STACK_DOUBLE, "frame.stack.popDouble()"); break;
        case "[":
        case "L": code += generateStackPush(STACK_REF, "frame.stack.refs[--frame.stack.length]"); break;
        }

        code += "        frame.stack.length = 0;\n";

        break;
      case 0xb1: // return
        if (methodInfo.isSynchronized) {
          code += "        if (frame.lockObject) {\n\
          ctx.monitorExit(frame.lockObject);\n\
        }\n";
        }

        code += "\
        if (ctx.frames.length == 1)\n\
            return;\n\
        var caller = ctx.popFrame();\n\
        return caller;\n";
        break;
      case 0xac: // ireturn
        if (methodInfo.isSynchronized) {
          code += "        if (frame.lockObject) {\n\
          ctx.monitorExit(frame.lockObject);\n\
        }\n";
        }

        code += "\
        if (ctx.frames.length == 1)\n\
            return;\n\
        var caller = ctx.popFrame();\n\
        caller.stack.pushInt(" + generateStackPop(STACK_INT) + ");\n\
        return caller;\n";
        break;
      case 0xae: // freturn
        if (methodInfo.isSynchronized) {
          code += "        if (frame.lockObject) {\n\
          ctx.monitorExit(frame.lockObject);\n\
        }\n";
        }

        code += "\
        if (ctx.frames.length == 1)\n\
            return;\n\
        var caller = ctx.popFrame();\n\
        caller.stack.pushFloat(" + generateStackPop(STACK_FLOAT) + ");\n\
        return caller;\n";
        break;
      case 0xb0: // areturn
        if (methodInfo.isSynchronized) {
          code += "        if (frame.lockObject) {\n\
          ctx.monitorExit(frame.lockObject);\n\
        }\n";
        }

        code += "\
            return;\n\
        var caller = ctx.popFrame();\n\
        caller.stack.pushRef(" + generateStackPop(STACK_REF) + ");\n\
        return caller;\n";
        break;
      case 0xad: // lreturn
        if (methodInfo.isSynchronized) {
          code += "        if (frame.lockObject) {\n\
          ctx.monitorExit(frame.lockObject);\n\
        }\n";
        }

        code += "\
        if (ctx.frames.length == 1)\n\
            return;\n\
        var caller = ctx.popFrame();\n\
        caller.stack.pushLong(" + generateStackPop(STACK_LONG) + ");\n\
        return caller;\n";
        break;
      case 0xaf: // dreturn
        if (methodInfo.isSynchronized) {
          code += "        if (frame.lockObject) {\n\
          ctx.monitorExit(frame.lockObject);\n\
        }\n";
        }

        code += "\
        if (ctx.frames.length == 1)\n\
            return;\n\
        var caller = ctx.popFrame();\n\
        caller.stack.pushDouble(" + generateStackPop(STACK_DOUBLE) + ");\n\
        return caller;\n";
        break;
      default:
        throw new Error("NOT SUPPORTED: " + op.toString(16));
    }

    generatedCases.push({
      ip: ip,
      code: code,
    });
  }

  var localsCode = "";
  for (var i = 0; i <= maxLocals; i++) {
    localsCode += "  var LT" + i + " = frame.locals.types[frame.localsBase + " + i + "];\n";
    localsCode += "  var L" + i + " = frame.getLocal(LT" + i + ", " + i + ");\n";
  }
  if (maxDepth > 0) {
    localsCode += "  var";
    for (var i = 0; i < maxDepth; i++) {
      localsCode += " ST" + i + ",";
      localsCode += " S" + i + ",";
    }
    localsCode = localsCode.slice(0, -1) + ";\n";
  }

  var generatedCode = "\n  var frame = ctx.current();\n\
  var cp = frame.cp;\n" + localsCode + "\
  var ip = frame.ip;\n\
\n\
  if (ip !== 0) {\n";

  for (var i = 0; i < maxDepth; i++) {
//    generatedCode += "if (frame.stack.length > 0) {\n";
    generatedCode += "  /* store S" + i + " */ ";
    generatedCode += "  ST" + i + " = frame.stack.types[" + i + "];";
    generatedCode += "  switch(ST" + i + ") {";
    generatedCode += "  case STACK_INT: S" + i + " = frame.stack.ints["+i+"]; break;";
    generatedCode += "  case STACK_LONG: S" + i + " = frame.stack.longs["+i+"]; break;";
    generatedCode += "  case STACK_FLOAT: S" + i + " = frame.stack.floats["+i+"]; break;";
    generatedCode += "  case STACK_DOUBLE: S" + i + " = frame.stack.doubles["+i+"]; break;";
    generatedCode += "  case STACK_REF: S" + i + " = frame.stack.refs["+i+"]; break;";
    generatedCode += "  }\n";
//    generatedCode += "}\n";
  }
  generatedCode += "  frame.stack.length = 0;\n";

  generatedCode += "  }\n\n\
  while (true) {\n\
    switch (ip) {\n";

  generatedCases.forEach(function(aCase) {
    if (targetIPs.has(aCase.ip)) {
      generatedCode += "case " + aCase.ip + ":\n";
    }

    generatedCode += aCase.code;
  });

  generatedCode += "      default:\n\
        console.log('IP: ' + ip);\n";

  generatedCode += "    }\n";
  generatedCode += "  }\n";

  return generatedCode;
}
