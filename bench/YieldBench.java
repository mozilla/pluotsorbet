package benchmark;

import com.sun.cldchi.jvm.JVM;
import org.mozilla.internal.Sys;

public class YieldBench {
    void runBenchmark() {
        long start = JVM.monotonicTimeMillis();

        for (int i = 0; i < 1000000; i++) {
            Thread.yield();
        }

        System.out.println("Unwinds: " + Sys.getUnwindCount());
        System.out.println(JVM.monotonicTimeMillis() - start);
    }

    public static void main(String args[]) {
      YieldBench bench = new YieldBench();
      bench.runBenchmark();
    }
}
