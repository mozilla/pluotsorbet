package tests;

public class TestException {
    static void throw1() {
	try {
	    throw new RuntimeException("Hello, this is error 1");
	} catch (Exception e) {
	}
    }

    public static void main(String[] args) {
	throw1();
    }
}