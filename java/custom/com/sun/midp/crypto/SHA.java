/* Sha160.java --
   Copyright (C) 2001, 2002, 2006 Free Software Foundation, Inc.

This file is a part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or (at
your option) any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301
USA

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version.  */

package com.sun.midp.crypto;

import org.mozilla.internal.Sys;

/**
 * Implements the SHA-1 message digest algorithm.
 */
final class SHA extends MessageDigest {
    private static final int BLOCK_SIZE = 64; // inner block size in bytes

    private static final int[] w = new int[80];

    private long count;

    private byte[] buffer = new byte[BLOCK_SIZE];

    /** 160-bit interim result. */
    private int h0, h1, h2, h3, h4;

    /** Create SHA digest object. */
    protected SHA() {
        resetContext();
    }

    /**
     * Gets the message digest algorithm.
     * @return algorithm implemented by this MessageDigest object
     */
    public String getAlgorithm() {
        return "SHA-1";
    }

    /**
     * Gets the length (in bytes) of the hash.
     * @return byte-length of the hash produced by this object
     */
    public int getDigestLength() {
        return 20;
    }

    /**
     * Resets the MessageDigest to the initial state for further use.
     */
    public void reset() {
        count = 0L;
        for (int i = 0; i < BLOCK_SIZE; i++) {
            buffer[i] = 0;
        }

        resetContext();
    }

    /**
     * Accumulates a hash of the input data. This method is useful when
     * the input data to be hashed is not available in one byte array.
     * @param b input buffer of data to be hashed
     * @param offset offset within inBuf where input data begins
     * @param len length (in bytes) of data to be hashed
     * @see #doFinal(byte[], int, int, byte[], int)
     */
    public void update(byte[] b, int offset, int len) {
        int n = (int)(count % BLOCK_SIZE);
        count += len;
        int partLen = BLOCK_SIZE - n;
        int i = 0;

        if (len >= partLen) {
            Sys.copyArray(b, offset, buffer, n, partLen);
            transform(buffer, 0);
            for (i = partLen; i + BLOCK_SIZE - 1 < len; i += BLOCK_SIZE) {
              transform(b, offset + i);
            }

            n = 0;
        }

        if (i < len) {
            Sys.copyArray(b, offset + i, buffer, n, len - i);
        }
    }

    /**
     * Completes the hash computation by performing final operations
     * such as padding. The digest is reset after this call is made.
     *
     * @param buf output buffer for the computed digest
     *
     * @param offset offset into the output buffer to begin storing the digest
     *
     * @param len number of bytes within buf allotted for the digest
     *
     * @return the number of bytes placed into <code>buf</code>
     *
     * @exception DigestException if an error occurs.
     */
    public int digest(byte[] buf, int offset, int len) throws DigestException {
        if (len < getDigestLength()) {
            throw new DigestException("Buffer too short.");
        }

        // Pad remaining bytes in buffer
        int n = (int)(count % BLOCK_SIZE);
        int padding = (n < 56) ? (56 - n) : (120 - n);
        byte[] tail = new byte[padding + 8];
        // padding is always binary 1 followed by binary 0s
        tail[0] = (byte) 0x80;
        // save number of bits, casting the long to an array of 8 bytes
        long bits = count << 3;
        tail[padding++] = (byte)(bits >>> 56);
        tail[padding++] = (byte)(bits >>> 48);
        tail[padding++] = (byte)(bits >>> 40);
        tail[padding++] = (byte)(bits >>> 32);
        tail[padding++] = (byte)(bits >>> 24);
        tail[padding++] = (byte)(bits >>> 16);
        tail[padding++] = (byte)(bits >>> 8);
        tail[padding  ] = (byte) bits;

        update(tail, 0, tail.length); // last transform of a message

        buf[offset] = (byte)(h0 >>> 24);
        buf[offset + 1] = (byte)(h0 >>> 16);
        buf[offset + 2] = (byte)(h0 >>> 8);
        buf[offset + 3] = (byte)h0;
        buf[offset + 4] = (byte)(h1 >>> 24);
        buf[offset + 5] = (byte)(h1 >>> 16);
        buf[offset + 6] = (byte)(h1 >>> 8);
        buf[offset + 7] = (byte)h1;
        buf[offset + 8] = (byte)(h2 >>> 24);
        buf[offset + 9] = (byte)(h2 >>> 16);
        buf[offset + 10] = (byte)(h2 >>> 8);
        buf[offset + 11] = (byte)h2;
        buf[offset + 12] = (byte)(h3 >>> 24);
        buf[offset + 13] = (byte)(h3 >>> 16);
        buf[offset + 14] = (byte)(h3 >>> 8);
        buf[offset + 15] = (byte)h3;
        buf[offset + 16] = (byte)(h4 >>> 24);
        buf[offset + 17] = (byte)(h4 >>> 16);
        buf[offset + 18] = (byte)(h4 >>> 8);
        buf[offset + 19] = (byte)h4;

        reset(); // reset this instance for future re-use

        return getDigestLength();
    }

    /**
     * Clones the MessageDigest object.
     * @return a clone of this object
     */
    public Object clone() {
        SHA cpy = new SHA();

        cpy.h0 = this.h0;
        cpy.h1 = this.h1;
        cpy.h2 = this.h2;
        cpy.h3 = this.h3;
        cpy.h4 = this.h4;
        cpy.count = this.count;
        Sys.copyArray(this.buffer, 0, cpy.buffer, 0, this.buffer.length);

        return cpy;
    }

    private void transform(byte[] in, int offset) {
        int A = h0;
        int B = h1;
        int C = h2;
        int D = h3;
        int E = h4;
        int r, T;

        for (r = 0; r < 16; r++) {
            w[r] =  in[offset++]         << 24 |
                    (in[offset++] & 0xFF) << 16 |
                    (in[offset++] & 0xFF) << 8 |
                    (in[offset++] & 0xFF);
        }

        for (r = 16; r < 80; r++) {
            T = w[r - 3] ^ w[r - 8] ^ w[r - 14] ^ w[r - 16];
            w[r] = T << 1 | T >>> 31;
        }

        for (r = 0; r < 20; r++) { // rounds 0-19
            T = (A << 5 | A >>> 27) + ((B & C) | (~B & D)) + E + w[r] + 0x5A827999;
            E = D;
            D = C;
            C = B << 30 | B >>> 2;
            B = A;
            A = T;
        }

        for (r = 20; r < 40; r++) { // rounds 20-39
            T = (A << 5 | A >>> 27) + (B ^ C ^ D) + E + w[r] + 0x6ED9EBA1;
            E = D;
            D = C;
            C = B << 30 | B >>> 2;
            B = A;
            A = T;
        }

        for (r = 40; r < 60; r++) { // rounds 40-59
            T = (A << 5 | A >>> 27) + (B & C | B & D | C & D) + E + w[r] + 0x8F1BBCDC;
            E = D;
            D = C;
            C = B << 30 | B >>> 2;
            B = A;
            A = T;
        }

        for (r = 60; r < 80; r++) { // rounds 60-79
            T = (A << 5 | A >>> 27) + (B ^ C ^ D) + E + w[r] + 0xCA62C1D6;
            E = D;
            D = C;
            C = B << 30 | B >>> 2;
            B = A;
            A = T;
        }

        h0 = h0 + A;
        h1 = h1 + B;
        h2 = h2 + C;
        h3 = h3 + D;
        h4 = h4 + E;
    }

    private void resetContext() {
        // magic SHA-1/RIPEMD160 initialisation constants
        h0 = 0x67452301;
        h1 = 0xEFCDAB89;
        h2 = 0x98BADCFE;
        h3 = 0x10325476;
        h4 = 0xC3D2E1F0;
    }
}
