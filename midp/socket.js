/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

const SOCKET_OPT = {
  DELAY: 0,
  LINGER: 1,
  KEEPALIVE: 2,
  RCVBUF: 3,
  SNDBUF: 4,
};

Native["com/sun/midp/io/j2me/socket/Protocol.getIpNumber0.(Ljava/lang/String;[B)I"] = function(ctx, stack) {
    var ipBytes = stack.pop(), host = stack.pop(), _this = stack.pop();
    // We'd need to modify ipBytes, that is an array with length 0
    // But we don't really need to do that, because getIpNumber0 is called only
    // before open0. So we just need to store the host and pass it to
    // mozTCPSocket::open.
    _this.host = util.fromJavaString(host);
    stack.push(0);
}

Native["com/sun/midp/io/j2me/socket/Protocol.getHost0.(Z)Ljava/lang/String;"] = function(ctx, stack) {
    var local = stack.pop(), _this = stack.pop();
    stack.push(ctx.newString((local) ? "127.0.0.1" : _this.socket.host));
}

Native["com/sun/midp/io/j2me/socket/Protocol.open0.([BI)V"] = function(ctx, stack) {
    var port = stack.pop(), ipBytes = stack.pop(), _this = stack.pop();

    try {
        _this.socket = navigator.mozTCPSocket.open(_this.host, port, { binaryType: "arraybuffer" });
    } catch (ex) {
        ctx.raiseExceptionAndYield("java/io/IOException");
    }

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

    _this.socket.onerror = function(event) {
        ctx.raiseException("java/io/IOException", event.data.name);
        ctx.resume();
    }

    _this.socket.ondata = function(event) {
        var receivedData = new Uint8Array(event.data);
        var newArray = new Uint8Array(_this.data.byteLength + receivedData.byteLength);
        newArray.set(_this.data);
        newArray.set(receivedData, _this.data.byteLength);
        _this.data = newArray;

        if (_this.waitingData) {
            _this.waitingData();
        }
    }

    throw VM.Pause;
}

Native["com/sun/midp/io/j2me/socket/Protocol.available0.()I"] = function(ctx, stack) {
    var _this = stack.pop();
    stack.push(_this.data.byteLength);
}

Native["com/sun/midp/io/j2me/socket/Protocol.read0.([BII)I"] = function(ctx, stack) {
    var length = stack.pop(), offset = stack.pop(), data = stack.pop(), _this = stack.pop();

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

    if (!_this.socket.send(data.buffer, offset, length)) {
        _this.socket.ondrain = function() {
            _this.socket.ondrain = null;
            stack.push(length);
            ctx.resume();
        };

        throw VM.Pause;
    }

    stack.push(length);
}

Native["com/sun/midp/io/j2me/socket/Protocol.setSockOpt0.(II)V"] = function(ctx, stack) {
    var value = stack.pop(), option = stack.pop(), _this = stack.pop();

    if (!(option in _this.options)) {
        ctx.raiseException("java/lang/IllegalArgumentException", "Unsupported socket option");
    }

    _this.options[option] = value;
}

Native["com/sun/midp/io/j2me/socket/Protocol.getSockOpt0.(I)I"] = function(ctx, stack) {
    var option = stack.pop(), _this = stack.pop();

    if (!(option in _this.options)) {
        ctx.raiseException("java/lang/IllegalArgumentException", "Unsupported socket option");
    }

    stack.push(_this.options[option]);
}

Native["com/sun/midp/io/j2me/socket/Protocol.close0.()V"] = function(ctx, stack) {
    var _this = stack.pop();

    if (_this.socket.readyState == "closed") {
        return;
    }

    _this.socket.onclose = function() {
        _this.socket.onclose = null;
        ctx.resume();
    }

    // If it's already closing, we don't need to close it, we just need to wait
    // for it to close; otherwise, we need to close it.
    if (_this.socket.readyState != "closing") {
        _this.socket.close();
    }

    throw VM.Pause;
}

Native["com/sun/midp/io/j2me/socket/Protocol.shutdownOutput0.()V"] = function(ctx, stack) {
    var _this = stack.pop();

    // We don't have the ability to close the output stream independently
    // of the connection as a whole.  But we don't seem to have to do anything
    // here, since this has just two call sites: one in Protocol.disconnect,
    // right before closing the socket; the other in Protocol.closeOutputStream,
    // which says it will be "called once by the child output stream," although
    // I can't find an actual caller.
}

Native["com/sun/midp/io/j2me/socket/Protocol.notifyClosedInput0.()V"] = function(ctx, stack) {
    var _this = stack.pop();

    if (_this.waitingData) {
        _this.waitingData();
    }
}

Native["com/sun/midp/io/j2me/socket/Protocol.notifyClosedOutput0.()V"] = function(ctx, stack) {
    var _this = stack.pop();

    // We should flush the output stream, but mozTCPSocket doesn't appear
    // to support that, so the best we can do is resume the thread by calling
    // the ondrain handler.

    if (_this.socket.ondrain) {
        _this.socket.ondrain();
    }
}
