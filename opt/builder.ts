module J2ME {

  var writer = new IndentingWriter();

  import BlockMap = Bytecode.BlockMap;
  import assert = Debug.assert;
  import unique = ArrayUtilities.unique;

  import IR = C4.IR;
  import Node = IR.Node;
  import Value = IR.Value;
  import Phi = IR.Phi;
  import Control = IR.Control;
  import Constant = IR.Constant;

  import Null = IR.Null;
  import Undefined = IR.Undefined;
  import True = IR.True;
  import False = IR.False;

  export interface FieldInfo {
    name: string;
    signature: any;
    classInfo: ClassInfo;
    access_flags: any;
  }

  export interface ConstantPoolEntry {
    tag: number;
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
    signature: any;
    classInfo: ClassInfo;
    code: Uint8Array;
    isNative: boolean;
    isPublic: boolean;
    isStatic: boolean;
    exception_table: ExceptionHandler [];
  }

  class State {
    private static _nextID = 0;
    id: number
    index: number;
    local: Value [];
    stack: Value [];
    store: Value;
    loads: Value [];
    saved: Value;
    constructor(index: number = 0) {
      this.id = State._nextID += 1;
      this.index = index;
      this.local = [];
      this.stack = [];
      this.store = Undefined;
      this.loads = [];
      this.saved = Undefined;
    }

    clone(index: number) {
      var s = new State();
      s.index = index !== undefined ? index : this.index;
      s.local = this.local.slice(0);
      s.stack = this.stack.slice(0);
      s.loads = this.loads.slice(0);
      s.saved = this.saved;
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
      s.index = this.index;
      s.local = this.local.map(function (v, i) {
        if (dirtyLocals[i]) {
          return makePhi(v);
        }
        return v;
      });
      s.stack = this.stack.map(makePhi);
      s.loads = this.loads.slice(0);
      s.saved = this.saved;
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
      this.saved = State.tryOptimizePhi(this.saved);
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
      return x;
    }

    toString(): string {
      return "<" + String(this.id + " @ " + this.index).padRight(' ', 10) +
        (" M: " + State.toBriefString(this.store)).padRight(' ', 14) +
        (" X: " + State.toBriefString(this.saved)).padRight(' ', 14) +
        (" L: " + this.local.map(State.toBriefString).join(", ")).padRight(' ', 40) +
        (" S: " + this.stack.map(State.toBriefString).join(", ")).padRight(' ', 60);
    }
  }


  export function compile(classes, classInfo: ClassInfo) {
    writer.enter("Compiling Class: " + classInfo.className + " {");
    classInfo.methods.forEach(compileMethodInfo);
    writer.leave("}");
  }

  function compileMethodInfo(methodInfo: MethodInfo) {
    if (!methodInfo.code) {
      return;
    }
    writer.enter("Compiling Method: " + methodInfo.name + " " + methodInfo.signature + " {");
    writer.writeLn(String(methodInfo.code.length));

    var blockMap = new BlockMap(methodInfo);
    blockMap.build();
    writer.writeLn("Blocks: " + blockMap.blocks.map(b => b.blockID).join(", "));
    writer.leave("}");
  }
}