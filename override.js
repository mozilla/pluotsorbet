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
    var syntheticMethod = new MethodInfo({
      name: "RaiseExceptionSynthetic",
      signature: "()V",
      isStatic: true,
      classInfo: J2ME.ClassInfo.createFromObject({
        className: {value: "java/lang/Object"},
        vmc: {value: {}},
        vfc: {value: {}},
        constant_pool: {value: [
          null
        ]}
      }),
      code: new Uint8Array([
        0x2a,             // aload_0
        0xbf              // athrow
      ])
    });
    var callee = Frame.create(syntheticMethod, [exception], 0);
    ctx.frames.push(callee);
    ctx.execute();
  });
  $.pause("Async");
}

Override["com/ibm/oti/connection/file/Connection.decode.(Ljava/lang/String;)Ljava/lang/String;"] = function(string) {
  return J2ME.newString(decodeURIComponent(J2ME.fromJavaString(string)));
};

Override["com/ibm/oti/connection/file/Connection.encode.(Ljava/lang/String;)Ljava/lang/String;"] = function(string) {
  return J2ME.newString(J2ME.fromJavaString(string).replace(/[^a-zA-Z0-9-_\.!~\*\\'()/:]/g, encodeURIComponent));
};

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
