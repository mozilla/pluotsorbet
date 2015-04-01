package benchmark;

import org.mozilla.Test;
import com.sun.cldchi.jvm.JVM;

public class CallNativeBench {
  void runBenchmark() {
    long start;

    start = JVM.monotonicTimeMillis();
    for (int i = 0; i < 1000; i++) {
      Test.callSyncNative();
    }
    System.out.println("Test.callSyncNative time: " + (JVM.monotonicTimeMillis() - start));

    start = JVM.monotonicTimeMillis();
    for (int i = 0; i < 1000; i++) {
      Test.callAsyncNative();
    }
    System.out.println("Test.callAsyncNative time: " + (JVM.monotonicTimeMillis() - start));
  }

  public static void main(String args[]) {
    CallNativeBench bench = new CallNativeBench();
    bench.runBenchmark();
  }
}
