package benchmark;

import com.sun.cldchi.jvm.JVM;

public class ArrayBench {
    void runBenchmark() {
        long start, time;
        int len = 1;
        int iterations = 1000;

        System.out.println("Time to allocate int arrays of various lengths " + iterations + " times, by length, in ms:");

        for (int i = 0; i < 20; i++) {

            start = JVM.monotonicTimeMillis();
            for (int j = 0; j < iterations; j++) {
                int[] array = new int[len];
            }
            time = JVM.monotonicTimeMillis() - start;
            System.out.println("ArrayBench-" + len + ": " + time);

            len = len * 2;
        }
    }

    public static void main(String args[]) {
        ArrayBench bench = new ArrayBench();
        bench.runBenchmark();
    }
}
