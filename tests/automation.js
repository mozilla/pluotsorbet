/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

var system = require('system');
var fs = require('fs');

// Enable TCP socket API and grant tcp-socket permission to the testing page
var { Cu } = require("chrome");
Cu.import("resource://gre/modules/Services.jsm");
Services.prefs.setBoolPref("dom.mozTCPSocket.enabled", true);
var uri = Services.io.newURI("http://localhost:8000", null, null);
var principal = Services.scriptSecurityManager.getNoAppCodebasePrincipal(uri);
Services.perms.addFromPrincipal(principal, "tcp-socket", Services.perms.ALLOW_ACTION);

casper.on('remote.message', function(message) {
    this.echo(message);
});

casper.options.waitTimeout = 80000;
casper.options.verbose = true;
casper.options.viewportSize = { width: 240, height: 320 };
casper.options.clientScripts = [
  "tests/mocks/getUserMedia.js",
];

casper.options.onWaitTimeout = function() {
    this.echo("data:image/png;base64," + this.captureBase64('png'));
    this.test.fail("Timeout");
};

var gfxTests = [
  { name: "gfx/AlertTest", maxDifferentLinux: 1266, maxDifferentMac: 2029 },
  { name: "gfx/AlertTwoCommandsTest", maxDifferentLinux: 1403, maxDifferentMac: 2186 },
  { name: "gfx/CanvasTest", maxDifferentLinux: 0, maxDifferentMac: 0 },
  { name: "gfx/CanvasWithHeaderTest", maxDifferentLinux: 823, maxDifferentMac: 1351 },
  { name: "gfx/ImageRenderingTest", maxDifferentLinux: 0, maxDifferentMac: 0 },
  { name: "gfx/FillRectTest", maxDifferentLinux: 0, maxDifferentMac: 0 },
  { name: "gfx/DrawAndFillRoundRectTest", maxDifferentLinux: 243, maxDifferentMac: 1592 },
  { name: "gfx/DrawAndFillArcTest", maxDifferentLinux: 5, maxDifferentMac: 1765 },
  { name: "gfx/DrawStringTest", maxDifferentLinux: 232, maxDifferentMac: 321 },
  { name: "gfx/DrawRedStringTest", maxDifferentLinux: 338, maxDifferentMac: 485 },
  { name: "gfx/TextBoxTest", maxDifferentLinux: 0, maxDifferentMac: 0, todo: true },
  { name: "gfx/DirectUtilsCreateImageTest", maxDifferentLinux: 0, maxDifferentMac: 0 },
  { name: "gfx/GetPixelsDrawPixelsTest", maxDifferentLinux: 0, maxDifferentMac: 0 },
  { name: "gfx/OffScreenCanvasTest", maxDifferentLinux: 0, maxDifferentMac: 0 },
  { name: "gfx/ARGBColorTest", maxDifferentLinux: 0, maxDifferentMac: 0 },
  { name: "gfx/GetRGBDrawRGBTest", maxDifferentLinux: 0, maxDifferentMac: 0 },
  { name: "gfx/GetRGBDrawRGBWidthHeightTest", maxDifferentLinux: 0, maxDifferentMac: 0 },
  { name: "gfx/GetRGBDrawRGBxyTest", maxDifferentLinux: 0, maxDifferentMac: 0 },
  { name: "gfx/GetRGBDrawRGBNoAlphaTest", maxDifferentLinux: 0, maxDifferentMac: 0, todo: true },
  { name: "gfx/ClippingTest", maxDifferentLinux: 0, maxDifferentMac: 0 },
  { name: "gfx/ImageProcessingTest", maxDifferentLinux: 0, maxDifferentMac: 0 },
  { name: "gfx/CreateImageWithRegionTest", maxDifferentLinux: 0, maxDifferentMac: 0 },
  { name: "gfx/DrawSubstringTest", maxDifferentLinux: 205, maxDifferentMac: 295 },
  { name: "gfx/DrawLineOffscreenCanvasTest", maxDifferentLinux: 0, maxDifferentMac: 788 },
  { name: "gfx/DirectUtilsClipAfter", maxDifferentLinux: 0, maxDifferentMac: 0 },
  { name: "gfx/DirectUtilsClipAfterOnScreen", maxDifferentLinux: 0, maxDifferentMac: 0, todo: true },
  { name: "gfx/DirectUtilsClipAfterOnScreen2", maxDifferentLinux: 0, maxDifferentMac: 0 },
  { name: "gfx/DirectUtilsClipAfterWithNormalImage", maxDifferentLinux: 0, maxDifferentMac: 0 },
  { name: "gfx/DirectUtilsClipBefore", maxDifferentLinux: 0, maxDifferentMac: 0 },
  { name: "gfx/DirectUtilsClipBeforeOnScreen", maxDifferentLinux: 0, maxDifferentMac: 0, todo: true },
  { name: "gfx/DirectUtilsClipBeforeOnScreen2", maxDifferentLinux: 0, maxDifferentMac: 0 },
  { name: "gfx/DirectUtilsClipBeforeWithNormalImage", maxDifferentLinux: 0, maxDifferentMac: 0 },
  { name: "gfx/ImmutableImageFromByteArrayTest", maxDifferentLinux: 0, maxDifferentMac: 0 },
  { name: "gfx/ClippingWithAnchorTest", maxDifferentLinux: 0, maxDifferentMac: 0 },
  { name: "gfx/DirectGraphicsDrawPixelsWithXY", maxDifferentLinux: 0, maxDifferentMac: 0 },
  { name: "gfx/DrawStringRightAnchorTest", maxDifferentLinux: 252, maxDifferentMac: 319 },
  { name: "gfx/DrawStringBaselineAnchorTest", maxDifferentLinux: 233, maxDifferentMac: 292 },
  { name: "gfx/DrawStringBottomAnchorTest", maxDifferentLinux: 233, maxDifferentMac: 322 },
  { name: "gfx/DrawStringHCenterAnchorTest", maxDifferentLinux: 213, maxDifferentMac: 301 },
  { name: "gfx/RectAfterText", maxDifferentLinux: 438, maxDifferentMac: 576 },
  { name: "gfx/DrawStringWithEmojiTest", maxDifferentLinux: 968, maxDifferentMac: 1151 },
  { name: "gfx/DrawSubstringWithEmojiTest", maxDifferentLinux: 968, maxDifferentMac: 1151 },
  { name: "gfx/DrawCharsWithEmojiTest", maxDifferentLinux: 968, maxDifferentMac: 1151 },
  { name: "gfx/CreateImmutableCopyTest", maxDifferentLinux: 0, maxDifferentMac: 0 },
  { name: "gfx/LauncherTest", maxDifferentLinux: 0, maxDifferentMac: 0 },
  { name: "gfx/MediaImageTest", maxDifferentLinux: 0, maxDifferentMac: 0 },
  { name: "gfx/TextEditorGfxTest", maxDifferentLinux: 968, maxDifferentMac: 1057 },
  { name: "gfx/DrawStringWithCopyrightAndRegisteredSymbols", maxDifferentLinux: 159, maxDifferentMac: 248 },
  { name: "gfx/VideoPlayerTest", maxDifferentLinux: 0, maxDifferentMac: 0 },
  { name: "gfx/ImageCapture", maxDifferentLinux: 0, maxDifferentMac: 0 },
  { name: "gfx/CameraTest", maxDifferentLinux: 0, maxDifferentMac: 0 },
  { name: "gfx/DrawRegionTransMirrorAnchorBottomHCenter", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransMirrorAnchorBottomLeft", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransMirrorAnchorBottomRight", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransMirrorAnchorTopHCenter", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransMirrorAnchorTopLeft", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransMirrorAnchorTopRight", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransMirrorAnchorVCenterHCenter", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransMirrorAnchorVCenterLeft", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransMirrorAnchorVCenterRight", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransMirrorRot180AnchorBottomHCenter", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransMirrorRot180AnchorBottomLeft", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransMirrorRot180AnchorBottomRight", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransMirrorRot180AnchorTopHCenter", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransMirrorRot180AnchorTopLeft", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransMirrorRot180AnchorTopRight", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransMirrorRot180AnchorVCenterHCenter", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransMirrorRot180AnchorVCenterLeft", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransMirrorRot180AnchorVCenterRight", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransMirrorRot270AnchorBottomHCenter", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransMirrorRot270AnchorBottomLeft", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransMirrorRot270AnchorBottomRight", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransMirrorRot270AnchorTopHCenter", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransMirrorRot270AnchorTopLeft", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransMirrorRot270AnchorTopRight", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransMirrorRot270AnchorVCenterHCenter", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransMirrorRot270AnchorVCenterLeft", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransMirrorRot270AnchorVCenterRight", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransMirrorRot90AnchorBottomHCenter", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransMirrorRot90AnchorBottomLeft", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransMirrorRot90AnchorBottomRight", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransMirrorRot90AnchorTopHCenter", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransMirrorRot90AnchorTopLeft", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransMirrorRot90AnchorTopRight", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransMirrorRot90AnchorVCenterHCenter", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransMirrorRot90AnchorVCenterLeft", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransMirrorRot90AnchorVCenterRight", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransNoneAnchorBottomHCenter", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransNoneAnchorBottomLeft", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransNoneAnchorBottomRight", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransNoneAnchorTopHCenter", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransNoneAnchorTopLeft", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransNoneAnchorTopRight", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransNoneAnchorVCenterHCenter", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransNoneAnchorVCenterLeft", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransNoneAnchorVCenterRight", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransRot180AnchorBottomHCenter", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransRot180AnchorBottomLeft", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransRot180AnchorBottomRight", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransRot180AnchorTopHCenter", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransRot180AnchorTopLeft", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransRot180AnchorTopRight", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransRot180AnchorVCenterHCenter", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransRot180AnchorVCenterLeft", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransRot180AnchorVCenterRight", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransRot270AnchorBottomHCenter", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransRot270AnchorBottomLeft", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransRot270AnchorBottomRight", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransRot270AnchorTopHCenter", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransRot270AnchorTopLeft", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransRot270AnchorTopRight", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransRot270AnchorVCenterHCenter", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransRot270AnchorVCenterLeft", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransRot270AnchorVCenterRight", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransRot90AnchorBottomHCenter", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransRot90AnchorBottomLeft", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransRot90AnchorBottomRight", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransRot90AnchorTopHCenter", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransRot90AnchorTopLeft", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransRot90AnchorTopRight", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransRot90AnchorVCenterHCenter", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransRot90AnchorVCenterLeft", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/DrawRegionTransRot90AnchorVCenterRight", maxDifferentLinux: 164, maxDifferentMac: 164 },
  { name: "gfx/MultipleImageGraphicsTest", maxDifferentLinux: 0, maxDifferentMac: 0 },
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

casper.test.begin("unit tests", 25 + gfxTests.length, function(test) {
    casper.start("data:text/plain,start");

    casper.page.onLongRunningScript = function(message) {
        casper.echo("FAIL unresponsive " + message, "ERROR");
        casper.page.stopJavaScript();
    };

    // Run the Init midlet, which does nothing by itself but ensures that any
    // initialization code gets run before we start a test that depends on it.
    casper
    .thenOpen("http://localhost:8000/index.html?midletClassName=midlets.InitMidlet&jars=tests/tests.jar&logConsole=web,page&logLevel=log")
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
            var regex = /DONE: (\d+) class pass, (\d+) class fail/;
            var match = content.match(regex);
            if (!match || !match.length || match.length < 3) {
                this.echo("data:image/png;base64," + this.captureBase64('png'));
                test.fail('failed to parse status line of main unit tests');
            } else {
                var failed = match[2];
                if (failed === "0") {
                    test.pass('main unit tests');
                } else {
                    test.fail(failed + " unit test(s) failed");
                }
            }
            syncFS();
        });
    }

    casper
    .thenOpen("http://localhost:8000/index.html?logConsole=web,page&logLevel=log")
    .withFrame(0, basicUnitTests);

    // Run the same unit tests again to test the compiled method cache.
    casper
    .thenOpen("http://localhost:8000/index.html?logConsole=web,page&logLevel=log")
    .withFrame(0, basicUnitTests);

    // Run the same unit tests again with baseline JIT enabled for all methods.
    casper
    .thenOpen("http://localhost:8000/index.html?logConsole=web,page&logLevel=log&forceRuntimeCompilation=1")
    .withFrame(0, basicUnitTests);

    casper
    .thenOpen("http://localhost:8000/index.html?main=tests/isolate/TestIsolate&logLevel=info&logConsole=web,page,raw")
    .withFrame(0, function() {
        casper.waitForText("DONE", function() {
            test.assertTextExists("I m\n" +
                                  "I a ma\n" +
                                  "I 3\n" +
                                  "I ma\n" +
                                  "I 3\n" +
                                  "I 1 isolate\n" +
                                  "I Isolate ID correct\n" +
                                  "I 5\n" +
                                  "I 6\n" +
                                  "I 1 isolate\n" +
                                  "I ma\n" +
                                  "I ma\n" +
                                  "I 3 isolates\n" +
                                  "I 1 m1\n" +
                                  "I 5\n" +
                                  "I 2 m2\n" +
                                  "I 6\n" +
                                  "I ma\n" +
                                  "I 1 isolate\n" +
                                  "I Isolates terminated\n" +
                                  "I r mar\n" +
                                  "I 3\n" +
                                  "I mar\n" +
                                  "I c marc\n" +
                                  "I 3\n" +
                                  "I marc\n" +
                                  "I Main isolate still running");
        });
    });

    casper
    .thenOpen("http://localhost:8000/index.html?main=MainStaticInitializer&logLevel=info&logConsole=web,page,raw")
    .withFrame(0, function() {
        casper.waitForText("DONE", function() {
            test.assertTextExists("I 1) static init\n" +
                                  "I 2) main");
        });
    });

    casper
    .thenOpen("http://localhost:8000/index.html?midletClassName=tests/alarm/MIDlet1&jad=tests/midlets/alarm/alarm.jad&jars=tests/tests.jar&logConsole=web,page&logLevel=log")
    .withFrame(0, function() {
        casper.waitForText("Hello World from MIDlet2", function() {
            test.pass();
        });
    });

    casper
    .thenOpen("http://localhost:8000/index.html?midletClassName=tests/recordstore/WriterMIDlet&jad=tests/midlets/RecordStore/recordstore.jad&jars=tests/tests.jar&logConsole=web,page&logLevel=log")
    .withFrame(0, function() {
        casper.waitForText("DONE", function() {
            test.assertTextDoesntExist("FAIL");
            test.assertTextExists("SUCCESS 8/8", "Test RecordStore with multiple MIDlets");
        });
    });

    casper
    .thenOpen("http://localhost:8000/index.html?midletClassName=tests.background.BackgroundMIDlet1&jad=tests/midlets/background/background1.jad&jars=tests/tests.jar&logConsole=web,page&logLevel=log")
    .withFrame(0, function() {
        casper.waitForText("Hello World from foreground MIDlet", function() {
            test.pass();
        });
    });

    casper
    .thenOpen("http://localhost:8000/index.html?midletClassName=tests.background.BackgroundMIDlet2&jad=tests/midlets/background/background2.jad&jars=tests/tests.jar&logConsole=web,page&logLevel=log")
    .withFrame(0, function() {
        casper.waitForText("Hello World from foreground MIDlet", function() {
            test.pass();
        });
    });

    casper
    .thenOpen("http://localhost:8000/index.html?midletClassName=tests.background.BackgroundMIDlet3&jad=tests/midlets/background/background3.jad&jars=tests/tests.jar&logConsole=web,page&logLevel=log")
    .withFrame(0, function() {
        casper.waitForText("Hello World from foreground MIDlet", function() {
            test.assertTextExists("prop1=hello prop2=ciao");
        });
    });

    casper
    .thenOpen("http://localhost:8000/index.html?midletClassName=tests.background.BackgroundMIDlet1&jad=tests/midlets/background/foregroundExit.jad&jars=tests/tests.jar&logConsole=web,page&logLevel=log", function() {
      casper.evaluate(function() {
        window.close = function() {
          document.title = "window.close called";
        }
      });

      casper.waitFor(function() {
        return !!this.getTitle();
      }, function() {
        test.assertEquals(this.getTitle(), "window.close called", "window.close called");
      });
    });

    casper
    .thenOpen("http://localhost:8000/index.html?midletClassName=tests.sms.SMSMIDlet&jars=tests/tests.jar&logConsole=web,page&logLevel=log")
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
    .thenOpen("http://localhost:8000/index.html?midletClassName=tests.fileui.FileUIMIDlet&jars=tests/tests.jar&logConsole=web,page&logLevel=log")
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
        .thenOpen("http://localhost:8000/index.html?fontSize=12&midletClassName=" + testCase.name + "&jars=tests/tests.jar&logConsole=web,page&logLevel=log")
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

                            var maxDifferent = navigator.platform.indexOf("Linux") != -1 ?
                                                 testCase.maxDifferentLinux :
                                                 testCase.maxDifferentMac;

                            var message = different + " <= " + maxDifferent;
                            if (different > maxDifferent) {
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
    .thenOpen("http://localhost:8000/index.html?downloadJAD=http://localhost:8000/tests/Manifest1.jad&midletClassName=tests.jaddownloader.AMIDlet&logConsole=web,page&args=1.0.0&logLevel=log")
    .withFrame(0, function() {
        casper.waitForText("DONE", function() {
            test.assertTextExists("SUCCESS 3/3", "test JAD downloader - Download");
            syncFS();
        });
    });

    // Run the test a second time to ensure loading the JAR stored in the JARStore works correctly.
    casper
    .thenOpen("http://localhost:8000/index.html?downloadJAD=http://localhost:8000/tests/Manifest1.jad&midletClassName=tests.jaddownloader.AMIDlet&logConsole=web,page&args=1.0.0&logLevel=log")
    .withFrame(0, function() {
        casper.waitForText("DONE", function() {
            test.assertTextExists("SUCCESS 3/3", "test JAD downloader - Load");
            syncFS();
        });
    });


    // Run the test that updates the MIDlet
    casper
    .thenOpen("http://localhost:8000/index.html?downloadJAD=http://localhost:8000/tests/Manifest1.jad&midletClassName=tests.jaddownloader.AMIDletUpdater&logConsole=web,page&logLevel=log")
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
    .thenOpen("http://localhost:8000/index.html?downloadJAD=http://localhost:8000/tests/Manifest1.jad&midletClassName=tests.jaddownloader.AMIDlet&logConsole=web,page&args=3.0.0&logLevel=log")
    .withFrame(0, function() {
        casper.waitForText("DONE", function() {
            test.assertTextExists("SUCCESS 3/3", "test JAD downloader - Load after update");
            syncFS();
        });
    });

    // Clear the JARStore before downloading another JAD
    casper
    .thenOpen("http://localhost:8000/tests/jarstore/clear-jarstore.html")
    .waitForText("DONE");

    casper
    .thenOpen("http://localhost:8000/index.html?downloadJAD=http://localhost:8000/tests/Manifest2.jad&midletClassName=tests.jaddownloader.AMIDlet&logConsole=web,page&args=2.0.0&logLevel=log")
    .withFrame(0, function() {
        casper.waitForText("DONE", function() {
            test.assertTextExists("SUCCESS 3/3", "test JAD downloader - Download with absolute URL");
            syncFS();
        });
    });

    casper
    .thenOpen("http://localhost:8000/index.html?midletClassName=com.sun.midp.midlet.TestMIDletPeer&jars=tests/tests.jar&logConsole=web,page&logLevel=log")
    .waitForPopup("test.html", function() {
        test.assertEquals(this.popups.length, 1);
        test.assertTextDoesntExist("FAIL");
    });

    casper
    .thenOpen("http://localhost:8000/index.html?midletClassName=midlets.TestAlertWithGauge&jars=tests/tests.jar&logConsole=web,page&logLevel=log")
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
    .thenOpen("http://localhost:8000/tests/jarstore/jarstoretests.html")
    .waitForText("DONE", function() {
        test.assertTextExists("DONE: 23 pass, 0 fail", "JARStore unit tests");
    });

    casper
    .run(function() {
        test.done();
    });
});
