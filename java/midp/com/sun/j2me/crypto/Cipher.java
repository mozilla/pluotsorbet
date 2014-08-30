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

package com.sun.j2me.crypto;

/**
 * Implements a class that generalizes all ciphers.
 */
public class Cipher {
    /** Cipher implementation object. */
    private com.sun.midp.crypto.Cipher cipher;

    /** Used in init to indicate encryption mode. */
    public static final int ENCRYPT_MODE = com.sun.midp.crypto.Cipher.ENCRYPT_MODE;

    /** Used in init to indicate decryption mode. */
    public static final int DECRYPT_MODE = com.sun.midp.crypto.Cipher.DECRYPT_MODE;

    private Cipher(com.sun.midp.crypto.Cipher cipher) {
        this.cipher = cipher;
    }

   /**
     * Generates a <code>Cipher</code> object for the specified transformation
     */
    public static final Cipher getNewInstance(String transformation)
            throws NoSuchAlgorithmException, NoSuchPaddingException {
        try {
            return new Cipher(com.sun.midp.crypto.Cipher.getInstance(
                                              transformation));
        }
        catch (com.sun.midp.crypto.NoSuchAlgorithmException e) {
            throw new NoSuchAlgorithmException(e.getMessage());
        }
        catch (com.sun.midp.crypto.NoSuchPaddingException e) {
            throw new NoSuchPaddingException(e.getMessage());
        }
    }

    /**
     * Initializes cipher with a key and a set of algorithm parameters
     */
    public void init(int opmode, com.sun.midp.crypto.Key key, CryptoParameter params)
        throws InvalidKeyException, InvalidAlgorithmParameterException {
        try {
            cipher.init(opmode, key, params);
        } catch (com.sun.midp.crypto.InvalidKeyException e) {
            throw new InvalidKeyException();
        } catch (com.sun.midp.crypto.InvalidAlgorithmParameterException e) {
            throw new InvalidAlgorithmParameterException();
        }
    }

    /**
     * Continues a multiple-part encryption or decryption operation 
     */
    public int update(byte[] input, int inputOffset, int inputLen,
                               byte[] output, int outputOffset)
        throws IllegalStateException, ShortBufferException { 
        try {
            return cipher.update(input, inputOffset, inputLen, output, outputOffset);
        } catch (com.sun.midp.crypto.ShortBufferException e) {
            throw new ShortBufferException();
        }
    }

    /**
     * Used for performing encryption or decryption single-part operation, 
     * or finishing multiple-part operation 
     */
    public int doFinal(byte[] input, int inputOffset, int inputLen,
        byte[] output, int outputOffset)
        throws IllegalStateException, ShortBufferException,
               IllegalBlockSizeException, BadPaddingException { 
        try {
            return cipher.doFinal(input, inputOffset, inputLen, output, outputOffset);
        } catch (com.sun.midp.crypto.ShortBufferException e) {
            throw new ShortBufferException();
        } catch (com.sun.midp.crypto.IllegalBlockSizeException e) {
            throw new IllegalBlockSizeException();
        } catch (com.sun.midp.crypto.BadPaddingException e) {
            throw new BadPaddingException();
        }
    
    }

    /**
     * Returns the initialization vector (IV)
     */
    public byte[] getIV() {
        return cipher.getIV();
    }

}
