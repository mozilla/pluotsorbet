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
 * Wrapper for all AES cipher classes.
 */
public class AES extends Cipher {
    Cipher cipher;

    /**
     * Public by name constructor.
     */
    public AES() {
    }

    /**
     * Called by the factory method to set the mode and padding parameters.
     * Need because Class.newInstance does not take args.
     *
     * @param mode the mode parsed from the transformation parameter of
     *             getInstance
     * @param padding the paddinge parsed from the transformation parameter of
     *                getInstance
     *
     * @exception NoSuchPaddingException if <code>transformation</code>
     * contains a padding scheme that is not available.
     */
    protected void setChainingModeAndPadding(String mode, String padding)
            throws NoSuchPaddingException {
        if (mode.equals("ECB") || mode.equals("")) {
            cipher = new AES_ECB();
        } else if (mode.equals("CBC")) {
            cipher = new AES_CBC();
        } else {
            throw new IllegalArgumentException();
        }

        cipher.setChainingModeAndPadding(mode, padding);
    }

    /**
     * Initializes this cipher with a key and a set of algorithm
     * parameters.
     */
    public void init(int opmode, Key key, CryptoParameter params)
            throws InvalidKeyException, InvalidAlgorithmParameterException {
        cipher.init(opmode, key, params);
    }

    /**
     * Continues a multiple-part encryption or decryption operation
     * (depending on how this cipher was initialized), processing another data
     * part.     
     */
    public int update(byte[] input, int inputOffset, int inputLen,
                               byte[] output, int outputOffset)
            throws IllegalStateException, ShortBufferException {
        return cipher.update(input, inputOffset, inputLen, output,
                             outputOffset);
    }

    /**
     * Encrypts or decrypts data in a single-part operation, or finishes a
     * multiple-part operation. The data is encrypted or decrypted,
     * depending on how this cipher was initialized.
     */
    public int doFinal(byte[] input, int inputOffset, int inputLen,
        byte[] output, int outputOffset)
        throws IllegalStateException, ShortBufferException,
               IllegalBlockSizeException, BadPaddingException {
        return cipher.doFinal(input, inputOffset, inputLen, output,
                              outputOffset);
    }

    /**
     * Returns the initialization vector (IV) in a new buffer.
     * This is useful in the case where a random IV was created.
     * @return the initialization vector in a new buffer,
     * or <code>null</code> if the underlying algorithm does
     * not use an IV, or if the IV has not yet been set.
     */
    public byte[] getIV() {
        return cipher.getIV();
    }
}
