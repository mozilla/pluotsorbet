/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

function LongArray(size) {
    var array = Array(size);
    // We can pass 'null' here because we know no exception will be raised.
    array.class = CLASSES.getClass(null, "[J");
    return array;
}

var ARRAY_TYPE = [
    null, // 0
    null, // 1
    null, // 2
    null, // 3
    Uint8Array, // 4
    Uint16Array, // 5
    Float32Array, // 6
    Float64Array, // 7
    Int8Array, // 8
    Int16Array, // 9
    Int32Array, // 10
    LongArray, // 11
];
