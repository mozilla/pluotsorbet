/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var LocalMsgConnection = function() {
    this.waitingForConnection = null;
    this.serverWaiting = [];
    this.clientWaiting = [];
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

    if (this.clientWaiting.length > 0) {
        this.clientWaiting.shift()();
    }
}

LocalMsgConnection.prototype.clientReceiveMessage = function(data) {
    return new Promise((function(resolve, reject) {
        if (this.clientMessages.length == 0) {
            this.clientWaiting.push(function() {
                resolve(this.copyMessage(this.clientMessages, data));
            }.bind(this));

            return;
        }

        resolve(this.copyMessage(this.clientMessages, data));
    }).bind(this));
}

LocalMsgConnection.prototype.sendMessageToServer = function(message) {
    this.serverMessages.push(message);

    if (this.serverWaiting.length > 0) {
        this.serverWaiting.shift()();
    }
}

LocalMsgConnection.prototype.serverReceiveMessage = function(data) {
    return new Promise((function(resolve, reject) {
        if (this.serverMessages.length == 0) {
            this.serverWaiting.push(function() {
                resolve(this.copyMessage(this.serverMessages, data));
            }.bind(this));
            return;
        }

        resolve(this.copyMessage(this.serverMessages, data));
    }).bind(this));
}

var NokiaMessagingLocalMsgConnection = function() {
    LocalMsgConnection.call(this);
    window.addEventListener("nokia.messaging", function(e) {
        this.receiveSMS(e.detail);
    }.bind(this));
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

var NokiaSASrvRegLocalMsgConnection = function() {
    LocalMsgConnection.call(this);
};

NokiaSASrvRegLocalMsgConnection.prototype = Object.create(LocalMsgConnection.prototype);

NokiaSASrvRegLocalMsgConnection.prototype.sendMessageToServer = function(message) {
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
      encoder.put(DataType.STRING, "version", "2.0");
      encoder.putEnd(DataType.STRUCT, "message");
      encoder.putEnd(DataType.STRUCT, "event");
      break;
    case "Discovery":
      encoder.putStart(DataType.STRUCT, "event");
      encoder.put(DataType.METHOD, "name", "Discovery");
      encoder.put(DataType.BYTE, "unknown_byte_1", 1);
      encoder.put(DataType.STRING, "unknown_string_1", "");
      encoder.putStart(DataType.ARRAY, "services");
      encoder.putStart(DataType.STRUCT, "service");
      encoder.put(DataType.STRING, "ServiceName", "file_ui");
      encoder.put(DataType.URI, "ServiceURI", "nokia.file-ui");
      encoder.put(DataType.STRING, "unknown_string_2", "");
      encoder.put(DataType.WSTRING, "unknown_string_3", "");
      encoder.put(DataType.STRING, "unknown_string_4", "");
      encoder.putEnd(DataType.STRUCT, "service");
      encoder.putEnd(DataType.ARRAY, "services");
      encoder.putEnd(DataType.STRUCT, "event");
      break;
  }

  var data = new TextEncoder().encode(encoder.getData());
  this.sendMessageToClient({
      data: data,
      length: data.length,
      offset: 0,
  });
};

var NokiaPhoneStatusLocalMsgConnection = function() {
  LocalMsgConnection.call(this);

  this.listeners = {
    "battery": false,
    "network_status": false,
    "wifi_status": false,
  };

  window.addEventListener('online', (function() {
    if (this.listeners["network_status"]) {
      this.sendChangeNotify(this.buildNetworkStatus.bind(this), true);
    }

    if (this.listeners["wifi_status"]) {
      this.sendChangeNotify(this.buildWiFiStatus.bind(this), true);
    }
  }).bind(this));;

  window.addEventListener('offline', (function() {
    if (this.listeners["network_status"]) {
      this.sendChangeNotify(this.buildNetworkStatus.bind(this), false);
    }

    if (this.listeners["wifi_status"]) {
      this.sendChangeNotify(this.buildWiFiStatus.bind(this), false);
    }
  }).bind(this));
};

NokiaPhoneStatusLocalMsgConnection.prototype = Object.create(LocalMsgConnection.prototype);

NokiaPhoneStatusLocalMsgConnection.prototype.buildNetworkStatus = function(encoder, online) {
  encoder.putStart(DataType.STRUCT, "network_status");
  encoder.put(DataType.STRING, "", "Home");  // Name unknown (value is "None", "Home" or "Roam")
  encoder.put(DataType.BOOLEAN, "", online ? 1 : 0);  // Name unknown
  encoder.putEnd(DataType.STRUCT, "network_status");
}

NokiaPhoneStatusLocalMsgConnection.prototype.buildWiFiStatus = function(encoder, online) {
  encoder.putStart(DataType.STRUCT, "wifi_status");
  encoder.put(DataType.BOOLEAN, "", online ? 1 : 0);  // Name unknown, we're assuming we're connected to a wifi network.
  encoder.putEnd(DataType.STRUCT, "wifi_status");
}

NokiaPhoneStatusLocalMsgConnection.prototype.buildBattery = function(encoder) {
  encoder.putStart(DataType.STRUCT, "battery");
  encoder.put(DataType.BYTE, "", 1);  // Name unknown
  encoder.put(DataType.BOOLEAN, "", 1);  // Name unknown
  encoder.putEnd(DataType.STRUCT, "battery");
}

NokiaPhoneStatusLocalMsgConnection.prototype.sendChangeNotify = function(replyBuilder, online) {
  var encoder = new DataEncoder();
  encoder.putStart(DataType.STRUCT, "event");
  encoder.put(DataType.METHOD, "name", "ChangeNotify");
  encoder.put(DataType.STRING, "status", "OK"); // Name and value unknown
  encoder.putStart(DataType.LIST, "subscriptions");

  replyBuilder(encoder, online);

  encoder.putEnd(DataType.LIST, "subscriptions");
  encoder.putEnd(DataType.STRUCT, "event");

  var data = new TextEncoder().encode(encoder.getData());
  this.sendMessageToClient({
    data: data,
    length: data.length,
    offset: 0,
  });
}

NokiaPhoneStatusLocalMsgConnection.prototype.addListener = function(type) {
  if (type === "battery") {
    console.warn("Battery notifications not supported");
    return;
  }

  this.listeners[type] = true;
}

NokiaPhoneStatusLocalMsgConnection.prototype.removeListener = function(type) {
  this.listeners[type] = false;
}

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
      encoder.put(DataType.STRING, "version", "1.[0-10]");
      encoder.putEnd(DataType.STRUCT, "message");
      encoder.putEnd(DataType.STRUCT, "event");

      var data = new TextEncoder().encode(encoder.getData());
      this.sendMessageToClient({
        data: data,
        length: data.length,
        offset: 0,
      });
      break;

    case "Query":
      // This will be true if there is at least one "CurrentStateOnly" request.
      var headerBuilt = false;

      // subscriptions
      decoder.getStart(DataType.LIST);
      while (decoder.getTag() == DataType.STRING) {
        var name = decoder.getName();
        var queryKind = decoder.getValue(DataType.STRING);

        if (queryKind === "CurrentStateOnly") {
          if (!headerBuilt) {
            encoder.putStart(DataType.STRUCT, "event");
            encoder.put(DataType.METHOD, "name", "Query");
            encoder.put(DataType.STRING, "status", "OK");
            encoder.putStart(DataType.LIST, "subscriptions");
            headerBuilt = true;
          }

          switch (name) {
            case "network_status":
              this.buildNetworkStatus(encoder, navigator.onLine);
              break;

            case "wifi_status":
              this.buildWiFiStatus(encoder, navigator.onLine);
              break;

            case "battery":
              this.buildBattery(encoder);
              break;

            default:
              console.error("(nokia.phone-status) Query " + decoder.getName() + " not implemented " +
                            util.decodeUtf8(new Uint8Array(message.data.buffer, message.offset, message.length)));
              break;
          }
        } else if (queryKind === "Disable") {
          this.removeListener(name);
        } else if (queryKind === "Enable") {
          this.addListener(name);
        }
      }

      if (headerBuilt) {
        encoder.putEnd(DataType.LIST, "subscriptions");
        encoder.putEnd(DataType.STRUCT, "event");

        var data = new TextEncoder().encode(encoder.getData());
        this.sendMessageToClient({
          data: data,
          length: data.length,
          offset: 0,
        });
      }

      break;

    default:
      console.error("(nokia.phone-status) event " + name + " not implemented " +
                    util.decodeUtf8(new Uint8Array(message.data.buffer, message.offset, message.length)));
      return;
  }
};

var NokiaContactsLocalMsgConnection = function() {
    LocalMsgConnection.call(this);
}

NokiaContactsLocalMsgConnection.prototype = Object.create(LocalMsgConnection.prototype);

NokiaContactsLocalMsgConnection.prototype.encodeContact = function(encoder, contact) {
    encoder.putStart(DataType.LIST, "Contact");

    encoder.put(DataType.WSTRING, "ContactID", contact.id.toString().substr(0,30));

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
}

NokiaContactsLocalMsgConnection.prototype.sendContact = function(trans_id, contact) {
    if (!contact.tel) {
        return;
    }

    var encoder = new DataEncoder();
    encoder.putStart(DataType.STRUCT, "event");
    encoder.put(DataType.METHOD, "name", "Notify");
    encoder.put(DataType.ULONG, "trans_id", trans_id); // The meaning of this field is unknown
    encoder.put(DataType.BYTE, "type", 1); // The name of this field is unknown (the value may be 1, 2, 3 according to the event (I'd guess CREATE, DELETE, UPDATE))
    this.encodeContact(encoder, contact);
    encoder.putEnd(DataType.STRUCT, "event");

    var data = new TextEncoder().encode(encoder.getData());
    this.sendMessageToClient({
        data: data,
        length: data.length,
        offset: 0,
    });
};

NokiaContactsLocalMsgConnection.prototype.getFirstOrNext = function(trans_id, method) {
  var gotContact = (function(contact) {
    if (contact && !contact.tel) {
      contacts.getNext(gotContact);
      return;
    }

    var encoder = new DataEncoder();
    encoder.putStart(DataType.STRUCT, "event");
    encoder.put(DataType.METHOD, "name", method);
    encoder.put(DataType.ULONG, "trans_id", trans_id);
    if (contact) {
      encoder.put(DataType.STRING, "result", "OK"); // Name unknown
      encoder.putStart(DataType.ARRAY, "contacts"); // Name unknown
      this.encodeContact(encoder, contact);
      encoder.putEnd(DataType.ARRAY, "contacts"); // Name unknown
    } else {
      encoder.put(DataType.STRING, "result", "Entry not found"); // Name unknown
    }
    encoder.putEnd(DataType.STRUCT, "event");

    var data = new TextEncoder().encode(encoder.getData());
    this.sendMessageToClient({
      data: data,
      length: data.length,
      offset: 0,
    });
  }).bind(this);

  contacts.getNext(gotContact);
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

    case "getFirst":
      var trans_id = decoder.getValue(DataType.ULONG);
      decoder.getEnd(DataType.ARRAY); // Ignore the contents of the "sources" array
      var numEntries = decoder.getValue(DataType.ULONG);
      if (numEntries !== 1) {
        console.error("(nokia.contacts) event getFirst with numEntries != 1 not implemented " +
                      util.decodeUtf8(new Uint8Array(message.data.buffer, message.offset, message.length)));
      }

      this.getFirstOrNext(trans_id, "getFirst");
    break;

    case "getNext":
      var trans_id = decoder.getValue(DataType.ULONG);
      decoder.getEnd(DataType.ARRAY); // Ignore the contents of the "sources" array
      decoder.getEnd(DataType.LIST); // Ignore the contents of the "filter" list
      decoder.getStart(DataType.LIST);
      var contactID = decoder.getValue(DataType.WSTRING);
      decoder.getEnd(DataType.LIST);
      var includeStartEntry = decoder.getValue(DataType.BOOLEAN);
      if (includeStartEntry == 1) {
        console.error("(nokia.contacts) event getNext with includeStartEntry == true not implemented " +
                      util.decodeUtf8(new Uint8Array(message.data.buffer, message.offset, message.length)));
      }
      var numEntries = decoder.getValue(DataType.ULONG);
      if (numEntries !== 1) {
        console.error("(nokia.contacts) event getNext with numEntries != 1 not implemented " +
                      util.decodeUtf8(new Uint8Array(message.data.buffer, message.offset, message.length)));
      }

      this.getFirstOrNext(trans_id, "getNext");
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

      var accept = '';

      switch (mediaType) {
        case "Picture":
          accept = "image/*";
        break;

        case "Video":
          accept = "video/*";
        break;

        case "Music":
        case "Sound":
          accept = "audio/*";
        break;

        default:
          throw new Error("Media type '" + mediaType + "' not supported");
      }

      var el = document.getElementById('nokia-fileui-prompt').cloneNode(true);
      el.style.display = 'block';
      el.classList.add('visible');

      var fileInput = el.querySelector('input');
      fileInput.accept = accept;

      var btnDone = el.querySelector('button.recommend');
      btnDone.disabled = true;

      var selectedFile = null;

      fileInput.addEventListener('change', function() {
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

        var ext = "";
        var extIndex = selectedFile.name.lastIndexOf(".");
        if (extIndex != -1) {
          ext = selectedFile.name.substr(extIndex);
        }

        var fileName = fs.createUniqueFile("/nokiafileui", "file" + ext, selectedFile);
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

      document.body.appendChild(el);
    break;

    default:
      console.error("(nokia.file-ui) event " + name + " not implemented " +
                    util.decodeUtf8(new Uint8Array(message.data.buffer, message.offset, message.length)));
      return;
  }
};

var NokiaImageProcessingLocalMsgConnection = function() {
    LocalMsgConnection.call(this);
};

NokiaImageProcessingLocalMsgConnection.prototype = Object.create(LocalMsgConnection.prototype);

NokiaImageProcessingLocalMsgConnection.prototype.sendMessageToServer = function(message) {
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

    case "Scale":
      var trans_id = decoder.getValue(DataType.BYTE);
      var fileName = decoder.getValue(DataType.WSTRING);
      var max_vres = 0;
      var max_hres = 0;
      var max_kb = 0;
      decoder.getStart(DataType.LIST);
      while (true) {
        var paramName = decoder.getName();
        var value = decoder.getValue(DataType.USHORT);
        if (paramName === "limits")
          break;

        switch (paramName) {
          case "max_kb":
            max_kb = value;
            break;
          case "max_vres":
            max_vres = value;
            break;
          case "max_hres":
            max_hres = value;
            break;
          default:
            console.error("(nokia.image-processing) event " + name + " with " +
              paramName + " = " + value + " not implemented.");
            return;
        }
      }
      decoder.getEnd(DataType.LIST);
      var aspect = decoder.getValue(DataType.STRING);
      var quality = decoder.getValue(DataType.BYTE) || 80;

      if (aspect != "FullImage" && aspect != "LockToPartialView") {
        console.error("(nokia.image-processing) event " + name + " with aspect != 'FullImage' or 'LockToPartialView' not implemented " +
                      util.decodeUtf8(new Uint8Array(message.data.buffer, message.offset, message.length)));
        return;
      }

      fs.open("/" + fileName, (function(fd) {
        var img = null;

        function _cleanupImg() {
          if (img) {
            URL.revokeObjectURL(img.src);
            img.src = '';
            img = null;
          }
        }

        var _sendBackScaledImage = function(blob) {
          _cleanupImg();

          var ext = "";
          var extIndex = fileName.lastIndexOf(".");
          if (extIndex != -1) {
            ext = fileName.substr(extIndex);
          }

          var uniqueFileName = fs.createUniqueFile("/nokiaimageprocessing", "image" + ext, blob);
          var encoder = new DataEncoder();

          encoder.putStart(DataType.STRUCT, "event");
          encoder.put(DataType.METHOD, "name", "Scale");
          encoder.put(DataType.BYTE, "trans_id", trans_id);
          encoder.put(DataType.STRING, "result", "Complete"); // Name unknown
          encoder.put(DataType.WSTRING, "filename", "nokiaimageprocessing/" + uniqueFileName); // Name unknown
          encoder.putEnd(DataType.STRUCT, "event");

          var data = new TextEncoder().encode(encoder.getData());
          this.sendMessageToClient({
            data: data,
            length: data.length,
            offset: 0,
          });
        }.bind(this);

        var imgData = fs.read(fd);
        var fileSize = fs.getsize(fd);
        fs.close(fd);

        img = new Image();
        img.src = URL.createObjectURL(new Blob([ imgData ]));

        img.onload = (function() {
          // If the image size is less than the given max_kb, and height/width
          // are less than max_hres/max_wres, send the original image immediately
          // without any scaling.
          if (max_kb > 0 && (max_kb * 1024) >= fileSize &&
              (max_hres <= 0 || img.naturalHeight <= max_vres) &&
              (max_vres <= 0 || img.naturalWidth <= max_hres)) {
            _sendBackScaledImage(new Blob([ imgData ]));
            return;
          }

          function _imageToBlob(aCanvas, aImage, aHeight, aWidth, aQuality) {
            aCanvas.width = aWidth;
            aCanvas.height = aHeight;
            var ctx = aCanvas.getContext("2d");
            ctx.drawImage(aImage, 0, 0, aWidth, aHeight);

            return new Promise(function(resolve, reject) {
              aCanvas.toBlob(resolve, "image/jpeg", aQuality / 100);
            });
          }

          var canvas = document.createElement("canvas");
          if (max_kb <= 0) {
            _imageToBlob(canvas, img, Math.min(img.naturalHeight, max_vres),
                         Math.min(img.naturalWidth, max_hres), quality).then(_sendBackScaledImage);
            return;
          }

          _imageToBlob(canvas, img, img.naturalHeight,
                       img.naturalWidth, quality).then(function(blob) {
            var imgSizeInKb = blob.size / 1024;

            // Roughly recalc max_vres and max_hres based on the max_kb and the real resolution.
            var ratio = Math.sqrt(max_kb / imgSizeInKb);
            max_hres = Math.min(img.naturalWidth * ratio,
              max_hres <= 0 ? img.naturalWidth : max_hres);
            max_vres = Math.min(img.naturalHeight * ratio,
              max_vres <=0 ? img.naturalHeight : max_vres);

            return _imageToBlob(canvas, img, Math.min(img.naturalHeight, max_vres),
                                Math.min(img.naturalWidth, max_hres), quality);
          }).then(_sendBackScaledImage);
        }).bind(this);

        img.onerror = function(e) {
          console.error("Error in decoding image");
          _cleanupImg();
        };
      }).bind(this));
    break;

    default:
      console.error("(nokia.image-processing) event " + name + " not implemented " +
                    util.decodeUtf8(new Uint8Array(message.data.buffer, message.offset, message.length)));
      return;
  }
};

var NokiaProductInfoLocalMsgConnection = function() {
    LocalMsgConnection.call(this);
};

NokiaProductInfoLocalMsgConnection.prototype = Object.create(LocalMsgConnection.prototype);

NokiaProductInfoLocalMsgConnection.prototype.sendMessageToServer = function(message) {
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
      encoder.put(DataType.STRING, "version", "1.1");
      encoder.putEnd(DataType.STRUCT, "message");
      encoder.putEnd(DataType.STRUCT, "event");

      var data = new TextEncoder().encode(encoder.getData());
      this.sendMessageToClient({
        data: data,
        length: data.length,
        offset: 0,
      });
      break;
    case "ReadProductInfo":
      encoder.putStart(DataType.STRUCT, "event");
      encoder.put(DataType.METHOD, "name", "ReadProductInfo");
      encoder.put(DataType.STRING, "result", "OK");
      encoder.put(DataType.STRING, "unkown_str_1", "");
      encoder.put(DataType.STRING, "unkown_str_2", "");
      encoder.put(DataType.STRING, "unkown_str_3", "");
      encoder.put(DataType.STRING, "unkown_str_4", ""); // Probably RMCODE
      encoder.put(DataType.STRING, "unkown_str_5", "");
      encoder.putEnd(DataType.STRUCT, "event");

      var data = new TextEncoder().encode(encoder.getData());
      this.sendMessageToClient({
        data: data,
        length: data.length,
        offset: 0,
      });
      break;
    default:
      console.error("(nokia.status-info) event " + name + " not implemented " +
                    util.decodeUtf8(new Uint8Array(message.data.buffer, message.offset, message.length)));
      return;
  }
};

var NokiaActiveStandbyLocalMsgConnection = function() {
    LocalMsgConnection.call(this);
}

NokiaActiveStandbyLocalMsgConnection.indicatorActive = false;
NokiaActiveStandbyLocalMsgConnection.pipeSender = null;

NokiaActiveStandbyLocalMsgConnection.prototype = Object.create(LocalMsgConnection.prototype);

NokiaActiveStandbyLocalMsgConnection.prototype.recipient = function(message) {
  switch (message.type) {
    case "close":
      DumbPipe.close(NokiaActiveStandbyLocalMsgConnection.pipeSender);
      NokiaActiveStandbyLocalMsgConnection.pipeSender = null;
    break;
  }
}

NokiaActiveStandbyLocalMsgConnection.prototype.sendMessageToServer = function(message) {
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
      encoder.put(DataType.STRING, "version", "1.[0-10]");
      encoder.putEnd(DataType.STRUCT, "message");
      encoder.putEnd(DataType.STRUCT, "event");

      var data = new TextEncoder().encode(encoder.getData());
      this.sendMessageToClient({
        data: data,
        length: data.length,
        offset: 0,
      });
    break;

    case "Register":
      var client_id = decoder.getValue(DataType.STRING);
      var personalise_view_text = decoder.getValue(DataType.WSTRING);
      decoder.getValue(DataType.BOOLEAN);

      encoder.putStart(DataType.STRUCT, "event");
      encoder.put(DataType.METHOD, "name", "Register");
      encoder.put(DataType.WSTRING, "client_id", client_id);
      encoder.put(DataType.STRING, "result", "OK"); // Name unknown
      encoder.putEnd(DataType.STRUCT, "event");

      var data = new TextEncoder().encode(encoder.getData());
      this.sendMessageToClient({
        data: data,
        length: data.length,
        offset: 0,
      });

      setZeroTimeout((function() {
        var encoder = new DataEncoder();

        encoder.putStart(DataType.STRUCT, "event");
        encoder.put(DataType.METHOD, "name", "Activated");
        encoder.put(DataType.WSTRING, "client_id", client_id);
        encoder.putStart(DataType.LIST, "unknown_list");
        // Unknown DataType.STRING elements
        encoder.putEnd(DataType.LIST, "unknown_list");
        encoder.put(DataType.BYTE, "unkown_byte", 1); // Name unknown
        encoder.put(DataType.SHORT, "unknown_short_1", 0); // Name and value unknown
        encoder.put(DataType.SHORT, "unknown_short_2", 0); // Name and value unknown
        encoder.putEnd(DataType.STRUCT, "event");

        var data = new TextEncoder().encode(encoder.getData());
        this.sendMessageToClient({
          data: data,
          length: data.length,
          offset: 0,
        });
      }).bind(this));
    break;

    case "Update":
      var client_id = decoder.getValue(DataType.STRING);
      var personalise_view_text = decoder.getValue(DataType.WSTRING);
      var activate_scroll_events = decoder.getValue(DataType.BOOLEAN);
      var content_icon = decoder.getNextValue();
      var mime_type = decoder.getValue(DataType.STRING);
      var context_text = decoder.getValue(DataType.WSTRING);

      if (NokiaActiveStandbyLocalMsgConnection.indicatorActive) {
        NokiaActiveStandbyLocalMsgConnection.pipeSender = DumbPipe.open("notification", {
          title: personalise_view_text,
          options: {
            body: context_text,
          },
          icon: content_icon,
          mime_type: mime_type,
        }, this.recipient.bind(this));
      }

      encoder.putStart(DataType.STRUCT, "event");
      encoder.put(DataType.METHOD, "name", "Update");
      encoder.put(DataType.WSTRING, "client_id", client_id);
      encoder.put(DataType.STRING, "result", "OK"); // Name unknown
      encoder.putEnd(DataType.STRUCT, "event");

      var data = new TextEncoder().encode(encoder.getData());
      this.sendMessageToClient({
        data: data,
        length: data.length,
        offset: 0,
      });
      break;

    default:
      console.error("(nokia.active-standby) event " + name + " not implemented " +
                    util.decodeUtf8(new Uint8Array(message.data.buffer, message.offset, message.length)));
      return;
  }
}

Native["com/nokia/mid/ui/lcdui/Indicator.setActive.(Z)V"] = function(active) {
  NokiaActiveStandbyLocalMsgConnection.indicatorActive = active;

  if (!active && NokiaActiveStandbyLocalMsgConnection.pipeSender) {
    NokiaActiveStandbyLocalMsgConnection.pipeSender({ type: "close" });
  }
};

MIDP.LocalMsgConnections = {};

// Add some fake servers because some MIDlets assume they exist.
// MIDlets are usually happy even if the servers don't reply, but we should
// remember to implement them in case they will be needed.
MIDP.FakeLocalMsgServers = [ "nokia.profile", "nokia.connectivity-settings" ];

MIDP.FakeLocalMsgServers.forEach(function(server) {
    MIDP.LocalMsgConnections[server] = LocalMsgConnection;
});

MIDP.LocalMsgConnections["nokia.contacts"] = NokiaContactsLocalMsgConnection;
MIDP.LocalMsgConnections["nokia.messaging"] = NokiaMessagingLocalMsgConnection;
MIDP.LocalMsgConnections["nokia.phone-status"] = NokiaPhoneStatusLocalMsgConnection;
MIDP.LocalMsgConnections["nokia.file-ui"] = NokiaFileUILocalMsgConnection;
MIDP.LocalMsgConnections["nokia.image-processing"] = NokiaImageProcessingLocalMsgConnection;
MIDP.LocalMsgConnections["nokia.sa.service-registry"] = NokiaSASrvRegLocalMsgConnection;
MIDP.LocalMsgConnections["nokia.active-standby"] = NokiaActiveStandbyLocalMsgConnection;
MIDP.LocalMsgConnections["nokia.product-info"] = NokiaProductInfoLocalMsgConnection;

Native["org/mozilla/io/LocalMsgConnection.init.(Ljava/lang/String;)V"] = function(jName) {
    var name = util.fromJavaString(jName);

    this.server = (name[2] == ":");
    this.protocolName = name.slice((name[2] == ':') ? 3 : 2);
    asyncImpl("V", new Promise((function(resolve, reject) {
        if (this.server) {
            // It seems that one server only serves on client at a time, let's
            // store an object instead of the constructor.
            this.connection = MIDP.LocalMsgConnections[this.protocolName] = new LocalMsgConnection();
            localmsgServerCreated = true;
            if (localmsgServerWait) {
              localmsgServerWait();
            }
        } else {
            // Actually, there should always be a server, but we need this check
            // for apps that use the Nokia built-in servers (because we haven't
            // implemented them yet).
            if (!MIDP.LocalMsgConnections[this.protocolName]) {
                console.error("localmsg server (" + this.protocolName + ") unimplemented");
                // Return without resolving the promise, we want the thread that is connecting
                // to this unimplemented server to stop indefinitely.
                return;
            }

            if (MIDP.FakeLocalMsgServers.indexOf(this.protocolName) != -1) {
                console.warn("connect to an unimplemented localmsg server (" + this.protocolName + ")");
            }

            this.connection = typeof MIDP.LocalMsgConnections[this.protocolName] === 'function' ?
              new MIDP.LocalMsgConnections[this.protocolName]() : MIDP.LocalMsgConnections[this.protocolName];
            this.connection.notifyConnection();
        }

        resolve();
    }).bind(this)));
};

Native["org/mozilla/io/LocalMsgConnection.waitConnection.()V"] = function() {
    asyncImpl("V", this.connection.waitConnection());
};

Native["org/mozilla/io/LocalMsgConnection.sendData.([BII)V"] = function(data, offset, length) {
    var message = {
      data: data,
      offset: offset,
      length: length,
    };

    if (this.server) {
        this.connection.sendMessageToClient(message);
    } else {
        if (MIDP.FakeLocalMsgServers.indexOf(this.protocolName) != -1) {
            console.warn("sendData (" + util.decodeUtf8(new Uint8Array(data.buffer, offset, length)) + ") to an unimplemented localmsg server (" + this.protocolName + ")");
        }

        this.connection.sendMessageToServer(message);
    }
};

Native["org/mozilla/io/LocalMsgConnection.receiveData.([B)I"] = function(data) {
    if (this.server) {
        asyncImpl("I", this.connection.serverReceiveMessage(data));
        return;
    }

    if (MIDP.FakeLocalMsgServers.indexOf(this.protocolName) != -1) {
        console.warn("receiveData from an unimplemented localmsg server (" + this.protocolName + ")");
    }

    asyncImpl("I", this.connection.clientReceiveMessage(data));
};

Native["org/mozilla/io/LocalMsgConnection.closeConnection.()V"] = function() {
    if (this.server) {
        delete MIDP.LocalMsgConnections[this.protocolName];
    }
    delete this.connection;
};
