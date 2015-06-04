package com.sun.midp.crypto;

import org.bouncycastle.crypto.digests.SHA256Digest;
import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

public class TestBouncyCastleSHA256 implements Testlet {
    public int getExpectedPass() { return 1; }
    public int getExpectedFail() { return 0; }
    public int getExpectedKnownFail() { return 0; }

    private static final String DIGEST0 = "BA7816BF8F01CFEA414140DE5DAE2223B00361A396177A9CB410FF61F20015AD";

    public void test(TestHarness th) {
        SHA256Digest md = new SHA256Digest();
        byte[] retValue = new byte[md.getDigestSize()];
        md.update((byte)0x61);
        md.update((byte)0x62);
        md.update((byte)0x63);
        md.doFinal(retValue, 0);
        th.check(Util.hexEncode(retValue).toUpperCase(), DIGEST0);
    }
}
