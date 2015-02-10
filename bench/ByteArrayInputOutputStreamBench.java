package benchmark;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import com.sun.cldchi.jvm.JVM;

public class ByteArrayInputOutputStreamBench {
    byte[] generateArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int n = 0; n < 16; n++) {
            for (int i = 0; i < 256; i++) {
                baos.write(i);
            }
        }
        baos.close();

        return baos.toByteArray();
    }

    void writeAndReadSingle(byte[] array) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = 0; i < 4096; i++) {
            baos.write(array[i]);
        }
        baos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        for (int i = 0; i < 4096; i++) {
          int val = bais.read();
        }
    }

    void writeAndReadArray(byte[] array) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(array, 0, 4096);
        baos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        byte[] output = new byte[4096];
        bais.read(output, 0, 4096);
    }

    void runBenchmark() {
      try {
          long start, time;

           byte[] array = generateArray();

          start = JVM.monotonicTimeMillis();
          for (int i = 0; i < 250; i++) {
              writeAndReadSingle(array);
          }
          time = JVM.monotonicTimeMillis() - start;
          System.out.println("writeAndReadSingle: " + time);

          start = JVM.monotonicTimeMillis();
          for (int i = 0; i < 20000; i++) {
              writeAndReadArray(array);
          }
          time = JVM.monotonicTimeMillis() - start;
          System.out.println("writeAndReadArray: " + time);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    public static void main(String args[]) {
      ByteArrayInputOutputStreamBench bench = new ByteArrayInputOutputStreamBench();
      bench.runBenchmark();
    }
}
