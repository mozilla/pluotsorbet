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
  Value.prototype.nodeName = "JVMLong";

  export class JVMNewArray extends StoreDependent {
  constructor(public control: Control, public store: Store, public arrayKind: Kind, public length: Value) {
    super(control, store);
  }
  visitInputs(visitor: NodeVisitor) {
    visitor(this.store);
    visitor(this.control);
    visitor(this.length);
  }
}

  JVMNewArray.prototype.nodeName = "JVMNewArray";

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
      visitor(this.control);
      visitor(this.store);
      this.loads && visitArrayInputs(this.loads, visitor);
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
      visitor(this.control);
      visitor(this.store);
      this.loads && visitArrayInputs(this.loads, visitor);
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

  export class JVMCallProperty extends StoreDependent {

    constructor(control: Control, store: Store, public state: State, public object: Value, public name: Value, public args: Value [], public callerMethodInfoId: string, public objectCheck?: boolean) {
      super(control, store);
      this.handlesAssignment = true;
    }
    visitInputs(visitor: NodeVisitor) {
      this.control && visitor(this.control);
      this.store && visitor(this.store);
      this.loads && visitArrayInputs(this.loads, visitor);
      visitor(this.object);
      visitor(this.name);
      visitArrayInputs(this.args, visitor);
      visitStateInputs(this.state.local, visitor);
      visitStateInputs(this.state.stack, visitor);
    }
    replaceInput(oldInput: Node, newInput: Node) {
      var count = super.replaceInput(oldInput, newInput);
      count += (<any>this.state.local).replace(oldInput, newInput);
      count += (<any>this.state.stack).replace(oldInput, newInput);
      return count;
    }
  }

  JVMCallProperty.prototype.nodeName = "JVMCallProperty";
}

module J2ME.C4.Backend {
  import Operator = IR.Operator;
  import assert = Debug.assert;

  IR.JVMLong.prototype.compile = function (cx: Context): AST.Node {
    return new AST.CallExpression(new AST.Identifier("Long.fromBits"), [constant(this.lowBits), constant(this.highBits)]);
  }

  IR.JVMNewArray.prototype.compile = function (cx: Context): AST.Node {
    var jsTypedArrayType: string;
    switch (this.arrayKind) {
      case Kind.Int:
        jsTypedArrayType = "Int32Array";
        break;
      case Kind.Char:
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
  }

  IR.JVMStoreIndexed.prototype.compile = function (cx: Context): AST.Node {
    var array = compileValue(this.array, cx);
    var index = compileValue(this.index, cx);
    var value = compileValue(this.value, cx);
    return assignment(new AST.MemberExpression(array, index, true), value);
  }

  IR.JVMLoadIndexed.prototype.compile = function (cx: Context): AST.Node {
    var array = compileValue(this.array, cx);
    var index = compileValue(this.index, cx);
    return new AST.MemberExpression(array, index, true);
  }

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

  function compileStateValues(cx: Context, values: IR.Value []): AST.Node [] {
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
    return compiledValues;
  }

  IR.JVMCallProperty.prototype.compile = function (cx: Context): AST.Node {

    var localValues = compileStateValues(cx, this.state.local);
    var stackValues = compileStateValues(cx, this.state.stack);;
    var object = compileValue(this.object, cx);
    var name = compileValue(this.name, cx);
    var callee = property(object, name);
    var args = this.args.map(function (arg) {
      return compileValue(arg, cx);
    });

    var objCheck = null;
    if (this.objectCheck) {
      var vmc = new AST.MemberExpression(object, new AST.Identifier("class"), false);
      objCheck = new AST.IfStatement(new AST.UnaryExpression("!", true, new AST.BinaryExpression("in", name, vmc)),
          new AST.ExpressionStatement(
            new AST.CallExpression(new AST.Identifier("J2ME.buildCompiledCall"), [
              vmc,
              name,
              new AST.CallExpression(new AST.Identifier("CLASSES.getMethod"), [
                new AST.MemberExpression(object, new AST.Identifier("class"), false), name])])),
          null);
      callee = property(vmc, name);
    }

    var callNode = call(callee, args);

    var exception = new AST.Identifier("e");
    var to = new AST.Identifier(this.variable.name);
    cx.useVariable(this.variable);

    var body = [];
    if (objCheck) {
      body.push(objCheck);
    }
    body.push(assignment(to, callNode));

    return new AST.TryStatement(
      new AST.BlockStatement(body),
      new AST.CatchClause(exception, null,
        new AST.BlockStatement([ // Ask mbx: is it bug I need ExpressionStatement here to get the semicolon inserted.
          new AST.ExpressionStatement(new AST.CallExpression(new AST.Identifier("ctx.JVMBailout"), [
            exception,
            new AST.Literal(this.callerMethodInfoId),
            new AST.Identifier("compiledDepth"),
            new AST.Literal(this.state.bci),
            new AST.ArrayExpression(localValues),
            new AST.ArrayExpression(stackValues)
          ])),
          new AST.ThrowStatement(exception)
        ])
      ),
      [],
      null
    );
  }
}
