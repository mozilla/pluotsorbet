/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

var casper = require('casper').create({
    verbose: true,
    logLevel: "debug",
});

casper.on('remote.message', function(message) {
    this.echo(message);
});

casper.start("http://localhost:8000/tests/fs/make-fs-v1.html");

casper.waitForText(
  "DONE",
  function() {
    casper.exit(0);
  },
  function() {
    casper.exit(1);
  }
);

casper.run();
