/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

var contacts = (function() {
  function forEach(callback) {
    window.parent.postMessage("contacts-getAll", "*");
    window.addEventListener("message", function contactListener(event) {
      if (event.data && event.data.name && event.data.name == "contact") {
        if (event.data.contact) {
          callback(event.data.contact);
        } else {
          window.removeEventListener("message", contactListener, false);
        }
      }
    }, false);
  }

  return {
    forEach: forEach,
  };
})();
