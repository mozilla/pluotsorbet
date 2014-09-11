/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

casper.on('remote.message', function(message) {
    this.echo(message);
});

casper.test.begin("unit tests", 6, function(test) {
    casper
    .start("http://localhost:8000/index.html")
    .waitForText("DONE", function then() {
        test.assertTextExists("DONE: 4398 pass, 0 fail, 162 known fail, 0 unknown pass", "run unit tests");
    }, function onTimeout() {
        test.fail();
    }, 30000);

    casper
    .thenOpen("http://localhost:8000/index.html?main=tests/isolate/TestIsolate&logLevel=info")
    .waitForText("DONE", function then() {
        test.assertTextExists("I m\nI a ma\nI 2\nI ma\nI 2\nI 1 isolate\nI Isolate ID correct\nI 4\nI 5\nI 1 isolate\nI ma\nI ma\nI 3 isolates\nI 1 m1\nI 2 m2\nI 4\nI 5\nI ma\nI 1 isolate\nI Isolates terminated\nI r mar\nI 2\nI mar\nI c marc\nI 2\nI marc\nI Main isolate still running");
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
        test.assertTextExists("DONE: 101 PASS, 0 FAIL", "run fs.js unit tests");
    });

    casper
    .thenOpen("http://localhost:8000/index.html?midletClassName=tests.sms.SMSMIDlet&main=com/sun/midp/main/MIDletSuiteLoader", function() {
      require('utils').dump("HERE0");
      this.waitForSelector("#sms_text", function() {
        require('utils').dump("HERE1");
        this.sendKeys("#sms_text", "Prova SMS");
        require('utils').dump("HERE2");
        this.waitForSelector("#sms_addr", function() {
          require('utils').dump("HERE3");
          this.sendKeys("#sms_addr", "+77777777777");
          require('utils').dump("HERE4");
          this.waitForSelector("#sms_receive", function() {
            require('utils').dump("HERE5");
            this.click("#sms_receive");
            require('utils').dump("HERE6");
this.waitForText("DONE", function then() {
  test.assertTextDoesntExist("FAIL");
}, function onTimeout() {
  this.debugPage();
    test.fail();
}, 10000);
          });
        });
      });
    });

    casper
    .run(function() {
        test.done();
    });
});
