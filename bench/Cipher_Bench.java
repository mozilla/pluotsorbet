package com.sun.midp.crypto;

import com.sun.cldchi.jvm.JVM;

public class Cipher_Bench {
    void runBenchmark() {
        try {
            System.out.println("Start");

            byte[] message = new byte [512 * 1024]; // "45a01f645fc35b383552544b9bf5".getBytes();
            byte[] key = "ABCDABCD".getBytes();

            long start = JVM.monotonicTimeMillis();

            Cipher encodeCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            encodeCipher.init(Cipher.ENCRYPT_MODE, new SecretKey(key, 0, key.length, "DES"));
            byte[] encMessage = new byte[1024 * 1024];
            int encMessageLen = encodeCipher.doFinal(message, 0, message.length, encMessage, 0);

            System.out.println("encMessageLen: " + encMessageLen);

            Cipher decodeCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            decodeCipher.init(Cipher.DECRYPT_MODE, new SecretKey(key, 0, key.length, "DES"));
            byte[] decMessage = new byte[1024 * 1024];
            int decMessageLen = decodeCipher.doFinal(encMessage, 0, encMessageLen, decMessage, 0);
            System.out.println("decMessageLen: " + decMessageLen);

            System.out.println("DES: " + (JVM.monotonicTimeMillis() - start));

        } catch (Exception e) {
            System.out.println("Unexpected exception: " + e);
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        Cipher_Bench bench = new Cipher_Bench();
        bench.runBenchmark();
    }
}
