import java.util.Date;

public class TestDate extends Test {
	public void main() {
		Date d = new Date();
		// This test is not reliable:
		// compare(d.getTime(), System.currentTimeMillis());
		Date d2 = new Date(4873984739798L);
		compare(d2.getTime(), 4873984739798L);
		check(!d.equals(d2));
		compare(d2.hashCode(), -803140168);
		check(d2.toString().indexOf("2124") != -1);
		d.setTime(4873984739798L);
		check(d.equals(d2));
	}

    public static void main(String[] args) {
	(new TestDate()).main();
    }
}
