package com.sun.midp.publickeystore;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;
import com.sun.midp.pki.*;
import com.sun.midp.security.*;
import com.sun.midp.main.Configuration;

public class TestWebPublicKeyStore implements Testlet {
    static private class SecurityTrusted implements ImplicitlyTrustedClass {}

    public void test(TestHarness th) {
        SecurityToken internalSecurityToken = SecurityInitializer.requestToken(new SecurityTrusted());
        WebPublicKeyStore.initKeystoreLocation(internalSecurityToken, Configuration.getProperty("com.sun.midp.publickeystore.WebPublicKeyStore"));
        CertStore cs = WebPublicKeyStore.getTrustedKeyStore();
    }
}
