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
 * Implements a pseudo random number generator.
 */ 
final class PRand extends SecureRandom {
    /** Local handle to message digest. */
    private static MessageDigest md = null;

    /**
     * For an arbitrary choice of the default seed, we use bits from the 
     * binary expansion of pi.
     * <p>
     * This seed is just an example implementation and NOT
     * considered for used in SECURE (unpredicable) protocols, for this class
     * to be considered a secure source of random data the seed MUST
     * be derived from unpredicatable data in a production
     * device at the native level.
     * (see IETF RFC 1750, Randomness Recommendations for Security,
     *  http://www.ietf.org/rfc/rfc1750.txt)
     */
    private static byte[] seed = {
	(byte) 0xC9, (byte) 0x0F, (byte) 0xDA, (byte) 0xA2,
	(byte) 0x21, (byte) 0x68, (byte) 0xC2, (byte) 0x34,
	(byte) 0xC4, (byte) 0xC6, (byte) 0x62, (byte) 0x8B,
	(byte) 0x80, (byte) 0xDC, (byte) 0x1C, (byte) 0xD1
    };

    /** buffer of random bytes */
    private static byte[] randomBytes;
    
    /** number of random bytes currently available */
    private static int bytesAvailable = 0;
    
    /** Constructor for random data. */
    public PRand() {
	if (md != null) 
	    return;
	
	try {
	    md = MessageDigest.getInstance("MD5");
	} catch (Exception e) {
	    throw new RuntimeException("MD5 missing");
	}

	randomBytes = new byte[seed.length];
	updateSeed();
    }
    
    /**
     * This does a reasonable job of producing unpredictable
     * random data by using a one way hash as a mixing function and
     * the current time in milliseconds as a source of entropy.
     * @param b buffer of input data
     * @param off offset into the provided buffer
     * @param len length of the data to be processed
     */ 
    public void nextBytes(byte[] b, int off, int len) {
	synchronized (md) {
	    int i = 0;
	    
	    while (true) {
		// see if we need to buffer more random bytes
		if (bytesAvailable == 0) {
		    md.update(seed, 0, seed.length);
                    try {
                        md.digest(randomBytes, 0, randomBytes.length);
                    } catch (DigestException de) {
                        // nothing to do
                    }

		    updateSeed();
		    bytesAvailable = randomBytes.length;
		}
		
		// hand out some of the random bytes from the buffer
		while (bytesAvailable > 0) {
		    if (i == len)
			return;
		    b[off + i] = randomBytes[--bytesAvailable];
		    i++;
		}
	    }
	}
    }

    /**
     * Perform a platform-defined procedure for obtaining random bytes and
     * store the obtained bytes into b, starting from index 0.
     * (see IETF RFC 1750, Randomness Recommendations for Security,
     *  http://www.ietf.org/rfc/rfc1750.txt)
     * @param b array that receives random bytes
     * @param nbytes the number of random bytes to receive, must not be less than size of b
     * @return true if successful
     */
    private native static boolean getRandomBytes(byte[] b, int nbytes);

    /**
     * Set the random number seed.
     * @param b initial data to use as the seed 
     * @param off offset into the provided buffer
     * @param len length of the data to be used
     */
    public void setSeed(byte[] b, int off, int len) {
	int j = 0;

	if ((len <= 0) || (b.length < (off + len)))
	    return;
	for (int i = 0; i < seed.length; i++, j++) {
	    if (j == len) j = 0;
	    seed[i] = b[off + j];
	}
    }
    
    /**
     * This does a reasonable job of producing unpredictable
     * random data by using a one way hash as a mixing function and
     * the current time in milliseconds as a source of entropy for the seed.
     * This method assumes the original seed data is unpredicatble.
     */
    private void updateSeed() {
        byte[] tmp = new byte[8];

        boolean haveSeed = getRandomBytes(tmp, tmp.length);

        if (!haveSeed) {
            throw new RuntimeException("could not obtain a random seed");
        }

        md.update(seed, 0, seed.length);
        md.update(tmp, 0, tmp.length);
        try {
            md.digest(seed, 0, seed.length);
        } catch (DigestException de) {
            // nothing to do
        }
    }
}
