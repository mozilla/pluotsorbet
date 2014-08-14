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

package com.sun.j2me.pim.formats;

import com.sun.j2me.pim.LineReader;

/**
 * Implementation of LineReader.Matcher that matches a case insensitive line
 * of the form "end\w*:\w*${param}", where ${param} is the argument to
 * EndMatcher's constructor.
 */
public class EndMatcher implements LineReader.Matcher {
    /** Original pattern for matching. */
    private final char[] parameter;
    /** Inverted case of original pattern string. */
    private final char[] parameter2;
    /**
     * Constructs an end matcher.
     * @param s pattern to match
     */    
    public EndMatcher(String s) {
        this.parameter = s.toCharArray();
        // make parameter2 have the opposite case in every character
        // to parameter
        this.parameter2 = new char[parameter.length];
        int delta = 'a' - 'A';
        for (int i = 0; i < parameter.length; i++) {
            char c = parameter[i];
            if (c >= 'A' && c <= 'Z') {
                c += delta;
            } else if (c >= 'a' && c <= 'z') {
                c -= delta;
            }
            parameter2[i] = c;
        }
    }
    /**
     * Matches string pattern.
     * @param sb input buffer for matching
     * @return <code>true</code> if matches
     */    
    public boolean match(StringBuffer sb) {
        int length = sb.length();
        // does the string start with 'end' ?
        int index = -1;
        int stopIndex = length - parameter.length - 3;
        for (int i = 0; i < stopIndex && index == -1; i++) {
            switch (sb.charAt(i)) {
                case ' ':
                case '\t':
                    continue;
                case 'e':
                case 'E':
                    switch (sb.charAt(i + 1)) {
                        case 'n':
                        case 'N':
                            switch (sb.charAt(i + 2)) {
                                case 'd':
                                case 'D':
                                    index = i + 3;
                                    break;
                                default:
                                    return false;
                            }
                            break;
                        default:
                            return false;
                    }
                    break;
                default:
                    return false;
            }
        }
        if (index == -1) {
            return false;
        }
        boolean foundColon = false;
        stopIndex = length - parameter.length + 1;
        while (index < stopIndex) {
            switch (sb.charAt(index)) {
                case ':':
                    if (foundColon) {
                        return false;
                    } else {
                        foundColon = true;
                    }
                case ' ':
                case '\t':
                    break;
                default:
                    if (index != stopIndex - 1) {
                        return false;
                    }
                    char[] cs = new char[parameter.length];
                    sb.getChars(index, index + parameter.length, cs, 0);
                    for (int i = 0; i < cs.length; i++) {
                        char c = cs[i];
                        if (c != parameter[i] && c != parameter2[i]) {
                            return false;
                        }
                    }
                    return true;
            }
            index ++;
        }
        return false;
    }
    
}
