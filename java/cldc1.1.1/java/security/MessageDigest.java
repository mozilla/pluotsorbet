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

package java.security;

/**
 * This MessageDigest class provides applications the functionality of a
 * message digest algorithm, such as MD5 or SHA.
 * Message digests are secure one-way hash functions that take arbitrary-sized
 * data and output a fixed-length hash value.
 *
 * <p>A <code>MessageDigest</code> object starts out initialized. The data is 
 * processed through it using the <code>update</code>
 * method. At any point {@link #reset() reset} can be called
 * to reset the digest. Once all the data to be updated has been
 * updated, the <code>digest</code> method should 
 * be called to complete the hash computation.
 *
 * <p>The <code>digest</code> method can be called once for a given number 
 * of updates. After <code>digest</code> has been called, 
 * the <code>MessageDigest</code>
 * object is reset to its initialized state.
 */

public abstract class MessageDigest  {

    /**
     * Message digest implementation.
     */
    com.sun.midp.crypto.MessageDigest messageDigest;

    /**
     * Creates a message digest with the specified algorithm name.
     * 
     * @param algorithm the standard name of the digest algorithm. 
     * See Appendix A in the
     * Java Cryptography Architecture API Specification &amp; Reference 
     * for information about standard algorithm names.
     */
    MessageDigest(String algorithm) {
    }

    /**
     * Generates a <code>MessageDigest</code> object that implements
     * the specified digest
     * algorithm. 
     *
     * @param algorithm the name of the algorithm requested. 
     * See Appendix A in the 
     * Java Cryptography Architecture API Specification &amp; Reference
     * for information about standard algorithm names.
     *
     * @return a MessageDigest object implementing the specified
     * algorithm.
     *
     * @exception NoSuchAlgorithmException if the algorithm is
     * not available in the caller's environment.  
     */
    public static MessageDigest getInstance(String algorithm) 
        throws NoSuchAlgorithmException {

        try {
            return new MessageDigestImpl(algorithm,
                com.sun.midp.crypto.MessageDigest.getInstance(algorithm));
        } catch (com.sun.midp.crypto.NoSuchAlgorithmException e) {
            throw new NoSuchAlgorithmException(e.getMessage());
        }
    }


    /**
     * Updates the digest using the specified array of bytes, starting
     * at the specified offset.
     * 
     * @param input the array of bytes.
     *
     * @param offset the offset to start from in the array of bytes.
     *
     * @param len the number of bytes to use, starting at 
     * <code>offset</code>.  
     */
    public void update(byte[] input, int offset, int len) {
        messageDigest.update(input, offset, len);
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
        try {
            return messageDigest.digest(buf, offset, len);
        } catch (com.sun.midp.crypto.DigestException e) {
            throw new DigestException(e.getMessage());
        }
    }

    /**
     * Resets the digest for further use.
     */
    public void reset() {
        messageDigest.reset();
    }
}

/**
 * The non-abstract MessageDigest.
 */
class MessageDigestImpl extends MessageDigest {

    /**
     * Creates a message digest with the specified algorithm name.
     *
     * @param algorithm the standard name of the digest algorithm.
     * See Appendix A in the
     * Java Cryptography Architecture API Specification &amp; Reference
     * for information about standard algorithm names.
     * @param messageDigest MessageDigest implementation
     */
    MessageDigestImpl(String algorithm,
                      com.sun.midp.crypto.MessageDigest messageDigest) {
        super(algorithm);
        this.messageDigest = messageDigest;
    }
}
