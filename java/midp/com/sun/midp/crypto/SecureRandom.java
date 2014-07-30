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
 * Implements an abstract class that generalizes random number
 * generators.
 */ 
public abstract class SecureRandom {
    /** Identifies a utility pseudo random number generation algorithm. */
    public static final byte ALG_PSEUDO_RANDOM = 1;
    
    /** 
     * Identifies a cryptographically secure random number generation
     * algorithm.
     */
    public static final byte ALG_SECURE_RANDOM = 2;
    
    /**
     * Protected constructor for subclassing.
     */ 
    protected SecureRandom() {
    }

    /**
     * Creates a RandomData instance of the selected algorithm. <BR />
     * <P />
     * <B>WARNING:</B>  Requests for a secure random number generator
     * are currently redirected to a class that implements a weakly 
     * unpredictable source of random data. Licensees of this reference
     * implementation are strongly urged to link requests for 
     * ALG_SECURE_RANDOM to better generators that may be available
     * on their specific platforms.
     *
     * @param alg the desired random number generation algorithm, e.g. 
     * ALG_PSEUDO_RANDOM
     *
     * @return a RandomData instance implementing the selected algorithm.
     *
     * @exception NoSuchAlgorithmException if an unsupported algorithm is
     * requested. 
     */ 
    public static SecureRandom getInstance(byte alg)
            throws NoSuchAlgorithmException {
	switch (alg) {
	 case ALG_SECURE_RANDOM:
	    // return (new SRand());
	 case ALG_PSEUDO_RANDOM:
	    return (new PRand());
	 default:
	    throw new NoSuchAlgorithmException();
	}
    }    

    /**
     * Generates the next bytes of random data. <BR />
     * @param buf output buffer in which the random data is to be placed
     * @param off starting offset within buf for the random data
     * @param len number of bytes of random data to be placed in buf
     */ 
    public abstract void nextBytes(byte[] buf, int off, int len);
    
    /**
     * Seeds the random number generator. <BR />
     * @param buf input buffer containing the seed
     * @param off offset within buf where the seed starts
     * @param len number of bytes of seed data in buf
     */ 
    public abstract void setSeed(byte[] buf, int off, int len);
}
