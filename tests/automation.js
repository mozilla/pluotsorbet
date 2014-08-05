/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

casper.on('remote.message', function(message) {
    this.echo(message);
});

casper.test.begin("unit tests", 1, function(test) {
    casper
    .start("http://localhost:8000/index.html?main=RunTests")
    .waitForText("DONE", function then() {
        test.assertTextExists("DONE: 702 pass, 0 fail", "run unit tests");
    })
    .run(function() {
        test.done();
    });
});
