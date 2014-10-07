/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

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
        return false;
    }
    if (idx < 0 || idx >= refArray.length) {
        ctx.raiseExceptionAndYield("java/lang/ArrayIndexOutOfBoundsException", idx);
        return false;
    }
    return true;
}

function resolve(ctx, cp, op, idx) {
    var constant = cp[idx];
    if (!constant.tag)
        return constant;
    switch(constant.tag) {
    case TAGS.CONSTANT_Integer:
        constant = constant.integer;
        break;
    case TAGS.CONSTANT_Float:
        constant = constant.float;
        break;
    case TAGS.CONSTANT_String:
        constant = ctx.newString(cp[constant.string_index].bytes);
        break;
    case TAGS.CONSTANT_Long:
        constant = Long.fromBits(constant.lowBits, constant.highBits);
        break;
    case TAGS.CONSTANT_Double:
        constant = constant.double;
        break;
    case TAGS.CONSTANT_Class:
        constant = CLASSES.getClass(cp[constant.name_index].bytes);
        break;
    case TAGS.CONSTANT_Fieldref:
        var classInfo = resolve(ctx, cp, op, constant.class_index);
        var fieldName = cp[cp[constant.name_and_type_index].name_index].bytes;
        var signature = cp[cp[constant.name_and_type_index].signature_index].bytes;
        constant = CLASSES.getField(classInfo, ((op === 0xb2 || op === 0xb3) ? "S" : "I") + "." + fieldName + "." + signature);
        if (!constant)
            ctx.raiseExceptionAndYield("java/lang/RuntimeException",
                               classInfo.className + "." + fieldName + "." + signature + " not found");
        break;
    case TAGS.CONSTANT_Methodref:
    case TAGS.CONSTANT_InterfaceMethodref:
        var classInfo = resolve(ctx, cp, op, constant.class_index);
        var methodName = cp[cp[constant.name_and_type_index].name_index].bytes;
        var signature = cp[cp[constant.name_and_type_index].signature_index].bytes;
        constant = CLASSES.getMethod(classInfo, ((op === 0xb8) ? "S" : "I") + "." + methodName + "." + signature);
        if (!constant)
            ctx.raiseExceptionAndYield("java/lang/RuntimeException",
                               classInfo.className + "." + methodName + "." + signature + " not found");
        break;
    default:
        throw new Error("not support constant type");
    }
    return cp[idx] = constant;
}

function classInitCheck(ctx, frame, classInfo, ip) {
    if (classInfo.isArrayClass || ctx.runtime.initialized[classInfo.className])
        return;
    frame.ip = ip;
    ctx.pushClassInitFrame(classInfo);
    throw VM.Yield;
}

function pushFrame(ctx, methodInfo, consumes) {
    var frame = ctx.pushFrame(methodInfo, consumes);
    if (frame.isSynchronized) {
        if (!frame.lockObject) {
          frame.lockObject = methodInfo.isStatic
                             ? methodInfo.classInfo.getClassObject(ctx)
                             : frame.getLocal(0);
        }
        ctx.monitorEnter(frame.lockObject);
    }
    return frame;
}

function popFrame(ctx, consumes) {
    var callee = ctx.current();
    if (callee.lockObject)
        ctx.monitorExit(callee.lockObject);
    var frame = ctx.popFrame();
    var stack = frame.stack;
    switch (consumes) {
    case 2:
        stack.push2(callee.stack.pop2());
        break;
    case 1:
        stack.push(callee.stack.pop());
        break;
    }
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
        //console.log(frame.methodInfo.name + " frame.ip: " + frame.ip);
        for (var i=0; exception_table && i<exception_table.length; i++) {
            //console.log("range: " + exception_table[i].start_pc + " - " + exception_table[i].end_pc);
            if (frame.ip >= exception_table[i].start_pc && frame.ip <= exception_table[i].end_pc) {
                if (exception_table[i].catch_type === 0) {
                    handler_pc = exception_table[i].handler_pc;
                } else {
                    var classInfo = resolve(ctx, cp, OPCODES.athrow, exception_table[i].catch_type);
                    //console.log("ClassInfo: " + classInfo);
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
            stack.push(ex);
            frame.ip = handler_pc;

            //console.log("EX: " + ex);
            //console.log("HANDLER FRAME: " + frame.methodInfo.name + " (COMPILED: " + (!!frame.methodInfo.compiled) + "), IP: " + frame.ip)

            if (VM.DEBUG_PRINT_ALL_EXCEPTIONS) {
                console.error(buildExceptionLog(ex, stackTrace));
            }

            return frame;
        }

        frame = popFrame(ctx, 0);
        stack = frame.stack;
        cp = frame.cp;
    } while (frame.methodInfo);
    ctx.kill();
    throw new Error(buildExceptionLog(ex, stackTrace));
}

VM.execute = function(ctx) {
    var frame = ctx.current();

    while (true) {
        while (frame && frame.methodInfo && frame.methodInfo.compiled) {
          frame = frame.methodInfo.compiled(ctx);
        }

        if (!frame || !frame.methodInfo) {
          return;
        }

        var cp = frame.cp;
        var stack = frame.stack;

        var op = frame.read8();
        switch (op) {
        case 0x00: // nop
            break;
        case 0x01: // aconst_null
            stack.push(null);
            break;
        case 0x02: // aconst_m1
            stack.push(-1);
            break;
        case 0x03: // iconst_0
        case 0x0b: // fconst_0
            stack.push(0);
            break;
        case 0x0e: // dconst_0
            stack.push2(0);
            break;
        case 0x04: // iconst_1
        case 0x0c: // fconst_1
            stack.push(1);
            break;
        case 0x0f: // dconst_1
            stack.push2(1);
            break;
        case 0x05: // iconst_2
        case 0x0d: // fconst_2
            stack.push(2);
            break;
        case 0x06: // iconst_3
            stack.push(3);
            break;
        case 0x07: // iconst_4
            stack.push(4);
            break;
        case 0x08: // iconst_5
            stack.push(5);
            break;
        case 0x09: // lconst_0
            stack.push2(Long.fromInt(0));
            break;
        case 0x0a: // lconst_1
            stack.push2(Long.fromInt(1));
            break;
        case 0x10: // bipush
            stack.push(frame.read8signed());
            break;
        case 0x11: // sipush
            stack.push(frame.read16signed());
            break;
        case 0x12: // ldc
        case 0x13: // ldc_w
            var idx = (op === 0x12) ? frame.read8() : frame.read16();
            var constant = cp[idx];
            if (constant.tag)
                constant = resolve(ctx, cp, op, idx);
            stack.push(constant);
            break;
        case 0x14: // ldc2_w
            var idx = frame.read16();
            var constant = cp[idx];
            if (constant.tag)
                constant = resolve(ctx, cp, op, idx);
            stack.push2(constant);
            break;
        case 0x15: // iload
        case 0x17: // fload
        case 0x19: // aload
            stack.push(frame.getLocal(frame.read8()));
            break;
        case 0x16: // lload
        case 0x18: // dload
            stack.push2(frame.getLocal(frame.read8()));
            break;
        case 0x1a: // iload_0
        case 0x22: // fload_0
        case 0x2a: // aload_0
            stack.push(frame.getLocal(0));
            break;
        case 0x1b: // iload_1
        case 0x23: // fload_1
        case 0x2b: // aload_1
            stack.push(frame.getLocal(1));
            break;
        case 0x1c: // iload_2
        case 0x24: // fload_2
        case 0x2c: // aload_2
            stack.push(frame.getLocal(2));
            break;
        case 0x1d: // iload_3
        case 0x25: // fload_3
        case 0x2d: // aload_3
            stack.push(frame.getLocal(3));
            break;
        case 0x1e: // lload_0
        case 0x26: // dload_0
            stack.push2(frame.getLocal(0));
            break;
        case 0x1f: // lload_1
        case 0x27: // dload_1
            stack.push2(frame.getLocal(1));
            break;
        case 0x20: // lload_2
        case 0x28: // dload_2
            stack.push2(frame.getLocal(2));
            break;
        case 0x21: // lload_3
        case 0x29: // dload_3
            stack.push2(frame.getLocal(3));
            break;
        case 0x2e: // iaload
        case 0x30: // faload
        case 0x32: // aaload
        case 0x33: // baload
        case 0x34: // caload
        case 0x35: // saload
            var idx = stack.pop();
            var refArray = stack.pop();
            if (!checkArrayAccess(ctx, refArray, idx))
                break;
            stack.push(refArray[idx]);
            break;
        case 0x2f: // laload
        case 0x31: // daload
            var idx = stack.pop();
            var refArray = stack.pop();
            if (!checkArrayAccess(ctx, refArray, idx))
                break;
            stack.push2(refArray[idx]);
            break;
        case 0x36: // istore
        case 0x38: // fstore
        case 0x3a: // astore
            frame.setLocal(frame.read8(), stack.pop());
            break;
        case 0x37: // lstore
        case 0x39: // dstore
            frame.setLocal(frame.read8(), stack.pop2());
            break;
        case 0x3b: // istore_0
        case 0x43: // fstore_0
        case 0x4b: // astore_0
            frame.setLocal(0, stack.pop());
            break;
        case 0x3c: // istore_1
        case 0x44: // fstore_1
        case 0x4c: // astore_1
            frame.setLocal(1, stack.pop());
            break;
        case 0x3d: // istore_2
        case 0x45: // fstore_2
        case 0x4d: // astore_2
            frame.setLocal(2, stack.pop());
            break;
        case 0x3e: // istore_3
        case 0x46: // fstore_3
        case 0x4e: // astore_3
            frame.setLocal(3, stack.pop());
            break;
        case 0x3f: // lstore_0
        case 0x47: // dstore_0
            frame.setLocal(0, stack.pop2());
            break;
        case 0x40: // lstore_1
        case 0x48: // dstore_1
            frame.setLocal(1, stack.pop2());
            break;
        case 0x41: // lstore_2
        case 0x49: // dstore_2
            frame.setLocal(2, stack.pop2());
            break;
        case 0x42: // lstore_3
        case 0x4a: // dstore_3
            frame.setLocal(3, stack.pop2());
            break;
        case 0x4f: // iastore
        case 0x51: // fastore
        case 0x54: // bastore
        case 0x55: // castore
        case 0x56: // sastore
            var val = stack.pop();
            var idx = stack.pop();
            var refArray = stack.pop();
            if (!checkArrayAccess(ctx, refArray, idx))
                break;
            refArray[idx] = val;
            break;
        case 0x50: // lastore
        case 0x52: // dastore
            var val = stack.pop2();
            var idx = stack.pop();
            var refArray = stack.pop();
            if (!checkArrayAccess(ctx, refArray, idx))
                break;
            refArray[idx] = val;
            break;
        case 0x53: // aastore
            var val = stack.pop();
            var idx = stack.pop();
            var refArray = stack.pop();
            if (!checkArrayAccess(ctx, refArray, idx))
                break;
            if (val && !val.class.isAssignableTo(refArray.class.elementClass)) {
                ctx.raiseExceptionAndYield("java/lang/ArrayStoreException");
                break;
            }
            refArray[idx] = val;
            break;
        case 0x57: // pop
            stack.pop();
            break;
        case 0x58: // pop2
            stack.pop2();
            break;
        case 0x59: // dup
            var val = stack.pop();
            stack.push(val);
            stack.push(val);
            break;
        case 0x5a: // dup_x1
            var a = stack.pop();
            var b = stack.pop();
            stack.push(a);
            stack.push(b);
            stack.push(a);
            break;
        case 0x5b: // dup_x2
            var a = stack.pop();
            var b = stack.pop();
            var c = stack.pop();
            stack.push(a);
            stack.push(c);
            stack.push(b);
            stack.push(a);
            break;
        case 0x5c: // dup2
            var a = stack.pop();
            var b = stack.pop();
            stack.push(b);
            stack.push(a);
            stack.push(b);
            stack.push(a);
            break;
        case 0x5d: // dup2_x1
            var a = stack.pop();
            var b = stack.pop();
            var c = stack.pop();
            stack.push(b);
            stack.push(a);
            stack.push(c);
            stack.push(b);
            stack.push(a);
            break;
        case 0x5e: // dup2_x2
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
        case 0x5f: // swap
            var a = stack.pop();
            var b = stack.pop();
            stack.push(a);
            stack.push(b);
            break;
        case 0x84: // iinc
            var idx = frame.read8();
            var val = frame.read8signed();
            frame.setLocal(idx, frame.getLocal(idx) + val);
            break;
        case 0x60: // iadd
            stack.push((stack.pop() + stack.pop())|0);
            break;
        case 0x61: // ladd
            stack.push2(stack.pop2().add(stack.pop2()));
            break;
        case 0x62: // fadd
            stack.push(Math.fround(stack.pop() + stack.pop()));
            break;
        case 0x63: // dadd
            stack.push2(stack.pop2() + stack.pop2());
            break;
        case 0x64: // isub
            stack.push((- stack.pop() + stack.pop())|0);
            break;
        case 0x65: // lsub
            stack.push2(stack.pop2().negate().add(stack.pop2()));
            break;
        case 0x66: // fsub
            stack.push(Math.fround(- stack.pop() + stack.pop()));
            break;
        case 0x67: // dsub
            stack.push2(- stack.pop2() + stack.pop2());
            break;
        case 0x68: // imul
            stack.push(Math.imul(stack.pop(), stack.pop()));
            break;
        case 0x69: // lmul
            stack.push2(stack.pop2().multiply(stack.pop2()));
            break;
        case 0x6a: // fmul
            stack.push(Math.fround(stack.pop() * stack.pop()));
            break;
        case 0x6b: // dmul
            stack.push2(stack.pop2() * stack.pop2());
            break;
        case 0x6c: // idiv
            var b = stack.pop();
            var a = stack.pop();
            if (!b) {
                ctx.raiseExceptionAndYield("java/lang/ArithmeticException", "/ by zero");
                break;
            }
            stack.push((a === util.INT_MIN && b === -1) ? a : ((a / b)|0));
            break;
        case 0x6d: // ldiv
            var b = stack.pop2();
            var a = stack.pop2();
            if (b.isZero()) {
                ctx.raiseExceptionAndYield("java/lang/ArithmeticException", "/ by zero");
                break;
            }
            stack.push2(a.div(b));
            break;
        case 0x6e: // fdiv
            var b = stack.pop();
            var a = stack.pop();
            stack.push(Math.fround(a / b));
            break;
        case 0x6f: // ddiv
            var b = stack.pop2();
            var a = stack.pop2();
            stack.push2(a / b);
            break;
        case 0x70: // irem
            var b = stack.pop();
            var a = stack.pop();
            if (!b) {
                ctx.raiseExceptionAndYield("java/lang/ArithmeticException", "/ by zero");
                break;
            }
            stack.push(a % b);
            break;
        case 0x71: // lrem
            var b = stack.pop2();
            var a = stack.pop2();
            if (b.isZero()) {
                ctx.raiseExceptionAndYield("java/lang/ArithmeticException", "/ by zero");
                break;
            }
            stack.push2(a.modulo(b));
            break;
        case 0x72: // frem
            var b = stack.pop();
            var a = stack.pop();
            stack.push(Math.fround(a % b));
            break;
        case 0x73: // drem
            var b = stack.pop2();
            var a = stack.pop2();
            stack.push2(a % b);
            break;
        case 0x74: // ineg
            stack.push((- stack.pop())|0);
            break;
        case 0x75: // lneg
            stack.push2(stack.pop2().negate());
            break;
        case 0x76: // fneg
            stack.push(- stack.pop());
            break;
        case 0x77: // dneg
            stack.push2(- stack.pop2());
            break;
        case 0x78: // ishl
            var b = stack.pop();
            var a = stack.pop();
            stack.push(a << b);
            break;
        case 0x79: // lshl
            var b = stack.pop();
            var a = stack.pop2();
            stack.push2(a.shiftLeft(b));
            break;
        case 0x7a: // ishr
            var b = stack.pop();
            var a = stack.pop();
            stack.push(a >> b);
            break;
        case 0x7b: // lshr
            var b = stack.pop();
            var a = stack.pop2();
            stack.push2(a.shiftRight(b));
            break;
        case 0x7c: // iushr
            var b = stack.pop();
            var a = stack.pop();
            stack.push(a >>> b);
            break;
        case 0x7d: // lushr
            var b = stack.pop();
            var a = stack.pop2();
            stack.push2(a.shiftRightUnsigned(b));
            break;
        case 0x7e: // iand
            stack.push(stack.pop() & stack.pop());
            break;
        case 0x7f: // land
            stack.push2(stack.pop2().and(stack.pop2()));
            break;
        case 0x80: // ior
            stack.push(stack.pop() | stack.pop());
            break;
        case 0x81: // lor
            stack.push2(stack.pop2().or(stack.pop2()));
            break;
        case 0x82: // ixor
            stack.push(stack.pop() ^ stack.pop());
            break;
        case 0x83: // lxor
            stack.push2(stack.pop2().xor(stack.pop2()));
            break;
        case 0x94: // lcmp
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
        case 0x95: // fcmpl
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
        case 0x96: // fcmpg
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
        case 0x97: // dcmpl
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
        case 0x98: // dcmpg
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
        case 0x99: // ifeq
            var jmp = frame.ip - 1 + frame.read16signed();
            if (stack.pop() === 0) {
              frame.ip = jmp;
              continue;
            }
            break;
        case 0x9a: // ifne
            var jmp = frame.ip - 1 + frame.read16signed();
            if (stack.pop() !== 0) {
              frame.ip = jmp;
              continue;
            }
            break;
        case 0x9b: // iflt
            var jmp = frame.ip - 1 + frame.read16signed();
            if (stack.pop() < 0) {
              frame.ip = jmp;
              continue;
            }
            break;
        case 0x9c: // ifge
            var jmp = frame.ip - 1 + frame.read16signed();
            if (stack.pop() >= 0) {
              frame.ip = jmp;
              continue;
            }
            break;
        case 0x9d: // ifgt
            var jmp = frame.ip - 1 + frame.read16signed();
            if (stack.pop() > 0) {
              frame.ip = jmp;
              continue;
            }
            break;
        case 0x9e: // ifle
            var jmp = frame.ip - 1 + frame.read16signed();
            if (stack.pop() <= 0) {
              frame.ip = jmp;
              continue;
            }
            break;
        case 0x9f: // if_icmpeq
            var jmp = frame.ip - 1 + frame.read16signed();
            if (stack.pop() === stack.pop()) {
              frame.ip = jmp;
              continue;
            }
            break;
        case 0xa0: // if_cmpne
            var jmp = frame.ip - 1 + frame.read16signed();
            if (stack.pop() !== stack.pop()) {
              frame.ip = jmp;
              continue;
            }
            break;
        case 0xa1: // if_icmplt
            var jmp = frame.ip - 1 + frame.read16signed();
            if (stack.pop() > stack.pop()) {
              frame.ip = jmp;
              continue;
            }
            break;
        case 0xa2: // if_icmpge
            var jmp = frame.ip - 1 + frame.read16signed();
            if (stack.pop() <= stack.pop()) {
              frame.ip = jmp;
              continue;
            }
            break;
        case 0xa3: // if_icmpgt
            var jmp = frame.ip - 1 + frame.read16signed();
            if (stack.pop() < stack.pop()) {
              frame.ip = jmp;
              continue;
            }
            break;
        case 0xa4: // if_icmple
            var jmp = frame.ip - 1 + frame.read16signed();
            if (stack.pop() >= stack.pop()) {
              frame.ip = jmp;
              continue;
            }
            break;
        case 0xa5: // if_acmpeq
            var jmp = frame.ip - 1 + frame.read16signed();
            if (stack.pop() === stack.pop()) {
              frame.ip = jmp;
              continue;
            }
            break;
        case 0xa6: // if_acmpne
            var jmp = frame.ip - 1 + frame.read16signed();
            if (stack.pop() !== stack.pop()) {
              frame.ip = jmp;
              continue;
            }
            break;
        case 0xc6: // ifnull
            var jmp = frame.ip - 1 + frame.read16signed();
            if (!stack.pop()) {
              frame.ip = jmp;
              continue;
            }
            break;
        case 0xc7: // ifnonnull
            var jmp = frame.ip - 1 + frame.read16signed();
            if (stack.pop()) {
              frame.ip = jmp;
              continue;
            }
            break;
        case 0xa7: // goto
            frame.ip += frame.read16signed() - 1;
            continue;
            break;
        case 0xc8: // goto_w
            frame.ip += frame.read32signed() - 1;
            continue;
            break;
        case 0xa8: // jsr
            var jmp = frame.read16();
            stack.push(frame.ip);
            frame.ip = jmp;
            continue;
            break;
        case 0xc9: // jsr_w
            var jmp = frame.read32();
            stack.push(frame.ip);
            frame.ip = jmp;
            continue;
            break;
        case 0xa9: // ret
            frame.ip = frame.getLocal(frame.read8());
            continue;
            break;
        case 0x85: // i2l
            stack.push2(Long.fromInt(stack.pop()));
            break;
        case 0x86: // i2f
            break;
        case 0x87: // i2d
            stack.push2(stack.pop());
            break;
        case 0x88: // l2i
            stack.push(stack.pop2().toInt());
            break;
        case 0x89: // l2f
            stack.push(Math.fround(stack.pop2().toNumber()));
            break;
        case 0x8a: // l2d
            stack.push2(stack.pop2().toNumber());
            break;
        case 0x8b: // f2i
            stack.push(util.double2int(stack.pop()));
            break;
        case 0x8c: // f2l
            stack.push2(Long.fromNumber(stack.pop()));
            break;
        case 0x8d: // f2d
            stack.push2(stack.pop());
            break;
        case 0x8e: // d2i
            stack.push(util.double2int(stack.pop2()));
            break;
        case 0x8f: // d2l
            stack.push2(util.double2long(stack.pop2()));
            break;
        case 0x90: // d2f
            stack.push(Math.fround(stack.pop2()));
            break;
        case 0x91: // i2b
            stack.push((stack.pop() << 24) >> 24);
            break;
        case 0x92: // i2c
            stack.push(stack.pop() & 0xffff);
            break;
        case 0x93: // i2s
            stack.push((stack.pop() << 16) >> 16);
            break;
        case 0xaa: // tableswitch
            var startip = frame.ip;
            while ((frame.ip & 3) != 0)
                frame.ip++;
            var def = frame.read32signed();
            var low = frame.read32signed();
            var high = frame.read32signed();
            var val = stack.pop();
            var jmp;
            if (val < low || val > high) {
                jmp = def;
            } else {
                frame.ip  += (val - low) << 2;
                jmp = frame.read32signed();
            }
            frame.ip = startip - 1 + jmp;
            continue;
            break;
        case 0xab: // lookupswitch
            var startip = frame.ip;
            while ((frame.ip & 3) != 0)
                frame.ip++;
            var jmp = frame.read32signed();
            var size = frame.read32();
            var val = frame.stack.pop();
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
            continue;
            break;
        case 0xbc: // newarray
            var type = frame.read8();
            var size = stack.pop();
            if (size < 0) {
                ctx.raiseExceptionAndYield("java/lang/NegativeArraySizeException", size);
                break;
            }
            stack.push(ctx.newPrimitiveArray("????ZCFDBSIJ"[type], size));
            break;
        case 0xbd: // anewarray
            var idx = frame.read16();
            var classInfo = cp[idx];
            if (classInfo.tag)
                classInfo = resolve(ctx, cp, op, idx);
            var size = stack.pop();
            if (size < 0) {
                ctx.raiseExceptionAndYield("java/lang/NegativeArraySizeException", size);
                break;
            }
            var className = classInfo.className;
            if (className[0] !== "[")
                className = "L" + className + ";";
            className = "[" + className;
            stack.push(ctx.newArray(className, size));
            break;
        case 0xc5: // multianewarray
            var idx = frame.read16();
            var classInfo = cp[idx];
            if (classInfo.tag)
                classInfo = resolve(ctx, cp, op, idx);
            var dimensions = frame.read8();
            var lengths = new Array(dimensions);
            for (var i=0; i<dimensions; i++)
                lengths[i] = stack.pop();
            stack.push(ctx.newMultiArray(classInfo.className, lengths.reverse()));
            break;
        case 0xbe: // arraylength
            var obj = stack.pop();
            if (!obj) {
                ctx.raiseExceptionAndYield("java/lang/NullPointerException");
                break;
            }
            stack.push(obj.length);
            break;
        case 0xb4: // getfield
            var idx = frame.read16();
            var field = cp[idx];
            if (field.tag)
                field = resolve(ctx, cp, op, idx);
            var obj = stack.pop();
            if (!obj) {
                ctx.raiseExceptionAndYield("java/lang/NullPointerException");
                break;
            }
            stack.pushType(field.signature, field.get(obj));
            break;
        case 0xb5: // putfield
            var idx = frame.read16();
            var field = cp[idx];
            if (field.tag)
                field = resolve(ctx, cp, op, idx);
            var val = stack.popType(field.signature);
            var obj = stack.pop();
            if (!obj) {
                ctx.raiseExceptionAndYield("java/lang/NullPointerException");
                break;
            }
            field.set(obj, val);
            break;
        case 0xb2: // getstatic
            var idx = frame.read16();
            var field = cp[idx];
            if (field.tag)
                field = resolve(ctx, cp, op, idx);
            classInitCheck(ctx, frame, field.classInfo, frame.ip-3);
            var value = ctx.runtime.getStatic(field);
            if (typeof value === "undefined") {
                value = util.defaultValue(field.signature);
            }
            stack.pushType(field.signature, value);
            break;
        case 0xb3: // putstatic
            var idx = frame.read16();
            var field = cp[idx];
            if (field.tag)
                field = resolve(ctx, cp, op, idx);
            classInitCheck(ctx, frame, field.classInfo, frame.ip-3);
            ctx.runtime.setStatic(field, stack.popType(field.signature));
            break;
        case 0xbb: // new
            var idx = frame.read16();
            var classInfo = cp[idx];
            if (classInfo.tag)
                classInfo = resolve(ctx, cp, op, idx);
            classInitCheck(ctx, frame, classInfo, frame.ip-3);
            stack.push(ctx.newObject(classInfo));
            break;
        case 0xc0: // checkcast
            var idx = frame.read16();
            var classInfo = cp[idx];
            if (classInfo.tag)
                classInfo = resolve(ctx, cp, op, idx);
            var obj = stack[stack.length - 1];
            if (obj) {
                if (!obj.class.isAssignableTo(classInfo)) {
                    ctx.raiseExceptionAndYield("java/lang/ClassCastException",
                                       obj.class.className + " is not assignable to " +
                                       classInfo.className);
                    break;
                }
            }
            break;
        case 0xc1: // instanceof
            var idx = frame.read16();
            var classInfo = cp[idx];
            if (classInfo.tag)
                classInfo = resolve(ctx, cp, op, idx);
            var obj = stack.pop();
            var result = !obj ? false : obj.class.isAssignableTo(classInfo);
            stack.push(result ? 1 : 0);
            break;
        case 0xbf: // athrow
            var obj = stack.pop();
            if (!obj) {
                ctx.raiseExceptionAndYield("java/lang/NullPointerException");
                break;
            }
            frame = throw_(obj, ctx);
            //console.log("THROW: " + frame.methodInfo.name)
            break;
        case 0xc2: // monitorenter
            var obj = stack.pop();
            if (!obj) {
                ctx.raiseExceptionAndYield("java/lang/NullPointerException");
                break;
            }
            ctx.monitorEnter(obj);
            break;
        case 0xc3: // monitorexit
            var obj = stack.pop();
            if (!obj) {
                ctx.raiseExceptionAndYield("java/lang/NullPointerException");
                break;
            }
            ctx.monitorExit(obj);
            break;
        case 0xc4: // wide
            switch (op = frame.read8()) {
            case 0x15: // iload
            case 0x17: // fload
            case 0x19: // aload
                stack.push(frame.getLocal(frame.read16()));
                break;
            case 0x16: // lload
            case 0x18: // dload
                stack.push2(frame.getLocal(frame.read16()));
                break;
            case 0x36: // istore
            case 0x38: // fstore
            case 0x3a: // astore
                frame.setLocal(frame.read16(), stack.pop());
                break;
            case 0x37: // lstore
            case 0x39: // dstore
                frame.setLocal(frame.read16(), stack.pop2());
                break;
            case 0x84: // iinc
                var idx = frame.read16();
                var val = frame.read16signed();
                frame.setLocal(idx, frame.getLocal(idx) + val);
                break;
            case 0xa9: // ret
                frame.ip = frame.getLocal(frame.read16());
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
            var startip = frame.ip - 1;
            var idx = frame.read16();
            if (op === 0xb9) {
                var argsNumber = frame.read8();
                var zero = frame.read8();
            }
            var isStatic = (op === 0xb8);
            var methodInfo = cp[idx];
            if (methodInfo.tag) {
                methodInfo = resolve(ctx, cp, op, idx);
                if (isStatic)
                    classInitCheck(ctx, frame, methodInfo.classInfo, startip);
            }
            var consumes = Signature.getINSlots(methodInfo.signature);
            if (!isStatic) {
              ++consumes;
              var obj = stack[stack.length - consumes];
              if (!obj) {
                  ctx.raiseExceptionAndYield("java/lang/NullPointerException");
                  break;
              }

              if (op === 0xb6 || op === 0xb9) {
                if (methodInfo.classInfo != obj.class) {
                    if (!methodInfo.key) {
                        methodInfo.key = "I." + methodInfo.name + "." + methodInfo.signature;
                    }

                    //console.log("OBJ: " + obj);

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
                try {
                    Instrument.callPauseHooks(ctx.current());
                    Instrument.measure(alternateImpl, ctx, methodInfo);
                    Instrument.callResumeHooks(ctx.current());
                } catch (e) {
                    Instrument.callResumeHooks(ctx.current());
                    throw e;
                }
                break;
            }

            var oldFrame = frame;
            frame = pushFrame(ctx, methodInfo, consumes);
            if (!methodInfo.compiled && methodInfo.numCalled >= 1 && !methodInfo.dontCompile) {
                try {
                  //console.log(VM.compile(methodInfo));
                  methodInfo.compiled = new Function("ctx", VM.compile(methodInfo, ctx));
                } catch (e) {
                  methodInfo.dontCompile = true;
                }
            }
            if (methodInfo.compiled) {
              var newFrame = methodInfo.compiled(ctx);
              if (oldFrame != newFrame) {
                return;
              }
              frame = newFrame;
            }
            break;
        case 0xb1: // return
            if (VM.DEBUG) {
                VM.trace("return", ctx.thread.pid, frame.methodInfo);
            }
            if (ctx.frames.length == 1)
                return;
            frame.methodInfo.numCalled++;
            frame = popFrame(ctx, 0);
            // Return if the caller is compiled
            if (!frame.methodInfo || frame.methodInfo.compiled) {
                return frame;
            }
            break;
        case 0xac: // ireturn
        case 0xae: // freturn
        case 0xb0: // areturn
            if (VM.DEBUG) {
                VM.trace("return", ctx.thread.pid, frame.methodInfo, stack[stack.length-1]);
            }
            if (ctx.frames.length == 1)
                return;
            frame.methodInfo.numCalled++;
            frame = popFrame(ctx, 1);
            // Return if the caller is compiled
            if (!frame.methodInfo || frame.methodInfo.compiled) {
                return frame;
            }
            break;
        case 0xad: // lreturn
        case 0xaf: // dreturn
            if (VM.DEBUG) {
                VM.trace("return", ctx.thread.pid, frame.methodInfo, stack[stack.length-1]);
            }
            if (ctx.frames.length == 1)
                return;
            frame.methodInfo.numCalled++;
            frame = popFrame(ctx, 2);
            // Return if the caller is compiled
            if (!frame.methodInfo || frame.methodInfo.compiled) {
                return frame;
            }
            break;
        default:
            var opName = OPCODES[op];
            throw new Error("Opcode " + opName + " [" + op + "] not supported. (" + ctx.thread.pid + ")");
        }
    };
}

VM.compile = function(methodInfo, ctx) {
  var depth = 0, maxDepth = 0;
  var locals = 0, maxLocals = 0;
  var ip = 0;

  var code = "";

  var frame = new Frame(methodInfo);
  var cp = frame.cp;

  var targetIPs = new Set([0]);
  var generatedCases = new Set();
  var stackLayout = new Map();

  function generateStackPush(val) {
    var gen = "        S" + (depth++) + " = " + val + ";\n";
    if (depth > maxDepth) {
      maxDepth = depth;
    }
    return gen;
  }

  function generateStackPush2(val) {
    var gen = "        S" + (depth++) + " = " + val + ";\n";
    gen += "        S" + (depth++) + " = null;\n";
    if (depth > maxDepth) {
      maxDepth = depth;
    }
    return gen;
  }

  function generateStackPop() {
    return "S" + (--depth);
  }

  function generateStackPop2() {
    --depth;
    return "S" + (--depth);
  }

  function generateSetLocal(num, val) {
    if (num > maxLocals) {
      maxLocals = num;
    }
    return "        L" + num + " = " + val + ";\n";
  }

  function generateGetLocal(num) {
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

  function generateStoreState(ip) {
    targetIPs.add(ip);

    var gen = "        frame.ip = " + ip + ";\n";
    for (var i = 0; i < depth; i++) {
      gen += "        frame.stack.push(S" + i + ");\n";
    }
    for (var i = 0; i <= maxLocals; i++) {
      gen += "        frame.setLocal(" + i + ", L" + i + ");\n";
    }

    return gen + "\n";
  }

  function generateCheckArrayAccess(ip) {
    return "\
    if (!refArray) {\n" +
    generateStoreState(ip) + "\
      ctx.raiseExceptionAndYield('java/lang/NullPointerException');\n\
    }\n\
    if (idx < 0 || idx >= refArray.length) {\n" +
    generateStoreState(ip) + "\
      ctx.raiseExceptionAndYield('java/lang/ArrayIndexOutOfBoundsException', idx);\n\
    }\n";
  }

  function resolveCompiled(cp, op, idx) {
      var constant = cp[idx];

      if (!constant.tag)
        return constant;

      switch(constant.tag) {
        case TAGS.CONSTANT_Integer:
          constant = constant.integer;
          break;
        case TAGS.CONSTANT_Float:
          constant = constant.float;
          break;
        case TAGS.CONSTANT_String:
          constant = ctx.newString(cp[constant.string_index].bytes);
          break;
        case TAGS.CONSTANT_Long:
          constant = Long.fromBits(constant.lowBits, constant.highBits);
          break;
        case TAGS.CONSTANT_Double:
          constant = constant.double;
          break;
        case TAGS.CONSTANT_Class:
          constant = CLASSES.getClass(cp[constant.name_index].bytes);
          break;
        case TAGS.CONSTANT_Fieldref:
          var classInfo = resolveCompiled(cp, op, constant.class_index);
          var fieldName = cp[cp[constant.name_and_type_index].name_index].bytes;
          var signature = cp[cp[constant.name_and_type_index].signature_index].bytes;
          constant = CLASSES.getField(classInfo, ((op === 0xb2 || op === 0xb3) ? "S" : "I") + "." + fieldName + "." + signature);
          if (!constant) {
            throw new Error("java/lang/RuntimeException");
          }
          break;
        case TAGS.CONSTANT_Methodref:
        case TAGS.CONSTANT_InterfaceMethodref:
          var classInfo = resolveCompiled(cp, op, constant.class_index);
          var methodName = cp[cp[constant.name_and_type_index].name_index].bytes;
          var signature = cp[cp[constant.name_and_type_index].signature_index].bytes;
          constant = CLASSES.getMethod(classInfo, ((op === 0xb8) ? "S" : "I") + "." + methodName + "." + signature);
          if (!constant) {
            throw new Error("java/lang/RuntimeException");
          }
          break;
        default:
          throw new Error("not support constant type");
      }

      return cp[idx] = constant;
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

    var opName = OPCODES[op];
    code += "      case " + ip + ": // " + opName + " [0x" + op.toString(16) + "]\n";
    //code += "      console.log('" + methodInfo.classInfo.className + "." + methodInfo.name + "." + methodInfo.signature + " IMAT: " + ip + "');\n";
    generatedCases.add(ip);

    var newDepth = stackLayout.get(ip);
    if (typeof newDepth !== "undefined") {
      depth = newDepth;
    }

    switch (op) {
      case 0x00: // nop
        break;
      case 0x01: // aconst_null
        code += generateStackPush("null");
        break;
      case 0x02: // aconst_m1
        code += generateStackPush(-1);
        break;
      case 0x03: // iconst_0
      case 0x0b: // fconst_0
        code += generateStackPush(0);
        break;
      case 0x0e: // dconst_0
        code += generateStackPush2(0);
        break;
      case 0x04: // iconst_1
      case 0x0c: // fconst_1
        code += generateStackPush(1);
        break;
      case 0x0f: // dconst_1
        code += generateStackPush2(1);
        break;
      case 0x05: // iconst_2
      case 0x0d: // fconst_2
        code += generateStackPush(2);
        break;
      case 0x06: // iconst_3
        code += generateStackPush(3);
        break;
      case 0x07: // iconst_4
        code += generateStackPush(4);
        break;
      case 0x08: // iconst_5
        code += generateStackPush(5);
        break;
      case 0x09: // lconst_0
        code += generateStackPush2("Long.fromInt(0)");
        break;
      case 0x0a: // lconst_1
        code += generateStackPush2("Long.fromInt(1)");
        break;
      case 0x10: // bipush
        code += generateStackPush(frame.read8signed());
        break;
      case 0x11: // sipush
        code += generateStackPush(frame.read16signed());
        break;
      case 0x12: // ldc
      case 0x13: // ldc_w
        var idx = (op === 0x12) ? frame.read8() : frame.read16();

        // Resolve the constant in advance.
        var constant = cp[idx];

        if (constant.tag) {
          constant = resolveCompiled(cp, op, idx);
        }

        code += generateStackPush("cp[" + idx + "]");
        break;
      case 0x14: // ldc2_w
        var idx = frame.read16();

        // Resolve the constant in advance.
        var constant = cp[idx];
        if (constant.tag) {
          constant = resolveCompiled(cp, op, idx);
        }

        code += generateStackPush2("cp[" + idx + "]");
        break;
      case 0x15: // iload
      case 0x17: // fload
      case 0x19: // aload
        code += generateStackPush(generateGetLocal(frame.read8()));
        break;
      case 0x16: // lload
      case 0x18: // dload
        code += generateStackPush2(generateGetLocal(frame.read8()));
        break;
      case 0x1a: // iload_0
      case 0x22: // fload_0
      case 0x2a: // aload_0
        code += generateStackPush(generateGetLocal(0));
        break;
      case 0x1b: // iload_1
      case 0x23: // fload_1
      case 0x2b: // aload_1
        code += generateStackPush(generateGetLocal(1));
        break;
      case 0x1c: // iload_2
      case 0x24: // fload_2
      case 0x2c: // aload_2
        code += generateStackPush(generateGetLocal(2));
        break;
      case 0x1d: // iload_3
      case 0x25: // fload_3
      case 0x2d: // aload_3
        code += generateStackPush(generateGetLocal(3));
        break;
      case 0x1e: // lload_0
      case 0x26: // dload_0
        code += generateStackPush2(generateGetLocal(0));
        break;
      case 0x1f: // lload_1
      case 0x27: // dload_1
        code += generateStackPush2(generateGetLocal(1));
        break;
      case 0x20: // lload_2
      case 0x28: // dload_2
        code += generateStackPush2(generateGetLocal(2));
        break;
      case 0x21: // lload_3
      case 0x29: // dload_3
        code += generateStackPush2(generateGetLocal(3));
        break;
      case 0x2e: // iaload
      case 0x30: // faload
      case 0x32: // aaload
      case 0x33: // baload
      case 0x34: // caload
      case 0x35: // saload
        code += "\
        var idx = " + generateStackPop() + ";\n\
        var refArray = " + generateStackPop() + ";\n";
        code += generateCheckArrayAccess(frame.ip);
        code += generateStackPush("refArray[idx]");
        break;
      case 0x2f: // laload
      case 0x31: // daload
        code += "\
        var idx = " + generateStackPop() + ";\n\
        var refArray = " + generateStackPop() + ";\n";
        code += generateCheckArrayAccess(frame.ip);
        code += generateStackPush2("refArray[idx]");
        break;
      case 0x36: // istore
      case 0x38: // fstore
      case 0x3a: // astore
        code += generateSetLocal(frame.read8(), generateStackPop());
        break;
      case 0x37: // lstore
      case 0x39: // dstore
        code += generateSetLocal(frame.read8(), generateStackPop2());
        break;
      case 0x3b: // istore_0
      case 0x43: // fstore_0
      case 0x4b: // astore_0
        code += generateSetLocal(0, generateStackPop());
        break;
      case 0x3c: // istore_1
      case 0x44: // fstore_1
      case 0x4c: // astore_1
        code += generateSetLocal(1, generateStackPop());
        break;
      case 0x3d: // istore_2
      case 0x45: // fstore_2
      case 0x4d: // astore_2
        code += generateSetLocal(2, generateStackPop());
        break;
      case 0x3e: // istore_3
      case 0x46: // fstore_3
      case 0x4e: // astore_3
        code += generateSetLocal(3, generateStackPop());
        break;
      case 0x3f: // lstore_0
      case 0x47: // dstore_0
        code += generateSetLocal(0, generateStackPop2());
        break;
      case 0x40: // lstore_1
      case 0x48: // dstore_1
        code += generateSetLocal(1, generateStackPop2());
        break;
      case 0x41: // lstore_2
      case 0x49: // dstore_2
        code += generateSetLocal(2, generateStackPop2());
        break;
      case 0x42: // lstore_3
      case 0x4a: // dstore_3
        code += generateSetLocal(3, generateStackPop2());
        break;
      case 0x4f: // iastore
      case 0x51: // fastore
      case 0x54: // bastore
      case 0x55: // castore
      case 0x56: // sastore
        code += "\
        var val = " + generateStackPop() + ";\n\
        var idx = " + generateStackPop() + ";\n\
        var refArray = " + generateStackPop() + ";\n";
        code += generateCheckArrayAccess(frame.ip) + "\
        refArray[idx] = val;\n";
        break;
      case 0x50: // lastore
      case 0x52: // dastore
        code += "\
        var val = " + generateStackPop2() + ";\n\
        var idx = " + generateStackPop() + ";\n\
        var refArray = " + generateStackPop() + ";\n";
        code += generateCheckArrayAccess(frame.ip) + "\
        refArray[idx] = val;\n";
        break;
      case 0x53: // aastore
        code += "\
        var val = " + generateStackPop() + ";\n\
        var idx = " + generateStackPop() + ";\n\
        var refArray = " + generateStackPop() + ";\n";
        code += generateCheckArrayAccess(frame.ip) + "\
        if (val && !val.class.isAssignableTo(refArray.class.elementClass)) {\n";
        code += generateStoreState(frame.ip) + "\
            ctx.raiseExceptionAndYield('java/lang/ArrayStoreException');\n\
        }\n\
        refArray[idx] = val;\n";
        break;
      case 0x57: // pop
        depth--;
        break;
      case 0x58: // pop2
        depth -= 2;
        break;
      case 0x59: // dup
        code += generateStackPush("S" + (depth - 1));
        break;
      case 0x5a: // dup_x1
        code += "\
        var a = " + generateStackPop() + ";\n\
        var b = " + generateStackPop() + ";\n";
        code += generateStackPush("a") +
                generateStackPush("b") +
                generateStackPush("a");
        break;
      case 0x5b: // dup_x2
        code += "\
        var a = " + generateStackPop() + ";\n\
        var b = " + generateStackPop() + ";\n\
        var c = " + generateStackPop() + ";\n";
        code += generateStackPush("a") +
                generateStackPush("c") +
                generateStackPush("b") +
                generateStackPush("a");
        break;
      case 0x5c: // dup2
        code += "\
        var a = " + generateStackPop() + ";\n\
        var b = " + generateStackPop() + ";\n";
        code += generateStackPush("b") +
                generateStackPush("a") +
                generateStackPush("b") +
                generateStackPush("a");
        break;
      case 0x5d: // dup2_x1
        code += "\
        var a = " + generateStackPop() + ";\n\
        var b = " + generateStackPop() + ";\n\
        var c = " + generateStackPop() + ";\n";
        code += generateStackPush("b") +
                generateStackPush("a") +
                generateStackPush("c") +
                generateStackPush("b") +
                generateStackPush("a");
        break;
      case 0x5e: // dup2_x2
        code += "\
        var a = " + generateStackPop() + ";\n\
        var b = " + generateStackPop() + ";\n\
        var c = " + generateStackPop() + ";\n\
        var d = " + generateStackPop() + ";\n";
        code += generateStackPush("b") +
                generateStackPush("a") +
                generateStackPush("d") +
                generateStackPush("c") +
                generateStackPush("b") +
                generateStackPush("a");
        break;
      case 0x5f: // swap
        code += "\
        var a = " + generateStackPop() + ";\n\
        var b = " + generateStackPop() + ";\n";
        code += generateStackPush("a") +
                generateStackPush("b");
        break;
      case 0x84: // iinc
        var idx = frame.read8();
        var val = frame.read8signed();
        code += generateSetLocal(idx, generateGetLocal(idx) + " + " + val);
        break;
      case 0x60: // iadd
        code += generateStackPush("(" + generateStackPop() + " + " + generateStackPop() + ") | 0");
        break;
      case 0x61: // ladd
        code += generateStackPush2(generateStackPop2() + ".add(" + generateStackPop2() + ")");
        break;
      case 0x62: // fadd
        code += generateStackPush("Math.fround(" + generateStackPop() + " + " + generateStackPop() + ")");
        break;
      case 0x63: // dadd
        code += generateStackPush2(generateStackPop2() + " + " + generateStackPop2());
        break;
      case 0x64: // isub
        code += generateStackPush("(-" + generateStackPop() + " + " + generateStackPop() + ") | 0");
        break;
      case 0x65: // lsub
        code += generateStackPush2(generateStackPop2() + ".negate().add(" + generateStackPop2() + ")");
        break;
      case 0x66: // fsub
        code += generateStackPush("Math.fround(-" + generateStackPop() + " + " + generateStackPop() + ")");
        break;
      case 0x67: // dsub
        code += generateStackPush2("-" + generateStackPop2() + " + " + generateStackPop2());
        break;
      case 0x68: // imul
        code += generateStackPush("Math.imul(" + generateStackPop() + ", " + generateStackPop() + ")");
        break;
      case 0x69: // lmul
        code += generateStackPush2(generateStackPop2() + ".multiply(" + generateStackPop2() + ")");
        break;
      case 0x6a: // fmul
        code += generateStackPush("Math.fround(" + generateStackPop() + " * " + generateStackPop() + ")");
        break;
      case 0x6b: // dmul
        code += generateStackPush2(generateStackPop2() + " * " + generateStackPop2());
        break;
      case 0x6c: // idiv
        code += "\
        var b = " + generateStackPop() + ";\n\
        var a = " + generateStackPop() + ";\n\
        if (!b) {\n";
        code += generateStoreState(frame.ip) + "\
          ctx.raiseExceptionAndYield('java/lang/ArithmeticException', '/ by zero');\n\
        }\n";
        code += generateStackPush("(a === util.INT_MIN && b === -1) ? a : ((a / b)|0)");
        break;
      case 0x6d: // ldiv
        code += "\
        var b = " + generateStackPop2() + ";\n\
        var a = " + generateStackPop2() + ";\n\
        if (b.isZero()) {\n";
        code += generateStoreState(frame.ip) + "\
          ctx.raiseExceptionAndYield('java/lang/ArithmeticException', '/ by zero');\n\
        }\n";
        code += generateStackPush2("a.div(b)");
        break;
      case 0x6e: // fdiv
        code += "\
        var b = " + generateStackPop() + ";\n\
        var a = " + generateStackPop() + ";\n";
        code += generateStackPush("Math.fround(a / b)");
        break;
      case 0x6f: // ddiv
        code += "\
        var b = " + generateStackPop2() + ";\n\
        var a = " + generateStackPop2() + ";\n";
        code += generateStackPush2("a / b");
        break;
      case 0x70: // irem
        code += "\
        var b = " + generateStackPop() + ";\n\
        var a = " + generateStackPop() + ";\n\
        if (!b) {\n";
        code += generateStoreState(frame.ip) + "\
          ctx.raiseExceptionAndYield('java/lang/ArithmeticException', '/ by zero');\n\
        }\n";
        code += generateStackPush("a % b");
        break;
      case 0x71: // lrem
        code += "\
        var b = " + generateStackPop2() + ";\n\
        var a = " + generateStackPop2() + ";\n\
        if (b.isZero()) {\n";
        code += generateStoreState(frame.ip) + "\
          ctx.raiseExceptionAndYield('java/lang/ArithmeticException', '/ by zero');\n\
        }\n";
        code += generateStackPush2("a.modulo(b)");
        break;
      case 0x72: // frem
        code += "\
        var b = " + generateStackPop() + ";\n\
        var a = " + generateStackPop() + ";\n";
        code += generateStackPush("Math.fround(a % b)");
        break;
      case 0x73: // drem
        code += "\
        var b = " + generateStackPop2() + ";\n\
        var a = " + generateStackPop2() + ";\n";
        code += generateStackPush2("a % b");
        break;
      case 0x74: // ineg
        code += generateStackPush("(-" + generateStackPop() + ") | 0");
        break;
      case 0x75: // lneg
        code += generateStackPush2(generateStackPop2() + ".negate()");
        break;
      case 0x76: // fneg
        code += generateStackPush("-" + generateStackPop());
        break;
      case 0x77: // dneg
        code += generateStackPush2("-" + generateStackPop2());
        break;
      case 0x78: // ishl
        code += "\
        var b = " + generateStackPop() + ";\n\
        var a = " + generateStackPop() + ";\n";
        code += generateStackPush("a << b");
        break;
      case 0x79: // lshl
        code += "\
        var b = " + generateStackPop() + ";\n\
        var a = " + generateStackPop2() + ";\n";
        code += generateStackPush2("a.shiftLeft(b)");
        break;
      case 0x7a: // ishr
        code += "\
        var b = " + generateStackPop() + ";\n\
        var a = " + generateStackPop() + ";\n";
        code += generateStackPush("a >> b");
        break;
      case 0x7b: // lshr
        code += "\
        var b = " + generateStackPop() + ";\n\
        var a = " + generateStackPop2() + ";\n";
        code += generateStackPush2("a.shiftRight(b)");
        break;
      case 0x7c: // iushr
        code += "\
        var b = " + generateStackPop() + ";\n\
        var a = " + generateStackPop() + ";\n";
        code += generateStackPush("a >>> b");
        break;
      case 0x7d: // lushr
        code += "\
        var b = " + generateStackPop() + ";\n\
        var a = " + generateStackPop2() + ";\n";
        code += generateStackPush2("a.shiftRightUnsigned(b)");
        break;
      case 0x7e: // iand
        code += generateStackPush(generateStackPop() + " & " + generateStackPop());
        break;
      case 0x7f: // land
        code += generateStackPush2(generateStackPop2() + ".and(" + generateStackPop2() + ")");
        break;
      case 0x80: // ior
        code += generateStackPush(generateStackPop() + " | " + generateStackPop());
        break;
      case 0x81: // lor
        code += generateStackPush2(generateStackPop2() + ".or(" + generateStackPop2() + ")");
        break;
      case 0x82: // ixor
        code += generateStackPush(generateStackPop() + " ^ " + generateStackPop());
        break;
      case 0x83: // lxor
        code += generateStackPush2(generateStackPop2() + ".xor(" + generateStackPop2() + ")");
        break;
      case 0x94: // lcmp
        code += "\
        var b = " + generateStackPop2() + ";\n\
        var a = " + generateStackPop2() + ";\n";

        code += "\
        if (a.greaterThan(b)) {\n\
          S" + depth + " = 1;\n\
        } else if (a.lessThan(b)) {\n\
          S" + depth + " = -1;\n\
        } else {\n\
          S" + depth + " = 0;\n\
        }\n";

        depth += 1;

        break;
      case 0x95: // fcmpl
        code += "\
        var b = " + generateStackPop() + ";\n\
        var a = " + generateStackPop() + ";\n";

        code += "\
        if (isNaN(a) || isNaN(b)) {\n\
          S" + depth + " = -1;\n\
        } else if (a > b) {\n\
          S" + depth + " = 1;\n\
        } else if (a < b){\n\
          S" + depth + " = -1;\n\
        } else {\n\
          S" + depth + " = 0;\n\
        }\n";

        depth += 1;

        break;
      case 0x96: // fcmpg
        code += "\
        var b = " + generateStackPop() + ";\n\
        var a = " + generateStackPop() + ";\n";

        code += "\
        if (isNaN(a) || isNaN(b)) {\n\
          S" + depth + " = 1;\n\
        } else if (a > b) {\n\
          S" + depth + " = 1;\n\
        } else if (a < b){\n\
          S" + depth + " = -1;\n\
        } else {\n\
          S" + depth + " = 0;\n\
        }\n";

        depth += 1;

        break;
      case 0x97: // dcmpl
        code += "\
        var b = " + generateStackPop2() + ";\n\
        var a = " + generateStackPop2() + ";\n";

        code += "\
        if (isNaN(a) || isNaN(b)) {\n\
          S" + depth + " = -1;\n\
        } else if (a > b) {\n\
          S" + depth + " = 1;\n\
        } else if (a < b){\n\
          S" + depth + " = -1;\n\
        } else {\n\
          S" + depth + " = 0;\n\
        }\n";

        depth += 1;

        break;
      case 0x98: // dcmpg
        code += "\
        var b = " + generateStackPop2() + ";\n\
        var a = " + generateStackPop2() + ";\n";

        code += "\
        if (isNaN(a) || isNaN(b)) {\n\
          S" + depth + " = 1;\n\
        } else if (a > b) {\n\
          S" + depth + " = 1;\n\
        } else if (a < b){\n\
          S" + depth + " = -1;\n\
        } else {\n\
          S" + depth + " = 0;\n\
        }\n";

        depth += 1;

        break;
      case 0x99: // ifeq
        var jmp = ip + frame.read16signed();
        code += generateIf(jmp, generateStackPop() + " === " + 0);
        break;
      case 0x9a: // ifne
        var jmp = ip + frame.read16signed();
        code += generateIf(jmp, generateStackPop() + " !== " + 0);
        break;
      case 0x9b: // iflt
        var jmp = ip + frame.read16signed();
        code += generateIf(jmp, generateStackPop() + " < " + 0);
        break;
      case 0x9c: // ifge
        var jmp = ip + frame.read16signed();
        code += generateIf(jmp, generateStackPop() + " >= " + 0);
        break;
      case 0x9d: // ifgt
        var jmp = ip + frame.read16signed();
        code += generateIf(jmp, generateStackPop() + " > " + 0);
        break;
      case 0x9e: // ifle
        var jmp = ip + frame.read16signed();
        code += generateIf(jmp, generateStackPop() + " <= " + 0);
        break;
      case 0x9f: // if_icmpeq
        var jmp = ip + frame.read16signed();
        code += generateIf(jmp, generateStackPop() + " === " + generateStackPop());
        break;
      case 0xa0: // if_cmpne
        var jmp = ip + frame.read16signed();
        code += generateIf(jmp, generateStackPop() + " !== " + generateStackPop());
        break;
      case 0xa1: // if_icmplt
        var jmp = ip + frame.read16signed();
        code += generateIf(jmp, generateStackPop() + " > " + generateStackPop());
        break;
      case 0xa2: // if_icmpge
        var jmp = ip + frame.read16signed();
        code += generateIf(jmp, generateStackPop() + " <= " + generateStackPop());
        break;
      case 0xa3: // if_icmpgt
        var jmp = ip + frame.read16signed();
        code += generateIf(jmp, generateStackPop() + " < " + generateStackPop());
        break;
      case 0xa4: // if_icmple
        var jmp = ip + frame.read16signed();
        code += generateIf(jmp, generateStackPop() + " >= " + generateStackPop());
        break;
      case 0xa5: // if_acmpeq
        var jmp = ip + frame.read16signed();
        code += generateIf(jmp, generateStackPop() + " === " + generateStackPop());
        break;
      case 0xa6: // if_acmpne
        var jmp = ip + frame.read16signed();
        code += generateIf(jmp, generateStackPop() + " !== " + generateStackPop());
        break;
      case 0xc6: // ifnull
        var jmp = ip + frame.read16signed();
        code += generateIf(jmp, "!" + generateStackPop());
        break;
      case 0xc7: // ifnonnull
        var jmp = ip + frame.read16signed();
        code += generateIf(jmp, generateStackPop());
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
        code += generateStackPush(ip) + "\n\
          ip = " + jmp + ";\n\
          continue;\n";
        targetIPs.add(jmp);
        stackLayout.set(jmp, depth);
        stackLayout.set(frame.ip, depth);
        break;
      case 0xc9: // jsr_w
        var jmp = frame.read32();
        targetIPs.add(frame.ip); // ret will return here
        code += generateStackPush(ip) + "\n\
          ip = " + jmp + ";\n\
          continue;\n";
        targetIPs.add(jmp);
        stackLayout.set(jmp, depth);
        stackLayout.set(frame.ip, depth);
        break;
      case 0xa9: // ret
        code += "        ip = " + generateGetLocal(frame.read8()) + ";\n        continue;\n";
        break;
      case 0x85: // i2l
        code += generateStackPush2("Long.fromInt(" + generateStackPop() + ")");
        break;
      case 0x86: // i2f
        break;
      case 0x87: // i2d
        code += generateStackPush2(generateStackPop());
        break;
      case 0x88: // l2i
        code += generateStackPush(generateStackPop2() + ".toInt()");
        break;
      case 0x89: // l2f
        code += generateStackPush("Math.fround(" + generateStackPop2() + ".toNumber()");
        break;
      case 0x8a: // l2d
        code += generateStackPush2(generateStackPop2() + ".toNumber()");
        break;
      case 0x8b: // f2i
        code += generateStackPush("util.double2int(" + generateStackPop() + ")")
        break;
      case 0x8c: // f2l
        code += generateStackPush2("Long.fromNumber(" + generateStackPop() + ")")
        break;
      case 0x8d: // f2d
        code += generateStackPush2(generateStackPop());
        break;
      case 0x8e: // d2i
        code += generateStackPush("util.double2int(" + generateStackPop2() + ")");
        break;
      case 0x8f: // d2l
        code += generateStackPush2("util.double2long(" + generateStackPop2() + ")");
        break;
      case 0x91: // i2b
        code += generateStackPush("(" + generateStackPop() + " << 24) >> 24");
        break;
      case 0x92: // i2c
        code += generateStackPush(generateStackPop() + " & 0xffff");
        break;
      case 0x93: // i2s
        code += generateStackPush("(" + generateStackPop() + " << 16) >> 16");
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

        code += "\
        var val = " + generateStackPop() + ";\n\
        var jmp;\n\
        if (val < " + low + " || val > " + high + ") {\n\
          ip = " + def + ";\n\
        } else {\n\
          ip = offsets[val - " + low + "];\n\
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

          code += "\
          var val = " + generateStackPop() + ";\n\
          var jmp = offsets[val];\n\
          if (!jmp) {\n\
            jmp = " + def + "\n\
          }\n\
          ip = jmp;\n\
          continue;\n";
          break;
      case 0xbc: // newarray
        var type = "????ZCFDBSIJ"[frame.read8()];
        code += "\
        var size = " + generateStackPop() + ";\n\
        if (size < 0) {\n";
        code += generateStoreState(frame.ip) + "\
          ctx.raiseExceptionAndYield('java/lang/NegativeArraySizeException', size);\n\
        }\n";
        code += generateStackPush("ctx.newPrimitiveArray('" + type + "', size)");
        break;
      case 0xbd: // anewarray
        var idx = frame.read16();

        // Resolve the classinfo in advance.
        var classInfo = cp[idx];
        if (classInfo.tag) {
          classInfo = resolveCompiled(cp, op, idx);
        }

        var className = classInfo.className;
        if (className[0] !== "[") {
          className = "L" + className + ";";
        }
        className = "[" + className;

        code += "\
        var size = " + generateStackPop() + ";\n\
        if (size < 0) {\n";
        code += generateStoreState(frame.ip) + "\
          ctx.raiseExceptionAndYield('java/lang/NegativeArraySizeException', size);\n\
        }\n";
        code += generateStackPush("ctx.newArray('" + className + "', size)");
        break;
      case 0xc5: // multianewarray
        var idx = frame.read16();

        // Resolve the classinfo in advance.
        var classInfo = cp[idx];
        if (classInfo.tag) {
          classInfo = resolveCompiled(cp, op, idx);
        }

        var dimensions = frame.read8();

        code += "        var lengths = new Array(" + dimensions + ");\n";
        for (var i = 0; i < dimensions; i++) {
          code += "        lengths[" + i + "] = " + generateStackPop() + ";\n";
        }
        code += generateStackPush("ctx.newMultiArray('" + classInfo.className + "', lengths.reverse())");
        break;
      case 0xbe: // arraylength
        code += "\
        var obj = " + generateStackPop() + ";\n\
        if (!obj) {\n";
        code += generateStoreState(frame.ip) + "\
          ctx.raiseExceptionAndYield('java/lang/NullPointerException');\n\
        }\n";
        code += generateStackPush("obj.length");
        break;
      case 0xb4: // getfield
        var idx = frame.read16();

        // Resolve the field in advance.
        var field = cp[idx];
        if (field && field.tag) {
          field = resolveCompiled(cp, op, idx);
        }

        code += "\
        var field = cp[" + idx + "];\n\
        var obj = " + generateStackPop() + ";\n\
        if (!obj) {\n";
        code += generateStoreState(frame.ip) + "\
          ctx.raiseExceptionAndYield('java/lang/NullPointerException');\n\
        }\n";

        if (field.signature === "J" || field.signature === "D") {
          code += generateStackPush2("field.get(obj)");
        } else {
          code += generateStackPush("field.get(obj)");
        }
        break;
      case 0xb5: // putfield
        var idx = frame.read16();

        // Resolve the field in advance.
        var field = cp[idx];
        if (field && field.tag) {
          field = resolveCompiled(cp, op, idx);
        }

        if (field.signature === "J" || field.signature === "D") {
          code += "        var val = " + generateStackPop2() + ";\n";
        } else {
          code += "        var val = " + generateStackPop() + ";\n";
        }

        code += "\
        var field = cp[" + idx + "];\n\
        var obj = " + generateStackPop() + ";\n\
        if (!obj) {\n";
        code += generateStoreState(frame.ip) + "\
          ctx.raiseExceptionAndYield('java/lang/NullPointerException');\n\
        }\n\
        field.set(obj, val);\n"
        break;
      case 0xb2: // getstatic
        var idx = frame.read16();

        // Resolve the field in advance.
        var field = cp[idx];
        if (field && field.tag) {
          field = resolveCompiled(cp, op, idx);
        }

        code += "\
        var field = cp[" + idx + "];\n";

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

        if (field.signature === "J" || field.signature === "D") {
          code += generateStackPush2("value");
        } else {
          code += generateStackPush("value");
        }
        break;
      case 0xb3: // putstatic
        var idx = frame.read16();

        // Resolve the field in advance.
        var field = cp[idx];
        if (field.tag) {
          field = resolveCompiled(cp, op, idx);
        }

        code += "\
        var field = cp[" + idx + "];\n";

        if (!field.classInfo.isArrayClass) {
          code +="\
        if (!ctx.runtime.initialized[field.classInfo.className]) {\n" +
          generateStoreState(ip) + "\
          ctx.pushClassInitFrame(field.classInfo);\n\
          throw VM.Yield;\n\
        }\n";
        }

        if (field.signature === "J" || field.signature === "D") {
          code += "        var val = " + generateStackPop2() + ";\n";
        } else {
          code += "        var val = " + generateStackPop() + ";\n";
        }

        code += "\
        ctx.runtime.setStatic(field, val);\n";
        break;
      case 0xbb: // new
        var idx = frame.read16();

        // Resolve class in advance.
        var classInfo = cp[idx];
        if (classInfo.tag) {
          classInfo = resolveCompiled(cp, op, idx);
        }

        code += "\
        var classInfo = cp[" + idx + "];\n";

        if (!classInfo.isArrayClass) {
          code +="\
        if (!ctx.runtime.initialized[classInfo.className]) {\n" +
          generateStoreState(ip) + "\
          ctx.pushClassInitFrame(classInfo);\n\
          throw VM.Yield;\n\
        }\n";
        }

        code += generateStackPush("ctx.newObject(classInfo)");
        break;
      case 0xc0: // checkcast
        var idx = frame.read16();

        // Resolve class in advance.
        var classInfo = cp[idx];
        if (classInfo.tag) {
          classInfo = resolveCompiled(cp, op, idx);
        }

        code += "\
        var classInfo = cp[" + idx + "];\n\
        var obj = S" + (depth-1) + ";\n\
        //console.log('Checkcast: ' + obj);\n\
        if (obj) {\n\
          if (!obj.class.isAssignableTo(classInfo)) {\n";
        code += "  " + generateStoreState(frame.ip) + "\
            ctx.raiseExceptionAndYield('java/lang/ClassCastException',\n\
                                       obj.class.className + ' is not assignable to ' +\n\
                                       classInfo.className);\n\
            }\n\
        }\n";
        break;
      case 0xc1: // instanceof
        var idx = frame.read16();

        // Resolve class in advance.
        var classInfo = cp[idx];
        if (classInfo.tag) {
          classInfo = resolveCompiled(cp, op, idx);
        }

        code += "\
        var classInfo = cp[" + idx + "];\n\
        var obj = " + generateStackPop() + ";\n\
        //console.log('Instanceof: ' + obj)\n\
        var result = !obj ? false : obj.class.isAssignableTo(classInfo);\n";
        code += generateStackPush("result ? 1 : 0");
        break;
      case 0xbf: // athrow
        // TODO: Optimize by inlining throw_ call (because we can be much more clever)
        code +="\
        var obj = " + generateStackPop() + "\n\
        if (!obj) {\n";
        code += generateStoreState(frame.ip) + "\
          ctx.raiseExceptionAndYield('java/lang/NullPointerException');\n\
        }\n";
        code += generateStoreState(frame.ip) + "\
        newFrame = throw_(obj, ctx);\n\
        //console.log('THROWING EXCEPTION to: ');\n\
        return newFrame;\n";
        break;
      case 0xc2: // monitorenter
        code += "\
        var obj = " + generateStackPop() + "\n\
        if (!obj) {\n";
        code += generateStoreState(frame.ip) + "\
          ctx.raiseExceptionAndYield('java/lang/NullPointerException');\n\
        }\n";
        code += generateStoreState(frame.ip);
        code += "        ctx.monitorEnter(obj);\n";
        break;
      case 0xc3: // monitorexit
        code += "\
        var obj = " + generateStackPop() + ";\n\
        if (!obj) {\n";
        code += generateStoreState(frame.ip) + "\
          ctx.raiseExceptionAndYield('java/lang/NullPointerException');\n\
        }\n\
        ctx.monitorExit(obj);\n";
        break;
      case 0xc4: // wide
        var op = frame.read8();
        var opName = OPCODES[op];
        code += "// " + opName + " [0x" + op.toString(16) + "]\n";
        switch (op) {
          case 0x15: // iload
          case 0x17: // fload
          case 0x19: // aload
            code += generateStackPush(generateGetLocal(frame.read16()));
            break;
          case 0x16: // lload
          case 0x18: // dload
            code += generateStackPush2(generateGetLocal(frame.read16()));
            break;
          case 0x36: // istore
          case 0x38: // fstore
          case 0x3a: // astore
            code += generateSetLocal(frame.read16(), generateStackPop());
            break;
          case 0x37: // lstore
          case 0x39: // dstore
            code += generateSetLocal(frame.read16(), generateStackPop2());
            break;
          case 0x84: // iinc
            var idx = frame.read16();
            var val = frame.read16signed();
            code += generateSetLocal(idx, generateGetLocal(idx) + " + " + val);
            break;
          case 0xa9: // ret
            code += "        ip = " + generateGetLocal(frame.read16()) + ";\n        continue;\n";
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
      case 0xb9: // invokeinterface // TODO: Optimize if call isn't virtual or interface
        var idx = frame.read16();
        if (op === 0xb9) {
          /*var argsNumber =*/ frame.read8();
          /*var zero =*/ frame.read8();
        }
        var isStatic = (op === 0xb8);
        var toCallMethodInfo = cp[idx];
        // Resolve method in advance.
        if (toCallMethodInfo.tag) {
          toCallMethodInfo = resolveCompiled(cp, op, idx);
        }

        code += "        var toCallMethodInfo = cp[" + idx + "];\n";
        //code += "        console.log(toCallMethodInfo.classInfo.className + '.' + toCallMethodInfo.name + '.' + toCallMethodInfo.signature);\n";

        if (isStatic && !toCallMethodInfo.classInfo.isArrayClass) {
            code +="\
          if (!ctx.runtime.initialized[toCallMethodInfo.classInfo.className]) {\n" +
            generateStoreState(ip) + "\
            ctx.pushClassInitFrame(toCallMethodInfo.classInfo);\n\
            throw VM.Yield;\n\
          }\n";
        }

        var consumes = Signature.getINSlots(toCallMethodInfo.signature);
        if (!isStatic) {
          ++consumes;

          code += "\
        var obj = S" + (depth - consumes) + ";\n\
        if (!obj) {\n";
        code += generateStoreState(frame.ip) + "\
          ctx.raiseExceptionAndYield('java/lang/NullPointerException');\n\
        }\n";

          if (op === 0xb6 || op === 0xb9) {
            toCallMethodInfo.key = "I." + toCallMethodInfo.name + "." + toCallMethodInfo.signature;

            code += "\
        if (toCallMethodInfo.classInfo != obj.class) {\n\
          // Check if the method is already in the virtual method cache\n\
          if (obj.class.vmc[" + toCallMethodInfo.key + "]) {\n\
            toCallMethodInfo = obj.class.vmc[" + toCallMethodInfo.key + "];\n\
          } else {\n\
            toCallMethodInfo = CLASSES.getMethod(obj.class, " + toCallMethodInfo.key + ");\n\
          }\n\
        }\n";
          }
        }

        // Store state
        // TODO: Optimize if callee is compiled

        code += generateStoreState(frame.ip);

        depth -= consumes;

        code += "\
        var alternateImpl = toCallMethodInfo.alternateImpl;\n\
        if (alternateImpl) {\n\
          try {\n\
            Instrument.callPauseHooks(ctx.current());\n\
            Instrument.measure(alternateImpl, ctx, toCallMethodInfo);\n\
            Instrument.callResumeHooks(ctx.current());\n\
          } catch (e) {\n\
            Instrument.callResumeHooks(ctx.current());\n\
            throw e;\n\
          }\n\
        } else {\n\
          var callee = ctx.pushFrame(toCallMethodInfo, " + consumes + ");\n\
          if (callee.isSynchronized) {\n\
            if (!callee.lockObject) {\n\
              callee.lockObject = toCallMethodInfo.isStatic\n\
                                  ? toCallMethodInfo.classInfo.getClassObject(ctx)\n\
                                  : callee.getLocal(0);\n\
            }\n\
            ctx.monitorEnter(callee.lockObject);\n\
          }\n\
\n\
          if (!toCallMethodInfo.compiled && toCallMethodInfo.numCalled >= 1 && !toCallMethodInfo.dontCompile) {\n\
            try {\n\
              //console.log(VM.compile(toCallMethodInfo, ctx));\n\
              toCallMethodInfo.compiled = new Function('ctx', VM.compile(toCallMethodInfo, ctx));\n\
            } catch (e) {\n\
              toCallMethodInfo.dontCompile = true;\n\
            }\n\
          }\n\
\n\
          var newFrame;\n\
\n\
          try {\n\
            if (toCallMethodInfo.compiled) {\n\
              newFrame = toCallMethodInfo.compiled(ctx);\n\
            } else {\n\
              newFrame = VM.execute(ctx);\n\
            }\n";

          code +="\
          } catch (e) {\n\
            throw e;\n\
          }\n\
\n\
          if (newFrame !== frame) {\n\
            //console.log('EXIT: ' + newFrame.methodInfo.name);\n\
            return newFrame;\n\
          }\n\
        }\n";

        var returnType = toCallMethodInfo.signature[toCallMethodInfo.signature.length - 1];
        if (returnType === 'J' || returnType === 'D') {
          //code += "        console.log('RET: ' + frame.stack.read(2));\n";
          code += generateStackPush2("frame.stack.pop2()");
        } else if (returnType !== 'V') {
          //code += "        console.log('RET FROM " + toCallMethodInfo.name + ": ' + frame.stack.read(1));\n";
          code += generateStackPush("frame.stack.pop()");
        }
        code += "        frame.stack.length = 0;\n";

        break;
      case 0xb1: // return
        code += "        if (frame.lockObject) {\n\
          ctx.monitorExit(frame.lockObject);\n\
        }\n\
\n\
        var caller = ctx.popFrame();\n\
        return caller;\n";
        break;
      case 0xac: // ireturn
      case 0xae: // freturn
      case 0xb0: // areturn
        code += "        if (frame.lockObject) {\n\
          ctx.monitorExit(frame.lockObject);\n\
        }\n\
\n\
        var caller = ctx.popFrame();\n\
        caller.stack.push(" + generateStackPop() + ");\n\
        return caller;\n";
        break;
      case 0xad: // lreturn
      case 0xaf: // dreturn
        code += "        if (frame.lockObject) {\n\
          ctx.monitorExit(frame.lockObject);\n\
        }\n\
\n\
        var caller = ctx.popFrame();\n\
        caller.stack.push2(" + generateStackPop2() + ");\n\
        return caller;\n";
        break;
      default:
        throw new Error("NOT SUPPORTED: " + op.toString(16));
    }
  }

  code += "      default:\
        console.log('IP: ' + ip);\n\
        console.trace();\n";

  code += "    }\n";
  code += "  }\n";

  var localsCode = "";
  for (var i = 0; i < maxLocals+1; i++) {
    localsCode += "  var L" + i + " = frame.getLocal(" + i + ");\n";
  }
  if (maxDepth > 0) {
    localsCode += "  var";
    for (var i = 0; i < maxDepth; i++) {
      localsCode += " S" + i + ",";
    }
    localsCode = localsCode.slice(0, -1) + ";\n";
  }

  var generatedCode = "\n  var frame = ctx.current();\n\
  var cp = frame.cp;\n" + localsCode + "\
  var ip = frame.ip;\n\
\n\
// I'm " + methodInfo.classInfo.className + "." + methodInfo.name + "." + methodInfo.signature + "\
\n\
\n\
  if (ip !== 0) {\n";

  for (var i = 0; i < maxDepth; i++) {
    generatedCode += "\
    if (frame.stack.length > 0) {\n\
      S" + i + " = frame.stack.shift();\n\
      //console.log('" + methodInfo.classInfo.className + "." + methodInfo.name + "." + methodInfo.signature + " ON " + i + ": ' + S" + i + ");\n\
    }\n";
  }

  generatedCode += "  }\n\n\
  while (true) {\n\
    switch (ip) {\n" + code;

  generatedCases.forEach(function(ip) {
    if (!targetIPs.has(ip)) {
      generatedCode = generatedCode.replace("case " + ip + ":", "");
    }
  });

  return generatedCode;
}
