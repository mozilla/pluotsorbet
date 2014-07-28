package gnu.testlet.vm;

import gnu.testlet.*;

public class ArrayTest implements Testlet {
    public void test(TestHarness th) {
	byte b1[][] = new byte[3][4];
	b1[1][2] = 5;
	th.check(b1[1][2] == 5);
	Object o[][] = new Object[5][5];
	o[1][1] = new Integer(5);
	th.check(o[1][1].toString().equals("5"));
	Object[] a = new Object[5];
	a[3] = "smt";
	th.check(a[3].equals("smt"));
	Object[][] b = new Object[6][];
	b[1] = new String[3];
	th.check(b[1][0] == null);
	th.check(b[0] == null);
    }
}