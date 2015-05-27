/* -*- Mode: JavaScript; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

var Content = (function() {
  var chRegisteredClassName = null;
  var chRegisteredStorageID = -1;
  var chRegisteredID = null;
  var chRegisteredRegistrationMethod = -1;

  function serializeString(parts) {
    return parts.reduce(function(prev, current) {
      return prev + String.fromCharCode(current.length * 2) + current;
    }, "");
  }

  addUnimplementedNative("com/sun/j2me/content/RegistryStore.init.()Z", 1);

  Native["com/sun/j2me/content/RegistryStore.forSuite0.(I)Ljava/lang/String;"] = function(suiteID) {
    if (!chRegisteredClassName) {
      return null;
    }

    var serializedString = serializeString([
                                              chRegisteredID,
                                              chRegisteredStorageID.toString(16),
                                              chRegisteredClassName,
                                              chRegisteredRegistrationMethod.toString(16)
                                           ]);

    return J2ME.newString(String.fromCharCode(serializedString.length * 2) + serializedString);
  };

  addUnimplementedNative("com/sun/j2me/content/RegistryStore.findHandler0.(Ljava/lang/String;ILjava/lang/String;)Ljava/lang/String;", null);

  Native["com/sun/j2me/content/RegistryStore.register0.(ILjava/lang/String;Lcom/sun/j2me/content/ContentHandlerRegData;)Z"] = function(storageId, className, handlerData) {
    chRegisteredID = J2ME.fromJavaString(handlerData.ID);
    chRegisteredClassName = J2ME.fromJavaString(className);
    chRegisteredStorageID = storageId;
    chRegisteredRegistrationMethod = handlerData.registrationMethod;

    return 1;
  };

  Native["com/sun/j2me/content/RegistryStore.getHandler0.(Ljava/lang/String;Ljava/lang/String;I)Ljava/lang/String;"] = function(callerId, id, mode) {
    if (!chRegisteredClassName) {
      return null;
    }

    if (mode != 0) {
      console.warn("com/sun/j2me/content/RegistryStore.getHandler0.(Ljava/lang/String;Ljava/lang/String;I)Ljava/lang/String; expected mode = 0");
    }

    if (callerId) {
      console.warn("com/sun/j2me/content/RegistryStore.getHandler0.(Ljava/lang/String;Ljava/lang/String;I)Ljava/lang/String; expected callerId = null");
    }

    return J2ME.newString(serializeString([
                                            chRegisteredID,
                                            chRegisteredStorageID.toString(16),
                                            chRegisteredClassName,
                                            chRegisteredRegistrationMethod.toString(16)
                                          ]));
  };

  Native["com/sun/j2me/content/AppProxy.isInSvmMode.()Z"] = function() {
    // We are in MVM mode (multiple MIDlets running concurrently)
    return 0;
  };

  addUnimplementedNative("com/sun/j2me/content/AppProxy.midletIsRemoved.(ILjava/lang/String;)V");
  addUnimplementedNative("com/sun/j2me/content/AppProxy.platformFinish0.(I)Z", 0);

  var invocation = null;

  function addInvocation(argument, action) {
    invocation = {
      argument: argument,
      action: action,
    };
  }

  Native["com/sun/j2me/content/InvocationStore.get0.(Lcom/sun/j2me/content/InvocationImpl;ILjava/lang/String;IZ)I"] = function(invoc, suiteId, className, mode, shouldBlock) {
    if (!invocation) {
      return 0;
    }

    if (invoc.arguments.length != 1) {
      invoc.argsLen = 1;
      return -1;
    }

    invoc.arguments[0] = J2ME.newString(invocation.argument);
    invoc.action = J2ME.newString(invocation.action);
    invoc.status = 2; // Invocation.ACTIVE

    invocation = null;

    return 1;
  };

  addUnimplementedNative("com/sun/j2me/content/InvocationStore.setCleanup0.(ILjava/lang/String;Z)V");
  addUnimplementedNative("com/sun/j2me/content/InvocationStore.getByTid0.(Lcom/sun/j2me/content/InvocationImpl;II)I", 0);
  addUnimplementedNative("com/sun/j2me/content/InvocationStore.resetFlags0.(I)V");

  return {
    addInvocation: addInvocation,
  };
})();
