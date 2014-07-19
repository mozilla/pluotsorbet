public class TestArrays extends Test {
    public void main() {
	byte b1[][] = new byte[3][4];
	b1[1][2] = 5;
	check(b1[1][2] == 5);
	Object o[][] = new Object[5][5];
	o[1][1] = new Integer(5);
	check(o[1][1].toString().equals("5"));
	Object[] a = new Object[5];
	a[3] = "smt";
	check(a[3].equals("smt"));
	Object[][] b = new Object[6][];
	b[1] = new String[3];
	check(b[1][0] == null);
	check(b[0] == null);
    }

    public static void main(String[] args) {
	(new TestArrays()).main();
    }
}