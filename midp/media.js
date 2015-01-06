/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var Media = {};

Media.ContentTypes = {
    memory: [
    ],

    file: [
        "audio/ogg",
        "audio/x-wav",
        "audio/mpeg",
        "image/jpeg",
        "image/png",
        "audio/amr"
    ],

    http: [
        "audio/x-wav",
        "audio/mpeg",
        "image/jpeg",
        "image/png",
        "audio/amr"
    ],

    https: [
        "audio/x-wav",
        "audio/mpeg",
        "image/jpeg",
        "image/png",
        "audio/amr"
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
    ["audio/ogg", "ogg"],
    ["audio/amr", "amr"],
    ["audio/x-wav", "wav"],
    ["audio/mpeg", "MPEG_layer_3"],
    ["image/jpeg", "JPEG"],
    ["image/png", "PNG"],
]);

Media.supportedAudioFormats = ["MPEG_layer_3", "wav", "amr", "ogg"];
Media.supportedImageFormats = ["JPEG", "PNG"];

Media.EVENT_MEDIA_END_OF_MEDIA = 1;
Media.EVENT_MEDIA_SNAPSHOT_FINISHED = 11;

Media.convert3gpToAmr = function(inBuffer) {
    // The buffer to store the converted amr file.
    var outBuffer = new Uint8Array(inBuffer.length);

    // Add AMR header.
    var AMR_HEADER = "#!AMR\n";
    outBuffer.set(new TextEncoder("utf-8").encode(AMR_HEADER));
    var outOffset = AMR_HEADER.length;

    var textDecoder = new TextDecoder("utf-8");
    var inOffset = 0;
    while (inOffset + 8 < inBuffer.length) {
        // Get the box size
        var size = 0;
        for (var i = 0; i < 4; i++) {
            size = inBuffer[inOffset + i] + (size << 8);
        }
        // Search the box of type mdat.
        var type = textDecoder.decode(inBuffer.subarray(inOffset + 4, inOffset + 8));
        if (type === "mdat" && inOffset + size <= inBuffer.length) {
            // Extract raw AMR data from the box and append to the out buffer.
            var data = inBuffer.subarray(inOffset + 8, inOffset + size);
            outBuffer.set(data, outOffset);
            outOffset += data.length;
        }
        inOffset += size;
    }

    if (outOffset === AMR_HEADER.length) {
        console.warn("Failed to extract AMR from 3GP file.");
    }
    return outBuffer.subarray(0, outOffset);
};

Native.create("com/sun/mmedia/DefaultConfiguration.nListContentTypesOpen.(Ljava/lang/String;)I", function(jProtocol) {
    var protocol = util.fromJavaString(jProtocol);
    var types = [];
    if (protocol) {
        types = Media.ContentTypes[protocol].slice();
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

    /* @type HTMLAudioElement */
    this.audio = new Audio();

    this.isVideoControlSupported = false;
    this.isVolumeControlSupported = true;
}

AudioPlayer.prototype.realize = function() {
    return Promise.resolve(true);
};

AudioPlayer.prototype.play = function() {
    this.audio.play();
    this.audio.onended = function() {
        MIDP.sendNativeEvent({
            type: MIDP.MMAPI_EVENT,
            intParam1: this.playerContainer.pId,
            intParam2: this.getDuration(),
            intParam3: 0,
            intParam4: Media.EVENT_MEDIA_END_OF_MEDIA
        }, MIDP.foregroundIsolateId);
    }.bind(this);
};

AudioPlayer.prototype.start = function() {
    if (this.playerContainer.contentSize == 0) {
        console.warn("Cannot start playing.");
        return;
    }

    if (this.audio.src) {
        this.play();
        return;
    }

    new Promise(function(resolve, reject) {
        var blob = new Blob([ this.playerContainer.data.subarray(0, this.playerContainer.contentSize) ],
                            { type: this.playerContainer.contentType });
        this.audio.src = URL.createObjectURL(blob);
        this.audio.onloadedmetadata = function() {
            resolve();
            this.play();
        }.bind(this);
        this.audio.onerror = reject;
    }.bind(this)).done(function() {
        URL.revokeObjectURL(this.audio.src);
    }.bind(this));
};

AudioPlayer.prototype.pause = function() {
    if (this.audio.paused) {
        return;
    }
    this.audio.onended = null;
    this.audio.pause();
};

AudioPlayer.prototype.resume = function() {
    if (!this.audio.paused) {
        return;
    }
    this.play();
};

AudioPlayer.prototype.close = function() {
    this.audio.pause();
    this.audio.src = "";
};

AudioPlayer.prototype.getMediaTime = function() {
    return Math.round(this.audio.currentTime * 1000);
};

// The range of ms has already been checked, we don't need to check it again.
AudioPlayer.prototype.setMediaTime = function(ms) {
    this.audio.currentTime = ms / 1000;
    return ms;
};

AudioPlayer.prototype.getVolume = function() {
    return Math.floor(this.audio.volume * 100);
};

AudioPlayer.prototype.setVolume = function(level) {
    if (level < 0) {
        level = 0;
    } else if (level > 100) {
        level = 100;
    }
    this.audio.volume = level / 100;
    return level;
};

AudioPlayer.prototype.getMute = function() {
    return this.audio.muted;
};

AudioPlayer.prototype.setMute = function(mute) {
    this.audio.muted = mute;
};

AudioPlayer.prototype.getDuration = function() {
    return Math.round(this.audio.duration * 1000);
};

function ImagePlayer(playerContainer) {
    this.url = playerContainer.url;

    this.image = new Image();
    this.image.style.position = "absolute";
    this.image.style.visibility = "hidden";

    this.isVideoControlSupported = true;
    this.isVolumeControlSupported = false;
}

ImagePlayer.prototype.realize = function() {
    var objectUrl;

    var p = new Promise((function(resolve, reject) {
        this.image.onload = resolve.bind(null, true);
        this.image.onerror = function() {
            reject(new JavaException("javax/microedition/media/MediaException", "Failed to load image"));
        };
        if (this.url.startsWith("file")) {
            fs.open(this.url.substring(7), (function(fd) {
                var imgData = fs.read(fd);
                fs.close(fd);
                this.image.src = URL.createObjectURL(new Blob([ imgData ]));
                objectUrl = this.image.src;
            }).bind(this));
        } else {
            this.image.src = this.url;
        }
    }).bind(this));

    p.done(function() {
        if (!objectUrl) {
            return;
        }
        URL.revokeObjectURL(objectUrl);
    });

    return p;
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

function ImageRecorder(playerContainer) {
    this.playerContainer = playerContainer;

    this.sender = null;

    this.width = -1;
    this.height = -1;

    this.isVideoControlSupported = true;
    this.isVolumeControlSupported = false;

    this.realizeResolver = null;

    this.snapshotData = null;
}

ImageRecorder.prototype.realize = function() {
    return new Promise((function(resolve, reject) {
        this.realizeResolver = resolve;
        this.realizeRejector = reject;
        this.sender = DumbPipe.open("camera", {}, this.recipient.bind(this));
    }).bind(this));
}

ImageRecorder.prototype.recipient = function(message) {
    switch (message.type) {
        case "initerror":
            if (message.name == "PermissionDeniedError") {
                this.realizeRejector(new JavaException("java/lang/SecurityException", "Not permitted to init camera"));
            } else {
                this.realizeRejector(new JavaException("javax/microedition/media/MediaException", "Failed to init camera, no camera?"));
            }
            this.realizeResolver = null;
            this.realizeRejector = null;
            this.sender({ type: "close" });
            break;

        case "gotstream":
            this.width = message.width;
            this.height = message.height;
            this.realizeResolver(true);
            this.realizeResolver = null;
            this.realizeRejector = null;
            break;

        case "snapshot":
            this.snapshotData = new Int8Array(message.data);

            MIDP.sendNativeEvent({
                type: MIDP.MMAPI_EVENT,
                intParam1: this.playerContainer.pId,
                intParam2: 0,
                intParam3: 0,
                intParam4: Media.EVENT_MEDIA_SNAPSHOT_FINISHED,
            }, MIDP.foregroundIsolateId);

            break;
    }
}

ImageRecorder.prototype.start = function() {
}

ImageRecorder.prototype.pause = function() {
}

ImageRecorder.prototype.close = function() {
    this.sender({ type: "close" });
}

ImageRecorder.prototype.getMediaTime = function() {
    return -1;
}

ImageRecorder.prototype.getWidth = function() {
    return this.width;
}

ImageRecorder.prototype.getHeight = function() {
    return this.height;
}

ImageRecorder.prototype.setLocation = function(x, y, w, h) {
    var displayElem = document.getElementById("display");
    this.sender({
        type: "setPosition",
        x: x + displayElem.offsetLeft,
        y: y + displayElem.offsetTop,
        w: w,
        h: h,
    });
}

ImageRecorder.prototype.setVisible = function(visible) {
    this.sender({ type: "setVisible", visible: visible });
}

ImageRecorder.prototype.startSnapshot = function(imageType) {
    var type = imageType ? this.playerContainer.getEncodingParam(imageType) : "image/jpeg";
    if (type === "jpeg") {
        type = "image/jpeg";
    }

    this.sender({ type: "snapshot", imageType: type });
}

ImageRecorder.prototype.getSnapshotData = function(imageType) {
    return this.snapshotData;
}

function PlayerContainer(url, pId) {
    this.url = url;
    // `pId` is the player id used in PlayerImpl.java, don't confuse with the id we used
    // here in Javascript. The reason we need to hold this `pId` is we need to send it
    // back when dispatch events, such as Media.EVENT_MEDIA_SNAPSHOT_FINISHED and
    // Media.EVENT_MEDIA_END_OF_MEDIA.
    this.pId = pId;

    this.mediaFormat = url ? this.guessFormatFromURL(url) : "UNKNOWN";
    this.contentType = "";

    this.wholeContentSize = -1;
    this.contentSize = 0;
    this.data = null;

    this.player = null;
}

// default buffer size 1 MB
PlayerContainer.DEFAULT_BUFFER_SIZE  = 1024 * 1024;

PlayerContainer.prototype.isImageCapture = function() {
    return !!(this.url && this.url.startsWith("capture://image"));
};

PlayerContainer.prototype.isAudioCapture = function() {
    return !!(this.url && this.url.startsWith("capture://audio"));
};

PlayerContainer.prototype.getEncodingParam = function(url) {
    var encoding = null;

    var idx = url.indexOf("encoding=");
    if (idx > -1) {
        var encodingKeyPair = url.substring(idx).split("&")[0].split("=");
        encoding = encodingKeyPair.length == 2 ? encodingKeyPair[1] : encoding;
    }

    return encoding;
};

PlayerContainer.prototype.guessFormatFromURL = function() {
    if (this.isAudioCapture()) {
        var encoding = "audio/ogg" || this.getEncodingParam(this.url); // Same as system property |audio.encodings|

        var format = Media.contentTypeToFormat.get(encoding);

        return format || "UNKNOWN";
    }

    if (this.isImageCapture()) {
        return "JPEG";
    }

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
            if (this.isAudioCapture()) {
                this.audioRecorder = new AudioRecorder(contentType);
            }
            this.player.realize().then(resolve);
        } else if (Media.supportedImageFormats.indexOf(this.mediaFormat) !== -1) {
            if (this.isImageCapture()) {
                this.player = new ImageRecorder(this);
            } else {
                this.player = new ImagePlayer(this);
            }
            this.player.realize().then(resolve, reject);
        } else {
            console.warn("Unsupported media format (" + this.mediaFormat + ") for " + this.url);
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

    // https://wiki.xiph.org/Ogg#Detecting_Ogg_files_and_extracting_information
    if (headerString.indexOf("OggS") === 0) {
        return "ogg";
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

PlayerContainer.prototype.getRecordedSize = function() {
    return this.audioRecorder.data.byteLength;
};

PlayerContainer.prototype.getRecordedData = function(offset, size, buffer) {
    var toRead = (size < this.audioRecorder.data.length) ? size : this.audioRecorder.data.byteLength;
    buffer.set(this.audioRecorder.data.subarray(0, toRead), offset);
    this.audioRecorder.data = new Uint8Array(this.audioRecorder.data.buffer.slice(toRead));
};

PlayerContainer.prototype.startSnapshot = function(imageType) {
    this.player.startSnapshot(imageType);
}

PlayerContainer.prototype.getSnapshotData = function() {
    return this.player.getSnapshotData();
}

PlayerContainer.prototype.getDuration = function() {
    return this.player.getDuration();
}

var AudioRecorder = function(aMimeType) {
    this.mimeType = aMimeType || "audio/3gpp";
    this.eventListeners = {};
    this.data = new Uint8Array();
    this.sender = DumbPipe.open("audiorecorder", {
        mimeType: this.mimeType
    }, this.recipient.bind(this));
};

AudioRecorder.prototype.recipient = function(message) {
    var callback = this["on" + message.type];
    if (typeof callback === "function") {
        callback(message);
    }

    if (this.eventListeners[message.type]) {
        this.eventListeners[message.type].forEach(function(listener) {
            if (typeof listener === "function") {
                listener(message);
            }
        });
    }
};

AudioRecorder.prototype.addEventListener = function(name, callback) {
    if (!callback || !name) {
        return;
    }

    if (!this.eventListeners[name]) {
        this.eventListeners[name] = [];
    }

    this.eventListeners[name].push(callback);
};

AudioRecorder.prototype.removeEventListener = function(name, callback) {
    if (!name || !callback || !this.eventListeners[name]) {
        return;
    }

    var newArray = [];
    this.eventListeners[name].forEach(function(listener) {
        if (callback != listener) {
            newArray.push(listener);
        }
    });

    this.eventListeners[name] = newArray;
};

AudioRecorder.prototype.start = function() {
    return new Promise(function(resolve, reject) {
        this.onstart = function() {
            this.onstart = null;
            this.onerror = null;
            resolve(1);
        }.bind(this);

        this.onerror = function() {
            this.onstart = null;
            this.onerror = null;
            resolve(0);
        }.bind(this);

        this.sender({ type: "start" });
    }.bind(this));
};

AudioRecorder.prototype.stop = function() {
    return new Promise(function(resolve, reject) {
        // To make sure the Player in Java can fetch data immediately, we
        // need to return after data is back.
        this.ondata = function ondata(message) {
            _cleanEventListeners();

            // The audio data we received are encoded with a proper format, it doesn't
            // make sense to concatenate them like the socket, so let just override
            // the buffered data here.
            var data = new Uint8Array(message.data);
            if (this.mimeType === "audio/amr") {
                data = Media.convert3gpToAmr(data);
            }
            this.data = data;
            resolve(1);
        }.bind(this);

        var _onerror = function() {
            _cleanEventListeners();
            resolve(0);
        }.bind(this);

        var _cleanEventListeners = function() {
            this.ondata = null;
            this.removeEventListener("error", _onerror);
        }.bind(this);

        this.addEventListener("error", _onerror);
        this.sender({ type: "stop" });
    }.bind(this));
};

AudioRecorder.prototype.pause = function() {
    return new Promise(function(resolve, reject) {
        // In Java, |stopRecord| might be called before |commit|, which triggers
        // the calling sequence:
        //    nPause -> nGetRecordedSize -> nGetRecordedData -> nClose
        //
        // to make sure the Player in Java can fetch data in such a case, we
        // need to request data immediately.
        //
        this.ondata = function ondata(message) {
            this.ondata = null;

            // The audio data we received are encoded with a proper format, it doesn't
            // make sense to concatenate them like the socket, so let just override
            // the buffered data here.
            this.data = new Uint8Array(message.data);
            resolve(1);
        }.bind(this);

        // Have to request data first before pausing.
        this.requestData();
        this.sender({ type: "pause" });
    }.bind(this));
};

AudioRecorder.prototype.requestData = function() {
    this.sender({ type: "requestData" });
};

AudioRecorder.prototype.close = function() {
    if (this._closed) {
        return new Promise(function(resolve) { resolve(1); });
    }

    // Make sure recording is stopped on the other side.
    return this.stop().then(function() {
        DumbPipe.close(this.sender);
        this._closed = true;
    }.bind(this));
};

Native.create("com/sun/mmedia/PlayerImpl.nInit.(IILjava/lang/String;)I", function(appId, pId, jURI) {
    var url = util.fromJavaString(jURI);
    var id = pId + (appId << 32);
    Media.PlayerCache[id] = new PlayerContainer(url, pId);
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

Native.create("com/sun/mmedia/DirectPlayer.nSetMediaTime.(IJ)I", function(handle, ms) {
    var container = Media.PlayerCache[handle];
    return container.player.setMediaTime(ms);
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

Native.create("com/sun/mmedia/DirectPlayer.nIsRecordControlSupported.(I)Z", function(handle) {
    return !!(Media.PlayerCache[handle] && Media.PlayerCache[handle].audioRecorder);
});

Native.create("com/sun/mmedia/DirectPlayer.nGetDuration.(I)I", function(handle) {
    return Media.PlayerCache[handle].getDuration();
})

Native.create("com/sun/mmedia/DirectRecord.nSetLocator.(ILjava/lang/String;)I", function(handle, locator) {
    // Let the DirectRecord class handle writing to files / uploading via HTTP
    return 0;
});

Native.create("com/sun/mmedia/DirectRecord.nGetRecordedSize.(I)I", function(handle) {
    return Media.PlayerCache[handle].getRecordedSize();
});

Native.create("com/sun/mmedia/DirectRecord.nGetRecordedData.(III[B)I", function(handle, offset, size, buffer) {
    Media.PlayerCache[handle].getRecordedData(offset, size, buffer);
    return 1;
});

Native.create("com/sun/mmedia/DirectRecord.nCommit.(I)I", function(handle) {
    // In DirectRecord.java, before nCommit, nPause or nStop is called,
    // which means all the recorded data has been fetched, so do nothing here.
    return 1;
});

Native.create("com/sun/mmedia/DirectRecord.nPause.(I)I", function(handle) {
    return Media.PlayerCache[handle].audioRecorder.pause();
}, true);

Native.create("com/sun/mmedia/DirectRecord.nStop.(I)I", function(handle) {
    return Media.PlayerCache[handle].audioRecorder.stop();
}, true);

Native.create("com/sun/mmedia/DirectRecord.nClose.(I)I", function(handle) {
    var player = Media.PlayerCache[handle];

    if (!player || !player.audioRecorder) {
        // We need to check if |audioRecorder| is still available, because |nClose|
        // might be called twice in DirectRecord.java, and only IOException is
        // handled in DirectRecord.java, let use IOException instead of IllegalStateException.
        throw new JavaException("java/io/IOException");
    }

    return player.audioRecorder.close().then(function(result) {
       delete player.audioRecorder;
       return result;
    });
}, true);

Native.create("com/sun/mmedia/DirectRecord.nStart.(I)I", function(handle) {
    // In DirectRecord.java, nStart plays two roles: real start and resume.
    // Let's handle this on the other side of the DumbPipe.
    return Media.PlayerCache[handle].audioRecorder.start();
}, true);

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

Media.TonePlayerCache = {
};

function TonePlayer() {
    this.audioContext = new AudioContext();

    // Use oscillator to generate tone.
    // @type {OscillatorNode}
    this.oscillator = null;

    // The gain node to control volume.
    this.gainNode = this.audioContext.createGain();
    this.gainNode.connect(this.audioContext.destination);
}

// Volume fade time in seconds.
TonePlayer.FADE_TIME = 0.1;

/*
 * Play back a tone as specified by a note and its duration.
 * A note is given in the range of 0 to 127 inclusive.  The frequency
 * of the note can be calculated from the following formula:
 *     SEMITONE_CONST = 17.31234049066755 = 1/(ln(2^(1/12)))
 *     note = ln(freq/8.176)*SEMITONE_CONST
 *     The musical note A = MIDI note 69 (0x45) = 440 Hz.
 * For the Asha implementaion, the note is shift by adding 21.
 * @param  note  Defines the tone of the note as specified by the above formula.
 * @param  duration  The duration of the tone in milli-seconds. Duration must be
 * positive.
 * @param  volume Audio volume range from 0 to 100.
 */
TonePlayer.prototype.playTone = function(note, duration, volume) {
    if (duration <= 0) {
        return;
    }
    duration /= 1000;

    if (note < 0) {
        note = 0;
    } else if (note > 127) {
        note = 127;
    }
    if (volume < 0) {
        volume = 0;
    } else if (volume > 100) {
        volume = 100;
    }
    volume /= 100;

    // Abort the previous tone.
    if (this.oscillator) {
        this.oscillator.onended = null;
        this.oscillator.disconnect();
    }

    var current = this.audioContext.currentTime;

    this.oscillator = this.audioContext.createOscillator();
    this.oscillator.connect(this.gainNode);

    // The default fequency is equivalent to 69 - 21 note and while 1 note = 100
    // cents.
    // Detune the frequency to the target note.
    this.oscillator.detune.value = (note - 69 + 21) * 100;

    // Fade in.
    this.oscillator.start(current);
    this.gainNode.gain.linearRampToValueAtTime(0, current);
    this.gainNode.gain.linearRampToValueAtTime(volume, current + TonePlayer.FADE_TIME);

    // Fade out.
    this.oscillator.stop(current + duration);
    this.gainNode.gain.linearRampToValueAtTime(volume, current + duration - TonePlayer.FADE_TIME);
    this.gainNode.gain.linearRampToValueAtTime(0, current + duration);
    this.oscillator.onended = function() {
        this.oscillator.disconnect();
        this.oscillator = null;
    }.bind(this);
};

TonePlayer.prototype.stopTone = function() {
    if (!this.oscillator) {
        return;
    }
    var current = this.audioContext.currentTime;
    this.gainNode.gain.linearRampToValueAtTime(0, current + TonePlayer.FADE_TIME);
};

Native.create("com/sun/mmedia/NativeTonePlayer.nPlayTone.(IIII)Z", function(appId, note, duration, volume) {
    if (!Media.TonePlayerCache[appId]) {
        Media.TonePlayerCache[appId] = new TonePlayer();
    }
    Media.TonePlayerCache[appId].playTone(note, duration, volume);
    return true;
});

Native.create("com/sun/mmedia/NativeTonePlayer.nStopTone.(I)Z", function(appId) {
    Media.TonePlayerCache[appId].stopTone();
    return true;
});

Native.create("com/sun/mmedia/DirectPlayer.nStartSnapshot.(ILjava/lang/String;)V", function(handle, imageType) {
    Media.PlayerCache[handle].startSnapshot(util.fromJavaString(imageType));
});

Native.create("com/sun/mmedia/DirectPlayer.nGetSnapshotData.(I)[B", function(handle) {
    return Media.PlayerCache[handle].getSnapshotData();
});

Native.create("com/sun/amms/GlobalMgrImpl.nCreatePeer.()I", function() {
    console.warn("com/sun/amms/GlobalMgrImpl.nCreatePeer.()I not implemented.");
    return 1;
});

Native.create("com/sun/amms/GlobalMgrImpl.nGetControlPeer.([B)I", function(typeName) {
    console.warn("com/sun/amms/GlobalMgrImpl.nGetControlPeer.([B)I not implemented.");
    return 2;
});

Native.create("com/sun/amms/directcontrol/DirectVolumeControl.nSetMute.(Z)V", function(mute) {
    console.warn("com/sun/amms/directcontrol/DirectVolumeControl.nSetMute.(Z)V not implemented.");
});

Native.create("com/sun/amms/directcontrol/DirectVolumeControl.nGetLevel.()I", function() {
    console.warn("com/sun/amms/directcontrol/DirectVolumeControl.nGetLevel.()I not implemented.");
    return 100;
});
