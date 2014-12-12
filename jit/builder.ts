module J2ME {

  var debug = false;
  var writer = null; // new IndentingWriter(true);
  var consoleWriter = new IndentingWriter();

  export var counter = new J2ME.Metrics.Counter(true);

  export function printResults() {
    counter.trace(stderrWriter);
    consoleWriter.writeLn(JSON.stringify(staticCallGraph, null, 2));
  }

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


  import mangleMethod = J2ME.C4.Backend.mangleMethod;
  import mangleClass = J2ME.C4.Backend.mangleClass;
  import mangleField = J2ME.C4.Backend.mangleField;

  function kindsFromSignature(signature: string) {

  }

  var staticCallGraph = Object.create(null);

  declare var Long: any;
  declare var VM: any;

  function conditionToOperator(condition: Condition): Operator {
    switch (condition) {
      case Condition.EQ: return Operator.EQ;
      case Condition.NE: return Operator.NE;
      case Condition.LT: return Operator.LT;
      case Condition.LE: return Operator.LE;
      case Condition.GT: return Operator.GT;
      case Condition.GE: return Operator.GE;
      default: throw "TODO"
    }
  }

  export function isTwoSlot(kind: Kind) {
    return kind === Kind.Long || kind === Kind.Double;
  }

  export function assertHigh(x: Value) {
    assert (x === null);
  }

  export enum CompilationTarget {
    Runtime,
    Buildtime,
    Static
  }

  export enum TAGS {
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

  export interface ExceptionHandler {
    start_pc: number;
    end_pc: number;
    handler_pc: number;
    catch_type: number;
  }

  export class CompiledMethodInfo {
    constructor(public args: string [], public body: string, public referencedClasses: ClassInfo []) {
      // ...
    }
  }

  function assertKind(kind: Kind, x: Node): Node {
    assert(stackKind(x.kind) === stackKind(kind), "Got " + kindCharacter(stackKind(x.kind)) + " expected " + kindCharacter(stackKind(kind)));
    return x;
  }

  export class State {
    private static _nextID = 0;
    id: number;
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
        phi.kind = x.kind;
        phi.isLoop = true;
        return phi;
      }
      s.bci = this.bci;
      s.local = this.local.map(function (v, i) {
        if (v === null) {
          return null;
        }
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
      var phi;
      if (a instanceof Phi && a.control === control) {
        phi = a;
      } else {
        phi = new Phi(control, a);
        phi.kind = a.kind;
      }
      if (a.kind === Kind.Store) {
        release || assert(b.kind === Kind.Store, "Got " + Kind[b.kind] + " should be store.");
      } else if (b === null || b === Illegal || stackKind(a.kind) !== stackKind(b.kind)) {
        // TODO get rid of the null check by pushing Illegals for doubles/longs.
        b = Illegal;
      }
      phi.pushValue(b);
      return phi;
    }

    static mergeValues(control: Control, a: Value [], b: Value []) {
      for (var i = 0; i < a.length; i++) {
        if (a[i] === null) {
          continue;
        }
        a[i] = State.mergeValue(control, a[i], b[i]);
        if (isTwoSlot(a[i].kind)) {
          i++;
        }
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

    static toBriefString(x: Node) : string {
      if (x instanceof Node) {
        return kindCharacter(x.kind); // + x.toString(true);
      }
      if (x === null) {
        return "_";
      } else if (x === undefined) {
        return "undefined";
      }
      return String(x);
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
      this.push(Kind.Int, x);
    }

    /**
     * Pushes a value onto the stack and checks that it is a float.
     * @param x the instruction to push onto the stack
     */
    public fpush(x: Node) {
      this.push(Kind.Float, x);
    }

    /**
     * Pushes a value onto the stack and checks that it is an object.
     */
    public apush(x: Node) {
      this.push(Kind.Reference, x);
    }

    /**
     * Pushes a value onto the stack and checks that it is a long.
     */
    public lpush(x: Node) {
      this.push(Kind.Long, x);
    }

    /**
     * Pushes a value onto the stack and checks that it is a double.
     */
    public dpush(x: Node) {
      this.push(Kind.Double, x);
    }

    /**
     * Pushes an instruction onto the stack with the expected type.
     */
    public push(kind: Kind, x: Value) {
      assert (kind !== Kind.Void);
      if (x.kind === undefined) {
        x.kind = kind;
      }

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
      kind = stackKind(kind);
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

    public peek(): Value {
      return this.stack[this.stack.length - 1];
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

  export function quote(s) {
    return "\"" + s + "\"";
  }

  export function compileMethodInfo(methodInfo: MethodInfo, ctx: Context, target: CompilationTarget): CompiledMethodInfo {
    if (!methodInfo.code) {
      counter.count("Cannot Compile: Method has no code.");
      return;
    }
    counter.count("Trying to Compile");
    //if (methodInfo.isSynchronized) {
    //  counter.count("Cannot Compile: Method is synchronized.");
    //  return;
    //} else
    if (methodInfo.exception_table.length) {
      counter.count("Cannot Compile: Method has exception handlers.");
      return;
    }
    var builder = new Builder(methodInfo, ctx, target);
    var fn;
    try {
      var compilation = builder.build();
      var args = [];
      for (var i = 0; i < builder.parameters.length; i++) {
        var parameter = builder.parameters[i];
        args.push(parameter.name);
      }
      var body = compilation.body;
      // consoleWriter.writeLn(fnSource);
      // var fn = new Function(args.join(","), body);
      // consoleWriter.writeLn(fn.toString());
      // debug && writer.writeLn(fn.toString());
      counter.count("Compiled");
    } catch (e) {
      counter.count("Failed to Compile " + e);
      //consoleWriter.writeLn(e);
      //consoleWriter.writeLns(e.stack);
      debug && Debug.warning("Failed to compile " + methodInfo.implKey + " " + e + " " + e.stack);
      if (e.message.indexOf("Not Implemented ") === -1) {
        throw e;
      }
    }

    return new CompiledMethodInfo(args, body, builder.referencedClasses);
  }

  interface WorklistItem {
    region: Region;
    block: Block;
  }

  interface Stop {

  }
  
  function genConstant(x: any, kind: Kind): IR.Constant {
    var constant;
    if (kind === Kind.Long) {
      constant = new IR.JVMLong(x, 0);
    } else if (kind === Kind.Reference) {
      if (isString(x)) {
        constant = new IR.JVMString(x);
      } else {
        constant = new IR.Constant(x);
      }
    } else {
      constant = new IR.Constant(x);
    }
    constant.kind = kind;
    return constant;
  }

  var Illegal = genConstant(undefined, Kind.Illegal);

  class StopInfo {
    constructor(
      public control: Control,
      public target: Block,
      public state: State) {
      // ...
    }
  }

  class ReturnInfo {
    constructor(
      public control: Control,
      public store: Node,
      public value: Value) {
      // ...
    }
  }


  /**
   * TODO: Consider using debug info for nicer parameter names, if available.
   */
  function getParameterName(methodInfo: MethodInfo, i: number): string {
    var parameterNames = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    return i < parameterNames.length ? parameterNames[i] : "p" + (parameterNames.length - i);
  }

  class Builder {
    peepholeOptimizer: PeepholeOptimizer;
    signatureDescriptor: SignatureDescriptor;

    /**
     * Current state vector.
     */
    state: State;

    /**
     * Current region.
     */
    region: Region;

    /**
     * Stop infos accumulated for the last processed block.
     */
    blockStopInfos: StopInfo [];

    /**
     * Methor return infos accumulated during the processing of this method.
     */
    methodReturnInfos: ReturnInfo [];

    /**
     * Current block map.
     */
    blockMap: BlockMap;

    parameters: IR.Parameter [];

    referencedClasses: ClassInfo [];

    constructor(public methodInfo: MethodInfo, public ctx: Context, public target: CompilationTarget) {
      // ...
      this.peepholeOptimizer = new PeepholeOptimizer();
      this.signatureDescriptor = SignatureDescriptor.makeSignatureDescriptor(methodInfo.signature);
      this.methodReturnInfos = [];
      this.parameters = [];
      this.referencedClasses = [];
    }

    build(): C4.Backend.Compilation {
      IR.Node.startNumbering();
      
      var methodInfo = this.methodInfo;

      writer && writer.enter("Compiling Method: " + methodInfo.name + " " + methodInfo.signature + " {");
      writer && writer.writeLn("Size: " + methodInfo.code.length);
      var blockMap = this.blockMap = new BlockMap(methodInfo);
      blockMap.build();

      // consoleWriter.writeLn("Compiling Method: " + methodInfo.name + " " + methodInfo.signature + " {");
      // blockMap.trace(consoleWriter, false);

      var start = this.buildStart();
      var dfg = this.buildGraph(start, start.entryState.clone());

      writer && dfg.trace(writer);

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

      writer && cfg.trace(writer);

      enterTimeline("Verify IR");
      cfg.verify();
      leaveTimeline();

      enterTimeline("Allocate Variables");
      cfg.allocateVariables();
      leaveTimeline();

      enterTimeline("Generate Source");
      var result = C4.Backend.generate(cfg);
      leaveTimeline();

      Node.stopNumbering();
      leaveTimeline();

      writer && writer.leave("}");
      IR.Node.stopNumbering();

      return result;
    }

    buildStart(): IR.Start {
      var start = new IR.Start();
      var state = start.entryState = new State();
      var methodInfo = this.methodInfo;

      for (var i = 0; i < methodInfo.max_locals; i++) {
        state.local.push(null);
      }

      state.store = new IR.Projection(start, ProjectionType.STORE);
      state.store.kind = Kind.Store;

      var signatureDescriptor = this.signatureDescriptor;
      writer && writer.writeLn("SIG: " + signatureDescriptor);

      var typeDescriptors = signatureDescriptor.typeDescriptors;

      var localIndex = 0;
      var parameterIndex = 1;
      if (!methodInfo.isStatic) {
        var self = new IR.This(start);
        self.kind = Kind.Reference;
        state.storeLocal(0, self);
        parameterIndex++;
        localIndex = 1;
      }
      // Skip the first typeDescriptor since it is the return type.
      for (var i = 1; i < typeDescriptors.length; i++) {
        var kind = Kind.Reference;
        if (typeDescriptors[i] instanceof AtomicTypeDescriptor) {
          kind = (<AtomicTypeDescriptor>typeDescriptors[i]).kind;
        }
        var parameter = new IR.Parameter(start, parameterIndex, getParameterName(this.methodInfo, parameterIndex - 1));
        this.parameters.push(parameter);
        parameter.kind = kind;
        parameterIndex++;
        state.storeLocal(localIndex, parameter);
        localIndex += isTwoSlot(kind) ? 2 : 1;
      }
      return start;
    }

    buildGraph(start: Region, state: State): IR.DFG {
      var worklist = new SortedList<WorklistItem>(function compare(a: WorklistItem, b: WorklistItem) {
        return a.block.blockID - b.block.blockID;
      });

      worklist.push({
        region: start,
        block: this.blockMap.blocks[0]
      });

      var next: WorklistItem;
      while ((next = worklist.pop())) {
        writer && writer.writeLn("Processing: " + next.region + " " + next.block.blockID + " " + next.region.entryState);
        this.buildBlock(next.region, next.block, next.region.entryState.clone());
        if (!this.blockStopInfos) {
          continue;
        }
        this.blockStopInfos.forEach(function (stop: StopInfo) {
          var target = stop.target;
          writer && writer.writeLn(String(target));
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
      var stop;
      var returnInfos = this.methodReturnInfos;
      assert (returnInfos.length > 0);
      var returnRegion = new Region(null);
      var returnValuePhi = new Phi(returnRegion, null);
      var returnStorePhi = new Phi(returnRegion, null);
      returnInfos.forEach(function (returnInfo) {
        returnRegion.predecessors.push(returnInfo.control);
        returnValuePhi.pushValue(returnInfo.value);
        returnStorePhi.pushValue(returnInfo.store);
      });
      stop = new IR.Stop(returnRegion, returnStorePhi, returnValuePhi);
      return new IR.DFG(stop);
    }

    buildBlock(region: Region, block: Block, state: State) {
      this.blockStopInfos = null;
      this.state = state;
      this.region = region;
      var code = this.methodInfo.code;
      var stream = new BytecodeStream(code);
      var bci = block.startBci;
      stream.setBCI(bci);

      while (stream.currentBCI <= block.endBci) {
        state.bci = bci;
        this.processBytecode(stream, state);
        if (Bytecode.isReturn(stream.currentBC()) ||
            Bytecodes.ATHROW === stream.currentBC()) {
          release || assert (!this.blockStopInfos, "Should not have any stops.");
          return;
        }
        stream.next();
        bci = stream.currentBCI;
      }

      if (!this.blockStopInfos) {
        this.blockStopInfos = [new StopInfo(region,
          this.blockMap.getBlock(stream.currentBCI),
          this.state
        )];
      }
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
      if (canTrap) {
        this.genDivideByZeroCheck(y);
      }
      var v;
//      var isStrictFP = false; // TODO
      switch(opcode) {
        case Bytecodes.IADD: v = new IR.Binary(Operator.IADD, x, y); break;
        case Bytecodes.LADD: v = new IR.JVMLongBinary(Operator.LADD, x, y); break;
        case Bytecodes.FADD: v = new IR.Binary(Operator.FADD, x, y/*, isStrictFP*/); break;
        case Bytecodes.DADD: v = new IR.Binary(Operator.DADD, x, y/*, isStrictFP*/); break;
        case Bytecodes.ISUB: v = new IR.Binary(Operator.ISUB, x, y); break;
        case Bytecodes.LSUB: v = new IR.JVMLongBinary(Operator.LSUB, x, y); break;
        case Bytecodes.FSUB: v = new IR.Binary(Operator.FSUB, x, y/*, isStrictFP*/); break;
        case Bytecodes.DSUB: v = new IR.Binary(Operator.DSUB, x, y/*, isStrictFP*/); break;
        case Bytecodes.IMUL: v = new IR.Binary(Operator.IMUL, x, y); break;
        case Bytecodes.LMUL: v = new IR.JVMLongBinary(Operator.LMUL, x, y); break;
        case Bytecodes.FMUL: v = new IR.Binary(Operator.FMUL, x, y/*, isStrictFP*/); break;
        case Bytecodes.DMUL: v = new IR.Binary(Operator.DMUL, x, y/*, isStrictFP*/); break;
        case Bytecodes.IDIV: v = new IR.Binary(Operator.IDIV, x, y); break;
        case Bytecodes.LDIV: v = new IR.JVMLongBinary(Operator.LDIV, x, y); break;
        case Bytecodes.FDIV: v = new IR.Binary(Operator.FDIV, x, y/*, isStrictFP*/); break;
        case Bytecodes.DDIV: v = new IR.Binary(Operator.DDIV, x, y/*, isStrictFP*/); break;
        case Bytecodes.IREM: v = new IR.Binary(Operator.IREM, x, y); break;
        case Bytecodes.LREM: v = new IR.JVMLongBinary(Operator.LREM, x, y); break;
        case Bytecodes.FREM: v = new IR.Binary(Operator.FREM, x, y/*, isStrictFP*/); break;
        case Bytecodes.DREM: v = new IR.Binary(Operator.DREM, x, y/*, isStrictFP*/); break;
        default:
          assert(false);
      }
      v = this.peepholeOptimizer.fold(v);
      state.push(result, v);
    }

    genShiftOp(kind: Kind, opcode: Bytecodes) {
      var state = this.state;
      var s = state.ipop();
      var x = state.pop(kind);
      var v;
      switch(opcode){
        case Bytecodes.ISHL: v = new IR.Binary(Operator.LSH, x, s); break;
        case Bytecodes.LSHL: v = new IR.JVMLongBinary(Operator.LSH, x, s); break;
        case Bytecodes.ISHR: v = new IR.Binary(Operator.RSH, x, s); break;
        case Bytecodes.LSHR: v = new IR.JVMLongBinary(Operator.RSH, x, s); break;
        case Bytecodes.IUSHR: v = new IR.Binary(Operator.URSH, x, s); break;
        case Bytecodes.LUSHR: v = new IR.JVMLongBinary(Operator.URSH, x, s); break;
        default:
          assert(false);
      }
      state.push(kind, v);
    }

    genLogicOp(kind: Kind, opcode: Bytecodes) {
      var state = this.state;
      var y = state.pop(kind);
      var x = state.pop(kind);
      var v;
      switch(opcode){
        case Bytecodes.IAND: v = new IR.Binary(Operator.AND, x, y); break;
        case Bytecodes.LAND: v = new IR.JVMLongBinary(Operator.AND, x, y); break;
        case Bytecodes.IOR: v = new IR.Binary(Operator.OR, x, y); break;
        case Bytecodes.LOR: v = new IR.JVMLongBinary(Operator.OR, x, y); break;
        case Bytecodes.IXOR: v = new IR.Binary(Operator.XOR, x, y); break;
        case Bytecodes.LXOR: v = new IR.JVMLongBinary(Operator.XOR, x, y); break;
        default:
          assert(false);
      }
      state.push(kind, v);
    }

    genNegateOp(kind: Kind) {
      var x = this.state.pop(kind);
      var v;
      switch (kind) {
        case Kind.Int: v = new IR.Unary(Operator.INEG, x); break;
        case Kind.Long: v = new IR.JVMLongUnary(Operator.LNEG, x); break;
        case Kind.Float: v = new IR.Unary(Operator.FNEG, x); break;
        case Kind.Double: v = new IR.Unary(Operator.DNEG, x); break;
        default:
          assert(false);
      }
      this.state.push(kind, v);
    }

    genNewInstance(cpi: number) {
      var classInfo = this.lookupClass(cpi);
      var jvmNew = new IR.JVMNew(this.region, this.state.store, classInfo);
      this.recordStore(jvmNew);
      this.state.apush(jvmNew);
    }

    genNewTypeArray(typeCode: number) {
      var kind = arrayTypeCodeToKind(typeCode);
      var length = this.state.ipop();
      var result = new IR.JVMNewArray(this.region, this.state.store, kind, length);
      this.recordStore(result);
      this.state.apush(result);
    }

    genNewObjectArray(cpi: number) {
      var classInfo = this.lookupClass(cpi);
      var length = this.state.ipop();
      var result = new IR.JVMNewObjectArray(this.region, this.state.store, classInfo, length);
      this.recordStore(result);
      this.state.apush(result);
    }

    genLoadConstant(cpi: number, state: State) {
      var cp = this.methodInfo.classInfo.constant_pool;
      var entry = cp[cpi];
      switch (entry.tag) {
        case TAGS.CONSTANT_Integer:
          state.ipush(genConstant(entry.integer, Kind.Int));
          return;
        case TAGS.CONSTANT_Float:
          state.fpush(genConstant(entry.float, Kind.Float));
          return;
        case TAGS.CONSTANT_Double:
          state.dpush(genConstant(entry.double, Kind.Double));
          return;
        case 5: // TAGS.CONSTANT_Long
          state.lpush(new IR.JVMLong(entry.lowBits, entry.highBits));
          return;
        case TAGS.CONSTANT_String:
          entry = cp[entry.string_index];
          return state.push(Kind.Reference, genConstant(entry.bytes, Kind.Reference));

        default:
          throw "Not done for: " + entry.tag;
      }
    }

    genCheckCast(cpi: Bytecodes) {
      var classInfo = this.lookupClass(cpi);
      var object = this.state.peek();
      var checkCast = new IR.JVMCheckCast(this.region, this.state.store, object, classInfo);
      this.recordStore(checkCast);
    }

    genInstanceOf(cpi: Bytecodes) {
      var classInfo = this.lookupClass(cpi);
      var object = this.state.apop();
      var instanceOf = new IR.JVMInstanceOf(this.region, this.state.store, object, classInfo);
      this.recordStore(instanceOf);
      this.state.push(Kind.Boolean, instanceOf);
    }

    genIncrement(stream: BytecodeStream) {
      var index = stream.readLocalIndex();
      var local = this.state.loadLocal(index);
      var increment = genConstant(stream.readIncrement(), Kind.Int);
      var value = new IR.Binary(Operator.IADD, local, increment);
      value.kind = stackKind(local.kind);
      this.state.storeLocal(index, value);
    }

    genConvert(from: Kind, to: Kind) {
      var value = this.state.pop(from);
      this.state.push(to, new IR.JVMConvert(from, to, value));
    }

    genIf(stream: BytecodeStream, predicate: IR.Binary) {
      release || assert (!this.blockStopInfos);
      var _if = new IR.If(this.region, predicate);
      this.blockStopInfos = [new StopInfo(
        <Control><any>new IR.Projection(_if, ProjectionType.TRUE),
        this.blockMap.getBlock(stream.readBranchDest()),
        this.state
      ), new StopInfo(
        <Control><any>new IR.Projection(_if, ProjectionType.FALSE),
        this.blockMap.getBlock(stream.nextBCI),
        this.state
      )];
    }

    genIfNull(stream: BytecodeStream, condition: Condition) {
      this.state.apush(Null);
      var y = this.state.apop();
      var x = this.state.apop();
      this.genIf(stream, new IR.Binary(conditionToOperator(condition), x, y));
    }

    genIfSame(stream: BytecodeStream, kind: Kind, condition: Condition) {
      var y = this.state.pop(kind);
      var x = this.state.pop(kind);
      this.genIf(stream, new IR.Binary(conditionToOperator(condition), x, y));
    }

    genIfZero(stream: BytecodeStream, condition: Condition) {
      this.state.ipush(genConstant(0, Kind.Int));
      var y = this.state.ipop();
      var x = this.state.ipop();
      this.genIf(stream, new IR.Binary(conditionToOperator(condition), x, y));
    }

    genCompareOp(kind: Kind, isLessThan: boolean) {
      var b = this.state.pop(kind);
      var a = this.state.pop(kind);
      var compare;
      if (kind === Kind.Long) {
        compare = new IR.JVMLongCompare(this.region, a, b);
      } else {
        compare = new IR.JVMFloatCompare(this.region, a, b, isLessThan);
      }
      this.state.ipush(compare);
    }

    genGoto(stream: BytecodeStream) {
      release || assert (!this.blockStopInfos);
      this.blockStopInfos = [new StopInfo(
        this.region,
        this.blockMap.getBlock(stream.readBranchDest()),
        this.state
      )];
    }

    genReturn(value: Value) {
      if (value === null) {
        value = Undefined;
      }
      this.methodReturnInfos.push(new ReturnInfo(
        this.region,
        this.state.store,
        value
      ));
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

    /**
     * Marks the |node| as the active store node, with dependencies on all loads appearing after the
     * previous active store node.
     */
    recordStore(node: any) {
      var state = this.state;
      state.store = new IR.Projection(node, ProjectionType.STORE);
      state.store.kind = Kind.Store;
      node.loads = state.loads.slice(0);
      state.loads.length = 0;
    }

    /**
     * Keeps track of the current set of loads.
     */
    recordLoad(node: Node): Value {
      var state = this.state;
      state.loads.push(node);
      return node;
    }

    genDivideByZeroCheck(value: Value) {
      var checkArithmetic = new IR.JVMCheckArithmetic(this.region, this.state.store, value);
      this.recordStore(checkArithmetic);
    }

    genThrow(bci: number) {
      var _throw = new IR.JVMThrow(this.region, this.state.store);
      this.recordStore(_throw);
      this.methodReturnInfos.push(new ReturnInfo(
        this.region,
        this.state.store,
        Undefined
      ));
    }

    genInvoke(methodInfo: MethodInfo, opcode: Bytecodes, nextBCI: number) {
      var callees = staticCallGraph[this.methodInfo.implKey];
      if (!callees) {
        callees = staticCallGraph[this.methodInfo.implKey] = [];
      }
      ArrayUtilities.pushUnique(callees, methodInfo.implKey);

      var signature = SignatureDescriptor.makeSignatureDescriptor(methodInfo.signature);
      var types = signature.typeDescriptors;
      var args: Value [] = [];
      for (var i = types.length - 1; i > 0; i--) {
        args.unshift(this.state.pop(types[i].kind));
      }
      var object = null;
      if (opcode !== Bytecodes.INVOKESTATIC) {
        object = this.state.pop(Kind.Reference);
      }
      var call = new IR.JVMInvoke(this.region, this.state.store, this.state.clone(nextBCI), opcode, object, methodInfo, args);
      this.recordStore(call);
      if (types[0].kind !== Kind.Void) {
        this.state.push(types[0].kind, call);
      }
    }

    genStoreIndexed(kind: Kind) {
      var value = this.state.pop(stackKind(kind));
      var index = this.state.ipop();
      var array = this.state.apop();
      var arrayStore = new IR.JVMStoreIndexed(this.region, this.state.store, kind, array, index, value);
      this.recordStore(arrayStore);
    }

    genLoadIndexed(kind: Kind) {
      var index = this.state.ipop();
      var array = this.state.apop();
      var arrayLoad = new IR.JVMLoadIndexed(this.region, this.state.store, kind, array, index);
      this.recordLoad(arrayLoad);
      this.state.push(kind, arrayLoad);
    }

    genArrayLength() {
      var array = this.state.apop();
      var getProperty = new IR.GetProperty(this.region, this.state.store, array, new Constant('length'));
      this.recordLoad(getProperty);
      this.state.ipush(getProperty);
    }

    genClass(classInfo: ClassInfo): Value {
      ArrayUtilities.pushUnique(this.referencedClasses, classInfo);
      return new IR.JVMClass(classInfo);
    }

    genGetField(fieldInfo: FieldInfo, isStatic: boolean) {
      var signature = TypeDescriptor.makeTypeDescriptor(fieldInfo.signature);
      var object = isStatic ? null : this.state.apop();
      var getField = new IR.JVMGetField(this.region, this.state.store, object, fieldInfo);
      this.recordLoad(getField);
      this.state.push(signature.kind, getField);
    }

    genPutField(fieldInfo: FieldInfo, isStatic: boolean) {
      var signature = TypeDescriptor.makeTypeDescriptor(fieldInfo.signature);
      var value = this.state.pop(signature.kind);
      var object = isStatic ? null : this.state.apop();
      var putField = new IR.JVMPutField(this.region, this.state.store, object, fieldInfo, value);
      this.recordStore(putField);
    }

    processBytecode(stream: BytecodeStream, state: State) {
      var cpi: number;
      var opcode: Bytecodes = stream.currentBC();
      writer && writer.enter("State Before: " + Bytecodes[opcode].padRight(" ", 12) + " " + state.toString());
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

        case Bytecodes.IALOAD         : this.genLoadIndexed(Kind.Int); break;
        case Bytecodes.LALOAD         : this.genLoadIndexed(Kind.Long); break;
        case Bytecodes.FALOAD         : this.genLoadIndexed(Kind.Float); break;
        case Bytecodes.DALOAD         : this.genLoadIndexed(Kind.Double); break;
        case Bytecodes.AALOAD         : this.genLoadIndexed(Kind.Reference); break;
        case Bytecodes.BALOAD         : this.genLoadIndexed(Kind.Byte); break;
        case Bytecodes.CALOAD         : this.genLoadIndexed(Kind.Char); break;
        case Bytecodes.SALOAD         : this.genLoadIndexed(Kind.Short); break;
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


        case Bytecodes.IASTORE        : this.genStoreIndexed(Kind.Int); break;
        case Bytecodes.LASTORE        : this.genStoreIndexed(Kind.Long); break;
        case Bytecodes.FASTORE        : this.genStoreIndexed(Kind.Float); break;
        case Bytecodes.DASTORE        : this.genStoreIndexed(Kind.Double); break;
        case Bytecodes.AASTORE        : this.genStoreIndexed(Kind.Reference); break;
        case Bytecodes.BASTORE        : this.genStoreIndexed(Kind.Byte); break;
        case Bytecodes.CASTORE        : this.genStoreIndexed(Kind.Char); break;
        case Bytecodes.SASTORE        : this.genStoreIndexed(Kind.Short); break;

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
        case Bytecodes.INEG           : this.genNegateOp(Kind.Int); break;
        case Bytecodes.LNEG           : this.genNegateOp(Kind.Long); break;
        case Bytecodes.FNEG           : this.genNegateOp(Kind.Float); break;
        case Bytecodes.DNEG           : this.genNegateOp(Kind.Double); break;
        case Bytecodes.ISHL           :
        case Bytecodes.ISHR           :
        case Bytecodes.IUSHR          : this.genShiftOp(Kind.Int, opcode); break;
        case Bytecodes.IAND           :
        case Bytecodes.IOR            :
        case Bytecodes.IXOR           : this.genLogicOp(Kind.Int, opcode); break;
        case Bytecodes.LSHL           :
        case Bytecodes.LSHR           :
        case Bytecodes.LUSHR          : this.genShiftOp(Kind.Long, opcode); break;
        case Bytecodes.LAND           :
        case Bytecodes.LOR            :
        case Bytecodes.LXOR           : this.genLogicOp(Kind.Long, opcode); break;
        case Bytecodes.IINC           : this.genIncrement(stream); break;
        case Bytecodes.I2L            : this.genConvert(Kind.Int, Kind.Long); break;
        case Bytecodes.I2F            : this.genConvert(Kind.Int, Kind.Float); break;
        case Bytecodes.I2D            : this.genConvert(Kind.Int, Kind.Double); break;
        case Bytecodes.L2I            : this.genConvert(Kind.Long, Kind.Int); break;
        case Bytecodes.L2F            : this.genConvert(Kind.Long, Kind.Float); break;
        case Bytecodes.L2D            : this.genConvert(Kind.Long, Kind.Double); break;
        case Bytecodes.F2I            : this.genConvert(Kind.Float, Kind.Int); break;
        case Bytecodes.F2L            : this.genConvert(Kind.Float, Kind.Long); break;
        case Bytecodes.F2D            : this.genConvert(Kind.Float, Kind.Double); break;
        case Bytecodes.D2I            : this.genConvert(Kind.Double, Kind.Int); break;
        case Bytecodes.D2L            : this.genConvert(Kind.Double, Kind.Long); break;
        case Bytecodes.D2F            : this.genConvert(Kind.Double, Kind.Float); break;
        case Bytecodes.I2B            : this.genConvert(Kind.Int, Kind.Byte); break;
        case Bytecodes.I2C            : this.genConvert(Kind.Int, Kind.Char); break;
        case Bytecodes.I2S            : this.genConvert(Kind.Int, Kind.Short); break;

        case Bytecodes.LCMP           : this.genCompareOp(Kind.Long, false); break;
        case Bytecodes.FCMPL          : this.genCompareOp(Kind.Float, true); break;
        case Bytecodes.FCMPG          : this.genCompareOp(Kind.Float, false); break;
        case Bytecodes.DCMPL          : this.genCompareOp(Kind.Double, true); break;
        case Bytecodes.DCMPG          : this.genCompareOp(Kind.Double, false); break;
        case Bytecodes.IFEQ           : this.genIfZero(stream, Condition.EQ); break;
        case Bytecodes.IFNE           : this.genIfZero(stream, Condition.NE); break;
        case Bytecodes.IFLT           : this.genIfZero(stream, Condition.LT); break;
        case Bytecodes.IFGE           : this.genIfZero(stream, Condition.GE); break;
        case Bytecodes.IFGT           : this.genIfZero(stream, Condition.GT); break;
        case Bytecodes.IFLE           : this.genIfZero(stream, Condition.LE); break;
        case Bytecodes.IF_ICMPEQ      : this.genIfSame(stream, Kind.Int, Condition.EQ); break;
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
        case Bytecodes.LRETURN        : this.genReturn(state.lpop()); break;
        case Bytecodes.FRETURN        : this.genReturn(state.fpop()); break;
        case Bytecodes.DRETURN        : this.genReturn(state.dpop()); break;
        case Bytecodes.ARETURN        : this.genReturn(state.apop()); break;
        case Bytecodes.RETURN         : this.genReturn(null); break;
        case Bytecodes.GETSTATIC      : cpi = stream.readCPI(); this.genGetField(this.lookupField(cpi, opcode, true), true); break;
        case Bytecodes.PUTSTATIC      : cpi = stream.readCPI(); this.genPutField(this.lookupField(cpi, opcode, true), true); break;
        case Bytecodes.GETFIELD       : cpi = stream.readCPI(); this.genGetField(this.lookupField(cpi, opcode, false), false); break;
        case Bytecodes.PUTFIELD       : cpi = stream.readCPI(); this.genPutField(this.lookupField(cpi, opcode, false), false); break;
        case Bytecodes.INVOKEVIRTUAL  : cpi = stream.readCPI(); this.genInvoke(this.lookupMethod(cpi, opcode, false), opcode, stream.nextBCI); break;
        case Bytecodes.INVOKESPECIAL  : cpi = stream.readCPI(); this.genInvoke(this.lookupMethod(cpi, opcode, false), opcode, stream.nextBCI); break;
        case Bytecodes.INVOKESTATIC   : cpi = stream.readCPI(); this.genInvoke(this.lookupMethod(cpi, opcode, true), opcode, stream.nextBCI); break;
        case Bytecodes.INVOKEINTERFACE: cpi = stream.readCPI(); this.genInvoke(this.lookupMethod(cpi, opcode, false), opcode, stream.nextBCI); break;
        case Bytecodes.NEW            : this.genNewInstance(stream.readCPI()); break;
        case Bytecodes.NEWARRAY       : this.genNewTypeArray(stream.readLocalIndex()); break;
        case Bytecodes.ANEWARRAY      : this.genNewObjectArray(stream.readCPI()); break;
        case Bytecodes.ARRAYLENGTH    : this.genArrayLength(); break;
        case Bytecodes.ATHROW         : this.genThrow(stream.currentBCI); break;
        case Bytecodes.CHECKCAST      : this.genCheckCast(stream.readCPI()); break;
        case Bytecodes.INSTANCEOF     : this.genInstanceOf(stream.readCPI()); break;
        /*
        case Bytecodes.MONITORENTER   : genMonitorEnter(state.apop()); break;
        case Bytecodes.MONITOREXIT    : genMonitorExit(state.apop()); break;
        case Bytecodes.MULTIANEWARRAY : genNewMultiArray(stream.readCPI()); break;
        */
        case Bytecodes.IFNULL         : this.genIfNull(stream, Condition.EQ); break;
        case Bytecodes.IFNONNULL      : this.genIfNull(stream, Condition.NE); break;
        /*
        case Bytecodes.GOTO_W         : genGoto(stream.readFarBranchDest()); break;
        case Bytecodes.JSR_W          : genJsr(stream.readFarBranchDest()); break;
        case Bytecodes.BREAKPOINT:
            throw new CiBailout("concurrent setting of breakpoint");
        default:
            throw new CiBailout("Unsupported opcode " + opcode + " (" + nameOf(opcode) + ") [bci=" + bci + "]");
      }
      */
        default:
          throw new Error("Not Implemented " + Bytecodes[opcode]);
      }
      writer && writer.leave("State  After: " + Bytecodes[opcode].padRight(" ", 12) + " " + state.toString());
      writer && writer.writeLn("");
    }
  }
}


































