package benchmark;

import com.sun.cldchi.jvm.JVM;

public class MathBench {
    void runBenchmark() {
        try {
            long start = JVM.monotonicTimeMillis();

            for (int i = 0; i < 500000; i++) {
                Math.min(i, 42);
            }

            long time = JVM.monotonicTimeMillis() - start;

            System.out.println("Math.min: " + time);
        } catch (Exception e) {
            System.out.println("Unexpected exception: " + e);
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
      MathBench bench = new MathBench();
      bench.runBenchmark();
    }
}
