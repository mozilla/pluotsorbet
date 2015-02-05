package javax.microedition.media;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;
import gnu.testlet.TestUtils;
import javax.microedition.io.*;
import javax.microedition.io.file.*;
import java.io.*;

public class TestAudioPlayer implements Testlet, PlayerListener {
    public int getExpectedPass() { return 21; }
    public int getExpectedFail() { return 0; }
    public int getExpectedKnownFail() { return 0; }
    TestHarness th;

    private static final long TIME_TOLERANCE = 100;

     /**
     * PlayerListener interface's method.
     */
    public void playerUpdate(Player player, String event, Object eventData) {
        System.out.println("playerUpdate event: " + event + " " + eventData);
        if (event.equals(PlayerListener.END_OF_MEDIA)) {
            synchronized (this) {
                this.notify();
            }
        }
    }

    public void test(TestHarness th) {
        this.th = th;

        // Test player with input stream.
        try {
            InputStream is = getClass().getResourceAsStream("/javax/microedition/media/hello.wav");
            Player player = Manager.createPlayer(is, "audio/x-wav");
            testPlay(player, "audio/x-wav");
        } catch (Exception e) {
            e.printStackTrace();
            th.fail("Unexpected exception: " + e);
        }

        // Test player with file URL.
        try {
            String url = "file:////hello.wav";
            FileConnection file = (FileConnection)Connector.open(url, Connector.READ_WRITE);
            if (!file.exists()) {
                file.create();
            }
            OutputStream os = file.openDataOutputStream();
            InputStream is = getClass().getResourceAsStream("/javax/microedition/media/hello.wav");
            os.write(TestUtils.read(is));
            os.close();

            Player player = Manager.createPlayer(url);
            testPlay(player, "audio/x-wav");

            file.delete();
            file.close();
        } catch (Exception e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
        }

        // Test player with file URL with a odd size
        try {
            String url = "file:////hello.ogg";
            FileConnection file = (FileConnection)Connector.open(url, Connector.READ_WRITE);
            if (!file.exists()) {
                file.create();
            }
            OutputStream os = file.openDataOutputStream();
            InputStream is = getClass().getResourceAsStream("/javax/microedition/media/hello.ogg");
            os.write(TestUtils.read(is));
            os.close();

            Player player = Manager.createPlayer(url);
            testPlay(player, "audio/ogg");

            file.delete();
            file.close();
        } catch (Exception e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
        }
    }

    private void testPlay(Player player, String expectedContentType) throws Exception {
        player.addPlayerListener(this);

        // Check duration
        th.check(player.getDuration(), Player.TIME_UNKNOWN);

        // Start playing.
        player.realize();
        player.prefetch();
        player.start();

        // Check content type.
        th.check(player.getContentType(), expectedContentType);

        // Play the audio for a short time.
        while (player.getMediaTime() <= 0) {
            Thread.sleep(10);
        }

        // Sleep 500 milliseconds and check if the change in media time
        // is around the time interval slept. We calculate the actual time
        // slept because it could be much different from the amount we
        // intend to sleep (if another thread hogs the CPU in the meantime).
        long currentTimeBeforeSleep = System.currentTimeMillis();
        long mediaTimeBeforeSleep = player.getMediaTime() / 1000;
        Thread.sleep(500);
        long actualTimeSlept = System.currentTimeMillis() - currentTimeBeforeSleep;
        long mediaTime = (player.getMediaTime() / 1000) - mediaTimeBeforeSleep;
        th.check(Math.abs(mediaTime - actualTimeSlept) < TIME_TOLERANCE,
                 "Math.abs(" + mediaTime + " - " + actualTimeSlept + ") < " + TIME_TOLERANCE);

        // Pause
        player.stop();
        mediaTime = player.getMediaTime() / 1000;
        Thread.sleep(200);
        th.check(player.getMediaTime() / 1000, mediaTime);

        // Resume
        player.start();

        // Check duration
        th.check(player.getDuration(), 4735000);

        // Wait for media ends.
        synchronized (this) {
            // When the media reaches the end, the state should be changed from
            // STARTED to PREFETCHED.
            while (player.getState() != Player.PREFETCHED) {
                this.wait();
            }
        }
        th.check(player.getState(), Player.PREFETCHED);

        // When audio reaches the end, the media time should be equal to the
        // duration.
        th.check(player.getMediaTime() == player.getDuration());

        // Replay the audio
        player.start();
        Thread.sleep(50);

        player.close();
    }
}

