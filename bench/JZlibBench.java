package benchmark;

import com.sun.cldchi.jvm.JVM;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import com.jcraft.jzlib.JZlib;
import com.jcraft.jzlib.ZOutputStream;
import com.jcraft.jzlib.ZInputStream;

public class JZlibBench {
    public static void main(String args[]) {
        String str = "";
        String part = "abcdefghilmnopqrstuvzABCDEFGHILMNOPQRSTUVZabcdefghilmnopqrstuvzABCDEFGHILMNOPQRSTUVZ";
        for (int i = 0; i < 1000; i++) {
          str += part;
        }

        byte[] bytes = str.getBytes();
        byte[] compressedBytes;

        long start, time = 0;

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            for (int i = 0; i < 5; i++) {
                ZOutputStream zOut = new ZOutputStream(out, JZlib.Z_BEST_COMPRESSION);
                DataOutputStream dataOut = new DataOutputStream(zOut);

                start = JVM.monotonicTimeMillis();
                dataOut.write(bytes);
                zOut.close();
                time += JVM.monotonicTimeMillis() - start;
            }

            System.out.println("compress: " + time);
            time = 0;

            compressedBytes = out.toByteArray();

            start = JVM.monotonicTimeMillis();
            for (int i = 0; i < 5; i++) {
                ByteArrayInputStream in = new ByteArrayInputStream(compressedBytes);
                ZInputStream zIn = new ZInputStream(in);
                DataInputStream dataIn = new DataInputStream(zIn);

                start = JVM.monotonicTimeMillis();
                dataIn.read(bytes);
                time += JVM.monotonicTimeMillis() - start;

                zIn.close();
            }
            System.out.println("uncompress: " + time);
        } catch (IOException e) {
            System.out.println("Unexpected exception: " + e);
        }
    }
}
