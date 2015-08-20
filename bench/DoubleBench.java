package benchmark;

import com.sun.cldchi.jvm.JVM;

public class DoubleBench {
    void runBenchmark() {
      long start, time;

      start = JVM.monotonicTimeMillis();
      for (int k = 0; k < 10000; k++) {
          Double.parseDouble(Double.toString(k));
      }
      time = JVM.monotonicTimeMillis() - start;
      System.out.println("DoubleBench: " + time);
    }

    public static void main(String args[]) {
      DoubleBench bench = new DoubleBench();
      bench.runBenchmark();
    }
}
