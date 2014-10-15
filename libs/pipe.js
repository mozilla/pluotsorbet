/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

var DumbPipe = {
  recipients: {},
  nextPipeID: 0,

  open: function(type, message, recipient) {
    var pipeID = this.nextPipeID++;

    this.send({
      command: "open",
      type: type,
      pipeID: pipeID,
      message: message,
    });

    this.recipients[pipeID] = recipient;

    // Return a function that can be used to send a message to the other side.
    var sender = function(message) {
      var envelope = {
        command: "message",
        pipeID: pipeID,
        message: message,
      };
      //console.log("inner send: " + JSON.stringify(envelope));
      this.send(envelope);
    }.bind(this);

    sender.pipeID = pipeID;

    return sender;
  },

  close: function(sender) {
    delete this.recipients[sender.pipeID];

    this.send({
      command: "close",
      pipeID: sender.pipeID,
    });
  },

  sendQueue: [],
  isRunningSendQueue: false,

  send: function(envelope, callback) {
    this.sendQueue.push({
      envelope: envelope,
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
      var result = JSON.parse(prompt(JSON.stringify(item.envelope)));
      try {
        item.callback(result);
      } catch(ex) {
        console.error(ex + "\n" + ex.stack);
      }
    } else {
      alert(JSON.stringify(item.envelope));
    }

    if (this.sendQueue.length > 0) {
      window.setZeroTimeout(this.runSendQueue.bind(this));
    } else {
      this.isRunningSendQueue = false;
    }
  },

  handleEvent: function(event) {
    // To ensure we don't fill up the browser history over time, we navigate
    // "back" every time the other side navigates us "forward" by changing
    // the hash.  This will trigger a second hashchange event; to avoid getting
    // messages twice, we only get them for the second hashchange event,
    // i.e. once we've returned to the hashless page, at which point a second
    // call to window.history.back() will have had no effect.
    // XXX Commented out because it causes the automated tests to time out.
    // var hash = window.location.hash;
    // window.history.back();
    // if (window.location.hash != hash) {
    //   return;
    // }

    this.send({ command: "get" }, function(envelopes) {
      envelopes.forEach((function(envelope) {
        //console.log("inner recv: " + JSON.stringify(envelope));
        window.setZeroTimeout(function() {
          if (this.recipients[envelope.pipeID]) {
            try {
              this.recipients[envelope.pipeID](envelope.message);
            } catch(ex) {
              console.error(ex + "\n" + ex.stack);
            }
          } else {
            console.warn("nonexistent pipe " + envelope.pipeID + " received message " +
                         JSON.stringify(envelope.message));
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
