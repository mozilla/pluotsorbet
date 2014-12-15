/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

var system = require('system');

casper.on('remote.message', function(message) {
    this.echo(message);
});

casper.options.waitTimeout = 45000;

casper.options.onWaitTimeout = function() {
    this.debugPage();
    this.echo(this.captureBase64('png'));
    this.test.fail("Timeout");
};

var expectedUnitTestResults = [
  { name: "pass", number: 71222 },
  { name: "fail", number: 0 },
  { name: "known fail", number: 180 },
  { name: "unknown pass", number: 0 }
];

casper.test.begin("unit tests", 7, function(test) {
    casper
    .start("http://localhost:8000/tests/fs/make-fs-v1.html")
    .waitForText("DONE");

    casper
    .thenOpen("http://localhost:8000/tests/fs/test-fs-init.html")
    .waitForText("DONE", function() {
        test.assertTextExists("DONE: 10 PASS, 0 FAIL", "test fs init");
    });

    function basicUnitTests() {
        casper.waitForText("DONE", function() {
            var content = this.getPageContent();
            var regex = /DONE: (\d+) pass, (\d+) fail, (\d+) known fail, (\d+) unknown pass/;
            var match = content.match(regex);
            if (!match || !match.length || match.length < 5) {
                this.debugPage();
                this.echo(this.captureBase64('png'));
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
                    this.debugPage();
                    this.echo(this.captureBase64('png'));
                    test.fail(msg);
                }
            }
        });
    }

    casper
    .thenOpen("http://localhost:8000/index.html")
    .withFrame(0, basicUnitTests);

    casper
    .thenOpen("http://localhost:8000/tests/fs/fstests.html")
    .waitForText("DONE", function() {
        test.assertTextExists("DONE: 133 PASS, 0 FAIL", "run fs.js unit tests");
    });

    casper
    .thenOpen("http://localhost:8000/index.html?midletClassName=tests.fileui.FileUIMIDlet&jars=tests/tests.jar")
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
                        this.debugPage();
                        this.echo(this.captureBase64('png'));
                        test.fail('file-ui test');
                    } else {
                        test.pass("file-ui test");
                    }
                });
            });
        });
    });

    casper
    .thenOpen("http://localhost:8000/index.html?downloadJAD=http://localhost:8000/tests/Manifest1.jad&midletClassName=tests.jaddownloader.AMIDlet")
    .withFrame(0, function() {
        casper.waitForText("DONE", function() {
            test.pass();
        });
    });

    // Run the test a second time to ensure loading the JAR stored in the FS works correctly.
    casper
    .thenOpen("http://localhost:8000/index.html?downloadJAD=http://localhost:8000/tests/Manifest1.jad&midletClassName=tests.jaddownloader.AMIDlet")
    .withFrame(0, function() {
        casper.waitForText("DONE", function() {
            test.pass();
        });
    });

    casper
    .thenOpen("http://localhost:8000/index.html?downloadJAD=http://localhost:8000/tests/Manifest2.jad&midletClassName=tests.jaddownloader.AMIDlet")
    .withFrame(0, function() {
        casper.waitForText("DONE", function() {
            test.pass();
        });
    });

    casper
    .run(function() {
        test.done();
    });
});
