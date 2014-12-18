package com.nokia.mid.ui;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.TextField;

public class TestTextEditor extends Canvas implements Testlet {
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

    public void testConstraints(TestHarness th, int constraints) {
        TextEditor textEditor = new TextEditor("Hello, world!", 20, 0, 100, 24);

        th.check(textEditor.getContent(), "Hello, world!");
        th.check(textEditor.getMaxSize(), 20);
        th.check(textEditor.getCaretPosition(), 13);

        textEditor.setConstraints(constraints);
        th.check(textEditor.getConstraints(), constraints);

        th.check(textEditor.getWidth(), 100);
        th.check(textEditor.getHeight(), 24);

        textEditor.setContent("Helló, világ!");
        th.check(textEditor.getContent(), "Helló, világ!");

        th.check(textEditor.setMaxSize(22), 22);
        th.check(textEditor.getMaxSize(), 22);
        th.check(textEditor.getContent(), "Helló, világ!");
        th.check(textEditor.setMaxSize(5), 5);
        th.check(textEditor.getContent(), "Helló");
        th.check(textEditor.getCaretPosition(), 5);

        textEditor.delete(2, 2);
        th.check(textEditor.getContent(), "Heó");
        th.check(textEditor.getCaretPosition(), 2);

        textEditor.insert("ll", 2);
        th.check(textEditor.getContent(), "Helló");
        th.check(textEditor.getCaretPosition(), 4);

        textEditor.setSize(120, 28);
        th.check(textEditor.getWidth(), 120);
        th.check(textEditor.getHeight(), 28);

        th.check(textEditor.isVisible(), false);
        textEditor.setVisible(true);
        th.check(textEditor.isVisible(), true);
        textEditor.setVisible(false);
        th.check(textEditor.isVisible(), false);

        textEditor.setParent(this);
        th.check(textEditor.getParent(), this);

        th.check(textEditor.isMultiline(), false);
        textEditor.setMultiline(true);
        th.check(textEditor.isMultiline(), true);
        textEditor.setMultiline(false);
        th.check(textEditor.isMultiline(), false);

        th.check(textEditor.getBackgroundColor(), 0xFFFFFFFF);
        textEditor.setBackgroundColor(0xBBBBBBBB);
        th.check(textEditor.getBackgroundColor(), 0xBBBBBBBB);

        th.check(textEditor.getForegroundColor(), 0xFF000000);
        textEditor.setForegroundColor(0x33333333);
        th.check(textEditor.getForegroundColor(), 0x33333333);

        th.check(textEditor.getLineMarginHeight(), 0);
        th.check(Math.abs(textEditor.getContentHeight() - Font.getDefaultFont().getHeight()) <= 1, "One");
        textEditor.setMultiline(true);
        th.check(Math.abs(textEditor.getContentHeight() - Font.getDefaultFont().getHeight()) <= 1, "Two");
        textEditor.setContent("A\nB");
        th.check(Math.abs(textEditor.getContentHeight() - Font.getDefaultFont().getHeight() * 2) <= 3, "Three");
        textEditor.setContent("A\r\nB");
        th.check(Math.abs(textEditor.getContentHeight() - Font.getDefaultFont().getHeight() * 2) <= 3, "Four");
        textEditor.setContent("A\nB\nC");
        th.check(Math.abs(textEditor.getContentHeight() - Font.getDefaultFont().getHeight() * 3) <= 4, "Five");
        textEditor.setContent("");
        th.check(Math.abs(textEditor.getContentHeight() - Font.getDefaultFont().getHeight()) <= 1, "Six"); // Check the value on the Nokia Asha

        th.check(textEditor.getPositionX(), 0);
        th.check(textEditor.getPositionY(), 0);
        textEditor.setPosition(10, 20);
        th.check(textEditor.getPositionX(), 10);
        th.check(textEditor.getPositionY(), 20);
        textEditor.setPosition(-10, -20);
        th.check(textEditor.getPositionX(), -10);
        th.check(textEditor.getPositionY(), -20);

        textEditor.setVisible(true);
        textEditor.setContent("XYZYZ");
        th.check(textEditor.getCaretPosition(), 5);
        textEditor.setCaret(3);
        th.check(textEditor.getCaretPosition(), 3);
        textEditor.setCaret(5);
        th.check(textEditor.getCaretPosition(), 5);
        textEditor.setCaret(0);
        th.check(textEditor.getCaretPosition(), 0);
        try {
            textEditor.setCaret(-1);
            th.fail("Exception expected");
        } catch (StringIndexOutOfBoundsException e) {
            th.check(true, "Exception expected");
        }
        th.check(textEditor.getCaretPosition(), 0);
        try {
            textEditor.setCaret(6);
            th.fail("Exception expected");
        } catch (StringIndexOutOfBoundsException e) {
            th.check(true, "Exception expected");
        }
        th.check(textEditor.getCaretPosition(), 0);

        Font font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_LARGE);
        textEditor.setFont(font);
        th.check(textEditor.getFont(), font);

        textEditor.setParent(null);
    }

    public void testEmoji(TestHarness th) {
        TextEditor textEditor = TextEditor.createTextEditor("Hello, world!", 200, TextField.ANY, 150, 50);

        textEditor.setParent(this);
        textEditor.setMultiline(true);
        textEditor.setBackgroundColor(0x00FFFFFF);
        textEditor.setForegroundColor(0xFF000000);
        textEditor.setVisible(true);
        textEditor.setFocus(true);
        textEditor.setPosition(0, 0);

        th.check(textEditor.getCaretPosition(), 13);

        String code = getSurrogatePairs(checkCodeFormat("1f609"));
        textEditor.insert(code, textEditor.getCaretPosition());

        th.check(textEditor.getCaretPosition(), 14);

        textEditor.insert(code, textEditor.getCaretPosition());

        th.check(textEditor.getCaretPosition(), 15);

        th.check(textEditor.size(), 15);

        textEditor.insert(code, 0);
        th.check(textEditor.getCaretPosition(), 1);
        th.check(textEditor.size(), 16);
        textEditor.insert(code, 1);
        th.check(textEditor.getCaretPosition(), 2);
        th.check(textEditor.size(), 17);
        textEditor.delete(1, 1);
        th.check(textEditor.size(), 16);

        // Emoji + "Hello, world!" + Emoji + Emoji
        th.check(textEditor.getContent(), code + "Hello, world!" + code + code);
        // Set caret position at the beginning
        textEditor.setCaret(0);
        th.check(textEditor.getCaretPosition(), 0);
        // Set caret position after the first emoji
        textEditor.setCaret(1);
        th.check(textEditor.getCaretPosition(), 1);
        // Set caret position in the middle of the text
        textEditor.setCaret(7);
        th.check(textEditor.getCaretPosition(), 7);
        // Set caret position before an emoji
        textEditor.setCaret(14);
        th.check(textEditor.getCaretPosition(), 14);
        // Set caret position between two emojis
        textEditor.setCaret(15);
        th.check(textEditor.getCaretPosition(), 15);
        // Set caret position at the end
        textEditor.setCaret(16);
        th.check(textEditor.getCaretPosition(), 16);
        try {
            textEditor.setCaret(17);
            th.fail("Exception expected");
        } catch (StringIndexOutOfBoundsException e) {
            th.check(true, "Exception expected");
        }

        // Test with empty TextEditor
        textEditor.setContent("");
        th.check(textEditor.getContent(), "");
        th.check(textEditor.getCaretPosition(), 0);

        // Test with a blank line
        textEditor.setContent("\n");
        th.check(textEditor.size(), 1);
        th.check(textEditor.getCaretPosition(), 1);
        textEditor.setCaret(0);
        th.check(textEditor.getCaretPosition(), 0);
        textEditor.setCaret(1);
        th.check(textEditor.getCaretPosition(), 1);

        // Test setting caret position to 0 with text
        textEditor.setContent("Ciao");
        textEditor.setCaret(0);
        th.check(textEditor.getCaretPosition(), 0);

        // An emoji with 2 codepoints turns into another emoji with 1 codepoint if
        // you press backspace (or call delete on one of the codepoints).
        // This looks weird to me, but it's how the Nokia implementation works.
        // Also, the size() and the getCaretPosition() methods return a number
        // related to the number of codepoints present, not the number of emoji.
        // So, in theory, you could set the caret position in the middle of an emoji.
        // Even if we don't want to do this when the user presses backspace (and indeed
        // we aren't doing it), we still want to keep the behavior of the API compatible
        // to the Nokia one. So we allow setting the caret position in the middle of an
        // emoji, we allow removing one of the two codepoints with the |delete| method and
        // we consider one emoji with two codepoints as taking two spaces in the TextEditor.
        // Hence, we're testing this behavior here.
        textEditor.setContent(getSurrogatePairs(checkCodeFormat("1f1ee1f1f9")));
        th.check(textEditor.getCaretPosition(), 2);
        th.check(textEditor.size(), 2);
        textEditor.delete(1, 1);
        th.check(textEditor.getContent(), getSurrogatePairs(checkCodeFormat("1f1ee")));
        th.check(textEditor.getCaretPosition(), 1);
        th.check(textEditor.size(), 1);
        textEditor.setContent(getSurrogatePairs(checkCodeFormat("1f1ee1f1f9")));
        textEditor.delete(0, 1);
        th.check(textEditor.getContent(), getSurrogatePairs(checkCodeFormat("1f1f9")));
        th.check(textEditor.getCaretPosition(), 0);
        th.check(textEditor.size(), 1);

        textEditor.setContent(getSurrogatePairs(checkCodeFormat("2320e3")));
        th.check(textEditor.getCaretPosition(), 2);
        th.check(textEditor.size(), 2);
        textEditor.delete(1, 1);
        th.check(textEditor.getContent(), "#");
        th.check(textEditor.getCaretPosition(), 1);
        th.check(textEditor.size(), 1);
        textEditor.setContent(getSurrogatePairs(checkCodeFormat("2320e3")));
        textEditor.delete(0, 1);
        th.check(textEditor.getContent(), getSurrogatePairs(checkCodeFormat("20e3")));
        th.check(textEditor.getCaretPosition(), 0);
        th.check(textEditor.size(), 1);

    }

    private native boolean isTextEditorReallyFocused();

    public void test(TestHarness th) {
        testConstraints(th, TextField.ANY);
        testConstraints(th, TextField.PASSWORD);
        testEmoji(th);
    }

    protected void paint(Graphics graphics) {}
}
