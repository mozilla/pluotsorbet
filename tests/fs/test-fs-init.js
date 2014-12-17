/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

"use strict";

// The complete list of paths we expect to find in the filesystem.
// We check that they all exist.
var paths = [
  "/",
  "/Persistent",
  "/_main.ks",
];

// The files we expect to find in the filesystem, indexed by parent dir.
// We check that the directories have the files we expect to find in them.
var filesByDir = {
  "/": [
    "Persistent",
    "_main.ks",
  ],

  "/Persistent": [],
};

checkPaths();
checkFilesByDir();
fs.init(next);
