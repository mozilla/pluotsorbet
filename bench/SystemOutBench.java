package benchmark;

import com.sun.cldchi.jvm.JVM;

public class SystemOutBench {
  void runBenchmark() {
    long start, time;

    start = JVM.monotonicTimeMillis();
    for (int i = 0; i < 1000; i++) {
      System.out.println("I am the very model of a modern major general.");
    }
    time = JVM.monotonicTimeMillis() - start;
    System.out.println("System.out.println time: " + time);

    start = JVM.monotonicTimeMillis();
    for (int i = 0; i < 100; i++) {
      for (int j = 0; j < 100; j++) {
        System.out.print(j % 10);
      }
    }
    time = JVM.monotonicTimeMillis() - start;
    System.out.println();
    System.out.println("System.out.print time: " + time);
  }

  public static void main(String args[]) {
    SystemOutBench bench = new SystemOutBench();
    bench.runBenchmark();
  }
}
