/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

(function(window) {
  var windowConsole = window.console;
  window.console = {
    trace: windowConsole.trace,
    debug: windowConsole.debug,
    info: windowConsole.info,
    warn: windowConsole.warn,
    error: windowConsole.error,
  };
})(window);
