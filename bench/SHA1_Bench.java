package com.sun.midp.crypto;

import com.sun.cldchi.jvm.JVM;

public class SHA1_Bench {
    void runBenchmark() {
        try {
            byte[] array = new byte[4096];
            for (int n = 0; n < 16; n++) {
                for (int i = 0; i < 256; i++) {
                    array[i * n] = (byte)i;
                }
            }

            long start = JVM.monotonicTimeMillis();

            SHA sha = new SHA();
            for (int i = 0; i < 1000; i++) {
                sha.update(array, 0, array.length);
            }
            byte[] result = new byte[20];
            sha.digest(result, 0, 20);

            System.out.println("SHA-1: " + (JVM.monotonicTimeMillis() - start));
        } catch (Exception e) {
            System.out.println("Unexpected exception: " + e);
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
      SHA1_Bench bench = new SHA1_Bench();
      bench.runBenchmark();
    }
}
