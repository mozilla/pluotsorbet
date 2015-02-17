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

    void writeSingle(byte[] array) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = 0; i < 4096; i++) {
            baos.write(array[i]);
        }
        baos.close();
    }

    void readSingle(byte[] array) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(array);
        for (int i = 0; i < 4096; i++) {
            int val = bais.read();
        }
    }

    void writeArray64(byte[] array) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = 0; i < 64; i++) {
            baos.write(array, i << 6, 64);
            baos.close();
        }
    }

    void readArray64(byte[] array) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(array);
        byte[] output = new byte[4096];
        for (int i = 0; i < 64; i++) {
            bais.read(output, i << 6, 64);
        }
    }

    void writeArray128(byte[] array) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = 0; i < 32; i++) {
            baos.write(array, i << 7, 128);
        }
        baos.close();
    }

    void readArray128(byte[] array) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(array);
        byte[] output = new byte[4096];
        for (int i = 0; i < 32; i++) {
            bais.read(output, i << 7, 128);
        }
    }

    void writeArray256(byte[] array) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = 0; i < 16; i++) {
            baos.write(array, i << 8, 256);
        }
        baos.close();
    }

    void readArray256(byte[] array) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(array);
        byte[] output = new byte[4096];
        for (int i = 0; i < 16; i++) {
            bais.read(output, i << 8, 256);
        }
    }

    void writeArray(byte[] array) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(array, 0, 4096);
        baos.close();
    }

    void readArray(byte[] array) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(array);
        byte[] output = new byte[4096];
        bais.read(output, 0, 4096);
    }

    void runBenchmark() {
      try {
          long start, time;

          byte[] array = generateArray();

          start = JVM.monotonicTimeMillis();
          for (int i = 0; i < 250; i++) {
              writeSingle(array);
          }
          time = JVM.monotonicTimeMillis() - start;
          System.out.println("writeSingle: " + time);

          start = JVM.monotonicTimeMillis();
          for (int i = 0; i < 250; i++) {
              readSingle(array);
          }
          time = JVM.monotonicTimeMillis() - start;
          System.out.println("readSingle: " + time);

          start = JVM.monotonicTimeMillis();
          for (int i = 0; i < 5000; i++) {
              writeArray64(array);
          }
          time = JVM.monotonicTimeMillis() - start;
          System.out.println("writeArray64: " + time);

          start = JVM.monotonicTimeMillis();
          for (int i = 0; i < 5000; i++) {
              readArray64(array);
          }
          time = JVM.monotonicTimeMillis() - start;
          System.out.println("readArray64: " + time);

          start = JVM.monotonicTimeMillis();
          for (int i = 0; i < 5000; i++) {
              writeArray128(array);
          }
          time = JVM.monotonicTimeMillis() - start;
          System.out.println("writeArray128: " + time);

          start = JVM.monotonicTimeMillis();
          for (int i = 0; i < 5000; i++) {
              readArray128(array);
          }
          time = JVM.monotonicTimeMillis() - start;
          System.out.println("readArray128: " + time);

          start = JVM.monotonicTimeMillis();
          for (int i = 0; i < 5000; i++) {
              writeArray256(array);
          }
          time = JVM.monotonicTimeMillis() - start;
          System.out.println("writeArray256: " + time);

          start = JVM.monotonicTimeMillis();
          for (int i = 0; i < 5000; i++) {
              readArray256(array);
          }
          time = JVM.monotonicTimeMillis() - start;
          System.out.println("readArray256: " + time);

          start = JVM.monotonicTimeMillis();
          for (int i = 0; i < 25000; i++) {
              writeArray(array);
          }
          time = JVM.monotonicTimeMillis() - start;
          System.out.println("writeArray: " + time);

          start = JVM.monotonicTimeMillis();
          for (int i = 0; i < 25000; i++) {
              readArray(array);
          }
          time = JVM.monotonicTimeMillis() - start;
          System.out.println("readArray: " + time);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    public static void main(String args[]) {
      ByteArrayInputOutputStreamBench bench = new ByteArrayInputOutputStreamBench();
      bench.runBenchmark();
    }
}
