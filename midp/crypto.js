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

MIDP.getSHA1Hasher = MIDP.createHasherGetter(CryptoJS.algo.SHA1);

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

    MIDP.getSHA1Hasher(data).update(MIDP.bin2String(inBuf.subarray(inOff, inOff + inLen)));
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

Native["com/sun/midp/crypto/SHA.nativeFinal.([BII[BI[I[I[I[I)V"] = MIDP.createNativeFinal(MIDP.getSHA1Hasher);

Native["com/sun/midp/crypto/MD5.nativeUpdate.([BII[I[I[I[I)V"] = function(ctx, stack) {
    var data = stack.pop(), count = stack.pop(), num = stack.pop(), state = stack.pop(),
        inLen = stack.pop(), inOff = stack.pop(), inBuf = stack.pop();

    MIDP.getMD5Hasher(data).update(MIDP.bin2String(inBuf.subarray(inOff, inOff + inLen)));
}

Native["com/sun/midp/crypto/MD5.nativeFinal.([BII[BI[I[I[I[I)V"] = MIDP.createNativeFinal(MIDP.getMD5Hasher);
