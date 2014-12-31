/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

"use strict";

initialPaths.push("/classes.jar");
initialPaths.push("/tests.jar");

// The files we expect to find in the filesystem, indexed by parent dir.
// We check that the directories have the files we expect to find in them.
initialFilesByDir["/"].push("classes.jar");
initialFilesByDir["/"].push("tests.jar");

// Include the larger population of files that we added to the filesystem.
for (var i = 0; i < 100; i++) {
  var filename = "file-" + i + ".png";
  initialPaths.push("/Persistent/" + filename);
  initialFilesByDir["/Persistent"].push(filename);
}

fs.init(testInit);
