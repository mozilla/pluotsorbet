/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

Native["gnu/testlet/vm/NativeTest.getInt.()I"] = function(ctx, stack) {
  stack.push(0xFFFFFFFF);
}

Native["gnu/testlet/vm/NativeTest.getLongReturnLong.(J)J"] = function(ctx, stack) {
  var val = stack.pop2();
  stack.push2(Long.fromNumber(40 + val.toNumber()));
}

Native["gnu/testlet/vm/NativeTest.getLongReturnInt.(J)I"] = function(ctx, stack) {
  var val = stack.pop2().toNumber();
  stack.push(40 + val);
}

Native["gnu/testlet/vm/NativeTest.getIntReturnLong.(I)J"] = function(ctx, stack) {
  var val = stack.pop();
  stack.push2(Long.fromNumber(40 + val));
}

Native["gnu/testlet/vm/NativeTest.throwException.()V"] = function(ctx, stack) {
  ctx.raiseExceptionAndYield("java/lang/NullPointerException", "An exception");
}

Native["gnu/testlet/vm/NativeTest.throwExceptionAfterPause.()V"] = function(ctx, stack) {
  setTimeout(function() {
    ctx.raiseException("java/lang/NullPointerException", "An exception");
    ctx.resume();
  }, 100);

  throw VM.Pause;
}

Native["gnu/testlet/vm/NativeTest.returnAfterPause.()I"] = function(ctx, stack) {
  setTimeout(function() {
    stack.push(42);
    ctx.resume();
  }, 100);

  throw VM.Pause;
}

Native["gnu/testlet/vm/NativeTest.nonStatic.(I)I"] = function(ctx, stack) {
  var val = stack.pop(), _this = stack.pop();
  stack.push(val + 40);
}

Native["gnu/testlet/vm/NativeTest.fromJavaString.(Ljava/lang/String;)I"] = function(ctx, stack) {
  var str = util.fromJavaString(stack.pop());
  stack.push(str.length);
}

Native["gnu/testlet/vm/NativeTest.decodeUtf8.([B)I"] = function(ctx, stack) {
  var str = util.decodeUtf8(stack.pop());
  stack.push(str.length);
}

Native["gnu/testlet/vm/NativeTest.newFunction.()Z"] = function(ctx, stack) {
  try {
    new Function();
    stack.push(1);
  } catch(ex) {
    console.error(ex);
    stack.push(0);
  }
}

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
