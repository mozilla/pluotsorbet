/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

var contacts = (function() {
  navigator.mozContacts.oncontactchange = function(event) {
    if (event.reason == "create" || event.reason == "update") {
      var contactID = event.contactID;

      var req = navigator.mozContacts.find({
        filterBy: ["id"],
        filterOp: "equals",
        filterValue: contactID
      });

      req.onsuccess = function () {
        var contact = req.result[0];
        // Notify listeners
      }

      req.onerror = function() {
        console.error("Error while reading contact");
      }
    } else if (event.reason == "remove") {
      delete contacts[event.contactID];
    }
  }

  function getAll(callback) {
    var contacts = [];

    var req = navigator.mozContacts.getAll();

    req.onsuccess = function(event) {
      var contact = req.result;
      if (contact) {
        contacts.push(contact);
        req.continue();
      } else {
        callback(contacts);
      }
    }

    req.onerror = function() {
      console.error("Error while reading contacts");
    }
  }

  return {
    getAll: getAll,
  };
})();
