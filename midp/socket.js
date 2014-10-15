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

Native.create("com/sun/midp/io/j2me/socket/Protocol.getIpNumber0.(Ljava/lang/String;[B)I", function(ctx, host, ipBytes) {
    // We'd need to modify ipBytes, that is an array with length 0
    // But we don't really need to do that, because getIpNumber0 is called only
    // before open0. So we just need to store the host and pass it to
    // mozTCPSocket::open.
    this.host = util.fromJavaString(host);
    return 0;
});

Native.create("com/sun/midp/io/j2me/socket/Protocol.getHost0.(Z)Ljava/lang/String;", function(ctx, local) {
    return local ? "127.0.0.1" : this.socket.host;
});

function Socket(host, port) {
    this.pipe = DumbPipe.open("socket", { host: host, port: port }, this.handleMessage.bind(this));
    this.isClosed = false;
}

Socket.prototype.handleMessage = function(message) {
    if (message.type == "close") {
        this.isClosed = true;
    }
    var callback = this["on" + message.type];
    if (callback) {
        callback(message);
    }
}

Socket.prototype.send = function(data, offset, length) {
    // Convert the data to a regular Array to traverse the mozbrowser boundary.
    data = Array.prototype.slice.call(data);
    data.constructor = Array;

    this.pipe({ type: "send", data: data, offset: offset, length: length });
}

Socket.prototype.close = function() {
    window.setZeroTimeout(function() {
        this.pipe({ type: "close" });
    }.bind(this));
}

Native["com/sun/midp/io/j2me/socket/Protocol.open0.([BI)V"] = function(ctx, stack) {
    var port = stack.pop(), ipBytes = stack.pop(), _this = stack.pop();
    // console.log("Protocol.open0: " + _this.host + ":" + port);

    _this.socket = new Socket(_this.host, port);

    _this.options = {};
    _this.options[SOCKET_OPT.DELAY] = 1;
    _this.options[SOCKET_OPT.LINGER] = 0;
    _this.options[SOCKET_OPT.KEEPALIVE] = 1;
    _this.options[SOCKET_OPT.RCVBUF] = 8192;
    _this.options[SOCKET_OPT.SNDBUF] = 8192;

    _this.data = new Uint8Array();
    _this.waitingData = null;

    _this.socket.onopen = function() {
        ctx.resume();
    }

    _this.socket.onerror = function(message) {
        ctx.raiseException("java/io/IOException", message.error);
        ctx.resume();
    }

    _this.socket.ondata = function(message) {
        var newArray = new Uint8Array(_this.data.byteLength + message.data.length);
        newArray.set(_this.data);
        newArray.set(message.data, _this.data.byteLength);
        _this.data = newArray;

        if (_this.waitingData) {
            _this.waitingData();
        }
    }

    throw VM.Pause;
}

Native.create("com/sun/midp/io/j2me/socket/Protocol.available0.()I", function(ctx) {
    return this.data.byteLength;
});

Native["com/sun/midp/io/j2me/socket/Protocol.read0.([BII)I"] = function(ctx, stack) {
    var length = stack.pop(), offset = stack.pop(), data = stack.pop(), _this = stack.pop();

    // console.log("Protocol.read0: " + _this.socket.isClosed);

    if (_this.socket.isClosed) {
        stack.push(-1);
        return;
    }

    function copyData() {
        var toRead = (length < _this.data.byteLength) ? length : _this.data.byteLength;

        data.set(_this.data.subarray(0, toRead), offset);

        _this.data = new Uint8Array(_this.data.buffer.slice(toRead));

        stack.push(toRead);
    }

    if (_this.data.byteLength == 0) {
        _this.waitingData = function() {
            _this.waitingData = null;
            copyData();
            ctx.resume();
        }
        throw VM.Pause;
    }

    copyData();
}

Native["com/sun/midp/io/j2me/socket/Protocol.write0.([BII)I"] = function(ctx, stack) {
    var length = stack.pop(), offset = stack.pop(), data = stack.pop(), _this = stack.pop();
    // console.log("Protocol.write0: " + String.fromCharCode.apply(String, Array.prototype.slice.call(data.subarray(offset, offset + length))));

    _this.socket.onsend = function(message) {
        _this.socket.onsend = null;
        if (message.result) {
            stack.push(length);
            ctx.start();
        } else {
            _this.socket.ondrain = function() {
                _this.socket.ondrain = null;
                stack.push(length);
                ctx.start();
            };
        }
    }

    _this.socket.send(data, offset, length);

    throw VM.Pause;
}

Native.create("com/sun/midp/io/j2me/socket/Protocol.setSockOpt0.(II)V", function(ctx, option, value) {
    if (!(option in this.options)) {
        throw new JavaException("java/lang/IllegalArgumentException", "Unsupported socket option");
    }

    this.options[option] = value;
});

Native.create("com/sun/midp/io/j2me/socket/Protocol.getSockOpt0.(I)I", function(ctx, option) {
    if (!(option in this.options)) {
        ctx.raiseException("java/lang/IllegalArgumentException", "Unsupported socket option");
    }

    return this.options[option];
});

Native["com/sun/midp/io/j2me/socket/Protocol.close0.()V"] = function(ctx, stack) {
    var _this = stack.pop();

    if (_this.socket.isClosed) {
        return;
    }

    _this.socket.onclose = function() {
        _this.socket.onclose = null;
        ctx.resume();
    }

    _this.socket.close();

    throw VM.Pause;
}

Native.create("com/sun/midp/io/j2me/socket/Protocol.shutdownOutput0.()V", function(ctx) {
    // We don't have the ability to close the output stream independently
    // of the connection as a whole.  But we don't seem to have to do anything
    // here, since this has just two call sites: one in Protocol.disconnect,
    // right before closing the socket; the other in Protocol.closeOutputStream,
    // which says it will be "called once by the child output stream," although
    // I can't find an actual caller.
});

Native.create("com/sun/midp/io/j2me/socket/Protocol.notifyClosedInput0.()V", function(ctx) {
    if (this.waitingData) {
        console.warn("Protocol.notifyClosedInput0.()V unimplemented while thread is blocked on read0");
    }
});

Native.create("com/sun/midp/io/j2me/socket/Protocol.notifyClosedOutput0.()V", function(ctx) {
    if (this.socket.ondrain) {
        console.warn("Protocol.notifyClosedOutput0.()V unimplemented while thread is blocked on write0");
    }
});
