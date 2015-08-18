package benchmark;

import com.sun.cldchi.jvm.JVM;

public class ArrayCopyBench {
    int sizes[] = {
      2,
      4,
      8,
      16,
      32,
      64,
      128,
      256,
      512,
      1024,
      2048,
      4096
    };

    void runBenchmark() {
      long start, time;

      // Primitive array
      for (int i = 0; i < sizes.length; i++) {
          int size = sizes[i];

          char[] srcArray = new char[size];
          char[] dstArray = new char[size];
          start = JVM.monotonicTimeMillis();
          for (int k = 0; k < 20000; k++) {
            System.arraycopy(srcArray, 0, dstArray, 0, size);
          }
          time = JVM.monotonicTimeMillis() - start;
          System.out.println("ArrayCopyBench-char-" + size + ": " + time);
      }

      // Object array
      for (int i = 0; i < sizes.length; i++) {
          int size = sizes[i];

          Object[] srcArray = new Object[size];
          Object[] dstArray = new Object[size];
          start = JVM.monotonicTimeMillis();
          for (int k = 0; k < 20000; k++) {
            System.arraycopy(srcArray, 0, dstArray, 0, size);
          }
          time = JVM.monotonicTimeMillis() - start;
          System.out.println("ArrayCopyBench-Object-" + size + ": " + time);
      }

      // Object array with casting
      for (int i = 0; i < sizes.length; i++) {
          int size = sizes[i];

          Object[] srcArray = new Object[size];
          String[] dstArray = new String[size];
          start = JVM.monotonicTimeMillis();
          for (int k = 0; k < 20000; k++) {
            System.arraycopy(srcArray, 0, dstArray, 0, size);
          }
          time = JVM.monotonicTimeMillis() - start;
          System.out.println("ArrayCopyBench-Object_with_casting-" + size + ": " + time);
      }
    }

    public static void main(String args[]) {
      ArrayCopyBench bench = new ArrayCopyBench();
      bench.runBenchmark();
    }
}
