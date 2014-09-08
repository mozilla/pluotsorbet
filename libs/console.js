/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

(function() {
  var windowConsole = window.console;

  /**
   * The console(s) to target.  A comma-separated list of one or more of these
   * possible values:
   *    web:    browser's Web Console (default)
   *    page:   in-page console
   *    native: native console
   */
  var targets = urlParams.logTarget ? urlParams.logTarget.split(",") : ["web"];

  var log = function(level) {
    if (targets.indexOf("web") != -1) {
      windowConsole[level].apply(windowConsole, Array.slice(arguments, 1));
    }

    var tag = level.toUpperCase();
    var message = [tag].concat(Array.slice(arguments, 1)).join(" ") + "\n";

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
