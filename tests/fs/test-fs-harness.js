/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

"use strict";

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
    log("DONE: " + passed + " pass, " + failed + " fail, " +
                (Math.round(performance.now() - then)) + " time");
  } else {
    var test = tests.shift();
    test();
  }
}

// The complete list of initial paths we expect to find in the filesystem.
// We check that they all exist.
var initialPaths = [
  "/",
  "/MemoryCard",
  "/Persistent",
  "/Phone",
  "/Phone/_my_downloads",
  "/Phone/_my_pictures",
  "/Phone/_my_videos",
  "/Phone/_my_recordings",
  "/Photos",
  "/Private",
  "/_main.ks",
];

// The initial files we expect to find in the filesystem, indexed by parent dir.
// We check that the directories have the files we expect to find in them.
// The keys are directory paths, while the values are filenames (with a slash
// appended to the names of directories).
var initialFilesByDir = {
  "/": [
    "MemoryCard/",
    "Persistent/",
    "Phone/",
    "Photos/",
    "Private/",
    "_main.ks",
  ],
  "/MemoryCard": [],
  "/Persistent": [],
  "/Phone": [
    "_my_downloads/",
    "_my_pictures/",
    "_my_videos/",
    "_my_recordings/",
  ],
  "/Photos": [],
  "/Private": [],
};

function testInit() {
  tests.push(function() {
    var i = -1;

    var checkNextPath = function() {
      if (++i < initialPaths.length) {
        var path = initialPaths[i];
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
    var dirs = Object.keys(initialFilesByDir);
    var i = -1;

    var checkNextDir = function() {
      if (++i < dirs.length) {
        var dir = dirs[i];
        var initialFiles = initialFilesByDir[dir].slice(0);
        fs.list(dir, function(error, files) {
          initialFiles.sort();
          files.sort();
          ok(files instanceof Array, "directory list is an array");
          is(files.length, initialFiles.length, "directory contains expected number of files");
          for (var j = 0; j < initialFiles.length; j++) {
            is(files[j], initialFiles[j], "file in position " + j + " is expected");
          }
          checkNextDir();
        });
      } else {
        next();
      }
    };

    checkNextDir();
  });

  next();
}
