/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

var DataType = {
  USHORT: 5,
  ULONG: 7,
  STRING: 10,
  WSTRING: 11,
  METHOD: 13,
  STRUCT: 14,
  LIST: 15,
  ARRAY: 16,
};

var DataEncoder = function() {
  this.data = [];
}

DataEncoder.START = 1;
DataEncoder.END = 2;

DataEncoder.prototype.putStart = function(tag, name) {
  this.data.push({
    type: DataEncoder.START,
    tag: tag,
    name: name,
  });
}

DataEncoder.prototype.putEnd = function(tag, name) {
  this.data.push({
    type: DataEncoder.END,
    tag: tag,
    name: name,
  })
}

DataEncoder.prototype.put = function(tag, name, value) {
  this.data.push({
    tag: tag,
    name: name,
    value: value,
  });
}

DataEncoder.prototype.getData = function() {
  return JSON.stringify(this.data);
}

var DataDecoder = function(data, offset, length) {
  this.data = JSON.parse(util.decodeUtf8(new Uint8Array(data.buffer, offset, length)));
}

DataDecoder.prototype.find = function(tag, type) {
  var elem;
  while (elem = this.data.shift()) {
    if ((!type || elem.type == type) && elem.tag == tag) {
      return elem.value;
    }
  }
}

DataDecoder.prototype.getStart = function(tag) {
  this.find(tag, DataEncoder.START);
}

DataDecoder.prototype.getEnd = function(tag) {
  this.find(tag, DataEncoder.END);
}

DataDecoder.prototype.getValue = function(tag) {
  return this.find(tag);
}

DataDecoder.prototype.getName = function() {
  return this.data[0].name;
}

DataDecoder.prototype.getTag = function() {
  return this.data[0].tag;
}

Native["com/nokia/mid/s40/codec/DataEncoder.init.()V"] = function(ctx, stack) {
  var _this = stack.pop();
  _this.encoder = new DataEncoder();
}

Native["com/nokia/mid/s40/codec/DataEncoder.putStart.(ILjava/lang/String;)V"] = function(ctx, stack) {
  var name = util.fromJavaString(stack.pop()), tag = stack.pop(), _this = stack.pop();
  _this.encoder.putStart(tag, name);
}

Native["com/nokia/mid/s40/codec/DataEncoder.put.(ILjava/lang/String;Ljava/lang/String;)V"] = function(ctx, stack) {
  var value = util.fromJavaString(stack.pop()), name = util.fromJavaString(stack.pop()), tag = stack.pop(), _this = stack.pop();
  _this.encoder.put(tag, name, value);
}

Native["com/nokia/mid/s40/codec/DataEncoder.put.(ILjava/lang/String;J)V"] = function(ctx, stack) {
  var value = stack.pop2().toNumber(), name = util.fromJavaString(stack.pop()), tag = stack.pop(), _this = stack.pop();
  _this.encoder.put(tag, name, value);
}

Native["com/nokia/mid/s40/codec/DataEncoder.put.(ILjava/lang/String;Z)V"] = function(ctx, stack) {
  var value = stack.pop(), name = stack.pop(), tag = stack.pop(), _this = stack.pop();
  _this.encoder.put(tag, name, value);
}

Native["com/nokia/mid/s40/codec/DataEncoder.putEnd.(ILjava/lang/String;)V"] = function(ctx, stack) {
  var name = util.fromJavaString(stack.pop()), tag = stack.pop(), _this = stack.pop();
  _this.encoder.putEnd(tag, name);
}

Native["com/nokia/mid/s40/codec/DataEncoder.getData.()[B"] = function(ctx, stack) {
  var _this = stack.pop();
  var data = _this.encoder.getData();

  var array = ctx.newPrimitiveArray("B", data.length);
  for (var i = 0; i < data.length; i++) {
    array[i] = data.charCodeAt(i);
  }

  stack.push(array);
}

Native["com/nokia/mid/s40/codec/DataDecoder.init.([BII)V"] = function(ctx, stack) {
  var length = stack.pop(), offset = stack.pop(), data = stack.pop(), _this = stack.pop();
  _this.decoder = new DataDecoder(data, offset, length);
}

Native["com/nokia/mid/s40/codec/DataDecoder.getStart.(I)V"] = function(ctx, stack) {
  var tag = stack.pop(), _this = stack.pop();
  _this.decoder.getStart(tag);
}

Native["com/nokia/mid/s40/codec/DataDecoder.getEnd.(I)V"] = function(ctx, stack) {
  var tag = stack.pop(), _this = stack.pop();
  _this.decoder.getEnd(tag);
}

Native["com/nokia/mid/s40/codec/DataDecoder.getString.(I)Ljava/lang/String;"] = function(ctx, stack) {
  var tag = stack.pop(), _this = stack.pop();
  var str = _this.decoder.getValue(tag);
  if (str === undefined) {
    ctx.raiseExceptionAndYield("java/io/IOException", "tag (" + tag + ") invalid");
  }
  stack.push(ctx.newString(str));
}

// Throw IOException if not found
Native["com/nokia/mid/s40/codec/DataDecoder.getInteger.(I)J"] = function(ctx, stack) {
  var tag = stack.pop(), _this = stack.pop();
  var num = _this.decoder.getValue(tag);
  if (num === undefined) {
    ctx.raiseExceptionAndYield("java/io/IOException", "tag (" + tag + ") invalid");
  }
  stack.push2(Long.fromNumber(num));
}

Native["com/nokia/mid/s40/codec/DataDecoder.getName.()Ljava/lang/String;"] = function(ctx, stack) {
  var _this = stack.pop();
  var name = _this.decoder.getName();
  if (name === undefined) {
    ctx.raiseExceptionAndYield("java/io/IOException");
  }
  stack.push(ctx.newString(name));
}

Native["com/nokia/mid/s40/codec/DataDecoder.getType.()I"] = function(ctx, stack) {
  var _this = stack.pop();
  var tag = _this.decoder.getTag();
  if (tag === undefined) {
    ctx.raiseExceptionAndYield("java/io/IOException");
  }
  stack.push(tag);
}
