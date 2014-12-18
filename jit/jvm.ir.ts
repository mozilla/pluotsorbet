/**
 * Created by mbebenita on 10/21/14.
 */

module J2ME.C4.IR {

  export class JVMLong extends Value {
    constructor(public lowBits: number, public highBits: number, public kind: Kind = Kind.Long) {
      super();
    }
    toString() : string {
      return "J<" + this.lowBits + "," + this.highBits + ">";
    }
  }
  JVMLong.prototype.nodeName = "JVMLong";

  export class JVMString extends Constant {
    constructor(value: string) {
      super(value);
    }
    toString() : string {
      return "S<" + this.value + ">";
    }
  }
  JVMString.prototype.nodeName = "JVMString";

  export class JVMClass extends Constant {
    constructor(classInfo: ClassInfo) {
      super(classInfo);
    }
    toString() : string {
      return "C<" + this.value.className + ">";
    }
  }
  JVMClass.prototype.nodeName = "JVMClass";

  export class JVMNewArray extends StoreDependent {
    constructor(public control: Control, public store: Store, public arrayKind: Kind, public length: Value) {
      super(control, store);
    }
    visitInputs(visitor: NodeVisitor) {
      super.visitInputs(visitor);
      visitor(this.length);
    }
  }

  JVMNewArray.prototype.nodeName = "JVMNewArray";

  export class JVMNewObjectArray extends StoreDependent {
    constructor(public control: Control, public store: Store, public classInfo: ClassInfo, public length: Value) {
      super(control, store);
    }
    visitInputs(visitor: NodeVisitor) {
      super.visitInputs(visitor);
      visitor(this.length);
    }
  }

  JVMNewObjectArray.prototype.nodeName = "JVMNewObjectArray";

  export class JVMLongBinary extends Value {
    constructor(public operator: Operator, public a: Value, public b: Value) {
      super();
      this.kind = Kind.Long;
    }
    visitInputs(visitor: NodeVisitor) {
      visitor(this.a);
      visitor(this.b);
    }
  }

  JVMLongBinary.prototype.nodeName = "JVMLongBinary";

  export class JVMLongUnary extends Value {
    constructor(public operator: Operator, public a: Value) {
      super();
      this.kind = Kind.Long;
    }
    visitInputs(visitor: NodeVisitor) {
      visitor(this.a);
    }
  }

  JVMLongUnary.prototype.nodeName = "JVMLongUnary";

  export class JVMFloatCompare extends Value {
    constructor(public control: Control, public a: Value, public b: Value, public lessThan: boolean) {
      super();
    }
    visitInputs(visitor: NodeVisitor) {
      visitor(this.control);
      visitor(this.a);
      visitor(this.b);
    }
  }

  JVMFloatCompare.prototype.nodeName = "JVMFloatCompare";

  export class JVMLongCompare extends Value {
    constructor(public control: Control, public a: Value, public b: Value) {
      super();
    }
    visitInputs(visitor: NodeVisitor) {
      visitor(this.control);
      visitor(this.a);
      visitor(this.b);
    }
  }

  JVMLongCompare.prototype.nodeName = "JVMLongCompare";

  export class JVMStoreIndexed extends StoreDependent {
    constructor(control: Control, store: Store, public kind: Kind, public array: Value, public index: Value, public value: Value) {
      super(control, store);
    }
    visitInputs(visitor: NodeVisitor) {
      super.visitInputs(visitor);
      visitor(this.array);
      visitor(this.index);
      visitor(this.value);
    }
  }

  JVMStoreIndexed.prototype.nodeName = "JVMStoreIndexed";

  export class JVMLoadIndexed extends StoreDependent {
    constructor(control: Control, store: Store, public kind: Kind, public array: Value, public index: Value) {
      super(control, store);
    }
    visitInputs(visitor: NodeVisitor) {
      super.visitInputs(visitor);
      visitor(this.array);
      visitor(this.index);
    }
  }

  JVMLoadIndexed.prototype.nodeName = "JVMLoadIndexed ";

  export class JVMConvert extends Value {
    constructor(public from: Kind, public to: Kind, public value: Value) {
      super();
    }
    visitInputs(visitor: NodeVisitor) {
      visitor(this.value);
    }
  }

  JVMConvert.prototype.nodeName = "JVMConvert";

  function visitStateInputs(a, visitor) {
    for (var i = 0; i < a.length; i++) {
      if (a[i] === null) {
        continue;
      }
      visitor(a[i]);
      if (isTwoSlot(a[i].kind)) {
        i++;
      }
    }
  }

  export class JVMInvoke extends StoreDependent {
    constructor(control: Control, store: Store, public state: State, public opcode: J2ME.Bytecode.Bytecodes, public object: Value, public methodInfo: MethodInfo, public args: Value []) {
      super(control, store);
    }
    visitInputs(visitor: NodeVisitor) {
      super.visitInputs(visitor);
      this.object && visitor(this.object);
      visitArrayInputs(this.args, visitor);
      if (this.state) {
        visitStateInputs(this.state.local, visitor);
        visitStateInputs(this.state.stack, visitor);
      }
    }
    replaceInput(oldInput: Node, newInput: Node) {
      var count = super.replaceInput(oldInput, newInput);
      if (this.state) {
        count += (<any>this.state.local).replace(oldInput, newInput);
        count += (<any>this.state.stack).replace(oldInput, newInput);
      }
      return count;
    }
  }

  JVMInvoke.prototype.nodeName = "JVMInvoke";

  export class JVMNew extends StoreDependent {
    constructor(control: Control, store: Store, public classInfo: ClassInfo) {
      super(control, store);
    }
  }

  JVMNew.prototype.nodeName = "JVMNew";

  export class JVMThrow extends StoreDependent {
    constructor(control: Control, store: Store, public object: Value) {
      super(control, store);
      this.handlesAssignment = true;
    }
    visitInputs(visitor: NodeVisitor) {
      super.visitInputs(visitor);
      visitor(this.object);
    }
  }

  JVMThrow.prototype.nodeName = "JVMThrow";

  export class JVMCheckCast extends StoreDependent {
    constructor(control: Control, store: Store, public object: Value, public classInfo: ClassInfo) {
      super(control, store);
    }
    visitInputs(visitor: NodeVisitor) {
      super.visitInputs(visitor);
      visitor(this.object);
    }
  }

  JVMCheckCast.prototype.nodeName = "JVMCheckCast";

  export class JVMInstanceOf extends StoreDependent {
    constructor(control: Control, store: Store, public object: Value, public classInfo: ClassInfo) {
      super(control, store);
    }
    visitInputs(visitor: NodeVisitor) {
      super.visitInputs(visitor);
      visitor(this.object);
    }
  }

  JVMInstanceOf.prototype.nodeName = "JVMInstanceOf";

  export class JVMCheckArithmetic extends StoreDependent {
    constructor(control: Control, store: Store, public value: Value) {
      super(control, store);
    }
    visitInputs(visitor: NodeVisitor) {
      super.visitInputs(visitor);
      visitor(this.value);
    }
  }

  JVMCheckArithmetic.prototype.nodeName = "JVMCheckArithmetic";

  export class JVMGetField extends StoreDependent {
    constructor(control: Control, store: Store, public object: Value, public fieldInfo: FieldInfo) {
      super(control, store);
    }
    visitInputs(visitor: NodeVisitor) {
      super.visitInputs(visitor);
      this.object && visitor(this.object);
    }
  }

  JVMGetField.prototype.nodeName = "JVMGetField";

  export class JVMPutField extends StoreDependent {
    constructor(control: Control, store: Store, public object: Value, public fieldInfo: FieldInfo, public value: Value) {
      super(control, store);
    }
    visitInputs(visitor: NodeVisitor) {
      super.visitInputs(visitor);
      this.object && visitor(this.object);
      visitor(this.value);
    }
  }

  JVMPutField.prototype.nodeName = "JVMPutField";
}

module J2ME.C4.Backend {
  import Operator = IR.Operator;
  import assert = Debug.assert;

  IR.JVMLong.prototype.compile = function (cx: Context): AST.Node {
    return new AST.CallExpression(new AST.Identifier("Long.fromBits"), [constant(this.lowBits), constant(this.highBits)]);
  };

  IR.JVMString.prototype.compile = function (cx: Context): AST.Node {
    return new AST.CallExpression(new AST.Identifier("$S"), [constant(this.value)]);
  };

  IR.JVMClass.prototype.compile = function (cx: Context): AST.Node {
    return id(mangleClass(this.value));
  };

  IR.JVMCheckCast.prototype.compile = function (cx: Context): AST.Node {
    var object = compileValue(this.object, cx);
    var runtimeFunction = "$CCK";
    if (this.classInfo.isInterface) {
      runtimeFunction = "$CCI";
    }
    return new AST.CallExpression(new AST.Identifier(runtimeFunction), [object, id(mangleClass(this.classInfo))]);
  };

  IR.JVMInstanceOf.prototype.compile = function (cx: Context): AST.Node {
    var object = compileValue(this.object, cx);
    var runtimeFunction = "$IOK";
    if (this.classInfo.isInterface) {
      runtimeFunction = "$IOI";
    }
    return new AST.BinaryExpression("|", new AST.CallExpression(new AST.Identifier(runtimeFunction), [object, id(mangleClass(this.classInfo))]), new AST.Literal(0));
  };

  IR.JVMCheckArithmetic.prototype.compile = function (cx: Context): AST.Node {
    var value = compileValue(this.value, cx);
    return new AST.CallExpression(new AST.Identifier("$CDZ"), [value]);
  };

  IR.JVMNewArray.prototype.compile = function (cx: Context): AST.Node {
    var jsTypedArrayType: string;
    switch (this.arrayKind) {
      case Kind.Int:
        jsTypedArrayType = "Int32Array";
        break;
      case Kind.Char:
        jsTypedArrayType = "Uint16Array";
        break;
      case Kind.Short:
        jsTypedArrayType = "Int16Array";
        break;
      case Kind.Byte:
      case Kind.Boolean:
        jsTypedArrayType = "Int8Array";
        break;
      case Kind.Float:
        jsTypedArrayType = "Float32Array";
        break;
      case Kind.Long:
        jsTypedArrayType = "Array";
        break;
      case Kind.Double:
        jsTypedArrayType = "Float64Array";
        break;
      default:
        throw Debug.unexpected(Kind[this.arrayKind]);
    }
    return new AST.NewExpression(new AST.Identifier(jsTypedArrayType), [compileValue(this.length, cx)]);
  };

  IR.JVMNewObjectArray.prototype.compile = function (cx: Context): AST.Node {
    var emitClassInitializationCheck = true;
    var callee: AST.Node = call(id("$NA"), [id(mangleClass(this.classInfo)), compileValue(this.length, cx)]);
    if (emitClassInitializationCheck) {
      callee = new AST.SequenceExpression([getRuntimeClass(this.classInfo), callee]);
    }
    return callee;
  };

  IR.JVMStoreIndexed.prototype.compile = function (cx: Context): AST.Node {
    var array = compileValue(this.array, cx);
    var index = compileValue(this.index, cx);
    var value = compileValue(this.value, cx);
    return assignment(new AST.MemberExpression(array, index, true), value);
  };

  IR.JVMLoadIndexed.prototype.compile = function (cx: Context): AST.Node {
    var array = compileValue(this.array, cx);
    var index = compileValue(this.index, cx);
    return new AST.MemberExpression(array, index, true);
  };

  IR.JVMFloatCompare.prototype.compile = function (cx: Context): AST.Node {
    var a = compileValue(this.a, cx);
    var b = compileValue(this.b, cx);
    var nanResult;
    if (this.lessThan) {
      nanResult = constant(-1);
    } else {
      nanResult = constant(1);
    }
    var nan = new AST.LogicalExpression("||", new AST.CallExpression(new AST.Identifier("isNaN"), [a]), new AST.CallExpression(new AST.Identifier("isNaN"), [b]));
    var gt = new AST.BinaryExpression(">", a, b);
    var lt = new AST.BinaryExpression("<", a, b);
    return new AST.ConditionalExpression(nan, nanResult,
        new AST.ConditionalExpression(gt, constant(1),
          new AST.ConditionalExpression(lt,
            constant(-1), constant(0))));
  };

  IR.JVMLongCompare.prototype.compile = function (cx: Context): AST.Node {
    var a = compileValue(this.a, cx);
    var b = compileValue(this.b, cx);
    var gt = call(new AST.MemberExpression(a, new AST.Identifier("greaterThan"), false), [b]);
    var lt = call(new AST.MemberExpression(a, new AST.Identifier("lessThan"), false), [b]);
    return new AST.ConditionalExpression(gt, constant(1),
        new AST.ConditionalExpression(lt,
          constant(-1), constant(0)));
  };

  IR.JVMLongBinary.prototype.compile = function (cx: Context): AST.Node {
    var a = compileValue(this.a, cx);
    var b = compileValue(this.b, cx);
    var operator;
    switch (this.operator) {
      case Operator.LADD: operator = "add"; break;
      case Operator.LSUB: operator = "subtract"; break;
      case Operator.LMUL: operator = "multiply"; break;
      case Operator.LDIV: operator = "div"; break;
      case Operator.LREM: operator = "modulo"; break;

      case Operator.LSH: operator = "shiftLeft"; break;
      case Operator.RSH: operator = "shiftRight"; break;
      case Operator.URSH: operator = "shiftRightUnsigned"; break;

      case Operator.AND: operator = "and"; break;
      case Operator.OR: operator = "or"; break;
      case Operator.XOR: operator = "xor"; break;

      default:
        assert(false);
    }
    return call(new AST.MemberExpression(a, new AST.Identifier(operator), false), [b]);
  };

  IR.JVMLongUnary.prototype.compile = function (cx: Context): AST.Node {
    var a = compileValue(this.a, cx);
    var operator;
    switch (this.operator) {
      case Operator.LNEG: operator = "negate"; break;
      default:
        assert(false);
    }
    return call(new AST.MemberExpression(a, new AST.Identifier(operator), false), []);
  };

  IR.JVMConvert.prototype.compile = function (cx: Context): AST.Node {
    var value = compileValue(this.value, cx);
    if (this.from === Kind.Int) {
      switch (this.to) {
        case Kind.Long:
          return call(new AST.Identifier("Long.fromInt"), [value]);
        case Kind.Float:
          return value;
        case Kind.Double:
          return value;
        case Kind.Short:
          return new AST.BinaryExpression(">>", new AST.BinaryExpression("<<", value, constant(16)), constant(16));
        case Kind.Char:
          return new AST.BinaryExpression("&", value, constant(0xffff));
        case Kind.Byte:
          return new AST.BinaryExpression(">>", new AST.BinaryExpression("<<", value, constant(24)), constant(24));
      }
    } else if (this.from === Kind.Long) {
      switch (this.to) {
        case Kind.Int:
          return call(new AST.MemberExpression(value, new AST.Identifier("toInt"), false), []);
        case Kind.Float:
          return call(new AST.Identifier("Math.fround"), [call(new AST.MemberExpression(value, new AST.Identifier("toNumber"), false), [])]);
        case Kind.Double:
          return call(new AST.MemberExpression(value, new AST.Identifier("toNumber"), false), []);
      }
    } else if (this.from === Kind.Float) {
      switch (this.to) {
        case Kind.Int:
          return call(new AST.Identifier("util.double2int"), [value]);
        case Kind.Long:
          return call(new AST.Identifier("Long.fromNumber"), [value]);
        case Kind.Double:
          return value;
      }
    } else if (this.from === Kind.Double) {
      switch (this.to) {
        case Kind.Int:
          return call(new AST.Identifier("util.double2int"), [value]);
        case Kind.Long:
          return call(new AST.Identifier("util.double2long"), [value]);
        case Kind.Float:
          return call(new AST.Identifier("Math.fround"), [value]);
      }
    }
    throw "Unimplemented conversion";
  }

  function compileStateValues(values: IR.Value [], cx: Context): AST.Node {
    var compiledValues = [];
    for (var i = 0; i < values.length; i++) {
      if (values[i] === null) {
        compiledValues.push(constant(null));
      } else {
        compiledValues.push(compileValue(values[i], cx));
        if (isTwoSlot(values[i].kind)) {
          compiledValues.push(constant(null));
          i++;
        }
      }
    }
    return new AST.ArrayExpression(compiledValues);
  }

  function compileState(state: State, cx: Context): AST.Node [] {
    var local = compileStateValues(state.local, cx);
    var stack = compileStateValues(state.stack, cx);
    return [new AST.Literal(state.bci), local, stack];
  }

  IR.JVMInvoke.prototype.compile = function (cx: Context): AST.Node {
    var object = this.object ? compileValue(this.object, cx) : null;
    var args = this.args.map(function (arg) {
      return compileValue(arg, cx);
    });
    var callee = null;
    var result = null;
    if (object) {
      if (this.opcode === J2ME.Bytecode.Bytecodes.INVOKESPECIAL) {
        result = callCall(id(mangleClassAndMethod(this.methodInfo)), object, args);
      } else {
        callee = property(object, mangleMethod(this.methodInfo));
        result = call(callee, args);
      }
    } else {
      release || assert (this.opcode === J2ME.Bytecode.Bytecodes.INVOKESTATIC);
      callee = id(mangleClassAndMethod(this.methodInfo));
      var emitClassInitializationCheck = true;
      if (emitClassInitializationCheck) {
        callee = new AST.SequenceExpression([getRuntimeClass(this.methodInfo.classInfo), callee]);
      }
      result = call(callee, args);
    }

    if (this.state) {
      var block = new AST.BlockStatement([]);
      var to = id(this.variable.name);
      cx.useVariable(this.variable);
      block.body.push(new AST.ExpressionStatement(assignment(to, result)));
      var ifUnwind = new AST.IfStatement(id("U"), new AST.BlockStatement([
        new AST.ExpressionStatement(call(property(id("$"), "B"), compileState(this.state, cx))),
        new AST.ReturnStatement(undefined)
      ]));
      block.body.push(ifUnwind);
      return block;
    }
    return result;
  };


  function getRuntimeClass(classInfo: ClassInfo) {
    return new AST.MemberExpression(id("$"), id(mangleClass(classInfo)), false);
  }

  IR.JVMGetField.prototype.compile = function (cx: Context): AST.Node {
    if (this.object) {
      var object = compileValue(this.object, cx);
      return new AST.MemberExpression(object, id(this.fieldInfo.mangledName), false);
    } else {
      assert(this.fieldInfo.isStatic);
      return new AST.MemberExpression(getRuntimeClass(this.fieldInfo.classInfo), id(this.fieldInfo.mangledName), false);
    }
  };

  IR.JVMPutField.prototype.compile = function (cx: Context): AST.Node {
    var value = compileValue(this.value, cx);
    if (this.object) {
      var object = compileValue(this.object, cx);
      return assignment(new AST.MemberExpression(object, id(this.fieldInfo.mangledName), false), value);
    } else {
      assert(this.fieldInfo.isStatic);
      return assignment(new AST.MemberExpression(getRuntimeClass(this.fieldInfo.classInfo), id(this.fieldInfo.mangledName), false), value);
    }
  };

  IR.JVMNew.prototype.compile = function (cx: Context): AST.Node {
    var callee: AST.Node = id(mangleClass(this.classInfo));
    var emitClassInitializationCheck = true;
    if (emitClassInitializationCheck) {
      callee = new AST.SequenceExpression([getRuntimeClass(this.classInfo), callee]);
    }
    return new AST.NewExpression(callee, []);
  };

  IR.JVMThrow.prototype.compile = function (cx: Context): AST.Node {
    var object = compileValue(this.object, cx);
    return new AST.ThrowStatement(object);
  };
}
