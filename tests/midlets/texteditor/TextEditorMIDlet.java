package tests.texteditor;

import javax.microedition.midlet.*;
import com.nokia.mid.ui.TextEditor;
import com.nokia.mid.ui.TextEditorListener;
import javax.microedition.lcdui.*;
import gnu.testlet.TestUtils;

public class TextEditorMIDlet extends MIDlet implements TextEditorListener, CommandListener {
    private Command insertEmoji;
    private Command getPosition;
    private Command getSize;
    private Command getContent;
    private Command getContentHeight;
    private Command quitCommand;
    private TextEditor textEditor;
    private TextEditorListener listener;
    private TextEditor emojiCodeEditor;

    class TestCanvas extends Canvas {
        protected void paint(Graphics g) {
            g.setColor(0x00FFFFFF);
            g.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
            g.setColor(0xFF000000);
            g.drawString(textEditor.getContent(), 0, 250, Graphics.TOP | Graphics.LEFT);
        }

        protected void pointerPressed(int x, int y) {
            repaint();
        }
    }

    public void inputAction(TextEditor aTextEditor, int actions) {
        if (aTextEditor == textEditor && (actions & TextEditorListener.ACTION_CONTENT_CHANGE) != 0) {
            System.out.println("CONTENT CHANGED");
        }
    }

    public void startApp() {
        Display display = Display.getDisplay(this);

        insertEmoji = new Command("Insert Emoji", Command.SCREEN, 1);
        getPosition = new Command("Get position", Command.SCREEN, 2);
        getSize = new Command("Get size", Command.SCREEN, 3);
        getContent = new Command("Get content", Command.SCREEN, 4);
        getContentHeight = new Command("Get content height", Command.SCREEN, 5);
        quitCommand = new Command("Quit", Command.EXIT, 6);

        TestCanvas canvas = new TestCanvas();
        canvas.addCommand(insertEmoji);
        canvas.addCommand(getPosition);
        canvas.addCommand(getSize);
        canvas.addCommand(getContent);
        canvas.addCommand(getContentHeight);
        canvas.addCommand(quitCommand);
        canvas.setCommandListener(this);
        display.setCurrent(canvas);

        textEditor = TextEditor.createTextEditor("AAAAA", 50, TextField.ANY, 70, 70);
        textEditor.setTextEditorListener(this);

        textEditor.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_LARGE));
        textEditor.setParent(canvas);
        textEditor.setMultiline(true);
        textEditor.setBackgroundColor(0x00FFFFFF);
        textEditor.setForegroundColor(0xFF000000);
        textEditor.setVisible(true);
        textEditor.setFocus(true);
        textEditor.setPosition(0, 0);

        emojiCodeEditor = TextEditor.createTextEditor("1f1ee1f1f9", 50, TextField.ANY, 70, 70);
        emojiCodeEditor.setTextEditorListener(this);
        emojiCodeEditor.setParent(canvas);
        emojiCodeEditor.setMultiline(true);
        emojiCodeEditor.setBackgroundColor(0x00FFFFFF);
        emojiCodeEditor.setForegroundColor(0xFF000000);
        emojiCodeEditor.setVisible(true);
        emojiCodeEditor.setFocus(false);
        emojiCodeEditor.setPosition(0, 100);

        TextEditor pwdEditor = TextEditor.createTextEditor("pwd", 50, TextField.PASSWORD, 70, 70);
        pwdEditor.setTextEditorListener(this);
        pwdEditor.setParent(canvas);
        pwdEditor.setMultiline(true);
        pwdEditor.setBackgroundColor(0x00FFFFFF);
        pwdEditor.setForegroundColor(0xFF000000);
        pwdEditor.setVisible(true);
        pwdEditor.setFocus(false);
        pwdEditor.setPosition(100, 100);
    }

    public void commandAction(Command c, Displayable s) {
        if (c == insertEmoji) {
            String code = TestUtils.getEmojiString(emojiCodeEditor.getContent());
            textEditor.insert(code, textEditor.getCaretPosition());
        } else if (c == getPosition) {
            Alert alert = new Alert("Position", "Position: " + textEditor.getCaretPosition(), null, AlertType.INFO);
            Display display = Display.getDisplay(this);
            Displayable current = display.getCurrent();
            if (!(current instanceof Alert)) {
                // This next call can't be done when current is an Alert
                display.setCurrent(alert, current);
            }
        } else if (c == getSize) {
            Alert alert = new Alert("Size", "Size: " + textEditor.size(), null, AlertType.INFO);
            Display display = Display.getDisplay(this);
            Displayable current = display.getCurrent();
            if (!(current instanceof Alert)) {
                // This next call can't be done when current is an Alert
                display.setCurrent(alert, current);
            }
        } else if (c == getContent) {
            String val = "";
            try {
                byte arr[] = textEditor.getContent().getBytes("UTF-8");
                for(int i=0; i < arr.length; i++) {
                    val += arr[i] + ", ";
                }
            } catch (Exception e) {
            }

            Alert alert = new Alert("Content", "Content: " + val, null, AlertType.INFO);
            Display display = Display.getDisplay(this);
            Displayable current = display.getCurrent();
            if (!(current instanceof Alert)) {
                // This next call can't be done when current is an Alert
                display.setCurrent(alert, current);
            }
        } else if (c == getContentHeight) {
            Alert alert = new Alert("Content height", "Content height: " + textEditor.getContentHeight(), null, AlertType.INFO);
            Display display = Display.getDisplay(this);
            Displayable current = display.getCurrent();
            if (!(current instanceof Alert)) {
                // This next call can't be done when current is an Alert
                display.setCurrent(alert, current);
            }
        } else if (c == quitCommand) {
            destroyApp(false);
            notifyDestroyed();
        }
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }
};
