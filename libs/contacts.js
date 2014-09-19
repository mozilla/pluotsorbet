/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

var contacts = (function() {
  var contacts = [{
    id: 1,
    name: ["Test Contact 1"],
    tel: [{
      type: "home",
      pref: true,
      value: "+11111111111",
      carrier: "C1",
    },
    {
      type: "work",
      pref: false,
      value: "+12222222222",
      carrier: "C2",
    },],
  }];

  function forEach(callback) {
    for (var i = 0; i < contacts.length; i++) {
        callback(contacts[i]);
    }
  }

  return {
    forEach: forEach,
  };
})();
