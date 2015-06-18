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

config.downloadJAD = "https://www.whatsapp.com/s40/WhatsApp.jad";
config.midletClassName = "com.whatsapp.client.test.WhatsAppBG";

config.ignoredFiles.add("/MemoryCard/WhatsApp/BG-WhatsAppLog.txt");
config.ignoredFiles.add("/MemoryCard/WhatsApp/FG-WhatsAppLog.txt");

config.chRegisteredID = "WhatsApp_Inc.-WhatsApp-com.whatsapp.client.test.ContactListMidlet";
config.chRegisteredClassName = "com.whatsapp.client.test.ContactListMidlet";
config.chRegisteredStorageID = 1;
config.chRegisteredRegistrationMethod = 0;
