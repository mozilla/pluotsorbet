package gfx;

import javax.microedition.midlet.*;
import com.nokia.mid.ui.TextEditor;
import javax.microedition.lcdui.*;
import gnu.testlet.TestUtils;

public class TextEditorGfxTest extends MIDlet {
    class TestCanvas extends Canvas {
        protected void paint(Graphics g) {
            g.setColor(0x00FFFFFF);
            g.fillRect(0, 0, getWidth(), getHeight());

            System.out.println("PAINTED");
        }
    }

    public void startApp() {
        TestCanvas canvas = new TestCanvas();
        canvas.setFullScreenMode(true);
        Display.getDisplay(this).setCurrent(canvas);

        String emoji1 = TestUtils.getEmojiString("1f1ee1f1f9");
        String emoji2 = TestUtils.getEmojiString("1f609");
        String emoji3 = TestUtils.getEmojiString("2320e3");

        TextEditor textEditor = TextEditor.createTextEditor("A stri" + emoji1 + "ng wit" + emoji2 + "h emoj" + emoji3 + "i", 50, TextField.ANY, 150, 70);
        textEditor.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_LARGE));
        textEditor.setParent(canvas);
        textEditor.setMultiline(true);
        textEditor.setBackgroundColor(0x00FFFFFF);
        textEditor.setForegroundColor(0xFF000000);
        textEditor.setVisible(true);
        textEditor.setFocus(false);
        textEditor.setPosition(50, 50);
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }
};
