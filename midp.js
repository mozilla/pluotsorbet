/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'Use strict';

var MIDP = {
    commandState: {
        suiteId: 1
    },
    suites: [
        null,
        {
            midletClassName: "HelloCommandMIDlet",
        }
    ]
};

Native["com/sun/midp/log/LoggingBase.report.(IILjava/lang/String;)V"] = function(ctx, stack) {
    var message = stack.pop(), channelID = stack.pop(), severity = stack.pop();
    console.info(util.fromJavaString(message));
}

Native.groupTBL = [
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
    var list = CLASSES.newArray("[Ljava/lang/String;", Native.groupTBL.length);
    Native.groupTBL.forEach(function (e, n) {
        list[n] = CLASSES.newString(e);
    });
    stack.push(list);
}

Native.messagesTBL = [
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
    Native.groupTBL.forEach(function(e, n) {
        if (e === name) {
            var messages = Native.messagesTBL[n];
            list = CLASSES.newArray("[Ljava/lang/String;", messages.length);
            messages.forEach(function (e, n) {
                list[n] = CLASSES.newString(e);
            });
        }
    });
    stack.push(list);
}

Native.membersTBL = [
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
    Native.groupTBL.forEach(function(e, n) {
        if (e === name) {
            var members = Native.membersTBL[n];
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
    var suiteId = MIDP.commandState.suiteId;
    var suite = MIDP.suites[suiteId];
    state["com/sun/midp/main/CommandState$midletClassName"] = CLASSES.newString(suite.midletClassName);
    state["com/sun/midp/main/CommandState$arg0"] = CLASSES.newString("");
    state["com/sun/midp/main/CommandState$arg1"] = CLASSES.newString("");
    state["com/sun/midp/main/CommandState$arg2"] = CLASSES.newString("");
    state["com/sun/midp/main/CommandState$suiteId"] = suiteId;
}

Native.domainTBL = [
    "manufacturer",
    "operator",
    "identified_third_party",
    "unidentified_third_party,unsecured",
    "minimum,unsecured",
    "maximum,unsecured",
];

Native["com/sun/midp/security/Permissions.loadDomainList.()[Ljava/lang/String;"] = function(ctx, stack) {
    var list = CLASSES.newArray("[Ljava/lang/String;", Native.domainTBL.length);
    Native.domainTBL.forEach(function (e, n) {
        list[n] = CLASSES.newString(e);
    });
    stack.push(list);
}

Native.NEVER = 0;
Native.ALLOW = 1;
Native.BLANKET = 4;
Native.SESSION = 8;
Native.ONESHOT = 16;

Native.identifiedTBL = {
    net_access: { max: Native.BLANKET, default: Native.SESSION},
    low_level_net_access: { max: Native.BLANKET, default: Native.SESSION},
    call_control: { max: Native.BLANKET, default: Native.ONESHOT},
    application_auto_invocation: { max: Native.BLANKET, default: Native.ONESHOT},
    local_connectivity: { max: Native.BLANKET, default: Native.SESSION},
    messaging: { max: Native.BLANKET, default: Native.ONESHOT},
    restricted_messaging: { max: Native.BLANKET, default: Native.ONESHOT},
    multimedia_recording: { max: Native.BLANKET, default: Native.SESSION},
    read_user_data_access: { max: Native.BLANKET, default: Native.ONESHOT},
    write_user_data_access: { max: Native.BLANKET, default: Native.ONESHOT},
    location: { max: Native.BLANKET, default: Native.SESSION},
    landmark: { max: Native.BLANKET, default: Native.SESSION},
    payment: { max: Native.ALLOW,   default: Native.ALLOW},
    authentication: { max: Native.BLANKET, default: Native.SESSION},
    smart_card: { max: Native.BLANKET, default: Native.SESSION},
    satsa: { max: Native.NEVER,   default: Native.NEVER},
};

Native.unidentifiedTBL = {
    net_access: { max: Native.SESSION, default: Native.ONESHOT},
    low_level_net_access: { max: Native.SESSION, default: Native.ONESHOT},
    call_control: { max: Native.ONESHOT, default: Native.ONESHOT},
    application_auto_invocation: { max: Native.SESSION, default: Native.ONESHOT},
    local_connectivity: { max: Native.BLANKET, default: Native.ONESHOT},
    messaging: { max: Native.ONESHOT, default: Native.ONESHOT},
    restricted_messaging: { max: Native.ONESHOT, default: Native.ONESHOT},
    multimedia_recording: { max: Native.SESSION, default: Native.ONESHOT},
    read_user_data_access: { max: Native.ONESHOT, default: Native.ONESHOT},
    write_user_data_access: { max: Native.ONESHOT, default: Native.ONESHOT},
    location: { max: Native.SESSION, default: Native.ONESHOT},
    landmark: { max: Native.SESSION, default: Native.ONESHOT},
    payment: { max: Native.NEVER,   default: Native.NEVER},
    authentication: { max: Native.NEVER,   default: Native.NEVER},
    smart_card: { max: Native.NEVER,   default: Native.NEVER},
    satsa: { max: Native.NEVER,   default: Native.NEVER},
};

Native["com/sun/midp/security/Permissions.getDefaultValue.(Ljava/lang/String;Ljava/lang/String;)B"] = function(ctx, stack) {
    var group = util.fromJavaString(stack.pop()), domain = util.fromJavaString(stack.pop());
    var allow = Native.NEVER;
    switch (domain) {
    case "manufacturer":
    case "maximum":
    case "operator":
        allow = Native.ALLOW;
        break;
    case "identified_third_party":
        allow = Native.identifiedTBL[group].default;
        break;
    case "unidentified_third_party":
        allow = Native.unidentifiedTBL[group].default;
        break;
    }
    stack.push(allow);
}

Native["com/sun/midp/security/Permissions.getMaxValue.(Ljava/lang/String;Ljava/lang/String;)B"] = function(ctx, stack) {
    var group = util.fromJavaString(stack.pop()), domain = util.fromJavaString(stack.pop());
    var allow = Native.NEVER;
    switch (domain) {
    case "manufacturer":
    case "maximum":
    case "operator":
        allow = Native.ALLOW;
        break;
    case "identified_third_party":
        allow = Native.identifiedTBL[group].max;
        break;
    case "unidentified_third_party":
        allow = Native.unidentifiedTBL[group].max;
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

Native["com/sun/midp/events/EventQueue.getNativeEventQueueHandle.()I"] = function(ctx, stack) {
    stack.push(0);
}

Native["com/sun/midp/events/EventQueue.resetNativeEventQueue.()V"] = function(ctx, stack) {
}

Native["com/sun/midp/events/EventQueue.sendNativeEventToIsolate.(Lcom/sun/midp/events/NativeEvent;I)V"] = function(ctx, stack) {
    var isolate = stack.pop(), obj = stack.pop();
    Native.sendEvent(obj);
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
    Native.skinFileData = new DataView(data);
    Native.skinFilePos = 0;
}

Native["com/sun/midp/chameleon/skins/resources/LoadedSkinData.readByteArray.(I)[B"] = function(ctx, stack) {
    var len = stack.pop();
    if (!Native.skinFileData || (Native.skinFilePos + len) > Native.skinFileData.byteLength)
        ctx.raiseException("java/lang/IllegalStateException");
    var bytes = CLASSES.newPrimitiveArray("B", len);
    for (var n = 0; n < len; ++n) {
        bytes[n] = Native.skinFileData.getUint8(Native.skinFilePos++);
    }
    stack.push(bytes);
}

Native["com/sun/midp/chameleon/skins/resources/LoadedSkinData.readIntArray.()[I"] = function(ctx, stack) {
    if (!Native.skinFileData || (Native.skinFilePos + 4) > Native.skinFileData.byteLength)
        ctx.raiseException("java/lang/IllegalStateException");
    var len = Native.skinFileData.getInt32(Native.skinFilePos, true);
    Native.skinFilePos += 4;
    var ints = CLASSES.newPrimitiveArray("I", len);
    for (var n = 0; n < len; ++n) {
        if ((Native.skinFilePos + 4) > Native.skinFileData.byteLength)
            ctx.raiseException("java/lang/IllegalStateException");
        ints[n] = Native.skinFileData.getInt32(Native.skinFilePos, true);
        Native.skinFilePos += 4;
    }
    stack.push(ints);
}

Native.STRING_ENCODING_USASCII = 0;
Native.STRING_ENCODING_UTF8 = 1;

Native["com/sun/midp/chameleon/skins/resources/LoadedSkinData.readStringArray.()[Ljava/lang/String;"] = function(ctx, stack) {
    if (!Native.skinFileData || (Native.skinFilePos + 4) > Native.skinFileData.byteLength)
        ctx.raiseException("java/lang/IllegalStateException");
    var len = Native.skinFileData.getInt32(Native.skinFilePos, true);
    Native.skinFilePos += 4;
    var strings = CLASSES.newArray("[Ljava/lang/String;", len);
    for (var n = 0; n < len; ++n) {
        if ((Native.skinFilePos + 2) > Native.skinFileData.byteLength)
            ctx.raiseException("java/lang/IllegalStateException");
        var strLen = Native.skinFileData.getUint8(Native.skinFilePos++);
        var strEnc = Native.skinFileData.getUint8(Native.skinFilePos++);
        if ((Native.skinFilePos + strLen) > Native.skinFileData.byteLength)
            ctx.raiseException("java/lang/IllegalStateException");
        var bytes = Native.skinFileData.buffer.slice(Native.skinFilePos, Native.skinFilePos + strLen);
        Native.skinFilePos += strLen;
        var str;
        if (strEnc === Native.STRING_ENCODING_USASCII) {
            var data = new Uint8Array(bytes);
            str = "";
            for (var i = 0; i < strLen; ++i)
                str += String.fromCharCode(data[i]);
        } else if (strEnc === Native.STRING_ENCODING_UTF8) {
            str = util.decodeUtf8(bytes);
        } else {
            ctx.raiseException("java/lang/IllegalStateException");
        }
        strings[n] = CLASSES.newString(str);
    }
    stack.push(strings);
}

Native["com/sun/midp/chameleon/skins/resources/LoadedSkinData.finishReadingSkinFile.()I"] = function(ctx, stack) {
    Native.skinFileData = null;
    Native.skinFilePos = 0;
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

Native["javax/microedition/lcdui/Font.init.(III)V"] = function(ctx, stack) {
    var size = stack.pop(), style = stack.pop(), face = stack.pop();
}

Native["javax/microedition/lcdui/ImageDataFactory.createImmutableImageDecodeImage.(Ljavax/microedition/lcdui/ImageData;[BII)V"] = function(ctx, stack) {
    var length = stack.pop(), offset = stack.pop(), bytes = stack.pop(), imageData = stack.pop();
    var blob = new Blob([bytes.buffer.slice(offset, offset + length)], { type: "image/png" });
    var img = new Image();
    img.src = URL.createObjectURL(blob);
    img.onload = function() {
        imageData["javax/microedition/lcdui/ImageData$width"] = img.naturalWidth;
        imageData["javax/microedition/lcdui/ImageData$height"] = img.naturalHeight;
        imageData["javax/microedition/lcdui/ImageData$nativeImageData"] = img;
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

Native["com/sun/cldchi/jvm/JVM.monotonicTimeMillis.()J"] = function(ctx, stack) {
    stack.push(Long.fromNumber(Date.now()));
}

Native["com/sun/midp/lcdui/DisplayDeviceContainer.getDisplayDevicesIds0.()[I"] = function(ctx, stack) {
    var ids = CLASSES.newPrimitiveArray("I", 1);
    ids[0] = 0;
    stack.push(ids);
}

Native["com/sun/midp/lcdui/DisplayDevice.getDisplayName0.(I)Ljava/lang/String;"] = function(ctx, stack) {
    var id = stack.pop();
    stack.push(null);
}

Native["com/sun/midp/lcdui/DisplayDevice.isDisplayPrimary0.(I)Z"] = function(ctx, stack) {
    var id = stack.pop();
    stack.push(1);
}

Native["com/sun/midp/lcdui/DisplayDevice.isbuildInDisplay0.(I)Z"] = function(ctx, stack) {
    var id = stack.pop();
    stack.push(1);
}

Native["com/sun/midp/lcdui/DisplayDevice.getDisplayCapabilities0.(I)I"] = function(ctx, stack) {
    var id = stack.pop();
    stack.push(0x3ff);
}

Native["com/sun/midp/lcdui/DisplayDevice.isDisplayPenSupported0.(I)Z"] = function(ctx, stack) {
    var id = stack.pop();
    stack.push(1);
}

Native["com/sun/midp/lcdui/DisplayDevice.isDisplayPenMotionSupported0.(I)Z"] = function(ctx, stack) {
    var id = stack.pop();
    stack.push(1);
}

Native["com/sun/midp/lcdui/DisplayDevice.getReverseOrientation0.(I)Z"] = function(ctx, stack) {
    var id = stack.pop();
    stack.push(0);
}

Native.Canvas = (function() {
    var c = document.getElementById("canvas");
    c.width = 320;
    c.height = 480;
    return c;  
})();

Native.Context2D = (function() {
    return Native.Canvas.getContext("2d");
})();

Native["com/sun/midp/lcdui/DisplayDevice.getScreenWidth0.(I)I"] = function(ctx, stack) {
    var id = stack.pop();
    stack.push(Native.Canvas.width);
}

Native["com/sun/midp/lcdui/DisplayDevice.getScreenHeight0.(I)I"] = function(ctx, stack) {
    var id = stack.pop();
    stack.push(Native.Canvas.height);
}

Native["com/sun/midp/lcdui/DisplayDevice.displayStateChanged0.(II)V"] = function(ctx, stack) {
    var sate = stack.pop(), hardwareId = stack.pop();
}

Native["com/sun/midp/midletsuite/MIDletSuiteStorage.loadSuitesIcons0.()I"] = function(ctx, stack) {
    stack.push(0);
}

Native["com/sun/midp/midletsuite/MIDletSuiteStorage.suiteExists.(I)Z"] = function(ctx, stack) {
    var id = stack.pop();
    stack.push(MIDP.suites[id] ? 1 : 0);
}

Native["com/sun/midp/midletsuite/MIDletSuiteImpl.lockMIDletSuite.(IZ)V"] = function(ctx, stack) {
    var lock = stack.pop(), id = stack.pop();
}

Native["com/sun/midp/midletsuite/SuiteSettings.load.()V"] = function(ctx, stack) {
}

Native["com/sun/midp/midletsuite/InstallInfo.load.()V"] = function(ctx, stack) {
}

Native["com/sun/midp/midletsuite/SuiteProperties.load.()[Ljava/lang/String;"] = function(ctx, stack) {
    stack.push(CLASSES.newArray("[Ljava/lang/String;", 0));
}

Native.nativeEventQueue = [];

Native.copyEvent = function(obj) {
    var e = Native.nativeEventQueue.pop();
    obj["com/sun/midp/events/Event$type"] = e["com/sun/midp/events/Event$type"];
    Object.keys(e).forEach(function (fieldName) {
        if (fieldName.indexOf("com/sun/midp/events/NativeEvent$") === 0)
            obj[fieldName] = e[fieldName];
    });
}

Native.deliverWaitForNativeEventResult = function(ctx) {
    var stack = ctx.current().stack;
    var obj = stack.pop();
    if (Native.nativeEventQueue.length > 0)
        Native.copyEvent(obj);
    stack.push(Native.nativeEventQueue.length);
}

Native.sendEvent = function(obj) {
    var e = { "com/sun/midp/events/Event$type": obj["com/sun/midp/events/Event$type"] };
    Object.keys(obj).forEach(function (fieldName) {
        if (fieldName.indexOf("com/sun/midp/events/NativeEvent$") === 0)
            e[fieldName] = obj[fieldName];
    });
    Native.nativeEventQueue.push(e);
    var ctx = Native.waitingNativeEventContext;
    if (!ctx)
        return;
    Native.deliverWaitForNativeEventResult(Native.waitingNativeEventContext);
    Native.waitingNativeEventContext.resume();
    Native.waitingNativeEventContext = null;
}

MIDP.KEY_EVENT = 1;
MIDP.EVENT_QUEUE_SHUTDOWN = 31;

window.addEventListener("keypress", function(ev) {
    Native.sendEvent({ "com/sun/midp/events/Event$type": MIDP.KEY_EVENT,
                       "com/sun/midp/events/NativeEvent$intParam1": 1 /* PRESSED */,
                       "com/sun/midp/events/NativeEvent$intParam2": ev.charCode,
                       "com/sun/midp/events/NativeEvent$intParam4": 0 /* Display ID */ });
});

Native["com/sun/midp/events/NativeEventMonitor.waitForNativeEvent.(Lcom/sun/midp/events/NativeEvent;)I"] = function(ctx, stack) {
    if (Native.nativeEventQueue.length === 0) {
        Native.waitingNativeEventContext = ctx;
        throw VM.Pause;
    }
    Native.deliverWaitForNativeEventResult(ctx);
}

Native["com/sun/midp/events/NativeEventMonitor.readNativeEvent.(Lcom/sun/midp/events/NativeEvent;)Z"] = function(ctx, stack) {
    var obj = stack.pop();
    if (!Native.nativeEventQueue.length) {
        stack.push(0);
        return;
    }
    Native.copyEvent(obj);
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
    var isGray = stack.pop(), gray = stack.pop(), rgb = stack.pop();
    stack.push(rgb);
}

Native["javax/microedition/lcdui/Display.drawTrustedIcon0.(IZ)V"] = function(ctx, stack) {
    var drawTrusted = stack.pop(), displayId = stack.pop();
}

Native["com/sun/midp/rms/RecordStoreRegistry.stopAllRecordStoreListeners.(I)V"] = function(ctx, stack) {
    var taskId = stack.pop();
}

Native["com/sun/midp/events/EventQueue.sendShutdownEvent.()V"] = function(ctx, stack) {
    Native.sendEvent({ type: MIDP.EVENT_QUEUE_SHUTDOWN });
}

Native["com/sun/midp/main/CommandState.saveCommandState.(Lcom/sun/midp/main/CommandState;)V"] = function(ctx, stack) {
    var commandState = stack.pop();
}

Native["com/sun/midp/main/CommandState.exitInternal.(I)V"] = function(ctx, stack) {
    console.log("Exit: " + stack.pop());
    throw VM.Pause;
}

Native["com/sun/midp/suspend/SuspendSystem$MIDPSystem.allMidletsKilled.()Z"] = function(ctx, stack) {
    stack.push(0);
}

Native["com/sun/midp/lcdui/DisplayDevice.setFullScreen0.(IIZ)V"] = function(ctx, stack) {
    var mode = stack.pop(), displayId = stack.pop(), hardwareId = stack.pop();
}

Native["com/sun/midp/lcdui/DisplayDevice.gainedForeground0.(II)V"] = function(ctx, stack) {
    var displayId = stack.pop(), hardwareId = stack.pop();
}

Native["com/sun/midp/lcdui/DisplayDeviceAccess.vibrate0.(IZ)Z"] = function(ctx, stack) {
    var on = stack.pop(), displayId = stack.pop();
    stack.push(1);
}

Native["com/sun/midp/lcdui/DisplayDeviceAccess.isBacklightSupported0.(I)Z"] = function(ctx, stack) {
    var displayId = stack.pop();
    stack.push(1);
}

MIDP.anchors = {
    HCENTER: 1,
    VCENTER: 2,
    LEFT: 4,
    RIGHT: 8,
    TOP: 16,
    BOTTOM: 32,
    BASELINE: 64    
}

Native.withAdjustedPosition = function(anchor, x, y, width, height, callback) {
    // LEFT and TOP: do nothing
    if (anchor & MIDP.anchors.RIGHT) {
        x = Native.Canvas.width - width;
    }
    if (anchor & MIDP.anchors.BOTTOM) {
        y = Native.Canvas.height - height;
    }
    if (anchor & MIDP.anchors.HCENTER) {
        x = Native.Canvas.width / 2 - width / 2;
    }
    if (anchor & MIDP.anchors.VCENTER) {
        y = Native.Canvas.height / 2 - height / 2;
    }
    if (anchor & MIDP.anchors.BASELINE) {
        y = y - height;
    }
    callback(x, y);    
}

Native["javax/microedition/lcdui/Graphics.render.(Ljavax/microedition/lcdui/Image;III)Z"] = function(ctx, stack) {
    var anchor = stack.pop(), y = stack.pop(), x = stack.pop(), image = stack.pop(), _this = stack.pop(),
        img = image["javax/microedition/lcdui/Image$imageData"]["javax/microedition/lcdui/ImageData$nativeImageData"];

    Native.withAdjustedPosition(anchor, x, y, img.width, img.height, function(anchorX, anchorY) {
        Native.Context2D.drawImage(img, anchorX, anchorY);
    })

    stack.push(1);
}

Native["javax/microedition/lcdui/Font.stringWidth.(Ljava/lang/String;)I"] = function(ctx, stack) {
    var str = util.fromJavaString(stack.pop()), _this = stack.pop(),
        metrics = Native.Context2D.measureText(str);
    stack.push(metrics.width);
}

Native["javax/microedition/lcdui/Graphics.drawString.(Ljava/lang/String;III)V"] = function(ctx, stack) {
    var anchor = stack.pop(), y = stack.pop(), x = stack.pop(), str = util.fromJavaString(stack.pop()), _this = stack.pop(),
        metrics = Native.Context2D.measureText(str).width;

    Native.withAdjustedPosition(anchor, x, y + 20, metrics.width, 20, function(anchorX, anchorY) {
        Native.Context2D.fillText(str, anchorX, anchorY);
    });
}

Native["javax/microedition/lcdui/Graphics.fillRect.(IIII)V"] = function(ctx, stack) {
    var height = stack.pop(), width = stack.pop(), y = stack.pop(), x = stack.pop(), _this = stack.pop();
    // TODO what color? Is it the color last passed to getPixel?
    Native.Context2D.fillStyle = "white";
    Native.Context2D.fillRect(x, y, width, height);
}

Native["javax/microedition/lcdui/Graphics.drawRect.(IIII)V"] = function(ctx, stack) {
    var height = stack.pop(), width = stack.pop(), y = stack.pop(), x = stack.pop(), _this = stack.pop();
    Native.Context2D.strokeStyle = "black";
    Native.Context2D.strokeRect(x, y, width, height);
}

Native["javax/microedition/lcdui/Graphics.drawChars.([CIIIII)V"] = function(ctx, stack) {
    var anchor = stack.pop(), y = stack.pop(), x = stack.pop(),
    len = stack.pop(), offset = stack.pop(), data = stack.pop(), _this = stack.pop(),
        str = "";

    for (var i in data) {
        if (typeof data[i] === "number") {
            str += String.fromCharCode(data[i]);
        }
    }

    var metrics = Native.Context2D.measureText(str).width;
    Native.withAdjustedPosition(anchor, x, y, metrics.width, 20, function(anchorX, anchorY) {
        Native.Context2D.fillStyle = "black";
        Native.Context2D.fillText(str, anchorX, anchorY);
    });
}

Native["javax/microedition/lcdui/Font.charWidth.(C)I"] = function(ctx, stack) {
    var str = String.fromCharCode(stack.pop()), _this = stack.pop(),
        metrics = Native.Context2D.measureText(str);
    stack.push(metrics.width);
}

Native["javax/microedition/lcdui/Font.substringWidth.(Ljava/lang/String;II)I"] = function(ctx, stack) {
    var len = stack.pop(), offset = stack.pop(), str = util.fromJavaString(stack.pop()), _this = stack.pop(),
        metrics = Native.Context2D.measureText(str.slice(offset, offset + len));

    stack.push(metrics.width);
}

Native["com/sun/midp/lcdui/DisplayDevice.refresh0.(IIIIII)V"] = function(ctx, stack) {
    var y2 = stack.pop(), x2 = stack.pop(), y1 = stack.pop(), x1 = stack.pop(), _this = stack.pop(),
        displayId = stack.pop(), hardwareId = stack.pop();

    console.log("refresh0 hardwareId:" + hardwareId + " displayId:" + displayId + " x1:" + x1 + " y1:" + y1 + " x2:" + x2 + " y2:" + y2);
}


Native["com/sun/midp/chameleon/input/InputModeFactory.getInputModeIds.()[I"] = function(ctx, stack) {
    var inputModeIds = CLASSES.newPrimitiveArray("I", 0);
    // TODO We want to return [1] here for KEYBOARD_INPUT_MODE but it causes a vm crash
    stack.push(inputModeIds);
}


