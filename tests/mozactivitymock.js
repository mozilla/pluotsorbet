/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

var lastSMSNumber = null;
var lastSMSBody = null;

Native.create("javax/wireless/messaging/SendSMSTest.getNumber.()Ljava/lang/String;", function() {
  return lastSMSNumber;
});

Native.create("javax/wireless/messaging/SendSMSTest.getBody.()Ljava/lang/String;", function() {
  return lastSMSBody;
});

function MozActivity(obj) {
  if (obj.name === "new") {
    switch (obj.data.type) {
      case "websms/sms":
        lastSMSNumber = obj.data.number;
        lastSMSBody = obj.data.body;

        setZeroTimeout((function() {
          this.onsuccess();
        }).bind(this));
      break;

      default:
        throw new Error("MozActivity with type " + obj.data.type + " not supported");
    }
  } else {
    throw new Error("MozActivity " + obj.name + " not supported");
  }
}
