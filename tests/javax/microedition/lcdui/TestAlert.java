/* vim: set filetype=java shiftwidth=4 tabstop=4 autoindent cindent expandtab : */

package javax.microedition.lcdui;

import com.nokia.mid.ui.TextEditor;
import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;

public class TestAlert extends Canvas implements Testlet {
    private native boolean isTextEditorReallyFocused();

    public void test(TestHarness th) {
        Alert alert = new Alert("Hello World", "Some text", null, AlertType.INFO);
        alert.setTimeout(Alert.FOREVER);
        TextEditor textEditor = TextEditor.createTextEditor("Hello, world!", 20, 0, 100, 24);
        textEditor.setParent(this);

        th.setScreenAndWait(this);

        textEditor.setFocus(true);
        th.check(textEditor.hasFocus(), "TextEditor gained focus");
        th.check(isTextEditorReallyFocused(), "TextEditor really gained focus");

        th.setScreenAndWait(alert);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            th.fail("Unexpected exception: " + e);
        }
        th.check(textEditor.hasFocus(), "TextEditor kept focus");
        th.check(!isTextEditorReallyFocused(), "TextEditor really lost focus");

        th.setScreenAndWait(this);
        th.check(textEditor.hasFocus(), "TextEditor maintained focus");
        th.check(isTextEditorReallyFocused(), "TextEditor really regained focus");

        th.setScreenAndWait(alert);
        th.check(textEditor.hasFocus(), "TextEditor still has focus");
        th.check(!isTextEditorReallyFocused(), "TextEditor really lost focus");
    }

    protected void paint(Graphics graphics) {}
}
