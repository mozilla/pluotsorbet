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
        WebPublicKeyStore cs = WebPublicKeyStore.getTrustedKeyStore();
        System.out.println(cs.numberOfKeys());
        PublicKeyInfo keyInfo = cs.getKey(0);
        // do some tests on keyInfo
        X509Certificate[] certificates1 = cs.getCertificates(keyInfo.getOwner());
        System.out.println(certificates1.length);
        X509Certificate aCert = certificates1[0];
        try {
          aCert.verify(aCert.getPublicKey());
        } catch (CertificateException e) {
            e.printStackTrace();
        }


        Vector keys = cs.getKeys();
        for (int i = 0; i < keys.size(); i++) {
            PublicKeyInfo key = (PublicKeyInfo)keys.elementAt(i);
            X509Certificate[] certificates = cs.getCertificates(key.getOwner());
            for (int j = 0; j < certificates.length; j++)
                System.out.println(certificates[j].toString());
        }
    }

    public void pauseApp() {
    }
    
    public void destroyApp(boolean unconditional) {
    }
}
