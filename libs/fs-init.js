/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

"use strict";

// Directories in the filesystem.
var initialDirs = [
  "/MemoryCard",
  "/Persistent",
  "/Phone",
  "/Phone/_my_downloads",
  "/Phone/_my_pictures",
  "/Phone/_my_videos",
  "/Phone/_my_recordings",
  "/Private",
];

// Files in the filesystem.  We load the data from the source path on the real
// filesystem and write it to the target path on the virtual filesystem.
// Source paths are relative to APP_BASE_DIR.
var initialFiles = [
  { sourcePath: "certs/_main.ks", targetPath: "/_main.ks" },
];

var initFS = new Promise(function(resolve, reject) {
  fs.init(resolve);
}).then(function() {
  if (typeof config !== "undefined" && config.midletClassName == "RunTests") {
    initialDirs.push("/tcktestdir");
  }

  initialDirs.forEach(function(dir) {
    fs.mkdir(dir);
  });
}).then(function() {
  var filePromises = [];

  if (typeof config !== "undefined" && config.midletClassName == "RunTests") {
    initialFiles.push({ sourcePath: "certs/_test.ks", targetPath: "/_test.ks" });
  }

  initialFiles.forEach(function(file) {
    filePromises.push(new Promise(function(resolve, reject) {
      if (fs.exists(file.targetPath)) {
        resolve();
      } else {
        load(APP_BASE_DIR + file.sourcePath, "blob").then(function(data) {
          fs.create(file.targetPath, data);
          resolve();
        });
      }
    }));
  });

  return Promise.all(filePromises);
});
