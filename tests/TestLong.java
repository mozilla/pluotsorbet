package tests;

public class TestLong {
    static long a = 0x0123456789abcdefL;

    static void Assert(String test, boolean ok) {
	System.out.println(test + " " + (ok ? "PASS" : "FAIL"));
    }

    public static void main(String[] args) {
	Assert("long to string", ("" + a).equals("81985529216486895"));
    }
}
