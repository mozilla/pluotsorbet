public class TestInteger extends Test {
	public void main() {
		compare(Integer.toString(543), ("543"));
		compare(Integer.toString(-543), ("-543"));
		compare(Integer.toString(71, 36), ("1z"));
		compare(Integer.toString(-71, 36), ("-1z"));
		compare(Integer.toString(127, 2), ("1111111"));
		compare(Integer.toString(127, 664), ("127"));
		compare(Integer.toString(127, -44), ("127"));
		compare(Integer.toString(127, 0), ("127"));
		compare(Integer.toString(127, 1), ("127"));
		compare(Integer.toString(127, 1), ("127"));
		compare(Integer.toHexString(-71), ("ffffffb9"));
		compare(Integer.toOctalString(-71), ("37777777671"));
		compare(Integer.toBinaryString(-71), ("11111111111111111111111110111001"));
		compare(Integer.toString(6546456, 0), ("6546456"));
		try {
			Integer.parseInt(null, 10);
			check(false);
		} catch	(NumberFormatException e) {
			check(true);
		}
		try {
			Integer.parseInt("", 10);
			check(false);
		} catch	(NumberFormatException e) {
			check(true);
		}
		try {
			Integer.parseInt("1", 0);
			check(false);
		} catch	(NumberFormatException e) {
			check(true);
		}
		try {
			Integer.parseInt("1", 55);
			check(false);
		} catch	(NumberFormatException e) {
			check(true);
		}
		try {
			Integer.parseInt("test", 10);
			check(false);
		} catch	(NumberFormatException e) {
			check(true);
		}
		try {
			Integer.parseInt("11111111111111111", 0);
			check(false);
		} catch	(NumberFormatException e) {
			check(true);
		}
		compare(Integer.parseInt("-1z", 36), -71);
		compare(Integer.parseInt("0434444310", 10), 434444310);
		compare(Integer.parseInt("0434444310"), 434444310);
		compare(Integer.valueOf("-1z", 36).intValue(), -71);
		Integer i = new Integer(2147483647);
		Integer j = new Integer(-2147483648);
		compare(i.byteValue(), -1);
		compare(j.byteValue(), -0);
		compare(i.shortValue(), -1);
		compare(j.shortValue(), 0);
		compare(i.intValue(), 2147483647);
		compare(j.intValue(), -2147483648);
		compare(i.longValue(), 2147483647L);
		compare(j.longValue(), -2147483648L);
		compare(i.doubleValue(),2147483647.0);
		compare(j.doubleValue(), -2147483648.0);
		compare(i.hashCode(), 2147483647);
		compare(j.hashCode(), -2147483648);
		compare(i, new Integer(2147483647));
		check(!j.equals("-2147483648"));
	}
}
