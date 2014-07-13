/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var Frame = function(methodInfo) {
    if (this instanceof Frame) {
        this.methodInfo = methodInfo;
        this.classInfo = methodInfo.classInfo;
        this.cp = this.classInfo.getConstantPool();
        this.signature = Signature.parse(this.cp[methodInfo.signature_index].bytes);
        this.code = methodInfo.code;
    } else {
        return new Frame(methodInfo);
    }
}

Frame.prototype.isWide = function() {
    return this.code[this.ip - 2] == OPCODES.wide;
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
                if (name === ex.getClassName()) {
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

Frame.prototype.newException = function(className, message) {
    var ex = CLASSES.newObject(className);
    var ctor = CLASSES.getMethod(className, "<init>", "(Ljava/lang/String;)V");
    this.invoke(ctor, ex, [message]);
    this.throw(ex);
}

Frame.prototype.run = function(stack) {
    var isStatic = ACCESS_FLAGS.isStatic(this.methodInfo.access_flags);
    var argc = 0;
    if (!isStatic) {
        ++argc;
    }
    var IN = this.signature.IN;
    for (var i=0; i<IN.length; i++) {
        var type = IN[i].type;
        ++argc;
        if (type === "long" || type === "double")
            ++argc;
    }

    var locals = stack.reserveLocals(argc, this.methodInfo.max_locals);
    if (!isStatic && !locals.get(0)) {
        this.newException("java/lang/NullPointerException");
        return;
    }

    var args = "args: ";
    for (var i=0; i < argc; ++i)
        args += locals.get(i) + " ";
    console.log(args);

    this.ip = 0;

    while (true) {
        var op = this.read8();
        console.log(this.classInfo.getClassName(), this.cp[this.methodInfo.name_index].bytes,
                    this.ip - 1, OPCODES[op], stack.array.length);
        switch (op) {
        case OPCODES.return:
            stack.popLocals(locals);
            return;

        case OPCODES.ireturn:
        case OPCODES.freturn:
        case OPCODES.areturn:
            var result = stack.pop();
            stack.popLocals(locals);
            stack.push(result);
            return;

        case OPCODES.lreturn:
        case OPCODES.dreturn:
            var result = stack.pop2();
            stack.popLocals(locals);
            stack.push2(result);
            return;

        default:
            var opName = OPCODES[op];
            if (!(opName in this)) {
                throw new Error(util.format("Opcode %s [%s] is not supported.", opName, op));
            }
            this[opName](stack, locals);
            break;
        }
    };
}

Frame.prototype.nop = function(stack, locals) {
}

Frame.prototype.aconst_null = function(stack, locals) {
    stack.push(null);
}

Frame.prototype.iconst_m1 = function(stack, locals) {
    stack.push(-1);
}

Frame.prototype.iconst_0 = Frame.prototype.fconst_0 = function(stack, locals) {
    stack.push(0);
}

Frame.prototype.lconst_0 = Frame.prototype.dconst_0 = function(stack, locals) {
    stack.push2(0);
}

Frame.prototype.iconst_1 = Frame.prototype.fconst_1 = function(stack, locals) {
    stack.push(1);
}

Frame.prototype.lconst_1 = Frame.prototype.dconst_1 = function(stack, locals) {
    stack.push2(1);
}

Frame.prototype.iconst_2 = Frame.prototype.fconst_2 = function(stack, locals) {
    stack.push(2);
}

Frame.prototype.iconst_3 = function(stack, locals) {
    stack.push(3);
}

Frame.prototype.iconst_4 = function(stack, locals) {
    stack.push(4);
}

Frame.prototype.iconst_5 = function(stack, locals) {
    stack.push(5);
}

Frame.prototype.sipush = function(stack, locals) {
    stack.push(this.read16());
}

Frame.prototype.bipush = function(stack, locals) {
    stack.push(this.read8());
}

Frame.prototype.ldc = function(stack, locals) {
    var constant = this.cp[this.read8()];
    switch(constant.tag) {
        case TAGS.CONSTANT_String:
            stack.push(CLASSES.newString(this.cp[constant.string_index].bytes));
            break;
        default:
            throw new Error("not support constant type");
    }
}

Frame.prototype.ldc_w = function(stack, locals) {
    var constant = this.cp[this.read16()];
    switch(constant.tag) {
        case TAGS.CONSTANT_String:
            stack.push(this.cp[constant.string_index].bytes);
            break;
        default:
            throw new Error("not support constant type");
    }
}

Frame.prototype.ldc2_w = function(stack, locals) {
    var constant = this.cp[this.read16()];
    switch(constant.tag) {
        case TAGS.CONSTANT_String:
            stack.push(this.cp[constant.string_index].bytes);
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
}

Frame.prototype.iload = Frame.prototype.iload = Frame.prototype.aload = function(stack, locals) {
    var idx = this.isWide() ? this.read16() : this.read8();
    stack.push(locals.get(idx));
}

Frame.prototype.lload = Frame.prototype.dload = function(stack, locals) {
    var idx = this.isWide() ? this.read16() : this.read8();
    stack.push2(locals.get(idx));
}

Frame.prototype.iload_0 = Frame.prototype.fload_0 = Frame.prototype.aload_0 = function(stack, locals) {
    stack.push(locals.get(0));
}

Frame.prototype.lload_0 = Frame.prototype.dloat_0 = function(stack, locals) {
    stack.push2(locals.get(0));
}

Frame.prototype.iload_1 = Frame.prototype.fload_1 = Frame.prototype.aload_1 = function(stack, locals) {
    stack.push(locals.get(1));
}

Frame.prototype.lload_1 = Frame.prototype.dloat_1 = function(stack, locals) {
    stack.push2(locals.get(1));
}

Frame.prototype.iload_2 = Frame.prototype.fload_2 = Frame.prototype.aload_2 = function(stack, locals) {
    stack.push(locals.get(2));
}

Frame.prototype.lload_2 = Frame.prototype.dloat_2 = function(stack, locals) {
    stack.push2(locals.get(2));
}

Frame.prototype.iload_3 = Frame.prototype.fload_3 = Frame.prototype.aload_3 = function(stack, locals) {
    stack.push(locals.get(3));
}

Frame.prototype.lload_3 = Frame.prototype.dloat_3 = function(stack, locals) {
    stack.push2(locals.get(3));
}

Frame.prototype.checkArrayAccess = function(refArray, idx) {
    if (!refArray) {
        this.newException("java/lang/NullPointerException");
        return false;
    }
    if (idx < 0 || idx >= refArray.length) {
        this.newException("java/lang/ArrayIndexOutOfBoundsException", idx);
        return false;
    }
    return true;
}

Frame.prototype.iaload = Frame.prototype.faload = Frame.prototype.aaload = Frame.prototype.baload = Frame.prototype.caload = Frame.prototype.saload = function(stack, locals) {
    var idx = stack.pop();
    var refArray = stack.pop();
    if (!this.checkArrayAccess(refArray, idx)) {
        return;
    }
    stack.push(refArray[idx]);
}

Frame.prototype.laload = Frame.prototype.daload = function(stack, locals) {
    var idx = stack.pop();
    var refArray = stack.pop();
    if (!this.checkArrayAccess(refArray, idx)) {
        return;
    }
    stack.push2(refArray[idx]);
}

Frame.prototype.istore = Frame.prototype.fstore = Frame.prototype.astore = function(stack, locals) {
    var idx = this.isWide() ? this.read16() : this.read8();
    locals.set(idx, stack.pop());
}

Frame.prototype.lstore = Frame.prototype.dstore = function(stack, locals) {
    var idx = this.isWide() ? this.read16() : this.read8();
    locals.set(idx, stack.pop2());
}

Frame.prototype.istore_0 = Frame.prototype.fstore_0 = Frame.prototype.astore_0 = function(stack, locals) {
    locals.set(0, stack.pop());
}

Frame.prototype.lstore_0 = Frame.prototype.dstore_0 = function(stack, locals) {
    locals.set(0, stack.pop2());
}

Frame.prototype.istore_1 = Frame.prototype.fstore_1 = Frame.prototype.astore_1 = function(stack, locals) {
    locals.set(1, stack.pop());
}

Frame.prototype.lstore_1 = Frame.prototype.dstore_1 = function(stack, locals) {
    locals.set(1, stack.pop2());
}

Frame.prototype.istore_2 = Frame.prototype.fstore_2 = Frame.prototype.astore_2 = function(stack, locals) {
    locals.set(2, stack.pop());
}

Frame.prototype.lstore_2 = Frame.prototype.dstore_2 = function(stack, locals) {
    locals.set(2, stack.pop2());
}

Frame.prototype.istore_3 = Frame.prototype.fstore_3 = Frame.prototype.astore_3 = function(stack, locals) {
    locals.set(3, stack.pop());
}

Frame.prototype.lstore_3 = Frame.prototype.dstore_3 = function(stack, locals) {
    locals.set(3, stack.pop2());
}

Frame.prototype.iastore = Frame.prototype.fastore = Frame.prototype.aastore = Frame.prototype.bastore = Frame.prototype.castore = Frame.prototype.sastore = function(stack, locals) {
    var val = stack.pop();
    var idx = stack.pop();
    var refArray = stack.pop();
    if (!this.checkArrayAccess(refArray, idx)) {
        return;
    }
    refArray[idx] = val;
}

Frame.prototype.lastore = Frame.prototype.dastore = function(stack, locals) {
    var val = stack.pop2();
    var idx = stack.pop();
    var refArray = stack.pop();
    if (!this.checkArrayAccess(refArray, idx)) {
        return;
    }
    refArray[idx] = val;
}

Frame.prototype.pop = function(stack, locals) {
    stack.pop();
}

Frame.prototype.pop2 = function(stack, locals) {
    stack.pop2();
}

Frame.prototype.dup = function(stack, locals) {
    var val = stack.pop();
    stack.push(val);
    stack.push(val);
}

Frame.prototype.dup_x1 = function(stack, locals) {
    var val1 = stack.pop();
    var val2 = stack.pop();
    stack.push(val1);
    stack.push(val2);
    stack.push(val1);
}

Frame.prototype.dup_x2 = function(stack, locals) {
    var val1 = stack.pop();
    var val2 = stack.pop();
    var val3 = stack.pop();
    stack.push(val1);
    stack.push(val3);
    stack.push(val2);
    stack.push(val1);
}

Frame.prototype.dup2 = function(stack, locals) {
    var val1 = stack.pop();
    var val2 = stack.pop();
    stack.push(val2);
    stack.push(val1);
    stack.push(val2);
    stack.push(val1);
}

Frame.prototype.dup2_x1 = function(stack, locals) {
    var val1 = stack.pop();
    var val2 = stack.pop();
    var val3 = stack.pop();
    stack.push(val2);
    stack.push(val1);
    stack.push(val3);
    stack.push(val2);
    stack.push(val1);
}

Frame.prototype.dup2_x2 = function(stack, locals) {
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
}

Frame.prototype.swap = function(stack, locals) {
    var val1 = stack.pop();
    var val2 = stack.pop();
    stack.push(val1);
    stack.push(val2);
}

Frame.prototype.iinc = function(stack, locals) {
    var wide = this.isWide();
    var idx = wide ? this.read16() : this.read8();
    var val = wide ? this.read16() : this.read8();
    locals.set(idx, locals.get(idx) + val);
}

Frame.prototype.iadd = function(stack, locals) {
    stack.push((stack.pop() + stack.pop())|0);
}

Frame.prototype.ladd = function(stack, locals) {
    stack.push2(stack.pop2().add(stack.pop2()));
}

Frame.prototype.dadd = function(stack, locals) {
    stack.push2(stack.pop2() + stack.pop2());
}

Frame.prototype.fadd = function(stack, locals) {
    stack.push(utils.double2float(stack.pop() + stack.pop()));
}

Frame.prototype.isub = function(stack, locals) {
    stack.push((- stack.pop() + stack.pop())|0);
}

Frame.prototype.lsub = function(stack, locals) {
    stack.push2(stack.pop2().add(stack.pop2()).negate());
}

Frame.prototype.dsub = function(stack, locals) {
    stack.push2(- stack.pop2() + stack.pop2());
}

Frame.prototype.fsub = function(stack, locals) {
    stack.push(utils.double2float(- stack.pop() + stack.pop()));
}

Frame.prototype.imul = function(stack, locals) {
    stack.push(Math.imul(stack.pop(), stack.pop()));
}

Frame.prototype.lmul = function(stack, locals) {
    stack.push2(stack.pop2().multiply(stack.pop2()));
}

Frame.prototype.dmul = function(stack, locals) {
    stack.push2(stack.pop2() * stack.pop2());
}

Frame.prototype.fmul = function(stack, locals) {
    stack.push(utils.double2float(stack.pop() * stack.pop()));
}

Frame.prototype.idiv = function(stack, locals) {
    var val1 = stack.pop();
    var val2 = stack.pop();
    if (!val1) {
        this.newException("java/lang/ArithmeticException", "/ by zero");
        return;
    }
    stack.push((val2 === utils.INT_MIN && val1 === -1) ? val2 : ((a / b)|0));
}

Frame.prototype.ldiv = function(stack, locals) {
    var val1 = stack.pop2();
    var val2 = stack.pop2();
    if (!val1.isZero()) {
        this.newException("java/lang/ArithmeticException", "/ by zero");
        return;
    }
    stack.push2(val2.div(val1));
}

Frame.prototype.ddiv = function(stack, locals) {
    var val1 = stack.pop2();
    var val2 = stack.pop2();
    stack.push2(val2 / val1);
}

Frame.prototype.fdiv = function(stack, locals) {
    var val1 = stack.pop();
    var val2 = stack.pop();
    stack.push(utils.double2float(val2 / val1));
}

Frame.prototype.irem = function(stack, locals) {
    var val1 = stack.pop();
    var val2 = stack.pop();
    if (!val1) {
        this.newException("java/lang/ArithmeticException", "/ by zero");
        return;
    }
    stack.push(val2 % val1);
}

Frame.prototype.lrem = function(stack, locals) {
    var val1 = stack.pop2();
    var val2 = stack.pop2();
    if (val1.isZero()) {
        this.newException("java/lang/ArithmeticException", "/ by zero");
        return;
    }
    stack.push2(val2.modulo(val1));
}

Frame.prototype.drem = function(stack, locals) {
    var val1 = stack.pop2();
    var val2 = stack.pop2();
    stack.push2(val2 % val1);
}

Frame.prototype.frem = function(stack, locals) {
    var val1 = stack.pop();
    var val2 = stack.pop();
    stack.push(utils.double2float(val2 % val1));
}

Frame.prototype.ineg = function(stack, locals) {
    stack.push((- stack.pop())|0);
}

Frame.prototype.lneg = function(stack, locals) {
    stack.push2(stack.pop2().negate());
}

Frame.prototype.dneg = function(stack, locals) {
    stack.push2(- stack.pop2());
}

Frame.prototype.fneg = function(stack, locals) {
    stack.push(- stack.pop());
}

Frame.prototype.ishl = function(stack, locals) {
    var val1 = stack.pop();
    var val2 = stack.pop();
    stack.push(val2 << val1);
}

Frame.prototype.lshl = function(stack, locals) {
    var val1 = stack.pop2();
    var val2 = stack.pop2();
    stack.push2(val2.shiftLeft(val1));
}

Frame.prototype.ishr = function(stack, locals) {
    var val1 = stack.pop();
    var val2 = stack.pop();
    stack.push(val2 >> val1);
}

Frame.prototype.lshr = function(stack, locals) {
    var val1 = stack.pop2();
    var val2 = stack.pop2();
    stack.push2(val2.shiftRight(val1));
}

Frame.prototype.iushr = function(stack, locals) {
    var val1 = stack.pop();
    var val2 = stack.pop();
    stack.push(val2 >>> val1);
}

Frame.prototype.lushr = function(stack, locals) {
    var val1 = stack.pop2();
    var val2 = stack.pop2();
    stack.push2(val2.shiftRightUnsigned(val1));
}

Frame.prototype.iand = function(stack, locals) {
    stack.push(stack.pop() & stack.pop());
}

Frame.prototype.land = function(stack, locals) {
    stack.push2(stack.pop2().and(stack.pop2()));
}

Frame.prototype.ior = function(stack, locals) {
    stack.push(stack.pop() | stack.pop());
}

Frame.prototype.lor = function(stack, locals) {
    stack.push2(stack.pop2().or(stack.pop2()));
}

Frame.prototype.ixor = function(stack, locals) {
    stack.push(stack.pop() ^ stack.pop());
}

Frame.prototype.lxor = function(stack, locals) {
    stack.push2(stack.pop2().xor(stack.pop2()));
}

Frame.prototype.lcmp = function(stack, locals) {
    var val1 = stack.pop2();
    var val2 = stack.pop2();
    if (val2.greaterThan(val1)) {
        stack.push(1);
    } else if (val2.lessThan(val1)) {
        stack.push(-1);
    } else {
        stack.push(0);
    }
}

Frame.prototype.fcmpl = function(stack, locals) {
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
}

Frame.prototype.fcmpg = function(stack, locals) {
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
}

Frame.prototype.dcmpl = function(stack, locals) {
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
}

Frame.prototype.dcmpg = function(stack, locals) {
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
}

Frame.prototype.newarray = function(stack, locals) {
    var type = this.read8();
    var size = stack.pop();
    if (size < 0) {
        this.newException("java/lang/NegativeSizeException");
        return;
    }
    stack.push(CLASSES.newArray(type, size));
}

Frame.prototype.anewarray = function(stack, locals) {
    var idx = this.read16();
    var className = this.cp[this.cp[idx].name_index].bytes;
    var size = stack.pop();
    if (size < 0) {
        this.newException("java/lang/NegativeSizeException");
        return;
    }
    stack.push(new Array(size));
}

Frame.prototype.multianewarray = function(stack, locals) {
    var idx = this.read16();
    var type = this.cp[this.cp[idx].name_index].bytes;
    var dimensions = this.read8();
    var lengths = new Array(dimensions);
    for(var i=0; i<dimensions; i++) {
        lengths[i] = stack.pop();
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
    stack.push(createMultiArray(lengths));
}

Frame.prototype.arraylength = function(stack, locals) {
    var ref = stack.pop();
    stack.push(ref.length);
}

Frame.prototype.if_icmpeq = function(stack, locals) {
    var jmp = this.ip - 1 + this.read16signed();
    var ref1 = stack.pop();
    var ref2 = stack.pop();
    this.ip = ref1 === ref2 ? jmp : this.ip;
}

Frame.prototype.if_icmpne = function(stack, locals) {
    var jmp = this.ip - 1 + this.read16signed();
    var ref1 = stack.pop();
    var ref2 = stack.pop();
    this.ip = ref1 !== ref2 ? jmp : this.ip;
}

Frame.prototype.if_icmpgt = function(stack, locals) {
    var jmp = this.ip - 1 + this.read16signed();
    var ref1 = stack.pop();
    var ref2 = stack.pop();
    this.ip = ref1 < ref2 ? jmp : this.ip;
}

Frame.prototype.if_icmple = function(stack, locals) {
    var jmp = this.ip - 1 + this.read16signed();
    this.ip = stack.pop() >= stack.pop() ? jmp : this.ip;
}

Frame.prototype.if_icmplt = function(stack, locals) {
    var jmp = this.ip - 1 + this.read16signed();
    this.ip = stack.pop() > stack.pop() ? jmp : this.ip;
}

Frame.prototype.if_icmpge = function(stack, locals) {
    var jmp = this.ip - 1 + this.read16signed();
    var ref1 = stack.pop();
    var ref2 = stack.pop();
    this.ip = ref1 <= ref2 ? jmp : this.ip;
}

Frame.prototype.if_acmpeq = function(stack, locals) {
    var jmp = this.ip - 1 + this.read16signed();
    var ref1 = stack.pop();
    var ref2 = stack.pop();
    this.ip = ref1 === ref2 ? jmp : this.ip;
}

Frame.prototype.if_acmpne = function(stack, locals) {
    var jmp = this.ip - 1 + this.read16signed();
    var ref1 = stack.pop();
    var ref2 = stack.pop();
    this.ip = ref1 !== ref2 ? jmp : this.ip;
}

Frame.prototype.ifne = function(stack, locals) {
    var jmp = this.ip - 1 + this.read16signed();
    this.ip = stack.pop() !== 0 ? jmp : this.ip;
}

Frame.prototype.ifeq = function(stack, locals) {
    var jmp = this.ip - 1 + this.read16signed();
    this.ip = stack.pop() === 0 ? jmp : this.ip;
}

Frame.prototype.iflt = function(stack, locals) {
    var jmp = this.ip - 1 + this.read16signed();
    this.ip = stack.pop() < 0 ? jmp : this.ip;
}

Frame.prototype.ifge = function(stack, locals) {
    var jmp = this.ip - 1 + this.read16signed();
    this.ip = stack.pop() >= 0 ? jmp : this.ip;
}

Frame.prototype.ifgt = function(stack, locals) {
    var jmp = this.ip - 1 + this.read16signed();
    this.ip = stack.pop() > 0 ? jmp : this.ip;
}

Frame.prototype.ifle = function(stack, locals) {
    var jmp = this.ip - 1 + this.read16signed();
    this.ip = stack.pop() <= 0 ? jmp : this.ip;
}

Frame.prototype.i2l = function(stack, locals) {
    stack.push2(new gLong(stack.pop()));
}

Frame.prototype.i2f = function(stack, locals) {
}

Frame.prototype.i2d = function(stack, locals) {
    stack.push2(stack.pop());
}

Frame.prototype.i2b = function(stack, locals) {
    stack.push((stack.pop() << 24) >> 24);
}

Frame.prototype.i2c = function(stack, locals) {
    stack.push(stack.pop() & 0xffff);
}

Frame.prototype.i2s = function(stack, locals) {
    stack.push((stack.pop() << 16) >> 16);
}

Frame.prototype.l2i = function(stack, locals) {
    stack.push(stack.pop2().toInt());
}

Frame.prototype.l2d = function(stack, locals) {
    stack.push2(stack.pop2().toNumber());
}

Frame.prototype.l2f = function(stack, locals) {
    stack.push(utils.double2float(stack.pop2().toNumber()));
}

Frame.prototype.d2i = function(stack, locals) {
    stack.push(utils.double2int(stack.pop2()));
}

Frame.prototype.d2l = function(stack, locals) {
    stack.push2(utils.double2long(stack.pop2()));
}

Frame.prototype.d2f = function(stack, locals) {
    stack.push(utils.double2float(stack.pop2()));
}

Frame.prototype.f2d = function(stack, locals) {
    stack.push2(stack.pop());
}

Frame.prototype.f2i = function(stack, locals) {
    stack.push(utils.double2int(stack.pop()));
}

Frame.prototype.f2l = function(stack, locals) {
    stack.push2(gLong.fromNumber(stack.pop()));
}

Frame.prototype.goto = function(stack, locals) {
    this.ip += this.read16signed() - 1;
}

Frame.prototype.goto_w = function(stack, locals) {
    this.ip += this.read32signed() - 1;
}

Frame.prototype.ifnull = function(stack, locals) {
    var ref = stack.pop();
    if (!ref) {
        this.ip += this.read16signed() - 1;
    }
}

Frame.prototype.ifnonnull = function(stack, locals) {
    var ref = stack.pop();
    if (!!ref) {
        this.ip += this.read16signed() - 1;
    }
}

Frame.prototype.putfield = function(stack, locals) {
    var idx = this.read16();
    var fieldName = this.cp[this.cp[this.cp[idx].name_and_type_index].name_index].bytes;
    var val = stack.pop();
    var obj = stack.pop();
    if (!obj) {
        this.newException("java/lang/NullPointerException");
        return;
    }
    obj[fieldName] = val;
}

Frame.prototype.getfield = function(stack, locals) {
    var cp = this.cp;
    var nameAndType = cp[cp[this.read16()].name_and_type_index];
    var fieldName = cp[nameAndType.name_index].bytes;
    var obj = stack.pop();
    if (!obj) {
        this.newException("java/lang/NullPointerException");
        return;
    }
    var value = obj[fieldName];
    if (typeof value === "undefined") {
        value = util.defaultValue(cp[nameAndType.signature_index].bytes);
    }
    stack.push(value);
}


Frame.prototype.new = function(stack, locals) {
    var idx = this.read16();
    var className = this.cp[this.cp[idx].name_index].bytes;
    stack.push(CLASSES.newObject(className));
}

Frame.prototype.getstatic = function(stack, locals) {
    var idx = this.read16();
    var className = this.cp[this.cp[this.cp[idx].class_index].name_index].bytes;
    var fieldName = this.cp[this.cp[this.cp[idx].name_and_type_index].name_index].bytes;
    stack.push(CLASSES.getStaticField(className, fieldName));
}

Frame.prototype.putstatic = function(stack, locals) {
    var idx = this.read16();
    var className = this.cp[this.cp[this.cp[idx].class_index].name_index].bytes;
    var fieldName = this.cp[this.cp[this.cp[idx].name_and_type_index].name_index].bytes;
    CLASSES.setStaticField(className, fieldName, stack.pop());
}

Frame.prototype.invoke = function(stack, method, signature) {
    var result;
    if (!(method instanceof MethodInfo)) {
        signature = Signature.parse(signature);
        var args = stack.popArgs(signature.IN);
        var instance = null;
        if (!ACCESS_FLAGS.isStatic(method.access_flags))
            instance = stack.pop();
        result = method.apply(instance, args);
        var OUT = Signature.parse(signature).OUT;
        if (OUT.length)
            stack.pushType(OUT[0], result);
    } else {
        new Frame(method).run(stack);
    }
}

Frame.prototype.invokestatic = function(stack, locals) {
    var idx = this.read16();

    var className = this.cp[this.cp[this.cp[idx].class_index].name_index].bytes;
    var methodName = this.cp[this.cp[this.cp[idx].name_and_type_index].name_index].bytes;
    var signature = this.cp[this.cp[this.cp[idx].name_and_type_index].signature_index].bytes;

    var method = CLASSES.getStaticMethod(className, methodName, signature);

    this.invoke(stack, method, signature);
}

Frame.prototype.invokevirtual = function(stack, locals) {
    var self = this;

    var idx = this.read16();

    var className = this.cp[this.cp[this.cp[idx].class_index].name_index].bytes;
    var methodName = this.cp[this.cp[this.cp[idx].name_and_type_index].name_index].bytes;
    var signature = this.cp[this.cp[this.cp[idx].name_and_type_index].signature_index].bytes;

    var method = CLASSES.getMethod(className, methodName, signature);

    this.invoke(stack, method, signature);
}

Frame.prototype.invokespecial = function(stack, locals) {
    var self = this;

    var idx = this.read16();

    var className = this.cp[this.cp[this.cp[idx].class_index].name_index].bytes;
    var methodName = this.cp[this.cp[this.cp[idx].name_and_type_index].name_index].bytes;
    var signature = this.cp[this.cp[this.cp[idx].name_and_type_index].signature_index].bytes;

    var method = CLASSES.getMethod(className, methodName, signature);

    this.invoke(stack, method, signature);
}

Frame.prototype.invokeinterface = function(stack, locals) {
    var self = this;

    var idx = this.read16();
    var argsNumber = this.read8();
    var zero = this.read8();

    var className = this.cp[this.cp[this.cp[idx].class_index].name_index].bytes;
    var methodName = this.cp[this.cp[this.cp[idx].name_and_type_index].name_index].bytes;
    var signature = this.cp[this.cp[this.cp[idx].name_and_type_index].signature_index].bytes;

    var method = stack.top()[methodName];

    this.invoke(stack, method, signature);
}

Frame.prototype.jsr = function(stack, locals) {
    var jmp = this.read16();
    stack.push(this.ip);
    this.ip = jmp;
}

Frame.prototype.jsr_w = function(stack, locals) {
    var jmp = this.read32();
    stack.push(this.ip);
    this.ip = jmp;
}

Frame.prototype.ret = function(stack, locals) {
    var idx = this.isWide() ? this.read16() : this.read8();
    this.ip = locals.get(idx);
}

Frame.prototype.tableswitch = function(stack, locals) {
    var startip = this.ip;
    var jmp;

    while ((this.ip % 4) != 0) {
        this.ip++;
    }

    var def = this.read32();
    var low = this.read32();
    var high = this.read32();
    var val = stack.pop();

    if (val < low || val > high) {
        jmp = def;
    } else {
        this.ip  += (val - low) << 2;
        jmp = this.read32();
    }

    this.ip = startip - 1 + this.u32_to_s32(jmp);
}

Frame.prototype.lookupswitch = function(stack, locals) {
    var startip = this.ip;

    while ((this.ip % 4) != 0) {
        this.ip++;
    }

    var jmp = this.read32();
    var size = this.read32();
    var val = stack.pop();

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

Frame.prototype.instanceof = function(stack, locals) {
    var idx = this.read16();
    var className = this.cp[this.cp[idx].name_index].bytes;
    var obj = stack.pop();
    stack.push(obj.class.getClassName() === className);
}

Frame.prototype.checkcast = function(stack, locals) {
    var idx = this.read16();
    var type = this.cp[this.cp[idx].name_index].bytes;
}

Frame.prototype.athrow = function(stack, locals) {
    this.throw(stack.pop());
}

Frame.prototype.wide = function(stack, locals) {
}

Frame.prototype.monitorenter = function(stack, locals) {
    var obj = stack.pop();
    if (!obj) {
        this.newException("java/lang/NullPointerException");
        return;
    }
    if (obj.hasOwnProperty("$lock$")) {
        stack.push(obj);
        this.ip--;
        // SCHEDULER.yield();
    } else {
        obj["$lock$"] = "locked";
    }
}

Frame.prototype.monitorexit = function(stack, locals) {
    var obj = stack.pop();
    if (!obj) {
        this.newException("java/lang/NullPointerException");
        return;
    }
    delete obj["$lock$"];
    // SCHEDULER.yield();
}
