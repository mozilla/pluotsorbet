package javax.microedition.media;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;
import javax.microedition.io.*;
import javax.microedition.io.file.*;
import java.io.*;

public class TestAudioPlayer implements Testlet, PlayerListener {
    TestHarness th;

    private static final long TIME_TOLERANCE = 50;

     /**
     * PlayerListener interface's method.
     */
    public void playerUpdate(Player player, String event, Object eventData) {
        System.out.println("playerUpdate event: " + event + " " + eventData);
    }

    public void test(TestHarness th) {
        this.th = th;

        // Test player with input stream.
        try {
            InputStream is = getClass().getResourceAsStream("/midlets/MediaSampler/res/laser.wav");
            Player player = Manager.createPlayer(is, "audio/x-wav");
            testPlay(player);
        } catch (Exception e) {
            e.printStackTrace();
            th.fail("Unexpected exception: " + e);
        }

        // Test player with file URL.
        try {
            String url = "file:///laser.wav";
            FileConnection file = (FileConnection)Connector.open(url, Connector.READ_WRITE);
            if (!file.exists()) {
                file.create();
            }
            OutputStream os = file.openDataOutputStream();
            InputStream is = getClass().getResourceAsStream("/midlets/MediaSampler/res/laser.wav");
            os.write(read(is));
            os.close();

            Player player = Manager.createPlayer(url);
            testPlay(player);

            file.delete();
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
            // Bug #651
            th.todo(false, "Unexpected exception: " + e);
        }
    }

    private byte[] read(InputStream is) throws IOException {
        int l = is.available();
        byte[] buffer = new byte[l+1];
        int length = 0;

        while ((l = is.read(buffer, length, buffer.length - length)) != -1) {
            length += l;
            if (length == buffer.length) {
                byte[] b = new byte[buffer.length + 4096];
                System.arraycopy(buffer, 0, b, 0, length);
                buffer = b;
            }
        }

        return buffer;
    }

    private void testPlay(Player player) throws Exception {
        player.addPlayerListener(this);

        // Check duration
        th.check(player.getDuration(), Player.TIME_UNKNOWN);

        // Start playing.
        player.realize();
        player.prefetch();
        player.start();

        // Check content type.
        th.check(player.getContentType(), "audio/x-wav");

        // Sleep 100 milliseconds and check if the change in media time
        // is around the time interval slept. We calculate the actual time
        // slept because it could be much different from the amount we
        // intend to sleep (if another thread hogs the CPU in the meantime).
        long currentTimeBeforeSleep = System.currentTimeMillis();
        long mediaTimeBeforeSleep = player.getMediaTime() / 1000;
        Thread.sleep(100);
        long actualTimeSlept = System.currentTimeMillis() - currentTimeBeforeSleep;
        long mediaTime = (player.getMediaTime() / 1000) - mediaTimeBeforeSleep;
        th.check(Math.abs(mediaTime - actualTimeSlept) < TIME_TOLERANCE);

        // Pause
        player.stop();
        mediaTime = player.getMediaTime() / 1000;
        Thread.sleep(200);
        th.check(player.getMediaTime() / 1000, mediaTime);

        // Resume
        player.start();
        currentTimeBeforeSleep = System.currentTimeMillis();
        mediaTimeBeforeSleep = player.getMediaTime() / 1000;
        Thread.sleep(100);
        actualTimeSlept = System.currentTimeMillis() - currentTimeBeforeSleep;
        mediaTime = (player.getMediaTime() / 1000) - mediaTimeBeforeSleep;
        th.check(Math.abs(mediaTime - actualTimeSlept) < TIME_TOLERANCE);

        // Check duration
        th.check(player.getDuration(), 500000);

        player.close();
    }
}

