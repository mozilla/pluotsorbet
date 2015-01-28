/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var MIDP = {
};

MIDP.manifest = {};

Native["com/sun/midp/jarutil/JarReader.readJarEntry0.(Ljava/lang/String;Ljava/lang/String;)[B"] = function(jar, entryName) {
    var bytes = CLASSES.loadFileFromJar(util.fromJavaString(jar), util.fromJavaString(entryName));
    if (!bytes)
        throw $.newIOException();
    var length = bytes.byteLength;
    var data = new Uint8Array(bytes);
    var array = J2ME.newByteArray(length);
    for (var n = 0; n < length; ++n)
        array[n] = data[n];
    return array;
};

Native["com/sun/midp/log/LoggingBase.report.(IILjava/lang/String;)V"] = function(severity, channelID, message) {
    console.info(util.fromJavaString(message));
};

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

Native["com/sun/midp/security/Permissions.loadGroupList.()[Ljava/lang/String;"] = function() {
    var list = J2ME.newStringArray(MIDP.groupTBL.length);
    MIDP.groupTBL.forEach(function (e, n) {
        list[n] = J2ME.newString(e);
    });
    return list;
};

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

Native["com/sun/midp/security/Permissions.getGroupMessages.(Ljava/lang/String;)[Ljava/lang/String;"] = function(jName) {
    var name = util.fromJavaString(jName);
    var list = null;
    MIDP.groupTBL.forEach(function(e, n) {
        if (e === name) {
            var messages = MIDP.messagesTBL[n];
            list = J2ME.newStringArray(messages.length);
            messages.forEach(function (e, n) {
                list[n] = J2ME.newString(e);
            });
        }
    });
    return list;
};

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

Native["com/sun/midp/security/Permissions.loadGroupPermissions.(Ljava/lang/String;)[Ljava/lang/String;"] = function(jName) {
    var name = util.fromJavaString(jName);
    var list = null;
    MIDP.groupTBL.forEach(function(e, n) {
        if (e === name) {
            var members = MIDP.membersTBL[n];
            list = J2ME.newStringArray(members.length);
            members.forEach(function (e, n) {
                list[n] = J2ME.newString(e);
            });
        }
    });
    return list;
};

Native["com/sun/midp/main/CldcPlatformRequest.dispatchPlatformRequest.(Ljava/lang/String;)Z"] = function(request) {
    request = util.fromJavaString(request);
    if (request.startsWith("http://") || request.startsWith("https://")) {
        if (request.endsWith(".jad")) {
            // The download will start after the MIDlet has terminated its execution.
            MIDP.pendingMIDletUpdate = request;
            return 1;
        } else {
            DumbPipe.close(DumbPipe.open("windowOpen", request));
        }
    } else if (request.startsWith("x-contacts:add?number=")) {
        new MozActivity({
            name: "new",
            data: {
                type: "webcontacts/contact",
                params: {
                    tel: request.substring(22),
                },
            },
        });
    } else {
      console.warn("com/sun/midp/main/CldcPlatformRequest.dispatchPlatformRequest.(Ljava/lang/String;)Z not implemented for: " + request);
    }

    return 0;
};

Native["com/sun/midp/main/CommandState.restoreCommandState.(Lcom/sun/midp/main/CommandState;)V"] = function(state) {
    var suiteId = (config.midletClassName === "internal") ? -1 : 1;
    state.klass.classInfo.getField("I.suiteId.I").set(state, suiteId);
    state.klass.classInfo.getField("I.midletClassName.Ljava/lang/String;").set(state, J2ME.newString(config.midletClassName));
    var args = config.args;
    state.klass.classInfo.getField("I.arg0.Ljava/lang/String;").set(state, J2ME.newString((args.length > 0) ? args[0] : ""));
    state.klass.classInfo.getField("I.arg1.Ljava/lang/String;").set(state, J2ME.newString((args.length > 1) ? args[1] : ""));
    state.klass.classInfo.getField("I.arg2.Ljava/lang/String;").set(state, J2ME.newString((args.length > 2) ? args[2] : ""));
};

MIDP.domainTBL = [
    "manufacturer",
    "operator",
    "identified_third_party",
    "unidentified_third_party,unsecured",
    "minimum,unsecured",
    "maximum,unsecured",
];

Native["com/sun/midp/security/Permissions.loadDomainList.()[Ljava/lang/String;"] = function() {
    var list = J2ME.newStringArray(MIDP.domainTBL.length);
    MIDP.domainTBL.forEach(function (e, n) {
        list[n] = J2ME.newString(e);
    });
    return list;
};

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

Native["com/sun/midp/security/Permissions.getDefaultValue.(Ljava/lang/String;Ljava/lang/String;)B"] = function(domain, group) {
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
};

Native["com/sun/midp/security/Permissions.getMaxValue.(Ljava/lang/String;Ljava/lang/String;)B"] = function(domain, group) {
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
};

Native["com/sun/midp/security/Permissions.loadingFinished.()V"] = function() {
    console.warn("Permissions.loadingFinished.()V not implemented");
};

Native["com/sun/midp/main/MIDletSuiteUtils.getIsolateId.()I"] = function() {
    return $.ctx.runtime.isolate.id;
};

Native["com/sun/midp/main/MIDletSuiteUtils.registerAmsIsolateId.()V"] = function() {
    MIDP.AMSIsolateId = $.ctx.runtime.isolate.id;
};

Native["com/sun/midp/main/MIDletSuiteUtils.getAmsIsolateId.()I"] = function() {
    return MIDP.AMSIsolateId;
};

Native["com/sun/midp/main/MIDletSuiteUtils.isAmsIsolate.()Z"] = function() {
    return MIDP.AMSIsolateId == $.ctx.runtime.isolate.id ? 1 : 0;
};

Native["com/sun/midp/main/MIDletSuiteUtils.vmBeginStartUp.(I)V"] = function(midletIsolateId) {
    // See DisplayContainer::createDisplayId, called by the LCDUIEnvironment constructor,
    // called by CldcMIDletSuiteLoader::createSuiteEnvironment.
    // The formula depens on the ID of the isolate that calls createDisplayId, that is
    // the same isolate that calls vmBeginStartUp. So this is a good place to calculate
    // the display ID.
    MIDP.displayId = ((midletIsolateId & 0xff)<<24) | (1 & 0x00ffffff);
};

Native["com/sun/midp/main/MIDletSuiteUtils.vmEndStartUp.(I)V"] = function(midletIsolateId) {
};

Native["com/sun/midp/main/AppIsolateMIDletSuiteLoader.allocateReservedResources0.()Z"] = function() {
  return 1;
};

Native["com/sun/midp/main/Configuration.getProperty0.(Ljava/lang/String;)Ljava/lang/String;"] = function(key) {
    var value;
    switch (util.fromJavaString(key)) {
    case "com.sun.midp.publickeystore.WebPublicKeyStore":
        if (config.midletClassName == "RunTests") {
          value = "_test.ks";
        } else {
          value = "_main.ks";
        }
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
    return J2ME.newString(value);
};

Native["com/sun/midp/util/ResourceHandler.loadRomizedResource0.(Ljava/lang/String;)[B"] = function(file) {
    var fileName = "assets/0/" + util.fromJavaString(file).replace("_", ".").replace("_png", ".png").replace("_raw", ".raw");
    var data = CLASSES.loadFile(fileName);
    if (!data) {
        console.warn("ResourceHandler::loadRomizedResource0: file " + fileName + " not found");
        return null;
    }
    var len = data.byteLength;
    var bytes = J2ME.newByteArray(len);
    var src = new Uint8Array(data);
    for (var n = 0; n < bytes.byteLength; ++n)
        bytes[n] = src[n];
    return bytes;
};

MIDP.ScreenHeight = 0;

MIDP.Context2D = (function() {
    var c = document.getElementById("canvas");

    if (config.autosize && !/no|0/.test(config.autosize)) {
      c.width = window.innerWidth;
      c.height = window.innerHeight;
      document.documentElement.classList.add('autosize');
    } else {
      document.documentElement.classList.add('debug-mode');
      c.width = 240;
      c.height = 320;
    }

    MIDP.ScreenHeight = c.height;

    function sendPenEvent(pt, whichType) {
        MIDP.sendNativeEvent({
            type: MIDP.PEN_EVENT,
            intParam1: whichType,
            intParam2: pt.x,
            intParam3: pt.y,
            intParam4: MIDP.displayId
        }, MIDP.foregroundIsolateId);
    }

    function sendGestureEvent(pt, distancePt, whichType, aFloatParam1, aIntParam7, aIntParam8, aIntParam9) {
        MIDP.sendNativeEvent({
            type: MIDP.GESTURE_EVENT,
            intParam1: whichType,
            intParam2: distancePt && distancePt.x || 0,
            intParam3: distancePt && distancePt.y || 0,
            intParam4: MIDP.displayId,
            intParam5: pt.x,
            intParam6: pt.y,
            floatParam1: aFloatParam1 || 0.0,
            intParam7: aIntParam7 || 0,
            intParam8: aIntParam8 || 0,
            intParam9: aIntParam9 || 0,
            intParam10: 0,
            intParam11: 0,
            intParam12: 0,
            intParam13: 0,
            intParam14: 0,
            intParam15: 0,
            intParam16: 0
        }, MIDP.foregroundIsolateId);
    }

    // In the simulator and on device, use touch events; in desktop
    // mode, we must use mouse events (unless you enable touch events
    // in devtools).
    var supportsTouch = ("ontouchstart" in document.documentElement);

    // Cache the canvas position for future computation.
    var canvasRect = c.getBoundingClientRect();
    c.addEventListener("canvasresize", function() {
        canvasRect = c.getBoundingClientRect();
    });

    function getEventPoint(event) {
        var item = ((event.touches && event.touches[0]) || // touchstart, touchmove
                    (event.changedTouches && event.changedTouches[0]) || // touchend
                    event); // mousedown, mouseup, mousemove
        return {
            x: item.pageX - (canvasRect.left | 0),
            y: item.pageY - (canvasRect.top | 0)
        };
    }

    // Input Handling: Some MIDlets (usually older ones) respond to
    // "pen" events; others respond to "gesture" events. We must fire
    // both. A distance threshold ensures that touches with an "intent
    // to tap" will likely result in a tap.

    var LONG_PRESS_TIMEOUT = 1000;
    var MIN_DRAG_DISTANCE_SQUARED = 5 * 5;
    var mouseDownInfo = null;
    var longPressTimeoutID = null;
    var longPressDetected = false;

    c.addEventListener(supportsTouch ? "touchstart" : "mousedown", function(event) {
        event.preventDefault(); // Prevent unnecessary fake mouse events.
        var pt = getEventPoint(event);
        sendPenEvent(pt, MIDP.PRESSED);
        mouseDownInfo = pt;

        longPressDetected = false;
        longPressTimeoutID = setTimeout(function() {
            longPressDetected = true;
            sendGestureEvent(pt, null, MIDP.GESTURE_LONG_PRESS);
        }, LONG_PRESS_TIMEOUT);
    });

    c.addEventListener(supportsTouch ? "touchmove" : "mousemove", function(event) {
        if (!mouseDownInfo) {
            return; // Mousemove on desktop; ignored.
        }
        event.preventDefault();

        if (longPressTimeoutID) {
            clearTimeout(longPressTimeoutID);
            longPressTimeoutID = null;
        }

        var pt = getEventPoint(event);
        sendPenEvent(pt, MIDP.DRAGGED);
        var distance = {
            x: pt.x - mouseDownInfo.x,
            y: pt.y - mouseDownInfo.y
        };
        // If this gesture is dragging, or we've moved a substantial
        // amount since the original "down" event, begin or continue a
        // drag event. Using squared distance to avoid needing sqrt.
        if (mouseDownInfo.isDragging ||
            (distance.x * distance.x + distance.y * distance.y > MIN_DRAG_DISTANCE_SQUARED)) {
            mouseDownInfo.isDragging = true;
            mouseDownInfo.x = pt.x;
            mouseDownInfo.y = pt.y;
            if (!longPressDetected) {
                sendGestureEvent(pt, distance, MIDP.GESTURE_DRAG);
            }
        }

        // Just store the dragging event info here, then calc the speed and
        // determine whether the gesture is GESTURE_DROP or GESTURE_FLICK in
        // the mouseup event listener.
        if (!mouseDownInfo.draggingPts) {
            mouseDownInfo.draggingPts = [];
        }

        // Only store the latest two drag events.
        if (mouseDownInfo.draggingPts.length > 1) {
            mouseDownInfo.draggingPts.shift();
        }

        mouseDownInfo.draggingPts.push({
            pt: getEventPoint(event),
            time: new Date().getTime()
        });
    });

    function calcFlickSpeed() {
        var currentDragPT = mouseDownInfo.draggingPts[1];
        var lastDragPT = mouseDownInfo.draggingPts[0];

        var deltaX = currentDragPT.pt.x - lastDragPT.pt.x;
        var deltaY = currentDragPT.pt.y - lastDragPT.pt.y;
        var deltaTimeInMs = currentDragPT.time - lastDragPT.time;

        var speedX = Math.round(deltaX * 1000 / deltaTimeInMs);
        var speedY = Math.round(deltaY * 1000 / deltaTimeInMs);
        var speed  = Math.round(Math.sqrt(speedX * speedX + speedY * speedY));

        var direction = 0;
        if (deltaX >= 0 && deltaY >=0) {
            direction = Math.atan(deltaY / deltaX);
        } else if (deltaX < 0 && deltaY >= 0) {
            direction = Math.PI + Math.atan(deltaY / deltaX);
        } else if (deltaX < 0 && deltaY < 0) {
            direction = Math.atan(deltaY / deltaX) - Math.PI;
        } else if (deltaX >= 0 && deltaY < 0) {
            direction = Math.atan(deltaY / deltaX);
        }

        return {
            direction: direction,
            speed: speed,
            speedX: speedX,
            speedY: speedY
        };
    }

    // The end listener goes on `document` so that we properly detect touchend/mouseup anywhere.
    document.addEventListener(supportsTouch ? "touchend" : "mouseup", function(event) {
        if (!mouseDownInfo) {
            return; // Touchstart wasn't on the canvas.
        }
        event.preventDefault();

        if (longPressTimeoutID) {
            clearTimeout(longPressTimeoutID);
            longPressTimeoutID = null;
        }

        var pt = getEventPoint(event);
        sendPenEvent(pt, MIDP.RELEASED);

        if (!longPressDetected) {
            if (mouseDownInfo.isDragging) {
                if (mouseDownInfo.draggingPts && mouseDownInfo.draggingPts.length == 2) {
                    var deltaTime = new Date().getTime() - mouseDownInfo.draggingPts[1].time;
                    var flickSpeed = calcFlickSpeed();
                    // On the real Nokia device, if user touch on the screen and
                    // move the finger, then stop moving for a while and lift
                    // the finger, it will trigger a normal GESTURE_DROP instead
                    // of GESTURE_FLICK event, so let's check if the time gap
                    // between touchend event and the last touchmove event is
                    // larger than a threshold.
                    if (deltaTime > 300 || flickSpeed.speed == 0) {
                        sendGestureEvent(pt, null, MIDP.GESTURE_DROP);
                    } else {
                        sendGestureEvent(pt, null, MIDP.GESTURE_FLICK,
                            flickSpeed.direction,
                            flickSpeed.speed,
                            flickSpeed.speedX,
                            flickSpeed.speedY);
                    }
                } else {
                    sendGestureEvent(pt, null, MIDP.GESTURE_DROP);
                }
            } else {
                sendGestureEvent(pt, null, MIDP.GESTURE_TAP);
            }
        }

        mouseDownInfo = null; // Clear the way for the next gesture.
    });

    return c.getContext("2d");
})();

Native["com/sun/midp/midletsuite/MIDletSuiteStorage.loadSuitesIcons0.()I"] = function() {
    return 0;
};

Native["com/sun/midp/midletsuite/MIDletSuiteStorage.suiteExists.(I)Z"] = function(id) {
    return id <= 1 ? 1 : 0;
};

Native["com/sun/midp/midletsuite/MIDletSuiteStorage.suiteIdToString.(I)Ljava/lang/String;"] = function(id) {
    return J2ME.newString(id.toString());
};

Native["com/sun/midp/midletsuite/MIDletSuiteStorage.getMidletSuiteStorageId.(I)I"] = function(suiteId) {
    // We should be able to use the same storage ID for all MIDlet suites.
    return 0; // storageId
};

Native["com/sun/midp/midletsuite/MIDletSuiteStorage.getMidletSuiteJarPath.(I)Ljava/lang/String;"] = function(id) {
    return J2ME.newString("");
};

Native["com/sun/midp/midletsuite/MIDletSuiteImpl.lockMIDletSuite.(IZ)V"] = function(id, lock) {
    console.warn("MIDletSuiteImpl.lockMIDletSuite.(IZ)V not implemented (" + id + ", " + lock + ")");
};

Native["com/sun/midp/midletsuite/MIDletSuiteImpl.unlockMIDletSuite.(I)V"] = function(suiteId) {
    console.warn("MIDletSuiteImpl.unlockMIDletSuite.(I)V not implemented (" + suiteId + ")");
};

Native["com/sun/midp/midletsuite/SuiteSettings.load.()V"] = function() {
    this.klass.classInfo.getField("I.pushInterruptSetting.B").set(this, 1);
    console.warn("com/sun/midp/midletsuite/SuiteSettings.load.()V incomplete");
};

Native["com/sun/midp/midletsuite/SuiteSettings.save0.(IBI[B)V"] = function(suiteId, pushInterruptSetting, pushOptions, permissions) {
    console.warn("SuiteSettings.save0.(IBI[B)V not implemented (" +
                 suiteId + ", " + pushInterruptSetting + ", " + pushOptions + ", " + permissions + ")");
};

Native["com/sun/midp/midletsuite/InstallInfo.load.()V"] = function() {
    // The MIDlet has to be trusted for opening SSL connections using port 443.
    this.klass.classInfo.getField("I.trusted.Z").set(this, 1);
    console.warn("com/sun/midp/midletsuite/InstallInfo.load.()V incomplete");
};

Native["com/sun/midp/midletsuite/SuiteProperties.load.()[Ljava/lang/String;"] = function() {
    var keys = Object.keys(MIDP.manifest);
    var arr = J2ME.newStringArray(keys.length * 2);
    var i = 0;
    keys.forEach(function(key) {
      arr[i++] = J2ME.newString(key);
      arr[i++] = J2ME.newString(MIDP.manifest[key]);
    });
    return arr;
};

Native["javax/microedition/lcdui/SuiteImageCacheImpl.loadAndCreateImmutableImageDataFromCache0.(Ljavax/microedition/lcdui/ImageData;ILjava/lang/String;)Z"] = function(imageData, suiteId, fileName) {
    // We're not implementing the cache because looks like it isn't used much.
    // In a MIDlet I've been testing for a few minutes, there's been only one hit.
    return 0;
};

MIDP.InterIsolateMutexes = [];
MIDP.LastInterIsolateMutexID = -1;

Native["com/sun/midp/util/isolate/InterIsolateMutex.getID0.(Ljava/lang/String;)I"] = function(jName) {
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
};

Native["com/sun/midp/util/isolate/InterIsolateMutex.lock0.(I)V"] = function(id) {
    var ctx = $.ctx;
    asyncImpl("V", new Promise(function(resolve, reject) {
        ctx.setAsCurrentContext();
        var mutex;
        for (var i = 0; i < MIDP.InterIsolateMutexes.length; i++) {
            if (MIDP.InterIsolateMutexes[i].id == id) {
                mutex = MIDP.InterIsolateMutexes[i];
                break;
            }
        }

        if (!mutex) {
            reject($.newIllegalStateException("Invalid mutex ID"));
            return;
        }

        if (!mutex.locked) {
            mutex.locked = true;
            mutex.holder = ctx.runtime.isolate.id;
            resolve();
            return;
        }

        if (mutex.holder == ctx.runtime.isolate.id) {
            reject($.newRuntimeException("Attempting to lock mutex twice within the same Isolate"));
            return;
        }

        mutex.waiting.push(function() {
            mutex.locked = true;
            mutex.holder = ctx.runtime.isolate.id;
            resolve();
        });
    }));
};

Native["com/sun/midp/util/isolate/InterIsolateMutex.unlock0.(I)V"] = function(id) {
    var mutex;
    for (var i = 0; i < MIDP.InterIsolateMutexes.length; i++) {
        if (MIDP.InterIsolateMutexes[i].id == id) {
            mutex = MIDP.InterIsolateMutexes[i];
            break;
        }
    }

    if (!mutex) {
        throw $.newIllegalStateException("Invalid mutex ID");
    }

    if (!mutex.locked) {
        throw $.newRuntimeException("Mutex is not locked");
    }

    if (mutex.holder !== $.ctx.runtime.isolate.id) {
        throw $.newRuntimeException("Mutex is locked by different Isolate");
    }

    mutex.locked = false;

    var firstWaiting = mutex.waiting.shift();
    if (firstWaiting) {
        firstWaiting();
    }
};

MIDP.exit = function(code) {
    $.pause("exit");
    DumbPipe.open("exit", null, function(message) {});
};

MIDP.pendingMIDletUpdate = null;

Native["com/sun/cldc/isolate/Isolate.stop.(II)V"] = function(code, reason) {
    console.info("Isolate stops with code " + code + " and reason " + reason);
    if (!MIDP.pendingMIDletUpdate) {
        MIDP.exit();
        return;
    }

    // Perform updating.
    var dialog = document.getElementById('download-progress-dialog').cloneNode(true);
    dialog.style.display = 'block';
    dialog.classList.add('visible');
    document.body.appendChild(dialog);

    performDownload(MIDP.pendingMIDletUpdate, dialog, function(data) {
        dialog.parentElement.removeChild(dialog);

        fs.remove("/midlet.jad");
        fs.create("/midlet.jad", new Blob([ data.jadData ]));
        fs.remove("/midlet.jar");
        fs.create("/midlet.jar", new Blob([ data.jarData ]));

        new Promise(function(resolve, reject) {
            fs.syncStore(resolve);
        }).then(function() {
            MIDP.pendingMIDletUpdate = null;
            DumbPipe.close(DumbPipe.open("alert", "Update completed!"));
            DumbPipe.close(DumbPipe.open("reload", {}));
        });
    });
};

// The foreground isolate will get the user events (keypresses, etc.)
MIDP.foregroundIsolateId;
MIDP.nativeEventQueues = {};
MIDP.waitingNativeEventQueue = {};

MIDP.copyEvent = function(obj, isolateId) {
    var e = MIDP.nativeEventQueues[isolateId].shift();
    obj.klass.classInfo.getField("I.type.I").set(obj, e.type);
    obj.klass.classInfo.fields.forEach(function(field) {
        if (e[field.name] === undefined) {
          return;
        }
        field.set(obj, e[field.name]);
    });
}

MIDP.deliverWaitForNativeEventResult = function(resolve, nativeEvent, isolateId) {
    if (MIDP.nativeEventQueues[isolateId].length > 0)
        MIDP.copyEvent(nativeEvent, isolateId);
    resolve(MIDP.nativeEventQueues[isolateId].length);
}

MIDP.sendEvent = function(obj, isolateId) {
    var e = { type: obj.klass.classInfo.getField("I.type.I").get(obj) };
    obj.klass.classInfo.fields.forEach(function(field) {
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
MIDP.MMAPI_EVENT = 45;
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
};

MIDP.keyRelease = function(keyCode) {
    if (!MIDP.suppressKeyEvents)
        MIDP.sendNativeEvent({ type: MIDP.KEY_EVENT, intParam1: MIDP.RELEASED, intParam2: keyCode, intParam3: 0, intParam4: MIDP.displayId }, MIDP.foregroundIsolateId);
};

window.addEventListener("keypress", function(ev) {
    MIDP.keyPress(ev.which);
});

window.addEventListener("keyup", function(ev) {
    MIDP.keyRelease(ev.which);
});

Native["com/sun/midp/events/EventQueue.getNativeEventQueueHandle.()I"] = function() {
    return 0;
};

Native["com/sun/midp/events/EventQueue.resetNativeEventQueue.()V"] = function() {
    console.warn("EventQueue.resetNativeEventQueue.()V not implemented");
};

Native["com/sun/midp/events/EventQueue.sendNativeEventToIsolate.(Lcom/sun/midp/events/NativeEvent;I)V"] =
function(obj, isolateId) {
    if (!MIDP.nativeEventQueues[isolateId]) {
      MIDP.nativeEventQueues[isolateId] = [];
    }

    MIDP.sendEvent(obj, isolateId);
};

Native["com/sun/midp/events/NativeEventMonitor.waitForNativeEvent.(Lcom/sun/midp/events/NativeEvent;)I"] =
function(nativeEvent) {
    var ctx = $.ctx;
    asyncImpl("I", new Promise(function(resolve, reject) {
        ctx.setAsCurrentContext();
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
    }));
};

Native["com/sun/midp/events/NativeEventMonitor.readNativeEvent.(Lcom/sun/midp/events/NativeEvent;)Z"] =
function(obj) {
    if (!MIDP.nativeEventQueues[$.ctx.runtime.isolate.id].length) {
        return 0;
    }
    MIDP.copyEvent(obj, $.ctx.runtime.isolate.id);
    return 1;
};

MIDP.localizedStrings = new Map();

Native["com/sun/midp/l10n/LocalizedStringsBase.getContent.(I)Ljava/lang/String;"] = function(id) {
    if (MIDP.localizedStrings.size === 0) {
        // First build up a mapping of field names to field IDs
        var classInfo = CLASSES.getClass("com/sun/midp/i18n/ResourceConstants");
        var constantsMap = new Map();
        classInfo.fields.forEach(function(field) {
          constantsMap.set(field.name, classInfo.constant_pool[field.constantValue].integer);
        });

        var data = CLASSES.loadFileFromJar("java/classes.jar", "assets/0/en-US.xml");
        if (!data)
            throw $.newIOException();

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
        throw $.newIllegalStateException();
    }

    return J2ME.newString(value);
};

Native["javax/microedition/lcdui/Display.drawTrustedIcon0.(IZ)V"] = function(displayId, drawTrusted) {
    console.warn("Display.drawTrustedIcon0.(IZ)V not implemented (" + displayId + ", " + drawTrusted + ")");
};

Native["com/sun/midp/events/EventQueue.sendShutdownEvent.()V"] = function() {
    var obj = J2ME.newObject(CLASSES.getClass("com/sun/midp/events/NativeEvent").klass);
    obj.klass.classInfo.getField("I.type.I").set(obj, MIDP.EVENT_QUEUE_SHUTDOWN);
    MIDP.sendEvent(obj, $.ctx.runtime.isolate.id);
};

Native["com/sun/midp/main/CommandState.saveCommandState.(Lcom/sun/midp/main/CommandState;)V"] = function(commandState) {
    console.warn("CommandState.saveCommandState.(L...CommandState;)V not implemented (" + commandState + ")");
};

Native["com/sun/midp/main/CommandState.exitInternal.(I)V"] = function(exit) {
    console.info("Exit: " + exit);
    MIDP.exit();
};

Native["com/sun/midp/suspend/SuspendSystem$MIDPSystem.allMidletsKilled.()Z"] = function() {
    console.warn("SuspendSystem$MIDPSystem.allMidletsKilled.()Z not implemented");
    return 0;
};

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

Native["javax/microedition/lcdui/KeyConverter.getSystemKey.(I)I"] = function(key) {
    return MIDP.systemKeyMap[key] || 0;
};

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

Native["javax/microedition/lcdui/KeyConverter.getKeyCode.(I)I"] = function(key) {
    return MIDP.keyMap[key] || 0;
};

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

Native["javax/microedition/lcdui/KeyConverter.getKeyName.(I)Ljava/lang/String;"] = function(keyCode) {
    return J2ME.newString((keyCode in MIDP.keyNames) ? MIDP.keyNames[keyCode] : String.fromCharCode(keyCode));
};

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

Native["javax/microedition/lcdui/KeyConverter.getGameAction.(I)I"] = function(keyCode) {
    return MIDP.gameKeys[keyCode] || 0;
};

Native["javax/microedition/lcdui/game/GameCanvas.setSuppressKeyEvents.(Ljavax/microedition/lcdui/Canvas;Z)V"] = function(canvas, suppressKeyEvents) {
    MIDP.suppressKeyEvents = suppressKeyEvents;
};

Native["com/sun/midp/main/MIDletProxyList.resetForegroundInNativeState.()V"] = function() {
    MIDP.displayId = -1;
};

Native["com/sun/midp/main/MIDletProxyList.setForegroundInNativeState.(II)V"] = function(isolateId, displayId) {
    MIDP.displayId = displayId;
    MIDP.foregroundIsolateId = isolateId;
};

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

Native["com/sun/midp/io/j2me/push/ConnectionRegistry.poll0.(J)I"] = function(time) {
    asyncImpl("I", new Promise(function(resolve, reject) {
        MIDP.ConnectionRegistry.waitForRegistration(function(id) {
            resolve(id);
        });
    }));
};

Native["com/sun/midp/io/j2me/push/ConnectionRegistry.add0.(Ljava/lang/String;)I"] = function(connection) {
    var values = util.fromJavaString(connection).split(',');

    console.warn("ConnectionRegistry.add0.(IL...String;)I isn't completely implemented");

    MIDP.ConnectionRegistry.addConnection({
        connection: values[0],
        midlet: values[1],
        filter: values[2],
        suiteId: values[3]
    });

    return 0;
};

Native["com/sun/midp/io/j2me/push/ConnectionRegistry.addAlarm0.([BJ)J"] = function(jMidlet, jTime) {
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
};

Native["com/sun/midp/io/j2me/push/ConnectionRegistry.getMIDlet0.(I[BI)I"] = function(handle, regentry, entrysz) {
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
};

Native["com/sun/midp/io/j2me/push/ConnectionRegistry.checkInByMidlet0.(ILjava/lang/String;)V"] = function(suiteId, className) {
    console.warn("ConnectionRegistry.checkInByMidlet0.(IL...String;)V not implemented (" +
                 suiteId + ", " + util.fromJavaString(className) + ")");
};

Native["com/sun/midp/io/j2me/push/ConnectionRegistry.checkInByName0.([B)I"] = function(name) {
    console.warn("ConnectionRegistry.checkInByName0.([B)V not implemented (" +
                 util.decodeUtf8(name) + ")");
    return 0;
};

Native["com/nokia/mid/ui/gestures/GestureInteractiveZone.isSupported.(I)Z"] = function(gestureEventIdentity) {
    console.warn("GestureInteractiveZone.isSupported.(I)Z not implemented (" + gestureEventIdentity + ")");
    return 0;
};

Native["com/sun/midp/security/SecurityHandler.checkPermission0.(II)Z"] = function(suiteId, permission) {
    return 1;
};

Native["com/sun/midp/security/SecurityHandler.checkPermissionStatus0.(II)I"] = function(suiteId, permission) {
    return 1;
};

Native["com/sun/midp/io/NetworkConnectionBase.initializeInternal.()V"] = function() {
    console.warn("NetworkConnectionBase.initializeInternal.()V not implemented");
};

Native["com/sun/j2me/content/RegistryStore.init.()Z"] = function() {
    console.warn("com/sun/j2me/content/RegistryStore.init.()Z not implemented");
    return 1;
};

Native["com/sun/j2me/content/RegistryStore.forSuite0.(I)Ljava/lang/String;"] = function(suiteID) {
    console.warn("com/sun/j2me/content/RegistryStore.forSuite0.(I)Ljava/lang/String; not implemented");
    return J2ME.newString("");
};

Native["com/sun/j2me/content/AppProxy.isInSvmMode.()Z"] = function() {
    console.warn("com/sun/j2me/content/AppProxy.isInSvmMode.()Z not implemented");
    return 0;
};

Native["com/sun/j2me/content/InvocationStore.setCleanup0.(ILjava/lang/String;Z)V"] =
    UnimplementedNative("com/sun/j2me/content/InvocationStore.setCleanup0.(ILjava/lang/String;Z)V");

Native["com/sun/j2me/content/InvocationStore.get0.(Lcom/sun/j2me/content/InvocationImpl;ILjava/lang/String;IZ)I"] =
    UnimplementedNative("com/sun/j2me/content/InvocationStore.get0.(Lcom/sun/j2me/content/InvocationImpl;ILjava/lang/String;IZ)I", 0);

Native["com/sun/j2me/content/InvocationStore.getByTid0.(Lcom/sun/j2me/content/InvocationImpl;II)I"] =
    UnimplementedNative("com/sun/j2me/content/InvocationStore.getByTid0.(Lcom/sun/j2me/content/InvocationImpl;II)I", 0);

Native["com/sun/j2me/content/InvocationStore.resetFlags0.(I)V"] =
    UnimplementedNative("com/sun/j2me/content/InvocationStore.resetFlags0.(I)V");

Native["com/sun/j2me/content/AppProxy.midletIsRemoved.(ILjava/lang/String;)V"] =
    UnimplementedNative("com/sun/j2me/content/AppProxy.midletIsRemoved.(ILjava/lang/String;)V");
