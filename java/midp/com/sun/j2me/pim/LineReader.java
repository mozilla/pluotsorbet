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

package com.sun.j2me.pim;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * Reader that knows how to read non-blank lines. Line may be concatenated
 * according to section 2.1.3 of the vCard 2.1 specification; CRLF followed by
 * an LWSP character is treated as only the LWSP character.
 *
 * <p>The line terminator is taken as CRLF.
 *
 */
public class LineReader extends InputStreamReader {
    /** Input stream. */
    private final InputStream in;
    /** Matcher function. */
    private final Matcher matcher;

    /**
     * Constructs a line reader handler.
     *
     * @param in an InputStream that must support mark()
     * @param encoding character encoding of input stream
     * @param matcher filter function
     * @throws UnsupportedEncodingException if encoding is not
     * available on the current platform
     */
    public LineReader(InputStream in, String encoding, Matcher matcher)
        throws UnsupportedEncodingException {
        super(in, encoding);

        this.in = new MarkableInputStream(in);
        this.matcher = matcher;
    }

    /**
     * Reads a non-blank line.
     *
     * @return a line of text (without line terminators)
     * or null if no more lines are available
     * @throws IOException if a read error occurs
     */
    public String readLine() throws IOException {
        StringBuffer sb = new StringBuffer();
        boolean lineIsOnlyWhiteSpace = true;
        boolean done = false;
        for (int i = in.read(); i != -1 && !done; ) {
            switch (i) {
                case '\r': {
                    // start of a new line. follow through and see if
                    // it is really a new line
                    i = in.read();
                    if (i != '\n') {
                        throw new IOException("Bad line terminator");
                    }
                    // fall through
                }
                // be generous and accept '\n' alone as a new line.
                // this is against the vCard/vCalendar specifications, but
                // appears to be common practice.
                case '\n': {
                    // lines with only whitespace are treated as empty lines
                    if (lineIsOnlyWhiteSpace) {
                        sb.setLength(0);
                    }
                    in.mark(1);
                    i = in.read();
                    reset();
                    switch (i) {
                        case ' ':
                        case '\t':
                            // append this line to the previous one
                            in.skip(1);
                            break;
                        default:
                            // end of the line
                            if (!lineIsOnlyWhiteSpace) {
                                // return this line
                                done = true;
                            } else {
                                in.skip(1);
                                // read another line and hope it contains
                                // more than white space
                            }
                            break;
                    }
                    break;
                }
                case ' ':
                case '\t':
                    sb.append((char) i);
                    i = in.read();
                    break;
                default:
                    sb.append((char) i);
                    if (matcher.match(sb)) {
                        return sb.toString().trim();
                    }
                    i = in.read();
                    lineIsOnlyWhiteSpace = false;
            }
        }
        if (lineIsOnlyWhiteSpace) {
            return null;
        } else {
            return sb.toString().trim();
        }
    }

    /**
     * Sets marker in input stream.
     * @param lookahead offset to peek ahead
     * @throws IOException if any read ahead error occurs
     */
    public void mark(int lookahead) throws IOException {
        in.mark(lookahead);
    }

    /**
     * Checks if mark is supported.
     * @return <code>true</code> if mark is supported
     */
    public boolean markSupported() {
        return true;
    }

    /**
     * Reset the line markers.
     * @throws IOException if an error occurs accessing the
     * input stream
     */
    public void reset() throws IOException {
        in.reset();
    }


    /**
     * Inner interface for matching function.
     */
    public static interface Matcher {
        /**
         * Matches input string buffer.
         * @param sb pattern string to match
         * @return <code>true</code> if string matches.
         */
        public boolean match(StringBuffer sb);
    }
}
