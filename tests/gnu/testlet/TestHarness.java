package gnu.testlet;

public abstract class TestHarness {
    public abstract void check(boolean ok);
    public abstract void debug(String msg);
    public abstract void setNote(String note);

    public void checkPoint(String note) {
	setNote(note);
    }

    public void check(boolean result, boolean expected) {
	boolean ok = (result == expected);
	check(ok);
	if (!ok)
	    debug("got (" + result + "), expected(" + expected + ")");
    }

    public void check(int result, int expected) {
	boolean ok = (result == expected);
	check(ok);
	if (!ok)
	    debug("got (" + result + "), expected(" + expected + ")");
    }

    public void check(boolean ok, String note) {
	setNote(note);
	check(ok);
    }

    public void check(boolean result, boolean expected, String note) {
	setNote(note);
	check(result, expected);
    }

    public void check(int result, int expected, String note) {
	setNote(note);
	check(result, expected);
    }

    public void fail(String note) {
	check(false, note);
    }
}
