/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

casper.on('remote.message', function(message) {
    this.echo(message);
});

casper.test.begin("unit tests", 2, function(test) {
    casper
    .start("http://localhost:8000/index.html?main=RunTests")
    .waitForText("DONE", function then() {
        test.assertTextExists("DONE: 712 pass", "run unit tests");
    });

    casper
    .thenOpen("http://localhost:8000/index.html?main=tests/isolate/TestIsolate")
    .waitForText("DONE", function then() {
    	test.assertTextExists("\
m\n\
a ma\n\
ma\n\
1 m1\n\
ma\n\
2 m2\n\
ma\n\
ma\n\
r mar\n\
mar\n\
c marc\n\
marc\n\
");
    });

    casper
    .run(function() {
        test.done();
    });
});
