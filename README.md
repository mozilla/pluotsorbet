# j2me.js [![Build Status](https://travis-ci.org/mozilla/j2me.js.svg)](https://travis-ci.org/mozilla/j2me.js)

j2me.js is a J2ME virtual machine in JavaScript.

The current goals of j2me.js are:

1. Run MIDlets in a way that emulates the reference implementation of phone ME Feature MR4 (b01)
1. Keep j2me.js simple and small: Leverage the phoneME JDK/infrastructure and existing Java code as much as we can, and implement as little as possible in JavaScript

## Building j2me.js

Make sure you have a [JRE](http://www.oracle.com/technetwork/java/javase/downloads/jre7-downloads-1880261.html) installed

You need to install the TypeScript compiler, the easiest way is via NPM: `npm install -g typescript`.

Get the [j2me.js repo](https://github.com/mozilla/j2me.js) if you don't have it already

        git clone https://github.com/mozilla/j2me.js

Build using make:

        cd j2me.js
        make

## Running apps & MIDlets, Debugging

index.html is a webapp that runs j2me.js. The URL parameters you pass to index.html control the specific behavior of j2me.js.

### URL parameters

You can specify URL parameters to override the configuration. See the full list of parameters at config/urlparams.js.

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

You can run the test suite with `make test`. The main driver for the test suite is tests/automation.js which uses the [CasperJS](http://casperjs.org/) testing framework and [SlimerJS](http://slimerjs.org/) (a Gecko backend for CasperJS). This test suite runs on every push (continuous integration) thanks to [Travis CI](https://travis-ci.org/).

`make test` downloads SlimerJS for you automatically, but you have to install CasperJS yourself. The easiest way to do that is via NPM: `npm install -g casperjs`.  On Mac, you may also be able to install it via Brew.

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

### VM profiling

The j2me.js VM has several profiling tools. The simplest feature is to use counters. `runtime.ts` defines several: `runtimeCounter`, `nativeCounter`, etc ... these are only available in debug builds.

To use them, just add calls to `runtimeCounter.count(name, count = 1)`. To view accumulated counts, allow the application to run for some time and then click the `Dump Counters` button. If you want, reset the counter count any time by clicking `Clear Counters`.

- Counting events:
  ```
  function readBytes(fileName, length) {
    runtimeCounter && runtimeCounter.count("readBytes");
  }
  ```

- Counting bucketed events:
  ```
  function readBytes(fileName, length) {
    runtimeCounter && runtimeCounter.count("readBytes " + fileName);
  }
  ```

- Counting events with larger counts: 
  ```
  function readBytes(fileName, length) {
    runtimeCounter && runtimeCounter.count("readBytes", length);
  }
  ```

- Counting events with caller context: This is useful to understand which call sites are the most common. 
  ```
  function readBytes(fileName, length) {
    runtimeCounter && runtimeCounter.count("readBytes " + arguments.callee.caller.name);
  }
  ```

The second, more heavy weight profiling tool is Shumway's timeline profiler. The profiler records `enter` / `leave` events in a large circular buffer that can be later displayed visually as a flame chart or saved in a text format. To use it, build j2me.js with `PROFILE=[1|2]`.

Next, you will need to wrap code regions that you're interested in measuring with calls to `timeline.enter` / `timeline.leave`.

If you want to record every Java method call, change the line in `runtime.ts` from:

```
if (false && methodTimeline) {
```
to
```
if (methodTimeline) {
```

This will wrap all methods with calls to `methodTimeline.enter` /  `methodTimeline.leave`. The resulting timeline is a very detailed trace of the application's execution. Note that this instrumentation has some overhead, and timing information of very short lived events may not be accurate and can lead to the entire application slowing down.

Similar to the way counters work, you can get creative with the timeline profiler. The API looks something like this:

```
timeline.enter(name: string, details?: Object);
timeline.leave(name?: string, details?: Object);
```

You must pair the calls to `enter` and `leave` but you don't necessarily need to specify arguments for `name` and `details`.

The `name` argument can be any string and it specifies a event type. The timeline view will draw different types of events in different colors. It will also give you some statistics about the number of times a certain event type was seen, how long it took, etc.. 

The `details` argument is an object whose properties are shown when you hover over a timeline segment in the profiler view. You can specify this object when you call `timeline.enter` or when you call `timeline.leave`. Usually, you have more information when you call `leave` so that's a more convenient place to put it.

The way in which you come up with event names can produce different results. In the `profilingWrapper` function, the `key` is used to specify the event type.

You can also create your own timelines. At the moment there are 3:
- `timeline`: VM Events like loading class files, linking, etc.
- `methodTimeline`: Method execution.
- `threadTimeline`: Thread scheduling.

You may have to change the CSS height style of the `profileContainer` if you don't see all timelines.

![Shumway's timeline viewer](https://cloud.githubusercontent.com/assets/311082/5998278/644761ec-aa7a-11e4-8149-3556b08b8c54.png)

Top band is an overview of all the timelines. Second band is the `timeline`, third is the `threadTimeline` and finally the fourth is the `methodTimeline`. Use your mouse wheel to zoom in and out, pan and hover.

The tooltip displays:
- `total`: ms spent in this event including all the child events.
- `self`: `total` - `total` sum of all child events.
- `count`: number of events seen with this name.
- `all total` and `all self`: cumulative total and self times for all events with this name.
- the remaining fields show the custom data specified in the `details` object.

If you build with `PROFILE=2` the timeline will be saved to a text file instead of shown in the flame chart. On desktop, you will be prompted to save the file. On the phone, the file will automatically be saved to `/sdcard/downloads/profile.txt` which you can later pull with `adb pull`. Note that no timeline events under 0.1 ms are written to the file output. You can change this in `main.js` if you'd like.

## Benchmarks

### Startup Benchmark

The startup benchmark measures from when the benchmark.js file loads to the call of `DisplayDevice.gainedForeground0`. It also measures memory usage after startup. Included in a benchmark build are helpers to build baseline scores so that subsequent runs of the benchmark can be compared. A t-test is used in the comparison to see if the changes were significant.

To use:

*It is recommended that a dedicated Firefox profile is used with the about:config preference of `security.turn_off_all_security_so_that_viruses_can_take_over_this_computer` set to true so garbage collection and cycle collection can be run in between test rounds. To do this on a Firefox OS device, see [B2G/QA/Tips And Tricks](https://wiki.mozilla.org/B2G/QA/Tips_And_Tricks#For_changing_the_preference:).*

1. Check out the version you want to be the baseline (usually mozilla/master).
1. Build a benchmark build with `RELEASE=1 BENCHMARK=1 make`. *`RELEASE=1` is not required, but it is recommended to avoid debug code from changing execution behavior.*
1. Open the midlet you want to test with `&logLevel=log` appended to the url and click `Build Benchmark Baseline`.
1. When finished, the message `FINISHED BUILDING BASELINE` will show up in the log.
1. Apply/check out your changes to the code.
1. Rebuild `RELEASE=1 BENCHMARK=1 make`.
1. Refresh the midlet.
1. Click `Run Startup Benchmark`.
1. Once done, the benchmark will dump results to the log. If it says "BETTER" or "WORSE" the t-test has determined the results were significant. If it says "SAME" the changes were likely not enough to be differentiated from the noise of the test.

## Filesystem

midp/fs.js contains native implementations of various midp filesystem APIs.

Those implementations call out to lib/fs.js which is a JS implementation of a filesystem.

Java APIs are sync, so our implementation stores files in memory and makes them available mostly synchronously.

## Implementing Java functions in native code

`native` keyword tells Java that the function is implemented in native code

e.g.:

    public static native long currentTimeMillis();

Java compiler will do nothing to ensure that implementation actually exists. At runtime, implementation better be available or you'll get a runtime exception.

We use `Native` object in JS to handle creation and registration of `native` functions. See native.js

    Native["name/of/function.(parameterTypes)returnType"] = jsFuncToCall;

e.g.:

    Native["java/lang/System.arraycopy.(Ljava/lang/Object;ILjava/lang/Object;II)V" = function(src, srcOffset, dst, dstOffset, length) {...};

If raising a Java `Exception`, throw new instance of Java `Exception` class as defined in vm/runtime.ts, e.g.:

    throw $.newNullPointerException("Cannot copy to/from a null array.");

If you need implement a native method with async JS calls, the following steps are required:

1. Add the method to the `yieldMap` in jit/analyze.ts
2. Use `asyncImpl` in native.js to return the asnyc value with a `Promise`.

e.g:

    Native["java/lang/Thread.sleep.(J)V"] = function(delay) {
        asyncImpl("V", new Promise(function(resolve, reject) {
            window.setTimeout(resolve, delay.toNumber());
        }));
    };

The `asyncImpl` call is optional if part of the code doesn't make async calls. The method can sometimes return a value synchronously, and the VM will handle it properly. However, if a native ever calls asyncImpl, even if it doesn't always do so, then you need to add the method to `yieldMap`.

e.g:

    Native["java/lang/Thread.newSleep.(J)Z"] = function(delay) {
        if (delay < 0) {
          // Return false synchronously. Note: we use 1 and 0 in JavaScript to
          // represent true and false in Java.
          return 0;
        }
        // Return true asynchronously with `asyncImpl`.
        asyncImpl("Z", new Promise(function(resolve, reject) {
            window.setTimeout(resolve.bind(null, 1), delay.toNumber());
        }));
    };

Remember:

  * Return types are automatically converted to Java types, but parameters are not automatically converted from Java types to JS types
  * `this` will be available in any context that `this` would be available to the Java method. i.e. `this` will be `null` for `static` methods.
  * `$` is current runtime and `$.ctx` current Context
  * Parameter types are specified in [JNI](http://www.iastate.edu/~java/docs/guide/nativemethod/types.doc.html)

## Overriding Java functions with JavaScript functions

To override a Java function with a JavaScript function, simply define a Native as
described earlier. Any Java functions can be overridden, not only Java functions
with the `native` keyword.

Overriding Java functions only works in debug mode (RELEASE=0).

## Packaging

`make app` packages j2me.js into an Open Web App in output directory.
It's possible to simply package the entire contents of your working directory,
but these tools will produce a better app.

### Compiling With AOT Compiler

`make aot` compiles some Java code into JavaScript with an ahead-of-time (AOT) compiler.

To use it, first install a recent version of the
[JavaScript shell](https://developer.mozilla.org/en-US/docs/Mozilla/Projects/SpiderMonkey/Introduction_to_the_JavaScript_shell).

### Compiling With Closure

`make closure` compiles some JavaScript code with the Closure compiler.
