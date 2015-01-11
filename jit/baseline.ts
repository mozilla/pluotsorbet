module J2ME {
  import assert = Debug.assert;
  import Bytecodes = Bytecode.Bytecodes;
  import Condition = Bytecode.Condition;
  import BytecodeStream = Bytecode.BytecodeStream;

  import Block = Bytecode.Block;
  import BlockMap = Bytecode.BlockMap;
  import ExceptionBlock = Bytecode.ExceptionBlock;

  var writer = null; // new IndentingWriter();


  export var baselineTotal = 0;
  export var baselineCompiled = 0;

  export function baselineCompileMethod(methodInfo: MethodInfo, ctx: Context, target: CompilationTarget): CompiledMethodInfo {

    var compileExceptions = true;
    if (!compileExceptions && methodInfo.exception_table && methodInfo.exception_table.length) {
      throw new Error("Method: " + methodInfo.implKey + " has exception handlers.");
    }
    writer && writer.writeLn("Compile: " + methodInfo.implKey);
    baselineTotal ++;
    try {
      var result = new BaselineCompiler(methodInfo, ctx, target).compile();
      baselineCompiled ++;
      return result;
    } catch (e) {
      throw e;
    }
  }

  class Emitter {
    private buffer: string [];
    private _indent = 0;
    constructor() {
      this.buffer = [];
    }
    enter(s: string) {
      this.writeLn(s);
      this._indent ++;
    }
    leave(s: string) {
      this._indent --;
      this.writeLn(s);
    }
    leaveAndEnter(s: string) {
      this._indent --;
      this.writeLn(s);
      this._indent ++;
    }
    writeLn(s: string) {
      var prefix = "";
      for (var i = 0; i < this._indent; i++) {
        prefix += "  ";
      }
      this.buffer.push(prefix + s);
      writer && writer.writeLn(prefix + s);
    }
    indent() {
      this._indent ++;
    }
    outdent() {
      this._indent --;
    }
    prependLn(s: string) {
      this.buffer.unshift(s);
    }
    toString(): string {
      return this.buffer.join("\n");
    }
  }

  function kindToTypedArrayName(kind: Kind): string {
    switch (kind) {
      case Kind.Int:
        return "Int32Array";
      case Kind.Char:
        return "Uint16Array";
      case Kind.Short:
        return "Int16Array";
      case Kind.Byte:
      case Kind.Boolean:
        return "Int8Array";
      case Kind.Float:
        return "Float32Array";
      case Kind.Long:
        return "Array";
      case Kind.Double:
        return"Float64Array";
      default:
        throw Debug.unexpected(Kind[kind]);
    }
  }

  function conditionToOperator(condition: Condition): string {
    switch (condition) {
      case Condition.EQ: return "==";
      case Condition.NE: return "!=";
      case Condition.LT: return "<";
      case Condition.LE: return "<=";
      case Condition.GT: return ">";
      case Condition.GE: return ">=";
      default:
        Debug.unexpected(Condition[condition]);
    }
  }

  function doubleConstant(v): string {
    // Check for -0 floats.
    if ((1 / v) < 0) {
      return "-" + Math.abs(v);
    }
    return String(v);
  }

  function longConstant(v): string {
    if (v === 0) {
      return "Long.ZERO";
    } else if (v === 1) {
      return "Long.ONE";
    }
    return "Long.fromInt(" + v + ")";
  }

  export class BaselineCompiler {
    sp: number;
    pc: number;

    private ctx: Context;
    private emitter: Emitter;
    private methodInfo: MethodInfo;
    private parameters: string [];
    private hasHandlers: boolean;
    private blockStackHeightMap: number [];
    private referencedClasses: ClassInfo [];
    private local: string [];
    private stack: string [];
    private variables: string [];

    static localNames = ["a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"];

    /**
     * Make sure that none of these shadow gloal names, like "U".
     */
    static stackNames = ["A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "_U", "V", "W", "X", "Y", "Z"];

    constructor(methodInfo: MethodInfo, ctx: Context, target: CompilationTarget) {
      this.ctx = ctx;
      this.methodInfo = methodInfo;
      this.local = [];
      this.variables = [];
      this.stack = [];
      this.parameters = [];
      this.referencedClasses = [];
      this.hasHandlers = !!methodInfo.exception_table.length;
      this.blockStackHeightMap = [0];
      this.emitter = new Emitter();
    }

    compile(): CompiledMethodInfo {
      this.emitPrologue();
      this.emitBody();
      if (this.variables.length) {
        this.emitter.prependLn("var " + this.variables.join(", ") + ";");
      }
      return new CompiledMethodInfo(this.parameters, this.emitter.toString(), this.referencedClasses);
    }
    needsVariable(name: string) {
      if (this.variables.indexOf(name) < 0) {
        this.variables.push(name);
      }
    }
    emitBody() {
      var blockMap = new BlockMap(this.methodInfo);
      blockMap.build();
      writer && blockMap.trace(writer);
      var blocks = blockMap.blocks;
      var stream = new BytecodeStream(this.methodInfo.code);

      if (blocks.length === 1 && !this.hasHandlers && !blocks[0].isLoopHeader) {
        this.emitBlockBody(stream, blocks[0]);
        return;
      }

      this.emitter.enter("while (true) {");
      this.hasHandlers && this.emitter.enter("try {");
      this.emitter.enter("switch (pc) {");

      for (var i = 0; i < blocks.length; i++) {
        var block = blocks[i];
        if (block instanceof ExceptionBlock) {
          continue
        }
        if (block.isExceptionEntry) {
          writer && writer.writeLn("block.isExceptionEntry");
          this.setBlockStackHeight(block.startBci, 1);
        }
        var lastBC = this.emitBlock(stream, block);
        var successors = block.successors;
        for (var j = 0; j < successors.length; j++) {
          var successor = successors[j];
          if (successor instanceof ExceptionBlock ||
              successor.isExceptionEntry) {
            continue;
          }
          this.setBlockStackHeight(successors[j].startBci, this.sp);
        }
        var needsFallthroughPC = !Bytecode.isBlockEnd(lastBC);
        // Check to see if we can get away without going through the switch again.
        if (i < blocks.length - 1 &&
            block.successors.length === 1 &&
            blocks[i + 1].startBci === stream.currentBCI) {
          needsFallthroughPC = false;
        }
        if (needsFallthroughPC) {
          this.emitter.writeLn("pc = " + stream.currentBCI + "; break;");
        }
        this.emitter.outdent();
      }
      this.emitter.leave("}");
      if (this.hasHandlers) {
        this.emitter.leaveAndEnter("} catch (ex) {");
        this.emitPush(Kind.Reference, "$TE(ex)");
        for (var i = 0; i < blockMap.blocks.length; i++) {
          var block = blockMap.blocks[i];
          if (block instanceof ExceptionBlock) {
            this.emitExceptionBlock(block);
          }
        }
        this.emitter.writeLn("throw " + this.peek(Kind.Reference) + ";");
        this.emitter.leave("}");
      }
      this.emitter.leave("}");
    }

    emitExceptionBlock(exceptionBlock: ExceptionBlock) {
      var handler = exceptionBlock.handler;
      writer && writer.writeLn("emitExceptionBlock: " + exceptionBlock.startBci + " " + this.sp);
      var check = "";
      if (handler.catch_type > 0) {
        var classInfo = this.lookupClass(handler.catch_type);
        check = "$IOK";
        if (classInfo.isInterface) {
          check = "$IOI";
        }
        check += "(" + this.peek(Kind.Reference) + ", " +  mangleClass(classInfo) + ")";
        check = " && " + check;
      }
      this.emitter.enter("if (pc >= " + handler.start_pc + " && pc < " + handler.end_pc + check + ") {");
      this.emitter.writeLn("pc = " + handler.handler_pc + "; continue;");
      this.emitter.leave("}");
      return;
    }

    emitBlock(stream: BytecodeStream, block: Block): Bytecodes {
      this.emitter.enter("case " + block.startBci + ":");
      return this.emitBlockBody(stream, block);
    }

    emitBlockBody(stream: BytecodeStream, block: Block): Bytecodes {
      this.sp = this.blockStackHeightMap[block.startBci];
      writer && writer.writeLn("emitBlock: " + block.startBci + " " + this.sp + " " + block.isExceptionEntry);
      assert(this.sp !== undefined, "Bad stack height");
      stream.setBCI(block.startBci);
      var lastSourceLocation = null;
      var lastBC: Bytecodes;
      while (stream.currentBCI <= block.endBci) {
        this.pc = stream.currentBCI;
        lastBC = stream.currentBC();
        this.emitBytecode(stream, block);
        stream.next();
      }
      return lastBC;
    }

    private emitPrologue() {
      var local = this.local;
      var localIndex = 0;

      var typeDescriptors = SignatureDescriptor.makeSignatureDescriptor(this.methodInfo.signature).typeDescriptors;

      // Skip the first typeDescriptor since it is the return type.
      for (var i = 1; i < typeDescriptors.length; i++) {
        var kind = Kind.Reference;
        if (typeDescriptors[i] instanceof AtomicTypeDescriptor) {
          kind = (<AtomicTypeDescriptor>typeDescriptors[i]).kind;
        }
        this.parameters.push(this.getLocalName(localIndex));
        localIndex += isTwoSlot(kind) ? 2 : 1;
      }

      var extraLocal = this.methodInfo.max_locals - (this.methodInfo.isStatic ? 0 : 1);
      for (var i = 0; i < extraLocal; i++) {
        local.push(this.getLocalName(i));
      }
      if (local.length) {
        this.emitter.writeLn("var " + local.join(", ") + ";");
      }
      if (!this.methodInfo.isStatic) {
        local.unshift("this");
      }

      var stack = this.stack;
      for (var i = 0; i < this.methodInfo.max_stack; i++) {
        stack.push(this.getStack(i));
      }
      if (stack.length) {
        this.emitter.writeLn("var " + stack.join(", ") + ";");
      }
      this.emitter.writeLn("var pc = 0;");
      if (this.hasHandlers) {
        this.emitter.writeLn("var ex;");
      }
    }

    lookupClass(cpi: number): ClassInfo {
      var classInfo = this.ctx.resolve(this.methodInfo.classInfo.constant_pool, cpi, false);
      ArrayUtilities.pushUnique(this.referencedClasses, classInfo);
      return classInfo;
    }

    lookupMethod(cpi: number, opcode: Bytecodes, isStatic: boolean): MethodInfo {
      var methodInfo = this.ctx.resolve(this.methodInfo.classInfo.constant_pool, cpi, isStatic);
      ArrayUtilities.pushUnique(this.referencedClasses, methodInfo.classInfo);
      return methodInfo;
    }

    lookupField(cpi: number, opcode: Bytecodes, isStatic: boolean): FieldInfo {
      var fieldInfo = this.ctx.resolve(this.methodInfo.classInfo.constant_pool, cpi, isStatic);
      ArrayUtilities.pushUnique(this.referencedClasses, fieldInfo.classInfo);
      return fieldInfo;
    }

    getStack(i: number): string {
      if (i >= BaselineCompiler.stackNames.length) {
        return "s" + (i - BaselineCompiler.stackNames.length);
      }
      return BaselineCompiler.stackNames[i];
    }

    getLocalName(i: number): string {
      if (i >= BaselineCompiler.localNames.length) {
        return "l" + (i - BaselineCompiler.localNames.length);
      }
      return BaselineCompiler.localNames[i];
    }

    getLocal(i: number): string {
      return this.local[i];
    }

    emitLoadLocal(kind: Kind, i: number) {
      this.emitPush(kind, this.getLocal(i));
    }

    emitStoreLocal(kind: Kind, i: number) {
      this.emitter.writeLn(this.getLocal(i) + " = " + this.pop(kind) + ";");
    }

    peekAny(): string {
      return this.peek(Kind.Void);
    }

    peek(kind: Kind): string {
      return this.getStack(this.sp - 1);
    }

    popAny(): string {
      return this.pop(Kind.Void);
    }

    emitPopTemporaries(n: number) {
      for (var i = 0; i < n; i++) {
        this.emitter.writeLn("var t" + i + " = " + this.pop(Kind.Void) + ";");
      }
    }

    emitPushTemporary(...indices: number []) {
      for (var i = 0; i < indices.length; i++) {
       this.emitPush(Kind.Void, "t" + indices[i]);
      }
    }

    pop(kind: Kind): string {
      writer && writer.writeLn(" popping: sp: " + this.sp + " " + Kind[kind]);
      assert (this.sp, "SP below zero.");
      this.sp -= isTwoSlot(kind) ? 2 : 1;
      var v = this.getStack(this.sp);
      writer && writer.writeLn("  popped: sp: " + this.sp + " " + Kind[kind] + " " + v);
      return v;
    }

    emitPushAny(v) {
      this.emitPush(Kind.Void, v);
    }

    emitPush(kind: Kind, v) {
      writer && writer.writeLn("push: sp: " + this.sp + " " + Kind[kind] + " " + v);
      this.emitter.writeLn(this.getStack(this.sp) + " = " + v + ";");
      this.sp += isTwoSlot(kind) ? 2 : 1;
    }

    emitReturn(kind: Kind) {
      if (kind === Kind.Void) {
        this.emitter.writeLn("return;");
        return
      }
      this.emitter.writeLn("return " + this.pop(kind) + ";");
    }

    emitGetField(fieldInfo: FieldInfo, isStatic: boolean) {
      var signature = TypeDescriptor.makeTypeDescriptor(fieldInfo.signature);
      var object = isStatic ? this.runtimeClass(fieldInfo.classInfo) : this.pop(Kind.Reference);
      this.emitPush(signature.kind, object + "." + fieldInfo.mangledName);
    }

    emitPutField(fieldInfo: FieldInfo, isStatic: boolean) {
      var signature = TypeDescriptor.makeTypeDescriptor(fieldInfo.signature);
      var value = this.pop(signature.kind);
      var object = isStatic ? this.runtimeClass(fieldInfo.classInfo) : this.pop(Kind.Reference);
      this.emitter.writeLn(object + "." + fieldInfo.mangledName + " = " + value + ";");
    }

    setBlockStackHeight(pc: number, height: number) {
      writer && writer.writeLn("Setting Block Height " + pc + " " + height);
      if (this.blockStackHeightMap[pc] !== undefined) {
        assert(this.blockStackHeightMap[pc] === height, pc + " " + this.blockStackHeightMap[pc] + " " + height);
      }
      this.blockStackHeightMap[pc] = height;
    }

    emitIf(stream: BytecodeStream, predicate: string) {
      var target = stream.readBranchDest();
      var next = stream.nextBCI;
      this.emitter.writeLn("pc = " + predicate + " ? " + target + " : " + next + "; break;");
    }

    emitIfNull(stream: BytecodeStream, condition: Condition) {
      var x = this.pop(Kind.Reference);
      this.emitIf(stream, x + " " + conditionToOperator(condition) + " null");
    }

    emitIfSame(stream: BytecodeStream, kind: Kind, condition: Condition) {
      var y = this.pop(kind);
      var x = this.pop(kind);
      this.emitIf(stream, x + " " + conditionToOperator(condition) + " " + y);
    }

    emitIfZero(stream: BytecodeStream, condition: Condition) {
      var x = this.pop(Kind.Int);
      this.emitIf(stream, x + " " + conditionToOperator(condition) + " 0");
    }

    runtimeClass(classInfo: ClassInfo) {
      return "$." + mangleClass(classInfo);
    }

    emitClassInitializationCheck(classInfo: ClassInfo) {
      while (classInfo.isArrayClass) {
        classInfo = classInfo.elementClass;
      }
      if (!CLASSES.isPreInitializedClass(classInfo)) {
        this.emitter.writeLn(this.runtimeClass(classInfo) + ";");
      }
    }

    emitInvoke(methodInfo: MethodInfo, opcode: Bytecodes, nextPC: number) {
      var calleeCanYield = YieldReason.Virtual;
      if (isStaticallyBound(opcode, methodInfo)) {
        calleeCanYield = canYield(this.ctx, methodInfo);
      }
      var signature = SignatureDescriptor.makeSignatureDescriptor(methodInfo.signature);
      var types = signature.typeDescriptors;
      var args: string [] = [];
      for (var i = types.length - 1; i > 0; i--) {
        args.unshift(this.pop(types[i].kind));
      }
      var object = null, call;
      if (opcode !== Bytecodes.INVOKESTATIC) {
        object = this.pop(Kind.Reference);
        if (opcode === Bytecodes.INVOKESPECIAL) {
          args.unshift(object);
          call = mangleClassAndMethod(methodInfo) + ".call(" + args.join(", ") + ")";
        } else {
          call = object + "." + mangleMethod(methodInfo) + "(" + args.join(", ") + ")";
        }
      } else {
        this.emitClassInitializationCheck(methodInfo.classInfo);
        call = mangleClassAndMethod(methodInfo) + "(" + args.join(", ") + ")";
      }
      this.needsVariable("re");
      this.emitter.writeLn("re = " + call + ";");
      if (calleeCanYield) {
        this.emitUnwind(nextPC);
      }
      if (types[0].kind !== Kind.Void) {
        this.emitPush(types[0].kind, "re");
      }
    }

    emitStoreIndexed(kind: Kind) {
      var value = this.pop(stackKind(kind));
      var index = this.pop(Kind.Int);
      var array = this.pop(Kind.Reference);
      this.emitter.writeLn(array + "[" + index + "] = " + value + ";");
    }

    emitLoadIndexed(kind: Kind) {
      var index = this.pop(Kind.Int);
      var array = this.pop(Kind.Reference);
      this.emitPush(kind, array + "[" + index + "]");
    }

    emitIncrement(stream: BytecodeStream) {
      this.emitter.writeLn(this.getLocal(stream.readLocalIndex()) + " += " + stream.readIncrement() + ";");
    }

    emitGoto(stream: BytecodeStream) {
      var target = stream.readBranchDest();
      this.emitter.writeLn("pc = " + target + "; break;");
    }

    emitLoadConstant(cpi: number) {
      var cp = this.methodInfo.classInfo.constant_pool;
      var entry = cp[cpi];
      switch (entry.tag) {
        case TAGS.CONSTANT_Integer:
          this.emitPush(Kind.Int, entry.integer);
          return;
        case TAGS.CONSTANT_Float:
          this.emitPush(Kind.Float, doubleConstant(entry.float));
          return;
        case TAGS.CONSTANT_Double:
          this.emitPush(Kind.Double, doubleConstant(entry.double));
          return;
        case TAGS.CONSTANT_Long:
          this.emitPush(Kind.Long, "Long.fromBits(" + entry.lowBits + ", " + entry.highBits + ")");
          return;
        case TAGS.CONSTANT_String:
          entry = cp[entry.string_index];
          this.emitPush(Kind.Reference, "$S(" + J2ME.C4.AST.escapeString(entry.bytes) + ")");
          return;
        default:
          throw "Not done for: " + entry.tag;
      }
    }

    emitThrow(pc: number) {
      var object = this.peek(Kind.Reference);
      this.emitter.writeLn("throw " + object + ";");
    }

    emitNewInstance(cpi: number) {
      var classInfo = this.lookupClass(cpi);
      this.emitClassInitializationCheck(classInfo);
      this.emitPush(Kind.Reference, "new " + mangleClass(classInfo)+ "()");
    }

    emitNewTypeArray(typeCode: number) {
      var kind = arrayTypeCodeToKind(typeCode);
      var length = this.pop(Kind.Int);
      this.emitPush(Kind.Reference, "new " + kindToTypedArrayName(kind) + "(" + length + ")");
    }

    emitCheckCast(cpi: number) {
      var object = this.peek(Kind.Reference);
      var classInfo = this.lookupClass(cpi);
      var call = "$CCK";
      if (classInfo.isInterface) {
        call = "$CCI";
      }
      this.emitter.writeLn(call + "(" + object + ", " + mangleClass(classInfo) + ");");
    }

    emitInstanceOf(cpi: number) {
      var object = this.pop(Kind.Reference);
      var classInfo = this.lookupClass(cpi);
      var call = "$IOK";
      if (classInfo.isInterface) {
        call = "$IOI";
      }
      this.emitPush(Kind.Int, call + "(" + object + ", " + mangleClass(classInfo) + ") | 0");
    }

    emitArrayLength() {
      this.emitPush(Kind.Int, this.pop(Kind.Reference) + ".length");
    }

    emitNewObjectArray(cpi: number) {
      var classInfo = this.lookupClass(cpi);
      this.emitClassInitializationCheck(classInfo);
      var length = this.pop(Kind.Int);
      this.emitPush(Kind.Reference, "$NA(" + mangleClass(classInfo) + "," + length + ")");
    }

    emitUnwind(pc: number) {
      var local = this.local.join(", ");
      var stack = this.stack.slice(0, this.sp).join(", ");
      this.emitter.writeLn("if (U) { $.B(" + pc + ", [" + local + "], [" + stack + "]); return; }");
    }

    emitMonitorEnter(nextPC: number) {
      var object = this.pop(Kind.Reference);
      this.emitter.writeLn("$ME(" + object + ");");
      this.emitUnwind(nextPC);
    }

    emitMonitorExit() {
      var object = this.pop(Kind.Reference);
      this.emitter.writeLn("$MX(" + object + ");");
    }

    emitStackOp(opcode: Bytecodes) {
      switch (opcode) {
        case Bytecodes.POP: {
          this.popAny();
          break;
        }
        case Bytecodes.POP2: {
          this.popAny();
          this.popAny();
          break;
        }
        case Bytecodes.DUP: {
          this.emitPushAny(this.peekAny());
          break;
        }
        case Bytecodes.DUP_X1: {
          this.emitPopTemporaries(2);
          this.emitPushTemporary(0, 1, 0);
          break;
        }
        case Bytecodes.DUP_X2: {
          this.emitPopTemporaries(3);
          this.emitPushTemporary(0, 2, 1, 0);
          break;
        }
        case Bytecodes.DUP2: {
          this.emitPopTemporaries(2);
          this.emitPushTemporary(1, 0, 1, 0);
          break;
        }
        case Bytecodes.DUP2_X1: {
          this.emitPopTemporaries(3);
          this.emitPushTemporary(1, 0, 2, 1, 0);
          break;
        }
        case Bytecodes.DUP2_X2: {
          this.emitPopTemporaries(4);
          this.emitPushTemporary(1, 0, 3, 2, 1, 0);
          break;
        }
        case Bytecodes.SWAP: {
          this.emitPopTemporaries(2);
          this.emitPushTemporary(0, 1);
          break;
        }
        default:
          Debug.unexpected(Bytecodes[opcode]);
      }
    }
    
    emitArithmeticOp(result: Kind, opcode: Bytecodes, canTrap: boolean) {
      var y = this.pop(result);
      var x = this.pop(result);
      if (canTrap) {
        var checkName = result === Kind.Long ? "$CDZL" : "$CDZ";
        this.emitter.writeLn(checkName + "(" + y + ");");
      }
      var v;
      switch(opcode) {
        case Bytecodes.IADD: v = x + " + " + y + " | 0"; break;
        case Bytecodes.ISUB: v = x + " - " + y + " | 0"; break;
        case Bytecodes.IMUL: v = "Math.imul(" + x + ", " + y + ")"; break;
        case Bytecodes.IDIV: v = x + " / " + y + " | 0"; break;
        case Bytecodes.IREM: v = x + " % " + y; break;

        case Bytecodes.FADD: v = "Math.fround(" + x + " + " + y + ")"; break;
        case Bytecodes.FSUB: v = "Math.fround(" + x + " - " + y + ")"; break;
        case Bytecodes.FMUL: v = "Math.fround(" + x + " * " + y + ")"; break;
        case Bytecodes.FDIV: v = "Math.fround(" + x + " / " + y + ")"; break;
        case Bytecodes.FREM: v = "Math.fround(" + x + " % " + y + ")"; break;

        case Bytecodes.LADD: v = x + ".add(" + y + ")"; break;
        case Bytecodes.LSUB: v = y + ".negate().add(" + x + ")"; break;
        case Bytecodes.LMUL: v = x + ".multiply(" + y + ")"; break;
        case Bytecodes.LDIV: v = x + ".div(" + y + ")"; break;
        case Bytecodes.LREM: v = x + ".modulo(" + y + ")"; break;

        case Bytecodes.DADD: v = x + " + " + y; break;
        case Bytecodes.DSUB: v = x + " - " + y; break;
        case Bytecodes.DMUL: v = x + " * " + y; break;
        case Bytecodes.DDIV: v = x + " / " + y; break;
        case Bytecodes.DREM: v = x + " % " + y; break;
        default:
          assert(false, Bytecodes[opcode]);
      }
      this.emitPush(result, v);
    }

    emitNegateOp(kind: Kind) {
      var x = this.pop(kind);
      switch(kind) {
        case Kind.Int:
          this.emitPush(kind, "(- " + x + ")|0");
          break;
        case Kind.Long:
          this.emitPush(kind, x + ".negate()");
          break;
        case Kind.Float:
        case Kind.Double:
          this.emitPush(kind, "- " + x);
          break;
        default:
          Debug.unexpected(Kind[kind]);
      }
    }

    emitShiftOp(kind: Kind, opcode: Bytecodes) {
      var s = this.pop(Kind.Int);
      var x = this.pop(kind);
      var v;
      switch(opcode) {
        case Bytecodes.ISHL: v = x + " << " + s; break;
        case Bytecodes.ISHR: v = x + " >> " + s; break;
        case Bytecodes.IUSHR: v = x + " >>> " + s; break;

        case Bytecodes.LSHL: v = x + ".shiftLeft(" + s + ")"; break;
        case Bytecodes.LSHR: v = x + ".shiftRight(" + s + ")"; break;
        case Bytecodes.LUSHR: v = x + ".shiftRightUnsigned(" + s + ")"; break;
        default:
          Debug.unexpected(Bytecodes[opcode]);
      }
      this.emitPush(kind, v);
    }

    emitLogicOp(kind: Kind, opcode: Bytecodes) {
      var y = this.pop(kind);
      var x = this.pop(kind);
      var v;
      switch(opcode) {
        case Bytecodes.IAND: v = x + " & " + y; break;
        case Bytecodes.IOR: v = x + " | " + y; break;
        case Bytecodes.IXOR: v = x + " ^ " + y; break;

        case Bytecodes.LAND: v = x + ".and(" + y + ")"; break;
        case Bytecodes.LOR: v = x + ".or(" + y + ")"; break;
        case Bytecodes.LXOR: v = x + ".xor(" + y + ")"; break;
        default:
          Debug.unexpected(Bytecodes[opcode]);
      }
      this.emitPush(kind, v);
    }

    emitConvertOp(from: Kind, to: Kind, opcode: Bytecodes) {
      var x = this.pop(from);
      var v;
      switch (opcode) {
        case Bytecodes.I2L: v = "Long.fromInt(" + x + ")"; break;
        case Bytecodes.I2F:
        case Bytecodes.I2D: v = x; break;
        case Bytecodes.I2B: v = "(" + x + " << 24) >> 24"; break;
        case Bytecodes.I2C: v = x + " & 0xffff"; break;
        case Bytecodes.I2S: v = "(" + x + " << 16) >> 16"; break;
        case Bytecodes.L2I: v = x + ".toInt()"; break;
        case Bytecodes.L2F: v = "Math.fround(" + x + ".toNumber())"; break;
        case Bytecodes.L2D: v = x + ".toNumber()"; break;
        case Bytecodes.D2I:
        case Bytecodes.F2I: v = "util.double2int(" + x + ")"; break;
        case Bytecodes.F2L: v = "Long.fromNumber(" + x + ")"; break;
        case Bytecodes.F2D: v = x; break;
        case Bytecodes.D2L: v = "util.double2long(" + x + ")"; break;
        case Bytecodes.D2F: v = "Math.fround(" + x + ")"; break;
      }
      this.emitPush(to, v);
    }

    emitCompareOp(kind: Kind, isLessThan: boolean) {
      var y = this.pop(kind);
      var x = this.pop(kind);
      var s = this.getStack(this.sp++);
      if (kind === Kind.Long) {
        this.emitter.enter("if (" + x + ".greaterThan(" + y + ")) {");
        this.emitter.writeLn(s + " = 1");
        this.emitter.leaveAndEnter("} else if (" + x + ".lessThan(" + y + ")) {");
        this.emitter.writeLn(s + " = -1");
        this.emitter.leaveAndEnter("} else {");
        this.emitter.writeLn(s + " = 0");
        this.emitter.leave("}");
      } else {
        this.emitter.enter("if (isNaN(" + x + ") || isNaN(" + y + ")) {");
        this.emitter.writeLn(s + " = " + (isLessThan ? "-1" : "1"));
        this.emitter.leaveAndEnter("} else if (" + x + " > " + y + ") {");
        this.emitter.writeLn(s + " = 1");
        this.emitter.leaveAndEnter("} else if (" + x + " < " + y + ") {");
        this.emitter.writeLn(s + " = -1");
        this.emitter.leaveAndEnter("} else {");
        this.emitter.writeLn(s + " = 0");
        this.emitter.leave("}");
      }
    }

    emitTableSwitch(stream: BytecodeStream) {
      var tableSwitch = stream.readTableSwitch();
      var value = this.pop(Kind.Int);
      this.emitter.enter("switch(" + value + ") {");
      for (var i = 0; i < tableSwitch.numberOfCases(); i++) {
        this.emitter.writeLn("case " + tableSwitch.keyAt(i) + ": pc = " + (stream.currentBCI + tableSwitch.offsetAt(i)) + "; break;");
      }
      this.emitter.writeLn("default: pc = " + (stream.currentBCI + tableSwitch.defaultOffset()) + "; break;");
      this.emitter.leave("} break;");
    }

    emitLookupSwitch(stream: BytecodeStream) {
      var lookupSwitch = stream.readLookupSwitch();
      var value = this.pop(Kind.Int);
      this.emitter.enter("switch(" + value + ") {");
      for (var i = 0; i < lookupSwitch.numberOfCases(); i++) {
        this.emitter.writeLn("case " + lookupSwitch.keyAt(i) + ": pc = " + (stream.currentBCI + lookupSwitch.offsetAt(i)) + "; break;");
      }
      this.emitter.writeLn("default: pc = " + (stream.currentBCI + lookupSwitch.defaultOffset()) + "; break;");
      this.emitter.leave("} break;");
    }

    emitBytecode(stream: BytecodeStream, block: Block) {
      var cpi: number;
      var opcode: Bytecodes = stream.currentBC();
      writer && writer.writeLn("emit: pc: " + stream.currentBCI + ", sp: " + this.sp + " " + Bytecodes[opcode]);
      // this.emitter.writeLn("// " + stream.currentBCI + " " + Bytecodes[opcode] + ", sp: " + this.sp);

      if (block.hasHandlers && Bytecode.canTrap(opcode)) {
        this.emitter.writeLn("pc = " + this.pc + ";");
      }

      switch (opcode) {
        case Bytecodes.NOP            : break;
        case Bytecodes.ACONST_NULL    : this.emitPush(Kind.Reference, "null"); break;
        case Bytecodes.ICONST_M1      :
        case Bytecodes.ICONST_0       :
        case Bytecodes.ICONST_1       :
        case Bytecodes.ICONST_2       :
        case Bytecodes.ICONST_3       :
        case Bytecodes.ICONST_4       :
        case Bytecodes.ICONST_5       : this.emitPush(Kind.Int, opcode - Bytecodes.ICONST_0); break;
        case Bytecodes.FCONST_0       :
        case Bytecodes.FCONST_1       :
        case Bytecodes.FCONST_2       : this.emitPush(Kind.Float, opcode - Bytecodes.FCONST_0); break;
        case Bytecodes.DCONST_0       :
        case Bytecodes.DCONST_1       : this.emitPush(Kind.Double, opcode - Bytecodes.DCONST_0); break;
        case Bytecodes.LCONST_0       :
        case Bytecodes.LCONST_1       : this.emitPush(Kind.Long, longConstant(opcode - Bytecodes.LCONST_0)); break;
        case Bytecodes.BIPUSH         : this.emitPush(Kind.Int, stream.readByte()); break;
        case Bytecodes.SIPUSH         : this.emitPush(Kind.Int, stream.readShort()); break;
        case Bytecodes.LDC            :
        case Bytecodes.LDC_W          :
        case Bytecodes.LDC2_W         : this.emitLoadConstant(stream.readCPI()); break;
        case Bytecodes.ILOAD          : this.emitLoadLocal(Kind.Int, stream.readLocalIndex()); break;
        case Bytecodes.LLOAD          : this.emitLoadLocal(Kind.Long, stream.readLocalIndex()); break;
        case Bytecodes.FLOAD          : this.emitLoadLocal(Kind.Float, stream.readLocalIndex()); break;
        case Bytecodes.DLOAD          : this.emitLoadLocal(Kind.Double, stream.readLocalIndex()); break;
        case Bytecodes.ALOAD          : this.emitLoadLocal(Kind.Reference, stream.readLocalIndex()); break;
        case Bytecodes.ILOAD_0        :
        case Bytecodes.ILOAD_1        :
        case Bytecodes.ILOAD_2        :
        case Bytecodes.ILOAD_3        : this.emitLoadLocal(Kind.Int, opcode - Bytecodes.ILOAD_0); break;
        case Bytecodes.LLOAD_0        :
        case Bytecodes.LLOAD_1        :
        case Bytecodes.LLOAD_2        :
        case Bytecodes.LLOAD_3        : this.emitLoadLocal(Kind.Long, opcode - Bytecodes.LLOAD_0); break;
        case Bytecodes.FLOAD_0        :
        case Bytecodes.FLOAD_1        :
        case Bytecodes.FLOAD_2        :
        case Bytecodes.FLOAD_3        : this.emitLoadLocal(Kind.Float, opcode - Bytecodes.FLOAD_0); break;
        case Bytecodes.DLOAD_0        :
        case Bytecodes.DLOAD_1        :
        case Bytecodes.DLOAD_2        :
        case Bytecodes.DLOAD_3        : this.emitLoadLocal(Kind.Double, opcode - Bytecodes.DLOAD_0); break;
        case Bytecodes.ALOAD_0        :
        case Bytecodes.ALOAD_1        :
        case Bytecodes.ALOAD_2        :
        case Bytecodes.ALOAD_3        : this.emitLoadLocal(Kind.Reference, opcode - Bytecodes.ALOAD_0); break;
        case Bytecodes.IALOAD         : this.emitLoadIndexed(Kind.Int); break;
        case Bytecodes.LALOAD         : this.emitLoadIndexed(Kind.Long); break;
        case Bytecodes.FALOAD         : this.emitLoadIndexed(Kind.Float); break;
        case Bytecodes.DALOAD         : this.emitLoadIndexed(Kind.Double); break;
        case Bytecodes.AALOAD         : this.emitLoadIndexed(Kind.Reference); break;
        case Bytecodes.BALOAD         : this.emitLoadIndexed(Kind.Byte); break;
        case Bytecodes.CALOAD         : this.emitLoadIndexed(Kind.Char); break;
        case Bytecodes.SALOAD         : this.emitLoadIndexed(Kind.Short); break;
        case Bytecodes.ISTORE         : this.emitStoreLocal(Kind.Int, stream.readLocalIndex()); break;
        case Bytecodes.LSTORE         : this.emitStoreLocal(Kind.Long, stream.readLocalIndex()); break;
        case Bytecodes.FSTORE         : this.emitStoreLocal(Kind.Float, stream.readLocalIndex()); break;
        case Bytecodes.DSTORE         : this.emitStoreLocal(Kind.Double, stream.readLocalIndex()); break;
        case Bytecodes.ASTORE         : this.emitStoreLocal(Kind.Reference, stream.readLocalIndex()); break;
        case Bytecodes.ISTORE_0       :
        case Bytecodes.ISTORE_1       :
        case Bytecodes.ISTORE_2       :
        case Bytecodes.ISTORE_3       : this.emitStoreLocal(Kind.Int, opcode - Bytecodes.ISTORE_0); break;
        case Bytecodes.LSTORE_0       :
        case Bytecodes.LSTORE_1       :
        case Bytecodes.LSTORE_2       :
        case Bytecodes.LSTORE_3       : this.emitStoreLocal(Kind.Long, opcode - Bytecodes.LSTORE_0); break;
        case Bytecodes.FSTORE_0       :
        case Bytecodes.FSTORE_1       :
        case Bytecodes.FSTORE_2       :
        case Bytecodes.FSTORE_3       : this.emitStoreLocal(Kind.Float, opcode - Bytecodes.FSTORE_0); break;
        case Bytecodes.DSTORE_0       :
        case Bytecodes.DSTORE_1       :
        case Bytecodes.DSTORE_2       :
        case Bytecodes.DSTORE_3       : this.emitStoreLocal(Kind.Double, opcode - Bytecodes.DSTORE_0); break;
        case Bytecodes.ASTORE_0       :
        case Bytecodes.ASTORE_1       :
        case Bytecodes.ASTORE_2       :
        case Bytecodes.ASTORE_3       : this.emitStoreLocal(Kind.Reference, opcode - Bytecodes.ASTORE_0); break;

        case Bytecodes.IASTORE        : this.emitStoreIndexed(Kind.Int); break;
        case Bytecodes.LASTORE        : this.emitStoreIndexed(Kind.Long); break;
        case Bytecodes.FASTORE        : this.emitStoreIndexed(Kind.Float); break;
        case Bytecodes.DASTORE        : this.emitStoreIndexed(Kind.Double); break;
        case Bytecodes.AASTORE        : this.emitStoreIndexed(Kind.Reference); break;
        case Bytecodes.BASTORE        : this.emitStoreIndexed(Kind.Byte); break;
        case Bytecodes.CASTORE        : this.emitStoreIndexed(Kind.Char); break;
        case Bytecodes.SASTORE        : this.emitStoreIndexed(Kind.Short); break;

        case Bytecodes.POP            :
        case Bytecodes.POP2           :
        case Bytecodes.DUP            :
        case Bytecodes.DUP_X1         :
        case Bytecodes.DUP_X2         :
        case Bytecodes.DUP2           :
        case Bytecodes.DUP2_X1        :
        case Bytecodes.DUP2_X2        :
        case Bytecodes.SWAP           : this.emitStackOp(opcode); break;

        case Bytecodes.IADD           :
        case Bytecodes.ISUB           :
        case Bytecodes.IMUL           : this.emitArithmeticOp(Kind.Int, opcode, false); break;
        case Bytecodes.IDIV           :
        case Bytecodes.IREM           : this.emitArithmeticOp(Kind.Int, opcode, true); break;
        case Bytecodes.LADD           :
        case Bytecodes.LSUB           :
        case Bytecodes.LMUL           : this.emitArithmeticOp(Kind.Long, opcode, false); break;
        case Bytecodes.LDIV           :
        case Bytecodes.LREM           : this.emitArithmeticOp(Kind.Long, opcode, true); break;
        case Bytecodes.FADD           :
        case Bytecodes.FSUB           :
        case Bytecodes.FMUL           :
        case Bytecodes.FDIV           :
        case Bytecodes.FREM           : this.emitArithmeticOp(Kind.Float, opcode, false); break;
        case Bytecodes.DADD           :
        case Bytecodes.DSUB           :
        case Bytecodes.DMUL           :
        case Bytecodes.DDIV           :
        case Bytecodes.DREM           : this.emitArithmeticOp(Kind.Double, opcode, false); break;
        case Bytecodes.INEG           : this.emitNegateOp(Kind.Int); break;
        case Bytecodes.LNEG           : this.emitNegateOp(Kind.Long); break;
        case Bytecodes.FNEG           : this.emitNegateOp(Kind.Float); break;
        case Bytecodes.DNEG           : this.emitNegateOp(Kind.Double); break;
        case Bytecodes.ISHL           :
        case Bytecodes.ISHR           :
        case Bytecodes.IUSHR          : this.emitShiftOp(Kind.Int, opcode); break;
        case Bytecodes.IAND           :
        case Bytecodes.IOR            :
        case Bytecodes.IXOR           : this.emitLogicOp(Kind.Int, opcode); break;
        case Bytecodes.LSHL           :
        case Bytecodes.LSHR           :
        case Bytecodes.LUSHR          : this.emitShiftOp(Kind.Long, opcode); break;
        case Bytecodes.LAND           :
        case Bytecodes.LOR            :
        case Bytecodes.LXOR           : this.emitLogicOp(Kind.Long, opcode); break;
        case Bytecodes.IINC           : this.emitIncrement(stream); break;

        case Bytecodes.I2L            : this.emitConvertOp(Kind.Int, Kind.Long, opcode); break;
        case Bytecodes.I2F            : this.emitConvertOp(Kind.Int, Kind.Float, opcode); break;
        case Bytecodes.I2D            : this.emitConvertOp(Kind.Int, Kind.Double, opcode); break;
        case Bytecodes.I2B            : this.emitConvertOp(Kind.Int, Kind.Byte, opcode); break;
        case Bytecodes.I2C            : this.emitConvertOp(Kind.Int, Kind.Char, opcode); break;
        case Bytecodes.I2S            : this.emitConvertOp(Kind.Int, Kind.Short, opcode); break;
        case Bytecodes.L2I            : this.emitConvertOp(Kind.Long, Kind.Int, opcode); break;
        case Bytecodes.L2F            : this.emitConvertOp(Kind.Long, Kind.Float, opcode); break;
        case Bytecodes.L2D            : this.emitConvertOp(Kind.Long, Kind.Double, opcode); break;
        case Bytecodes.F2I            : this.emitConvertOp(Kind.Float, Kind.Int, opcode); break;
        case Bytecodes.F2L            : this.emitConvertOp(Kind.Float, Kind.Long, opcode); break;
        case Bytecodes.F2D            : this.emitConvertOp(Kind.Float, Kind.Double, opcode); break;
        case Bytecodes.D2I            : this.emitConvertOp(Kind.Double, Kind.Int, opcode); break;
        case Bytecodes.D2L            : this.emitConvertOp(Kind.Double, Kind.Long, opcode); break;
        case Bytecodes.D2F            : this.emitConvertOp(Kind.Double, Kind.Float, opcode); break;

        case Bytecodes.LCMP           : this.emitCompareOp(Kind.Long, false); break;
        case Bytecodes.FCMPL          : this.emitCompareOp(Kind.Float, true); break;
        case Bytecodes.FCMPG          : this.emitCompareOp(Kind.Float, false); break;
        case Bytecodes.DCMPL          : this.emitCompareOp(Kind.Double, true); break;
        case Bytecodes.DCMPG          : this.emitCompareOp(Kind.Double, false); break;
        case Bytecodes.IFEQ           : this.emitIfZero(stream, Condition.EQ); break;
        case Bytecodes.IFNE           : this.emitIfZero(stream, Condition.NE); break;
        case Bytecodes.IFLT           : this.emitIfZero(stream, Condition.LT); break;
        case Bytecodes.IFGE           : this.emitIfZero(stream, Condition.GE); break;
        case Bytecodes.IFGT           : this.emitIfZero(stream, Condition.GT); break;
        case Bytecodes.IFLE           : this.emitIfZero(stream, Condition.LE); break;
        case Bytecodes.IF_ICMPEQ      : this.emitIfSame(stream, Kind.Int, Condition.EQ); break;
        case Bytecodes.IF_ICMPNE      : this.emitIfSame(stream, Kind.Int, Condition.NE); break;
        case Bytecodes.IF_ICMPLT      : this.emitIfSame(stream, Kind.Int, Condition.LT); break;
        case Bytecodes.IF_ICMPGE      : this.emitIfSame(stream, Kind.Int, Condition.GE); break;
        case Bytecodes.IF_ICMPGT      : this.emitIfSame(stream, Kind.Int, Condition.GT); break;
        case Bytecodes.IF_ICMPLE      : this.emitIfSame(stream, Kind.Int, Condition.LE); break;
        case Bytecodes.IF_ACMPEQ      : this.emitIfSame(stream, Kind.Reference, Condition.EQ); break;
        case Bytecodes.IF_ACMPNE      : this.emitIfSame(stream, Kind.Reference, Condition.NE); break;
        case Bytecodes.GOTO           : this.emitGoto(stream); break;
        ///*
        //case Bytecodes.JSR            : genJsr(stream.readBranchDest()); break;
        //case Bytecodes.RET            : genRet(stream.readLocalIndex()); break;
        case Bytecodes.TABLESWITCH    : this.emitTableSwitch(stream); break;
        case Bytecodes.LOOKUPSWITCH   : this.emitLookupSwitch(stream); break;
        //*/
        case Bytecodes.IRETURN        : this.emitReturn(Kind.Int); break;
        case Bytecodes.LRETURN        : this.emitReturn(Kind.Long); break;
        case Bytecodes.FRETURN        : this.emitReturn(Kind.Float); break;
        case Bytecodes.DRETURN        : this.emitReturn(Kind.Double); break;
        case Bytecodes.ARETURN        : this.emitReturn(Kind.Reference); break;
        case Bytecodes.RETURN         : this.emitReturn(Kind.Void); break;
        case Bytecodes.GETSTATIC      : cpi = stream.readCPI(); this.emitGetField(this.lookupField(cpi, opcode, true), true); break;
        case Bytecodes.PUTSTATIC      : cpi = stream.readCPI(); this.emitPutField(this.lookupField(cpi, opcode, true), true); break;
        case Bytecodes.GETFIELD       : cpi = stream.readCPI(); this.emitGetField(this.lookupField(cpi, opcode, false), false); break;
        case Bytecodes.PUTFIELD       : cpi = stream.readCPI(); this.emitPutField(this.lookupField(cpi, opcode, false), false); break;
        case Bytecodes.INVOKEVIRTUAL  : cpi = stream.readCPI(); this.emitInvoke(this.lookupMethod(cpi, opcode, false), opcode, stream.nextBCI); break;
        case Bytecodes.INVOKESPECIAL  : cpi = stream.readCPI(); this.emitInvoke(this.lookupMethod(cpi, opcode, false), opcode, stream.nextBCI); break;
        case Bytecodes.INVOKESTATIC   : cpi = stream.readCPI(); this.emitInvoke(this.lookupMethod(cpi, opcode, true), opcode, stream.nextBCI); break;
        case Bytecodes.INVOKEINTERFACE: cpi = stream.readCPI(); this.emitInvoke(this.lookupMethod(cpi, opcode, false), opcode, stream.nextBCI); break;
        case Bytecodes.NEW            : this.emitNewInstance(stream.readCPI()); break;
        case Bytecodes.NEWARRAY       : this.emitNewTypeArray(stream.readLocalIndex()); break;
        case Bytecodes.ANEWARRAY      : this.emitNewObjectArray(stream.readCPI()); break;
        case Bytecodes.ARRAYLENGTH    : this.emitArrayLength(); break;
        case Bytecodes.ATHROW         : this.emitThrow(stream.currentBCI); break;
        case Bytecodes.CHECKCAST      : this.emitCheckCast(stream.readCPI()); break;
        case Bytecodes.INSTANCEOF     : this.emitInstanceOf(stream.readCPI()); break;
        ///*
        case Bytecodes.MONITORENTER   : this.emitMonitorEnter(stream.nextBCI); break;
        case Bytecodes.MONITOREXIT    : this.emitMonitorExit(); break;
        //case Bytecodes.MULTIANEWARRAY : genNewMultiArray(stream.readCPI()); break;
        //*/
        case Bytecodes.IFNULL         : this.emitIfNull(stream, Condition.EQ); break;
        case Bytecodes.IFNONNULL      : this.emitIfNull(stream, Condition.NE); break;
        ///*
        //case Bytecodes.GOTO_W         : genGoto(stream.readFarBranchDest()); break;
        //case Bytecodes.JSR_W          : genJsr(stream.readFarBranchDest()); break;
        //case Bytecodes.BREAKPOINT:
        //throw new CiBailout("concurrent setting of breakpoint");
        //default:
        //throw new CiBailout("Unsupported opcode " + opcode + " (" + nameOf(opcode) + ") [bci=" + bci + "]");
        //}
        //*/
        default:
          throw new Error("Not Implemented " + Bytecodes[opcode]);
      }
      writer && writer.writeLn("");
    }
  }
}