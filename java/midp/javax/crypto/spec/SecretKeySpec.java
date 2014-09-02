/*
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

/*
 * NOTE:
 * Because of various external restrictions (i.e. US export
 * regulations, etc.), the actual source code can not be provided
 * at this time. This file represents the skeleton of the source
 * file, so that javadocs of the API can be created.
 */

package javax.crypto.spec;

import com.sun.j2me.crypto.Util;

import java.security.Key;
import java.security.spec.KeySpec;

/** 
 * This class specifies a secret key in a provider-independent fashion.
 *
 * <p>It can be used to construct a <code>SecretKey</code> from a byte array.
 *
 * <p>This class is only useful for raw secret keys that can be represented as
 * a byte array and have no key parameters associated with them, e.g., DES or
 * Triple DES keys.
 *
 */
public class SecretKeySpec implements KeySpec, Key {
    /** 
     * The secret key.
     */
    private byte[] keyData;

    /** 
     * The name of the algorithm associated with this key.
     */
    private String algorithm;
    
    /** 
     * Constructs a secret key from the given byte array, using the first
     * <code>len</code> bytes of <code>key</code>, starting at
     * <code>offset</code> inclusive.
     *
     * <p> The bytes that constitute the secret key are
     * those between <code>key[offset]</code> and
     * <code>key[offset+len-1]</code> inclusive.
     *
     * <p>This constructor does not check if the given bytes indeed specify a
     * secret key of the specified algorithm. For example, if the algorithm is
     * DES, this constructor does not check if <code>key</code> is 8 bytes
     * long, and also does not check for weak or semi-weak keys.
     * In order for those checks to be performed, an algorithm-specific key
     * specification class 
     * must be used.
     *
     * @param key the key material of the secret key.
     * @param offset the offset in <code>key</code> where the key material
     * starts.
     * @param len the length of the key material.
     * @param algorithm the name of the secret-key algorithm to be associated
     * with the given key material.
     * See Appendix A in the 
     * Java Cryptography Extension Reference Guide
     * for information about standard algorithm names.
     */
    public SecretKeySpec(byte[] key, int offset, int len, String algorithm) { 
        keyData = new byte[len];
        System.arraycopy(key, offset, keyData, 0, len);
        this.algorithm = algorithm.toUpperCase();
    }

    /** 
     * Returns the name of the algorithm associated with this secret key.
     *
     * @return the secret key algorithm.
     */
    public String getAlgorithm() { 
        return algorithm;
    }

    /** 
     * Returns the name of the encoding format for this secret key.
     *
     * @return the string "RAW".
     */
    public String getFormat() {	
        return "RAW"; 
    }

    /** 
     * Returns the key material of this secret key.
     *
     * @return the key material
     */
    public byte[] getEncoded() { 
        return Util.cloneArray(keyData);
    }
}
