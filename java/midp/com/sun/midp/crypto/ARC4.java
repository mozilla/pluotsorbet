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
 * This class implements the ARCfour stream cipher
 */
public final class ARC4 extends Cipher {
    /** Current cipher mode. */
    private int mode;
    /** Local certificate key. */
    private SecretKey ckey = null;
    /** Seed array. */
    private byte[] S = null;
    /** First intermediate result array. */
    private int[] ii = null;
    /** Second intermediate result array. */
    private int[] jj = null;
    
    /**
     * Constructor for algorithm 3 (ALG_ARCFOUR)
     */
    public ARC4() {
	mode = Cipher.MODE_UNINITIALIZED;
	S = null;
	ii = null;
	jj = null;
    }


    /**
     * Called by the factory method to set the mode and padding parameters.
     * Need because Class.newInstance does not take args.
     *
     * @param mode the mode parsed from the transformation parameter of
     *             getInstance and upper cased
     * @param padding the paddinge parsed from the transformation parameter of
     *                getInstance and upper cased
     *
     * @exception NoSuchPaddingException if <code>transformation</code>
     * contains a padding scheme that is not available
     * @exception IllegalArgumentException if the mode is invalid for the
     * cipher
     */
    protected void setChainingModeAndPadding(String mode, String padding)
            throws NoSuchPaddingException {

        if (!(mode.equals("") || mode.equals("NONE"))) {
            throw new IllegalArgumentException();
        }

        // NOPADDING is not an option.
        if (!(padding.equals("") || padding.equals("NOPADDING"))) {
            throw new NoSuchPaddingException();
        }
    }

    /**
     * Initializes the cipher's S-boxes based on the key.
     *  This code is based on the cipher's description in 
     * Bruce Schenier's "Applied Cryptography", Second Edition, pp 397-398,
     * ISBN 0-471-11709-9
     *
     * @param opmode the operation mode of this cipher (this is one of the
     * following:
     * <code>ENCRYPT_MODE</code> or <code>DECRYPT_MODE</code>)
     * @param key the encryption key
     * @param params the algorithm parameters
     *
     * @exception InvalidKeyException if the given key is inappropriate for
     * initializing this cipher, or its keysize exceeds the maximum allowable
     * keysize.
     * @exception InvalidAlgorithmParameterException if the given algorithm
     * parameters are inappropriate for this cipher,
     * or this cipher is being initialized for decryption and requires
     * algorithm parameters and <code>params</code> is null, or the given
     * algorithm parameters imply a cryptographic strength that would exceed
     * the legal limits.
     * @exception IllegalArgumentException if the opmode is invalid
     */
    public void init(int opmode, Key key, CryptoParameter params)
            throws InvalidKeyException, InvalidAlgorithmParameterException {

        if (!(key instanceof SecretKey)) {
            throw new InvalidKeyException();
        }

        if (opmode != Cipher.ENCRYPT_MODE && opmode != Cipher.DECRYPT_MODE) {
	    throw new IllegalArgumentException();
	}
	
	mode = opmode;
	ckey = (SecretKey)key;

	// Initialize the counters
        ii = new int[1];
        ii[0] = 0;
        jj = new int[1];
        jj[0] = 0;

	S = new byte[256];
	
        // Initialize S 
	for (int i = 0; i < 256; i++) {
            S[i] = (byte) i;
        }
    
	// Initilaize K based on the key
        byte[] K = new byte[256];
	int index = 0;
	while (index < 256) {
	    for (int i = 0; (i < ckey.secret.length) && (index < 256); i++) {
		K[index++] = ckey.secret[i];
	    }
	}

	// Populate the 8*8 S-box
	int j = 0;
	byte temp;
	for (int i = 0; i < 256; i++) {
	    j = (j + ((S[i] + K[i]) & 0xff)) & 0xff;
	    temp = S[i];
	    S[i] = S[j];
	    S[j] = temp;
	}
    }
    /**
     * Native function to transform a buffer.
     * @param S array of S box values
     * @param X first set intermediate results
     * @param Y second set of intermediate results
     * @param inbuf input buffer of data 
     * @param inoff offset in the provided input buffer
     * @param inlen length of data to be processed
     * @param outbuf output buffer of data 
     * @param outoff offset in the provided output buffer
     */
    private static native void nativetx(byte[] S, int[] X, int[] Y,
			byte[] inbuf, int inoff, int inlen,
			byte[] outbuf, int outoff);
    /**
     * Transform a buffer of data,
     * @param inBuf input buffer of data 
     * @param inOff offset in the provided input buffer
     * @param inLen length of data to be processed
     * @param outBuf output buffer of data 
     * @param outOff offset in the provided output buffer
     * @return number of bytes copied to output buffer
     */
    private int transform(byte[] inBuf, int inOff, int inLen,
			   byte[] outBuf, int outOff) {
        /*
	 * Normally, we would use something like:
	 * int test = inBuf[inOff] + inBuf[inLen - 1] + 
         *            inBuf[inOff + inLen - 1] + 
	 *             outBuf[outOff] + outBuf[outOff + inLen - 1];
	 * to force an array bounds check that might otherwise crash
	 * the VM. However, since we have such checks in the update
	 * method, we do not need them here.
	 */ 
	nativetx(S, ii, jj, inBuf, inOff, inLen, outBuf, outOff); 
	return inLen;
    }
    
    
    /**
     * Update the current data record.
     * 
     * @param input the input buffer
     * @param inputOffset the offset in <code>input</code> where the input
     * starts
     * @param inputLen the input length
     * @param output the buffer for the result
     * @param outputOffset the offset in <code>output</code> where the result
     * is stored
     *
     * @return the number of bytes stored in <code>output</code>
     *
     * @exception IllegalStateException if this cipher is in a wrong state
     * (e.g., has not been initialized)
     * @exception ShortBufferException if the given output buffer is too small
     * to hold the result
     */
    public int update(byte[] input, int inputOffset, int inputLen,
                      byte[] output, int outputOffset)
            throws IllegalStateException, ShortBufferException {

        Util.checkBounds(input, inputOffset, inputLen,
                         output, outputOffset);
	
	if (mode == Cipher.MODE_UNINITIALIZED) {
	    throw new IllegalStateException();
        }
	
	if (inputLen == 0) {
	    return 0;
        }    

        if (output.length - outputOffset < inputLen) {
            throw new ShortBufferException();
        }

	return transform(input, inputOffset, inputLen, output, outputOffset);
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
        int val = update(input, inputOffset, inputLen, output, outputOffset);

        try {
            init(mode, ckey);
        } catch (InvalidKeyException ike) {
            // ignore, the key was already checked
        }

        return val;
    }
}
