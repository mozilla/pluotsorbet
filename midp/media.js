/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var ContentTypes = {
    memory: [
    ],

    file: [
        "audio/x-wav",
        "audio/mpeg",
    ],

    http: [
        "audio/x-wav",
        "audio/mpeg",
    ],

    https: [
        "audio/x-wav",
        "audio/mpeg",
    ],

    rtp: [],

    rtsp: [],

    capture: []
};

var ListCache = {

    create: function(data) {
        var id = this._nextId;
        this._cached[id] = data;
        if (++this._nextId > 0xffff) {
            this._nextId = 0;
        }
        return id;
    },

    get: function(id) {
        return this._cached[id];
    },

    remove: function(id) {
        delete this._cached[id];
    },

    _cached: {},
    // A valid ID should be greater than 0.
    _nextId: 1
}

var extToFormat = new Map([
    ["mp3", "MPEG_layer_3"],
]);

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
        console.error("Invalid hdlr: " + hdlr);
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
        console.error("Invalid hdlr: " + hdlr);
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
    // this.mediaFormat will only be updated by PlayerImpl.nGetMediaFormat.
    this.mediaFormat = url ? this.guessFormatFromURL(url) : "UNKNOWN";
    this.contentType = "";
    this.wholeContentSize = -1;
    this.contentSize = 0;
    this.volume = -1;
    this.isMuted = false;

    /* @type {Int8Array} */
    this.data = null;

    /* @type {AudioBuffer} */
    this.audioBuffer = null;

    /* @type {AudioContext} */
    this.audioContext = null;

    /* @type {AudioBufferSourceNode} */
    this.source = null;

    /*
     * Audio gain node used to control volume.
     * @type {GainNode}
     */
    this.gainNode = null;

    this.isPlaying = false;
    this.startTime = 0;
    this.stopTime = 0;
    this.duration = 0;
}

// default buffer size 1 MB
Player.DEFAULT_BUFFER_SIZE  = 1024 * 1024;

Player.prototype.guessFormatFromURL = function() {
    return extToFormat.get(this.url.substr(this.url.lastIndexOf(".") + 1)) || "UNKNOWN";
}

Player.prototype.realize = function(contentType) {
    if (contentType) {
        switch (contentType) {
            case "audio/x-wav":
            case "audio/amr":
            case "audio/mpeg":
                this.contentType = contentType;
                break;
            default:
                console.warn("Unsupported content type: " + contentType);
                return false;
        }
    }
    this.audioContext = new AudioContext();
    if (this.isVolumeControlSupported()) {
        this.gainNode = this.audioContext.createGain();
        this.volume = Math.round(this.gainNode.gain.value * 100);
        this.gainNode.connect(this.audioContext.destination);
    }
    return true;
};

Player.prototype.close = function() {
    if (this.source) {
        this.source.stop();
        this.source.disconnect();
        this.source = null;
    }
    if (this.gainNode) {
        this.gainNode.disconnect();
        this.gainNode = null;
    }
    this.audioBuffer = null;
    this.data = null;

    this.startTime = 0;
    this.stopTime = 0;
    this.isPlaying = false;
};

/**
 * @return current time in ms.
 */
Player.prototype.getMediaTime = function() {
    if (!this.audioContext) {
        return -1;
    }
    var time = 0;
    if (this.isPlaying) {
        time = this.audioContext.currentTime - this.startTime;
    } else {
        time = Math.min(this.duration, this.stopTime - this.startTime);
    }
    return Math.round(time * 1000);
};

Player.prototype.getBufferSize = function() {
    return this.wholeContentSize === -1 ? Player.DEFAULT_BUFFER_SIZE :
                                          this.wholeContentSize;
};

Player.prototype.getMediaFormat = function() {
    if (this.url === null || this.contentSize === 0) {
        return this.mediaFormat;
    }

    var headerString = util.decodeUtf8(this.data.subarray(0, 50));

    // Refer to https://www.ffmpeg.org/doxygen/0.6/amr_8c-source.html.
    if (headerString.indexOf("#!AMR\n") === 0){
        return "amr";
    }

    // Refer to https://www.ffmpeg.org/doxygen/0.6/wav_8c-source.html
    if (headerString.indexOf("RIFF") === 0 && headerString.indexOf("WAVE") === 8) {
        return "wav";
    }

    // Refer to http://www.sonicspot.com/guide/midifiles.html
    if (headerString.indexOf("MThd") === 0) {
        return "mid";
    }

    return this.mediaFormat;
};

Player.prototype.isVolumeControlSupported = function() {
    if (this.mediaFormat !== "UNKNOWN") {
        switch (this.mediaFormat) {
            case "amr":
            case "wav":
            case "MPEG_layer_3":
                return true;
            default:
                return false;
        }
    }
    if (this.contentType) {
        switch (this.contentType) {
            case "audio/amr":
            case "audio/x-wav":
            case "audio/mpeg":
                return true;
            default:
                return false;
        }
    }
    return false;
};

Player.prototype.writeBuffer = function(buffer) {
    if (this.contentSize === 0) {
        this.data = util.newPrimitiveArray("B", this.getBufferSize());
    }

    this.data.set(buffer, this.contentSize);
    this.contentSize += buffer.length;
};

Player.prototype.play = function() {
    var offset = this.stopTime - this.startTime;
    this.source = this.audioContext.createBufferSource();
    this.source.buffer = this.cloneBuffer();
    this.source.connect(this.gainNode || this.audioContext.destination);
    this.source.start(0, offset);
    this.isPlaying = true;
    this.startTime = this.audioContext.currentTime - offset;
    this.source.onended = function() {
        this.close();
    }.bind(this);
};

Player.prototype.start = function() {
    return new Promise(function(resolve, reject) {
        if (this.contentSize > 0) {
            this.decode(this.data.subarray(0, this.contentSize), function(decoded) {
                // Save a copy of the audio buffer for resumimg or replaying.
                this.audioBuffer = decoded;
                this.duration = decoded.duration;
                this.play();
                resolve();
            }.bind(this));
            return;
        }
        console.warn("Cannot start playing.");
        resolve();
    }.bind(this));
};

Player.prototype.pause = function() {
    if (!this.isPlaying) {
        return;
    }
    this.isPlaying = false;
    this.source.onended = null;
    this.stopTime = this.audioContext.currentTime;
    this.source.stop();
    this.source.disconnect();
    this.source = null;
};

Player.prototype.resume = function() {
    if (this.isPlaying) {
        return;
    }
    if (this.stopTime - this.startTime >= this.duration) {
        return;
    }
    this.play();
};

Player.prototype.cloneBuffer = function() {
    var buffer = this.audioBuffer;
    var cloned = this.audioContext.createBuffer(
                    buffer.numberOfChannels,
                    buffer.length,
                    buffer.sampleRate
                 );

    for (var i = 0; i < buffer.numberOfChannels; ++i) {
        var channel = buffer.getChannelData(i);
        cloned.getChannelData(i).set(new Float32Array(channel));
    }
    return cloned;
};

Player.prototype.decode = function(encoded, callback) {
    this.audioContext.decodeAudioData(encoded.buffer, callback);
};

Player.prototype.getVolume = function() {
    return this.volume;
};

Player.prototype.setVolume = function(level) {
    if (!this.gainNode) {
        return -1;
    }
    if (level < 0) {
        level = 0;
    } else if (level > 100) {
        level = 100;
    }
    this.volume = level;
    if (!this.isMuted) {
        this.gainNode.gain.value = level / 100;
    }
    return level;
};

Player.prototype.getMute = function() {
    return this.isMuted;
};

Player.prototype.setMute = function(mute) {
    if (this.isMuted === mute) {
        return;
    }
    this.isMuted = mute;
    if (!this.gainNode) {
        return;
    }
    if (mute) {
        this.gainNode.gain.value = 0;
    } else {
        this.gainNode.gain.value = this.volume / 100;
    }
};

Native.create("com/sun/mmedia/PlayerImpl.nInit.(IILjava/lang/String;)I", function(appId, pId, jURI) {
    var url = util.fromJavaString(jURI);
    var id = pId + (appId << 32);
    PlayerCache[id] = new Player(url);
    return id;
});

/**
 * @return 0 - failed; 1 - succeeded.
 */
Native.create("com/sun/mmedia/PlayerImpl.nTerm.(I)I", function(handle) {
    var player = PlayerCache[handle];
    if (!player) {
        return 1;
    }
    player.close();
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
    var player = PlayerCache[handle];
    return player.realize(mime);
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

Native.create("com/sun/mmedia/MediaDownload.nSetWholeContentSize.(IJ)V", function(handle, contentSize, _) {
    var player = PlayerCache[handle];
    player.wholeContentSize = contentSize.toNumber();
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
    console.warn("com/sun/mmedia/DirectPlayer.nIsVideoControlSupported.(I)Z not implemented.");
    return false;
});

Native.create("com/sun/mmedia/DirectPlayer.nIsVolumeControlSupported.(I)Z", function(handle) {
    var player = PlayerCache[handle];
    return player.isVolumeControlSupported();
});

Native.create("com/sun/mmedia/DirectPlayer.nIsNeedBuffering.(I)Z", function(handle) {
    var player = PlayerCache[handle];
    console.warn("com/sun/mmedia/DirectPlayer.nIsNeedBuffering.(I)Z not implemented.");
    return false;
});

Native.create("com/sun/mmedia/DirectPlayer.nPcmAudioPlayback.(I)Z", function(handle) {
    var player = PlayerCache[handle];
    console.warn("com/sun/mmedia/DirectPlayer.nPcmAudioPlayback.(I)Z not implemented.");
    return false;
});

// Device is available?
Native.create("com/sun/mmedia/DirectPlayer.nAcquireDevice.(I)Z", function(handle) {
    var player = PlayerCache[handle];
    console.warn("com/sun/mmedia/DirectPlayer.nAcquireDevice.(I)Z not implemented.");
    return true;
});

// Relase device reference
Native.create("com/sun/mmedia/DirectPlayer.nReleaseDevice.(I)V", function(handle) {
    var player = PlayerCache[handle];
    console.warn("com/sun/mmedia/DirectPlayer.nReleaseDevice.(I)V not implemented.");
});

Native.create("com/sun/mmedia/DirectPlayer.nSwitchToForeground.(II)Z", function(handle, options) {
    var player = PlayerCache[handle];
    console.warn("com/sun/mmedia/DirectPlayer.nSwitchToForeground.(II)Z not implemented. ");
    return true;
});

Native.create("com/sun/mmedia/DirectPlayer.nSwitchToBackground.(II)Z", function(handle, options) {
    var player = PlayerCache[handle];
    console.warn("com/sun/mmedia/DirectPlayer.nSwitchToBackground.(II)Z not implemented. ");
    return true;
});

// Start Prefetch of Native Player
Native.create("com/sun/mmedia/DirectPlayer.nPrefetch.(I)Z", function(handle) {
    var player = PlayerCache[handle];
    console.warn("com/sun/mmedia/DirectPlayer.nPrefetch.(I)Z not implemented.");
    return true;
});

Native.create("com/sun/mmedia/DirectPlayer.nGetMediaTime.(I)I", function(handle) {
    var player = PlayerCache[handle];
    return player.getMediaTime();
});

Native.create("com/sun/mmedia/DirectPlayer.nStart.(I)Z", function(handle) {
    var player = PlayerCache[handle];
    player.start();
    return true;
});

Native.create("com/sun/mmedia/DirectPlayer.nStop.(I)Z", function(handle) {
    var player = PlayerCache[handle];
    player.close();
    return true;
});

Native.create("com/sun/mmedia/DirectPlayer.nTerm.(I)I", function(handle) {
    var player = PlayerCache[handle];
    player.close();
    delete PlayerCache[handle];
    return 1;
});

Native.create("com/sun/mmedia/DirectPlayer.nPause.(I)Z", function(handle) {
    var player = PlayerCache[handle];
    player.pause();
    return true;
});

Native.create("com/sun/mmedia/DirectPlayer.nResume.(I)Z", function(handle) {
    var player = PlayerCache[handle];
    player.resume();
    return true;
});

/**
 * @return the volume level between 0 and 100 if succeeded. Otherwise -1.
 */
Native.create("com/sun/mmedia/DirectVolume.nGetVolume.(I)I", function(handle) {
    var player = PlayerCache[handle];
    return player.getVolume();
});

/**
 * @param level The volume level between 0 and 100.
 * @return the volume level set between 0 and 100 if succeeded. Otherwise -1.
 */
Native.create("com/sun/mmedia/DirectVolume.nSetVolume.(II)I", function(handle, level) {
    var player = PlayerCache[handle];
    return player.setVolume(level);
});

Native.create("com/sun/mmedia/DirectVolume.nIsMuted.(I)Z", function(handle) {
    var player = PlayerCache[handle];
    return player.getMute();
});

Native.create("com/sun/mmedia/DirectVolume.nSetMute.(IZ)Z", function(handle, mute) {
    var player = PlayerCache[handle];
    player.setMute(mute);
    return true;
});

Native.create("com/sun/mmedia/NativeTonePlayer.nPlayTone.(IIII)Z", function(appId, note, duration, volume) {
    console.warn("com/sun/mmedia/NativeTonePlayer.nPlayTone.(IIII)Z not implemented.");
    return true;
});

Native.create("com/sun/mmedia/NativeTonePlayer.nStopTone.(I)Z", function(appId) {
    console.warn("com/sun/mmedia/NativeTonePlayer.nStopTone.(I)Z not implemented.");
    return true;
});
