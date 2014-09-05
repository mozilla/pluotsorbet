/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

// Use only the info about the first SIM for the time being
var mobileInfo = (function() {
  var pad = function(num, len) {
    return "0".repeat(len - num.toString().length) + num;
  };

  // Initialize the object with the URL params and fallback placeholders.
  var mobileInfo = {
    network: {
      mcc: pad(urlParams.network_mcc, 3) || "310", // United States
      mnc: pad(urlParams.network_mnc, 3) || "001",
    },
    icc: {
      mcc: pad(urlParams.icc_mcc, 3) || "310", // United States
      mnc: pad(urlParams.icc_mnc, 3) || "001",
      msisdn: urlParams.icc_msisdn || "10005551212",
    },
  };

  var mobileConnections = window.navigator.mozMobileConnections;
  if (!mobileConnections && window.navigator.mozMobileConnection) {
    mobileConnections = [ window.navigator.mozMobileConnection ];
  }

  // If we have access to the Mobile Connection API, then we use it to get
  // the actual values.
  if (mobileConnections) {
    var networkInfo = mobileConnections[0].voice.network;

    mobileInfo.network.mcc = pad(networkInfo.mcc, 3);
    mobileInfo.network.mnc = pad(networkInfo.mnc, 3);

    // XXX If we're a certified app, then get the ICC (i.e. SIM) values too
    // from mobileConnections[0].iccInfo.
  }

  return mobileInfo;
})();
