/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

var mobileInfo = (function() {
  // The info is constant, so we only have to retrieve it once, after which
  // we can cache it and return the cached value.
  var info;

  return {
    get: function(callback) {
      // Call the callback after a timeout so it's always called asynchronously
      // regardless of whether or not the info is cached.
      if (info) {
        window.setZeroTimeout(function() {
          callback(info);
        });
        return;
      }
      DumbPipe.open("mobileInfo", {}, function(message) {
        info = message;
        callback(info);
      });
    }
  };
})();
