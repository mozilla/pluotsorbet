/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

function loadScript(path) {
  return new Promise(function(resolve, reject) {
    var element = document.createElement('script');
    element.setAttribute("type", "text/javascript");
    element.setAttribute("src", path);
    document.getElementsByTagName("head")[0].appendChild(element);
    element.onload = resolve;
  });
}

/**
 * Pre-load dependencies and then load the main page.
 */
(function() {
  var midletClassName = urlParams.midletClassName ? urlParams.midletClassName.replace(/\//g, '.') : "RunTests";
  var loadingPromises = [];
  if (midletClassName == "RunTests") {
    loadingPromises.push(loadScript("tests/contacts.js"),
                         loadScript("tests/index.js"));
  }

  Promise.all(loadingPromises).then(function() {
    document.getElementById("mozbrowser").src = "main.html" + location.search;
  });
})();

var DumbPipe = {
  // Functions that handle requests to open a pipe, indexed by type.
  openers: {},

  // Functions that receive messages from the other side for active pipes.
  recipients: {},

  // Queue of messages to send to the other side.  Retrieved by the other side
  // via a "get" message.
  outgoingMessages: [],

  // Every time we want to make the other side retrieve messages, the hash
  // of the other side's web page has to change, so we increment it.
  nextHashID: 0,

  registerOpener: function(type, opener) {
    this.openers[type] = opener;
  },

  handleEvent: function(event) {
    if (event.detail.promptType == "custom-prompt") {
      console.warn("unresponsive script warning; figure out how to handle");
      return;
    }

    /**
     * We embed messages in the mozbrowsershowmodalprompt event's detail.message
     * property.  The value of that property is a JSON string representing
     * the message envelope, whose inner "message" property contains the actual
     * message.
     *
     * @property command {String} the command to invoke: open|message|get|close
     * @property type {String} the type of pipe to open (when command == open)
     * @property pipeID {Number} unique ID (when command == open|message|close)
     * @property message {String} the JSON message to forward to this side
     */
    var envelope = JSON.parse(event.detail.message);

    switch (envelope.command) {
      case "open":
        //console.log("outer recv: " + JSON.stringify(envelope));
        this.openPipe(envelope.pipeID, envelope.type, envelope.message);
        break;
      case "message":
        //console.log("outer recv: " + JSON.stringify(envelope));
        this.receiveMessage(envelope.pipeID, envelope.message);
        break;
      case "get":
        this.getMessages(event);
        break;
      case "close":
        //console.log("outer recv: " + JSON.stringify(envelope));
        this.closePipe(envelope.pipeID);
        break;
    }
  },

  openPipe: function(pipeID, type, message) {
    var opener = this.openers[type];

    if (!opener) {
      console.error("no opener for pipe type " + type);
      return;
    }

    // Create a function that this side of the boundary can use to send
    // a message to the other side.
    var sender = this.sendMessage.bind(this, pipeID);

    this.recipients[pipeID] = opener(message, sender);
  },

  sendMessage: function(pipeID, message) {
    // Sadly, we have no way to send a message to the other side directly.
    // Instead, we change the hash part of the other side's URL, which triggers
    // a hashchange event on the other side.  A listener on the other side
    // then sends us a "get" prompt, and we set its return value to the message.
    // Oh my shod, that's some funky git!
    var envelope = { pipeID: pipeID, message: message };
    //console.log("outer send: " + JSON.stringify(envelope));
    this.outgoingMessages.push(envelope);

    var mozbrowser = document.getElementById("mozbrowser");
    window.setTimeout(function() {
      mozbrowser.src = mozbrowser.src.split("#")[0] + "#" + this.nextHashID++;
    }.bind(this), 0);
  },

  receiveMessage: function(pipeID, message, detail) {
    window.setTimeout(function() {
      if (!this.recipients[pipeID]) {
        console.warn("nonexistent pipe " + pipeID + " received message " + JSON.stringify(message));
        return;
      }

      try {
        this.recipients[pipeID](message);
      } catch(ex) {
        console.error(ex + "\n" + ex.stack);
      }
    }.bind(this), 0);
  },

  getMessages: function(event) {
    try {
      event.detail.returnValue = JSON.stringify(this.outgoingMessages);
    } catch(ex) {
      console.error("failed to stringify outgoing messages: " + ex);
    } finally {
      this.outgoingMessages = [];
      event.detail.unblock();
    }
  },

  closePipe: function(pipeID) {
    delete this.recipients[pipeID];
  }
};

document.getElementById("mozbrowser").addEventListener("mozbrowsershowmodalprompt",
                                                       DumbPipe.handleEvent.bind(DumbPipe),
                                                       true);

DumbPipe.registerOpener("mobileInfo", function(message, sender) {
  // Initialize the object with the URL params and fallback placeholders
  // for testing/debugging on a desktop.
  var mobileInfo = {
    network: {
      mcc: urlParams.network_mcc || "310", // United States
      mnc: urlParams.network_mnc || "001",
    },
    icc: {
      mcc: urlParams.icc_mcc || "310", // United States
      mnc: urlParams.icc_mnc || "001",
      msisdn: urlParams.icc_msisdn || "10005551212",
    },
  };

  var mobileConnections = window.navigator.mozMobileConnections;
  if (!mobileConnections && window.navigator.mozMobileConnection) {
    mobileConnections = [ window.navigator.mozMobileConnection ];
  }

  // If we have access to the Mobile Connection API, then we use it to get
  // the actual values.
  if (mobileConnections) {
    // Then the only part of the Mobile Connection API that is accessible
    // to privileged apps is lastKnownNetwork and lastKnownHomeNetwork, which
    // is fortunately all we need.  lastKnownNetwork is a string of format
    // "<mcc>-<mnc>", while lastKnownHomeNetwork is "<mcc>-<mnc>[-<spn>]".
    // Use only the info about the first SIM for the time being.
    var lastKnownNetwork = mobileConnections[0].lastKnownNetwork.split("-");
    mobileInfo.network.mcc = lastKnownNetwork[0];
    mobileInfo.network.mnc = lastKnownNetwork[1];

    var lastKnownHomeNetwork = mobileConnections[0].lastKnownHomeNetwork.split("-");
    mobileInfo.icc.mcc = lastKnownHomeNetwork[0];
    mobileInfo.icc.mnc = lastKnownHomeNetwork[1];
  }

  sender(mobileInfo);
});

DumbPipe.registerOpener("contacts", function(message, sender) {
  var req = navigator.mozContacts.getAll();

  req.onsuccess = function() {
    var contact = req.result;
    sender(contact);
    if (contact) {
      req.continue();
    }
  }

  req.onerror = function() {
    console.error("Error while reading contacts");
  }
});

DumbPipe.registerOpener("socket", function(message, sender) {
  var socket;
  try {
    socket = navigator.mozTCPSocket.open(message.host, message.port, { binaryType: "arraybuffer" });
  } catch(ex) {
    sender({ type: "error", error: "error opening socket" });
    return function() {};
  }

  socket.onopen = function() {
    sender({ type: "open" });
  }

  socket.onerror = function(event) {
    sender({ type: "error", error: event.data.name });
  }

  socket.ondata = function(event) {
    // Turn the buffer into a regular Array to traverse the mozbrowser boundary.
    var array = Array.prototype.slice.call(new Uint8Array(event.data));
    array.constructor = Array;

    sender({ type: "data", data: array });
  }

  socket.ondrain = function(event) {
    sender({ type: "drain" });
  }

  socket.onclose = function(event) {
    sender({ type: "close" });
  }

  var send = function(data, offset, length) {
    // Convert the data back to an Int8Array.
    data = new Int8Array(data);

    var result = socket.send(data.buffer, offset, length);

    sender({ type: "send", result: result });
  };

  return function(message) {
    switch (message.type) {
      case "send":
        send(message.data, message.offset, message.length);
        break;
      case "close":
        socket.close();
        break;
    }
  };
});
