/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

var pageVisible = false;

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

        pageVisible = true;
      }

      resolve();
    });
  });
}

Native["com/nokia/mid/s40/bg/BGUtils.getFGMIDlet.(I)Ljava/lang/String;"] = function(midletNumber) {
    var midlet = MIDP.manifest["MIDlet-" + midletNumber];
    return J2ME.newString(midlet.substr(midlet.lastIndexOf(",") + 1));
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
