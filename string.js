/* -*- tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */
'use strict';

Override["java/lang/String.equals.(Ljava/lang/Object;)Z"] = function(ctx, stack) {
  var str = stack.pop(), _this = stack.pop();
  if (str && str.class === _this.class) {
    stack.push(
      util.compareTypedArrays(
        util.javaStringToArrayBuffer(str),
        util.javaStringToArrayBuffer(_this))
        ? 1 : 0);
  } else {
    stack.push(0);
  }
};

Override["java/lang/String.indexOf.(Ljava/lang/String;I)I"] = function(ctx, stack) {
  var fromIndex = stack.pop(), str = stack.pop(), _this = stack.pop();

  if (!str) {
    ctx.raiseExceptionAndYield("java/lang/NullPointerException");
    return;
  }

  var needle = util.javaStringToArrayBuffer(str);
  var haystack = util.javaStringToArrayBuffer(_this);

  if (fromIndex >= haystack.length) {
    if (haystack.length === 0 && fromIndex === 0 && needle.length === 0) {
      stack.push(0);
    } else {
      stack.push(-1);
    }
  } else if (needle.length === 0) {
    stack.push(fromIndex);
  } else {
    var partialIdx = substringSearch(haystack.subarray(fromIndex), needle);

    if (partialIdx === -1) {
      stack.push(-1);
    } else {
      stack.push(fromIndex + partialIdx);
    }
  }
};

Override["java/lang/String.indexOf.(II)I"] = function(ctx, stack) {
  var fromIndex = stack.pop(), ch = stack.pop(), _this = stack.pop();

  var value = CLASSES.java_lang_String.getField("value", "[C").get(_this);
  var offset = CLASSES.java_lang_String.getField("offset", "I").get(_this);
  var count = CLASSES.java_lang_String.getField("count", "I").get(_this);
  var max = offset + count;

  if (fromIndex < 0) {
    fromIndex = 0;
  } else if (fromIndex >= count) {
    stack.push(-1);
    return;
  }
  for (var i = offset + fromIndex; i < max; i++) {
    if (value[i] == ch) {
      stack.push(i - offset);
      return;
    }
  }
  stack.push(-1);
};

Override["java/lang/String.hashCode.()I"] = function(ctx, stack) {
  var _this = stack.pop();
  var buf = util.javaStringToArrayBuffer(_this);

  // Same hashing algorithm as Java. If we find that this is too slow,
  // we might be able to get away with a simpler/dumber variant.
  var hash = 0;
  for (var i = 0; i < buf.length; i++) {
    hash = Math.imul(31, hash) + buf[i] | 0;
  }
  stack.push(hash);
};

Override["java/lang/String.charAt.(I)C"] = function(ctx, stack) {
  var idx = stack.pop(), _this = stack.pop();

  var buf = util.javaStringToArrayBuffer(_this);

  if (idx < 0 || idx >= buf.length) {
    ctx.raiseExceptionAndYield("java/lang/StringIndexOutOfBoundsException",
                               "String.charAt(" + idx + ")");
  } else {
    stack.push(buf[idx]);
  }
};

const SPACE_CODE_POINT = 32; // Everything below space is a control character.

Override["java/lang/String.trim.()Ljava/lang/String;"] = function(ctx, stack) {
  var _this = stack.pop();
  var buf = util.javaStringToArrayBuffer(_this);

  var end = buf.length;
  var start = 0;
  while ((start < end) && (buf[start] <= SPACE_CODE_POINT)) {
    start++;
  }
  while ((start < end) && (buf[end - 1] <= SPACE_CODE_POINT)) {
    end--;
  }

  if ((start > 0) || (end < buf.length)) {
    stack.push(ctx.newStringFromUint16Array(buf.subarray(start, end)));
  } else {
    stack.push(_this);
  }
};


Override["java/lang/String.substring.(II)Ljava/lang/String;"] = function(ctx, stack) {
  var endIndex = stack.pop(), beginIndex = stack.pop(), _this = stack.pop();
  var buf = util.javaStringToArrayBuffer(_this);

  if (beginIndex < 0 || endIndex > buf.length || beginIndex > endIndex) {
    ctx.raiseExceptionAndYield("java/lang/StringIndexOutOfBoundsException",
                               "String.substring()");
  } else {
    stack.push(ctx.newStringFromUint16Array(buf.subarray(beginIndex, endIndex)));
  }
};



/**
 * Boyer-Moore-Horspool: Efficient substring search, same as used in
 * Gecko. This function is compatible with typed arrays as well as
 * strings (we use it with typed arrays in the overrides above).
 *
 * Reference implementation:
 *   <http://en.wikipedia.org/wiki/Boyer%E2%80%93Moore%E2%80%93Horspool_algorithm>
 */
function substringSearch(haystack, needle) {
  var hlen = haystack.length;
  var nlen = needle.length;
  var badCharSkip = {};
  var last = nlen - 1;
  var scan;
  var offset = 0;

  if (nlen <= 0 || !haystack || !needle) {
    return -1;
  }
  
  for (scan = 0; scan < last; scan++) {
    badCharSkip[needle[scan]] = last - scan;
  }

  while (hlen >= nlen) {
    for (scan = last; haystack[offset + scan] === needle[scan]; scan--) {
      if (scan === 0) {
        return offset;
      }
    }

    var skip = badCharSkip[haystack[offset + last]] || nlen;
    hlen -= skip;
    offset += skip;
  }
  
  return -1;
}
