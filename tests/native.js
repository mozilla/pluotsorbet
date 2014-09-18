/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

Native["gnu/testlet/vm/NativeTest.getInt.()I"] = function(ctx, stack) {
  stack.push(0xFFFFFFFF);
}

Native["gnu/testlet/vm/NativeTest.fromJavaString.(Ljava/lang/String;)I"] = function(ctx, stack) {
  var str = util.fromJavaString(stack.pop());
  stack.push(str.length);
}
