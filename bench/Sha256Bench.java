package benchmark;

import gnu.java.security.hash.Sha256;
import com.sun.cldchi.jvm.JVM;

public class Sha256Bench {
    public static void main(String args[]) {
        byte[] array = new byte[4096];
        for (int n = 0; n < 16; n++) {
            for (int i = 0; i < 256; i++) {
                array[i * n] = (byte)i;
            }
        }

        long start = JVM.monotonicTimeMillis();
        Sha256 md = new Sha256();
        for (int i = 0; i < 100; i++) {
            md.update(array);
        }
        byte[] result = md.digest();
        System.out.println("Time: " + (JVM.monotonicTimeMillis() - start));
    }
}
