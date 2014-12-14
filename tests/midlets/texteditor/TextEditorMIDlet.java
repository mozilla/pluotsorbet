package tests.texteditor;

import javax.microedition.midlet.*;
import com.nokia.mid.ui.TextEditor;
import com.nokia.mid.ui.TextEditorListener;
import javax.microedition.lcdui.*;

public class TextEditorMIDlet extends MIDlet implements TextEditorListener, CommandListener {
    private Command insertEmoji;
    private Command getPosition;
    private Command getSize;
    private Command quitCommand;
    private TextEditor textEditor;
    private TextEditorListener listener;

    public static String checkCodeFormat(String code) {
        if (code == null) {
            return code;
        }
        if (code.length() == 10) {
            String c1 = code.substring(0, 5);
            String c2 = code.substring(5);
            return c1 + " " + c2;
        } else if (code.length() == 6) {
            String c1 = code.substring(0, 2);
            String c2 = code.substring(2);
            return c1 + " " + c2;
        } else {
            return code;
        }
    }

    private final static int LEAD_OFFSET = 0xD800 - (0x10000 >> 10);

    public static String getSurrogatePairs(String inputString) {
        int character;
        char low, high;
        int start = 0, end = 0;
        if (inputString == null) {
            return inputString;
        }

        StringBuffer sb = new StringBuffer(1000);

        // Go through all characters in the input.
        // Space (0x20) is used as separator
        do {
            end = inputString.indexOf(" ", start);

            // Space not found -> last sub-string
            if (end == -1) {
                end = inputString.length();
            }

            try {
                character = Integer.parseInt(inputString.substring(start, end), 16);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            // Anything below 0xffff is not surrogate pair
            if (character < 0xffff) {
                sb.append((char) character);
            } else {
                // From http://www.unicode.org/faq/utf_bom.html
                high = (char) (LEAD_OFFSET + (character >> 10));
                low = (char) (0xDC00 + (character & 0x3FF));

                sb.append(high);
                sb.append(low);
            }

            // skip the space
            start = (end + 1);
        } while (end != inputString.length());

        return sb.toString();
    }

    class TestCanvas extends Canvas {
        protected void paint(Graphics g) {
            g.setColor(0x00FFFFFF);;
            g.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
        }
    }

    public void inputAction(TextEditor textEditor, int actions) {
        if ((actions & TextEditorListener.ACTION_CONTENT_CHANGE) != 0) {
            System.out.println("CONTENT CHANGED");
        }
    }

    public void startApp() {
        Display display = Display.getDisplay(this);

        insertEmoji = new Command("Insert Emoji", Command.SCREEN, 1);
        getPosition = new Command("Get position", Command.SCREEN, 2);
        getSize = new Command("Get size", Command.SCREEN, 3);
        quitCommand = new Command("Quit", Command.EXIT, 4);

        TestCanvas canvas = new TestCanvas();
        canvas.addCommand(insertEmoji);
        canvas.addCommand(getPosition);
        canvas.addCommand(getSize);
        canvas.addCommand(quitCommand);
        canvas.setCommandListener(this);
        display.setCurrent(canvas);

        textEditor = TextEditor.createTextEditor("Hello, world!", 200, TextField.ANY, 150, 50);
        textEditor.setTextEditorListener(this);

        textEditor.setParent(canvas);
        textEditor.setMultiline(true);
        textEditor.setBackgroundColor(0x00FFFFFF);
        textEditor.setForegroundColor(0xFF000000);
        textEditor.setVisible(true);
        textEditor.setFocus(true);
        textEditor.setPosition(0, 0);
    }

    public void commandAction(Command c, Displayable s) {
        if (c == insertEmoji) {
            String code = getSurrogatePairs(checkCodeFormat("1f609"));
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
