package com.sun.midp.crypto;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

public class TestMD5 implements Testlet {
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
        MD5 md5 = new MD5();
        th.check(md5.getAlgorithm(), "MD5");
        th.check(md5.getDigestLength(), 16);

        byte[] buf = new byte[16];
        try {
            md5.digest(buf, 0, 16);
        } catch (DigestException e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
        }
        th.todo(bytesToHex(buf), "d41d8cd98f00b204e9800998ecf8427e");

        md5.reset();

        String part1 = "a";
        md5.update(part1.getBytes(), 0, part1.length());

        try {
            md5.digest(buf, 0, 16);
        } catch (DigestException e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
        }

        th.todo(bytesToHex(buf), "0cc175b9c0f1b6a831c399e269772661");

        md5.reset();

        md5.update(part1.getBytes(), 0, part1.length());

        String part2 = "NOb";
        md5.update(part2.getBytes(), 2, part2.length()-2);

        String part3 = "NOcNO";
        md5.update(part3.getBytes(), 2, part3.length()-4);

        try {
            md5.digest(buf, 0, 16);
        } catch (DigestException e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
        }

        th.todo(bytesToHex(buf), "900150983cd24fb0d6963f7d28e17f72");

        // Calculate another hash without calling sha.reset() (sha.digest should reset automatically)

        String longStr = "12345678901234567890123456789012345678901234567890123456789012345678901234567890";
        md5.update(longStr.getBytes(), 0, longStr.length());

        try {
            md5.digest(buf, 0, 16);
        } catch (DigestException e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
        }

        th.todo(bytesToHex(buf), "57edf4a22be3c955ac49da2e2107b67a");

        boolean exception = false;
        byte[] shortBuf = new byte[15];
        try {
            md5.digest(buf, 0, 15);
        } catch (DigestException ex) {
            exception = true;
        }
        th.check(exception);
    }
}
