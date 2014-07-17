package tests;

public class TestOps {
    static void Assert(String test, boolean ok) {
	System.out.println(test + " " + (ok ? "PASS" : "FAIL"));
    }

    static int add(int a, int b) {
	return a + b;
    }

    public static void main(String[] args) {
	Assert("Do asserts work", true);
	Assert("add two ints", add(1, 2) == 3);
	Assert("overflow", add(0x7fffffff, 1) == -2147483648);
    }
}

