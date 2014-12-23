/* --- Copyright Jonathan Meyer 1997. All rights reserved. -----------------
 > File:        jasmin/src/jasmin/ScannerUtils.java
 > Purpose:     Various static methods utilized to breakdown strings
 > Author:      Jonathan Meyer, 8 Feb 1997
 */

package jasmin;

abstract class ScannerUtils {

    //
    // Converts a string of a given radix to an int or a long
    // (uses smallest format that will hold the number)
    //
    public static Number convertInt(String str, int radix)
                throws NumberFormatException
    {
        long x = Long.parseLong(str, radix);
        if (x <= (long)Integer.MAX_VALUE && x >= (long)Integer.MIN_VALUE) {
            return new Integer((int)x);
        }
        return new Long(x);
    }

    //
    // Converts a string to a number (int, float, long, or double).
    // (uses smallest format that will hold the number)
    //
    public static Number convertNumber(String str)
                throws NumberFormatException
    {
        if(str.startsWith("+")) {
            return new Integer(str.substring(1,str.length()));
        }
        if (str.startsWith("0x")) {
            // base 16 integer
            return (convertInt(str.substring(2), 16));
        } else if (str.indexOf('.') != -1) {
            // make a double
            Double dc = new Double(str);
            // if number have suffix 'd' force double value
            // thanks to JD Brennan
            if(!str.endsWith("d")) {
                double x = dc.doubleValue();

                // see if it will fit into a float...
                if (x <= (double)Float.MAX_VALUE && x >= (float)Float.MIN_VALUE) {
                    // bug fix here thanks to Peter Chubb (replaced int->float)
                    return new Float((float)x);
                }
                // if not, return a double
            }
            return dc;
        } else {
            // assume long or int in base 10
            return (convertInt(str, 10));
        }
    }

    //
    // Maps '.' characters to '/' characters in a string
    //
    public static String convertDots(String orig_name)
    {
        return convertChars(orig_name, ".", '/');
    }

    //
    // Maps chars to toChar in a given String
    //
    public static String convertChars(String orig_name,
                                      String chars, char toChar)
    {
        StringBuffer tmp = new StringBuffer(orig_name);
        int i;
        for (i = 0; i < tmp.length(); i++) {
            if (chars.indexOf(tmp.charAt(i)) != -1) {
                tmp.setCharAt(i, toChar);
            }
        }
        return new String(tmp);
    }

    //
    // Splits a string like:
    //     "a/b/c/d(xyz)v"
    // into three strings:
    //     "a/b/c", "d", "(xyz)v"
    //
    public static String[] splitClassMethodSignature(String name) {
        String result[] = new String[3];
        int i, pos = 0, sigpos = 0;
        for (i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c == '.' || c == '/') pos = i;
            else if (c == '(') {sigpos = i; break; }
        }
        try {
            result[0] = convertDots(name.substring(0, pos));
            result[1] = name.substring(pos + 1, sigpos);
            result[2] = convertDots(name.substring(sigpos));
        } catch(StringIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("malformed signature : "+name);
        }
        return result;
    }

    //
    // Splits a string like:
    //    "java/lang/System/out"
    // into two strings:
    //    "java/lang/System" and "out"
    //
    public static String[] splitClassField(String name)
    {
        String result[] = new String[2];
        int i, pos = -1, sigpos = 0;
        for (i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c == '.' || c == '/') pos = i;
        }
        if (pos == -1) {    // no '/' in string
            result[0] = null;
            result[1] = name;
        } else {
            result[0] = convertDots(name.substring(0, pos));
            result[1] = name.substring(pos + 1);
        }

        return result;
    }

    // Splits a string like:
    //      "main(Ljava/lang/String;)V
    // into two strings:
    //      "main" and "(Ljava/lang/String;)V"
    //
    public static String[] splitMethodSignature(String name)
    {
        String result[] = new String[2];
        int i, sigpos = 0;
        for (i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c == '(') {sigpos = i; break; }
        }
        result[0] = name.substring(0, sigpos);
        result[1] = convertDots(name.substring(sigpos));

        return result;
    }
}

/* --- Revision History ---------------------------------------------------
--- Iouri Kharon, May 07 2010
    'd' in real constant, force double mode.
*/
