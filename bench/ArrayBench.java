package benchmark;

import com.sun.cldchi.jvm.JVM;

public class ArrayBench {
    void runConstantIterationBenchmark() {
        long start, time;
        int len = 1;
        int iterations = 1000;

        System.out.println("Time to allocate int arrays of variable lengths a constant " + iterations + " times, by length, in ms:");

        for (int i = 0; i < 20; i++) {

            start = JVM.monotonicTimeMillis();
            for (int j = 0; j < iterations; j++) {
                int[] array = new int[len];
            }
            time = JVM.monotonicTimeMillis() - start;
            System.out.println("ArrayBench-" + len + ": " + time);

            // There's no Math.pow in J2ME, so we multiply length ourselves.
            len = len * 2;
        }
    }

    void runVariableIterationBenchmark() {
        long start, time;
        int len = 1;
        int iterations = 0;

        System.out.println("Time to allocate int arrays of variable lengths a variable number of times, by length * iterations, in ms:");

        for (int i = 0; i < 20; i++) {
            // There's no Math.pow in J2ME, so we multiply iterations ourselves.
            iterations = 1;
            for (int k = 1; k < (20 - i); k++) {
                iterations *= 2;
            }

            start = JVM.monotonicTimeMillis();
            for (int j = 0; j < iterations; j++) {
                int[] array = new int[len];
            }
            time = JVM.monotonicTimeMillis() - start;
            System.out.println("ArrayBench-" + len + "*" + iterations + ": " + time);

            len = len * 2;
        }
    }

    public static void main(String args[]) {
        ArrayBench bench = new ArrayBench();
        bench.runConstantIterationBenchmark();
        bench.runVariableIterationBenchmark();
    }
}
