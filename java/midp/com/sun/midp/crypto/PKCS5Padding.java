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
 
package com.sun.midp.crypto;

/**
 * This class implements padding as specified in the PKCS#5 standard.
 */
public final class PKCS5Padding implements Padder {

    /** Contains the block size. */
    private int blockSize;

    /**
     * Constructor.
     * @param blockSize block size
     */
    public PKCS5Padding(int blockSize) {
        this.blockSize = blockSize;
    }
    
    /**
     * Pads the input according to the PKCS5 padding scheme.
     * @param queue containing the last bytes of data that must be padded
     * @param count number of data bytes
     * @return the number of padding bytes added
    */
    public int pad(byte[] queue, int count) {
        int len = blockSize - count;

        for (int i = count; i < blockSize; i++) {
            queue[i] = (byte) len;
        }
        return len;
    }

    /**
     * Removes padding bytes that were added to the input.
     * @param outBuff the output buffer
     * @param size size of data
     * @return the number of padding bytes, allowing them to be removed
     * prior to
     * putting results in the users output buffer and -1 if input is 
     * not properly
     * padded
     * @exception BadPaddingException if not properly padded     
     */
    public int unPad(byte[] outBuff, int size) throws BadPaddingException {
        int padValue = outBuff[size - 1] & 0xff;

        if (padValue < 1 || padValue > blockSize) {
            throw new BadPaddingException();
        }

        for (int i = 0; i < padValue; i++) {
            if (outBuff[--size] != padValue)
                throw new BadPaddingException();
        }
        return padValue;
    }
}
