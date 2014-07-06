/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var Reader = function(bytes, offset) {
    if (this instanceof Reader) {
        this.bytes = DataView(bytes);
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

var Utf8TextDecoder;

Reader.prototype.readString = function(length) {
    return util.decodeUtf8(this.readBytes(length));
}

Reader.prototype.readBytes = function(length) {
    var data = this.bytes.buffer.slice(this.offset, this.offset + length);
    this.offset += length;
    return data;
}
