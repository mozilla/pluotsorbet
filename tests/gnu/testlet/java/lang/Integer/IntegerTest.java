package gnu.testlet.java.lang.Integer;

import gnu.testlet.*;

public class IntegerTest implements Testlet {
	public void test(TestHarness th) {
		th.check(Integer.toString(543), ("543"));
		th.check(Integer.toString(-543), ("-543"));
		th.check(Integer.toString(71, 36), ("1z"));
		th.check(Integer.toString(-71, 36), ("-1z"));
		th.check(Integer.toString(127, 2), ("1111111"));
		th.check(Integer.toString(127, 664), ("127"));
		th.check(Integer.toString(127, -44), ("127"));
		th.check(Integer.toString(127, 0), ("127"));
		th.check(Integer.toString(127, 1), ("127"));
		th.check(Integer.toString(127, 1), ("127"));
		th.check(Integer.toHexString(-71), ("ffffffb9"));
		th.check(Integer.toOctalString(-71), ("37777777671"));
		th.check(Integer.toBinaryString(-71), ("11111111111111111111111110111001"));
		th.check(Integer.toString(6546456, 0), ("6546456"));
		try {
			Integer.parseInt(null, 10);
			th.check(false);
		} catch	(NumberFormatException e) {
			th.check(true);
		}
		try {
			Integer.parseInt("", 10);
			th.check(false);
		} catch	(NumberFormatException e) {
			th.check(true);
		}
		try {
			Integer.parseInt("1", 0);
			th.check(false);
		} catch	(NumberFormatException e) {
			th.check(true);
		}
		try {
			Integer.parseInt("1", 55);
			th.check(false);
		} catch	(NumberFormatException e) {
			th.check(true);
		}
		try {
			Integer.parseInt("test", 10);
			th.check(false);
		} catch	(NumberFormatException e) {
			th.check(true);
		}
		try {
			Integer.parseInt("11111111111111111", 0);
			th.check(false);
		} catch	(NumberFormatException e) {
			th.check(true);
		}
		th.check(Integer.parseInt("-1z", 36), -71);
		th.check(Integer.parseInt("0434444310", 10), 434444310);
		th.check(Integer.parseInt("0434444310"), 434444310);
		th.check(Integer.valueOf("-1z", 36).intValue(), -71);
		Integer i = new Integer(2147483647);
		Integer j = new Integer(-2147483648);
		th.check(i.byteValue(), -1);
		th.check(j.byteValue(), -0);
		th.check(i.shortValue(), -1);
		th.check(j.shortValue(), 0);
		th.check(i.intValue(), 2147483647);
		th.check(j.intValue(), -2147483648);
		th.check(i.longValue(), 2147483647L);
		th.check(j.longValue(), -2147483648L);
		th.check(i.doubleValue(),2147483647.0);
		th.check(j.doubleValue(), -2147483648.0);
		th.check(i.hashCode(), 2147483647);
		th.check(j.hashCode(), -2147483648);
		th.check(i, new Integer(2147483647));
		th.check(!j.equals("-2147483648"));
	}
}
