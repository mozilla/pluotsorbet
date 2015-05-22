/* -*- Mode: JavaScript; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

var Content = (function() {
   var chRegisteredClassName = null;
   var chRegisteredStorageID = -1;
   var chRegisteredID = null;
   var chRegisteredRegistrationMethod = -1;

  addUnimplementedNative("com/sun/j2me/content/RegistryStore.init.()Z", 1);

  Native["com/sun/j2me/content/RegistryStore.forSuite0.(I)Ljava/lang/String;"] = function(suiteID) {
    console.warn("com/sun/j2me/content/RegistryStore.forSuite0.(I)Ljava/lang/String; not implemented");
    return J2ME.newString("");
  };

  addUnimplementedNative("com/sun/j2me/content/RegistryStore.findHandler0.(Ljava/lang/String;ILjava/lang/String;)Ljava/lang/String;", null);

  Native["com/sun/j2me/content/RegistryStore.register0.(ILjava/lang/String;Lcom/sun/j2me/content/ContentHandlerRegData;)Z"] = function(storageId, className, handlerData) {
    chRegisteredID = J2ME.fromJavaString(handlerData.ID);
    chRegisteredClassName = J2ME.fromJavaString(className);
    chRegisteredStorageID = storageId;
    chRegisteredRegistrationMethod = handlerData.registrationMethod;

    return 1;
  };

  addUnimplementedNative("com/sun/j2me/content/RegistryStore.getHandler0.(Ljava/lang/String;Ljava/lang/String;I)Ljava/lang/String;", null);

  Native["com/sun/j2me/content/AppProxy.isInSvmMode.()Z"] = function() {
    // We are in MVM mode (multiple MIDlets running concurrently)
    return 0;
  };

  addUnimplementedNative("com/sun/j2me/content/AppProxy.midletIsRemoved.(ILjava/lang/String;)V");

  addUnimplementedNative("com/sun/j2me/content/InvocationStore.setCleanup0.(ILjava/lang/String;Z)V");
  addUnimplementedNative("com/sun/j2me/content/InvocationStore.get0.(Lcom/sun/j2me/content/InvocationImpl;ILjava/lang/String;IZ)I", 0);
  addUnimplementedNative("com/sun/j2me/content/InvocationStore.getByTid0.(Lcom/sun/j2me/content/InvocationImpl;II)I", 0);
  addUnimplementedNative("com/sun/j2me/content/InvocationStore.resetFlags0.(I)V");
})();
