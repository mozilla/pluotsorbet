'use strict';

// Use only the info about the first SIM for the time being
var mobileInfo = (function() {
  var networkID = "01 (TEST)";
  var mnc = "01";

  var mobileConnections = window.navigator.mozMobileConnections;
  if (!mobileConnections && window.navigator.mozMobileConnection) {
    mobileConnections = [ window.navigator.mozMobileConnection ];
  }

  if (mobileConnections) {
    var networkInfo = mobileConnections[0].voice.network;
    mcn = networkInfo.mnc;
    networkID = mnc + " (" + networkInfo.shortName + ")";
  }

  return {
    networkID: networkID,
    mnc: mnc,
  };
})();