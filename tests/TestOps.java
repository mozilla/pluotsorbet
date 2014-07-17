package tests;

public class TestOps {
    static void Assert(String test, boolean ok) {
	System.out.println(test + " " + (ok ? "PASS" : "FAIL"));
    }

    static int iadd(int a, int b) {
	return a + b;
    }

    static float fadd(float a, float b) {
	return a + b;
    }

    public static void main(String[] args) {
	Assert("Do asserts work", true);
	Assert("add two ints", iadd(1, 2) == 3);
	Assert("overflow", iadd(0x7fffffff, 1) == -2147483648);
	Assert("underflow", iadd(-2147483648, -1) == 0x7fffffff);
	Assert("add two floats", fadd(1.0f, 2.0f) == 3.0f);
    }
}

