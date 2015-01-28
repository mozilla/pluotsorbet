/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

var system = require('system');
var fs = require('fs');

casper.on('remote.message', function(message) {
    this.echo(message);
});

casper.options.waitTimeout = 70000;
casper.options.verbose = true;
casper.options.logLevel = "debug";
casper.options.viewportSize = { width: 240, height: 320 };

casper.options.onWaitTimeout = function() {
    this.echo("data:image/png;base64," + this.captureBase64('png'));
    this.test.fail("Timeout");
};

var gfxTests = [
  { name: "gfx/AlertTest", maxDifferent: 1942 },
  { name: "gfx/CanvasTest", maxDifferent: 0 },
  { name: "gfx/CanvasWithHeaderTest", maxDifferent: 1400 },
  { name: "gfx/DrawRegionTest", maxDifferent: 0 },
  { name: "gfx/ImageRenderingTest", maxDifferent: 0 },
  { name: "gfx/FillRectTest", maxDifferent: 0 },
  { name: "gfx/DrawAndFillRoundRectTest", maxDifferent: 2000 },
  { name: "gfx/DrawAndFillArcTest", maxDifferent: 2000 },
  { name: "gfx/DrawStringTest", maxDifferent: 345 },
  { name: "gfx/DrawRedStringTest", maxDifferent: 513 },
  { name: "gfx/TextBoxTest", maxDifferent: 0, todo: true },
  { name: "gfx/DirectUtilsCreateImageTest", maxDifferent: 0 },
  { name: "gfx/GetPixelsDrawPixelsTest", maxDifferent: 0 },
  { name: "gfx/OffScreenCanvasTest", maxDifferent: 0 },
  { name: "gfx/ARGBColorTest", maxDifferent: 0 },
  { name: "gfx/GetRGBDrawRGBTest", maxDifferent: 0 },
  { name: "gfx/GetRGBDrawRGBWidthHeightTest", maxDifferent: 0 },
  { name: "gfx/GetRGBDrawRGBxyTest", maxDifferent: 0 },
  { name: "gfx/GetRGBDrawRGBNoAlphaTest", maxDifferent: 0, todo: true },
  { name: "gfx/ClippingTest", maxDifferent: 0 },
  { name: "gfx/ImageProcessingTest", maxDifferent: 6466 },
  { name: "gfx/CreateImageWithRegionTest", maxDifferent: 0 },
  { name: "gfx/DrawSubstringTest", maxDifferent: 332 },
  { name: "gfx/DrawLineOffscreenCanvasTest", maxDifferent: 1500 },
  { name: "gfx/DirectUtilsClipAfter", maxDifferent: 0 },
  { name: "gfx/DirectUtilsClipAfterOnScreen", maxDifferent: 0, todo: true },
  { name: "gfx/DirectUtilsClipAfterOnScreen2", maxDifferent: 0 },
  { name: "gfx/DirectUtilsClipAfterWithNormalImage", maxDifferent: 0 },
  { name: "gfx/DirectUtilsClipBefore", maxDifferent: 0 },
  { name: "gfx/DirectUtilsClipBeforeOnScreen", maxDifferent: 0, todo: true },
  { name: "gfx/DirectUtilsClipBeforeOnScreen2", maxDifferent: 0 },
  { name: "gfx/DirectUtilsClipBeforeWithNormalImage", maxDifferent: 0 },
  { name: "gfx/ImmutableImageFromByteArrayTest", maxDifferent: 0 },
  { name: "gfx/ClippingWithAnchorTest", maxDifferent: 0 },
  { name: "gfx/DirectGraphicsDrawPixelsWithXY", maxDifferent: 0 },
  { name: "gfx/DrawStringRightAnchorTest", maxDifferent: 333 },
  { name: "gfx/DrawStringBaselineAnchorTest", maxDifferent: 327 },
  { name: "gfx/DrawStringBottomAnchorTest", maxDifferent: 347 },
  { name: "gfx/DrawStringHCenterAnchorTest", maxDifferent: 333 },
  { name: "gfx/RectAfterText", maxDifferent: 637 },
  { name: "gfx/DrawStringWithEmojiTest", maxDifferent: 1133 },
  { name: "gfx/DrawSubstringWithEmojiTest", maxDifferent: 1133 },
  { name: "gfx/DrawCharsWithEmojiTest", maxDifferent: 1133 },
  { name: "gfx/CreateImmutableCopyTest", maxDifferent: 0 },
  { name: "gfx/LauncherTest", maxDifferent: 0 },
  { name: "gfx/MediaImageTest", maxDifferent: 0 },
  { name: "gfx/TextEditorGfxTest", maxDifferent: 1339 },
  { name: "gfx/DrawStringWithCopyrightAndRegisteredSymbols", maxDifferent: 244 },
];

var expectedUnitTestResults = [
  { name: "pass", number: 71548 },
  { name: "fail", number: 0 },
  { name: "known fail", number: 214 },
  { name: "unknown pass", number: 0 }
];

/**
 * Add a step that syncs the virtual filesystem to the persistent datastore,
 * to ensure all changes are synced before we move to the next step.
 *
 * We need to do this because the virtual filesystem caches changes,
 * while the tests often unload pages right after writing to the filesystem,
 * so sometimes those changes won't yet be synced on unload, though a subsequent
 * step depends on them.
 *
 * And we can't block unload while forcing a sync from within the app
 * because IndexedDB doesn't block unloads, it simply drops transactions
 * when the page is unloaded.
 */
function syncFS() {
    casper.waitForText("SYNC FILESYSTEM");
    casper.evaluate(function() {
        fs.syncStore(function() {
            console.log("SYNC FILESYSTEM");
        });
    });
}

casper.test.begin("unit tests", 16 + gfxTests.length, function(test) {
    casper.start("data:text/plain,start");

    casper.page.onLongRunningScript = function(message) {
        casper.echo("FAIL unresponsive " + message, "ERROR");
        casper.page.stopJavaScript();
    };

    // Run the Init midlet, which does nothing by itself but ensures that any
    // initialization code gets run before we start a test that depends on it.
    casper
    .thenOpen("http://localhost:8000/index.html?midletClassName=midlets.InitMidlet&jars=tests/tests.jar&logConsole=web,page")
    .withFrame(0, function() {
        casper.waitForText("DONE", syncFS);
    });

    casper
    .thenOpen("http://localhost:8000/tests/fs/test-fs-init.html")
    .waitForText("DONE", function() {
        test.assertTextExists("DONE: 30 pass, 0 fail", "test fs init");
    });

    function basicUnitTests() {
        casper.waitForText("DONE", function() {
            var content = this.getPageContent();
            var regex = /DONE: (\d+) pass, (\d+) fail, (\d+) known fail, (\d+) unknown pass/;
            var match = content.match(regex);
            if (!match || !match.length || match.length < 5) {
                this.echo("data:image/png;base64," + this.captureBase64('png'));
                test.fail('failed to parse status line of main unit tests');
            } else {
                var msg = "";
                for (var i = 0; i < expectedUnitTestResults.length; i++) {
                    if (match[i+1] != expectedUnitTestResults[i].number) {
                        msg += "\n\tExpected " + expectedUnitTestResults[i].number + " " + expectedUnitTestResults[i].name + ". Got " + match[i+1];
                    }
                }
                if (!msg) {
                    test.pass('main unit tests');
                } else {
                    this.echo("data:image/png;base64," + this.captureBase64('png'));
                    test.fail(msg);
                }
            }
            syncFS();
        });
    }

    casper
    .thenOpen("http://localhost:8000/index.html?logConsole=web,page")
    .withFrame(0, basicUnitTests);

    casper
    .thenOpen("http://localhost:8000/index.html?numCalled=1000&logConsole=web,page")
    .withFrame(0, basicUnitTests);

    casper
    .thenOpen("http://localhost:8000/index.html?main=tests/isolate/TestIsolate&logLevel=info&logConsole=web,page,raw")
    .withFrame(0, function() {
        casper.waitForText("DONE", function() {
            test.assertTextExists("I m\n" +
                                  "I a ma\n" +
                                  "I 2\n" +
                                  "I ma\n" +
                                  "I 2\n" +
                                  "I 1 isolate\n" +
                                  "I Isolate ID correct\n" +
                                  "I 4\n" +
                                  "I 5\n" +
                                  "I 1 isolate\n" +
                                  "I ma\n" +
                                  "I ma\n" +
                                  "I 3 isolates\n" +
                                  "I 1 m1\n" +
                                  "I 4\n" +
                                  "I 2 m2\n" +
                                  "I 5\n" +
                                  "I ma\n" +
                                  "I 1 isolate\n" +
                                  "I Isolates terminated\n" +
                                  "I r mar\n" +
                                  "I 2\n" +
                                  "I mar\n" +
                                  "I c marc\n" +
                                  "I 2\n" +
                                  "I marc\n" +
                                  "I Main isolate still running");
        });
    });

    casper
    .thenOpen("http://localhost:8000/index.html?midletClassName=tests/alarm/MIDlet1&jad=tests/midlets/alarm/alarm.jad&jars=tests/tests.jar&logConsole=web,page")
    .withFrame(0, function() {
        casper.waitForText("Hello World from MIDlet2", function() {
            test.pass();
        });
    });

    casper
    .thenOpen("http://localhost:8000/index.html?midletClassName=tests.sms.SMSMIDlet&jars=tests/tests.jar&logConsole=web,page")
    .withFrame(0, function() {
        this.waitForText("START", function() {
            this.evaluate(function() {
                promptForMessageText();
            });
            this.waitUntilVisible(".sms-listener-prompt", function() {
                this.sendKeys(".sms-listener-prompt.visible input", "Prova SMS", { reset: true });
                this.click(".sms-listener-prompt.visible button.recommend");
                this.waitForText("DONE", function() {
                    test.assertTextDoesntExist("FAIL");
                });
            });
        });
    });

    casper
    .thenOpen("http://localhost:8000/index.html?midletClassName=tests.fileui.FileUIMIDlet&jars=tests/tests.jar&logConsole=web,page")
    .withFrame(0, function() {
        this.waitForText("START", function() {
            this.waitUntilVisible(".nokia-fileui-prompt", function() {
                this.fill("form.nokia-fileui-prompt.visible", {
                    "nokia-fileui-file": system.args[4],
                });
                this.click(".nokia-fileui-prompt.visible input");
                this.click(".nokia-fileui-prompt.visible button.recommend");
                this.waitForText("DONE", function() {
                    var content = this.getPageContent();
                    if (content.contains("FAIL")) {
                        this.echo("data:image/png;base64," + this.captureBase64('png'));
                        test.fail('file-ui test');
                    } else {
                        test.pass("file-ui test");
                    }
                });
            });
        });
    });

    // Graphics tests

    gfxTests.forEach(function(testCase) {
        casper
        .thenOpen("http://localhost:8000/index.html?fontSize=12&midletClassName=" + testCase.name + "&jars=tests/tests.jar&logConsole=web,page")
        .withFrame(0, function() {
            casper.waitForText("PAINTED", function() {
                this.waitForSelector("#canvas", function() {
                    this.capture("test.png");

                    this.evaluate(function(testCase) {
                        var gotURL = "test.png";
                        var expectedURL = "tests/" + testCase.name + ".png";

                        var getImageData = function(url) {
                            return new Promise(function(resolve, reject) {
                                var img = new Image();
                                img.src = url;
                                img.onload = function() {
                                    var canvas = document.createElement('canvas');
                                    canvas.width = img.width;
                                    canvas.height = img.height;
                                    canvas.getContext("2d").drawImage(img, 0, 0);
                                    var pixels = new Uint32Array(canvas.getContext("2d").getImageData(0, 0, img.width, img.height).data.buffer);
                                    resolve({
                                        canvas: canvas,
                                        pixels: pixels,
                                    });
                                };
                                img.onerror = function() {
                                    console.log("Error while loading test image " + url);
                                    console.log("FAIL");
                                    reject();
                                };
                            });
                        };

                        Promise.all([getImageData(gotURL), getImageData(expectedURL)]).then(function(results) {
                            var got = results[0];
                            var expected = results[1];

                            if (expected.canvas.width !== got.canvas.width || expected.canvas.height !== got.canvas.height) {
                                console.log("Dimensions are wrong");
                                console.log("FAIL");
                                return;
                            }

                            var different = 0;
                            var i = 0;
                            for (var x = 0; x < got.canvas.width; x++) {
                                for (var y = 0; y < got.canvas.height; y++) {
                                    if (expected.pixels[i] !== got.pixels[i]) {
                                        different++;
                                    }

                                    i++;
                                }
                            }

                            var message = different + " <= " + testCase.maxDifferent;
                            if (different > testCase.maxDifferent) {
                                console.log(got.canvas.toDataURL());
                                if (!testCase.todo) {
                                  console.log("FAIL - " + message);
                                } else {
                                  console.log("TODO - " + message);
                                }
                            } else {
                                if (!testCase.todo) {
                                    console.log("PASS - " + message);
                                } else {
                                    console.log("UNEXPECTED PASS - " + message);
                                }
                            }

                            console.log("DONE");
                        });
                    }, testCase);

                    this.waitForText("DONE", function() {
                        var content = this.getPageContent();
                        var fail = content.contains("FAIL");
                        var todo = content.contains("TODO");
                        var unexpected = content.contains("UNEXPECTED");

                        if (fail) {
                            test.fail(testCase.name + " - Failure");
                        } else if (unexpected) {
                            test.fail(testCase.name + " - Unexpected pass");
                        } else if (todo) {
                            test.skip(1, testCase.name + " - Todo");
                        } else {
                            test.pass(testCase.name + " - Pass");
                        }

                        fs.remove("test.png");
                    });
                });
            });
        });
    });

    casper
    .thenOpen("http://localhost:8000/index.html?downloadJAD=http://localhost:8000/tests/Manifest1.jad&midletClassName=tests.jaddownloader.AMIDlet&logConsole=web,page&args=1.0.0")
    .withFrame(0, function() {
        casper.waitForText("DONE", function() {
            test.assertTextExists("SUCCESS 3/3", "test JAD downloader - Download");
            syncFS();
        });
    });

    // Run the test a second time to ensure loading the JAR stored in the FS works correctly.
    casper
    .thenOpen("http://localhost:8000/index.html?downloadJAD=http://localhost:8000/tests/Manifest1.jad&midletClassName=tests.jaddownloader.AMIDlet&logConsole=web,page&args=1.0.0")
    .withFrame(0, function() {
        casper.waitForText("DONE", function() {
            test.assertTextExists("SUCCESS 3/3", "test JAD downloader - Load");
            syncFS();
        });
    });


    // Run the test that updates the MIDlet
    casper
    .thenOpen("http://localhost:8000/index.html?downloadJAD=http://localhost:8000/tests/Manifest1.jad&midletClassName=tests.jaddownloader.AMIDletUpdater&logConsole=web,page")
    .withFrame(0, function() {
        var alertText = null;
        casper.on('remote.alert', function onAlert(message) {
            casper.removeListener('remote.alert', onAlert);
            alertText = message;
        });

        casper.waitFor(function() {
            return !!alertText;
        }, function() {
            test.assertEquals(alertText, "Update completed!");
            syncFS();
        });
    });

    // Verify that the update has been applied
    casper
    .thenOpen("http://localhost:8000/index.html?downloadJAD=http://localhost:8000/tests/Manifest1.jad&midletClassName=tests.jaddownloader.AMIDlet&logConsole=web,page&args=3.0.0")
    .withFrame(0, function() {
        casper.waitForText("DONE", function() {
            test.assertTextExists("SUCCESS 3/3", "test JAD downloader - Load after update");
            syncFS();
        });
    });

    // Clear the FS before downloading another JAD
    casper
    .thenOpen("http://localhost:8000/tests/fs/delete-fs.html")
    .waitForText("DONE");

    casper
    .thenOpen("http://localhost:8000/index.html?downloadJAD=http://localhost:8000/tests/Manifest2.jad&midletClassName=tests.jaddownloader.AMIDlet&logConsole=web,page&args=2.0.0")
    .withFrame(0, function() {
        casper.waitForText("DONE", function() {
            test.assertTextExists("SUCCESS 3/3", "test JAD downloader - Download with absolute URL");
            syncFS();
        });
    });

    casper
    .thenOpen("http://localhost:8000/index.html?midletClassName=com.sun.midp.midlet.TestMIDletPeer&jars=tests/tests.jar&logConsole=web,page")
    .waitForPopup("test.html", function() {
        test.assertEquals(this.popups.length, 1);
        test.assertTextDoesntExist("FAIL");
    });

    casper
    .thenOpen("http://localhost:8000/index.html?midletClassName=midlets.TestAlertWithGauge&jars=tests/tests.jar&logConsole=web,page")
    .withFrame(0, function() {
        this.waitUntilVisible(".lcdui-alert.visible .button1", function() {
            this.click(".lcdui-alert.visible .button0");
            this.waitForText("You pressed 'Yes'", function() {
                test.assertTextDoesntExist("FAIL");

                this.click(".lcdui-alert.visible .button1");
                this.waitForText("You pressed 'No'", function() {
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
