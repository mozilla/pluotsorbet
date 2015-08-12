package benchmark;
import com.sun.cldchi.jvm.JVM;

class Invoke {
    public static void f(int a) {}
    public static void f(int a, int b) {}
    public static void f(int a, int b, int c) {}
    public static void f(int a, int b, int c, int d) {}
    public static void f(int a, int b, int c, int d, int e) {}
    public static void f(int a, int b, int c, int d, int e, int f) {}
    public static void f(int a, int b, int c, int d, int e, int f, int g) {}
    public static void f(int a, int b, int c, int d, int e, int f, int g, int h) {}

    public static void f(long a) {}
    public static void f(long a, long b) {}
    public static void f(long a, long b, long c) {}
    public static void f(long a, long b, long c, long d) {}
    public static void f(long a, long b, long c, long d, long e) {}
    public static void f(long a, long b, long c, long d, long e, long f) {}
    public static void f(long a, long b, long c, long d, long e, long f, long g) {}
    public static void f(long a, long b, long c, long d, long e, long f, long g, long h) {}

    public static void f(float a) {}
    public static void f(float a, float b) {}
    public static void f(float a, float b, float c) {}
    public static void f(float a, float b, float c, float d) {}
    public static void f(float a, float b, float c, float d, float e) {}
    public static void f(float a, float b, float c, float d, float e, float f) {}
    public static void f(float a, float b, float c, float d, float e, float f, float g) {}
    public static void f(float a, float b, float c, float d, float e, float f, float g, float h) {}

    public static void f(double a) {}
    public static void f(double a, double b) {}
    public static void f(double a, double b, double c) {}
    public static void f(double a, double b, double c, double d) {}
    public static void f(double a, double b, double c, double d, double e) {}
    public static void f(double a, double b, double c, double d, double e, double f) {}
    public static void f(double a, double b, double c, double d, double e, double f, double g) {}
    public static void f(double a, double b, double c, double d, double e, double f, double g, double h) {}


    public static void main(String[] args) {
        int i = 0;
        long l = 0;
        float f = 0;
        double d = 0;

        long start = JVM.monotonicTimeMillis();

        for (int k = 0; k < 1000000; k++) {
            f(i);
            f(i, i);
            f(i, i, i);
            f(i, i, i, i);
            f(i, i, i, i, i);
            f(i, i, i, i, i, i);
            f(i, i, i, i, i, i, i);
            f(i, i, i, i, i, i, i, i);
        }

        System.out.println("int: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        for (int k = 0; k < 1000000; k++) {
            f(l);
            f(l, l);
            f(l, l, l);
            f(l, l, l, l);
            f(l, l, l, l, l);
            f(l, l, l, l, l, l);
            f(l, l, l, l, l, l, l);
            f(l, l, l, l, l, l, l, l);
        }

        System.out.println("long: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        for (int k = 0; k < 1000000; k++) {
            f(f);
            f(f, f);
            f(f, f, f);
            f(f, f, f, f);
            f(f, f, f, f, f);
            f(f, f, f, f, f, f);
            f(f, f, f, f, f, f, f);
            f(f, f, f, f, f, f, f, f);
        }

        System.out.println("float: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();

        for (int k = 0; k < 1000000; k++) {
            f(d);
            f(d, d);
            f(d, d, d);
            f(d, d, d, d);
            f(d, d, d, d, d);
            f(d, d, d, d, d, d);
            f(d, d, d, d, d, d, d);
            f(d, d, d, d, d, d, d, d);
        }

        System.out.println("double: " + (JVM.monotonicTimeMillis() - start)); start = JVM.monotonicTimeMillis();
    }
}
