/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

var bgMidletClass;
var bgMidletDisplayName;
var fgMidletClass;
var fgMidletDisplayName;

var display = document.getElementById("display");

// The splash and download screens generate many style recalculations while
// attached to the DOM, regardless of their display styles, because of their
// animations.  So instead of setting their display styles, we add/remove them
// to/from the DOM.

var splashScreen = document.getElementById('splash-screen');
display.removeChild(splashScreen);
splashScreen.style.display = 'block';
function showSplashScreen() {
  display.appendChild(splashScreen);
}
function hideSplashScreen() {
  if (splashScreen.parentNode) {
    splashScreen.parentNode.removeChild(splashScreen);
  }
}

var downloadDialog = document.getElementById('download-screen');
display.removeChild(downloadDialog);
downloadDialog.style.display = 'block';
function showDownloadScreen() {
  display.appendChild(downloadDialog);
}
function hideDownloadScreen() {
  if (downloadDialog.parentNode) {
    downloadDialog.parentNode.removeChild(downloadDialog);
  }
}

function showBackgroundScreen() {
  document.getElementById("background-screen").style.display = "block";
}
function hideBackgroundScreen() {
  document.getElementById("background-screen").style.display = "none";
}

// The exit screen is hidden by default, and we only ever show it,
// so we don't need a hideExitScreen function.
function showExitScreen() {
  document.getElementById("exit-screen").style.display = "block";
}

function backgroundCheck() {
  fgMidletClass = config.midletClassName;
  fgMidletDisplayName = "";

  var bgServer = MIDP.manifest["Nokia-MIDlet-bg-server"];
  if (!bgServer) {
    showSplashScreen();
    hideBackgroundScreen();
    return;
  }

  bgMidletClass = MIDP.manifest["MIDlet-" + bgServer].split(",")[2];
  bgMidletDisplayName = MIDP.manifest["MIDlet-" + bgServer].split(",")[0];

  if (fgMidletClass === bgMidletClass) {
    // The config is targeting the BG MIDlet. We have to make an educated guess about
    // which MIDlet is intended to be the FG MIDlet.
    // We're assuming there are only two midlets
    var fgMidletNumber = (bgServer == 2) ? 1 : 2;
    fgMidletClass = MIDP.manifest["MIDlet-" + fgMidletNumber].split(",")[2];
    fgMidletDisplayName = MIDP.manifest["MIDlet-" + fgMidletNumber].split(",")[0];
  }


  DumbPipe.close(DumbPipe.open("backgroundCheck", {}));
}

MIDP.additionalProperties = {};

Native["com/nokia/mid/s40/bg/BGUtils.launchIEMIDlet.(Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;)Z"] = function(midletSuiteVendor, midletName, midletNumber, startupNoteText, args) {
  var vendor = J2ME.fromJavaString(midletSuiteVendor);
  if (vendor !== MIDP.manifest["MIDlet-Vendor"]) {
    console.warn("Launching MIDlets from other suites is not supported! (vendor mismatch)");
    return 0;
  }

  var name = J2ME.fromJavaString(midletName);
  if (name !== MIDP.manifest["MIDlet-Name"]) {
    console.warn("Launching MIDlets from other suites is not supported! (name mismatch)");
    return 0;
  }

  // Set up args
  MIDP.additionalProperties = {};
  J2ME.fromJavaString(args).split(";").splice(1).forEach(function(arg) {
    var elems = arg.split("=");
    MIDP.additionalProperties[elems[0]] = elems[1];
  });

  var className = MIDP.manifest["MIDlet-" + midletNumber].split(",")[2];
  var displayName = MIDP.manifest["MIDlet-" + midletNumber].split(",")[0];

  MIDP.sendExecuteMIDletEvent(1, className, displayName, "", "", "");

  return 1;
};

Native["com/nokia/mid/s40/bg/BGUtils.setBGMIDletResident.(Z)V"] = function(param) {
  console.log("BGUtils.setBGMIDletResident(" + param + ")");
};

Native["com/nokia/mid/s40/bg/BGUtils.maybeWaitUserInteraction.(Ljava/lang/String;)V"] = function(midletClassName) {
  if (J2ME.fromJavaString(midletClassName) !== fgMidletClass) {
    return;
  }

  // If the page is visible, just start the FG MIDlet
  if (!document.hidden) {
    showSplashScreen();
    hideBackgroundScreen();

    if (profile === 3) {
      // Start the "warm startup" profiler after a timeout to better imitate
      // what happens in a warm startup, where the bg midlet has time to settle.
      asyncImpl("V", new Promise(function(resolve, reject) {
        setTimeout(function() {
          startTimeline();
          resolve();
        }, 5000);
      }));
    }

    return;
  }

  asyncImpl("V", new Promise(function(resolve, reject) {
    // Otherwise, wait until the page becomes visible, then start the FG MIDlet
    document.addEventListener("visibilitychange", function onVisible() {
      if (!document.hidden) {
        document.removeEventListener("visibilitychange", onVisible, false);
        resolve();
      }
    }, false);
  }).then(function() {
    showSplashScreen();
    hideBackgroundScreen();
    profile === 3 && startTimeline();
  }));
};

Native["org/mozilla/ams/MIDletLauncher.init0.()V"] = function() {
  asyncImpl("V", Promise.all(loadingMIDletPromises).then(function() {
    var suiteId = (config.midletClassName === "internal") ? -1 : 1;

    if (bgMidletClass) {
      MIDP.sendExecuteMIDletEvent(suiteId, bgMidletClass, bgMidletDisplayName, "", "", "");
    }

    var args = config.args;
    MIDP.sendExecuteMIDletEvent(suiteId,
                                fgMidletClass,
                                fgMidletDisplayName,
                                (args.length > 0) ? args[0] : "",
                                (args.length > 1) ? args[1] : "",
                                (args.length > 2) ? args[2] : "");
  }));
};

Native["org/mozilla/ams/AmsMIDlet.isBGMIDlet0.(Ljava/lang/String;)Z"] = function(className) {
  return bgMidletClass === J2ME.fromJavaString(className) ? 1 : 0;
};

// If the document is hidden, then we've been started by an alarm and are in
// the background, so we show the background screen.
if (document.hidden) {
  showBackgroundScreen();
}
