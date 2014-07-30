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

package com.sun.midp.installer;

import java.io.*;
import java.util.*;

/**
 * Handles the properties in a JAR manifest. Each property is know as a header
 * in the syntax definition. A line that begins with a space is a continuation
 * of the previous line, and the first space ignored. To save code this
 * manifest parser uses lets its super class parse the key and value
 * of property once it has read the entire line, including continuation lines.
 * This means that this parser is lax on reporting illegal characters.
 * <p>
 * The BNF for the manifest syntax included below is extended as follows:
 * <pre>
 *    ":":separates the name and value of a rule
 *    +: 1 or more
 *    *: 0 or more
 *    {}: encloses a list of alternatives
 *    ;: comment
 *    terminals can also be represented in UPPER CASE
 *    non-terminals are not encluded in angle brackets
 * </pre><p>
 * Syntax definition from the manifest spec:
 * <p>
 * In most cases, information contained within the manifest file or
 * signature files is represented as so-called "name: value" pairs
 * inspired by the RFC822 standard.
 * <p>
 * Groups of name-value pairs are known as a "section". Sections are
 * separated from other sections by empty lines. 
 * <p>
 * Binary data of any form is represented as base64. Continuations are
 * required for binary data which causes line length to exceed 72
 * bytes. Examples of binary data are digests and signatures.
 * <p>
 * Implementations shall support header values of up to 65535 bytes.
 * <pre>
 *  section: *header +newline
 *  nonempty-section: +header +newline
 *  newline: CR LF | LF | CR (not followed by LF)
 *
 *  ; That 'not followed by LF' probably requires some minor
 *  ; ugliness in the parser. Sorry.
 *
 *  header: alphanum *headerchar ":" SPACE *otherchar newline
 *          *continuation
 *
 *  continuation: SPACE *otherchar newline
 *
 *  ; RFC822 has +(SPACE | TAB), but this way continuation lines 
 *  ; never get mangled.
 *
 *  alphanum: {"A"-"Z"} | {"a"-"z"} | {"0"-"9"}
 *
 *  headerchar: alphanum | "-" | "_"
 *
 *  otherchar: any Unicode character except NUL, CR and LF
 *
 *  ; Also: To prevent mangling of files sent via straight e-mail, no 
 *  ; header will start with the four letters "From".
 *
 *  ; When version numbering is used:
 *
 *  number: {"0"-"9"}+
 *
 *  ; The number needn't be, e.g. 1.11 is considered to be later
 *  ; than 1.9. Both major and minor versions must be 3 digits or less.
 *</pre>
 */
public class ManifestProperties extends JadProperties {
    /** Signals that there is no remainder. */
    protected static final int NO_REMAINDER = -2;

    /** Holds the remainder from the last look ahead operation. */
    protected int remainder = NO_REMAINDER;

    /**
     * Constructor - creates an empty property list.
     */
    public ManifestProperties() {
    }
    /**
    * Read a portion of the manifest.
    * @param inStream the current data source for the manifest
    * @param enc the character set encoding of the manifest 
    * @param propertiesToLoad the names of the properties to load
    * @exception IOException is thrown if an error occurs reading the 
    * manifest data stream.
    * @exception  InvalidJadException if the JAD is not formatted correctly.
    */
    public void partialLoad(InputStream inStream, String enc,
                             int propertiesToLoad) throws IOException,
            InvalidJadException {
        // reset any leftover remainder
        remainder = NO_REMAINDER;
        super.partialLoad(inStream, enc, propertiesToLoad);
    }

    /**
     * Reads one line using a given reader. CR, LF, or CR + LF end a line.
     * However lines may be continued by beginning the next line with a space.
     * The end of line and end of file characters and continuation space are
     * dropped.
     * @param in reader for a JAD
     * @return one line of the JAD or null at the end of the JAD
     * @exception IOException thrown by the reader
     */
    protected String readLine(Reader in) throws IOException {
        int lastChar = 0;
	int room;
	int offset = 0;
	int c;
        char[] temp;

	room = lineBuffer.length;

        if (remainder != NO_REMAINDER) {
            c = remainder;
            remainder = NO_REMAINDER;
        } else {
            c = in.read();
        }

        for (; c != -1; lastChar = c, c = in.read()) {
            /*
             * if we read the end of the line last time and the next line
             * does not begin with a space we are done. But save this character
             * for next time. CR | LF | CR LF ends a line.
             */
            if (lastChar == LF) {
                if (c == SP) {
                    // Marks a continuation line, throw away the space
                    continue;
                }
                    
                remainder = c;
                break;
            }

            if (lastChar == CR) {
                if (c == SP) {
                    // Marks a continuation line, throw away the space
                    continue;
                }

                if (c != LF) {
                    remainder = c;
                    break;
                }
            }

            /*
             * do not include the end of line characters and the end
             * of file character.
             */
            if (c == CR || c == LF || c == EOF) {
                continue;
            }

            if (--room < 0) {
                temp = new char[offset + 128];
                room = temp.length - offset - 1;
                System.arraycopy(lineBuffer, 0, temp, 0, offset);
                lineBuffer = temp;
            }

            lineBuffer[offset++] = (char) c;
	}

	if ((c == -1) && (offset <= 0)) {
	    return null;
	}

        return new String(lineBuffer, 0, offset);
    }

    /**
     * Check to see if all the chars in the key of a property are valid.
     *
     * @param key key to check
     *
     * @return false if a character is not valid for a key
     */
    protected boolean checkKeyChars(String key) {
        char[] temp = key.toCharArray();
        int len = temp.length;

        for (int i = 0; i < len; i++) {
            char current = temp[i];

            if (current >= 'A' && current <= 'Z') {
                continue;
            }

            if (current >= 'a' && current <= 'z') {
                continue;
            }

            if (current >= '0' && current <= '9') {
                continue;
            }

            if (i > 0 && (current == '-' || current == '_')) {
                continue;
            }

            return false;
        }

        return true;
    }

    /**
     * Check to see if all the chars in the value of a property are valid.
     *
     * @param value value to check
     *
     * @return false if a character is not valid for a value
     */
    protected boolean checkValueChars(String value) {
        char[] temp = value.toCharArray();
        int len = temp.length;

        // assume whitespace and new lines are trimmed
        for (int i = 0; i < len; i++) {
            if (temp[i] == 0) {
                return false;
            }
        }

        return true;
    }
}
