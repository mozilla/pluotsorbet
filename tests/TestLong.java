public class TestLong extends Test {
    static long h = 0x0123456789abcdefL;
    static long a = 5;
    static long b = 7;
    static long c = 50;

    public void main() {
        compare(("" + h), "81985529216486895");
	compare(a + b, 12L);
	compare(a - b, -2L);
	compare(a * b, 35L);
	compare(c / a, 10L);
	compare(c % b, 1L);
    }
}
