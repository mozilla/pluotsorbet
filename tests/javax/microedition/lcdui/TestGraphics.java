package javax.microedition.lcdui;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

public class TestGraphics implements Testlet {
    public int getExpectedPass() { return 5; }
    public int getExpectedFail() { return 0; }
    public int getExpectedKnownFail() { return 0; }

    public void test(TestHarness th) {
      Image image = Image.createImage(240, 320);
      Graphics g = image.getGraphics();

      short width = g.getMaxWidth();
      th.check(width, 240);
      short height = g.getMaxHeight();
      th.check(height, 320);

      Font font = g.getFont();
      th.check(font != null, "initial call to getFont() returns Font");
      g.setFont(font);
      Font font2 = g.getFont();
      th.check(font2 != null, "subsequent call to getFont() returns Font");
      th.check(font2 == font, "getFont() returns Font set by setFont()");
    }
}
