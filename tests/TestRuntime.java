public class TestRuntime extends Test {
	public void main() {
		Runtime r = Runtime.getRuntime();
		check(r.freeMemory() < r.totalMemory());
		r.gc();
		// r.exit(99);
	}
}
