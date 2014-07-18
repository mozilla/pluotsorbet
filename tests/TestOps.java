public class TestOps {
    static void Assert(String test, boolean ok) {
        System.out.println(test + " " + (ok ? "PASS" : "FAIL"));
    }

    static int iadd(int a, int b) {
        return a + b;
    }

    static int idiv(int a, int b) {
        return a / b;
    }

    static int irem(int a, int b) {
        return a % b;
    }

    static int imul(int a, int b) {
        return a * b;
    }

    static float fadd(float a, float b) {
        return a + b;
    }

    static float fneg(float a) {
        return -a;
    }

    static float fmul(float a, float b) {
        return a * b;
    }

    static float fdiv(float a, float b) {
        return a / b;
    }

    static float frem(float a, float b) {
        return a % b;
    }

    static long ladd(long a, long b) {
        return a + b;
    }

    static long ldiv(long a, long b) {
        return a / b;
    }

    static double dadd(double a, double b) {
        return a + b;
    }

    public static void main(String[] args) {
        Assert("Do asserts work", true);
        Assert("add two ints", iadd(1, 2) == 3);
        Assert("int div", idiv(8, 2) == 4);
        Assert("int rem", irem(10, 3) == 1);
        Assert("int mul", imul(20, 3) == 60);
        Assert("int mul overflow", imul(0x12345678, 0x12345678) == 502585408);
        Assert("overflow", iadd(0x7fffffff, 1) == -2147483648);
        Assert("underflow", iadd(-2147483648, -1) == 0x7fffffff);
        Assert("add two floats", fadd(1.0f, 2.0f) == 3.0f);
        Assert("max float + 1", fadd(Float.MAX_VALUE, 1) == Float.MAX_VALUE);
        Assert("float neg", fneg(1.0f) == -1.0f);
        Assert("float mul", fmul(2.0f, 3.0f) == 6.0f);
        Assert("float div", fdiv(6.0f, 2.0f) == 3.0f);
        Assert("long add", ladd(1L, 2L) == 3L);
        Assert("long div", ldiv(12L, 4L) == 3L);
        Assert("double add", dadd(3.0d, 4.0d) == 7.0d);
    }
}
