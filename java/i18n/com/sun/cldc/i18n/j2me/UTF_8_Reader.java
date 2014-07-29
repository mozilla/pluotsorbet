/*
 *   
 *
 * Copyright  1990-2007 Sun Microsystems, Inc. All Rights Reserved.
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

/** Reader for UTF-8 encoded input streams. */
public class UTF_8_Reader extends com.sun.cldc.i18n.StreamReader {
    /** signals that no byte is available, but not the end of stream */
    private static final int NO_BYTE = -2;
    /** 'replacement character' [Unicode 1.1.0] */ 
    private static final int RC = 0xFFFD; 
    /** read ahead buffer to hold a part of char from the last read.
     * The only case this buffer is needed is like following:
     * after a number of characters (at least one) have been read,
     * the next character is encoded by 4 bytes, of which only 3 are
     * already available in the input stream. In this case read()
     * will finish without waiting for the last byte of the character.
     */
    private int[] readAhead;
    /* the number of UTF8 bytes that may encode one character */
    private static final int READ_AHEAD_SIZE = 4;
    /**
     * If non-zero, the last read code point must be represented by two
     * surrogate code units, and the low surrogate code unit has not yet
     * been retrieved during the last read operation.
     */
    protected int pendingSurrogate = 0;

    /** Constructs a UTF-8 reader. */
    public UTF_8_Reader() {
        readAhead = new int[READ_AHEAD_SIZE];
    }

    public Reader open(InputStream in, String enc)
        throws UnsupportedEncodingException {
        super.open(in, enc);
        prepareForNextChar(NO_BYTE);
        return this;
    }

    /**
     * maps the number of extra bytes onto the minimal valid value that may
     * be encoded with this number of bytes
     */
    private static final int[] minimalValidValue
            = {0x00, 0x80, 0x800, 0x10000 /*, 0x200000*/};
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
    public int read(char cbuf[], int off, int len) throws IOException {
        int count = 0;
        int firstByte;
        int extraBytes;
        int currentChar = 0;
        int nextByte;
        int headByte = NO_BYTE;

        if (len == 0) {
            return 0;
        }
        if (pendingSurrogate != 0) {
            cbuf[off + count] = (char)pendingSurrogate;
            count++;
            pendingSurrogate = 0;
            if (len == 1) {
                return 1;
            }
        }

        while (count < len) {
            // must wait for the first character, and
            // other characters are read only if they are available
            final boolean mustBlockTillGetsAChar = (0 == count);
            firstByte = getByteOfCurrentChar(0, mustBlockTillGetsAChar);
            if (firstByte < 0) {
                if (firstByte == -1 && count == 0) {
                    // end of stream
                    return -1;
                }

                return count;
            }
            /* Let's reduce amount of case-mode comparisons */
            if ((firstByte&0x80) == 0) {
                extraBytes = 0;
                currentChar = firstByte;
            } else {
                switch (firstByte >> 4) {
                case 12: case 13:
                    /* 11 bits: 110x xxxx   10xx xxxx */
                    extraBytes = 1;
                    currentChar = firstByte & 0x1F;
                    break;
    
                case 14:
                    /* 16 bits: 1110 xxxx  10xx xxxx  10xx xxxx */
                    extraBytes = 2;
                    currentChar = firstByte & 0x0F;
                    break;

                case 15:
                    if ((firstByte&0x08)==0) {
                        /* 21 bits: 1111 0xxx  10xx xxxx  10xx xxxx  10xx xxxx */
                        extraBytes = 3;
                        currentChar = firstByte & 0x07;
                        break;
                    } // else as default

                default:
                    /* we do replace malformed character with special symbol */
                    extraBytes = 0;
                    currentChar = RC;
                }
            }

            for (int j = 1; j <= extraBytes; j++) {
                nextByte = getByteOfCurrentChar(j, mustBlockTillGetsAChar);
                if (nextByte == NO_BYTE) {
                    // done for now, comeback later for the rest of char
                    return count;
                }

                if (nextByte == -1) {
                    // end of stream in the middle of char -- set 'RC'
                    currentChar = RC;
                    break;
                }

                if ((nextByte & 0xC0) != 0x80) {
                    // invalid byte - move it at head of next read sequence
                    currentChar = RC;
                    headByte = nextByte;
                    break;
                }

                // each extra byte has 6 bits more of the char
                currentChar = (currentChar << 6) + (nextByte & 0x3F);
            }

            if (currentChar < minimalValidValue[extraBytes]) {
                // the character is malformed: it should be encoded
                // with a shorter sequence of bytes
                currentChar = RC;
                cbuf[off + count] = (char)currentChar;
                count++;
            } else if (currentChar <= 0xd7ff
             // d800...d8ff and dc00...dfff are high and low surrogate code
             // points, they do not represent characters
             || (0xe000 <= currentChar && currentChar <= 0xffff)) {
                cbuf[off + count] = (char)currentChar;
                count++;
            } else if (0xffff < currentChar && currentChar <= 0x10ffff) {
                int highSurrogate = 0xd800 | ((currentChar-0x10000) >> 10);
                int lowSurrogate = 0xdc00 | (currentChar & 0x3ff);
                cbuf[off + count] = (char)highSurrogate;
                count++;
                if (count < len) {
                    cbuf[off + count] = (char)lowSurrogate;
                    count++;
                } else {
                    pendingSurrogate=lowSurrogate;
                }
            } else {
                currentChar = RC;
                cbuf[off + count] = (char)currentChar;
                count++;
            }
            prepareForNextChar(headByte);
        }
        return count;
    }

    /**
     * Get one of the raw bytes for the current character.
     * The byte first gets read into the read ahead buffer, unless
     * it's already there.
     *
     * @param byteOfChar which raw byte to get 0 for the first, 3 for the last.
     *                   The bytes must be accessed sequentially, that is,
     *                   the only possible order of byteOfChar values
     *                   in a series of calls is 0, 1, 2, 3.
     * @param allowBlockingRead  false allows returning NO_BYTE if no byte is
     *                   available in the input stream; true forces reading.
     * @return a byte value, NO_BYTE for no byte available or -1 for end of
     *          stream
     *
     * @exception  IOException   if an I/O error occurs.
     */
    private int getByteOfCurrentChar(int byteOfChar, boolean allowBlockingRead) throws IOException {
        if (readAhead[byteOfChar] != NO_BYTE) {
            return readAhead[byteOfChar];
        }

        /*
         * allowBlockingRead will be true for the first character.
         * Our read method must block until it gets one char so don't call
         * available() for the first character.
         */
        if (allowBlockingRead || in.available() > 0) {
            readAhead[byteOfChar] = in.read();
        }

        return readAhead[byteOfChar];
    }

    /**
     * Prepare the reader for the next character by clearing the look
     * ahead buffer.
     * @param headByte value of first byte. If previous sequence is interrupted
     * by malformed byte - this byte should be moved at head of next sequence
     */
    private void prepareForNextChar(int headByte) {
        readAhead[0] = headByte;
        for (int i=1; i<READ_AHEAD_SIZE; i++) {
            readAhead[i]=NO_BYTE;
        }
    }

    /**
     * Tell whether this reader supports the mark() operation.
     * The UTF-8 implementation always returns false because it does not
     * support mark().
     *
     * @return false
     */
    public boolean markSupported() {
        /*
         * For readers mark() is in characters, since UTF-8 character are
         * variable length, so we can't just forward this to the underlying
         * byte InputStream like other readers do.
         * So this reader does not support mark at this time.
         */
        return false;
    }

    /**
     * Mark a read ahead character is not supported for UTF8
     * readers.
     * @param readAheadLimit number of characters to buffer ahead
     * @exception IOException is thrown, for all calls to this method
     * because marking is not supported for UTF8 readers
     */
    public void mark(int readAheadLimit) throws IOException {
        throw new IOException("mark() not supported");
    }

    /**
     * Reset the read ahead marks is not supported for UTF8 readers.
     * @exception IOException is thrown, for all calls to this method
     * because marking is not supported for UTF8 readers
     */
    public void reset() throws IOException {
        throw new IOException("reset() not supported");
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
