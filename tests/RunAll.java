public class RunAll {
    private static String[] tests = {"TestArrays", "TestOps", "TestLong", "TestException", "TestDup", "TestBoolean",
				     "TestByteArrayInputStream", "TestByteArrayOutputStream", "TestClass",
				     "TestDouble", "TestDataInputStream"};

    public static void main(String[] args) {
	boolean failed = false;
	for (int n = 0; n < tests.length; ++n) {
	    try {
		System.out.print(tests[n] + " ");
		Test test = (Test) Class.forName(tests[n]).newInstance();
		if (!test.run())
		    failed = true;
	    } catch (Exception e) {
		System.out.println("Failed to load " + tests[n]);
		failed = true;
	    }
	}
	if (failed)
	    System.out.println("FAILED");
    }
}
