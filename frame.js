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

        case OPCODES.return:
            callee.popFrame();
            return;

        case OPCODES.ireturn:
        case OPCODES.freturn:
        case OPCODES.areturn:
            callee.popFrame().stack.push(callee.stack.pop());
            return;

        case OPCODES.lreturn:
        case OPCODES.dreturn:
            callee.popFrame().stack.push2(callee.stack.pop2());
            return;

        default:
            var opName = OPCODES[op];
            if (!(opName in this))
                throw new Error("Opcode " + opName + " [" + op + "] not supported.");
            callee[opName]();
            break;
        }
    };
}

Frame.prototype.idiv = function() {
    var val1 = this.stack.pop();
    var val2 = this.stack.pop();
    if (!val1) {
        this.raiseException("java/lang/ArithmeticException", "/ by zero");
        return;
    }
    this.stack.push((val2 === utils.INT_MIN && val1 === -1) ? val2 : ((a / b)|0));
}

Frame.prototype.ldiv = function() {
    var val1 = this.stack.pop2();
    var val2 = this.stack.pop2();
    if (!val1.isZero()) {
        this.raiseException("java/lang/ArithmeticException", "/ by zero");
        return;
    }
    this.stack.push2(val2.div(val1));
}

Frame.prototype.ddiv = function() {
    var val1 = this.stack.pop2();
    var val2 = this.stack.pop2();
    this.stack.push2(val2 / val1);
}

Frame.prototype.fdiv = function() {
    var val1 = this.stack.pop();
    var val2 = this.stack.pop();
    this.stack.push(utils.double2float(val2 / val1));
}

Frame.prototype.irem = function() {
    var val1 = this.stack.pop();
    var val2 = this.stack.pop();
    if (!val1) {
        this.raiseException("java/lang/ArithmeticException", "/ by zero");
        return;
    }
    this.stack.push(val2 % val1);
}

Frame.prototype.lrem = function() {
    var val1 = this.stack.pop2();
    var val2 = this.stack.pop2();
    if (val1.isZero()) {
        this.raiseException("java/lang/ArithmeticException", "/ by zero");
        return;
    }
    this.stack.push2(val2.modulo(val1));
}

Frame.prototype.drem = function() {
    var val1 = this.stack.pop2();
    var val2 = this.stack.pop2();
    this.stack.push2(val2 % val1);
}

Frame.prototype.frem = function() {
    var val1 = this.stack.pop();
    var val2 = this.stack.pop();
    this.stack.push(utils.double2float(val2 % val1));
}

Frame.prototype.ineg = function() {
    this.stack.push((- this.stack.pop())|0);
}

Frame.prototype.lneg = function() {
    this.stack.push2(this.stack.pop2().negate());
}

Frame.prototype.dneg = function() {
    this.stack.push2(- this.stack.pop2());
}

Frame.prototype.fneg = function() {
    this.stack.push(- this.stack.pop());
}

Frame.prototype.ishl = function() {
    var val1 = this.stack.pop();
    var val2 = this.stack.pop();
    this.stack.push(val2 << val1);
}

Frame.prototype.lshl = function() {
    var val1 = this.stack.pop2();
    var val2 = this.stack.pop2();
    this.stack.push2(val2.shiftLeft(val1));
}

Frame.prototype.ishr = function() {
    var val1 = this.stack.pop();
    var val2 = this.stack.pop();
    this.stack.push(val2 >> val1);
}

Frame.prototype.lshr = function() {
    var val1 = this.stack.pop2();
    var val2 = this.stack.pop2();
    this.stack.push2(val2.shiftRight(val1));
}

Frame.prototype.iushr = function() {
    var val1 = this.stack.pop();
    var val2 = this.stack.pop();
    this.stack.push(val2 >>> val1);
}

Frame.prototype.lushr = function() {
    var val1 = this.stack.pop2();
    var val2 = this.stack.pop2();
    this.stack.push2(val2.shiftRightUnsigned(val1));
}

Frame.prototype.iand = function() {
    this.stack.push(this.stack.pop() & this.stack.pop());
}

Frame.prototype.land = function() {
    this.stack.push2(this.stack.pop2().and(this.stack.pop2()));
}

Frame.prototype.ior = function() {
    this.stack.push(this.stack.pop() | this.stack.pop());
}

Frame.prototype.lor = function() {
    this.stack.push2(this.stack.pop2().or(this.stack.pop2()));
}

Frame.prototype.ixor = function() {
    this.stack.push(this.stack.pop() ^ this.stack.pop());
}

Frame.prototype.lxor = function() {
    this.stack.push2(this.stack.pop2().xor(this.stack.pop2()));
}

Frame.prototype.lcmp = function() {
    var val1 = this.stack.pop2();
    var val2 = this.stack.pop2();
    if (val2.greaterThan(val1)) {
        this.stack.push(1);
    } else if (val2.lessThan(val1)) {
        this.stack.push(-1);
    } else {
        this.stack.push(0);
    }
}

Frame.prototype.fcmpl = function() {
    var val1 = this.stack.pop();
    var val2 = this.stack.pop();
    if (isNaN(val1) || isNaN(val2)) {
        this.stack.push(-1);
    } else if (val2 > val1) {
        this.stack.push(1);
    } else if (val2 < val1) {
        this.stack.push(-1);
    } else {
        this.stack.push(0);
    }
}

Frame.prototype.fcmpg = function() {
    var val1 = this.stack.pop();
    var val2 = this.stack.pop();
    if (isNaN(val1) || isNaN(val2)) {
        this.stack.push(1);
    } else if (val2 > val1) {
        this.stack.push(1);
    } else if (val2 < val1) {
        this.stack.push(-1);
    } else {
        this.stack.push(0);
    }
}

Frame.prototype.dcmpl = function() {
    var val1 = this.stack.pop2();
    var val2 = this.stack.pop2();
    if (isNaN(val1) || isNaN(val2)) {
        this.stack.push(-1);
    } else if (val2 > val1) {
        this.stack.push(1);
    } else if (val2 < val1) {
        this.stack.push(-1);
    } else {
        this.stack.push(0);
    }
}

Frame.prototype.dcmpg = function() {
    var val1 = this.stack.pop2();
    var val2 = this.stack.pop2();
    if (isNaN(val1) || isNaN(val2)) {
        this.stack.push(1);
    } else if (val2 > val1) {
        this.stack.push(1);
    } else if (val2 < val1) {
        this.stack.push(-1);
    } else {
        this.stack.push(0);
    }
}

Frame.prototype.newarray = function() {
    var type = this.read8();
    var size = this.stack.pop();
    if (size < 0) {
        this.raiseException("java/lang/NegativeSizeException");
        return;
    }
    this.stack.push(CLASSES.newArray(this, ARRAY_TYPE[type], size));
}

Frame.prototype.anewarray = function() {
    var idx = this.read16();
    var className = this.cp[this.cp[idx].name_index].bytes;
    var size = this.stack.pop();
    if (size < 0) {
        this.raiseException("java/lang/NegativeSizeException");
        return;
    }
    this.stack.push(new Array(size));
}

Frame.prototype.multianewarray = function() {
    var idx = this.read16();
    var type = this.cp[this.cp[idx].name_index].bytes;
    var dimensions = this.read8();
    var lengths = new Array(dimensions);
    for(var i=0; i<dimensions; i++) {
        lengths[i] = this.stack.pop();
    }
    var createMultiArray = function(lengths) {
        if (lengths.length === 0) {
            return null;
        }
        var length = lengths.shift();
        var array = new Array(length);
        for (var i=0; i<length; i++) {
            array[i] = createMultiArray(lengths);
        }
        return array;
    };
    this.stack.push(createMultiArray(lengths));
}

Frame.prototype.arraylength = function() {
    var ref = this.stack.pop();
    this.stack.push(ref.length);
}

Frame.prototype.if_icmpeq = function() {
    var jmp = this.ip - 1 + this.read16signed();
    var ref1 = this.stack.pop();
    var ref2 = this.stack.pop();
    this.ip = ref1 === ref2 ? jmp : this.ip;
}

Frame.prototype.if_icmpne = function() {
    var jmp = this.ip - 1 + this.read16signed();
    var ref1 = this.stack.pop();
    var ref2 = this.stack.pop();
    this.ip = ref1 !== ref2 ? jmp : this.ip;
}

Frame.prototype.if_icmpgt = function() {
    var jmp = this.ip - 1 + this.read16signed();
    var ref1 = this.stack.pop();
    var ref2 = this.stack.pop();
    this.ip = ref1 < ref2 ? jmp : this.ip;
}

Frame.prototype.if_icmple = function() {
    var jmp = this.ip - 1 + this.read16signed();
    this.ip = this.stack.pop() >= this.stack.pop() ? jmp : this.ip;
}

Frame.prototype.if_icmplt = function() {
    var jmp = this.ip - 1 + this.read16signed();
    this.ip = this.stack.pop() > this.stack.pop() ? jmp : this.ip;
}

Frame.prototype.if_icmpge = function() {
    var jmp = this.ip - 1 + this.read16signed();
    var ref1 = this.stack.pop();
    var ref2 = this.stack.pop();
    this.ip = ref1 <= ref2 ? jmp : this.ip;
}

Frame.prototype.if_acmpeq = function() {
    var jmp = this.ip - 1 + this.read16signed();
    var ref1 = this.stack.pop();
    var ref2 = this.stack.pop();
    this.ip = ref1 === ref2 ? jmp : this.ip;
}

Frame.prototype.if_acmpne = function() {
    var jmp = this.ip - 1 + this.read16signed();
    var ref1 = this.stack.pop();
    var ref2 = this.stack.pop();
    this.ip = ref1 !== ref2 ? jmp : this.ip;
}

Frame.prototype.ifne = function() {
    var jmp = this.ip - 1 + this.read16signed();
    this.ip = this.stack.pop() !== 0 ? jmp : this.ip;
}

Frame.prototype.ifeq = function() {
    var jmp = this.ip - 1 + this.read16signed();
    this.ip = this.stack.pop() === 0 ? jmp : this.ip;
}

Frame.prototype.iflt = function() {
    var jmp = this.ip - 1 + this.read16signed();
    this.ip = this.stack.pop() < 0 ? jmp : this.ip;
}

Frame.prototype.ifge = function() {
    var jmp = this.ip - 1 + this.read16signed();
    this.ip = this.stack.pop() >= 0 ? jmp : this.ip;
}

Frame.prototype.ifgt = function() {
    var jmp = this.ip - 1 + this.read16signed();
    this.ip = this.stack.pop() > 0 ? jmp : this.ip;
}

Frame.prototype.ifle = function() {
    var jmp = this.ip - 1 + this.read16signed();
    this.ip = this.stack.pop() <= 0 ? jmp : this.ip;
}

Frame.prototype.i2l = function() {
    this.stack.push2(new gLong(this.stack.pop()));
}

Frame.prototype.i2f = function() {
}

Frame.prototype.i2d = function() {
    this.stack.push2(this.stack.pop());
}

Frame.prototype.i2b = function() {
    this.stack.push((this.stack.pop() << 24) >> 24);
}

Frame.prototype.i2c = function() {
    this.stack.push(this.stack.pop() & 0xffff);
}

Frame.prototype.i2s = function() {
    this.stack.push((this.stack.pop() << 16) >> 16);
}

Frame.prototype.l2i = function() {
    this.stack.push(this.stack.pop2().toInt());
}

Frame.prototype.l2d = function() {
    this.stack.push2(this.stack.pop2().toNumber());
}

Frame.prototype.l2f = function() {
    this.stack.push(utils.double2float(this.stack.pop2().toNumber()));
}

Frame.prototype.d2i = function() {
    this.stack.push(utils.double2int(this.stack.pop2()));
}

Frame.prototype.d2l = function() {
    this.stack.push2(utils.double2long(this.stack.pop2()));
}

Frame.prototype.d2f = function() {
    this.stack.push(utils.double2float(this.stack.pop2()));
}

Frame.prototype.f2d = function() {
    this.stack.push2(this.stack.pop());
}

Frame.prototype.f2i = function() {
    this.stack.push(utils.double2int(this.stack.pop()));
}

Frame.prototype.f2l = function() {
    this.stack.push2(gLong.fromNumber(this.stack.pop()));
}

Frame.prototype.goto = function() {
    this.ip += this.read16signed() - 1;
}

Frame.prototype.goto_w = function() {
    this.ip += this.read32signed() - 1;
}

Frame.prototype.ifnull = function() {
    var ref = this.stack.pop();
    if (!ref) {
        this.ip += this.read16signed() - 1;
    }
}

Frame.prototype.ifnonnull = function() {
    var ref = this.stack.pop();
    if (!!ref) {
        this.ip += this.read16signed() - 1;
    }
}

Frame.prototype.putfield = function() {
    var idx = this.read16();
    var fieldName = this.cp[this.cp[this.cp[idx].name_and_type_index].name_index].bytes;
    var val = this.stack.pop();
    var obj = this.stack.pop();
    if (!obj) {
        this.raiseException("java/lang/NullPointerException");
        return;
    }
    obj[fieldName] = val;
}

Frame.prototype.getfield = function() {
    var cp = this.cp;
    var nameAndType = cp[cp[this.read16()].name_and_type_index];
    var fieldName = cp[nameAndType.name_index].bytes;
    var obj = this.stack.pop();
    if (!obj) {
        this.raiseException("java/lang/NullPointerException");
        return;
    }
    var value = obj[fieldName];
    if (typeof value === "undefined") {
        value = util.defaultValue(cp[nameAndType.signature_index].bytes);
    }
    this.stack.push(value);
}


Frame.prototype.new = function() {
    var idx = this.read16();
    var className = this.cp[this.cp[idx].name_index].bytes;
    this.stack.push(CLASSES.newObject(this, className));
}

Frame.prototype.getstatic = function() {
    var idx = this.read16();
    var className = this.cp[this.cp[this.cp[idx].class_index].name_index].bytes;
    var fieldName = this.cp[this.cp[this.cp[idx].name_and_type_index].name_index].bytes;
    this.stack.push(CLASSES.getStaticField(this, className, fieldName));
}

Frame.prototype.putstatic = function() {
    var idx = this.read16();
    var className = this.cp[this.cp[this.cp[idx].class_index].name_index].bytes;
    var fieldName = this.cp[this.cp[this.cp[idx].name_and_type_index].name_index].bytes;
    CLASSES.setStaticField(this, className, fieldName, this.stack.pop());
}

Frame.prototype.invokestatic = Frame.prototype.invokevirtual = Frame.prototype.invokespecial = Frame.prototype.invokeinterface = function() {
    var op = this.getOp();

    var idx = this.read16();

    if (op === OPCODES.invokeinterface) {
        var argsNumber = this.read8();
        var zero = this.read8();
    }

    var cp = this.cp;

    var className = cp[cp[cp[idx].class_index].name_index].bytes;
    var methodName = cp[cp[cp[idx].name_and_type_index].name_index].bytes;
    var signature = cp[cp[cp[idx].name_and_type_index].signature_index].bytes;

    var classInfo = CLASSES.getClass(this, className);
    var method = CLASSES.getMethod(this, classInfo, methodName, signature, op === OPCODES.invokestatic);

    this.invoke(op, method);
}

Frame.prototype.jsr = function() {
    var jmp = this.read16();
    this.stack.push(this.ip);
    this.ip = jmp;
}

Frame.prototype.jsr_w = function() {
    var jmp = this.read32();
    this.stack.push(this.ip);
    this.ip = jmp;
}

Frame.prototype.ret = function() {
    var idx = this.isWide() ? this.read16() : this.read8();
    this.ip = this.getLocal(idx);
}

Frame.prototype.tableswitch = function() {
    var startip = this.ip;
    var jmp;

    while ((this.ip % 4) != 0) {
        this.ip++;
    }

    var def = this.read32signed();
    var low = this.read32signed();
    var high = this.read32signed();
    var val = this.stack.pop();

    if (val < low || val > high) {
        jmp = def;
    } else {
        this.ip  += (val - low) << 2;
        jmp = this.read32signed();
    }

    this.ip = startip - 1 + jmp;
}

Frame.prototype.lookupswitch = function() {
    var startip = this.ip;

    while ((this.ip % 4) != 0) {
        this.ip++;
    }

    var jmp = this.read32signed();
    var size = this.read32();
    var val = this.stack.pop();

    lookup:
        for(var i=0; i<size; i++) {
            var key = this.read32signed();
            var offset = this.read32signed();
            if (key === val) {
                jmp = offset;
            }
            if (key >= val) {
                break lookup;
            }
        }

    this.ip = startip - 1 + jmp;
}

Frame.prototype.instanceof = function() {
    var idx = this.read16();
    var className = this.cp[this.cp[idx].name_index].bytes;
    var obj = this.stack.pop();
    this.stack.push(obj.class.className === className);
}

Frame.prototype.checkcast = function() {
    var idx = this.read16();
    var type = this.cp[this.cp[idx].name_index].bytes;
}

Frame.prototype.athrow = function() {
    this.throw(this.stack.pop());
}

Frame.prototype.wide = function() {
}

Frame.prototype.monitorenter = function() {
    var obj = this.stack.pop();
    if (!obj) {
        this.raiseException("java/lang/NullPointerException");
        return;
    }
    /*
    if (obj.hasOwnProperty("$lock$")) {
        this.stack.push(obj);
        this.ip--;
        // SCHEDULER.yield();
    } else {
        obj["$lock$"] = "locked";
    }
    */
}

Frame.prototype.monitorexit = function() {
    var obj = this.stack.pop();
    if (!obj) {
        this.raiseException("java/lang/NullPointerException");
        return;
    }
    /*
    delete obj["$lock$"];
    // SCHEDULER.yield();
    */
}
