public class TestBoolean extends Test {
	public void main() {
		check(Boolean.TRUE.booleanValue());
		check(!Boolean.FALSE.booleanValue());
		Boolean b = new Boolean(true);
		Boolean b2 = new Boolean(false);
		Boolean b3 = new Boolean(true);
		check(b.booleanValue());
		check(!b2.booleanValue());
		compare(b.toString(), "true");
		compare(b2.toString(), "false");
		compare(b.hashCode(), 1231);
		compare(b2.hashCode(), 1237);
		check(!b.equals(null));
		check(!b.equals(b2));
		check(b.equals(b3));
	}

    public static void main(String[] args) {
	(new TestBoolean()).main();
    }
}
