/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

"use strict";

var log = function() {
  var s = Array.prototype.join.call(arguments, ",");

  // Write it to the document body so the test automation harness
  // and manual testers can observe it.
  document.body.textContent += s + "\n";

  // Log it via console.log so it gets written to the test automation log.
  // XXX Commented out because `make test` currently fails if the test log
  // contains the text "FAIL", and this causes that text to appear in it.
  // console.log(s);
}

var passed = 0, failed = 0, then = performance.now();

function is(a, b, msg) {
  if (a == b) {
    ++passed;
    log("PASS " + msg);
  } else {
    ++failed;
    log("FAIL " + msg);
    log("GOT: " + JSON.stringify(a));
    log("EXPECTED: " + JSON.stringify(b));
  }
}

function ok(a, msg) {
  if (!!a) {
    ++passed;
    log("PASS " + msg);
  } else {
    ++failed;
    log("FAIL " + msg);
  }
}

var tests = [];

function next() {
  if (tests.length == 0) {
    ok(true, "TESTS COMPLETED");
    log("DONE: " + passed + " PASS, " + failed + " FAIL, " +
                (Math.round(performance.now() - then)) + " TIME");
  } else {
    var test = tests.shift();
    test();
  }
}

// The complete list of initial paths we expect to find in the filesystem.
// We check that they all exist.
var initialPaths = [
  "/",
  "/Persistent",
  "/Photos",
  "/_main.ks",
];

// The initial files we expect to find in the filesystem, indexed by parent dir.
// We check that the directories have the files we expect to find in them.
var initialFilesByDir = {
  "/": [
    "Persistent/",
    "Photos/",
    "_main.ks",
  ],

  "/Persistent": [],

  "/Photos": [],
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
