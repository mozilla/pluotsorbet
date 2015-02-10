package gnu.testlet.vm;

import gnu.testlet.*;

public class OpsTest implements Testlet {
     public int getExpectedPass() { return 15; }
     public int getExpectedFail() { return 0; }
     public int getExpectedKnownFail() { return 0; }
     int iadd(int a, int b) {
        return a + b;
    }

     int idiv(int a, int b) {
        return a / b;
    }

     int irem(int a, int b) {
        return a % b;
    }

     int imul(int a, int b) {
        return a * b;
    }

     float fadd(float a, float b) {
        return a + b;
    }

     float fneg(float a) {
        return -a;
    }

     float fmul(float a, float b) {
        return a * b;
    }

     float fdiv(float a, float b) {
        return a / b;
    }

     float frem(float a, float b) {
        return a % b;
    }

     long ladd(long a, long b) {
        return a + b;
    }

     long ldiv(long a, long b) {
        return a / b;
    }

     double dadd(double a, double b) {
        return a + b;
    }

    public void test(TestHarness th) {
        th.check(iadd(1, 2), 3);
        th.check(idiv(8, 2), 4);
        th.check(irem(10, 3), 1);
        th.check(imul(20, 3), 60);
        th.check(imul(0x12345678, 0x12345678), 502585408);
        th.check(iadd(0x7fffffff, 1), -2147483648);
        th.check(iadd(-2147483648, -1), 0x7fffffff);
        th.check(fadd(1.0f, 2.0f), 3.0f);
        th.check(fadd(Float.MAX_VALUE, 1), Float.MAX_VALUE);
        th.check(fneg(1.0f), -1.0f);
        th.check(fmul(2.0f, 3.0f), 6.0f);
        th.check(fdiv(6.0f, 2.0f), 3.0f);
        th.check(ladd(1L, 2L), 3L);
        th.check(ldiv(12L, 4L), 3L);
        th.check(dadd(3.0d, 4.0d), 7.0d);
    }
}
