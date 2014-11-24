/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var SOCKET_OPT = {
  DELAY: 0,
  LINGER: 1,
  KEEPALIVE: 2,
  RCVBUF: 3,
  SNDBUF: 4,
};

Native.create("com/sun/midp/io/j2me/socket/Protocol.getIpNumber0.(Ljava/lang/String;[B)I", function(host, ipBytes) {
    // We'd need to modify ipBytes, that is an array with length 0
    // But we don't really need to do that, because getIpNumber0 is called only
    // before open0. So we just need to store the host and pass it to
    // mozTCPSocket::open.
    this.host = util.fromJavaString(host);
    return 0;
});

Native.create("com/sun/midp/io/j2me/socket/Protocol.getHost0.(Z)Ljava/lang/String;", function(local) {
    return local ? "127.0.0.1" : this.socket.host;
});

function Socket(host, port) {
    this.sender = DumbPipe.open("socket", { host: host, port: port }, this.recipient.bind(this));
    this.isClosed = false;
}

Socket.prototype.recipient = function(message) {
    if (message.type == "close") {
        this.isClosed = true;
        DumbPipe.close(this.sender);
    }
    var callback = this["on" + message.type];
    if (callback) {
        callback(message);
    }
}

Socket.prototype.send = function(data, offset, length) {
    // Convert the data to a regular Array to traverse the mozbrowser boundary.
    data = Array.prototype.slice.call(data.subarray(offset, offset + length));
    data.constructor = Array;

    this.sender({ type: "send", data: data });
}

Socket.prototype.close = function() {
    this.sender({ type: "close" });
}

Native.create("com/sun/midp/io/j2me/socket/Protocol.open0.([BI)V", function(ipBytes, port) {
    // console.log("Protocol.open0: " + this.host + ":" + port);

    return new Promise((function(resolve, reject) {
        this.socket = new Socket(this.host, port);

        this.options = {};
        this.options[SOCKET_OPT.DELAY] = 1;
        this.options[SOCKET_OPT.LINGER] = 0;
        this.options[SOCKET_OPT.KEEPALIVE] = 1;
        this.options[SOCKET_OPT.RCVBUF] = 8192;
        this.options[SOCKET_OPT.SNDBUF] = 8192;

        this.data = new Uint8Array();
        this.waitingData = null;

        this.socket.onopen = function() {
            // console.log("this.socket.onopen");
            resolve();
        }

        this.socket.onerror = function(message) {
            // console.log("this.socket.onerror: " + message.error);
            reject(new JavaException("java/io/IOException", message.error));
        }

        this.socket.ondata = (function(message) {
            // console.log("this.socket.ondata: " + JSON.stringify(message));
            var newData = new Uint8Array(message.data);
            var newArray = new Uint8Array(this.data.byteLength + newData.byteLength);
            newArray.set(this.data);
            newArray.set(newData, this.data.byteLength);
            this.data = newArray;

            if (this.waitingData) {
                this.waitingData();
            }
        }).bind(this);
    }).bind(this));
}, true);

Native.create("com/sun/midp/io/j2me/socket/Protocol.available0.()I", function() {
    // console.log("Protocol.available0: " + this.data.byteLength);
    return this.data.byteLength;
});

Native.create("com/sun/midp/io/j2me/socket/Protocol.read0.([BII)I", function(data, offset, length) {
    // console.log("Protocol.read0: " + this.socket.isClosed);

    return new Promise((function(resolve, reject) {
        // There might be data left in the buffer when the socket is closed, so we
        // should allow buffer reading even the socket has been closed.
        if (this.socket.isClosed && this.data.length == 0) {
            resolve(-1);
            return;
        }

        var copyData = (function() {
            var toRead = (length < this.data.byteLength) ? length : this.data.byteLength;

            data.set(this.data.subarray(0, toRead), offset);

            this.data = new Uint8Array(this.data.buffer.slice(toRead));

            resolve(toRead);
        }).bind(this);

        if (this.data.byteLength == 0) {
            this.waitingData = (function() {
                this.waitingData = null;
                copyData();
            }).bind(this);

            return;
        }

        copyData();
    }).bind(this));
}, true);

Native.create("com/sun/midp/io/j2me/socket/Protocol.write0.([BII)I", function(data, offset, length) {
    return new Promise(function(resolve, reject) {
        if (this.socket.isClosed) {
          reject(new JavaException("java/io/IOException", "socket is closed"));
          return;
        }

        this.socket.onsend = function(message) {
            this.socket.onsend = null;
            if ("error" in message) {
                console.error(message.error);
                reject(new JavaException("java/io/IOException", "error writing to socket"));
            } else if (message.result) {
                resolve(length);
            } else {
                this.socket.ondrain = function() {
                    this.socket.ondrain = null;
                    resolve(length);
                }.bind(this);
            }
        }.bind(this);

        this.socket.send(data, offset, length);
    }.bind(this));
}, true);

Native.create("com/sun/midp/io/j2me/socket/Protocol.setSockOpt0.(II)V", function(option, value) {
    if (!(option in this.options)) {
        throw new JavaException("java/lang/IllegalArgumentException", "Unsupported socket option");
    }

    this.options[option] = value;
});

Native.create("com/sun/midp/io/j2me/socket/Protocol.getSockOpt0.(I)I", function(option) {
    if (!(option in this.options)) {
        throw new JavaException("java/lang/IllegalArgumentException", "Unsupported socket option");
    }

    return this.options[option];
});

Native.create("com/sun/midp/io/j2me/socket/Protocol.close0.()V", function() {
    // console.log("Protocol.close0: " + this.socket.isClosed);

    return new Promise((function(resolve, reject) {
        if (this.socket.isClosed) {
            resolve();
            return;
        }

        this.socket.onclose = (function() {
            // console.log("this.socket.onclose");
            this.socket.onclose = null;
            resolve();
        }).bind(this);

        this.socket.close();
    }).bind(this));
}, true);

Native.create("com/sun/midp/io/j2me/socket/Protocol.shutdownOutput0.()V", function() {
    // We don't have the ability to close the output stream independently
    // of the connection as a whole.  But we don't seem to have to do anything
    // here, since this has just two call sites: one in Protocol.disconnect,
    // right before closing the socket; the other in Protocol.closeOutputStream,
    // which says it will be "called once by the child output stream," although
    // I can't find an actual caller.
});

Native.create("com/sun/midp/io/j2me/socket/Protocol.notifyClosedInput0.()V", function() {
    if (this.waitingData) {
        console.warn("Protocol.notifyClosedInput0.()V unimplemented while thread is blocked on read0");
    }
});

Native.create("com/sun/midp/io/j2me/socket/Protocol.notifyClosedOutput0.()V", function() {
    if (this.socket.ondrain) {
        console.warn("Protocol.notifyClosedOutput0.()V unimplemented while thread is blocked on write0");
    }
});
