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

import com.sun.cldc.i18n.StreamReader;

import java.io.*;

/** Reader for UTF-8 encoded input streams. */
public class UTF_8_Reader extends StreamReader {
    boolean initialized = false;

    /** Constructs a UTF-8 reader. */
    public UTF_8_Reader() {
    }

    public Reader open(InputStream in, String enc)
        throws UnsupportedEncodingException {
        super.open(in, enc);
        return this;
    }

    private native void init(byte[] bytes);

    /**
     * Read a block of UTF8 characters.
     *
     * @param cbuf output buffer for converted characters read
     * @param off initial offset into the provided buffer
     * @param len length of characters in the buffer
     * @return the number of converted characters
     * @exception IOException is thrown if the input stream 
     * could not be read for the raw unconverted character
     */
    public native int readNative(char cbuf[], int off, int len);
    public int read(char cbuf[], int off, int len) throws IOException {
        if (!initialized) {
            // Read the whole stream in memory
            byte[] buffer = new byte[1024];
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try {
                int numRead = 0;
                while ((numRead = in.read(buffer)) > -1) {
                    output.write(buffer, 0, numRead);
                }
                output.flush();
            } catch (Exception e) {
                throw new UnsupportedEncodingException("Failed to read from the stream");
            }

            init(output.toByteArray());

            initialized = true;
        }

        return readNative(cbuf, off, len);
    }

    /**
     * Mark the present position in the stream.
     *
     * @param readAheadLimit number of characters to buffer ahead
     * @exception  IOException  If an I/O error occurs or
     *             marking is not supported by the underlying input stream.
     */
    public void mark(int readAheadLimit) throws IOException {
        throw new RuntimeException("WARNING: mark() not supported in the overriden UTF_8_Reader");
    }

    /**
     * Reset the read ahead marks is not supported for UTF8 readers.
     * @exception IOException is thrown, for all calls to this method
     * because marking is not supported for UTF8 readers
     */
    public void reset() throws IOException {
        throw new RuntimeException("WARNING: reset() not supported in the overriden UTF_8_Reader");
    }

    /**
     * Get the size in chars of an array of bytes.
     *
     * @param      array  Source buffer
     * @param      offset Offset at which to start counting characters
     * @param      length number of bytes to use for counting
     *
     * @return     number of characters that would be converted
     */
    /*
     * This method is only used by our internal Helper class in the method
     * byteToCharArray to know how much to allocate before using a
     * reader. If we encounter bad encoding we should return a count
     * that includes that character so the reader will throw an IOException
     */
    public int sizeOf(byte[] array, int offset, int length) {
        int count = 0;
        int endOfArray;
        int extraBytes;

        for (endOfArray = offset + length; offset < endOfArray; ) {
            int oldCount = count;
            count++;
            /* Reduce amount of case-mode comparisons */
            if ((array[offset]&0x80) == 0) {
                extraBytes = 0;
            } else {
                switch (((int)array[offset] & 0xff) >> 4) {
                case 12: case 13:
                    /* 11 bits: 110x xxxx   10xx xxxx */
                    extraBytes = 1;
                    break;
    
                case 14:
                    /* 16 bits: 1110 xxxx  10xx xxxx  10xx xxxx */
                    extraBytes = 2;
                    break;

                case 15:
                    if (((int)array[offset] & 0x08)==0) {
                        /* 21 bits: 1111 0xxx  10xx xxxx  10xx xxxx  10xx xxxx */
                        // we imply that the 5 high bits are not all zeroes
                        extraBytes = 3;
                        count++;
                        break;
                    } // else as default

             default:
                    /*
                     * this byte will be replaced with 'RC'
                     */
                    extraBytes = 0;
                }
            }
            offset++;
            // test if extra bytes are in form 10xx xxxx
            while (extraBytes-- > 0){
                if (offset < endOfArray) {
                    if ((((int)array[offset]) & 0xC0) != 0x80) {
                        break;  // test fails: char will be replaced with 'RC'
                    } else {
                        offset++;
                    }
                } else {
                    // broken sequence of bytes detected at the array tail
                    // the broken char still must be counted
                    count = oldCount+1;
                    break;
                }
            }
        }

        return count;
    }
}
