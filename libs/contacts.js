/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

var contacts = (function() {
  function forEach(callback) {
    var sender = DumbPipe.open("contacts", {}, function(message) {
      if (message) {
        callback(message);
      } else {
        DumbPipe.close(sender);
      }
    });
  }

  return {
    forEach: forEach,
  };
})();
