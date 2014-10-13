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
    loadingPromises.push(loadScript("tests/contacts.js"));
  }

  Promise.all(loadingPromises).then(function() {
    document.getElementById("mozbrowser").src = "main.html" + location.search;
  });
})();

function getAllContacts(message, pipe) {
  var req = navigator.mozContacts.getAll();

  req.onsuccess = function() {
    var contact = req.result;
    pipe(contact);
    if (contact) {
      req.continue();
    }
  }

  req.onerror = function() {
    console.error("Error while reading contacts");
  }
}

function getMobileInfo(message, pipe) {
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

  pipe(mobileInfo);
}

var DumbPipe = {
  pipes: {},

  packets: [],

  // Every time we want to make the other side retrieve messages, the hash has
  // to change, so we set it to an ever-incrementing value.
  nextHashID: 0,

  handleEvent: function(event) {
    if (event.detail.promptType == "custom-prompt") {
      console.warn("unresponsive script warning; figure out how to handle");
      return;
    }

    /**
     * We embed packets in the mozbrowsershowmodalprompt event's detail.message
     * property.  The inner "message" property of the parsed JSON object
     * is the payload that the other side is sending to our side via the event.
     *
     * @property command {String} the command to invoke: open|receive|get
     * @property type {String} the type of pipe to open (when command == open)
     * @property id {Number} the ID of the pipe (when command == open, receive)
     * @property message {String} the JSON message to forward to this side
     */
    var packet = JSON.parse(event.detail.message);

    switch (packet.command) {
      case "open":
        //console.log("outer recv: " + JSON.stringify(packet));
        this.openPipe(packet.id, packet.type, packet.message);
        break;
      case "message":
        //console.log("outer recv: " + JSON.stringify(packet));
        this.receiveMessage(packet.id, packet.message, event.detail);
        break;
      case "get":
        this.getPackets(event);
        break;
      case "close":
        //console.log("outer recv: " + JSON.stringify(packet));
        this.closePipe(packet.id);
        break;
    }
  },

  openPipe: function(id, type, message) {
    // Create a function that the pipe on this side of the boundary
    // can use to communicate back to the pipe on the other side.
    var sendMessage = this.sendMessage.bind(this, id);

    // Call the appropriate function to initialize the pipe.
    switch (type) {
      case "echo":
        this.pipes[id] = openEchoPipe(message, sendMessage);
        break;
      case "socket":
        this.pipes[id] = openSocketPipe(message, sendMessage);
        break;
      case "mobileInfo":
        this.pipes[id] = getMobileInfo(message, sendMessage);
        break;
      case "contacts":
        this.pipes[id] = getAllContacts(message, sendMessage);
        break;
    }
  },

  sendMessage: function(id, message) {
    // Sadly, we have no way to send a message to the other side directly.
    // Instead, we change the hash part of the other side's URL, which triggers
    // a hashchange event on the other side.  A listener on the other side
    // then sends us a "get" prompt, and we set its return value to the message.
    // Oh my shod, that's some funky git!
    var packet = { id: id, message: message };
    //console.log("outer send: " + JSON.stringify(packet));
    this.packets.push(packet);

    var mozbrowser = document.getElementById("mozbrowser");
    window.setTimeout(function() {
      mozbrowser.src = mozbrowser.src.split("#")[0] + "#" + this.nextHashID++;
    }.bind(this), 0);
  },

  receiveMessage: function(id, message, detail) {
    if (!this.pipes[id]) {
      console.warn("nonexistent pipe " + id + " received message " + JSON.stringify(message));
      return;
    }

    try {
      this.pipes[id](message);
    } catch(ex) {
      console.error(ex);
    }
  },

  getPackets: function(event) {
    try {
      event.detail.returnValue = JSON.stringify(this.packets);
      this.packets = [];
    }
    finally {
      event.detail.unblock();
    }
  },

  closePipe: function(id) {
    delete this.pipes[id];
  }
};

document.getElementById("mozbrowser").addEventListener("mozbrowsershowmodalprompt", DumbPipe.handleEvent.bind(DumbPipe), true);

var openSocketPipe = function(message, pipe) {
  var socket;
  try {
    socket = navigator.mozTCPSocket.open(message.host, message.port, { binaryType: "arraybuffer" });
  } catch(ex) {
    pipe({ type: "error", error: "error opening socket" });
    return function() {};
  }

  socket.onopen = function() {
    pipe({ type: "open" });
  }

  socket.onerror = function(event) {
    pipe({ type: "error", error: event.data.name });
  }

  socket.ondata = function(event) {
    // Turn the buffer into a regular Array to traverse the mozbrowser boundary.
    var array = Array.prototype.slice.call(new Uint8Array(event.data));
    array.constructor = Array;

    pipe({ type: "data", data: array });
  }

  socket.ondrain = function(event) {
    pipe({ type: "drain" });
  }

  socket.onclose = function(event) {
    pipe({ type: "close" });
  }

  var send = function(data, offset, length) {
    // Convert the data back to an Int8Array.
    data = new Int8Array(data);

    var result = socket.send(data.buffer, offset, length);

    pipe({ type: "send", result: result });
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
};

var openEchoPipe = function(message, pipe) {
  pipe(message);
};
