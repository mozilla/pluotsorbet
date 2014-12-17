/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

"use strict";

// The complete list of paths we expect to find in the filesystem.
// We check that they all exist.
var paths = [
  "/",
  "/Persistent",
  "/_main.ks",
  "/tests.jar",
  "/classes.jar",
];

// The files we expect to find in the filesystem, indexed by parent dir.
// We check that the directories have the files we expect to find in them.
var filesByDir = {
  "/": [
    "Persistent",
    "_main.ks",
    "tests.jar",
    "classes.jar",
  ],

  "/Persistent": [],
};

// Push a ton of files onto the list so we test a large population.
for (var i = 0; i < 1000; i++) {
  var filename = "file-" + i + ".png";
  paths.push("/Persistent/" + filename);
  filesByDir["/Persistent"].push(filename);
}

filesByDir["/Persistent"].sort();

tests.push(function() {
  var i = -1;

  var checkNextPath = function() {
    if (++i < paths.length) {
      var path = paths[i];
      fs.exists(path, function(exists) {
        is(exists, true, path + " exists");
        checkNextPath();
      });
    } else {
      next();
    }
  };

  checkNextPath();
});

tests.push(function() {
  var dirs = Object.keys(filesByDir);
  var i = -1;

  var checkNextDir = function() {
    if (++i < dirs.length) {
      var dir = dirs[i];
      fs.list(dir, function(files) {
        ok(files instanceof Array, "directory list is an array");
        is(files.length, filesByDir[dir].length, "directory contains expected number of files");
        for (var j = 0; j < filesByDir[dir].length; j++) {
          is(files[j], filesByDir[dir][j], "file in position " + j + " is expected");
        }
        checkNextDir();
      });
    } else {
      next();
    }
  };

  checkNextDir();
});

fs.init(next);
