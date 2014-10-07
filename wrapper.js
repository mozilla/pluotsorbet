/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

'use strict';

window.addEventListener("message", function(event) {
  switch (event.data) {
    case "contacts-getAll":
      getAllContacts();
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
  document.getElementById("wrappee").src = "index.html";
});
