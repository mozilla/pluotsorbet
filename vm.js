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
            // Same as `frame.ip = frame.getLocalInt(frame.read8());`
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

            // Instrument.callPauseHooks(frame);
            // if (!methodInfo.compiled && methodInfo.numCalled >= 100 && !methodInfo.dontCompile) {
            //     try {
            //       methodInfo.compiled = new Function("ctx", VM.compile(methodInfo, ctx));
            //     } catch (e) {
            //       methodInfo.dontCompile = true;
            //       console.warn("Can't compile function: " + e);
            //     }
            // }
            // Instrument.callResumeHooks(frame);

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

// VM.compile = function(methodInfo, ctx, debugStmtEnabled) {
//   var depth = 0, maxDepth = 0;
//   var locals = 0, maxLocals = 0;
//   var ip = 0;

//   var frame = new Frame(methodInfo);
//   var cp = frame.cp;

//   var targetIPs = new Set([0]);
//   var stackLayout = new Map();
//   var generatedCases = [];

//   function generateStackPush(val) {
//     var gen = "        S" + (depth++) + " = " + val + ";\n";
//     if (depth > maxDepth) {
//       maxDepth = depth;
//     }
//     return gen;
//   }

//   function generateStackPush2(val) {
//     var gen = "        S" + (depth++) + " = " + val + ";\n        S" + (depth++) + " = null;\n";
//     if (depth > maxDepth) {
//       maxDepth = depth;
//     }
//     return gen;
//   }

//   function generateStackPop() {
//     return "S" + (--depth);
//   }

//   function generateStackPop2() {
//     --depth;
//     return "S" + (--depth);
//   }

//   function generateSetLocal(num, val) {
//     if (num > maxLocals) {
//       maxLocals = num;
//     }
//     return "        L" + num + " = " + val + ";\n";
//   }

//   function generateGetLocal(num) {
//     if (num > maxLocals) {
//       maxLocals = num;
//     }
//     return "L" + num;
//   }

//   function generateIf(jmp, cond) {
//     stackLayout.set(jmp, depth);
//     targetIPs.add(jmp);
//     return "        if (" + cond + ") {\n\
//           ip = " + jmp + ";\n\
//           continue;\n\
//         }\n";
//   }

//   function generateStoreLocals() {
//     var gen = "";
//     for (var i = 0; i <= maxLocals; i++) {
//       gen += "        frame.setLocal(" + i + ", L" + i + ");\n";
//     }
//     return gen;
//   }

//   function generateStoreState(ip) {
//     targetIPs.add(ip);

//     var gen = "        frame.ip = " + ip + ";\n";
//     for (var i = 0; i < depth; i++) {
//       gen += "        frame.stack.push(S" + i + ");\n";
//     }
//     gen += generateStoreLocals();

//     return gen + "\n";
//   }

//   function generateCheckArrayAccess(ip, idx, refArray) {
//     return "\
//     if (!" + refArray + ") {\n\
//       frame.ip = " + ip + "\n" +
//       generateStoreLocals() + "\
//       ctx.raiseExceptionAndYield('java/lang/NullPointerException');\n\
//     }\n\
//     if (" + idx + " < 0 || " + idx + " >= " + refArray + ".length) {\n\
//       frame.ip = " + ip + "\n" +
//       generateStoreLocals() + "\
//       ctx.raiseExceptionAndYield('java/lang/ArrayIndexOutOfBoundsException', " + idx + ");\n\
//     }\n";
//   }

//   function resolveCompiled(cp, idx, isStatic) {
//       var constant = cp[idx];

//       if (!constant.tag)
//         return constant;

//       switch(constant.tag) {
//         case 3: // TAGS.CONSTANT_Integer
//           constant = constant.integer;
//           break;
//         case 4: // TAGS.CONSTANT_Float
//           constant = constant.float;
//           break;
//         case 8: // TAGS.CONSTANT_String
//           constant = util.newString(cp[constant.string_index].bytes);
//           break;
//         case 5: // TAGS.CONSTANT_Long
//           constant = Long.fromBits(constant.lowBits, constant.highBits);
//           break;
//         case 6: // TAGS.CONSTANT_Double
//           constant = constant.double;
//           break;
//         case 7: // TAGS.CONSTANT_Class
//           constant = CLASSES.getClass(cp[constant.name_index].bytes);
//           break;
//         case 9: // TAGS.CONSTANT_Fieldref
//           var classInfo = resolveCompiled(cp, constant.class_index, isStatic);
//           var fieldName = cp[cp[constant.name_and_type_index].name_index].bytes;
//           var signature = cp[cp[constant.name_and_type_index].signature_index].bytes;
//           constant = CLASSES.getField(classInfo, (isStatic ? "S." : "I.") + fieldName + "." + signature);
//           if (!constant) {
//             throw new Error("java/lang/RuntimeException");
//           }
//           break;
//         case 10: // TAGS.CONSTANT_Methodref
//         case 11: // TAGS.CONSTANT_InterfaceMethodref
//           var classInfo = resolveCompiled(cp, constant.class_index, isStatic);
//           var methodName = cp[cp[constant.name_and_type_index].name_index].bytes;
//           var signature = cp[cp[constant.name_and_type_index].signature_index].bytes;
//           constant = CLASSES.getMethod(classInfo, (isStatic ? "S." : "I.") + methodName + "." + signature);
//           if (!constant) {
//             throw new Error("java/lang/RuntimeException");
//           }
//           break;
//         default:
//           throw new Error("not supported constant type");
//       }

//       return cp[idx] = constant;
//   }

//   var exception_table = frame.methodInfo.exception_table;
//   var handlerIPs = new Set();
//   for (var i = 0; exception_table && i < exception_table.length; i++) {
//     targetIPs.add(exception_table[i].handler_pc);
//     handlerIPs.add(exception_table[i].handler_pc);
//     // On handler IPs, an exception is put on the stack
//     stackLayout.set(exception_table[i].handler_pc, 1);
//   }

//   while (frame.ip !== frame.code.byteLength) {
//     ip = frame.ip;

//     var op = frame.read8();

//     var code = "";

//     if (debugStmtEnabled) {
//       code += "      // " + OPCODES[op] + " [0x" + op.toString(16) + "]\n";
//     }

//     var newDepth = stackLayout.get(ip);
//     if (typeof newDepth !== "undefined") {
//       depth = newDepth;
//     }

//     switch (op) {
//       case 0x00: // nop
//         break;
//       case 0x01: // aconst_null
//         code += generateStackPush("null");
//         break;
//       case 0x02: // aconst_m1
//         code += generateStackPush(-1);
//         break;
//       case 0x03: // iconst_0
//       case 0x0b: // fconst_0
//         code += generateStackPush(0);
//         break;
//       case 0x0e: // dconst_0
//         code += generateStackPush2(0);
//         break;
//       case 0x04: // iconst_1
//       case 0x0c: // fconst_1
//         code += generateStackPush(1);
//         break;
//       case 0x0f: // dconst_1
//         code += generateStackPush2(1);
//         break;
//       case 0x05: // iconst_2
//       case 0x0d: // fconst_2
//         code += generateStackPush(2);
//         break;
//       case 0x06: // iconst_3
//         code += generateStackPush(3);
//         break;
//       case 0x07: // iconst_4
//         code += generateStackPush(4);
//         break;
//       case 0x08: // iconst_5
//         code += generateStackPush(5);
//         break;
//       case 0x09: // lconst_0
//         code += generateStackPush2("Long.fromInt(0)");
//         break;
//       case 0x0a: // lconst_1
//         code += generateStackPush2("Long.fromInt(1)");
//         break;
//       case 0x10: // bipush
//         code += generateStackPush(frame.read8signed());
//         break;
//       case 0x11: // sipush
//         code += generateStackPush(frame.read16signed());
//         break;
//       case 0x12: // ldc
//       case 0x13: // ldc_w
//         var idx = (op === 0x12) ? frame.read8() : frame.read16();

//         // Resolve the constant in advance.
//         var constant = cp[idx];

//         if (constant.tag) {
//           constant = resolveCompiled(cp, idx);
//         }

//         code += generateStackPush("cp[" + idx + "]");
//         break;
//       case 0x14: // ldc2_w
//         var idx = frame.read16();

//         // Resolve the constant in advance.
//         var constant = cp[idx];
//         if (constant.tag) {
//           constant = resolveCompiled(cp, idx);
//         }

//         code += generateStackPush2("cp[" + idx + "]");
//         break;
//       case 0x15: // iload
//       case 0x17: // fload
//       case 0x19: // aload
//         code += generateStackPush(generateGetLocal(frame.read8()));
//         break;
//       case 0x16: // lload
//       case 0x18: // dload
//         code += generateStackPush2(generateGetLocal(frame.read8()));
//         break;
//       case 0x1a: // iload_0
//       case 0x22: // fload_0
//       case 0x2a: // aload_0
//         code += generateStackPush(generateGetLocal(0));
//         break;
//       case 0x1b: // iload_1
//       case 0x23: // fload_1
//       case 0x2b: // aload_1
//         code += generateStackPush(generateGetLocal(1));
//         break;
//       case 0x1c: // iload_2
//       case 0x24: // fload_2
//       case 0x2c: // aload_2
//         code += generateStackPush(generateGetLocal(2));
//         break;
//       case 0x1d: // iload_3
//       case 0x25: // fload_3
//       case 0x2d: // aload_3
//         code += generateStackPush(generateGetLocal(3));
//         break;
//       case 0x1e: // lload_0
//       case 0x26: // dload_0
//         code += generateStackPush2(generateGetLocal(0));
//         break;
//       case 0x1f: // lload_1
//       case 0x27: // dload_1
//         code += generateStackPush2(generateGetLocal(1));
//         break;
//       case 0x20: // lload_2
//       case 0x28: // dload_2
//         code += generateStackPush2(generateGetLocal(2));
//         break;
//       case 0x21: // lload_3
//       case 0x29: // dload_3
//         code += generateStackPush2(generateGetLocal(3));
//         break;
//       case 0x2e: // iaload
//       case 0x30: // faload
//       case 0x32: // aaload
//       case 0x33: // baload
//       case 0x34: // caload
//       case 0x35: // saload
//         var idx = generateStackPop();
//         var refArray = generateStackPop();
//         code += generateCheckArrayAccess(ip, idx, refArray) +
//                 generateStackPush(refArray + "[" + idx + "]");
//         break;
//       case 0x2f: // laload
//       case 0x31: // daload
//         var idx = generateStackPop();
//         var refArray = generateStackPop();
//         code += generateCheckArrayAccess(ip, idx, refArray) +
//                 generateStackPush2(refArray + "[" + idx + "]");
//         break;
//       case 0x36: // istore
//       case 0x38: // fstore
//       case 0x3a: // astore
//         code += generateSetLocal(frame.read8(), generateStackPop());
//         break;
//       case 0x37: // lstore
//       case 0x39: // dstore
//         code += generateSetLocal(frame.read8(), generateStackPop2());
//         break;
//       case 0x3b: // istore_0
//       case 0x43: // fstore_0
//       case 0x4b: // astore_0
//         code += generateSetLocal(0, generateStackPop());
//         break;
//       case 0x3c: // istore_1
//       case 0x44: // fstore_1
//       case 0x4c: // astore_1
//         code += generateSetLocal(1, generateStackPop());
//         break;
//       case 0x3d: // istore_2
//       case 0x45: // fstore_2
//       case 0x4d: // astore_2
//         code += generateSetLocal(2, generateStackPop());
//         break;
//       case 0x3e: // istore_3
//       case 0x46: // fstore_3
//       case 0x4e: // astore_3
//         code += generateSetLocal(3, generateStackPop());
//         break;
//       case 0x3f: // lstore_0
//       case 0x47: // dstore_0
//         code += generateSetLocal(0, generateStackPop2());
//         break;
//       case 0x40: // lstore_1
//       case 0x48: // dstore_1
//         code += generateSetLocal(1, generateStackPop2());
//         break;
//       case 0x41: // lstore_2
//       case 0x49: // dstore_2
//         code += generateSetLocal(2, generateStackPop2());
//         break;
//       case 0x42: // lstore_3
//       case 0x4a: // dstore_3
//         code += generateSetLocal(3, generateStackPop2());
//         break;
//       case 0x4f: // iastore
//       case 0x51: // fastore
//       case 0x54: // bastore
//       case 0x55: // castore
//       case 0x56: // sastore
//         var val = generateStackPop();
//         var idx = generateStackPop();
//         var refArray = generateStackPop();
//         code += generateCheckArrayAccess(ip, idx, refArray) + "\
//         " + refArray + "[" + idx + "] = " + val + ";\n";
//         break;
//       case 0x50: // lastore
//       case 0x52: // dastore
//         var val = generateStackPop2();
//         var idx = generateStackPop();
//         var refArray = generateStackPop();
//         code += generateCheckArrayAccess(ip, idx, refArray) + "\
//         " + refArray + "[" + idx + "] = " + val + ";\n";
//         break;
//       case 0x53: // aastore
//         var val = generateStackPop();
//         var idx = generateStackPop();
//         var refArray = generateStackPop();
//         code += generateCheckArrayAccess(ip, idx, refArray) + "\
//         if (" + val + " && !" + val + ".class.isAssignableTo(" + refArray + ".class.elementClass)) {\n\
//           ctx.raiseExceptionAndYield('java/lang/ArrayStoreException');\n\
//         }\n\
//         " + refArray + "[" + idx + "] = " + val + ";\n";
//         break;
//       case 0x57: // pop
//         depth--;
//         break;
//       case 0x58: // pop2
//         depth -= 2;
//         break;
//       case 0x59: // dup
//         code += generateStackPush("S" + (depth - 1));
//         break;
//       case 0x5a: // dup_x1
//         var a = generateStackPop();
//         var b = generateStackPop();
//         code += "var tmp = " + b + "\n" +
//                 generateStackPush(a) +
//                 generateStackPush("tmp") +
//                 generateStackPush(b);
//         break;
//       case 0x5b: // dup_x2
//         code += "\
//         var a = " + generateStackPop() + ";\n\
//         var b = " + generateStackPop() + ";\n\
//         var c = " + generateStackPop() + ";\n";
//         code += generateStackPush("a") +
//                 generateStackPush("c") +
//                 generateStackPush("b") +
//                 generateStackPush("a");
//         break;
//       case 0x5c: // dup2
//         code += "\
//         var a = " + generateStackPop() + ";\n\
//         var b = " + generateStackPop() + ";\n";
//         code += generateStackPush("b") +
//                 generateStackPush("a") +
//                 generateStackPush("b") +
//                 generateStackPush("a");
//         break;
//       case 0x5d: // dup2_x1
//         code += "\
//         var a = " + generateStackPop() + ";\n\
//         var b = " + generateStackPop() + ";\n\
//         var c = " + generateStackPop() + ";\n";
//         code += generateStackPush("b") +
//                 generateStackPush("a") +
//                 generateStackPush("c") +
//                 generateStackPush("b") +
//                 generateStackPush("a");
//         break;
//       case 0x5e: // dup2_x2
//         code += "\
//         var a = " + generateStackPop() + ";\n\
//         var b = " + generateStackPop() + ";\n\
//         var c = " + generateStackPop() + ";\n\
//         var d = " + generateStackPop() + ";\n";
//         code += generateStackPush("b") +
//                 generateStackPush("a") +
//                 generateStackPush("d") +
//                 generateStackPush("c") +
//                 generateStackPush("b") +
//                 generateStackPush("a");
//         break;
//       case 0x5f: // swap
//         var a = generateStackPop();
//         var b = generateStackPop();
//         code += generateStackPush(a) +
//                 generateStackPush(b);
//         break;
//       case 0x84: // iinc
//         var idx = frame.read8();
//         var val = frame.read8signed();
//         code += generateSetLocal(idx, generateGetLocal(idx) + " + " + val);
//         break;
//       case 0x60: // iadd
//         code += generateStackPush("(" + generateStackPop() + " + " + generateStackPop() + ") | 0");
//         break;
//       case 0x61: // ladd
//         code += generateStackPush2(generateStackPop2() + ".add(" + generateStackPop2() + ")");
//         break;
//       case 0x62: // fadd
//         code += generateStackPush("Math.fround(" + generateStackPop() + " + " + generateStackPop() + ")");
//         break;
//       case 0x63: // dadd
//         code += generateStackPush2(generateStackPop2() + " + " + generateStackPop2());
//         break;
//       case 0x64: // isub
//         code += generateStackPush("(-" + generateStackPop() + " + " + generateStackPop() + ") | 0");
//         break;
//       case 0x65: // lsub
//         code += generateStackPush2(generateStackPop2() + ".negate().add(" + generateStackPop2() + ")");
//         break;
//       case 0x66: // fsub
//         code += generateStackPush("Math.fround(-" + generateStackPop() + " + " + generateStackPop() + ")");
//         break;
//       case 0x67: // dsub
//         code += generateStackPush2("-" + generateStackPop2() + " + " + generateStackPop2());
//         break;
//       case 0x68: // imul
//         code += generateStackPush("Math.imul(" + generateStackPop() + ", " + generateStackPop() + ")");
//         break;
//       case 0x69: // lmul
//         code += generateStackPush2(generateStackPop2() + ".multiply(" + generateStackPop2() + ")");
//         break;
//       case 0x6a: // fmul
//         code += generateStackPush("Math.fround(" + generateStackPop() + " * " + generateStackPop() + ")");
//         break;
//       case 0x6b: // dmul
//         code += generateStackPush2(generateStackPop2() + " * " + generateStackPop2());
//         break;
//       case 0x6c: // idiv
//         var b = generateStackPop();
//         var a = generateStackPop();
//         code += "\
//         if (!" + b + ") {\n\
//           frame.ip = " + ip + "\n" +
//           generateStoreLocals() + "\
//           ctx.raiseExceptionAndYield('java/lang/ArithmeticException', '/ by zero');\n\
//         }\n";
//         code += generateStackPush("(" + a + " === util.INT_MIN && " + b + " === -1) ? " + a + " : ((" + a + " / " + b + ")|0)");
//         break;
//       case 0x6d: // ldiv
//         var b = generateStackPop2();
//         var a = generateStackPop2();
//         code += "\
//         if (" + b + ".isZero()) {\n\
//           frame.ip = " + ip + "\n" +
//           generateStoreLocals() + "\
//           ctx.raiseExceptionAndYield('java/lang/ArithmeticException', '/ by zero');\n\
//         }\n";
//         code += generateStackPush2(a + ".div(" + b + ")");
//         break;
//       case 0x6e: // fdiv
//         var b = generateStackPop();
//         var a = generateStackPop();
//         code += generateStackPush("Math.fround(" + a + " / " + b + ")");
//         break;
//       case 0x6f: // ddiv
//         var b = generateStackPop2();
//         var a = generateStackPop2();
//         code += generateStackPush2(a + " / " + b);
//         break;
//       case 0x70: // irem
//         var b = generateStackPop();
//         var a = generateStackPop();
//         code += "\
//         if (!" + b + ") {\n\
//           frame.ip = " + ip + "\n" +
//           generateStoreLocals() + "\
//           ctx.raiseExceptionAndYield('java/lang/ArithmeticException', '/ by zero');\n\
//         }\n";
//         code += generateStackPush(a + " % " + b);
//         break;
//       case 0x71: // lrem
//         var b = generateStackPop2();
//         var a = generateStackPop2();
//         code += "\
//         if (" + b + ".isZero()) {\n\
//           frame.ip = " + ip + "\n" +
//           generateStoreLocals() + "\
//           ctx.raiseExceptionAndYield('java/lang/ArithmeticException', '/ by zero');\n\
//         }\n";
//         code += generateStackPush2(a + ".modulo(" + b + ")");
//         break;
//       case 0x72: // frem
//         var b = generateStackPop();
//         var a = generateStackPop();
//         code += generateStackPush("Math.fround(" + a + " % " + b + ")");
//         break;
//       case 0x73: // drem
//         var b = generateStackPop2();
//         var a = generateStackPop2();
//         code += generateStackPush2(a + " % " + b);
//         break;
//       case 0x74: // ineg
//         code += generateStackPush("(-" + generateStackPop() + ") | 0");
//         break;
//       case 0x75: // lneg
//         code += generateStackPush2(generateStackPop2() + ".negate()");
//         break;
//       case 0x76: // fneg
//         code += generateStackPush("-" + generateStackPop());
//         break;
//       case 0x77: // dneg
//         code += generateStackPush2("-" + generateStackPop2());
//         break;
//       case 0x78: // ishl
//         var b = generateStackPop();
//         var a = generateStackPop();
//         code += generateStackPush(a + " << " + b);
//         break;
//       case 0x79: // lshl
//         var b = generateStackPop();
//         var a = generateStackPop2();
//         code += generateStackPush2(a + ".shiftLeft(" + b + ")");
//         break;
//       case 0x7a: // ishr
//         var b = generateStackPop();
//         var a = generateStackPop();
//         code += generateStackPush(a + " >> " + b);
//         break;
//       case 0x7b: // lshr
//         var b = generateStackPop();
//         var a = generateStackPop2();
//         code += generateStackPush2(a + ".shiftRight(" + b + ")");
//         break;
//       case 0x7c: // iushr
//         var b = generateStackPop();
//         var a = generateStackPop();
//         code += generateStackPush(a + " >>> " + b);
//         break;
//       case 0x7d: // lushr
//         var b = generateStackPop();
//         var a = generateStackPop2();
//         code += generateStackPush2(a + ".shiftRightUnsigned(" + b + ")");
//         break;
//       case 0x7e: // iand
//         code += generateStackPush(generateStackPop() + " & " + generateStackPop());
//         break;
//       case 0x7f: // land
//         code += generateStackPush2(generateStackPop2() + ".and(" + generateStackPop2() + ")");
//         break;
//       case 0x80: // ior
//         code += generateStackPush(generateStackPop() + " | " + generateStackPop());
//         break;
//       case 0x81: // lor
//         code += generateStackPush2(generateStackPop2() + ".or(" + generateStackPop2() + ")");
//         break;
//       case 0x82: // ixor
//         code += generateStackPush(generateStackPop() + " ^ " + generateStackPop());
//         break;
//       case 0x83: // lxor
//         code += generateStackPush2(generateStackPop2() + ".xor(" + generateStackPop2() + ")");
//         break;
//       case 0x94: // lcmp
//         var b = generateStackPop2();
//         var a = generateStackPop2();

//         code += "\
//         if (" + a + ".greaterThan(" + b + ")) {\n\
//           S" + depth + " = 1;\n\
//         } else if (" + a + ".lessThan(" + b + ")) {\n\
//           S" + depth + " = -1;\n\
//         } else {\n\
//           S" + depth + " = 0;\n\
//         }\n";

//         depth += 1;

//         break;
//       case 0x95: // fcmpl
//         var b = generateStackPop();
//         var a = generateStackPop();

//         code += "\
//         if (isNaN(" + a + ") || isNaN(" + b + ")) {\n\
//           S" + depth + " = -1;\n\
//         } else if (" + a + " > " + b + ") {\n\
//           S" + depth + " = 1;\n\
//         } else if (" + a + " < " + b + "){\n\
//           S" + depth + " = -1;\n\
//         } else {\n\
//           S" + depth + " = 0;\n\
//         }\n";

//         depth += 1;

//         break;
//       case 0x96: // fcmpg
//         var b = generateStackPop();
//         var a = generateStackPop();

//         code += "\
//         if (isNaN(" + a + ") || isNaN(" + b + ")) {\n\
//           S" + depth + " = 1;\n\
//         } else if (" + a + " > " + b + ") {\n\
//           S" + depth + " = 1;\n\
//         } else if (" + a + " < " + b + "){\n\
//           S" + depth + " = -1;\n\
//         } else {\n\
//           S" + depth + " = 0;\n\
//         }\n";

//         depth += 1;

//         break;
//       case 0x97: // dcmpl
//         var b = generateStackPop2();
//         var a = generateStackPop2();

//         code += "\
//         if (isNaN(" + a + ") || isNaN(" + b + ")) {\n\
//           S" + depth + " = -1;\n\
//         } else if (" + a + " > " + b + ") {\n\
//           S" + depth + " = 1;\n\
//         } else if (" + a + " < " + b + "){\n\
//           S" + depth + " = -1;\n\
//         } else {\n\
//           S" + depth + " = 0;\n\
//         }\n";

//         depth += 1;

//         break;
//       case 0x98: // dcmpg
//         var b = generateStackPop2();
//         var a = generateStackPop2();

//         code += "\
//         if (isNaN(" + a + ") || isNaN(" + b + ")) {\n\
//           S" + depth + " = 1;\n\
//         } else if (" + a + " > " + b + ") {\n\
//           S" + depth + " = 1;\n\
//         } else if (" + a + " < " + b + "){\n\
//           S" + depth + " = -1;\n\
//         } else {\n\
//           S" + depth + " = 0;\n\
//         }\n";

//         depth += 1;

//         break;
//       case 0x99: // ifeq
//         var jmp = ip + frame.read16signed();
//         code += generateIf(jmp, generateStackPop() + " === " + 0);
//         break;
//       case 0x9a: // ifne
//         var jmp = ip + frame.read16signed();
//         code += generateIf(jmp, generateStackPop() + " !== " + 0);
//         break;
//       case 0x9b: // iflt
//         var jmp = ip + frame.read16signed();
//         code += generateIf(jmp, generateStackPop() + " < " + 0);
//         break;
//       case 0x9c: // ifge
//         var jmp = ip + frame.read16signed();
//         code += generateIf(jmp, generateStackPop() + " >= " + 0);
//         break;
//       case 0x9d: // ifgt
//         var jmp = ip + frame.read16signed();
//         code += generateIf(jmp, generateStackPop() + " > " + 0);
//         break;
//       case 0x9e: // ifle
//         var jmp = ip + frame.read16signed();
//         code += generateIf(jmp, generateStackPop() + " <= " + 0);
//         break;
//       case 0x9f: // if_icmpeq
//         var jmp = ip + frame.read16signed();
//         code += generateIf(jmp, generateStackPop() + " === " + generateStackPop());
//         break;
//       case 0xa0: // if_cmpne
//         var jmp = ip + frame.read16signed();
//         code += generateIf(jmp, generateStackPop() + " !== " + generateStackPop());
//         break;
//       case 0xa1: // if_icmplt
//         var jmp = ip + frame.read16signed();
//         code += generateIf(jmp, generateStackPop() + " > " + generateStackPop());
//         break;
//       case 0xa2: // if_icmpge
//         var jmp = ip + frame.read16signed();
//         code += generateIf(jmp, generateStackPop() + " <= " + generateStackPop());
//         break;
//       case 0xa3: // if_icmpgt
//         var jmp = ip + frame.read16signed();
//         code += generateIf(jmp, generateStackPop() + " < " + generateStackPop());
//         break;
//       case 0xa4: // if_icmple
//         var jmp = ip + frame.read16signed();
//         code += generateIf(jmp, generateStackPop() + " >= " + generateStackPop());
//         break;
//       case 0xa5: // if_acmpeq
//         var jmp = ip + frame.read16signed();
//         code += generateIf(jmp, generateStackPop() + " === " + generateStackPop());
//         break;
//       case 0xa6: // if_acmpne
//         var jmp = ip + frame.read16signed();
//         code += generateIf(jmp, generateStackPop() + " !== " + generateStackPop());
//         break;
//       case 0xc6: // ifnull
//         var jmp = ip + frame.read16signed();
//         code += generateIf(jmp, "!" + generateStackPop());
//         break;
//       case 0xc7: // ifnonnull
//         var jmp = ip + frame.read16signed();
//         code += generateIf(jmp, generateStackPop());
//         break;
//       case 0xa7: // goto
//         var target = ip + frame.read16signed();
//         code += "        ip = " + target + ";\n        continue;\n";
//         targetIPs.add(target);
//         stackLayout.set(target, depth);
//         break;
//       case 0xc8: // goto_w
//         var target = ip + frame.read32signed();
//         code += "        ip = " + target + ";\n        continue;\n";
//         targetIPs.add(target);
//         stackLayout.set(target, depth);
//         break;
//       case 0xa8: // jsr
//         var jmp = frame.read16();
//         targetIPs.add(frame.ip); // ret will return here
//         code += generateStackPush(ip) + "\n\
//           ip = " + jmp + ";\n\
//           continue;\n";
//         targetIPs.add(jmp);
//         stackLayout.set(jmp, depth);
//         stackLayout.set(frame.ip, depth);
//         break;
//       case 0xc9: // jsr_w
//         var jmp = frame.read32();
//         targetIPs.add(frame.ip); // ret will return here
//         code += generateStackPush(ip) + "\n\
//           ip = " + jmp + ";\n\
//           continue;\n";
//         targetIPs.add(jmp);
//         stackLayout.set(jmp, depth);
//         stackLayout.set(frame.ip, depth);
//         break;
//       case 0xa9: // ret
//         code += "        ip = " + generateGetLocal(frame.read8()) + ";\n        continue;\n";
//         break;
//       case 0x85: // i2l
//         code += generateStackPush2("Long.fromInt(" + generateStackPop() + ")");
//         break;
//       case 0x86: // i2f
//         break;
//       case 0x87: // i2d
//         code += generateStackPush2(generateStackPop());
//         break;
//       case 0x88: // l2i
//         code += generateStackPush(generateStackPop2() + ".toInt()");
//         break;
//       case 0x89: // l2f
//         code += generateStackPush("Math.fround(" + generateStackPop2() + ".toNumber())");
//         break;
//       case 0x8a: // l2d
//         code += generateStackPush2(generateStackPop2() + ".toNumber()");
//         break;
//       case 0x8b: // f2i
//         code += generateStackPush("util.double2int(" + generateStackPop() + ")")
//         break;
//       case 0x8c: // f2l
//         code += generateStackPush2("Long.fromNumber(" + generateStackPop() + ")")
//         break;
//       case 0x8d: // f2d
//         code += generateStackPush2(generateStackPop());
//         break;
//       case 0x8e: // d2i
//         code += generateStackPush("util.double2int(" + generateStackPop2() + ")");
//         break;
//       case 0x8f: // d2l
//         code += generateStackPush2("util.double2long(" + generateStackPop2() + ")");
//         break;
//       case 0x90: // d2f
//         code += generateStackPush("Math.fround(" + generateStackPop2() + ")");
//         break;
//       case 0x91: // i2b
//         code += generateStackPush("(" + generateStackPop() + " << 24) >> 24");
//         break;
//       case 0x92: // i2c
//         code += generateStackPush(generateStackPop() + " & 0xffff");
//         break;
//       case 0x93: // i2s
//         code += generateStackPush("(" + generateStackPop() + " << 16) >> 16");
//         break;
//       case 0xaa: // tableswitch TODO: ADD STACKLAYOUT STUFF
//         var startip = frame.ip - 1;

//         while ((frame.ip & 3) != 0) {
//           frame.ip++;
//         }

//         var def = frame.read32signed() + startip;
//         var low = frame.read32signed();
//         var high = frame.read32signed();

//         var numJumpOffsets = high - low + 1;

//         code += "\
//         var offsets = new Int32Array(" + numJumpOffsets + ");\n";
//         for (var i = 0; i < numJumpOffsets; i++) {
//           var jmp = frame.read32signed() + startip;
//           code += "        offsets[" + i + "] = " + jmp + ";\n";
//           targetIPs.add(jmp);
//         }

//         targetIPs.add(def);

//         var val = generateStackPop();

//         code += "\
//         var jmp;\n\
//         if (" + val + " < " + low + " || " + val + " > " + high + ") {\n\
//           ip = " + def + ";\n\
//         } else {\n\
//           ip = offsets[" + val + " - " + low + "];\n\
//         }\n\
//         continue;\n";
//         break;
//       case 0xab: // lookupswitch TODO: ADD STACKLAYOUT STUFF
//           var startip = frame.ip - 1;

//           while ((frame.ip & 3) != 0) {
//             frame.ip++;
//           }

//           var def = frame.read32signed() + startip;
//           var size = frame.read32();

//           code += "\
//           var offsets = {};\n";
//           for (var i = 0; i < size; i++) {
//             var key = frame.read32signed();
//             var jmp = frame.read32signed() + startip;
//             code += "        offsets[" + key + "] = " + jmp + ";\n";
//             targetIPs.add(jmp);
//           }

//           targetIPs.add(def);

//           var val = generateStackPop();

//           code += "\
//           var jmp = offsets[" + val + "];\n\
//           if (!jmp) {\n\
//             jmp = " + def + "\n\
//           }\n\
//           ip = jmp;\n\
//           continue;\n";
//           break;
//       case 0xbc: // newarray
//         var type = "????ZCFDBSIJ"[frame.read8()];
//         var size = generateStackPop();
//         code += "\
//         if (" + size + " < 0) {\n\
//           frame.ip = " + ip + "\n" +
//           generateStoreLocals() + "\
//           ctx.raiseExceptionAndYield('java/lang/NegativeArraySizeException', " + size + ");\n\
//         }\n" + generateStackPush("util.newPrimitiveArray('" + type + "', " + size + ")");
//         break;
//       case 0xbd: // anewarray
//         var idx = frame.read16();

//         // Resolve the classinfo in advance.
//         var classInfo = cp[idx];
//         if (classInfo.tag) {
//           classInfo = resolveCompiled(cp, idx);
//         }

//         var className = classInfo.className;
//         if (className[0] !== "[") {
//           className = "L" + className + ";";
//         }
//         className = "[" + className;

//         var size = generateStackPop();

//         code += "\
//         if (" + size + " < 0) {\n\
//           frame.ip = " + ip + "\n" +
//           generateStoreLocals() + "\
//           ctx.raiseExceptionAndYield('java/lang/NegativeArraySizeException', " + size + ");\n\
//         }\n" + generateStackPush("util.newArray('" + className + "', " + size + ")");
//         break;
//       case 0xc5: // multianewarray
//         var idx = frame.read16();

//         // Resolve the classinfo in advance.
//         var classInfo = cp[idx];
//         if (classInfo.tag) {
//           classInfo = resolveCompiled(cp, idx);
//         }

//         var dimensions = frame.read8();

//         code += "        var lengths = new Array(" + dimensions + ");\n";
//         for (var i = 0; i < dimensions; i++) {
//           code += "        lengths[" + i + "] = " + generateStackPop() + ";\n";
//         }
//         code += generateStackPush("util.newMultiArray('" + classInfo.className + "', lengths.reverse())");
//         break;
//       case 0xbe: // arraylength
//         var obj = generateStackPop();

//         code += "\
//         if (!" + obj + ") {\n\
//           frame.ip = " + ip + "\n" +
//           generateStoreLocals() + "\
//           ctx.raiseExceptionAndYield('java/lang/NullPointerException');\n\
//         }\n";
//         code += generateStackPush(obj + ".length");
//         break;
//       case 0xb4: // getfield
//         var idx = frame.read16();

//         // Resolve the field in advance.
//         var field = cp[idx];
//         if (field && field.tag) {
//           field = resolveCompiled(cp, idx, false);
//         }

//         var obj = generateStackPop();

//         code += "\
//         if (!" + obj + ") {\n\
//           frame.ip = " + ip + "\n" +
//           generateStoreLocals() + "\
//           ctx.raiseExceptionAndYield('java/lang/NullPointerException');\n\
//         }\n";

//         if (field.signature === "J" || field.signature === "D") {
//           code += generateStackPush2("cp[" + idx + "].get(" + obj + ")");
//         } else {
//           code += generateStackPush("cp[" + idx + "].get(" + obj + ")");
//         }
//         break;
//       case 0xb5: // putfield
//         var idx = frame.read16();

//         // Resolve the field in advance.
//         var field = cp[idx];
//         if (field && field.tag) {
//           field = resolveCompiled(cp, idx, false);
//         }

//         var val;
//         if (field.signature === "J" || field.signature === "D") {
//           val = generateStackPop2();
//         } else {
//           val = generateStackPop();
//         }

//         var obj = generateStackPop();

//         code += "\
//         if (!" + obj + ") {\n\
//           frame.ip = " + ip + "\n" +
//           generateStoreLocals() + "\
//           ctx.raiseExceptionAndYield('java/lang/NullPointerException');\n\
//         }\n\
//         cp[" + idx + "].set(" + obj + ", " + val + ");\n"
//         break;
//       case 0xb2: // getstatic
//         var idx = frame.read16();

//         // Resolve the field in advance.
//         var field = cp[idx];
//         if (field && field.tag) {
//           field = resolveCompiled(cp, idx, true);
//         }

//         code += "\
//         var field = cp[" + idx + "];\n";

//         if (!field.classInfo.isArrayClass) {
//           code +="\
//         if (!ctx.runtime.initialized[field.classInfo.className]) {\n" +
//           generateStoreState(ip) + "\
//           ctx.pushClassInitFrame(field.classInfo);\n\
//           throw VM.Yield;\n\
//         }\n";
//         }

//         code += "\
//         var value = ctx.runtime.getStatic(field);\n\
//         if (typeof value === 'undefined') {\n\
//           value = util.defaultValue(field.signature);\n\
//         }\n";

//         if (field.signature === "J" || field.signature === "D") {
//           code += generateStackPush2("value");
//         } else {
//           code += generateStackPush("value");
//         }
//         break;
//       case 0xb3: // putstatic
//         var idx = frame.read16();

//         // Resolve the field in advance.
//         var field = cp[idx];
//         if (field.tag) {
//           field = resolveCompiled(cp, idx, true);
//         }

//         code += "\
//         var field = cp[" + idx + "];\n";

//         if (!field.classInfo.isArrayClass) {
//           code +="\
//         if (!ctx.runtime.initialized[field.classInfo.className]) {\n" +
//           generateStoreState(ip) + "\
//           ctx.pushClassInitFrame(field.classInfo);\n\
//           throw VM.Yield;\n\
//         }\n";
//         }

//         var val;
//         if (field.signature === "J" || field.signature === "D") {
//           val = generateStackPop2();
//         } else {
//           val = generateStackPop();
//         }

//         code += "\
//         ctx.runtime.setStatic(field, " + val + ");\n";
//         break;
//       case 0xbb: // new
//         var idx = frame.read16();

//         // Resolve class in advance.
//         var classInfo = cp[idx];
//         if (classInfo.tag) {
//           classInfo = resolveCompiled(cp, idx);
//         }

//         code += "\
//         var classInfo = cp[" + idx + "];\n";

//         if (!classInfo.isArrayClass) {
//           code +="\
//         if (!ctx.runtime.initialized[classInfo.className]) {\n" +
//           generateStoreState(ip) + "\
//           ctx.pushClassInitFrame(classInfo);\n\
//           throw VM.Yield;\n\
//         }\n";
//         }

//         code += generateStackPush("util.newObject(classInfo)");
//         break;
//       case 0xc0: // checkcast
//         var idx = frame.read16();

//         // Resolve class in advance.
//         var classInfo = cp[idx];
//         if (classInfo.tag) {
//           classInfo = resolveCompiled(cp, idx);
//         }

//         var obj = "S" + (depth-1);

//         code += "\
//         var classInfo = cp[" + idx + "];\n\
//         if (" + obj + ") {\n\
//           if (!" + obj + ".class.isAssignableTo(classInfo)) {\n\
//             frame.ip = " + ip + "\n" +
//             generateStoreLocals() + "\
//             ctx.raiseExceptionAndYield('java/lang/ClassCastException',\n\
//                                        " + obj + ".class.className + ' is not assignable to ' +\n\
//                                        classInfo.className);\n\
//             }\n\
//         }\n";
//         break;
//       case 0xc1: // instanceof
//         var idx = frame.read16();

//         // Resolve class in advance.
//         var classInfo = cp[idx];
//         if (classInfo.tag) {
//           classInfo = resolveCompiled(cp, idx);
//         }

//         var obj = generateStackPop();

//         code += "\
//         var classInfo = cp[" + idx + "];\n\
//         var result = !" + obj + " ? false : " + obj + ".class.isAssignableTo(classInfo);\n" +
//         generateStackPush("result ? 1 : 0");
//         break;
//       case 0xbf: // athrow
//         var obj = generateStackPop();

//         code += generateStoreState(ip) + "\
//         if (!" + obj + ") {\n\
//           ctx.raiseExceptionAndYield('java/lang/NullPointerException');\n\
//         }\n" + "\
//         return throw_(" + obj + ", ctx);\n";
//         break;
//       case 0xc2: // monitorenter
//         var obj = generateStackPop();

//         code += "\
//         if (!" + obj + ") {\n\
//           frame.ip = " + ip + "\n" +
//           generateStoreLocals() + "\
//           ctx.raiseExceptionAndYield('java/lang/NullPointerException');\n\
//         }\n\
//         try {\n\
//           ctx.monitorEnter(" + obj + ");\n\
//         }\n\
//         catch (e) {\n\
//           " + generateStoreState(frame.ip) + "\
//           throw e;\n\
//         }\n";
//         break;
//       case 0xc3: // monitorexit
//         var obj = generateStackPop();

//         code += "\
//         if (!" + obj + ") {\n\
//           frame.ip = " + ip + "\n" +
//           generateStoreLocals() + "\
//           ctx.raiseExceptionAndYield('java/lang/NullPointerException');\n\
//         }\n\
//         ctx.monitorExit(" + obj + ");\n";
//         break;
//       case 0xc4: // wide
//         var op = frame.read8();
//         var opName = OPCODES[op];
//         code += "// " + opName + " [0x" + op.toString(16) + "]\n";
//         switch (op) {
//           case 0x15: // iload
//           case 0x17: // fload
//           case 0x19: // aload
//             code += generateStackPush(generateGetLocal(frame.read16()));
//             break;
//           case 0x16: // lload
//           case 0x18: // dload
//             code += generateStackPush2(generateGetLocal(frame.read16()));
//             break;
//           case 0x36: // istore
//           case 0x38: // fstore
//           case 0x3a: // astore
//             code += generateSetLocal(frame.read16(), generateStackPop());
//             break;
//           case 0x37: // lstore
//           case 0x39: // dstore
//             code += generateSetLocal(frame.read16(), generateStackPop2());
//             break;
//           case 0x84: // iinc
//             var idx = frame.read16();
//             var val = frame.read16signed();
//             code += generateSetLocal(idx, generateGetLocal(idx) + " + " + val);
//             break;
//           case 0xa9: // ret
//             code += "        ip = " + generateGetLocal(frame.read16()) + ";\n        continue;\n";
//             continue;
//             break;
//           default:
//             var opName = OPCODES[op];
//             throw new Error("Wide opcode " + opName + " [" + op + "] not supported.");
//         }
//         break;
//       case 0xb6: // invokevirtual
//       case 0xb7: // invokespecial
//       case 0xb8: // invokestatic
//       case 0xb9: // invokeinterface
//         var idx = frame.read16();
//         if (op === 0xb9) {
//           /*var argsNumber =*/ frame.read8();
//           /*var zero =*/ frame.read8();
//         }
//         var isStatic = (op === 0xb8);
//         var toCallMethodInfo = cp[idx];
//         // Resolve method in advance.
//         if (toCallMethodInfo.tag) {
//           toCallMethodInfo = resolveCompiled(cp, idx, isStatic);
//         }

//         code += "        var toCallMethodInfo = cp[" + idx + "];\n";

//         if (isStatic && !toCallMethodInfo.classInfo.isArrayClass) {
//             code +="\
//           if (!ctx.runtime.initialized[toCallMethodInfo.classInfo.className]) {\n" +
//             generateStoreState(ip) + "\
//             ctx.pushClassInitFrame(toCallMethodInfo.classInfo);\n\
//             throw VM.Yield;\n\
//           }\n";
//         }

//         if (!isStatic) {
//           var obj = "S" + (depth - toCallMethodInfo.consumes);

//           code += "\
//         if (!" + obj + ") {\n\
//           frame.ip = " + ip + "\n" +
//           generateStoreLocals() + "\
//           ctx.raiseExceptionAndYield('java/lang/NullPointerException');\n\
//         }\n";

//           if (op === 0xb6 || op === 0xb9) {
//             code += "\
//         if (toCallMethodInfo.classInfo != " + obj + ".class) {\n\
//           if (" + obj + ".class.vmc['" + toCallMethodInfo.key + "']) {\n\
//             toCallMethodInfo = " + obj + ".class.vmc['" + toCallMethodInfo.key + "'];\n\
//           } else {\n\
//             toCallMethodInfo = CLASSES.getMethod(" + obj + ".class, '" + toCallMethodInfo.key + "');\n\
//           }\n\
//         }\n";
//           }
//         }

//         code += generateStoreState(frame.ip);

//         depth -= toCallMethodInfo.consumes;

//         var alternateImplCall = "\
//           Instrument.callPauseHooks(frame);\n\
//           Instrument.measure(alternateImpl, ctx, toCallMethodInfo);\n\
//           Instrument.callResumeHooks(frame);\n";

//         var normalCall = "\
//           Instrument.callPauseHooks(frame);\n\
//           if (!toCallMethodInfo.compiled && toCallMethodInfo.numCalled >= 100 && !toCallMethodInfo.dontCompile) {\n\
//             try {\n\
//               toCallMethodInfo.compiled = new Function('ctx', VM.compile(toCallMethodInfo, ctx));\n\
//             } catch (e) {\n\
//               toCallMethodInfo.dontCompile = true;\n\
//               console.warn('Can\\'t compile function: ' + e);\n\
//             }\n\
//           }\n\
//           Instrument.callResumeHooks(frame);\n\
// \n\
//           var callee = ctx.pushFrame(toCallMethodInfo);\n";
//           if (toCallMethodInfo.isSynchronized) {
//             normalCall += "\
//             if (!callee.lockObject) {\n\
//               callee.lockObject = " + (toCallMethodInfo.isStatic
//                                          ? "toCallMethodInfo.classInfo.getClassObject(ctx)\n"
//                                          : "callee.getLocal(0);\n") + "\
//             }\n\
//             ctx.monitorEnter(callee.lockObject);\n";
//           }

//           normalCall += "\n\
//           var newFrame;\n\
// \n\
//           if (toCallMethodInfo.compiled) {\n\
//             newFrame = toCallMethodInfo.compiled(ctx);\n\
//           } else {\n\
//             newFrame = VM.execute(ctx);\n\
//           }\n\
// \n\
//           if (newFrame !== frame) {\n\
//             return newFrame;\n\
//           }\n";

//         if (op === 0xb6 || op == 0xb9) {
//           code += "\
//           var alternateImpl = toCallMethodInfo.alternateImpl;\n\
//           if (alternateImpl) {\n" +
//             alternateImplCall + "\
//           } else {\n" +
//             normalCall + "\
//           }\n";
//         } else if (toCallMethodInfo.alternateImpl) {
//           code += "\
//           var alternateImpl = toCallMethodInfo.alternateImpl;\n" + alternateImplCall;
//         } else {
//           code += normalCall;
//         }

//         var returnType = toCallMethodInfo.signature[toCallMethodInfo.signature.length - 1];
//         if (returnType === 'J' || returnType === 'D') {
//           code += generateStackPush2("frame.stack.pop2()");
//         } else if (returnType !== 'V') {
//           code += generateStackPush("frame.stack.pop()");
//         }
//         code += "        frame.stack.length = 0;\n";

//         break;
//       case 0xb1: // return
//         if (methodInfo.isSynchronized) {
//           code += "        if (frame.lockObject) {\n\
//           ctx.monitorExit(frame.lockObject);\n\
//         }\n";
//         }

//         code += "\
//         if (ctx.frames.length == 1)\n\
//             return;\n\
//         var caller = ctx.popFrame();\n\
//         return caller;\n";
//         break;
//       case 0xac: // ireturn
//       case 0xae: // freturn
//       case 0xb0: // areturn
//         if (methodInfo.isSynchronized) {
//           code += "        if (frame.lockObject) {\n\
//           ctx.monitorExit(frame.lockObject);\n\
//         }\n";
//         }

//         code += "\
//         if (ctx.frames.length == 1)\n\
//             return;\n\
//         var caller = ctx.popFrame();\n\
//         caller.stack.push(" + generateStackPop() + ");\n\
//         return caller;\n";
//         break;
//       case 0xad: // lreturn
//       case 0xaf: // dreturn
//         if (methodInfo.isSynchronized) {
//           code += "        if (frame.lockObject) {\n\
//           ctx.monitorExit(frame.lockObject);\n\
//         }\n";
//         }

//         code += "\
//         if (ctx.frames.length == 1)\n\
//             return;\n\
//         var caller = ctx.popFrame();\n\
//         caller.stack.push2(" + generateStackPop2() + ");\n\
//         return caller;\n";
//         break;
//       default:
//         throw new Error("NOT SUPPORTED: " + op.toString(16));
//     }

//     generatedCases.push({
//       ip: ip,
//       code: code,
//     });
//   }

//   var localsCode = "";
//   for (var i = 0; i < maxLocals+1; i++) {
//     localsCode += "  var L" + i + " = frame.getLocal(" + i + ");\n";
//   }
//   if (maxDepth > 0) {
//     localsCode += "  var";
//     for (var i = 0; i < maxDepth; i++) {
//       localsCode += " S" + i + ",";
//     }
//     localsCode = localsCode.slice(0, -1) + ";\n";
//   }

//   var generatedCode = "\n  var frame = ctx.current();\n\
//   var cp = frame.cp;\n" + localsCode + "\
//   var ip = frame.ip;\n\
// \n\
//   if (ip !== 0) {\n";

//   for (var i = 0; i < maxDepth; i++) {
//     generatedCode += "\
//     if (frame.stack.length > 0) {\n\
//       S" + i + " = frame.stack.shift();\n\
//     }\n";
//   }

//   generatedCode += "  }\n\n\
//   while (true) {\n\
//     switch (ip) {\n";

//   generatedCases.forEach(function(aCase) {
//     if (targetIPs.has(aCase.ip)) {
//       generatedCode += "case " + aCase.ip + ":\n";
//     }

//     generatedCode += aCase.code;
//   });

//   generatedCode += "      default:\n\
//         console.log('IP: ' + ip);\n";

//   generatedCode += "    }\n";
//   generatedCode += "  }\n";

//   return generatedCode;
// }
