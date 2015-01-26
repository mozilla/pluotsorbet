/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

var MIDlet = {
  name: "aMIDlet",

  SMSDialogVerificationText: "This app sent you an SMS. Type the message you received here:",
  SMSDialogTimeout: 300000, // Five minutes
  SMSDialogTimeoutText: "left",
  SMSDialogReceiveFilter: function(message) {
    return message;
  },
};

var config = {
  main: "com/sun/midp/main/MIDletSuiteLoader",
  midletClassName: "RunTests",
};

// The base directory of the app, relative to the current page.  Normally this
// is the directory from which the page was loaded, but some test pages load
// from a subdirectory, like tests/fs/, and they set this accordingly such that
// code loads files, like libs/fs-init.js, can load them from the right place.
var APP_BASE_DIR = "./";
