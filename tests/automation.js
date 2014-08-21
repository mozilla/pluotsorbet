/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

casper.on('remote.message', function(message) {
    this.echo(message);
});

casper.test.begin("unit tests", 6, function(test) {
    casper
    .start("http://localhost:8000/index.html?main=RunTests")
    .waitForText("DONE", function then() {
        test.assertTextExists("DONE: 758 pass, 0 fail", "run unit tests");
    });

    casper
    .thenOpen("http://localhost:8000/index.html?main=tests/isolate/TestIsolate")
    .waitForText("DONE", function then() {
        test.assertTextExists("m\na ma\n2\nma\n2\n1 isolate\nIsolate ID correct\n4\n5\n1 isolate\nma\nma\n3 isolates\n1 m1\n2 m2\n4\n5\nma\n1 isolate\nIsolates terminated\nr mar\n2\nmar\nc marc\n2\nmarc\nMain isolate still running");
    });

    casper
    .thenOpen("http://localhost:8000/index.html?main=com/sun/midp/main/MIDletSuiteLoader&midletClassName=gfx/CanvasTest")
    .waitForText("PAINTED", function then() {
        test.assert(true);
    });

    casper
    .thenOpen("http://localhost:8000/index.html?main=com/sun/midp/main/MIDletSuiteLoader&midletClassName=tests/alarm/MIDlet1&jad=tests/midlets/alarm/alarm.jad")
    .waitForText("Hello World from MIDlet2", function then() {
        test.assert(true);
    }, function onTimeout() {
        test.fail();
    }, 10000);

    casper
    .thenOpen("http://localhost:8000/tests/fstests.html")
    .waitForText("DONE", function then() {
        test.assertTextExists("DONE: 95 PASS, 0 FAIL", "run fs.js unit tests");
    });

    casper
    .thenOpen("http://localhost:8000/index.html?main=com/sun/midp/main/MIDletSuiteLoader&midletClassName=javax.microedition.rms.TestRecordStore")
    .waitForText("DONE", function then() {
        test.assertTextExists("START\nDONE");
    });

    casper
    .run(function() {
        test.done();
    });
});
