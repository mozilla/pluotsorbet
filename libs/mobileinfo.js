'use strict';

// Use only the info about the first SIM for the time being
var mobileInfo = (function() {
  var networkID = "001 0001";
  var mcc = "310"; // United States
  var mnc = "001";

  var mobileConnections = window.navigator.mozMobileConnections;
  if (!mobileConnections && window.navigator.mozMobileConnection) {
    mobileConnections = [ window.navigator.mozMobileConnection ];
  }

  if (mobileConnections) {
    var networkInfo = mobileConnections[0].voice.network;
    mcc = networkInfo.mcc;
    mnc = networkInfo.mnc;
    networkID = mnc + " (" + networkInfo.shortName + ")";
  }

  return {
    networkID: networkID,
    mcc: mcc,
    mnc: mnc,
  };
})();
