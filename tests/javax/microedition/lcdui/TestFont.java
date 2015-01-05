package javax.microedition.lcdui;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;
import gnu.testlet.TestUtils;

public class TestFont implements Testlet {
    void testEmojiLength(TestHarness th, String code, int expectedUpper) {
        String emoji = TestUtils.getEmojiString(code);
        th.check(Font.getDefaultFont().stringWidth(emoji) <= expectedUpper);
        th.check(Font.getDefaultFont().substringWidth(emoji, 0, emoji.length()) <= expectedUpper);
        th.check(Font.getDefaultFont().charsWidth(emoji.toCharArray(), 0, emoji.toCharArray().length) <= expectedUpper);
    }

    public void test(TestHarness th) {
        // Test that an emoji represented with two codepoints is considered as 1 character long.
        testEmojiLength(th, "1f1ee1f1f9", Font.getDefaultFont().stringWidth("mm"));
        // Test that an emoji represented with one codepoint is considered as 1 character long.
        testEmojiLength(th, "1f355", Font.getDefaultFont().stringWidth("mm"));
    }
}
