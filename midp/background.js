/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

var fgMIDletClass = config.pushMidlet;
var fgMIDletNumber = 1;

function backgroundCheck() {
  if (!MIDP.manifest["Nokia-MIDlet-bg-server"]) {
    return;
  }

  DumbPipe.close(DumbPipe.open("backgroundCheck", {}));
}

Native["com/nokia/mid/s40/bg/BGUtils.getFGMIDletClass.()Ljava/lang/String;"] = function() {
  return J2ME.newString(fgMIDletClass);
};

Native["com/nokia/mid/s40/bg/BGUtils.getFGMIDletNumber.()I"] = function() {
  return fgMIDletNumber;
};

MIDP.additionalProperties = {};

Native["com/nokia/mid/s40/bg/BGUtils.addSystemProperties.(Ljava/lang/String;)V"] = function(args) {
    util.fromJavaString(args).split(";").splice(1).forEach(function(arg) {
        var elems = arg.split("=");
        MIDP.additionalProperties[elems[0]] = elems[1];
    });
};

Native["com/nokia/mid/s40/bg/BGUtils.waitUserInteraction.()V"] = function() {
  asyncImpl("V", new Promise(function(resolve, reject) {
    // If the page is visible, just start the FG MIDlet
    if (!document.hidden) {
        resolve();
        return;
    }

    // Otherwise, wait until the page becomes visible, then start the FG MIDlet
    document.addEventListener("visibilitychange", function onVisible() {
      if (!document.hidden) {
        document.removeEventListener("visibilitychange", onVisible, false);
        resolve();
      }
    }, false);
  }));
};
