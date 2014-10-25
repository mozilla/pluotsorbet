/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

const ContentTypes = {
    memory: [
        "audio/wav"
    ],

    file: [
        "audio/wav"
    ],

    http: [
        "audio/wav"
    ],

    https: [
        "audio/wav"
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
