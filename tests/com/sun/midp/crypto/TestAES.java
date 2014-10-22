package com.sun.midp.crypto;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

public class TestAES implements Testlet {
    public static byte[] hexToBytes(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                  + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public void testAESECB(TestHarness th, String keyStr, String messageStr) throws NoSuchAlgorithmException,
                                                                                  InvalidKeyException,
                                                                                  ShortBufferException,
                                                                                  NoSuchPaddingException,
                                                                                  IllegalBlockSizeException,
                                                                                  BadPaddingException {
        byte[] message = messageStr.getBytes();

        byte[] key = keyStr.getBytes();
        SecretKey cipherKey = new SecretKey(key, 0, key.length, "AES");

        Cipher encodeCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        encodeCipher.init(Cipher.ENCRYPT_MODE, cipherKey);
        byte[] encMessage = new byte[1024];
        int encMessageLen = encodeCipher.doFinal(message, 0, message.length, encMessage, 0);

        Cipher decodeCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        decodeCipher.init(Cipher.DECRYPT_MODE, cipherKey);
        byte[] decMessage = new byte[1024];
        int decMessageLen = decodeCipher.doFinal(encMessage, 0, encMessageLen, decMessage, 0);

        th.check(message.length, decMessageLen);
        for (int i = 0; i < decMessageLen; i++) {
            th.check(decMessage[i], message[i]);
        }
    }

    public void testAESCBC(TestHarness th, String keyStr, String messageStr) throws NoSuchAlgorithmException,
                                                                                  InvalidKeyException,
                                                                                  ShortBufferException,
                                                                                  NoSuchPaddingException,
                                                                                  IllegalBlockSizeException,
                                                                                  BadPaddingException,
                                                                                  InvalidAlgorithmParameterException {
        byte[] message = messageStr.getBytes();

        byte[] key = keyStr.getBytes();
        SecretKey cipherKey = new SecretKey(key, 0, key.length, "AES");

        IvParameter ivparam = new IvParameter(key, 0, key.length);

        Cipher encodeCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        encodeCipher.init(Cipher.ENCRYPT_MODE, cipherKey, ivparam);
        byte[] encMessage = new byte[1024];
        int encMessageLen = encodeCipher.doFinal(message, 0, message.length, encMessage, 0);

        Cipher decodeCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        decodeCipher.init(Cipher.DECRYPT_MODE, cipherKey, ivparam);
        byte[] decMessage = new byte[1024];
        int decMessageLen = decodeCipher.doFinal(encMessage, 0, encMessageLen, decMessage, 0);

        th.check(message.length, decMessageLen);
        for (int i = 0; i < decMessageLen; i++) {
            th.check(decMessage[i], message[i]);
        }
    }

    public void test(TestHarness th) {
        try {
            testAESECB(th, "aj2me.jspassword", "Plaintext");
        } catch (Exception e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
        }

        try {
            testAESCBC(th, "aj2me.jspassword", "Plaintext");
        } catch (Exception e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
        }
    }
}
