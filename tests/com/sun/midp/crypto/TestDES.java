package com.sun.midp.crypto;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

public class TestDES implements Testlet {
    public static byte[] hexToBytes(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                  + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public void testDESECB(TestHarness th, String keyStr, String messageStr) throws NoSuchAlgorithmException,
                                                                                  InvalidKeyException,
                                                                                  ShortBufferException,
                                                                                  NoSuchPaddingException,
                                                                                  IllegalBlockSizeException,
                                                                                  BadPaddingException {
        byte[] message = messageStr.getBytes();

        byte[] key = keyStr.getBytes();

        Cipher encodeCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        encodeCipher.init(Cipher.ENCRYPT_MODE, new SecretKey(key, 0, key.length, "DES"));
        byte[] encMessage = new byte[1024];
        int encMessageLen = encodeCipher.doFinal(message, 0, message.length, encMessage, 0);

        Cipher decodeCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        decodeCipher.init(Cipher.DECRYPT_MODE, new SecretKey(key, 0, key.length, "DES"));
        byte[] decMessage = new byte[1024];
        int decMessageLen = decodeCipher.doFinal(encMessage, 0, encMessageLen, decMessage, 0);

        th.check(message.length, decMessageLen);
        for (int i = 0; i < decMessageLen; i++) {
            th.check(decMessage[i], message[i]);
        }
    }

    public void testDESCBC(TestHarness th, String keyStr, String messageStr) throws NoSuchAlgorithmException,
                                                                                  InvalidKeyException,
                                                                                  ShortBufferException,
                                                                                  NoSuchPaddingException,
                                                                                  IllegalBlockSizeException,
                                                                                  BadPaddingException,
                                                                                  InvalidAlgorithmParameterException {
        byte[] message = messageStr.getBytes();

        byte[] key = keyStr.getBytes();

        byte[] iv = hexToBytes("8E12399C07726F5A");
        IvParameter ivparam = new IvParameter(iv, 0, iv.length);

        Cipher encodeCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
        encodeCipher.init(Cipher.ENCRYPT_MODE, new SecretKey(key, 0, key.length, "DES"), ivparam);
        byte[] encMessage = new byte[1024];
        int encMessageLen = encodeCipher.doFinal(message, 0, message.length, encMessage, 0);

        Cipher decodeCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
        decodeCipher.init(Cipher.DECRYPT_MODE, new SecretKey(key, 0, key.length, "DES"), ivparam);
        byte[] decMessage = new byte[1024];
        int decMessageLen = decodeCipher.doFinal(encMessage, 0, encMessageLen, decMessage, 0);

        th.check(message.length, decMessageLen);
        for (int i = 0; i < decMessageLen; i++) {
            th.check(decMessage[i], message[i]);
        }
    }

    public void test(TestHarness th) {
        try {
            testDESECB(th, "eightkey", "Plaintext");
        } catch (Exception e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
        }

        try {
            testDESCBC(th, "eightkey", "Plaintext");
        } catch (Exception e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
        }
    }
}
