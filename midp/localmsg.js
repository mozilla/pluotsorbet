/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var LocalMsgConnection = function() {
    this.waitingForConnection = null;
    this.serverWaiting = null;
    this.clientWaiting = null;
    this.serverMessages = [];
    this.clientMessages = [];
}

LocalMsgConnection.prototype.notifyConnection = function() {
    if (this.waitingForConnection) {
        this.waitingForConnection();
    }
}

LocalMsgConnection.prototype.waitConnection = function(ctx) {
    this.waitingForConnection = function() {
        this.waitingForConnection = null;
        ctx.resume();
    }

    throw VM.Pause;
}

LocalMsgConnection.prototype.copyMessage = function(messageQueue, data) {
    var msg = messageQueue.shift();

    for (var i = 0; i < msg.length; i++) {
        data[i] = msg.data[i + msg.offset];
    }

    return msg.length;
}

LocalMsgConnection.prototype.sendMessageToClient = function(message) {
    this.clientMessages.push(message);

    if (this.clientWaiting) {
        this.clientWaiting();
    }
}

LocalMsgConnection.prototype.clientReceiveMessage = function(ctx, stack, data) {
    if (this.clientMessages.length == 0) {
        this.clientWaiting = function() {
            this.clientWaiting = null;

            stack.push(this.copyMessage(this.clientMessages, data));

            ctx.resume();
        }

        throw VM.Pause;
    }

    stack.push(this.copyMessage(this.clientMessages, data));
}

LocalMsgConnection.prototype.sendMessageToServer = function(message) {
    this.serverMessages.push(message);

    if (this.serverWaiting) {
        this.serverWaiting();
    }
}

LocalMsgConnection.prototype.serverReceiveMessage = function(ctx, stack, data) {
    if (this.serverMessages.length == 0) {
        this.serverWaiting = function() {
            this.serverWaiting = null;

            stack.push(this.copyMessage(this.serverMessages, data));

            ctx.resume();
        }

        throw VM.Pause;
    }

    stack.push(this.copyMessage(this.serverMessages, data));
}

var NokiaMessagingLocalMsgConnection = function() {
    LocalMsgConnection.call(this);
}

NokiaMessagingLocalMsgConnection.prototype = Object.create(LocalMsgConnection.prototype);

NokiaMessagingLocalMsgConnection.prototype.receiveSMS = function(sms) {
  var encoder = new DataEncoder();

  encoder.putStart(DataType.STRUCT, "event");
  encoder.put(DataType.METHOD, "name", "MessageNotify");
  encoder.put(DataType.USHORT, "trans_id", Date.now() % 255); // The meaning of this field is unknown
  encoder.put(DataType.STRING, "type", "SMS"); // The name of this field is unknown
  encoder.put(DataType.ULONG, "message_id", sms.id);
  encoder.putEnd(DataType.STRUCT, "event");

  var data = new TextEncoder().encode(encoder.getData());
  this.sendMessageToClient({
    data: data,
    length: data.length,
    offset: 0,
  });
}

NokiaMessagingLocalMsgConnection.prototype.sendMessageToServer = function(message) {
  var encoder = new DataEncoder();

  var decoder = new DataDecoder(message.data, message.offset, message.length);

  decoder.getStart(DataType.STRUCT);
  var name = decoder.getValue(DataType.METHOD);

  switch (name) {
    case "Common":
      encoder.putStart(DataType.STRUCT, "event");
      encoder.put(DataType.METHOD, "name", "Common");
      encoder.putStart(DataType.STRUCT, "message");
      encoder.put(DataType.METHOD, "name", "ProtocolVersion");
      encoder.put(DataType.STRING, "version", "2.[0-10]");
      encoder.putEnd(DataType.STRUCT, "message");
      encoder.putEnd(DataType.STRUCT, "event");
    break;

    case "SubscribeMessages":
      encoder.putStart(DataType.STRUCT, "event");
      encoder.put(DataType.METHOD, "name", "SubscribeMessages");
      encoder.put(DataType.USHORT, "trans_id", decoder.getValue(DataType.USHORT)); // The meaning of this field is unknown
      encoder.put(DataType.STRING, "result", "OK"); // The name of this field is unknown
      encoder.putEnd(DataType.STRUCT, "event");
    break;

    case "GetMessageEntity":
      var trans_id = decoder.getValue(DataType.USHORT);
      var sms_id = decoder.getValue(DataType.ULONG);

      var sms;
      for (var i = 0; i < MIDP.nokiaSMSMessages.length; i++) {
        if (MIDP.nokiaSMSMessages[i].id == sms_id) {
          sms = MIDP.nokiaSMSMessages[i];
          break;
        }
      }

      encoder.putStart(DataType.STRUCT, "event");
      encoder.put(DataType.METHOD, "name", "GetMessageEntity");
      encoder.put(DataType.USHORT, "trans_id", trans_id); // The meaning of this field is unknown
      encoder.put(DataType.STRING, "result", "OK"); // The name of this field is unknown
      encoder.put(DataType.ULONG, "message_id", sms_id);
      encoder.putStart(DataType.LIST, "list_name_unknown"); // The name of this field is unknown
      encoder.put(DataType.WSTRING, "body_text", sms.text);
      encoder.put(DataType.STRING, "address", sms.addr);
      encoder.putEnd(DataType.LIST);
      encoder.putEnd(DataType.STRUCT, "event");
    break;

    case "DeleteMessages":
      decoder.getValue(DataType.USHORT);
      decoder.getStart(DataType.ARRAY);
      var sms_id = decoder.getValue(DataType.ULONG);

      for (var i = 0; i < MIDP.nokiaSMSMessages.length; i++) {
        if (MIDP.nokiaSMSMessages[i].id == sms_id) {
          MIDP.nokiaSMSMessages.splice(i, 1);
          break;
        }
      }
    break;

    default:
      console.error("(nokia.messaging) event " + name + " not implemented");
      return;
  }

  var data = new TextEncoder().encode(encoder.getData());
  this.sendMessageToClient({
    data: data,
    length: data.length,
    offset: 0,
  });
}

MIDP.LocalMsgConnections = {};

// Add some fake servers because some MIDlets assume they exist.
// MIDlets are usually happy even if the servers don't reply, but we should
// remember to implement them in case they will be needed.
MIDP.FakeLocalMsgServers = [ "nokia.phone-status", "nokia.active-standby", "nokia.profile",
                             "nokia.connectivity-settings", "nokia.contacts", "nokia.file-ui" ];
MIDP.FakeLocalMsgServers.forEach(function(server) {
    MIDP.LocalMsgConnections[server] = new LocalMsgConnection();
});

MIDP.LocalMsgConnections["nokia.messaging"] = new NokiaMessagingLocalMsgConnection();

Native["org/mozilla/io/LocalMsgConnection.init.(Ljava/lang/String;)V"] = function(ctx, stack) {
    var name = util.fromJavaString(stack.pop()), _this = stack.pop();

    _this.server = (name[2] == ":");
    _this.protocolName = name.slice((name[2] == ':') ? 3 : 2);

    if (_this.server) {
        MIDP.LocalMsgConnections[_this.protocolName] = new LocalMsgConnection();
        pushNotify("localmsg:" + _this.protocolName);
    } else {
        // Actually, there should always be a server, but we need this check
        // for apps that use the Nokia built-in servers (because we haven't
        // implemented them yet).
        if (!MIDP.LocalMsgConnections[_this.protocolName]) {
            console.warn("localmsg server (" + _this.protocolName + ") unimplemented");
            throw VM.Pause;
        }

        if (MIDP.FakeLocalMsgServers.indexOf(_this.protocolName) != -1) {
            console.warn("connect to an unimplemented localmsg server (" + _this.protocolName + ")");
        }

        MIDP.LocalMsgConnections[_this.protocolName].notifyConnection();
    }
}

Native["org/mozilla/io/LocalMsgConnection.waitConnection.()V"] = function(ctx, stack) {
    var _this = stack.pop();

    MIDP.LocalMsgConnections[_this.protocolName].waitConnection(ctx);
}

Native["org/mozilla/io/LocalMsgConnection.sendData.([BII)V"] = function(ctx, stack) {
    var length = stack.pop(), offset = stack.pop(), data = stack.pop(), _this = stack.pop();

    var message = {
      data: data,
      offset: offset,
      length: length,
    };

    if (_this.server) {
        MIDP.LocalMsgConnections[_this.protocolName].sendMessageToClient(message);
    } else {
        if (MIDP.FakeLocalMsgServers.indexOf(_this.protocolName) != -1) {
            console.warn("sendData (" + util.decodeUtf8(new Uint8Array(data.buffer, offset, length)) + ") to an unimplemented localmsg server (" + _this.protocolName + ")");
        }

        MIDP.LocalMsgConnections[_this.protocolName].sendMessageToServer(message);
    }
}

Native["org/mozilla/io/LocalMsgConnection.receiveData.([B)I"] = function(ctx, stack) {
    var data = stack.pop(), _this = stack.pop();

    if (_this.server) {
        MIDP.LocalMsgConnections[_this.protocolName].serverReceiveMessage(ctx, stack, data);
    } else {
        if (MIDP.FakeLocalMsgServers.indexOf(_this.protocolName) != -1) {
            console.warn("receiveData from an unimplemented localmsg server (" + _this.protocolName + ")");
        }

        MIDP.LocalMsgConnections[_this.protocolName].clientReceiveMessage(ctx, stack, data);
    }
}

Native["org/mozilla/io/LocalMsgConnection.closeConnection.()V"] = function(ctx, stack) {
    var _this = stack.pop()

    if (_this.server) {
        delete MIDP.LocalMsgConnections[_this.protocolName];
    }
}
