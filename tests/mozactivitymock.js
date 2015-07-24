/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

var lastSMSNumber = null;
var lastSMSBody = null;
var lastAddContactParams = null;

DumbPipe.registerOpener("lastSMSNumber", function(message, sender) {
  sender(lastSMSNumber);
});

DumbPipe.registerOpener("lastSMSBody", function(message, sender) {
  sender(lastSMSBody);
});

DumbPipe.registerOpener("lastAddContactParams", function(message, sender) {
  sender(lastAddContactParams);
});

function MozActivity(obj) {
  if (obj.name === "new") {
    switch (obj.data.type) {
      case "websms/sms":
        lastSMSNumber = obj.data.number;
        lastSMSBody = obj.data.body;
      break;

      case "webcontacts/contact":
        lastAddContactParams = obj.data.params;

      break;

      default:
        throw new Error("MozActivity with type " + obj.data.type + " not supported");
    }

    nextTickBeforeEvents((function() {
      this.onsuccess();
    }).bind(this));

  } else {
    throw new Error("MozActivity " + obj.name + " not supported");
  }
}

var messageHandlers = {};
navigator.mozSetMessageHandler = function(name, func) {
  messageHandlers[name] = func;
}

DumbPipe.registerOpener("callShareActivityMessageHandler", function(message, sender) {
  var activity = {
    source: {
      name: "share",
      data: {
        "type": "image/*",
        "number": 1,
        "blobs": [ new Blob([]) ],
        "filenames": ["j2mesharetestimage" + message.num + ".jpg"],
        "filepaths": ["/sdcard/DCIM/100MZLLA/j2mesharetestimage" + message.num + ".jpg"]
      },
    },
  };
  messageHandlers["activity"](activity);
});
