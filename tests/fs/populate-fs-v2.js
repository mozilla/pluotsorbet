/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

"use strict";

var APP_BASE_DIR = "../../";

initialFiles.push({ sourcePath: "tests/tests.jar", targetPath: "/tests.jar" });

initFS.then(function() {
  // We changed the name between versions, so use whichever is available.
  var sync = fs.syncStore || fs.storeSync;

  sync.bind(fs)(function() {
    document.body.appendChild(document.createTextNode("DONE"));
  });
});
