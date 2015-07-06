/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

Native["com/sun/midp/crypto/PRand.getRandomBytes.([BI)Z"] = function(addr, bAddr, nbytes) {
    window.crypto.getRandomValues(J2ME.getArrayFromAddr(bAddr).subarray(0, nbytes));
    return 1;
};

MIDP.hashers = new Map();

/**
 * A 16-byte Int32Array whose values are all initialized to zero.
 * Useful for comparing with other such arrays to determine whether or not
 * they've been populated with other values.  Also useful for resetting
 * data arrays back to their initial state.
 */
MIDP.emptyDataArray = new Int32Array(16);

MIDP.getMD5Hasher = function(data) {
    if (!util.compareTypedArrays(data, MIDP.emptyDataArray)) {
        return MIDP.hashers.get(data);
    }

    var hasher = forge.md.md5.create();
    window.crypto.getRandomValues(data);
    MIDP.hashers.set(data, hasher);
    return hasher;
};

var bin2StringResult = new Array();
MIDP.bin2String = function(array) {
  bin2StringResult.length = array.length;
  for (var i = 0; i < array.length; i++) {
    bin2StringResult[i] = String.fromCharCode(array[i] & 0xff);
  }
  return bin2StringResult.join("");
};

Native["com/sun/midp/crypto/MD5.nativeUpdate.([BII[I[I[I[I)V"] =
function(addr, inBufAddr, inOff, inLen, stateAddr, numAddr, countAddr, dataAddr) {
    var inBuf = J2ME.getArrayFromAddr(inBufAddr);
    var data = J2ME.getArrayFromAddr(dataAddr);
    MIDP.getMD5Hasher(data).update(MIDP.bin2String(new Int8Array(inBuf.subarray(inOff, inOff + inLen))));
};

Native["com/sun/midp/crypto/MD5.nativeFinal.([BII[BI[I[I[I[I)V"] =
function(addr, inBufAddr, inOff, inLen, outBufAddr, outOff, stateAddr, numAddr, countAddr, dataAddr) {
    var inBuf;
    var outBuf = J2ME.getArrayFromAddr(outBufAddr);
    var data = J2ME.getArrayFromAddr(dataAddr);
    var hasher = MIDP.getMD5Hasher(data);

    if (inBufAddr) {
        // digest passes `null` for inBuf, and there are no other callers,
        // so this should never happen; but I'm including it for completeness
        // (and in case a subclass ever uses it).
        inBuf = J2ME.getArrayFromAddr(inBufAddr);
        hasher.update(MIDP.bin2String(inBuf.subarray(inOff, inOff + inLen)));
    }

    var hash = hasher.digest();

    for (var i = 0; i < hash.length(); i++) {
        outBuf[outOff + i] = hash.at(i);
    }

    // XXX Call the reset method instead to completely reset the object.
    data.set(MIDP.emptyDataArray);

    MIDP.hashers.delete(data);
};

Native["com/sun/midp/crypto/MD5.nativeClone.([I)V"] = function(addr, dataAddr) {
    var data = J2ME.getArrayFromAddr(dataAddr);
    for (var key of MIDP.hashers.keys()) {
        if (util.compareTypedArrays(key, data)) {
            var value = MIDP.hashers.get(key);
            var hasher = value.clone();
            window.crypto.getRandomValues(data);
            MIDP.hashers.set(data, hasher);
            break;
        }
    }
};

var hexEncodeArray = [ '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', ];

var bytesToHexStringResult = new Array();
function bytesToHexString(array) {
    bytesToHexStringResult.length = array.length * 2;
    for (var i = 0; i < array.length; i++) {
      var code = array[i] & 0xFF;
      bytesToHexStringResult[i * 2] = hexEncodeArray[code >>> 4];
      bytesToHexStringResult[i * 2 + 1] = hexEncodeArray[code & 0x0F];
    }
    return bytesToHexStringResult.join("");
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

Native["com/sun/midp/crypto/RSA.modExp.([B[B[B[B)I"] = function(addr, dataAddr, exponentAddr, modulusAddr, resultAddr) {
    // The jsbn library doesn't work well with typed arrays, so we're using this
    // hack of translating the numbers to hexadecimal strings before handing
    // them to jsbn (and we're getting the result back in a hex string).

    var data = J2ME.getArrayFromAddr(dataAddr);
    var exponent = J2ME.getArrayFromAddr(exponentAddr);
    var modulus = J2ME.getArrayFromAddr(modulusAddr);
    var result = J2ME.getArrayFromAddr(resultAddr);

    var bnBase = new BigInteger(bytesToHexString(data), 16);
    var bnExponent = new BigInteger(bytesToHexString(exponent), 16);
    var bnModulus = new BigInteger(bytesToHexString(modulus), 16);
    var bnRemainder = bnBase.modPow(bnExponent, bnModulus);
    var remainder = hexStringToBytes(bnRemainder.toString(16));

    result.set(remainder);
    return remainder.length;
};

Native["com/sun/midp/crypto/ARC4.nativetx.([B[I[I[BII[BI)V"] =
function(addr, SAddr, XAddr, YAddr, inbufAddr, inoff, inlen, outbufAddr, outoff) {
    var S = J2ME.getArrayFromAddr(SAddr);
    var X = J2ME.getArrayFromAddr(XAddr);
    var Y = J2ME.getArrayFromAddr(YAddr);
    var inbuf = J2ME.getArrayFromAddr(inbufAddr);
    var outbuf = J2ME.getArrayFromAddr(outbufAddr);

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
};
