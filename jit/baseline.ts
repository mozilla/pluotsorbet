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
   * The preemption check should be quick. We don't always want to measure
   * time so we use a quick counter and mask to determine when to do the
   * more expensive preemption check.
   */
  var preemptionSampleMask = 0xFF;

  /**
   * Expressions to inline for commonly invoked methods.
   */
  var inlineMethods = {
    "java/lang/Object.<init>.()V": "undefined"
  };

  /**
   * These methods have special powers. Methods are added to this set based on the regexp patterns in |privilegedPatterns|.
   */
  var privilegedMethods = {};

  var privilegedPatterns = [
    "com/sun/midp/crypto/SHA*",
    "java/io/DataInputStream*",
    "org/mozilla/internal/Sys*",
  ];

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
  export var emitCheckArrayBounds = true;

  /**
   * Emits null checks. Although this is necessary for correctness, most
   * applications work without them.
   */
  export var emitCheckNull = true;

  /**
   * Inline calls to runtime methods whenever possible.
   */
  export var inlineRuntimeCalls = true;

  /**
   * Emits array store type checks. Although this is necessary for correctness,
   * most applications work without them.
   */
  export var emitCheckArrayStore = true;

  /**
   * Unsafe methods.
   */
  function isPrivileged(methodInfo: MethodInfo) {
    var privileged = privilegedMethods[methodInfo.implKey];
    if (privileged) {
      return true;
    } else if (privileged === false) {
      return false;
    }
    // Check patterns.
    for (var i = 0; i < privilegedPatterns.length; i++) {
      if (methodInfo.implKey.match(privilegedPatterns[i])) {
        return privilegedMethods[methodInfo.implKey] = true;
      }
    }
    return privilegedMethods[methodInfo.implKey] = false;
  }

  /**
   * Emits preemption checks for methods that already yield.
   */
  export var emitCheckPreemption = false;

  export function baselineCompileMethod(methodInfo: MethodInfo, target: CompilationTarget): CompiledMethodInfo {
    var compileExceptions = true;
    var compileSynchronized = true;

    if (!compileExceptions && methodInfo.exception_table_length) {
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
    constructor(emitIndent: boolean) {
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
    writeLns(lines: string []) {
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
    copyLines(): string [] {
      return this._buffer.slice();
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
        return "Int8Array";
      case Kind.Boolean:
        return "Uint8Array";
      case Kind.Float:
        return "Float32Array";
      case Kind.Long:
        return "Array";
      case Kind.Double:
        return"Float64Array";
      default:
        throw Debug.unexpected(getKindName(kind));
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
        Debug.unexpected((<any>Bytecode).Condition[condition]);
    }
  }

  function doubleConstant(v): string {
    // Check for -0 floats.
    if ((1 / v) < 0) {
      return "-" + Math.abs(v);
    }
    return String(v);
  }

  function throwCompilerError(message: string) {
    throw new Error(message);
  }

  export class BaselineCompiler {
    sp: number;
    pc: number;

    private target: CompilationTarget;
    private bodyEmitter: Emitter;
    private blockEmitter: Emitter;
    private blockBodies: string [][];
    private blockMap: BlockMap;
    private methodInfo: MethodInfo;
    private parameters: string [];
    private hasHandlers: boolean;
    private hasMonitorEnter: boolean;
    private blockStackHeightMap: number [];
    private initializedClasses: any;
    private referencedClasses: ClassInfo [];
    private variables: any;
    private lockObject: string;
    private hasOSREntryPoint = false;
    private entryBlock: number;
    private isPrivileged: boolean;

    static localNames = ["a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"];

    /**
     * Make sure that none of these shadow global names, like "U" and "O".
     */
    static stackNames = ["A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "_O", "P", "Q", "R", "S", "T", "_U", "V", "W", "X", "Y", "Z"];

    /**
     * Indicates whether a unwind throw was emitted.
     */
    private hasUnwindThrow: boolean;

    constructor(methodInfo: MethodInfo, target: CompilationTarget) {
      this.blockBodies = [];
      this.methodInfo = methodInfo;
      this.variables = {};
      this.pc = 0;
      this.sp = 0;
      this.parameters = [];
      this.referencedClasses = [this.methodInfo.classInfo];
      this.initializedClasses = null;
      this.hasHandlers = methodInfo.exception_table_length > 0;
      this.hasMonitorEnter = false;
      this.blockStackHeightMap = [0];
      this.bodyEmitter = new Emitter(!release);
      this.blockEmitter = new Emitter(!release);
      this.target = target;
      this.hasUnwindThrow = false;
      this.isPrivileged = isPrivileged(this.methodInfo);
    }

    compile(): CompiledMethodInfo {
      var s;
      s = performance.now();
      this.blockMap = new BlockMap(this.methodInfo);
      this.blockMap.build();
      baselineCounter && baselineCounter.count("Create BlockMap", 1, performance.now() - s);
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

      var variables = [];
      for (var k in this.variables) {
        if (this.variables[k] !== undefined) {
          variables.push(k + "=" + this.variables[k]);
        } else {
          variables.push(k);
        }
      }
      if (variables.length > 0) {
        this.bodyEmitter.prependLn("var " + variables.join(",") + ";");
      }
      if (this.hasMonitorEnter) {
        this.bodyEmitter.prependLn("var th=$.ctx.threadAddress;");
      }

      // All methods get passed in a |self| address. For static methods this parameter is always null but we still
      // need it in front.
      if (this.methodInfo.isStatic) {
        this.parameters.unshift("self");
      }

      return new CompiledMethodInfo(this.parameters, this.bodyEmitter.toString(), this.referencedClasses, this.hasOSREntryPoint ? this.blockMap.getOSREntryPoints() : []);
    }

    needsVariable(name: string, value?: string) {
      this.variables[name] = value;
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
      var stream = new BytecodeStream(this.methodInfo.codeAttribute.code);

      var needsTry = this.hasHandlers || this.methodInfo.isSynchronized;

      // We need a while to loop back to the top and dispatch to the appropriate exception handler.
      var needsWhile = this.hasHandlers;

      if (emitCallMethodLoopCounter) {
        this.bodyEmitter.writeLn("J2ME.baselineMethodCounter.count(\"" + this.methodInfo.implKey + "\");");
      }

      var s = performance.now();
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
      baselineCounter && baselineCounter.count("Emit Blocks", 1, performance.now() - s);
      if (this.hasUnwindThrow) {
        needsTry = true;
      }

      needsWhile && this.bodyEmitter.enter("while(1){");
      needsTry && this.bodyEmitter.enter("try{");
      this.bodyEmitter.writeLn("var label=0;");

      // Fill scaffolding with block bodies.
      s = performance.now();
      var scaffolding = Relooper.render(this.entryBlock).split("\n");
      baselineCounter && baselineCounter.count("Relooper", 1, performance.now() - s);
      for (var i = 0; i < scaffolding.length; i++) {
        var line = scaffolding[i];
        if (line.length > 0 && line[0] === "@") {
          this.bodyEmitter.writeLns(this.blockBodies[line.substring(1) | 0]);
        } else {
          this.bodyEmitter.writeLn(scaffolding[i]);
        }
      }

      emitCompilerAssertions && this.bodyEmitter.writeLn("J2ME.Debug.assert(false, 'Invalid PC: ' + pc)");

      if (needsTry) {
        this.bodyEmitter.leaveAndEnter("}catch(ex){");
        if (this.hasUnwindThrow) {
          this.emitBailout(this.bodyEmitter, "ex.getPC()", "ex.getSP()", this.sp);
        }
        this.bodyEmitter.writeLn(this.getStackName(0) + "=TE(ex)._address;");
        this.sp = 1;
        if (this.hasHandlers) {
          for (var i = 0; i < this.methodInfo.exception_table_length; i++) {
            this.emitExceptionHandler(this.bodyEmitter, this.methodInfo.getExceptionEntryViewByIndex(i));
          }
        }
        if (this.methodInfo.isSynchronized) {
          this.emitMonitorExit(this.bodyEmitter, this.lockObject);
        }
        this.bodyEmitter.writeLn("throw GH(" + this.peek(Kind.Reference) + ");");
        this.bodyEmitter.leave("}");
      }
      if (needsWhile) {
        this.bodyEmitter.leave("}");
      }
    }

    private emitExceptionHandler(emitter: Emitter, handler: ExceptionEntryView) {
      var check = "";
      if (handler.catch_type > 0) {
        var classInfo = this.lookupClass(handler.catch_type);
        check = "IOK";
        if (classInfo.isInterface) {
          check = "IOI";
        }
        check += "(" + this.peek(Kind.Reference) + "," + this.classInfoSymbol(classInfo) + ")";
        check = "&&" + check;
      }
      this.bodyEmitter.writeLn("if(pc>=" + handler.start_pc + "&&pc<" + handler.end_pc + check + "){pc=" + this.getBlockIndex(handler.handler_pc) + ";continue;}");
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
      // Instead of setting the relooper block code to the generated source,
      // we set it to the block ID which we later replace with the source.
      // This is done to avoid joining and serializing strings to the asm.js
      // heap for use in the relooper, and then converting them back to JS
      // strings later.
      Relooper.setBlockCode(block.relooperBlockID, "@" + block.blockID);
      this.blockBodies[block.blockID] = this.blockEmitter.copyLines();
    }

    private emitPrologue() {
      var signatureKinds = this.methodInfo.signatureKinds;
      var parameterLocalIndex = 0;

      // For virtual methods, the first parameter is the self address.
      if (!this.methodInfo.isStatic) {
        this.parameters.push(this.getLocalName(parameterLocalIndex++));
      }

      // Skip the first typeDescriptor since it is the return type.
      for (var i = 1; i < signatureKinds.length; i++) {
        var kind = signatureKinds[i];
        this.parameters.push(this.getLocalName(parameterLocalIndex++));
        if (isTwoSlot(kind)) {
          this.parameters.push(this.getLocalName(parameterLocalIndex++));
        }
      }

      var maxLocals = this.methodInfo.codeAttribute.max_locals;
      var nonParameterLocals = [];
      for (var i = parameterLocalIndex; i < maxLocals; i++) {
        nonParameterLocals.push(this.getLocalName(i));
      }
      if (nonParameterLocals.length) {
        this.bodyEmitter.writeLn("var " + nonParameterLocals.map(x => x + "=0").join(",") + ";");
      }
      if (!this.methodInfo.isStatic) {
        this.bodyEmitter.writeLn("var self="+ this.getLocal(0) + ";");
      }
      var maxStack = this.methodInfo.codeAttribute.max_stack;
      if (maxStack) {
        var stack = [];
        for (var i = 0; i < maxStack; i++) {
          stack.push(this.getStackName(i));
        }
        this.bodyEmitter.writeLn("var " + stack.map(x => x + "=0").join(",") + ";");
      }
      this.bodyEmitter.writeLn("var pc=0;");
      if (this.hasHandlers) {
        this.bodyEmitter.writeLn("var ex;");
      }

      if (emitCallMethodCounter) {
        this.bodyEmitter.writeLn("J2ME.baselineMethodCounter.count(\"" + this.methodInfo.implKey + "\");");
      }

      this.lockObject = this.methodInfo.isSynchronized ?
        this.methodInfo.isStatic ? this.runtimeClassObject(this.methodInfo.classInfo) : "self"
        : "null";

      this.emitEntryPoints();
    }

    private emitEntryPoints() {
      var needsOSREntryPoint = false;
      var needsEntryDispatch = false;

      var blockMap = this.blockMap;
      var blocks = blockMap.blocks;
      for (var i = 0; i < blocks.length; i++) {
        var block = blocks[i];
        if (block.isLoopHeader && !block.isInnerLoopHeader()) {
          needsOSREntryPoint = true;
          needsEntryDispatch = true;
        }
        if (block.isExceptionEntry) {
          needsEntryDispatch = true;
        }
      }

      if (needsOSREntryPoint) {
        // Are we doing an OSR?
        this.bodyEmitter.enter("if(O){");
        this.bodyEmitter.writeLn("var nt=$.ctx.nativeThread;");
        this.bodyEmitter.writeLn("var fp=nt.fp;");
        this.bodyEmitter.writeLn("var lp=fp-" + this.methodInfo.codeAttribute.max_locals +";");

        // Restore locals.
        var restoreLocals = [];
        for (var i = 0; i < this.methodInfo.codeAttribute.max_locals; i++) {
          restoreLocals.push(this.getLocal(i) + "=i32[lp+" + i + "]");
        }
        this.bodyEmitter.writeLn(restoreLocals.join(",") + ";");
        this.needsVariable("re");
        if (!this.methodInfo.isStatic) {
          this.bodyEmitter.writeLn("self=i32[fp+" + FrameLayout.MonitorOffset + "]");
        }
        this.bodyEmitter.writeLn("pc=nt.pc;");
        this.bodyEmitter.writeLn("nt.popFrame(O);");
        this.bodyEmitter.writeLn("nt.pushMarkerFrame(" + FrameType.Native + ");");
        this.bodyEmitter.writeLn("O=null;");
        if (this.methodInfo.isSynchronized) {
          this.bodyEmitter.leaveAndEnter("}else{");
          this.emitMonitorEnter(this.bodyEmitter, 0, this.lockObject);
        }
        this.bodyEmitter.leave("}");
        this.hasOSREntryPoint = true;
      } else {
        if (this.methodInfo.isSynchronized) {
          this.emitMonitorEnter(this.bodyEmitter, 0, this.lockObject);
        }
      }

      // Insert a preemption check after the OSR code so the pc
      // and state will be stored. We can only do this if the
      // method has the necessary unwinding code.
      if (canYield(this.methodInfo)) {
        this.emitPreemptionCheck(this.bodyEmitter);
      }

      if (needsEntryDispatch) {
        var entryBlock = Relooper.addBlock("// Entry Dispatch Block");

        // Add entry points
        var blocks = this.blockMap.blocks;
        for (var i = 0; i < blocks.length; i++) {
          var block = blocks[i];
          if (i === 0 || // First block always gets a entry point.
              (block.isLoopHeader && !block.isInnerLoopHeader()) || // Outer loop headers need entry points so we can OSR.
              block.isExceptionEntry) {
            Relooper.addBranch(entryBlock, block.relooperBlockID, "pc===" + block.startBci);
          }
        }

        // Add invalid block.
        var osrInvalidBlock = Relooper.addBlock(emitCompilerAssertions ? "J2ME.Debug.assert(false, 'Invalid OSR PC: ' + pc)" : "");
        Relooper.addBranch(entryBlock, osrInvalidBlock);

        this.entryBlock = entryBlock;
      }
    }

    lookupClass(cpi: number): ClassInfo {
      var classInfo = this.methodInfo.classInfo.constantPool.resolveClass(cpi);
      ArrayUtilities.pushUnique(this.referencedClasses, classInfo);
      return classInfo;
    }

    classInfoSymbol(classInfo: ClassInfo): string {
      var id = this.referencedClasses.indexOf(classInfo);
      assert(id >= 0, "Class info not found in the referencedClasses list.");
      return classInfoSymbolPrefix + id;
    }

    classInfoObject(classInfo: ClassInfo): string {
      return "CI[" + this.classInfoSymbol(classInfo) + "]";
    }

    lookupMethod(cpi: number, opcode: Bytecodes, isStatic: boolean): MethodInfo {
      var methodInfo = this.methodInfo.classInfo.constantPool.resolveMethod(cpi, isStatic);
      ArrayUtilities.pushUnique(this.referencedClasses, methodInfo.classInfo);
      return methodInfo;
    }

    methodInfoSymbol(methodInfo: MethodInfo): string {
      var id = this.referencedClasses.indexOf(methodInfo.classInfo);
      assert(id >= 0, "Class info not found in the referencedClasses list.");
      return methodInfoSymbolPrefix + id + "_" + methodInfo.index;
    }

    lookupField(cpi: number, opcode: Bytecodes, isStatic: boolean): FieldInfo {
      var fieldInfo = this.methodInfo.classInfo.constantPool.resolveField(cpi, isStatic);
      ArrayUtilities.pushUnique(this.referencedClasses, fieldInfo.classInfo);
      return fieldInfo;
    }

    getStackName(i: number): string {
      if (i >= BaselineCompiler.stackNames.length) {
        return "s" + (i - BaselineCompiler.stackNames.length);
      }
      return BaselineCompiler.stackNames[i];
    }

    getStack(i: number): string {
      return this.getStackName(i);
    }

    getLocalName(i: number): string {
      if (i >= BaselineCompiler.localNames.length) {
        return "l" + (i - BaselineCompiler.localNames.length);
      }
      return BaselineCompiler.localNames[i];
    }

    getLocal(i: number): string {
      if (i < 0 || i >= this.methodInfo.codeAttribute.max_locals) {
        throw new Error("Out of bounds local read");
      }
      return this.getLocalName(i);
    }

    emitLoadLocal(kind: Kind, i: number) {
      this.emitPush(kind, this.getLocal(i));
      if (isTwoSlot(kind)) {
        this.emitPush(kind, this.getLocal(i + 1));
      }
    }

    emitStoreLocal(kind: Kind, i: number) {
      if (isTwoSlot(kind)) {
        this.blockEmitter.writeLn(this.getLocal(i + 1) + "=" + this.pop(Kind.High) + ";");
      }
      this.blockEmitter.writeLn(this.getLocal(i) + "=" + this.pop(kind) + ";");
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
        this.blockEmitter.writeLn("var t" + i + "=" + this.pop(Kind.Void) + ";");
      }
    }

    emitPushTemporary(...indices: number []) {
      for (var i = 0; i < indices.length; i++) {
        this.emitPush(Kind.Void, "t" + indices[i]);
      }
    }

    pop(kind: J2ME.Kind): string {
      release || assert (this.sp, "SP should not be less than zero, popping: " + getKindName(kind));
      this.sp --;
      return this.getStack(this.sp);
    }

    popSlot() {
      return this.pop(Kind.Int);
    }

    emitPushAny(v) {
      this.emitPush(Kind.Void, v);
    }

    emitPushBits(kind: Kind, v: number) {
      release || assert((v | 0) === v, "(v | 0) === v");
      if (v < 0) {
        this.emitPush(kind, "-" + Math.abs(v));
      } else {
        this.emitPush(kind, String(v));
      }
    }

    emitPushInt(v: number) {
      release || assert((v | 0) === v, "(v | 0) === v");
      this.emitPushBits(Kind.Int, v);
    }

    emitPushFloat(v: number) {
      aliasedF32[0] = v;
      this.emitPushBits(Kind.Float, aliasedI32[0]);
    }

    emitPushDouble(v: number) {
      aliasedF64[0] = v;
      this.emitPushBits(Kind.Double, aliasedI32[0]);
      this.emitPushBits(Kind.High, aliasedI32[1]);
    }

    emitPushLongBits(l: number, h: number) {
      this.emitPushBits(Kind.Long, l);
      this.emitPushBits(Kind.High, h);
    }

    emitPush(kind: Kind, v: string) {
      this.blockEmitter.writeLn(this.getStackName(this.sp) + "=" + v + ";");
      this.sp ++;
    }

    emitReturn(kind: Kind) {
      if (this.methodInfo.isSynchronized) {
        this.emitMonitorExit(this.blockEmitter, this.lockObject);
      }
      if (kind === Kind.Void) {
        this.blockEmitter.writeLn("return;");
        return
      }
      if (isTwoSlot(kind)) {
        var h = this.pop(kind);
        var l = this.pop(kind);
        this.blockEmitter.writeLn("tempReturn0=" + h + ";");
        this.blockEmitter.writeLn("return " + l + ";");
      } else {
        this.blockEmitter.writeLn("return " + this.pop(kind) + ";");
      }
    }

    emitGetField(fieldInfo: FieldInfo, isStatic: boolean) {
      if (isStatic) {
        this.emitClassInitializationCheck(fieldInfo.classInfo);
      }
      var kind = getSignatureKind(fieldInfo.utf8Signature);
      var object = isStatic ? this.runtimeClass(fieldInfo.classInfo) : this.pop(Kind.Reference);
      if (!isStatic) {
        this.emitNullCheck(object);
      }
      var address = object + "+" + fieldInfo.byteOffset;
      if (isTwoSlot(kind)) {
        this.needsVariable("ea");
        this.blockEmitter.writeLn("ea=" + address + ";");
        this.emitPush(kind, "i32[ea>>2]");
        this.emitPush(Kind.High, "i32[ea+4>>2]");
      } else {
        this.emitPush(kind, "i32[" + address + ">>2]");
      }
    }

    emitPutField(fieldInfo: FieldInfo, isStatic: boolean) {
      if (isStatic) {
        this.emitClassInitializationCheck(fieldInfo.classInfo);
      }
      var kind = getSignatureKind(fieldInfo.utf8Signature);
      var l, h;
      if (isTwoSlot(kind)) {
        h = this.pop(Kind.High);
        l = this.pop(kind);
      } else {
        l = this.pop(kind);
      }
      var object = isStatic ? this.runtimeClass(fieldInfo.classInfo) : this.pop(Kind.Reference);
      if (!isStatic) {
        this.emitNullCheck(object);
      }
      var address = object + "+" + fieldInfo.byteOffset;
      if (isTwoSlot(kind)) {
        this.needsVariable("ea");
        this.blockEmitter.writeLn("ea=" + address + ";");
        this.blockEmitter.writeLn("i32[ea>>2]=" + l + ";");
        this.blockEmitter.writeLn("i32[ea+4>>2]=" + h + ";");
      } else {
        this.blockEmitter.writeLn("i32[" + address + ">>2]=" + l + ";");
      }
    }

    setBlockStackHeight(pc: number, height: number) {
      writer && writer.writeLn("Setting Block Height " + pc + " " + height);
      if (this.blockStackHeightMap[pc] !== undefined) {
        release || assert(this.blockStackHeightMap[pc] === height, "Bad block height: " + pc + " " + this.blockStackHeightMap[pc] + " " + height);
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
      this.emitIf(block, stream, x + conditionToOperator(condition) + String(Constants.NULL));
    }

    emitIfSame(block: Block, stream: BytecodeStream, kind: Kind, condition: Condition) {
      var y = this.pop(kind);
      var x = this.pop(kind);
      this.emitIf(block, stream, x + conditionToOperator(condition) + y);
    }

    emitIfZero(block: Block, stream: BytecodeStream, condition: Condition) {
      var x = this.pop(Kind.Int);
      this.emitIf(block, stream, x + conditionToOperator(condition) + "0");
    }

    runtimeClass(classInfo: ClassInfo) {
      this.needsVariable("sa", "$.SA");
      return "sa[" + this.classInfoSymbol(classInfo) + "]";
    }

    runtimeClassObject(classInfo: ClassInfo) {
      this.needsVariable("co", "$.CO");
      return "co[" + this.classInfoSymbol(classInfo) + "]";
    }

    emitClassInitializationCheck(classInfo: ClassInfo) {
      while (classInfo instanceof ArrayClassInfo) {
        classInfo = (<ArrayClassInfo>classInfo).elementClass;
      }
      if (!CLASSES.isPreInitializedClass(classInfo)) {
        var message;
        if (this.initializedClasses[classInfo.id]) {
          (emitDebugInfoComments || baselineCounter) && (message = "Optimized ClassInitializationCheck: " + classInfo.getClassNameSlow() + ", block redundant.");
        } else if (classInfo === this.methodInfo.classInfo) {
          (emitDebugInfoComments || baselineCounter) && (message = "Optimized ClassInitializationCheck: " + classInfo.getClassNameSlow() + ", self access.");
        } else if (!classInfo.isInterface && this.methodInfo.classInfo.isAssignableTo(classInfo)) {
          (emitDebugInfoComments || baselineCounter) && (message = "Optimized ClassInitializationCheck: " + classInfo.getClassNameSlow() + ", base access.");
        } else {
          (emitDebugInfoComments || baselineCounter) && (message = "ClassInitializationCheck: " + classInfo.getClassNameSlow());
          this.needsVariable("ci", "$.I");
          this.blockEmitter.writeLn("ci[" + this.classInfoSymbol(classInfo) + "] || CIC(" + this.classInfoObject(classInfo) + ");");
          if (canStaticInitializerYield(classInfo)) {
            this.emitUnwind(this.blockEmitter, String(this.pc));
          } else {
            emitCompilerAssertions && this.emitNoUnwindAssertion();
          }
        }
        emitDebugInfoComments && this.blockEmitter.writeLn("// " + message);
        baselineCounter && baselineCounter.count(message);
        this.initializedClasses[classInfo.id] = true;
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

      var signatureKinds = methodInfo.signatureKinds;
      var args: string [] = [];
      for (var i = signatureKinds.length - 1; i > 0; i--) {
        args.unshift(this.pop(signatureKinds[i]));
        if (isTwoSlot(signatureKinds[i])) {
          args.unshift(this.pop(Kind.High));
        }
      }

      var call;
      var classInfoObject = this.classInfoObject(methodInfo.classInfo);
      var methodId = null;
      if (opcode !== Bytecodes.INVOKESTATIC) {
        var object = this.pop(Kind.Reference);
        this.emitNullCheck(object);
        args.unshift(object);
        if (opcode === Bytecodes.INVOKESPECIAL) {
          methodId = this.methodInfoSymbol(methodInfo);
          call = "(LM[" + methodId + "]||" + "GLM(" + methodId + "))(" + args.join(",") + ")";
        } else if (opcode === Bytecodes.INVOKEVIRTUAL) {
          var classId = "i32[(" + object + "|0)>>2]";
          if (methodInfo.vTableIndex < (1 << Constants.LOG_MAX_FLAT_VTABLE_SIZE)) {
            call = "(FT[(" + classId + "<<" + Constants.LOG_MAX_FLAT_VTABLE_SIZE + ")+" + methodInfo.vTableIndex + "]||" + "GLVM(" + classId + "," + methodInfo.vTableIndex + "))(" + args.join(",") + ")";
          } else {
            call = "(VT[" + classId + "][" + methodInfo.vTableIndex + "]||" + "GLVM(" + classId + "," + methodInfo.vTableIndex + "))(" + args.join(",") + ")";
          }
        } else if (opcode === Bytecodes.INVOKEINTERFACE) {
          var objClass = "CI[i32[(" + object + "+" + Constants.OBJ_CLASS_ID_OFFSET + ")>>2]]";
          methodId = objClass + ".iTable['" + methodInfo.mangledName + "'].id";
          call = "(LM[" + methodId + "]||" + "GLM(" + methodId + "))(" + args.join(",") + ")";
        } else {
          Debug.unexpected(Bytecode.getBytecodesName(opcode));
        }
      } else {
        args.unshift(String(Constants.NULL));
        methodId = this.methodInfoSymbol(methodInfo);
        call = "(LM[" + methodId + "]||" + "GLM(" + methodId + "))(" + args.join(",") + ")";
      }

      if (methodInfo.implKey in inlineMethods) {
        emitDebugInfoComments && this.blockEmitter.writeLn("// Inlining: " + methodInfo.implKey);
        call = inlineMethods[methodInfo.implKey];
      }
      this.needsVariable("re");
      emitDebugInfoComments && this.blockEmitter.writeLn("// " + Bytecode.getBytecodesName(opcode) + ": " + methodInfo.implKey);
      this.blockEmitter.writeLn("re=" + call + ";");
      if (calleeCanYield) {
        this.emitUnwind(this.blockEmitter, String(this.pc));
      } else {
        emitCompilerAssertions && this.emitUndefinedReturnAssertion();
        emitCompilerAssertions && this.emitNoUnwindAssertion();
      }
      if (signatureKinds[0] !== Kind.Void) {
        this.emitPush(signatureKinds[0], "re");
        if (isTwoSlot(signatureKinds[0])) {
          this.emitPush(Kind.High, "tempReturn0");
        }
      }
    }

    emitNegativeArraySizeCheck(length: string) {
      if (this.isPrivileged) {
        return;
      }
      this.blockEmitter.writeLn(length + "<0&&TS();");
    }

    emitBoundsCheck(array: string, index: string) {
      if (this.isPrivileged || !emitCheckArrayBounds) {
        return;
      }
      if (inlineRuntimeCalls) {
        this.blockEmitter.writeLn("if((" + index + ">>>0)>=(i32[" + array + "+" + Constants.ARRAY_LENGTH_OFFSET + ">>2]>>>0))TI(" + index + ");");
      } else {
        this.blockEmitter.writeLn("CAB(" + array + "," + index + ");");
      }
    }

    emitArrayStoreCheck(array: string, value: string) {
      if (this.isPrivileged || !emitCheckArrayStore) {
        return;
      }
      this.blockEmitter.writeLn("CAS(" + array + "," + value + ");");
    }

    emitStoreIndexed(kind: Kind) {
      var l, h;
      if (isTwoSlot(kind)) {
        h = this.pop(Kind.High);
      }
      l = this.pop(stackKind(kind));
      var index = this.pop(Kind.Int);
      var array = this.pop(Kind.Reference);
      if (kind === Kind.Reference) {
        this.emitNullCheck(array);
      }
      this.emitBoundsCheck(array, index);
      if (kind === Kind.Reference) {
        this.emitArrayStoreCheck(array, l);
      }
      var base = array + "+" + Constants.ARRAY_HDR_SIZE;
      switch (kind) {
        case Kind.Byte:
          this.blockEmitter.writeLn("i8[" + base + "+" + index + "]=" + l + ";");
          return;
        case Kind.Char:
          this.blockEmitter.writeLn("u16[(" + base + ">>1)+" + index + "|0]=" + l + ";");
          return;
        case Kind.Short:
          this.blockEmitter.writeLn("i16[(" + base + ">>1)+" + index + "|0]=" + l + ";");
          return;
        case Kind.Int:
        case Kind.Float:
        case Kind.Reference:
          this.blockEmitter.writeLn("i32[(" + base + ">>2)+" + index + "|0]=" + l + ";");
          return;
        case Kind.Long:
        case Kind.Double:
          this.needsVariable("ea");
          this.blockEmitter.writeLn("ea=(" + base + ">>2)+(" + index + "<<1)|0;");
          this.blockEmitter.writeLn("i32[ea]=" + l + ";");
          this.blockEmitter.writeLn("i32[ea+1|0]=" + h + ";");
          return;
        default:
          Debug.assertUnreachable("Unimplemented type: " + getKindName(kind));
          break;
      }
    }

    emitLoadIndexed(kind: Kind) {
      var index = this.pop(Kind.Int);
      var array = this.pop(Kind.Reference);
      this.emitNullCheck(array);
      this.emitBoundsCheck(array, index);

      var base = array + "+" + Constants.ARRAY_HDR_SIZE;
      switch (kind) {
        case Kind.Byte:
          this.emitPush(kind, "i8[" + base + "+" + index + "|0]");
          break;
        case Kind.Char:
          this.emitPush(kind, "u16[(" + base + ">>1)+" + index + "|0]");
          break;
        case Kind.Short:
          this.emitPush(kind, "i16[(" + base + ">>1)+" + index + "|0]");
          break;
        case Kind.Int:
        case Kind.Float:
        case Kind.Reference:
          this.emitPush(kind, "i32[(" + base + ">>2)+" + index + "|0]");
          break;
        case Kind.Long:
        case Kind.Double:
          this.needsVariable("ea");
          this.blockEmitter.writeLn("ea=(" + base + ">>2)+(" + index + "<<1)|0;");
          this.emitPush(kind, "i32[ea]");
          this.emitPush(kind, "i32[ea+1|0]");
          break;
        default:
          Debug.assertUnreachable("Unimplemented type: " + getKindName(kind));
          break;
      }
    }

    emitIncrement(stream: BytecodeStream) {
      var l = this.getLocal(stream.readLocalIndex());
      this.blockEmitter.writeLn(l + "=" + l + "+" + stream.readIncrement() + "|0;");
    }

    emitGoto(block: Block, stream: BytecodeStream) {
      var targetBCI = stream.readBranchDest();
      var targetBlock = this.getBlock(targetBCI);
      Relooper.addBranch(block.relooperBlockID, targetBlock.relooperBlockID);
    }

    emitLoadConstant(index: number) {
      var cp = this.methodInfo.classInfo.constantPool;
      var offset = cp.entries[index];
      var buffer = cp.buffer;
      var tag = buffer[offset++];
      switch (tag) {
        case TAGS.CONSTANT_Float:
        case TAGS.CONSTANT_Integer:
          var value = buffer[offset++] << 24 | buffer[offset++] << 16 | buffer[offset++] << 8 | buffer[offset++];
          this.emitPushBits(Kind.Int, value);
          return;
        case TAGS.CONSTANT_Long:
        case TAGS.CONSTANT_Double:
          var h = buffer[offset++] << 24 | buffer[offset++] << 16 | buffer[offset++] << 8 | buffer[offset++];
          var l = buffer[offset++] << 24 | buffer[offset++] << 16 | buffer[offset++] << 8 | buffer[offset++];
          this.emitPushLongBits(l, h);
          return;
        case TAGS.CONSTANT_String:
          this.emitPush(Kind.Reference, this.classInfoObject(this.methodInfo.classInfo) + ".constantPool.resolve(" + index + ", " + TAGS.CONSTANT_String + ")");
          return;
        default:
          throw "Not done for: " + getTAGSName(tag);
      }
    }

    emitThrow(pc: number) {
      var object = this.peek(Kind.Reference);
      this.emitNullCheck(object);
      this.blockEmitter.writeLn("throw GH(" + object + ");");
    }

    emitNewInstance(cpi: number) {
      var classInfo = this.lookupClass(cpi);
      this.emitClassInitializationCheck(classInfo);
      this.emitPush(Kind.Reference, "AO(" + this.classInfoObject(classInfo) + ")");
    }

    emitNewTypeArray(typeCode: number) {
      var kind = arrayTypeCodeToKind(typeCode);
      var length = this.pop(Kind.Int);
      this.emitNegativeArraySizeCheck(length);
      // TODO: inline the logic for allocating a new array.
      this.emitPush(Kind.Reference, "NA(J2ME.PrimitiveClassInfo." + "????ZCFDBSIJ"[typeCode] + ", " + length + ")");
    }

    emitCheckCast(cpi: number) {
      var object = this.peek(Kind.Reference);
      if (this.isPrivileged) {
        return;
      }
      var classInfo = this.lookupClass(cpi);
      var call = "CCK";
      if (classInfo.isInterface) {
        call = "CCI";
      }
      call = call + "(" + object + "," + this.classInfoSymbol(classInfo) + ")"
      if (inlineRuntimeCalls) {
        this.blockEmitter.writeLn("(!" + object + ")||i32[" + object + "+" + Constants.OBJ_CLASS_ID_OFFSET + ">>2]===" + this.classInfoSymbol(classInfo) + "||" + call + ";");
      } else {
        this.blockEmitter.writeLn(call + ";");
      }
    }

    emitInstanceOf(cpi: number) {
      var object = this.pop(Kind.Reference);
      var classInfo = this.lookupClass(cpi);
      var call = "IOK";
      if (classInfo.isInterface) {
        call = "IOI";
      }
      call = call + "(" + object + "," + this.classInfoSymbol(classInfo) + ")|0";
      if (inlineRuntimeCalls) {
        call = "((" + object + "&&i32[" + object + "+" + Constants.OBJ_CLASS_ID_OFFSET + ">>2]=== " + this.classInfoSymbol(classInfo) + ")||" + call + ")|0";
      }
      this.emitPush(Kind.Int, call);
    }

    emitNullCheck(address) {
      if (this.isPrivileged || !emitCheckNull) {
        return;
      }
      this.blockEmitter.writeLn("!" + address + "&&TN();");
    }

    emitArrayLength() {
      var array = this.pop(Kind.Reference);
      this.emitNullCheck(array);
      this.emitPush(Kind.Int, "i32[" + array + "+" + Constants.ARRAY_LENGTH_OFFSET + ">>2]");
    }

    emitNewObjectArray(cpi: number) {
      var classInfo = this.lookupClass(cpi);
      this.emitClassInitializationCheck(classInfo);
      var length = this.pop(Kind.Int);
      this.emitNegativeArraySizeCheck(length);
      this.emitPush(Kind.Reference, "NA(" + this.classInfoObject(classInfo) + ", " + length + ")");
    }

    emitNewMultiObjectArray(cpi: number, stream: BytecodeStream) {
      var classInfo = this.lookupClass(cpi);
      var numDimensions = stream.readUByte(stream.currentBCI + 3);
      var dimensions = new Array(numDimensions);
      for (var i = numDimensions - 1; i >= 0; i--) {
        dimensions[i] = this.pop(Kind.Int);
      }
      this.emitPush(Kind.Reference, "NM(" + this.classInfoObject(classInfo) + ",[" + dimensions.join(",") + "])");
    }

    private emitUnwind(emitter: Emitter, pc: string, forceInline: boolean = false) {
      // Only emit unwind throws if it saves on code size.
      if (false && !forceInline && this.blockMap.invokeCount > 2 &&
          this.methodInfo.codeAttribute.max_stack < 8) {
        emitter.writeLn("U&&B" + this.sp + "(" + pc + ");");
        this.hasUnwindThrow = true;
      } else {
        this.emitBailout(emitter, pc, String(this.sp), this.sp);
      }
      baselineCounter && baselineCounter.count("emitUnwind");
    }

    private emitBailout(emitter: Emitter, pc: string, sp: string, stackCount: number) {
      var localCount = this.methodInfo.codeAttribute.max_locals;
      var args = [this.methodInfoSymbol(this.methodInfo), pc, this.lockObject];
      for (var i = 0; i < localCount; i++) {
        args.push(this.getLocalName(i));
      }
      for (var i = 0; i < stackCount; i++) {
        args.push(this.getStackName(i));
      }
      emitter.writeLn("if(U){$.B(" + args.join(",") + ");return;}");
    }

    emitNoUnwindAssertion() {
      this.blockEmitter.writeLn("if(U){J2ME.Debug.assert(false,'Unexpected unwind.');}");
    }

    emitUndefinedReturnAssertion() {
      this.blockEmitter.writeLn("if (U && re !== undefined) { J2ME.Debug.assert(false, 'Unexpected return value during unwind.'); }");
    }

    private emitMonitorEnter(emitter: Emitter, nextPC: number, object: string) {
      this.hasMonitorEnter = true;

      this.needsVariable("lk");
      emitter.writeLn("lk=J2ME.getMonitor(" + object + ");");
      // TODO: add back fast path for lock level = 0
      emitter.writeLn("ME(lk);");
      this.emitUnwind(emitter, String(nextPC), true);
    }

    private emitPreemptionCheck(emitter: Emitter) {
      if (!emitCheckPreemption || this.methodInfo.implKey in noPreemptMap) {
        return;
      }
      emitter.writeLn("PS++;");
      emitter.writeLn("if((PS&" + preemptionSampleMask + ")===0)PE();");
      this.emitUnwind(emitter, String(this.pc), true);
    }

    private emitMonitorExit(emitter: Emitter, object: string) {
      emitter.writeLn("lk=J2ME.getMonitor(" + object + ");");
      // TODO: add back fast path for lock level = 1
      emitter.writeLn("MX(lk);");
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
          Debug.unexpected(Bytecode.getBytecodesName(opcode));
      }
    }

    emitDivideByZeroCheck(kind: Kind, l: string, h: string) {
      if (this.isPrivileged) {
        return;
      }
      if (kind === Kind.Int) {
        this.blockEmitter.writeLn("!" + l + "&&TA();");
      } else if (kind === Kind.Long) {
        this.blockEmitter.writeLn("!" + l + "&&!" + h + "&&TA();");
      } else {
        Debug.unexpected(getKindName(kind));
      }
    }

    emitArithmeticOp(kind: Kind, opcode: Bytecodes, canTrap: boolean) {
      var al, ah;
      var bl, bh;
      if (isTwoSlot(kind)) {
        bh = this.pop(kind), bl = this.pop(kind);
        ah = this.pop(kind), al = this.pop(kind);
      } else {
        bl = this.pop(kind), al = this.pop(kind);
      }
      if (canTrap) {
        this.emitDivideByZeroCheck(kind, bl, bh);
      }
      switch (opcode) {
        case Bytecodes.IADD:
          this.emitPush(Kind.Int, al + "+" + bl + "|0");
          break;
        case Bytecodes.ISUB:
          this.emitPush(Kind.Int, al + "-" + bl + "|0");
          break;
        case Bytecodes.IMUL:
          this.emitPush(Kind.Int, "Math.imul(" + al + "," + bl + ")");
          break;
        case Bytecodes.IDIV:
          this.emitPush(Kind.Int, al + "/" + bl + "|0");
          break;
        case Bytecodes.IREM:
          this.emitPush(Kind.Int, al + "%" + bl);
          break;
        case Bytecodes.FADD:
        case Bytecodes.FSUB:
        case Bytecodes.FMUL:
        case Bytecodes.FDIV:
        case Bytecodes.FREM:
          this.emitPush(Kind.Float, Bytecode.getBytecodesName(opcode).toLowerCase() + "(" + al + "," + bl + ")");
          break;
        case Bytecodes.LADD:
        case Bytecodes.LSUB:
        case Bytecodes.LMUL:
        case Bytecodes.LDIV:
        case Bytecodes.LREM:
        case Bytecodes.DADD:
        case Bytecodes.DSUB:
        case Bytecodes.DMUL:
        case Bytecodes.DDIV:
        case Bytecodes.DREM:
          this.emitPush(Kind.Double, Bytecode.getBytecodesName(opcode).toLowerCase() + "(" + al + "," + ah + "," + bl + "," + bh + ")");
          this.emitPush(Kind.High, "tempReturn0");
          break;
        default:
          release || assert(false, "emitArithmeticOp: " + Bytecode.getBytecodesName(opcode));
      }
    }

    emitNegateOp(kind: Kind, opcode: Bytecodes) {
      var l, h;
      if (isTwoSlot(kind)) {
        h = this.pop(kind);
      }
      l = this.pop(kind);
      switch (kind) {
        case Kind.Int:
          this.emitPush(kind, "(- " + l + ")|0");
          break;
        case Kind.Float:
          this.emitPush(kind, "fneg(" + l + ")");
          break;
        case Kind.Long:
        case Kind.Double:
          this.emitPush(kind, Bytecode.getBytecodesName(opcode).toLowerCase() + "(" + l + "," + h + ")");
          this.emitPush(Kind.High, "tempReturn0");
          break;
        default:
          Debug.unexpected(getKindName(kind));
      }
    }

    emitShiftOp(kind: Kind, opcode: Bytecodes) {
      var s = this.pop(Kind.Int);
      var l, h;
      if (isTwoSlot(kind)) {
        h = this.pop(kind);
      }
      l = this.pop(kind);
      var v;
      switch(opcode) {
        case Bytecodes.ISHL:  this.emitPush(kind, l + "<<"  + s); return;
        case Bytecodes.ISHR:  this.emitPush(kind, l + ">>"  + s); return;
        case Bytecodes.IUSHR: this.emitPush(kind, l + ">>>" + s); return;
        case Bytecodes.LSHL:
        case Bytecodes.LSHR:
        case Bytecodes.LUSHR:
          this.emitPush(kind, Bytecode.getBytecodesName(opcode).toLowerCase() + "(" + l + "," + h + "," + s + ")");
          this.emitPush(Kind.High, "tempReturn0");
          return;
        default:
          Debug.unexpected(Bytecode.getBytecodesName(opcode));
      }
    }

    emitLogicOp(kind: Kind, opcode: Bytecodes) {
      var al, ah;
      var bl, bh;
      if (isTwoSlot(kind)) {
        bh = this.pop(kind), bl = this.pop(kind);
        ah = this.pop(kind), al = this.pop(kind);
      } else {
        bl = this.pop(kind), al = this.pop(kind);
      }
      switch(opcode) {
        case Bytecodes.IAND: this.emitPush(kind, al + "&" + bl); return;
        case Bytecodes.IOR:  this.emitPush(kind, al + "|" + bl);  return;
        case Bytecodes.IXOR: this.emitPush(kind, al + "^" + bl); return;
        case Bytecodes.LAND: this.emitPush(kind, al + "&" + bl);
                             this.emitPush(kind, ah + "&" + bh); return;
        case Bytecodes.LOR:  this.emitPush(kind, al + "|" + bl);
                             this.emitPush(kind, ah + "|" + bh); return;
        case Bytecodes.LXOR: this.emitPush(kind, al + "^" + bl);
                             this.emitPush(kind, ah + "^" + bh); return;
        default:
          Debug.unexpected(Bytecode.getBytecodesName(opcode));
      }
    }

    emitConvertOp(from: Kind, to: Kind, opcode: Bytecodes) {
      var l, h;
      if (isTwoSlot(from)) {
        h = this.pop(from);
      }
      l = this.pop(from);

      switch (opcode) {
        case Bytecodes.I2L:
          this.emitPush(Kind.Long, l);
          this.emitPush(Kind.High, "(" + l + "<0?-1:0)");
          break;
        case Bytecodes.I2F:
          this.emitPush(Kind.Float, "i2f(" + l + ")");
          break;
        case Bytecodes.I2B:
          this.emitPush(Kind.Int, "(" + l + "<<24)>>24");
          break;
        case Bytecodes.I2C:
          this.emitPush(Kind.Int, l + "&0xffff");
          break;
        case Bytecodes.I2S:
          this.emitPush(Kind.Int, "(" + l + "<<16)>>16");
          break;
        case Bytecodes.I2D:
          this.emitPush(Kind.Double, "i2d(" + l + ")");
          this.emitPush(Kind.High, "tempReturn0");
          break;
        case Bytecodes.L2I:
          this.emitPush(Kind.Int, l);
          break;
        case Bytecodes.L2F:
          this.emitPush(Kind.Float, "l2f(" + l + "," + h + ")");
          break;
        case Bytecodes.L2D:
          this.emitPush(Kind.Double, "l2d(" + l + "," + h + ")");
          this.emitPush(Kind.High, "tempReturn0");
          break;
        case Bytecodes.D2I:
          this.emitPush(Kind.Int, "d2i(" + l + "," + h + ")");
          break;
        case Bytecodes.F2I:
          this.emitPush(Kind.Int, "f2i(" + l + ")");
          break;
        case Bytecodes.F2L:
          this.emitPush(Kind.Long, "f2l(" + l + ")");
          this.emitPush(Kind.High, "tempReturn0");
          break;
        case Bytecodes.F2D:
          this.emitPush(Kind.Double, "f2d(" + l + ")");
          this.emitPush(Kind.High, "tempReturn0");
          break;
        case Bytecodes.D2L:
          this.emitPush(Kind.Long, "d2l(" + l + "," + h + ")");
          this.emitPush(Kind.High, "tempReturn0");
          break;
        case Bytecodes.D2F:
          this.emitPush(Kind.Float, "d2f(" + l + "," + h + ")");
          break;
        default:
          throwCompilerError(Bytecode.getBytecodesName(opcode));
      }
    }

    emitCompareOp(kind: Kind, isLessThan: boolean) {
      var al, ah;
      var bl, bh;
      if (isTwoSlot(kind)) {
        bh = this.pop(kind), bl = this.pop(kind);
        ah = this.pop(kind), al = this.pop(kind);
      } else {
        bl = this.pop(kind), al = this.pop(kind);
      }
      if (kind === Kind.Long) {
        this.emitPush(Kind.Int, "lcmp(" + al + "," + ah + "," + bl + "," + bh + ")");
      } else if (kind === Kind.Double) {
        this.emitPush(Kind.Int, "dcmp(" + al + "," + ah + "," + bl + "," + bh + "," + isLessThan + ")");
      } else if (kind === Kind.Float) {
        this.emitPush(Kind.Int, "fcmp(" + al + "," + bl + "," + isLessThan + ")");
      }
    }

    getBlockIndex(pc: number): number {
      return pc;
    }

    getBlock(pc: number): Block {
      return this.blockMap.getBlock(pc);
    }

    emitTableSwitch(block: Block, stream: BytecodeStream) {
      var tableSwitch = stream.readTableSwitch();
      var value = this.pop(Kind.Int);
      // We need some text in the body of the table switch block, otherwise the
      // branch condition variable is ignored.
      var branchBlock = Relooper.addBlock("// Table Switch", String(value));
      Relooper.addBranch(block.relooperBlockID, branchBlock);
      var defaultTarget = this.getBlock(stream.currentBCI + tableSwitch.defaultOffset()).relooperBlockID;
      for (var i = 0; i < tableSwitch.numberOfCases(); i++) {
        var key = tableSwitch.keyAt(i);
        var target: any = this.getBlock(stream.currentBCI + tableSwitch.offsetAt(i)).relooperBlockID;
        if (target === defaultTarget) {
          continue;
        }
        var caseTargetBlock = Relooper.addBlock();
        Relooper.addBranch(branchBlock, caseTargetBlock, "case " + key + ":");
        Relooper.addBranch(caseTargetBlock, target);
      }
      Relooper.addBranch(branchBlock, defaultTarget);
    }

    emitLookupSwitch(block: Block, stream: BytecodeStream) {
      var lookupSwitch = stream.readLookupSwitch();
      var value = this.pop(Kind.Int);
      // We need some text in the body of the lookup switch block, otherwise the
      // branch condition variable is ignored.
      var branchBlock = Relooper.addBlock("// Lookup Switch", String(value));
      Relooper.addBranch(block.relooperBlockID, branchBlock);
      var defaultTarget = this.getBlock(stream.currentBCI + lookupSwitch.defaultOffset()).relooperBlockID;
      for (var i = 0; i < lookupSwitch.numberOfCases(); i++) {
        var key = lookupSwitch.keyAt(i);
        var target: any = this.getBlock(stream.currentBCI + lookupSwitch.offsetAt(i)).relooperBlockID;
        if (target === defaultTarget) {
          continue;
        }
        var caseTargetBlock = Relooper.addBlock();
        Relooper.addBranch(branchBlock, caseTargetBlock, "case " + key + ":");
        Relooper.addBranch(caseTargetBlock, target);
      }
      Relooper.addBranch(branchBlock, defaultTarget);
    }

    emitBytecode(stream: BytecodeStream, block: Block) {
      var cpi: number;
      var opcode: Bytecodes = stream.currentBC();
      writer && writer.writeLn("emit: pc: " + stream.currentBCI + ", sp: " + this.sp + " " + Bytecode.getBytecodesName(opcode));

      if ((block.isExceptionEntry || block.hasHandlers) && Bytecode.canTrap(opcode)) {
        this.blockEmitter.writeLn("pc=" + this.pc + ";");
      }

      switch (opcode) {
        case Bytecodes.NOP            : break;
        case Bytecodes.ACONST_NULL    : this.emitPushBits(Kind.Reference, Constants.NULL); break;
        case Bytecodes.ICONST_M1      :
        case Bytecodes.ICONST_0       :
        case Bytecodes.ICONST_1       :
        case Bytecodes.ICONST_2       :
        case Bytecodes.ICONST_3       :
        case Bytecodes.ICONST_4       :
        case Bytecodes.ICONST_5       : this.emitPushInt(opcode - Bytecodes.ICONST_0); break;
        case Bytecodes.FCONST_0       :
        case Bytecodes.FCONST_1       :
        case Bytecodes.FCONST_2       : this.emitPushFloat(opcode - Bytecodes.FCONST_0); break;
        case Bytecodes.DCONST_0       :
        case Bytecodes.DCONST_1       : this.emitPushDouble(opcode - Bytecodes.DCONST_0); break;
        case Bytecodes.LCONST_0       :
        case Bytecodes.LCONST_1       : this.emitPushLongBits(opcode - Bytecodes.LCONST_0, 0); break;
        case Bytecodes.BIPUSH         : this.emitPushInt(stream.readByte()); break;
        case Bytecodes.SIPUSH         : this.emitPushInt(stream.readShort()); break;
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
        case Bytecodes.INEG           : this.emitNegateOp(Kind.Int, opcode); break;
        case Bytecodes.LNEG           : this.emitNegateOp(Kind.Long, opcode); break;
        case Bytecodes.FNEG           : this.emitNegateOp(Kind.Float, opcode); break;
        case Bytecodes.DNEG           : this.emitNegateOp(Kind.Double, opcode); break;
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
        case Bytecodes.MULTIANEWARRAY : this.emitNewMultiObjectArray(stream.readCPI(), stream); break;
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
        default:
          throw new Error("Not Implemented " + Bytecode.getBytecodesName(opcode));
      }
      writer && writer.writeLn("");
    }
  }
}
