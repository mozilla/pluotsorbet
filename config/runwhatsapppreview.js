/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

MIDlet.name = "WhatsApp";

MIDlet.SMSDialogVerificationText = "WhatsApp sent you an SMS, please enter the code you received:";
MIDlet.SMSDialogReceiveFilter = function(message) {
  // Remove the leading 'WhatsApp code ' text, if present
  if (message.toLowerCase().startsWith("whatsapp code ")) {
    message = message.substring(14);
  }

  // Remove the '-' character, if present
  message = message.replace(/-/g, "");

  return 'WhatsApp code ' + message.substr(0, 3) + "-" + message.substr(3, 3);
};
MIDlet.SMSDialogInputType = "number";
MIDlet.SMSDialogInputMaxLength = 6;

MIDlet.shouldStartBackgroundService = function() {
  // This file is created after verification.
  if (fs.exists("/Private/crypto/0/ax_prekeys_self")) {
    return true;
  }

  // The cdb directory is empty before registration, contains files after registration.
  if (fs.exists("/Private/cdb") && fs.list("/Private/cdb").length > 0) {
    return true;
  }

  // This file is created before the verification succeeds, so the other
  // checks are more precise. If the other checks won't be applicable anymore
  // at some point, we could use this check that seems more stable (this file
  // has been there in all WhatsApp versions).
  // return fs.exists("/Persistent/WhatsApp_token");

  return false;
};

config.downloadJAD = "https://www.whatsapp.com/s40/WhatsApp.jad";
config.midletClassName = "com.whatsapp.client.test.WhatsAppBG";

config.ignoredFiles.add("/MemoryCard/WhatsApp/BG-WhatsAppLog.txt");
config.ignoredFiles.add("/MemoryCard/WhatsApp/FG-WhatsAppLog.txt");

config.chRegisteredID = "WhatsApp_Inc.-WhatsApp-com.whatsapp.client.test.ContactListMidlet";
config.chRegisteredClassName = "com.whatsapp.client.test.ContactListMidlet";
config.chRegisteredStorageID = 1;
config.chRegisteredRegistrationMethod = 0;
config.useOffscreenCanvas = false;
config.ignoreRgbChanges = true;
