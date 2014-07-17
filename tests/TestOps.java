package tests;

public class TestOps {
    public static void Assert(String test, boolean ok) {
	System.out.println(test + " " + (ok ? "PASS" : "FAIL"));
    }

    public static void main(String[] args) {
	Assert("Do asserts work", true);
    }
}

