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

casper.options.waitTimeout = 90000;
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
  { name: "gfx/AlertTest", maxDifferentLinux: 1401, maxDifferentMac: 1889 },
  { name: "gfx/AlertTwoCommandsTest", maxDifferentLinux: 1538, maxDifferentMac: 2046 },
  { name: "gfx/CanvasTest", maxDifferentLinux: 0, maxDifferentMac: 0 },
  { name: "gfx/CanvasWithHeaderTest", maxDifferentLinux: 823, maxDifferentMac: 1351 },
  { name: "gfx/ImageRenderingTest", maxDifferentLinux: 0, maxDifferentMac: 22113 },
  { name: "gfx/FillRectTest", maxDifferentLinux: 0, maxDifferentMac: 756 },
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
  { name: "gfx/ImageProcessingTest2", maxDifferentLinux: 0, maxDifferentMac: 0 },
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
  { name: "gfx/DrawStringViaImageTest", maxDifferentLinux: 0, maxDifferentMac: 0 },
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

casper.test.begin("gfx tests", gfxTests.length, function(test) {
    casper.start("data:text/plain,start");

    casper.page.onLongRunningScript = function(message) {
        casper.echo("FAIL unresponsive " + message, "ERROR");
        casper.page.stopJavaScript();
    };

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
    .run(function() {
        test.done();
    });
});
