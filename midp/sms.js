/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

MIDP.lastSMSConnection = -1;
MIDP.lastSMSID = -1;
MIDP.smsConnections = {};
MIDP.j2meSMSMessages = [];
MIDP.j2meSMSWaiting = null;
MIDP.nokiaSMSMessages = [];

/**
 * Simulate a received SMS with the given text, sent to the specified addr.
 * (It appears the value of `addr` is unimportant for most apps.)
 */
function receiveSms(text, addr) {
    var sms = {
      text: text,
      addr: addr,
      id: ++MIDP.lastSMSID,
    };

    MIDP.nokiaSMSMessages.push(sms);
    MIDP.j2meSMSMessages.push(sms);

    window.dispatchEvent(new CustomEvent("nokia.messaging", {
      detail: sms
    }));

    if (MIDP.j2meSMSWaiting) {
      MIDP.j2meSMSWaiting();
    }
}

/**
 * This app is listening for SMS messages; for most apps, that means
 * they're looking for the content of a message the app's servers just
 * sent. Prompt the user to enter that code here, and forward it to
 * the app.
 */
function promptForMessageText() {
    var el = document.getElementById('sms-listener-prompt').cloneNode(true);
    el.style.display = 'block';
    el.classList.add('visible');

    var input = el.querySelector('input');
    var btnCancel = el.querySelector('button.cancel');
    var btnDone = el.querySelector('button.recommend');

    btnDone.disabled = true; // Wait for input before enabling.
    input.addEventListener('input', function() {
        btnDone.disabled = (input.value.length === 0);
    });

    btnCancel.addEventListener('click', function() {
        console.warn('SMS prompt canceled.');
        clearInterval(intervalID);
        clearTimeout(timeoutID);
        el.parentElement.removeChild(el);
    });

    btnDone.addEventListener('click', function() {
        clearInterval(intervalID);
        clearTimeout(timeoutID);
        el.parentElement.removeChild(el);
        console.log('SMS prompt filled out:', input.value);
        // We don't have easy access to our own phone number; use a
        // dummy unknown value instead.
        receiveSms(MIDlet.SMSDialogReceiveFilter(input.value), 'unknown');
    });

    function toTimeText(ms) {
      var seconds = ms / 1000;
      var minutes = Math.floor(seconds / 60);
      seconds -= minutes * 60;

      var text = minutes + ":";

      if (seconds > 10) {
        text += seconds;
      } else {
        text += "0" + seconds;
      }

      return text;
    }

    el.querySelector('p.timeLeft').textContent = toTimeText(MIDlet.SMSDialogTimeout) +
                                                 " " + MIDlet.SMSDialogTimeoutText;

    document.body.appendChild(el);
    input.focus();

    var elapsedMS = 0;
    var intervalID = setInterval(function() {
      elapsedMS += 1000;
      el.querySelector('p.timeLeft').textContent = toTimeText(MIDlet.SMSDialogTimeout - elapsedMS) +
                                                   " " + MIDlet.SMSDialogTimeoutText;
      el.querySelector('progress.timeLeftBar').value = elapsedMS / MIDlet.SMSDialogTimeout * 100;
    }, 1000);

    // Remove the dialog after a timeout
    var timeoutID = setTimeout(function() {
        clearInterval(intervalID);
        el.parentElement.removeChild(el);
    }, MIDlet.SMSDialogTimeout);
}

Native.create("com/sun/midp/io/j2me/sms/Protocol.open0.(Ljava/lang/String;II)I", function(host, msid, port) {
    MIDP.smsConnections[++MIDP.lastSMSConnection] = {
      port: port,
      msid: msid,
      host: util.fromJavaString(host),
    };

    return ++MIDP.lastSMSConnection;
});

Native.create("com/sun/midp/io/j2me/sms/Protocol.receive0.(IIILcom/sun/midp/io/j2me/sms/Protocol$SMSPacket;)I",
function(port, msid, handle, smsPacket) {
    return new Promise(function(resolve, reject) {
        promptForMessageText();

        function receiveSMS() {
            var sms = MIDP.j2meSMSMessages.shift();
            var text = sms.text;
            var addr = sms.addr;

            var message = util.newPrimitiveArray("B", text.length);
            for (var i = 0; i < text.length; i++) {
                message[i] = text.charCodeAt(i);
            }

            var address = util.newPrimitiveArray("B", addr.length);
            for (var i = 0; i < addr.length; i++) {
                address[i] = addr.charCodeAt(i);
            }

            smsPacket.class.getField("I.message.[B").set(smsPacket, message);
            smsPacket.class.getField("I.address.[B").set(smsPacket, address);
            smsPacket.class.getField("I.port.I").set(smsPacket, port);
            smsPacket.class.getField("I.sentAt.J").set(smsPacket, Long.fromNumber(Date.now()));
            smsPacket.class.getField("I.messageType.I").set(smsPacket, 0); // GSM_TEXT

            return text.length;
        }

        if (MIDP.j2meSMSMessages.length > 0) {
          resolve(receiveSMS());
        } else {
          MIDP.j2meSMSWaiting = function() {
            MIDP.j2meSMSWaiting = null;
            resolve(receiveSMS());
          }
        }
    });
}, true);

Native.create("com/sun/midp/io/j2me/sms/Protocol.close0.(III)I", function(port, handle, deRegister) {
    delete MIDP.smsConnections[handle];
    return 0;
});

Native.create("com/sun/midp/io/j2me/sms/Protocol.numberOfSegments0.([BIIZ)I", function(msgBuffer, msgLen, msgType, hasPort) {
    console.warn("com/sun/midp/io/j2me/sms/Protocol.numberOfSegments0.([BIIZ)I not implemented");
    return 1;
});

Native.create("com/sun/midp/io/j2me/sms/Protocol.send0.(IILjava/lang/String;II[B)I",
function(handle, type, host, destPort, sourcePort, message) {
    return new Promise(function(resolve, reject) {
        var activity = new MozActivity({
            name: "new",
            data: {
              type: "websms/sms",
              number: util.fromJavaString(host),
              body: new TextDecoder('utf-16be').decode(message),
            },
        });

        activity.onsuccess = function() {
          resolve(message.byteLength);
        };

        activity.onerror = function() {
          reject(new JavaException("java/io/IOException", "Error while sending SMS message"));
        };
    });
}, true);
