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

package com.sun.cldc.i18n.j2me;

import java.io.Reader;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/** Reader for Little Endian UTF-16 encoded input streams. */
public class UTF_16LE_Reader extends UTF_16_Reader {

    /** Constructs a UTF-16LE reader. */
    public UTF_16LE_Reader() {
        super();
        bytesForBOM = 0;
    }

    /**
     * Open the reader
     * @param in the input stream to be read
     * @param enc identifies the encoding to be used
     * @return a reader for the given input stream and encoding
     * @throws UnsupportedEncodingException
     */
    public Reader open(InputStream in, String enc)
        throws UnsupportedEncodingException {
        super.open(in, enc);
        byteOrder = LITTLE_ENDIAN; // AFTER the constructor
        return this;
    }
    /*
     * This method is only used by our internal Helper class in the method
     * byteToCharArray to know how much to allocate before using a
     * reader. If we encounter bad encoding we should return a count
     * that includes that character so the reader will throw an IOException
     */
    public int sizeOf(byte[] array, int offset, int length) {
        return length/BYTES_PER_CHAR;
    }
}
