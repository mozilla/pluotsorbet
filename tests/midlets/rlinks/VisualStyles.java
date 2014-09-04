/**
* Copyright (c) 2012-2013 Nokia Corporation. All rights reserved.
* Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
* Oracle and Java are trademarks or registered trademarks of Oracle and/or its
* affiliates. Other product and company names mentioned herein may be trademarks
* or trade names of their respective owners. 
* See LICENSE.TXT for license information.
*/

package com.nokia.example.rlinks;

import com.nokia.example.rlinks.Main;
import com.nokia.mid.ui.DirectUtils;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Font;

/**
 * Predefined constant visual styles for easy reference and code de-duplication.
 */
public class VisualStyles {

    private final static Display display = Display.getDisplay(Main.getInstance());

    public static final int COLOR_SCORE = 0x005484;
    public static final int COLOR_FOREGROUND = display.getColor(Display.COLOR_FOREGROUND);
    public static final int COLOR_FOREGROUND_DIM;
    public static final int COLOR_BACKGROUND = display.getColor(Display.COLOR_BACKGROUND);
    public static final int COLOR_BORDER = display.getColor(Display.COLOR_BORDER);
    public static final int COLOR_HIGHLIGHTED_FOREGROUND = display.getColor(Display.COLOR_HIGHLIGHTED_BACKGROUND);
    public static final int COLOR_HIGHLIGHTED_BACKGROUND = display.getColor(Display.COLOR_HIGHLIGHTED_FOREGROUND);
    public static final int COLOR_HIGHLIGHTED_BORDER = display.getColor(Display.COLOR_HIGHLIGHTED_BORDER);

    public static Font SOFTKEY_FONT;
    public static Font SMALL_FONT;
    public static Font SMALL_BOLD_FONT;
    public static Font MEDIUM_FONT;
    public static Font MEDIUM_BOLD_FONT;
    public static Font LARGE_FONT;
    public static Font LARGE_BOLD_FONT;

    public static final int LINK_H_SPACE = 4;
    public static final int LINK_V_SPACE = 8;
    public static final int LINK_SEPARATOR_H_SPACE = 14;
    public static final int LINK_SEPARATOR_V_SPACE = 4;
    public static final int COMMENT_H_SPACE = 8;
    public static final int COMMENT_V_SPACE = 6;
    public static final int CATEGORY_H_SPACE = 4;
    public static final int CATEGORY_V_SPACE = 12;

    static {
        if ("true".equals(System.getProperty("com.nokia.mid.ui.customfontsize"))) {
            SOFTKEY_FONT = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL);
            SMALL_FONT = DirectUtils.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, 12);
            SMALL_BOLD_FONT = DirectUtils.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, 12);
            MEDIUM_FONT = DirectUtils.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, 13);
            MEDIUM_BOLD_FONT = DirectUtils.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, 13);
            LARGE_FONT = DirectUtils.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, 14);
            LARGE_BOLD_FONT = DirectUtils.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, 14);
        }
        else {
            SOFTKEY_FONT = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL);
            SMALL_FONT = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
            SMALL_BOLD_FONT = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL);
            MEDIUM_FONT = SMALL_FONT;
            MEDIUM_BOLD_FONT = SMALL_BOLD_FONT;
            LARGE_FONT = SMALL_FONT;
            LARGE_BOLD_FONT = SMALL_BOLD_FONT;
        }

        // Determine brightness of foreground color in current theme, and
        // create a "dimmed" secondary color accordingly. When the primary
        // color is white, the secondary color will be a light gray,
        // and in case of black it will be a dark gray.
        boolean isBrightForegroundColor = getBrightness(COLOR_FOREGROUND) > 125;
        int amount = 50;
        amount = isBrightForegroundColor ? -amount : amount;
        COLOR_FOREGROUND_DIM = adjustBrightness(COLOR_FOREGROUND, amount);
    }

    /**
     * Adjust brightness for a color. Use positive value to lighten,
     * negative to darken.
     *
     * @param color Input color
     * @param amount Amount to adjust (positive to brighten, negative to darken)
     * @return Adjusted color
     */
    public static int adjustBrightness(int color, int amount) {
        int r = (color >> 16) + amount;
        int g = (color & 0x0000FF) + amount;
        int b = ((color >> 8) & 0x00FF) + amount;
        return g | (b << 8) | (r << 16);
    }

    /**
     * Calculate a "perceived brightness" value for a color, on a range
     * from 0 (black) to 255 (white).
     *
     * For legibility, the difference in brightness between background
     * and foreground should be >125.
     *
     * This formula is found on the W3 site: http://www.w3.org/TR/AERT
     *
     * @param color Input color
     * @return Perceived brightness for given color, 0 to 255
     */
    public static int getBrightness(int color) {
        int r = (color >> 16);
        int g = (color & 0x0000FF);
        int b = ((color >> 8) & 0x00FF);

        return ((r * 299) + (g * 587) + (b * 114)) / 1000;
    }
}
