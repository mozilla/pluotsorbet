/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'Use strict';

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
     ["satsa"]];

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