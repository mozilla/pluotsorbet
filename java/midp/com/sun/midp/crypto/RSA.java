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
 * This class implements RSA encryption/decryption
 */
public final class RSA extends Cipher {
    /** Local certificate key. */
    private RSAKey ckey = null;
    /** Current cipher mode. */
    private int mode;
    /** Local random number for seed. */
    private static SecureRandom rnd = null;

    /** Signature pad offset. */
    private final static int PAD_OFFSET = 2;

    /** Message to sign. */
    private byte[] messageToSign;

    /** Number of bytes in the message to sign. */
    private int bytesInMessage;

    /**
     * A native method for performing modular exponentiation.
     *
     * @param data      contains the data on which exponentiation is to
     *                  be performed
     * @param exponent  contains the exponent, e.g. 65537 (decimal) is 
     *                  written as a three-byte array containing 
     *                  0x01, 0x00, 0x01
     * @param modulus   contains the modulus
     * @param result    the result of the modular exponentiation is 
     *                  returned in this array
     * @return          length of the result in bytes
     * @exception IllegalArgumentException if a argument is too long for
     *    the native code to handle. (currently (32K - 8) bits max)
     */
    private static native int modExp(byte[] data, byte[] exponent,
                                      byte[] modulus, byte[] result)
        throws IllegalArgumentException;

    /**
     * Performs an RSA operation on specified data. If the data length
     * is not the same as the modulus length (as may happen for an 
     * encryption request), PKCS#1 block type 2 padding is added.
     * <P />
     * @param data byte array to be encrypted/decrypted
     * @return a byte array containing the result
     * @exception IllegalStateException 
     *            if the encryption/decryption key is missing a modulus or
     *            exponent
     */ 
    private byte[] doIt(byte[] data) {
        int modLen = ckey.getModulusLen();
        byte[] buf = new byte[modLen];
        byte[] mod = new byte[modLen];
        int bufLen;
        
        // Note: Both RSAPublicKey and RSAPrivateKey provide the same
        // interface
        short val = ckey.getModulus(mod, (short) 0);
        
        byte[] tmp = new byte[modLen];
        val = ckey.getExponent(tmp, (short) 0);
        byte[] exp = new byte [val];
        System.arraycopy(tmp, 0, exp, 0, val);
        
        bufLen = modExp(data, exp, mod, buf);

        if (bufLen == modLen) {
            return buf;
        } else if (bufLen < modLen) {
            // Reuse tmp which already points to a byte array of modLen size
            for (int i = 0; i < modLen; i++) tmp[i] = 0;
            
            if (buf[0] == (byte) 0x01) {
                tmp[0] = (byte) 0x00;
                tmp[1] = (byte) 0x01;
                for (int i = 2; i < modLen - bufLen + 1; i++) {
                    tmp[i] = (byte) 0xff;
                }
                System.arraycopy(buf, 1, tmp, 
                                 modLen - bufLen + 1, (bufLen - 1));
            } else {
                System.arraycopy(buf, 0, tmp, (modLen - bufLen), bufLen);
            }

            return tmp;
        } else {  // bufLen > modLen, key may be too long
            throw new IllegalArgumentException("Key too long");
        }
    }

    /**
     * Constructor for RSA.
     *
     * @exception RuntimeException if the random number generator can't be
     *            created
     */
    public RSA() {
        mode = Cipher.MODE_UNINITIALIZED;

        try {
            rnd = SecureRandom.getInstance(SecureRandom.ALG_SECURE_RANDOM);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Random number generator missing");
        }
    }

    /**
     * Called by the factory method to set the mode and padding parameters.
     * Need because Class.newInstance does not take args.
     *
     * @param mode the mode parsed from the transformation parameter of
     *             getInstance and upper cased
     * @param padding the padding parsed from the transformation parameter of
     *                getInstance and upper cased
     *
     * @exception NoSuchPaddingException if <code>transformation</code>
     * contains a padding scheme that is not available.
     * @exception IllegalArgumentException if mode is incorrect
     */
    protected void setChainingModeAndPadding(String mode, String padding)
            throws NoSuchPaddingException {

        if (!(mode.equals("") || mode.equals("NONE"))) {
            throw new IllegalArgumentException("illegal chaining mode");
        }

        // NOPADDING is not an option.
        if (!(padding.equals("") || padding.equals("PKCS1PADDING"))) {
            throw new NoSuchPaddingException();
        }
    }

    /**
     * Initializes this cipher with a key and a set of algorithm
     * parameters.
     *
     * @param opMode the operation mode of this cipher
     * @param key the encryption key
     * @param params the algorithm parameters
     *
     * @exception java.security.InvalidKeyException if the given key
     * is inappropriate for initializing this cipher
     * @exception java.security.InvalidAlgorithmParameterException
     * if the given algorithm parameters are inappropriate for this cipher
     * @exception IllegalArgumentException if opMode is incorrect
     */
    public void init(int opMode, Key key, CryptoParameter params)
            throws InvalidKeyException, InvalidAlgorithmParameterException {

        if (!(key instanceof RSAKey)) {
            throw new InvalidKeyException();
        }

        if (opMode != DECRYPT_MODE && opMode != ENCRYPT_MODE) {
            throw new IllegalArgumentException("Wrong operation mode");
        }

        mode = opMode;
        ckey = (RSAKey)key;

        if (ckey.getModulusLen() == 0) {
            throw new InvalidKeyException();
        }

        messageToSign = new byte[ckey.getModulusLen()];
        bytesInMessage = 0;
    }
    
    /**
     * Fills the internal buffer to be encrypted or decrypted
     * (depending on how this cipher was initialized).
     * For the RSA public key cipher there is no output until doFinal.
     *
     * @param inBuf the input buffer
     * @param inOff the offset in <code>input</code> where the input
     * starts
     * @param inLen the input length
     * @param outBuf the buffer for the result
     * @param outOff the offset in <code>output</code> where the result
     * is stored
     *
     * @return the number of bytes stored in <code>output</code>
     *
     * @exception IllegalStateException if this cipher is in a wrong state
     * (e.g., has not been initialized)
     * @exception ShortBufferException if the given output buffer is too small
     * to hold the result
     * @exception IllegalArgumentException if a length or offset is incorrect
     */
    public int update(byte inBuf[], int inOff, int inLen,
                      byte outBuf[], int outOff)
            throws IllegalStateException, ShortBufferException {

        addToMessage(inBuf, inOff, inLen);
        return 0;
    }

    /**
     * Fills the internal message buffer to be encrypted or decrypted
     * (depending on how this cipher was initialized).
     * For the RSA public key cipher there is no output until doFinal.
     *
     * @param inBuf the input buffer
     * @param inOff the offset in <code>input</code> where the input
     * starts
     * @param inLen the input length
     *
     * @exception IllegalStateException if this cipher is in a wrong state
     * (e.g., has not been initialized)
     * @exception IllegalArgumentException if a length or offset is incorrect
     */
    private void addToMessage(byte inBuf[], int inOff, int inLen)
            throws IllegalStateException {
        int bytesToCopy;

        if (mode == Cipher.MODE_UNINITIALIZED) {
            throw new IllegalStateException();
        }

        if (inLen == 0) {
            return;
        }

        if (inBuf == null || inOff < 0 || inLen < 0 ||
                inOff + inLen > inBuf.length) {
            throw new IllegalArgumentException("input out of bounds");
        }

        bytesToCopy = messageToSign.length - bytesInMessage;
        if (inLen < bytesToCopy) {
            bytesToCopy = inLen;
        }

        System.arraycopy(inBuf, inOff, messageToSign, bytesInMessage,
                         bytesToCopy);
        bytesInMessage += bytesToCopy;
    }

    /**
     * Performs the crypto process the buffer.
     *
     * @param inBuf the input buffer
     * @param inOff the offset in <code>input</code> where the input
     * starts
     * @param inLen the input length
     * @param outBuf the buffer for the result
     * @param outOff the offset in <code>output</code> where the result
     * is stored
     *
     * @return the number of bytes stored in <code>output</code>
     *
     * @exception IllegalStateException if this cipher is in a wrong state
     * (e.g., has not been initialized)
     * @exception IllegalBlockSizeException if this cipher is a block cipher,
     * no padding has been requested (only in encryption mode), and the total
     * input length of the data processed by this cipher is not a multiple of
     * block size
     * @exception ShortBufferException if the given output buffer is too small
     * to hold the result
     * @exception BadPaddingException if this cipher is in decryption mode,
     * and (un)padding has been requested, but the decrypted data is not
     * bounded by the appropriate padding bytes
     * @exception IllegalArgumentException if input is greater than the
     *            cipher with the given key can handle
     */
    private int performRsa(byte inBuf[], int inOff, int inLen,
                      byte outBuf[], int outOff)
            throws IllegalStateException, ShortBufferException,
            IllegalBlockSizeException, BadPaddingException {
        int modLen;
        int outLen;
        byte[] tmp;
        byte[] res;
        int padLen;
        int endOfPad;
                            
        modLen = ckey.getModulusLen();

        switch (mode) {
        case Cipher.ENCRYPT_MODE:
            if (inLen > modLen - 11) {
                throw new IllegalArgumentException("Too much input");
            }

            /*
             * Add PKCS#1 (ver 1.5) padding
             * 0x00 | 0x02 | <random, non-zero pad bytes> | 0x00 | <data>
             */ 
            tmp = new byte[modLen];
            tmp[0] = (byte) 0x00;

            padLen = modLen - inLen - 3;
            endOfPad = padLen + PAD_OFFSET;

            if (ckey instanceof RSAPublicKey) {
                tmp[1] = (byte) 0x02;  // for block type 02

                // Use random padding (replacing 0x00s)
                rnd.nextBytes(tmp, PAD_OFFSET, padLen);
                
                for (int i = PAD_OFFSET; i < endOfPad; i++) {
                    if (tmp[i] == (byte) 0x00) {
                        // padding byte must be non-zero
                        tmp[i] = (byte) 0xff;
                    }
                }
            } else {
                // NOTE: RFC2313 suggests 0x01 for private key signatures
                tmp[1] = (byte) 0x01; 
                for (int i = PAD_OFFSET; i < endOfPad; i++) {
                    tmp[i] = (byte) 0xff;
                }
            }

            if (inLen > modLen) {
                throw new IllegalArgumentException("inlen > modlen");
            }                

            tmp[modLen - inLen - 1] = (byte) 0x00;
            System.arraycopy(inBuf, inOff, tmp, modLen - inLen, inLen);

            res = doIt(tmp);

            if (outOff + res.length > outBuf.length) {
                throw new ShortBufferException();
            }

            System.arraycopy(res, 0, outBuf, outOff, res.length);
            outLen = res.length;
            break;
            
        case Cipher.DECRYPT_MODE:
            // This is specified in RFC2313
            if (inLen != modLen) {
                throw new IllegalArgumentException("inlen != modlen");
            }

            if (inOff != 0) {
                tmp = new byte[modLen];
                System.arraycopy(inBuf, inOff, tmp, 0, modLen);
                res = doIt(tmp);
            } else {
                res = doIt(inBuf);
            }
            
            // Count number of padding bytes (these must be non-zero)
            padLen = 0;
            for (int i = 2; (i < res.length) && (res[i] != (byte) 0x00); i++) {
                padLen++;
            }

            /*
             * Note that whatever our decryption key type is,
             * the other side used an opposite key type when encrypting
             * so if our key type is TYPE_RSA_PUBLIC, the sender used
             * TYPE_RSA_PRIVATE and the expected block type after 
             * decryption (before encryption) is 0x00 or 0x01
             */ 
            if ((padLen < modLen - 3) && (res.length > 1) &&
                (res[0] == (byte) 0x00) &&
                (((ckey instanceof RSAPublicKey) &&
                  ((res[1] == (byte) 0x01) || (res[1] == (byte) 0x00))) ||
                 ((ckey instanceof RSAPrivateKey) && 
                  (res[1] == (byte) 0x02)))) {
                outLen = modLen - padLen - 3;

                if (outOff + outLen > outBuf.length) {
                    throw new ShortBufferException();
                }

                System.arraycopy(res, padLen + 3, outBuf, outOff, outLen);
            } else {
                throw new BadPaddingException();
            }

            break;

        default:
            throw new IllegalStateException();
        }
                            
        return outLen;
    }
    
    /**
     * Process the final data record.
     * 
     * @param inBuf input buffer of data 
     * @param inOff offset in the provided input buffer
     * @param inLen length of data to be processed
     * @param outBuf output buffer of data 
     * @param outOff offset in the provided output buffer
     * @return number of bytes copied to output buffer
     *
     * @exception IllegalStateException if this cipher is in a wrong state
     * (e.g., has not been initialized)
     * @exception IllegalBlockSizeException if this cipher is a block cipher,
     * no padding has been requested (only in encryption mode), and the total
     * input length of the data processed by this cipher is not a multiple of
     * block size
     * @exception ShortBufferException if the given output buffer is too small
     * to hold the result
     * @exception BadPaddingException if this cipher is in decryption mode,
     * and (un)padding has been requested, but the decrypted data is not
     * bounded by the appropriate padding bytes
     * @exception IllegalArgumentException if input is greater than the
     *            cipher with the given key can handle, or the output
     *            parameters are invalid
     */
    public int doFinal(byte[] inBuf, int inOff, int inLen, 
                       byte[] outBuf, int outOff)
        throws IllegalStateException, ShortBufferException,
               IllegalBlockSizeException, BadPaddingException {

        addToMessage(inBuf, inOff, inLen);

        if (outBuf == null || outOff < 0) {
            throw new IllegalArgumentException("output out of bounds");
        }

        int val = performRsa(messageToSign, 0, bytesInMessage, outBuf, outOff);

        try {
            init(mode, ckey);
        } catch (InvalidKeyException ike) {
            // Ignore, nothing to do
        }

        return val;
    }
}
