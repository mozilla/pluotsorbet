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

import java.io.*;

/**
 * Writer for UTF-8 encoded output streams. NOTE: The UTF-8 writer only
 * supports UCS-2, or Unicode, to UTF-8 conversion. There is no support
 * for UTF-16 encoded characters outside of the Basic Multilingual Plane
 * (BMP). These are encoded in UTF-16 using previously reserved values
 * between U+D800 and U+DFFF. Additionally, the UTF-8 writer does not
 * support any character that requires 4 or more UTF-8 encoded bytes.
 */
public class UTF_8_Writer extends com.sun.cldc.i18n.StreamWriter {

    /** pending high surrogate code unit, or zero */
    protected int pendingSurrogate;

    /** This value replaces invalid characters
     * (that is, surrogates code units without a pair) */
    static final private int replacementValue = 0x3f;
    /**
     * Write a portion of an array of characters.
     *
     * @param  cbuf  Array of characters
     * @param  off   Offset from which to start writing characters
     * @param  len   Number of characters to write
     *
     * @exception  IOException  If an I/O error occurs
     */
    public void write(char cbuf[], int off, int len) throws IOException {
        byte[] outputByte = new byte[4];     // Never more than 4 encoded bytes
        int inputChar;
        int outputSize;
        int count = 0;

        while (count < len) {
            inputChar = 0xffff & cbuf[off + count];
            if (0 != pendingSurrogate) {
                if (0xdc00<=inputChar && inputChar<=0xdfff) {
                    //000u uuuu xxxx xxxx xxxx xxxx
                    //1101 10ww wwxx xxxx   1101 11xx xxxx xxxx
                    final int highHalf = (pendingSurrogate & 0x03ff) + 0x0040;
                    final int lowHalf = inputChar & 0x03ff;
                    inputChar = (highHalf << 10) | lowHalf;
                } else {
                    // write replacement value instead of unpaired surrogate
                    outputByte[0] = replacementValue;
                    outputSize = 1;
                    out.write(outputByte, 0, outputSize);
                }
                pendingSurrogate = 0;
            }
            if (inputChar < 0x80) {
                outputByte[0] = (byte)inputChar;
                outputSize = 1;
            } else if (inputChar < 0x800) {
                outputByte[0] = (byte)(0xc0 | ((inputChar >> 6) & 0x1f));
                outputByte[1] = (byte)(0x80 | (inputChar & 0x3f));
                outputSize = 2;
            } else if (0xd800<=inputChar && inputChar<=0xdbff) {
                pendingSurrogate = inputChar;
                outputSize = 0;
            } else if (0xdc00<=inputChar && inputChar<=0xdfff) {
                // unpaired surrogate
                outputByte[0] = replacementValue;
                outputSize = 1;
            } else if (inputChar < 0x10000) {
                outputByte[0] = (byte)(0xe0 | ((inputChar >> 12) & 0x0f));
                outputByte[1] = (byte)(0x80 | ((inputChar >> 6) & 0x3f));
                outputByte[2] = (byte)(0x80 | (inputChar & 0x3f));
                outputSize = 3;
            } else {
                /* 21 bits: 1111 0xxx  10xx xxxx  10xx xxxx  10xx xxxx
                 * a aabb  bbbb cccc  ccdd dddd
                */
                outputByte[0] = (byte)(0xf0 | ((inputChar >> 18) & 0x07));
                outputByte[1] = (byte)(0x80 | ((inputChar >> 12) & 0x3f));
                outputByte[2] = (byte)(0x80 | ((inputChar >> 6) & 0x3f));
                outputByte[3] = (byte)(0x80 | (inputChar & 0x3f));
                outputSize = 4;
            }
            out.write(outputByte, 0, outputSize);
            count++;
        }
    }

    /**
     * Get the size in bytes of an array of chars.
     *
     * @param      cbuf   Source buffer
     * @param      offset Offset at which to start counting character sizes
     * @param      length number of characters to use for counting
     *
     * @return     number of bytes that the characters would be converted to
     */
    public int sizeOf(char[] cbuf, int offset, int length) {
        int inputChar;
        int outputSize;
        int outputCount = 0;
        int count = 0;
        int localPendingSurrogate = pendingSurrogate;
        while (count < length) {
            inputChar = 0xffff & cbuf[offset + count];
            if (0 != localPendingSurrogate) {
                if (0xdc00<=inputChar && inputChar<=0xdfff) {
                    //000u uuuu xxxx xxxx xxxx xxxx
                    //1101 10ww wwxx xxxx   1101 11xx xxxx xxxx
                    final int highHalf = (localPendingSurrogate & 0x03ff) + 0x0040;
                    final int lowHalf = inputChar & 0x03ff;
                    inputChar = (highHalf << 10) | lowHalf;
                } else {
                    // going to write replacement value instead of unpaired surrogate
                    outputSize = 1;
                    outputCount += outputSize;
                }
                localPendingSurrogate = 0;
            }
            if (inputChar < 0x80) {
                outputSize = 1;
            } else if (inputChar < 0x800) {
                outputSize = 2;
            } else if (0xd800<=inputChar && inputChar<=0xdbff) {
                localPendingSurrogate = inputChar;
                outputSize = 0;
            } else if (0xdc00<=inputChar && inputChar<=0xdfff) {
                // unpaired surrogate
                // going to output replacementValue;
                outputSize = 1;
            } else if (inputChar < 0x10000) {
                outputSize = 3;
            } else {
                /* 21 bits: 1111 0xxx  10xx xxxx  10xx xxxx  10xx xxxx
                 * a aabb  bbbb cccc  ccdd dddd
                */
                outputSize = 4;
            }
            outputCount += outputSize;
            count++;
        }
        return outputCount;
    }

    /**
     * Open the writer.
     *
     * @param outputStream
     * @param encoding encoding
     * @return the writer
     * @throws UnsupportedEncodingException
     */
    public Writer open(OutputStream outputStream, String encoding)
            throws UnsupportedEncodingException {
        pendingSurrogate = 0;
        return super.open(outputStream,encoding);
    }

    /**
     * Close the writer and the output stream.
     *
     * @throws IOException
     */
    public void close() throws IOException {
        if (0 != pendingSurrogate) {
            // write replacement value instead of the unpaired surrogate
            byte[] outputByte = new byte[1];
            outputByte[0] = replacementValue;
            out.write(outputByte, 0, 1);
        }
        pendingSurrogate = 0;
        super.close();
    }

    // flush() can do nothing with pendingSurrogate because the surrogate
    // contains only a portion of the character code, and the second half
    // is still expected to arrive.
    // public void flush() throws IOException { super.flush(); }
}



