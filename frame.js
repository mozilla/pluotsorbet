/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

Array.prototype.push2 = function (value) {
    this.push(value);
    this.push(null);
}

Array.prototype.pop2 = function () {
    this.pop();
    return this.pop();
}

Array.prototype.top = function () {
    return this[this.length - 1];
}

var Frame = function(methodInfo) {
    if (methodInfo) {
        this.methodInfo = methodInfo;
        this.cp = methodInfo.classInfo.constant_pool;
        this.code = methodInfo.code;
        this.ip = 0;
    }
    this.stack = [];
}

Frame.prototype.pushFrame = function(methodInfo, consumes) {
    var callee = new Frame(methodInfo);
    callee.locals = this.stack;
    callee.localsBase = this.stack.length - consumes;
    callee.caller = this;
    THREADS.current.frame = callee;
    return callee;
}

Frame.prototype.popFrame = function() {
    var caller = this.caller;
    caller.stack.length = this.localsBase;
    THREADS.current.frame = caller;
    return caller;
}

Frame.prototype.getLocal = function(idx) {
    return this.locals[this.localsBase + idx];
}

Frame.prototype.setLocal = function(idx, value) {
    this.locals[this.localsBase + idx] = value;
}

Frame.prototype.isWide = function() {
    return this.code[this.ip - 2] === OPCODES.wide;
}

Frame.prototype.getOp = function() {
    return this.code[this.ip - 1];
}

Frame.prototype.read8 = function() {
    return this.code[this.ip++];
};

Frame.prototype.read16 = function() {
    return this.read8()<<8 | this.read8();
};

Frame.prototype.read32 = function() {
    return this.read16()<<16 | this.read16();
};

Frame.prototype.read8signed = function() {
    var x = this.read8();
    return (x > 0x7f) ? (x - 0x100) : x;
}

Frame.prototype.read16signed = function() {
    var x = this.read16();
    return (x > 0x7fff) ? (x - 0x10000) : x;
}

Frame.prototype.read32signed = function() {
    var x = this.read32();
    return (x > 0x7fffffff) ? (x - 0x100000000) : x;
}

Frame.prototype.throw = function(ex) {
    var handler_pc = null;

    for (var i=0; i<this.exception_table.length; i++) {
        if (this.ip >= this.exception_table[i].start_pc && this.ip <= this.exception_table[i].end_pc) {
            if (this.exception_table[i].catch_type === 0) {
                handler_pc = this.exception_table[i].handler_pc;
            } else {
                var name = this.cp[this.cp[this.exception_table[i].catch_type].name_index].bytes;
                if (name === ex.className) {
                    handler_pc = this.exception_table[i].handler_pc;
                    break;
                }
            }
        }
    }

    if (handler_pc != null) {
        stack.push(ex);
        this.ip = handler_pc;
    } else {
        throw ex;
    }
}

Frame.prototype.raiseException = function(className, message) {
    var ex = CLASSES.newObject(this, className);
    var ctor = CLASSES.getMethod(this, ex.class, "<init>", "(Ljava/lang/String;)V", false, false);
    this.stack.push(ex);
    this.stack.push(message);
    this.invoke(OPCODES.invokespecial, ctor);
    this.throw(ex);
}

Frame.prototype.checkArrayAccess = function(refArray, idx) {
    if (!refArray) {
        this.raiseException("java/lang/NullPointerException");
        return false;
    }
    if (idx < 0 || idx >= refArray.length) {
        this.raiseException("java/lang/ArrayIndexOutOfBoundsException", idx);
        return false;
    }
    return true;
}

Frame.prototype.invoke = function(op, methodInfo) {
    var consumes = Signature.parse(methodInfo.signature).IN.slots;

    if (op !== OPCODES.invokestatic) {
        ++consumes;
        var obj = this.stack[this.stack.length - consumes];
        if (!obj) {
            this.raiseException("java/lang/NullPointerException");
            return;
        }
        switch (op) {
        case OPCODES.invokevirtual:
            // console.log("virtual dispatch", methodInfo.classInfo.className, obj.class.className, methodInfo.name, methodInfo.signature);
            if (methodInfo.classInfo != obj.class)
                methodInfo = CLASSES.getMethod(this, obj.class, methodInfo.name, methodInfo.signature, op === OPCODES.invokestatic);
            break;
        }
    }

    if (ACCESS_FLAGS.isNative(methodInfo.access_flags)) {
        NATIVE.invokeNative(this, methodInfo);
        return;
    }

    var callee = this.pushFrame(methodInfo, consumes);
    var cp = callee.cp;
    var stack = callee.stack;

    while (true) {
        var op = callee.read8();
        // var x = [];
        // callee.stack.forEach(function (e) { x.push(e.toSource()); });
        // console.log(callee.methodInfo.classInfo.className, callee.methodInfo.name, callee.ip - 1, OPCODES[op], x.join(" "));
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
            stack.push2(gLong.fromInt(0));
            break;
        case 0x0a: // lconst_1
            stack.push2(gLong.fromInt(1));
            break;
        case 0x10: // bipush
            stack.push(callee.read8signed());
            break;
        case 0x11: // sipush
            stack.push(callee.read16signed());
            break;
        case 0x12: // ldc
            var constant = cp[callee.read8()];
            switch(constant.tag) {
            case TAGS.CONSTANT_String:
                stack.push(CLASSES.newString(callee, cp[constant.string_index].bytes));
                break;
            default:
                throw new Error("not support constant type");
            }
            break;
        case 0x13: // ldc_w
            var constant = cp[callee.read16()];
            switch(constant.tag) {
            case TAGS.CONSTANT_String:
                stack.push(cp[constant.string_index].bytes);
                break;
            default:
                throw new Error("not support constant type");
            }
            break;
        case 0x14: // ldc2_w
            var constant = cp[callee.read16()];
            switch(constant.tag) {
            case TAGS.CONSTANT_String:
                stack.push(cp[constant.string_index].bytes);
                break;
            case TAGS.CONSTANT_Long:
                stack.push2(Numeric.getLong(constant.bytes));
                break;
            case TAGS.CONSTANT_Double:
                stack.push2(constant.bytes.readDoubleBE(0));
                break;
            default:
                throw new Error("not support constant type");
            }
            break;
        case 0x15: // iload
        case 0x17: // fload
        case 0x19: // aload
            var idx = callee.isWide() ? callee.read16() : callee.read8();
            stack.push(callee.getLocal(idx));
            break;
        case 0x16: // lload
        case 0x18: // dload
            var idx = callee.isWide() ? callee.read16() : callee.read8();
            stack.push2(callee.getLocal(idx));
            break;
        case 0x1a: // iload_0
        case 0x22: // fload_0
        case 0x2a: // aload_0
            stack.push(callee.getLocal(0));
            break;
        case 0x1b: // iload_1
        case 0x23: // fload_1
        case 0x2b: // aload_1
            stack.push(callee.getLocal(1));
            break;
        case 0x1c: // iload_2
        case 0x24: // fload_2
        case 0x2c: // aload_2
            stack.push(callee.getLocal(2));
            break;
        case 0x1d: // iload_3
        case 0x25: // fload_3
        case 0x2d: // aload_3
            stack.push(callee.getLocal(3));
            break;
        case 0x1e: // lload_0
        case 0x26: // dload_0
            stack.push2(callee.getLocal(0));
            break;
        case 0x1f: // lload_1
        case 0x27: // dload_1
            stack.push2(callee.getLocal(1));
            break;
        case 0x20: // lload_2
        case 0x28: // dload_2
            stack.push2(callee.getLocal(2));
            break;
        case 0x21: // lload_3
        case 0x29: // dload_3
            stack.push2(callee.getLocal(3));
            break;
        case 0x2e: // iaload
        case 0x30: // faload
        case 0x32: // aaload
        case 0x33: // baload
        case 0x34: // caload
        case 0x35: // saload
            var idx = stack.pop();
            var refArray = stack.pop();
            if (!callee.checkArrayAccess(refArray, idx))
                break;
            stack.push(refArray[idx]);
            break;
        case 0x2f: // laload
        case 0x31: // daload
            var idx = stack.pop();
            var refArray = stack.pop();
            if (!callee.checkArrayAccess(refArray, idx))
                break;
            stack.push2(refArray[idx]);
            break;
        case 0x36: // istore
        case 0x38: // fstore
        case 0x3a: // astore
            var idx = callee.isWide() ? callee.read16() : callee.read8();
            callee.setLocal(idx, stack.pop());
            break;
        case 0x37: // lstore
        case 0x39: // dstore
            var idx = callee.isWide() ? callee.read16() : callee.read8();
            callee.setLocal(idx, stack.pop2());
            break;
        case 0x3b: // istore_0
        case 0x43: // fstore_0
        case 0x4b: // astore_0
            callee.setLocal(0, stack.pop());
            break;
        case 0x3c: // istore_1
        case 0x44: // fstore_1
        case 0x4c: // astore_1
            callee.setLocal(1, stack.pop());
            break;
        case 0x3d: // istore_2
        case 0x45: // fstore_2
        case 0x4d: // astore_2
            callee.setLocal(2, stack.pop());
            break;
        case 0x3e: // istore_3
        case 0x46: // fstore_3
        case 0x4e: // astore_3
            callee.setLocal(3, stack.pop());
            break;
        case 0x3f: // lstore_0
        case 0x47: // dstore_0
            callee.setLocal(0, stack.pop2());
            break;
        case 0x40: // lstore_1
        case 0x48: // dstore_1
            callee.setLocal(1, stack.pop2());
            break;
        case 0x41: // lstore_2
        case 0x49: // dstore_2
            callee.setLocal(2, stack.pop2());
            break;
        case 0x42: // lstore_3
        case 0x4a: // dstore_3
            callee.setLocal(2, stack.pop2());
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
            if (!callee.checkArrayAccess(refArray, idx))
                break;
            refArray[idx] = val;
            break;
        case 0x50: // lastore
        case 0x52: // dastore
            var val = stack.pop2();
            var idx = stack.pop();
            var refArray = stack.pop();
            if (!callee.checkArrayAccess(refArray, idx))
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
            var val1 = stack.pop();
            var val2 = stack.pop();
            stack.push(val1);
            stack.push(val2);
            stack.push(val1);
            break;
        case 0x5b: // dup_x2
            var val1 = stack.pop();
            var val2 = stack.pop();
            var val3 = stack.pop();
            stack.push(val1);
            stack.push(val3);
            stack.push(val2);
            stack.push(val1);
            break;
        case 0x5c: // dup2
            var val1 = stack.pop();
            var val2 = stack.pop();
            stack.push(val2);
            stack.push(val1);
            stack.push(val2);
            stack.push(val1);
            break;
        case 0x5d: // dup2_x1
            var val1 = stack.pop();
            var val2 = stack.pop();
            var val3 = stack.pop();
            stack.push(val2);
            stack.push(val1);
            stack.push(val3);
            stack.push(val2);
            stack.push(val1);
            break;
        case 0x5e: // dup2_x2
            var val1 = stack.pop();
            var val2 = stack.pop();
            var val3 = stack.pop();
            var val4 = stack.pop();
            stack.push(val2);
            stack.push(val1);
            stack.push(val4);
            stack.push(val3);
            stack.push(val2);
            stack.push(val1);
            break;
        case 0x5f: // swap
            var val1 = stack.pop();
            var val2 = stack.pop();
            stack.push(val1);
            stack.push(val2);
            break;
        case 0x84: // iinc
            var wide = callee.isWide();
            var idx = wide ? callee.read16() : callee.read8();
            var val = wide ? callee.read16signed() : callee.read8signed();
            callee.setLocal(idx, callee.getLocal(idx) + val);
            break;
        case 0x60: // iadd
            stack.push((stack.pop() + stack.pop())|0);
            break;
        case 0x61: // ladd
            stack.push2(stack.pop2().add(stack.pop2()));
            break;
        case 0x62: // fadd
            stack.push(utils.double2float(stack.pop() + stack.pop()));
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
            stack.push(utils.double2float(- stack.pop() + stack.pop()));
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
            stack.push(utils.double2float(stack.pop() * stack.pop()));
            break;
        case 0x6b: // dmul
            stack.push2(stack.pop2() * stack.pop2());
            break;
        case 0x6c: // idiv
            var val1 = stack.pop();
            var val2 = stack.pop();
            if (!val1) {
                callee.raiseException("java/lang/ArithmeticException", "/ by zero");
                break;
            }
            stack.push((val2 === utils.INT_MIN && val1 === -1) ? val2 : ((a / b)|0));
            break;
        case 0x6d: // ldiv
            var val1 = stack.pop2();
            var val2 = stack.pop2();
            if (!val1.isZero()) {
                callee.raiseException("java/lang/ArithmeticException", "/ by zero");
                break;
            }
            stack.push2(val2.div(val1));
            break;
        case 0x6e: // fdiv
            var val1 = stack.pop();
            var val2 = stack.pop();
            stack.push(utils.double2float(val2 / val1));
            break;
        case 0x6f: // ddiv
            var val1 = stack.pop2();
            var val2 = stack.pop2();
            stack.push2(val2 / val1);
            break;
        case 0x70: // irem
            var val1 = stack.pop();
            var val2 = stack.pop();
            if (!val1) {
                callee.raiseException("java/lang/ArithmeticException", "/ by zero");
                break;
            }
            stack.push(val2 % val1);
            break;
        case 0x71: // lrem
            var val1 = stack.pop2();
            var val2 = stack.pop2();
            if (val1.isZero()) {
                callee.raiseException("java/lang/ArithmeticException", "/ by zero");
                break;
            }
            stack.push2(val2.modulo(val1));
            break;
        case 0x72: // frem
            var val1 = stack.pop();
            var val2 = stack.pop();
            stack.push(utils.double2float(val2 % val1));
            break;
        case 0x73: // drem
            var val1 = stack.pop2();
            var val2 = stack.pop2();
            stack.push2(val2 % val1);
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
            var val1 = stack.pop();
            var val2 = stack.pop();
            stack.push(val2 << val1);
            break;
        case 0x79: // lshl
            var val1 = stack.pop2();
            var val2 = stack.pop2();
            stack.push2(val2.shiftLeft(val1));
            break;
        case 0x7a: // ishr
            var val1 = stack.pop();
            var val2 = stack.pop();
            stack.push(val2 >> val1);
            break;
        case 0x7b: // lshr
            var val1 = stack.pop2();
            var val2 = stack.pop2();
            stack.push2(val2.shiftRight(val1));
            break;
        case 0x7c: // iushr
            var val1 = stack.pop();
            var val2 = stack.pop();
            stack.push(val2 >>> val1);
            break;
        case 0x7d: // lushr
            var val1 = stack.pop2();
            var val2 = stack.pop2();
            stack.push2(val2.shiftRightUnsigned(val1));
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
            var val1 = stack.pop2();
            var val2 = stack.pop2();
            if (val2.greaterThan(val1)) {
                stack.push(1);
            } else if (val2.lessThan(val1)) {
                stack.push(-1);
            } else {
                stack.push(0);
            }
            break;
        case 0x95: // fcmpl
            var val1 = stack.pop();
            var val2 = stack.pop();
            if (isNaN(val1) || isNaN(val2)) {
                stack.push(-1);
            } else if (val2 > val1) {
                stack.push(1);
            } else if (val2 < val1) {
                stack.push(-1);
            } else {
                stack.push(0);
            }
            break;
        case 0x96: // fcmpg
            var val1 = stack.pop();
            var val2 = stack.pop();
            if (isNaN(val1) || isNaN(val2)) {
                stack.push(1);
            } else if (val2 > val1) {
                stack.push(1);
            } else if (val2 < val1) {
                stack.push(-1);
            } else {
                stack.push(0);
            }
            break;
        case 0x97: // dcmpl
            var val1 = stack.pop2();
            var val2 = stack.pop2();
            if (isNaN(val1) || isNaN(val2)) {
                stack.push(-1);
            } else if (val2 > val1) {
                stack.push(1);
            } else if (val2 < val1) {
                stack.push(-1);
            } else {
                stack.push(0);
            }
            break;
        case 0x98: // dcmpg
            var val1 = stack.pop2();
            var val2 = stack.pop2();
            if (isNaN(val1) || isNaN(val2)) {
                stack.push(1);
            } else if (val2 > val1) {
                stack.push(1);
            } else if (val2 < val1) {
                stack.push(-1);
            } else {
                stack.push(0);
            }
            break;
        case 0x99: // ifeq
            var jmp = callee.ip - 1 + callee.read16signed();
            callee.ip = stack.pop() === 0 ? jmp : callee.ip;
            break;
        case 0x9a: // ifne
            var jmp = callee.ip - 1 + callee.read16signed();
            callee.ip = stack.pop() !== 0 ? jmp : callee.ip;
            break;
        case 0x9b: // iflt
            var jmp = callee.ip - 1 + callee.read16signed();
            callee.ip = stack.pop() < 0 ? jmp : callee.ip;
            break;
        case 0x9c: // ifge
            var jmp = callee.ip - 1 + callee.read16signed();
            callee.ip = stack.pop() >= 0 ? jmp : callee.ip;
            break;
        case 0x9d: // ifgt
            var jmp = callee.ip - 1 + callee.read16signed();
            callee.ip = stack.pop() > 0 ? jmp : callee.ip;
            break;
        case 0x9e: // ifle
            var jmp = callee.ip - 1 + callee.read16signed();
            callee.ip = stack.pop() <= 0 ? jmp : callee.ip;
            break;
        case 0x9f: // if_icmpeq
            var jmp = callee.ip - 1 + callee.read16signed();
            callee.ip = stack.pop() === stack.pop() ? jmp : callee.ip;
            break;
        case 0xa0: // if_cmpne
            var jmp = callee.ip - 1 + callee.read16signed();
            callee.ip = stack.pop() !== stack.pop() ? jmp : callee.ip;
            break;
        case 0xa1: // if_icmplt
            var jmp = callee.ip - 1 + callee.read16signed();
            callee.ip = stack.pop() > stack.pop() ? jmp : callee.ip;
            break;
        case 0xa2: // if_icmpge
            var jmp = callee.ip - 1 + callee.read16signed();
            callee.ip = stack.pop() <= stack.pop() ? jmp : callee.ip;
            break;
        case 0xa3: // if_icmpgt
            var jmp = callee.ip - 1 + callee.read16signed();
            callee.ip = stack.pop() < stack.pop() ? jmp : callee.ip;
            break;
        case 0xa4: // if_icmple
            var jmp = callee.ip - 1 + callee.read16signed();
            callee.ip = stack.pop() >= stack.pop() ? jmp : callee.ip;
            break;
        case 0xa5: // if_acmpeq
            var jmp = callee.ip - 1 + calee.read16signed();
            callee.ip = stack.pop() === stack.pop() ? jmp : callee.ip;
            break;
        case 0xa6: // if_acmpne
            var jmp = callee.ip - 1 + callee.read16signed();
            callee.ip = stack.pop() !== stack.pop() ? jmp : callee.ip;
            break;
        case 0xc6: // ifnull
            var ref = stack.pop();
            if (!ref)
                callee.ip += callee.read16signed() - 1;
            break;
        case 0xc7: // ifnonnull
            var ref = stack.pop();
            if (!!ref)
                callee.ip += callee.read16signed() - 1;
            break;
        case 0xa7: // goto
            callee.ip += callee.read16signed() - 1;
            break;
        case 0xc8: // goto_w
            callee.ip += callee.read32signed() - 1;
            break;
        case 0xa8: // jsr
            var jmp = callee.read16();
            stack.push(callee.ip);
            callee.ip = jmp;
            break;
        case 0xc9: // jsr_w
            var jmp = callee.read32();
            stack.push(callee.ip);
            callee.ip = jmp;
            break;
        case 0xa9: // ret
            var idx = callee.isWide() ? callee.read16() : callee.read8();
            callee.ip = callee.getLocal(idx);
            break;
        case 0x85: // i2l
            stack.push2(new gLong(stack.pop()));
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
            stack.push(utils.double2float(stack.pop2().toNumber()));
            break;
        case 0x8a: // l2d
            stack.push2(stack.pop2().toNumber());
            break;
        case 0x8b: // f2i
            stack.push(utils.double2int(stack.pop()));
            break;
        case 0x8c: // f2l
            stack.push2(gLong.fromNumber(stack.pop()));
            break;
        case 0x8d: // f2d
            stack.push2(stack.pop());
            break;
        case 0x8e: // d2i
            stack.push(utils.double2int(stack.pop2()));
            break;
        case 0x8f: // d2l
            stack.push2(utils.double2long(stack.pop2()));
            break;
        case 0x90: // d2f
            stack.push(utils.double2float(stack.pop2()));
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
            var startip = callee.ip;
            while ((callee.ip & 3) != 0)
                callee.ip++;
            var def = callee.read32signed();
            var low = callee.read32signed();
            var high = callee.read32signed();
            var val = stack.pop();
            var jmp;
            if (val < low || val > high) {
                jmp = def;
            } else {
                callee.ip  += (val - low) << 2;
                jmp = callee.read32signed();
            }
            callee.ip = startip - 1 + jmp;
            break;
        case 0xab: // lookupswitch
            var startip = callee.ip;
            while ((callee.ip & 3) != 0)
                callee.ip++;
            var jmp = callee.read32signed();
            var size = callee.read32();
            var val = callee.stack.pop();
          lookup:
            for (var i=0; i<size; i++) {
                var key = callee.read32signed();
                var offset = callee.read32signed();
                if (key === val) {
                    jmp = offset;
                }
                if (key >= val) {
                    break lookup;
                }
            }
            callee.ip = startip - 1 + jmp;
            break;
        case 0xbc: // newarray
            var type = callee.read8();
            var size = stack.pop();
            if (size < 0) {
                callee.raiseException("java/lang/NegativeSizeException");
                break;
            }
            stack.push(CLASSES.newPrimitiveArray(ARRAY_TYPE[type], size));
            break;
        case 0xbd: // anewarray
            var idx = callee.read16();
            var className = cp[cp[idx].name_index].bytes;
            var size = stack.pop();
            if (size < 0) {
                callee.raiseException("java/lang/NegativeSizeException");
                break;
            }
            stack.push(CLASSES.newArray(callee, className, size));
            break;
        case 0xc5: // multianewarray
            var idx = callee.read16();
            var typeName = cp[cp[idx].name_index].bytes;
            var dimensions = callee.read8();
            var lengths = new Array(dimensions);
            for (var i=0; i<dimensions; i++)
                lengths[i] = stack.pop();
            stack.push(CLASSES.newMultiArray(callee, typeName, lengths));
            break;
        case 0xbe: // arraylength
            stack.push(stack.pop().length);
            break;
        case 0xb4: // getfield
            var idx = callee.read16();
            var fieldName = cp[cp[cp[idx].name_and_type_index].name_index].bytes;
            var obj = stack.pop();
            if (!obj) {
                callee.raiseException("java/lang/NullPointerException");
                break;
            }
            var value = obj[fieldName];
            if (typeof value === "undefined") {
                value = util.defaultValue(cp[cp[cp[idx].name_and_type_index].signature_index].bytes);
            }
            stack.push(value);
            break;
        case 0xb5: // putfield
            var idx = callee.read16();
            var fieldName = cp[cp[cp[idx].name_and_type_index].name_index].bytes;
            var val = stack.pop();
            var obj = stack.pop();
            if (!obj) {
                callee.raiseException("java/lang/NullPointerException");
                break;
            }
            obj[fieldName] = val;
            break;
        case 0xb2: // getstatic
            var idx = callee.read16();
            var className = cp[cp[cp[idx].class_index].name_index].bytes;
            var fieldName = cp[cp[cp[idx].name_and_type_index].name_index].bytes;
            stack.push(CLASSES.getStaticField(callee, className, fieldName));
            break;
        case 0xb3: // putstatic
            var idx = callee.read16();
            var className = cp[cp[cp[idx].class_index].name_index].bytes;
            var fieldName = cp[cp[cp[idx].name_and_type_index].name_index].bytes;
            CLASSES.setStaticField(callee, className, fieldName, stack.pop());
            break;
        case 0xbb: // new
            var idx = callee.read16();
            var className = cp[cp[idx].name_index].bytes;
            stack.push(CLASSES.newObject(callee, className));
            break;
        case 0xc0: // checkcast
            var idx = callee.read16();
            var type = cp[cp[idx].name_index].bytes;
            break;
        case 0xc1: // instanceof
            var idx = callee.read16();
            var className = cp[cp[idx].name_index].bytes;
            var obj = stack.pop();
            stack.push(obj.class.className === className);
            break;
        case 0xbf: // athrow
            callee.throw(stack.pop());
            break;
        case 0xc2: // monitorenter
            var obj = stack.pop();
            if (!obj) {
                callee.raiseException("java/lang/NullPointerException");
                break;
            }
            // if (obj.hasOwnProperty("$lock$")) {
            // stack.push(obj);
            // callee.ip--;
            // SCHEDULER.yield();
            // } else {
            // obj["$lock$"] = "locked";
            // }
            break;
        case 0xc3: // monitorexit
            var obj = stack.pop();
            if (!obj) {
                callee.raiseException("java/lang/NullPointerException");
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
            var idx = callee.read16();
            if (op === 0xb9) {
                var argsNumber = callee.read8();
                var zero = callee.read8();
            }
            var className = cp[cp[cp[idx].class_index].name_index].bytes;
            var methodName = cp[cp[cp[idx].name_and_type_index].name_index].bytes;
            var signature = cp[cp[cp[idx].name_and_type_index].signature_index].bytes;
            var classInfo = CLASSES.getClass(callee, className);
            var method = CLASSES.getMethod(callee, classInfo, methodName, signature, op === 0xb8);
            callee.invoke(op, method);
            break;
        case 0xb1: // return
            callee.popFrame();
            return;
        case 0xac: // ireturn
        case 0xae: // freturn
        case 0xb0: // areturn
            callee.popFrame().stack.push(callee.stack.pop());
            return;
        case 0xad: // lreturn
        case 0xaf: // dreturn
            callee.popFrame().stack.push2(callee.stack.pop2());
            return;
        default:
            var opName = OPCODES[op];
            throw new Error("Opcode " + opName + " [" + op + "] not supported.");
        }
    };
}
