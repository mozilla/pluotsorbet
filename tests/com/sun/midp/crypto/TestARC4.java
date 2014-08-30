package com.sun.midp.crypto;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

public class TestARC4 implements Testlet {
    public static byte[] hexToBytes(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                  + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public void testARC4(TestHarness th, String keyStr, String messageStr, String hexVal) throws NoSuchAlgorithmException,
                                                                                                 InvalidKeyException,
                                                                                                 ShortBufferException,
                                                                                                 NoSuchPaddingException,
                                                                                                 IllegalBlockSizeException,
                                                                                                 BadPaddingException {
        byte[] message = messageStr.getBytes();

        byte[] key = keyStr.getBytes();
        SecretKey cipherKey = new SecretKey(key, 0, key.length, "ARC4");

        Cipher encodeCipher = Cipher.getInstance("ARC4");
        encodeCipher.init(Cipher.ENCRYPT_MODE, cipherKey);
        byte[] encMessage = new byte[message.length];
        encodeCipher.doFinal(message, 0, message.length, encMessage, 0);

        th.check(Util.hexEncode(encMessage), hexVal);

        Cipher decodeCipher = Cipher.getInstance("ARC4");
        decodeCipher.init(Cipher.DECRYPT_MODE, cipherKey);
        byte[] decMessage = new byte[message.length];
        decodeCipher.doFinal(encMessage, 0, encMessage.length, decMessage, 0);

        for (int i = 0; i < message.length; i++) {
            th.check(decMessage[i], message[i]);
        }
    }

    public void test(TestHarness th) {
        try {
            testARC4(th, "Key", "Plaintext", "bbf316e8d940af0ad3");
        } catch (Exception e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
        }
    }
}
