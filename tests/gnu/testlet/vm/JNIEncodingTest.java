package gnu.testlet.vm;

import gnu.testlet.*;

import javax.microedition.lcdui.Font;

public class JNIEncodingTest implements Testlet {
    public void test(TestHarness th) {
        Font font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_LARGE);
        String s = "marco";
        th.check(s.substring(0, 0), "");
        th.check(font.stringWidth(s.substring(0, 0)), font.stringWidth(""));
        th.check(font.stringWidth(s.substring(0, 1)), font.stringWidth("m"));
    }
}
