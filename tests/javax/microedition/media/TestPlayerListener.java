package javax.microedition.media;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;
import java.io.InputStream;

class aPlayerListener implements PlayerListener, Runnable {
    public boolean endOfMedia = false;
    public boolean started = false;

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

    public void run() {
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
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }
}

public class TestPlayerListener implements Testlet {
    public void test(TestHarness th) {
        try {
            aPlayerListener playerListener = new aPlayerListener();
            Thread t = new Thread(playerListener);
            t.start();
            t.join();
            th.check(playerListener.started);
            th.check(playerListener.endOfMedia);
        } catch (InterruptedException e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
        }
    }
}

