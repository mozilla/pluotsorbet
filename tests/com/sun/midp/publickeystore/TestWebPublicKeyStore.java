package com.sun.midp.publickeystore;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;
import com.sun.midp.pki.*;

public class TestWebPublicKeyStore implements Testlet {
    public void test(TestHarness th) {
        CertStore cs = WebPublicKeyStore.getTrustedKeyStore();
    }
}
