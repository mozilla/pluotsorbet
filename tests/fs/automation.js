/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

var system = require('system');

casper.on('remote.message', function(message) {
    this.echo(message);
});

casper.options.waitTimeout = 15000;

casper.options.onWaitTimeout = function() {
    this.debugPage();
    this.echo(this.captureBase64('png'));
    this.test.fail("Timeout");
};

casper.test.begin("fs tests", 7, function(test) {
    // The main test automation script already initializes the fs database
    // to its latest version and runs the fs tests against it.  So this script
    // focuses on running tests against a database that is initialized
    // to the original version and then upgraded by the tests, so we can ensure
    // that upgrading the database works as expected.

    // In the long run, we may want to move all the fs tests into this script,
    // including the ones that initialize the database to the latest version,
    // by deleting and recreating the database between test runs.  We may also
    // want to move over the other tests that touch the fs.

    casper
    .start("http://localhost:8000/tests/fs/init-fs-v1.html")
    .waitForText("DONE");

    casper
    .thenOpen("http://localhost:8000/tests/fs/test-fs-init.html")
    .waitForText("DONE", function() {
        test.assertTextExists("DONE: 10 PASS, 0 FAIL", "test fs init");
    });

    casper
    .thenOpen("http://localhost:8000/tests/fs/fstests.html")
    .waitForText("DONE", function() {
        test.assertTextExists("DONE: 133 PASS, 0 FAIL", "run fs.js unit tests");
    });

    // Start with a v2 database.

    // Delete the existing db.
    casper
    .thenOpen("http://localhost:8000/tests/fs/delete-fs.html")
    .waitForText("DONE");

    // Initialize a v2 db and test that it contains the initial set of files.
    casper
    .thenOpen("http://localhost:8000/tests/fs/test-fs-init-v2.html")
    .waitForText("DONE", function() {
        test.assertTextExists("DONE: 10 PASS, 0 FAIL", "test fs init v2");
    });

    // Upgrade the db and test that it still contains the initial set of files.
    casper
    .thenOpen("http://localhost:8000/tests/fs/test-fs-init.html")
    .waitForText("DONE", function() {
        test.assertTextExists("DONE: 10 PASS, 0 FAIL", "test fs init");
    });

    // Run the unit tests.
    casper
    .thenOpen("http://localhost:8000/tests/fs/fstests.html")
    .waitForText("DONE", function() {
        test.assertTextExists("DONE: 133 PASS, 0 FAIL", "run fs.js unit tests");
    });

    // Start with a more populated v2 database.

    casper
    .thenOpen("http://localhost:8000/tests/fs/delete-fs.html")
    .waitForText("DONE");

    casper
    .thenOpen("http://localhost:8000/tests/fs/test-fs-populate-v2.html")
    .waitForText("DONE", function() {
        test.assertTextExists("DONE: 214 PASS, 0 FAIL", "test fs populate v2");
    });

    casper
    .thenOpen("http://localhost:8000/tests/fs/test-fs-population.html")
    .waitForText("DONE", function() {
        test.assertTextExists("DONE: 214 PASS, 0 FAIL", "test fs population");
    });

    casper
    .run(function() {
        test.done();
    });
});
