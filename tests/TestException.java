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
	int i = 8;
	try {
	    i /= 0;
	} catch (Exception e) {
	    i++;
	}
	check(i == 9);
    }

    public void main() {
	throw1();
    }
}