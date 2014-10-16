/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

Native.create("gnu/testlet/vm/NativeTest.getInt.()I", function(ctx) {
  return 0xFFFFFFFF;
});

Native.create("gnu/testlet/vm/NativeTest.getLongReturnLong.(J)J", function(ctx, val, _) {
  return Long.fromNumber(40 + val.toNumber());
});

Native.create("gnu/testlet/vm/NativeTest.getLongReturnInt.(J)I", function(ctx, val, _) {
  return 40 + val.toNumber();
});

Native.create("gnu/testlet/vm/NativeTest.getIntReturnLong.(I)J", function(ctx, val) {
  return Long.fromNumber(40 + val);
});

Native.create("gnu/testlet/vm/NativeTest.throwException.()V", function(ctx) {
  throw new JavaException("java/lang/NullPointerException", "An exception");
});

Native.create("gnu/testlet/vm/NativeTest.throwExceptionAfterPause.()V", function(ctx) {
  setTimeout(function() {
    ctx.raiseException("java/lang/NullPointerException", "An exception");
    ctx.resume();
  }, 100);

  throw VM.Pause;
});

Native["gnu/testlet/vm/NativeTest.returnAfterPause.()I"] = function(ctx, stack) {
  setTimeout(function() {
    stack.push(42);
    ctx.resume();
  }, 100);

  throw VM.Pause;
}

Native.create("gnu/testlet/vm/NativeTest.nonStatic.(I)I", function(ctx, val) {
  return val + 40;
});

Native.create("gnu/testlet/vm/NativeTest.fromJavaString.(Ljava/lang/String;)I", function(ctx, str) {
  return util.fromJavaString(str).length;
});

Native.create("gnu/testlet/vm/NativeTest.decodeUtf8.([B)I", function(ctx, str) {
  return util.decodeUtf8(str).length;
});

Native.create("gnu/testlet/vm/NativeTest.newFunction.()Z", function(ctx) {
  try {
    var fn = new Function("return true;");
    return fn();
  } catch(ex) {
    console.error(ex);
    return false;
  }
});

Native["gnu/testlet/vm/NativeTest.dumbPipe.()Z"] = function(ctx, stack) {
  // Ensure we can echo a large amount of data.
  var array = [];
  for (var i = 0; i < 128 * 1024; i++) {
    array[i] = i;
  }
  DumbPipe.open("echo", array, function(message) {
    stack.push(JSON.stringify(array) === JSON.stringify(message) ? 1 : 0);
    ctx.resume();
  });

  throw VM.Pause;
}
