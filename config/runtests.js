config.jars = "tests/tests.jar";
config.jad = "tests/runtests.jad";
config.midletClassName = "RunTestsMIDlet";

MIDlet.shouldStartBackgroundService = function() {
  return fs.exists("/startBackgroundService");
};
