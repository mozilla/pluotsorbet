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
  this.data = JSON.parse(util.decodeUtf8(data.subarray(offset, offset + length)));
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

Native["com/nokia/mid/s40/codec/DataEncoder.init.()V"] = function(addr) {
  setNative(addr, new DataEncoder());
};

Native["com/nokia/mid/s40/codec/DataEncoder.putStart.(ILjava/lang/String;)V"] = function(addr, tag, nameAddr) {
  NativeMap.get(addr).putStart(tag, J2ME.fromStringAddr(nameAddr));
};

Native["com/nokia/mid/s40/codec/DataEncoder.put.(ILjava/lang/String;Ljava/lang/String;)V"] = function(addr, tag, nameAddr, valueAddr) {
  NativeMap.get(addr).put(tag, J2ME.fromStringAddr(nameAddr), J2ME.fromStringAddr(valueAddr));
};

Native["com/nokia/mid/s40/codec/DataEncoder.put.(ILjava/lang/String;J)V"] = function(addr, tag, nameAddr, valueLow, valueHigh) {
  NativeMap.get(addr).put(tag, J2ME.fromStringAddr(nameAddr), J2ME.longToNumber(valueLow, valueHigh));
};

Native["com/nokia/mid/s40/codec/DataEncoder.put.(ILjava/lang/String;Z)V"] = function(addr, tag, nameAddr, value) {
  NativeMap.get(addr).put(tag, J2ME.fromStringAddr(nameAddr), value);
};

Native["com/nokia/mid/s40/codec/DataEncoder.put.(Ljava/lang/String;[BI)V"] = function(addr, nameAddr, dataAddr, length) {
  var array = Array.prototype.slice.call(J2ME.getArrayFromAddr(dataAddr).subarray(0, length));
  array.constructor = Array;
  NativeMap.get(addr).putNoTag(J2ME.fromStringAddr(nameAddr), array);
};

Native["com/nokia/mid/s40/codec/DataEncoder.putEnd.(ILjava/lang/String;)V"] = function(addr, tag, nameAddr) {
  NativeMap.get(addr).putEnd(tag, J2ME.fromStringAddr(nameAddr));
};

Native["com/nokia/mid/s40/codec/DataEncoder.getData.()[B"] = function(addr) {
  var data = NativeMap.get(addr).getData();

  var arrayAddr = J2ME.newByteArray(data.length);
  var array = J2ME.getArrayFromAddr(arrayAddr);
  for (var i = 0; i < data.length; i++) {
    array[i] = data.charCodeAt(i);
  }

  return arrayAddr;
};

Native["com/nokia/mid/s40/codec/DataDecoder.init.([BII)V"] = function(addr, dataAddr, offset, length) {
  setNative(addr, new DataDecoder(J2ME.getArrayFromAddr(dataAddr), offset, length));
};

Native["com/nokia/mid/s40/codec/DataDecoder.getStart.(I)V"] = function(addr, tag) {
  if (!NativeMap.get(addr).getStart(tag)) {
    throw $.newIOException("no start found " + tag);
  }
};

Native["com/nokia/mid/s40/codec/DataDecoder.getEnd.(I)V"] = function(addr, tag) {
  if (!NativeMap.get(addr).getEnd(tag)) {
    throw $.newIOException("no end found " + tag);
  }
};

Native["com/nokia/mid/s40/codec/DataDecoder.getString.(I)Ljava/lang/String;"] = function(addr, tag) {
  var str = NativeMap.get(addr).getValue(tag);
  if (str === undefined) {
    throw $.newIOException("tag (" + tag + ") invalid");
  }
  return J2ME.newUncollectableString(str);
};

Native["com/nokia/mid/s40/codec/DataDecoder.getInteger.(I)J"] = function(addr, tag) {
  var num = NativeMap.get(addr).getValue(tag);
  if (num === undefined) {
    throw $.newIOException("tag (" + tag + ") invalid");
  }
  return J2ME.returnLongValue(num);
};

Native["com/nokia/mid/s40/codec/DataDecoder.getBoolean.()Z"] = function(addr) {
  var val = NativeMap.get(addr).getNextValue();
  if (val === undefined) {
    throw $.newIOException();
  }
  return val === 1 ? 1 : 0;
};

Native["com/nokia/mid/s40/codec/DataDecoder.getName.()Ljava/lang/String;"] = function(addr) {
  var name = NativeMap.get(addr).getName();
  if (name === undefined) {
    throw $.newIOException();
  }
  return J2ME.newUncollectableString(name);
};

Native["com/nokia/mid/s40/codec/DataDecoder.getType.()I"] = function(addr) {
  var tag = NativeMap.get(addr).getTag();
  if (tag === undefined) {
    throw $.newIOException();
  }
  return tag;
};

Native["com/nokia/mid/s40/codec/DataDecoder.listHasMoreItems.()Z"] = function(addr) {
  return NativeMap.get(addr).getType() != DataEncoder.END ? 1 : 0;
};
