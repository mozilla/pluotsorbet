/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

/**
 * Pre-load dependencies and then load the main page.
 */
(function() {
  var midletClassName = config.midletClassName ? config.midletClassName.replace(/\//g, '.') : "RunTests";
  var loadingPromises = [];
  if (midletClassName == "RunTests") {
    loadingPromises.push(loadScript("tests/contacts.js"),
                         loadScript("tests/index.js"));
  }

  if (navigator.userAgent.indexOf('Chrome')) {
    loadingPromises.push(loadScript("chrome_polyfills.js"));
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
    var envelope = { pipeID: pipeID, message: message };
    //console.log("outer send: " + JSON.stringify(envelope));

    try {
      document.getElementById("mozbrowser").contentWindow.postMessage(envelope, "*");
    } catch (e) {
      console.log("Error " + e + " while sending message: " + JSON.stringify(envelope));
    }
  },

  receiveMessage: function(pipeID, message, detail) {
    window.setZeroTimeout(function() {
      if (!this.recipients[pipeID]) {
        console.warn("nonexistent pipe " + pipeID + " received message " + JSON.stringify(message));
        return;
      }

      try {
        this.recipients[pipeID](message);
      } catch(ex) {
        console.error(ex + "\n" + ex.stack);
      }
    }.bind(this));
  },

  closePipe: function(pipeID) {
    delete this.recipients[pipeID];
  }
};

document.getElementById("mozbrowser").addEventListener("mozbrowsershowmodalprompt",
                                                       DumbPipe.handleEvent.bind(DumbPipe),
                                                       true);

DumbPipe.registerOpener("alert", function(message, sender) {
  alert(message);
});

DumbPipe.registerOpener("mobileInfo", function(message, sender) {
  // Initialize the object with the URL params and fallback placeholders
  // for testing/debugging on a desktop.
  var mobileInfo = {
    network: {
      mcc: config.network_mcc || "310", // United States
      mnc: config.network_mnc || "001",
    },
    icc: {
      mcc: config.icc_mcc || "310", // United States
      mnc: config.icc_mnc || "001",
      msisdn: config.icc_msisdn || "10005551212",
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
    // Transform the mozContact into a normal object, otherwise
    // the pipe won't be able to send it.
    sender(contact ? JSON.parse(JSON.stringify(contact)) : null);
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
    sender({ type: "data", data: event.data });
  }

  socket.ondrain = function(event) {
    sender({ type: "drain" });
  }

  socket.onclose = function(event) {
    sender({ type: "close" });
  }

  var send = function(data) {
    // Convert the data back to an Int8Array.
    data = new Int8Array(data);

    try {
      var result = socket.send(data.buffer, 0, data.length);
      sender({ type: "send", result: result });
    } catch (ex) {
      sender({ type: "send", error: ex.toString() });
    }
  };

  return function(message) {
    switch (message.type) {
      case "send":
        send(message.data);
        break;
      case "close":
        socket.close();
        break;
    }
  };
});

DumbPipe.registerOpener("audiorecorder", function(message, sender) {
    var mediaRecorder = null;
    var localAudioStream = null;

    function startRecording(localStream) {
        localAudioStream = localStream;

        mediaRecorder = new MediaRecorder(localStream, {
            mimeType: message.mimeType // 'audio/3gpp' // need to be certified app.
        });

        mediaRecorder.ondataavailable = function(e) {
            if (e.data.size == 0) {
                return;
            }

            var fileReader = new FileReader();
            fileReader.onload = function() {
                sender({ type: "data", data: fileReader.result });
            };
            fileReader.readAsArrayBuffer(e.data);
        };

        mediaRecorder.onstop = function(e) {
            // Do nothing here, just relay the event.
            //
            // We can't close the pipe here, one reason is |onstop| is fired before |ondataavailable|,
            // if close pipe here, there is no chance to deliever the recorded voice. Another reason is
            // the recording might be stopped and started back and forth. So let's do the pipe
            // closing on the other side instead, i.e. DirectRecord::nClose.
            sender({ type: "stop" });
        };

        mediaRecorder.onerror = function(e) {
            sender({ type: "error" });
        };

        mediaRecorder.onpause = function(e) {
            sender({ type: "pause" });
        };

        mediaRecorder.onstart = function(e) {
            sender({ type: "start" });
        };

        mediaRecorder.start();
    }

    return function(message) {
        switch(message.type) {
            case "start":
                try {
                    if (!mediaRecorder) {
                        navigator.mozGetUserMedia({
                            audio: true
                        }, function(localStream) {
                            startRecording(localStream);
                        }, function(e) {
                            sender({ type: "error", error: e });
                        });
                    } else if (mediaRecorder.state == "paused") {
                        mediaRecorder.resume();
                    } else {
                        mediaRecorder.start();
                    }
                } catch (e) {
                    sender({ type: "error", error: e });
                }
                break;
            case "requestData":
                try {
                    // An InvalidState error might be thrown.
                    mediaRecorder.requestData();
                } catch (e) {
                    sender({ type: "error", error: e });
                }
                break;
            case "pause":
                try {
                    mediaRecorder.pause();
                } catch (e) {
                    sender({ type: "error", error: e });
                }
                break;
            case "stop":
                try {
                    mediaRecorder.stop();
                    localAudioStream.stop();
                    mediaRecorder = null;
                    localAudioStream = null;
                } catch (e) {
                    sender({ type: "error", error: e });
                }
                break;
        }
    };
});

DumbPipe.registerOpener("camera", function(message, sender) {
  var mediaStream = null;
  var url = null;

  var video = document.createElement("video");
  document.body.appendChild(video);
  video.style.position = "absolute";
  video.style.visibility = "hidden";
  // Some MIDlets need user touch/click on the screen to complete the snapshot,
  // to make sure the MIDlet itself instead of the video element can capture
  // the mouse/touch events, we need to set `pointer-events` as `none`.
  video.style.pointerEvents = "none";

  video.addEventListener('canplay', function(ev) {
    // We should use videoWidth and videoHeight, but they are unavailable (https://bugzilla.mozilla.org/show_bug.cgi?id=926753)
    var getDimensions = setInterval(function() {
      if (video.videoWidth > 0 && video.videoHeight > 0) {
        clearInterval(getDimensions);
        sender({ type: "gotstream", width: video.videoWidth, height: video.videoHeight });
      }
    }, 50);
  }, false);

  navigator.mozGetUserMedia({
    video: true,
    audio: false,
  }, function(localMediaStream) {
    mediaStream = localMediaStream;
    url = URL.createObjectURL(localMediaStream);

    video.onerror = video.onloadeddata = function() {
      URL.revokeObjectURL(url);
    };

    video.src = url;
    video.play();
  }, function(err) {
    console.log("Error: " + err);
    sender({ type: "initerror", name: err.name, message: err.message });
  });

  return function(message) {
    switch (message.type) {
      case "setPosition":
        video.style.left = message.x + "px";
        video.style.top = message.y + "px";
        video.style.width = message.w + "px";
        video.style.height = message.h + "px";
        break;

      case "setVisible":
        video.style.visibility = message.visible ? "visible" : "hidden";
        break;

      case "snapshot":
        var canvas = document.createElement("canvas");
        canvas.width = video.videoWidth;
        canvas.height = video.videoHeight;
        var ctx = canvas.getContext("2d");
        ctx.drawImage(video, 0, 0, video.videoWidth, video.videoHeight);

        canvas.toBlob(function(blob) {
          var fileReader = new FileReader();

          fileReader.onload = function(data) {
            sender({ type: "snapshot", data: fileReader.result });
          }

          fileReader.readAsArrayBuffer(blob);
        }, message.imageType);
        break;

      case "close":
        if (mediaStream) {
          mediaStream.stop();
        }

        if (video.parentNode) {
          document.body.removeChild(video);
        }

        break;
    }
  };
});

var notification = null;
DumbPipe.registerOpener("notification", function(message, sender) {
  if (notification) {
    notification.close();
    notification = null;
  }

  var img = new Image();
  img.src = URL.createObjectURL(new Blob([ new Uint8Array(message.icon) ], { type : message.mime_type }));
  img.onload = function() {
    var width = Math.min(32, img.naturalWidth);
    var height = Math.min(32, img.naturalHeight);

    var canvas = document.createElement("canvas");
    canvas.width = width;
    canvas.height = height;
    var ctx = canvas.getContext("2d");
    ctx.drawImage(img, 0, 0, width, height);

    URL.revokeObjectURL(img.src);
    img.src = '';

    message.options.icon = canvas.toDataURL();

    function permissionGranted() {
      notification = new Notification(message.title, message.options);
      notification.onshow = function() {
        sender({ type: "opened" });
      };
      notification.onclick = function() {
        var request = navigator.mozApps.getSelf();
        request.onsuccess = function() {
          var app = request.result;
          if (app) {
            app.launch();
          }
        };
      };
    }

    if (Notification.permission === "granted") {
      permissionGranted();
    } else if (Notification.permission !== 'denied') {
      Notification.requestPermission(function(permission) {
        if (permission === "granted") {
          permissionGranted();
        }
      });
    }
  }

  img.onerror = function() {
    URL.revokeObjectURL(img.src);
  }

  return function(message) {
    switch(message.type) {
      case "close":
        if (notification) {
          notification.close();
          notification = null;
        }

        sender({ type: "close" });
      break;
    }
  }
});

DumbPipe.registerOpener("JARDownloader", function(url, sender) {
  loadWithProgress(url, "text", function(jadData) {
    try {
      var manifest = {};

      jadData
      .replace(/\r\n|\r/g, "\n")
      .replace(/\n /g, "")
      .split("\n")
      .forEach(function(entry) {
        if (entry) {
          var keyEnd = entry.indexOf(":");
          var key = entry.substring(0, keyEnd);
          var val = entry.substring(keyEnd + 1).trim();
          manifest[key] = val;
        }
      });

      var jarURL = manifest["MIDlet-Jar-URL"];

      if (!jarURL.startsWith("http")) {
        var jarName = jarURL.substring(jarURL.lastIndexOf("/") + 1);
        jarURL = url.substring(0, url.lastIndexOf("/") + 1) + jarName;
      }

      loadWithProgress(jarURL, "arraybuffer", function(jarData) {
        sender({ type: "done", data: { jadData: jadData, jarData: jarData } });
      }, function() {
        sender({ type: "fail" });
      }, function(progress) {
        sender({ type: "progress", progress: progress });
      }, manifest["MIDlet-Jar-Size"] || 0);
    } catch (e) {
      sender({ type: "fail" });
    }
  }, function() {
    sender({ type: "fail" });
  });
});

DumbPipe.registerOpener("windowOpen", function(message, sender) {
  window.open(message);
});

DumbPipe.registerOpener("reload", function(message, sender) {
  window.location.reload();
});
