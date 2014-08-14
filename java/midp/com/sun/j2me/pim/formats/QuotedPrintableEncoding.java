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

import com.sun.j2me.main.Configuration;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Interprets the Quoted-Printable encoding.
 * The Quoted-Printable Encoding is defined in RFC 2045.
 *
 */
public class QuotedPrintableEncoding {

    /**
     * Converts a quoted-printable string to a byte array.
     * @param sdata input data to be converted
     * @return processed data from quoted printable
     */
    public static byte[] fromQuotedPrintable(String sdata) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream whitespaceAccumulator =
            new ByteArrayOutputStream();
        char[] data = sdata.toCharArray();
        String lb = Configuration.getProperty("file.linebreak");
        if (lb == null) {
            lb = "\r";
        }
        byte[] linebreak = lb.getBytes();
        boolean followingEqualsSign = false;
        for (int i = 0; i < data.length; i++) {
            char currSym = data[i];
            if (followingEqualsSign) {
                switch (currSym) {
                    case '\r':
                        // RFC 2045: check for soft break (=CRLF)
                        if (i + 1 < data.length) {
                            if (data[i + 1] == '\n') {
                                // concatenate lines
                                i++;
                                break;
                            }
                        }
                    default: {
                        // inlined byte
                        if (i < data.length - 1) {
                            String charCode = sdata.substring(i, i + 2);
                            i ++;
                            try {
                                out.write(Integer.parseInt(charCode, 16));
                                break;
                            } catch (NumberFormatException nfe) {
                                // illegal data. write the data as is
                                out.write('=');
                                out.write(charCode.charAt(0));
                                out.write(charCode.charAt(1));
                            }
                        } else {
                            // '=' is the penultimate character, which is also
                            // illegal. write the data as is
                            out.write('=');
                            out.write(currSym);
                        }
                    }
                }
                followingEqualsSign = false;
            } else {
                try {
                    /**
                     * RFC 2045: Control characters other than TAB, or CR and LF
                     * as parts of CRLF pairs, must not appear. The same is true
                     * for octets with decimal values greater than 126.  If
                     * found in incoming quoted-printable data by a decoder, a
                     * robust implementation might exclude them from the
                     * decoded data.
                     */
                    if (((currSym < ' ') &&
                        (currSym != '\t') &&
                        (currSym != '\r')) ||
                        (currSym > 126)) { // ignore
                        continue;
                    }
                    switch (currSym) {
                        case '=': {
                            out.write(whitespaceAccumulator.toByteArray());
                            whitespaceAccumulator.reset();
                            followingEqualsSign = true;
                            break;
                        }
                        case '\t':
                        case ' ':
                            whitespaceAccumulator.write(currSym);
                            break;
                        case '\r': // must be a part of CRLF (RFC 2045)
                            if (i + 1 < data.length) {
                                if (data[i + 1] == '\n') {
                                    whitespaceAccumulator.reset();
                                    out.write(linebreak, 0, linebreak.length);
                                    i ++; // skip LF
                                }
                            }
                            break;
                        default: {
                            out.write(whitespaceAccumulator.toByteArray());
                            whitespaceAccumulator.reset();
                            out.write(currSym);
                        }
                    }
                } catch (IOException e) {
                    // the compiler claims that an IOException can occur
                    // on a call to toByteArray(). The javadocs and
                    // the ByteArrayOutputStream class file claim otherwise.

                }
            }
        }
        try {
            out.write(whitespaceAccumulator.toByteArray());
        } catch (IOException e) {
            // same story as above.
        }
        return out.toByteArray();
    }

}
