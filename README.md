# j2me.js [![Build Status](https://travis-ci.org/andreasgal/j2me.js.svg)](https://travis-ci.org/andreasgal/j2me.js)

j2me.js is a J2ME virtual machine in JavaScript.

The current goals of j2me.js are:

1. Run MIDlets in a way that emulates the reference implementation of phone ME Feature MR4 (b01)
1. Keep j2me.js simple and small: Leverage the phoneME JDK/infrastructure and existing Java code as much as we can, and implement as little as possible in JavaScript

## Building j2me.js

Make sure you have a [JRE](http://www.oracle.com/technetwork/java/javase/downloads/jre7-downloads-1880261.html) installed

Get the [j2me.js repo](https://github.com/andreasgal/j2me.js) if you don't have it already

        git clone https://github.com/andreasgal/j2me.js

Build using make:

        cd j2me.js
        make

## Running apps & MIDlets, Debugging

index.html is a webapp that runs j2me.js. The URL parameters you pass to index.html control the specific behavior of j2me.js.

### URL parameters

See full list at libs/urlparams.js

* `main` - default is `com/sun/midp/main/MIDletSuiteLoader`
* `midletClassName` - must be set to the main class to run. Only valid when default `main` parameter is used. Defaults to `RunTests`
* `autosize` - if set to `1`, j2me app will fill the page.
* `gamepad` - if set to `1`, gamepad will be visible/available.

### Desktop

To run a MIDlet on desktop, you must first start an http server that will host index.html. You can then connect to the http server, passing URL parameters to index.html

        python tests/httpServer.py &
        http://localhost:8000/index.html?jad=ExampleApp.jad&jars=ExampleApp.jar&midletClassName=com.example.yourClassNameHere

Example - Asteroids

        python tests/httpServer.py &
        http://localhost:8000/index.html?midletClassName=asteroids.Game&jars=tests/tests.jar&gamepad=1

Some apps require access to APIs that aren't enabled by default on Desktop Firefox and there is no UI built in to Desktop Firefox to enable them. APIs matching this description include:

* mozTCPSocket
* mozContacts
* mozbrowser
* notifications

To enable this type of API for a MIDlet you're running, use [Myk's API Enabler Addon](https://github.com/mykmelez/tcpsocketpup)

### FirefoxOS device (or emulator)

To run a MIDlet on a FirefoxOS device, update the `launch_path` property in manifest.webapp. The `midletClassName` URL parameter needs to point to an app.

Once you've updated manifest.webapp, connect to the device or emulator as described in the [FirefoxOS Developer Phone Guide](https://developer.mozilla.org/en-US/Firefox_OS/Developer_phone_guide/Flame) and select your j2me.js directory (the one containing manifest.webapp) when choosing the app to push to device.

Example - Asteroids

        "launch_path": "/index.html?midletClassName=asteroids.Game&jars=tests/tests.jar&logConsole=web&autosize=1&gamepad=1"

## Tests

You can run the test suite with `make test`. The main driver for the test suite is automation.js which uses the Casper.js testing framework and slimer.js (a Gecko backend for casper.js). This test suite runs on every push (continuous integration) thanks to Travis.

If you want to pass additional [casperJS command line options](http://docs.slimerjs.org/current/configuration.html), look at the "test" target in Makefile and place additional command line options before the automation.js filename.

gfx tests use image comparison; a reference image is provided with the test and the output of the test must match the reference image. The output is allowed to differ from the reference image by a number of pixels specified in automation.js.

The main set of unit tests that automation.js runs is the set covered by the RunTests MIDlet. The full list of RunTests tests available in the tests/Testlets.java generated file. RunTests runs a number of "Testlets" (Java classes that implement the `Testlet` interface).

### Running a single test

If the test you want to run is a class with a main method, specify a `main` URL parameter to index.html, e.g.:

        main=gnu/testlet/vm/SystemTest&jars=tests/tests.jar

If the test you want to run is a MIDlet, specify `midletClassName` and `jad` URL parameters to index.html (`main` will default to the MIDletSuiteLoader), e.g.:

        midletClassName=tests/alarm/MIDlet1&jad=tests/midlets/alarm/alarm.jad&jars=tests/tests.jar

If the test you want to run is a Testlet , specify an `args` URL parameter to index.html. You can specify multiple Testlets separated by commas, and you can use either '.' or '/' in the class name, e.g.:

        args=java.lang.TestSystem,javax.crypto.TestRC4,com/nokia/mid/ui/TestVirtualKeyboard

If the testlet uses sockets, you must start 4 servers (instead of just the http server):

        python tests/httpServer.py &
        python tests/echoServer.py &
        cd tests && python httpsServer.py &
        cd tests && python sslEchoServer.py &

### Failures (and what to do)

Frequent causes of failure include:

* automation.js expects a different number of tests to have passed than the number that actually passed (this is very common when adding new tests)
* timeout: Travis machines are generally slower than dev machines and so tests that pass locally will fail in the continuous integration tests
* Number of differing pixels in a gfx/rendering test exceeds the threshold allowed in automation.js. This will often happen because slimerJS uses a different version of Firefox than the developer. This can also happen because the test renders text, whose font rendering can vary from machine to machine, perhaps even with the same font.

gfx/rendering tests will print a number next to the error message. That number is the number of differing pixels. If it is close to the threshold you can probably just increase the threshold in automation.js with no ill effect.

The test output will include base64 encoded images; copy this into your browser's URL bar as a data URL to see what the actual test output looked like.

When running `make test`, verbose test output will be printed to your terminal. Check that for additional info on the failures that occurred.

## Logging

See `logConsole` and `logLevel` URL params in libs/console.js

## Running j2me.js in the SpiderMonkey shell

1. Download the [SpiderMonkey shell](https://developer.mozilla.org/en-US/docs/Mozilla/Projects/SpiderMonkey/Introduction_to_the_JavaScript_shell)
1. Execute the jsshell.js file as follows:

        js jsshell.js package.ClassName

## Coding Style

In general, stick with whatever style exists in the file you are modifying.

If you're creating a new file, use 4-space indents for Java and 2-space indents of JS.

Use JavaDoc to document public APIs in Java.

Modeline for Java files:

    /* vim: set filetype=java shiftwidth=4 tabstop=4 autoindent cindent expandtab : */

Modelines for JS files:

    /* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
    /* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

## Profiling

### JS profiling

One way to profile j2me.js is to use the JS profiler available in Firefox Dev Tools. This will tell us how well the JVM is working and how well natives work. This type of profiling will not measure time that is taken waiting for async callbacks to be called (for example, when using the native JS filesystem API).

When debugging using the WebIDE, enable the option "select an iframe as the currently targeted document" and select the iframe containing main.html as the debugging target. NB: you need to connect to a target device running FxOS 2.1 or up to use this feature in WebIDE.

Use these JS calls within the console to start and stop profiling (TODO: I haven't actually gotten these to work):

        Instrument.startProfile()
        Instrument.stopProfile()

It can be helpful to increase this `about:config` option: `devtools.hud.loglimit.console`

Alternatively, use the "Performance" tab of the Firefox Developer Tools.

### Java profiling

j2me.js includes its own profiler that is capable of measuring the performance of Java methods running inside its JVM.

When running j2me.js in Desktop Firefox, click the "profile" button that appears below the output iframe. Press the button again to stop profiling. You should get output including the total time taken inside each method and the number of times each method was called.

Add "&profile=1" to your URL parameter list to enable profile immediately upon loading j2me.js (index.html).

## Filesystem

midp/fs.js contains native implementations of various midp filesystem APIs.

Those implementations call out to lib/fs.js which is a JS implementation of a filesystem.

Uses async\_storage.js (from gaia) - async API for accessing IndexedDB

Java APIs are sync but our implementations use async APIs

## Implementing Java functions in native code

`native` keyword tells Java that the function is implemented in native code

e.g.:

    public static native long currentTimeMillis();

Java compiler will do nothing to ensure that implementation actually exists. At runtime, implementation better be available or you'll get a runtime exception.

We use `Native` object in JS to handle creation and registration of `native` functions. See native.js

    Native.create("name/of/function.(parameterTypes)returnType", jsFuncToCall, isAsync)

e.g.:

    Native.create("java/lang/System.arraycopy.(Ljava/lang/Object;ILjava/lang/Object;II)V", function(src, srcOffset, dst, dstOffset, length) {...});

If you need to implement a method in JS but you can't declare it `native` in Java, use `Override`.

e.g.:

   Override.create("com/ibm/oti/connection/file/Connection.decode.(Ljava/lang/String;)Ljava/lang/String;", function(...) {...});


If raising a Java `Exception`, throw new instance of Java `Exception` class as defined in runtime.ts, e.g.:

    throw $.newNullPointerException("Cannot copy to/from a null array.");

Remember:

  * Return types are automatically converted to Java types, but parameters are not automatically converted from Java types to JS types
  * Pass `true` as last param if JS will make async calls and return a `Promise`
  * `this` will be available in any context that `this` would be available to the Java method. i.e. `this` will be `null` for `static` methods.
  * Context is last param to every function registered using `Native.create` or `Override.create`
  * Parameter types are specified in [JNI](http://www.iastate.edu/~java/docs/guide/nativemethod/types.doc.html)
