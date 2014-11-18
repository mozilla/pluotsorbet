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

  function getAll(callback) {
    var contacts = [];
    var sender = DumbPipe.open("contacts", {}, function(contact) {
      if (!contact) {
        callback(contacts);
        DumbPipe.close(sender);
        return;
      }

      contacts.push(contact);
    });
  }

  var requestHandler = null;
  function getNext(callback) {
    if (requestHandler) {
      callback(requestHandler());
      return;
    }

    getAll(function(contacts) {
      var idx = -1;

      requestHandler = function() {
        idx++;

        if (idx < contacts.length) {
          return contacts[idx];
        }

        requestHandler = null;

        return null;
      }

      callback(requestHandler());
    });
  }

  return {
    forEach: forEach,
    getNext: getNext,
  };
})();
