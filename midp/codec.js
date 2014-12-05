/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

var DataType = {
  BOOLEAN: 0,
  CHAR: 1,
  BYTE: 2,
  WCHAR: 3,
  SHORT: 4,
  USHORT: 5,
  LONG: 6,
  ULONG: 7,
  FLOAT: 8,
  DOUBLE: 9,
  STRING: 10,
  WSTRING: 11,
  URI: 12,
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

DataEncoder.prototype.putNoTag = function(name, value) {
  this.data.push({
    name: name,
    value: value,
  });
}

DataEncoder.prototype.getData = function() {
  return JSON.stringify(this.data);
}

var DataDecoder = function(data, offset, length) {
  this.data = JSON.parse(util.decodeUtf8(new Uint8Array(data.buffer, offset, length)));
  this.current = [];
}

DataDecoder.prototype.find = function(tag, type) {
  var elem;
  var i = 0;
  while (elem = this.data[i++]) {
    if ((!type || elem.type == type) && elem.tag == tag) {
      this.data = this.data.slice(i);
      return elem;
    }

    if (elem.type == DataEncoder.END) {
      break;
    }
  }
}

DataDecoder.prototype.getStart = function(tag) {
  var elem = this.find(tag, DataEncoder.START);
  if (!elem) {
    return false;
  }

  this.current.push(elem);

  return true;
}

DataDecoder.prototype.getEnd = function(tag) {
  var elem = this.find(tag, DataEncoder.END);
  if (!elem) {
    return false;
  }

  // If this happens, a father has ended before a child
  if (elem.tag != this.current[this.current.length - 1].tag ||
      elem.name != this.current[this.current.length - 1].name) {
    return false;
  }

  this.current.pop();

  return true;
}

DataDecoder.prototype.getValue = function(tag) {
  var elem = this.find(tag);
  return elem ? elem.value : undefined;
}

DataDecoder.prototype.getNextValue = function() {
  var elem = this.data.shift();
  return elem ? elem.value : undefined;
}

DataDecoder.prototype.getName = function() {
  return this.data[0].name;
}

DataDecoder.prototype.getTag = function() {
  return this.data[0].tag;
}

DataDecoder.prototype.getType = function() {
  return this.data[0].type || -1;
}

Native.create("com/nokia/mid/s40/codec/DataEncoder.init.()V", function() {
  this.encoder = new DataEncoder();
});

Native.create("com/nokia/mid/s40/codec/DataEncoder.putStart.(ILjava/lang/String;)V", function(tag, name) {
  this.encoder.putStart(tag, util.fromJavaString(name));
});

Native.create("com/nokia/mid/s40/codec/DataEncoder.put.(ILjava/lang/String;Ljava/lang/String;)V", function(tag, name, value) {
  this.encoder.put(tag, util.fromJavaString(name), util.fromJavaString(value));
});

Native.create("com/nokia/mid/s40/codec/DataEncoder.put.(ILjava/lang/String;J)V", function(tag, name, value, _) {
  this.encoder.put(tag, util.fromJavaString(name), value.toNumber());
});

Native.create("com/nokia/mid/s40/codec/DataEncoder.put.(ILjava/lang/String;Z)V", function(tag, name, value) {
  this.encoder.put(tag, util.fromJavaString(name), value);
});

Native.create("com/nokia/mid/s40/codec/DataEncoder.put.(Ljava/lang/String;[BI)V", function(name, data, length) {
  var array = Array.prototype.slice.call(data.subarray(0, length));
  array.constructor = Array;
  this.encoder.putNoTag(util.fromJavaString(name), array);
});

Native.create("com/nokia/mid/s40/codec/DataEncoder.putEnd.(ILjava/lang/String;)V", function(tag, name) {
  this.encoder.putEnd(tag, util.fromJavaString(name));
});

Native.create("com/nokia/mid/s40/codec/DataEncoder.getData.()[B", function() {
  var data = this.encoder.getData();

  var array = util.newPrimitiveArray("B", data.length);
  for (var i = 0; i < data.length; i++) {
    array[i] = data.charCodeAt(i);
  }

  return array;
});

Native.create("com/nokia/mid/s40/codec/DataDecoder.init.([BII)V", function(data, offset, length) {
  this.decoder = new DataDecoder(data, offset, length);
});

Native.create("com/nokia/mid/s40/codec/DataDecoder.getStart.(I)V", function(tag) {
  if (!this.decoder.getStart(tag)) {
    throw new JavaException("java/io/IOException", "no start found " + tag);
  }
});

Native.create("com/nokia/mid/s40/codec/DataDecoder.getEnd.(I)V", function(tag) {
  if (!this.decoder.getEnd(tag)) {
    throw new JavaException("java/io/IOException", "no end found " + tag);
  }
});

Native.create("com/nokia/mid/s40/codec/DataDecoder.getString.(I)Ljava/lang/String;", function(tag) {
  var str = this.decoder.getValue(tag);
  if (str === undefined) {
    throw new JavaException("java/io/IOException", "tag (" + tag + ") invalid");
  }
  return str;
});

Native.create("com/nokia/mid/s40/codec/DataDecoder.getInteger.(I)J", function(tag) {
  var num = this.decoder.getValue(tag);
  if (num === undefined) {
    throw new JavaException("java/io/IOException", "tag (" + tag + ") invalid");
  }
  return Long.fromNumber(num);
});

Native.create("com/nokia/mid/s40/codec/DataDecoder.getBoolean.()Z", function() {
  var val = this.decoder.getNextValue();
  if (val === undefined) {
    throw new JavaException("java/io/IOException");
  }
  return val === 1;
});

Native.create("com/nokia/mid/s40/codec/DataDecoder.getName.()Ljava/lang/String;", function() {
  var name = this.decoder.getName();
  if (name === undefined) {
    throw new JavaException("java/io/IOException");
  }
  return name;
});

Native.create("com/nokia/mid/s40/codec/DataDecoder.getType.()I", function() {
  var tag = this.decoder.getTag();
  if (tag === undefined) {
    throw new JavaException("java/io/IOException");
  }
  return tag;
});

Native.create("com/nokia/mid/s40/codec/DataDecoder.listHasMoreItems.()Z", function() {
  return this.decoder.getType() != DataEncoder.END;
});
