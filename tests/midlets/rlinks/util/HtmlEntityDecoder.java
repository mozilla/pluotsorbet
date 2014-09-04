/**
* Copyright (c) 2012-2013 Nokia Corporation. All rights reserved.
* Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
* Oracle and Java are trademarks or registered trademarks of Oracle and/or its
* affiliates. Other product and company names mentioned herein may be trademarks
* or trade names of their respective owners. 
* See LICENSE.TXT for license information.
*/

package com.nokia.example.rlinks.util;

/**
 * Utility class for decoding Strings with HTML entities.
 *
 * Only selectively decodes a small fraction of all possible entities.
 */
public class HtmlEntityDecoder {

    public static String decode(String s) {
        String result = replace(s, "&nbsp;", " ");
        result = replace(result, "&lt;", "<");
        result = replace(result, "&gt;", ">");
        result = replace(result, "&amp;", "&");
        result = replace(result, "&copy;", "(c)");
        result = replace(result, "&#3232;", "@"); // quite Reddit-specific
        return result;
    }

    /**
     * Replace all instances of <search> in a given String with <replace>.
     *
     * Does not alter the original String but instead returns a new one.
     *
     * @param subject String to look in
     * @param search Text to search
     * @param replace Text to replace it with
     * @return String with all appearances of <search> replaced with <replace>
     */
    private static String replace(String subject, String search, String replace) {
        int matchIndex = subject.indexOf(search);
        if (matchIndex == -1) {
            return subject;
        }

        StringBuffer result = new StringBuffer();
        int prevIndex = 0;
        while (matchIndex >= 0) {
            result.append(subject.substring(prevIndex, matchIndex)).append(replace);

            prevIndex = matchIndex + search.length();
            matchIndex = subject.indexOf(search, prevIndex);

            if (matchIndex == -1) {
                result.append(subject.substring(prevIndex));
            }
        }
        return result.toString();
    }
}
