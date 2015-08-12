package benchmark;
import com.sun.cldchi.jvm.JVM;

class Stress {

    public static void main(String[] args) {

        long start = JVM.monotonicTimeMillis();
        double s = 0;

        for (int k = 0; k < 100000; k++) {
            s += Double.parseDouble(Double.toString(k));
        }
        System.out.println("Result: " + s);
        System.out.println("Double.toString: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();
    }
}
