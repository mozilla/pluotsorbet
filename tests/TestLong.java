public class TestLong extends Test {
    static long a = 0x0123456789abcdefL;

    public void main() {
        compare(("" + a), "81985529216486895");
    }
}
