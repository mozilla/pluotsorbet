/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

casper.on('remote.message', function(message) {
    this.echo(message);
});

casper.options.waitTimeout = 35000;

casper.options.onWaitTimeout = function() {
    this.echo("Timeout");
    this.debugPage();
};

var gfxTests = [ "gfx/CanvasTest" ];

casper.test.begin("unit tests", 5 + gfxTests.length, function(test) {
    casper
    .start("http://localhost:8000/index.html")
    .waitForText("DONE", function() {
        test.assertTextExists("DONE: 4516 pass, 0 fail, 162 known fail, 0 unknown pass", "run unit tests");
    });

    casper
    .thenOpen("http://localhost:8000/index.html?main=tests/isolate/TestIsolate&logLevel=info")
    .waitForText("DONE", function() {
        test.assertTextExists("I m\nI a ma\nI 2\nI ma\nI 2\nI 1 isolate\nI Isolate ID correct\nI 4\nI 5\nI 1 isolate\nI ma\nI ma\nI 3 isolates\nI 1 m1\nI 2 m2\nI 4\nI 5\nI ma\nI 1 isolate\nI Isolates terminated\nI r mar\nI 2\nI mar\nI c marc\nI 2\nI marc\nI Main isolate still running");
    });

    casper
    .thenOpen("http://localhost:8000/index.html?main=com/sun/midp/main/MIDletSuiteLoader&midletClassName=tests/alarm/MIDlet1&jad=tests/midlets/alarm/alarm.jad")
    .waitForText("Hello World from MIDlet2", function() {
        test.assert(true);
    });

    casper
    .thenOpen("http://localhost:8000/tests/fstests.html")
    .waitForText("DONE", function() {
        test.assertTextExists("DONE: 106 PASS, 0 FAIL", "run fs.js unit tests");
    });

    casper
    .thenOpen("http://localhost:8000/index.html?midletClassName=tests.sms.SMSMIDlet&main=com/sun/midp/main/MIDletSuiteLoader", function() {
        this.waitForText("START", function() {
            this.waitForSelector("#sms_text", function() {
                this.waitForSelector("#sms_addr", function() {
                    this.waitForSelector("#sms_receive", function() {
                        this.sendKeys("#sms_text", "Prova SMS", { reset: true });
                        this.sendKeys("#sms_addr", "+77777777777", { reset: true });
                        this.click("#sms_receive");

                        this.waitForText("DONE", function() {
                            test.assertTextDoesntExist("FAIL");
                        });
                    });
                });
            });
        });
    });

    // Graphics tests

    gfxTests.forEach(function(testName) {
        casper
        .thenOpen("http://localhost:8000/index.html?main=com/sun/midp/main/MIDletSuiteLoader&midletClassName=" + testName)
        .waitForText("PAINTED", function() {
            this.waitForSelector("#canvas", function() {
                var got = this.evaluate(function(testName) {
                    var gotCanvas = document.getElementById("canvas");
                    var gotPixels = new Uint32Array(gotCanvas.getContext("2d").getImageData(0, 0, gotCanvas.width, gotCanvas.height).data.buffer);

                    var img = new Image();
                    img.src = "tests/" + testName + ".png";

                    img.onload = function() {
                        var expectedCanvas = document.createElement('canvas');
                        expectedCanvas.width = img.width;
                        expectedCanvas.height = img.height;
                        expectedCanvas.getContext("2d").drawImage(img, 0, 0);

                        var expectedPixels = new Uint32Array(expectedCanvas.getContext("2d").getImageData(0, 0, img.width, img.height).data.buffer);

                        if (expectedCanvas.width !== gotCanvas.width || expectedCanvas.height !== gotCanvas.height) {
                            console.log("Canvas dimensions are wrong");
                            console.log("FAIL");
                            return;
                        }

                        var different = 0;
                        var i = 0;
                        for (var x = 0; x < gotCanvas.width; x++) {
                            for (var y = 0; y < gotCanvas.height; y++) {
                                if (expectedPixels[i] !== gotPixels[i]) {
                                    different++;
                                }

                                i++;
                            }
                        }

                        // Allow 0.5% of different pixels
                        var maxDifferent = gotCanvas.width * gotCanvas.height * 0.005;

                        if (different > maxDifferent) {
                            console.log(gotCanvas.toDataURL());
                            console.log("FAIL");
                        }

                        console.log("DONE - " + different);
                    };

                    img.onerror = function() {
                        console.log("Error while loading test image");
                        console.log("FAIL");
                    };
                }, testName);

                this.waitForText("DONE", function() {
                    test.assertTextDoesntExist("FAIL");
                });
            });
        });
    });

    casper
    .run(function() {
        test.done();
    });
});
