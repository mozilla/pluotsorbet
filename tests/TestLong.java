package tests;

public class TestLong {
    static long a = 0x0123456789abcdefL;

    public static long x(long a, long b) {
	return a + b;
    }

    public static void main(String[] args) {
	if (x(1L, 2L) == 3L)
	    System.out.println("OK");
    }
}
