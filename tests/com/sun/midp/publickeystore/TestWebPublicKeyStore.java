package com.sun.midp.publickeystore;

import java.util.*;
import javax.microedition.midlet.*;
import javax.microedition.pki.CertificateException;
import com.sun.midp.pki.*;
import com.sun.midp.security.*;
import com.sun.midp.main.Configuration;

public class TestWebPublicKeyStore extends MIDlet {
    public TestWebPublicKeyStore() {
    }

    public void startApp() {
        System.out.println("START");

        WebPublicKeyStore cs = WebPublicKeyStore.getTrustedKeyStore();
        int numberOfKeys = cs.numberOfKeys();
        if (numberOfKeys < 1) {
            System.out.println("FAIL - Number of keys (" + numberOfKeys + ") < 1");
        }

        PublicKeyInfo keyInfo = cs.getKey(0);
        if (keyInfo.getOwner().length() == 0) {
            System.out.println("FAIL - Owner must be a string with length > 0");
        }

        X509Certificate[] certificates = cs.getCertificates(keyInfo.getOwner());
        X509Certificate aCert = certificates[0];
        try {
          aCert.verify(aCert.getPublicKey());
        } catch (CertificateException e) {
            System.out.println("FAIL - Verification should succeed");
            e.printStackTrace();
        }

        System.out.println("DONE");
    }

    public void pauseApp() {
    }
    
    public void destroyApp(boolean unconditional) {
    }
}
