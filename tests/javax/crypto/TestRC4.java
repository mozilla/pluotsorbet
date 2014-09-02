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

package javax.crypto;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;

/**
 * This test case tests ARCFOUR (RC4) cipher
 */
public class TestRC4 implements Testlet {
    private static final byte[] SECRET_KEY = {
        0x73, 0x65, 0x63, 0x72, 0x65, 0x74 // "secret"
    };

    private static final byte[] PLAIN_TEXT = {
        0x70, 0x6C, 0x61, 0x69, 0x6E, 0x74, 0x65, 0x78, 0x74 // "plaintext"
    };

    // cipher text corresponded to the plain text "plaintextplaintext"
    private static final byte[] CIPHER_TEXT_PART_1 = {
        (byte) 0x9D, 0x5A, (byte) 0xB3, 0x75, (byte) 0xEC,
        (byte) 0xD0, (byte) 0xB3, (byte) 0xDE, 0x46
    };
    private static final byte[] CIPHER_TEXT_PART_2 = {
        (byte) 0xBB, (byte) 0xD7, (byte) 0xBD, (byte) 0x9A,
        (byte) 0x88, (byte) 0x81, 0x52, (byte) 0xD5, 0x43
    };

    private static final String ALGORITHM = "arcfour";
    private static final String MODE = "/none";
    private static final String PADDING = "/nopadding";

    private byte[] outBuf1 = null;
    private byte[] outBuf2 = null;

    private Cipher cipher = null;
    private SecretKeySpec key = null;

    /**
     * Test all primitive operations.
     */
    private void testOne(TestHarness th) {

        // preparation

        boolean noSuchAlg = false;
        boolean noSuchPad = false;
        boolean invKey = false;
        boolean illState = false;
        boolean shortBuf = false;
        boolean illBlSize = false;
        boolean badPad = false;

        int outLen = 0;
        outBuf1 = new byte[PLAIN_TEXT.length];
        outBuf2 = new byte[PLAIN_TEXT.length];
        th.check(outBuf1 != null);
        th.check(outBuf2 != null);

        cipher = null;
        key = null;

        // create cipher for not supported algorithm/mode
        // -> NoSuchAlgorithmException should be thrown

        try {
            cipher = Cipher.getInstance(null);
        } catch (NoSuchAlgorithmException e) {
            noSuchAlg = true;
        } catch (NoSuchPaddingException e) {
            noSuchPad = true;
        }
        th.check(!noSuchPad);
        th.check(noSuchAlg);
        th.check(cipher == null);
        noSuchAlg = false;

        try {
            cipher = Cipher.getInstance("");
        } catch (NoSuchAlgorithmException e) {
            noSuchAlg = true;
        } catch (NoSuchPaddingException e) {
            noSuchPad = true;
        }
        th.check(!noSuchPad);
        th.check(noSuchAlg);
        th.check(cipher == null);
        noSuchAlg = false;

        try {
            cipher = Cipher.getInstance("///");
        } catch (NoSuchAlgorithmException e) {
            noSuchAlg = true;
        } catch (NoSuchPaddingException e) {
            noSuchPad = true;
        }
        th.check(!noSuchPad);
        th.check(noSuchAlg);
        th.check(cipher == null);
        noSuchAlg = false;

        try {
            cipher = Cipher.getInstance("a/b/c");
        } catch (NoSuchAlgorithmException e) {
            noSuchAlg = true;
        } catch (NoSuchPaddingException e) {
            noSuchPad = true;
        }
        th.check(!noSuchPad);
        th.check(noSuchAlg);
        th.check(cipher == null);
        noSuchAlg = false;

        try {
            cipher = Cipher.getInstance("a/d/f/t/y");
        } catch (NoSuchAlgorithmException e) {
            noSuchAlg = true;
        } catch (NoSuchPaddingException e) {
            noSuchPad = true;
        }
        th.check(!noSuchPad);
        th.check(noSuchAlg);
        th.check(cipher == null);
        noSuchAlg = false;

        try {
            cipher = Cipher.getInstance(ALGORITHM + "/zzz");
        } catch (NoSuchAlgorithmException e) {
            noSuchAlg = true;
        } catch (NoSuchPaddingException e) {
            noSuchPad = true;
        }
        th.check(!noSuchPad);
        th.check(noSuchAlg);
        th.check(cipher == null);
        noSuchAlg = false;

        try {
            cipher = Cipher.getInstance(ALGORITHM + MODE + PADDING + "/xxx");
        } catch (NoSuchAlgorithmException e) {
            noSuchAlg = true;
        } catch (NoSuchPaddingException e) {
            noSuchPad = true;
        }
        th.check(!noSuchPad);
        th.check(noSuchAlg);
        th.check(cipher == null);
        noSuchAlg = false;

        // create cipher with not supported padding
        // -> NoSuchPaddingException should be thrown

        try {
            cipher = Cipher.getInstance(ALGORITHM + MODE + "/yyy");
        } catch (NoSuchAlgorithmException e) {
            noSuchAlg = true;
        } catch (NoSuchPaddingException e) {
            noSuchPad = true;
        }
        th.check(!noSuchAlg);
        th.check(noSuchPad);
        th.check(cipher == null);
        noSuchPad = false;

        // create cipher

        try {
            cipher = Cipher.getInstance(ALGORITHM + MODE + PADDING);
        } catch (NoSuchAlgorithmException e) {
            noSuchAlg = true;
        } catch (NoSuchPaddingException e) {
            noSuchPad = true;
        }
        th.check(!noSuchAlg);
        th.check(!noSuchPad);
        th.check(cipher != null);

        // create key

        key = new SecretKeySpec(SECRET_KEY, 0, SECRET_KEY.length, ALGORITHM);
        th.check(key != null);
        th.check(ALGORITHM.toUpperCase(), key.getAlgorithm().toUpperCase());
        th.check(cmpArr(SECRET_KEY, key.getEncoded()));

        // update() before init() -> IllegalStateException should be thrown

        try {
            outLen = cipher.update(PLAIN_TEXT, 0, PLAIN_TEXT.length,
                                   outBuf1, 0);
        } catch (IllegalStateException e) {
            illState = true;
        } catch (ShortBufferException e) {
            shortBuf = true;
        }
        th.check(!shortBuf);
        th.check(illState);
        illState = false;

        // doFinal() before init() -> IllegalStateException should be thrown

        try {
            outLen = cipher.doFinal(PLAIN_TEXT, 0, PLAIN_TEXT.length,
                                    outBuf2, 0);
        } catch (IllegalStateException e) {
            illState = true;
        } catch (ShortBufferException e) {
            shortBuf = true;
        } catch (IllegalBlockSizeException e) {
            illBlSize = true;
        } catch (BadPaddingException e) {
            badPad = true;
        }
        th.check(!shortBuf);
        th.check(!illBlSize);
        th.check(!badPad);
        th.check(illState);
        illState = false;

        // init cipher for encryption

        try {
            cipher.init(Cipher.ENCRYPT_MODE, key);
        } catch (InvalidKeyException e) {
            invKey = true;
        }
        th.check(!invKey);

        // encrypt using update()

        try {
            outLen = cipher.update(PLAIN_TEXT, 0, PLAIN_TEXT.length,
                                   outBuf1, 0);
        } catch (IllegalStateException e) {
            illState = true;
        } catch (ShortBufferException e) {
            shortBuf = true;
        }
        th.check(!shortBuf);
        th.check(!illState);
        th.check(PLAIN_TEXT.length, outLen);

        // encrypt using doFinal()

        try {
            outLen = cipher.doFinal(PLAIN_TEXT, 0, PLAIN_TEXT.length,
                                    outBuf2, 0);
        } catch (IllegalStateException e) {
            illState = true;
        } catch (ShortBufferException e) {
            shortBuf = true;
        } catch (IllegalBlockSizeException e) {
            illBlSize = true;
        } catch (BadPaddingException e) {
            badPad = true;
        }
        th.check(!shortBuf);
        th.check(!illBlSize);
        th.check(!badPad);
        th.check(!illState);
        th.check(PLAIN_TEXT.length, outLen);

        // check encryption results

        th.check(cmpArr(CIPHER_TEXT_PART_1, outBuf1));
        th.check(cmpArr(CIPHER_TEXT_PART_2, outBuf2));

        // init cipher for decryption

        try {
            cipher.init(Cipher.DECRYPT_MODE, key);
        } catch (InvalidKeyException e) {
            invKey = true;
        }
        th.check(!invKey);

        // decrypt using update()

        try {
            outLen = cipher.update(CIPHER_TEXT_PART_1, 0,
                                   CIPHER_TEXT_PART_1.length, outBuf1, 0);
        } catch (IllegalStateException e) {
            illState = true;
        } catch (ShortBufferException e) {
            shortBuf = true;
        }
        th.check(!shortBuf);
        th.check(!illState);
        th.check(PLAIN_TEXT.length, outLen);

        // decrypt using doFinal()

        try {
            outLen = cipher.doFinal(CIPHER_TEXT_PART_2, 0,
                                    CIPHER_TEXT_PART_2.length, outBuf2, 0);
        } catch (IllegalStateException e) {
            illState = true;
        } catch (ShortBufferException e) {
            shortBuf = true;
        } catch (IllegalBlockSizeException e) {
            illBlSize = true;
        } catch (BadPaddingException e) {
            badPad = true;
        }
        th.check(!shortBuf);
        th.check(!illBlSize);
        th.check(!badPad);
        th.check(!illState);
        th.check(PLAIN_TEXT.length, outLen);

        // check decryption results

        th.check(cmpArr(PLAIN_TEXT, outBuf1));
        th.check(cmpArr(PLAIN_TEXT, outBuf2));

        // incorrect outBuf size -> ShortBufferException should be thrown

        try {
            outLen = cipher.update(PLAIN_TEXT, 0, PLAIN_TEXT.length,
                                   outBuf1, 1);
        } catch (IllegalStateException e) {
            illState = true;
        } catch (ShortBufferException e) {
            shortBuf = true;
        }
        th.check(!illState);
        th.check(shortBuf);
        shortBuf = false;

        try {
            outLen = cipher.doFinal(PLAIN_TEXT, 0, PLAIN_TEXT.length,
                                    outBuf2, 1);
        } catch (IllegalStateException e) {
            illState = true;
        } catch (ShortBufferException e) {
            shortBuf = true;
        } catch (IllegalBlockSizeException e) {
            illBlSize = true;
        } catch (BadPaddingException e) {
            badPad = true;
        }
        th.check(!illState);
        th.check(!illBlSize);
        th.check(!badPad);
        th.check(shortBuf);
        shortBuf = false;
    }

    /**
     * Run tests.
     */
    public void test(TestHarness th) {
        try {
            testOne(th);
        }
        catch (Throwable t) {
            th.fail("" + t);
        }
    }

    /**
     * Compare two byte arrays.
     */
    private boolean cmpArr(byte[] one, byte[] two) {
        if (one.length != two.length) {
            return false;
        }
        for (int i = 0; i < one.length; i++) {
            if (one[i] != two[i]) {
                return false;
            }
        }
        return true;
    }

}
