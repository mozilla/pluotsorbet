/**
* Copyright (c) 2012-2013 Nokia Corporation. All rights reserved.
* Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
* Oracle and Java are trademarks or registered trademarks of Oracle and/or its
* affiliates. Other product and company names mentioned herein may be trademarks
* or trade names of their respective owners. 
* See LICENSE.TXT for license information.
*/

package com.nokia.example.rlinks.util;

import java.util.Vector;
import javax.microedition.lcdui.Font;

/**
 * Utility for wrapping text to a certain width with a brute force method.
 */
public final class TextWrapper {

    /**
     * Wrap text to a given width with a specified font.
     *
     * @param text Text to be wrapped
     * @param wrapWidth Max width of one line in pixels
     * @param font Font to be used in calculating 
     * @return
     */
    public static Vector wrapTextToWidth(String text, int wrapWidth, Font font) {
        if (wrapWidth < 20) {
            wrapWidth = 240;
        }

        Vector lines = new Vector();

        int start = 0;
        int position = 0;
        int length = text.length();
        while (position < length - 1) {
            start = position;
            int lastBreak = -1;
            int i = position;
            for (; i < length && font.stringWidth(text.substring(position, i))
                <= wrapWidth; i++) {
                if (text.charAt(i) == ' ') {
                    lastBreak = i;
                }
                else if (text.charAt(i) == '\n') {
                    lastBreak = i;
                    break;
                }
            }
            if (i == length) {
                position = i;
            }
            else if (lastBreak <= position) {
                position = i;
            }
            else {
                position = lastBreak;
            }

            lines.addElement(text.substring(start, position));

            if (position == lastBreak) {
                position++;
            }
        }

        return lines;
    }
}
