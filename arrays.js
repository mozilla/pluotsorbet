/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

function LongArray(size) {
    var array = Array(size);
    array.class = CLASSES.getClass("[J");
    return array;
}

var ARRAYS = {
    'Z': Uint8Array,
    'C': Uint16Array,
    'F': Float32Array,
    'D': Float64Array,
    'B': Int8Array,
    'S': Int16Array,
    'I': Int32Array,
    'J': LongArray,
};
