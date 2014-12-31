/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

"use strict";

function load(file, responseType) {
  return new Promise(function(resolve, reject) {
    var xhr = new XMLHttpRequest();
    xhr.open("GET", file, true);
    xhr.responseType = responseType;
    xhr.onload = function () {
      resolve(xhr.response);
    };
    xhr.onerror = function() {
      reject();
    };
    xhr.send(null);
  });
}

function loadWithProgress(file, responseType, successCb, failureCb, progressCb, length) {
  var xhr = new XMLHttpRequest({ mozSystem: true });
  xhr.open("GET", file, true);
  xhr.responseType = responseType;

  if (progressCb) {
    xhr.onprogress = function(e) {

      if (e.lengthComputable) {
        progressCb(e.loaded / e.total * 100);
      } else if (length) {
        progressCb(e.loaded / length * 100);
      }
    }
  }

  xhr.onload = function() {
    if (xhr.status === 200) {
      successCb(xhr.response);
    } else {
      failureCb();
    }
  };

  xhr.onerror = function(event) {
    failureCb();
  };

  xhr.send(null);
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
