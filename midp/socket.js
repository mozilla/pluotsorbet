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

Native["com/sun/midp/io/j2me/socket/Protocol.getIpNumber0.(Ljava/lang/String;[B)I"] =
function(addr, hostAddr, ipBytesAddr) {
    // We're supposed to write the IP address of the host into ipBytes,
    // but we don't actually have to do that, because we can defer resolution
    // until Protocol.open0 (and delegate it to the native socket impl).
    // So this is a no-op.
    return 0;
};

Native["com/sun/midp/io/j2me/socket/Protocol.getHost0.(Z)Ljava/lang/String;"] = function(addr, local) {
    // XXX We should probably retrieve the host directly from the Java object,
    // as Protocol.open0 does.  Then we wouldn't have to get the native socket,
    // and we might avoid an exception if the socket hasn't been opened yet
    // (so the native socket doesn't exist).
    // var self = getHandle(addr);
    // var host = J2ME.fromStringAddr(self.host);

    var socket = NativeMap.get(addr);
    return J2ME.newUncollectableString(local ? "127.0.0.1" : socket.host);
};

function Socket(host, port, ctx, resolve, reject) {
    this.sender = DumbPipe.open("socket", { host: host, port: port }, this.recipient.bind(this));
    this.isClosed = false;

    this.options = {};
    this.options[SOCKET_OPT.DELAY] = 1;
    this.options[SOCKET_OPT.LINGER] = 0;
    this.options[SOCKET_OPT.KEEPALIVE] = 1;
    this.options[SOCKET_OPT.RCVBUF] = 8192;
    this.options[SOCKET_OPT.SNDBUF] = 8192;

    this.data = [];
    this.dataLen = 0;
    this.waitingData = null;

    // XXX Move the below functions to the prototype (if possible; they would
    // need to called with the socket instance as their *this* object, and it
    // isn't clear that they are).

    this.onopen = function() {
        // console.log("this.onopen");
        resolve();
    }

    this.onerror = function(message) {
        ctx.setAsCurrentContext();
        // console.log("this.onerror: " + message.error);
        reject($.newIOException(message.error));
    }

    this.onclose = function() {
        if (this.waitingData) {
            this.waitingData();
        }
    }.bind(this);

    this.ondata = function(message) {
        this.data.push(new Int8Array(message.data));
        this.dataLen += message.data.byteLength;

        if (this.waitingData) {
            this.waitingData();
        }
    }.bind(this);
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

Native["com/sun/midp/io/j2me/socket/Protocol.open0.([BI)V"] = function(addr, ipBytesAddr, port) {
    var self = getHandle(addr);
    var host = J2ME.fromStringAddr(self.host);
    // console.log("Protocol.open0: " + host + ":" + port);
    asyncImpl("V", new Promise(function(resolve, reject) {
        setNative(addr, new Socket(host, port, $.ctx, resolve, reject));
    }));
};

Native["com/sun/midp/io/j2me/socket/Protocol.available0.()I"] = function(addr) {
    var socket = NativeMap.get(addr);
    // console.log("Protocol.available0: " + socket.data.byteLength);
    return socket.dataLen;
};

Native["com/sun/midp/io/j2me/socket/Protocol.read0.([BII)I"] = function(addr, dataAddr, offset, length) {
    var data = J2ME.getArrayFromAddr(dataAddr);
    var socket = NativeMap.get(addr);
    // console.log("Protocol.read0: " + socket.isClosed);

    asyncImpl("I", new Promise(function(resolve, reject) {
        // There might be data left in the buffer when the socket is closed, so we
        // should allow buffer reading even the socket has been closed.
        if (socket.isClosed && socket.dataLen === 0) {
            resolve(-1);
            return;
        }

        var copyData = function() {
            var toRead = (length < socket.dataLen) ? length : socket.dataLen;
            var read = 0;
            while (read < toRead) {
                var remaining = toRead - read;

                var array = socket.data[0];

                if (array.byteLength > remaining) {
                    data.set(array.subarray(0, remaining), read + offset);
                    socket.data[0] = array.subarray(remaining);
                    read += remaining;
                } else {
                    data.set(array, read + offset);
                    socket.data.shift();
                    read += array.byteLength;
                }
            }

            socket.dataLen -= read;

            resolve(read);
        };

        if (socket.dataLen === 0) {
            socket.waitingData = function() {
                socket.waitingData = null;
                copyData();
            };

            return;
        }

        copyData();
    }));
};

Native["com/sun/midp/io/j2me/socket/Protocol.write0.([BII)I"] = function(addr, dataAddr, offset, length) {
    var data = J2ME.getArrayFromAddr(dataAddr);
    var socket = NativeMap.get(addr);
    var ctx = $.ctx;
    asyncImpl("I", new Promise(function(resolve, reject) {
        if (socket.isClosed) {
          ctx.setAsCurrentContext();
          reject($.newIOException("socket is closed"));
          return;
        }

        socket.onsend = function(message) {
            socket.onsend = null;
            if ("error" in message) {
                console.error(message.error);
                ctx.setAsCurrentContext();
                reject($.newIOException("error writing to socket"));
            } else if (message.result) {
                resolve(length);
            } else {
                socket.ondrain = function() {
                    socket.ondrain = null;
                    resolve(length);
                };
            }
        };

        socket.send(data, offset, length);
    }));
};

Native["com/sun/midp/io/j2me/socket/Protocol.setSockOpt0.(II)V"] = function(addr, option, value) {
    var socket = NativeMap.get(addr);
    if (!(option in socket.options)) {
        throw $.newIllegalArgumentException("Unsupported socket option");
    }

    socket.options[option] = value;
};

Native["com/sun/midp/io/j2me/socket/Protocol.getSockOpt0.(I)I"] = function(addr, option) {
    var socket = NativeMap.get(addr);
    if (!(option in socket.options)) {
        throw new $.newIllegalArgumentException("Unsupported socket option");
    }

    return socket.options[option];
};

Native["com/sun/midp/io/j2me/socket/Protocol.close0.()V"] = function(addr) {
    var socket = NativeMap.get(addr);
    // console.log("Protocol.close0: " + socket.isClosed);

    asyncImpl("V", new Promise(function(resolve, reject) {
        if (socket.isClosed) {
            resolve();
            return;
        }

        socket.onclose = function() {
            // console.log("socket.onclose");
            socket.onclose = null;
            resolve();
        };

        socket.close();
    }));
};

Native["com/sun/midp/io/j2me/socket/Protocol.shutdownOutput0.()V"] = function(addr) {
    // We don't have the ability to close the output stream independently
    // of the connection as a whole.  But we don't seem to have to do anything
    // here, since this has just two call sites: one in Protocol.disconnect,
    // right before closing the socket; the other in Protocol.closeOutputStream,
    // which says it will be "called once by the child output stream," although
    // I can't find an actual caller.
};

Native["com/sun/midp/io/j2me/socket/Protocol.notifyClosedInput0.()V"] = function(addr) {
    var socket = NativeMap.get(addr);
    if (socket.waitingData) {
        console.warn("Protocol.notifyClosedInput0.()V unimplemented while thread is blocked on read0");
    }
};

Native["com/sun/midp/io/j2me/socket/Protocol.notifyClosedOutput0.()V"] = function(addr) {
    var socket = NativeMap.get(addr);
    if (socket.ondrain) {
        console.warn("Protocol.notifyClosedOutput0.()V unimplemented while thread is blocked on write0");
    }
};
