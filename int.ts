module J2ME {

  import assert = Debug.assert;
  import Bytecodes = Bytecode.Bytecodes;
  import toHEX = IntegerUtilities.toHEX;

  function toName(o) {
    if (o instanceof MethodInfo) {
      return "MI: " + o.implKey;
    }
    function getArrayInfo(o) {
      var s = [];
      var x = [];
      for (var i = 0; i < Math.min(o.length, 8); i++) {
        s.push(o[i]);
        x.push(String.fromCharCode(o[i]));
      }
      var suffix = (o.length > 8 ? "..." : "");
      return fromUTF8(o.klass.classInfo.utf8Name) +
        ", length: " + o.length +
        ", values: [" + s.join(", ") + suffix + "]" +
        ", chars: \"" + x.join("") + suffix + "\"";
    }
    function getObjectInfo(o) {
      if (o.length !== undefined) {
        return getArrayInfo(o);
      }
      return fromUTF8(o.klass.classInfo.utf8Name) + (o._address ? " " + toHEX(o._address) : "");
    }
    if (o && !o.klass) {
      return o;
    }
    if (o && o.klass === Klasses.java.lang.Class) {
      return "[" + getObjectInfo(o) + "] " + o.runtimeKlass.templateKlass.classInfo.getClassNameSlow();
    }
    if (o && o.klass === Klasses.java.lang.String) {
      return "[" + getObjectInfo(o) + "] \"" + fromJavaString(o) + "\"";
    }
    return o ? ("[" + getObjectInfo(o) + "]") : "null";
  }

  /**
   * The number of opcodes executed thus far.
   */
  export var bytecodeCount = 0;

  /**
   * The number of times the interpreter method was called thus far.
   */
  export var interpreterCount = 0;

  export var onStackReplacementCount = 0;

  /**
   * The closest floating-point representation to this long value.
   */
  export function longToNumber(l: number, h: number): number {
    return h * Constants.TWO_PWR_32_DBL + ((l >= 0) ? l : Constants.TWO_PWR_32_DBL + l);
  }

  export function numberToLong(v: number): number {
    // TODO Extract logic from Long so we don't allocate here.
    var long = Long.fromNumber(v);
    tempReturn0 = long.high_;
    return long.low_;
  }

  function wordsToDouble(l: number, h: number): number {
    aliasedI32[0] = l;
    aliasedI32[1] = h;
    return aliasedF64[0];
  }

  /**
   * Calling Convention:
   *
   * Interpreter -> Interpreter:
   *   This follows the JVM bytecode calling convention. This interpreter is highly
   *   optimized for this calling convention.
   *
   * Compiled / Native -> Compiled / Native:
   *   64-bit floats and single word values can be encoded using only one JS value. However, 64-bit longs cannot and
   *   require a (low, high) JS value pair. For example, the signature: "foo.(IDJi)J" is expressed as:
   *
   *   function foo(i, d, lowBits, highBits, i) {
   *     return tempReturn0 = highBits, lowBits;
   *   }
   *
   *   Returning longs is equally problamatic, the convention is to return the lowBits, and save the highBits in a
   *   global |tempReturn0| variable.
   */

  /*
   * Stack Frame Layout:
   *
   *   LP  --->  +--------------------------------+
   *             | Parameter 0                    |
   *             +--------------------------------+
   *             |              ...               |
   *             +--------------------------------+
   *             | Parameter (P-1)                |
   *             +--------------------------------+
   *             | Non-Parameter Local 0          |
   *             +--------------------------------+
   *             |              ...               |
   *             +--------------------------------+
   *             | Non-Parameter Local (L-1)      |
   *   FP  --->  +--------------------------------+
   *             | Caller Return Address          | // The opPC of the caller's invoke bytecode.
   *             +--------------------------------+
   *             | Caller FP                      |
   *             +--------------------------------+
   *             | Callee Method Info             |
   *             +--------------------------------+
   *             | Monitor                        |
   *             +--------------------------------+
   *             | Stack slot 0                   |
   *             +--------------------------------+
   *             |              ...               |
   *             +--------------------------------+
   *             | Stack slot (S-1)               |
   *   SP  --->  +--------------------------------+
   */

  enum FrameLayout {
    CalleeMethodInfoOffset      = 2,
    CallerFPOffset              = 1,
    CallerRAOffset              = 0,
    MonitorOffset               = 3,
    CallerSaveSize              = 4
  }

  export class FrameView {
    public fp: number;
    public sp: number;
    public pc: number;
    constructor() {

    }

    set(fp: number, sp: number, pc: number) {
      this.fp = fp;
      this.sp = sp;
      this.pc = pc;

      if (!release) {
        var callee = ref[this.fp + FrameLayout.CalleeMethodInfoOffset];
        assert(
          callee === null ||
          callee instanceof MethodInfo,
          "Callee @" + (this.fp + FrameLayout.CalleeMethodInfoOffset) + " is not a MethodInfo, " + toName(callee)
        );
      }
    }

    setParameter(kind: Kind, i: number, v: any) {
      switch (kind) {
        case Kind.Reference:
          ref[this.fp + this.parameterOffset + i] = v;
          break;
        case Kind.Int:
          i32[this.fp + this.parameterOffset + i] = v;
          break;
        default:
          release || assert(false, "Cannot set parameter of kind: " + Kind[kind]);
      }
    }

    get methodInfo(): MethodInfo {
      return ref[this.fp + FrameLayout.CalleeMethodInfoOffset];
    }

    set methodInfo(methodInfo: MethodInfo) {
      ref[this.fp + FrameLayout.CalleeMethodInfoOffset] = methodInfo;
    }

    set monitor(object: java.lang.Object) {
      ref[this.fp + FrameLayout.MonitorOffset] = object;
    }

    get monitor(): java.lang.Object {
      return ref[this.fp + FrameLayout.MonitorOffset];
    }

    get parameterOffset() {
      return this.methodInfo ? -this.methodInfo.codeAttribute.max_locals : 0;
    }

    get stackOffset(): number {
      return FrameLayout.CallerSaveSize;
    }

    traceStack(writer: IndentingWriter) {
      var fp = this.fp;
      var sp = this.sp;
      var pc = this.pc;
      while (this.fp > FrameLayout.CallerSaveSize) {
        writer.writeLn((this.methodInfo ? this.methodInfo.implKey : "null") + ", FP: " + this.fp + ", SP: " + this.sp + ", PC: " + this.pc);
        this.set(i32[this.fp + FrameLayout.CallerFPOffset],
                 this.fp + this.parameterOffset,
                 i32[this.fp + FrameLayout.CallerRAOffset]);

      }
      this.fp = fp;
      this.sp = sp;
      this.pc = pc;
    }

    trace(writer: IndentingWriter) {
      function toNumber(v) {
        if (v < 0) {
          return String(v);
        } else if (v === 0) {
          return " 0";
        } else {
          return "+" + v;
        }
      }

      function clampString(v, n) {
        if (v.length > n) {
          return v.substring(0, n - 3) + "...";
        }
        return v;
      }

      writer.writeLn("Frame: " + this.methodInfo.implKey + ", FP: " + this.fp + ", SP: " + this.sp + ", PC: " + this.pc);
      for (var i = Math.max(0, this.fp + this.parameterOffset); i < this.sp; i++) {
        var prefix = "    ";
        if (i >= this.fp + this.stackOffset) {
          prefix = "S" + (i - (this.fp + this.stackOffset)) + ": ";
        } else if (i === this.fp + FrameLayout.CalleeMethodInfoOffset) {
          prefix = "MI: ";
        } else if (i === this.fp + FrameLayout.CallerFPOffset) {
          prefix = "CF: ";
        } else if (i === this.fp + FrameLayout.CallerRAOffset) {
          prefix = "RA: ";
        } else if (i >= this.fp + this.parameterOffset) {
          prefix = "L" + (i - (this.fp + this.parameterOffset)) + ": ";
        }
        writer.writeLn(" " + prefix.padRight(' ', 5) + " " + toNumber(i - this.fp).padLeft(' ', 3) + " " + String(i).padLeft(' ', 4) + " " + toHEX(i << 2)  + ": " +
          String(i32[i]).padLeft(' ', 12) + " " +
          toHEX(i32[i]) + " " +
          ((i32[i] >= 32 && i32[i] < 1024) ? String.fromCharCode(i32[i]) : "?") + " " +
          clampString(String(f32[i]), 12).padLeft(' ', 12) + " " +
          clampString(String(wordsToDouble(i32[i], i32[i + 1])), 12).padLeft(' ', 12) + " " +
          toName(ref[i]));
      }
    }
  }

  export var interpreterCounter = new Metrics.Counter(true);

  export class Thread {

    /**
     * Thread base pointer.
     */
    tp: number;

    /**
     * Stack base pointer.
     */
    bp: number

    /**
     * Current frame pointer.
     */
    fp: number

    /**
     * Current stack pointer.
     */
    sp: number

    /**
     * Current program counter.
     */
    pc: number

    /**
     * Context associated with this thread.
     */
    ctx: Context;

    view: FrameView;

    constructor(ctx: Context) {
      this.tp = ASM._gcMalloc(1024 * 128);
      this.bp = this.tp;
      this.fp = this.bp;
      this.sp = this.fp;
      this.pc = -1;
      this.view = new FrameView();
      this.ctx = ctx;
      release || threadWriter && threadWriter.writeLn("creatingThread: tp: " + toHEX(this.tp << 2) + " " + toHEX(i32.byteLength));
    }

    set(fp: number, sp: number, pc: number) {
      this.fp = fp;
      this.sp = sp;
      this.pc = pc;
    }

    hashFrame(): number {
      var fp = this.fp << 2;
      var sp = this.sp << 2;
      return HashUtilities.hashBytesTo32BitsAdler(u8, fp, sp);
    }

    /**
     * Advances the |pc| to the next |pc| after the current invoke bytecode.
     */
    advancePastInvokeBytecode() {
      var mi = ref[this.fp + FrameLayout.CalleeMethodInfoOffset];
      var code = mi.codeAttribute.code;
      var op = code[this.pc];
      release || assert(Bytecode.isInvoke(op), "The PC should be at an invoke bytecode.");
      this.pc += (op === Bytecodes.INVOKEINTERFACE ? 5 : 3);
    }

    get frame(): FrameView {
      this.view.set(this.fp, this.sp, this.pc);
      return this.view;
    }

    pushFrame(methodInfo: MethodInfo) {
      var fp = this.fp;
      if (methodInfo) {
        this.fp = this.sp + methodInfo.codeAttribute.max_locals;
      } else {
        this.fp = this.sp;
      }
      i32[this.fp + FrameLayout.CallerRAOffset] = this.pc;    // Caller RA
      i32[this.fp + FrameLayout.CallerFPOffset] = fp;         // Caller FP
      ref[this.fp + FrameLayout.CalleeMethodInfoOffset] = methodInfo; // Callee
      ref[this.fp + FrameLayout.MonitorOffset] = null; // Monitor
      this.sp = this.fp + FrameLayout.CallerSaveSize;
      this.pc = 0;
    }

    popFrame(methodInfo: MethodInfo): MethodInfo {
      var mi = ref[this.fp + FrameLayout.CalleeMethodInfoOffset];
      release || assert(mi === methodInfo);
      this.pc = i32[this.fp + FrameLayout.CallerRAOffset];
      var maxLocals = mi ? mi.codeAttribute.max_locals : 0;
      this.sp = this.fp - maxLocals;
      this.fp = i32[this.fp + FrameLayout.CallerFPOffset];
      return ref[this.fp + FrameLayout.CalleeMethodInfoOffset];
    }

    run() {
      return interpret(this);
    }

    exceptionUnwind(e: java.lang.Exception) {
      release || traceWriter && traceWriter.writeLn("exceptionUnwind: " + toName(e));
      var pc = -1;
      var classInfo;
      var mi = ref[this.fp + FrameLayout.CalleeMethodInfoOffset];
      while (mi) {
        release || traceWriter && traceWriter.writeLn("Looking for handler in: " + mi.implKey);
        for (var i = 0; i < mi.exception_table_length; i++) {
          var exceptionEntryView = mi.getExceptionEntryViewByIndex(i);
          release || traceWriter && traceWriter.writeLn("Checking catch range: " + exceptionEntryView.start_pc + " - " + exceptionEntryView.end_pc);
          if (this.pc >= exceptionEntryView.start_pc && this.pc < exceptionEntryView.end_pc) {
            if (exceptionEntryView.catch_type === 0) {
              pc = exceptionEntryView.handler_pc;
              break;
            } else {
              classInfo = resolveClass(exceptionEntryView.catch_type, mi.classInfo);
              release || traceWriter && traceWriter.writeLn("Checking catch type: " + classInfo.klass);
              if (isAssignableTo(e.klass, classInfo.klass)) {
                pc = exceptionEntryView.handler_pc;
                break;
              }
            }
          }
        }
        if (pc >= 0) {
          this.pc = pc;
          this.sp = this.fp + FrameLayout.CallerSaveSize;
          ref[this.sp++] = e;
          return;
        }
        if (mi.isSynchronized) {
          this.ctx.monitorExit(ref[this.fp + FrameLayout.MonitorOffset]);
        }
        mi = this.popFrame(mi);
        release || traceWriter && traceWriter.outdent();
        release || traceWriter && traceWriter.writeLn("<< I Unwind");
      }
      release || traceWriter && traceWriter.writeLn("Cannot catch: " + toName(e));
      throw e;
    }

    classInitAndUnwindCheck(fp: number, sp: number, pc: number, classInfo: ClassInfo) {
      this.set(fp, sp, pc);
      classInitCheck(classInfo);
    }

    throwException(fp: number, sp: number, pc: number, type: ExceptionType, a?) {
      this.set(fp, sp, pc);
      switch (type) {
        case ExceptionType.ArrayIndexOutOfBoundsException:
          throwArrayIndexOutOfBoundsException(a);
          break;
        case ExceptionType.ArithmeticException:
          throwArithmeticException();
          break;
        case ExceptionType.NegativeArraySizeException:
          throwNegativeArraySizeException();
          break;
        case ExceptionType.NullPointerException:
          throwNullPointerException();
          break;
      }
    }
  }

  export function prepareInterpretedMethod(methodInfo: MethodInfo): Function {
    var method = function fastInterpreterFrameAdapter() {
      var thread = $.ctx.nativeThread;
      var callerFP = thread.fp;
      var callerPC = thread.pc;
      // release || traceWriter && traceWriter.writeLn(">> I");
      thread.pushFrame(null);
      thread.pushFrame(methodInfo);
      var calleeFP = thread.fp;
      var frame = thread.frame;
      var kinds = methodInfo.signatureKinds;
      var index = 0;
      if (!methodInfo.isStatic) {
        frame.setParameter(Kind.Reference, index++, this);
      }
      for (var i = 1; i < kinds.length; i++) {
        frame.setParameter(kinds[i], index++, arguments[i - 1]);
      }
      if (methodInfo.isSynchronized) {
        var monitor = methodInfo.isStatic
          ? methodInfo.classInfo.getClassObject()
          : this;
        frame.monitor = monitor;
        $.ctx.monitorEnter(monitor);
        release || assert(U !== VMState.Yielding, "Monitors should never yield.");
        if (U === VMState.Pausing || U === VMState.Stopping) {
          return;
        }
      }
      var v = interpret(thread);
      if (U) {
        release || assert(v === undefined, "Return value must be undefined.");
        // Splice out the marker frame so the interpreter doesn't return early when execution is resumed.
        i32[calleeFP + FrameLayout.CallerFPOffset] = callerFP;
        i32[calleeFP + FrameLayout.CallerRAOffset] = callerPC;
        return;
      }
      release || assert(callerFP === thread.fp);
      // release || traceWriter && traceWriter.writeLn("<< I");
      return v;
    };
    (<any>method).methodInfo = methodInfo;
    return method;
  }

  function resolveClass(index: number, classInfo: ClassInfo): ClassInfo {
    var classInfo = classInfo.constantPool.resolveClass(index);
    linkKlass(classInfo);
    return classInfo;
  }

  var args = new Array(16);

  export enum ExceptionType {
    ArithmeticException,
    ArrayIndexOutOfBoundsException,
    NegativeArraySizeException,
    NullPointerException
  }

  /**
   * Debugging helper to make sure native methods were implemented correctly.
   */
  function checkReturnValue(methodInfo: MethodInfo, l: any, h: number) {
    if (U) {
      if (typeof l !== "undefined") {
        assert(false, "Expected undefined return value during unwind, got " + l + " in " + methodInfo.implKey);
      }
      return;
    }
    if (!(getKindCheck(methodInfo.returnKind)(l, h))) {
      assert(false, "Expected " + Kind[methodInfo.returnKind] + " return value, got " + l + " in " + methodInfo.implKey);
    }
  }
  
  /**
   * Main interpreter loop. This method is carefully written to avoid memory allocation and
   * function calls on fast paths. Therefore, everything is inlined, even if it makes the code
   * look ugly.
   *
   * The interpreter loop caches the thread state in local variables. Doing so avoids a lot of
   * property accesses but also makes the code brittle since you need to manually sync up the
   * thread state with the local thead state at precise points.
   *
   * At call sites, caller frame |pc|s are always at the beggining of invoke bytecodes. In the
   * interpreter return bytecodes advance the pc past the invoke bytecode. Native code that
   * unwinds and resumes execution at a later point needs to adjust the pc accordingly.
   *
   * Bytecodes that construct exception objects must save the tread state before executing any
   * code that may overwrite the frame. Use the |throwException| helper method to ensure that
   * the thread state is property saved.
   */
  export function interpret(thread: Thread) {
    var frame = thread.frame;

    var mi = frame.methodInfo;
    var maxLocals = mi.codeAttribute.max_locals;
    var ci = mi.classInfo;
    var cp = ci.constantPool;

    var code = mi ? mi.codeAttribute.code : null;

    var fp = thread.fp;
    var lp = fp - maxLocals;
    var sp = thread.sp;
    var opPC = 0, pc = thread.pc;

    var tag: TAGS;
    var type, size;
    var value, index, array, object, returnValue, constant, offset, buffer, tag: TAGS, targetPC, kind;
    var address = 0, isStatic = false;
    var ia = 0, ib = 0; // Integer Operands
    var ll = 0, lh = 0; // Long Low / High
    var fa = 0, fb = 0; // Float / Double Operands

    var classInfo: ClassInfo;
    var fieldInfo: FieldInfo;

    var monitor: java.lang.Object;

    // HEAD

    while (true) {
      opPC = pc, op = code[pc++];

      if (!release) {
        assert(code === mi.codeAttribute.code, "Bad Code.");
        assert(ci === mi.classInfo, "Bad Class Info.");
        assert(cp === ci.constantPool, "Bad Constant Pool.");
        assert(lp === fp - mi.codeAttribute.max_locals, "Bad lp.");
        bytecodeCount++;

        if (traceStackWriter) {
          frame.set(fp, sp, opPC); frame.trace(traceStackWriter);
          traceStackWriter.writeLn();
          traceStackWriter.greenLn(mi.implKey + ": PC: " + opPC + ", FP: " + fp + ", " + Bytecodes[op]);
        }
      }

      try {
        switch (op) {
          case Bytecodes.NOP:
            continue;
          case Bytecodes.ACONST_NULL:
            ref[sp++] = null;
            continue;
          case Bytecodes.ICONST_M1:
          case Bytecodes.ICONST_0:
          case Bytecodes.ICONST_1:
          case Bytecodes.ICONST_2:
          case Bytecodes.ICONST_3:
          case Bytecodes.ICONST_4:
          case Bytecodes.ICONST_5:
            i32[sp++] = op - Bytecodes.ICONST_0;
            continue;
          case Bytecodes.FCONST_0:
          case Bytecodes.FCONST_1:
          case Bytecodes.FCONST_2:
            f32[sp++] = op - Bytecodes.FCONST_0;
            continue;
          case Bytecodes.DCONST_0:
            i32[sp++] = 0;
            i32[sp++] = 0;
            continue;
          case Bytecodes.DCONST_1:
            i32[sp++] = 0;
            i32[sp++] = 1072693248;
            continue;
          case Bytecodes.LCONST_0:
          case Bytecodes.LCONST_1:
            i32[sp++] = op - Bytecodes.LCONST_0;
            i32[sp++] = 0;
            continue;
          case Bytecodes.BIPUSH:
            i32[sp++] = code[pc++] << 24 >> 24;
            continue;
          case Bytecodes.SIPUSH:
            i32[sp++] = (code[pc++] << 8 | code[pc++]) << 16 >> 16;
            continue;
          case Bytecodes.LDC:
          case Bytecodes.LDC_W:
            index = (op === Bytecodes.LDC) ? code[pc++] : code[pc++] << 8 | code[pc++];
            offset = cp.entries[index];
            buffer = cp.buffer;
            tag = buffer[offset++];
            if (tag === TAGS.CONSTANT_Integer || tag === TAGS.CONSTANT_Float) {
              i32[sp++] = buffer[offset++] << 24 | buffer[offset++] << 16 | buffer[offset++] << 8 | buffer[offset++];
            } else if (tag === TAGS.CONSTANT_String) {
              ref[sp++] = constant = ci.constantPool.resolve(index, tag, false);;
            } else {
              release || assert(false, TAGS[tag]);
            }
            continue;
          case Bytecodes.LDC2_W:
            index = code[pc++] << 8 | code[pc++];
            offset = cp.entries[index];
            buffer = cp.buffer;
            tag = buffer[offset++];
            if (tag === TAGS.CONSTANT_Long || tag === TAGS.CONSTANT_Double) {
              i32[sp + 1] = buffer[offset++] << 24 | buffer[offset++] << 16 | buffer[offset++] << 8 | buffer[offset++];
              i32[sp    ] = buffer[offset++] << 24 | buffer[offset++] << 16 | buffer[offset++] << 8 | buffer[offset++];
              sp += 2;
            } else {
              release || assert(false, TAGS[tag]);
            }
            continue;
          case Bytecodes.ILOAD:
          case Bytecodes.FLOAD:
            i32[sp++] = i32[lp + code[pc++]];
            continue;
          case Bytecodes.ALOAD:
            ref[sp++] = ref[lp + code[pc++]];
            continue;
          case Bytecodes.LLOAD:
          case Bytecodes.DLOAD:
            offset = lp + code[pc++];
            i32[sp++] = i32[offset];
            i32[sp++] = i32[offset + 1];
            continue;
          case Bytecodes.ILOAD_0:
          case Bytecodes.ILOAD_1:
          case Bytecodes.ILOAD_2:
          case Bytecodes.ILOAD_3:
            i32[sp++] = i32[lp + op - Bytecodes.ILOAD_0];
            continue;
          case Bytecodes.FLOAD_0:
          case Bytecodes.FLOAD_1:
          case Bytecodes.FLOAD_2:
          case Bytecodes.FLOAD_3:
            i32[sp++] = i32[lp + op - Bytecodes.FLOAD_0];
            continue;
          case Bytecodes.ALOAD_0:
            ref[sp++] = ref[lp];
            continue;
          case Bytecodes.ALOAD_1:
          case Bytecodes.ALOAD_2:
          case Bytecodes.ALOAD_3:
            ref[sp++] = ref[lp + op - Bytecodes.ALOAD_0];
            continue;
          case Bytecodes.LLOAD_0:
          case Bytecodes.LLOAD_1:
          case Bytecodes.LLOAD_2:
          case Bytecodes.LLOAD_3:
            offset = lp + op - Bytecodes.LLOAD_0;
            i32[sp++] = i32[offset];
            i32[sp++] = i32[offset + 1];
            continue;
          case Bytecodes.DLOAD_0:
          case Bytecodes.DLOAD_1:
          case Bytecodes.DLOAD_2:
          case Bytecodes.DLOAD_3:
            offset = lp + op - Bytecodes.DLOAD_0;
            i32[sp++] = i32[offset];
            i32[sp++] = i32[offset + 1];
            continue;
          case Bytecodes.IALOAD:
            index = i32[--sp];
            array = ref[--sp];
            if ((index >>> 0) >= (array.length >>> 0)) {
              thread.throwException(fp, sp, opPC, ExceptionType.ArrayIndexOutOfBoundsException, index);
            }
            i32[sp++] = array[index];
            continue;
          case Bytecodes.BALOAD:
            index = i32[--sp];
            array = ref[--sp];
            if ((index >>> 0) >= (array.length >>> 0)) {
              thread.throwException(fp, sp, opPC, ExceptionType.ArrayIndexOutOfBoundsException, index);
            }
            i32[sp++] = array[index];
            continue;
          case Bytecodes.CALOAD:
            index = i32[--sp];
            array = ref[--sp];
            if ((index >>> 0) >= (array.length >>> 0)) {
              thread.throwException(fp, sp, opPC, ExceptionType.ArrayIndexOutOfBoundsException, index);
            }
            i32[sp++] = array[index];
            continue;
          case Bytecodes.SALOAD:
            index = i32[--sp];
            array = ref[--sp];
            if ((index >>> 0) >= (array.length >>> 0)) {
              thread.throwException(fp, sp, opPC, ExceptionType.ArrayIndexOutOfBoundsException, index);
            }
            i32[sp++] = array[index];
            continue;
          case Bytecodes.FALOAD:
            index = i32[--sp];
            array = ref[--sp];
            if ((index >>> 0) >= (array.length >>> 0)) {
              thread.throwException(fp, sp, opPC, ExceptionType.ArrayIndexOutOfBoundsException, index);
            }
            f32[sp++] = array[index];
            continue;
          case Bytecodes.AALOAD:
            index = i32[--sp];
            array = ref[--sp];
            if ((index >>> 0) >= (array.length >>> 0)) {
              thread.throwException(fp, sp, opPC, ExceptionType.ArrayIndexOutOfBoundsException, index);
            }
            ref[sp++] = array[index];
            continue;
          case Bytecodes.DALOAD:
            index = i32[--sp];
            array = ref[--sp];
            if ((index >>> 0) >= (array.length >>> 0)) {
              thread.throwException(fp, sp, opPC, ExceptionType.ArrayIndexOutOfBoundsException, index);
            }
            aliasedF64[0] = array[index];
            i32[sp++] = aliasedI32[0];
            i32[sp++] = aliasedI32[1];
            continue;
          case Bytecodes.ISTORE:
          case Bytecodes.FSTORE:
            i32[lp + code[pc++]] = i32[--sp];
            continue;
          case Bytecodes.ASTORE:
            ref[lp + code[pc++]] = ref[--sp];
            continue;
          case Bytecodes.LSTORE:
          case Bytecodes.DSTORE:
            offset = lp + code[pc++];
            i32[offset + 1] = i32[--sp];
            i32[offset]     = i32[--sp];
            continue;
          case Bytecodes.ISTORE_0:
          case Bytecodes.ISTORE_1:
          case Bytecodes.ISTORE_2:
          case Bytecodes.ISTORE_3:
            i32[lp + op - Bytecodes.ISTORE_0] = i32[--sp];
            continue;
          case Bytecodes.FSTORE_0:
          case Bytecodes.FSTORE_1:
          case Bytecodes.FSTORE_2:
          case Bytecodes.FSTORE_3:
            i32[lp + op - Bytecodes.FSTORE_0] = i32[--sp];
            continue;
          case Bytecodes.ASTORE_0:
          case Bytecodes.ASTORE_1:
          case Bytecodes.ASTORE_2:
          case Bytecodes.ASTORE_3:
            ref[lp + op - Bytecodes.ASTORE_0] = ref[--sp];
            continue;
          case Bytecodes.LSTORE_0:
          case Bytecodes.LSTORE_1:
          case Bytecodes.LSTORE_2:
          case Bytecodes.LSTORE_3:
            offset = lp + op - Bytecodes.LSTORE_0;
            i32[offset + 1] = i32[--sp];
            i32[offset]     = i32[--sp];
            continue;
          case Bytecodes.DSTORE_0:
          case Bytecodes.DSTORE_1:
          case Bytecodes.DSTORE_2:
          case Bytecodes.DSTORE_3:
            offset = lp + op - Bytecodes.DSTORE_0;
            i32[offset + 1] = i32[--sp];
            i32[offset]     = i32[--sp];
            continue;
          case Bytecodes.IASTORE:
            value = i32[--sp];
            index = i32[--sp];
            array = ref[--sp];
            if ((index >>> 0) >= (array.length >>> 0)) {
              thread.throwException(fp, sp, opPC, ExceptionType.ArrayIndexOutOfBoundsException, index);
            }
            array[index] = value;
            continue;
          case Bytecodes.FASTORE:
            value = f32[--sp];
            index = i32[--sp];
            array = ref[--sp];
            if ((index >>> 0) >= (array.length >>> 0)) {
              thread.throwException(fp, sp, opPC, ExceptionType.ArrayIndexOutOfBoundsException, index);
            }
            array[index] = value;
            continue;
          case Bytecodes.BASTORE:
            value = i32[--sp];
            index = i32[--sp];
            array = ref[--sp];
            if ((index >>> 0) >= (array.length >>> 0)) {
              thread.throwException(fp, sp, opPC, ExceptionType.ArrayIndexOutOfBoundsException, index);
            }
            array[index] = value;
            continue;
          case Bytecodes.CASTORE:
            value = i32[--sp];
            index = i32[--sp];
            array = ref[--sp];
            if ((index >>> 0) >= (array.length >>> 0)) {
              thread.throwException(fp, sp, opPC, ExceptionType.ArrayIndexOutOfBoundsException, index);
            }
            array[index] = value;
            continue;
          case Bytecodes.SASTORE:
            value = i32[--sp];
            index = i32[--sp];
            array = ref[--sp];
            if ((index >>> 0) >= (array.length >>> 0)) {
              thread.throwException(fp, sp, opPC, ExceptionType.ArrayIndexOutOfBoundsException, index);
            }
            array[index] = value;
            continue;
          case Bytecodes.LASTORE:
            lh = i32[--sp];
            ll = i32[--sp];
            index = i32[--sp];
            array = ref[--sp];
            if ((index >>> 0) >= (array.length >>> 0)) {
              thread.throwException(fp, sp, opPC, ExceptionType.ArrayIndexOutOfBoundsException, index);
            }
            array.value[index * 2    ] = ll;
            array.value[index * 2 + 1] = lh;
            continue;
          case Bytecodes.LALOAD:
            index = i32[--sp];
            array = ref[--sp];
            if ((index >>> 0) >= (array.length >>> 0)) {
              thread.throwException(fp, sp, opPC, ExceptionType.ArrayIndexOutOfBoundsException, index);
            }
            i32[sp++] = array.value[index * 2    ];
            i32[sp++] = array.value[index * 2 + 1];
            continue;
          case Bytecodes.DASTORE:
            aliasedI32[1] = i32[--sp];
            aliasedI32[0] = i32[--sp];
            value = aliasedF64[0];
            index = i32[--sp];
            array = ref[--sp];
            if ((index >>> 0) >= (array.length >>> 0)) {
              thread.throwException(fp, sp, opPC, ExceptionType.ArrayIndexOutOfBoundsException, index);
            }
            array[index] = value;
            continue;
          case Bytecodes.AASTORE:
            value = ref[--sp];
            index = i32[--sp];
            array = ref[--sp];
            if ((index >>> 0) >= (array.length >>> 0)) {
              thread.throwException(fp, sp, opPC, ExceptionType.ArrayIndexOutOfBoundsException, index);
            }
            checkArrayStore(array, value);
            array[index] = value;
            continue;
          case Bytecodes.POP:
            --sp;
            continue;
          case Bytecodes.POP2:
            sp -= 2;
            continue;
          case Bytecodes.DUP: // ... a -> ... a, a
            i32[sp] = i32[sp - 1];          ref[sp] = ref[sp - 1];
            sp++;
            continue;
          case Bytecodes.DUP2: // ... b, a -> ... b, a, b, a
            i32[sp    ] = i32[sp - 2];      ref[sp    ] = ref[sp - 2]; // b
            i32[sp + 1] = i32[sp - 1];      ref[sp + 1] = ref[sp - 1]; // a
            sp += 2;
            continue;
          case Bytecodes.DUP_X1: // ... b, a -> ... a, b, a
            i32[sp    ] = i32[sp - 1];      ref[sp    ] = ref[sp - 1]; // a
            i32[sp - 1] = i32[sp - 2];      ref[sp - 1] = ref[sp - 2]; // b
            i32[sp - 2] = i32[sp];          ref[sp - 2] = ref[sp];     // a
            sp++;
            continue;
          case Bytecodes.DUP_X2: // ... c, b, a -> ... a, c, b, a
            i32[sp    ] = i32[sp - 1];      ref[sp    ] = ref[sp - 1]; // a
            i32[sp - 1] = i32[sp - 2];      ref[sp - 1] = ref[sp - 2]; // b
            i32[sp - 2] = i32[sp - 3];      ref[sp - 2] = ref[sp - 3]; // c
            i32[sp - 3] = i32[sp];          ref[sp - 3] = ref[sp];     // a
            sp++;
            continue;
          case Bytecodes.DUP2_X1: // ... c, b, a -> ... b, a, c, b, a
            i32[sp + 1] = i32[sp - 1];      ref[sp + 1] = ref[sp - 1]; // a
            i32[sp    ] = i32[sp - 2];      ref[sp    ] = ref[sp - 2]; // b
            i32[sp - 1] = i32[sp - 3];      ref[sp - 1] = ref[sp - 3]; // c
            i32[sp - 2] = i32[sp + 1];      ref[sp - 2] = ref[sp + 1]; // a
            i32[sp - 3] = i32[sp    ];      ref[sp - 3] = ref[sp    ]; // b
            sp += 2;
            continue;
          case Bytecodes.DUP2_X2: // ... d, c, b, a -> ... b, a, d, c, b, a
            i32[sp + 1] = i32[sp - 1];      ref[sp + 1] = ref[sp - 1]; // a
            i32[sp    ] = i32[sp - 2];      ref[sp    ] = ref[sp - 2]; // b
            i32[sp - 1] = i32[sp - 3];      ref[sp - 1] = ref[sp - 3]; // c
            i32[sp - 2] = i32[sp - 4];      ref[sp - 2] = ref[sp - 4]; // d
            i32[sp - 3] = i32[sp + 1];      ref[sp - 3] = ref[sp + 1]; // a
            i32[sp - 4] = i32[sp    ];      ref[sp - 4] = ref[sp    ]; // b
            sp += 2;
            continue;
          case Bytecodes.SWAP:
            ia = i32[sp - 1];               object = ref[sp - 1];
            i32[sp - 1] = i32[sp - 2];      ref[sp - 1] = ref[sp - 2];
            i32[sp - 2] = ia;               ref[sp - 2] = object;
            continue;
          case Bytecodes.IINC:
            index = code[pc++];
            value = code[pc++] << 24 >> 24;
            i32[lp + index] = i32[lp + index] + value | 0;
            continue;
          case Bytecodes.IADD:
            i32[sp - 2] = (i32[sp - 2] + i32[sp - 1]) | 0; sp--;
            continue;
          case Bytecodes.LADD:
            ASM._lAdd(sp - 4 << 2, sp - 4 << 2, sp - 2 << 2); sp -= 2;
            continue;
          case Bytecodes.FADD:
            f32[sp - 2] = f32[sp - 2] + f32[sp - 1]; sp--;
            continue;
          case Bytecodes.DADD:
            aliasedI32[0] = i32[sp - 4];
            aliasedI32[1] = i32[sp - 3]; ia = aliasedF64[0];
            aliasedI32[0] = i32[sp - 2];
            aliasedI32[1] = i32[sp - 1]; ib = aliasedF64[0];
            aliasedF64[0] = ia + ib;
            i32[sp - 4] = aliasedI32[0];
            i32[sp - 3] = aliasedI32[1];
            sp -= 2;
            continue;
          case Bytecodes.ISUB:
            i32[sp - 2] = (i32[sp - 2] - i32[sp - 1]) | 0; sp--;
            continue;
          case Bytecodes.LSUB:
            ASM._lSub(sp - 4 << 2, sp - 4 << 2, sp - 2 << 2); sp -= 2;
            continue;
          case Bytecodes.FSUB:
            f32[sp - 2] = f32[sp - 2] - f32[sp - 1]; sp--;
            continue;
          case Bytecodes.DSUB:
            aliasedI32[0] = i32[sp - 4];
            aliasedI32[1] = i32[sp - 3]; ia = aliasedF64[0];
            aliasedI32[0] = i32[sp - 2];
            aliasedI32[1] = i32[sp - 1]; ib = aliasedF64[0];
            aliasedF64[0] = ia - ib;
            i32[sp - 4] = aliasedI32[0];
            i32[sp - 3] = aliasedI32[1];
            sp -= 2;
            continue;
          case Bytecodes.IMUL:
            i32[sp - 2] = Math.imul(i32[sp - 2], i32[sp - 1]) | 0; sp--;
            continue;
          case Bytecodes.LMUL:
            ASM._lMul(sp - 4 << 2, sp - 4 << 2, sp - 2 << 2); sp -= 2;
            continue;
          case Bytecodes.FMUL:
            f32[sp - 2] = f32[sp - 2] * f32[sp - 1]; sp--;
            continue;
          case Bytecodes.DMUL:
            aliasedI32[0] = i32[sp - 4];
            aliasedI32[1] = i32[sp - 3]; ia = aliasedF64[0];
            aliasedI32[0] = i32[sp - 2];
            aliasedI32[1] = i32[sp - 1]; ib = aliasedF64[0];
            aliasedF64[0] = ia * ib;
            i32[sp - 4] = aliasedI32[0];
            i32[sp - 3] = aliasedI32[1];
            sp -= 2;
            continue;
          case Bytecodes.IDIV:
            if (i32[sp - 1] === 0) {
              thread.throwException(fp, sp, opPC, ExceptionType.ArithmeticException);
            }
            ia = i32[sp - 2];
            ib = i32[sp - 1];
            i32[sp - 2] = (ia === Constants.INT_MIN && ib === -1) ? ia : ((ia / ib) | 0); sp--;
            continue;
          case Bytecodes.LDIV:
            if (i32[sp - 2] === 0 && i32[sp - 1] === 0) {
              thread.throwException(fp, sp, opPC, ExceptionType.ArithmeticException);
            }
            ASM._lDiv(sp - 4 << 2, sp - 4 << 2, sp - 2 << 2); sp -= 2;
            continue;
          case Bytecodes.FDIV:
            fb = f32[--sp];
            fa = f32[--sp];
            f32[sp++] = Math.fround(fa / fb);
            continue;
          case Bytecodes.DDIV:
            aliasedI32[0] = i32[sp - 4];
            aliasedI32[1] = i32[sp - 3]; ia = aliasedF64[0];
            aliasedI32[0] = i32[sp - 2];
            aliasedI32[1] = i32[sp - 1]; ib = aliasedF64[0];
            aliasedF64[0] = ia / ib;
            i32[sp - 4] = aliasedI32[0];
            i32[sp - 3] = aliasedI32[1];
            sp -= 2;
            continue;
          case Bytecodes.IREM:
            if (i32[sp - 1] === 0) {
              thread.throwException(fp, sp, opPC, ExceptionType.ArithmeticException);
            }
            i32[sp - 2] = (i32[sp - 2] % i32[sp - 1]) | 0; sp--;
            continue;
          case Bytecodes.LREM:
            if (i32[sp - 2] === 0 && i32[sp - 1] === 0) {
              thread.throwException(fp, sp, opPC, ExceptionType.ArithmeticException);
            }
            ASM._lRem(sp - 4 << 2, sp - 4 << 2, sp - 2 << 2); sp -= 2;
            continue;
          case Bytecodes.FREM:
            fb = f32[--sp];
            fa = f32[--sp];
            f32[sp++] = Math.fround(fa % fb);
            continue;
          case Bytecodes.DREM:
            aliasedI32[0] = i32[sp - 4];
            aliasedI32[1] = i32[sp - 3]; ia = aliasedF64[0];
            aliasedI32[0] = i32[sp - 2];
            aliasedI32[1] = i32[sp - 1]; ib = aliasedF64[0];
            aliasedF64[0] = ia % ib;
            i32[sp - 4] = aliasedI32[0];
            i32[sp - 3] = aliasedI32[1];
            sp -= 2;
            continue;
          case Bytecodes.INEG:
            i32[sp - 1] = -i32[sp - 1] | 0;
            continue;
          case Bytecodes.LNEG:
            ASM._lNeg(sp - 2 << 2, sp - 2 << 2);
            continue;
          case Bytecodes.FNEG:
            f32[sp - 1] = -f32[sp - 1];
            continue;
          case Bytecodes.DNEG:
            aliasedI32[0] = i32[sp - 2];
            aliasedI32[1] = i32[sp - 1];
            aliasedF64[0] = -aliasedF64[0];
            i32[sp - 2] = aliasedI32[0];
            i32[sp - 1] = aliasedI32[1];
            continue;
          case Bytecodes.ISHL:
            ib = i32[--sp];
            ia = i32[--sp];
            i32[sp++] = ia << ib;
            continue;
          case Bytecodes.LSHL:
            ASM._lShl(sp - 3 << 2, sp - 3 << 2, i32[sp - 1]); sp -= 1;
            continue;
          case Bytecodes.ISHR:
            ib = i32[--sp];
            ia = i32[--sp];
            i32[sp++] = ia >> ib;
            continue;
          case Bytecodes.LSHR:
            ASM._lShr(sp - 3 << 2, sp - 3 << 2, i32[sp - 1]); sp -= 1;
            continue;
          case Bytecodes.IUSHR:
            ib = i32[--sp];
            ia = i32[--sp];
            i32[sp++] = ia >>> ib;
            continue;
          case Bytecodes.LUSHR:
            ASM._lUshr(sp - 3 << 2, sp - 3 << 2, i32[sp - 1]); sp -= 1;
            continue;
          case Bytecodes.IAND:
            i32[sp - 2] &= i32[--sp];
            continue;
          case Bytecodes.LAND:
            i32[sp - 4] &= i32[sp - 2];
            i32[sp - 3] &= i32[sp - 1]; sp -= 2;
            break;
          case Bytecodes.IOR:
            i32[sp - 2] |= i32[--sp];
            continue;
          case Bytecodes.LOR:
            i32[sp - 4] |= i32[sp - 2];
            i32[sp - 3] |= i32[sp - 1]; sp -= 2;
            continue;
          case Bytecodes.IXOR:
            i32[sp - 2] ^= i32[--sp];
            continue;
          case Bytecodes.LXOR:
            i32[sp - 4] ^= i32[sp - 2];
            i32[sp - 3] ^= i32[sp - 1]; sp -= 2;
            continue;
          case Bytecodes.LCMP:
            ASM._lCmp(sp - 4 << 2, sp - 4 << 2, sp - 2 << 2); sp -= 3;
            continue;
          case Bytecodes.FCMPL:
          case Bytecodes.FCMPG:
            fb = f32[--sp];
            fa = f32[--sp];
            if (isNaN(fa) || isNaN(fb)) {
              i32[sp++] = op === Bytecodes.FCMPL ? -1 : 1;
            } else if (fa > fb) {
              i32[sp++] = 1;
            } else if (fa < fb) {
              i32[sp++] = -1;
            } else {
              i32[sp++] = 0;
            }
            continue;
          case Bytecodes.DCMPL:
          case Bytecodes.DCMPG:
            aliasedI32[0] = i32[sp - 2];
            aliasedI32[1] = i32[sp - 1];
            fb = aliasedF64[0];
            aliasedI32[0] = i32[sp - 4];
            aliasedI32[1] = i32[sp - 3];
            fa = aliasedF64[0];
            sp -= 4;
            if (isNaN(fa) || isNaN(fb)) {
              i32[sp++] = op === Bytecodes.DCMPL ? -1 : 1;
            } else if (fa > fb) {
              i32[sp++] = 1;
            } else if (fa < fb) {
              i32[sp++] = -1;
            } else {
              i32[sp++] = 0;
            }
            continue;
          case Bytecodes.IFEQ:
            targetPC = opPC + ((code[pc++] << 8 | code[pc++]) << 16 >> 16);
            if (i32[--sp] === 0) {
              pc = targetPC;
            }
            continue;
          case Bytecodes.IFNE:
            targetPC = opPC + ((code[pc++] << 8 | code[pc++]) << 16 >> 16);
            if (i32[--sp] !== 0) {
              pc = targetPC;
            }
            continue;
          case Bytecodes.IFLT:
            targetPC = opPC + ((code[pc++] << 8 | code[pc++]) << 16 >> 16);
            if (i32[--sp] < 0) {
              pc = targetPC;
            }
            continue;
          case Bytecodes.IFGE:
            targetPC = opPC + ((code[pc++] << 8 | code[pc++]) << 16 >> 16);
            if (i32[--sp] >= 0) {
              pc = targetPC;
            }
            continue;
          case Bytecodes.IFGT:
            targetPC = opPC + ((code[pc++] << 8 | code[pc++]) << 16 >> 16);
            if (i32[--sp] > 0) {
              pc = targetPC;
            }
            continue;
          case Bytecodes.IFLE:
            targetPC = opPC + ((code[pc++] << 8 | code[pc++]) << 16 >> 16);
            if (i32[--sp] <= 0) {
              pc = targetPC;
            }
            continue;
          case Bytecodes.IF_ICMPEQ:
            targetPC = opPC + ((code[pc++] << 8 | code[pc++]) << 16 >> 16);
            if (i32[--sp] === i32[--sp]) {
              pc = targetPC;
            }
            continue;
          case Bytecodes.IF_ICMPNE:
            targetPC = opPC + ((code[pc++] << 8 | code[pc++]) << 16 >> 16);
            if (i32[--sp] !== i32[--sp]) {
              pc = targetPC;
            }
            continue;
          case Bytecodes.IF_ICMPLT:
            targetPC = opPC + ((code[pc++] << 8 | code[pc++]) << 16 >> 16);
            if (i32[--sp] > i32[--sp]) {
              pc = targetPC;
            }
            continue;
          case Bytecodes.IF_ICMPGE:
            targetPC = opPC + ((code[pc++] << 8 | code[pc++]) << 16 >> 16);
            if (i32[--sp] <= i32[--sp]) {
              pc = targetPC;
            }
            continue;
          case Bytecodes.IF_ICMPGT:
            targetPC = opPC + ((code[pc++] << 8 | code[pc++]) << 16 >> 16);
            if (i32[--sp] < i32[--sp]) {
              pc = targetPC;
            }
            continue;
          case Bytecodes.IF_ICMPLE:
            targetPC = opPC + ((code[pc++] << 8 | code[pc++]) << 16 >> 16);
            if (i32[--sp] >= i32[--sp]) {
              pc = targetPC;
            }
            continue;
          case Bytecodes.IF_ACMPEQ:
            targetPC = opPC + ((code[pc++] << 8 | code[pc++]) << 16 >> 16);
            if (ref[--sp] === ref[--sp]) {
              pc = targetPC;
            }
            continue;
          case Bytecodes.IF_ACMPNE:
            targetPC = opPC + ((code[pc++] << 8 | code[pc++]) << 16 >> 16);
            if (ref[--sp] !== ref[--sp]) {
              pc = targetPC;
            }
            continue;
          case Bytecodes.IFNULL:
            targetPC = opPC + ((code[pc++] << 8 | code[pc++]) << 16 >> 16);
            if (!ref[--sp]) {
              pc = targetPC;
            }
            continue;
          case Bytecodes.IFNONNULL:
            targetPC = opPC + ((code[pc++] << 8 | code[pc++]) << 16 >> 16);
            if (ref[--sp]) {
              pc = targetPC;
            }
            continue;
          case Bytecodes.GOTO:
            pc = opPC + ((code[pc++] << 8 | code[pc++]) << 16 >> 16);
            continue;
          //        case Bytecodes.GOTO_W:
          //          frame.pc = frame.read32Signed() - 1;
          //          break;
          //        case Bytecodes.JSR:
          //          pc = frame.read16();
          //          stack.push(frame.pc);
          //          frame.pc = pc;
          //          break;
          //        case Bytecodes.JSR_W:
          //          pc = frame.read32();
          //          stack.push(frame.pc);
          //          frame.pc = pc;
          //          break;
          //        case Bytecodes.RET:
          //          frame.pc = frame.local[frame.read8()];
          //          break;
          case Bytecodes.I2L:
            i32[sp] = i32[sp - 1] < 0 ? -1 : 0; sp++;
            continue;
          case Bytecodes.I2F:
            aliasedF32[0] = i32[--sp];
            i32[sp++] = aliasedI32[0];
            continue;
          case Bytecodes.I2D:
            aliasedF64[0] = i32[--sp];
            i32[sp++] = aliasedI32[0];
            i32[sp++] = aliasedI32[1];
            continue;
          case Bytecodes.L2I:
            sp--;
            continue;
          case Bytecodes.L2F:
            aliasedF32[0] = Math.fround(longToNumber(i32[sp - 2], i32[sp - 1]));
            i32[sp - 2] = aliasedI32[0];
            sp --;
            continue;
          case Bytecodes.L2D:
            aliasedF64[0] = longToNumber(i32[sp - 2], i32[sp - 1]);
            i32[sp - 2] = aliasedI32[0];
            i32[sp - 1] = aliasedI32[1];
            continue;
          case Bytecodes.F2I:
            fa = f32[sp - 1];
            if (fa > Constants.INT_MAX) {
              i32[sp - 1] = Constants.INT_MAX;
            } else if (fa < Constants.INT_MIN) {
              i32[sp - 1] = Constants.INT_MIN;
            } else {
              i32[sp - 1] = fa | 0;
            }
            continue;
          case Bytecodes.F2L:
            fa = f32[--sp];
            value = Long.fromNumber(fa);
            i32[sp++] = value.low_;
            i32[sp++] = value.high_;
            continue;
          case Bytecodes.F2D:
            aliasedF64[0] = f32[--sp];
            i32[sp++] = aliasedI32[0];
            i32[sp++] = aliasedI32[1];
            continue;
          case Bytecodes.D2I:
            aliasedI32[0] = i32[sp - 2];
            aliasedI32[1] = i32[sp - 1];
            fa = aliasedF64[0];
            if (fa > Constants.INT_MAX) {
              i32[sp - 2] = Constants.INT_MAX;
            } else if (fa < Constants.INT_MIN) {
              i32[sp - 2] = Constants.INT_MIN;
            } else {
              i32[sp - 2] = fa | 0;
            }
            sp --;
            continue;
          case Bytecodes.D2L:
            aliasedI32[0] = i32[sp - 2];
            aliasedI32[1] = i32[sp - 1];
            fa = aliasedF64[0];
            if (fa === Number.POSITIVE_INFINITY) {
              i32[sp - 2] = Constants.LONG_MAX_LOW;
              i32[sp - 1] = Constants.LONG_MAX_HIGH;
            } else if (fa === Number.NEGATIVE_INFINITY) {
              i32[sp - 2] = Constants.LONG_MIN_LOW;
              i32[sp - 1] = Constants.LONG_MIN_HIGH;
            } else {
              value = Long.fromNumber(fa);
              i32[sp - 2] = value.low_;
              i32[sp - 1] = value.high_;
            }
            continue;
          case Bytecodes.D2F:
            aliasedI32[0] = i32[sp - 2];
            aliasedI32[1] = i32[sp - 1];
            f32[sp - 2] = Math.fround(aliasedF64[0]);
            sp --;
            continue;
          case Bytecodes.I2B:
            i32[sp - 1] = (i32[sp - 1] << 24) >> 24;
            continue;
          case Bytecodes.I2C:
            i32[sp - 1] &= 0xffff;
            continue;
          case Bytecodes.I2S:
            i32[sp - 1] = (i32[sp - 1] << 16) >> 16;
            continue;
          case Bytecodes.TABLESWITCH:
            pc = (pc + 3) & ~0x03; // Consume Padding
            offset = code[pc++] << 24 | code[pc++] << 16 | code[pc++] << 8 | code[pc++];
            ia = code[pc++] << 24 | code[pc++] << 16 | code[pc++] << 8 | code[pc++];
            ib = code[pc++] << 24 | code[pc++] << 16 | code[pc++] << 8 | code[pc++];
            value = i32[--sp];
            if (value >= ia && value <= ib) {
              pc += (value - ia) << 2;
              offset = code[pc++] << 24 | code[pc++] << 16 | code[pc++] << 8 | code[pc++];
            }
            pc = opPC + offset;
            continue;
          case Bytecodes.LOOKUPSWITCH:
            pc = (pc + 3) & ~0x03; // Consume Padding
                offset = code[pc++] << 24 | code[pc++] << 16 | code[pc++] << 8 | code[pc++];
            var npairs = code[pc++] << 24 | code[pc++] << 16 | code[pc++] << 8 | code[pc++];
            value = i32[--sp];
            lookup:
            for (var i = 0; i < npairs; i++) {
              var key  = code[pc++] << 24 | code[pc++] << 16 | code[pc++] << 8 | code[pc++];
              if (key === value) {
                offset = code[pc++] << 24 | code[pc++] << 16 | code[pc++] << 8 | code[pc++];
              } else {
                pc += 4;
              }
              if (key >= value) {
                break lookup;
              }
            }
            pc = opPC + offset;
            continue;
          case Bytecodes.ANEWARRAY:
            index = code[pc++] << 8 | code[pc++];
            classInfo = resolveClass(index, ci);
            size = i32[--sp];
            if (size < 0) {
              thread.throwException(fp, sp, opPC, ExceptionType.NegativeArraySizeException);
            }
            ref[sp++] = newArray(classInfo.klass, size);
            continue;
          case Bytecodes.MULTIANEWARRAY:
            index = code[pc++] << 8 | code[pc++];
            classInfo = resolveClass(index, ci);
            var dimensions = code[pc++];
            var lengths = new Array(dimensions);
            for (var i = 0; i < dimensions; i++) {
              lengths[i] = i32[--sp];
              if (size < 0) {
                thread.throwException(fp, sp, opPC, ExceptionType.NegativeArraySizeException);
              }
            }
            ref[sp++] = J2ME.newMultiArray(classInfo.klass, lengths.reverse());
            continue;
          case Bytecodes.ARRAYLENGTH:
            array = ref[--sp];
            i32[sp++] = array.length;
            continue;
          case Bytecodes.GETFIELD:
          case Bytecodes.GETSTATIC:
            index = code[pc++] << 8 | code[pc++];
            fieldInfo = cp.resolved[index] || cp.resolveField(index, op === Bytecodes.GETSTATIC);
            if (op === Bytecodes.GETSTATIC) {
              thread.classInitAndUnwindCheck(fp, sp, opPC, fieldInfo.classInfo);
              if (U) {
                return;
              }
              object = fieldInfo.classInfo.getStaticObject($.ctx);
            } else {
              object = ref[--sp];
            }
            address = object._address + fieldInfo.byteOffset;
            switch (fieldInfo.kind) {
              case Kind.Reference:
                ref[sp++] = ref[address >> 2];
                continue;
              case Kind.Int:
              case Kind.Byte:
              case Kind.Char:
              case Kind.Short:
              case Kind.Boolean:
              case Kind.Float:
                i32[sp++] = i32[address >> 2];
                continue;
              case Kind.Long:
              case Kind.Double:
                i32[sp++] = i32[address     >> 2];
                i32[sp++] = i32[address + 4 >> 2];
                continue;
              default:
                release || assert(false);
            }
            continue;
          case Bytecodes.PUTFIELD:
          case Bytecodes.PUTSTATIC:
            index = code[pc++] << 8 | code[pc++];
            fieldInfo = cp.resolved[index] || cp.resolveField(index, op === Bytecodes.PUTSTATIC);
            isStatic = op === Bytecodes.PUTSTATIC;
            if (isStatic) {
              thread.classInitAndUnwindCheck(fp, sp, opPC, fieldInfo.classInfo);
              if (U) {
                return;
              }
              object = fieldInfo.classInfo.getStaticObject($.ctx);
            } else {
              object = ref[sp - (isTwoSlot(fieldInfo.kind) ? 3 : 2)];
            }
            address = object._address + fieldInfo.byteOffset;
            switch (fieldInfo.kind) {
              case Kind.Reference:
                ref[address >> 2] = ref[--sp];
                break;
              case Kind.Int:
              case Kind.Byte:
              case Kind.Char:
              case Kind.Short:
              case Kind.Boolean:
              case Kind.Float:
                i32[address >> 2] = i32[--sp];
                break;
              case Kind.Long:
              case Kind.Double:
                i32[address + 4 >> 2] = i32[--sp];
                i32[address     >> 2] = i32[--sp];
                break;
              default:
                release || assert(false);
            }
            if (!isStatic) {
              sp--; // Pop Reference
            }
            continue;
          case Bytecodes.NEW:
            index = code[pc++] << 8 | code[pc++];
            release || traceWriter && traceWriter.writeLn(mi.implKey + " " + index);
            classInfo = resolveClass(index, ci);
            thread.classInitAndUnwindCheck(fp, sp, opPC, classInfo);
            if (U) {
              return;
            }
            ref[sp++] = newObject(classInfo.klass);
            continue;
          case Bytecodes.CHECKCAST:
            index = code[pc++] << 8 | code[pc++];
            classInfo = resolveClass(index, mi.classInfo);
            object = ref[sp - 1];
            if (object && !isAssignableTo(object.klass, classInfo.klass)) {
              thread.set(fp, sp, opPC);
              throw $.newClassCastException (
                object.klass.classInfo.getClassNameSlow() + " is not assignable to " +
                classInfo.getClassNameSlow()
              );
            }
            continue;
          case Bytecodes.INSTANCEOF:
            index = code[pc++] << 8 | code[pc++];
            classInfo = resolveClass(index, ci);
            object = ref[--sp];
            i32[sp++] = (!object ? false : isAssignableTo(object.klass, classInfo.klass)) ? 1 : 0;
            continue;
          case Bytecodes.ATHROW:
            object = ref[--sp];
            if (!object) {
              thread.throwException(fp, sp, opPC, ExceptionType.NullPointerException);
            }
            throw object;
            continue;
          case Bytecodes.MONITORENTER:
            object = ref[--sp];
            thread.ctx.monitorEnter(object);
            release || assert(U !== VMState.Yielding, "Monitors should never yield.");
            if (U === VMState.Pausing || U === VMState.Stopping) {
              thread.set(fp, sp, pc); // We need to resume past the MONITORENTER bytecode.
              return;
            }
            continue;
          case Bytecodes.MONITOREXIT:
            object = ref[--sp];
            thread.ctx.monitorExit(object);
            continue;
          case Bytecodes.WIDE:
            var op = code[pc++];
            switch (op) {
              case Bytecodes.ILOAD:
              case Bytecodes.FLOAD:
                i32[sp++] = i32[lp + (code[pc++] << 8 | code[pc++])];
                continue;
              case Bytecodes.ALOAD:
                ref[sp++] = ref[lp + (code[pc++] << 8 | code[pc++])];
                continue;
              case Bytecodes.LLOAD:
              case Bytecodes.DLOAD:
                offset = lp + (code[pc++] << 8 | code[pc++]);
                i32[sp++] = i32[offset];
                i32[sp++] = i32[offset + 1];
                continue;
              case Bytecodes.ISTORE:
              case Bytecodes.FSTORE:
                i32[lp + (code[pc++] << 8 | code[pc++])] = i32[--sp];
                continue;
              case Bytecodes.ASTORE:
                ref[lp + (code[pc++] << 8 | code[pc++])] = ref[--sp];
                continue;
              case Bytecodes.LSTORE:
              case Bytecodes.DSTORE:
                offset = lp + (code[pc++] << 8 | code[pc++]);
                i32[offset + 1] = i32[--sp];
                i32[offset]     = i32[--sp];
                continue;
              case Bytecodes.IINC:
                index = code[pc++] << 8 | code[pc++];
                value = (code[pc++] << 8 | code[pc++]) << 16 >> 16;
                i32[lp + index] = i32[lp + index] + value | 0;
                continue;
              //case Bytecodes.RET:
              //  this.pc = this.local[this.read16()];
              //  break;
              default:
                var opName = Bytecodes[op];
                throw new Error("Wide opcode " + opName + " [" + op + "] not supported.");
            }
            continue;
          case Bytecodes.NEWARRAY:
            type = code[pc++];
            size = i32[--sp];
            if (size < 0) {
              thread.throwException(fp, sp, opPC, ExceptionType.NegativeArraySizeException);
            }
            ref[sp++] = newArray(PrimitiveClassInfo["????ZCFDBSIJ"[type]].klass, size);
            continue;
          case Bytecodes.LRETURN:
          case Bytecodes.DRETURN:
          case Bytecodes.IRETURN:
          case Bytecodes.FRETURN:
          case Bytecodes.ARETURN:
          case Bytecodes.RETURN:
            var lastSP = sp;
            var lastMI = mi;
            if (lastMI.isSynchronized) {
              monitor = ref[fp + FrameLayout.MonitorOffset];
              $.ctx.monitorExit(monitor);
            }
            opPC = i32[fp + FrameLayout.CallerRAOffset];
            sp = fp - maxLocals;
            fp = i32[fp + FrameLayout.CallerFPOffset];
            mi = ref[fp + FrameLayout.CalleeMethodInfoOffset];
            if (mi === null) {
              thread.set(fp, sp, opPC);
              thread.popFrame(null);
              // REDUX: What do we do about the return value here?
              return;
            }
            maxLocals = mi.codeAttribute.max_locals;
            lp = fp - maxLocals;
            release || traceWriter && traceWriter.outdent();
            release || traceWriter && traceWriter.writeLn("<< I " + lastMI.implKey);
            ci = mi.classInfo;
            cp = ci.constantPool;
            code = mi.codeAttribute.code;
            // Calculate the PC based on the size of the caller's invoke bytecode.
            pc = opPC + (code[opPC] === Bytecodes.INVOKEINTERFACE ? 5 : 3);
            // Push return value.
            switch (op) {
              case Bytecodes.LRETURN:
              case Bytecodes.DRETURN:
                i32[sp++] = i32[lastSP - 2]; // Low Bits
                // Fallthrough
              case Bytecodes.IRETURN:
              case Bytecodes.FRETURN:
                i32[sp++] = i32[lastSP - 1];
                continue;
              case Bytecodes.ARETURN:
                ref[sp++] = ref[lastSP - 1];
                continue;
            }
            continue;
          case Bytecodes.INVOKEVIRTUAL:
          case Bytecodes.INVOKESPECIAL:
          case Bytecodes.INVOKESTATIC:
          case Bytecodes.INVOKEINTERFACE:
            index = code[pc++] << 8 | code[pc++];
            if (op === Bytecodes.INVOKEINTERFACE) {
              pc += 2; // Args Number & Zero
            }
            isStatic = (op === Bytecodes.INVOKESTATIC);

            // Resolve method and do the class init check if necessary.
            var calleeMethodInfo = cp.resolved[index] || cp.resolveMethod(index, isStatic);
            var calleeTargetMethodInfo: MethodInfo = null;

            var callee = null;
            object = null;
            if (!isStatic) {
              object = ref[sp - calleeMethodInfo.argumentSlots];
            }

            if (isStatic) {
              thread.classInitAndUnwindCheck(fp, sp, opPC, calleeMethodInfo.classInfo);
              if (U) {
                return;
              }
            }

            switch (op) {
              case Bytecodes.INVOKESPECIAL:
                if (!object) {
                  thread.throwException(fp, sp, opPC, ExceptionType.NullPointerException);
                }
              case Bytecodes.INVOKESTATIC:
                calleeTargetMethodInfo = calleeMethodInfo;
                break;
              case Bytecodes.INVOKEVIRTUAL:
                calleeTargetMethodInfo = object.klass.classInfo.vTable[calleeMethodInfo.vTableIndex];
                break;
              case Bytecodes.INVOKEINTERFACE:
                calleeTargetMethodInfo = object.klass.classInfo.iTable[calleeMethodInfo.mangledName];
                break;
              default:
                release || traceWriter && traceWriter.writeLn("Not Implemented: " + Bytecodes[op]);
                assert(false, "Not Implemented: " + Bytecodes[op]);
            }

            // Call Native or Compiled Method.
            if (calleeTargetMethodInfo.isNative || calleeTargetMethodInfo.state === MethodState.Compiled) {
              args.length = 0;
              var signatureKinds = calleeTargetMethodInfo.signatureKinds;
              for (var i = signatureKinds.length - 1; i > 0; i--) {
                kind = signatureKinds[i];
                switch (kind) {
                  case Kind.Double: // Doubles are passed in as a number value.
                    aliasedI32[1] = i32[--sp];
                    aliasedI32[0] = i32[--sp];
                    args.unshift(aliasedF64[0]);
                    break;
                  case Kind.Float:
                    args.unshift(f32[--sp]);
                    break;
                  case Kind.Long:
                    args.unshift(i32[--sp]); // High Bits
                    // Fallthrough
                  case Kind.Int:
                  case Kind.Byte:
                  case Kind.Char:
                  case Kind.Short:
                  case Kind.Boolean:
                    args.unshift(i32[--sp]);
                    break;
                  case Kind.Reference:
                    args.unshift(ref[--sp]);
                    break;
                  default:
                    release || assert(false, "Invalid Kind: " + Kind[kind]);
                }
              }
              if (!isStatic) {
                --sp; // Pop Reference
              }
              thread.set(fp, sp, opPC);
              callee = calleeTargetMethodInfo.fn || getLinkedMethod(calleeTargetMethodInfo);
              if (!release) {
                // assert(callee.length === args.length, "Function " + callee + " (" + calleeTargetMethodInfo.implKey + "), should have " + args.length + " arguments.");
              }

              var frameHash = 0;

              returnValue = callee.apply(object, args);

              if (!release) {
                checkReturnValue(calleeMethodInfo, returnValue, tempReturn0);
              }

              if (U) {
                release || traceWriter && traceWriter.writeLn("<< U Unwind: " + VMState[U]);
                return;
              }

              kind = signatureKinds[0];

              // Push return value.
              switch (kind) {
                case Kind.Double: // Doubles are passed in as a number value.
                  aliasedF64[0] = returnValue;
                  i32[sp++] = aliasedI32[0];
                  i32[sp++] = aliasedI32[1];
                  continue;
                case Kind.Float:
                  f32[sp++] = returnValue;
                  continue;
                case Kind.Long:
                  i32[sp++] = returnValue;
                  i32[sp++] = tempReturn0;
                  continue;
                case Kind.Int:
                case Kind.Byte:
                case Kind.Char:
                case Kind.Short:
                case Kind.Boolean:
                  i32[sp++] = returnValue;
                  continue;
                case Kind.Reference:
                  ref[sp++] = returnValue;
                  continue;
                case Kind.Void:
                  continue;
                default:
                  release || assert(false, "Invalid Kind: " + Kind[kind]);
              }
              continue;
            }

            // Call Interpreted Method.
            release || traceWriter && traceWriter.writeLn(">> I " + calleeTargetMethodInfo.implKey);
            mi = calleeTargetMethodInfo;
            maxLocals = mi.codeAttribute.max_locals;
            ci = mi.classInfo;
            cp = ci.constantPool;

            var callerFPOffset = fp;
            // Reserve space for non-parameter locals.
            lp = sp - mi.argumentSlots;
            fp = lp + maxLocals;
            sp = fp + FrameLayout.CallerSaveSize;

            // Caller saved values.
            i32[fp + FrameLayout.CallerRAOffset] = opPC;
            i32[fp + FrameLayout.CallerFPOffset] = callerFPOffset;
            ref[fp + FrameLayout.CalleeMethodInfoOffset] = mi;
            ref[fp + FrameLayout.MonitorOffset] = null; // Monitor

            // Reset PC.
            opPC = pc = 0;

            if (calleeTargetMethodInfo.isSynchronized) {
              monitor = calleeTargetMethodInfo.isStatic
                ? calleeTargetMethodInfo.classInfo.getClassObject()
                : object;
              ref[fp + FrameLayout.MonitorOffset] = monitor;
              $.ctx.monitorEnter(monitor);
              release || assert(U !== VMState.Yielding, "Monitors should never yield.");
              if (U === VMState.Pausing || U === VMState.Stopping) {
                thread.set(fp, sp, opPC);
                return;
              }
            }

            code = mi.codeAttribute.code;

            release || traceWriter && traceWriter.indent();
            continue;
          default:
            release || traceWriter && traceWriter.writeLn("Not Implemented: " + Bytecodes[op] + ", PC: " + opPC + ", CODE: " + code.length);
            release || assert(false, "Not Implemented: " + Bytecodes[op]);
            continue;
        }
      } catch (e) {
        release || traceWriter && traceWriter.redLn("XXX I Caught: " + e + ", details: " + toName(e));
        // release || traceWriter && traceWriter.writeLns(e.stack);
        // release || traceWriter && traceWriter.writeLn(jsGlobal.getBacktrace());

        thread.set(fp, sp, opPC);
        e = translateException(e);
        if (!e.klass) {
          // A non-java exception was thrown. Rethrow so it is not handled by exceptionUnwind.
          throw e;
        }
        thread.exceptionUnwind(e);

        // Load thread state after exception unwind.
        fp = thread.fp;
        sp = thread.sp;
        pc = thread.pc;

        mi = thread.frame.methodInfo;
        maxLocals = mi.codeAttribute.max_locals;
        lp = fp - maxLocals;
        ci = mi.classInfo;
        cp = ci.constantPool;
        code = mi.codeAttribute.code;
        continue;
      }
    }
  }

  // print(disassemble(interpret));
}
