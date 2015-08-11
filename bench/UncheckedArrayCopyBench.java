package benchmark;

import com.sun.cldchi.jvm.JVM;

public class UncheckedArrayCopyBench {
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

      for (int i = 0; i < sizes.length; i++) {
          int size = sizes[i];

          char[] srcArray = new char[size];
          char[] dstArray = new char[size];
          start = JVM.monotonicTimeMillis();
          for (int k = 0; k < 20000; k++) {
            JVM.unchecked_char_arraycopy(srcArray, 0, dstArray, 0, size);
          }
          time = JVM.monotonicTimeMillis() - start;
          System.out.println("UncheckedArrayCopyBench-char-" + size + ": " + time);
      }
    }

    public static void main(String args[]) {
      UncheckedArrayCopyBench bench = new UncheckedArrayCopyBench();
      bench.runBenchmark();
    }
}
