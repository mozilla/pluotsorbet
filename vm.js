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

VM.execute = function(ctx) {
    var frame = ctx.current();

    var cp = frame.cp;
    var stack = frame.stack;

    function pushFrame(methodInfo, consumes) {
        var caller = frame;
        frame = ctx.pushFrame(methodInfo, consumes);
        stack = frame.stack;
        cp = frame.cp;
        if (ACCESS_FLAGS.isSynchronized(methodInfo.access_flags)) {
            frame.lockObject = ACCESS_FLAGS.isStatic(methodInfo.access_flags)
                               ? methodInfo.classInfo.getClassObject(ctx)
                               : frame.getLocal(0);
            ctx.monitorEnter(frame.lockObject);
        }
        return frame;
    }

    function popFrame(consumes) {
        if (frame.lockObject)
            ctx.monitorExit(frame.lockObject);
        var callee = frame;
        frame = ctx.popFrame();
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
        return frame;
    }

    function buildExceptionLog(ex, stackTrace) {
        var className = ex.class.className;
        var detailMessage = util.fromJavaString(CLASSES.getField(ex.class, "detailMessage", "Ljava/lang/String;", false).get(ex));
        return className + ": " + (detailMessage || "") + "\n" + stackTrace.join("\n") + "\n\n";
    }

    function throw_(ex, ctx) {
        var exClass = ex.class;

        var stackTrace = [];

        do {
            var exception_table = frame.methodInfo.exception_table;
            var handler_pc = null;
            for (var i=0; exception_table && i<exception_table.length; i++) {
                if (frame.ip >= exception_table[i].start_pc && frame.ip <= exception_table[i].end_pc) {
                    if (exception_table[i].catch_type === 0) {
                        handler_pc = exception_table[i].handler_pc;
                    } else {
                        var classInfo = resolve(OPCODES.athrow, exception_table[i].catch_type);
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

                if (VM.DEBUG_PRINT_ALL_EXCEPTIONS) {
                    console.error(buildExceptionLog(ex, stackTrace));
                }

                return;
            }
            popFrame(0);
        } while (frame.methodInfo);
        ctx.kill();
        throw new Error(buildExceptionLog(ex, stackTrace));
    }

    function checkArrayAccess(refArray, idx) {
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

    function classInitCheck(classInfo, ip) {
        if (classInfo.isArrayClass || ctx.runtime.initialized[classInfo.className])
            return;
        frame.ip = ip;
        ctx.pushClassInitFrame(classInfo);
        throw VM.Yield;
    }

    function resolve(op, idx) {
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
            var classInfo = resolve(op, constant.class_index);
            var fieldName = cp[cp[constant.name_and_type_index].name_index].bytes;
            var signature = cp[cp[constant.name_and_type_index].signature_index].bytes;
            constant = CLASSES.getField(classInfo, fieldName, signature, (op === 0xb2 || op == 0xb3));
            if (!constant)
                ctx.raiseExceptionAndYield("java/lang/RuntimeException",
                                   classInfo.className + "." + fieldName + "." + signature + " not found");
            break;
        case TAGS.CONSTANT_Methodref:
        case TAGS.CONSTANT_InterfaceMethodref:
            var classInfo = resolve(op, constant.class_index);
            var methodName = cp[cp[constant.name_and_type_index].name_index].bytes;
            var signature = cp[cp[constant.name_and_type_index].signature_index].bytes;
            constant = CLASSES.getMethod(classInfo, methodName, signature, op === 0xb8, op !== 0xb8);
            if (!constant)
                ctx.raiseExceptionAndYield("java/lang/RuntimeException",
                                   classInfo.className + "." + methodName + "." + signature + " not found");
            break;
        default:
            throw new Error("not support constant type");
        }
        return cp[idx] = constant;
    }

    var funcTable = {
      // nop
      0x00: function() {
      },
      // aconst_null
      0x01: function() {
        stack.push(null);
      },
      // aconst_m1
      0x02: function() {
        stack.push(-1);
      },
      // iconst_0
      0x03: function() {
        stack.push(0);
      },
      // fconst_0
      0x0b: function() {
        stack.push(0);
      },
      // dconst_0
      0x0e: function() {
        stack.push2(0);
      },
      // iconst_1
      0x04: function() {
        stack.push(1);
      },
      // fconst_1
      0x0c: function() {
        stack.push(1);
      },
      // dconst_1
      0x0f: function() {
        stack.push2(1);
      },
      // iconst_2
      0x05: function() {
        stack.push(2);
      },
      // fconst_2
      0x0d: function() {
        stack.push(2);
      },
      // iconst_3
      0x06: function() {
        stack.push(3);
      },
      // iconst_4
      0x07: function() {
        stack.push(4);
      },
      // iconst_5
      0x08: function() {
        stack.push(5);
      },
      // lconst_0
      0x09: function() {
        stack.push2(Long.fromInt(0));
      },
      // lconst_1
      0x0a: function() {
        stack.push2(Long.fromInt(1));
      },
      // bipush
      0x10: function() {
        stack.push(frame.read8signed());
      },
      // sipush
      0x11: function() {
        stack.push(frame.read16signed());
      },
      // ldc
      0x12: function() {
        var idx = frame.read8();
        var constant = cp[idx];
        if (constant.tag)
            constant = resolve(0x12, idx);
        stack.push(constant);
      },
      // ldc_w
      0x13: function() {
        var idx = frame.read16();
        var constant = cp[idx];
        if (constant.tag)
            constant = resolve(0x13, idx);
        stack.push(constant);
      },
      // ldc2_w
      0x14: function() {
        var idx = frame.read16();
        var constant = cp[idx];
        if (constant.tag)
            constant = resolve(0x14, idx);
        stack.push2(constant);
      },
      // iload
      0x15: function() {
        stack.push(frame.getLocal(frame.read8()));
      },
      // fload
      0x17: function() {
        stack.push(frame.getLocal(frame.read8()));
      },
      // aload
      0x19: function() {
        stack.push(frame.getLocal(frame.read8()));
      },
      // lload
      0x16: function() {
        stack.push2(frame.getLocal(frame.read8()));
      },
      // dload
      0x18: function() {
        stack.push2(frame.getLocal(frame.read8()));
      },
      // iload_0
      0x1a: function() {
        stack.push(frame.getLocal(0));
      },
      // fload_0
      0x22: function() {
        stack.push(frame.getLocal(0));
      },
      // aload_0
      0x2a: function() {
        stack.push(frame.getLocal(0));
      },
      // iload_1
      0x1b: function() {
        stack.push(frame.getLocal(1));
      },
      // fload_1
      0x23: function() {
        stack.push(frame.getLocal(1));
      },
      // aload_1
      0x2b: function() {
        stack.push(frame.getLocal(1));
      },
      // iload_2
      0x1c: function() {
        stack.push(frame.getLocal(2));
      },
      // fload_2
      0x24: function() {
        stack.push(frame.getLocal(2));
      },
      // aload_2
      0x2c: function() {
        stack.push(frame.getLocal(2));
      },
      // iload_3
      0x1d: function() {
        stack.push(frame.getLocal(3));
      },
      // fload_3
      0x25: function() {
        stack.push(frame.getLocal(3));
      },
      // aload_3
      0x2d: function() {
        stack.push(frame.getLocal(3));
      },
      // lload_0
      0x1e: function() {
        stack.push2(frame.getLocal(0));
      },
      // dload_0
      0x26: function() {
        stack.push2(frame.getLocal(0));
      },
      // lload_1
      0x1f: function() {
        stack.push2(frame.getLocal(1));
      },
      // dload_1
      0x27: function() {
        stack.push2(frame.getLocal(1));
      },
      // lload_2
      0x20: function() {
        stack.push2(frame.getLocal(2));
      },
      // dload_2
      0x28: function() {
        stack.push2(frame.getLocal(2));
      },
      // lload_3
      0x21: function() {
        stack.push2(frame.getLocal(3));
      },
      // dload_3
      0x29: function() {
        stack.push2(frame.getLocal(3));
      },
      // iaload
      0x2e: function() {
        var idx = stack.pop();
        var refArray = stack.pop();
        if (!checkArrayAccess(refArray, idx))
            return;
        stack.push(refArray[idx]);
      },
      // faload
      0x30: function() {
        var idx = stack.pop();
        var refArray = stack.pop();
        if (!checkArrayAccess(refArray, idx))
            return;
        stack.push(refArray[idx]);
      },
      // aaload
      0x32: function() {
        var idx = stack.pop();
        var refArray = stack.pop();
        if (!checkArrayAccess(refArray, idx))
            return;
        stack.push(refArray[idx]);
      },
      // baload
      0x33: function() {
        var idx = stack.pop();
        var refArray = stack.pop();
        if (!checkArrayAccess(refArray, idx))
            return;
        stack.push(refArray[idx]);
      },
      // caload
      0x34: function() {
        var idx = stack.pop();
        var refArray = stack.pop();
        if (!checkArrayAccess(refArray, idx))
            return;
        stack.push(refArray[idx]);
      },
      // saload
      0x35: function() {
        var idx = stack.pop();
        var refArray = stack.pop();
        if (!checkArrayAccess(refArray, idx))
            return;
        stack.push(refArray[idx]);
      },
      // laload
      0x2f: function() {
        var idx = stack.pop();
        var refArray = stack.pop();
        if (!checkArrayAccess(refArray, idx))
            return;
        stack.push2(refArray[idx]);
      },
      // daload
      0x31: function() {
        var idx = stack.pop();
        var refArray = stack.pop();
        if (!checkArrayAccess(refArray, idx))
            return;
        stack.push2(refArray[idx]);
      },
      // istore
      0x36: function() {
        frame.setLocal(frame.read8(), stack.pop());
      },
      // fstore
      0x38: function() {
        frame.setLocal(frame.read8(), stack.pop());
      },
      // astore
      0x3a: function() {
        frame.setLocal(frame.read8(), stack.pop());
      },
      // lstore
      0x37: function() {
        frame.setLocal(frame.read8(), stack.pop2());
      },
      // dstore
      0x39: function() {
        frame.setLocal(frame.read8(), stack.pop2());
      },
      // istore_0
      0x3b: function() {
        frame.setLocal(0, stack.pop());
      },
      // fstore_0
      0x43: function() {
        frame.setLocal(0, stack.pop());
      },
      // astore_0
      0x4b: function() {
        frame.setLocal(0, stack.pop());
      },
      // istore_1
      0x3c: function() {
        frame.setLocal(1, stack.pop());
      },
      // fstore_1
      0x44: function() {
        frame.setLocal(1, stack.pop());
      },
      // astore_1
      0x4c: function() {
        frame.setLocal(1, stack.pop());
      },
      // istore_2
      0x3d: function() {
        frame.setLocal(2, stack.pop());
      },
      // fstore_2
      0x45: function() {
        frame.setLocal(2, stack.pop());
      },
      // astore_1
      0x4d: function() {
        frame.setLocal(2, stack.pop());
      },
      // istore_3
      0x3e: function() {
        frame.setLocal(3, stack.pop());
      },
      // fstore_3
      0x46: function() {
        frame.setLocal(3, stack.pop());
      },
      // astore_3
      0x4e: function() {
        frame.setLocal(3, stack.pop());
      },
      // lstore_0
      0x3f: function() {
        frame.setLocal(0, stack.pop2());
      },
      // dstore_0
      0x47: function() {
        frame.setLocal(0, stack.pop2());
      },
      // lstore_1
      0x40: function() {
        frame.setLocal(1, stack.pop2());
      },
      // dstore_1
      0x48: function() {
        frame.setLocal(1, stack.pop2());
      },
      // lstore_2
      0x41: function() {
        frame.setLocal(2, stack.pop2());
      },
      // dstore_2
      0x49: function() {
        frame.setLocal(2, stack.pop2());
      },
      // lstore_3
      0x42: function() {
        frame.setLocal(3, stack.pop2());
      },
      // dstore_3
      0x4a: function() {
        frame.setLocal(3, stack.pop2());
      },
      // iastore
      0x4f: function() {
        var val = stack.pop();
        var idx = stack.pop();
        var refArray = stack.pop();
        if (!checkArrayAccess(refArray, idx))
            return;
        refArray[idx] = val;
      },
      // fastore
      0x51: function() {
        var val = stack.pop();
        var idx = stack.pop();
        var refArray = stack.pop();
        if (!checkArrayAccess(refArray, idx))
            return;
        refArray[idx] = val;
      },
      // bastore
      0x54: function() {
        var val = stack.pop();
        var idx = stack.pop();
        var refArray = stack.pop();
        if (!checkArrayAccess(refArray, idx))
            return;
        refArray[idx] = val;
      },
      // castore
      0x55: function() {
        var val = stack.pop();
        var idx = stack.pop();
        var refArray = stack.pop();
        if (!checkArrayAccess(refArray, idx))
            return;
        refArray[idx] = val;
      },
      // sastore
      0x56: function() {
        var val = stack.pop();
        var idx = stack.pop();
        var refArray = stack.pop();
        if (!checkArrayAccess(refArray, idx))
            return;
        refArray[idx] = val;
      },
      // lastore
      0x50: function() {
        var val = stack.pop2();
        var idx = stack.pop();
        var refArray = stack.pop();
        if (!checkArrayAccess(refArray, idx))
            return;
        refArray[idx] = val;
      },
      // dastore
      0x52: function() {
        var val = stack.pop2();
        var idx = stack.pop();
        var refArray = stack.pop();
        if (!checkArrayAccess(refArray, idx))
            return;
        refArray[idx] = val;
      },
      // aastore
      0x53: function() {
        var val = stack.pop();
        var idx = stack.pop();
        var refArray = stack.pop();
        if (!checkArrayAccess(refArray, idx))
            return;
        if (val && !val.class.isAssignableTo(refArray.class.elementClass)) {
            ctx.raiseExceptionAndYield("java/lang/ArrayStoreException");
            return;
        }
        refArray[idx] = val;
      },
      // pop
      0x57: function() {
        stack.pop();
      },
      // pop2
      0x58: function() {
        stack.pop2();
      },
      // dup
      0x59: function() {
        var val = stack.pop();
        stack.push(val);
        stack.push(val);
      },
      // dup_x1
      0x5a: function() {
        var a = stack.pop();
        var b = stack.pop();
        stack.push(a);
        stack.push(b);
        stack.push(a);
      },
      // dup_x2
      0x5b: function() {
        var a = stack.pop();
        var b = stack.pop();
        var c = stack.pop();
        stack.push(a);
        stack.push(c);
        stack.push(b);
        stack.push(a);
      },
      // dup2
      0x5c: function() {
        var a = stack.pop();
        var b = stack.pop();
        stack.push(b);
        stack.push(a);
        stack.push(b);
        stack.push(a);
      },
      // dup2_x1
      0x5d: function() {
        var a = stack.pop();
        var b = stack.pop();
        var c = stack.pop();
        stack.push(b);
        stack.push(a);
        stack.push(c);
        stack.push(b);
        stack.push(a);
      },
      // dup2_x2
      0x5e: function() {
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
      },
      // swap
      0x5f: function() {
        var a = stack.pop();
        var b = stack.pop();
        stack.push(a);
        stack.push(b);
      },
      // iinc
      0x84: function() {
        var idx = frame.read8();
        var val = frame.read8signed();
        frame.setLocal(idx, frame.getLocal(idx) + val);
      },
      // iadd
      0x60: function() {
        stack.push((stack.pop() + stack.pop())|0);
      },
      // ladd
      0x61: function() {
        stack.push2(stack.pop2().add(stack.pop2()));
      },
      // fadd
      0x62: function() {
        stack.push(Math.fround(stack.pop() + stack.pop()));
      },
      // dadd
      0x63: function() {
        stack.push2(stack.pop2() + stack.pop2());
      },
      // isub
      0x64: function() {
        stack.push((- stack.pop() + stack.pop())|0);
      },
      // lsub
      0x65: function() {
        stack.push2(stack.pop2().negate().add(stack.pop2()));
      },
      // fsub
      0x66: function() {
        stack.push(Math.fround(- stack.pop() + stack.pop()));
      },
      // dsub
      0x67: function() {
        stack.push2(- stack.pop2() + stack.pop2());
      },
      // imul
      0x68: function() {
        stack.push(Math.imul(stack.pop(), stack.pop()));
      },
      // lmul
      0x69: function() {
        stack.push2(stack.pop2().multiply(stack.pop2()));
      },
      // fmul
      0x6a: function() {
        stack.push(Math.fround(stack.pop() * stack.pop()));
      },
      // dmul
      0x6b: function() {
        stack.push2(stack.pop2() * stack.pop2());
      },
      // idiv
      0x6c: function() {
        var b = stack.pop();
        var a = stack.pop();
        if (!b) {
            ctx.raiseExceptionAndYield("java/lang/ArithmeticException", "/ by zero");
            return;
        }
        stack.push((a === util.INT_MIN && b === -1) ? a : ((a / b)|0));
      },
      // ldiv
      0x6d: function() {
        var b = stack.pop2();
        var a = stack.pop2();
        if (b.isZero()) {
            ctx.raiseExceptionAndYield("java/lang/ArithmeticException", "/ by zero");
            return;
        }
        stack.push2(a.div(b));
      },
      // fdiv
      0x6e: function() {
        var b = stack.pop();
        var a = stack.pop();
        stack.push(Math.fround(a / b));
      },
      // ddiv
      0x6f: function() {
        var b = stack.pop2();
        var a = stack.pop2();
        stack.push2(a / b);
      },
      // irem
      0x70: function() {
        var b = stack.pop();
        var a = stack.pop();
        if (!b) {
            ctx.raiseExceptionAndYield("java/lang/ArithmeticException", "/ by zero");
            return;
        }
        stack.push(a % b);
      },
      // lrem
      0x71: function() {
        var b = stack.pop2();
        var a = stack.pop2();
        if (b.isZero()) {
            ctx.raiseExceptionAndYield("java/lang/ArithmeticException", "/ by zero");
            return;
        }
        stack.push2(a.modulo(b));
      },
      // frem
      0x72: function() {
        var b = stack.pop();
        var a = stack.pop();
        stack.push(Math.fround(a % b));
      },
      // drem
      0x73: function() {
        var b = stack.pop2();
        var a = stack.pop2();
        stack.push2(a % b);
      },
      // ineg
      0x74: function() {
        stack.push((- stack.pop())|0);
      },
      // lneg
      0x75: function() {
        stack.push2(stack.pop2().negate());
      },
      // fneg
      0x76: function() {
        stack.push(- stack.pop());
      },
      // dneg
      0x77: function() {
        stack.push2(- stack.pop2());
      },
      // ishl
      0x78: function() {
        var b = stack.pop();
        var a = stack.pop();
        stack.push(a << b);
      },
      // lshl
      0x79: function() {
        var b = stack.pop();
        var a = stack.pop2();
        stack.push2(a.shiftLeft(b));
      },
      // ishr
      0x7a: function() {
        var b = stack.pop();
        var a = stack.pop();
        stack.push(a >> b);
      },
      // lshr
      0x7b: function() {
        var b = stack.pop();
        var a = stack.pop2();
        stack.push2(a.shiftRight(b));
      },
      // iushr
      0x7c: function() {
        var b = stack.pop();
        var a = stack.pop();
        stack.push(a >>> b);
      },
      // lushr
      0x7d: function() {
        var b = stack.pop();
        var a = stack.pop2();
        stack.push2(a.shiftRightUnsigned(b));
      },
      // iand
      0x7e: function() {
        stack.push(stack.pop() & stack.pop());
      },
      // land
      0x7f: function() {
        stack.push2(stack.pop2().and(stack.pop2()));
      },
      // ior
      0x80: function() {
        stack.push(stack.pop() | stack.pop());
      },
      // lor
      0x81: function() {
        stack.push2(stack.pop2().or(stack.pop2()));
      },
      // ixor
      0x82: function() {
        stack.push(stack.pop() ^ stack.pop());
      },
      // lxor
      0x83: function() {
        stack.push2(stack.pop2().xor(stack.pop2()));
      },
      // lcmp
      0x94: function() {
        var b = stack.pop2();
        var a = stack.pop2();
        if (a.greaterThan(b)) {
            stack.push(1);
        } else if (a.lessThan(b)) {
            stack.push(-1);
        } else {
            stack.push(0);
        }
      },
      // fcmpl
      0x95: function() {
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
      },
      // fcmpg
      0x96: function() {
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
      },
      // dcmpl
      0x97: function() {
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
      },
      // dcmpg
      0x98: function() {
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
      },
      // ifeq
      0x99: function() {
        var jmp = frame.ip - 1 + frame.read16signed();
        frame.ip = stack.pop() === 0 ? jmp : frame.ip;
      },
      // ifne
      0x9a: function() {
        var jmp = frame.ip - 1 + frame.read16signed();
        frame.ip = stack.pop() !== 0 ? jmp : frame.ip;
      },
      // iflt
      0x9b: function() {
        var jmp = frame.ip - 1 + frame.read16signed();
        frame.ip = stack.pop() < 0 ? jmp : frame.ip;
      },
      // ifge
      0x9c: function() {
        var jmp = frame.ip - 1 + frame.read16signed();
        frame.ip = stack.pop() >= 0 ? jmp : frame.ip;
      },
      // ifgt
      0x9d: function() {
        var jmp = frame.ip - 1 + frame.read16signed();
        frame.ip = stack.pop() > 0 ? jmp : frame.ip;
      },
      // ifle
      0x9e: function() {
        var jmp = frame.ip - 1 + frame.read16signed();
        frame.ip = stack.pop() <= 0 ? jmp : frame.ip;
      },
      // if_icmpeq
      0x9f: function() {
        var jmp = frame.ip - 1 + frame.read16signed();
        frame.ip = stack.pop() === stack.pop() ? jmp : frame.ip;
      },
      // if_cmpne
      0xa0: function() {
        var jmp = frame.ip - 1 + frame.read16signed();
        frame.ip = stack.pop() !== stack.pop() ? jmp : frame.ip;
      },
      // if_icmplt
      0xa1: function() {
        var jmp = frame.ip - 1 + frame.read16signed();
        frame.ip = stack.pop() > stack.pop() ? jmp : frame.ip;
      },
      // if_icmpge
      0xa2: function() {
        var jmp = frame.ip - 1 + frame.read16signed();
        frame.ip = stack.pop() <= stack.pop() ? jmp : frame.ip;
      },
      // if_icmpgt
      0xa3: function() {
        var jmp = frame.ip - 1 + frame.read16signed();
        frame.ip = stack.pop() < stack.pop() ? jmp : frame.ip;
      },
      // if_icmple
      0xa4: function() {
        var jmp = frame.ip - 1 + frame.read16signed();
        frame.ip = stack.pop() >= stack.pop() ? jmp : frame.ip;
      },
      // if_acmpeq
      0xa5: function() {
        var jmp = frame.ip - 1 + frame.read16signed();
        frame.ip = stack.pop() === stack.pop() ? jmp : frame.ip;
      },
      // if_acmpne
      0xa6: function() {
        var jmp = frame.ip - 1 + frame.read16signed();
        frame.ip = stack.pop() !== stack.pop() ? jmp : frame.ip;
      },
      // ifnull
      0xc6: function() {
        var jmp = frame.ip - 1 + frame.read16signed();
        frame.ip = !stack.pop() ? jmp : frame.ip;
      },
      // ifnonnull
      0xc7: function() {
        var jmp = frame.ip - 1 + frame.read16signed();
        frame.ip = stack.pop() ? jmp : frame.ip;
      },
      // goto
      0xa7: function() {
        frame.ip += frame.read16signed() - 1;
      },
      // goto_w
      0xc8: function() {
        frame.ip += frame.read32signed() - 1;
      },
      // jsr
      0xa8: function() {
        var jmp = frame.read16();
        stack.push(frame.ip);
        frame.ip = jmp;
      },
      // jsr_w
      0xc9: function() {
        var jmp = frame.read32();
        stack.push(frame.ip);
        frame.ip = jmp;
      },
      // ret
      0xa9: function() {
        frame.ip = frame.getLocal(frame.read8());
      },
      // i2l
      0x85: function() {
        stack.push2(Long.fromInt(stack.pop()));
      },
      // i2f
      0x86: function() {
      },
      // i2d
      0x87: function() {
        stack.push2(stack.pop());
      },
      // l2i
      0x88: function() {
        stack.push(stack.pop2().toInt());
      },
      // l2f
      0x89: function() {
        stack.push(Math.fround(stack.pop2().toNumber()));
      },
      // l2d
      0x8a: function() {
        stack.push2(stack.pop2().toNumber());
      },
      // f2i
      0x8b: function() {
        stack.push(util.double2int(stack.pop()));
      },
      // f2l
      0x8c: function() {
        stack.push2(Long.fromNumber(stack.pop()));
      },
      // f2d
      0x8d: function() {
        stack.push2(stack.pop());
      },
      // d2i
      0x8e: function() {
        stack.push(util.double2int(stack.pop2()));
      },
      // d2l
      0x8f: function() {
        stack.push2(util.double2long(stack.pop2()));
      },
      // d2f
      0x90: function() {
        stack.push(Math.fround(stack.pop2()));
      },
      // i2b
      0x91: function() {
        stack.push((stack.pop() << 24) >> 24);
      },
      // i2c
      0x92: function() {
        stack.push(stack.pop() & 0xffff);
      },
      // i2s
      0x93: function() {
        stack.push((stack.pop() << 16) >> 16);
      },
      // tableswitch
      0xaa: function() {
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
      },
      // lookupswitch
      0xab: function() {
        var startip = frame.ip;
        while ((frame.ip & 3) != 0)
            frame.ip++;
        var jmp = frame.read32signed();
        var size = frame.read32();
        var val = frame.stack.pop();

        for (var i=0; i<size; i++) {
            var key = frame.read32signed();
            var offset = frame.read32signed();
            if (key === val) {
                jmp = offset;
            }
            if (key >= val) {
                break;
            }
        }
        frame.ip = startip - 1 + jmp;
      },
      // newarray
      0xbc: function() {
        var type = frame.read8();
        var size = stack.pop();
        if (size < 0) {
            ctx.raiseExceptionAndYield("java/lang/NegativeArraySizeException", size);
            return;
        }
        stack.push(ctx.newPrimitiveArray("????ZCFDBSIJ"[type], size));
      },
      // anewarray
      0xbd: function() {
        var idx = frame.read16();
        var classInfo = cp[idx];
        if (classInfo.tag)
            classInfo = resolve(0xbd, idx);
        var size = stack.pop();
        if (size < 0) {
            ctx.raiseExceptionAndYield("java/lang/NegativeArraySizeException", size);
            return;
        }
        var className = classInfo.className;
        if (className[0] !== "[")
            className = "L" + className + ";";
        className = "[" + className;
        stack.push(ctx.newArray(className, size));
      },
      // multinewarray
      0xc5: function() {
        var idx = frame.read16();
        var classInfo = cp[idx];
        if (classInfo.tag)
            classInfo = resolve(0xc5, idx);
        var dimensions = frame.read8();
        var lengths = new Array(dimensions);
        for (var i=0; i<dimensions; i++)
            lengths[i] = stack.pop();
        stack.push(ctx.newMultiArray(classInfo.className, lengths.reverse()));
      },
      // arraylength
      0xbe: function() {
        var obj = stack.pop();
        if (!obj) {
            ctx.raiseExceptionAndYield("java/lang/NullPointerException");
            return;
        }
        stack.push(obj.length);
      },
      // getfield
      0xb4: function() {
        var idx = frame.read16();
        var field = cp[idx];
        if (field.tag)
            field = resolve(0xb4, idx);
        var obj = stack.pop();
        if (!obj) {
            ctx.raiseExceptionAndYield("java/lang/NullPointerException");
            return;
        }
        stack.pushType(field.signature, field.get(obj));
      },
      // putfield
      0xb5: function() {
        var idx = frame.read16();
        var field = cp[idx];
        if (field.tag)
            field = resolve(0xb5, idx);
        var val = stack.popType(field.signature);
        var obj = stack.pop();
        if (!obj) {
            ctx.raiseExceptionAndYield("java/lang/NullPointerException");
            return;
        }
        field.set(obj, val);
      },
      // getstatic
      0xb2: function() {
        var idx = frame.read16();
        var field = cp[idx];
        if (field.tag)
            field = resolve(0xb2, idx);
        classInitCheck(field.classInfo, frame.ip-3);
        var value = ctx.runtime.getStatic(field);
        if (typeof value === "undefined") {
            value = util.defaultValue(field.signature);
        }
        stack.pushType(field.signature, value);
      },
      // putstatic
      0xb3: function() {
        var idx = frame.read16();
        var field = cp[idx];
        if (field.tag)
            field = resolve(0xb3, idx);
        classInitCheck(field.classInfo, frame.ip-3);
        ctx.runtime.setStatic(field, stack.popType(field.signature));
      },
      // new
      0xbb: function() {
        var idx = frame.read16();
        var classInfo = cp[idx];
        if (classInfo.tag)
            classInfo = resolve(0xbb, idx);
        classInitCheck(classInfo, frame.ip-3);
        stack.push(ctx.newObject(classInfo));
      },
      // checkcast
      0xc0: function() {
        var idx = frame.read16();
        var classInfo = cp[idx];
        if (classInfo.tag)
            classInfo = resolve(0xc0, idx);
        var obj = stack[stack.length - 1];
        if (obj) {
            if (!obj.class.isAssignableTo(classInfo)) {
                ctx.raiseExceptionAndYield("java/lang/ClassCastException",
                                   obj.class.className + " is not assignable to " +
                                   classInfo.className);
                return;
            }
        }
      },
      // instanceof
      0xc1: function() {
        var idx = frame.read16();
        var classInfo = cp[idx];
        if (classInfo.tag)
            classInfo = resolve(0xc1, idx);
        var obj = stack.pop();
        var result = !obj ? false : obj.class.isAssignableTo(classInfo);
        stack.push(result ? 1 : 0);
      },
      // athrow
      0xbf: function() {
        var obj = stack.pop();
        if (!obj) {
            ctx.raiseExceptionAndYield("java/lang/NullPointerException");
            return;
        }
        throw_(obj, ctx);
      },
      // monitorenter
      0xc2: function() {
        var obj = stack.pop();
        if (!obj) {
            ctx.raiseExceptionAndYield("java/lang/NullPointerException");
            return;
        }
        ctx.monitorEnter(obj);
      },
      // monitorexit
      0xc3: function() {
        var obj = stack.pop();
        if (!obj) {
            ctx.raiseExceptionAndYield("java/lang/NullPointerException");
            return;
        }
        ctx.monitorExit(obj);
      },
      // wide
      0xc4: function() {
        switch (frame.read8()) {
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
            break;
        default:
            var opName = OPCODES[op];
            throw new Error("Wide opcode " + opName + " [" + op + "] not supported.");
        }
      },
      // invokevirtual
      0xb6: function() {
        var startip = frame.ip - 1;
        var idx = frame.read16();
        var isStatic = false;
        var methodInfo = cp[idx];
        if (methodInfo.tag) {
            methodInfo = resolve(0xb6, idx);
            if (isStatic)
                classInitCheck(methodInfo.classInfo, startip);
        }
        var consumes = Signature.getINSlots(methodInfo.signature);
        if (!isStatic) {
            ++consumes;
            var obj = stack[stack.length - consumes];
            if (!obj) {
                ctx.raiseExceptionAndYield("java/lang/NullPointerException");
                return;
            }
            if (methodInfo.classInfo != obj.class)
                methodInfo = CLASSES.getMethod(obj.class, methodInfo.name, methodInfo.signature, false, true);
        }

        if (VM.DEBUG) {
            VM.trace("invoke", ctx.thread.pid, methodInfo);
        }

        var Alt = ACCESS_FLAGS.isNative(methodInfo.access_flags) ? Native : Override.hasMethod(methodInfo) ? Override : null;
        if (Alt) {
            try {
                Instrument.callPauseHooks(ctx.current());
                Alt.invoke(ctx, methodInfo);
                Instrument.callResumeHooks(ctx.current());
            } catch (e) {
                Instrument.callResumeHooks(ctx.current());
                if (!e.class) {
                    throw e;
                }
                throw_(e, ctx);
            }
            return;
        }
        pushFrame(methodInfo, consumes);
      },
      // invokespecial
      0xb7: function() {
        var startip = frame.ip - 1;
        var idx = frame.read16();
        var isStatic = false;
        var methodInfo = cp[idx];
        if (methodInfo.tag) {
            methodInfo = resolve(0xb7, idx);
            if (isStatic)
                classInitCheck(methodInfo.classInfo, startip);
        }
        var consumes = Signature.getINSlots(methodInfo.signature);
        if (!isStatic) {
            ++consumes;
            var obj = stack[stack.length - consumes];
            if (!obj) {
                ctx.raiseExceptionAndYield("java/lang/NullPointerException");
                return;
            }
        }

        if (VM.DEBUG) {
            VM.trace("invoke", ctx.thread.pid, methodInfo);
        }

        var Alt = ACCESS_FLAGS.isNative(methodInfo.access_flags) ? Native : Override.hasMethod(methodInfo) ? Override : null;
        if (Alt) {
            try {
                Instrument.callPauseHooks(ctx.current());
                Alt.invoke(ctx, methodInfo);
                Instrument.callResumeHooks(ctx.current());
            } catch (e) {
                Instrument.callResumeHooks(ctx.current());
                if (!e.class) {
                    throw e;
                }
                throw_(e, ctx);
            }
            return;
        }
        pushFrame(methodInfo, consumes);
      },
      // invokestatic
      0xb8: function() {
        var startip = frame.ip - 1;
        var idx = frame.read16();
        var isStatic = true;
        var methodInfo = cp[idx];
        if (methodInfo.tag) {
            methodInfo = resolve(0xb8, idx);
            if (isStatic)
                classInitCheck(methodInfo.classInfo, startip);
        }
        var consumes = Signature.getINSlots(methodInfo.signature);
        if (!isStatic) {
            ++consumes;
            var obj = stack[stack.length - consumes];
            if (!obj) {
                ctx.raiseExceptionAndYield("java/lang/NullPointerException");
                return;
            }
        }

        if (VM.DEBUG) {
            VM.trace("invoke", ctx.thread.pid, methodInfo);
        }

        var Alt = ACCESS_FLAGS.isNative(methodInfo.access_flags) ? Native : Override.hasMethod(methodInfo) ? Override : null;
        if (Alt) {
            try {
                Instrument.callPauseHooks(ctx.current());
                Alt.invoke(ctx, methodInfo);
                Instrument.callResumeHooks(ctx.current());
            } catch (e) {
                Instrument.callResumeHooks(ctx.current());
                if (!e.class) {
                    throw e;
                }
                throw_(e, ctx);
            }
            return;
        }
        pushFrame(methodInfo, consumes);
      },
      // invokeinterface
      0xb9: function() {
        var startip = frame.ip - 1;
        var idx = frame.read16();
        var argsNumber = frame.read8();
        var zero = frame.read8();
        var isStatic = false;
        var methodInfo = cp[idx];
        if (methodInfo.tag) {
            methodInfo = resolve(0xb9, idx);
            if (isStatic)
                classInitCheck(methodInfo.classInfo, startip);
        }
        var consumes = Signature.getINSlots(methodInfo.signature);
        if (!isStatic) {
            ++consumes;
            var obj = stack[stack.length - consumes];
            if (!obj) {
                ctx.raiseExceptionAndYield("java/lang/NullPointerException");
                return;
            }
            if (methodInfo.classInfo != obj.class)
                methodInfo = CLASSES.getMethod(obj.class, methodInfo.name, methodInfo.signature, false, true);
        }

        if (VM.DEBUG) {
            VM.trace("invoke", ctx.thread.pid, methodInfo);
        }

        var Alt = ACCESS_FLAGS.isNative(methodInfo.access_flags) ? Native : Override.hasMethod(methodInfo) ? Override : null;
        if (Alt) {
            try {
                Instrument.callPauseHooks(ctx.current());
                Alt.invoke(ctx, methodInfo);
                Instrument.callResumeHooks(ctx.current());
            } catch (e) {
                Instrument.callResumeHooks(ctx.current());
                if (!e.class) {
                    throw e;
                }
                throw_(e, ctx);
            }
            return;
        }
        pushFrame(methodInfo, consumes);
      },
      // return
      0xb1: function() {
        if (VM.DEBUG) {
            VM.trace("return", ctx.thread.pid, frame.methodInfo);
        }
        popFrame(0);
        if (!frame.methodInfo) {
          throw VM.Yield;
        }
      },
      // ireturn
      0xac: function() {
        if (VM.DEBUG) {
            VM.trace("return", ctx.thread.pid, frame.methodInfo, stack[stack.length-1]);
        }
        popFrame(1);
        if (!frame.methodInfo) {
          throw VM.Yield;
        }
      },
      // freturn
      0xae: function() {
        if (VM.DEBUG) {
            VM.trace("return", ctx.thread.pid, frame.methodInfo, stack[stack.length-1]);
        }
        popFrame(1);
        if (!frame.methodInfo) {
          throw VM.Yield;
        }
      },
      // areturn
      0xb0: function() {
        if (VM.DEBUG) {
            VM.trace("return", ctx.thread.pid, frame.methodInfo, stack[stack.length-1]);
        }
        popFrame(1);
        if (!frame.methodInfo) {
          throw VM.Yield;
        }
      },
      // lreturn
      0xad: function() {
        if (VM.DEBUG) {
            VM.trace("return", ctx.thread.pid, frame.methodInfo, stack[stack.length-1]);
        }
        popFrame(2);
        if (!frame.methodInfo) {
          throw VM.Yield;
        }
      },
      // dreturn
      0xaf: function() {
        if (VM.DEBUG) {
            VM.trace("return", ctx.thread.pid, frame.methodInfo, stack[stack.length-1]);
        }
        popFrame(2);
        if (!frame.methodInfo) {
          throw VM.Yield;
        }
      },
    }

    while (true) {
        funcTable[frame.read8()]();
    }
}
