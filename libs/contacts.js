/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

var contacts = (function() {
  function forEach(callback) {
    var req = navigator.mozContacts.getAll();

    req.onsuccess = function() {
      var contact = req.result;
      if (contact) {
        callback(contact);
        req.continue();
      }
    }

    req.onerror = function() {
      console.error("Error while reading contacts");
    }
  }

  return {
    forEach: forEach,
  };
})();
