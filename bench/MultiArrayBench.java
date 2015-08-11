package benchmark;

import com.sun.cldchi.jvm.JVM;

public class MultiArrayBench {
    void runBenchmark() {
        long start, time;

        start = JVM.monotonicTimeMillis();
        for (int i = 0; i < 1000; i++) {
          int[][] array = new int[1024][1024];
        }
        time = JVM.monotonicTimeMillis() - start;
        System.out.println("Allocate multiarray: " + time);
    }

    public static void main(String args[]) {
      MultiArrayBench bench = new MultiArrayBench();
      bench.runBenchmark();
    }
}
