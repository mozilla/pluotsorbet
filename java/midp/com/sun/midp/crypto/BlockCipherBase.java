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
 * Class providing common functionality for DES ciphers.
 */
abstract class BlockCipherBase extends Cipher {

    /** Block size. */
    private int blockSize;

    /** ENCRYPT or DECRYPT. */
    protected int mode;

    /** The padder. */
    protected Padder padder;

    /** Contains the data that is not encrypted or decrypted yet. */
    protected byte[] holdData;

    /** The holding buffer counter. */
    protected int holdCount;

    /** Indicates if this cipher object has been updated or not. */
    protected boolean isUpdated;

    /** Saved state variable. */
    private byte[] savedHoldData;

    /** Saved state variable. */
    private int savedHoldCount;

    /** Saved state variable. */
    private boolean savedIsUpdated;

    /** Initial vector. */
    protected byte[] IV;

    /** True in decryption with padder mode. */
    private boolean keepLastBlock;

    /**
     * Constructor.
     *
     * @param blockSize block size
     */
    protected BlockCipherBase(int blockSize) {
        this.blockSize = blockSize;

        holdData = new byte[blockSize];
        mode = MODE_UNINITIALIZED;
    }

    /**
     * Sets the padder.
     *
     * @param padding Upper case padding arg from the transformation given to
     *               Cipher.getInstance
     *
     * @exception NoSuchPaddingException if <code>padding</code>
     * contains a padding scheme that is not available.
     */
    protected void setPadding(String padding)
        throws NoSuchPaddingException {
        
        if (padding.equals("") || padding.equals("PKCS5PADDING")) {
            padder = new PKCS5Padding(blockSize);
        } else if (!padding.equals("NOPADDING")) {
            throw new NoSuchPaddingException(padding);
        }
    }

    /**
     * Initializes a cipher object with the key and sets
     * encryption or decryption mode.
     *
     * @param mode either encryption or decription mode
     * @param keyAlgorithm algorithm the key should have
     * @param key key to be used
     * @param needIV true if this algorithm accepts IV parameter
     * @param params the algorithm parameters
     *
     * @exception InvalidKeyException if the given key
     * is inappropriate for initializing this cipher
     * @exception InvalidAlgorithmParameterException
     * if the given algorithm parameters are inappropriate for this cipher
     */
    protected void doInit(int mode, String keyAlgorithm, Key key,
            boolean needIV, CryptoParameter params)
            throws InvalidKeyException, InvalidAlgorithmParameterException {

        byte[] IV;

        if (needIV) {
            if (params == null) {
                if (mode == Cipher.DECRYPT_MODE) {
                    throw new InvalidAlgorithmParameterException();
                }
                IV = new byte[blockSize];
            } else {
                if (! (params instanceof IvParameter)) {
                    throw new InvalidAlgorithmParameterException();
                }
                IV = Util.cloneArray(((IvParameter)params).getIV());
                if (IV.length != blockSize)  {
                    throw new InvalidAlgorithmParameterException();
                }
            }
        } else {
            if (params != null) {
                throw new InvalidAlgorithmParameterException();
            }
            IV = null;
        }

        if (!(key instanceof SecretKey &&
              keyAlgorithm.equals(key.getAlgorithm()))) {
            throw new InvalidKeyException();
        }

        initKey(key.getEncoded(), mode);

        holdCount = 0;
        isUpdated = false;
        this.mode = mode;
        this.IV = IV;
        keepLastBlock = mode == Cipher.DECRYPT_MODE && padder != null;
    }

    /**
     * Encrypts or decrypts data in a single-part operation, or finishes a
     * multiple-part operation. The data is encrypted or decrypted,
     * depending on how this cipher was initialized.
     *
     * @param in the input buffer
     * @param offset the offset in <code>input</code> where the input
     * starts
     * @param len the input length
     * @param out the buffer for the result
     * @param outOffset the offset in <code>output</code> where the result
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
     */
    public int doFinal(byte in[], int offset, int len,
                       byte out[], int outOffset)
        throws IllegalStateException, IllegalBlockSizeException,
	           ShortBufferException, BadPaddingException {

        Util.checkBounds(in, offset, len, out, outOffset);

        if (mode == MODE_UNINITIALIZED)  {
            throw new IllegalStateException();
        }

        if (len == 0 && ! isUpdated) {
            return 0;
        }

        boolean encrypt = mode == Cipher.ENCRYPT_MODE;

        // calculate the size of the possible out buffer
        int expectedSize = len + holdCount;
        int delta = expectedSize % blockSize;
        if (delta != 0) {
            if (! encrypt || (encrypt && padder == null)) {
                throw new IllegalBlockSizeException();
            }
            expectedSize += blockSize - delta;
        } else
        if (encrypt && padder != null) {
            expectedSize += blockSize;
        }

        int excess = outOffset + expectedSize - out.length;

        // padder may remove up to blockSize bytes after decryption
        if (excess > (keepLastBlock ? blockSize : 0)) {
            throw new ShortBufferException();
        }

        if (keepLastBlock && excess > 0) {
            // after unpadding data may not fit into output buffer
            saveState();
        }

        int counter = update(in, offset, len, out, outOffset);

        if (padder != null) {
            if (encrypt) {
                if (padder.pad(holdData, holdCount) != 0) {
                    processBlock(out, outOffset + counter);
                    counter += blockSize;
                }
            } else {
                byte[] lastBlock = new byte[blockSize];
                processBlock(lastBlock, 0);
                int tail = blockSize - padder.unPad(lastBlock, blockSize);
                if (outOffset + counter + tail > out.length) {
                    restoreState();
                    throw new ShortBufferException();
                }
                System.arraycopy(lastBlock, 0,
                                 out, outOffset + counter, tail);
                counter += tail;
            }
        }

        holdCount = 0;
        return counter;
    }

    /**
     * Continues a multiple-part encryption or decryption operation
     * (depending on how this cipher was initialized), processing another data
     * part.
     * @param in the input buffer
     * @param offset the offset in <code>input</code> where the input
     * starts
     * @param len the input length
     * @param out the buffer for the result
     * @param outOffset the offset in <code>output</code> where the result
     * is stored
     *
     * @return the number of bytes stored in <code>output</code>
     *
     * @exception IllegalStateException if this cipher is in a wrong state
     * (e.g., has not been initialized)
     * @exception ShortBufferException if the given output buffer is too small
     * to hold the result
     */
    public int update(byte in[], int offset, int len,
                      byte out[], int outOffset)
        throws IllegalStateException, ShortBufferException {

        Util.checkBounds(in, offset, len, out, outOffset);

        if (mode == MODE_UNINITIALIZED)  {
            throw new IllegalStateException();
        }

        if (len == 0)  {
            return 0;
        }

        if (((holdCount + len) / blockSize -
             (keepLastBlock ? 1 : 0)) * blockSize >
            out.length - outOffset) {
            throw new ShortBufferException();
        }

        isUpdated = true;

        if (in == out) {
            in = new byte[len];
            System.arraycopy(out, offset, in, 0, len);
            offset = 0;
        }

        int counter = 0;
        while (true)  {

            int got;
            System.arraycopy(in, offset, holdData, holdCount,
                             got = Math.min(blockSize - holdCount, len));
            offset += got;
            len -= got;
            holdCount += got;

            if (holdCount < blockSize || (len == 0 && keepLastBlock)) {
                return counter;
            }

            processBlock(out, outOffset);

            counter   += blockSize;
            outOffset += blockSize;
        }
    }

    /**
     * Returns the initialization vector (IV) in a new buffer.
     * This is useful in the case where a random IV was created.
     * @return the initialization vector in a new buffer,
     * or <code>null</code> if the underlying algorithm does
     * not use an IV.
     */
    public byte[] getIV() {
        return IV == null ? null : Util.cloneArray(IV);
    }

    /**
     * Saves cipher state.
     */
    protected void saveState() {
        savedHoldCount = holdCount;
        savedHoldData = holdCount == 0 ?
                        holdData :
                        Util.cloneArray(holdData);
        savedIsUpdated = isUpdated;
    }

    /**
     * Restores cipher state.
     */
    protected void restoreState() {
        holdCount = savedHoldCount;
        holdData = savedHoldData;
        isUpdated = savedIsUpdated;
    }

    /**
     * Depending on the mode, either encrypts or decrypts data block.
     * @param out will contain the result of encryption
     * or decryption operation
     * @param offset is the offset in out
     */
    abstract void processBlock(byte[] out, int offset);

    /**
     * Initializes key.
     * @param data key data
     * @param mode cipher mode
     * @exception InvalidKeyException if the given key is inappropriate
     * for this cipher
     */
    abstract void initKey(byte[] data, int mode) throws InvalidKeyException;
}
