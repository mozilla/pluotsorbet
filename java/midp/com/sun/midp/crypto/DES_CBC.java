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
 * DES CBC Cipher algorithm.
 */
public class DES_CBC extends DES_ECB {

    /** Internal data. */
    private byte[] chainingBlock;

    /** Internal data. */
    private byte[] storeBuffer;

    /** Saved state variable. */
    private byte[] savedChainingBlock;

    /**
     * Constructor.
     *
     * @param useTripleDes true if the class is being used for triple DES
     *
     */
    public DES_CBC(boolean useTripleDes) {
        super(useTripleDes);
        chainingBlock = new byte[BLOCK_SIZE];
        storeBuffer = new byte[BLOCK_SIZE];
    }

    /**
     * Initializes this cipher with a key and a set of algorithm
     * parameters.
     * @param mode the operation mode of this cipher
     * @param key the encryption key
     * @param params the algorithm parameters
     * @exception java.security.InvalidKeyException if the given key
     * is inappropriate for initializing this cipher
     * @exception java.security.InvalidAlgorithmParameterException
     * if the given algorithm parameters are inappropriate for this cipher
     */
    public void init(int mode, Key key, CryptoParameter params)
        throws InvalidKeyException, InvalidAlgorithmParameterException {

        doInit(mode, keyAlgorithm, key, true, params);
        System.arraycopy(IV, 0, chainingBlock, 0, BLOCK_SIZE);
    }

    /**
     * Encrypts or decrypts data in a single-part operation, or finishes a
     * multiple-part operation. The data is encrypted or decrypted,
     * depending on how this cipher was initialized.
     *
     * @param inBuff the input buffer
     * @param inOffset the offset in <code>input</code> where the input
     * starts
     * @param inLength the input length
     * @param outBuff the buffer for the result
     * @param outOffset the offset in <code>output</code> where the result
     * is stored
     *
     * @return the number of bytes stored in <code>output</code>
     *
     * @exception IllegalStateException if this cipher is in a wrong state
     * (e.g., has not been initialized)
     * @exception javax.crypto.IllegalBlockSizeException if this cipher is
     * a block cipher,
     * no padding has been requested (only in encryption mode), and the total
     * input length of the data processed by this cipher is not a multiple of
     * block size
     * @exception javax.crypto.ShortBufferException if the given output buffer
     * is too small to hold the result
     * @exception javax.crypto.BadPaddingException if this cipher is int
     * decryption mode,
     * and (un)padding has been requested, but the decrypted data is not
     * bounded by the appropriate padding bytes
     */
    public int doFinal(byte inBuff[], int inOffset, int inLength,
        byte outBuff[], int outOffset)
        throws IllegalStateException, IllegalBlockSizeException,
            ShortBufferException, BadPaddingException {
        int result = super.doFinal(inBuff, inOffset, inLength,
                                   outBuff, outOffset);
        System.arraycopy(IV, 0, chainingBlock, 0, BLOCK_SIZE);
        return result;
    }

    /**
     * Depending on the mode, either encrypts or decrypts data block.
     * @param out will contain the result of encryption
     * or decryption operation
     * @param offset is the offset in out
     */
    protected void processBlock(byte[] out, int offset) {

        if (mode == Cipher.ENCRYPT_MODE) {
            Util.xorArrays(holdData, chainingBlock);
            super.processBlock(out, offset);
            System.arraycopy(out, offset, chainingBlock, 0, BLOCK_SIZE);
        } else {
            System.arraycopy(holdData, 0, storeBuffer, 0, BLOCK_SIZE);
            super.processBlock(out, offset);
            for (int i = 0; i < BLOCK_SIZE; i++) {
                out[offset + i] ^= chainingBlock[i];
            }
            System.arraycopy(storeBuffer, 0, chainingBlock, 0, BLOCK_SIZE);
        }
    }

    /**
     * Saves cipher state.
     */
    protected void saveState() {
        super.saveState();
        savedChainingBlock = Util.cloneArray(chainingBlock);
    }

    /**
     * Restores cipher state.
     */
    protected void restoreState() {
        super.restoreState();
        chainingBlock = savedChainingBlock;
    }
}
