/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

MIDP.lastSMSConnection = -1;

Native["com/sun/midp/io/j2me/sms/Protocol.open0.(Ljava/lang/String;II)I"] = function(ctx, stack) {
    var port = stack.pop(), msid = stack.pop(), host = util.fromJavaString(stack.pop()), _this = stack.pop();

    console.warn("com/sun/midp/io/j2me/sms/Protocol.open0.(L...String;II)I not implemented + (" + host + ", " + msid + ", " + port + ")");

    stack.push(++MIDP.lastSMSConnection);
}

Native["com/sun/midp/io/j2me/sms/Protocol.receive0.(IIILcom/sun/midp/io/j2me/sms/Protocol$SMSPacket;)I"] = function(ctx, stack) {
    var smsPacket = stack.pop(), handle = stack.pop(), msid = stack.pop(), port = stack.pop(), _this = stack.pop();

    console.warn("com/sun/midp/io/j2me/sms/Protocol.receive0.(IIIL...Protocol$SMSPacket;)I not implemented + (" + handle + ")");

    // Block until a message is received
    throw VM.Pause;
}

Native["com/sun/midp/io/j2me/sms/Protocol.close0.(III)I"] = function(ctx, stack) {
    var deRegister = stack.pop(), handle = stack.pop(), port = stack.pop(), _this = stack.pop();

    console.warn("com/sun/midp/io/j2me/sms/Protocol.close0.(III)I not implemented + (" + handle + ")");

    stack.push(0);
}
