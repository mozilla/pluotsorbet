/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

Native["com/sun/midp/crypto/PRand.getRandomBytes.([BI)Z"] = function(ctx, stack) {
    var nbytes = stack.pop(), b = stack.pop();
    window.crypto.getRandomValues(b.subarray(0,nbytes));
    stack.push(1);
}

MIDP.hashers = new Map();

MIDP.getRandomInt = function(min, max) {
  return Math.floor(Math.random() * (max - min)) + min;
};

/**
 * A constructor for a SHA-1 hasher.  To create a new hasher:
 *
 *     var hasher = new MIDP.SHA1Hasher();
 */
MIDP.SHA1Hasher = function() {
    this.buffer = new Int8Array(0);
};

/**
 * Add data to the hasher.  Does not implement true progressive hashing,
 * but simulates it well enough for the Java API that uses these natives.
 */
MIDP.SHA1Hasher.prototype.update = function(newData) {
    var oldData = this.buffer;
    this.buffer = new Int8Array(oldData.length + newData.length);
    this.buffer.set(oldData, 0);
    this.buffer.set(newData, oldData.length);
};

/**
 * Clone this hasher.
 *
 * @returns {MIDP.SHA1Hasher} the cloned hasher
 */
MIDP.SHA1Hasher.prototype.clone = function() {
    var hasher = new MIDP.SHA1Hasher();
    hasher.update(this.buffer);
    return hasher;
};

MIDP.compareTypedArrays = function(ary1, ary2) {
    if (ary1.length != ary2.length) {
        return false;
    }

    for (var i = 0; i < ary1.length; i++) {
        if (ary1[i] !== ary2[i]) {
            return false;
        }
    }

    return true;
};

/**
 * A 16-byte Int8Array whose values are all initialized to zero.
 * Useful for comparing with other such arrays to determine whether or not
 * they've been populated with other values.  Also useful for resetting
 * data arrays back to their initial state.
 */
MIDP.emptyDataArray = new Int32Array(16);

MIDP.getSHA1Hasher = function(data) {
    var hasher;

    if (!MIDP.compareTypedArrays(data, MIDP.emptyDataArray)) {
        hasher = MIDP.hashers.get(data);

        if (hasher) {
            return hasher;
        }

        // If data contains a value, but we don't yet have a native hasher
        // for it, then it was cloned from the data of another native hasher,
        // so find the original and clone it.
        for (var [key, value] of MIDP.hashers) {
            if (MIDP.compareTypedArrays(key, data)) {
                hasher = value.clone();
                window.crypto.getRandomValues(data);
                MIDP.hashers.set(data, hasher);
                return hasher;
            }
        }

        throw new Error("couldn't get existing hasher");
    }

    hasher = new MIDP.SHA1Hasher();
    window.crypto.getRandomValues(data);
    MIDP.hashers.set(data, hasher);
    return hasher;
};

MIDP.getMD5Hasher = function(data) {
    var hasher;

    if (!MIDP.compareTypedArrays(data, MIDP.emptyDataArray)) {
        hasher = MIDP.hashers.get(data);

        if (hasher) {
            return hasher;
        }

        // If data contains a value, but we don't yet have a native hasher
        // for it, then it was cloned from the data of another native hasher,
        // so find the original and clone it.
        for (var [key, value] of MIDP.hashers) {
            if (MIDP.compareTypedArrays(key, data)) {
                hasher = value.clone();
                window.crypto.getRandomValues(data);
                MIDP.hashers.set(data, hasher);
                return hasher;
            }
        }

        throw new Error("couldn't get existing hasher");
    }

    hasher = CryptoJS.algo.MD5.create();
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

Native["com/sun/midp/crypto/SHA.nativeUpdate.([BII[I[I[I[I)V"] = function(ctx, stack) {
    var data = stack.pop(), count = stack.pop(), num = stack.pop(), state = stack.pop(),
        inLen = stack.pop(), inOff = stack.pop(), inBuf = stack.pop();

    MIDP.getSHA1Hasher(data).update(inBuf.subarray(inOff, inOff + inLen));
}

Native["com/sun/midp/crypto/SHA.nativeFinal.([BII[BI[I[I[I[I)V"] = function(ctx, stack) {
    var data = stack.pop(), count = stack.pop(), num = stack.pop(), state = stack.pop(),
        outOff = stack.pop(), outBuf = stack.pop(), inLen = stack.pop(), inOff = stack.pop(),
        inBuf = stack.pop();

    var hasher = MIDP.getSHA1Hasher(data);

    if (inBuf) {
        // digest passes `null` for inBuf, and there are no other callers,
        // so this should never happen; but I'm including it for completeness
        // (and in case a subclass ever uses it).
        hasher.update(inBuf.subarray(inOff, inOff + inLen));
    }

    var hash = new Rusha().rawDigest(hasher.buffer);
    outBuf.set(new Uint8Array(hash.buffer), outOff);

    // XXX Call the reset method instead to completely reset the object.
    data.set(MIDP.emptyDataArray);

    MIDP.hashers.delete(data);
}

Native["com/sun/midp/crypto/MD5.nativeUpdate.([BII[I[I[I[I)V"] = function(ctx, stack) {
    var data = stack.pop(), count = stack.pop(), num = stack.pop(), state = stack.pop(),
        inLen = stack.pop(), inOff = stack.pop(), inBuf = stack.pop();

    MIDP.getMD5Hasher(data).update(MIDP.bin2String(inBuf.subarray(inOff, inOff + inLen)));
}

Native["com/sun/midp/crypto/MD5.nativeFinal.([BII[BI[I[I[I[I)V"] = function(ctx, stack) {
    var data = stack.pop(), count = stack.pop(), num = stack.pop(), state = stack.pop(),
        outOff = stack.pop(), outBuf = stack.pop(), inLen = stack.pop(), inOff = stack.pop(),
        inBuf = stack.pop();

    var hasher = MIDP.getMD5Hasher(data);

    if (inBuf) {
        // digest passes `null` for inBuf, and there are no other callers,
        // so this should never happen; but I'm including it for completeness
        // (and in case a subclass ever uses it).
        hasher.update(MIDP.bin2String(inBuf.subarray(inOff, inOff + inLen)));
    }

    var hash = hasher.finalize();

    for (var i = 0; i < hash.words.length; i++) {
        outBuf[outOff + i * 4] = (hash.words[i] >> 24) & 0xff;
        outBuf[outOff + i * 4 + 1] = (hash.words[i] >> 16) & 0xff;
        outBuf[outOff + i * 4 + 2] = (hash.words[i] >> 8) & 0xff;
        outBuf[outOff + i * 4 + 3] = hash.words[i] & 0xff;
    }

    // XXX Call the reset method instead to completely reset the object.
    data.set(MIDP.emptyDataArray);

    MIDP.hashers.delete(data);
}

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

Native["com/sun/midp/crypto/RSA.modExp.([B[B[B[B)I"] = function(ctx, stack) {
    var result = stack.pop(), modulus = stack.pop(), exponent = stack.pop(), data = stack.pop();

    // The jsbn library doesn't work well with typed arrays, so we're using this
    // hack of translating the numbers to hexadecimal strings before handing
    // them to jsbn (and we're getting the result back in a hex string).

    var bnBase = new BigInteger(bytesToHexString(data), 16);
    var bnExponent = new BigInteger(bytesToHexString(exponent), 16);
    var bnModulus = new BigInteger(bytesToHexString(modulus), 16);
    var bnRemainder = bnBase.modPow(bnExponent, bnModulus);
    var remainder = hexStringToBytes(bnRemainder.toString(16));

    result.set(remainder);
    stack.push(remainder.length);
}

Native["com/sun/midp/crypto/ARC4.nativetx.([B[I[I[BII[BI)V"] = function(ctx, stack) {
    var outoff = stack.pop(), outbuf = stack.pop(), inlen = stack.pop(), inoff = stack.pop(),
        inbuf = stack.pop(), Y = stack.pop(), X = stack.pop(), S = stack.pop();

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
}
