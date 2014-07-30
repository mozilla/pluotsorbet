/*
 *   
 *
 * Copyright  1990-2009 Sun Microsystems, Inc. All Rights Reserved. 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER 
 *  
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License version 
 * 2 only, as published by the Free Software Foundation. 
 *  
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License version 2 for more details (a copy is 
 * included at /legal/license.txt). 
 *  
 * You should have received a copy of the GNU General Public License 
 * version 2 along with this work; if not, write to the Free Software 
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 
 * 02110-1301 USA 
 *  
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa 
 * Clara, CA 95054 or visit www.sun.com if you need additional 
 * information or have any questions.
 */

package com.sun.midp.lcdui;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

import com.sun.midp.chameleon.skins.StringItemSkin;
import com.sun.midp.chameleon.skins.ScreenSkin;
import com.sun.midp.chameleon.skins.TextFieldSkin;
import com.sun.midp.i18n.Resource;
import com.sun.midp.i18n.ResourceConstants;

/**
 * Static method class use to draw and size text.
 */

public class Text {

    /** Character to be used as a truncation indiactor,
     *  for example, \u2026 which is ellipsis (...). */
    private static final char truncationMark = Resource.getString(ResourceConstants.TRUNCATION_MARK).charAt(0);//'\u2026';
 
    // the following are used in calling the getNextLine method

    /** Line start. */
    private static final int GNL_LINE_START = 0;
    /** Line end. */
    private static final int GNL_LINE_END = 1;
    /** New line start. */
    private static final int GNL_NEW_LINE_START = 2;
    /** Screen width available. */
    private static final int GNL_WIDTH = 3;
    /** Screen height available. */
    private static final int GNL_HEIGHT = 4;
    /** Font height. */
    private static final int GNL_FONT_HEIGHT = 5;
    /** Line number. */
    private static final int GNL_NUM_LINES = 6;
    /** Text options (NORMAL, INVERT...) see below. */
    private static final int GNL_OPTIONS = 7;
    /** Text pixel offset. */
    private static final int GNL_OFFSET = 8;
    /** Width of the ellipsis in the current font. */
    private static final int GNL_ELLIP_WIDTH = 9;
    /** Line width in pixels. */
    private static final int GNL_LINE_WIDTH = 10;
    
    /** Number of GNL_ parameter constants. */
    private static final int GNL_NUM_PARAMS = 11;
    

    // constants to affect how text drawing is handled
    // These values can be OR'd together. no error checking is performed

    /** NORMAL text. */
    public static final int NORMAL    = 0x0;
    /** INVERTED text color. */
    public static final int INVERT    = 0x1;
    /** Draw a hyperlink for the text. */
    public static final int HYPERLINK = 0x2;
    /** Truncate the text and put a "..." if the text doesn't fit the bounds. */
    public static final int TRUNCATE  = 0x4;

    // these values are stored used as setting in a TextCursor object
    /** 
     * When a paint occurs use the cursor index to know when to
     * paint the cursor.
     */
    public static final int PAINT_USE_CURSOR_INDEX = 0;

    /** 
     * When a paint occurs try to find the best value for the cursor
     * index based on the x,y coordinates of the cursor.
     */
    public static final int PAINT_GET_CURSOR_INDEX = 1;

    /**
     * Don't draw a cursor.
     */
    public static final int PAINT_HIDE_CURSOR = 2;

    /**
     * Sets up a new inout structure used in various 
     * text methods.
     * @param font the font to use
     * @param w the available width for the text
     * @param h the available height for the text
     * @param options any of NORMAL | INVERT | HYPERLINK | TRUNCATE
     * @param offset the first line pixel offset
     * @return initialized GNL_struct 
     */
    public static int[] initGNL(Font font, int w, int h, 
				int options, int offset) {
	
	int[] inout = new int[GNL_NUM_PARAMS];
	
        inout[GNL_FONT_HEIGHT] = font.getHeight();
        inout[GNL_WIDTH] = w;
        inout[GNL_HEIGHT] = h;
        inout[GNL_OPTIONS] = options;
        inout[GNL_OFFSET] = offset;
        inout[GNL_ELLIP_WIDTH] = font.charWidth(truncationMark);
        inout[GNL_LINE_START] = 0;
        inout[GNL_LINE_END] = 0;
        inout[GNL_NEW_LINE_START] = 0;
	inout[GNL_LINE_WIDTH] = 0;
	inout[GNL_NUM_LINES] = 0;
	
	return inout;
    }
    
    /**
     * Paints the text in a single line, scrolling left or right as 
     * necessary to keep the cursor visible within the available
     * width for the text.  The offset of the text after the 
     * paintLine call, whether modified or not, is returned.
     * <p>
     * If the cursor is null, signifying an uneditable TextField is
     * being painted, the text will not be scrolled left or right, and
     * the returned value will always equal the <code>offset</code>
     * argument passed in to this method.
     *
     * @param g the Graphics object to paint in
     * @param str the String to paint
     * @param font the font to use
     * @param fgColor foreground color
     * @param w the available width for the text
     * @param h the available height for the text
     * @param cursor TextCursor object to use for cursor placement
     * @param offset the pixel offset of the text (possibly negative)
     * @return the current scroll offset
     */
    public static int paintLine(Graphics g, String str, Font font, int fgColor, 
				int w, int h, TextCursor cursor, int offset) {


        if (w <= 0 ||
            (cursor == null && (str == null || str.length() == 0))) {
            return 0;
        }
	
        if (str == null) {
            str = "";
        }
	
        g.setFont(font);
        g.setColor(fgColor);
	
        char[] text = str.toCharArray();
        int fontHeight = font.getHeight();
	
        if (cursor != null && cursor.visible == false) {
            cursor = null;
        }
	
        // side-scroll distance in pixels, with default
        int scrollPix = w / 2;

	//
	// draw a vertical cursor indicator if required
	//
	if (cursor != null &&
	    cursor.option == PAINT_USE_CURSOR_INDEX && 
	    cursor.index >= 0 && cursor.index <= str.length()) {
	    int pos = offset;
	    if (cursor.index > 0) {
            pos += font.charsWidth(text, 0, cursor.index);
        }
	    // IMPL_NOTE: optimize this with math instead of iteration

        cursor.x = pos;
        if (ScreenSkin.RL_DIRECTION) {

            cursor.x = w - pos;
            if (cursor.x <= 0) {
            while (cursor.x <= 0) {
                offset -= scrollPix;
                cursor.x += scrollPix;
            }
            } else {
            while ((cursor.x > w / 2) && (offset < 0)) {
                offset += scrollPix;
                cursor.x -= scrollPix;
            }
            }
        } else {
            if (cursor.x >= w) {
            while (cursor.x >= w) {
                offset -= scrollPix;
                cursor.x -= scrollPix;
            }
            } else {
            while ((cursor.x < w / 2) && (offset < 0)) {
                offset += scrollPix;
                cursor.x += scrollPix;
            }
            }
        }
        cursor.y      = fontHeight;
	    cursor.width  = 1;
	    cursor.height = fontHeight;
	    
	    cursor.paint(g);
	    cursor = null;
	}

    g.drawChars(text, 0, text.length,  (ScreenSkin.RL_DIRECTION) ? w - offset : offset, h,
		    Graphics.BOTTOM | ScreenSkin.TEXT_ORIENT);

    return offset;
    }

    /**
     * Creates a current TextInfo struct, linewraping text
     * when necessary.  TextInfo struct is updated when
     * <code>str</code> changes, or when scrolling happens.
     * This method does not do any painting, but updates
     * <code>info</code> to be current for use by the
     * paint routine, <code>paintText</code>...
     *
     * @param str the text to use
     * @param font the font to use for sizing
     * @param w the available width for the text
     * @param h the available height for the text
     * @param offset the pixel offset of the text (possibly negative)
     * @param options only TRUNCATE matters here
     * @param cursor text cursor object for cursor position
     * @param info TextInfo structure to fill
     * @return true if successful, false if there was an error
     */
    // IMPL_NOTE: break into 3 simpler update methods: text, Y scroll, X scroll
    public static boolean updateTextInfo(String str, Font font, 
					 int w, int h, int offset, int options,
					 TextCursor cursor, TextInfo info) {


    if (w <= 0 ||
	    (cursor == null && (str == null || str.length() == 0))) {
	    return false;
	}
	
	if (str == null) {
	    str = "";
	}
	
	char[] text = str.toCharArray();
	
	int fontHeight = font.getHeight();
	
	if (cursor != null && cursor.visible == false) {
	    cursor = null;
	}
	
    int oldNumLines = info.numLines;
	if (info.isModified) {
	    
	    int[] inout = initGNL(font, w, h, options, offset);
	    
	    int numLines = 0;
	    int height   = 0;
	    
	    do {
		numLines++;
		height += fontHeight;
		info.numLines = numLines;
		if (height < h) {
		    info.visLines = info.numLines;
		}
		
		inout[GNL_NUM_LINES] = numLines;
		
		getNextLine(text, font, inout);
		
		int lineStart    = inout[GNL_LINE_START];
		int lineEnd      = inout[GNL_LINE_END];
		int newLineStart = inout[GNL_NEW_LINE_START];
		
		// IMPL_NOTE: add accessor fn to TextInfo and hide this
		//
		// check that we don't exceed info's capacity
		// before we cache line data and expand if needed
		//
		if (numLines > info.lineStart.length) {
		    info.expand();
		}
		info.lineStart[numLines - 1] = lineStart;
		info.lineEnd[numLines - 1] = lineEnd;
		
		inout[GNL_LINE_START] = newLineStart;
		inout[GNL_OFFSET] = 0;
		offset = 0;
	    } while (inout[GNL_LINE_END] < text.length);	    
	    info.height = height;
	}
	if (info.scrollY) {
            // if (lineEnd > lineStart) {
	    
	    // we are given x,y coordinates and we must calculate
	    // the best array index to put the cursor
	    //
	    if (cursor != null && 
		cursor.option == PAINT_GET_CURSOR_INDEX && 
		cursor.x >= 0) {
		// cursor.y == height) {

		int curLine = (cursor.y / fontHeight) - 1;

        int curX;
        int bestIndex = info.lineStart[curLine];
            if (ScreenSkin.RL_DIRECTION) {
                curX = curLine == 0 ? w - offset - cursor.width : w;
            } else {
                curX = curLine == 0 ? offset : 0;
            }

        int curY = cursor.y;
		int bestX = curX;


        // take one character at a time and check its position
		// against the supplied coordinates in cursor
		//
        int lineStart = info.lineStart[curLine];
        int lineEnd = info.lineEnd[curLine];


        for (int i = lineStart; i < lineEnd; i++) {

            char ch = text[i];
            if (Math.abs(curX - cursor.preferredX) <
                 Math.abs(bestX - cursor.preferredX)) {
                 bestIndex = i;
                   bestX = curX;
            }
            if (ScreenSkin.RL_DIRECTION) {
                curX -= font.charWidth(ch);
            } else {
                curX += font.charWidth(ch);
            }
        }

        if (Math.abs(curX - cursor.preferredX) <
		    Math.abs(bestX - cursor.preferredX)) {
		    bestIndex = lineEnd;
            bestX = curX;
        }

        cursor.index = bestIndex;
		cursor.x = bestX;
        // cursor.y = height;
		cursor.option = PAINT_USE_CURSOR_INDEX;
		info.cursorLine = curLine;
	    }
	}
	if (info.scrollX || info.isModified) {
	    if (cursor != null &&
		cursor.option == PAINT_USE_CURSOR_INDEX) {
		if (cursor.index >= info.lineStart[info.cursorLine] &&
		    cursor.index <= info.lineEnd[info.cursorLine]) {
		    // no change to info.cursorLine
		} else {
		    // IMPL_NOTE: start at cursorLine and search before/after
		    //      as this search is non-optimal
		    for (int i = 0; i < info.numLines; i++) {
			// we are given an index...what line is it on? 
			if (cursor.index >= info.lineStart[i] &&
			    cursor.index <= info.lineEnd[i]) {
			    info.cursorLine = i;
			    break;
			}
		    }
		}
	    }
	}
	// check scroll position and move if needed
	if (info.isModified ||info.scrollX || info.scrollY) {
	    if (info.numLines > info.visLines) {
            if (cursor != null) {
                if (info.cursorLine > info.topVis + info.visLines - 1) {
                    int diff = info.cursorLine - 
                        (info.topVis + info.visLines - 1);
                    info.topVis += diff;
                } else if (info.cursorLine < info.topVis) {
                    int diff = info.topVis - info.cursorLine;
                    info.topVis -= diff;
                }
            } else if (oldNumLines != 0) {
                info.topVis = (info.topVis * info.numLines) / oldNumLines;
            }                
            if (info.topVis + info.visLines > info.numLines) {
                info.topVis = info.numLines - info.visLines;
            }
	    } else {
            info.topVis = 0;
        }
        if (cursor != null) {
            cursor.yOffset = info.topVis * fontHeight;
        }
	}
	info.scrollX = info.scrollY = info.isModified = false;
    return true;
    }    


    /**
     * Paints text from a TextInfo structure.
     *
     * @param info the TextInfo struct
     * @param g the Graphics to paint with
     * @param str the text to paint
     * @param font the font to use in painting the text
     * @param fgColor foreground color
     * @param fgHColor foreground hilight color
     * @param w the available width for the text
     * @param h the available height for the text
     * @param offset the first line pixel offset
     * @param options any of NORMAL | INVERT | HYPERLINK | TRUNCATE
     * @param cursor text cursor object
     */
    public static void paintText(TextInfo info, Graphics g, String str, 
                  Font font, int fgColor, int fgHColor,
                  int w, int h, int offset, int options,
                  TextCursor cursor) {

    // NOTE paint not called if TextInfo struct fails
	g.setFont(font);
        g.setColor(fgColor);
	
        char[] text = str.toCharArray();
        int fontHeight = font.getHeight();
	
        if (cursor != null && cursor.visible == false) {
            cursor = null;
        }
	
	int currentLine = info.topVis;
	int height = currentLine * fontHeight;
	int y = 0;

    if (ScreenSkin.RL_DIRECTION) {
        offset = w - offset;
    }

    while (currentLine < (info.topVis + info.visLines)) {
        height += fontHeight;

        y += fontHeight;

        g.drawChars(text, info.lineStart[currentLine],
			info.lineEnd[currentLine] - info.lineStart[currentLine],
			offset, y,
			Graphics.BOTTOM | ScreenSkin.TEXT_ORIENT);

        // draw the vertical cursor indicator if needed
	    // update the cursor.x and cursor.y info
        if (cursor != null &&
		cursor.option == PAINT_USE_CURSOR_INDEX &&
		cursor.index >= info.lineStart[currentLine] &&
		cursor.index <= info.lineEnd[currentLine]) {

		int off = offset;
		if (cursor.index > info.lineStart[currentLine]) {
            if (ScreenSkin.RL_DIRECTION) {
                off -= font.charsWidth(text, info.lineStart[currentLine],
					   cursor.index -
					   info.lineStart[currentLine]);
            } else {
                off += font.charsWidth(text, info.lineStart[currentLine],
					   cursor.index -
					   info.lineStart[currentLine]);
            }
        }

		cursor.x      = off;
        cursor.y      = height;
        cursor.width  = 1;  // IMPL_NOTE: must these always be set?
		cursor.height = fontHeight;

		cursor.paint(g);
		cursor = null;
	    }

        if (ScreenSkin.RL_DIRECTION) {
            offset = w;
        } else {
            offset = 0;
        }
	    currentLine++;
	}
    }
    
    /**
     * Paints the text, linewrapping when necessary.
     *
     * @param g the Graphics to use to paint with
     * @param str the text to paint
     * @param font the font to use to paint the text
     * @param fgColor foreground color
     * @param fgHColor foreground highlight color
     * @param w the available width for the text
     * @param h the available height for the text
     * @param offset the first line pixel offset
     * @param options any of NORMAL | INVERT | HYPERLINK | TRUNCATE
     * @param cursor text cursor object to use to draw vertical bar
     * @return the width of the last line painted
     */
    public static int paint(Graphics g, String str, 
			    Font font, int fgColor, int fgHColor,
                            int w, int h, int offset, int options,
                            TextCursor cursor) {

        if (w <= 0 ||
            (cursor == null && (str == null || str.length() == 0))) {
            return 0;
        }
	
        if (str == null) {
            str = "";
        }

        g.setFont(font);
        g.setColor(fgColor);
	
        char[] text = str.toCharArray();
        int fontHeight = font.getHeight();
	
        if (cursor != null && cursor.visible == false) {
            cursor = null;
        }


        int[] inout = initGNL(font, w, h, options, offset );

        if (ScreenSkin.RL_DIRECTION) {
            offset = w - offset;
        }

        int numLines = 0;
        int height   = 0;

        do {

            numLines++;
            height += fontHeight;
	    
            if (height > h) {
                break;
            }

            inout[GNL_NUM_LINES] = numLines;

            boolean truncate = getNextLine(text, font, inout);

            int lineStart    = inout[GNL_LINE_START];
            int lineEnd      = inout[GNL_LINE_END];
            int newLineStart = inout[GNL_NEW_LINE_START];

            //
            // now we can get around to actually draw the text
            // lineStart is the array index of the first character to
            // start drawing, while lineEnd is the index just after
            // the last character to draw.
            //
            if (lineEnd > lineStart) {

                if ((options & INVERT) == INVERT) {
                    g.setColor(fgHColor);
                } else {
                    g.setColor(fgColor);
                }
                if ((options & HYPERLINK) == HYPERLINK) {
                    drawHyperLink(g, offset, height, inout[GNL_LINE_WIDTH]);
                }

                //
                // we are given x,y coordinates and we must calculate
                // the best array index to put the cursor
                //
                if (cursor != null && 
                    cursor.option == PAINT_GET_CURSOR_INDEX && 
                    cursor.x >= 0 && 
                    cursor.y == height) {
 
                    int bestIndex = lineStart;
                    int bestX = offset;
                    int curX = offset;
                    int curY = height;

                    //
                    // draw one character at a time and check its position
                    // against the supplied coordinates in cursor
                    //
                    for (int i = lineStart; i < lineEnd; i++) {

                        char ch = text[i];

                        g.drawChar(ch, curX, curY, 
                                    Graphics.BOTTOM | ScreenSkin.TEXT_ORIENT);


                        if (Math.abs(curX - cursor.preferredX) <
                            Math.abs(bestX - cursor.preferredX)) {
                            bestIndex = i;
                            bestX = curX;
                        }

                        if (ScreenSkin.RL_DIRECTION) {
                            curX -= font.charWidth(ch);
                        } else {
                            curX += font.charWidth(ch);
                        }
                    }
                    
                    if (Math.abs(curX - cursor.preferredX) <
                        Math.abs(bestX - cursor.preferredX)) {
                        bestIndex = lineEnd;
                        bestX = curX;
                    }
                    
                    //
                    // draw the ellipsis
                    //
                    if (truncate) {
                        g.drawChar(truncationMark,
                                    curX, curY,
                                    Graphics.BOTTOM | ScreenSkin.TEXT_ORIENT);
                    }

                    cursor.index = bestIndex;
                    cursor.x = bestX;
                    cursor.y = height;
                    cursor.option = PAINT_USE_CURSOR_INDEX;

                } else {
                    g.drawChars(text, lineStart, lineEnd - lineStart,
                                offset, height,
                                Graphics.BOTTOM | ScreenSkin.TEXT_ORIENT);
                                         
                    //
                    // draw the ellipsis
                    //
                    if (truncate) {
                        g.drawChar(truncationMark,
                                    offset + font.charsWidth(
                                        text, lineStart, (lineEnd - lineStart)),
                                    height,
                                    Graphics.BOTTOM | ScreenSkin.TEXT_ORIENT);
                    }
                }
            }

            //
            // try to draw a vertical cursor indicator
            //
            if (cursor != null &&
                cursor.option == PAINT_USE_CURSOR_INDEX && 
                cursor.index >= lineStart && cursor.index <= lineEnd) {
    
                int off = offset;
                if (cursor.index > lineStart) {
                    off += font.charsWidth(text, lineStart, 
                                            cursor.index - lineStart);
                }

                cursor.x      = off;
                cursor.y      = height;
                cursor.width  = 1;
                cursor.height = fontHeight;
		
                cursor.paint(g);
                cursor = null;
            }
    
            inout[GNL_LINE_START] = newLineStart;
            inout[GNL_OFFSET] = 0;
            if (ScreenSkin.RL_DIRECTION) {
                offset = w;
            } else {
                offset = 0;
            }

        } while (inout[GNL_LINE_END] < text.length);

        return inout[GNL_LINE_WIDTH];
    }

    /**
     * Draws a hyperlink image.
     *
     * @param g the graphics to use to draw the image
     * @param x the x location of the image
     * @param y the y location of the image
     * @param w the width of the hyperlink image
     */
    public static void drawHyperLink(Graphics g, int x, int y, int w) {

        if (StringItemSkin.IMAGE_LINK == null) {
            // System.err.println("Hyperlink image is null");
            return;
        }
        int linkHeight = StringItemSkin.IMAGE_LINK.getHeight();
        int linkWidth = StringItemSkin.IMAGE_LINK.getWidth();

        int oldClipX = g.getClipX();
        int oldClipW = g.getClipWidth();
        int oldClipY = g.getClipY();
        int oldClipH = g.getClipHeight();

        if (ScreenSkin.RL_DIRECTION) {
             x -= w;
        }

        g.clipRect(x, oldClipY, w, oldClipH);

        // Then, loop from the end of the string to the beginning,
        // drawing the image as we go
        for (int j = x + w - linkWidth, first = x - linkWidth; 
             j > first; j -= linkWidth) {
            g.drawImage(StringItemSkin.IMAGE_LINK, j, y,
                        Graphics.BOTTOM | ScreenSkin.TEXT_ORIENT);
        }

        g.setClip(oldClipX, oldClipY, oldClipW, oldClipH);
    }

    /**
     * Gets the height in pixels and the width of the widest line in pixels
     * for the given string, calculated based on the availableWidth.
     * size[WIDTH] and size[HEIGHT] should be set by this method.
     * @param size The array that holds Item content size and location 
     *             in Item internal bounds coordinate system.
     * @param availableWidth The width available for this Item
     * @param str the string to render
     * @param font the font to use to render the string
     * @param offset the pixel offset for the first line
     * 
     */
    public static void getSizeForWidth(int[] size, int availableWidth,
				       String str, Font font, int offset) {
        // Case 0: null or empty string, no height
        if (str == null || str.length() == 0 || availableWidth <= 0) {
	    size[HEIGHT] = 0;
	    size[WIDTH] = 0;
	    return;
        }

	char[] text = str.toCharArray();

        int[] inout = initGNL(font, availableWidth, 0, Text.NORMAL, offset);

        int numLines = 0;
        int widest = 0;
	boolean widthFound = false;

        do {

            numLines++;

            inout[GNL_NUM_LINES] = numLines;

            getNextLine(text, font, inout);


        if (!widthFound) {
		// a long line with no spaces
		if (inout[GNL_LINE_WIDTH] > availableWidth && offset == 0) {
		    widest = availableWidth;
		    widthFound = true;
		} else if (inout[GNL_LINE_WIDTH] > widest) {
		    widest = inout[GNL_LINE_WIDTH];
		}
	    }

            inout[GNL_LINE_START] = inout[GNL_NEW_LINE_START];
            inout[GNL_OFFSET] = 0;

        } while (inout[GNL_LINE_END] < text.length);

	size[WIDTH] = widest;
	size[HEIGHT] = font.getHeight() * numLines;
	// return values in size[]
    }

    /**
     * Gets the height in pixels to render the given string.
     * @param str the string to render
     * @param font the font to use to render the string
     * @param w the available width for the string
     * @param offset the pixel offset for the first line
     * @return the height in pixels required to render this string completely
     */
     // IMPL_NOTE - could remove and use getSizeForWidth()
    public static int getHeightForWidth(String str, Font font, 
                                        int w, int offset) {

	int[] tmpSize = new int[] {0, 0, 0, 0};

    getSizeForWidth(tmpSize, w, str, font, offset);
	return tmpSize[HEIGHT];
    }

    /**
     * Utility method to retrieve the length of the longest line of the 
     * text given the width. this may not necessarily be the entire 
     * string if there are line breaks or word wraps.
     *
     * @param str the String to use.
     * @param offset a pixel offset for the first line
     * @param width the available width for the text
     * @param font the font to render the text in
     * @return the length of the longest line given the width
     */
    // IMPL_NOTE - could remove and use getSizeForWidth()
    public static int getWidestLineWidth(String str, int offset,
                                         int width, Font font) {

	int[] tmpSize = new int[] {0, 0, 0, 0};

    if (ScreenSkin.RL_DIRECTION) {
        offset = width - offset;
    }

    getSizeForWidth(tmpSize, width, str, font, offset);
	return tmpSize[WIDTH];
    }

    /**
     * Calculates the starting and ending points for a new line of
     * text given the font and input parameters. Beware of the
     * multiple returns statements within the body.
     *
     * @param text text to process. this must not be null
     * @param font font to use for width information
     * @param inout an array of in/out parameters, the GNL_ constants
     *              define the meaning of each element;
     *              this array implements a structure that keeps data
     *              between invocations of getNextLine.
     * @return true if the text had to be truncated, false otherwise
     */
    private static boolean getNextLine(char[] text, Font font, int[] inout) {

        //
        // this inner loop will set lineEnd and newLineStart to 
        // the proper values so that a line is broken correctly
        //
        int     curLoc     = inout[GNL_LINE_START];
        boolean foundBreak = false;
        int     leftWidth  = 0;

        inout[GNL_LINE_WIDTH] = 0;
        int prevLineWidth     = 0;
        int curLineWidth      = 0;

        while (curLoc < text.length) {

            //
            // a newLine forces a break and immediately terminates
            // the loop
            //
            // a space will be remembered as a possible place to break
            //
            if (text[curLoc] == '\n') {            
                inout[GNL_LINE_END] = curLoc;
                inout[GNL_NEW_LINE_START] = curLoc + 1;
                inout[GNL_LINE_WIDTH] = prevLineWidth;
                return
                  (  ((inout[GNL_OPTIONS] & TRUNCATE) == TRUNCATE)
                  && ((inout[GNL_NUM_LINES] + 1) * inout[GNL_FONT_HEIGHT]
                        > inout[GNL_HEIGHT])
                  );
            // we allow \r\n as an alternative delimiter, but not \r alone
            } else if ( text[curLoc] == '\r'
                     && curLoc+1 < text.length
                     && text[curLoc+1] == '\n') {
                inout[GNL_LINE_END] = curLoc;
                inout[GNL_NEW_LINE_START] = curLoc + 2;
                inout[GNL_LINE_WIDTH] = prevLineWidth;
                return
                  (  ((inout[GNL_OPTIONS] & TRUNCATE) == TRUNCATE)
                  && ((inout[GNL_NUM_LINES] + 1) * inout[GNL_FONT_HEIGHT]
                        > inout[GNL_HEIGHT])
                  );
            } else if (text[curLoc] == ' ') {
                inout[GNL_LINE_END] = curLoc;
                inout[GNL_NEW_LINE_START] = curLoc + 1;
                inout[GNL_LINE_WIDTH] = prevLineWidth;                
                foundBreak = true;
            }

            //
            // if the text is longer than one line then we
            // cut the word at a word boundary if possible, 
            // otherwise the word is broken. 
            //

            curLineWidth = prevLineWidth + font.charWidth(text[curLoc]);
            
            // check up the mode is "truncate" and we reached the end of
            // the last line that we can put into the specifed rectangle area
            // (inout[GNL_WIDTH] x inout[GNL_HEIGHT])
            if (((inout[GNL_OPTIONS] & TRUNCATE) == TRUNCATE)
                && ((inout[GNL_NUM_LINES] + 1) * inout[GNL_FONT_HEIGHT] 
                    > inout[GNL_HEIGHT])
                && (inout[GNL_OFFSET] + curLineWidth + inout[GNL_ELLIP_WIDTH]
                    > inout[GNL_WIDTH])) {

                leftWidth =  font.charsWidth(text, curLoc + 1, 
                                             text.length - curLoc - 1);
                //
                // we are on the last line and at the point where
                // we will need to put an ellipsis if we can't fit
                // the rest of the line
                //
                // if the rest of the line will fit, then don't
                // put an ellipsis
                //
                if (inout[GNL_OFFSET] + curLineWidth + leftWidth
                    > inout[GNL_WIDTH]) {
                    
                    prevLineWidth += inout[GNL_ELLIP_WIDTH];

                    inout[GNL_LINE_END] = curLoc;
                    inout[GNL_NEW_LINE_START] = curLoc;
                    inout[GNL_LINE_WIDTH] = prevLineWidth;

                    return true;

                } else {
                
                    curLineWidth += leftWidth;

                    inout[GNL_LINE_END] = text.length;
                    inout[GNL_NEW_LINE_START] = text.length;
                    inout[GNL_LINE_WIDTH] = curLineWidth;

                    return false;
                }
            // reached the end of the line
            } else if (inout[GNL_OFFSET] + curLineWidth > inout[GNL_WIDTH]) {
                              
                if (!foundBreak) {
                    if (inout[GNL_OFFSET] > 0) {
                        // move to the next line which will have 0 offset
                        inout[GNL_LINE_END] = inout[GNL_LINE_START];
                        inout[GNL_NEW_LINE_START] = inout[GNL_LINE_START];
                        inout[GNL_LINE_WIDTH] = 0;
                    } else {
                        // the line is too long and we need to break it
                        inout[GNL_LINE_END] = curLoc;
                        inout[GNL_NEW_LINE_START] = curLoc;
                        inout[GNL_LINE_WIDTH] = prevLineWidth;
                    }
                }

                return false;
            }

            // go to next character            
            curLoc++;
            prevLineWidth = curLineWidth;
            
        } // while end

        // we reach this code only if we reach the end of the text
        inout[GNL_LINE_END] = text.length;
        inout[GNL_NEW_LINE_START] = text.length;
        inout[GNL_LINE_WIDTH] = curLineWidth;
                
        return false;
    }

    /**
     * Utility method to calculate the width and height in which 2 
     * strings can fit  given the strings, fonts and maximum width 
     * in which those strings should fit. Returned width is either 
     * the passed in width or a smaller one.
     * The offset in pixels for the first string is 0, second string is 
     * laid out right after the first one with padding in between 
     * equal to the passed in value.
     * 
     * The width in which both strings would fit given the maximum 
     * is returned in size[WIDTH].  The height in which both strings
     * would fit is returned in size[HEIGHT];
     *
     * @param size The array that returns contents size
     * @param firstStr the first string to use.
     * @param secondStr the first string to use.
     * @param width the available width for the text
     * @param firstFont the font to render the first string in
     * @param secondFont the font to render the second string in
     * @param pad the horizontal padding that should be used between strings
     */
    public static void getTwoStringsSize(int[] size, String firstStr,
					 String secondStr, Font firstFont,
					 Font secondFont, int width, int pad) 
    {
	if (((firstStr == null || firstStr.length() == 0) &&
             (secondStr == null || secondStr.length() == 0)) ||
	    (width <= 0)) {
            size[WIDTH] = size[HEIGHT] = 0;
	    return;
        }
	
        int[] inout = new int[GNL_NUM_PARAMS];
	
        char[] text; 
	
        int offset = 0;
        int widest = 0;
        int numLines = 0;
	int height = 0;
	int fontHeight = 0;
	
        if (firstStr != null && firstStr.length() > 0) {

            text = firstStr.toCharArray();

            fontHeight = firstFont.getHeight();

	    inout = initGNL(firstFont, width, 0, Text.NORMAL, 0);

            do {
                
                numLines++;
                height += fontHeight;
		
                inout[GNL_NUM_LINES] = numLines;
                
                getNextLine(text, firstFont, inout);
                
                if (inout[GNL_LINE_WIDTH] > widest) {
                    widest = inout[GNL_LINE_WIDTH];
                }
		
		
                inout[GNL_LINE_START] = inout[GNL_NEW_LINE_START];
                
            } while (inout[GNL_LINE_END] < firstStr.length());
	    
            offset = inout[GNL_LINE_WIDTH];

            if (secondStr == null || secondStr.length() == 0) {
                // last \n in the two strings should be ignored
                if (firstStr.charAt(firstStr.length() - 1) == '\n') {
                    height -= fontHeight;
                }
                size[HEIGHT] = height;
		size[WIDTH] = widest;
		return;
            }
        }
        // Second string is not null and it is not empty
        if (secondStr != null && secondStr.length() > 0) {
            if (offset > 0) {
                offset += pad;
            }

            text = secondStr.toCharArray();

            fontHeight = secondFont.getHeight();

            // Line that has the end of the first string and the beginning
            // of the second one is a special one;
            // We have to make sure that it is not counted twice and that
            // the right font height is being added (the max of the two)
            if (numLines > 0) {
                numLines--;
                if (inout[GNL_FONT_HEIGHT] > fontHeight) {
                    height -= fontHeight;
                } else {
                    height -= inout[GNL_FONT_HEIGHT];
                }
            }
	    
	    inout = initGNL(secondFont, width, 0, Text.NORMAL, offset);

            do {
                numLines++;
                height += fontHeight;

                inout[GNL_NUM_LINES] = numLines;
                
                getNextLine(text, secondFont, inout);

                if (inout[GNL_OFFSET] + inout[GNL_LINE_WIDTH] > widest) {
                    widest = inout[GNL_OFFSET] + inout[GNL_LINE_WIDTH];
                }
                                
                inout[GNL_LINE_START] = inout[GNL_NEW_LINE_START];
                inout[GNL_OFFSET] = 0;
                
            } while (inout[GNL_LINE_END] < secondStr.length());

            // last \n should be ignored
            if (secondStr.charAt(secondStr.length() - 1) == '\n') {
                height -= fontHeight;
            }
        }

	size[WIDTH] = widest;
	size[HEIGHT] = height;
	return;
    }

    // IMPL_NOTE:  remove these - there must be a common place to get them
    /** Used as an index into the size[], for the x. */
    public final static int X      = 0;

    /** Used as an index into the size[], for the y. */
    public final static int Y      = 1;

    /** Used as an index into the size[], for the width. */
    public final static int WIDTH  = 2;
    
    /** Used as an index into the size[], for the height. */
    public final static int HEIGHT = 3;

    /**
     *
     *
     * @param g the Graphics to paint with
     * @param text the string to be painted
     * @param font the font to be used
     * @param fgColor foreground text color
     * @param shdColor shadow color
     * @param shdAlign shadow alignment
     * @param titlew width

     */
    public static void drawTruncStringShadowed(Graphics g, String text, Font font,
                                               int fgColor, int shdColor, int shdAlign,
                                               int titlew) {
        int dx=1, dy=1;
        // draw the shadow
        if (shdColor != fgColor) {
            switch (shdAlign) {
                case (Graphics.TOP | Graphics.LEFT):
                    dx=-1;
                    dy=-1;
                    break;
                case (Graphics.TOP | Graphics.RIGHT):
                    dx=1;
                    dy=-1;
                    break;
                case (Graphics.BOTTOM | Graphics.LEFT):
                    dx=-1;
                    dy=1;
                    break;
                case (Graphics.BOTTOM | Graphics.RIGHT):
                default:
                    dx=1;
                    dy=1;
                    break;
            }
            g.translate(dx, dy);
            drawTruncString(g, text, font,
                    shdColor, titlew);
/* if we wanted multi-line text output, we would use this:
            paint(g, text, font,
                    shdColor, 0,
                    titlew, titleh, 0,
                    TRUNCATE, null);
*/
            g.translate(-dx, -dy);
        }
        // now draw the text whose shadow we have drawn above
        drawTruncString(g, text, font,
                fgColor, titlew);
/* if we wanted multi-line text output, we would use this:
        paint(g, text, font,
                fgColor, 0,
                titlew, titleh, 0,
                TRUNCATE, null);
*/
    }

    /**
     * Given a string, determine the length of a substring that can be drawn
     * within the current clipping area.
     * If the whole string fits into the clip area,
     * return the length of the string.
     * Else, return the length of a substring (starting from the beginning
     * of the original string) that can be drawn within the current clipping
     * area before the truncation indicator.
     * The truncation indicator, typically, ellipsis, is not included into
     * the returned length.
     *
     * @param g the Graphics to paint with
     * @param str the string to be painted
     * @param width the available width, including room
     *          for the truncation indicator
     * @return either the length of str (if it fits into the clip area),
     *          or the length of the substring that can fit into the clip area
     *          (not including the truncation mark)
     */
    public static int canDrawStringPart(Graphics g, String str,
                                 int width) {
        if (width < 0) {
            return 0;
        }
        final Font font = g.getFont();
        final int stringWidth = font.stringWidth(str);

        if (width >= stringWidth) {
            return str.length();
        }
        final int widthForTruncatedText = width - font.charWidth(truncationMark);
        int availableLength;
        for (availableLength = str.length() - 1 ;
             font.substringWidth(str,0,availableLength) > widthForTruncatedText;
             availableLength-- ) {};
        return availableLength;
    }

    /**
     * Draw the string within the specified width.
     * If the string does not fit in the available width,
     * it is truncated at the end,
     * and a truncation indicator is displayed (usually,
     * an ellipsis, but this can be changed).
     * Use Graphics.translate(x,y) to specify the anchor point location
     * (the alignment will be TOP|LEFT relative to 0,0).
     *
     * @param g the Graphics to paint with
     * @param str the string to be painted
     * @param font the font to be used
     * @param fgColor the color to paint with
     * @param width the width available for painting
     */
    public static void drawTruncString(Graphics g, String str,
                                       Font font, int fgColor, int width) {
        int offset = 0;
        if (ScreenSkin.RL_DIRECTION) {
            offset = width - offset;
        }
        g.setFont(font);
        g.setColor(fgColor);
        int lengthThatCanBeShown = canDrawStringPart(g, str, width);
        if (lengthThatCanBeShown == str.length()) {
            g.drawString(str, offset, 0, Graphics.TOP | ScreenSkin.TEXT_ORIENT);
        } else {
            String s = str.substring(0,lengthThatCanBeShown) + truncationMark;
            g.drawString(s, offset, 0, Graphics.TOP | ScreenSkin.TEXT_ORIENT);
        }
    }

}

