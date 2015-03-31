package javax.microedition.lcdui;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

public class TestGraphics implements Testlet {
    public int getExpectedPass() { return 2; }
    public int getExpectedFail() { return 0; }
    public int getExpectedKnownFail() { return 0; }

    public void test(TestHarness th) {
      Image image = Image.createImage(240, 320);
      Graphics g = image.getGraphics();

      short width = g.getMaxWidth();
      th.check(width, 240);
      short height = g.getMaxHeight();
      th.check(height, 320);
    }
}
