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

initialPaths.push("/classes.jar");
initialPaths.push("/tests.jar");

// The files we expect to find in the filesystem, indexed by parent dir.
// We check that the directories have the files we expect to find in them.
initialFilesByDir["/"].push("classes.jar");
initialFilesByDir["/"].push("tests.jar");

// Push some files onto the list so we test a larger population.
for (var i = 0; i < 100; i++) {
  var filename = "file-" + i + ".png";
  initialPaths.push("/Persistent/" + filename);
  initialFilesByDir["/Persistent"].push(filename);
}

initialFilesByDir["/Persistent"].sort();

initFS.then(function() {
  // We changed the name between versions, so use whichever is available.
  (fs.syncStore || fs.storeSync)(testInit);
});
