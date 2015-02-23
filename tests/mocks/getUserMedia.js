navigator.mozGetUserMedia = function(constraints, callback, errorCallback) {
  load("tests/javax/microedition/media/test.webm", "blob").then(function(blob) {
    callback(blob);
  });
};
