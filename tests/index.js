DumbPipe.registerOpener("echo", function(message, sender) {
  sender(message);
});

var oldHeight = document.getElementById("mozbrowser").style.height;
DumbPipe.registerOpener("showKeyboard", function(message, sender) {
  document.getElementById("mozbrowser").style.height = "50%";
});

DumbPipe.registerOpener("hideKeyboard", function(message, sender) {
  document.getElementById("mozbrowser").style.height = oldHeight;
});
