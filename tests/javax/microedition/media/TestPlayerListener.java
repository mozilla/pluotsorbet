package javax.microedition.media;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;
import java.io.InputStream;

public class TestPlayerListener implements Testlet, PlayerListener {
    public int getExpectedPass() { return 2; }
    public int getExpectedFail() { return 0; }
    public int getExpectedKnownFail() { return 0; }
    boolean endOfMedia = false;
    boolean started = false;

    public void playerUpdate(Player player, String event, Object eventData) {
        if (event.equals("started")) {
            started = true;
        }

        if (event.equals("endOfMedia")) {
            endOfMedia = true;

            synchronized (this) {
                this.notify();
            }
        }
    }

    public void test(TestHarness th) {
        try {
            InputStream is = getClass().getResourceAsStream("/midlets/MediaSampler/res/laser.wav");
            Player player = Manager.createPlayer(is, "audio/x-wav");
            player.addPlayerListener(this);
            player.realize();
            player.prefetch();
            player.start();

            synchronized (this) {
                while (!endOfMedia) {
                    this.wait();
                }
            }

            th.check(started);
            th.check(endOfMedia);
        } catch (Exception e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
        }
    }
}

