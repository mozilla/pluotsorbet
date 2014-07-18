/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var VM = {};

VM.level = 0;

VM.invoke = function(methodInfo, args, callback) {
    var caller = new Frame();
    var consumes = 0;
    if (args) {
        consumes = args.length;
        var stack = caller.stack;
        for (var n = 0; n < consumes; ++n)
            stack.push(args[n]);
    }
    VM.level++;
    VM.resume(caller.pushFrame(methodInfo, consumes), function () {
        VM.level--;
        return !callback || callback();
    });
}

VM.resume = function(frame, callback) {
    var cp = frame.cp;
    var stack = frame.stack;

    function pushFrame(methodInfo, consumes) {
        frame = frame.pushFrame(methodInfo, consumes);
        stack = frame.stack;
        cp = frame.cp;
    }

    function popFrame(consumes) {
        var callee = frame;
        frame = frame.popFrame();
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
    }

    function throw_(ex) {
        var exClass = CLASSES.getClass(ex.class.className);
        do {
            var exception_table = frame.methodInfo.exception_table;
            var handler_pc = null;
            for (var i=0; i<exception_table.length; i++) {
                if (frame.ip >= exception_table[i].start_pc && frame.ip <= exception_table[i].end_pc) {
                    if (exception_table[i].catch_type === 0) {
                        handler_pc = exception_table[i].handler_pc;
                    } else {
                        var name = cp[cp[exception_table[i].catch_type].name_index].bytes;
                        if (exClass.canAssignTo(CLASSES.getClass(name))) {
                            handler_pc = exception_table[i].handler_pc;
                            break;
                        }
                    }
                }
            }
            if (handler_pc != null) {
                stack.push(ex);
                frame.ip = handler_pc;
                return;
            }
            popFrame();
        } while (frame.caller);
        throw new NATIVE.JavaException(ex.class.className, util.fromJavaString(ex.detailMessage));
    }

    function raiseException(className, message) {
        var ex = CLASSES.newObject(className);
        var ctor = CLASSES.getMethod(ex.class, "<init>", "(Ljava/lang/String;)V", false, false);
        VM.invoke(ctor, [ex, message]);
        throw_(ex);
    }

    function checkArrayAccess(refArray, idx) {
        if (!refArray) {
            raiseException("java/lang/NullPointerException");
            return false;
        }
        if (idx < 0 || idx >= refArray.length) {
            raiseException("java/lang/ArrayIndexOutOfBoundsException", idx);
            return false;
        }
        return true;
    }

    while (true) {
        var op = frame.read8();
        // console.log(frame.methodInfo.classInfo.className + " " + frame.methodInfo.name + " " + (frame.ip - 1) + " " + OPCODES[op] + " " + stack.length);
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
        case 0x0e: // dconst_0
            stack.push(0);
            break;
        case 0x04: // iconst_1
        case 0x0c: // fconst_1
        case 0x0f: // dconst_1
            stack.push(1);
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
            var constant = cp[(op === 0x12) ? frame.read8() : frame.read16()];
            switch(constant.tag) {
            case TAGS.CONSTANT_Integer:
                stack.push(constant.integer);
                break;
            case TAGS.CONSTANT_Float:
                stack.push(constant.float);
                break;
            case TAGS.CONSTANT_String:
                stack.push(CLASSES.newString(cp[constant.string_index].bytes));
                break;
            default:
                throw new Error("not support constant type");
            }
            break;
        case 0x14: // ldc2_w
            var constant = cp[frame.read16()];
            switch(constant.tag) {
            case TAGS.CONSTANT_Long:
                stack.push2(Long.fromBits(constant.lowBits, constant.highBits));
                break;
            case TAGS.CONSTANT_Double:
                stack.push2(constant.double);
                break;
            default:
                throw new Error("not support constant type");
            }
            break;
        case 0x15: // iload
        case 0x17: // fload
        case 0x19: // aload
            var idx = frame.isWide() ? frame.read16() : frame.read8();
            stack.push(frame.getLocal(idx));
            break;
        case 0x16: // lload
        case 0x18: // dload
            var idx = frame.isWide() ? frame.read16() : frame.read8();
            stack.push2(frame.getLocal(idx));
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
            if (!checkArrayAccess(refArray, idx))
                break;
            stack.push(refArray[idx]);
            break;
        case 0x2f: // laload
        case 0x31: // daload
            var idx = stack.pop();
            var refArray = stack.pop();
            if (!checkArrayAccess(refArray, idx))
                break;
            stack.push2(refArray[idx]);
            break;
        case 0x36: // istore
        case 0x38: // fstore
        case 0x3a: // astore
            var idx = frame.isWide() ? frame.read16() : frame.read8();
            frame.setLocal(idx, stack.pop());
            break;
        case 0x37: // lstore
        case 0x39: // dstore
            var idx = frame.isWide() ? frame.read16() : frame.read8();
            frame.setLocal(idx, stack.pop2());
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
        case 0x53: // aastore
        case 0x54: // bastore
        case 0x55: // castore
        case 0x56: // sastore
            var val = stack.pop();
            var idx = stack.pop();
            var refArray = stack.pop();
            if (!checkArrayAccess(refArray, idx))
                break;
            refArray[idx] = val;
            break;
        case 0x50: // lastore
        case 0x52: // dastore
            var val = stack.pop2();
            var idx = stack.pop();
            var refArray = stack.pop();
            if (!checkArrayAccess(refArray, idx))
                break;
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
            var wide = frame.isWide();
            var idx = wide ? frame.read16() : frame.read8();
            var val = wide ? frame.read16signed() : frame.read8signed();
            frame.setLocal(idx, frame.getLocal(idx) + val);
            break;
        case 0x60: // iadd
            stack.push((stack.pop() + stack.pop())|0);
            break;
        case 0x61: // ladd
            stack.push2(stack.pop2().add(stack.pop2()));
            break;
        case 0x62: // fadd
            stack.push(util.double2float(stack.pop() + stack.pop()));
            break;
        case 0x63: // dadd
            stack.push2(stack.pop2() + stack.pop2());
            break;
        case 0x64: // isub
            stack.push((- stack.pop() + stack.pop())|0);
            break;
        case 0x65: // lsub
            stack.push2(stack.pop2().add(stack.pop2()).negate());
            break;
        case 0x66: // fsub
            stack.push(util.double2float(- stack.pop() + stack.pop()));
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
            stack.push(util.double2float(stack.pop() * stack.pop()));
            break;
        case 0x6b: // dmul
            stack.push2(stack.pop2() * stack.pop2());
            break;
        case 0x6c: // idiv
            var b = stack.pop();
            var a = stack.pop();
            if (!b) {
                raiseException("java/lang/ArithmeticException", "/ by zero");
                break;
            }
            stack.push((a === util.INT_MIN && b === -1) ? a : ((a / b)|0));
            break;
        case 0x6d: // ldiv
            var b = stack.pop2();
            var a = stack.pop2();
            if (b.isZero()) {
                raiseException("java/lang/ArithmeticException", "/ by zero");
                break;
            }
            stack.push2(a.div(b));
            break;
        case 0x6e: // fdiv
            var b = stack.pop();
            var a = stack.pop();
            stack.push(util.double2float(a / b));
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
                raiseException("java/lang/ArithmeticException", "/ by zero");
                break;
            }
            stack.push(a % b);
            break;
        case 0x71: // lrem
            var b = stack.pop2();
            var a = stack.pop2();
            if (b.isZero()) {
                raiseException("java/lang/ArithmeticException", "/ by zero");
                break;
            }
            stack.push2(a.modulo(b));
            break;
        case 0x72: // frem
            var b = stack.pop();
            var a = stack.pop();
            stack.push(util.double2float(a % b));
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
            var b = stack.pop2();
            var a = stack.pop2();
            stack.push2(a.shiftLeft(b));
            break;
        case 0x7a: // ishr
            var b = stack.pop();
            var a = stack.pop();
            stack.push(a >> b);
            break;
        case 0x7b: // lshr
            var b = stack.pop2();
            var a = stack.pop2();
            stack.push2(a.shiftRight(b));
            break;
        case 0x7c: // iushr
            var b = stack.pop();
            var a = stack.pop();
            stack.push(a >>> b);
            break;
        case 0x7d: // lushr
            var b = stack.pop2();
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
            frame.ip = stack.pop() === 0 ? jmp : frame.ip;
            break;
        case 0x9a: // ifne
            var jmp = frame.ip - 1 + frame.read16signed();
            frame.ip = stack.pop() !== 0 ? jmp : frame.ip;
            break;
        case 0x9b: // iflt
            var jmp = frame.ip - 1 + frame.read16signed();
            frame.ip = stack.pop() < 0 ? jmp : frame.ip;
            break;
        case 0x9c: // ifge
            var jmp = frame.ip - 1 + frame.read16signed();
            frame.ip = stack.pop() >= 0 ? jmp : frame.ip;
            break;
        case 0x9d: // ifgt
            var jmp = frame.ip - 1 + frame.read16signed();
            frame.ip = stack.pop() > 0 ? jmp : frame.ip;
            break;
        case 0x9e: // ifle
            var jmp = frame.ip - 1 + frame.read16signed();
            frame.ip = stack.pop() <= 0 ? jmp : frame.ip;
            break;
        case 0x9f: // if_icmpeq
            var jmp = frame.ip - 1 + frame.read16signed();
            frame.ip = stack.pop() === stack.pop() ? jmp : frame.ip;
            break;
        case 0xa0: // if_cmpne
            var jmp = frame.ip - 1 + frame.read16signed();
            frame.ip = stack.pop() !== stack.pop() ? jmp : frame.ip;
            break;
        case 0xa1: // if_icmplt
            var jmp = frame.ip - 1 + frame.read16signed();
            frame.ip = stack.pop() > stack.pop() ? jmp : frame.ip;
            break;
        case 0xa2: // if_icmpge
            var jmp = frame.ip - 1 + frame.read16signed();
            frame.ip = stack.pop() <= stack.pop() ? jmp : frame.ip;
            break;
        case 0xa3: // if_icmpgt
            var jmp = frame.ip - 1 + frame.read16signed();
            frame.ip = stack.pop() < stack.pop() ? jmp : frame.ip;
            break;
        case 0xa4: // if_icmple
            var jmp = frame.ip - 1 + frame.read16signed();
            frame.ip = stack.pop() >= stack.pop() ? jmp : frame.ip;
            break;
        case 0xa5: // if_acmpeq
            var jmp = frame.ip - 1 + calee.read16signed();
            frame.ip = stack.pop() === stack.pop() ? jmp : frame.ip;
            break;
        case 0xa6: // if_acmpne
            var jmp = frame.ip - 1 + frame.read16signed();
            frame.ip = stack.pop() !== stack.pop() ? jmp : frame.ip;
            break;
        case 0xc6: // ifnull
            var ref = stack.pop();
            if (!ref)
                frame.ip += frame.read16signed() - 1;
            break;
        case 0xc7: // ifnonnull
            var ref = stack.pop();
            if (!!ref)
                frame.ip += frame.read16signed() - 1;
            break;
        case 0xa7: // goto
            frame.ip += frame.read16signed() - 1;
            break;
        case 0xc8: // goto_w
            frame.ip += frame.read32signed() - 1;
            break;
        case 0xa8: // jsr
            var jmp = frame.read16();
            stack.push(frame.ip);
            frame.ip = jmp;
            break;
        case 0xc9: // jsr_w
            var jmp = frame.read32();
            stack.push(frame.ip);
            frame.ip = jmp;
            break;
        case 0xa9: // ret
            var idx = frame.isWide() ? frame.read16() : frame.read8();
            frame.ip = frame.getLocal(idx);
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
            stack.push(util.double2float(stack.pop2().toNumber()));
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
            stack.push(util.double2float(stack.pop2()));
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
            break;
        case 0xbc: // newarray
            var type = frame.read8();
            var size = stack.pop();
            if (size < 0) {
                raiseException("java/lang/NegativeSizeException");
                break;
            }
            stack.push(CLASSES.newPrimitiveArray("????ZCFDBSIJ"[type], size));
            break;
        case 0xbd: // anewarray
            var idx = frame.read16();
            var className = cp[cp[idx].name_index].bytes;
            var size = stack.pop();
            if (size < 0) {
                raiseException("java/lang/NegativeSizeException");
                break;
            }
            stack.push(CLASSES.newArray(className, size));
            break;
        case 0xc5: // multianewarray
            var idx = frame.read16();
            var typeName = cp[cp[idx].name_index].bytes;
            var dimensions = frame.read8();
            var lengths = new Array(dimensions);
            for (var i=0; i<dimensions; i++)
                lengths[i] = stack.pop();
            stack.push(CLASSES.newMultiArray(typeName, lengths));
            break;
        case 0xbe: // arraylength
            stack.push(stack.pop().length);
            break;
        case 0xb4: // getfield
            var idx = frame.read16();
            var fieldName = cp[cp[cp[idx].name_and_type_index].name_index].bytes;
            var signature = cp[cp[cp[idx].name_and_type_index].signature_index].bytes;
            var obj = stack.pop();
            if (!obj) {
                raiseException("java/lang/NullPointerException");
                break;
            }
            var value = obj[fieldName];
            if (typeof value === "undefined") {
                value = util.defaultValue(signature);
            }
            stack.pushType(signature, value);
            break;
        case 0xb5: // putfield
            var idx = frame.read16();
            var fieldName = cp[cp[cp[idx].name_and_type_index].name_index].bytes;
            var signature = cp[cp[cp[idx].name_and_type_index].signature_index].bytes;
            var val = stack.popType(signature);
            var obj = stack.pop();
            if (!obj) {
                raiseException("java/lang/NullPointerException");
                break;
            }
            obj[fieldName] = val;
            break;
        case 0xb2: // getstatic
            var idx = frame.read16();
            var className = cp[cp[cp[idx].class_index].name_index].bytes;
            var fieldName = cp[cp[cp[idx].name_and_type_index].name_index].bytes;
            var signature = cp[cp[cp[idx].name_and_type_index].signature_index].bytes;
            stack.pushType(signature, CLASSES.getStaticField(className, fieldName));
            break;
        case 0xb3: // putstatic
            var idx = frame.read16();
            var className = cp[cp[cp[idx].class_index].name_index].bytes;
            var fieldName = cp[cp[cp[idx].name_and_type_index].name_index].bytes;
            var signature = cp[cp[cp[idx].name_and_type_index].signature_index].bytes;
            CLASSES.setStaticField(className, fieldName, stack.popType(signature));
            break;
        case 0xbb: // new
            var idx = frame.read16();
            var className = cp[cp[idx].name_index].bytes;
            stack.push(CLASSES.newObject(className));
            break;
        case 0xc0: // checkcast
            var idx = frame.read16();
            var type = cp[cp[idx].name_index].bytes;
            break;
        case 0xc1: // instanceof
            var idx = frame.read16();
            var className = cp[cp[idx].name_index].bytes;
            var obj = stack.pop();
            stack.push(obj.class.className === className);
            break;
        case 0xbf: // athrow
            throw_(stack.pop());
            break;
        case 0xc2: // monitorenter
            var obj = stack.pop();
            if (!obj) {
                raiseException("java/lang/NullPointerException");
                break;
            }
            // if (obj.hasOwnProperty("$lock$")) {
            // stack.push(obj);
            // frame.ip--;
            // SCHEDULER.yield();
            // } else {
            // obj["$lock$"] = "locked";
            // }
            break;
        case 0xc3: // monitorexit
            var obj = stack.pop();
            if (!obj) {
                raiseException("java/lang/NullPointerException");
                break;
            }
            // delete obj["$lock$"];
            // SCHEDULER.yield();
            break;
        case 0xc4: // wide
            break;
        case 0xb6: // invokevirtual
        case 0xb7: // invokespecial
        case 0xb8: // invokestatic
        case 0xb9: // invokeinterface
            var idx = frame.read16();
            if (op === 0xb9) {
                var argsNumber = frame.read8();
                var zero = frame.read8();
            }
            var className = cp[cp[cp[idx].class_index].name_index].bytes;
            var methodName = cp[cp[cp[idx].name_and_type_index].name_index].bytes;
            var signature = cp[cp[cp[idx].name_and_type_index].signature_index].bytes;
            var classInfo = CLASSES.getClass(className, op === 0xb8);
            var methodInfo = CLASSES.getMethod(classInfo, methodName, signature, op === 0xb8);
            var consumes = Signature.parse(methodInfo.signature).IN.slots;
            if (op !== OPCODES.invokestatic) {
                ++consumes;
                var obj = stack[stack.length - consumes];
                if (!obj) {
                    raiseException("java/lang/NullPointerException");
                    break;
                }
                switch (op) {
                case OPCODES.invokevirtual:
                case OPCODES.invokeinterface:
                    // console.log("virtual dispatch", methodInfo.classInfo.className, obj.class.className, methodInfo.name, methodInfo.signature);
                    if (methodInfo.classInfo != obj.class)
                        methodInfo = CLASSES.getMethod(obj.class, methodInfo.name, methodInfo.signature, op === OPCODES.invokestatic);
                    break;
                }
            }
            if (ACCESS_FLAGS.isNative(methodInfo.access_flags)) {
                try {
                    NATIVE.invokeNative(frame, methodInfo);
                } catch (e) {
                    if (!(e instanceof NATIVE.JavaException)) {
                        throw e;
                    }
                    raiseException(e.className, e.msg);
                    return;
                }
                break;
            }
            pushFrame(methodInfo, consumes);
            break;
        case 0xb1: // return
            popFrame(0);
            if (!frame.caller)
                return !callback || callback();
            break;
        case 0xac: // ireturn
        case 0xae: // freturn
        case 0xb0: // areturn
            popFrame(1);
            if (!frame.caller)
                return !callback || callback();
            break;
        case 0xad: // lreturn
        case 0xaf: // dreturn
            popFrame(2);
            if (!frame.caller)
                return !callback || callback();
            break;
        default:
            var opName = OPCODES[op];
            throw new Error("Opcode " + opName + " [" + op + "] not supported.");
        }
    };
}
