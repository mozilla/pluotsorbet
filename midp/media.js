/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

const ContentTypes = {
    memory: [
        "audio/x-wav"
    ],

    file: [
        "audio/x-wav"
    ],

    http: [
        "audio/x-wav"
    ],

    https: [
        "audio/x-wav"
    ],

    rtp: [],

    rtsp: [],

    capture: []
};

var ListCache = {

    create: function(data) {
        var id = this._nextId;
        this._cached[id] = data;
        // Find next valid id.
        while (this._cached[++this._nextId]);
        return id;
    },

    get: function(id) {
        return this._cached[id];
    },

    remove: function(id) {
        this._cached[id] = null;
        this._nextId = id;
    },

    _cached: [],
    // A valid ID should be greater than 0.
    _nextId: 1
}

Native.create("com/sun/mmedia/DefaultConfiguration.nListContentTypesOpen.(Ljava/lang/String;)I", function(jProtocol) {
    var protocol = util.fromJavaString(jProtocol);
    var types = [];
    if (protocol) {
        types = ContentTypes[protocol];
        if (!types) {
            console.warn("Unknown protocol type: " + protocol);
            return 0;
        }
    } else {
        for (var p in ContentTypes) {
            ContentTypes[p].forEach(function(type) {
                if (types.indexOf(type) === -1) {
                    types.push(type);
                }
            })
        }
    }
    if (types.length == 0) {
        return 0;
    }
    return ListCache.create(types);
});

Native.create("com/sun/mmedia/DefaultConfiguration.nListContentTypesNext.(I)Ljava/lang/String;", function(hdlr) {
    var cached = ListCache.get(hdlr);
    if (!cached) {
        console.error("Invaid hdlr: " + hdlr);
        return null;
    }
    return cached.shift() || null;
});

Native.create("com/sun/mmedia/DefaultConfiguration.nListContentTypesClose.(I)V", function(hdlr) {
    ListCache.remove(hdlr);
});

Native.create("com/sun/mmedia/DefaultConfiguration.nListProtocolsOpen.(Ljava/lang/String;)I", function(jMime) {
    var mime = util.fromJavaString(jMime);
    var protocols = [];
    for (var protocol in ContentTypes) {
        if (!mime || ContentTypes[protocol].indexOf(mime) >= 0) {
            protocols.push(protocol);
        }
    }
    if (!protocols.length) {
        return 0;
    }
    return ListCache.create(protocols);
});

Native.create("com/sun/mmedia/DefaultConfiguration.nListProtocolsNext.(I)Ljava/lang/String;", function(hdlr) {
    var cached = ListCache.get(hdlr);
    if (!cached) {
        console.error("Invaid hdlr: " + hdlr);
        return null;
    }
    return cached.shift() || null;
});

Native.create("com/sun/mmedia/DefaultConfiguration.nListProtocolsClose.(I)V", function(hdlr) {
    ListCache.remove(hdlr);
});

var PlayerCache = {
};

function Player(url) {
    this.url = url;
    this.mediaFormat = "UNKNOWN";
    this.wholeContentSize = -1;
    this.contentSize = 0;
    this.data = null;
}

// default buffer size 1 MB
Player.DEFAULT_BUFFER_SIZE  = 1024 * 1024;

Player.prototype.getBufferSize = function() {
    return this.wholeContentSize === -1 ? Player.DEFAULT_BUFFER_SIZE :
                                          this.wholeContentSize;
};

Player.prototype.getMediaFormat = function() {
    if (this.url === null && this.contentSize === 0) {
        return "UNKNOWN";
    }

    var headerString = util.decodeUtf8(this.data.subarray(0, 50));

    // Refer to https://www.ffmpeg.org/doxygen/0.6/amr_8c-source.html.
    if (headerString.indexOf("#!AMR\n") === 0){
        return "audio/amr";
    }

    // Refer to https://www.ffmpeg.org/doxygen/0.6/wav_8c-source.html
    if (headerString.indexOf("RIFF") === 0 && headerString.indexOf("WAVE") === 8) {
        return "audio/x-wav";
    }

    // Refer to http://www.sonicspot.com/guide/midifiles.html
    if (headerString.indexOf("MThd") === 0) {
        return "audio/midi";
    }

    return "UNKNOWN";
};

Player.prototype.writeBuffer = function(buffer) {
    if (this.contentSize === 0) {
        this.data = util.newPrimitiveArray("B", this.getBufferSize());
    }

    this.data.set(buffer, this.contentSize);
    this.contentSize += buffer.length;
};

Native.create("com/sun/mmedia/PlayerImpl.nInit.(IILjava/lang/String;)I", function(appId, pId, jURI) {
    var url = util.fromJavaString(jURI);
    console.log("PlayerImpl.nInit(" + [appId, pId, url].join() + ")");
    var id = pId + (appId << 32);
    PlayerCache[id] = new Player(url);
    return id;
});

/**
 * @return 0 - failed; 1 - succeeded.
 */
Native.create("com/sun/mmedia/PlayerImpl.nTerm.(I)I", function(handle) {
    delete PlayerCache[handle];
    return 1;
});

Native.create("com/sun/mmedia/PlayerImpl.nGetMediaFormat.(I)Ljava/lang/String;", function(handle) {
    var player = PlayerCache[handle];
    player.mediaFormat = player.getMediaFormat();
    return player.mediaFormat;
});

Native.create("com/sun/mmedia/PlayerImpl.nIsHandledByDevice.(I)Z", function(handle) {
    console.warn("com/sun/mmedia/PlayerImpl.nIsHandledByDevice.(I)Z not implemented");
    return false;
});

Native.create("com/sun/mmedia/PlayerImpl.nRealize.(ILjava/lang/String;)Z", function(handle, jMime) {
    var mime = util.fromJavaString(jMime);
    console.warn("com/sun/mmedia/PlayerImpl.nRealize.(ILjava/lang/String;)Z not implemented");
    return true;
});


Native.create("com/sun/mmedia/MediaDownload.nGetJavaBufferSize.(I)I", function(handle) {
    var player = PlayerCache[handle];
    return player.getBufferSize();
});

Native.create("com/sun/mmedia/MediaDownload.nGetFirstPacketSize.(I)I", function(handle) {
    var player = PlayerCache[handle];
    return player.getBufferSize() / 2;
});

Native.create("com/sun/mmedia/MediaDownload.nBuffering.(I[BII)I", function(handle, buffer, offset, size) {
    var player = PlayerCache[handle];
    var bufferSize = player.getBufferSize();

    // Check the parameters.
    if (buffer === null || size === 0) {
        return bufferSize / 2;
    }

    player.writeBuffer(buffer.subarray(offset, offset + size));

    // Returns the package size and set it to the half of the java buffer size.
    return bufferSize / 2;
});

Native.create("com/sun/mmedia/MediaDownload.nNeedMoreDataImmediatelly.(I)Z", function(handle) {
    console.error("com/sun/mmedia/MediaDownload.nNeedMoreDataImmediatelly.(I)Z not implemented");
    return true;
});

Native.create("com/sun/mmedia/MediaDownload.nSetWholeContentSize.(IJ)V", function(handle, contentSize) {
    var player = PlayerCache[handle];
    player.wholeContentSize = contentSize;
});

Native.create("com/sun/mmedia/DirectPlayer.nIsToneControlSupported.(I)Z", function(handle) {
    console.info("To support ToneControl, implement com.sun.mmedia.DirectTone.");
    return false;
});

Native.create("com/sun/mmedia/DirectPlayer.nIsMIDIControlSupported.(I)Z", function(handle) {
    console.info("To support MIDIControl, implement com.sun.mmedia.DirectMIDI.");
    return false;
});

Native.create("com/sun/mmedia/DirectPlayer.nIsVideoControlSupported.(I)Z", function(handle) {
    var player = PlayerCache[handle];
    // TODO Is there any other types supporting video control?
    if (player.mediaFormat.indexOf("video/") === 0) {
        return true;
    }
    return false;
});

Native.create("com/sun/mmedia/NativeTonePlayer.nPlayTone.(IIII)Z", function(appId, note, duration, volume) {
    console.warn("com/sun/mmedia/NativeTonePlayer.nPlayTone.(IIII)Z not implemented.");
    return true;
});

Native.create("com/sun/mmedia/NativeTonePlayer.nStopTone.(I)Z", function(appId) {
    console.warn("com/sun/mmedia/NativeTonePlayer.nStopTone.(I)Z not implemented.");
    return true;
});
