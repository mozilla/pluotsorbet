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

package com.sun.satsa.crypto;

import com.sun.satsa.util.TLV;
import com.sun.satsa.util.TLVException;
import com.sun.satsa.util.Utils;

import java.security.PublicKey;
import java.security.spec.KeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

/**
 * This class represents an RSA public key.
 */
public class RSAPublicKey implements PublicKey {

    /** OID for RSA crypto algorithm, 1.2.840.113549.1.1.1. */
    private static byte[] RSA_OID = {0x2A, (byte) 0x86, 0x48, (byte)0x86,
                                     (byte)0xF7, 0x0D, 0x01, 0x01, 0x01};
    /** Key object. */
    private com.sun.j2me.crypto.RSAPublicKey key;

    /** Key length in bytes. */
    private int keyLen;

    /** Specification of the key material. */
    private X509EncodedKeySpec keySpec;

    /**
     * Constructs an RSAPublicKey object.
     * @param keySpec specification of the key material
     * @throws InvalidKeySpecException if key specification is invalid
     */
    public RSAPublicKey(KeySpec keySpec) throws InvalidKeySpecException {

        if (! (keySpec instanceof X509EncodedKeySpec)) {
            throw new InvalidKeySpecException();
        }

        this.keySpec = (X509EncodedKeySpec) keySpec;

        byte[] data = getEncoded();

        /*
         * SubjectPublicKeyInfo { ALGORITHM : IOSet} ::= SEQUENCE {
         *       algorithm        AlgorithmIdentifier {{IOSet}},
         *       subjectPublicKey BIT STRING
         *  }
         *
         *  AlgorithmIdentifier  ::=  SEQUENCE  {
         *       algorithm               OBJECT IDENTIFIER,
         *       parameters              ANY DEFINED BY algorithm OPTIONAL
         *  }
         *
         */

        try {
            TLV t = new TLV(data, 0);

            t = t.child;    // AlgorithmIdentifier
            if (! Utils.byteMatch(data, t.child.valueOffset, t.child.length,
                                 RSA_OID, 0, RSA_OID.length)) {
                throw new InvalidKeySpecException(
                        "Invalid algorithm identifier");
            }

            t = t.next;     // subjectPublicKey

            /*
             *  RSAPublicKey ::= SEQUENCE {
             *      modulus            INTEGER, -- n
             *      publicExponent     INTEGER  -- e --
             *  }
             */

            // the first byte of value in BIT STRING is the number of
            // unused bits
            t = new TLV(data, t.valueOffset + 1);

            t = t.child;    // modulus

            int offset = t.valueOffset;
            int len = t.length;
            while (data[offset] == 0) {
                offset++;
                len--;
            }

            keyLen = ((len + 7) / 8) * 8;

            t = t.next;

            key = new com.sun.j2me.crypto.RSAPublicKey(data, offset, len,
                      data, t.valueOffset, t.length);
        } catch (InvalidKeySpecException ikse) {
            throw ikse;
        } catch (NullPointerException npe) {
            throw new InvalidKeySpecException();
        } catch (TLVException tlve) {
            throw new InvalidKeySpecException();
        }
    }

    /**
     * Returns key object.
     * @return key object
     */
    public com.sun.j2me.crypto.RSAPublicKey getKey() {
        return key;
    }

    /**
     * Returns the size of key in bytes.
     * @return size of key in bytes
     */
    public int getKeySize() {
        return keyLen;
    }

    /**
     * Returns the standard algorithm name for this key.
     * @return the name of the algorithm associated with this key.
     */
    public String getAlgorithm() {
        return "RSA";
    }

    /**
     * Returns the name of the primary encoding format of this key.
     * @return the primary encoding format of the key.
     */
    public String getFormat() {
        return keySpec.getFormat();
    }

    /**
     * Returns the key in its primary encoding format, or null
     * if this key does not support encoding.
     * @return the encoded key.
     */
    public byte[] getEncoded() {
        return keySpec.getEncoded();
    }
}
