package benchmark;

import com.sun.cldc.i18n.uclc.DefaultCaseConverter;
import com.sun.cldchi.jvm.JVM;

public class DefaultCaseConverterBench {
    void runBenchmark() {
        try {
            long start = JVM.monotonicTimeMillis();

            for (int i = 0; i < 500000; i++) {
                DefaultCaseConverter.toLowerCase('M');
            }

            long time = JVM.monotonicTimeMillis() - start;

            System.out.println("toLowerCase: " + time);

            start = JVM.monotonicTimeMillis();

            for (int i = 0; i < 500000; i++) {
                DefaultCaseConverter.toUpperCase('m');
            }

            time = JVM.monotonicTimeMillis() - start;

            System.out.println("toUpperCase: " + time);
        } catch (Exception e) {
            System.out.println("Unexpected exception: " + e);
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
      DefaultCaseConverterBench bench = new DefaultCaseConverterBench();
      bench.runBenchmark();
    }
}
