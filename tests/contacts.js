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

// Turn the contact object into a mozContact to make the testing
// environment more similar to reality.
function toMozContact(fakeContact) {
  var contact = new mozContact();

  for (var attr in fakeContact) {
    if (fakeContact.hasOwnProperty(attr)) {
      contact[attr] = fakeContact[attr];
    }
  }

  return contact;
}

navigator.mozContacts = {
  getAll: function() {
    var req = {};

    var contacts = fakeContacts.slice(0);

    req.continue = function() {
      setZeroTimeout(function() {
        req.result = (contacts.length > 0) ? toMozContact(contacts.shift()) : null;
        req.onsuccess();
      });
    };

    req.continue();

    return req;
  }
};
