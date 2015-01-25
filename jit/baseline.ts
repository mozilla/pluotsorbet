module J2ME {
  import assert = Debug.assert;
  import Bytecodes = Bytecode.Bytecodes;
  import Condition = Bytecode.Condition;
  import BytecodeStream = Bytecode.BytecodeStream;

  import Block = Bytecode.Block;
  import BlockMap = Bytecode.BlockMap;
  import ExceptionBlock = Bytecode.ExceptionBlock;

  export interface Relooper {
    addBlock(text: string, branchVar?: string): number;
    addBranch(from: number, to: number, condition?: string, code?: string);
    render(entry: number): string;
    init(): string;
  }

  var writer = null; // new IndentingWriter();

  declare var Relooper;

  export var baselineCounter = null; // new Metrics.Counter(true);

  /**
   * Expressions to inline for commonly invoked methods.
   */
  var inlineMethods = {
    "java/lang/Object.<init>.()V": "undefined"
  };

  /**
   * Emits optimization results inline as comments in the generated source.
   */
  var emitDebugInfoComments = false;

  /**
   * Emits control flow and yielding assertions.
   */
  var emitCompilerAssertions = false;

  /**
   * Emits profiling code that counts the number of times a method is invoked.
   */
  var emitCallMethodCounter = false;

  /**
   * Emits profiling code that counts the number of times control flow is dispatched
   * to a basic block.
   */
  var emitCallMethodLoopCounter = false;

  /**
   * Emits array bounds checks. Although this is necessary for correctness, most
   * applications work without them.
   */
  var emitCheckArrayBounds = true;

  /**
   * Emits array store type checks. Although this is necessary for correctness,
   * most applications work without them.
   */
  var emitCheckArrayStore = true;

  export function baselineCompileMethod(methodInfo: MethodInfo, target: CompilationTarget): CompiledMethodInfo {
    var compileExceptions = true;
    var compileSynchronized = true;

    if (!compileExceptions && methodInfo.exception_table && methodInfo.exception_table.length) {
      throw new Error("Method: " + methodInfo.implKey + " has exception handlers.");
    }
    if (!compileSynchronized && methodInfo.isSynchronized) {
      throw new Error("Method: " + methodInfo.implKey + " is synchronized.");
    }
    writer && writer.writeLn("Compile: " + methodInfo.implKey);
    return new BaselineCompiler(methodInfo, target).compile();
  }

  class Emitter {
    private _buffer: string [];
    private _indent = 0;
    private _emitIndent;
    constructor(emitIndent: boolean = true) {
      this._buffer = [];
      this._emitIndent = emitIndent;
    }
    reset() {
      this._buffer.length = 0;
      this._indent = 0;
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
      if (this._emitIndent) {
        var prefix = "";
        for (var i = 0; i < this._indent; i++) {
          prefix += "  ";
        }
        s = prefix + s;
      }
      this._buffer.push(s);
    }
    writeLns(s: string) {
      var lines = s.split("\n");
      for (var i = 0; i < lines.length; i++) {
        var line = lines[i];
        if (line.length > 0) {
          this.writeLn(lines[i]);
        }
      }
    }
    writeEmitter(emitter: Emitter) {
      this._buffer.push.apply(this._buffer, emitter._buffer);
    }
    indent() {
      this._indent ++;
    }
    outdent() {
      this._indent --;
    }
    prependLn(s: string) {
      this._buffer.unshift(s);
    }
    toString(): string {
      return this._buffer.join("\n");
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
      case Condition.EQ: return "===";
      case Condition.NE: return "!==";
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

  function classConstant(classInfo: ClassInfo): string {
    // PrimitiveArrayClassInfo have custom mangledNames;
    if (classInfo instanceof PrimitiveArrayClassInfo) {
      return classInfo.mangledName;
    }
    if (classInfo.isArrayClass) {
      return "$AK(" + classConstant(classInfo.elementClass) + ")";
    }
    if (classInfo.mangledName) {
      return classInfo.mangledName;
    }
    release || assert(classInfo.mangledName);
    return classInfo.mangledName;
  }

  export class BaselineCompiler {
    sp: number;
    pc: number;

    private target: CompilationTarget;
    private bodyEmitter: Emitter;
    private blockEmitter: Emitter;
    private blockMap: BlockMap;
    private methodInfo: MethodInfo;
    private parameters: string [];
    private hasHandlers: boolean;
    private blockStackHeightMap: number [];
    private initializedClasses: any;
    private referencedClasses: ClassInfo [];
    private local: string [];
    private stack: string [];
    private variables: string [];
    private lockObject: string;
    private hasOSREntryPoint = false;
    private entryBlock: number;
    static localNames = ["a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"];

    /**
     * Make sure that none of these shadow gloal names, like "U" and "O".
     */
    static stackNames = ["A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "_O", "P", "Q", "_R", "S", "T", "_U", "V", "W", "X", "Y", "Z"];

    constructor(methodInfo: MethodInfo, target: CompilationTarget) {
      this.methodInfo = methodInfo;
      this.local = [];
      this.variables = [];
      this.pc = 0;
      this.sp = 0;
      this.stack = [];
      this.parameters = [];
      this.referencedClasses = [];
      this.initializedClasses = null;
      this.hasHandlers = !!methodInfo.exception_table.length;
      this.blockStackHeightMap = [0];
      this.bodyEmitter = new Emitter(target !== CompilationTarget.Runtime);
      this.blockEmitter = new Emitter(target !== CompilationTarget.Runtime);
      this.target = target;
    }

    compile(): CompiledMethodInfo {
      this.blockMap = new BlockMap(this.methodInfo);
      this.blockMap.build();
      Relooper.cleanup();
      Relooper.init();

      var blocks = this.blockMap.blocks;
      // Create relooper blocks ahead of time so we can add branches in one pass over the bytecode.
      for (var i = 0; i < blocks.length; i++) {
        blocks[i].relooperBlockID = Relooper.addBlock("// Block: " + blocks[i].blockID);
      }
      this.entryBlock = blocks[0].relooperBlockID;
      this.emitPrologue();
      this.emitBody();
      if (this.variables.length) {
        this.blockEmitter.prependLn("var " + this.variables.join(", ") + ";");
      }
      return new CompiledMethodInfo(this.parameters, this.bodyEmitter.toString(), this.referencedClasses, this.hasOSREntryPoint);
    }

    needsVariable(name: string) {
      if (this.variables.indexOf(name) < 0) {
        this.variables.push(name);
      }
    }

    setSuccessorsBlockStackHeight(block: Block, sp: number) {
      var successors = block.successors;
      for (var i = 0; i < successors.length; i++) {
        var successor = successors[i];
        if (successor instanceof ExceptionBlock ||
          successor.isExceptionEntry) {
          continue;
        }
        this.setBlockStackHeight(successors[i].startBci, sp);
      }
    }

    emitBody() {
      var blockMap = this.blockMap;
      writer && blockMap.trace(writer);
      var stream = new BytecodeStream(this.methodInfo.code);

      var needsTry = this.hasHandlers || this.methodInfo.isSynchronized;

      // We need a while to loop back to the top and dispatch to the appropriate exception handler.
      var needsWhile = this.hasHandlers;

      if (emitCallMethodLoopCounter) {
        this.bodyEmitter.writeLn("J2ME.baselineMethodCounter.count(\"" + this.methodInfo.implKey + "\");");
      }

      needsWhile && this.bodyEmitter.enter("while (1) {");
      needsTry && this.bodyEmitter.enter("try {");

      this.bodyEmitter.writeLn("var label = 0;");

      var blocks = blockMap.blocks;
      for (var i = 0; i < blocks.length; i++) {
        var block = blocks[i];
        if (block instanceof ExceptionBlock) {
          continue
        }
        if (block.isExceptionEntry) {
          writer && writer.writeLn("block.isExceptionEntry");
          this.setBlockStackHeight(block.startBci, 1);
        }
        this.blockEmitter.reset();
        this.emitBlockBody(stream, block);
      }

      this.bodyEmitter.writeLns(Relooper.render(this.entryBlock));

      emitCompilerAssertions && this.bodyEmitter.writeLn("J2ME.Debug.assert(false, 'Invalid PC: ' + pc)");

      if (needsTry) {
        this.bodyEmitter.leaveAndEnter("} catch (ex) {");
        this.bodyEmitter.writeLn(this.getStack(0) + " = $TE(ex);");
        this.sp = 1;
        if (this.hasHandlers) {
          var handlers = this.methodInfo.exception_table;
          for (var i = 0; i < handlers.length; i++) {
            this.emitExceptionHandler(this.bodyEmitter, handlers[i]);
          }
        }
        if (this.methodInfo.isSynchronized) {
          this.emitMonitorExit(this.bodyEmitter, this.lockObject);
        }
        this.bodyEmitter.writeLn("throw " + this.peek(Kind.Reference) + ";");
        this.bodyEmitter.leave("}");
      }
      if (needsWhile) {
        this.bodyEmitter.leave("}");
      }
    }

    private emitExceptionHandler(emitter: Emitter, handler: ExceptionHandler) {
      var check = "";
      if (handler.catch_type > 0) {
        var classInfo = this.lookupClass(handler.catch_type);
        check = "$IOK";
        if (classInfo.isInterface) {
          check = "$IOI";
        }
        check += "(" + this.peek(Kind.Reference) + ", " + classConstant(classInfo) + ")";
        check = " && " + check;
      }
      this.bodyEmitter.enter("if (pc >= " + handler.start_pc + " && pc < " + handler.end_pc + check + ") {");
      this.bodyEmitter.writeLn("pc = " + this.getBlockIndex(handler.handler_pc) + "; continue;");
      this.bodyEmitter.leave("}");
      return;
    }

    /**
     * Resets block level optimization state.
     */
    resetOptimizationState() {
      this.initializedClasses = Object.create(null);
    }

    emitBlockBody(stream: BytecodeStream, block: Block) {
      this.resetOptimizationState();
      this.sp = this.blockStackHeightMap[block.startBci];
      emitDebugInfoComments && this.blockEmitter.writeLn("// " + this.blockMap.blockToString(block));
      writer && writer.writeLn("emitBlock: " + block.startBci + " " + this.sp + " " + block.isExceptionEntry);
      release || assert(this.sp !== undefined, "Bad stack height");
      stream.setBCI(block.startBci);
      var lastSourceLocation = null;
      var lastBC: Bytecodes;
      while (stream.currentBCI <= block.endBci) {
        this.pc = stream.currentBCI;
        lastBC = stream.currentBC();
        this.emitBytecode(stream, block);
        stream.next();
      }
      if (this.sp >= 0) {
        this.setSuccessorsBlockStackHeight(block, this.sp);
        if (!Bytecode.isBlockEnd(lastBC)) { // Fallthrough.
          Relooper.addBranch(block.relooperBlockID, this.getBlock(stream.currentBCI).relooperBlockID);
        }
      } else {
        // TODO: ...
      }
      Relooper.setBlockCode(block.relooperBlockID, this.blockEmitter.toString());
    }

    private emitPrologue() {
      var local = this.local;
      var parameterLocalIndex = this.methodInfo.isStatic ? 0 : 1;

      var typeDescriptors = SignatureDescriptor.makeSignatureDescriptor(this.methodInfo.signature).typeDescriptors;

      // Skip the first typeDescriptor since it is the return type.
      for (var i = 1; i < typeDescriptors.length; i++) {
        var kind = Kind.Reference;
        if (typeDescriptors[i] instanceof AtomicTypeDescriptor) {
          kind = (<AtomicTypeDescriptor>typeDescriptors[i]).kind;
        }
        this.parameters.push(this.getLocalName(parameterLocalIndex));
        parameterLocalIndex += isTwoSlot(kind) ? 2 : 1;
      }

      var maxLocals = this.methodInfo.max_locals;
      for (var i = 0; i < maxLocals; i++) {
        local.push(this.getLocalName(i));
      }
      if (local.length) {
        this.bodyEmitter.writeLn("var " + local.join(", ") + ";");
      }
      if (!this.methodInfo.isStatic) {
        this.bodyEmitter.writeLn(this.getLocal(0) + " = this;");
      }
      var stack = this.stack;
      for (var i = 0; i < this.methodInfo.max_stack; i++) {
        stack.push(this.getStack(i));
      }
      if (stack.length) {
        this.bodyEmitter.writeLn("var " + stack.join(", ") + ";");
      }
      this.bodyEmitter.writeLn("var pc = 0;");
      if (this.hasHandlers) {
        this.bodyEmitter.writeLn("var ex;");
      }

      if (emitCallMethodCounter) {
        this.bodyEmitter.writeLn("J2ME.baselineMethodCounter.count(\"" + this.methodInfo.implKey + "\");");
      }

      this.lockObject = this.methodInfo.isSynchronized ?
        this.methodInfo.isStatic ? this.runtimeClassObject(this.methodInfo.classInfo) : this.getLocal(0)
        : "null";

      this.emitEntryPoints();
    }

    private emitEntryPoints() {
      var needsOSREntryPoint = false;
      var needsEntryDispatch = false;

      var blocks = this.blockMap.blocks;
      for (var i = 0; i < blocks.length; i++) {
        var block = blocks[i];
        if (block.isLoopHeader) {
          needsOSREntryPoint = true;
          needsEntryDispatch = true;
        }
        if (block.isExceptionEntry) {
          needsEntryDispatch = true;
        }
      }

      if (needsOSREntryPoint) {
        // Are we doing an OSR?
        this.bodyEmitter.enter("if (O) {");
        this.bodyEmitter.writeLn("var local = O.local;");

        // Restore locals.
        for (var i = 0; i < this.methodInfo.max_locals; i++) {
          this.bodyEmitter.writeLn(this.getLocal(i) + " = local[" + i + "];");
        }
        this.needsVariable("re");
        this.bodyEmitter.writeLn("pc = O.pc;");
        this.bodyEmitter.writeLn("O = null;");
        if (this.methodInfo.isSynchronized) {
          this.bodyEmitter.leaveAndEnter("} else {");
          this.emitMonitorEnter(this.bodyEmitter, 0, this.lockObject);
        }
        this.bodyEmitter.leave("}");
        this.hasOSREntryPoint = true;
      } else {
        if (this.methodInfo.isSynchronized) {
          this.emitMonitorEnter(this.bodyEmitter, 0, this.lockObject);
        }
      }

      if (needsEntryDispatch) {
        var entryBlock = Relooper.addBlock("// Entry Dispatch Block");

        // Add entry points
        var blocks = this.blockMap.blocks;
        for (var i = 0; i < blocks.length; i++) {
          var block = blocks[i];
          if (i === 0 || // First block always gets a entry point.
              block.isLoopHeader || // Loop headers need entry points so we can OSR.
              block.isExceptionEntry) {
            Relooper.addBranch(entryBlock, block.relooperBlockID, "pc === " + block.startBci);
          }
        }

        // Add invalid block.
        var osrInvalidBlock = Relooper.addBlock(emitCompilerAssertions ? "J2ME.Debug.assert(false, 'Invalid OSR PC: ' + pc)" : "");
        Relooper.addBranch(entryBlock, osrInvalidBlock);

        this.entryBlock = entryBlock;
      }
    }

    lookupClass(cpi: number): ClassInfo {
      var classInfo = this.methodInfo.classInfo.resolve(cpi, false);
      ArrayUtilities.pushUnique(this.referencedClasses, classInfo);
      return classInfo;
    }

    lookupMethod(cpi: number, opcode: Bytecodes, isStatic: boolean): MethodInfo {
      var methodInfo = this.methodInfo.classInfo.resolve(cpi, isStatic);
      ArrayUtilities.pushUnique(this.referencedClasses, methodInfo.classInfo);
      return methodInfo;
    }

    lookupField(cpi: number, opcode: Bytecodes, isStatic: boolean): FieldInfo {
      var fieldInfo = this.methodInfo.classInfo.resolve(cpi, isStatic);
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
      if (i < 0 || i >= this.local.length) {
        throw new Error("Out of bounds local read");
      }
      return this.local[i];
    }

    emitLoadLocal(kind: Kind, i: number) {
      this.emitPush(kind, this.getLocal(i));
    }

    emitStoreLocal(kind: Kind, i: number) {
      this.blockEmitter.writeLn(this.getLocal(i) + " = " + this.pop(kind) + ";");
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
        this.blockEmitter.writeLn("var t" + i + " = " + this.pop(Kind.Void) + ";");
      }
    }

    emitPushTemporary(...indices: number []) {
      for (var i = 0; i < indices.length; i++) {
       this.emitPush(Kind.Void, "t" + indices[i]);
      }
    }

    pop(kind: Kind): string {
      writer && writer.writeLn(" popping: sp: " + this.sp + " " + Kind[kind]);
      release || assert (this.sp, "SP below zero.");
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
      this.blockEmitter.writeLn(this.getStack(this.sp) + " = " + v + ";");
      this.sp += isTwoSlot(kind) ? 2 : 1;
    }

    emitReturn(kind: Kind) {
      if (this.methodInfo.isSynchronized) {
        this.emitMonitorExit(this.blockEmitter, this.lockObject);
      }
      if (kind === Kind.Void) {
        this.blockEmitter.writeLn("return;");
        return
      }
      this.blockEmitter.writeLn("return " + this.pop(kind) + ";");
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
      this.blockEmitter.writeLn(object + "." + fieldInfo.mangledName + " = " + value + ";");
    }

    setBlockStackHeight(pc: number, height: number) {
      writer && writer.writeLn("Setting Block Height " + pc + " " + height);
      if (this.blockStackHeightMap[pc] !== undefined) {
        release || assert(this.blockStackHeightMap[pc] === height, pc + " " + this.blockStackHeightMap[pc] + " " + height);
      }
      this.blockStackHeightMap[pc] = height;
    }

    emitIf(block: Block, stream: BytecodeStream, predicate: string) {
      var nextBlock = this.getBlock(stream.nextBCI);
      var targetBlock = this.getBlock(stream.readBranchDest());
      Relooper.addBranch(block.relooperBlockID, nextBlock.relooperBlockID);
      if (targetBlock !== nextBlock) {
        Relooper.addBranch(block.relooperBlockID, targetBlock.relooperBlockID, predicate);
      }
    }

    emitIfNull(block: Block, stream: BytecodeStream, condition: Condition) {
      var x = this.pop(Kind.Reference);
      this.emitIf(block, stream, x + " " + conditionToOperator(condition) + " null");
    }

    emitIfSame(block: Block, stream: BytecodeStream, kind: Kind, condition: Condition) {
      var y = this.pop(kind);
      var x = this.pop(kind);
      this.emitIf(block, stream, x + " " + conditionToOperator(condition) + " " + y);
    }

    emitIfZero(block: Block, stream: BytecodeStream, condition: Condition) {
      var x = this.pop(Kind.Int);
      this.emitIf(block, stream, x + " " + conditionToOperator(condition) + " 0");
    }

    runtimeClass(classInfo: ClassInfo) {
      return "$." + classConstant(classInfo);
    }

    runtimeClassObject(classInfo: ClassInfo) {
      return "$." + classConstant(classInfo) + ".classObject";
    }

    emitClassInitializationCheck(classInfo: ClassInfo) {
      while (classInfo.isArrayClass) {
        classInfo = classInfo.elementClass;
      }
      if (!CLASSES.isPreInitializedClass(classInfo)) {
        if (this.target === CompilationTarget.Runtime && $.initialized[classInfo.className]) {
          var message = "Optimized ClassInitializationCheck: " + classInfo.className + ", is already initialized.";
          baselineCounter && baselineCounter.count(message);
        } else if (this.initializedClasses[classInfo.className]) {
          var message = "Optimized ClassInitializationCheck: " + classInfo.className + ", block redundant.";
          emitDebugInfoComments && this.blockEmitter.writeLn("// " + message);
          baselineCounter && baselineCounter.count(message);
        } else if (classInfo === this.methodInfo.classInfo) {
          var message = "Optimized ClassInitializationCheck: " + classInfo.className + ", self access.";
          emitDebugInfoComments && this.blockEmitter.writeLn("// " + message);
          baselineCounter && baselineCounter.count(message);
        } else if (this.methodInfo.classInfo.isAssignableTo(classInfo)) {
          var message = "Optimized ClassInitializationCheck: " + classInfo.className + ", base access.";
          emitDebugInfoComments && this.blockEmitter.writeLn("// " + message);
          baselineCounter && baselineCounter.count(message);
        } else {
          baselineCounter && baselineCounter.count("ClassInitializationCheck: " + classInfo.className);
          this.blockEmitter.writeLn(this.runtimeClass(classInfo) + ";");
          if (classInfo.staticInitializer && canYield(classInfo.staticInitializer)) {
            this.emitUnwind(this.blockEmitter, this.pc, this.pc);
          } else {
            emitCompilerAssertions && this.emitNoUnwindAssertion();
          }
        }
        this.initializedClasses[classInfo.className] = true;
      }
    }

    emitInvoke(methodInfo: MethodInfo, opcode: Bytecodes, nextPC: number) {
      var calleeCanYield = YieldReason.Virtual;
      if (isStaticallyBound(opcode, methodInfo)) {
        calleeCanYield = canYield(methodInfo);
      }
      if (opcode === Bytecodes.INVOKESTATIC) {
        this.emitClassInitializationCheck(methodInfo.classInfo);
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
          call = methodInfo.mangledClassAndMethodName + ".call(" + args.join(", ") + ")";
        } else {
          call = object + "." + methodInfo.mangledName + "(" + args.join(", ") + ")";
        }
      } else {
        call = methodInfo.mangledClassAndMethodName + "(" + args.join(", ") + ")";
      }
      if (methodInfo.implKey in inlineMethods) {
        emitDebugInfoComments && this.blockEmitter.writeLn("// Inlining: " + methodInfo.implKey);
        call = inlineMethods[methodInfo.implKey];
      }
      this.needsVariable("re");
      this.blockEmitter.writeLn("re = " + call + ";");
      if (calleeCanYield) {
        this.emitUnwind(this.blockEmitter, this.pc, nextPC);
      } else {
        emitCompilerAssertions && this.emitNoUnwindAssertion();
      }
      if (types[0].kind !== Kind.Void) {
        this.emitPush(types[0].kind, "re");
      }
    }

    emitStoreIndexed(kind: Kind) {
      var value = this.pop(stackKind(kind));
      var index = this.pop(Kind.Int);
      var array = this.pop(Kind.Reference);
      emitCheckArrayBounds && this.blockEmitter.writeLn("$CAB(" + array + ", " + index + ");");
      if (kind === Kind.Reference) {
        emitCheckArrayStore && this.blockEmitter.writeLn("$CAS(" + array + ", " + value + ");");
      }
      this.blockEmitter.writeLn(array + "[" + index + "] = " + value + ";");
    }

    emitLoadIndexed(kind: Kind) {
      var index = this.pop(Kind.Int);
      var array = this.pop(Kind.Reference);
      emitCheckArrayBounds && this.blockEmitter.writeLn("$CAB(" + array + ", " + index + ");");
      this.emitPush(kind, array + "[" + index + "]");
    }

    emitIncrement(stream: BytecodeStream) {
      this.blockEmitter.writeLn(this.getLocal(stream.readLocalIndex()) + " += " + stream.readIncrement() + ";");
    }

    emitGoto(block: Block, stream: BytecodeStream) {
      var targetBCI = stream.readBranchDest();
      var targetBlock = this.getBlock(targetBCI);
      Relooper.addBranch(block.relooperBlockID, targetBlock.relooperBlockID);
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
          this.emitPush(Kind.Reference, "$S(" + StringUtilities.escapeStringLiteral(entry.bytes) + ")");
          return;
        default:
          throw "Not done for: " + entry.tag;
      }
    }

    emitThrow(pc: number) {
      var object = this.peek(Kind.Reference);
      this.blockEmitter.writeLn("throw " + object + ";");
    }

    emitNewInstance(cpi: number) {
      var classInfo = this.lookupClass(cpi);
      this.emitClassInitializationCheck(classInfo);
      this.emitPush(Kind.Reference, "new " + classConstant(classInfo)+ "()");
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
      this.blockEmitter.writeLn(call + "(" + object + ", " + classConstant(classInfo) + ");");
    }

    emitInstanceOf(cpi: number) {
      var object = this.pop(Kind.Reference);
      var classInfo = this.lookupClass(cpi);
      var call = "$IOK";
      if (classInfo.isInterface) {
        call = "$IOI";
      }
      this.emitPush(Kind.Int, call + "(" + object + ", " + classConstant(classInfo) + ") | 0");
    }

    emitArrayLength() {
      this.emitPush(Kind.Int, this.pop(Kind.Reference) + ".length");
    }

    emitNewObjectArray(cpi: number) {
      var classInfo = this.lookupClass(cpi);
      this.emitClassInitializationCheck(classInfo);
      var length = this.pop(Kind.Int);
      this.emitPush(Kind.Reference, "$NA(" + classConstant(classInfo) + ", " + length + ")");
    }

    private emitUnwind(emitter: Emitter, pc: number, nextPC: number) {
      var local = this.local.join(", ");
      var stack = this.stack.slice(0, this.sp).join(", ");
      emitter.writeLn("if (U) { $.B(" + pc + ", " + nextPC + ", [" + local + "], [" + stack + "], " + this.lockObject + "); return; }");
      baselineCounter && baselineCounter.count("emitUnwind");
    }

    emitNoUnwindAssertion() {
      this.blockEmitter.writeLn("if (U) { J2ME.Debug.assert(false, 'Unexpected unwind.'); }");
    }

    private emitMonitorEnter(emitter: Emitter, nextPC: number, object: string) {
      emitter.writeLn("$ME(" + object + ");");
      this.emitUnwind(emitter, this.pc, nextPC);
    }

    private emitMonitorExit(emitter: Emitter, object: string) {
      emitter.writeLn("$MX(" + object + ");");
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
        this.blockEmitter.writeLn(checkName + "(" + y + ");");
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
          release || assert(false, Bytecodes[opcode]);
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
        this.blockEmitter.enter("if (" + x + ".greaterThan(" + y + ")) {");
        this.blockEmitter.writeLn(s + " = 1");
        this.blockEmitter.leaveAndEnter("} else if (" + x + ".lessThan(" + y + ")) {");
        this.blockEmitter.writeLn(s + " = -1");
        this.blockEmitter.leaveAndEnter("} else {");
        this.blockEmitter.writeLn(s + " = 0");
        this.blockEmitter.leave("}");
      } else {
        this.blockEmitter.enter("if (isNaN(" + x + ") || isNaN(" + y + ")) {");
        this.blockEmitter.writeLn(s + " = " + (isLessThan ? "-1" : "1"));
        this.blockEmitter.leaveAndEnter("} else if (" + x + " > " + y + ") {");
        this.blockEmitter.writeLn(s + " = 1");
        this.blockEmitter.leaveAndEnter("} else if (" + x + " < " + y + ") {");
        this.blockEmitter.writeLn(s + " = -1");
        this.blockEmitter.leaveAndEnter("} else {");
        this.blockEmitter.writeLn(s + " = 0");
        this.blockEmitter.leave("}");
      }
    }

    getBlockIndex(pc: number): number {
      return pc;
      // return this.getBlock(pc).blockID;
    }

    getBlock(pc: number): Block {
      return this.blockMap.getBlock(pc);
    }

    emitTableSwitch(block: Block, stream: BytecodeStream) {
      var tableSwitch = stream.readTableSwitch();
      var value = this.pop(Kind.Int);

      var targets = {};
      var defaultTarget = this.getBlock(stream.currentBCI + tableSwitch.defaultOffset()).relooperBlockID;
      for (var i = 0; i < tableSwitch.numberOfCases(); i++) {
        var key = tableSwitch.keyAt(i);
        var target: any = this.getBlock(stream.currentBCI + tableSwitch.offsetAt(i)).relooperBlockID;
        if (target === defaultTarget) {
          continue;
        }
        if (!targets[target]) {
          targets[target] = [];
        }
        targets[target].push(value + " === " + key);
      }
      for (var target in targets) {
        var condition = targets[target].join(" || ");
        Relooper.addBranch(block.relooperBlockID, target, condition);
      }
      Relooper.addBranch(block.relooperBlockID, defaultTarget);
    }

    emitLookupSwitch(block: Block, stream: BytecodeStream) {
      var lookupSwitch = stream.readLookupSwitch();
      var value = this.pop(Kind.Int);
      var targets = {};
      var defaultTarget = this.getBlock(stream.currentBCI + lookupSwitch.defaultOffset()).relooperBlockID;
      for (var i = 0; i < lookupSwitch.numberOfCases(); i++) {
        var key = lookupSwitch.keyAt(i);
        var target: any = this.getBlock(stream.currentBCI + lookupSwitch.offsetAt(i)).relooperBlockID;
        if (target === defaultTarget) {
          continue;
        }
        if (!targets[target]) {
          targets[target] = [];
        }
        targets[target].push(value + " === " + key);
      }
      for (var target in targets) {
        var condition = targets[target].join(" || ");
        Relooper.addBranch(block.relooperBlockID, target, condition);
      }
      Relooper.addBranch(block.relooperBlockID, defaultTarget);
    }

    emitBytecode(stream: BytecodeStream, block: Block) {
      var cpi: number;
      var opcode: Bytecodes = stream.currentBC();
      writer && writer.writeLn("emit: pc: " + stream.currentBCI + ", sp: " + this.sp + " " + Bytecodes[opcode]);
      if ((block.isExceptionEntry || block.hasHandlers) && Bytecode.canTrap(opcode)) {
        // This needs to update the PC not the BI.
        this.blockEmitter.writeLn("pc = " + this.pc + ";");
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
        case Bytecodes.IFEQ           : this.emitIfZero(block, stream, Condition.EQ); break;
        case Bytecodes.IFNE           : this.emitIfZero(block, stream, Condition.NE); break;
        case Bytecodes.IFLT           : this.emitIfZero(block, stream, Condition.LT); break;
        case Bytecodes.IFGE           : this.emitIfZero(block, stream, Condition.GE); break;
        case Bytecodes.IFGT           : this.emitIfZero(block, stream, Condition.GT); break;
        case Bytecodes.IFLE           : this.emitIfZero(block, stream, Condition.LE); break;
        case Bytecodes.IF_ICMPEQ      : this.emitIfSame(block, stream, Kind.Int, Condition.EQ); break;
        case Bytecodes.IF_ICMPNE      : this.emitIfSame(block, stream, Kind.Int, Condition.NE); break;
        case Bytecodes.IF_ICMPLT      : this.emitIfSame(block, stream, Kind.Int, Condition.LT); break;
        case Bytecodes.IF_ICMPGE      : this.emitIfSame(block, stream, Kind.Int, Condition.GE); break;
        case Bytecodes.IF_ICMPGT      : this.emitIfSame(block, stream, Kind.Int, Condition.GT); break;
        case Bytecodes.IF_ICMPLE      : this.emitIfSame(block, stream, Kind.Int, Condition.LE); break;
        case Bytecodes.IF_ACMPEQ      : this.emitIfSame(block, stream, Kind.Reference, Condition.EQ); break;
        case Bytecodes.IF_ACMPNE      : this.emitIfSame(block, stream, Kind.Reference, Condition.NE); break;
        case Bytecodes.GOTO           : this.emitGoto(block, stream); break;
        case Bytecodes.TABLESWITCH    : this.emitTableSwitch(block, stream); break;
        case Bytecodes.LOOKUPSWITCH   : this.emitLookupSwitch(block, stream); break;
        case Bytecodes.IRETURN        : this.emitReturn(Kind.Int); break;
        case Bytecodes.LRETURN        : this.emitReturn(Kind.Long); break;
        case Bytecodes.FRETURN        : this.emitReturn(Kind.Float); break;
        case Bytecodes.DRETURN        : this.emitReturn(Kind.Double); break;
        case Bytecodes.ARETURN        : this.emitReturn(Kind.Reference); break;
        case Bytecodes.RETURN         : this.emitReturn(Kind.Void); break;
        case Bytecodes.GETSTATIC      : cpi = stream.readCPI(); this.emitGetField(this.lookupField(cpi, opcode, true), true); break;
        case Bytecodes.PUTSTATIC      : cpi = stream.readCPI(); this.emitPutField(this.lookupField(cpi, opcode, true), true); break;
        case Bytecodes.RESOLVED_GETFIELD      : opcode = Bytecodes.GETFIELD;
        case Bytecodes.GETFIELD               : cpi = stream.readCPI(); this.emitGetField(this.lookupField(cpi, opcode, false), false); break;
        case Bytecodes.RESOLVED_PUTFIELD      : opcode = Bytecodes.PUTFIELD;
        case Bytecodes.PUTFIELD               : cpi = stream.readCPI(); this.emitPutField(this.lookupField(cpi, opcode, false), false); break;
        case Bytecodes.RESOLVED_INVOKEVIRTUAL : opcode = Bytecodes.INVOKEVIRTUAL;
        case Bytecodes.INVOKEVIRTUAL          : cpi = stream.readCPI(); this.emitInvoke(this.lookupMethod(cpi, opcode, false), opcode, stream.nextBCI); break;
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
        case Bytecodes.MONITORENTER   : this.emitMonitorEnter(this.blockEmitter, stream.nextBCI, this.pop(Kind.Reference)); break;
        case Bytecodes.MONITOREXIT    : this.emitMonitorExit(this.blockEmitter, this.pop(Kind.Reference)); break;
        case Bytecodes.IFNULL         : this.emitIfNull(block, stream, Condition.EQ); break;
        case Bytecodes.IFNONNULL      : this.emitIfNull(block, stream, Condition.NE); break;
        // The following bytecodes are not supported yet and are not frequently used.
        // case Bytecodes.JSR            : ... break;
        // case Bytecodes.RET            : ... break;
        // case Bytecodes.MULTIANEWARRAY : ... break;
        default:
          throw new Error("Not Implemented " + Bytecodes[opcode]);
      }
      writer && writer.writeLn("");
    }
  }
}