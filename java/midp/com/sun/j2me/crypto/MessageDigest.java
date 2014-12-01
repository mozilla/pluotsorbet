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

package com.sun.j2me.crypto;

/**
 * Provides applications the functionality of a message digest algorithm
 */

public class MessageDigest {
    /**
     * Message digest implementation.
     */
    com.sun.midp.crypto.MessageDigest messageDigest;

    public MessageDigest(String algorithm) throws NoSuchAlgorithmException {
        try {
            messageDigest = com.sun.midp.crypto.MessageDigest.getInstance(algorithm);
        }
        catch (com.sun.midp.crypto.NoSuchAlgorithmException e) {
            throw new NoSuchAlgorithmException(e.getMessage());
        }
    }

    public void reset() {
        messageDigest.reset();
    }

    public void update(byte[] input, int offset, int len) {
        messageDigest.update(input, offset, len);
    }

    public void digest(byte[] buf, int offset, int len) throws DigestException {
        try {
            messageDigest.digest(buf, offset, len);
        } catch (com.sun.midp.crypto.DigestException e) {
            throw new DigestException(e.getMessage());
        }
    }

    public int getDigestLength() {
        return messageDigest.getDigestLength();
    }

}
