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

/**
 * Class that represents the line-wrapping and scroll position of 
 * text in a TextBox (editable) or Label, StringItem, or ListItem(uneditable)
 *
 * From this structure, <code>Text.paintText()</code> should be able to 
 * quickly render wrapped text 
 */
public class TextInfo {

    /** total number of lines */
    public int numLines;
    
    /** number of visible lines */
    public int visLines;      

    /** first visible line */
    public int topVis;
    
    /** the line where the cursor resides */
    public int cursorLine;
    
    /** set to true to indicate this has been modified */
    public boolean isModified;

    /** set to true if this has been scrolled in the Y direction */
    public boolean scrollY;

    /** set to true if this has been scrolled in the X direction */
    public boolean scrollX;

    /** starting offset of each line */
    public int[] lineStart;

    /** offset of last character of each line */
    public int[] lineEnd;

    /** the height of the block of text described by this object */
    public int height;

    /** scroll up */
    public final static int BACK = 1;

    /** scroll down */
    public final static int FORWARD = 2;

    /**
     * Construct a new TextInfo object with <code>size</code> 
     * lines initially
     * 
     * @param size maximum number of lines this TextInfo
     *        struct can store without expanding 
     */
    public TextInfo(int size) {
	isModified = true;
	scrollY = true;
	scrollX = true;
	lineStart = new int[size];
	lineEnd = new int[size];
    }

    /**
     * Expand the capacity of this TextInfo structure by doubling the
     * length of the lineStart and lineEnd arrays
     */
    public void expand() {
	int[] tmpStart = new int [lineStart.length * 2];
	int[] tmpEnd = new int [tmpStart.length];

	System.arraycopy(lineStart, 0, tmpStart, 0, 
			 lineStart.length);
	System.arraycopy(lineEnd, 0, tmpEnd, 0, 
			 lineEnd.length);
	
	lineStart = tmpStart;
	lineEnd = tmpEnd;
    }


    /**
     * Scroll Up or down by one line if possible
     *
     * @param dir direction of scroll, FORWARD or BACK
     * @return true if scrolling happened, false if not
     */
    public boolean scroll(int dir) {
        return scroll(dir, 1);
    }
    
    /**
     * Scroll Up or down by one line if possible
     *
     * @param dir direction of scroll, FORWARD or BACK
     * @param length how many lines to scroll    
     * @return true if scrolling happened, false if not
     */
    public boolean scroll(int dir, int length) {
	boolean rv = false;
	
	if (visLines < numLines) {
	    switch (dir) {
	    case FORWARD:
                if (topVis + visLines < numLines) {
                    topVis += length;
                    if (topVis + visLines > numLines) {
                        topVis = numLines - visLines;
                    }
                    rv = true;
                }
		break;
	    case BACK:
		if (topVis > 0) {
		    topVis -= length;
                    if (topVis < 0) {
                        topVis = 0;
                    }
		    rv = true;
		}
		break;
	    default:
		// no-op
	    }
	}
        scrollY |= rv;
	return rv;
    }
    

    /**
     * Scroll Up or down by page if possible
     *
     * @param dir direction of scroll, FORWARD or BACK
     * @return number of scrolled lines
     */
    public int scrollByPage(int dir) {
	int oldTopVis = topVis;
            
	if (visLines < numLines) {
	    switch (dir) {
	    case FORWARD:
		if ((topVis + visLines) < numLines) {
                    topVis = numLines - (topVis + visLines - 1) < visLines ?
                        numLines - visLines : topVis + visLines - 1;
		}
		break;
	    case BACK:
		if (topVis > 0) {
                    topVis = (topVis - visLines + 1) < 0 ?
                        0 : topVis - visLines + 1;
		}
		break;
	    default:
		// no-op
	    }
	}
        scrollY |= (topVis != oldTopVis);
	return topVis - oldTopVis;
    }

    /**
     * Returns scroll position from 0-100
     * @return scroll position mapped to the range 0-100
     */
    public int getScrollPosition() {
        // used to set scroll indicator visibility
        if (numLines == 0 || numLines <= visLines) {
            return 0;
        } else {
            return (topVis * 100) / (numLines - visLines);
        }
    }

    /**
     * Returns scroll proportion from 0-100
     * @return scroll proportion, as a percentage of the screen that
     *         is viewable.
     */
    public int getScrollProportion() {
        // used to set scroll indicator visibility
        if (visLines >= numLines || numLines == 0) {
            return 100;
        } else {
            return (visLines * 100) / numLines;
        }
    }
}    


