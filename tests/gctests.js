'use strict';

// Define objects and functions that j2me.js expects
// but are unavailable in the shell environment.

if (typeof console === "undefined") {
  var console = {
    log: print,
  }
}

console.info = function (c) {
  putstr(String.fromCharCode(c));
};

console.error = function (c) {
  putstr(String.fromCharCode(c));
};

var freedAddr = 0;

var passed = 0, failed = 0;

function is(a, b, msg) {
  if (a == b) {
    ++passed;
    print("pass " + msg);
  } else {
    ++failed;
    print("fail " + msg + "; expected " + JSON.stringify(b) + ", got " + JSON.stringify(a));
  }
}

function isNot(a, b, msg) {
  if (a != b) {
    ++passed;
    print("pass " + msg);
  } else {
    ++failed;
    print("fail " + msg + "; expected " + JSON.stringify(b) + " != " + JSON.stringify(a));
  }
}

function ok(a, msg) {
  if (!!a) {
    ++passed;
    print("pass " + msg);
  } else {
    ++failed;
    print("fail " + msg);
  }
}

var tests = [];

function next() {
  freedAddr = 0;

  if (tests.length == 0) {
    ok(true, "TESTS COMPLETED");
    print("DONE " + passed + "/" + (passed + failed));
  } else {
    var test = tests.shift();
    test();
  }
}

// Test gcMalloc
tests.push(function() {
  var addr = ASM._gcMalloc(4);
  ASM._forceCollection();
  is(freedAddr, addr, "Object allocated with gcMalloc is freed");

  next();
});

// Test gcMallocUncollectable
tests.push(function() {
  var addr = ASM._gcMallocUncollectable(4);
  ASM._forceCollection();
  isNot(freedAddr, addr, "Object allocated with gcMallocUncollectable isn't freed");

  ASM._gcFree(addr);

  ASM._forceCollection();
  ASM._forceCollection();
  is(freedAddr, addr, "Finalizer is called for a freed object");

  next();
});

// TODO: Test gcMallocAtomic (make sure values inside the allocated area aren't considered pointers)

tests.push(function() {
  var objAddr = J2ME.allocObject(CLASSES.java_lang_Object.klass);
  ASM._forceCollection();
  is(freedAddr, objAddr, "An object not referenced by anyone is freed");

  next();
});

tests.push(function() {
  var addr = ASM._gcMallocUncollectable(4);
  var objAddr = J2ME.allocObject(CLASSES.java_lang_Object.klass);
  i32[addr >> 2] = objAddr;
  ASM._forceCollection();
  isNot(freedAddr, addr, "Object allocated with GC_MALLOC_UNCOLLECTABLE isn't collected");
  isNot(freedAddr, objAddr, "Object referenced by someone isn't freed");

  ASM._gcFree(addr);

  ASM._forceCollection();
  is(freedAddr, objAddr, "Object that isn't referenced by anyone anymore is freed");

  ASM._forceCollection();
  is(freedAddr, addr, "Finalizer is called for a freed object");

  next();
});

tests.push(function() {
  var addr = ASM._gcMallocUncollectable(4);
  var objAddr = J2ME.allocObject(CLASSES.java_lang_Object.klass);
  i32[addr >> 2] = objAddr;
  ASM._forceCollection();
  isNot(freedAddr, addr, "Object allocated with GC_MALLOC_UNCOLLECTABLE isn't collected");
  isNot(freedAddr, objAddr, "Object referenced by someone isn't freed");

  i32[addr >> 2] = 0;

  ASM._forceCollection();
  is(freedAddr, objAddr, "Object that isn't referenced by anyone anymore is freed");

  next();
});

tests.push(function() {
  var addr = J2ME.newArray(CLASSES.java_lang_Object.klass, 1);
  var objAddr = J2ME.allocObject(CLASSES.java_lang_Object.klass);
  i32[addr >> 2] = objAddr;
  ASM._forceCollection();
  is(freedAddr, addr, "Object not referenced by anyone is freed");
  ASM._forceCollection();
  is(freedAddr, objAddr, "Object that isn't referenced by anyone anymore is freed");

  next();
});

// TODO: Test J2ME.newArray with a primitive array (similar to the _gcMallocAtomic test)

try {
  load("polyfill/promise.js", "bld/native.js", "bld/j2me.js", "libs/zipfile.js",
       "libs/jarstore.js", "native.js");

  var dump = putstr;

  JARStore.addBuiltIn("java/classes.jar", snarf("java/classes.jar", "binary").buffer);

  CLASSES.initializeBuiltinClasses();

  J2ME.onFinalize = function(addr) {
    freedAddr = addr;
  };

  var start = dateNow();

  next();

  print("Time: " + (dateNow() - start).toFixed(4) + " ms");
} catch (x) {
  print(x);
  print(x.stack);
}
