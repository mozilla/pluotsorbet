package gnu.testlet;

import java.io.*;

public class TestUtils {
    public static byte[] read(InputStream is) throws IOException {
        int l = is.available();
        byte[] buffer = new byte[l+1];
        int length = 0;

        while ((l = is.read(buffer, length, buffer.length - length)) != -1) {
            length += l;
            if (length == buffer.length) {
                byte[] b = new byte[buffer.length + 4096];
                System.arraycopy(buffer, 0, b, 0, length);
                buffer = b;
            }
        }

        if (length < buffer.length) {
            byte[] b = new byte[length];
            System.arraycopy(buffer, 0, b, 0, length);
            buffer = b;
        }

        return buffer;
    }

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

    public static String getEmojiString(String code) {
        return getSurrogatePairs(checkCodeFormat(code));
    }
}
