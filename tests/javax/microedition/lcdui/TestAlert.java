/* vim: set filetype=java shiftwidth=4 tabstop=4 autoindent cindent expandtab : */

package javax.microedition.lcdui;

import com.nokia.mid.ui.TextEditor;
import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;

public class TestAlert extends Canvas implements Testlet {
    private native boolean isTextEditorReallyFocused();
    private TestHarness th;

    private void waitForScreen(Displayable screen) {
        try {
            do {
                Thread.sleep(100);
            } while (!screen.isShown());
        } catch (InterruptedException e) {
            th.fail("INTERRUPTED");
        }
    }

    public void test(TestHarness harness) {
        Alert alert = new Alert("Hello World", "Some text", null, AlertType.INFO);
        TextEditor textEditor = TextEditor.createTextEditor("Hello, world!", 20, 0, 100, 24);
        th = harness;
        Display display = th.getDisplay();
        textEditor.setParent(this);

        display.setCurrent(this);
        waitForScreen(this);

        textEditor.setFocus(true);
        th.check(textEditor.hasFocus(), "TextEditor gained focus");
        th.check(isTextEditorReallyFocused(), "TextEditor really gained focus");

        display.setCurrent(alert);
        waitForScreen(alert);
        int numDifferent = th.compareScreenToReferenceImage("gfx/AlertTest.png");
        th.check(numDifferent < 1621, "Screen must match");
        th.check(textEditor.hasFocus(), "TextEditor kept focus");
        th.check(!isTextEditorReallyFocused(), "TextEditor really lost focus");

        display.setCurrent(this);
        waitForScreen(this);
        textEditor.setFocus(true);
        th.check(textEditor.hasFocus(), "TextEditor maintained focus");
        th.check(isTextEditorReallyFocused(), "TextEditor really regained focus");

        display.setCurrent(alert);
        waitForScreen(alert);
        numDifferent = th.compareScreenToReferenceImage("gfx/AlertTest.png");
        th.check(numDifferent < 1621, "Screen must match");
        th.check(textEditor.hasFocus(), "TextEditor still has focus");
        th.check(!isTextEditorReallyFocused(), "TextEditor really lost focus");
    }

    protected void paint(Graphics graphics) {}
}
