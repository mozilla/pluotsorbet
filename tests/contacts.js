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

contacts.forEach = fakeContacts.forEach.bind(fakeContacts);
