/*
 *   
 *
 * Portions Copyright  2000-2009 Sun Microsystems, Inc. All Rights
 * Reserved.  Use is subject to license terms.
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
 *
 * Copyright (c) 1995-2005 The Cryptix Foundation Limited.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 *   1. Redistributions of source code must retain the copyright notice,
 *      this list of conditions and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in
 *      the documentation and/or other materials provided with the
 *      distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE CRYPTIX FOUNDATION LIMITED AND
 * CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE CRYPTIX FOUNDATION LIMITED OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 * IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.sun.midp.crypto;

/**
 * This class is an implementation of AES cipher in ECB mode.
 */
public class AES_ECB extends BlockCipherBase {

    /** Substitution values. */
    private static final byte[] SBox = Util.unpackBytes(
            "c|w{rkoE0\001g+~W+vJ\002I}zYGp-T\042/\034$r@7}\023&6?wL4%" +
            "eqqX1\025\004G#C\030\026\005\032\007\022\000bk'2u\011\003" +
            ",\032\033nZ\040R;V3)c/\004SQ\000m\040|1[jK>9JLXOPo*{CM3" +
            "\005Ey\002P<\037(Q#@\017\022\0358u<6Z!\020sRM\014\023l_" +
            "\027D\027D'~=d]\031s`\001O\134\042*\020\010Fn8\024^^\013[" +
            "`2:\012I\006$\134BS,b\021\025dygH7m\015UN)lVtjez.\010:x%." +
            "\034&4Fh]t\037K=\013\012p>5fH\003v\016a5W9\006A\035\036ax" +
            "\030\021iY\016\024\033\036\007iNU(_\014!\011\015?fBhA\031" +
            "-\0170T;\026\020a]|{X\013U\134\0042Ufp#a:O'\031\040Ye\000" +
            "wf2\012>\034\021\134g;^\021\005");

    /** Inverse substitution table. */
    private static final byte[] ISBox;

    static {
        ISBox = new byte[256];
        for (int i = 0; i < 256; i++) {
            ISBox[SBox[i] & 0xff] = (byte) i;
        }
    }

    /** Precalculated table for matrix multiplication. */
    private int[] SB0;
    /** Precalculated table for matrix multiplication. */
    private int[] SB1;
    /** Precalculated table for matrix multiplication. */
    private int[] SB2;
    /** Precalculated table for matrix multiplication. */
    private int[] SB3;

    /**
    * Round constants.
    */
    private static final byte[] Rcon = Util.unpackBytes(
            "\000\001\002\004\010\020\040@\000\0336l\000\002");

    /** AES ciphers encrypt/decrypt in block size of 16 bytes. */
    static final int BLOCK_SIZE = 16;

    /** Number of columns (32-bit words) comprising the state.	*/
    private static final int Nb = 4;

    /**
     * Number of 32-bit words comprising the key (4, 6, 8 for 128, 192
     * and 256 bits).
     */
    private int Nk;

    /** Number of rounds (10, 12, 14). */
    private int Nr;

    /**
     * Key Schedule. Length depends on Block and Key length.
     * Block length = 128 bits or 16 bytes. For 128 bit key
     * W.length = Nb * (Nr + 1) == 4(10 + 1) = 44 Words = 44 * 4 bytes =
     * 176 bytes
     */
    private int[] W;

    /** Internal buffer. */
    protected byte[] state;

    /**
     * Constructor.
     */
    public AES_ECB()  {
        super(BLOCK_SIZE);
        state = new byte[BLOCK_SIZE];
        SB0 = new int[256];
        SB1 = new int[256];
        SB2 = new int[256];
        SB3 = new int[256];
    }

    /**
     * Called by the factory method to set the mode and padding parameters.
     * Need because Class.newInstance does not take args.
     *
     * @param mode the mode parsed from the transformation parameter of
     *             getInstance and upper cased
     * @param padding the paddinge parsed from the transformation parameter of
     *                getInstance and upper cased
     */
    protected void setChainingModeAndPadding(String mode, String padding)
            throws NoSuchPaddingException {
        // Note: The chaining mode is implicitly set by using this class.

        setPadding(padding);
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

        doInit(mode, "AES", key, false, null);
    }

    /**
     * Initializes key.
     * @param data key data
     * @param mode cipher mode
     * @exception InvalidKeyException if the given key is inappropriate
     * for this cipher
     */
    void initKey(byte[] data, int mode) throws InvalidKeyException {

        // Min key 128 bits, max key 256 bits
        if (data.length != 16 && data.length != 24 && data.length != 32) {
            throw new InvalidKeyException();
        }

        Nk = data.length >> 2;
        Nr = 6 + Nk;

        int row = 0x0e090d0b;
        byte[] box = ISBox;
        if (mode == Cipher.ENCRYPT_MODE) {
            row = 0x02010103;
            box = SBox;
        }

        if (multiply(row, box[0]) != SB0[0]) {
            for (int i = 0; i < 256; i++) {
                int j;
                SB0[i] = j = multiply(row, box[i]);
                SB1[i] = (j >>> 8) | (j << 24);
                SB2[i] = (j >>> 16) | (j << 16);
                SB3[i] = (j >>> 24) | (j << 8);
            }
        }

        KeyExpansion(data, mode);
    }

    /**
     * Calculates values for matrix multiplication.
     * @param a represents a matrix column (4 bytes).
     * @param b multiplier
     * @return result of multiplication.
     */
    private static int multiply(int a, int b)  {
        int result = 0;
        b &= 0xff;
        for (int i = 0; i < 4; i++) {
            result ^= ((a >> i) & 0x01010101) * b;
            b = b < 128 ? b << 1 : (b << 1) ^ 0x11b;
        }
        return result;
    }

    /**
     * Depending on the mode, either encrypts or decrypts data block.
     * @param out will contain the result of encryption
     * or decryption operation
     * @param offset is the offset in out
     */
    protected void processBlock(byte[] out, int offset) {

        holdCount = 0;

        if (mode == Cipher.ENCRYPT_MODE)  {
            cipherBlock();
        } else {
            decipherBlock();
        }

        System.arraycopy(state, 0, out, offset, BLOCK_SIZE);
    }

    /**
     * Performs the encryption of data.
     */
    protected void cipherBlock() {

        int t0 = Util.getInt(holdData, 0) ^ W[0];
        int t1 = Util.getInt(holdData, 4) ^ W[1];
        int t2 = Util.getInt(holdData, 8) ^ W[2];
        int t3 = Util.getInt(holdData, 12) ^ W[3];

        int j = 4;
        for (int i = 1; i < Nr; i++) {
            int v0, v1, v2;
            t0 = SB0[(v0 = t0) >>> 24] ^ SB1[(v1 = t1) >>> 16 & 0xff] ^
                 SB2[(v2 = t2) >>> 8 & 0xff] ^ SB3[t3 & 0xff] ^ W[j];
            t1 = SB0[v1 >>> 24] ^ SB1[v2 >>> 16 & 0xff] ^
                 SB2[t3 >>> 8 & 0xff] ^ SB3[v0 & 0xff] ^ W[j + 1];
            t2 = SB0[v2 >>> 24] ^ SB1[t3 >>> 16 & 0xff] ^
                 SB2[v0 >>> 8 & 0xff] ^ SB3[v1 & 0xff] ^ W[j + 2];
            t3 = SB0[t3 >>> 24] ^ SB1[v0 >>> 16 & 0xff] ^
                 SB2[v1 >>> 8 & 0xff] ^ SB3[v2 & 0xff] ^ W[j + 3];
            j += 4;
        }

        int k;
        byte out[];
        (out = state)[0] = (byte)(SBox[t0 >>> 24] ^ (k = W[j]) >>> 24);
        out[1] = (byte)(SBox[t1 >>> 16 & 0xff] ^ k >>> 16);
        out[2] = (byte)(SBox[t2 >>> 8 & 0xff] ^ k >>> 8);
        out[3] = (byte)(SBox[t3 & 0xff] ^ k);
        out[4] = (byte)(SBox[t1 >>> 24] ^ (k = W[j + 1]) >>> 24);
        out[5] = (byte)(SBox[t2 >>> 16 & 0xff] ^ k >>> 16);
        out[6] = (byte)(SBox[t3 >>> 8 & 0xff] ^ k >>> 8);
        out[7] = (byte)(SBox[t0 & 0xff] ^ k);
        out[8] = (byte)(SBox[t2 >>> 24] ^ (k = W[j + 2]) >>> 24);
        out[9] = (byte)(SBox[t3 >>> 16 & 0xff] ^ k >>> 16);
        out[10] = (byte)(SBox[t0 >>> 8 & 0xff] ^ k >>> 8);
        out[11] = (byte)(SBox[t1 & 0xff] ^ k);
        out[12] = (byte)(SBox[t3 >>> 24] ^ (k = W[j + 3]) >>> 24);
        out[13] = (byte)(SBox[t0 >>> 16 & 0xff] ^ k >>> 16);
        out[14] = (byte)(SBox[t1 >>> 8 & 0xff] ^ k >>> 8);
        out[15] = (byte)(SBox[t2 & 0xff] ^ k);
    }

    /**
     * Performs the decryption of data.
     */
    protected void decipherBlock() {

        int j;
        int t0 = Util.getInt(holdData, 0) ^ W[j = Nr * 4];
        int t1 = Util.getInt(holdData, 4) ^ W[j + 1];
        int t2 = Util.getInt(holdData, 8) ^ W[j + 2];
        int t3 = Util.getInt(holdData, 12) ^ W[j + 3];

        for (int i = 1; i < Nr; i++) {
            int v0, v1, v2;
            t0 = SB0[(v0 = t0) >>> 24] ^ SB1[t3 >>> 16 & 0xff] ^
                 SB2[(v2 = t2) >>> 8 & 0xff] ^
                 SB3[(v1 = t1) & 0xff] ^ W[j = j - 4];
            t1 = SB0[v1 >>> 24] ^ SB1[v0 >>> 16 & 0xff] ^
                 SB2[t3 >>> 8 & 0xff] ^ SB3[v2 & 0xff] ^ W[j + 1];
            t2 = SB0[v2 >>> 24] ^ SB1[v1 >>> 16 & 0xff] ^
                 SB2[v0 >>> 8 & 0xff] ^ SB3[t3 & 0xff] ^ W[j + 2];
            t3 = SB0[t3 >>> 24] ^ SB1[v2 >>> 16 & 0xff] ^
                 SB2[v1 >>> 8 & 0xff] ^ SB3[v0 & 0xff] ^ W[j + 3];
        }

        int k;
        byte out[];
        (out = state)[0] = (byte)(ISBox[t0 >>> 24] ^ (k = W[0])>>> 24);
        out[1] = (byte)(ISBox[t3 >>> 16 & 0xff] ^ k >>> 16);
        out[2] = (byte)(ISBox[t2 >>> 8 & 0xff] ^ k >>> 8);
        out[3] = (byte)(ISBox[t1 & 0xff] ^ k);
        out[4] = (byte)(ISBox[t1 >>> 24] ^ (k = W[1]) >>> 24);
        out[5] = (byte)(ISBox[t0 >>> 16 & 0xff] ^ k >>> 16);
        out[6] = (byte)(ISBox[t3 >>> 8 & 0xff] ^ k >>> 8);
        out[7] = (byte)(ISBox[t2 & 0xff] ^ k);
        out[8] = (byte)(ISBox[t2 >>> 24] ^ (k = W[2]) >>> 24);
        out[9] = (byte)(ISBox[t1 >>> 16 & 0xff] ^ k >>> 16);
        out[10] = (byte)(ISBox[t0 >>> 8 & 0xff] ^ k >>> 8);
        out[11] = (byte)(ISBox[t3 & 0xff] ^ k);
        out[12] = (byte)(ISBox[t3 >>> 24] ^ (k = W[3]) >>> 24);
        out[13] = (byte)(ISBox[t2 >>> 16 & 0xff] ^ k >>> 16);
        out[14] = (byte)(ISBox[t1 >>> 8 & 0xff] ^ k >>> 8);
        out[15] = (byte)(ISBox[t0 & 0xff] ^ k);
    }

    /**
     * Generates KeySchedule.
     * @param data key data
     * @param mode cipher mode
     */
    private void KeyExpansion(byte[] data, int mode)  {

        byte[] W = new byte[Nb * (Nr + 1) << 2];

        int diff;
        System.arraycopy(data, 0, W, 0, (diff = Nk << 2));

        int round = 1;
        for (int i = Nk; i < Nb * (Nr + 1); i++) {

            int v = i << 2;

            if (i % Nk == 0) {
                int u = v - 1;
                for (int j = 0; j < 4; j++, v++) {
                    W[v] = (byte)((W[v - diff] ^
                            SBox[W[u - ((6 - j) & 3)] & 0xff]));
                }
                W[i << 2] ^= Rcon[round++];
            } else
            if (Nk > 6 && i % Nk == 4) {
                for (int j = 0; j < 4; j++, v++) {
                    W[v] = (byte) (W[v - diff] ^ SBox[W[v - 4] & 0xff]);
                }
            } else {
                for (int j = 0; j < 4; j++, v++) {
                    W[v] = (byte) (W[v - diff] ^ W[v - 4]);
                }
            }
        }

        int[] V = (this.W = new int[W.length >> 2]);
        for (int i = 0; i < V.length; i++) {
            V[i] = Util.getInt(W, i * 4);
        }

        if (mode == Cipher.DECRYPT_MODE) {
            for (int i = 4; i < Nr * 4; i++) {
                V[i] = SB0[SBox[W[i * 4] & 0xff] & 0xff] ^
                       SB1[SBox[W[i * 4 + 1] & 0xff] & 0xff] ^
                       SB2[SBox[W[i * 4 + 2] & 0xff] & 0xff] ^
                       SB3[SBox[W[i * 4 + 3] & 0xff] & 0xff];
            }
        }
    }
}
