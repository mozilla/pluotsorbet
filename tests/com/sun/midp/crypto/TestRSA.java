package com.sun.midp.crypto;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

public class TestRSA implements Testlet {
    public static byte[] hexToBytes(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                  + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public void testRSA(TestHarness th, byte[] n, byte[] e, byte[] d, byte[] message) throws NoSuchAlgorithmException,
                                                                                             InvalidKeyException,
                                                                                             ShortBufferException,
                                                                                             NoSuchPaddingException,
                                                                                             IllegalBlockSizeException,
                                                                                             BadPaddingException {
        RSAPublicKey pubKey = new RSAPublicKey(n, e);
        RSAPrivateKey privKey = new RSAPrivateKey(n, d);

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
    }

    public void test(TestHarness th) {
        String message = "Marco";

        // Generate the keys using:
        // openssl genrsa -out private_key.pem 128
        // openssl rsa -text -in private_key.pem

        try {
          testRSA(th,
                  hexToBytes("752246f60de21d71d98f2aa996add76f"),
                  hexToBytes("010001"),
                  hexToBytes("15fb5fcbcf612edb2c73f528be8c6ad1"),
                  message.getBytes());
        } catch (Exception e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
        }

        try {
          testRSA(th,
                  hexToBytes("7877819a103272312a16c9dac9168a1a106410e061e0c8935a96300a00036cc4694e8bda5924bb0cce70e191e28b5fd4ae37483025e850a0599c70a079515287fb0360c65293144a5e8f952607c8721eda13fb61ec43567c81f9cf27af789bb8e39e0936370f72f4746b8c5eb76fe3dfe083a813d49b2aea7b1f11d57dd8be0f25c1e1115582baec3930807a85c7f809e4b74d2760777b20c895f57c42225710dd0021ac1b735057e4f4da6894d4cf69fd1c6a3ebb69612900b61eca673405e3c8fc88ceac8e2f7e98b93ac4d74646b89491ac2ad28497d6878dedd8e6c979edc96878699c03e411841ffc4fd56a98b804ce2f43a224186d4a695b2856198d43"),
                  hexToBytes("010001"),
                  hexToBytes("2c442db9810e286bec7d673b1ffd4a4dfd8be7afac07bf6c76330dfffeb543788101bdde44377d0c5da7991bd45546a78e44fe1fc32e34c2576b66ef70e4f320c89a1b2b85184ab27140ed9b85eca012ba2bf189b019a6642616b78a6bae33faa29965e52822632974c638546daf8ddaac5374a5a4d047dad5d73ae46a5404b2670f6740ed51eb77d0059d364af99561d8628bc189a43aa78cabaec3b1287ee75df035685cdcdb44377d17941e5592e02894ed95be2b4b04fa06235afb02f6713d7205e3afda3cbf496b4a1c5daffc323f8f36b685afe2d48402dead25bc69d0bbd35bd05456ebfabf34c04f64f8fe2ae029b5c5b9a0b5d0e78d64ffabbd1751"),
                  message.getBytes());
        } catch (Exception e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
        }

        try {
            String longMessage = "MykMyk";
            testRSA(th,
                    hexToBytes("752246f60de21d71d98f2aa996add76f"),
                    hexToBytes("010001"),
                    hexToBytes("15fb5fcbcf612edb2c73f528be8c6ad1"),
                    longMessage.getBytes());
            th.fail("Exception expected");
        } catch (IllegalArgumentException e) {
            th.check(true, "Exception expected");
        } catch (Exception e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
        }
    }
}
