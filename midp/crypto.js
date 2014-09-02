/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

Native["com/sun/midp/crypto/PRand.getRandomBytes.([BI)Z"] = function(ctx, stack) {
    var nbytes = stack.pop(), b = stack.pop();
    window.crypto.getRandomValues(b.subarray(0,nbytes));
    stack.push(1);
}

MIDP.hashers = new Map();

MIDP.createHasherGetter = function createHasherGetter(algo) {
    return function (data) {
      var hasher;

      // Use data[0] to determine the state of the hasher: if 1, we've created
      // the hasher (and are in the process of hashing); otherwise, we haven't.
      // That isn't what the data array is intended for, but it works well enough
      // for our purposes, and the non-native code doesn't use it for anything,
      // so it doesn't expect it to have any particular values.

      if (data[0] == 1) {
          hasher = MIDP.hashers.get(data);
      } else {
          hasher = algo.create();
          MIDP.hashers.set(data, hasher);
          data[0] = 1;
      }

      return hasher;
    }
}

MIDP.getSHA1Hasher = function(data) {
    var hasher;

    // Use data[0] to determine the state of the hasher: if 1, we've created
    // the hasher (and are in the process of hashing); otherwise, we haven't.
    // That isn't what the data array is intended for, but it works well enough
    // for our purposes, and the non-native code doesn't use it for anything,
    // so it doesn't expect it to have any particular values.

    if (data[0] == 1) {
        hasher = MIDP.hashers.get(data);
    } else {
        hasher = {
            buffer: new Int8Array(0),
            update: function(newData) {
              var oldData = this.buffer;
              this.buffer = new Int8Array(oldData.length + newData.length);
              this.buffer.set(oldData, 0);
              this.buffer.set(newData, oldData.length);
            },
        };
        MIDP.hashers.set(data, hasher);
        data[0] = 1;
    }

    return hasher;
};

MIDP.getMD5Hasher = MIDP.createHasherGetter(CryptoJS.algo.MD5);

MIDP.bin2String = function(array) {
  var result = "";
  for (var i = 0; i < array.length; i++) {
    result += String.fromCharCode(parseInt(array[i]));
  }
  return result;
};

Native["com/sun/midp/crypto/SHA.nativeUpdate.([BII[I[I[I[I)V"] = function(ctx, stack) {
    var data = stack.pop(), count = stack.pop(), num = stack.pop(), state = stack.pop(),
        inLen = stack.pop(), inOff = stack.pop(), inBuf = stack.pop();

    MIDP.getSHA1Hasher(data).update(inBuf.subarray(inOff, inOff + inLen));
}

MIDP.createNativeFinal = function createNativeFinal(hasherGetter) {
    return function(ctx, stack) {
        var data = stack.pop(), count = stack.pop(), num = stack.pop(), state = stack.pop(),
            outOff = stack.pop(), outBuf = stack.pop(), inLen = stack.pop(), inOff = stack.pop(),
            inBuf = stack.pop();

        var hasher = hasherGetter(data);

        if (inBuf) {
            // digest passes `null` for inBuf, and there are no other callers,
            // so this should never happen; but I'm including it for completeness
            // (and in case a subclass ever uses it).
            hasher.update(MIDP.bin2String(inBuf.subarray(inOff + inLen)));
        }

        var hash = hasher.finalize();

        for (var i = 0; i < hash.words.length; i++) {
            outBuf[outOff + i * 4] = (hash.words[i] >> 24) & 0xff;
            outBuf[outOff + i * 4 + 1] = (hash.words[i] >> 16) & 0xff;
            outBuf[outOff + i * 4 + 2] = (hash.words[i] >> 8) & 0xff;
            outBuf[outOff + i * 4 + 3] = hash.words[i] & 0xff;
        }

        // XXX Call the reset method instead to completely reset the object.
        data[0] = 0;

        MIDP.hashers.delete(data);
    }
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

    for (var i = 0; i < hash.length; i++) {
        outBuf[outOff + i * 4] = hash[i] & 0xff;
        outBuf[outOff + i * 4 + 1] = (hash[i] >> 8) & 0xff;
        outBuf[outOff + i * 4 + 2] = (hash[i] >> 16) & 0xff;
        outBuf[outOff + i * 4 + 3] = (hash[i] >> 24) & 0xff;
    }

    // XXX Call the reset method instead to completely reset the object.
    data[0] = 0;

    MIDP.hashers.delete(data);
}

Native["com/sun/midp/crypto/MD5.nativeUpdate.([BII[I[I[I[I)V"] = function(ctx, stack) {
    var data = stack.pop(), count = stack.pop(), num = stack.pop(), state = stack.pop(),
        inLen = stack.pop(), inOff = stack.pop(), inBuf = stack.pop();

    MIDP.getMD5Hasher(data).update(MIDP.bin2String(inBuf.subarray(inOff, inOff + inLen)));
}

Native["com/sun/midp/crypto/MD5.nativeFinal.([BII[BI[I[I[I[I)V"] = MIDP.createNativeFinal(MIDP.getMD5Hasher);

Native["com/sun/midp/crypto/RSA.modExp.([B[B[B[B)I"] = function(ctx, stack) {
    var result = stack.pop(), modulus = stack.pop(), exponent = stack.pop(), data = stack.pop();

    var bnBase = new BigInteger(data, 256);
    var bnExponent = new BigInteger(exponent, 256);
    var bnModulus = new BigInteger(modulus, 256);
    var bnRemainder = bnBase.modPow(bnExponent, bnModulus);
    var remainder = bnRemainder.toByteArray();

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
