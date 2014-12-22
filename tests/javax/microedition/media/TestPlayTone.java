package javax.microedition.media;

import com.sun.mmedia.Configuration;
import com.sun.mmedia.TonePlayer;
import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;
import javax.microedition.media.control.ToneControl;

public class TestPlayTone implements Testlet {

    public void test(TestHarness th) {
        Configuration config = Configuration.getConfiguration();

        try {
          TonePlayer tonePlayer = config.getTonePlayer();
          tonePlayer.playTone(ToneControl.C4, 500, 100);
          Thread.sleep(200);
          tonePlayer.playTone(ToneControl.C4, 500, 100);
          Thread.sleep(100);
          tonePlayer.playTone(ToneControl.C4 + 2, 500, 100);
          Thread.sleep(100);
          tonePlayer.stopTone();
          th.check(true);
        } catch (Exception e) {
            e.printStackTrace();
            th.fail("Unexpected exception: " + e);
        }
    }
}

