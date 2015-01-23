/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

function backgroundCheck() {
  return new Promise(function(resolve, reject) {
    if (!MIDP.manifest["Nokia-MIDlet-bg-server"]) {
        resolve();
        return;
    }

    DumbPipe.open("backgroundCheck", {}, function(background) {
      if (!background) {
        MIDP.ConnectionRegistry.addConnection({
          connection: config.pushConn,
          midlet: config.pushMidlet,
          filter: "*",
          suiteId: "1"
        });
      }

      resolve();
    });
  });
}

MIDP.additionalProperties = {};

Native["com/nokia/mid/s40/bg/BGUtils.getFGMIDlet.(I)Ljava/lang/String;"] = function(midletNumber) {
  var midlet = MIDP.manifest["MIDlet-" + midletNumber];
  return J2ME.newString(midlet.substr(midlet.lastIndexOf(",") + 1));
};

MIDP.additionalProperties = {};

Native["com/nokia/mid/s40/bg/BGUtils.addSystemProperties.(Ljava/lang/String;)V"] = function(args) {
    util.fromJavaString(args).split(";").splice(1).forEach(function(arg) {
        var elems = arg.split("=");
        MIDP.additionalProperties[elems[0]] = elems[1];
    });
};

Native["com/nokia/mid/s40/bg/BGUtils.startMIDlet.(ILjava/lang/String;Ljava/lang/String;)V"] = function(midletNumber, midletClass, thirdArg) {
    var isolateClassInfo = CLASSES.getClass("com/sun/midp/main/MIDletSuiteUtils");
    $.ctx.executeFrames([
        Frame.create(CLASSES.getMethod(isolateClassInfo, "S.execute.(ILjava/lang/String;Ljava/lang/String;)Z"),
                     [ midletNumber, midletClass, thirdArg ], 0)
    ]);
};

Native["com/nokia/mid/s40/bg/BGUtils.waitUserInteraction.()V"] = function() {
  asyncImpl("V", new Promise(function(resolve, reject) {
    // If the page is visible, just start the FG MIDlet
    if (document.hidden === false) {
        resolve();
        return;
    }

    // Otherwise, wait until the page becomes visible, then start the FG MIDlet
    document.addEventListener("visibilitychange", function onVisible() {
      if (document.hidden === false) {
        document.removeEventListener("visibilitychange", onVisible, false);
        resolve();
      }
    }, false);
  }));
};
