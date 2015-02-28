'use strict';

var log = function() {
  var s = Array.prototype.join.call(arguments, ",");

  // Write it to the document body so the test automation harness
  // and manual testers can observe it.
  document.body.textContent += s + "\n";

  // Log it via console.log so it gets written to the test automation log.
  console.log(s);
}

var passed = 0, failed = 0, then = performance.now();

function is(a, b, msg) {
  if (a == b) {
    ++passed;
    log("pass " + msg);
  } else {
    ++failed;
    log("fail " + msg + "; expected " + JSON.stringify(b) + ", got " + JSON.stringify(a));
  }
}

function ok(a, msg) {
  if (!!a) {
    ++passed;
    log("pass " + msg);
  } else {
    ++failed;
    log("fail " + msg);
  }
}

var tests = [];

function next() {
  if (tests.length == 0) {
    ok(true, "TESTS COMPLETED");
    log("DONE: " + passed + " pass, " + failed + " fail");
  } else {
    var test = tests.shift();
    test();
  }
}

tests.push(function() {
  load("../uncompressed.jar", "arraybuffer").then(function(data) {
    JARStore.installJAR("uncompressed.jar", data).then(function() {
      JARStore.loadJAR("uncompressed.jar").then(function(loaded) {
        ok(loaded === true, "Uncompressed JAR loaded");

        ok(JARStore.loadFile("build/RunTests.class") != null, "Class file from uncompressed JAR loaded");

        ok(JARStore.loadFile("build/RunTests.class") == null, "Class file from uncompressed JAR can be loaded only once");

        ok(JARStore.loadFile("META-INF/MANIFEST.MF") != null, "Data file from uncompressed JAR loaded");

        ok(JARStore.loadFile("META-INF/MANIFEST.MF") != null, "Data file from uncompressed JAR can be loaded multiple times");

        next();
      });
    });
  });
});

tests.push(function() {
  JARStore.clear().then(next);
});

tests.push(function() {
  load("../compressed.jar", "arraybuffer").then(function(data) {
    JARStore.installJAR("compressed.jar", data).then(function() {
      JARStore.loadJAR("compressed.jar").then(function(loaded) {
        ok(loaded === true, "Compressed JAR loaded");

        ok(JARStore.loadFile("build/RunTests.class") != null, "Class file from compressed JAR loaded");

        ok(JARStore.loadFile("build/RunTests.class") == null, "Class file from compressed JAR can be loaded only once");

        ok(JARStore.loadFile("META-INF/MANIFEST.MF") != null, "Data file from compressed JAR loaded");

        ok(JARStore.loadFile("META-INF/MANIFEST.MF") != null, "Data file from compressed JAR can be loaded multiple times");

        next();
      });
    });
  });
});

tests.push(function() {
  JARStore.clear().then(next);
});

tests.push(function() {
  load("../uncompressed.jar", "arraybuffer").then(function(data) {
    JARStore.addBuiltIn("uncompressed.jar", data);

    ok(JARStore.loadFile("build/RunTests.class") != null, "Class file from built-in uncompressed JAR loaded");

    ok(JARStore.loadFile("build/RunTests.class") != null, "Class file from built-in uncompressed JAR can be loaded multiple times");

    ok(JARStore.loadFile("META-INF/MANIFEST.MF") != null, "Data file from built-in uncompressed JAR loaded");

    ok(JARStore.loadFile("META-INF/MANIFEST.MF") != null, "Data file from built-in uncompressed JAR can be loaded multiple times");

    next();
  });
});

tests.push(function() {
  JARStore.clear().then(next);
});

tests.push(function() {
  load("../compressed.jar", "arraybuffer").then(function(data) {
    JARStore.addBuiltIn("compressed.jar", data);

    ok(JARStore.loadFile("build/RunTests.class") != null, "Class file from built-in compressed JAR loaded");

    ok(JARStore.loadFile("build/RunTests.class") != null, "Class file from built-in compressed JAR can be loaded multiple times");

    ok(JARStore.loadFile("META-INF/MANIFEST.MF") != null, "Data file from built-in compressed JAR loaded");

    ok(JARStore.loadFile("META-INF/MANIFEST.MF") != null, "Data file from built-in compressed JAR can be loaded multiple times");

    next();
  });
});

tests.push(function() {
  JARStore.clear().then(next);
});

tests.push(function() {
  JARStore.loadJAR("uncompressed.jar").then(function(loaded) {
    ok(loaded === false, "JAR not loaded");
    next();
  });
});

tests.push(function() {
  JARStore.clear().then(next);
});

tests.push(function() {
  load("../uncompressed.jar", "arraybuffer").then(function(data) {
    JARStore.installJAR("uncompressed.jar", data, "aJAD").then(function() {
      JARStore.loadJAR("uncompressed.jar").then(function(loaded) {
        ok(loaded === true, "JAR loaded");

        ok(JARStore.getJAD() === "aJAD", "JAD loaded");

        ok(JARStore.loadFile("build/RunTests.class") != null, "Class file loaded");

        next();
      });
    });
  });
});

tests.push(function() {
  JARStore.clear().then(next);
});

next();
