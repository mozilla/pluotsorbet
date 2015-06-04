package benchmark;

import com.sun.cldchi.jvm.JVM;
import org.bouncycastle.crypto.digests.SHA256Digest;

public class BouncyCastleSHA256 {

    private static final int UPDATES = 170;

    public static void main(String args[]) {
        // Try to mimic a popular midlet that commonly does around
        // 170 update calls on 4096 bytes at a time.
        int updates = 170;
        byte[] digest = new byte[4096];
        for (int i = 0; i < digest.length; i++) {
            digest[i] = (byte)i;
        }

        long start = JVM.monotonicTimeMillis();
        for (int i = 0; i < 20; i++) {
            SHA256Digest digester = new SHA256Digest();
            byte[] retValue = new byte[digester.getDigestSize()];
            for (int j = 0; j < UPDATES; j++) {
                digester.update(digest, 0, digest.length);
            }
            digester.doFinal(retValue, 0);
        }
        long time = JVM.monotonicTimeMillis() - start;
        System.out.println("SHA256: " + time);
    }
}
