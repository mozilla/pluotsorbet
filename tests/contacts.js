/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

var fakeContacts = [{
  id: 1,
  name: ["Test Contact 1"],
  familyName: ["Contact 1"],
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
},
{
  id: 2,
  name: ["Test Contact 2"],
  familyName: ["Contact 2"],
  tel: [{
    type: ["home"],
    pref: true,
    value: "+16505550102",
    carrier: "C3",
  },
  {
    type: ["work"],
    pref: false,
    value: "+16505550103",
    carrier: "C4",
  },],
},];

// Override the regular "contacts" registration with our fake one.
DumbPipe.registerOpener("contacts", function(message, sender) {
  fakeContacts.forEach(function(contact) {
    window.setTimeout(function() {
      sender(contact);
    }, 0);
  });
  window.setTimeout(function() {
    sender(null);
  }, 0);
});
