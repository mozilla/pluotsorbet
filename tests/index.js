DumbPipe.registerOpener("echo", function(message, sender) {
  sender(message);
});
