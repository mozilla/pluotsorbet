/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var Media = {};

Media.ContentTypes = {
    memory: [
    ],

    file: [
        "audio/x-wav",
        "audio/mpeg",
        "image/jpeg",
        "image/png",
    ],

    http: [
        "audio/x-wav",
        "audio/mpeg",
        "image/jpeg",
        "image/png",
    ],

    https: [
        "audio/x-wav",
        "audio/mpeg",
        "image/jpeg",
        "image/png",
    ],

    rtp: [],

    rtsp: [],

    capture: []
};

Media.ListCache = {
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

Media.extToFormat = new Map([
    ["mp3", "MPEG_layer_3"],
    ["jpg", "JPEG"],
    ["jpeg", "JPEG"],
    ["png", "PNG"],
]);

Media.contentTypeToFormat = new Map([
    ["audio/amr", "amr"],
    ["audio/x-wav", "wav"],
    ["audio/mpeg", "MPEG_layer_3"],
    ["image/jpeg", "JPEG"],
    ["image/png", "PNG"],
]);

Media.supportedAudioFormats = ["MPEG_layer_3", "wav", "amr"];
Media.supportedImageFormats = ["JPEG", "PNG"];

Native.create("com/sun/mmedia/DefaultConfiguration.nListContentTypesOpen.(Ljava/lang/String;)I", function(jProtocol) {
    var protocol = util.fromJavaString(jProtocol);
    var types = [];
    if (protocol) {
        types = Media.ContentTypes[protocol];
        if (!types) {
            console.warn("Unknown protocol type: " + protocol);
            return 0;
        }
    } else {
        for (var p in Media.ContentTypes) {
            Media.ContentTypes[p].forEach(function(type) {
                if (types.indexOf(type) === -1) {
                    types.push(type);
                }
            });
        }
    }
    if (types.length == 0) {
        return 0;
    }
    return Media.ListCache.create(types);
});

Native.create("com/sun/mmedia/DefaultConfiguration.nListContentTypesNext.(I)Ljava/lang/String;", function(hdlr) {
    var cached = Media.ListCache.get(hdlr);
    if (!cached) {
        console.error("Invalid hdlr: " + hdlr);
        return null;
    }
    return cached.shift() || null;
});

Native.create("com/sun/mmedia/DefaultConfiguration.nListContentTypesClose.(I)V", function(hdlr) {
    Media.ListCache.remove(hdlr);
});

Native.create("com/sun/mmedia/DefaultConfiguration.nListProtocolsOpen.(Ljava/lang/String;)I", function(jMime) {
    var mime = util.fromJavaString(jMime);
    var protocols = [];
    for (var protocol in Media.ContentTypes) {
        if (!mime || Media.ContentTypes[protocol].indexOf(mime) >= 0) {
            protocols.push(protocol);
        }
    }
    if (!protocols.length) {
        return 0;
    }
    return Media.ListCache.create(protocols);
});

Native.create("com/sun/mmedia/DefaultConfiguration.nListProtocolsNext.(I)Ljava/lang/String;", function(hdlr) {
    var cached = Media.ListCache.get(hdlr);
    if (!cached) {
        console.error("Invalid hdlr: " + hdlr);
        return null;
    }
    return cached.shift() || null;
});

Native.create("com/sun/mmedia/DefaultConfiguration.nListProtocolsClose.(I)V", function(hdlr) {
    Media.ListCache.remove(hdlr);
});

Media.PlayerCache = {
};

function AudioPlayer(playerContainer) {
    this.playerContainer = playerContainer;

    this.isMuted = false;

    /* @type {AudioBuffer} */
    this.audioBuffer = null;

    this.audioContext = new AudioContext();

    /* @type {AudioBufferSourceNode} */
    this.source = null;

    /*
     * Audio gain node used to control volume.
     */
    this.gainNode = this.audioContext.createGain();

    this.volume = Math.round(this.gainNode.gain.value * 100);

    this.gainNode.connect(this.audioContext.destination);

    this.isPlaying = false;
    this.startTime = 0;
    this.stopTime = 0;
    this.duration = 0;

    this.isVideoControlSupported = false;
    this.isVolumeControlSupported = true;
}

AudioPlayer.prototype.realize = function() {
    return new Promise(function(resolve, reject) { resolve(true); });
}

AudioPlayer.prototype.play = function() {
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
}

AudioPlayer.prototype.start = function() {
    if (this.playerContainer.contentSize > 0) {
        this.decode(this.playerContainer.data.subarray(0, this.playerContainer.contentSize), function(decoded) {
            // Save a copy of the audio buffer for resuming or replaying.
            this.audioBuffer = decoded;
            this.duration = decoded.duration;
            this.play();
        }.bind(this));

        return;
    }

    console.warn("Cannot start playing.");
}

AudioPlayer.prototype.pause = function() {
    if (!this.isPlaying) {
        return;
    }

    this.isPlaying = false;
    this.source.onended = null;
    this.stopTime = this.audioContext.currentTime;
    this.source.stop();
    this.source.disconnect();
    this.source = null;
}

AudioPlayer.prototype.resume = function() {
    if (this.isPlaying) {
        return;
    }

    if (this.stopTime - this.startTime >= this.duration) {
        return;
    }

    this.play();
}

AudioPlayer.prototype.close = function() {
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

    this.startTime = 0;
    this.stopTime = 0;
    this.isPlaying = false;
}

AudioPlayer.prototype.getMediaTime = function() {
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
}

AudioPlayer.prototype.cloneBuffer = function() {
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

AudioPlayer.prototype.decode = function(encoded, callback) {
    this.audioContext.decodeAudioData(encoded.buffer, callback);
};

AudioPlayer.prototype.getVolume = function() {
    return this.volume;
};

AudioPlayer.prototype.setVolume = function(level) {
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
}

AudioPlayer.prototype.getMute = function() {
    return this.isMuted;
}

AudioPlayer.prototype.setMute = function(mute) {
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
}

function ImagePlayer(playerContainer) {
    this.url = playerContainer.url;

    this.image = new Image();
    this.image.style.position = "absolute";
    this.image.style.visibility = "hidden";

    this.isVideoControlSupported = true;
    this.isAudioControlSupported = false;
}

ImagePlayer.prototype.realize = function() {
    return new Promise((function(resolve, reject) {
        if (this.url.startsWith("file")) {
            fs.open(this.url.substring(7), (function(fd) {
                var imgData = fs.read(fd);
                fs.close(fd);

                this.image.src = URL.createObjectURL(new Blob([ imgData ]));
                resolve(true);
            }).bind(this));
        } else {
            this.image.src = this.url;
            resolve(true);
        }
    }).bind(this));
}

ImagePlayer.prototype.start = function() {
}

ImagePlayer.prototype.pause = function() {
}

ImagePlayer.prototype.close = function() {
    if (this.image.parentNode) {
        document.getElementById("display").removeChild(this.image);
    }
}

ImagePlayer.prototype.getMediaTime = function() {
    return -1;
}

ImagePlayer.prototype.getWidth = function() {
    return this.image.naturalWidth;
}

ImagePlayer.prototype.getHeight = function() {
    return this.image.naturalHeight;
}

ImagePlayer.prototype.setLocation = function(x, y, w, h) {
    this.image.style.left = x + "px";
    this.image.style.top = y + "px";
    this.image.style.width = w + "px";
    this.image.style.height = h + "px";
    document.getElementById("display").appendChild(this.image);
}

ImagePlayer.prototype.setVisible = function(visible) {
    this.image.style.visibility = visible ? "visible" : "hidden";
}

function PlayerContainer(url) {
    this.url = url;

    this.mediaFormat = url ? this.guessFormatFromURL(url) : "UNKNOWN";
    this.contentType = "";

    this.wholeContentSize = -1;
    this.contentSize = 0;
    this.data = null;

    this.player = null;
}

// default buffer size 1 MB
PlayerContainer.DEFAULT_BUFFER_SIZE  = 1024 * 1024;

PlayerContainer.prototype.guessFormatFromURL = function() {
    return Media.extToFormat.get(this.url.substr(this.url.lastIndexOf(".") + 1)) || "UNKNOWN";
}

PlayerContainer.prototype.realize = function(contentType) {
    return new Promise((function(resolve, reject) {
        if (contentType) {
            this.contentType = contentType;
            this.mediaFormat = Media.contentTypeToFormat.get(contentType) || this.mediaFormat;
            if (this.mediaFormat === "UNKNOWN") {
                console.warn("Unsupported content type: " + contentType);
                resolve(false);
                return;
            }
        }

        if (Media.supportedAudioFormats.indexOf(this.mediaFormat) !== -1) {
            this.player = new AudioPlayer(this);
            this.player.realize().then(resolve);
        } else if (Media.supportedImageFormats.indexOf(this.mediaFormat) !== -1) {
            this.player = new ImagePlayer(this);
            this.player.realize().then(resolve);
        } else {
            console.warn("Unsupported media format: " + this.mediaFormat);
            resolve(false);
        }
    }).bind(this));
};

PlayerContainer.prototype.close = function() {
    this.data = null;
    if (this.player) {
        this.player.close();
    }
};

/**
 * @return current time in ms.
 */
PlayerContainer.prototype.getMediaTime = function() {
    return this.player.getMediaTime();
};

PlayerContainer.prototype.getBufferSize = function() {
    return this.wholeContentSize === -1 ? PlayerContainer.DEFAULT_BUFFER_SIZE :
                                          this.wholeContentSize;
};

PlayerContainer.prototype.getMediaFormat = function() {
    if (this.contentSize === 0) {
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

PlayerContainer.prototype.getContentType = function() {
    return this.contentType;
};

PlayerContainer.prototype.isHandledByDevice = function() {
    // TODO: Handle download in JS also for audio formats
    return this.url !== null && Media.supportedAudioFormats.indexOf(this.mediaFormat) === -1;
};

PlayerContainer.prototype.isVideoControlSupported = function() {
    return this.player.isVideoControlSupported;
};

PlayerContainer.prototype.isVolumeControlSupported = function() {
    return this.player.isVolumeControlSupported;
};

PlayerContainer.prototype.writeBuffer = function(buffer) {
    if (this.contentSize === 0) {
        this.data = util.newPrimitiveArray("B", this.getBufferSize());
    }

    this.data.set(buffer, this.contentSize);
    this.contentSize += buffer.length;
};

PlayerContainer.prototype.play = function() {
    this.player.play();
};

PlayerContainer.prototype.start = function() {
    this.player.start();
};

PlayerContainer.prototype.pause = function() {
    this.player.pause();
};

PlayerContainer.prototype.resume = function() {
    this.player.resume();
};

PlayerContainer.prototype.getVolume = function() {
    return this.player.getVolume();
};

PlayerContainer.prototype.setVolume = function(level) {
    this.player.setVolume(level);
};

PlayerContainer.prototype.getMute = function() {
    return this.player.getMute();
};

PlayerContainer.prototype.setMute = function(mute) {
    return this.player.setMute(mute);
};

PlayerContainer.prototype.getWidth = function() {
    return this.player.getWidth();
}

PlayerContainer.prototype.getHeight = function() {
    return this.player.getHeight();
}

PlayerContainer.prototype.setLocation = function(x, y, w, h) {
    this.player.setLocation(x, y, w, h);
}

PlayerContainer.prototype.setVisible = function(visible) {
    this.player.setVisible(visible);
}

Native.create("com/sun/mmedia/PlayerImpl.nInit.(IILjava/lang/String;)I", function(appId, pId, jURI) {
    var url = util.fromJavaString(jURI);
    var id = pId + (appId << 32);
    Media.PlayerCache[id] = new PlayerContainer(url);
    return id;
});

/**
 * @return 0 - failed; 1 - succeeded.
 */
Native.create("com/sun/mmedia/PlayerImpl.nTerm.(I)I", function(handle) {
    var player = Media.PlayerCache[handle];
    if (!player) {
        return 1;
    }
    player.close();
    delete Media.PlayerCache[handle];
    return 1;
});

Native.create("com/sun/mmedia/PlayerImpl.nGetMediaFormat.(I)Ljava/lang/String;", function(handle) {
    var player = Media.PlayerCache[handle];
    player.mediaFormat = player.getMediaFormat();
    return player.mediaFormat;
});

Native.create("com/sun/mmedia/DirectPlayer.nGetContentType.(I)Ljava/lang/String;", function(handle) {
    return Media.PlayerCache[handle].getContentType();
});

Native.create("com/sun/mmedia/PlayerImpl.nIsHandledByDevice.(I)Z", function(handle) {
    return Media.PlayerCache[handle].isHandledByDevice();
});

Native.create("com/sun/mmedia/PlayerImpl.nRealize.(ILjava/lang/String;)Z", function(handle, jMime) {
    var mime = util.fromJavaString(jMime);
    var player = Media.PlayerCache[handle];
    return player.realize(mime);
}, true);


Native.create("com/sun/mmedia/MediaDownload.nGetJavaBufferSize.(I)I", function(handle) {
    var player = Media.PlayerCache[handle];
    return player.getBufferSize();
});

Native.create("com/sun/mmedia/MediaDownload.nGetFirstPacketSize.(I)I", function(handle) {
    var player = Media.PlayerCache[handle];
    return player.getBufferSize() / 2;
});

Native.create("com/sun/mmedia/MediaDownload.nBuffering.(I[BII)I", function(handle, buffer, offset, size) {
    var player = Media.PlayerCache[handle];
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
    console.warn("com/sun/mmedia/MediaDownload.nNeedMoreDataImmediatelly.(I)Z not implemented");
    return true;
});

Native.create("com/sun/mmedia/MediaDownload.nSetWholeContentSize.(IJ)V", function(handle, contentSize, _) {
    var player = Media.PlayerCache[handle];
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
    return Media.PlayerCache[handle].isVideoControlSupported();
});

Native.create("com/sun/mmedia/DirectPlayer.nIsVolumeControlSupported.(I)Z", function(handle) {
    var player = Media.PlayerCache[handle];
    return player.isVolumeControlSupported();
});

Native.create("com/sun/mmedia/DirectPlayer.nIsNeedBuffering.(I)Z", function(handle) {
    var player = Media.PlayerCache[handle];
    console.warn("com/sun/mmedia/DirectPlayer.nIsNeedBuffering.(I)Z not implemented.");
    return false;
});

Native.create("com/sun/mmedia/DirectPlayer.nPcmAudioPlayback.(I)Z", function(handle) {
    var player = Media.PlayerCache[handle];
    console.warn("com/sun/mmedia/DirectPlayer.nPcmAudioPlayback.(I)Z not implemented.");
    return false;
});

// Device is available?
Native.create("com/sun/mmedia/DirectPlayer.nAcquireDevice.(I)Z", function(handle) {
    var player = Media.PlayerCache[handle];
    console.warn("com/sun/mmedia/DirectPlayer.nAcquireDevice.(I)Z not implemented.");
    return true;
});

// Relase device reference
Native.create("com/sun/mmedia/DirectPlayer.nReleaseDevice.(I)V", function(handle) {
    var player = Media.PlayerCache[handle];
    console.warn("com/sun/mmedia/DirectPlayer.nReleaseDevice.(I)V not implemented.");
});

Native.create("com/sun/mmedia/DirectPlayer.nSwitchToForeground.(II)Z", function(handle, options) {
    var player = Media.PlayerCache[handle];
    console.warn("com/sun/mmedia/DirectPlayer.nSwitchToForeground.(II)Z not implemented. ");
    return true;
});

Native.create("com/sun/mmedia/DirectPlayer.nSwitchToBackground.(II)Z", function(handle, options) {
    var player = Media.PlayerCache[handle];
    console.warn("com/sun/mmedia/DirectPlayer.nSwitchToBackground.(II)Z not implemented. ");
    return true;
});

// Start Prefetch of Native Player
Native.create("com/sun/mmedia/DirectPlayer.nPrefetch.(I)Z", function(handle) {
    var player = Media.PlayerCache[handle];
    console.warn("com/sun/mmedia/DirectPlayer.nPrefetch.(I)Z not implemented.");
    return true;
});

Native.create("com/sun/mmedia/DirectPlayer.nGetMediaTime.(I)I", function(handle) {
    var player = Media.PlayerCache[handle];
    return player.getMediaTime();
});

Native.create("com/sun/mmedia/DirectPlayer.nStart.(I)Z", function(handle) {
    var player = Media.PlayerCache[handle];
    player.start();
    return true;
});

Native.create("com/sun/mmedia/DirectPlayer.nStop.(I)Z", function(handle) {
    var player = Media.PlayerCache[handle];
    player.close();
    return true;
});

Native.create("com/sun/mmedia/DirectPlayer.nTerm.(I)I", function(handle) {
    var player = Media.PlayerCache[handle];
    player.close();
    delete Media.PlayerCache[handle];
    return 1;
});

Native.create("com/sun/mmedia/DirectPlayer.nPause.(I)Z", function(handle) {
    var player = Media.PlayerCache[handle];
    player.pause();
    return true;
});

Native.create("com/sun/mmedia/DirectPlayer.nResume.(I)Z", function(handle) {
    var player = Media.PlayerCache[handle];
    player.resume();
    return true;
});

Native.create("com/sun/mmedia/DirectPlayer.nGetWidth.(I)I", function(handle) {
    return Media.PlayerCache[handle].getWidth();
});

Native.create("com/sun/mmedia/DirectPlayer.nGetHeight.(I)I", function(handle) {
    return Media.PlayerCache[handle].getHeight();
});

Native.create("com/sun/mmedia/DirectPlayer.nSetLocation.(IIIII)Z", function(handle, x, y, w, h) {
    Media.PlayerCache[handle].setLocation(x, y, w, h);
    return true;
});

Native.create("com/sun/mmedia/DirectPlayer.nSetVisible.(IZ)Z", function(handle, visible) {
    Media.PlayerCache[handle].setVisible(visible);
    return true;
});

/**
 * @return the volume level between 0 and 100 if succeeded. Otherwise -1.
 */
Native.create("com/sun/mmedia/DirectVolume.nGetVolume.(I)I", function(handle) {
    var player = Media.PlayerCache[handle];
    return player.getVolume();
});

/**
 * @param level The volume level between 0 and 100.
 * @return the volume level set between 0 and 100 if succeeded. Otherwise -1.
 */
Native.create("com/sun/mmedia/DirectVolume.nSetVolume.(II)I", function(handle, level) {
    var player = Media.PlayerCache[handle];
    return player.setVolume(level);
});

Native.create("com/sun/mmedia/DirectVolume.nIsMuted.(I)Z", function(handle) {
    var player = Media.PlayerCache[handle];
    return player.getMute();
});

Native.create("com/sun/mmedia/DirectVolume.nSetMute.(IZ)Z", function(handle, mute) {
    var player = Media.PlayerCache[handle];
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
