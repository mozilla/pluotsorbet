/* -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */

"use strict";

var initFS = new Promise(function(resolve, reject) {
  fs.init(resolve);
}).then(function() {
  var fsPromises = [
    new Promise(function(resolve, reject) {
      fs.mkdir("/Persistent", resolve);
    }),

    new Promise(function(resolve, reject) {
      fs.exists("/_main.ks", function(exists) {
        if (exists) {
          resolve();
        } else {
          load("certs/_main.ks", "blob").then(function(data) {
            fs.create("/_main.ks", data, function() {
              resolve();
            });
          });
        }
      });
    }),
  ];

  if (MIDP && MIDP.midletClassName == "RunTests") {
    fsPromises.push(
      new Promise(function(resolve, reject) {
        fs.exists("/_test.ks", function(exists) {
          if (exists) {
            resolve();
          } else {
            load("certs/_test.ks", "blob").then(function(data) {
              fs.create("/_test.ks", data, function() {
                resolve();
              });
            });
          }
        });
      })
    );
  }

  return Promise.all(fsPromises);
});
