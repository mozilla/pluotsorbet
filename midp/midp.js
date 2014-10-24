/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var MIDP = {
};

MIDP.manifest = {};

Native.create("com/sun/midp/jarutil/JarReader.readJarEntry0.(Ljava/lang/String;Ljava/lang/String;)[B", function(ctx, jar, entryName) {
    var bytes = CLASSES.loadFileFromJar(util.fromJavaString(jar), util.fromJavaString(entryName));
    if (!bytes)
        throw new JavaException("java/io/IOException");
    var length = bytes.byteLength;
    var data = new Uint8Array(bytes);
    var array = ctx.newPrimitiveArray("B", length);
    for (var n = 0; n < length; ++n)
        array[n] = data[n];
    return array;
});

Native.create("com/sun/midp/log/LoggingBase.report.(IILjava/lang/String;)V", function(ctx, severity, channelID, message) {
    console.info(util.fromJavaString(message));
});

MIDP.groupTBL = [
    "net_access",
    "low_level_net_access",
    "call_control",
    "application_auto_invocation",
    "local_connectivity",
    "messaging",
    "restricted_messaging",
    "multimedia_recording",
    "read_user_data_access",
    "write_user_data_access",
    "location",
    "landmark",
    "payment",
    "authentication",
    "smart_card",
    "satsa"
];

Native.create("com/sun/midp/security/Permissions.loadGroupList.()[Ljava/lang/String;", function(ctx) {
    var list = ctx.newArray("[Ljava/lang/String;", MIDP.groupTBL.length);
    MIDP.groupTBL.forEach(function (e, n) {
        list[n] = ctx.newString(e);
    });
    return list;
});

MIDP.messagesTBL = [
     ["Airtime",
      "How often should %1 ask for permission to use airtime? Using airtime may result in charges.",
      "Don't use airtime and don't ask",
      "Is it OK to Use Airtime?",
      "%1 wants to send and receive data using the network. This will use airtime and may result in charges.\n\nIs it OK to use airtime?",
      ],
     ["Network",
      "How often should %1 ask for permission to use network? Using network may result in charges.",
      "Don't use network and don't ask",
      "Is it OK to Use Network?",
      "%1 wants to send and receive data using the network. This will use airtime and may result in charges.\n\nIs it OK to use network?"
      ],
     ["Restricted Network Connections",
      "How often should %1 ask for permission to open a restricted network connection?",
      "Don't open any restricted connections and don't ask",
      "Is it OK to open a restricted network connection?",
      "%1 wants to open a restricted network connection.\n\nIs it OK to open a restricted network connection?"
      ],
     ["Auto-Start Registration",
      "How often should %1 ask for permission to register itself to automatically start?",
      "Don't register and don't ask",
      "Is it OK to automatically start the application?",
      "%1 wants to register itself to be automatically started.\n\nIs it OK to register to be automatically started?"
      ],
     ["Computer Connection",
      "How often should %1 ask for permission to connect to a computer? This may require a data cable that came with your phone.",
      "Don't connect and don't ask",
      "Is it OK to Connect?",
      "%1 wants to connect to a computer. This may require a data cable.\n\nIs it OK to make a connection?"
      ],
     ["Messaging",
      "How often should %1 ask for permission before sending or receiving text messages?",
      "Don't send or receive messages and don't ask",
      "Is it OK to Send Messages?",
      "%1 wants to send text message(s). This could result in charges.%3 message(s) will be sent to %2.\n\nIs it OK to send messages?"
      ],
     ["Secured Messaging",
      "How often should %1 ask for permission before sending or receiving secured text messages?",
      "Don't send or receive secured messages and don't ask",
      "Is it OK to Send secured Messages?",
      "%1 wants to send text secured message(s). This could result in charges.%3 message(s) will be sent to %2.\n\nIs it OK to send messages?"
      ],
     ["Recording",
      "How often should %1 ask for permission to record audio and images? This will use space on your phone.",
      "Don't record and don't ask",
      "Is it OK to Record?",
      "%1 wants to record an image or audio clip.\n\nIs it OK to record?"
      ],
     ["Read Personal Data",
      "How often should %1 ask for permission to read your personal data (contacts, appointments, etc)?",
      "Don't read my data and don't ask",
      "Is it OK to read your personal data?",
      "%1 wants to read your personal data (contacts, appointments, etc)\n\nIs it OK to read your personal data?"
      ],
     ["Update Personal Data",
      "How often should %1 ask for permission to update your personal data (contacts, appointments, etc)?",
      "Don't update my data and don't ask",
      "Is it OK to update your personal data?",
      "%1 wants to update your personal data (contacts, appointments, etc)\n\nIs it OK to update your personal data?",
      "%1 wants to update %2\n\nIs it OK to update this data?"
      ],
     ["Obtain Current Location",
      "How often should %1 ask for permission to obtain your location?",
      "Don't give my location and don't ask",
      "Is it OK to obtain your location?",
      "Application %1 wants to obtain your the location.\n\nIs it OK to obtain your location?"
      ],
     ["Access Landmark Database",
      "How often should %1 ask for permission to access your landmark database?",
      "Don't access my landmark database and don't ask",
      "Is it OK to access your landmark database?",
      "Application %1 wants to access your landmark database.\n\nIs it OK to access your landmark database?"
      ],
     ["payment"],
     ["Personal Indentification",
      "How often should %1 ask for permission to use your smart card to identify you?",
      "Don't sign and don't ask",
      "Is it OK to obtain your personal signature?",
      "%1 wants to obtain your personal digital signature.\n\nIs it OK to obtain your personal signature?\nContent to be signed:\n\n%3"
      ],
     ["Smart Card Communication",
      "How often should %1 ask for permission to access your smart card?",
      "Don't access my smart card and don't ask",
      "Is it OK to access your smart card?",
      "Application %1 wants to access your smart card.\n\nIs it OK to access your smart card?"
      ],
     ["satsa"]
];

Native.create("com/sun/midp/security/Permissions.getGroupMessages.(Ljava/lang/String;)[Ljava/lang/String;", function(ctx, jName) {
    var name = util.fromJavaString(jName);
    var list = null;
    MIDP.groupTBL.forEach(function(e, n) {
        if (e === name) {
            var messages = MIDP.messagesTBL[n];
            list = ctx.newArray("[Ljava/lang/String;", messages.length);
            messages.forEach(function (e, n) {
                list[n] = ctx.newString(e);
            });
        }
    });
    return list;
});

MIDP.membersTBL = [
    ["javax.microedition.io.Connector.http",
     "javax.microedition.io.Connector.https",
     "javax.microedition.io.Connector.obex.client.tcp",
     "javax.microedition.io.Connector.obex.server.tcp"],
    ["javax.microedition.io.Connector.datagram",
     "javax.microedition.io.Connector.datagramreceiver",
     "javax.microedition.io.Connector.socket",
     "javax.microedition.io.Connector.serversocket",
     "javax.microedition.io.Connector.ssl"],
    ["javax.microedition.io.Connector.sip",
     "javax.microedition.io.Connector.sips"],
    ["javax.microedition.io.PushRegistry",
     "javax.microedition.content.ContentHandler"],
    ["javax.microedition.io.Connector.comm",
     "javax.microedition.io.Connector.obex.client",
     "javax.microedition.io.Connector.obex.server",
     "javax.microedition.io.Connector.bluetooth.client",
     "javax.microedition.io.Connector.bluetooth.server"],
    ["javax.wireless.messaging.sms.send",
     "javax.wireless.messaging.mms.send",
     "javax.microedition.io.Connector.sms",
     "javax.wireless.messaging.sms.receive",
     "javax.microedition.io.Connector.mms",
     "javax.wireless.messaging.mms.receive"],
    ["javax.wireless.messaging.cbs.receive",
     "javax.microedition.io.Connector.cbs"],
    ["javax.microedition.media.control.RecordControl",
     "javax.microedition.media.control.VideoControl.getSnapshot",
     "javax.microedition.amms.control.camera.enableShutterFeedback"],
    ["javax.microedition.pim.ContactList.read",
     "javax.microedition.pim.EventList.read",
     "javax.microedition.pim.ToDoList.read",
     "javax.microedition.io.Connector.file.read"],
    ["javax.microedition.pim.ContactList.write",
     "javax.microedition.pim.EventList.write",
     "javax.microedition.pim.ToDoList.write",
     "javax.microedition.io.Connector.file.write",
     "javax.microedition.amms.control.tuner.setPreset"],
    ["javax.microedition.location.Location",
     "javax.microedition.location.ProximityListener",
     "javax.microedition.location.Orientation"],
    ["javax.microedition.location.LandmarkStore.read",
     "javax.microedition.location.LandmarkStore.write",
     "javax.microedition.location.LandmarkStore.category",
     "javax.microedition.location.LandmarkStore.management"],
    ["javax.microedition.payment.process"],
    ["javax.microedition.securityservice.CMSMessageSignatureService"],
    ["javax.microedition.apdu.aid",
     "javax.microedition.jcrmi"],
    ["javax.microedition.apdu.sat"],
];

Native.create("com/sun/midp/security/Permissions.loadGroupPermissions.(Ljava/lang/String;)[Ljava/lang/String;", function(ctx, jName) {
    var name = util.fromJavaString(jName);
    var list = null;
    MIDP.groupTBL.forEach(function(e, n) {
        if (e === name) {
            var members = MIDP.membersTBL[n];
            list = ctx.newArray("[Ljava/lang/String;", members.length);
            members.forEach(function (e, n) {
                list[n] = ctx.newString(e);
            });
        }
    });
    return list;
});

Native.create("com/sun/midp/main/CldcPlatformRequest.dispatchPlatformRequest.(Ljava/lang/String;)Z", function(ctx, request) {
    request = util.fromJavaString(request);
    if (request.startsWith("http://") || request.startsWith("https://")) {
      window.open(request);
    } else {
      console.warn("com/sun/midp/main/CldcPlatformRequest.dispatchPlatformRequest.(Ljava/lang/String;)Z not implemented for: " + request);
    }
});

Native.create("com/sun/midp/main/CommandState.restoreCommandState.(Lcom/sun/midp/main/CommandState;)V", function(ctx, state) {
    var suiteId = (MIDP.midletClassName === "internal") ? -1 : 1;
    state.class.getField("I.suiteId.I").set(state, suiteId);
    state.class.getField("I.midletClassName.Ljava/lang/String;").set(state, ctx.newString(MIDP.midletClassName));
    var args = urlParams.args;
    state.class.getField("I.arg0.Ljava/lang/String;").set(state, ctx.newString((args.length > 0) ? args[0] : ""));
    state.class.getField("I.arg1.Ljava/lang/String;").set(state, ctx.newString((args.length > 1) ? args[1] : ""));
    state.class.getField("I.arg2.Ljava/lang/String;").set(state, ctx.newString((args.length > 2) ? args[2] : ""));
});

MIDP.domainTBL = [
    "manufacturer",
    "operator",
    "identified_third_party",
    "unidentified_third_party,unsecured",
    "minimum,unsecured",
    "maximum,unsecured",
];

Native.create("com/sun/midp/security/Permissions.loadDomainList.()[Ljava/lang/String;", function(ctx) {
    var list = ctx.newArray("[Ljava/lang/String;", MIDP.domainTBL.length);
    MIDP.domainTBL.forEach(function (e, n) {
        list[n] = ctx.newString(e);
    });
    return list;
});

MIDP.NEVER = 0;
MIDP.ALLOW = 1;
MIDP.BLANKET = 4;
MIDP.SESSION = 8;
MIDP.ONESHOT = 16;

MIDP.identifiedTBL = {
    net_access: { max: MIDP.BLANKET, default: MIDP.SESSION},
    low_level_net_access: { max: MIDP.BLANKET, default: MIDP.SESSION},
    call_control: { max: MIDP.BLANKET, default: MIDP.ONESHOT},
    application_auto_invocation: { max: MIDP.BLANKET, default: MIDP.ONESHOT},
    local_connectivity: { max: MIDP.BLANKET, default: MIDP.SESSION},
    messaging: { max: MIDP.BLANKET, default: MIDP.ONESHOT},
    restricted_messaging: { max: MIDP.BLANKET, default: MIDP.ONESHOT},
    multimedia_recording: { max: MIDP.BLANKET, default: MIDP.SESSION},
    read_user_data_access: { max: MIDP.BLANKET, default: MIDP.ONESHOT},
    write_user_data_access: { max: MIDP.BLANKET, default: MIDP.ONESHOT},
    location: { max: MIDP.BLANKET, default: MIDP.SESSION},
    landmark: { max: MIDP.BLANKET, default: MIDP.SESSION},
    payment: { max: MIDP.ALLOW,   default: MIDP.ALLOW},
    authentication: { max: MIDP.BLANKET, default: MIDP.SESSION},
    smart_card: { max: MIDP.BLANKET, default: MIDP.SESSION},
    satsa: { max: MIDP.NEVER,   default: MIDP.NEVER},
};

MIDP.unidentifiedTBL = {
    net_access: { max: MIDP.SESSION, default: MIDP.ONESHOT},
    low_level_net_access: { max: MIDP.SESSION, default: MIDP.ONESHOT},
    call_control: { max: MIDP.ONESHOT, default: MIDP.ONESHOT},
    application_auto_invocation: { max: MIDP.SESSION, default: MIDP.ONESHOT},
    local_connectivity: { max: MIDP.BLANKET, default: MIDP.ONESHOT},
    messaging: { max: MIDP.ONESHOT, default: MIDP.ONESHOT},
    restricted_messaging: { max: MIDP.ONESHOT, default: MIDP.ONESHOT},
    multimedia_recording: { max: MIDP.SESSION, default: MIDP.ONESHOT},
    read_user_data_access: { max: MIDP.ONESHOT, default: MIDP.ONESHOT},
    write_user_data_access: { max: MIDP.ONESHOT, default: MIDP.ONESHOT},
    location: { max: MIDP.SESSION, default: MIDP.ONESHOT},
    landmark: { max: MIDP.SESSION, default: MIDP.ONESHOT},
    payment: { max: MIDP.NEVER,   default: MIDP.NEVER},
    authentication: { max: MIDP.NEVER,   default: MIDP.NEVER},
    smart_card: { max: MIDP.NEVER,   default: MIDP.NEVER},
    satsa: { max: MIDP.NEVER,   default: MIDP.NEVER},
};

Native.create("com/sun/midp/security/Permissions.getDefaultValue.(Ljava/lang/String;Ljava/lang/String;)B", function(ctx, domain, group) {
    var allow = MIDP.NEVER;
    switch (util.fromJavaString(domain)) {
    case "manufacturer":
    case "maximum":
    case "operator":
        allow = MIDP.ALLOW;
        break;
    case "identified_third_party":
        allow = MIDP.identifiedTBL[util.fromJavaString(group)].default;
        break;
    case "unidentified_third_party":
        allow = MIDP.unidentifiedTBL[util.fromJavaString(group)].default;
        break;
    }
    return allow;
});

Native.create("com/sun/midp/security/Permissions.getMaxValue.(Ljava/lang/String;Ljava/lang/String;)B", function(ctx, domain, group) {
    var allow = MIDP.NEVER;
    switch (util.fromJavaString(domain)) {
    case "manufacturer":
    case "maximum":
    case "operator":
        allow = MIDP.ALLOW;
        break;
    case "identified_third_party":
        allow = MIDP.identifiedTBL[util.fromJavaString(group)].max;
        break;
    case "unidentified_third_party":
        allow = MIDP.unidentifiedTBL[util.fromJavaString(group)].max;
        break;
    }
    return allow;
});

Native.create("com/sun/midp/security/Permissions.loadingFinished.()V", function(ctx) {
    console.warn("Permissions.loadingFinished.()V not implemented");
});

Native.create("com/sun/midp/main/MIDletSuiteUtils.getIsolateId.()I", function(ctx) {
    return ctx.runtime.isolate.id;
});

Native.create("com/sun/midp/main/MIDletSuiteUtils.registerAmsIsolateId.()V", function(ctx) {
    MIDP.AMSIsolateId = ctx.runtime.isolate.id;
});

Native.create("com/sun/midp/main/MIDletSuiteUtils.getAmsIsolateId.()I", function(ctx) {
    return MIDP.AMSIsolateId;
});

Native.create("com/sun/midp/main/MIDletSuiteUtils.isAmsIsolate.()Z", function(ctx) {
    return MIDP.AMSIsolateId == ctx.runtime.isolate.id;
});

Native.create("com/sun/midp/main/MIDletSuiteUtils.vmBeginStartUp.(I)V", function(ctx, midletIsolateId) {
    // See DisplayContainer::createDisplayId, called by the LCDUIEnvironment constructor,
    // called by CldcMIDletSuiteLoader::createSuiteEnvironment.
    // The formula depens on the ID of the isolate that calls createDisplayId, that is
    // the same isolate that calls vmBeginStartUp. So this is a good place to calculate
    // the display ID.
    MIDP.displayId = ((midletIsolateId & 0xff)<<24) | (1 & 0x00ffffff);
});

Native.create("com/sun/midp/main/MIDletSuiteUtils.vmEndStartUp.(I)V", function(ctx, midletIsolateId) {
});

Native.create("com/sun/midp/main/AppIsolateMIDletSuiteLoader.allocateReservedResources0.()Z", function(ctx) {
  return true;
});

Native.create("com/sun/midp/main/Configuration.getProperty0.(Ljava/lang/String;)Ljava/lang/String;", function(ctx, key) {
    var value;
    switch (util.fromJavaString(key)) {
    case "com.sun.midp.publickeystore.WebPublicKeyStore":
        value = "_main.ks";
        break;
    case "com.sun.midp.events.dispatchTableInitSize":
        value = "16";
        break;
    case "microedition.locale":
        value = navigator.language;
        break;
    case "datagram":
        value = "com.sun.midp.io.j2me.datagram.ProtocolPushImpl";
        break;
    case "com.sun.midp.io.j2me.socket.buffersize":
        value = null;
        break;
    case "com.sun.midp.io.http.proxy":
        value = null;
        break;
    case "com.sun.midp.io.http.force_non_persistent":
        value = null;
        break;
    case "com.sun.midp.io.http.max_persistent_connections":
        value = null;
        break;
    case "com.sun.midp.io.http.persistent_connection_linger_time":
        value = null;
        break;
    case "com.sun.midp.io.http.input_buffer_size":
        value = null;
        break;
    case "com.sun.midp.io.http.output_buffer_size":
        value = null;
        break;
    default:
        console.warn("UNKNOWN PROPERTY (com/sun/midp/main/Configuration): " + util.fromJavaString(key));
        value = null;
        break;
    }
    return value ? value : null;
});

Native.create("com/sun/midp/chameleon/skins/resources/LoadedSkinData.beginReadingSkinFile.(Ljava/lang/String;)V", function(ctx, fileName) {
    var data = CLASSES.loadFile(util.fromJavaString(fileName));
    if (!data)
        throw new JavaException("java/io/IOException");
    MIDP.skinFileData = new DataView(data);
    MIDP.skinFilePos = 0;
});

Native.create("com/sun/midp/chameleon/skins/resources/LoadedSkinData.readByteArray.(I)[B", function(ctx, len) {
    if (!MIDP.skinFileData || (MIDP.skinFilePos + len) > MIDP.skinFileData.byteLength)
        throw new JavaException("java/lang/IllegalStateException");
    var bytes = ctx.newPrimitiveArray("B", len);
    for (var n = 0; n < len; ++n) {
        bytes[n] = MIDP.skinFileData.getUint8(MIDP.skinFilePos++);
    }
    return bytes;
});

Native.create("com/sun/midp/chameleon/skins/resources/LoadedSkinData.readIntArray.()[I", function(ctx) {
    if (!MIDP.skinFileData || (MIDP.skinFilePos + 4) > MIDP.skinFileData.byteLength)
        throw new JavaException("java/lang/IllegalStateException");
    var len = MIDP.skinFileData.getInt32(MIDP.skinFilePos, true);
    MIDP.skinFilePos += 4;
    var ints = ctx.newPrimitiveArray("I", len);
    for (var n = 0; n < len; ++n) {
        if ((MIDP.skinFilePos + 4) > MIDP.skinFileData.byteLength)
            throw new JavaException("java/lang/IllegalStateException");
        ints[n] = MIDP.skinFileData.getInt32(MIDP.skinFilePos, true);
        MIDP.skinFilePos += 4;
    }
    return ints;
});

MIDP.STRING_ENCODING_USASCII = 0;
MIDP.STRING_ENCODING_UTF8 = 1;

Native.create("com/sun/midp/chameleon/skins/resources/LoadedSkinData.readStringArray.()[Ljava/lang/String;", function(ctx) {
    if (!MIDP.skinFileData || (MIDP.skinFilePos + 4) > MIDP.skinFileData.byteLength)
        throw new JavaException("java/lang/IllegalStateException");
    var len = MIDP.skinFileData.getInt32(MIDP.skinFilePos, true);
    MIDP.skinFilePos += 4;
    var strings = ctx.newArray("[Ljava/lang/String;", len);
    for (var n = 0; n < len; ++n) {
        if ((MIDP.skinFilePos + 2) > MIDP.skinFileData.byteLength)
            throw new JavaException("java/lang/IllegalStateException");
        var strLen = MIDP.skinFileData.getUint8(MIDP.skinFilePos++);
        var strEnc = MIDP.skinFileData.getUint8(MIDP.skinFilePos++);
        if ((MIDP.skinFilePos + strLen) > MIDP.skinFileData.byteLength)
            throw new JavaException("java/lang/IllegalStateException");
        var bytes = new Uint8Array(MIDP.skinFileData.buffer).subarray(MIDP.skinFilePos, MIDP.skinFilePos + strLen);
        MIDP.skinFilePos += strLen;
        var str;
        if (strEnc === MIDP.STRING_ENCODING_USASCII) {
            str = "";
            for (var i = 0; i < strLen; ++i)
                str += String.fromCharCode(bytes[i]);
        } else if (strEnc === MIDP.STRING_ENCODING_UTF8) {
            str = util.decodeUtf8(bytes);
        } else {
            throw new JavaException("java/lang/IllegalStateException");
        }
        strings[n] = ctx.newString(str);
    }
    return strings;
});

Native.create("com/sun/midp/chameleon/skins/resources/LoadedSkinData.finishReadingSkinFile.()I", function(ctx) {
    MIDP.skinFileData = null;
    MIDP.skinFilePos = 0;
    return 0;
});

MIDP.sharedPool = null;
MIDP.sharedSkinData = null;

Native.create("com/sun/midp/chameleon/skins/resources/SkinResourcesImpl.shareResourcePool.(Ljava/lang/Object;)V", function(ctx, pool) {
    MIDP.sharedPool = pool;
});

Native.create("com/sun/midp/chameleon/skins/resources/SkinResourcesImpl.getSharedResourcePool.()Ljava/lang/Object;", function(ctx) {
    return MIDP.sharedPool;
});

Native.create("com/sun/midp/chameleon/skins/resources/SkinResourcesImpl.shareSkinData.(Ljava/lang/Object;)V", function(ctx, skinData) {
    MIDP.sharedSkinData = skinData;
});

Native.create("com/sun/midp/chameleon/skins/resources/SkinResourcesImpl.getSharedSkinData.()Ljava/lang/Object;", function(ctx) {
    return MIDP.sharedSkinData;
});

Native.create("com/sun/midp/chameleon/skins/resources/SkinResourcesImpl.ifLoadAllResources0.()Z", function(ctx) {
    return false;
});

Native.create("com/sun/midp/util/ResourceHandler.loadRomizedResource0.(Ljava/lang/String;)[B", function(ctx, file) {
    var fileName = "assets/0/" + util.fromJavaString(file).replace("_", ".").replace("_png", ".png");
    var data = CLASSES.loadFile(fileName);
    if (!data) {
        console.error("ResourceHandler::loadRomizedResource0: file " + fileName + " not found");
        throw new JavaException("java/io/IOException");
    }
    var len = data.byteLength;
    var bytes = ctx.newPrimitiveArray("B", len);
    var src = new Uint8Array(data);
    for (var n = 0; n < bytes.byteLength; ++n)
        bytes[n] = src[n];
    return bytes;
});

Native.create("com/sun/midp/chameleon/layers/SoftButtonLayer.isNativeSoftButtonLayerSupported0.()Z", function(ctx) {
    return false;
});

MIDP.Context2D = (function() {
    var c = document.getElementById("canvas");

    if (urlParams.autosize && !/no|0/.test(urlParams.autosize)) {
      c.width = window.innerWidth;
      c.height = window.innerHeight;
      document.documentElement.classList.add('autosize');
    } else {
      document.documentElement.classList.add('debug-mode');
      c.width = 240;
      c.height = 320;
    }

    // TODO These mouse event handlers only work on firefox right now,
    // because they use layerX and layerY.

    var mouse_is_down = false;
    var mouse_moved = false;
    var posX = 0;
    var posY = 0;

    c.addEventListener("mousedown", function(ev) {
        mouse_is_down = true;
        mouse_moved = false;
        posX = ev.layerX;
        posY = ev.layerY;
        MIDP.sendNativeEvent({ type: MIDP.PEN_EVENT, intParam1: MIDP.PRESSED, intParam2: posX, intParam3: posY, intParam4: MIDP.displayId }, MIDP.foregroundIsolateId);
    });

    c.addEventListener("mousemove", function(ev) {
        var distanceX = ev.layerX - posX;
        var distanceY = ev.layerY - posY;
        mouse_moved = true;
        if (mouse_is_down) {
            MIDP.sendNativeEvent({ type: MIDP.PEN_EVENT, intParam1: MIDP.DRAGGED, intParam2: ev.layerX, intParam3: ev.layerY, intParam4: MIDP.displayId }, MIDP.foregroundIsolateId);
            MIDP.sendNativeEvent({ type: MIDP.GESTURE_EVENT, intParam1: MIDP.GESTURE_DRAG, intParam2: distanceX, intParam3: distanceY, intParam4: MIDP.displayId,
                                   intParam5: posX, intParam6: posY, floatParam1: 0.0, intParam7: 0, intParam8: 0, intParam9: 0,
                                   intParam10: 0, intParam11: 0, intParam12: 0, intParam13: 0, intParam14: 0, intParam15: 0, intParam16: 0 }, MIDP.foregroundIsolateId);
        }
        posX = ev.layerX;
        posY = ev.layerY;
    });

    c.addEventListener("mouseup", function(ev) {
        mouse_is_down = false;
        if (!mouse_moved) {
            MIDP.sendNativeEvent({ type: MIDP.GESTURE_EVENT, intParam1: MIDP.GESTURE_TAP, intParam2: 0, intParam3: 0, intParam4: MIDP.displayId,
                                   intParam5: ev.layerX, intParam6: ev.layerY, floatParam1: 0.0, intParam7: 0, intParam8: 0, intParam9: 0,
                                   intParam10: 0, intParam11: 0, intParam12: 0, intParam13: 0, intParam14: 0, intParam15: 0, intParam16: 0 }, MIDP.foregroundIsolateId);
        }
        MIDP.sendNativeEvent({ type: MIDP.PEN_EVENT, intParam1: MIDP.RELEASED, intParam2: ev.layerX, intParam3: ev.layerY, intParam4: MIDP.displayId }, MIDP.foregroundIsolateId);
    });

    return c.getContext("2d");
})();

Native.create("com/sun/midp/midletsuite/MIDletSuiteStorage.loadSuitesIcons0.()I", function(ctx) {
    return 0;
});

Native.create("com/sun/midp/midletsuite/MIDletSuiteStorage.suiteExists.(I)Z", function(ctx, id) {
    return id <= 1;
});

Native.create("com/sun/midp/midletsuite/MIDletSuiteStorage.suiteIdToString.(I)Ljava/lang/String;", function(ctx, id) {
    return id.toString();
});

Native.create("com/sun/midp/midletsuite/MIDletSuiteStorage.getMidletSuiteStorageId.(I)I", function(ctx, suiteId) {
    // We should be able to use the same storage ID for all MIDlet suites.
    return 0; // storageId
});

Native.create("com/sun/midp/midletsuite/MIDletSuiteStorage.getMidletSuiteJarPath.(I)Ljava/lang/String;", function(ctx, id) {
    return "";
});

Native.create("com/sun/midp/midletsuite/MIDletSuiteImpl.lockMIDletSuite.(IZ)V", function(ctx, id, lock) {
    console.warn("MIDletSuiteImpl.lockMIDletSuite.(IZ)V not implemented (" + id + ", " + lock + ")");
});

Native.create("com/sun/midp/midletsuite/MIDletSuiteImpl.unlockMIDletSuite.(I)V", function(ctx, suiteId) {
    console.warn("MIDletSuiteImpl.unlockMIDletSuite.(I)V not implemented (" + suiteId + ")");
});

Native.create("com/sun/midp/midletsuite/SuiteSettings.load.()V", function(ctx) {
    this.class.getField("I.pushInterruptSetting.B").set(this, 1);
    console.warn("com/sun/midp/midletsuite/SuiteSettings.load.()V incomplete");
});

Native.create("com/sun/midp/midletsuite/SuiteSettings.save0.(IBI[B)V", function(ctx, suiteId, pushInterruptSetting, pushOptions, permissions) {
    console.warn("SuiteSettings.save0.(IBI[B)V not implemented (" +
                 suiteId + ", " + pushInterruptSetting + ", " + pushOptions + ", " + permissions + ")");
});

Native.create("com/sun/midp/midletsuite/InstallInfo.load.()V", function(ctx) {
    // The MIDlet has to be trusted for opening SSL connections using port 443.
    this.class.getField("I.trusted.Z").set(this, 1);
    console.warn("com/sun/midp/midletsuite/InstallInfo.load.()V incomplete");
});

Native.create("com/sun/midp/midletsuite/SuiteProperties.load.()[Ljava/lang/String;", function(ctx) {
    var keys = Object.keys(MIDP.manifest);
    var arr = ctx.newArray("[Ljava/lang/String;", keys.length * 2);
    var i = 0;
    keys.forEach(function(key) {
      arr[i++] = ctx.newString(key);
      arr[i++] = ctx.newString(MIDP.manifest[key]);
    });
    return arr;
});

Native.create("javax/microedition/lcdui/SuiteImageCacheImpl.loadAndCreateImmutableImageDataFromCache0.(Ljavax/microedition/lcdui/ImageData;ILjava/lang/String;)Z", function(ctx, imageData, suiteId, fileName) {
    console.warn("SuiteImageCacheImpl.loadAndCreateImmutableImageDataFromCache0.(L...ImageData;IL...String;)Z " +
                 "not implemented (" + imageData + ", " + suiteId + ", " + util.fromJavaString(fileName) + ")");
    return false;
});

MIDP.InterIsolateMutexes = [];
MIDP.LastInterIsolateMutexID = -1;

Native.create("com/sun/midp/util/isolate/InterIsolateMutex.getID0.(Ljava/lang/String;)I", function(ctx, jName) {
    var name = util.fromJavaString(jName);

    var mutex;
    for (var i = 0; i < MIDP.InterIsolateMutexes.length; i++) {
        if (MIDP.InterIsolateMutexes[i].name === name) {
            mutex = MIDP.InterIsolateMutexes[i];
        }
    }

    if (!mutex) {
      mutex = {
        name: name,
        id: ++MIDP.LastInterIsolateMutexID,
        locked: false,
        waiting: [],
      };
      MIDP.InterIsolateMutexes.push(mutex);
    }

    return mutex.id;
});

Native.create("com/sun/midp/util/isolate/InterIsolateMutex.lock0.(I)V", function(ctx, id) {
    return new Promise(function(resolve, reject) {
        var mutex;
        for (var i = 0; i < MIDP.InterIsolateMutexes.length; i++) {
            if (MIDP.InterIsolateMutexes[i].id == id) {
                mutex = MIDP.InterIsolateMutexes[i];
                break;
            }
        }

        if (!mutex) {
            reject(new JavaException("java/lang/IllegalStateException", "Invalid mutex ID"));
            return;
        }

        if (!mutex.locked) {
            mutex.locked = true;
            mutex.holder = ctx.runtime.isolate.id;
            resolve();
            return;
        }

        if (mutex.holder == ctx.runtime.isolate.id) {
            reject(new JavaException("java/lang/RuntimeException", "Attempting to lock mutex twice within the same Isolate"));
            return;
        }

        mutex.waiting.push(function() {
            mutex.locked = true;
            mutex.holder = ctx.runtime.isolate.id;
            resolve();
        });
    });
});

Native.create("com/sun/midp/util/isolate/InterIsolateMutex.unlock0.(I)V", function(ctx, id) {
    var mutex;
    for (var i = 0; i < MIDP.InterIsolateMutexes.length; i++) {
        if (MIDP.InterIsolateMutexes[i].id == id) {
            mutex = MIDP.InterIsolateMutexes[i];
            break;
        }
    }

    if (!mutex) {
        throw new JavaException("java/lang/IllegalStateException", "Invalid mutex ID");
    }

    if (!mutex.locked) {
        throw new JavaException("java/lang/RuntimeException", "Mutex is not locked");
    }

    if (mutex.holder !== ctx.runtime.isolate.id) {
        throw new JavaException("java/lang/RuntimeException", "Mutex is locked by different Isolate");
    }

    mutex.locked = false;

    var firstWaiting = mutex.waiting.shift();
    if (firstWaiting) {
        firstWaiting();
    }
});

// The foreground isolate will get the user events (keypresses, etc.)
MIDP.foregroundIsolateId;
MIDP.nativeEventQueues = {};
MIDP.waitingNativeEventQueue = {};

MIDP.copyEvent = function(obj, isolateId) {
    var e = MIDP.nativeEventQueues[isolateId].shift();
    obj.class.getField("I.type.I").set(obj, e.type);
    obj.class.fields.forEach(function(field) {
        field.set(obj, e[field.name]);
    });
}

MIDP.deliverWaitForNativeEventResult = function(resolve, nativeEvent, isolateId) {
    if (MIDP.nativeEventQueues[isolateId].length > 0)
        MIDP.copyEvent(nativeEvent, isolateId);
    resolve(MIDP.nativeEventQueues[isolateId].length);
}

MIDP.sendEvent = function(obj, isolateId) {
    var e = { type: obj.class.getField("I.type.I").get(obj) };
    obj.class.fields.forEach(function(field) {
        e[field.name] = field.get(obj);
    });
    MIDP.sendNativeEvent(e, isolateId);
}

MIDP.sendNativeEvent = function(e, isolateId) {
    MIDP.nativeEventQueues[isolateId].push(e);
    var elem = MIDP.waitingNativeEventQueue[isolateId];
    if (!elem)
        return;
    MIDP.deliverWaitForNativeEventResult(elem.resolve, elem.nativeEvent, isolateId);
    delete MIDP.waitingNativeEventQueue[isolateId];
}

MIDP.KEY_EVENT = 1;
MIDP.PEN_EVENT = 2;
MIDP.PRESSED = 1;
MIDP.RELEASED = 2;
MIDP.DRAGGED = 3;
MIDP.COMMAND_EVENT = 3;
MIDP.EVENT_QUEUE_SHUTDOWN = 31;
MIDP.GESTURE_EVENT = 71;
MIDP.GESTURE_TAP = 0x1;
MIDP.GESTURE_LONG_PRESS = 0x2;
MIDP.GESTURE_DRAG = 0x4;
MIDP.GESTURE_DROP = 0x8;
MIDP.GESTURE_FLICK = 0x10;
MIDP.GESTURE_LONG_PRESS_REPEATED = 0x20;
MIDP.GESTURE_PINCH = 0x40;
MIDP.GESTURE_DOUBLE_TAP = 0x80;
MIDP.GESTURE_RECOGNITION_START = 0x4000;
MIDP.GESTURE_RECOGNITION_END = 0x8000;

MIDP.suppressKeyEvents = false;

MIDP.keyPress = function(keyCode) {
    if (!MIDP.suppressKeyEvents)
        MIDP.sendNativeEvent({ type: MIDP.KEY_EVENT, intParam1: MIDP.PRESSED, intParam2: keyCode, intParam3: 0, intParam4: MIDP.displayId }, MIDP.foregroundIsolateId);
}

window.addEventListener("keypress", function(ev) {
    MIDP.keyPress(ev.which);
});

Native.create("com/sun/midp/events/EventQueue.getNativeEventQueueHandle.()I", function(ctx) {
    return 0;
});

Native.create("com/sun/midp/events/EventQueue.resetNativeEventQueue.()V", function(ctx) {
    console.warn("EventQueue.resetNativeEventQueue.()V not implemented");
});

Native.create("com/sun/midp/events/EventQueue.sendNativeEventToIsolate.(Lcom/sun/midp/events/NativeEvent;I)V",
function(ctx, obj, isolateId) {
    MIDP.sendEvent(obj, isolateId);
});

Native.create("com/sun/midp/events/NativeEventMonitor.waitForNativeEvent.(Lcom/sun/midp/events/NativeEvent;)I",
function(ctx, nativeEvent) {
    return new Promise(function(resolve, reject) {
        var isolateId = ctx.runtime.isolate.id;

        if (!MIDP.nativeEventQueues[isolateId]) {
          MIDP.nativeEventQueues[isolateId] = [];
        }

        if (MIDP.nativeEventQueues[isolateId].length === 0) {
            MIDP.waitingNativeEventQueue[isolateId] = {
              resolve: resolve,
              nativeEvent: nativeEvent,
            };
            return;
        }

        MIDP.deliverWaitForNativeEventResult(resolve, nativeEvent, isolateId);
    });
});

Native.create("com/sun/midp/events/NativeEventMonitor.readNativeEvent.(Lcom/sun/midp/events/NativeEvent;)Z",
function(ctx, obj) {
    if (!MIDP.nativeEventQueues[ctx.runtime.isolate.id].length) {
        return false;
    }
    MIDP.copyEvent(obj, ctx.runtime.isolate.id);
    return true;
});

MIDP.localizedStrings = new Map();

Native.create("com/sun/midp/l10n/LocalizedStringsBase.getContent.(I)Ljava/lang/String;", function(ctx, id) {
    if (MIDP.localizedStrings.size === 0) {
        // First build up a mapping of field names to field IDs
        var classInfo = CLASSES.getClass("com/sun/midp/i18n/ResourceConstants");
        var constantsMap = new Map();
        classInfo.fields.forEach(function(field) {
          constantsMap.set(field.name, classInfo.constant_pool[field.constantValue].integer);
        });

        var data = CLASSES.loadFileFromJar("java/classes.jar", "assets/0/en-US.xml");
        if (!data)
            throw new JavaException("java/io/IOException");

        var text = util.decodeUtf8(data);
        var xml = new window.DOMParser().parseFromString(text, "text/xml");
        var entries = xml.getElementsByTagName("localized_string");

        for (var n = 0; n < entries.length; ++n) {
            var attrs = entries[n].attributes;
            // map the key value to a field ID
            var id = constantsMap.get(attrs.Key.value);
            MIDP.localizedStrings.set(id, attrs.Value.value);
        }
    }

    var value = MIDP.localizedStrings.get(id);

    if (!value) {
        throw new JavaException("java/lang/IllegalStateException");
    }

    return value;
});

Native.create("javax/microedition/lcdui/Display.drawTrustedIcon0.(IZ)V", function(ctx, displayId, drawTrusted) {
    console.warn("Display.drawTrustedIcon0.(IZ)V not implemented (" + displayId + ", " + drawTrusted + ")");
});

Native.create("com/sun/midp/events/EventQueue.sendShutdownEvent.()V", function(ctx) {
    var obj = ctx.newObject(CLASSES.getClass("com/sun/midp/events/NativeEvent"));
    obj.class.getField("I.type.I").set(obj, MIDP.EVENT_QUEUE_SHUTDOWN);
    MIDP.sendEvent(obj, ctx.runtime.isolate.id);
});

Native.create("com/sun/midp/main/CommandState.saveCommandState.(Lcom/sun/midp/main/CommandState;)V", function(ctx, commandState) {
    console.warn("CommandState.saveCommandState.(L...CommandState;)V not implemented (" + commandState + ")");
});

Native.create("com/sun/midp/main/CommandState.exitInternal.(I)V", function(ctx, exit) {
    console.info("Exit: " + exit);
    return new Promise(function(){});
});

Native.create("com/sun/midp/suspend/SuspendSystem$MIDPSystem.allMidletsKilled.()Z", function(ctx) {
    console.warn("SuspendSystem$MIDPSystem.allMidletsKilled.()Z not implemented");
    return false;
});

Native.create("com/sun/midp/chameleon/input/InputModeFactory.getInputModeIds.()[I", function(ctx) {
    var ids = ctx.newPrimitiveArray("I", 1);
    ids[0] = 1; // KEYBOARD_INPUT_MODE
    return ids;
});

/* We don't care about the system keys SELECT,
  SOFT_BUTTON1, SOFT_BUTTON2, DEBUG_TRACE1, CLAMSHELL_OPEN, CLAMSHELL_CLOSE,
  but we do care about SYSTEM_KEY_CLEAR, so send it when the delete key is pressed.
*/

MIDP.SYSTEM_KEY_POWER = 1;
MIDP.SYSTEM_KEY_SEND = 2;
MIDP.SYSTEM_KEY_END = 3;
MIDP.SYSTEM_KEY_CLEAR = 4;

MIDP.systemKeyMap = {
  8: MIDP.SYSTEM_KEY_CLEAR, // Backspace
  112: MIDP.SYSTEM_KEY_POWER, // F1
  116: MIDP.SYSTEM_KEY_SEND, // F5
  114: MIDP.SYSTEM_KEY_END, // F3
};

Native.create("javax/microedition/lcdui/KeyConverter.getSystemKey.(I)I", function(ctx, key) {
    return MIDP.systemKeyMap[key] || 0;
});

MIDP.keyMap = {
    1: 119, // UP
    2: 97, // LEFT
    5: 100, // RIGHT
    6: 115, // DOWN
    8: 32, // FIRE
    9: 113, // GAME_A
    10: 101, // GAME_B
    11: 122, // GAME_C
    12: 99, // GAME_D
};

Native.create("javax/microedition/lcdui/KeyConverter.getKeyCode.(I)I", function(ctx, key) {
    return MIDP.keyMap[key] || 0;
});

MIDP.keyNames = {
    119: "Up",
    97: "Left",
    100: "Right",
    115: "Down",
    32: "Select",
    113: "Calendar",
    101: "Addressbook",
    122: "Menu",
    99: "Mail",
};

Native.create("javax/microedition/lcdui/KeyConverter.getKeyName.(I)Ljava/lang/String;", function(ctx, keyCode) {
    return (keyCode in MIDP.keyNames) ? MIDP.keyNames[keyCode] : String.fromCharCode(keyCode);
});

MIDP.gameKeys = {
    119: 1,  // UP
    97: 2,   // LEFT
    115: 6,  // DOWN
    100: 5,  // RIGHT
    32: 8,   // FIRE
    113: 9,  // GAME_A
    101: 10, // GAME_B
    122: 11, // GAME_C
    99: 12   // GAME_D
};

Native.create("javax/microedition/lcdui/KeyConverter.getGameAction.(I)I", function(ctx, keyCode) {
    return MIDP.gameKeys[keyCode] || 0;
});

Native.create("javax/microedition/lcdui/game/GameCanvas.setSuppressKeyEvents.(Ljavax/microedition/lcdui/Canvas;Z)V", function(ctx, canvas, suppressKeyEvents) {
    MIDP.suppressKeyEvents = suppressKeyEvents;
});

Native.create("com/sun/midp/main/MIDletProxyList.resetForegroundInNativeState.()V", function(ctx) {
    MIDP.displayId = -1;
});

Native.create("com/sun/midp/main/MIDletProxyList.setForegroundInNativeState.(II)V", function(ctx, isolateId, displayId) {
    MIDP.displayId = displayId;
    MIDP.foregroundIsolateId = isolateId;
});

MIDP.ConnectionRegistry = {
    // The lastRegistrationId is in common between alarms and push notifications
    lastRegistrationId:  -1,
    pushRegistrations: [],
    alarms: [],
    readyRegistrations: [],
    addReadyRegistration: function(id) {
        this.readyRegistrations.push(id);
        this.notify();
    },
    notify: function() {
        if (!this.readyRegistrations.length || !this.pendingPollCallback) {
            return;
        }
        var cb = this.pendingPollCallback;
        this.pendingPollCallback = null;
        cb(this.readyRegistrations.pop());
    },
    pushNotify: function(protocolName) {
        for (var i = 0; i < this.pushRegistrations.length; i++) {
            if (protocolName == this.pushRegistrations[i].connection) {
                this.addReadyRegistration(this.pushRegistrations[i].id);
            }
        }
    },
    waitForRegistration: function(cb) {
        if (this.pendingPollCallback) {
            throw new Error("There can only be one waiter.");
        }
        this.pendingPollCallback = cb;
        this.notify();
    },
    addConnection: function(connection) {
        connection.id = ++this.lastRegistrationId;
        this.pushRegistrations.push(connection);
        return connection.id;
    },
    addAlarm: function(alarm) {
        alarm.id = ++this.lastRegistrationId;
        this.alarms.push(alarm);
        return alarm.id;
    }
};

Native.create("com/sun/midp/io/j2me/push/ConnectionRegistry.poll0.(J)I", function(ctx, time, _) {
    return new Promise(function(resolve, reject) {
        MIDP.ConnectionRegistry.waitForRegistration(function(id) {
            resolve(id);
        });
    });
});

Native.create("com/sun/midp/io/j2me/push/ConnectionRegistry.add0.(Ljava/lang/String;)I", function(ctx, connection) {
    var values = util.fromJavaString(connection).split(',');

    console.warn("ConnectionRegistry.add0.(IL...String;)I isn't completely implemented");

    MIDP.ConnectionRegistry.addConnection({
        connection: values[0],
        midlet: values[1],
        filter: values[2],
        suiteId: values[3]
    });

    return 0;
});

Native.create("com/sun/midp/io/j2me/push/ConnectionRegistry.addAlarm0.([BJ)J", function(ctx, jMidlet, jTime, _) {
    var time = jTime.toNumber(), midlet = util.decodeUtf8(jMidlet);

    var lastAlarm = 0;
    var id = null;
    var alarms = MIDP.ConnectionRegistry.alarms;
    for (var i = 0; i < alarms.length; i++) {
        if (alarms[i].midlet == midlet) {
            if (time != 0) {
                id = alarms[i].id;
                lastAlarm = alarms[i].time;
                alarms[i].time = time;
            } else {
                alarms[i].splice(i, 1);
            }

            break;
        }
    }

    if (lastAlarm == 0 && time != 0) {
        id = MIDP.ConnectionRegistry.addAlarm({
            midlet: midlet,
            time: time
        });
    }

    if (id !== null) {
        var relativeTime = time - Date.now();
        if (relativeTime < 0) {
            relativeTime = 0;
        }

        setTimeout(function() {
            MIDP.ConnectionRegistry.addReadyRegistration(id);
        }, relativeTime);
    }

    return Long.fromNumber(lastAlarm);
});

Native.create("com/sun/midp/io/j2me/push/ConnectionRegistry.getMIDlet0.(I[BI)I", function(ctx, handle, regentry, entrysz) {
    var reg;
    var alarms = MIDP.ConnectionRegistry.alarms;
    for (var i = 0; i < alarms.length; i++) {
        if (alarms[i].id == handle) {
            reg = alarms[i];
        }
    }

    if (!reg) {
        var pushRegistrations = MIDP.ConnectionRegistry.pushRegistrations;
        for (var i = 0; i < pushRegistrations.length; i++) {
            if (pushRegistrations[i].id == handle) {
                reg = pushRegistrations[i];
            }
        }
    }

    if (!reg) {
        console.error("getMIDlet0 returns -1, this should never happen");
        return -1;
    }

    var str;

    if (reg.time) {
        str = reg.midlet + ", 0, 1";
    } else {
        str = reg.connection + ", " + reg.midlet + ", " + reg.filter + ", " + reg.suiteId;
    }

    for (var i = 0; i < str.length; i++) {
        regentry[i] = str.charCodeAt(i);
    }
    regentry[str.length] = 0;

    return 0;
});

Native.create("com/sun/midp/io/j2me/push/ConnectionRegistry.checkInByMidlet0.(ILjava/lang/String;)V", function(ctx, suiteId, className) {
    console.warn("ConnectionRegistry.checkInByMidlet0.(IL...String;)V not implemented (" +
                 suiteId + ", " + className + ")");
});

Native.create("com/sun/midp/io/j2me/push/ConnectionRegistry.checkInByName0.([B)I", function(ctx, name) {
    console.warn("ConnectionRegistry.checkInByName0.([B)V not implemented (" +
                 util.decodeUtf8(name) + ")");
    return 0;
});

Native.create("com/nokia/mid/ui/gestures/GestureInteractiveZone.isSupported.(I)Z", function(ctx, gestureEventIdentity) {
    console.warn("GestureInteractiveZone.isSupported.(I)Z not implemented (" + gestureEventIdentity + ")");
    return false;
});

Native.create("com/sun/midp/security/SecurityHandler.checkPermission0.(II)Z", function(ctx, suiteId, permission) {
    return true;
});

Native.create("com/sun/midp/security/SecurityHandler.checkPermissionStatus0.(II)I", function(ctx, suiteId, permission) {
    return 1;
});

Native.create("com/sun/midp/io/NetworkConnectionBase.initializeInternal.()V", function(ctx) {
    console.warn("NetworkConnectionBase.initializeInternal.()V not implemented");
});

Native.create("com/sun/j2me/content/RegistryStore.init.()Z", function(ctx) {
    console.warn("com/sun/j2me/content/RegistryStore.init.()Z not implemented");
    return true;
});

Native.create("com/sun/j2me/content/RegistryStore.forSuite0.(I)Ljava/lang/String;", function(ctx, suiteID) {
    console.warn("com/sun/j2me/content/RegistryStore.forSuite0.(I)Ljava/lang/String; not implemented");
    return "";
});

Native.create("com/sun/j2me/content/AppProxy.isInSvmMode.()Z", function(ctx) {
    console.warn("com/sun/j2me/content/AppProxy.isInSvmMode.()Z not implemented");
    return false;
});

Native.create("com/sun/j2me/content/InvocationStore.setCleanup0.(ILjava/lang/String;Z)V", function(ctx, suiteID, className, cleanup) {
    console.warn("com/sun/j2me/content/InvocationStore.setCleanup0.(ILjava/lang/String;Z)V not implemented");
});
