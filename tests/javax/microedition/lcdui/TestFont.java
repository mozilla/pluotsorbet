package javax.microedition.lcdui;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

public class TestFont implements Testlet {
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

    void testEmojiLength(TestHarness th, String code, int expectedLower, int expectedUpper) {
        String emoji = getSurrogatePairs(checkCodeFormat(code));
        th.check(Font.getDefaultFont().stringWidth(emoji) >= expectedLower);
        th.check(Font.getDefaultFont().stringWidth(emoji) <= expectedUpper);
        th.check(Font.getDefaultFont().substringWidth(emoji, 0, emoji.length()) >= expectedLower);
        th.check(Font.getDefaultFont().substringWidth(emoji, 0, emoji.length()) <= expectedUpper);
        th.check(Font.getDefaultFont().charsWidth(emoji.toCharArray(), 0, emoji.toCharArray().length) >= expectedLower);
        th.check(Font.getDefaultFont().charsWidth(emoji.toCharArray(), 0, emoji.toCharArray().length) <= expectedUpper);
    }

    public void test(TestHarness th) {
        // Test that an emoji represented with two codepoints is considered as 4 characters long.
        testEmojiLength(th, "1f1ee1f1f9", Font.getDefaultFont().stringWidth("mmm"), Font.getDefaultFont().stringWidth("mmmmm"));
        // Test that an emoji represented with one codepoint is considered as 2 characters long.
        testEmojiLength(th, "1f355", Font.getDefaultFont().stringWidth("m"), Font.getDefaultFont().stringWidth("mmm"));
    }
}
