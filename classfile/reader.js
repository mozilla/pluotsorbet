/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var Reader = function(bytes, offset) {
    if (this instanceof Reader) {
        this.bytes = new DataView(bytes);
        this.offset = offset || 0;
    } else {
        return new Reader(bytes, offset);
    }
}

Reader.prototype.read8 = function() {
    var data = this.bytes.getUint8(this.offset);
    this.offset += 1;
    return data;
}

Reader.prototype.read16 = function() {
    var data = this.bytes.getUint16(this.offset, false);
    this.offset += 2;
    return data;
}

Reader.prototype.read32 = function() {
    var data = this.bytes.getUint32(this.offset, false);
    this.offset += 4;
    return data;
}

Reader.prototype.readInteger = function() {
    var data = this.bytes.getInt32(this.offset, false);
    this.offset += 4;
    return data;
}

Reader.prototype.readFloat = function() {
    var data = this.bytes.getFloat32(this.offset, false);
    this.offset += 4;
    return data;
}

Reader.prototype.readDouble = function() {
    var data = this.bytes.getFloat64(this.offset, false);
    this.offset += 8;
    return data;
}

// Decode Java's modified UTF-8 (JVM specs, $ 4.4.7)
function javaUTF8Decode(buf) {
  var rv = '';

  var arr = new Uint8Array(buf)

  var i = 0;
  while (i < arr.length) {
    var x = arr[i++] & 0xff;

    if (x <= 0x7f) {
        // Code points in the range '\u0001' to '\u007F' are represented by a
        // single byte.
        // The 7 bits of data in the byte give the value of the code point
        // represented.
        rv += String.fromCharCode(x);
    } else if (x <= 0xdf) {
        // The null code point ('\u0000') and code points in the range '\u0080'
        // to '\u07FF' are represented by a pair of bytes x and y.
        var y = arr[i++];
        rv += String.fromCharCode(((x & 0x1f) << 6) + (y & 0x3f));
    } else {
        // Code points in the range '\u0800' to '\uFFFF' are represented by 3
        // bytes x, y, and z.
        var y = arr[i++];
        var z = arr[i++];
        rv += String.fromCharCode(((x & 0xf) << 12) + ((y & 0x3f) << 6) + (z & 0x3f));
    }
  }

  return rv;
}

Reader.prototype.readString = function(length) {
    return javaUTF8Decode(this.readBytes(length));
}

Reader.prototype.readBytes = function(length) {
    var data = this.bytes.buffer.slice(this.offset, this.offset + length);
    this.offset += length;
    return data;
}
