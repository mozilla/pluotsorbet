/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

"use strict";

// This is the asynchronous version of libs/fs-init.js, from before we made
// the filesystem API mostly synchronous.  We need to use this version when
// initializing the filesystem with the fs-v1.js and fs-v2.js libraries,
// since they're still mostly asynchronous.

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

  // Create directories sequentially so parents exist before children
  // are created.  We could use fs.mkdirp instead, but it won't necessarily
  // be faster, since it'll still create parents before children, and each
  // child's creation will trigger existence checks on all parent dirs.
  return initialDirs.reduce(function(current, next) {
    return current.then(function() {
      return new Promise(function(resolve, reject) {
        fs.mkdir(next, resolve);
      });
    });
  }, Promise.resolve());
}).then(function() {
  var filePromises = [];

  if (typeof config !== "undefined" && config.midletClassName == "RunTests") {
    initialFiles.push({ sourcePath: "certs/_test.ks", targetPath: "/_test.ks" });
  }

  initialFiles.forEach(function(file) {
    filePromises.push(new Promise(function(resolve, reject) {
        fs.exists(file.targetPath, function(exists) {
          if (exists) {
            resolve();
          } else {
            load(APP_BASE_DIR + file.sourcePath, "blob").then(function(data) {
              fs.create(file.targetPath, data, resolve);
            });
          }
      });
    }));
  });

  return Promise.all(filePromises);
});
