/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

window.addEventListener("message", function(event) {
  switch (event.data) {
    case "contacts-getAll":
      getAllContacts();
      break;
    case "mobileInfo-get":
      getMobileInfo();
      break;
  }
}, false);

function getAllContacts() {
  var req = navigator.mozContacts.getAll();

  req.onsuccess = function() {
    var contact = req.result;
    document.getElementById("wrappee").contentWindow.postMessage({
      name: "contact",
      contact: contact,
    }, "*");
    if (contact) {
      req.continue();
    }
  }

  req.onerror = function() {
    console.error("Error while reading contacts");
  }
}

function getMobileInfo() {
  // Initialize the object with the URL params and fallback placeholders
  // for testing/debugging on a desktop.
  var mobileInfo = {
    network: {
      mcc: urlParams.network_mcc || "310", // United States
      mnc: urlParams.network_mnc || "001",
    },
    icc: {
      mcc: urlParams.icc_mcc || "310", // United States
      mnc: urlParams.icc_mnc || "001",
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
    // Then the only part of the Mobile Connection API that is accessible
    // to privileged apps is lastKnownNetwork and lastKnownHomeNetwork, which
    // is fortunately all we need.  lastKnownNetwork is a string of format
    // "<mcc>-<mnc>", while lastKnownHomeNetwork is "<mcc>-<mnc>[-<spn>]".
  // Use only the info about the first SIM for the time being.
    var lastKnownNetwork = mobileConnections[0].lastKnownNetwork.split("-");
    mobileInfo.network.mcc = lastKnownNetwork[0];
    mobileInfo.network.mnc = lastKnownNetwork[1];

    var lastKnownHomeNetwork = mobileConnections[0].lastKnownHomeNetwork.split("-");
    mobileInfo.icc.mcc = lastKnownHomeNetwork[0];
    mobileInfo.icc.mnc = lastKnownHomeNetwork[1];
  }

  document.getElementById("wrappee").contentWindow.postMessage({
    name: "mobileInfo",
    info: mobileInfo,
  }, "*");
}

function loadScript(path) {
  return new Promise(function(resolve, reject) {
    var element = document.createElement('script');
    element.setAttribute("type", "text/javascript");
    element.setAttribute("src", path);
    document.getElementsByTagName("head")[0].appendChild(element);
    element.onload = resolve;
  });
}

var midletClassName = urlParams.midletClassName ? urlParams.midletClassName.replace(/\//g, '.') : "RunTests";
var loadingPromises = [];
if (midletClassName == "RunTests") {
  loadingPromises.push(loadScript("tests/contacts.js"));
}

Promise.all(loadingPromises).then(function() {
  document.getElementById("wrappee").src = "index.html" + location.search;
});
