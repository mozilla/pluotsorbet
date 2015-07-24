package com.sun.midp.crypto;

import org.bouncycastle.crypto.digests.SHA256Digest;
import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

public class TestBouncyCastleSHA256 implements Testlet {
    public int getExpectedPass() { return 6; }
    public int getExpectedFail() { return 0; }
    public int getExpectedKnownFail() { return 0; }

    private static final String[] messages = {
        "",
        "a",
        "abc",
        "abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq",
        "message digest",
        "secure hash algorithm"
    };

    private static final String[] digests = {
        "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
        "ca978112ca1bbdcafac231b39a23dc4da786eff8147c4e72b9807785afee48bb",
        "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
        "248d6a61d20638b8e5c026930c3e6039a33ce45964ff2167f6ecedd419db06c1",
        "f7846f55cf23e14eebeab5b4e1550cad5b509e3348fbc4efa3a1413d393cb650",
        "f30ceb2bb2829e79e4ca9753d35a8ecc00262d164cc077080295381cbd643f0d"
    };

    private static final String MILLION_A_DIGEST = "cdc76e5c9914fb9281a1c7e284d73e67f1809a48a497200e046d39ccc7112cd0";

    public void test(TestHarness th) {
        SHA256Digest md = new SHA256Digest();
        byte[] retValue = new byte[md.getDigestSize()];

        for (int i = 0; i < messages.length; i++) {
            byte[] bytes = messages[i].getBytes();
            md.update(bytes, 0, bytes.length);
            md.doFinal(retValue, 0);
            th.check(Util.hexEncode(retValue).toLowerCase(), digests[i]);
        }

        /* TODO: Re-enable once we're fast enough
        for (int i = 0; i < 1000000; i++) {
            md.update((byte)'a');
        }
        md.doFinal(retValue, 0);
        th.check(Util.hexEncode(retValue).toLowerCase(), MILLION_A_DIGEST);*/
    }
}
