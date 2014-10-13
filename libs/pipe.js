/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

var DumbPipe = {
  pipes: [],

  nextPipeID: 0,

  open: function(type, message, handler) {
    var id = this.nextPipeID++;

    this.send({
      command: "open",
      type: type,
      id: id,
      message: message,
    });

    this.pipes[id] = handler;

    // Return a function that can be used to send a message to the other side.
    var pipe = function(message) {
      var packet = {
        command: "message",
        id: id,
        message: message,
      };
      console.log("inner send: " + JSON.stringify(packet));
      this.send(packet);
    }.bind(this);

    pipe.id = id;

    return pipe;
  },

  close: function(pipe) {
    delete this.pipes[pipe.id];

    this.send({
      command: "close",
      id: pipe.id,
    });
  },

  sendQueue: [],
  isRunningSendQueue: false,

  send: function(packet, callback) {
    this.sendQueue.push({
      packet: packet,
      callback: callback,
    });

    if (!this.isRunningSendQueue) {
      this.isRunningSendQueue = true;
      window.setZeroTimeout(this.runSendQueue.bind(this));
    }
  },

  runSendQueue: function() {
    var item = this.sendQueue.shift();

    if (item.callback) {
      var result = JSON.parse(prompt(JSON.stringify(item.packet)));
      try {
        item.callback(result);
      } catch(ex) {
        console.error(ex);
      }
    } else {
      alert(JSON.stringify(item.packet));
    }

    if (this.sendQueue.length > 0) {
      window.setZeroTimeout(this.runSendQueue.bind(this));
    } else {
      this.isRunningSendQueue = false;
    }
  },

  handleEvent: function(event) {
    this.send({ command: "get" }, function(packets) {
      packets.forEach((function(packet) {
        console.log("inner recv: " + JSON.stringify(packet));
        window.setZeroTimeout(function() {
          if (this.pipes[packet.id]) {
            try {
              this.pipes[packet.id](packet.message);
            } catch(ex) {
              console.error(ex);
            }
          } else {
            console.warn("nonexistent pipe " + packet.id + " received message " + JSON.stringify(packet.message));
          }
        }.bind(this));
      }).bind(this));
    }.bind(this));
  },
};

window.addEventListener("hashchange", DumbPipe.handleEvent.bind(DumbPipe), false);

// If "mozbrowser" isn't enabled on the frame we're loaded in, then override
// the alert/prompt functions to funnel messages to the endpoint in the parent.
if (window.parent !== window) {
  alert = function(message) {
    window.parent.DumbPipe.handleEvent({
      detail: {
        promptType: "alert",
        message: message,
      }
    });
  };

  prompt = function(message) {
    var event = {
      detail: {
        promptType: "prompt",
        message: message,
        unblock: function() {},
      }
    };
    window.parent.DumbPipe.handleEvent(event);
    return event.detail.returnValue;
  };
}
