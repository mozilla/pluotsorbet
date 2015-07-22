/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

Native["javax/wireless/messaging/SendSMSTest.getNumber.()Ljava/lang/String;"] = function(addr) {
  asyncImpl("Ljava/lang/String;", new Promise(function(resolve, reject) {
    var sender = DumbPipe.open("lastSMSNumber", {}, function(lastSMSNumber) {
      DumbPipe.close(sender);
      resolve(J2ME.newUncollectableString(lastSMSNumber));
    });
  }));
};

Native["javax/wireless/messaging/SendSMSTest.getBody.()Ljava/lang/String;"] = function(addr) {
  asyncImpl("Ljava/lang/String;", new Promise(function(resolve, reject) {
    var sender = DumbPipe.open("lastSMSBody", {}, function(lastSMSBody) {
      DumbPipe.close(sender);
      resolve(J2ME.newUncollectableString(lastSMSBody));
    });
  }));
};

Native["com/sun/midp/midlet/AddContactTest.getNumber.()Ljava/lang/String;"] = function(addr) {
  asyncImpl("Ljava/lang/String;", new Promise(function(resolve, reject) {
    var sender = DumbPipe.open("lastAddContactParams", {}, function(lastAddContactParams) {
      DumbPipe.close(sender);
      resolve(J2ME.newUncollectableString(lastAddContactParams.tel));
    });
  }));
};

Native["com/sun/midp/midlet/AddContactTest.hasNumber.()Z"] = function(addr) {
  asyncImpl("Z", new Promise(function(resolve, reject) {
    var sender = DumbPipe.open("lastAddContactParams", {}, function(lastAddContactParams) {
      DumbPipe.close(sender);
      resolve(lastAddContactParams.tel ? 1 : 0);
    });
  }));
};
