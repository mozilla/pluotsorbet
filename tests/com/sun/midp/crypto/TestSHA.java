package com.sun.midp.crypto;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

public class TestSHA implements Testlet {
    protected static final char[] hexArray = "0123456789abcdef".toCharArray();

    public String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public void test(TestHarness th) {
        SHA sha = new SHA();
        th.check(sha.getAlgorithm(), "SHA-1");
        th.check(sha.getDigestLength(), 20);

        byte[] buf = new byte[20];
        try {
            sha.digest(buf, 0, 20);
        } catch (DigestException e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
        }
        th.check(bytesToHex(buf), "da39a3ee5e6b4b0d3255bfef95601890afd80709");

        sha.reset();

        String part1 = "a";
        sha.update(part1.getBytes(), 0, part1.length());

        try {
            sha.digest(buf, 0, 20);
        } catch (DigestException e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
        }

        th.check(bytesToHex(buf), "86f7e437faa5a7fce15d1ddcb9eaeaea377667b8");

        sha.reset();

        sha.update(part1.getBytes(), 0, part1.length());

        String part2 = "NOb";
        sha.update(part2.getBytes(), 2, part2.length()-2);

        String part3 = "NOcNO";
        sha.update(part3.getBytes(), 2, part3.length()-4);

        try {
            sha.digest(buf, 0, 20);
        } catch (DigestException e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
        }

        th.check(bytesToHex(buf), "a9993e364706816aba3e25717850c26c9cd0d89d");

        // Calculate another hash without calling sha.reset() (sha.digest should reset automatically)

        String longStr = "12345678901234567890123456789012345678901234567890123456789012345678901234567890";
        sha.update(longStr.getBytes(), 0, longStr.length());

        try {
            sha.digest(buf, 0, 20);
        } catch (DigestException e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
        }

        th.check(bytesToHex(buf), "50abf5706a150990a08b2c5ea40fa0e585554732");

        boolean exception = false;
        byte[] shortBuf = new byte[15];
        try {
            sha.digest(buf, 0, 15);
        } catch (DigestException ex) {
            exception = true;
        }
        th.check(exception);

        sha.reset();

        // Ensure we can calculate two digests simultaneously.
        sha.update(part1.getBytes(), 0, part1.length());
        SHA sha2 = new SHA();
        sha2.update(part2.getBytes(), 0, part2.length());
        byte[] buf2 = new byte[20];
        try {
            sha.digest(buf, 0, 20);
            sha2.digest(buf2, 0, 20);
        } catch (DigestException e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
        }
        th.check(bytesToHex(buf), "86f7e437faa5a7fce15d1ddcb9eaeaea377667b8");
        th.check(bytesToHex(buf2), "5bd138dc4bccbc9526f6575c21d5e66450cd257f");

        sha.reset();
        sha2.reset();
    }
}
