package com.sun.midp.crypto;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

public class TestRSA implements Testlet {
    public void test(TestHarness th) {
        String messageStr = "Marco";
        byte[] message = messageStr.getBytes();

        // Generate these values using:
        // openssl genrsa -out private_key.pem 128
        // openssl rsa -text -in private_key.pem
        byte[] n = new byte[] { (byte)0x75, (byte)0x22, (byte)0x46, (byte)0xf6, (byte)0x0d, (byte)0xe2, (byte)0x1d, (byte)0x71, (byte)0xd9, (byte)0x8f, (byte)0x2a, (byte)0xa9, (byte)0x96, (byte)0xad, (byte)0xd7, (byte)0x6f };
        byte[] e = new byte[] { (byte)0x01, (byte)0x00, (byte)0x01 };
        byte[] d = new byte[] { (byte)0x15, (byte)0xfb, (byte)0x5f, (byte)0xcb, (byte)0xcf, (byte)0x61, (byte)0x2e, (byte)0xdb, (byte)0x2c, (byte)0x73, (byte)0xf5, (byte)0x28, (byte)0xbe, (byte)0x8c, (byte)0x6a, (byte)0xd1 };
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

          boolean ok = true;
          for (int i = 0; i < message.length; i++) {
              if (message[i] != decMessage[i]) {
                  ok = false;
              }
          }

          th.todo(ok);
        } catch (Exception ex) {
            th.fail("Unexpected exception: " + ex);
            ex.printStackTrace();
        }
    }
}
