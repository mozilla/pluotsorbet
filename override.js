/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

var Override = {};

function asyncImpl(returnKind, promise) {
  var ctx = $.ctx;

  promise.then(function(res) {
    if (returnKind === "J" || returnKind === "D") {
      ctx.current().stack.push2(res);
    } else if (returnKind !== "V") {
      ctx.current().stack.push(res);
    } else {
      // void, do nothing
    }
    ctx.execute();
  }, function(exception) {
    var classInfo = CLASSES.getClass("org/mozilla/internal/Sys");
    var methodInfo = classInfo.getMethodByName("throwException", "(Ljava/lang/Exception;)V", true);
    ctx.frames.push(Frame.create(methodInfo, [exception], 0));
    ctx.execute();
  });
  $.pause("Async");
}

// The following Permissions methods are overriden to avoid expensive calls to
// DomainPolicy.loadValues. This has the added benefit that we avoid many other
// computations.

Override["com/sun/midp/security/Permissions.forDomain.(Ljava/lang/String;)[[B"] = function(name) {
  // NUMBER_OF_PERMISSIONS = PermissionsStrings.PERMISSION_STRINGS.length + 2
  // The 2 is the two hardcoded MIPS and AMS permissions.
  var NUMBER_OF_PERMISSIONS = 61;
  var ALLOW = 1;

  var maximums = J2ME.newByteArray(NUMBER_OF_PERMISSIONS);
  var defaults = J2ME.newByteArray(NUMBER_OF_PERMISSIONS);

  for (var i = 0; i < NUMBER_OF_PERMISSIONS; i++) {
    maximums[i] = defaults[i] = ALLOW;
  }

  var permissions = J2ME.newArray(J2ME.PrimitiveArrayClassInfo.B.klass, 2);
  permissions[0] = maximums;
  permissions[1] = defaults;

  return permissions;
};

// Always return true to make Java think the MIDlet domain is trusted.
Override["com/sun/midp/security/Permissions.isTrusted.(Ljava/lang/String;)Z"] = function(name) {
  return 1;
};

// Returns the ID of the permission. The callers will use this ID to check the
// permission in the permissions array returned by Permissions::forDomain.
Override["com/sun/midp/security/Permissions.getId.(Ljava/lang/String;)I"] = function(name) {
  return 0;
};

// The Java code that uses this method doesn't actually use the return value, but
// passes it to Permissions.getId. So we can return anything.
Override["com/sun/midp/security/Permissions.getName.(I)Ljava/lang/String;"] = function(id) {
  return J2ME.newString("com.sun.midp");
};

Override["com/sun/cldc/i18n/uclc/DefaultCaseConverter.toLowerCase.(C)C"] = function(char) {
    return String.fromCharCode(char).toLowerCase().charCodeAt(0);
};

Override["com/sun/cldc/i18n/uclc/DefaultCaseConverter.toUpperCase.(C)C"] = function(char) {
    return String.fromCharCode(char).toUpperCase().charCodeAt(0);
};
