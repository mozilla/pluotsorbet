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

Reader.prototype.readString = function(length) {
    // NB: no need to create a new slice.
    var data = new Uint8Array(this.bytes.buffer, this.offset, length);
    this.offset += length;

    // First try w/ TextDecoder, fallback to manually parsing if there was an
    // error. This will handle parsing errors resulting from Java's modified
    // UTF-8 implementation.
    try {
        return util.decodeUtf8Array(data);
    } catch(e) {
        return util.javaUTF8Decode(data);
    }
}

Reader.prototype.readBytes = function(length) {
    var data = this.bytes.buffer.slice(this.offset, this.offset + length);
    this.offset += length;
    return data;
}
