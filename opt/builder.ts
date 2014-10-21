module J2ME {

  var writer = new IndentingWriter();

  import Block = Bytecode.Block;
  import BlockMap = Bytecode.BlockMap;

  import assert = Debug.assert;
  import unique = ArrayUtilities.unique;

  import IR = C4.IR;
  import Node = IR.Node;
  import Value = IR.Value;
  import Phi = IR.Phi;
  import Control = IR.Control;
  import Constant = IR.Constant;
  import Start = IR.Start;
  import Region = IR.Region;
  import ProjectionType = IR.ProjectionType;
  import Null = IR.Null;
  import Undefined = IR.Undefined;
  import True = IR.True;
  import False = IR.False;
  import Operator = IR.Operator;
  import PeepholeOptimizer = IR.PeepholeOptimizer;

  import Bytecodes = Bytecode.Bytecodes;
  import BytecodeStream = Bytecode.BytecodeStream;
  import Condition = Bytecode.Condition;

  function kindsFromSignature(signature: string) {

  }

  export function isTwoSlot(kind: Kind) {
    return kind === Kind.Long || kind === Kind.Double;
  }

  export function assertHigh(x: Value) {
    assert (x === null);
  }

  export interface FieldInfo {
    name: string;
    signature: any;
    classInfo: ClassInfo;
    access_flags: any;
  }

  enum TAGS {
    CONSTANT_Class = 7,
    CONSTANT_Fieldref = 9,
    CONSTANT_Methodref = 10,
    CONSTANT_InterfaceMethodref = 11,
    CONSTANT_String = 8,
    CONSTANT_Integer = 3,
    CONSTANT_Float = 4,
    CONSTANT_Long = 5,
    CONSTANT_Double = 6,
    CONSTANT_NameAndType = 12,
    CONSTANT_Utf8 = 1,
    CONSTANT_Unicode = 2
}

  export interface ConstantPoolEntry {
    tag: TAGS;
    name_index: number;
    bytes: string;
    class_index: number;
    name_and_type_index: number;
    signature_index: number;
    string_index: number;
    integer: number;
    float: number;
    double: number;
    highBits: number;
    lowBits: number;
  }

  export interface ClassInfo {
    className: string;
    superClassName: string;
    interfaces: ClassInfo [];
    fields: any [];
    methods: any [];
    classes: any [];
    constant_pool: ConstantPoolEntry [];
  }

  export interface ExceptionHandler {
    start_pc: number;
    end_pc: number;
    handler_pc: number;
    catch_type: number;
  }

  export interface MethodInfo {
    name: string;
    classInfo: ClassInfo;
    code: Uint8Array;
    isNative: boolean;
    isPublic: boolean;
    isStatic: boolean;
    exception_table: ExceptionHandler [];
    max_locals: number;
    max_stack: number;
    consumes: number;
    signature: string;
  }

  function assertKind(kind: Kind, x: Node): Node {
    return x;
  }

  class State {
    private static _nextID = 0;
    id: number
    bci: number;
    local: Value [];
    stack: Value [];
    store: Value;
    loads: Value [];
    constructor(bci: number = 0) {
      this.id = State._nextID += 1;
      this.bci = bci;
      this.local = [];
      this.stack = [];
      this.store = Undefined;
      this.loads = [];
    }

    clone(bci: number) {
      var s = new State();
      s.bci = bci !== undefined ? bci : this.bci;
      s.local = this.local.slice(0);
      s.stack = this.stack.slice(0);
      s.loads = this.loads.slice(0);
      s.store = this.store;
      return s;
    }

    matches(other: State) {
      return this.stack.length === other.stack.length &&
             this.local.length === other.local.length;
    }

    makeLoopPhis(control: Control, dirtyLocals: boolean []) {
      var s = new State();
      release || assert (control);
      function makePhi(x) {
        var phi = new Phi(control, x);
        phi.isLoop = true;
        return phi;
      }
      s.bci = this.bci;
      s.local = this.local.map(function (v, i) {
        if (true || dirtyLocals[i]) {
          return makePhi(v);
        }
        return v;
      });
      s.stack = this.stack.map(makePhi);
      s.loads = this.loads.slice(0);
      s.store = makePhi(this.store);
      return s;
    }

    static tryOptimizePhi(x: Value) {
      if (x instanceof Phi) {
        var phi: Phi = <Phi>x;
        if (phi.isLoop) {
          return phi;
        }
        var args = unique(phi.args);
        if (args.length === 1) {
          phi.seal();
          countTimeline("Builder: OptimizedPhi");
          return args[0];
        }
      }
      return x;
    }

    optimize() {
      this.local = this.local.map(State.tryOptimizePhi);
      this.stack = this.stack.map(State.tryOptimizePhi);
      this.store = State.tryOptimizePhi(this.store);
    }

    static mergeValue(control: Control, a: Value, b: Value): Phi {
      var phi: Phi = <Phi>(a instanceof Phi && a.control === control ? a : new Phi(control, a));
      phi.pushValue(b);
      return phi;
    }

    static mergeValues(control: Control, a: Value [], b: Value []) {
      for (var i = 0; i < a.length; i++) {
        a[i] = State.mergeValue(control, a[i], b[i]);
      }
    }

    merge(control: Control, other: State) {
      release || assert (control);
      release || assert (this.matches(other), this + " !== " + other);
      State.mergeValues(control, this.local, other.local);
      State.mergeValues(control, this.stack, other.stack);
      this.store = State.mergeValue(control, this.store, other.store);
      this.store.abstract = true;
    }

    trace(writer: IndentingWriter) {
      writer.writeLn(this.toString());
    }

    static toBriefString(x: Node) {
      if (x instanceof Node) {
        return x.toString(true);
      }
      if (x === null) {
        return "null";
      } else if (x === undefined) {
        return "undefined";
      }
      return x;
    }

    toString(): string {
      return "<" + String(this.id + " @ " + this.bci).padRight(' ', 10) +
        (" M: " + State.toBriefString(this.store)).padRight(' ', 14) +
        (" L: " + this.local.map(State.toBriefString).join(", ")).padRight(' ', 40) +
        (" S: " + this.stack.map(State.toBriefString).join(", ")).padRight(' ', 60);
    }

    /**
     * Pushes a value onto the stack without checking the type.
     */
    public xpush(x: Node) {
      assert (x === null || !x.isDeleted);
      assert (x === null || (x.kind !== Kind.Void && x.kind !== Kind.Illegal), "Unexpected value: " + x);
      this.stack.push(x);
    }

    /**
     * Pushes a value onto the stack and checks that it is an int.
     */
    public ipush(x: Node) {
      this.xpush(assertKind(Kind.Int, x));
    }

    /**
     * Pushes a value onto the stack and checks that it is a float.
     * @param x the instruction to push onto the stack
     */
    public fpush(x: Node) {
      this.xpush(assertKind(Kind.Float, x));
    }

    /**
     * Pushes a value onto the stack and checks that it is an object.
     */
    public apush(x: Node) {
      this.xpush(assertKind(Kind.Reference, x));
    }

    /**
     * Pushes a value onto the stack and checks that it is a long.
     */
    public lpush(x: Node) {
      this.xpush(assertKind(Kind.Long, x));
      this.xpush(null);
    }

    /**
     * Pushes a value onto the stack and checks that it is a double.
     */
    public dpush(x: Node) {
      this.xpush(assertKind(Kind.Double, x));
      this.xpush(null);
    }

    /**
     * Pushes an instruction onto the stack with the expected type.
     */
    public push(kind: Kind, x: Value) {
      assert (kind !== Kind.Void);
      this.xpush(assertKind(kind, x));
      if (isTwoSlot(kind)) {
        this.xpush(null);
      }
    }
    
    /**
     * Pops an instruction off the stack with the expected type.
     */
    public pop(kind: Kind): Value {
      assert (kind !== Kind.Void);
      if (isTwoSlot(kind)) {
        this.xpop();
      }
      return assertKind(kind, this.xpop());
    }

    /**
     * Pops a value off of the stack without checking the type.
     */
    public xpop(): Value {
      var result = this.stack.pop();
      assert (result === null || !result.isDeleted);
      return result;
    }

    /**
     * Pops a value off of the stack and checks that it is an int.
     */
    public ipop(): Value {
      return assertKind(Kind.Int, this.xpop());
    }

    /**
     * Pops a value off of the stack and checks that it is a float.
     */
    public fpop(): Value {
      return assertKind(Kind.Float, this.xpop());
    }

    /**
     * Pops a value off of the stack and checks that it is an object.
     */
    public apop(): Value {
      return assertKind(Kind.Reference, this.xpop());
    }

    /**
     * Pops a value off of the stack and checks that it is a long.
     */
    public lpop(): Value {
      assertHigh(this.xpop());
      return assertKind(Kind.Long, this.xpop());
    }

    /**
     * Pops a value off of the stack and checks that it is a double.
     */
    public dpop(): Value {
      assertHigh(this.xpop());
      return assertKind(Kind.Double, this.xpop());
    }

    /**
     * Loads the local variable at the specified index.
     */
    public loadLocal(i: number): Value {
      var x = this.local[i];
      if (x != null) {
        if (x instanceof Phi) {
          // assert ((PhiNode) x).type() == PhiType.Value;
          if (x.isDeleted) {
            return null;
          }
        }
        assert (!isTwoSlot(x.kind) || this.local[i + 1] === null || this.local[i + 1] instanceof Phi);
      }
      return x;
    }

    /**
     * Stores a given local variable at the specified index. If the value takes up two slots,
     * then the next local variable index is also overwritten.
     */
    public storeLocal(i: number, x: Value) {
      assert (x === null || (x.kind !== Kind.Void && x.kind !== Kind.Illegal), "Unexpected value: " + x);
      var local = this.local;
      local[i] = x;
      if (isTwoSlot(x.kind)) {
        // (tw) if this was a double word then kill i + 1
        local[i + 1] = null;
      }
      if (i > 0) {
        // if there was a double word at i - 1, then kill it
        var p = local[i - 1];
        if (p !== null && isTwoSlot(p.kind)) {
          local[i - 1] = null;
        }
      }
    }
  }

  export function compile(classes, classInfo: ClassInfo) {
    if (classInfo.className.indexOf("SimpleClass") < 0) {
      // return;
    }
    writer.enter("Compiling Class: " + classInfo.className + " {");
    classInfo.methods.forEach(compileMethodInfo);
    writer.leave("}");
  }

  function compileMethodInfo(methodInfo: MethodInfo) {
    if (methodInfo.name !== 'asd') {
      return;
    }
    if (!methodInfo.code) {
      return;
    }
    var builder = new Builder(methodInfo);
    builder.build();
  }

  interface WorklistItem {
    region: Region;
    block: Block;
  }

  interface Stop {

  }
  
  function genConstant(x: any, kind: Kind): IR.Constant {
    var constant = new IR.Constant(x);
    constant.kind = kind;
    return constant;
  }

  function getConstantFromPool(entry : ConstantPoolEntry): IR.Constant {
    switch (entry.tag) {
      case TAGS.CONSTANT_Integer:
        return genConstant(entry.integer, Kind.Int);
      case TAGS.CONSTANT_Float:
        return genConstant(entry.float, Kind.Float);
      case TAGS.CONSTANT_Double:
        return genConstant(entry.double, Kind.Double);
      default:
        throw "Not done.";
    }
  }

  class StopInfo {
    constructor(
      public control: Node,
      public target: Block,
      public state: State) {
      // ...
    }
  }

  class ReturnInfo {
    constructor(
      public control: Node,
      public value: Value) {
      // ...
    }
  }

  class Builder {
    state: State;
    region: Region;
    stream: BytecodeStream;
    peepholeOptimizer: PeepholeOptimizer;
    signatureDescriptor: SignatureDescriptor;
    stops: StopInfo [];
    returns: ReturnInfo [];
    blockMap: BlockMap;

    constructor(public methodInfo: MethodInfo) {
      // ...
      this.peepholeOptimizer = new PeepholeOptimizer();
      this.signatureDescriptor = SignatureDescriptor.makeSignatureDescriptor(methodInfo.signature);
    }

    build() {
      IR.Node.startNumbering();
      var methodInfo = this.methodInfo;

      writer.enter("Compiling Method: " + methodInfo.name + " " + methodInfo.signature + " {");
      writer.writeLn("Size: " + methodInfo.code.length);
      var blockMap = new BlockMap(methodInfo);
      this.blockMap = blockMap;
      blockMap.build();
      blockMap.trace(writer, true);

      var start = this.buildStart();
      var dfg = this.buildGraph(start, start.entryState.clone(), blockMap);

      true && dfg.trace(writer);

      enterTimeline("Build CFG");
      var cfg = dfg.buildCFG();
      leaveTimeline();

      enterTimeline("Verify IR");
      cfg.verify();
      leaveTimeline();

      enterTimeline("Optimize Phis");
      cfg.optimizePhis();
      leaveTimeline();

      enterTimeline("Schedule Nodes");
      cfg.scheduleEarly();
      leaveTimeline();

      true && cfg.trace(writer);

      enterTimeline("Verify IR");
      cfg.verify();
      leaveTimeline();

      enterTimeline("Allocate Variables");
      cfg.allocateVariables();
      leaveTimeline();

      enterTimeline("Generate Source");
      var result = C4.Backend.generate(cfg);
      leaveTimeline();
      true && writer.writeLn(result.body);

      Node.stopNumbering();
      leaveTimeline();

      writer.leave("}");
      IR.Node.stopNumbering();
    }

    buildStart(): IR.Start {
      var start = new IR.Start();
      var state = start.entryState = new State();
      // trace.writeLn(JSON.stringify(this.methodInfo));
      var methodInfo = this.methodInfo;

      for (var i = 0; i < methodInfo.max_locals; i++) {
        state.local.push(null);
      }

      var signatureDescriptor = this.signatureDescriptor;
      writer.writeLn("SIG: " + signatureDescriptor);

      var typeDescriptors = signatureDescriptor.typeDescriptors;

      var j = 0;
      for (var i = 1; i < typeDescriptors.length; i++) {
        var kind = Kind.Reference;
        if (typeDescriptors[i] instanceof AtomicTypeDescriptor) {
          kind = (<AtomicTypeDescriptor>typeDescriptors[i]).kind;
        }
        var parameter = new IR.Parameter(start, i - 1, "P" + kindCharacter(kind) + (i - 1));
        parameter.kind = kind;
        state.storeLocal(j, parameter);
        j += isTwoSlot(kind) ? 2 : 1;
      }
      return start;
    }

    buildGraph(start: Region, state: State, blockMap: BlockMap): IR.DFG {
      var worklist = new SortedList<WorklistItem>(function compare(a: WorklistItem, b: WorklistItem) {
        return a.block.blockID - b.block.blockID;
      });

      worklist.push({
        region: start,
        block: blockMap.blocks[0]
      });

//      var self = this;
//      blockMap.blocks.forEach(block => {
//        self.buildBlock(start, block, state);
//      });

      var next: WorklistItem;
      debugger;
      while ((next = worklist.pop())) {
        writer && writer.writeLn("Processing: " + next.region + " " + next.block.blockID + " " + next.region.entryState);
        this.buildBlock(next.region, next.block, next.region.entryState.clone()).forEach(function (stop: StopInfo) {
          var target = stop.target;
          var region = target.region;
          if (region) {
            writer && writer.enter("Merging into region: " + region + " @ " + target.startBci + ", block " + target.blockID + " {");
            writer && writer.writeLn("  R " + region.entryState);
            writer && writer.writeLn("+ I " + stop.state);

            region.entryState.merge(region, stop.state);
            region.predecessors.push(stop.control);

            writer && writer.writeLn("  = " + region.entryState);
            writer && writer.leave("}");
          } else {
            region = target.region = new Region(stop.control);
            var dirtyLocals: boolean [] = [];
//            if (target.loop) {
//              dirtyLocals = enableDirtyLocals.value && target.loop.getDirtyLocals();
//              writer && writer.writeLn("Adding PHIs to loop region. " + dirtyLocals);
//            }
            region.entryState = target.isLoopHeader ? stop.state.makeLoopPhis(region, dirtyLocals) : stop.state.clone(target.startBci);
            writer && writer.writeLn("Adding new region: " + region + " @ " + target.startBci + " to worklist.");
            worklist.push({region: region, block: target});
          }
        });

        writer && writer.enter("Worklist: {");
        worklist.forEach(function (item) {
          writer && writer.writeLn(item.region + " " + item.block.blockID + " " + item.region.entryState);
        });
        writer && writer.leave("}");
      }
      var signatureDescriptor = this.signatureDescriptor;
      var returnType = signatureDescriptor.typeDescriptors[0];
      // TODO handle void return types
      var stop = new IR.Stop(this.returns[0].control, this.returns[0].control, this.returns[0].value);
      return new IR.DFG(stop);
    }

    buildBlock(region: Region, block: Block, state: State): StopInfo [] {
      this.stops = null;
      this.returns = null;
      this.state = state;
      this.region = region;
      var code = this.methodInfo.code;
      var stream = new BytecodeStream(code);
      var bci = block.startBci;
      stream.setBCI(bci);

      while (stream.currentBCI <= block.endBci) {
        state.bci = bci;
        this.processBytecode(stream, state);
        stream.next();
        bci = stream.currentBCI;
      }
      if (this.returns) {
        return [];
      }
      if (!this.stops) {
        return [new StopInfo(region,
          this.blockMap.getBlock(stream.nextBCI),
          this.state
        )];
      }
      return this.stops;
    }

    private loadLocal(index: number, kind: Kind) {
      this.state.push(kind, this.state.loadLocal(index));
    }

    private storeLocal(kind: Kind, index: number) {
      this.state.storeLocal(index, this.state.pop(kind));
    }
    
    private stackOp(opcode: Bytecodes) {
      var state = this.state;
      switch (opcode) {
        case Bytecodes.POP: {
          state.xpop();
          break;
        }
        case Bytecodes.POP2: {
          state.xpop();
          state.xpop();
          break;
        }
        case Bytecodes.DUP: {
          var w = state.xpop();
          state.xpush(w);
          state.xpush(w);
          break;
        }
        case Bytecodes.DUP_X1: {
          var w1 = state.xpop();
          var w2 = state.xpop();
          state.xpush(w1);
          state.xpush(w2);
          state.xpush(w1);
          break;
        }
        case Bytecodes.DUP_X2: {
          var w1 = state.xpop();
          var w2 = state.xpop();
          var w3 = state.xpop();
          state.xpush(w1);
          state.xpush(w3);
          state.xpush(w2);
          state.xpush(w1);
          break;
        }
        case Bytecodes.DUP2: {
          var w1 = state.xpop();
          var w2 = state.xpop();
          state.xpush(w2);
          state.xpush(w1);
          state.xpush(w2);
          state.xpush(w1);
          break;
        }
        case Bytecodes.DUP2_X1: {
          var w1 = state.xpop();
          var w2 = state.xpop();
          var w3 = state.xpop();
          state.xpush(w2);
          state.xpush(w1);
          state.xpush(w3);
          state.xpush(w2);
          state.xpush(w1);
          break;
        }
        case Bytecodes.DUP2_X2: {
          var w1 = state.xpop();
          var w2 = state.xpop();
          var w3 = state.xpop();
          var w4 = state.xpop();
          state.xpush(w2);
          state.xpush(w1);
          state.xpush(w4);
          state.xpush(w3);
          state.xpush(w2);
          state.xpush(w1);
          break;
        }
        case Bytecodes.SWAP: {
          var w1 = state.xpop();
          var w2 = state.xpop();
          state.xpush(w1);
          state.xpush(w2);
          break;
        }
        default:
          Debug.unexpected("");
      }
    }

    genArithmeticOp(result: Kind, opcode: Bytecodes, canTrap: boolean) {
      var state = this.state;
      var y = state.pop(result);
      var x = state.pop(result);
      var v;
//      var isStrictFP = false; // TODO
      switch(opcode) {
        case Bytecodes.IADD:
        case Bytecodes.LADD: v = new IR.Binary(Operator.IADD, x, y); break;
        case Bytecodes.FADD:
        case Bytecodes.DADD: v = new IR.Binary(Operator.FADD, x, y /*, isStrictFP*/); break;
        /*
        case Bytecodes.ISUB:
        case Bytecodes.LSUB: v = new IntegerSubNode(result, x, y); break;
        case Bytecodes.FSUB:
        case Bytecodes.DSUB: v = new FloatSubNode(result, x, y, isStrictFP); break;
        case Bytecodes.IMUL:
        case Bytecodes.LMUL: v = new IntegerMulNode(result, x, y); break;
        case Bytecodes.FMUL:
        case Bytecodes.DMUL: v = new FloatMulNode(result, x, y, isStrictFP); break;
        case Bytecodes.IDIV:
        case Bytecodes.LDIV: v = new IntegerDivNode(result, x, y); break;
        case Bytecodes.FDIV:
        case Bytecodes.DDIV: v = new FloatDivNode(result, x, y, isStrictFP); break;
        case Bytecodes.IREM:
        case Bytecodes.LREM: v = new IntegerRemNode(result, x, y); break;
        case Bytecodes.FREM:
        case Bytecodes.DREM: v = new FloatRemNode(result, x, y, isStrictFP); break;
        default:
          throw new CiBailout("should not reach");
        */
      }
//      ValueNode result1 = append(graph.unique(v));
//      if (canTrap) {
//        append(graph.add(new ValueAnchorNode(result1)));
//      }

      v = this.peepholeOptimizer.fold(v);
      state.push(result, v);
    }

    genNewInstance(cpi: number) {
      this.state.apush(genConstant("NEW", Kind.Reference));
    }

    genLoadConstant(cpi: number, state: State) {
      var constant = getConstantFromPool(this.methodInfo.classInfo.constant_pool[cpi]);

//      Object con = constantPool.lookupConstant(cpi);
//
//      if (con instanceof RiType) {
//        // this is a load of class constant which might be unresolved
//        RiType riType = (RiType) con;
//        if (riType instanceof RiResolvedType) {
//          frameState.push(CiKind.Object, append(ConstantNode.forCiConstant(((RiResolvedType) riType).getEncoding(Representation.JavaClass), runtime, graph)));
//        } else {
//          append(graph.add(new DeoptimizeNode(DeoptAction.InvalidateRecompile)));
//          frameState.push(CiKind.Object, append(ConstantNode.forObject(null, runtime, graph)));
//        }
//      } else if (con instanceof CiConstant) {
//        CiConstant constant = (CiConstant) con;
        state.push(constant.kind, constant);
//      } else {
//        throw new Error("lookupConstant returned an object of incorrect type");
//      }
    }

    genIncrement(stream: BytecodeStream) {
      var index = stream.readLocalIndex();
      var local = this.state.loadLocal(index);
      var increment = genConstant(stream.readIncrement(), Kind.Int);
      var value = new IR.Binary(Operator.IADD, local, increment);
      this.state.storeLocal(index, value);
    }

    genCondition(kind: Kind, condition: Condition) {
      var y = this.state.pop(kind);
      var x = this.state.pop(kind);
      // Map condition to operator somehow.
      return new IR.Binary(Operator.LE, x, y);
    }

    genIfSame(stream: BytecodeStream, kind: Kind, condition: Condition) {
      release || assert (!this.stops);
      var predicate = this.genCondition(kind, condition);
      var _if = new IR.If(this.region, predicate);
      this.stops = [new StopInfo(
        new IR.Projection(_if, ProjectionType.FALSE),
        this.blockMap.getBlock(stream.readBranchDest()),
        this.state
      ), new StopInfo(
        new IR.Projection(_if, ProjectionType.TRUE),
        this.blockMap.getBlock(stream.nextBCI),
        this.state
      )];
    }

    genGoto(stream: BytecodeStream) {
      release || assert (!this.stops);
      this.stops = [new StopInfo(
        this.region,
        this.blockMap.getBlock(stream.readBranchDest()),
        this.state
      )];
    }

    genReturn(value: Value) {
      if (!this.returns) {
        this.returns = [];
      }
      this.returns.push(new ReturnInfo(
        this.region,
        value
      ));
    }

    processBytecode(stream: BytecodeStream, state: State) {
      var opcode: Bytecodes = stream.currentBC();
      writer.enter("State Before: " + Bytecodes[opcode].padRight(" ", 12) + " " + state.toString());
      switch (opcode) {
        case Bytecodes.NOP            : break;
        case Bytecodes.ACONST_NULL    : state.apush(genConstant(null, Kind.Reference)); break;
        case Bytecodes.ICONST_M1      : state.ipush(genConstant(-1, Kind.Int)); break;
        case Bytecodes.ICONST_0       : state.ipush(genConstant(0, Kind.Int)); break;
        case Bytecodes.ICONST_1       : state.ipush(genConstant(1, Kind.Int)); break;
        case Bytecodes.ICONST_2       : state.ipush(genConstant(2, Kind.Int)); break;
        case Bytecodes.ICONST_3       : state.ipush(genConstant(3, Kind.Int)); break;
        case Bytecodes.ICONST_4       : state.ipush(genConstant(4, Kind.Int)); break;
        case Bytecodes.ICONST_5       : state.ipush(genConstant(5, Kind.Int)); break;
        case Bytecodes.LCONST_0       : state.lpush(genConstant(0, Kind.Long)); break;
        case Bytecodes.LCONST_1       : state.lpush(genConstant(1, Kind.Long)); break;
        case Bytecodes.FCONST_0       : state.fpush(genConstant(0, Kind.Float)); break;
        case Bytecodes.FCONST_1       : state.fpush(genConstant(1, Kind.Float)); break;
        case Bytecodes.FCONST_2       : state.fpush(genConstant(2, Kind.Float)); break;
        case Bytecodes.DCONST_0       : state.dpush(genConstant(0, Kind.Double)); break;
        case Bytecodes.DCONST_1       : state.dpush(genConstant(1, Kind.Double)); break;
        case Bytecodes.BIPUSH         : state.ipush(genConstant(stream.readByte(), Kind.Int)); break;
        case Bytecodes.SIPUSH         : state.ipush(genConstant(stream.readShort(), Kind.Int)); break;
        case Bytecodes.LDC            :
        case Bytecodes.LDC_W          :
        case Bytecodes.LDC2_W         : this.genLoadConstant(stream.readCPI(), state); break;
        case Bytecodes.ILOAD          : this.loadLocal(stream.readLocalIndex(), Kind.Int); break;
        case Bytecodes.LLOAD          : this.loadLocal(stream.readLocalIndex(), Kind.Long); break;
        case Bytecodes.FLOAD          : this.loadLocal(stream.readLocalIndex(), Kind.Float); break;
        case Bytecodes.DLOAD          : this.loadLocal(stream.readLocalIndex(), Kind.Double); break;
        case Bytecodes.ALOAD          : this.loadLocal(stream.readLocalIndex(), Kind.Reference); break;
        case Bytecodes.ILOAD_0        :
        case Bytecodes.ILOAD_1        :
        case Bytecodes.ILOAD_2        :
        case Bytecodes.ILOAD_3        : this.loadLocal(opcode - Bytecodes.ILOAD_0, Kind.Int); break;
        case Bytecodes.LLOAD_0        :
        case Bytecodes.LLOAD_1        :
        case Bytecodes.LLOAD_2        :
        case Bytecodes.LLOAD_3        : this.loadLocal(opcode - Bytecodes.LLOAD_0, Kind.Long); break;
        case Bytecodes.FLOAD_0        :
        case Bytecodes.FLOAD_1        :
        case Bytecodes.FLOAD_2        :
        case Bytecodes.FLOAD_3        : this.loadLocal(opcode - Bytecodes.FLOAD_0, Kind.Float); break;
        case Bytecodes.DLOAD_0        :
        case Bytecodes.DLOAD_1        :
        case Bytecodes.DLOAD_2        :
        case Bytecodes.DLOAD_3        : this.loadLocal(opcode - Bytecodes.DLOAD_0, Kind.Double); break;
        case Bytecodes.ALOAD_0        :
        case Bytecodes.ALOAD_1        :
        case Bytecodes.ALOAD_2        :
        case Bytecodes.ALOAD_3        : this.loadLocal(opcode - Bytecodes.ALOAD_0, Kind.Reference); break;

//        case Bytecodes.IALOAD         : genLoadIndexed(Kind.Int); break;
//        case Bytecodes.LALOAD         : genLoadIndexed(Kind.Long); break;
//        case Bytecodes.FALOAD         : genLoadIndexed(Kind.Float); break;
//        case Bytecodes.DALOAD         : genLoadIndexed(Kind.Double); break;
//        case Bytecodes.AALOAD         : genLoadIndexed(Kind.Reference); break;
//        case Bytecodes.BALOAD         : genLoadIndexed(Kind.Byte); break;
//        case Bytecodes.CALOAD         : genLoadIndexed(Kind.Char); break;
//        case Bytecodes.SALOAD         : genLoadIndexed(Kind.Short); break;
        case Bytecodes.ISTORE         : this.storeLocal(Kind.Int, stream.readLocalIndex()); break;
        case Bytecodes.LSTORE         : this.storeLocal(Kind.Long, stream.readLocalIndex()); break;
        case Bytecodes.FSTORE         : this.storeLocal(Kind.Float, stream.readLocalIndex()); break;
        case Bytecodes.DSTORE         : this.storeLocal(Kind.Double, stream.readLocalIndex()); break;
        case Bytecodes.ASTORE         : this.storeLocal(Kind.Reference, stream.readLocalIndex()); break;
        case Bytecodes.ISTORE_0       :
        case Bytecodes.ISTORE_1       :
        case Bytecodes.ISTORE_2       :
        case Bytecodes.ISTORE_3       : this.storeLocal(Kind.Int, opcode - Bytecodes.ISTORE_0); break;
        case Bytecodes.LSTORE_0       :
        case Bytecodes.LSTORE_1       :
        case Bytecodes.LSTORE_2       :
        case Bytecodes.LSTORE_3       : this.storeLocal(Kind.Long, opcode - Bytecodes.LSTORE_0); break;
        case Bytecodes.FSTORE_0       :
        case Bytecodes.FSTORE_1       :
        case Bytecodes.FSTORE_2       :
        case Bytecodes.FSTORE_3       : this.storeLocal(Kind.Float, opcode - Bytecodes.FSTORE_0); break;
        case Bytecodes.DSTORE_0       :
        case Bytecodes.DSTORE_1       :
        case Bytecodes.DSTORE_2       :
        case Bytecodes.DSTORE_3       : this.storeLocal(Kind.Double, opcode - Bytecodes.DSTORE_0); break;
        case Bytecodes.ASTORE_0       :
        case Bytecodes.ASTORE_1       :
        case Bytecodes.ASTORE_2       :
        case Bytecodes.ASTORE_3       : this.storeLocal(Kind.Reference, opcode - Bytecodes.ASTORE_0); break;

        /*
        case Bytecodes.IASTORE        : genStoreIndexed(Kind.Int   ); break;
        case Bytecodes.LASTORE        : genStoreIndexed(Kind.Long  ); break;
        case Bytecodes.FASTORE        : genStoreIndexed(Kind.Float ); break;
        case Bytecodes.DASTORE        : genStoreIndexed(Kind.Double); break;
        case Bytecodes.AASTORE        : genStoreIndexed(Kind.Reference); break;
        case Bytecodes.BASTORE        : genStoreIndexed(Kind.Byte  ); break;
        case Bytecodes.CASTORE        : genStoreIndexed(Kind.Char  ); break;
        case Bytecodes.SASTORE        : genStoreIndexed(Kind.Short ); break;
        */
        case Bytecodes.POP            :
        case Bytecodes.POP2           :
        case Bytecodes.DUP            :
        case Bytecodes.DUP_X1         :
        case Bytecodes.DUP_X2         :
        case Bytecodes.DUP2           :
        case Bytecodes.DUP2_X1        :
        case Bytecodes.DUP2_X2        :
        case Bytecodes.SWAP           : this.stackOp(opcode); break;

        case Bytecodes.IADD           :
        case Bytecodes.ISUB           :
        case Bytecodes.IMUL           : this.genArithmeticOp(Kind.Int, opcode, false); break;
        case Bytecodes.IDIV           :
        case Bytecodes.IREM           : this.genArithmeticOp(Kind.Int, opcode, true); break;
        case Bytecodes.LADD           :
        case Bytecodes.LSUB           :
        case Bytecodes.LMUL           : this.genArithmeticOp(Kind.Long, opcode, false); break;
        case Bytecodes.LDIV           :
        case Bytecodes.LREM           : this.genArithmeticOp(Kind.Long, opcode, true); break;
        case Bytecodes.FADD           :
        case Bytecodes.FSUB           :
        case Bytecodes.FMUL           :
        case Bytecodes.FDIV           :
        case Bytecodes.FREM           : this.genArithmeticOp(Kind.Float, opcode, false); break;
        case Bytecodes.DADD           :
        case Bytecodes.DSUB           :
        case Bytecodes.DMUL           :
        case Bytecodes.DDIV           :
        case Bytecodes.DREM           : this.genArithmeticOp(Kind.Double, opcode, false); break;
        /*
        case Bytecodes.INEG           : genNegateOp(Kind.Int); break;
        case Bytecodes.LNEG           : genNegateOp(Kind.Long); break;
        case Bytecodes.FNEG           : genNegateOp(Kind.Float); break;
        case Bytecodes.DNEG           : genNegateOp(Kind.Double); break;
        case Bytecodes.ISHL           :
        case Bytecodes.ISHR           :
        case Bytecodes.IUSHR          : genShiftOp(Kind.Int, opcode); break;
        case Bytecodes.IAND           :
        case Bytecodes.IOR            :
        case Bytecodes.IXOR           : genLogicOp(Kind.Int, opcode); break;
        case Bytecodes.LSHL           :
        case Bytecodes.LSHR           :
        case Bytecodes.LUSHR          : genShiftOp(Kind.Long, opcode); break;
        case Bytecodes.LAND           :
        case Bytecodes.LOR            :
        case Bytecodes.LXOR           : genLogicOp(Kind.Long, opcode); break;
        */
        case Bytecodes.IINC           : this.genIncrement(stream); break;
        /*
        case Bytecodes.I2L            : genConvert(ConvertNode.Op.I2L); break;
        case Bytecodes.I2F            : genConvert(ConvertNode.Op.I2F); break;
        case Bytecodes.I2D            : genConvert(ConvertNode.Op.I2D); break;
        case Bytecodes.L2I            : genConvert(ConvertNode.Op.L2I); break;
        case Bytecodes.L2F            : genConvert(ConvertNode.Op.L2F); break;
        case Bytecodes.L2D            : genConvert(ConvertNode.Op.L2D); break;
        case Bytecodes.F2I            : genConvert(ConvertNode.Op.F2I); break;
        case Bytecodes.F2L            : genConvert(ConvertNode.Op.F2L); break;
        case Bytecodes.F2D            : genConvert(ConvertNode.Op.F2D); break;
        case Bytecodes.D2I            : genConvert(ConvertNode.Op.D2I); break;
        case Bytecodes.D2L            : genConvert(ConvertNode.Op.D2L); break;
        case Bytecodes.D2F            : genConvert(ConvertNode.Op.D2F); break;
        case Bytecodes.I2B            : genConvert(ConvertNode.Op.I2B); break;
        case Bytecodes.I2C            : genConvert(ConvertNode.Op.I2C); break;
        case Bytecodes.I2S            : genConvert(ConvertNode.Op.I2S); break;
        case Bytecodes.LCMP           : genCompareOp(Kind.Long, false); break;
        case Bytecodes.FCMPL          : genCompareOp(Kind.Float, true); break;
        case Bytecodes.FCMPG          : genCompareOp(Kind.Float, false); break;
        case Bytecodes.DCMPL          : genCompareOp(Kind.Double, true); break;
        case Bytecodes.DCMPG          : genCompareOp(Kind.Double, false); break;
        case Bytecodes.IFEQ           : genIfZero(Operator.EQ); break;
        case Bytecodes.IFNE           : genIfZero(Condition.NE); break;
        case Bytecodes.IFLT           : genIfZero(Condition.LT); break;
        case Bytecodes.IFGE           : genIfZero(Condition.GE); break;
        case Bytecodes.IFGT           : genIfZero(Condition.GT); break;
        case Bytecodes.IFLE           : genIfZero(Condition.LE); break;
        case Bytecodes.IF_ICMPEQ      : genIfSame(Kind.Int, Condition.EQ); break;
        */
        case Bytecodes.IF_ICMPNE      : this.genIfSame(stream, Kind.Int, Condition.NE); break;
        case Bytecodes.IF_ICMPLT      : this.genIfSame(stream, Kind.Int, Condition.LT); break;
        case Bytecodes.IF_ICMPGE      : this.genIfSame(stream, Kind.Int, Condition.GE); break;
        case Bytecodes.IF_ICMPGT      : this.genIfSame(stream, Kind.Int, Condition.GT); break;
        case Bytecodes.IF_ICMPLE      : this.genIfSame(stream, Kind.Int, Condition.LE); break;
        case Bytecodes.IF_ACMPEQ      : this.genIfSame(stream, Kind.Reference, Condition.EQ); break;
        case Bytecodes.IF_ACMPNE      : this.genIfSame(stream, Kind.Reference, Condition.NE); break;
        case Bytecodes.GOTO           : this.genGoto(stream); break;
        /*
        case Bytecodes.JSR            : genJsr(stream.readBranchDest()); break;
        case Bytecodes.RET            : genRet(stream.readLocalIndex()); break;
        case Bytecodes.TABLESWITCH    : genTableswitch(); break;
        case Bytecodes.LOOKUPSWITCH   : genLookupswitch(); break;
        */
        case Bytecodes.IRETURN        : this.genReturn(state.ipop()); break;
        /*
        case Bytecodes.LRETURN        : genReturn(state.lpop()); break;
        case Bytecodes.FRETURN        : genReturn(state.fpop()); break;
        case Bytecodes.DRETURN        : genReturn(state.dpop()); break;
        case Bytecodes.ARETURN        : genReturn(state.apop()); break;
        case Bytecodes.RETURN         : genReturn(null); break;
        case Bytecodes.GETSTATIC      : cpi = stream.readCPI(); genGetStatic(cpi, lookupField(cpi, opcode)); break;
        case Bytecodes.PUTSTATIC      : cpi = stream.readCPI(); genPutStatic(cpi, lookupField(cpi, opcode)); break;
        case Bytecodes.GETFIELD       : cpi = stream.readCPI(); genGetField(cpi, lookupField(cpi, opcode)); break;
        case Bytecodes.PUTFIELD       : cpi = stream.readCPI(); genPutField(cpi, lookupField(cpi, opcode)); break;
        case Bytecodes.INVOKEVIRTUAL  : cpi = stream.readCPI(); genInvokeVirtual(lookupMethod(cpi, opcode), cpi, constantPool); break;
        case Bytecodes.INVOKESPECIAL  : cpi = stream.readCPI(); genInvokeSpecial(lookupMethod(cpi, opcode), null, cpi, constantPool); break;
        case Bytecodes.INVOKESTATIC   : cpi = stream.readCPI(); genInvokeStatic(lookupMethod(cpi, opcode), cpi, constantPool); break;
        case Bytecodes.INVOKEINTERFACE: cpi = stream.readCPI(); genInvokeInterface(lookupMethod(cpi, opcode), cpi, constantPool); break;
        */
        case Bytecodes.NEW            : this.genNewInstance(stream.readCPI()); break;
        /*
        case Bytecodes.NEWARRAY       : genNewTypeArray(stream.readLocalIndex()); break;
        case Bytecodes.ANEWARRAY      : genNewObjectArray(stream.readCPI()); break;
        case Bytecodes.ARRAYLENGTH    : genArrayLength(); break;
        case Bytecodes.ATHROW         : genThrow(stream.currentBCI()); break;
        case Bytecodes.CHECKCAST      : genCheckCast(); break;
        case Bytecodes.INSTANCEOF     : genInstanceOf(); break;
        case Bytecodes.MONITORENTER   : genMonitorEnter(state.apop()); break;
        case Bytecodes.MONITOREXIT    : genMonitorExit(state.apop()); break;
        case Bytecodes.MULTIANEWARRAY : genNewMultiArray(stream.readCPI()); break;
        case Bytecodes.IFNULL         : genIfNull(Condition.EQ); break;
        case Bytecodes.IFNONNULL      : genIfNull(Condition.NE); break;
        case Bytecodes.GOTO_W         : genGoto(stream.readFarBranchDest()); break;
        case Bytecodes.JSR_W          : genJsr(stream.readFarBranchDest()); break;
        case Bytecodes.BREAKPOINT:
            throw new CiBailout("concurrent setting of breakpoint");
        default:
            throw new CiBailout("Unsupported opcode " + opcode + " (" + nameOf(opcode) + ") [bci=" + bci + "]");
      }
      */
        default:
          Debug.somewhatImplemented(Bytecodes[opcode]);
      }
      writer.leave("State  After: " + Bytecodes[opcode].padRight(" ", 12) + " " + state.toString());
      writer.writeLn("");
    }
  }
}


































