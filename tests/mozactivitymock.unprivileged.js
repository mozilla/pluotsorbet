/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

Native["javax/wireless/messaging/SendSMSTest.getNumber.()Ljava/lang/String;"] = function() {
  asyncImpl("Ljava/lang/String;", new Promise(function(resolve, reject) {
    var sender = DumbPipe.open("lastSMSNumber", {}, function(lastSMSNumber) {
      DumbPipe.close(sender);
      resolve(J2ME.newString(lastSMSNumber));
    });
  }));
};

Native["javax/wireless/messaging/SendSMSTest.getBody.()Ljava/lang/String;"] = function() {
  asyncImpl("Ljava/lang/String;", new Promise(function(resolve, reject) {
    var sender = DumbPipe.open("lastSMSBody", {}, function(lastSMSBody) {
      DumbPipe.close(sender);
      resolve(J2ME.newString(lastSMSBody));
    });
  }));
};
