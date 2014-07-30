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
 * Implements RSAKey with methods to set and get RSA exponent
 * and modulus.
 */ 
class RSAKey implements Key {
    /** Local variable to hold the exponent. */
    byte[] exp = null; 

    /**
     * The number of bytes allocated to the modulus
     * is always a multiple of 8.
     */
    byte[] mod = null; 

    /**
     * Constructor for RSA public key.
     *
     * @param modulus modulus of key to process
     * @param modOffset offset of the modulus
     * @param modLen length of modulus in bytes
     * @param exponent exponent the key
     * @param expOffset offset of the exponent
     * @param expLen length of the exponent in bytes
     */
    RSAKey(byte[] modulus, int modOffset, int modLen,
           byte[] exponent, int expOffset, int expLen) {
        setModulus(modulus, modOffset, modLen);
        setExponent(exponent, expOffset, expLen);
    }

    /**
     * Compare the current key as a RSA private key. 
     * @param k the key to compare
     * @return true if the kesy are the same.
     */
    public boolean equals(RSAPrivateKey k) {
	    return equals((RSAKey) k);
    }
    
    /**
     * Compare the current key as a RSA public key. 
     * @param k the key to compare
     * @return true if the keys are the same.
     */
    public boolean equals(RSAPublicKey k) {
	    return equals((RSAKey) k);
    }
    
    /**
     * Compare the current key as a generic RSA key. 
     * @param k the key to compare
     * @return true if the keys are the same.
     */
    public boolean equals(RSAKey k) {
        byte[] kexp = new byte[exp.length];
        byte[] kmod = new byte[mod.length];

        if (this.getClass() != k.getClass()) {
            return false;
        }
        if (k.getExponent(kexp, (short) 0) != exp.length) {
            return false;
        }
        if (k.getModulus(kmod, (short) 0) != mod.length) {
            return false;
        }
        for (int i = 0; i < exp.length; i++) {
            if (kexp[i] != exp[i])
            return false;
        }
        for (int i = 0; i < mod.length; i++) {
            if (kmod[i] != mod[i])
            return false;
        }
        return true;
    }
    
    /**
     * Gets the type of the current key.
     * @return the type of the key
     */    
    public String getAlgorithm() {
	    return "RSA";
    }

    /** 
     * Returns the name of the encoding format for this key.
     *
     * @return the string "RAW".
     */
    public String getFormat() {	
        return "RAW"; 
    }

    /**
     * Returns the encoding of key.
     *
     * @return if OCSP is enabled, returns DER encoding of this key,
     *         otherwise returns null
     */
    public byte[] getEncoded() {
        return DEREncoder.encode(this);
    }

    // The next four are for RSA public/private keys
    /**
     * Get the exponent for the key.
     * @param buf output buffer to hold the exponent
     * @param off offset in the buffer to copy the exponent
     * @return length of the data copied
     */
    public short getExponent(byte[] buf, short off) {
        if (off + exp.length > buf.length) {
            return 0;
        }

        System.arraycopy(exp, 0, buf, off, exp.length);
        return ((short) exp.length);
    }
    
    /**
     * Get the length of modulus for the key.
     *
     * @return length of the modulus in bytes
     */
    public int getModulusLen() {
        return mod.length;
    }

    /**
     * Get the modulus for the key.
     * @param buf output buffer to hold the modulus
     * @param off offset in the buffer to copy the modulus
     * @return length of the data copied
     * @see #setModulus
     */
    public short getModulus(byte[] buf, short off) {
        if (off + mod.length > buf.length) {
            return ((short) 0);
        }
        System.arraycopy(mod, 0, buf, off, mod.length);
        return ((short) mod.length);
    }    

    /**
     * Set the exponent for the key.
     * @param buf input buffer which hold the exponent
     * @param off offset in the buffer
     * @param len length of the data to be copied
     * @see #getExponent
     * @exception IllegalArgumentException if exponent is too large
     */
    private void setExponent(byte[] buf, int off, int len) {
        exp = new byte[len];
        System.arraycopy(buf, off, exp, 0, len);
    }


    /**
     * Set the modulus for the key.
     * @param buf input buffer which hold the modulus
     * @param off offset in the buffer
     * @param len length of the data to be copied in bytes
     * @see #getModulus
     */
    private void setModulus(byte[] buf, int off, int len) {
        // move modulus len out to a multiple of 64 bits (8 bytes)
        int len8m = (len + 7) / 8 * 8;

        mod = new byte[len8m];
        System.arraycopy(buf, off, mod, len8m - len, len);
    }

    /**
     * Convert the key to a readable string.
     * @return string representation of key
     */
    public String toString() {
        return ("[" + (getModulusLen() * 8) + "-bit RSA key" +
            ", Exponent: 0x" + Util.hexEncode(exp) + 
            ", Modulus: 0x" + Util.hexEncode(mod) + "]");
    }
}
