package midlets;

import javax.microedition.midlet.MIDlet;

public abstract class TestMidlet extends MIDlet {
	int tests = 0;
	int passed = 0;
	long time = 0;
	public void check(boolean result) {
		tests++;
		if (result) {
			System.out.println("Test " + tests + " passed");
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
	public void start() {
		time = System.currentTimeMillis();
	}
	public void stop() {
		System.out.println(System.currentTimeMillis() - time + "ms");
	}
	public void finish() {
		String result = "UNKNOWN";
		if (passed == tests) {
			result = "SUCCESS";
		} else {
			result = "FAIL";
		}
		System.out.println(result + " " + passed + "/" + tests);
	}
	public void pauseApp() {
	}
	public void destroyApp(boolean b) {
	}
}
