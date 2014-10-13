/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

var fakeContacts = [{
  id: 1,
  name: ["Test Contact 1"],
  tel: [{
    type: ["home"],
    pref: true,
    value: "+16505550100",
    carrier: "C1",
  },
  {
    type: ["work"],
    pref: false,
    value: "+16505550101",
    carrier: "C2",
  },],
}];

function getAllContacts(message, pipe) {
  fakeContacts.forEach(function(contact) {
    window.setTimeout(function() {
      pipe(contact);
    }, 0);
  });
  window.setTimeout(function() {
    pipe(null);
  }, 0);
}
