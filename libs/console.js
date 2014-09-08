/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

(function() {
  var windowConsole = window.console;

  /**
   * The console(s) to which messages should be output.  A comma-separated list
   * of one or more of these targets:
   *    web: the browser's Web Console (default)
   *    page: the in-page console (an HTML element with ID "console")
   *    native: the native console (via the *dump* function)
   */
  var targets = urlParams.logTarget ? urlParams.logTarget.split(",") : ["web"];

  // If we're only targeting the web console, then we use the original console
  // object, so file/line number references show up correctly in it.
  if (targets.every(function(v) { return v == "web" })) {
    return;
  }

  var levels = {
    trace: 0,
    log: 1,
    info: 2,
    warn: 3,
    error: 4,
    silent: 5,
  };

  var logLevel = urlParams.logLevel || "log";

  var log = function(messageLevel) {
    if (levels[messageLevel] < levels[logLevel]) {
      return;
    };

    if (targets.indexOf("web") != -1) {
      windowConsole[messageLevel].apply(windowConsole, Array.slice(arguments, 1));
    }

    var tag = messageLevel[0].toUpperCase();

    var message = tag + " ";
    if (messageLevel == "trace") {
      var stack = new Error().stack;
      // Strip the first frame, which is this log function itself.
      message += stack.substring(stack.indexOf("\n") + 1);
    } else {
      message += Array.slice(arguments, 1).join(" ");
    }
    message += "\n";

    if (targets.indexOf("page") != -1) {
      document.getElementById("console").textContent += message;
    }

    if (targets.indexOf("native") != -1) {
      dump(message);
    }
  };

  window.console = {
    trace: log.bind(null, "trace"),
    log: log.bind(null, "log"),
    info: log.bind(null, "info"),
    warn: log.bind(null, "warn"),
    error: log.bind(null, "error"),
  };
})();
