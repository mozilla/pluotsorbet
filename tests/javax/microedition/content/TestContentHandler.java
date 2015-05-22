package javax.microedition.content;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

public class TestContentHandler implements Testlet {
    public int getExpectedPass() { return 5; }
    public int getExpectedFail() { return 0; }
    public int getExpectedKnownFail() { return 0; }

    native static void addInvocation(String argument, String action);

    public void test(TestHarness th) {
        try {
            Registry.getRegistry("RunTests")
                    .register("RunTests",
                              new String[] { "image/jpeg", "image/png", "image/gif", "audio/amr", "audio/mp3", "video/3gpp", "video/mp4" },
                              null,
                              new String[] { "share" },
                              null,
                              null,
                              null);

            ContentHandlerServer chServer = Registry.getServer("RunTests");

            Invocation invoc = chServer.getRequest(false);
            th.check(invoc, null, "Invocation is null");

            addInvocation("argument", "action");

            invoc = chServer.getRequest(false);
            th.check(invoc != null, "Invocation isn't null");
            th.check(invoc.getAction(), "action");
            th.check(invoc.getArgs().length, 1, "1 argument");
            th.check(invoc.getArgs()[0], "argument", "Argument value is 'argument'");

            invoc.setArgs(null);
            chServer.finish(invoc, Invocation.INITIATED);
        } catch (Exception e) {
            th.fail("Unexpected exception: " + e);
        }
    }
};
