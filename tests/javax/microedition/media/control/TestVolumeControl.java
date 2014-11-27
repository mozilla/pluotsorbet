package javax.microedition.media.control;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;
import java.io.*;
import javax.microedition.media.*;

public class TestVolumeControl implements Testlet, PlayerListener {
    TestHarness th;

     /**
     * PlayerListener interface's method.
     */
    public void playerUpdate(Player player, String event, Object eventData) {
        System.out.println("playerUpdate event: " + event + " " + eventData);
    }

    public void test(TestHarness th) {
        this.th = th;

        try {
            // Create audio player and start playing.
            InputStream is = getClass().getResourceAsStream("/midlets/MediaSampler/res/laser.wav");
            Player player = Manager.createPlayer(is, "audio/x-wav");
            player.addPlayerListener(this);
            player.realize();
            player.prefetch();
            player.start();

            // Get associated volume control from the audio player.
            VolumeControl control = (VolumeControl)player.getControl("VolumeControl");

            // Change volume level.
            th.check(control.setLevel(10), 10);
            Thread.sleep(100);
            th.check(control.getLevel(), 10);
            th.check(control.isMuted(), false);

            // Ensure the volume level is between 0 and 100.
            th.check(control.setLevel(-1), 0);
            th.check(control.getLevel(), 0);
            th.check(control.setLevel(120), 100);
            th.check(control.getLevel(), 100);

            // Mute
            control.setMute(true);
            Thread.sleep(100);
            th.check(control.getLevel(), 100);
            th.check(control.isMuted(), true);

            // Unmute
            control.setMute(false);
            th.check(control.isMuted(), false);
            th.check(control.getLevel(), 100);
        } catch (Exception e) {
            e.printStackTrace();
            th.fail("Unexpected exception: " + e);
        }
    }
}

