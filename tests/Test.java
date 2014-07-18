public abstract class Test {
    private int tests = 0;
    private int passed = 0;
    private long time = 0;

    public void check(boolean result) {
	tests++;
	if (result) {
	    passed++;
	} else {
	    System.out.println("Test " + tests + " failed");
	}
    }

    public void compare(long a, long b) {
	if (a == b) {
	    check(true);
	} else {
	    check(false);
	    System.out.println(a + "!=" + b);
	}
    }

    public void compare(double a, double b) {
	if (a == b) {
	    check(true);
	} else {
	    check(false);
	    System.out.println(a + "!=" + b);
	}
    }

    public void compare(Object a, Object b) {
	if (a.equals(b)) {
	    check(true);
	} else {
	    check(false);
	    System.out.println(a + "!=" + b);
	}
    }

    public void fail(Object a) {
	tests++;
    }

    public abstract void main();

    private void start() {
	long time = System.currentTimeMillis();
    }

    private void stop() {
	//System.out.println(System.currentTimeMillis() - time + "ms");
    }

    private boolean finish() {
	String result = "UNKNOWN";
	if (passed == tests) {
	    result = "SUCCESS";
	} else {
	    result = "FAIL";
	}
	System.out.println(result + " " + passed + "/" + tests);
	return passed == tests;
    }

    public boolean run() {
	start();
	main();
	stop();
	return finish();
    }
}
