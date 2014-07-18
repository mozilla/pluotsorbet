public class TestException extends Test {
    void throw1() {
	boolean caught = false;
	try {
	    throw new RuntimeException("Foo");
	} catch (Exception e) {
	    compare(e.getMessage(), "Foo");
	    caught = true;
	}
	check(caught);
    }

    public void main() {
	throw1();
    }
}