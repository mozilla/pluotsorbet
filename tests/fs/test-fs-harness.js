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
  "/Private": [],
};

function testInit() {
  tests.push(function() {
    initialPaths.forEach(function(path) {
      is(fs.exists(path), true, path + " exists");
    });
    next();
  });

  tests.push(function() {
    Object.keys(initialFilesByDir).forEach(function(dir) {
      var initialFiles = initialFilesByDir[dir].slice(0);
      var files = fs.list(dir);
      initialFiles.sort();
      files.sort();
      ok(files instanceof Array, "directory list is an array");
      is(files.length, initialFiles.length, "directory contains expected number of files");
      for (var i = 0; i < initialFiles.length; i++) {
        is(files[i], initialFiles[i], "file in position " + i + " is expected");
      }
    });
    next();
  });

  next();
}
