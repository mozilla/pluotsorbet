/**
 * Created by mbebenita on 10/21/14.
 */

module J2ME.C4.IR {
  export class JVMNewArray extends Value {
    constructor(public control: Control, public kind: Kind, public length: Value) {
      super();
    }
    visitInputs(visitor: NodeVisitor) {
      visitor(this.control);
      visitor(this.length);
    }
  }

  JVMNewArray.prototype.nodeName = "JVMNewArray";

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
}

module J2ME.C4.Backend {
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

  IR.JVMConvert.prototype.compile = function (cx: Context): AST.Node {
    var value = compileValue(this.value, cx);
    // bdahl: Add all the conversions here.
    return new AST.BinaryExpression("|", value, constant(0));
  }
}