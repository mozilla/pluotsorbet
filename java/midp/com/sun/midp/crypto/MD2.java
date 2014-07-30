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
 * Implements the MD2 hashing algorithm as described in IETF RFC 1321
 * (see http://www.ietf.org/rfc/rfc1321.txt)
 */ 
final class MD2 extends MessageDigest {
    /*
     * The compute intensive operations are implemented in C
     * based on the OpenSSL MD2 code (from pilotSSLeay). Here we
     * replicate the state the C code needs.
     */

    /** State needed for MD2 hash. */
    private int[] num = new int[1];
    /** State needed for MD2 hash. */
    private byte[] data = new byte[16];
    /** State needed for MD2 hash. */
    private int[] cksm = new int[16];
    /** State needed for MD2 hash. */
    private int[] state = new int[16];
    
    /** Create an MD2 digest object. */
    MD2() {
        reset();
    }
    
    /** 
     * Gets the message digest algorithm.
     * @return algorithm implemented by this MessageDigest object
     */
    public String getAlgorithm() { 
	return "MD2";
    }

    /** 
     * Gets the length (in bytes) of the hash.
     * @return byte-length of the hash produced by this object
     */
    public int getDigestLength() {
	return 16;
    }
    
    /** 
     * Resets the MessageDigest to the initial state for further use.
     */
    public void reset() {
	num[0] = 0;

	for (int i = 0; i < 16; i++) {
	    data[i] = (byte) 0;
	    cksm[i] = 0;
	    state[i] = 0;
	}
    }

    /**
     * Accumulates a hash of the input data. Continues an MD2
     * message-digest operation, processing another message
     * block, and updating the internal context.
     * @param inBuf input buffer of data to be hashed
     * @param inOff offset within inBuf where input data begins
     * @param inLen length (in bytes) of data to be hashed
     * @see #doFinal(byte[], int, int, byte[], int)
     */
    public void update(byte[] inBuf, int inOff, int inLen) {
	if (inLen == 0) {
	    return;
	}
	
	// check parameters to prevent VM from crashing
	int test = inBuf[inOff] + inBuf[inLen - 1] + inBuf[inOff + inLen - 1];
	
	nativeUpdate(inBuf, inOff, inLen, state, num, cksm, data);
    }
    
    /**
     * Accumulates a hash of the input data. Continues an MD2
     * message-digest operation, processing another message
     * block, and updating the internal context.
     * @param inBuf input buffer of data to be hashed
     * @param inOff offset within inBuf where input data begins
     * @param inLen length (in bytes) of data to be hashed
     * @param state internal hash state
     * @param num internal hash state
     * @param cksm internal hash state
     * @param data internal hash state
     */
    private static native void nativeUpdate(byte[] inBuf, int inOff,
	    int inLen, int[] state, int[] num, int[] cksm, byte[] data);

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

        // check the parameters to prevent a VM crash
        int test = buf[offset] + buf[offset + getDigestLength() - 1];
        
        nativeFinal(null, 0, 0, buf, offset, state, num, cksm, data);
        return getDigestLength();
    }

    /** 
     * Generates a hash of all/last input data. Completes and returns the
     * hash compuatation after performing final operations such as padding.
     * The MessageDigest object is reset after this call. 
     * @param inBuf input buffer of data to be hashed
     * @param inOff offset within inBuf where input data begins
     * @param inLen length (in bytes) of data to be hashed
     * @param outBuf output buffer where the hash should be placed
     * @param outOff offset within outBuf where the resulting hash begins
     * @param state internal hash state
     * @param num internal hash state
     * @param cksm internal hash state
     * @param data internal hash state
     */ 
    private static native void nativeFinal(byte[] inBuf, int inOff, 
			   int inLen, byte[] outBuf, int outOff, 
			   int[] state, int[] num, int[] cksm, byte[] data);
    
    /** 
     * Clones the MessageDigest object.
     * @return a clone of this object
     */
    public Object clone() {
	MD2 cpy = new MD2();
	
	System.arraycopy(this.state, 0, cpy.state, 0, 16);
	System.arraycopy(this.num, 0, cpy.num, 0, 1);
	System.arraycopy(this.cksm, 0, cpy.cksm, 0, 16);
	System.arraycopy(this.data, 0, cpy.data, 0, 16);
	return cpy;
    }
}




