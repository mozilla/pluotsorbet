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
    constructor(public control: Control, public store: Store, public kind: Kind, public length: Value) {
      super(control, store);
    }
    visitInputs(visitor: NodeVisitor) {
      visitor(this.store);
      visitor(this.control);
      visitor(this.length);
    }
  }

  JVMNewArray.prototype.nodeName = "JVMNewArray";

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

    constructor(control: Control, store: Store, public state: State, public object: Value, public name: Value, public args: Value [], public flags: number) {
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
  IR.JVMLong.prototype.compile = function (cx: Context): AST.Node {
    return new AST.CallExpression(new AST.Identifier("Long.fromBits"), [constant(this.lowBits), constant(this.highBits)]);
  }

  IR.JVMNewArray.prototype.compile = function (cx: Context): AST.Node {
    var jsTypedArrayType: string;
    switch (this.kind) {
      case Kind.Int:
        jsTypedArrayType = "Int32Array";
        break;
      case Kind.Short:
        jsTypedArrayType = "Int16Array";
        break;
      case Kind.Byte:
        jsTypedArrayType = "Int8Array";
        break;
      case Kind.Float:
        jsTypedArrayType = "Float32Array";
        break;
      case Kind.Long:
        jsTypedArrayType = "Float64Array"; // Tricky.
        break;
      case Kind.Double:
        jsTypedArrayType = "Float64Array";
        break;
      default:
        throw Debug.unexpected(this.kind);
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

  IR.JVMCallProperty.prototype.compile = function (cx: Context): AST.Node {
    var local = this.state.local;
    var stack = this.state.stack;

    var localValues = [];
    var stackValues = [];

    var $ = new AST.Identifier("$");
    for (var i = 0; i < local.length; i++) {
      if (local[i] === null) {
        continue;
      }
      localValues.push(compileValue(local[i], cx));
      if (isTwoSlot(local[i].kind)) {
        localValues.push(constant(null));
      }
    }
    for (var i = 0; i < stack.length; i++) {
      if (stack[i] === null) {
        continue;
      }
      stackValues.push(compileValue(stack[i], cx));
      if (isTwoSlot(stack[i].kind)) {
        stackValues.push(constant(null));
      }
    }

    var object = compileValue(this.object, cx);
    var name = compileValue(this.name, cx);
    var callee = property(object, name);
    var args = this.args.map(function (arg) {
      return compileValue(arg, cx);
    });
    var callNode;
    if (this.flags & IR.Flags.PRISTINE) {
      callNode = call(callee, args);
    } else {
      callNode = callCall(callee, object, args);
    }

    var exception = new AST.Identifier("e");
    var to = new AST.Identifier(this.variable.name);
    cx.useVariable(this.variable);

    return new AST.TryStatement(
      new AST.BlockStatement([assignment(to, callNode)]),
      new AST.CatchClause(exception, null,
        new AST.BlockStatement([ // Ask mbx: is it bug I need ExpressionStatement here to get the semicolon inserted.
          new AST.ExpressionStatement(new AST.CallExpression(new AST.Identifier("ctx.JVMBailout"), [
            exception,
            new AST.Identifier("methodInfoId"),
            new AST.Identifier("frameIndex"),
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
