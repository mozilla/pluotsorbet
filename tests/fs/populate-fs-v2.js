/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

"use strict";

var APP_BASE_DIR = "../../";

initialFiles = initialFiles.concat([
  { sourcePath: "java/classes.jar", targetPath: "/classes.jar" },
  { sourcePath: "tests/tests.jar", targetPath: "/tests.jar" },
]);

// Push some files onto the list so we test a larger population.
for (var i = 0; i < 100; i++) {
  initialFiles.push({ sourcePath: "tests/gfx/AlertTest.png", targetPath: "/Persistent/file-" + i + ".png" });
}

initFS.then(function() {
  fs.syncStore(function() {
    document.body.appendChild(document.createTextNode("DONE"));
  });
});
