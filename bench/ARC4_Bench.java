package com.sun.midp.crypto;

import com.sun.cldchi.jvm.JVM;

public class ARC4_Bench {
    void runBenchmark() {
        try {
            System.out.println("Start");

            byte[] message = "45a01f645fc35b383552544b9bf5".getBytes();
            byte[] key = "Secret".getBytes();
            SecretKey cipherKey = new SecretKey(key, 0, key.length, "ARC4");

            long start = JVM.monotonicTimeMillis();

            Cipher encodeCipher = Cipher.getInstance("ARC4");
            encodeCipher.init(Cipher.ENCRYPT_MODE, cipherKey);
            byte[] encMessage = new byte[message.length];
            for (int i = 0; i < 50000; i++) {
                encodeCipher.update(message, 0, message.length, encMessage, 0);
            }
            encodeCipher.doFinal(message, 0, message.length, encMessage, 0);

            System.out.println("ARC4: " + (JVM.monotonicTimeMillis() - start));
        } catch (Exception e) {
            System.out.println("Unexpected exception: " + e);
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        ARC4_Bench bench = new ARC4_Bench();
        bench.runBenchmark();
    }
}
