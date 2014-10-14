/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

Native.create("com/sun/midp/crypto/PRand.getRandomBytes.([BI)Z", function(ctx, b, nbytes) {
    window.crypto.getRandomValues(b.subarray(0, nbytes));
    return true;
}, { static: true });

MIDP.hashers = new Map();

/**
 * A constructor for a SHA-1 hasher.  To create a new hasher:
 *
 *     var hasher = new MIDP.SHA1Hasher();
 */
MIDP.SHA1Hasher = function() {
    this.input = new Int8Array(0);
};

/**
 * Add data to the hasher.  Does not implement true progressive hashing,
 * but simulates it well enough for the Java API that uses these natives.
 */
MIDP.SHA1Hasher.prototype.update = function(newInput) {
    var oldInput = this.input;
    this.input = new Int8Array(oldInput.length + newInput.length);
    this.input.set(oldInput, 0);
    this.input.set(newInput, oldInput.length);
};

/**
 * Clone this hasher.
 *
 * @returns {MIDP.SHA1Hasher} the cloned hasher
 */
MIDP.SHA1Hasher.prototype.clone = function() {
    var hasher = new MIDP.SHA1Hasher();
    hasher.update(this.input);
    return hasher;
};

MIDP.SHA1Hasher.prototype.digest = function() {
    var hash = new Rusha().rawDigest(this.input);
    return hash;
};

/**
 * A 16-byte Int32Array whose values are all initialized to zero.
 * Useful for comparing with other such arrays to determine whether or not
 * they've been populated with other values.  Also useful for resetting
 * data arrays back to their initial state.
 */
MIDP.emptyDataArray = new Int32Array(16);

MIDP.getSHA1Hasher = function(data) {
    if (!util.compareTypedArrays(data, MIDP.emptyDataArray)) {
        return MIDP.hashers.get(data);
    }

    var hasher = new MIDP.SHA1Hasher();
    window.crypto.getRandomValues(data);
    MIDP.hashers.set(data, hasher);
    return hasher;
};

MIDP.getMD5Hasher = function(data) {
    if (!util.compareTypedArrays(data, MIDP.emptyDataArray)) {
        return MIDP.hashers.get(data);
    }

    var hasher = forge.md.md5.create();
    window.crypto.getRandomValues(data);
    MIDP.hashers.set(data, hasher);
    return hasher;
};

MIDP.bin2String = function(array) {
  var result = "";
  for (var i = 0; i < array.length; i++) {
    result += String.fromCharCode(array[i] & 0xff);
  }
  return result;
};

Native.create("com/sun/midp/crypto/SHA.nativeUpdate.([BII[I[I[I[I)V", function(ctx, inBuf, inOff, inLen, state, num, count, data) {
    MIDP.getSHA1Hasher(data).update(inBuf.subarray(inOff, inOff + inLen));
}, { static: true });

Native.create("com/sun/midp/crypto/SHA.nativeFinal.([BII[BI[I[I[I[I)V", function(ctx, inBuf, inOff, inLen, outBuf, outOff, state, num, count, data) {
    var hasher = MIDP.getSHA1Hasher(data);

    if (inBuf) {
        // digest passes `null` for inBuf, and there are no other callers,
        // so this should never happen; but I'm including it for completeness
        // (and in case a subclass ever uses it).
        hasher.update(inBuf.subarray(inOff, inOff + inLen));
    }

    var hash = hasher.digest();
    outBuf.set(new Uint8Array(hash.buffer), outOff);

    // XXX Call the reset method instead to completely reset the object.
    data.set(MIDP.emptyDataArray);

    MIDP.hashers.delete(data);
}, { static: true });

Native.create("com/sun/midp/crypto/SHA.nativeClone.([I)V", function(ctx, data) {
    for (var key of MIDP.hashers.keys()) {
        if (util.compareTypedArrays(key, data)) {
            var value = MIDP.hashers.get(key);
            var hasher = value.clone();
            window.crypto.getRandomValues(data);
            MIDP.hashers.set(data, hasher);
            break;
        }
    }
}, { static: true });

Native.create("com/sun/midp/crypto/MD5.nativeUpdate.([BII[I[I[I[I)V", function(ctx, inBuf, inOff, inLen, state, num, count, data) {
    MIDP.getMD5Hasher(data).update(MIDP.bin2String(new Uint8Array(inBuf.subarray(inOff, inOff + inLen))));
}, { static: true });

Native.create("com/sun/midp/crypto/MD5.nativeFinal.([BII[BI[I[I[I[I)V", function(ctx, inBuf, inOff, inLen, outBuf, outOff, state, num, count, data) {
    var hasher = MIDP.getMD5Hasher(data);

    if (inBuf) {
        // digest passes `null` for inBuf, and there are no other callers,
        // so this should never happen; but I'm including it for completeness
        // (and in case a subclass ever uses it).
        hasher.update(MIDP.bin2String(inBuf.subarray(inOff, inOff + inLen)));
    }

    var hash = hasher.digest();

    for (var i = 0; i < hash.length(); i++) {
        outBuf[outOff + i] = hash.at(i);
    }

    // XXX Call the reset method instead to completely reset the object.
    data.set(MIDP.emptyDataArray);

    MIDP.hashers.delete(data);
}, { static: true });

Native.create("com/sun/midp/crypto/MD5.nativeClone.([I)V", function(ctx, data) {
    for (var key of MIDP.hashers.keys()) {
        if (util.compareTypedArrays(key, data)) {
            var value = MIDP.hashers.get(key);
            var hasher = value.clone();
            window.crypto.getRandomValues(data);
            MIDP.hashers.set(data, hasher);
            break;
        }
    }
}, { static: true });

var hexEncodeArray = [ '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', ];

function bytesToHexString(array) {
    var s = '';
    for (var i = 0; i < array.length; i++) {
      var code = array[i] & 0xFF;
      s += hexEncodeArray[code >>> 4];
      s += hexEncodeArray[code & 0x0F];
    }
    return s;
}

function hexStringToBytes(hex) {
    // The jsbn library (that provides BigInteger support) produces a
    // hexadecimal string that doesn't contain a leading 0 (e.g. "010010" would
    // be "10010").
    var length = hex.length / 2;
    if (length % 1 !== 0) {
      hex = "0" + hex;
    }

    var bytes = new Int8Array(hex.length / 2);

    for (var i = 0; i < hex.length; i += 2) {
        bytes[i/2] = parseInt(hex.substr(i, 2), 16);
    }

    return bytes;
}

Native.create("com/sun/midp/crypto/RSA.modExp.([B[B[B[B)I", function(ctx, data, exponent, modulus, result) {
    // The jsbn library doesn't work well with typed arrays, so we're using this
    // hack of translating the numbers to hexadecimal strings before handing
    // them to jsbn (and we're getting the result back in a hex string).

    var bnBase = new BigInteger(bytesToHexString(data), 16);
    var bnExponent = new BigInteger(bytesToHexString(exponent), 16);
    var bnModulus = new BigInteger(bytesToHexString(modulus), 16);
    var bnRemainder = bnBase.modPow(bnExponent, bnModulus);
    var remainder = hexStringToBytes(bnRemainder.toString(16));

    result.set(remainder);
    return remainder.length;
}, { static: true });

Native.create("com/sun/midp/crypto/ARC4.nativetx.([B[I[I[BII[BI)V", function(ctx, S, X, Y, inbuf, inoff, inlen, outbuf, outoff) {
    var x = X[0];
    var y = Y[0];

    for (var i = 0; i < inlen; i++) {
        x = (x + 1) & 0xff;
        y = (y + S[x]) & 0xff;

        var tx = S[x];
        S[x] = S[y];
        S[y] = tx;

        var ty = S[x] + S[y] & 0xff;

        outbuf[i+outoff] = S[ty] ^ inbuf[i+inoff];
    }

    X[0] = x;
    Y[0] = y;
}, { static: true });
