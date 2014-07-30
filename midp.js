/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'Use strict';

Native["com/sun/midp/log/LoggingBase.report.(IILjava/lang/String;)V"] = function(ctx, stack) {
    var message = stack.pop(), channelID = stack.pop(), severity = stack.pop();
    console.info(util.fromJavaString(message));
}

Native["com/sun/midp/security/Permissions.loadGroupList.()[Ljava/lang/String;"] = function(ctx, stack) {
    var groupTBL [
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
    var list = CLASSES.newArray("java/lang/String", groupTBL.length);
    groupTBL.forEach(function (e, n) {
    });
}
