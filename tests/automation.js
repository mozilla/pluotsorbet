/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

casper.on('remote.message', function(message) {
    this.echo(message);
});

casper.test.begin("unit tests", 3, function(test) {
    casper
    .start("http://localhost:8000/index.html?main=RunTests")
    .waitForText("DONE", function then() {
        test.assertTextExists("DONE: 712 pass", "run unit tests");
    });

    casper
    .thenOpen("http://localhost:8000/index.html?main=tests/isolate/TestIsolate")
    .waitForText("DONE", function then() {
        test.assertTextExists("m\na ma\n2\nma\n2\n1 isolate\n4\n5\n1 isolate\nma\nma\n3 isolates\n1 m1\n2 m2\n4\n5\nma\n1 isolate\nIsolates terminated\nr mar\n2\nmar\nc marc\n2\nmarc\nMain isolate still running");
    });

    casper
    .thenOpen("http://localhost:8000/index.html?main=com/sun/midp/main/MIDletSuiteLoader&midletClassName=gfx/CanvasTest")
    .waitForText("PAINTED", function then() {
        test.assert(true);
    });

    casper
    .run(function() {
        test.done();
    });
});
