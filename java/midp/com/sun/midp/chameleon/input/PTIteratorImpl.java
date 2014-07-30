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

package com.sun.midp.chameleon.input;

import com.sun.midp.io.Util;
import javax.microedition.lcdui.Canvas;
import com.sun.midp.i18n.Resource;
import com.sun.midp.i18n.ResourceConstants;
import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;

import java.util.Vector;


/** 
 * Implements JAVA PTIterator using the simple logic.
 *
 */
public class PTIteratorImpl implements PTIterator {
    /** buffer accomulating user input */ 
    private StringBuffer buffer;

    /** selected option from the list */ 
    private int selected;

    /** symbols mapped to the digital keys get from resources */ 
    private char[][] keyMap;

    /** list of matches cached until word is increased/decreased */ 
    private String[] list;

    /** storage accomulating all word states until it's committed by the user */
    private Vector states = new Vector();

    /** 
     * Create a new iterator
     */
    public PTIteratorImpl() {
        buffer = new StringBuffer();
        String lowerInLine = Resource.getString(
                          ResourceConstants.LCDUI_TF_ALPHA_KEY_MAP);
        keyMap = getMapByLine(lowerInLine);
    }


    /**
     * Converts the string to key map. The rows are separated each from other
     * by '$'. The characters inside of one row follow each to other without
     * any separator.
     * @param line string combines all keys
     * @return map of the keys in char[][] format
     */
    private char[][] getMapByLine(String line) {
        char[] chars = line.toCharArray();
        int rows = 1;
        for (int i = chars.length - 1; i >= 0; i--) {
            if (chars[i] == '$') rows++;
        }

        char[][] map = new char[rows][];
        for (int start = 0, j = 0; start < chars.length; j++) {
            int end = -1;
            for (int k = start; k < chars.length && end == -1; k++) {
                if (chars[k] == '$') {
                    end = k;
                }
            }
            
            // if '$' is not found that means the end of string is reached
            if (end == -1) end = chars.length;
            map[j] = new char[end - start];
            System.arraycopy(chars, start, map[j], 0, map[j].length);
            start = end + 1;
        }
        return map;
    }    
    
    /** 
     * Clear completion state
     */
    public void reset() {
        if (buffer.length() > 0) {
            buffer.delete(0, buffer.length());
        }
        states.removeAllElements();
        selected = 0;
        list = null;
    }
    
    /** 
     * Adds a key to current completion string
     * @param keyCode char in the range '0'-'9', '#', or '*'
     */
    public void nextLevel(int keyCode) {
        String[] ls = getList();
        if (ls != null && ls.length > 0) {
            states.addElement(ls[selected > 0 ? selected - 1 : 0]);
        }
        buffer.append((char)keyCode);
        selected = 0;
        list = null;
    }
    
    /** 
     * Backspace on key in current completion string.
     */
    public void prevLevel() {
        if (states.size() > 0) {
            states.removeElementAt(states.size() - 1);
        }
        if (buffer.length() > 0) {
            buffer.deleteCharAt(buffer.length() - 1);
        }
        selected = 0;
        list = null;
    }
    
    /**
     * Returns true if the iteration has more elements. (In other words,
     * returns <code>true</code> if <code>next</code> would return an
     * element rather than throwing an exception.)
     *
     * @return true if the iterator has more elements.
     */
    public boolean hasNext() {
        boolean ret = false;
        String[] ls = getList();
        if (ls != null && ls.length > 0) {
            ret = selected < ls.length;
        }
        return ret;
    }
    
    /** 
     * Reverts to first possible completion.
     * If next() has been called uptil hasNext() returns false, then after 
     * calling reviewCompletionOptions(), calling next() will return
     * the 1st completion
     */
    public void resetNext() {
        selected = 0;
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return next element in the iteration.
     *
     * @exception java.util.NoSuchElementException iteration has no more elements.
     */
    public String next() {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                "[iter.nextCompletionOption] >>");
        }
        String ret = null;

        ret = getList()[selected++];

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                "[iter.next] : " + ret);
        }
        return ret;
    }

    /**
     * Get the list of matches. The list is cached until the word is decreased/
     * increased by the user.
     * @return the list of matches. Returns null if no matches are found
     */
    private String[] getList() {
        if (list == null && buffer.length() > 0) {
            char[] next = getCharOptions(buffer.charAt(buffer.length() - 1));
            list = new String[next.length];

            String base = new String();
            if (states.size() > 0) {
                base = (String)(states.elementAt(states.size() - 1));
            } 
            for (int j = 0; j < next.length; j++) {
                list[j] = base + next[j];
                if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                    Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
                        "[getList] next = " + list[j]);
                }
            }
        }
        return list;
    }


    /**
     * Gets the possible matches for the key code
     *
     * @param lastKey the key code
     *
     * @return returns the set of options. Return null if matches are not found.
     */
    protected char[] getCharOptions(int lastKey) {
        char[] chars = null;
    
        switch (lastKey) {
        case Canvas.KEY_NUM0:
            chars = keyMap[0];
            break;          
        case Canvas.KEY_NUM1:
            chars = keyMap[1];
            break;
        case Canvas.KEY_NUM2:
            chars = keyMap[2];
            break;
        case Canvas.KEY_NUM3:
            chars = keyMap[3];
            break;
        case Canvas.KEY_NUM4:
            chars = keyMap[4];
            break;
        case Canvas.KEY_NUM5:
            chars = keyMap[5];
            break;
        case Canvas.KEY_NUM6:
            chars = keyMap[6];
            break;
        case Canvas.KEY_NUM7:
            chars = keyMap[7];
            break;
        case Canvas.KEY_NUM8:
            chars = keyMap[8];
            break;
        case Canvas.KEY_NUM9:
            chars = keyMap[9];
            break;
        case Canvas.KEY_POUND:
            chars = keyMap[10];
            break;
        case Canvas.KEY_STAR:
            break;
                
        default:
            // This can actually happen if the Timer went off without
            // a pending key, which can sometimes happen.
            break;

        }
       
        return chars;
    }
}
