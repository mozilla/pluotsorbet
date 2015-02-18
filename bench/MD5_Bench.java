package com.sun.midp.crypto;

import com.sun.cldchi.jvm.JVM;

public class MD5_Bench {
    void runBenchmark() {
        try {
            byte[] array = new byte[4096];
            for (int n = 0; n < 16; n++) {
                for (int i = 0; i < 256; i++) {
                    array[i * n] = (byte)i;
                }
            }

            long start = JVM.monotonicTimeMillis();

            MD5 md5 = new MD5();
            for (int i = 0; i < 1000; i++) {
                md5.update(array, 0, array.length);
            }
            byte[] result = new byte[16];
            md5.digest(result, 0, 16);

            System.out.println("MD5: " + (JVM.monotonicTimeMillis() - start));
        } catch (Exception e) {
            System.out.println("Unexpected exception: " + e);
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
      MD5_Bench bench = new MD5_Bench();
      bench.runBenchmark();
    }
}
