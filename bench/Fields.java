package benchmark;
import com.sun.cldchi.jvm.JVM;

class Fields {
    public static int i;
    public static long l;
    public static float f;
    public static double d;

    public int I;
    public long L;
    public float F;
    public double D;

    public static void main(String[] args) {

        long start = JVM.monotonicTimeMillis();

        // Static Fields

        for (int k = 0; k < 1000000; k++) {
            i = i; i = i; i = i; i = i; i = i;
        }
        System.out.println("static int: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        for (int k = 0; k < 1000000; k++) {
            l = l; l = l; l = l; l = l; l = l;
        }
        System.out.println("static long: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        for (int k = 0; k < 1000000; k++) {
            f = f; f = f; f = f; f = f; f = f;
        }
        System.out.println("static float: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        for (int k = 0; k < 1000000; k++) {
            d = d; d = d; d = d; d = d; d = d;
        }
        System.out.println("static double: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        // Instance Fields

        Fields x = new Fields();

        for (int k = 0; k < 1000000; k++) {
            x.I = x.I; x.I = x.I; x.I = x.I; x.I = x.I; x.I = x.I;
        }
        System.out.println("int: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        for (int k = 0; k < 1000000; k++) {
            x.L = x.L; x.L = x.L; x.L = x.L; x.L = x.L; x.L = x.L;
        }
        System.out.println("long: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        for (int k = 0; k < 1000000; k++) {
            x.F = x.F; x.F = x.F; x.F = x.F; x.F = x.F; x.F = x.F;
        }
        System.out.println("float: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        for (int k = 0; k < 1000000; k++) {
            x.D = x.D; x.D = x.D; x.D = x.D; x.D = x.D; x.D = x.D;
        }
        System.out.println("double: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();
    }
}
