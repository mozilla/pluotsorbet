/* vim: set filetype=java shiftwidth=4 tabstop=4 autoindent cindent expandtab : */

package javax.microedition.lcdui;

import com.nokia.mid.ui.TextEditor;
import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;

class TestScreenSetFocus extends Canvas {
    public TextEditor textEditor;

    protected void paint(Graphics graphics) {
        textEditor = TextEditor.createTextEditor("Hello, world!", 20, 0, 100, 24);
        textEditor.setParent(this);
        textEditor.setFocus(true);
    }
}

class TestScreenWithoutFocus extends Canvas {
    public TextEditor textEditor;

    protected void paint(Graphics graphics) {
        textEditor = TextEditor.createTextEditor("Hello, world!", 20, 0, 100, 24);
        textEditor.setParent(this);
    }
}

public class TestTextEditorFocus implements Testlet {
    public int getExpectedPass() { return 9; }
    public int getExpectedFail() { return 0; }
    public int getExpectedKnownFail() { return 1; }
    private native boolean isTextEditorReallyFocused(TextEditor textEditor);

    public void test(TestHarness th) {
        TestScreenSetFocus testScreen1 = new TestScreenSetFocus();
        th.setScreenAndWait(testScreen1);
        th.check(testScreen1.textEditor.hasFocus(), "First TextEditor gained focus");
        th.check(isTextEditorReallyFocused(testScreen1.textEditor), "First TextEditor really gained focus");

        TestScreenSetFocus testScreen2 = new TestScreenSetFocus();
        th.setScreenAndWait(testScreen2);
        th.check(!testScreen1.textEditor.hasFocus(), "First TextEditor lost focus");
        th.check(testScreen2.textEditor.hasFocus(), "Second TextEditor gained focus");
        th.check(!isTextEditorReallyFocused(testScreen1.textEditor), "First TextEditor really lost focus");
        th.check(isTextEditorReallyFocused(testScreen2.textEditor), "Second TextEditor really gained focus");

        TestScreenWithoutFocus testScreen3 = new TestScreenWithoutFocus();
        th.setScreenAndWait(testScreen3);
        th.todo(!testScreen2.textEditor.hasFocus(), "Second TextEditor lost focus");
        th.check(!testScreen3.textEditor.hasFocus(), "Third TextEditor didn't gain focus");
        th.check(!isTextEditorReallyFocused(testScreen2.textEditor), "Second TextEditor really lost focus");
        th.check(!isTextEditorReallyFocused(testScreen3.textEditor), "Third TextEditor didn't gain focus");
    }
}
