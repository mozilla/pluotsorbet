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

LocalMsgConnection.prototype.waitConnection = function() {
    return new Promise((function(resolve, reject) {
      this.waitingForConnection = function() {
          this.waitingForConnection = null;
          resolve();
      }
    }).bind(this));
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

LocalMsgConnection.prototype.clientReceiveMessage = function(data) {
    return new Promise((function(resolve, reject) {
        if (this.clientMessages.length == 0) {
            this.clientWaiting = function() {
                this.clientWaiting = null;

                resolve(this.copyMessage(this.clientMessages, data));
            }

            return;
        }

        resolve(this.copyMessage(this.clientMessages, data));
    }).bind(this));
}

LocalMsgConnection.prototype.sendMessageToServer = function(message) {
    this.serverMessages.push(message);

    if (this.serverWaiting) {
        this.serverWaiting();
    }
}

LocalMsgConnection.prototype.serverReceiveMessage = function(data) {
    return new Promise((function(resolve, reject) {
        if (this.serverMessages.length == 0) {
            this.serverWaiting = function() {
                this.serverWaiting = null;

                resolve(this.copyMessage(this.serverMessages, data));
            }
            return;
        }

        resolve(this.copyMessage(this.serverMessages, data));
    }).bind(this));
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

      return;
    break;

    default:
      console.error("(nokia.messaging) event " + name + " not implemented " +
                    util.decodeUtf8(new Uint8Array(message.data.buffer, message.offset, message.length)));
      return;
  }

  var data = new TextEncoder().encode(encoder.getData());
  this.sendMessageToClient({
    data: data,
    length: data.length,
    offset: 0,
  });
}

var NokiaPhoneStatusLocalMsgConnection = function() {
    LocalMsgConnection.call(this);
};

NokiaPhoneStatusLocalMsgConnection.prototype = Object.create(LocalMsgConnection.prototype);

NokiaPhoneStatusLocalMsgConnection.prototype.sendMessageToServer = function(message) {
  var decoder = new DataDecoder(message.data, message.offset, message.length);

  decoder.getStart(DataType.STRUCT);
  var name = decoder.getValue(DataType.METHOD);

  var encoder = new DataEncoder();

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
    case "Query":
      encoder.putStart(DataType.STRUCT, "event");
      encoder.put(DataType.METHOD, "name", "Query");
      encoder.put(DataType.STRING, "status", "OK");
      encoder.putStart(DataType.LIST, "subscriptions");

      // subscriptions
      decoder.getStart(DataType.LIST);
      while (decoder.getTag() == DataType.STRING) {
        switch (decoder.getName()) {
          case "network_status":
            encoder.putStart(DataType.STRUCT, "network_status");
            encoder.put(DataType.STRING, "", "");  // unknow name
            encoder.put(DataType.BOOLEAN, "", 1);  // unknow name
            encoder.putEnd(DataType.STRUCT, "network_status");
            break;
          case "wifi_status":
            encoder.putStart(DataType.STRUCT, "wifi_status");
            encoder.put(DataType.BOOLEAN, "", 1);  // unknow name, but it should indicate if the wifi is connected, and let's assume it's always connected.
            encoder.putEnd(DataType.STRUCT, "wifi_status");
            break;
          case "battery":
            encoder.putStart(DataType.STRUCT, "battery");
            encoder.put(DataType.BYTE, "", 1);  // unknow name
            encoder.put(DataType.BOOLEAN, "", 1);  // unknow name
            encoder.putEnd(DataType.STRUCT, "battery");
            break;
          default:
            console.error("(nokia.phone-status) Query " + decoder.getName() + " not implemented " +
                  util.decodeUtf8(new Uint8Array(message.data.buffer, message.offset, message.length)));
            break;
        }
        decoder.getValue(DataType.STRING);
      }

      encoder.putEnd(DataType.LIST, "subscriptions");
      encoder.putEnd(DataType.STRUCT, "event");
      break;
    default:
      console.error("(nokia.phone-status) event " + name + " not implemented " +
                    util.decodeUtf8(new Uint8Array(message.data.buffer, message.offset, message.length)));
      return;
  }

  var data = new TextEncoder().encode(encoder.getData());
  this.sendMessageToClient({
      data: data,
      length: data.length,
      offset: 0,
  });
};

var NokiaContactsLocalMsgConnection = function() {
    LocalMsgConnection.call(this);
}

NokiaContactsLocalMsgConnection.prototype = Object.create(LocalMsgConnection.prototype);

NokiaContactsLocalMsgConnection.prototype.sendContact = function(trans_id, contact) {
    var encoder = new DataEncoder();
    encoder.putStart(DataType.STRUCT, "event");
    encoder.put(DataType.METHOD, "name", "Notify");
    encoder.put(DataType.ULONG, "trans_id", trans_id); // The meaning of this field is unknown
    encoder.put(DataType.BYTE, "type", 1); // The name of this field is unknown (the value may be 1, 2, 3 according to the event (I'd guess CREATE, DELETE, UPDATE))
    encoder.putStart(DataType.LIST, "Contact");

    encoder.put(DataType.WSTRING, "ContactID", contact.id.toString());

    encoder.put(DataType.WSTRING, "DisplayName", contact.name[0]);

    encoder.putStart(DataType.ARRAY, "Numbers");
    contact.tel.forEach(function(tel) {
        encoder.putStart(DataType.LIST, "NumbersList"); // The name of this field is unknown
        // encoder.put(DataType.ULONG, "Kind", ???); // The meaning of this field is unknown
        encoder.put(DataType.WSTRING, "Number", tel.value);
        encoder.putEnd(DataType.LIST, "NumbersList");
    });
    encoder.putEnd(DataType.ARRAY, "Numbers");

    encoder.putEnd(DataType.LIST, "Contact");
    encoder.putEnd(DataType.STRUCT, "event");

    var data = new TextEncoder().encode(encoder.getData());
    this.sendMessageToClient({
        data: data,
        length: data.length,
        offset: 0,
    });
};

NokiaContactsLocalMsgConnection.prototype.sendMessageToServer = function(message) {
  var decoder = new DataDecoder(message.data, message.offset, message.length);

  decoder.getStart(DataType.STRUCT);
  var name = decoder.getValue(DataType.METHOD);

  switch (name) {
    case "Common":
      var encoder = new DataEncoder();

      encoder.putStart(DataType.STRUCT, "event");
      encoder.put(DataType.METHOD, "name", "Common");
      encoder.putStart(DataType.STRUCT, "message");
      encoder.put(DataType.METHOD, "name", "ProtocolVersion");
      encoder.put(DataType.STRING, "version", "2.[0-10]");
      encoder.putEnd(DataType.STRUCT, "message");
      encoder.putEnd(DataType.STRUCT, "event");

      var data = new TextEncoder().encode(encoder.getData());
      this.sendMessageToClient({
          data: data,
          length: data.length,
          offset: 0,
      });
    break;

    case "NotifySubscribe":
      contacts.forEach(this.sendContact.bind(this, decoder.getValue(DataType.ULONG)));
    break;

    default:
      console.error("(nokia.contacts) event " + name + " not implemented " +
                    util.decodeUtf8(new Uint8Array(message.data.buffer, message.offset, message.length)));
      return;
  }
}

var NokiaFileUILocalMsgConnection = function() {
    LocalMsgConnection.call(this);
};

NokiaFileUILocalMsgConnection.prototype = Object.create(LocalMsgConnection.prototype);

NokiaFileUILocalMsgConnection.prototype.sendMessageToServer = function(message) {
  var decoder = new DataDecoder(message.data, message.offset, message.length);

  decoder.getStart(DataType.STRUCT);
  var name = decoder.getValue(DataType.METHOD);

  switch (name) {
    case "Common":
      var encoder = new DataEncoder();

      encoder.putStart(DataType.STRUCT, "event");
      encoder.put(DataType.METHOD, "name", "Common");
      encoder.putStart(DataType.STRUCT, "message");
      encoder.put(DataType.METHOD, "name", "ProtocolVersion");
      encoder.put(DataType.STRING, "version", "1.0");
      encoder.putEnd(DataType.STRUCT, "message");
      encoder.putEnd(DataType.STRUCT, "event");

      var data = new TextEncoder().encode(encoder.getData());
      this.sendMessageToClient({
          data: data,
          length: data.length,
          offset: 0,
      });
      break;

    case "FileSelect":
      var trans_id = decoder.getValue(DataType.USHORT);
      var storageType = decoder.getValue(DataType.STRING);
      var mediaType = decoder.getValue(DataType.STRING);
      var multipleSelection = decoder.getValue(DataType.BOOLEAN);
      var startingURL = decoder.getValue(DataType.STRING);

      var el = document.getElementById('nokia-fileui-prompt').cloneNode(true);
      el.style.display = 'block';
      el.classList.add('visible');

      var btnDone = el.querySelector('button.recommend');
      btnDone.disabled = true;

      var selectedFile = null;

      el.querySelector('input').addEventListener('change', function() {
        btnDone.disabled = false;
        selectedFile = this.files[0];
      });

      el.querySelector('button.cancel').addEventListener('click', function() {
        el.parentElement.removeChild(el);
      });

      btnDone.addEventListener('click', (function() {
        el.parentElement.removeChild(el);

        if (!selectedFile) {
          return;
        }

        var createFile = (function(fileName) {
          fs.mkdir("/nokiafileui", (function() {
            fs.create("/nokiafileui/" + fileName, selectedFile, (function() {
              var encoder = new DataEncoder();

              encoder.putStart(DataType.STRUCT, "event");
              encoder.put(DataType.METHOD, "name", "FileSelect");
              encoder.put(DataType.USHORT, "trans_id", trans_id);
              encoder.put(DataType.STRING, "result", "OK"); // Name unknown
              encoder.putStart(DataType.ARRAY, "unknown_array"); // Name unknown
              encoder.putStart(DataType.STRUCT, "unknown_struct"); // Name unknown
              encoder.put(DataType.STRING, "unknown_string_1", ""); // Name and value unknown
              encoder.put(DataType.WSTRING, "unknown_string_2", ""); // Name and value unknown
              encoder.put(DataType.WSTRING, "unknown_string_3", "nokiafileui/" + fileName); // Name unknown
              encoder.put(DataType.BOOLEAN, "unknown_boolean", 1); // Name and value unknown
              encoder.put(DataType.ULONG, "unknown_long", 0); // Name and value unknown
              encoder.putEnd(DataType.STRUCT, "unknown_struct"); // Name unknown
              encoder.putEnd(DataType.ARRAY, "unknown_array"); // Name unknown
              encoder.putEnd(DataType.STRUCT, "event");

              var data = new TextEncoder().encode(encoder.getData());
              this.sendMessageToClient({
                data: data,
                length: data.length,
                offset: 0,
              });
            }).bind(this));
          }).bind(this));
        }).bind(this);

        var name = selectedFile.name;
        var ext = "";
        var extIndex = name.lastIndexOf(".");
        if (extIndex !== -1) {
          ext = name.substring(extIndex);
          name = name.substring(0, extIndex);
        }

        var i = 0;
        var tryFile = (function(fileName) {
          fs.exists("/nokiafileui/" + fileName, function(exists) {
            if (exists) {
              i++;
              tryFile(name + "-" + i + ext);
            } else {
              createFile(fileName);
            }
          });
        }).bind(this);

        tryFile(selectedFile.name);
      }).bind(this));

      document.body.appendChild(el);
    break;

    default:
      console.error("(nokia.phone-status) event " + name + " not implemented " +
                    util.decodeUtf8(new Uint8Array(message.data.buffer, message.offset, message.length)));
      return;
  }
};

MIDP.LocalMsgConnections = {};

// Add some fake servers because some MIDlets assume they exist.
// MIDlets are usually happy even if the servers don't reply, but we should
// remember to implement them in case they will be needed.
MIDP.FakeLocalMsgServers = [ "nokia.active-standby", "nokia.profile",
                             "nokia.connectivity-settings", "nokia.image-processing" ];

MIDP.FakeLocalMsgServers.forEach(function(server) {
    MIDP.LocalMsgConnections[server] = new LocalMsgConnection();
});

MIDP.LocalMsgConnections["nokia.contacts"] = new NokiaContactsLocalMsgConnection();
MIDP.LocalMsgConnections["nokia.messaging"] = new NokiaMessagingLocalMsgConnection();
MIDP.LocalMsgConnections["nokia.phone-status"] = new NokiaPhoneStatusLocalMsgConnection();
MIDP.LocalMsgConnections["nokia.file-ui"] = new NokiaFileUILocalMsgConnection();

Native.create("org/mozilla/io/LocalMsgConnection.init.(Ljava/lang/String;)V", function(jName) {
    var name = util.fromJavaString(jName);

    this.server = (name[2] == ":");
    this.protocolName = name.slice((name[2] == ':') ? 3 : 2);

    return new Promise((function(resolve, reject) {
        if (this.server) {
            MIDP.LocalMsgConnections[this.protocolName] = new LocalMsgConnection();
            MIDP.ConnectionRegistry.pushNotify("localmsg:" + this.protocolName);
        } else {
            // Actually, there should always be a server, but we need this check
            // for apps that use the Nokia built-in servers (because we haven't
            // implemented them yet).
            if (!MIDP.LocalMsgConnections[this.protocolName]) {
                console.warn("localmsg server (" + this.protocolName + ") unimplemented");
                // Return without resolving the promise, we want the thread that is connecting
                // to this unimplemented server to stop indefinitely.
                return;
            }

            if (MIDP.FakeLocalMsgServers.indexOf(this.protocolName) != -1) {
                console.warn("connect to an unimplemented localmsg server (" + this.protocolName + ")");
            }

            MIDP.LocalMsgConnections[this.protocolName].notifyConnection();
        }

        resolve();
    }).bind(this));
});

Native.create("org/mozilla/io/LocalMsgConnection.waitConnection.()V", function() {
    return MIDP.LocalMsgConnections[this.protocolName].waitConnection();
});

Native.create("org/mozilla/io/LocalMsgConnection.sendData.([BII)V", function(data, offset, length) {
    var message = {
      data: data,
      offset: offset,
      length: length,
    };

    if (this.server) {
        MIDP.LocalMsgConnections[this.protocolName].sendMessageToClient(message);
    } else {
        if (MIDP.FakeLocalMsgServers.indexOf(this.protocolName) != -1) {
            console.warn("sendData (" + util.decodeUtf8(new Uint8Array(data.buffer, offset, length)) + ") to an unimplemented localmsg server (" + this.protocolName + ")");
        }

        MIDP.LocalMsgConnections[this.protocolName].sendMessageToServer(message);
    }
});

Native.create("org/mozilla/io/LocalMsgConnection.receiveData.([B)I", function(data) {
    if (this.server) {
        return MIDP.LocalMsgConnections[this.protocolName].serverReceiveMessage(data);
    }

    if (MIDP.FakeLocalMsgServers.indexOf(this.protocolName) != -1) {
        console.warn("receiveData from an unimplemented localmsg server (" + this.protocolName + ")");
    }

    return MIDP.LocalMsgConnections[this.protocolName].clientReceiveMessage(data);
});

Native.create("org/mozilla/io/LocalMsgConnection.closeConnection.()V", function() {
    if (this.server) {
        delete MIDP.LocalMsgConnections[this.protocolName];
    }
});
