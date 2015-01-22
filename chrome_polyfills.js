'use strict';

if (config.midletClassName !== "RunTests" && navigator && !navigator.mozContacts) {
  navigator.mozContacts = {
    getAll: function () {
      var req = {};
      setZeroTimeout(function() {
        if (req.onsuccess) {
          req.result = null;
          req.onsuccess();
        }
      });
      return req;
    }
  }
}
