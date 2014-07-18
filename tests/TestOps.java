public class TestOps extends Test {
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

    public void main() {
        compare(iadd(1, 2), 3);
        compare(idiv(8, 2), 4);
        compare(irem(10, 3), 1);
        compare(imul(20, 3), 60);
        compare(imul(0x12345678, 0x12345678), 502585408);
        compare(iadd(0x7fffffff, 1), -2147483648);
        compare(iadd(-2147483648, -1), 0x7fffffff);
        compare(fadd(1.0f, 2.0f), 3.0f);
        compare(fadd(Float.MAX_VALUE, 1), Float.MAX_VALUE);
        compare(fneg(1.0f), -1.0f);
        compare(fmul(2.0f, 3.0f), 6.0f);
        compare(fdiv(6.0f, 2.0f), 3.0f);
        compare(ladd(1L, 2L), 3L);
        compare(ldiv(12L, 4L), 3L);
        compare(dadd(3.0d, 4.0d), 7.0d);
    }

    public static void main(String[] args) {
	(new TestOps()).main();
    }
}
