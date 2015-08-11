package benchmark;
import com.sun.cldchi.jvm.JVM;

class Arithmetic {
    public static void f(int a) {}
    public static void f(int a, int b) {}
    public static void f(int a, int b, int c) {}

    public static void f(long a) {}
    public static void f(long a, long b) {}
    public static void f(long a, long b, long c) {}

    public static void f(float a) {}
    public static void f(float a, float b) {}
    public static void f(float a, float b, float c) {}

    public static void f(double a) {}
    public static void f(double a, double b) {}
    public static void f(double a, double b, double c) {}

    public static void main(String[] args) {
        int i = 0;
        long l = 0;
        float f = 123;
        double d = 0;

        long start = JVM.monotonicTimeMillis();

        for (int k = 0; k < 1000000; k++) {
            i +=  1;
            i >>= 2;
            i <<= 3;
            i &=  4;
            i |=  5;
        }

        System.out.println("int: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        for (int k = 0; k < 1000000; k++) {
            l +=  1;
            l >>= 2;
            l <<= 3;
            l &=  4;
            l |=  5;
        }

        System.out.println("long: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        for (int k = 0; k < 1000000; k++) {
            f += 1;
            f -= 1;
            f *= 1;
            f /= 1;
            f %= 1;
            f = -f;
            f = f < f ? f : f;
        }

        System.out.println("float: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        for (int k = 0; k < 1000000; k++) {
            d += 1;
            d -= 1;
            d *= 1;
            d /= 1;
            d %= 1;
            d = -d;
            d = d < d ? d : d;
        }

        System.out.println("double: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();
    }
}
