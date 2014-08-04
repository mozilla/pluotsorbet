/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'Use strict';

var MIDP = {
};

Native["com/sun/midp/jarutil/JarReader.readJarEntry0.(Ljava/lang/String;Ljava/lang/String;)[B"] = function(ctx, stack) {
    var entryName = util.fromJavaString(stack.pop()), jar = util.fromJavaString(stack.pop());
    var bytes = CLASSES.loadFileFromJar(jar, entryName);
    if (!bytes)
        ctx.raiseException("java/lang/IOException");
    var length = bytes.byteLength;
    var data = new Uint8Array(bytes);
    var array = CLASSES.newPrimitiveArray("B", length);
    for (var n = 0; n < length; ++n)
        array[n] = data[n];
    stack.push(array);
}

Native["com/sun/midp/log/LoggingBase.report.(IILjava/lang/String;)V"] = function(ctx, stack) {
    var message = stack.pop(), channelID = stack.pop(), severity = stack.pop();
    console.info(util.fromJavaString(message));
}

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

Native["com/sun/midp/security/Permissions.loadGroupList.()[Ljava/lang/String;"] = function(ctx, stack) {
    var list = CLASSES.newArray("[Ljava/lang/String;", MIDP.groupTBL.length);
    MIDP.groupTBL.forEach(function (e, n) {
        list[n] = CLASSES.newString(e);
    });
    stack.push(list);
}

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

Native["com/sun/midp/security/Permissions.getGroupMessages.(Ljava/lang/String;)[Ljava/lang/String;"] = function(ctx, stack) {
    var name = util.fromJavaString(stack.pop());
    var list = null;
    MIDP.groupTBL.forEach(function(e, n) {
        if (e === name) {
            var messages = MIDP.messagesTBL[n];
            list = CLASSES.newArray("[Ljava/lang/String;", messages.length);
            messages.forEach(function (e, n) {
                list[n] = CLASSES.newString(e);
            });
        }
    });
    stack.push(list);
}

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

Native["com/sun/midp/security/Permissions.loadGroupPermissions.(Ljava/lang/String;)[Ljava/lang/String;"] = function(ctx, stack) {
    var name = util.fromJavaString(stack.pop());
    var list = null;
    MIDP.groupTBL.forEach(function(e, n) {
        if (e === name) {
            var members = MIDP.membersTBL[n];
            list = CLASSES.newArray("[Ljava/lang/String;", members.length);
            members.forEach(function (e, n) {
                list[n] = CLASSES.newString(e);
            });
        }
    });
    stack.push(list);
}

Native["com/sun/midp/main/CommandState.restoreCommandState.(Lcom/sun/midp/main/CommandState;)V"] = function(ctx, stack) {
    var state = stack.pop();
    var midletClassName = urlParams.midletClassName ? urlParams.midletClassName : "internal";
    var suiteId = (midletClassName === "internal") ? -1 : 1;
    state.class.getField("suiteId", "I").set(state, suiteId);
    state.class.getField("midletClassName", "Ljava/lang/String;").set(state, CLASSES.newString(midletClassName));
    var args = urlParams.args;
    state.class.getField("arg0", "Ljava/lang/String;").set(state, CLASSES.newString((args.length > 0) ? args[0] : ""));
    state.class.getField("arg1", "Ljava/lang/String;").set(state, CLASSES.newString((args.length > 1) ? args[1] : ""));
    state.class.getField("arg2", "Ljava/lang/String;").set(state, CLASSES.newString((args.length > 2) ? args[2] : ""));
}

MIDP.domainTBL = [
    "manufacturer",
    "operator",
    "identified_third_party",
    "unidentified_third_party,unsecured",
    "minimum,unsecured",
    "maximum,unsecured",
];

Native["com/sun/midp/security/Permissions.loadDomainList.()[Ljava/lang/String;"] = function(ctx, stack) {
    var list = CLASSES.newArray("[Ljava/lang/String;", MIDP.domainTBL.length);
    MIDP.domainTBL.forEach(function (e, n) {
        list[n] = CLASSES.newString(e);
    });
    stack.push(list);
}

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

Native["com/sun/midp/security/Permissions.getDefaultValue.(Ljava/lang/String;Ljava/lang/String;)B"] = function(ctx, stack) {
    var group = util.fromJavaString(stack.pop()), domain = util.fromJavaString(stack.pop());
    var allow = MIDP.NEVER;
    switch (domain) {
    case "manufacturer":
    case "maximum":
    case "operator":
        allow = MIDP.ALLOW;
        break;
    case "identified_third_party":
        allow = MIDP.identifiedTBL[group].default;
        break;
    case "unidentified_third_party":
        allow = MIDP.unidentifiedTBL[group].default;
        break;
    }
    stack.push(allow);
}

Native["com/sun/midp/security/Permissions.getMaxValue.(Ljava/lang/String;Ljava/lang/String;)B"] = function(ctx, stack) {
    var group = util.fromJavaString(stack.pop()), domain = util.fromJavaString(stack.pop());
    var allow = MIDP.NEVER;
    switch (domain) {
    case "manufacturer":
    case "maximum":
    case "operator":
        allow = MIDP.ALLOW;
        break;
    case "identified_third_party":
        allow = MIDP.identifiedTBL[group].max;
        break;
    case "unidentified_third_party":
        allow = MIDP.unidentifiedTBL[group].max;
        break;
    }
    stack.push(allow);
}

Native["com/sun/midp/security/Permissions.loadingFinished.()V"] = function(ctx, stack) {
}

Native["com/sun/midp/main/MIDletSuiteUtils.getIsolateId.()I"] = function(ctx, stack) {
    stack.push(0);
}

Native["com/sun/midp/main/MIDletSuiteUtils.registerAmsIsolateId.()V"] = function(ctx, stack) {
}

Native["com/sun/midp/main/MIDletSuiteUtils.getAmsIsolateId.()I"] = function(ctx, stack) {
    stack.push(0);
}

Native["com/sun/midp/main/MIDletSuiteUtils.isAmsIsolate.()Z"] = function(ctx, stack) {
    stack.push(1);
}

Native["com/sun/midp/main/MIDletSuiteUtils.vmBeginStartUp.(I)V"] = function(ctx, stack) {
    var midletIsolateId = stack.pop();
}

Native["com/sun/midp/main/MIDletSuiteUtils.vmEndStartUp.(I)V"] = function(ctx, stack) {
    var midletIsolateId = stack.pop();
}

Native["com/sun/midp/main/Configuration.getProperty0.(Ljava/lang/String;)Ljava/lang/String;"] = function(ctx, stack) {
    var key = stack.pop();
    var value;
    switch (util.fromJavaString(key)) {
    case "com.sun.midp.publickeystore.WebPublicKeyStore":
        value = "web.pks";
        break;
    case "com.sun.midp.events.dispatchTableInitSize":
        value = "16";
        break;
    case "microedition.locale":
        value = navigator.language;
        break;
    default:
        console.log("UNKNOWN PROPERTY (com/sun/midp/main/Configuration): " + util.fromJavaString(key));
        value = null;
        break;
    }
    stack.push(value ? CLASSES.newString(value) : null);
}

Native["com/sun/midp/io/j2me/storage/File.initConfigRoot.(I)Ljava/lang/String;"] = function(ctx, stack) {
    var storageId = stack.pop();
    stack.push(CLASSES.newString("assets/" + storageId + "/"));
}

Native["com/sun/midp/chameleon/skins/resources/LoadedSkinData.beginReadingSkinFile.(Ljava/lang/String;)V"] = function(ctx, stack) {
    var fileName = util.fromJavaString(stack.pop());
    var data = CLASSES.loadFile(fileName);
    if (!data)
        ctx.raiseException("java/lang/IOException");
    MIDP.skinFileData = new DataView(data);
    MIDP.skinFilePos = 0;
}

Native["com/sun/midp/chameleon/skins/resources/LoadedSkinData.readByteArray.(I)[B"] = function(ctx, stack) {
    var len = stack.pop();
    if (!MIDP.skinFileData || (MIDP.skinFilePos + len) > MIDP.skinFileData.byteLength)
        ctx.raiseException("java/lang/IllegalStateException");
    var bytes = CLASSES.newPrimitiveArray("B", len);
    for (var n = 0; n < len; ++n) {
        bytes[n] = MIDP.skinFileData.getUint8(MIDP.skinFilePos++);
    }
    stack.push(bytes);
}

Native["com/sun/midp/chameleon/skins/resources/LoadedSkinData.readIntArray.()[I"] = function(ctx, stack) {
    if (!MIDP.skinFileData || (MIDP.skinFilePos + 4) > MIDP.skinFileData.byteLength)
        ctx.raiseException("java/lang/IllegalStateException");
    var len = MIDP.skinFileData.getInt32(MIDP.skinFilePos, true);
    MIDP.skinFilePos += 4;
    var ints = CLASSES.newPrimitiveArray("I", len);
    for (var n = 0; n < len; ++n) {
        if ((MIDP.skinFilePos + 4) > MIDP.skinFileData.byteLength)
            ctx.raiseException("java/lang/IllegalStateException");
        ints[n] = MIDP.skinFileData.getInt32(MIDP.skinFilePos, true);
        MIDP.skinFilePos += 4;
    }
    stack.push(ints);
}

MIDP.STRING_ENCODING_USASCII = 0;
MIDP.STRING_ENCODING_UTF8 = 1;

Native["com/sun/midp/chameleon/skins/resources/LoadedSkinData.readStringArray.()[Ljava/lang/String;"] = function(ctx, stack) {
    if (!MIDP.skinFileData || (MIDP.skinFilePos + 4) > MIDP.skinFileData.byteLength)
        ctx.raiseException("java/lang/IllegalStateException");
    var len = MIDP.skinFileData.getInt32(MIDP.skinFilePos, true);
    MIDP.skinFilePos += 4;
    var strings = CLASSES.newArray("[Ljava/lang/String;", len);
    for (var n = 0; n < len; ++n) {
        if ((MIDP.skinFilePos + 2) > MIDP.skinFileData.byteLength)
            ctx.raiseException("java/lang/IllegalStateException");
        var strLen = MIDP.skinFileData.getUint8(MIDP.skinFilePos++);
        var strEnc = MIDP.skinFileData.getUint8(MIDP.skinFilePos++);
        if ((MIDP.skinFilePos + strLen) > MIDP.skinFileData.byteLength)
            ctx.raiseException("java/lang/IllegalStateException");
        var bytes = MIDP.skinFileData.buffer.slice(MIDP.skinFilePos, MIDP.skinFilePos + strLen);
        MIDP.skinFilePos += strLen;
        var str;
        if (strEnc === MIDP.STRING_ENCODING_USASCII) {
            var data = new Uint8Array(bytes);
            str = "";
            for (var i = 0; i < strLen; ++i)
                str += String.fromCharCode(data[i]);
        } else if (strEnc === MIDP.STRING_ENCODING_UTF8) {
            str = util.decodeUtf8(bytes);
        } else {
            ctx.raiseException("java/lang/IllegalStateException");
        }
        strings[n] = CLASSES.newString(str);
    }
    stack.push(strings);
}

Native["com/sun/midp/chameleon/skins/resources/LoadedSkinData.finishReadingSkinFile.()I"] = function(ctx, stack) {
    MIDP.skinFileData = null;
    MIDP.skinFilePos = 0;
    stack.push(0);
}

Native["com/sun/midp/chameleon/skins/resources/SkinResourcesImpl.shareResourcePool.(Ljava/lang/Object;)V"] = function(ctx, stack) {
    var pool = stack.pop();
}

Native["com/sun/midp/chameleon/skins/resources/SkinResourcesImpl.shareSkinData.(Ljava/lang/Object;)V"] = function(ctx, stack) {
    var data = stack.pop();
}

Native["com/sun/midp/chameleon/skins/resources/SkinResourcesImpl.ifLoadAllResources0.()Z"] = function(ctx, stack) {
    stack.push(0);
}

Native["com/sun/midp/util/ResourceHandler.loadRomizedResource0.(Ljava/lang/String;)[B"] = function(ctx, stack) {
    var fileName = "assets/0/" + util.fromJavaString(stack.pop()).replace("_", ".").replace("_png", ".png");
    var data = CLASSES.loadFile(fileName);
    if (!data) {
        console.log(fileName);
        ctx.raiseException("java/lang/IOException");
    }
    var len = data.byteLength;
    var bytes = CLASSES.newPrimitiveArray("B", len);
    var src = new Uint8Array(data);
    for (var n = 0; n < bytes.byteLength; ++n)
        bytes[n] = src[n];
    stack.push(bytes);
}

Native["javax/microedition/lcdui/ImageDataFactory.createImmutableImageDecodeImage.(Ljavax/microedition/lcdui/ImageData;[BII)V"] = function(ctx, stack) {
    var length = stack.pop(), offset = stack.pop(), bytes = stack.pop(), imageData = stack.pop(), _this = stack.pop();
    var blob = new Blob([bytes.buffer.slice(offset, offset + length)], { type: "image/png" });
    var img = new Image();
    img.src = URL.createObjectURL(blob);
    img.onload = function() {
        imageData.class.getField("width", "I").set(imageData, img.naturalWidth);
        imageData.class.getField("height", "I").set(imageData, img.naturalHeight);
        imageData.class.getField("nativeImageData", "I").set(imageData, img);
        ctx.resume();
    }
    img.onerror = function(e) {
        ctx.resume();
    }
    throw VM.Pause;
}

Native["com/sun/midp/chameleon/layers/SoftButtonLayer.isNativeSoftButtonLayerSupported0.()Z"] = function(ctx, stack) {
    stack.push(0);
}

Native["com/sun/midp/lcdui/DisplayDeviceContainer.getDisplayDevicesIds0.()[I"] = function(ctx, stack) {
    var _this = stack.pop(), ids = CLASSES.newPrimitiveArray("I", 1);
    ids[0] = 1;
    stack.push(ids);
}

Native["com/sun/midp/lcdui/DisplayDevice.getDisplayName0.(I)Ljava/lang/String;"] = function(ctx, stack) {
    var id = stack.pop(), _this = stack.pop();
    stack.push(null);
}

Native["com/sun/midp/lcdui/DisplayDevice.isDisplayPrimary0.(I)Z"] = function(ctx, stack) {
    var id = stack.pop(), _this = stack.pop();
    stack.push(1);
}

Native["com/sun/midp/lcdui/DisplayDevice.isbuildInDisplay0.(I)Z"] = function(ctx, stack) {
    var id = stack.pop(), _this = stack.pop();
    stack.push(1);
}

Native["com/sun/midp/lcdui/DisplayDevice.getDisplayCapabilities0.(I)I"] = function(ctx, stack) {
    var id = stack.pop(), _this = stack.pop();
    stack.push(0x3ff);
}

Native["com/sun/midp/lcdui/DisplayDevice.isDisplayPenSupported0.(I)Z"] = function(ctx, stack) {
    var id = stack.pop(), _this = stack.pop();
    stack.push(1);
}

Native["com/sun/midp/lcdui/DisplayDevice.isDisplayPenMotionSupported0.(I)Z"] = function(ctx, stack) {
    var id = stack.pop(), _this = stack.pop();
    stack.push(1);
}

Native["com/sun/midp/lcdui/DisplayDevice.getReverseOrientation0.(I)Z"] = function(ctx, stack) {
    var id = stack.pop(), _this = stack.pop();
    stack.push(0);
}

MIDP.Context2D = (function() {
    var c = document.getElementById("canvas");
    c.width = 320;
    c.height = 480;


    // TODO These mouse event handlers only work on firefox right now,
    // because they use layerX and layerY.

    var mouse_is_down = false;
    
    c.addEventListener("mousedown", function(ev) {
        mouse_is_down = true;
        MIDP.sendNativeEvent(MIDP.PEN_EVENT, MIDP.PRESSED, ev.layerX, ev.layerY);
    });
    
    c.addEventListener("mousemove", function(ev) {
        if (mouse_is_down) {
            MIDP.sendNativeEvent(MIDP.PEN_EVENT, MIDP.DRAGGED, ev.layerX, ev.layerY)
        }
    });
    
    c.addEventListener("mouseup", function(ev) {
        mouse_is_down = false;
        MIDP.sendNativeEvent(MIDP.PEN_EVENT, MIDP.RELEASED, ev.layerX, ev.layerY);
    });

    return c.getContext("2d");
})();

Native["com/sun/midp/lcdui/DisplayDevice.getScreenWidth0.(I)I"] = function(ctx, stack) {
    var id = stack.pop(), _this = stack.pop();
    stack.push(MIDP.Context2D.canvas.width);
}

Native["com/sun/midp/lcdui/DisplayDevice.getScreenHeight0.(I)I"] = function(ctx, stack) {
    var id = stack.pop(), _this = stack.pop();
    stack.push(MIDP.Context2D.canvas.height);
}

Native["com/sun/midp/lcdui/DisplayDevice.displayStateChanged0.(II)V"] = function(ctx, stack) {
    var state = stack.pop(), hardwareId = stack.pop(), _this = stack.pop();
}

Native["com/sun/midp/midletsuite/MIDletSuiteStorage.loadSuitesIcons0.()I"] = function(ctx, stack) {
    stack.push(0);
}

Native["com/sun/midp/midletsuite/MIDletSuiteStorage.suiteExists.(I)Z"] = function(ctx, stack) {
    var id = stack.pop(), _this = stack.pop();
    stack.push(id <= 1 ? 1 : 0);
}

Native["com/sun/midp/midletsuite/MIDletSuiteStorage.getSecureFilenameBase.(I)Ljava/lang/String;"] = function(ctx, stack) {
    var id = stack.pop(), _this = stack.pop();
    stack.push(CLASSES.newString(""));
}

Native["com/sun/midp/rms/RecordStoreUtil.exists.(Ljava/lang/String;Ljava/lang/String;I)Z"] = function(ctx, stack) {
    var ext = stack.pop(), name = util.fromJavaString(stack.pop()), path = util.fromJavaString(stack.pop());
    stack.push(0);
}

Native["com/sun/midp/midletsuite/MIDletSuiteStorage.getMidletSuiteStorageId.(I)I"] = function(ctx, stack) {
    var id = stack.pop();
    stack.push(0);
}

Native["com/sun/midp/rms/RecordStoreFile.spaceAvailableNewRecordStore0.(Ljava/lang/String;I)I"] = function(ctx, stack) {
    var storageId = stack.pop(), name = util.fromJavaString(stack.pop());
    stack.push(10 * 4096 * 4096);
}

Native["com/sun/midp/rms/RecordStoreFile.spaceAvailableRecordStore.(ILjava/lang/String;I)I"] = function(ctx, stack) {
    var storageId = stack.pop(), base = util.fromJavaString(stack.pop()), handle = stack.pop();
    stack.push(10 * 4096 * 4096);
}

Native["com/sun/midp/rms/RecordStoreFile.openRecordStoreFile.(Ljava/lang/String;Ljava/lang/String;I)I"] = function(ctx, stack) {
    var ext = stack.pop(), name = util.fromJavaString(stack.pop()), base = util.fromJavaString(stack.pop()), _this = stack.pop();
    stack.push(0);
}

Native["com/sun/midp/rms/RecordStoreFile.setPosition.(II)V"] = function(ctx, stack) {
    var pos = stack.pop(), handle = stack.pop();
}

Native["com/sun/midp/rms/RecordStoreFile.writeBytes.(I[BII)V"] = function(ctx, stack) {
    var count = stack.pop(), offset = stack.pop(), bytes = stack.pop(), fileId = stack.pop();
}

Native["com/sun/midp/rms/RecordStoreFile.commitWrite.(I)V"] = function(ctx, stack) {
    var fileId = stack.pop();
}

Native["com/sun/midp/rms/RecordStoreFile.closeFile.(I)V"] = function(ctx, stack) {
    var fileId = stack.pop();
}

Native["com/sun/midp/rms/RecordStoreSharedDBHeader.getLookupId0.(ILjava/lang/String;I)I"] = function(ctx, stack) {
    var headerDataSize = stack.pop(), storeName = util.fromJavaString(stack.pop()), suiteId = stack.pop();
    stack.push(0);
}

Native["com/sun/midp/rms/RecordStoreSharedDBHeader.shareCachedData0.(I[BI)I"] = function(ctx, stack) {
    var headerDataSize = stack.pop(), headerData = stack.pop(), lookupId = stack.pop();
    stack.push(0);
}

Native["com/sun/midp/rms/RecordStoreSharedDBHeader.updateCachedData0.(I[BII)I"] = function(ctx, stack) {
    var headerVersion = stack.pop(), headerDataSize = stack.pop(), headerData = stack.pop(), lookupId = stack.pop();
    stack.push(0);
}

Native["com/sun/midp/rms/RecordStoreSharedDBHeader.cleanup0.()V"] = function(ctx, stack) {
    var _this = stack.pop();
}

Native["com/sun/midp/rms/RecordStoreRegistry.getRecordStoreListeners.(ILjava/lang/String;)[I"] = function(ctx, stack) {
    var storeName = util.fromJavaString(stack.pop()), suiteId = stack.pop();
    stack.push(CLASSES.newPrimitiveArray("I", 0));
}

Native["com/sun/midp/rms/RecordStoreRegistry.sendRecordStoreChangeEvent.(ILjava/lang/String;II)V"] = function(ctx, stack) {
    var recordId = stack.pop(), changeType = stack.pop(), storeName = util.fromJavaString(stack.pop()), suiteId = stack.pop();
}

Native["com/sun/midp/rms/RecordStoreRegistry.stopRecordStoreListening.(ILjava/lang/String;)V"] = function(ctx, stack) {
    var storeName = util.fromJavaString(stack.pop()), suiteId = stack.pop();
}

Native["com/sun/midp/midletsuite/MIDletSuiteImpl.lockMIDletSuite.(IZ)V"] = function(ctx, stack) {
    var lock = stack.pop(), id = stack.pop();
}

Native["com/sun/midp/midletsuite/SuiteSettings.load.()V"] = function(ctx, stack) {
    var _this = stack.pop();
}

Native["com/sun/midp/midletsuite/InstallInfo.load.()V"] = function(ctx, stack) {
    var _this = stack.pop();
}

Native["com/sun/midp/midletsuite/SuiteProperties.load.()[Ljava/lang/String;"] = function(ctx, stack) {
    var _this = stack.pop();
    stack.push(CLASSES.newArray("[Ljava/lang/String;", 0));
}

Native["javax/microedition/lcdui/SuiteImageCacheImpl.loadAndCreateImmutableImageDataFromCache0.(Ljavax/microedition/lcdui/ImageData;ILjava/lang/String;)Z"] = function(ctx, stack) {
    var fileName = util.fromJavaString(stack.pop()), suiteId = stack.pop(), imageData = stack.pop();
    stack.push(0);
}

Native["com/sun/midp/util/isolate/InterIsolateMutex.getID0.(Ljava/lang/String;)I"] = function(ctx, stack) {
    var name = util.fromJavaString(stack.pop());
    stack.push(0);
}

Native["com/sun/midp/util/isolate/InterIsolateMutex.lock0.(I)V"] = function(ctx, stack) {
    var id = stack.pop();
}

Native["com/sun/midp/util/isolate/InterIsolateMutex.unlock0.(I)V"] = function(ctx, stack) {
    var id = stack.pop();
}

Native["com/sun/midp/rms/RecordStoreSharedDBHeader.getHeaderRefCount0.(I)I"] = function(ctx, stack) {
    var id = stack.pop();
    stack.push(1);
}

MIDP.nativeEventQueue = [];

MIDP.copyEvent = function(obj) {
    var e = MIDP.nativeEventQueue.shift();
    obj.class.getField("type", "I").set(obj, e.type);
    obj.class.fields.forEach(function(field) {
        field.set(obj, e[field.name]);
    });
}

MIDP.deliverWaitForNativeEventResult = function(ctx) {
    var stack = ctx.current().stack;
    var obj = stack.pop();
    if (MIDP.nativeEventQueue.length > 0)
        MIDP.copyEvent(obj);
    stack.push(MIDP.nativeEventQueue.length);
}

MIDP.sendEvent = function(obj) {
    var e = { type: obj.class.getField("type", "I").get(obj) };
    obj.class.fields.forEach(function(field) {
        e[field.name] = field.get(obj);
    });
    MIDP.nativeEventQueue.push(e);
    var ctx = MIDP.waitingNativeEventContext;
    if (!ctx)
        return;
    MIDP.deliverWaitForNativeEventResult(MIDP.waitingNativeEventContext);
    MIDP.waitingNativeEventContext.resume();
    MIDP.waitingNativeEventContext = null;
}

MIDP.sendNativeEvent = function(type, intParam1, intParam2, intParam3) {
    var obj = CLASSES.newObject(CLASSES.getClass("com/sun/midp/events/NativeEvent"));
    obj.class.getField("type", "I").set(obj, type);
    if (intParam1 !== undefined)
        obj.class.getField("intParam1", "I").set(obj, intParam1); // PRESSED
    if (intParam2 !== undefined)
        obj.class.getField("intParam2", "I").set(obj, intParam2);
    if (intParam3 !== undefined)
        obj.class.getField("intParam3", "I").set(obj, intParam3);
    
    obj.class.getField("intParam4", "I").set(obj, 1); // displayID
    MIDP.sendEvent(obj);  
}

MIDP.KEY_EVENT = 1;
MIDP.PEN_EVENT = 2;
MIDP.PRESSED = 1;
MIDP.RELEASED = 2;
MIDP.DRAGGED = 3;
MIDP.COMMAND_EVENT = 3;
MIDP.EVENT_QUEUE_SHUTDOWN = 31;

window.addEventListener("keypress", function(ev) {
    ev.preventDefault();
    MIDP.sendNativeEvent(MIDP.KEY_EVENT, MIDP.PRESSED, ev.which);
});

Native["com/sun/midp/events/EventQueue.getNativeEventQueueHandle.()I"] = function(ctx, stack) {
    var _this = stack.pop();
    stack.push(0);
}

Native["com/sun/midp/events/EventQueue.resetNativeEventQueue.()V"] = function(ctx, stack) {
    var _this = stack.pop();
}

Native["com/sun/midp/events/EventQueue.sendNativeEventToIsolate.(Lcom/sun/midp/events/NativeEvent;I)V"] = function(ctx, stack) {
    var isolate = stack.pop(), obj = stack.pop(), _this = stack.pop();
    MIDP.sendEvent(obj);
}

Native["com/sun/midp/events/NativeEventMonitor.waitForNativeEvent.(Lcom/sun/midp/events/NativeEvent;)I"] = function(ctx, stack) {
    if (MIDP.nativeEventQueue.length === 0) {
        MIDP.waitingNativeEventContext = ctx;
        throw VM.Pause;
    }
    MIDP.deliverWaitForNativeEventResult(ctx);
}

Native["com/sun/midp/events/NativeEventMonitor.readNativeEvent.(Lcom/sun/midp/events/NativeEvent;)Z"] = function(ctx, stack) {
    var obj = stack.pop();
    if (!MIDP.nativeEventQueue.length) {
        stack.push(0);
        return;
    }
    MIDP.copyEvent(obj);
    stack.push(1);
}

Native["com/sun/midp/l10n/LocalizedStringsBase.getContent.(I)Ljava/lang/String;"] = function(ctx, stack) {
    var id = stack.pop();
    var classInfo = CLASSES.getClass("com/sun/midp/i18n/ResourceConstants");
    var key;
    classInfo.fields.forEach(function(field) {
        if (classInfo.constant_pool[field.constantValue].integer === id)
            key = field.name;
    });
    var data = CLASSES.loadFile("assets/0/en-US.xml");
    if (!data || !key)
        ctx.raiseException("java/lang/IOException");
    var text = util.decodeUtf8(data);
    var xml = new window.DOMParser().parseFromString(text, "text/xml");
    var entries = xml.getElementsByTagName("localized_string");
    for (n = 0; n < entries.length; ++n) {
        var entry = entries[n];
        if (entry.attributes.Key.value === key) {
            stack.push(CLASSES.newString(entry.attributes.Value.value));
            return;
        }
    }
    ctx.raiseException("java/lang/IllegalStateException");
}

Native["javax/microedition/lcdui/Graphics.getPixel.(IIZ)I"] = function(ctx, stack) {
    var isGray = stack.pop(), gray = stack.pop(), rgb = stack.pop(), _this = stack.pop();
    stack.push(rgb);
}

Native["javax/microedition/lcdui/Display.drawTrustedIcon0.(IZ)V"] = function(ctx, stack) {
    var drawTrusted = stack.pop(), displayId = stack.pop(), _this = stack.pop();
}

Native["com/sun/midp/rms/RecordStoreRegistry.stopAllRecordStoreListeners.(I)V"] = function(ctx, stack) {
    var taskId = stack.pop();
}

Native["com/sun/midp/events/EventQueue.sendShutdownEvent.()V"] = function(ctx, stack) {
    var _this = stack.pop();
    var obj = CLASSES.newObject(CLASSES.getClass("com/sun/midp/events/NativeEvent"));
    obj.class.getField("type", "I").set(obj, MIDP.EVENT_QUEUE_SHUTDOWN);
    MIDP.sendEvent(obj);
}

Native["com/sun/midp/main/CommandState.saveCommandState.(Lcom/sun/midp/main/CommandState;)V"] = function(ctx, stack) {
    var commandState = stack.pop();
}

Native["com/sun/midp/main/CommandState.exitInternal.(I)V"] = function(ctx, stack) {
    console.log("Exit: " + stack.pop());
    throw VM.Pause;
}

Native["com/sun/midp/suspend/SuspendSystem$MIDPSystem.allMidletsKilled.()Z"] = function(ctx, stack) {
    var _this = stack.pop();
    stack.push(0);
}

Native["com/sun/midp/lcdui/DisplayDevice.setFullScreen0.(IIZ)V"] = function(ctx, stack) {
    var mode = stack.pop(), displayId = stack.pop(), hardwareId = stack.pop(), _this = stack.pop();
}

Native["com/sun/midp/lcdui/DisplayDevice.gainedForeground0.(II)V"] = function(ctx, stack) {
    var displayId = stack.pop(), hardwareId = stack.pop(), _this = stack.pop();
}

Native["com/sun/midp/lcdui/DisplayDeviceAccess.vibrate0.(IZ)Z"] = function(ctx, stack) {
    var on = stack.pop(), displayId = stack.pop(), _this = stack.pop();
    stack.push(1);
}

Native["com/sun/midp/lcdui/DisplayDeviceAccess.isBacklightSupported0.(I)Z"] = function(ctx, stack) {
    var displayId = stack.pop(), _this = stack.pop();
    stack.push(1);
}

MIDP.HCENTER = 1;
MIDP.VCENTER = 2;
MIDP.LEFT = 4;
MIDP.RIGHT = 8;
MIDP.TOP = 16;
MIDP.BOTTOM = 32;
MIDP.BASELINE = 64;

MIDP.withClip = function(g, x, y, cb) {
    var clipX1 = g.class.getField("clipX1", "S").get(g),
        clipY1 = g.class.getField("clipY1", "S").get(g),
        clipX2 = g.class.getField("clipX2", "S").get(g),
        clipY2 = g.class.getField("clipY2", "S").get(g),
        clipped = g.class.getField("clipped", "Z").get(g),
        transX = g.class.getField("transX", "I").get(g),
        transY = g.class.getField("transY", "I").get(g);
    var ctx = MIDP.Context2D;
    if (clipped) {
        ctx.save();
        ctx.beginPath();
        ctx.rect(clipX1, clipY1, clipX2 - clipX1, clipY2 - clipY1);
        ctx.clip();
    }
    if (transX || transY)
        ctx.translate(transX, transY);
    cb(x, y);
    if (clipped) {
        ctx.restore();
    }
}

MIDP.withAnchor = function(g, anchor, x, y, w, h, cb) {
    MIDP.withClip(g, x, y, function(x, y) {
        if (anchor & MIDP.RIGHT)
            x -= w;
        if (anchor & MIDP.HCENTER)
            x -= (w/2)|0;
        if (anchor & MIDP.BOTTOM)
            y -= h;
        if (anchor & MIDP.VCENTER)
            y -= (h/2)|0;
        cb(x, y);
    });
}

MIDP.withFont = function(font, str, cb) {
    var ctx = MIDP.Context2D;
    ctx.font = font.css;
    cb(ctx.measureText(str).width | 0);
}

MIDP.withTextAnchor = function(g, anchor, x, y, str, cb) {
    MIDP.withClip(g, x, y, function(x, y) {
        MIDP.withFont(g.class.getField("currentFont", "Ljavax/microedition/lcdui/Font;").get(g), str, function(w) {
            var ctx = MIDP.Context2D;
            ctx.textAlign = "left";
            ctx.textBaseline = "top";
            if (anchor & MIDP.RIGHT)
                x -= w;
            if (anchor & MIDP.HCENTER)
                x -= (w/2)|0;
            if (anchor & MIDP.BOTTOM)
                ctx.textBaseline = "bottom";
            if (anchor & MIDP.VCENTER)
                ctx.textBaseline = "middle";
            if (anchor & MIDP.BASELINE)
                ctx.textBaseline = "alphabetic";
            cb(x, y, w);
        });
    });
}

MIDP.withPixel = function(g, cb) {
    var pixel = g.class.getField("pixel", "I").get(g);
    var style = "#" + ("00000" + pixel.toString(16)).slice(-6);
    MIDP.Context2D.fillStyle = style;
    MIDP.Context2D.strokeStyle = style;
    cb();
}

Native["javax/microedition/lcdui/Graphics.render.(Ljavax/microedition/lcdui/Image;III)Z"] = function(ctx, stack) {
    var anchor = stack.pop(), y = stack.pop(), x = stack.pop(), image = stack.pop(), _this = stack.pop(),
        img = image.class.getField("imageData", "Ljavax/microedition/lcdui/ImageData;").get(image),
        imgData = img.class.getField("nativeImageData", "I").get(img);
    MIDP.withAnchor(_this, anchor, x, y, imgData.width, imgData.height, function(x, y) {
        MIDP.Context2D.drawImage(imgData, x, y);
    });
    stack.push(1);
}

Native["javax/microedition/lcdui/Graphics.drawString.(Ljava/lang/String;III)V"] = function(ctx, stack) {
    var anchor = stack.pop(), y = stack.pop(), x = stack.pop(), str = util.fromJavaString(stack.pop()), _this = stack.pop();
    MIDP.withTextAnchor(_this, anchor, x, y, str, function(x, y) {
        MIDP.withPixel(_this, function() {
            MIDP.Context2D.fillText(str, x, y);
        });
    });
}

Native["javax/microedition/lcdui/Graphics.drawChars.([CIIIII)V"] = function(ctx, stack) {
    var anchor = stack.pop(), y = stack.pop(), x = stack.pop(),
        len = stack.pop(), offset = stack.pop(), data = stack.pop(), _this = stack.pop(),
        str = util.fromJavaChars(data, offset, len);
    MIDP.withTextAnchor(_this, anchor, x, y, str, function(x, y) {
        MIDP.withPixel(_this, function() {
            MIDP.Context2D.fillText(str, x, y);
        });
    });
}

Native["javax/microedition/lcdui/Graphics.fillRect.(IIII)V"] = function(ctx, stack) {
    var height = stack.pop(), width = stack.pop(), y = stack.pop(), x = stack.pop(), _this = stack.pop();
    MIDP.withClip(_this, x, y, function(x, y) {
        MIDP.withPixel(_this, function() {
            MIDP.Context2D.fillRect(x, y, width, height);
        });
    });
}

Native["javax/microedition/lcdui/Graphics.drawRect.(IIII)V"] = function(ctx, stack) {
    var h = stack.pop(), w = stack.pop(), y = stack.pop(), x = stack.pop(), _this = stack.pop();
    MIDP.withClip(_this, x, y, function(x, y) {
        MIDP.withPixel(_this, function() {
            MIDP.Context2D.strokeRect(x, y, Math.max(w, 1), Math.max(h, 1));
        });
    });
}

Native["javax/microedition/lcdui/Graphics.fillRoundRect.(IIIIII)V"] = function(ctx, stack) {
    var arcHeight = stack.pop(), arcWidth = stack.pop(),
        height = stack.pop(), width = stack.pop(),
        y = stack.pop(), x = stack.pop(), _this = stack.pop();
    MIDP.withClip(_this, x, y, function(x, y) {
        MIDP.withPixel(_this, function() {
            // TODO implement rounding
            MIDP.Context2D.fillRect(x, y, Math.max(width, 1), Math.max(height, 1));
        });
    });
}

MIDP.FACE_SYSTEM = 0;
MIDP.FACE_MONOSPACE = 32;
MIDP.FACE_PROPORTIONAL = 64;
MIDP.STYLE_PLAIN = 0;
MIDP.STYLE_BOLD = 1;
MIDP.STYLE_ITALIC = 2;
MIDP.STYLE_UNDERLINED = 4;
MIDP.SIZE_SMALL = 8;
MIDP.SIZE_MEDIUM = 0;
MIDP.SIZE_LARGE = 16;

Native["javax/microedition/lcdui/Font.init.(III)V"] = function(ctx, stack) {
    var size = stack.pop(), style = stack.pop(), face = stack.pop(), _this = stack.pop();
    var defaultSize = Math.max(12, (MIDP.Context2D.canvas.height / 40) | 0);
    if (size & MIDP.SIZE_SMALL)
        size = defaultSize / 1.5;
    else if (size & MIDP.SIZE_LARGE)
        size = defaultSize * 1.5;
    else
        size = defaultSize;
    size |= 0;

    if (style & MIDP.STYLE_BOLD)
        style = "bold";
    else if (style & MIDP.STYLE_ITALIC)
        style = "italic";
    else
        style = "";

    if (face & MIDP.MONOSPACE)
        face = "monospace";
    else if (face & MIDP.PROPORTIONAL)
        face = "san-serif";
    else
        face = "arial";

    _this.class.getField("baseline", "I").set(_this, (size/2)|0);
    _this.class.getField("height", "I").set(_this, (size * 1.3)|0);
    _this.css = style + " " + size + "pt " + face;
}

Native["javax/microedition/lcdui/Font.stringWidth.(Ljava/lang/String;)I"] = function(ctx, stack) {
    var str = util.fromJavaString(stack.pop()), _this = stack.pop();
    MIDP.withFont(_this, str, function(w) {
        stack.push(w);
    });
}

Native["javax/microedition/lcdui/Font.charWidth.(C)I"] = function(ctx, stack) {
    var str = String.fromCharCode(stack.pop()), _this = stack.pop();
    MIDP.withFont(_this, str, function(w) {
        stack.push(w);
    });
}

Native["javax/microedition/lcdui/Font.charsWidth.([CII)I"] = function(ctx, stack) {
    var len = stack.pop(), offset = stack.pop(), str = util.fromJavaChars(stack.pop()), _this = stack.pop();
    MIDP.withFont(_this, str.slice(offset, offset + len), function(w) {
        stack.push(w);
    });
}

Native["javax/microedition/lcdui/Font.substringWidth.(Ljava/lang/String;II)I"] = function(ctx, stack) {
    var len = stack.pop(), offset = stack.pop(), str = util.fromJavaString(stack.pop()), _this = stack.pop();
    MIDP.withFont(_this, str.slice(offset, offset + len), function(w) {
        stack.push(w);
    });
}

Native["com/sun/midp/lcdui/DisplayDevice.refresh0.(IIIIII)V"] = function(ctx, stack) {
    var y2 = stack.pop(), x2 = stack.pop(), y1 = stack.pop(), x1 = stack.pop(),
        displayId = stack.pop(), hardwareId = stack.pop(), _this = stack.pop();
}


Native["com/sun/midp/chameleon/input/InputModeFactory.getInputModeIds.()[I"] = function(ctx, stack) {
    var ids = CLASSES.newPrimitiveArray("I", 1);
    ids[0] = 1; // KEYBOARD_INPUT_MODE
    stack.push(ids);
}

MIDP.TRANS_NONE = 0;
MIDP.TRANS_MIRROR_ROT180 = 1;
MIDP.TRANS_MIRROR = 2;
MIDP.TRANS_MIRROR_ROT90 = 3;
MIDP.TRANS_MIRROR_ROT270 = 4;
MIDP.TRANS_MIRROR_ROT90 = 5;
MIDP.TRANS_MIRROR_ROT270 = 6;
MIDP.TRANS_MIRROR_ROT90 = 7;

Native["javax/microedition/lcdui/Graphics.renderRegion.(Ljavax/microedition/lcdui/Image;IIIIIIII)Z"] = function(ctx, stack) {
    var anchor = stack.pop(), y = stack.pop(), x = stack.pop(),
        transform = stack.pop(), sh = stack.pop(), sw = stack.pop(), sy = stack.pop(), sx = stack.pop(), image = stack.pop(), _this = stack.pop(),
        img = image.class.getField("imageData", "Ljavax/microedition/lcdui/ImageData;").get(image),
        imgData = img.class.getField("nativeImageData", "I").get(img);
    var w = sw, h = sh;
    if (transform >= 4) {
        w = sh;
        h = sw;
    }
    MIDP.withAnchor(_this, anchor, x, y, w, h, function(x, y) {
        var ctx = MIDP.Context2D;
        ctx.translate(w/2, h/2);
        if (transform === MIDP.TRANS_MIRROR || transform === MIDP.TRANS_MIRROR_ROT180)
            ctx.scale(-1, 1);
        if (transform === MIDP.TRANS_MIRROR_ROT90 || transform === MIDP.TRANS_MIRROR_ROT270)
            ctx.scale(1, -1);
        if (transform === MIDP.TRANS_ROT90 || transform === MIDP.TRANS_MIRROR_ROT90)
            ctx.rotate(Math.PI / 2);
        if (transform === MIDP.TRANS_ROT180 || transform === MIDP.TRANS_MIRROR_ROT180)
            ctx.rotate(Math.PI);
        if (transform === MIDP.TRANS_ROT270 || transform === MIDP.TRANS_MIRROR_ROT270)
            ctx.rotate(1.5 * Math.PI);
        MIDP.Context2D.drawImage(imgData, sx, sy, w, h, -w / 2, -h / 2, sw, sh);
    });
    stack.push(1);
}

Native["javax/microedition/lcdui/Graphics.drawLine.(IIII)V"] = function(ctx, stack) {
    var y2 = stack.pop(), x2 = stack.pop(), y1 = stack.pop(), x1 = stack.pop(), _this = stack.pop(),
        dx = x2 - x1, dy = y2 - y1;
    MIDP.withClip(_this, x1, y1, function(x, y) {
        var ctx = MIDP.Context2D;
        ctx.beginPath();
        ctx.moveTo(x, y);
        ctx.lineTo(x + dx, y + dy);
        ctx.stroke();
        ctx.closePath();
    });
}

Native["javax/microedition/lcdui/KeyConverter.getSystemKey.(I)I"] = function(ctx, stack) {
    var key = stack.pop();
    /* We don't care about the system keys POWER, SEND, END, SELECT,
      SOFT_BUTTON1, SOFT_BUTTON2, DEBUG_TRACE1, CLAMSHELL_OPEN, CLAMSHELL_CLOSE,
      but we do care about SYSTEM_KEY_CLEAR, so send it when the delete key is pressed.
    */
    if (key === 8) {
        stack.push(4)
    } else {
        stack.push(0);
    }
}

Native["com/sun/midp/io/j2me/push/ConnectionRegistry.checkInByMidlet0.(ILjava/lang/String;)V"] = function(ctx, stack) {
    var className = stack.pop(), suiteId = stack.pop();
}

Native["javax/microedition/lcdui/KeyConverter.getGameAction.(I)I"] = function(ctx, stack) {
    var keyCode = stack.pop();
    stack.push(0);
}

