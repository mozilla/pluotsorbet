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

Frame.prototype.u16_to_s16 = function(x) {
    return (x > 0x7fff) ? (x - 0x10000) : x;
}

Frame.prototype.u32_to_s32 = function(x) {
    return (x > 0x7fffffff) ? (x - 0x100000000) : x;
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

Frame.prototype.read16signed = function() {
    return this.u16_to_s16(this.read16());
}

Frame.prototype.read32signed = function() {
    return this.u32_to_s32(this.read32());
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

Frame.prototype.invoke = function(op, methodInfo) {
    if (ACCESS_FLAGS.isNative(methodInfo.access_flags)) {
        NATIVE.invokeNative(this, methodInfo);
        return;
    }

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
            console.log("virtual dispatch", methodInfo.classInfo.className, obj.class.className, methodInfo.name, methodInfo.signature);
            if (methodInfo.classInfo != obj.class)
                methodInfo = CLASSES.getMethod(this, obj.class, methodInfo.name, methodInfo.signature, op === OPCODES.invokestatic);
            break;
        }
    }

    var callee = new Frame(methodInfo);
    callee.locals = this.stack;
    callee.localsBase = this.stack.length - consumes;

    console.log("consumes:", consumes, "localsBase:", callee.localsBase);

    while (true) {
        var op = callee.read8();
        console.log(callee.methodInfo.classInfo.className, callee.methodInfo.name, callee.ip - 1, OPCODES[op], callee.stack.join(","));
        switch (op) {
        case OPCODES.return:
            this.stack.length -= consumes;
            return;

        case OPCODES.ireturn:
        case OPCODES.freturn:
        case OPCODES.areturn:
            this.stack.length -= consumes;
            this.stack.push(callee.stack.pop());
            return;

        case OPCODES.lreturn:
        case OPCODES.dreturn:
            this.stack.length -= consumes;
            this.stack.push2(callee.stack.pop2());
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

Frame.prototype.nop = function() {
}

Frame.prototype.aconst_null = function() {
    this.stack.push(null);
}

Frame.prototype.iconst_m1 = function() {
    this.stack.push(-1);
}

Frame.prototype.iconst_0 = Frame.prototype.fconst_0 = function() {
    this.stack.push(0);
}

Frame.prototype.lconst_0 = Frame.prototype.dconst_0 = function() {
    this.stack.push2(0);
}

Frame.prototype.iconst_1 = Frame.prototype.fconst_1 = function() {
    this.stack.push(1);
}

Frame.prototype.lconst_1 = Frame.prototype.dconst_1 = function() {
    this.stack.push2(1);
}

Frame.prototype.iconst_2 = Frame.prototype.fconst_2 = function() {
    this.stack.push(2);
}

Frame.prototype.iconst_3 = function() {
    this.stack.push(3);
}

Frame.prototype.iconst_4 = function() {
    this.stack.push(4);
}

Frame.prototype.iconst_5 = function() {
    this.stack.push(5);
}

Frame.prototype.sipush = function() {
    this.stack.push(this.read16());
}

Frame.prototype.bipush = function() {
    this.stack.push(this.read8());
}

Frame.prototype.ldc = function() {
    var constant = this.cp[this.read8()];
    switch(constant.tag) {
        case TAGS.CONSTANT_String:
            this.stack.push(CLASSES.newString(this, this.cp[constant.string_index].bytes));
            break;
        default:
            throw new Error("not support constant type");
    }
}

Frame.prototype.ldc_w = function() {
    var constant = this.cp[this.read16()];
    switch(constant.tag) {
        case TAGS.CONSTANT_String:
            this.stack.push(this.cp[constant.string_index].bytes);
            break;
        default:
            throw new Error("not support constant type");
    }
}

Frame.prototype.ldc2_w = function() {
    var constant = this.cp[this.read16()];
    switch(constant.tag) {
        case TAGS.CONSTANT_String:
            this.stack.push(this.cp[constant.string_index].bytes);
            break;
        case TAGS.CONSTANT_Long:
            this.stack.push2(Numeric.getLong(constant.bytes));
            break;
        case TAGS.CONSTANT_Double:
            this.stack.push2(constant.bytes.readDoubleBE(0));
            break;
        default:
            throw new Error("not support constant type");
    }
}

Frame.prototype.iload = Frame.prototype.iload = Frame.prototype.aload = function() {
    var idx = this.isWide() ? this.read16() : this.read8();
    this.stack.push(this.getLocal(idx));
}

Frame.prototype.lload = Frame.prototype.dload = function() {
    var idx = this.isWide() ? this.read16() : this.read8();
    this.stack.push2(this.getLocal(idx));
}

Frame.prototype.iload_0 = Frame.prototype.fload_0 = Frame.prototype.aload_0 = function() {
    this.stack.push(this.getLocal(0));
}

Frame.prototype.lload_0 = Frame.prototype.dloat_0 = function() {
    this.stack.push2(this.getLocal(0));
}

Frame.prototype.iload_1 = Frame.prototype.fload_1 = Frame.prototype.aload_1 = function() {
    this.stack.push(this.getLocal(1));
}

Frame.prototype.lload_1 = Frame.prototype.dloat_1 = function() {
    this.stack.push2(this.getLocal(1));
}

Frame.prototype.iload_2 = Frame.prototype.fload_2 = Frame.prototype.aload_2 = function() {
    this.stack.push(this.getLocal(2));
}

Frame.prototype.lload_2 = Frame.prototype.dloat_2 = function() {
    this.stack.push2(this.getLocal(2));
}

Frame.prototype.iload_3 = Frame.prototype.fload_3 = Frame.prototype.aload_3 = function() {
    this.stack.push(this.getLocal(3));
}

Frame.prototype.lload_3 = Frame.prototype.dloat_3 = function() {
    this.stack.push2(this.getLocal(3));
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

Frame.prototype.iaload = Frame.prototype.faload = Frame.prototype.aaload = Frame.prototype.baload = Frame.prototype.caload = Frame.prototype.saload = function() {
    var idx = this.stack.pop();
    var refArray = this.stack.pop();
    if (!this.checkArrayAccess(refArray, idx)) {
        return;
    }
    this.stack.push(refArray[idx]);
}

Frame.prototype.laload = Frame.prototype.daload = function() {
    var idx = this.stack.pop();
    var refArray = this.stack.pop();
    if (!this.checkArrayAccess(refArray, idx)) {
        return;
    }
    this.stack.push2(refArray[idx]);
}

Frame.prototype.istore = Frame.prototype.fstore = Frame.prototype.astore = function() {
    var idx = this.isWide() ? this.read16() : this.read8();
    this.setLocal(idx, this.stack.pop());
}

Frame.prototype.lstore = Frame.prototype.dstore = function() {
    var idx = this.isWide() ? this.read16() : this.read8();
    this.setLocal(idx, this.stack.pop2());
}

Frame.prototype.istore_0 = Frame.prototype.fstore_0 = Frame.prototype.astore_0 = function() {
    this.setLocal(0, this.stack.pop());
}

Frame.prototype.lstore_0 = Frame.prototype.dstore_0 = function() {
    this.setLocal(0, this.stack.pop2());
}

Frame.prototype.istore_1 = Frame.prototype.fstore_1 = Frame.prototype.astore_1 = function() {
    this.setLocal(1, this.stack.pop());
}

Frame.prototype.lstore_1 = Frame.prototype.dstore_1 = function() {
    this.setLocal(1, this.stack.pop2());
}

Frame.prototype.istore_2 = Frame.prototype.fstore_2 = Frame.prototype.astore_2 = function() {
    this.setLocal(2, this.stack.pop());
}

Frame.prototype.lstore_2 = Frame.prototype.dstore_2 = function() {
    this.setLocal(2, this.stack.pop2());
}

Frame.prototype.istore_3 = Frame.prototype.fstore_3 = Frame.prototype.astore_3 = function() {
    this.setLocal(3, this.stack.pop());
}

Frame.prototype.lstore_3 = Frame.prototype.dstore_3 = function() {
    this.setLocal(3, this.stack.pop2());
}

Frame.prototype.iastore = Frame.prototype.fastore = Frame.prototype.aastore = Frame.prototype.bastore = Frame.prototype.castore = Frame.prototype.sastore = function() {
    var val = this.stack.pop();
    var idx = this.stack.pop();
    var refArray = this.stack.pop();
    if (!this.checkArrayAccess(refArray, idx)) {
        return;
    }
    refArray[idx] = val;
}

Frame.prototype.lastore = Frame.prototype.dastore = function() {
    var val = this.stack.pop2();
    var idx = this.stack.pop();
    var refArray = this.stack.pop();
    if (!this.checkArrayAccess(refArray, idx)) {
        return;
    }
    refArray[idx] = val;
}

Frame.prototype.pop = function() {
    this.stack.pop();
}

Frame.prototype.pop2 = function() {
    this.stack.pop2();
}

Frame.prototype.dup = function() {
    var val = this.stack.pop();
    this.stack.push(val);
    this.stack.push(val);
}

Frame.prototype.dup_x1 = function() {
    var val1 = this.stack.pop();
    var val2 = this.stack.pop();
    this.stack.push(val1);
    this.stack.push(val2);
    this.stack.push(val1);
}

Frame.prototype.dup_x2 = function() {
    var val1 = this.stack.pop();
    var val2 = this.stack.pop();
    var val3 = this.stack.pop();
    this.stack.push(val1);
    this.stack.push(val3);
    this.stack.push(val2);
    this.stack.push(val1);
}

Frame.prototype.dup2 = function() {
    var val1 = this.stack.pop();
    var val2 = this.stack.pop();
    this.stack.push(val2);
    this.stack.push(val1);
    this.stack.push(val2);
    this.stack.push(val1);
}

Frame.prototype.dup2_x1 = function() {
    var val1 = this.stack.pop();
    var val2 = this.stack.pop();
    var val3 = this.stack.pop();
    this.stack.push(val2);
    this.stack.push(val1);
    this.stack.push(val3);
    this.stack.push(val2);
    this.stack.push(val1);
}

Frame.prototype.dup2_x2 = function() {
    var val1 = this.stack.pop();
    var val2 = this.stack.pop();
    var val3 = this.stack.pop();
    var val4 = this.stack.pop();
    this.stack.push(val2);
    this.stack.push(val1);
    this.stack.push(val4);
    this.stack.push(val3);
    this.stack.push(val2);
    this.stack.push(val1);
}

Frame.prototype.swap = function() {
    var val1 = this.stack.pop();
    var val2 = this.stack.pop();
    this.stack.push(val1);
    this.stack.push(val2);
}

Frame.prototype.iinc = function() {
    var wide = this.isWide();
    var idx = wide ? this.read16() : this.read8();
    var val = wide ? this.read16() : this.read8();
    this.setLocal(idx, this.getLocal(idx) + val);
}

Frame.prototype.iadd = function() {
    this.stack.push((this.stack.pop() + this.stack.pop())|0);
}

Frame.prototype.ladd = function() {
    this.stack.push2(this.stack.pop2().add(this.stack.pop2()));
}

Frame.prototype.dadd = function() {
    this.stack.push2(this.stack.pop2() + this.stack.pop2());
}

Frame.prototype.fadd = function() {
    this.stack.push(utils.double2float(this.stack.pop() + this.stack.pop()));
}

Frame.prototype.isub = function() {
    this.stack.push((- this.stack.pop() + this.stack.pop())|0);
}

Frame.prototype.lsub = function() {
    this.stack.push2(this.stack.pop2().add(this.stack.pop2()).negate());
}

Frame.prototype.dsub = function() {
    this.stack.push2(- this.stack.pop2() + this.stack.pop2());
}

Frame.prototype.fsub = function() {
    this.stack.push(utils.double2float(- this.stack.pop() + this.stack.pop()));
}

Frame.prototype.imul = function() {
    this.stack.push(Math.imul(this.stack.pop(), this.stack.pop()));
}

Frame.prototype.lmul = function() {
    this.stack.push2(this.stack.pop2().multiply(this.stack.pop2()));
}

Frame.prototype.dmul = function() {
    this.stack.push2(this.stack.pop2() * this.stack.pop2());
}

Frame.prototype.fmul = function() {
    this.stack.push(utils.double2float(this.stack.pop() * this.stack.pop()));
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

    var def = this.read32();
    var low = this.read32();
    var high = this.read32();
    var val = this.stack.pop();

    if (val < low || val > high) {
        jmp = def;
    } else {
        this.ip  += (val - low) << 2;
        jmp = this.read32();
    }

    this.ip = startip - 1 + this.u32_to_s32(jmp);
}

Frame.prototype.lookupswitch = function() {
    var startip = this.ip;

    while ((this.ip % 4) != 0) {
        this.ip++;
    }

    var jmp = this.read32();
    var size = this.read32();
    var val = this.stack.pop();

    lookup:
        for(var i=0; i<size; i++) {
            var key = this.read32();
            var offset = this.read32();
            if (key === val) {
                jmp = offset;
            }
            if (key >= val) {
                break lookup;
            }
        }

    this.ip = startip - 1 + this.u32_to_s32(jmp);
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
    if (obj.hasOwnProperty("$lock$")) {
        this.stack.push(obj);
        this.ip--;
        // SCHEDULER.yield();
    } else {
        obj["$lock$"] = "locked";
    }
}

Frame.prototype.monitorexit = function() {
    var obj = this.stack.pop();
    if (!obj) {
        this.raiseException("java/lang/NullPointerException");
        return;
    }
    delete obj["$lock$"];
    // SCHEDULER.yield();
}
