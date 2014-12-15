/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

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
