/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

MIDP.lastSMSConnection = -1;
MIDP.lastSMSID = -1;
MIDP.smsConnections = {};
MIDP.j2meSMSMessages = [];
MIDP.j2meSMSWaiting = null;
MIDP.nokiaSMSMessages = [];

document.getElementById("sms_receive").onclick = function() {
    var text = document.getElementById("sms_text").value;
    var addr = document.getElementById("sms_addr").value;

    document.getElementById("sms_text").value = "SMS text";
    document.getElementById("sms_addr").value = "SMS phone number";

    var sms = {
      text: text,
      addr: addr,
      id: ++MIDP.lastSMSID,
    };

    MIDP.nokiaSMSMessages.push(sms);
    MIDP.j2meSMSMessages.push(sms);

    MIDP.LocalMsgConnections["nokia.messaging"].receiveSMS(sms);

    if (MIDP.j2meSMSWaiting) {
      MIDP.j2meSMSWaiting();
    }
}

Native["com/sun/midp/io/j2me/sms/Protocol.open0.(Ljava/lang/String;II)I"] = function(ctx, stack) {
    var port = stack.pop(), msid = stack.pop(), host = util.fromJavaString(stack.pop()), _this = stack.pop();

    MIDP.smsConnections[++MIDP.lastSMSConnection] = {
      port: port,
      msid: msid,
      host: host,
    };

    stack.push(++MIDP.lastSMSConnection);
}

Native["com/sun/midp/io/j2me/sms/Protocol.receive0.(IIILcom/sun/midp/io/j2me/sms/Protocol$SMSPacket;)I"] = function(ctx, stack) {
    var smsPacket = stack.pop(), handle = stack.pop(), msid = stack.pop(), port = stack.pop(), _this = stack.pop();

    function receiveSMS() {
        var sms = MIDP.j2meSMSMessages.shift();
        var text = sms.text;
        var addr = sms.addr;

        var message = ctx.newPrimitiveArray("B", text.length);
        for (var i = 0; i < text.length; i++) {
            message[i] = text.charCodeAt(i);
        }

        var address = ctx.newPrimitiveArray("B", addr.length);
        for (var i = 0; i < addr.length; i++) {
            address[i] = addr.charCodeAt(i);
        }

        smsPacket.class.getField("message", "[B").set(smsPacket, message);
        smsPacket.class.getField("address", "[B").set(smsPacket, address);
        smsPacket.class.getField("port", "I").set(smsPacket, port);
        smsPacket.class.getField("sentAt", "J").set(smsPacket, Long.fromNumber(Date.now()));
        smsPacket.class.getField("messageType", "I").set(smsPacket, 0); // GSM_TEXT

        stack.push(text.length);
    }

    if (MIDP.j2meSMSMessages.length > 0) {
      receiveSMS();
    } else {
      MIDP.j2meSMSWaiting = function() {
        MIDP.j2meSMSWaiting = null;
        receiveSMS();
        ctx.resume();
      }
    }

    throw VM.Pause;
}

Native["com/sun/midp/io/j2me/sms/Protocol.close0.(III)I"] = function(ctx, stack) {
    var deRegister = stack.pop(), handle = stack.pop(), port = stack.pop(), _this = stack.pop();

    delete MIDP.smsConnections[handle];

    stack.push(0);
}
