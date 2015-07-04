'use strict';

if (config.midletClassName !== "RunTestsMIDlet" && navigator && !navigator.mozContacts) {
  navigator.mozContacts = {
    getAll: function () {
      var req = {};
      nextTickBeforeEvents(function() {
        if (req.onsuccess) {
          req.result = null;
          req.onsuccess();
        }
      });
      return req;
    }
  }
}
