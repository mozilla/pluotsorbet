public class TestDup extends Test {
    public void main() {
	// do it twice so we know the adding was done correctly in the first call
	compare(dup2(), 5);
	compare(dup2(), 6);

	DupMore d = new DupMore();
	compare(d.dup2_x1(), 4);
	compare(d.dup2_x1(), 5);

	compare(d.dup2_x2(), 1);
	compare(d.dup2_x2(), 2);

	compare(d.dup_x2(), 4);
	compare(d.dup_x2(), 5);
    }

    private static long longValue = 5;

    // this function generates the dup2 instruction
    public static long dup2() {
	return longValue++;
    }

    static class DupMore {
	private long longValue = 4;

	private long[] longArr = { 1 };

	// since this is not static, the 'this' operand causes javac to generate dup_x1
	public long dup2_x1() {
	    return longValue++;
	}

	void popLong(long a) {}

	// the array ref operand makes this a dup_x2
	public long dup2_x2() {
	    return longArr[0]++;
	}

	private static int intArr[] = { 4 };

	public static int dup_x2() {
	    return intArr[0]++;
	}
    }
}