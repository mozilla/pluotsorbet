public class TestConditions extends Test {
	public void main() {
		String a, b, c;
		a = "smt";
		b = a;
		c = "smt2";
		int d = 9;
		int e = -9;
		int f = 0;
		String g = null;
		check(a == b);
		check(a != c);
		check(a != null);
		check(g == null);
		check(d == 9);
		check(d > 7);
		check(d >= 9);
		check(d >= 7);
		check(7 < d);
		check(9 <= d);
		check(7 <= d);
		check(d != 6);
		check(d >= 0);
		check(e <= 0);
		check(f >= 0);
		check(f <= 0);
		check(f == 0);
		check(e != 0);
		check(d != 0);
	}
}
