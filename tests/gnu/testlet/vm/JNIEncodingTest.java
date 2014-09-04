package gnu.testlet.vm;

import gnu.testlet.*;

import javax.microedition.lcdui.Font;

public class JNIEncodingTest implements Testlet {
	  public void test(TestHarness th) {
			  Font font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_LARGE);
			  String s = "microedition.encoding";
				System.out.println("PROP: " + font.stringWidth(s.substring(0, 0)));
	  }
}
