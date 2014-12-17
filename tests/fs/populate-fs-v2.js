/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

"use strict";

var APP_BASE_DIR = "../../";

initialFiles.concat([
  { sourcePath: "tests/tests.jar", targetPath: "/tests.jar" },
  { sourcePath: "java/classes.jar", targetPath: "/classes.jar" },
]);

// Push a ton of files onto the list so we test a large population.
for (var i = 0; i < 1000; i++) {
  initialFiles.push({ sourcePath: "tests/gfx/AlertTest.png", targetPath: "/Persistent/file-" + i + ".png" });
}

initFS.then(function() {
  // We changed the name between versions, so use whichever is available.
  var sync = fs.syncStore || fs.storeSync;

  sync.bind(fs)(function() {
    document.body.appendChild(document.createTextNode("DONE"));
  });
});
