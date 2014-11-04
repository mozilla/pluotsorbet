# j2me.js [![Build Status](https://travis-ci.org/andreasgal/j2me.js.svg)](https://travis-ci.org/andreasgal/j2me.js)

j2me.js is a J2ME virtual machine in JavaScript.

The current goals of j2me.js are:

1. Run MIDlets in a way that emulates the reference implementation of phone ME Feature MR4 (b01)
1. Keep j2me.js simple and small: Leverage the phoneME JDK/infrastructre and existing Java code as much as we can, and implement as little as possible in JavaScript

## Building j2me.js

Make sure you have a [JRE](http://www.oracle.com/technetwork/java/javase/downloads/jre7-downloads-1880261.html) installed

Get the [j2me.js repo](https://github.com/andreasgal/j2me.js) if you don't have it already

        git clone https://github.com/andreasgal/j2me.js

Build using make:

        cd j2me.js
        make

## Running MIDlets, tests

To run a MIDlet:

        python tests/httpServer.py &
        http://localhost:8000/index.html?jad=ExampleApp.jad&jars=ExampleApp.jar&midletClassName=com.example.yourClassNameHere

URL parameters:

* See full list at libs/urlparams.js
* Default `midletClassName` if none specified is `RunTests`

If testing sockets, 4 servers are necessary:

        python tests/httpServer.py &
        python tests/echoServer.py &
        cd tests && python httpsServer.py &
        cd tests && python sslEchoServer.py &

To run specific tests (e.g. TestSystem and TestRC4):

        python tests/httpServer.py &
        http://localhost:8000/index.html?args=java/lang/TestSystem,javax/crypto/TestRC4

Full list of RunTests tests available in the tests/Testlets.java generated file

There's no UI to enable certain privileges on Desktop that MIDlets might need. Use [Myk's tcpsocketpup addon](https://github.com/mykmelez/tcpsocketpup) to accomplish that

Continuous integration:

* Uses Travis
* Runs automation.js which uses Casper.js testing framework and slimer.js (Gecko backend for casper.js)
* `make test`

gfx tests use image comparison; output should match reference

## Logging

See `logConsole` and `logLevel` URL params in libs/console.js

## Running j2me.js in the SpiderMonkey shell

1. Download the [SpiderMonkey shell](https://developer.mozilla.org/en-US/docs/Mozilla/Projects/SpiderMonkey/Introduction_to_the_JavaScript_shell)
1. Execute the jsshell.js file as follows:

        js jsshell.js package.ClassName
