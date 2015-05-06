package benchmark;

import com.nokia.mid.ui.gestures.GestureInteractiveZone;
import com.sun.cldchi.jvm.JVM;

public class GestureInteractiveZoneBench {
    void runBenchmark() {
        try {
            GestureInteractiveZone giz = new GestureInteractiveZone(GestureInteractiveZone.GESTURE_TAP);

            long start = JVM.monotonicTimeMillis();

            for (int i = 0; i < 500000; i++) {
                giz = new GestureInteractiveZone(GestureInteractiveZone.GESTURE_TAP);
            }

            long time = JVM.monotonicTimeMillis() - start;
            System.out.println("GestureInteractiveZone constructor: " + time);

            start = JVM.monotonicTimeMillis();

            for (int i = 0; i < 500000; i++) {
                giz.setRectangle(5, 5, 50, 50);
            }

            time = JVM.monotonicTimeMillis() - start;
            System.out.println("GestureInteractiveZone setRectangle: " + time);

            start = JVM.monotonicTimeMillis();

            for (int i = 0; i < 500000; i++) {
                giz.contains(10, 10);
            }

            time = JVM.monotonicTimeMillis() - start;
            System.out.println("GestureInteractiveZone contains: " + time);

            start = JVM.monotonicTimeMillis();

            for (int i = 0; i < 500000; i++) {
                giz.supports(GestureInteractiveZone.GESTURE_TAP);
            }

            time = JVM.monotonicTimeMillis() - start;
            System.out.println("GestureInteractiveZone supports: " + time);
        } catch (Exception e) {
            System.out.println("Unexpected exception: " + e);
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
      GestureInteractiveZoneBench bench = new GestureInteractiveZoneBench();
      bench.runBenchmark();
    }
}
