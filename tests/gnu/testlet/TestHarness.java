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

    public void check(long result, long expected) {
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

    public void check(long result, long expected, String note) {
	setNote(note);
	check(result, expected);
    }

    public void check(String result, String expected) {
	boolean ok = result.equals(expected);
	check(ok);
	if (!ok)
	    debug("got (" + result + "), expected(" + expected + ")");
    }
    
    public void pass() {
	check(true);
    }

    public void pass(String note) {
	check(true, note);
    }

    public void fail() {
	check(false);
    }

    public void fail(String note) {
	check(false, note);
    }
}
