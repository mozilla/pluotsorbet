package com.sun.midp.crypto;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

public class TestRSA implements Testlet {
    public void test(TestHarness th) {
        String messageStr = "Marco";
        byte[] message = messageStr.getBytes();

        // Generate these values using:
        // openssl genrsa -out private_key.pem 64
        // openssl rsa -text -in private_key.pem
        byte[] n = new byte[] { (byte)0x00, (byte)0xb5, (byte)0xef, (byte)0xbf, (byte)0x6b, (byte)0xc3, (byte)0xcc, (byte)0x78, (byte)0x5f, (byte)0x59, (byte)0xa1, (byte)0xef, (byte)0x81, (byte)0x70, (byte)0x38, (byte)0xe7, (byte)0x25 };
        byte[] e = new byte[] { (byte)0x01, (byte)0x00, (byte)0x01 };
        byte[] d = new byte[] { (byte)0x00, (byte)0x8c, (byte)0x65, (byte)0xdc, (byte)0xe4, (byte)0x3e, (byte)0x8e, (byte)0xb5, (byte)0x7c, (byte)0x21, (byte)0xc7, (byte)0x99, (byte)0x21, (byte)0xbd, (byte)0x30, (byte)0x6d, (byte)0x41 };
        RSAPublicKey pubKey = new RSAPublicKey(n, e);
        RSAPrivateKey privKey = new RSAPrivateKey(n, d);

        try {
            Cipher rsa = Cipher.getInstance("RSA");
            rsa.init(Cipher.ENCRYPT_MODE, privKey);
            byte[] encMessage = new byte[privKey.getModulusLen()];
            rsa.doFinal(message, 0, message.length, encMessage, 0);

            Cipher rsa2 = Cipher.getInstance("RSA");
            rsa2.init(Cipher.DECRYPT_MODE, pubKey);
            byte[] decMessage = new byte[pubKey.getModulusLen()];
            rsa2.doFinal(encMessage, 0, encMessage.length, decMessage, 0);

            for (int i = 0; i < message.length; i++) {
                th.check(decMessage[i], message[i]);
            }
        } catch (Exception ex) {
            th.fail("Unexpected exception: " + ex);
            ex.printStackTrace();
        }
    }
}
